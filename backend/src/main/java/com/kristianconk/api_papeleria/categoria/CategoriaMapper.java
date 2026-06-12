package com.kristianconk.api_papeleria.categoria;

public class CategoriaMapper {

    private CategoriaMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static CategoriaResponseDTO toDto(final Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion());
    }

    public static Categoria toEntity(final CategoriaRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        final Categoria categoria = new Categoria();
        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());
        return categoria;
    }

    public static void updateEntity(final Categoria categoria, final CategoriaRequestDTO dto) {
        if (categoria != null && dto != null) {
            categoria.setNombre(dto.nombre());
            categoria.setDescripcion(dto.descripcion());
        }
    }
}
