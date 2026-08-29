package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.proveedor.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Búsqueda por código de barras (único)
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    // Búsqueda por categoría con paginación
    Page<Producto> findByCategoria(Categoria categoria, Pageable pageable);

    // Búsqueda por proveedor con paginación
    Page<Producto> findByProveedor(Proveedor proveedor, Pageable pageable);

    // Búsqueda por nombre exacto (para autocompletado)
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Búsqueda avanzada con múltiples criterios
    @Query("SELECT p FROM Producto p WHERE " +
            "(:termino IS NULL OR :termino = '' OR " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
            "(:proveedorId IS NULL OR p.proveedor.id = :proveedorId) AND " +
            "(:activo IS NULL OR p.activo = :activo)")
    Page<Producto> buscarProductos(
            @Param("termino") String termino,
            @Param("categoriaId") Long categoriaId,
            @Param("proveedorId") Long proveedorId,
            @Param("activo") Boolean activo,
            Pageable pageable);

    // Búsqueda por rango de precios
    @Query("SELECT p FROM Producto p WHERE " +
            "p.precioVenta BETWEEN :precioMin AND :precioMax")
    Page<Producto> findByRangoPrecio(
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            Pageable pageable);

    // Productos con stock bajo
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo")
    Page<Producto> findProductosStockBajo(Pageable pageable);

    // Productos por proveedor con filtro adicional
    @Query("SELECT p FROM Producto p WHERE p.proveedor.id = :proveedorId " +
            "AND (:termino IS NULL OR :termino = '' OR " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<Producto> buscarProductosPorProveedor(
            @Param("proveedorId") Long proveedorId,
            @Param("termino") String termino,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.proveedor.id = :proveedorId")
    long countByProveedorId(@Param("proveedorId") Long proveedorId);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId")
    long countByCategoriaId(@Param("categoriaId") final Long categoriaId);

    // Productos más vendidos (para reportes)
    @Query("SELECT p, SUM(dv.cantidad) as totalVendido " +
            "FROM Producto p JOIN DetalleVenta dv ON p.id = dv.producto.id " +
            "WHERE dv.venta.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
            "GROUP BY p ORDER BY totalVendido DESC")
    Page<Object[]> findProductosMasVendidos(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN FETCH p.fotos " +
            "WHERE p.id = :id")
    Optional<Producto> findByIdWithFotos(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN FETCH p.fotos " +
            "WHERE p.codigoBarras = :codigoBarras")
    Optional<Producto> findByCodigoBarrasWithFotos(@Param("codigoBarras") String codigoBarras);
}
