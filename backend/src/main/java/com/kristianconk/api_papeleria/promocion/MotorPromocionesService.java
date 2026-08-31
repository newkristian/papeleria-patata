package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.PromocionCliente;
import com.kristianconk.api_papeleria.cliente.PromocionClienteRepository;
import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.enums.TipoPromocion;
import com.kristianconk.api_papeleria.producto.Producto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio que evalúa promociones automáticas (por cantidad de producto y
 * por nivel de cliente) para una línea ya consolidada, sin depender del frontend ni de
 * {@code VentaService}. La integración al flujo de venta pertenece a T5.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MotorPromocionesService {

    private static final int ESCALA_MONETARIA = 2;

    private final PromocionRepository promocionRepository;
    private final PromocionClienteRepository promocionClienteRepository;

    @Transactional(readOnly = true)
    public ResultadoPromocion evaluar(final Producto producto, final int cantidadTotal, final Cliente cliente,
                                       final LocalDateTime fechaHora) {
        if (producto == null || producto.getPrecioVenta() == null) {
            throw new IllegalArgumentException("El producto y su precio de venta son obligatorios");
        }
        if (cantidadTotal <= 0) {
            throw new IllegalArgumentException("La cantidad total debe ser mayor a 0");
        }

        final BigDecimal subtotalLista = producto.getPrecioVenta()
                .multiply(BigDecimal.valueOf(cantidadTotal))
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);

        final List<Candidata> candidatas = new ArrayList<>();
        candidatas.addAll(candidatasDeProducto(producto, cantidadTotal, fechaHora, subtotalLista));
        candidatas.addAll(candidatasDeCliente(cliente, fechaHora, subtotalLista));

        final Optional<Candidata> ganadora = candidatas.stream().max(COMPARADOR_CANDIDATAS);

        if (ganadora.isEmpty()) {
            log.info("[POS/MotorPromocionesService] - EVALUAR: sin promociones aplicables para productoId: {}",
                    producto.getId());
            return ResultadoPromocion.ninguna(subtotalLista);
        }

        final Candidata elegida = ganadora.get();
        final BigDecimal subtotalFinal = subtotalLista.subtract(elegida.montoDescuento());
        log.info("[POS/MotorPromocionesService] - EVALUAR: productoId: {}, promoción ganadora tipo: {}, ID: {}, "
                        + "montoDescuento: {}",
                producto.getId(), elegida.tipo(), elegida.id(), elegida.montoDescuento());

        return new ResultadoPromocion(
                elegida.tipo(), elegida.id(), elegida.porcentaje(), subtotalLista, elegida.montoDescuento(),
                subtotalFinal);
    }

    private List<Candidata> candidatasDeProducto(final Producto producto, final int cantidadTotal,
                                                   final LocalDateTime fechaHora, final BigDecimal subtotalLista) {
        final List<Promocion> promociones = new ArrayList<>(promocionRepository.findByProductoId(producto.getId()));
        if (producto.getCategoria() != null) {
            promociones.addAll(promocionRepository.findByCategoriaId(producto.getCategoria().getId()));
        }

        final List<Candidata> candidatas = new ArrayList<>();
        for (final Promocion promocion : promociones) {
            if (!aplicaPromocionDeCantidad(promocion, cantidadTotal, fechaHora)) {
                continue;
            }
            final BigDecimal porcentaje = promocion.getReglaDescuentoPorCantidad().getPorcentaje();
            final BigDecimal montoDescuento = calcularMontoPorcentaje(subtotalLista, porcentaje);
            final boolean vigenciaDefinida = promocion.getFechaInicio() != null && promocion.getFechaFin() != null;
            candidatas.add(new Candidata(TipoDescuento.CANTIDAD, promocion.getId(), promocion.getPrioridad(),
                    vigenciaDefinida, porcentaje, montoDescuento));
        }
        return candidatas;
    }

    private List<Candidata> candidatasDeCliente(final Cliente cliente, final LocalDateTime fechaHora,
                                                  final BigDecimal subtotalLista) {
        if (cliente == null) {
            return List.of();
        }
        final LocalDate fecha = fechaHora.toLocalDate();
        final List<Candidata> candidatas = new ArrayList<>();
        for (final PromocionCliente promocion : promocionClienteRepository.findByClienteId(cliente.getId())) {
            if (!promocion.isActiva() || !vigente(promocion.getFechaInicio(), promocion.getFechaFin(), fecha)) {
                continue;
            }
            // La vigencia de PromocionCliente siempre está acotada (fechaInicio/fechaFin
            // son obligatorias), por lo que su vigencia es siempre "definida".
            if (promocion.getPorcentajeDescuento() != null) {
                final BigDecimal montoDescuento = calcularMontoPorcentaje(
                        subtotalLista, promocion.getPorcentajeDescuento());
                candidatas.add(new Candidata(TipoDescuento.CLIENTE, promocion.getId(), promocion.getPrioridad(),
                        true, promocion.getPorcentajeDescuento(), montoDescuento));
            } else if (promocion.getMontoDescuentoFijo() != null) {
                final BigDecimal montoDescuento = promocion.getMontoDescuentoFijo().min(subtotalLista)
                        .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
                final BigDecimal porcentajeEfectivo = calcularPorcentajeEfectivo(subtotalLista, montoDescuento);
                candidatas.add(new Candidata(TipoDescuento.CLIENTE, promocion.getId(), promocion.getPrioridad(),
                        true, porcentajeEfectivo, montoDescuento));
            } else {
                log.warn("[POS/MotorPromocionesService] - CANDIDATAS_CLIENTE: promoción cliente ID: {} sin "
                        + "porcentaje ni monto fijo configurado, se ignora", promocion.getId());
            }
        }
        return candidatas;
    }

    private boolean aplicaPromocionDeCantidad(final Promocion promocion, final int cantidadTotal,
                                                final LocalDateTime fechaHora) {
        if (!promocion.isActiva() || promocion.getTipo() != TipoPromocion.DESCUENTO_POR_CANTIDAD) {
            return false;
        }
        final ReglaDescuentoPorCantidad regla = promocion.getReglaDescuentoPorCantidad();
        if (regla == null) {
            log.warn("[POS/MotorPromocionesService] - APLICA_PROMOCION: promoción ID: {} sin regla de cantidad, "
                    + "se ignora", promocion.getId());
            return false;
        }
        if (!vigente(promocion.getFechaInicio(), promocion.getFechaFin(), fechaHora)) {
            return false;
        }
        return cantidadTotal >= regla.getCantidadMinima();
    }

    private BigDecimal calcularMontoPorcentaje(final BigDecimal subtotalLista, final BigDecimal porcentaje) {
        return subtotalLista.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPorcentajeEfectivo(final BigDecimal subtotalLista, final BigDecimal montoDescuento) {
        if (subtotalLista.signum() == 0) {
            return BigDecimal.ZERO.setScale(ESCALA_MONETARIA);
        }
        return montoDescuento.multiply(BigDecimal.valueOf(100))
                .divide(subtotalLista, ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    private static <T extends Comparable<T>> boolean vigente(final T inicio, final T fin, final T momento) {
        final boolean despuesDeInicio = inicio == null || momento.compareTo(inicio) >= 0;
        final boolean antesDeFin = fin == null || momento.compareTo(fin) <= 0;
        return despuesDeInicio && antesDeFin;
    }

    /**
     * Candidata homogénea de cualquier origen (producto/categoría o cliente),
     * comparable bajo las mismas reglas de desempate.
     */
    private record Candidata(TipoDescuento tipo, Long id, Integer prioridad, boolean vigenciaDefinida,
                              BigDecimal porcentaje, BigDecimal montoDescuento) {
    }

    /**
     * Orden de selección: mayor beneficio monetario, luego mayor prioridad explícita,
     * luego vigencia acotada (tiempo definido) sobre vigencia atemporal, y finalmente
     * la promoción más reciente (mayor ID) como desempate final.
     */
    private static final Comparator<Candidata> COMPARADOR_CANDIDATAS = Comparator
            .comparing(Candidata::montoDescuento)
            .thenComparing(Candidata::prioridad)
            .thenComparing(c -> c.vigenciaDefinida() ? 1 : 0)
            .thenComparing(Candidata::id);
}
