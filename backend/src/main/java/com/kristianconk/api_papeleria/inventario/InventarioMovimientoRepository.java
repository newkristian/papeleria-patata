package com.kristianconk.api_papeleria.inventario;

import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {

    @Query("SELECT im FROM InventarioMovimiento im WHERE " +
           "(:productoId IS NULL OR im.producto.id = :productoId) AND " +
           "(:tipo IS NULL OR im.tipo = :tipo) AND " +
           "(:usuarioId IS NULL OR im.usuario.id = :usuarioId) AND " +
           "(:fechaInicio IS NULL OR im.fechaMovimiento >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR im.fechaMovimiento <= :fechaFin)")
    Page<InventarioMovimiento> buscarMovimientos(
            @Param("productoId") Long productoId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);
}
