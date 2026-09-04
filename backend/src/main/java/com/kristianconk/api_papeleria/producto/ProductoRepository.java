package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.proveedor.Proveedor;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigoBarrasIgnoreCase(String codigoBarras);

    boolean existsByCodigoBarrasIgnoreCaseAndIdNot(String codigoBarras, Long id);

    Page<Producto> findByCategoriaAndActivoTrue(Categoria categoria, Pageable pageable);

    Page<Producto> findByProveedorAndActivoTrue(Proveedor proveedor, Pageable pageable);

    // Búsqueda avanzada con múltiples criterios
    @Query("SELECT p FROM Producto p WHERE " +
            "(:termino IS NULL OR :termino = '' OR " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(COALESCE(p.descripcion, '')) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :termino, '%'))) AND " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
            "(:proveedorId IS NULL OR p.proveedor.id = :proveedorId) AND " +
            "(:activo IS NULL OR p.activo = :activo) AND " +
            "(:precioMin IS NULL OR p.precioVenta >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precioVenta <= :precioMax) AND " +
            "(:soloStockBajo = FALSE OR (p.cantidadDesconocida = FALSE AND p.stockActual <= p.stockMinimo))")
    Page<Producto> buscarProductos(
            @Param("termino") String termino,
            @Param("categoriaId") Long categoriaId,
            @Param("proveedorId") Long proveedorId,
            @Param("activo") Boolean activo,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            @Param("soloStockBajo") boolean soloStockBajo,
            Pageable pageable);


    @Query("SELECT COUNT(p) FROM Producto p WHERE p.proveedor.id = :proveedorId")
    long countByProveedorId(@Param("proveedorId") Long proveedorId);

    @Modifying
    @Query("UPDATE Producto p SET p.proveedor = :proveedorPendiente WHERE p.proveedor.id = :proveedorId")
    int reasignarProveedor(
            @Param("proveedorId") Long proveedorId,
            @Param("proveedorPendiente") Proveedor proveedorPendiente);

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
            "WHERE LOWER(p.codigoBarras) = LOWER(:codigoBarras) AND p.activo = TRUE")
    Optional<Producto> findByCodigoBarrasActivoWithFotos(@Param("codigoBarras") String codigoBarras);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}

