package com.kristianconk.api_papeleria.promocion;

public class PromocionMapper {

    private PromocionMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static PromocionResponseDTO toDto(final Promocion promocion) {
        if (promocion == null) {
            return null;
        }
        return new PromocionResponseDTO(
                promocion.getId(),
                promocion.getNombre(),
                promocion.getDescripcion(),
                promocion.getTipo(),
                promocion.isActiva(),
                promocion.getProducto() != null ? promocion.getProducto().getId() : null,
                promocion.getProducto() != null ? promocion.getProducto().getNombre() : null,
                promocion.getCategoria() != null ? promocion.getCategoria().getId() : null,
                promocion.getCategoria() != null ? promocion.getCategoria().getNombre() : null,
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.getPrioridad(),
                toReglaDto(promocion.getReglaDescuentoPorCantidad()),
                promocion.getFechaCreacion(),
                promocion.getFechaActualizacion());
    }

    public static Promocion toEntity(final PromocionRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        final Promocion promocion = new Promocion();
        aplicarCamposBasicos(promocion, dto);
        return promocion;
    }

    public static void updateEntity(final Promocion promocion, final PromocionRequestDTO dto) {
        if (promocion != null && dto != null) {
            aplicarCamposBasicos(promocion, dto);
        }
    }

    private static void aplicarCamposBasicos(final Promocion promocion, final PromocionRequestDTO dto) {
        promocion.setNombre(dto.nombre());
        promocion.setDescripcion(dto.descripcion());
        promocion.setTipo(dto.tipo());
        promocion.setActiva(dto.activa());
        promocion.setFechaInicio(dto.fechaInicio());
        promocion.setFechaFin(dto.fechaFin());
        promocion.setPrioridad(dto.prioridad());
    }

    private static ReglaDescuentoPorCantidadDTO toReglaDto(final ReglaDescuentoPorCantidad regla) {
        if (regla == null) {
            return null;
        }
        return new ReglaDescuentoPorCantidadDTO(regla.getCantidadMinima(), regla.getPorcentaje());
    }
}
