package com.kristianconk.api_papeleria.producto.foto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record SubirFotoRequest(
        @NotNull(message = "El archivo es requerido")
        MultipartFile archivo,

        Boolean esPrincipal,

        Integer orden,

        String descripcion
) {}
