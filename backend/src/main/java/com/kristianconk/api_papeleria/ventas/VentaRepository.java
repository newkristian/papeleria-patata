package com.kristianconk.api_papeleria.ventas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByUsuarioId(Long usuarioId);
    List<Venta> findByClienteId(Long clienteId);
    List<Venta> findByTiendaId(Long tiendaId);
    List<Venta> findByVentaAnonimaTrue();  // Ventas sin cliente registrado
    List<Venta> findByVentaAnonimaFalse(); // Ventas con cliente registrado
    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT v FROM Venta v WHERE v.tienda.id = :tiendaId AND DATE(v.fechaVenta) = :fecha")
    List<Venta> findVentasDelDia(@Param("tiendaId") Long tiendaId, @Param("fecha") LocalDate fecha);

    @Query("SELECT p, SUM(dv.subtotal) FROM DetalleVenta dv JOIN dv.producto p WHERE p.proveedor.id = :proveedorId AND dv.venta.estado = 'COMPLETADA' GROUP BY p")
    List<Object[]> getVentasPorProveedor(@Param("proveedorId") Long proveedorId);

    // Estadísticas de clientes vs anónimos
    @Query("SELECT COUNT(v), SUM(v.total) FROM Venta v WHERE v.ventaAnonima = true AND v.fechaVenta BETWEEN :inicio AND :fin")
    Object[] getEstadisticasVentasAnonimas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v), SUM(v.total) FROM Venta v WHERE v.ventaAnonima = false AND v.fechaVenta BETWEEN :inicio AND :fin")
    Object[] getEstadisticasVentasRegistradas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
