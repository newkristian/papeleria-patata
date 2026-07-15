package com.kristianconk.api_papeleria.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequestDTO(
        @NotBlank(message = "La contraseña anterior no puede estar vacía")
        String oldPassword,

        @NotBlank(message = "La nueva contraseña no puede estar vacía")
        @Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
                message = "La nueva contraseña debe contener mayúsculas, minúsculas y números"
        )
        String newPassword
) {
}
