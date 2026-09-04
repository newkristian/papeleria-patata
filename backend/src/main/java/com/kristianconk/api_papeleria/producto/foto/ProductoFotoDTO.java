package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.enums.EstadoProcesamientoFoto;
import java.time.LocalDateTime;

public record ProductoFotoDTO(
        Long id,
        String nombreArchivo,
        String contentType,
        Long tamanio,
        Boolean esPrincipal,
        Integer orden,
        LocalDateTime fechaSubida,
        String urlOriginal,          // URL para la imagen normalizada
        String urlThumbnail,         // URL para el thumbnail
        EstadoProcesamientoFoto estadoProcesamiento,
        String mensajeError
) {}
