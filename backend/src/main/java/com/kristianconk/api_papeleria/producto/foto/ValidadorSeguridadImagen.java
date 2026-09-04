package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.error.ArchivoDemasiadoGrandeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

@Component
public class ValidadorSeguridadImagen {

    public static final long MAX_FILE_SIZE = 4L * 1024 * 1024; // 4 MB exactos
    public static final int MAX_DIMENSION = 4096;
    public static final long MAX_PIXELS = 16_000_000L; // 16 Megapíxeles (prevención bomba de descompresión)

    public record InfoImagenValidada(
            String formato,
            int ancho,
            int alto,
            String extension,
            String mimeType,
            byte[] contenido,
            String nombreSanitizado
    ) {}

    public InfoImagenValidada validar(final MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        }

        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new ArchivoDemasiadoGrandeException("El archivo excede el tamaño máximo permitido de 4 MB");
        }

        final byte[] bytes;
        try {
            bytes = archivo.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el contenido del archivo", e);
        }

        // Sanitización de nombre de archivo contra Path Traversal
        final String nombreOriginal = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "foto.jpg";
        final String nombreSanitizado = sanitizarNombreArchivo(nombreOriginal);

        // Validación de magic bytes (firma real en binario)
        final String formato = detectarFormatoPorFirma(bytes);
        if (formato == null) {
            throw new IllegalArgumentException("Formato o firma de archivo no permitido. Solo se aceptan JPEG, PNG y WEBP válidos");
        }

        // Detección y rechazo de SVG (falsificación de extensión)
        if (esPosibleSvg(bytes)) {
            throw new IllegalArgumentException("El formato SVG no está permitido por motivos de seguridad");
        }

        // Inspección de dimensiones y cantidad total de píxeles antes de decodificación completa
        try (final ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (iis == null) {
                throw new IllegalArgumentException("El archivo no es una imagen válida o está truncado");
            }

            final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("No se pudo decodificar el formato de la imagen");
            }

            final ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, false);

                final int ancho = reader.getWidth(0);
                final int alto = reader.getHeight(0);

                if (ancho <= 0 || alto <= 0) {
                    throw new IllegalArgumentException("Dimensiones de imagen inválidas o archivo truncado");
                }

                if (ancho > MAX_DIMENSION || alto > MAX_DIMENSION || ((long) ancho * alto > MAX_PIXELS)) {
                    throw new IllegalArgumentException("La imagen excede las dimensiones máximas permitidas (máximo 4096x4096 px o 16 MP)");
                }

                // Evitar archivos con múltiples imágenes (secuencias animadas / multi-frame no autorizados)
                int numImagenes = 1;
                try {
                    numImagenes = reader.getNumImages(true);
                } catch (Exception ignored) {
                }

                if (numImagenes > 1) {
                    throw new IllegalArgumentException("No se permiten archivos con múltiples imágenes o secuencias animadas");
                }

                final String extension = formato.equalsIgnoreCase("JPEG") ? "jpg" : formato.toLowerCase();
                final String mimeType = formato.equalsIgnoreCase("JPEG") ? "image/jpeg" : "image/" + formato.toLowerCase();

                return new InfoImagenValidada(formato, ancho, alto, extension, mimeType, bytes, nombreSanitizado);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al inspeccionar la imagen: " + e.getMessage(), e);
        }
    }

    public static String sanitizarNombreArchivo(final String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "imagen.jpg";
        }
        // Extraer únicamente el nombre base sin rutas
        String base = Paths.get(nombre).getFileName().toString();
        // Eliminar caracteres de path traversal
        base = base.replace("..", "").replace("/", "").replace("\\", "").trim();
        return base.isEmpty() ? "imagen.jpg" : base;
    }

    private static String detectarFormatoPorFirma(final byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return null;
        }

        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "JPEG";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((bytes[0] & 0xFF) == 0x89 && (bytes[1] & 0xFF) == 0x50 && (bytes[2] & 0xFF) == 0x4E && (bytes[3] & 0xFF) == 0x47
                && (bytes[4] & 0xFF) == 0x0D && (bytes[5] & 0xFF) == 0x0A && (bytes[6] & 0xFF) == 0x1A && (bytes[7] & 0xFF) == 0x0A) {
            return "PNG";
        }

        // WEBP: RIFF....WEBP (52 49 46 46 .... 57 45 42 50)
        if ((bytes[0] & 0xFF) == 0x52 && (bytes[1] & 0xFF) == 0x49 && (bytes[2] & 0xFF) == 0x46 && (bytes[3] & 0xFF) == 0x46
                && (bytes[8] & 0xFF) == 0x57 && (bytes[9] & 0xFF) == 0x45 && (bytes[10] & 0xFF) == 0x42 && (bytes[11] & 0xFF) == 0x50) {
            return "WEBP";
        }

        return null;
    }

    private static boolean esPosibleSvg(final byte[] bytes) {
        final int longitud = Math.min(bytes.length, 1024);
        final String inicio = new String(bytes, 0, longitud).toLowerCase();
        return inicio.contains("<svg") || inicio.contains("<?xml") || inicio.contains("<!doctype svg");
    }
}
