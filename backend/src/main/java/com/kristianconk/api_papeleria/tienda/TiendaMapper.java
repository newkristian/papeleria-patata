package com.kristianconk.api_papeleria.tienda;

public class TiendaMapper {

    private TiendaMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static TiendaResponseDTO toDto(final Tienda tienda) {
        if (tienda == null) {
            return null;
        }
        return new TiendaResponseDTO(
                tienda.getId(),
                tienda.getNombre(),
                tienda.getDireccion(),
                tienda.getTelefono(),
                tienda.getEmail());
    }

    public static Tienda toEntity(final TiendaRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        final Tienda tienda = new Tienda();
        tienda.setNombre(dto.nombre());
        tienda.setDireccion(dto.direccion());
        tienda.setTelefono(dto.telefono());
        tienda.setEmail(dto.email());

        return tienda;
    }

    public static void updateEntity(final Tienda tienda, final TiendaRequestDTO dto) {
        if (tienda != null && dto != null) {
            tienda.setNombre(dto.nombre());
            tienda.setDireccion(dto.direccion());
            tienda.setTelefono(dto.telefono());
            tienda.setEmail(dto.email());
        }
    }
}
