package com.kristianconk.api_papeleria.producto.foto;

import java.time.LocalDateTime;

public record ProductoFotoDTO(
        Long id,
        String nombreArchivo,
        String contentType,
        Long tamanio,
        Boolean esPrincipal,
        Integer orden,
        LocalDateTime fechaSubida,
        String url,
        String urlThumbnail
) {}
