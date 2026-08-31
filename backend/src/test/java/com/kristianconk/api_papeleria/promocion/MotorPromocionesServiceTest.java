package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.PromocionCliente;
import com.kristianconk.api_papeleria.cliente.PromocionClienteRepository;
import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.enums.TipoPromocion;
import com.kristianconk.api_papeleria.producto.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MotorPromocionesServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 9, 15, 12, 0);

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private PromocionClienteRepository promocionClienteRepository;

    @InjectMocks
    private MotorPromocionesService motor;

    private Producto producto;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        final Categoria categoria = new Categoria();
        categoria.setId(10L);

        producto = new Producto();
        producto.setId(1L);
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setCategoria(categoria);

        cliente = new Cliente();
        cliente.setId(5L);

        lenient().when(promocionRepository.findByProductoId(anyLong())).thenReturn(List.of());
        lenient().when(promocionRepository.findByCategoriaId(anyLong())).thenReturn(List.of());
        lenient().when(promocionClienteRepository.findByClienteId(anyLong())).thenReturn(List.of());
    }

    @Test
    void evaluar_sinPromociones_devuelveNinguna() {
        final ResultadoPromocion resultado = motor.evaluar(producto, 5, cliente, AHORA);

        assertEquals(TipoDescuento.NINGUNO, resultado.tipo());
        assertNull(resultado.promocionId());
        assertEquals(new BigDecimal("500.00"), resultado.subtotalLista());
        assertEquals(new BigDecimal("0.00"), resultado.montoDescuento());
        assertEquals(new BigDecimal("500.00"), resultado.subtotalFinal());
    }

    @Test
    void evaluar_cantidadDebajoDelUmbral_noAplicaDescuento() {
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 10, "5.00", null, null, 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 9, cliente, AHORA);

        assertEquals(TipoDescuento.NINGUNO, resultado.tipo());
    }

    @Test
    void evaluar_cantidadExactaEnElUmbral_aplicaDescuento() {
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 10, "5.00", null, null, 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(TipoDescuento.CANTIDAD, resultado.tipo());
        assertEquals(1L, resultado.promocionId());
        assertEquals(new BigDecimal("50.00"), resultado.montoDescuento());
        assertEquals(new BigDecimal("950.00"), resultado.subtotalFinal());
    }

    @Test
    void evaluar_cantidadPorEncimaDelUmbral_aplicaDescuento() {
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 10, "5.00", null, null, 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 15, cliente, AHORA);

        assertEquals(TipoDescuento.CANTIDAD, resultado.tipo());
        assertEquals(new BigDecimal("75.00"), resultado.montoDescuento());
    }

    @Test
    void evaluar_variosEscalones_eligeElDeMayorBeneficio() {
        // 10u -> 5% y 20u -> 8% sobre el mismo producto; con 25 unidades ambas aplican.
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(
                promocionCantidad(1L, 10, "5.00", null, null, 0),
                promocionCantidad(2L, 20, "8.00", null, null, 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 25, cliente, AHORA);

        assertEquals(2L, resultado.promocionId());
        assertEquals(new BigDecimal("8.00"), resultado.porcentaje());
        assertEquals(new BigDecimal("200.00"), resultado.montoDescuento());
    }

    @Test
    void evaluar_promocionInactiva_seExcluye() {
        final Promocion inactiva = promocionCantidad(1L, 5, "10.00", null, null, 0);
        inactiva.setActiva(false);
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(inactiva));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(TipoDescuento.NINGUNO, resultado.tipo());
    }

    @Test
    void evaluar_promocionFutura_seExcluye() {
        final LocalDateTime inicio = AHORA.plusDays(1);
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 5, "10.00", inicio, inicio.plusDays(30), 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(TipoDescuento.NINGUNO, resultado.tipo());
    }

    @Test
    void evaluar_promocionVencida_seExcluye() {
        final LocalDateTime fin = AHORA.minusDays(1);
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 5, "10.00", fin.minusDays(30), fin, 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(TipoDescuento.NINGUNO, resultado.tipo());
    }

    @Test
    void evaluar_vigenciaEnLosLimitesExactos_esInclusiva() {
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 5, "10.00", AHORA, AHORA, 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(TipoDescuento.CANTIDAD, resultado.tipo());
    }

    @Test
    void evaluar_promocionClienteConMayorBeneficioQuePromocionProducto_eligeVip() {
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 5, "5.00", null, null, 0)));
        when(promocionClienteRepository.findByClienteId(5L))
                .thenReturn(List.of(promocionClientePorcentaje(9L, "10.00", AHORA.toLocalDate(),
                        AHORA.toLocalDate().plusMonths(1), 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(TipoDescuento.CLIENTE, resultado.tipo());
        assertEquals(9L, resultado.promocionId());
        assertEquals(new BigDecimal("100.00"), resultado.montoDescuento());
    }

    @Test
    void evaluar_clienteNulo_noLanzaExcepcionYSoloEvaluaProducto() {
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 5, "10.00", null, null, 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, null, AHORA);

        assertEquals(TipoDescuento.CANTIDAD, resultado.tipo());
    }

    @Test
    void evaluar_promocionClienteInactivaOFueraDeVigencia_seExcluye() {
        final PromocionCliente inactiva = promocionClientePorcentaje(9L, "50.00", AHORA.toLocalDate().minusDays(10),
                AHORA.toLocalDate().plusDays(10), 0);
        inactiva.setActiva(false);
        final PromocionCliente vencida = promocionClientePorcentaje(10L, "50.00",
                AHORA.toLocalDate().minusDays(30), AHORA.toLocalDate().minusDays(1), 0);
        when(promocionClienteRepository.findByClienteId(5L)).thenReturn(List.of(inactiva, vencida));

        final ResultadoPromocion resultado = motor.evaluar(producto, 1, cliente, AHORA);

        assertEquals(TipoDescuento.NINGUNO, resultado.tipo());
    }

    @Test
    void evaluar_promocionClienteConMontoFijo_seCalculaYSeLimitaAlSubtotal() {
        // Producto de 100 x 1 unidad = subtotal 100; monto fijo de 500 debe limitarse a 100.
        when(promocionClienteRepository.findByClienteId(5L)).thenReturn(List.of(
                promocionClienteMontoFijo(9L, "500.00", AHORA.toLocalDate(), AHORA.toLocalDate(), 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 1, cliente, AHORA);

        assertEquals(TipoDescuento.CLIENTE, resultado.tipo());
        assertEquals(new BigDecimal("100.00"), resultado.montoDescuento());
        assertEquals(new BigDecimal("0.00"), resultado.subtotalFinal());
    }

    @Test
    void evaluar_montoFijoCompiteConPorcentaje_eligeElDeMayorBeneficioSiguiendoLasMismasReglas() {
        producto.setPrecioVenta(new BigDecimal("100.00"));
        // 10 unidades: porcentaje 5% = 50.00; monto fijo = 80.00. Gana el monto fijo.
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 10, "5.00", null, null, 0)));
        when(promocionClienteRepository.findByClienteId(5L)).thenReturn(List.of(
                promocionClienteMontoFijo(9L, "80.00", AHORA.toLocalDate(), AHORA.toLocalDate(), 0)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(TipoDescuento.CLIENTE, resultado.tipo());
        assertEquals(9L, resultado.promocionId());
        assertEquals(new BigDecimal("80.00"), resultado.montoDescuento());
    }

    @Test
    void evaluar_empatePorBeneficio_ganaMayorPrioridad() {
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(
                promocionCantidad(1L, 5, "10.00", null, null, 0),
                promocionCantidad(2L, 5, "10.00", null, null, 5)));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(2L, resultado.promocionId());
    }

    @Test
    void evaluar_empatePorBeneficioYPrioridad_ganaVigenciaDefinidaSobreAtemporal() {
        final Promocion atemporal = promocionCantidad(1L, 5, "10.00", null, null, 0);
        final Promocion definida = promocionCantidad(2L, 5, "10.00", AHORA.minusDays(1), AHORA.plusDays(1), 0);
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(atemporal, definida));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(2L, resultado.promocionId());
    }

    @Test
    void evaluar_empateTotal_ganaLaMasNuevaPorId() {
        final Promocion antigua = promocionCantidad(1L, 5, "10.00", null, null, 0);
        final Promocion nueva = promocionCantidad(2L, 5, "10.00", null, null, 0);
        when(promocionRepository.findByProductoId(1L)).thenReturn(List.of(antigua, nueva));

        final ResultadoPromocion resultado = motor.evaluar(producto, 10, cliente, AHORA);

        assertEquals(2L, resultado.promocionId());
    }

    @Test
    void evaluar_cantidadConsolidada_calculaDescuentoSobreElTotalNoFragmentado() {
        // 6 + 5 unidades del mismo producto en dos líneas deben consolidarse en 11 antes
        // de llamar al motor; el motor no fragmenta ni evade el umbral de 10 unidades.
        when(promocionRepository.findByProductoId(1L))
                .thenReturn(List.of(promocionCantidad(1L, 10, "5.00", null, null, 0)));

        final ResultadoPromocion resultadoConsolidado = motor.evaluar(producto, 11, cliente, AHORA);
        final ResultadoPromocion resultadoLineaSuelta = motor.evaluar(producto, 6, cliente, AHORA);

        assertEquals(TipoDescuento.CANTIDAD, resultadoConsolidado.tipo());
        assertEquals(new BigDecimal("55.00"), resultadoConsolidado.montoDescuento());
        assertEquals(TipoDescuento.NINGUNO, resultadoLineaSuelta.tipo());
    }

    private Promocion promocionCantidad(final Long id, final int cantidadMinima, final String porcentaje,
                                         final LocalDateTime inicio, final LocalDateTime fin,
                                         final int prioridad) {
        final Promocion promocion = new Promocion();
        promocion.setId(id);
        promocion.setTipo(TipoPromocion.DESCUENTO_POR_CANTIDAD);
        promocion.setActiva(true);
        promocion.setFechaInicio(inicio);
        promocion.setFechaFin(fin);
        promocion.setPrioridad(prioridad);

        final ReglaDescuentoPorCantidad regla = new ReglaDescuentoPorCantidad();
        regla.setCantidadMinima(cantidadMinima);
        regla.setPorcentaje(new BigDecimal(porcentaje));
        regla.setPromocion(promocion);
        promocion.setReglaDescuentoPorCantidad(regla);

        return promocion;
    }

    private PromocionCliente promocionClientePorcentaje(final Long id, final String porcentaje,
                                                          final LocalDate inicio, final LocalDate fin,
                                                          final int prioridad) {
        final PromocionCliente promocion = new PromocionCliente();
        promocion.setId(id);
        promocion.setActiva(true);
        promocion.setPorcentajeDescuento(new BigDecimal(porcentaje));
        promocion.setFechaInicio(inicio);
        promocion.setFechaFin(fin);
        promocion.setPrioridad(prioridad);
        return promocion;
    }

    private PromocionCliente promocionClienteMontoFijo(final Long id, final String montoFijo,
                                                         final LocalDate inicio, final LocalDate fin,
                                                         final int prioridad) {
        final PromocionCliente promocion = new PromocionCliente();
        promocion.setId(id);
        promocion.setActiva(true);
        promocion.setMontoDescuentoFijo(new BigDecimal(montoFijo));
        promocion.setFechaInicio(inicio);
        promocion.setFechaFin(fin);
        promocion.setPrioridad(prioridad);
        return promocion;
    }
}
