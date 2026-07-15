package com.kristianconk.api_papeleria.usuario;

import com.kristianconk.api_papeleria.tienda.Tienda;

public class UsuarioMapper {

    private UsuarioMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static UsuarioResponseDTO toDto(final Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getTienda() != null ? usuario.getTienda().getId() : null,
                usuario.getTienda() != null ? usuario.getTienda().getNombre() : null,
                usuario.isActivo(),
                usuario.isRequiereCambioPassword()
        );
    }

    public static Usuario toEntity(
            final UsuarioCreateRequestDTO dto,
            final Tienda tienda,
            final String encodedPassword) {
        if (dto == null) {
            return null;
        }
        return Usuario.builder()
                .withUsername(dto.email())
                .withEmail(dto.email())
                .withNombre(dto.nombre())
                .withApellidos(dto.apellidos())
                .withRol(dto.rol())
                .withPassword(encodedPassword)
                .withTienda(tienda)
                .withActivo(true)
                .withRequiereCambioPassword(true)
                .build();
    }

    public static void updateEntity(
            final Usuario usuario,
            final UsuarioUpdateRequestDTO dto,
            final Tienda tienda) {
        if (usuario != null && dto != null) {
            usuario.setNombre(dto.nombre());
            usuario.setApellidos(dto.apellidos());
            usuario.setEmail(dto.email());
            usuario.setUsername(dto.email());
            usuario.setRol(dto.rol());
            usuario.setTienda(tienda);
        }
    }
}
