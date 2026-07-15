package com.kristianconk.api_papeleria.usuario;

import com.kristianconk.api_papeleria.enums.RolUsuario;

public record UsuarioResponseDTO(
        Long id,
        String username,
        String nombre,
        String apellidos,
        String email,
        RolUsuario rol,
        Long tiendaId,
        String tiendaNombre,
        boolean activo,
        boolean requiereCambioPassword
) {
}
