package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.storage.ByteArrayMultipartFile;
import com.kristianconk.api_papeleria.storage.StorageService;
import com.kristianconk.api_papeleria.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoFotoService {

    private final ProductoFotoRepository fotoRepository;
    private final ProductoRepository productoRepository;
    private final StorageService storageService;

    @Value("${app.storage-service.upload-path}")
    private String uploadPath;

    @Value("${app.fotos.thumbnail-size:200}")
    private int thumbnailSize;

    @Transactional
    public ProductoFotoDTO subirFoto(Long productoId, SubirFotoRequest request, Usuario usuario) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        MultipartFile archivo = request.archivo();
        String uuid = UUID.randomUUID().toString();
        String subDirectorio = "productos/" + productoId + "/" + uuid;

        // Validar tipo de archivo
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Guardar archivo original
        String rutaGuardada = storageService.store(archivo, subDirectorio);

        // Generar y guardar thumbnail
        try {
            generarYGuardarThumbnail(archivo, subDirectorio);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar thumbnail", e);
        }

        // Crear entidad
        ProductoFoto foto = new ProductoFoto();
        foto.setProducto(producto);
        foto.setNombreArchivo(archivo.getOriginalFilename());
        foto.setRutaArchivo(rutaGuardada);
        foto.setContentType(contentType);
        foto.setTamanio(archivo.getSize());
        foto.setOrden(request.orden() != null ? request.orden() : 0);
        foto.setDescripcion(request.descripcion());

        // Si es principal, resetear otras principales
        if (Boolean.TRUE.equals(request.esPrincipal())) {
            fotoRepository.resetPrincipalByProductoId(productoId);
            foto.setEsPrincipal(true);
        } else {
            // Si no hay principal, esta será la primera
            if (fotoRepository.countByProductoId(productoId) == 0) {
                foto.setEsPrincipal(true);
            }
        }

        ProductoFoto fotoGuardada = fotoRepository.save(foto);
        return mapToDTO(fotoGuardada, productoId);
    }

    @Transactional
    public void eliminarFoto(Long productoId, Long fotoId, Usuario usuario) {
        ProductoFoto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        if (!foto.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La foto no pertenece al producto especificado");
        }

        // Eliminar archivos del storage
        storageService.delete(foto.getRutaArchivo());

        // Eliminar thumbnail
        String thumbnailPath = foto.getRutaArchivo().replaceFirst("(\\.[^.]+)$", "_thumbnail$1");
        try {
            storageService.delete(thumbnailPath);
        } catch (Exception e) {
            // Log error pero continuar
        }

        boolean eraPrincipal = foto.getEsPrincipal();
        fotoRepository.delete(foto);

        // Si era la principal, asignar otra como principal
        if (eraPrincipal) {
            fotoRepository.findByProductoIdOrderByOrdenAsc(productoId)
                    .stream()
                    .findFirst()
                    .ifPresent(nuevaPrincipal -> {
                        nuevaPrincipal.setEsPrincipal(true);
                        fotoRepository.save(nuevaPrincipal);
                    });
        }
    }

    @Transactional(readOnly = true)
    public List<ProductoFotoDTO> listarFotos(Long productoId) {
        return fotoRepository.findByProductoIdOrderByOrdenAsc(productoId)
                .stream()
                .map(foto -> mapToDTO(foto, productoId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoFotoDTO establecerFotoPrincipal(Long productoId, Long fotoId, Usuario usuario) {
        ProductoFoto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        if (!foto.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La foto no pertenece al producto especificado");
        }

        fotoRepository.resetPrincipalByProductoId(productoId);
        foto.setEsPrincipal(true);

        return mapToDTO(fotoRepository.save(foto), productoId);
    }

    public Resource descargarFoto(Long productoId, Long fotoId, Integer size) {
        ProductoFoto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        if (!foto.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La foto no pertenece al producto especificado");
        }

        String ruta = foto.getRutaArchivo();

        // Si se solicita thumbnail y existe tamaño
        if (size != null && size > 0) {
            String thumbnailPath = ruta.replaceFirst("(\\.[^.]+)$", "_thumbnail$1");
            try {
                return storageService.loadAsResource(thumbnailPath);
            } catch (Exception e) {
                // Si no existe thumbnail, devolver original
                return storageService.loadAsResource(ruta);
            }
        }

        return storageService.loadAsResource(ruta);
    }

    private void generarYGuardarThumbnail(MultipartFile archivo, String subDirectorio) throws IOException {
        BufferedImage imagenOriginal = ImageIO.read(archivo.getInputStream());

        BufferedImage thumbnail = Thumbnails.of(imagenOriginal)
                .size(thumbnailSize, thumbnailSize)
                .keepAspectRatio(true)
                .asBufferedImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String extension = archivo.getOriginalFilename()
                .substring(archivo.getOriginalFilename().lastIndexOf(".") + 1);

        ImageIO.write(thumbnail, extension, baos);

        // Crear MultipartFile temporal para el thumbnail
        String thumbnailNombre = archivo.getOriginalFilename()
                .replaceFirst("(\\.[^.]+)$", "_thumbnail$1");

        // Usar el mismo storage service pero con el thumbnail
        // Nota: Necesitarías modificar storageService o crear un método específico
        MultipartFile thumbnailFile = new ByteArrayMultipartFile(
                thumbnailNombre,
                thumbnailNombre,
                archivo.getContentType(),
                baos.toByteArray()
        );

        storageService.store(thumbnailFile, subDirectorio);
    }

    private ProductoFotoDTO mapToDTO(ProductoFoto foto, Long productoId) {
        String baseUrl = "/api/v1/productos/" + productoId + "/fotos/" + foto.getId();
        return new ProductoFotoDTO(
                foto.getId(),
                foto.getNombreArchivo(),
                foto.getContentType(),
                foto.getTamanio(),
                foto.getEsPrincipal(),
                foto.getOrden(),
                foto.getFechaSubida(),
                baseUrl,
                baseUrl + "?size=" + thumbnailSize
        );
    }
}
