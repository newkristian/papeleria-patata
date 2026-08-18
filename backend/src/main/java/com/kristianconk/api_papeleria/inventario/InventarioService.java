package com.kristianconk.api_papeleria.inventario;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final ProductoRepository productoRepository;

    private static final double COSTO_BAJO = 50.0;
    private static final double COSTO_MEDIO = 200.0;
    private static final double PORCENTAJE_BAJO = 50.0;
    private static final double PORCENTAJE_MEDIO = 40.0;
    private static final double PORCENTAJE_ALTO = 30.0;

    public InventarioMovimiento registrarEntrada(final InventarioMovimientoRequestDTO request, final Usuario usuario) {
        log.info("[POS/InventarioService] - REGISTRAR_ENTRADA: productoId: {}, cantidad: {}, userId: {}", 
                request.productoId(), request.cantidad(), usuario.getId());

        verificarPermisos(usuario, "registrar entrada");

        final Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> {
                    log.error("[POS/InventarioService] - REGISTRAR_ENTRADA: productoId: {} no encontrado, userId: {}", request.productoId(), usuario.getId());
                    return new ResourceNotFoundException("Producto no encontrado");
                });

        if (!producto.isActivo()) {
            log.error("[POS/InventarioService] - REGISTRAR_ENTRADA: productoId: {} no activo, userId: {}", request.productoId(), usuario.getId());
            throw new IllegalArgumentException("El producto no está activo");
        }

        // Actualizar stock
        producto.setStockActual(producto.getStockActual() + request.cantidad());

        // Actualizar costo de compra y porcentaje de ganancia si el nuevo costo es mayor
        if (request.costoUnitario() > producto.getCostoCompra()) {
            log.info("[POS/InventarioService] - REGISTRAR_ENTRADA: actualizando costo de compra de {} a {}, userId: {}", 
                    producto.getCostoCompra(), request.costoUnitario(), usuario.getId());
            producto.setCostoCompra(request.costoUnitario());
            producto.setPorcentajeGanancia(calcularPorcentajeGanancia(request.costoUnitario()));
        }

        productoRepository.save(producto);

        // Crear movimiento
        final InventarioMovimiento movimiento = new InventarioMovimiento();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(request.cantidad());
        movimiento.setMotivo(request.motivo());
        movimiento.setCostoUnitario(request.costoUnitario());

        final InventarioMovimiento guardado = inventarioMovimientoRepository.save(movimiento);
        log.info("[POS/InventarioService] - REGISTRAR_ENTRADA: entrada registrada con éxito, movimientoId: {}, userId: {}", guardado.getId(), usuario.getId());
        return guardado;
    }

    public InventarioMovimiento registrarSalida(final InventarioMovimientoRequestDTO request, final Usuario usuario) {
        log.info("[POS/InventarioService] - REGISTRAR_SALIDA: productoId: {}, cantidad: {}, userId: {}", 
                request.productoId(), request.cantidad(), usuario.getId());

        verificarPermisos(usuario, "registrar salida");

        final Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> {
                    log.error("[POS/InventarioService] - REGISTRAR_SALIDA: productoId: {} no encontrado, userId: {}", request.productoId(), usuario.getId());
                    return new ResourceNotFoundException("Producto no encontrado");
                });

        if (!producto.isActivo()) {
            log.error("[POS/InventarioService] - REGISTRAR_SALIDA: productoId: {} no activo, userId: {}", request.productoId(), usuario.getId());
            throw new IllegalArgumentException("El producto no está activo");
        }

        if (!producto.isCantidadDesconocida() && producto.getStockActual() < request.cantidad()) {
            log.error("[POS/InventarioService] - REGISTRAR_SALIDA: stock insuficiente para productoId: {}, stock actual: {}, solicitado: {}, userId: {}", 
                    request.productoId(), producto.getStockActual(), request.cantidad(), usuario.getId());
            throw new IllegalArgumentException("Stock insuficiente para realizar la salida (Stock actual: " + producto.getStockActual() + ")");
        }

        // Actualizar stock
        producto.setStockActual(producto.getStockActual() - request.cantidad());
        productoRepository.save(producto);

        // Crear movimiento
        final InventarioMovimiento movimiento = new InventarioMovimiento();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setCantidad(request.cantidad());
        movimiento.setMotivo(request.motivo());
        // En salida, registramos el costo unitario de compra actual del producto o el del request
        movimiento.setCostoUnitario(request.costoUnitario() != null ? request.costoUnitario() : producto.getCostoCompra());

        final InventarioMovimiento guardado = inventarioMovimientoRepository.save(movimiento);
        log.info("[POS/InventarioService] - REGISTRAR_SALIDA: salida registrada con éxito, movimientoId: {}, userId: {}", guardado.getId(), usuario.getId());
        return guardado;
    }

    @Transactional(readOnly = true)
    public Page<InventarioMovimiento> obtenerMovimientos(
            final Long productoId,
            final TipoMovimiento tipo,
            final Long usuarioId,
            final LocalDateTime fechaInicio,
            final LocalDateTime fechaFin,
            final Pageable pageable) {
        log.info("[POS/InventarioService] - OBTENER_MOVIMIENTOS: consultando historial de movimientos");
        return inventarioMovimientoRepository.buscarMovimientos(productoId, tipo, usuarioId, fechaInicio, fechaFin, pageable);
    }

    private void verificarPermisos(final Usuario usuario, final String accion) {
        if (usuario.getRol() != RolUsuario.ADMINISTRADOR &&
            usuario.getRol() != RolUsuario.GERENTE &&
            usuario.getRol() != RolUsuario.INVENTARISTA) {
            log.error("[POS/InventarioService] - VERIFICAR_PERMISOS: rol {} no tiene permisos para {}, userId: {}", usuario.getRol(), accion, usuario.getId());
            throw new AccesoDenegadoException("No tiene permisos para " + accion);
        }
    }

    private double calcularPorcentajeGanancia(final Double costoCompra) {
        if (costoCompra < COSTO_BAJO) {
            return PORCENTAJE_BAJO;
        }
        if (costoCompra < COSTO_MEDIO) {
            return PORCENTAJE_MEDIO;
        }
        return PORCENTAJE_ALTO;
    }
}
