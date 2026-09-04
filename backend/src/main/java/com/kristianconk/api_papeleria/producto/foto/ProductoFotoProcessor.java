package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.enums.EstadoProcesamientoFoto;
import com.kristianconk.api_papeleria.storage.ByteArrayMultipartFile;
import com.kristianconk.api_papeleria.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoFotoProcessor {

    private final ProductoFotoRepository fotoRepository;
    private final StorageService storageService;

    @Async("fotoProcessingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarFotoAsincrona(Long fotoId, Path rutaArchivoTemporal, String extension) {
        log.info("[POS/ProductoFotoProcessor] - PROCESAR_FOTO: iniciando para fotoId: {}", fotoId);

        ProductoFoto foto = fotoRepository.findById(fotoId).orElse(null);
        if (foto == null) {
            log.error("[POS/ProductoFotoProcessor] - Foto no encontrada con id: {}", fotoId);
            limpiarTemporal(rutaArchivoTemporal);
            return;
        }

        foto.setEstadoProcesamiento(EstadoProcesamientoFoto.PROCESANDO);
        fotoRepository.save(foto);

        String rutaPrincipalGuardada = null;
        String rutaMiniaturaGuardada = null;

        try {
            byte[] bytesOriginales = Files.readAllBytes(rutaArchivoTemporal);
            BufferedImage imgOriginal = ImageIO.read(new ByteArrayInputStream(bytesOriginales));
            if (imgOriginal == null) {
                throw new IllegalArgumentException("No se pudo decodificar la imagen original");
            }

            String subDirectorio = "productos/" + foto.getProducto().getId() + "/" + foto.getId();
            String formatoSalida = extension.equalsIgnoreCase("png") ? "png" : "jpg";
            String mimeType = formatoSalida.equals("png") ? "image/png" : "image/jpeg";

            // 1. Normalización: máximo 512 x 512 manteniendo proporción
            ByteArrayOutputStream osNormalizada = new ByteArrayOutputStream();
            Thumbnails.of(imgOriginal)
                    .size(512, 512)
                    .outputFormat(formatoSalida)
                    .toOutputStream(osNormalizada);
            byte[] bytesNormalizados = osNormalizada.toByteArray();

            BufferedImage imgNormalizada = ImageIO.read(new ByteArrayInputStream(bytesNormalizados));

            ByteArrayMultipartFile archivoNormalizado = new ByteArrayMultipartFile(
                    "foto", "foto." + formatoSalida, mimeType, bytesNormalizados);
            rutaPrincipalGuardada = storageService.store(archivoNormalizado, subDirectorio);

            // 2. Miniatura: 80 x 80 con recorte centrado (center crop)
            ByteArrayOutputStream osMiniatura = new ByteArrayOutputStream();
            Thumbnails.of(imgOriginal)
                    .size(80, 80)
                    .crop(Positions.CENTER)
                    .outputFormat(formatoSalida)
                    .toOutputStream(osMiniatura);
            byte[] bytesMiniatura = osMiniatura.toByteArray();

            ByteArrayMultipartFile archivoMiniatura = new ByteArrayMultipartFile(
                    "miniatura", "thumb_80." + formatoSalida, mimeType, bytesMiniatura);
            rutaMiniaturaGuardada = storageService.store(archivoMiniatura, subDirectorio);

            // 3. Actualizar entidad a COMPLETADO
            foto.setRutaArchivo(rutaPrincipalGuardada);
            foto.setRutaMiniatura(rutaMiniaturaGuardada);
            foto.setContentType(mimeType);
            foto.setTamanio((long) bytesNormalizados.length);
            foto.setAncho(imgNormalizada != null ? imgNormalizada.getWidth() : 512);
            foto.setAlto(imgNormalizada != null ? imgNormalizada.getHeight() : 512);
            foto.setEstadoProcesamiento(EstadoProcesamientoFoto.COMPLETADO);
            foto.setMensajeError(null);
            fotoRepository.save(foto);

            log.info("[POS/ProductoFotoProcessor] - Foto procesada con éxito: fotoId: {}", fotoId);
        } catch (Exception e) {
            log.error("[POS/ProductoFotoProcessor] - Error al procesar fotoId: {}", fotoId, e);
            if (rutaPrincipalGuardada != null) {
                try {
                    storageService.delete(rutaPrincipalGuardada);
                } catch (Exception ignored) {
                }
            }
            if (rutaMiniaturaGuardada != null) {
                try {
                    storageService.delete(rutaMiniaturaGuardada);
                } catch (Exception ignored) {
                }
            }

            foto.setEstadoProcesamiento(EstadoProcesamientoFoto.ERROR);
            foto.setMensajeError("Error durante el procesamiento de la imagen: " + e.getMessage());
            fotoRepository.save(foto);
        } finally {
            limpiarTemporal(rutaArchivoTemporal);
        }
    }

    private void limpiarTemporal(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("[POS/ProductoFotoProcessor] - No se pudo eliminar archivo temporal: {}", path);
            }
        }
    }
}
