package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.enums.EstadoProcesamientoFoto;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.storage.StorageService;
import com.kristianconk.api_papeleria.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoFotoService {

    private final ProductoFotoRepository fotoRepository;
    private final ProductoRepository productoRepository;
    private final StorageService storageService;
    private final ValidadorSeguridadImagen validadorSeguridad;
    private final ProductoFotoProcessor fotoProcessor;

    @Value("${app.storage-service.upload-path}")
    private String uploadPath;

    @Value("${app.fotos.thumbnail-size:80}")
    private int thumbnailSize;

    @Transactional
    public ProductoFotoDTO subirFoto(Long productoId, SubirFotoRequest request, Usuario usuario) {
        log.info("[POS/ProductoFotoService] - SUBIR_FOTO: productoId={}, usuario={}", productoId, usuario != null ? usuario.getUsername() : "anon");

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        MultipartFile archivo = request.archivo();

        // 1. Validación exhaustiva de seguridad (tamaño 4MB, magic bytes, SVG, dimensiones, descompresión bomb)
        ValidadorSeguridadImagen.InfoImagenValidada info = validadorSeguridad.validar(archivo);

        // 2. Guardar archivo temporal usando UUID del servidor (sin usar nombres proporcionados por cliente)
        Path tempFile;
        try {
            tempFile = Files.createTempFile("foto_upload_" + UUID.randomUUID() + "_", "." + info.extension());
            Files.write(tempFile, info.contenido());
        } catch (IOException e) {
            log.error("[POS/ProductoFotoService] - Error al crear archivo temporal para procesamiento", e);
            throw new RuntimeException("Error interno al preparar archivo para procesamiento", e);
        }

        // 3. Crear entidad en estado PENDIENTE
        ProductoFoto foto = new ProductoFoto();
        foto.setProducto(producto);
        foto.setNombreArchivo(info.nombreSanitizado());
        foto.setRutaArchivo(""); // Se actualizará al completar procesamiento asíncrono
        foto.setContentType(info.mimeType());
        foto.setTamanio((long) info.contenido().length);
        foto.setAncho(info.ancho());
        foto.setAlto(info.alto());
        foto.setOrden(request.orden() != null ? request.orden() : 0);
        foto.setDescripcion(request.descripcion());
        foto.setEstadoProcesamiento(EstadoProcesamientoFoto.PENDIENTE);
        foto.setMensajeError(null);

        // Si es principal o es la primera foto
        if (Boolean.TRUE.equals(request.esPrincipal())) {
            fotoRepository.resetPrincipalByProductoId(productoId);
            foto.setEsPrincipal(true);
        } else {
            if (fotoRepository.countByProductoId(productoId) == 0) {
                foto.setEsPrincipal(true);
            } else {
                foto.setEsPrincipal(false);
            }
        }

        ProductoFoto fotoGuardada = fotoRepository.save(foto);

        // 4. Encolar procesamiento asíncrono seguro
        fotoProcessor.procesarFotoAsincrona(fotoGuardada.getId(), tempFile, info.extension());

        return mapToDTO(fotoGuardada, productoId);
    }

    @Transactional(readOnly = true)
    public ProductoFotoDTO consultarEstadoFoto(Long productoId, Long fotoId) {
        ProductoFoto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        if (!foto.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La foto no pertenece al producto especificado");
        }

        return mapToDTO(foto, productoId);
    }

    @Transactional
    public ProductoFotoDTO reintentarProcesamiento(Long productoId, Long fotoId, Usuario usuario) {
        log.info("[POS/ProductoFotoService] - REINTENTAR_FOTO: productoId={}, fotoId={}, usuario={}", productoId, fotoId, usuario != null ? usuario.getUsername() : "anon");

        ProductoFoto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        if (!foto.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La foto no pertenece al producto especificado");
        }

        if (foto.getEstadoProcesamiento() != EstadoProcesamientoFoto.ERROR) {
            throw new IllegalStateException("Solo se pueden reintentar fotografías en estado ERROR");
        }

        // Si ya tenía rutaArchivo existente pero falló, verificar si existe para reintentar
        if (foto.getRutaArchivo() == null || foto.getRutaArchivo().isBlank()) {
            throw new IllegalStateException("No existe archivo original almacenado para reintentar. Debe subirse nuevamente.");
        }

        try {
            Resource recursoOriginal = storageService.loadAsResource(foto.getRutaArchivo());
            Path tempFile = Files.createTempFile("foto_retry_" + UUID.randomUUID() + "_", ".tmp");
            Files.copy(recursoOriginal.getInputStream(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            foto.setEstadoProcesamiento(EstadoProcesamientoFoto.PENDIENTE);
            foto.setMensajeError(null);
            foto = fotoRepository.save(foto);

            String extension = "jpg";
            if (foto.getContentType() != null && foto.getContentType().contains("png")) {
                extension = "png";
            }

            fotoProcessor.procesarFotoAsincrona(foto.getId(), tempFile, extension);
            return mapToDTO(foto, productoId);
        } catch (Exception e) {
            log.error("[POS/ProductoFotoService] - Error al iniciar reintento para fotoId: {}", fotoId, e);
            throw new RuntimeException("Error al preparar reintento de procesamiento", e);
        }
    }

    @Transactional
    public void eliminarFoto(Long productoId, Long fotoId, Usuario usuario) {
        log.info("[POS/ProductoFotoService] - ELIMINAR_FOTO: productoId={}, fotoId={}, usuario={}", productoId, fotoId, usuario != null ? usuario.getUsername() : "anon");

        ProductoFoto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        if (!foto.getProducto().getId().equals(productoId)) {
            throw new IllegalArgumentException("La foto no pertenece al producto especificado");
        }

        // Eliminar archivo principal del storage si existe
        if (foto.getRutaArchivo() != null && !foto.getRutaArchivo().isBlank()) {
            try {
                storageService.delete(foto.getRutaArchivo());
            } catch (Exception e) {
                log.warn("[POS/ProductoFotoService] - Error al eliminar archivo principal: {}", foto.getRutaArchivo());
            }
        }

        // Eliminar miniatura del storage si existe
        if (foto.getRutaMiniatura() != null && !foto.getRutaMiniatura().isBlank()) {
            try {
                storageService.delete(foto.getRutaMiniatura());
            } catch (Exception e) {
                log.warn("[POS/ProductoFotoService] - Error al eliminar miniatura: {}", foto.getRutaMiniatura());
            }
        }

        boolean eraPrincipal = Boolean.TRUE.equals(foto.getEsPrincipal());
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
        log.info("[POS/ProductoFotoService] - ESTABLECER_PRINCIPAL: productoId={}, fotoId={}, usuario={}", productoId, fotoId, usuario != null ? usuario.getUsername() : "anon");

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

        // Si se solicita tamaño miniatura (<= 100) y tenemos rutaMiniatura
        if (size != null && size > 0 && size <= 100 && foto.getRutaMiniatura() != null && !foto.getRutaMiniatura().isBlank()) {
            try {
                return storageService.loadAsResource(foto.getRutaMiniatura());
            } catch (Exception e) {
                log.warn("[POS/ProductoFotoService] - No se pudo cargar miniatura, usando original: {}", foto.getRutaMiniatura());
            }
        }

        if (foto.getRutaArchivo() == null || foto.getRutaArchivo().isBlank()) {
            throw new ResourceNotFoundException("La fotografía aún se encuentra en procesamiento o no tiene archivo disponible");
        }

        return storageService.loadAsResource(foto.getRutaArchivo());
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
                baseUrl + "?size=" + thumbnailSize,
                foto.getEstadoProcesamiento(),
                foto.getMensajeError()
        );
    }
}

