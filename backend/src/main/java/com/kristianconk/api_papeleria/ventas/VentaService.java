package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.autorizacion.AutorizacionDescuento;
import com.kristianconk.api_papeleria.autorizacion.AutorizacionDescuentoService;
import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.ClienteRepository;
import com.kristianconk.api_papeleria.cliente.PromocionCliente;
import com.kristianconk.api_papeleria.cliente.PromocionClienteRepository;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.promocion.MotorPromocionesService;
import com.kristianconk.api_papeleria.promocion.PromocionRepository;
import com.kristianconk.api_papeleria.promocion.ResultadoPromocion;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.utils.FolioGenerador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Transactional
public class VentaService {

    private static final int ESCALA_MONETARIA = 2;
    private static final BigDecimal UMBRAL_CLIENTE_VIP = new BigDecimal("5000.00");
    private static final BigDecimal DESCUENTO_CLIENTE_VIP = new BigDecimal("10.00");

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PromocionClienteRepository promocionClienteRepository;
    private final PromocionRepository promocionRepository;
    private final MotorPromocionesService motorPromocionesService;
    private final AutorizacionDescuentoService autorizacionDescuentoService;
    private final FolioGenerador folioGenerador;

    public Venta crearVenta(final VentaRequestDTO ventaDTO, final Usuario usuario) {
        // Solo un chequeo preliminar de rol; la autorización fina de descuentos
        // manuales pertenece a la Tarea 6, no a este método.
        if (usuario.getRol() == RolUsuario.INVENTARISTA) {
            throw new AccesoDenegadoException("El rol INVENTARISTA no tiene permisos para crear ventas");
        }

        if (usuario.getTienda() == null) {
            throw new IllegalArgumentException("El usuario no tiene una tienda asignada para realizar ventas");
        }

        final Venta venta = new Venta();
        venta.setFolio(folioGenerador.generarFolio());
        venta.setUsuario(usuario);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setMetodoPago(ventaDTO.metodoPago());
        venta.setTienda(usuario.getTienda());

        // Resuelve el cliente de la venta. `clienteParaPromociones` queda en null para
        // ventas anónimas, de modo que el motor de promociones (más abajo) nunca
        // evalúe promociones de cliente sobre el registro placeholder "PÚBLICO
        // GENERAL" (id=1), aunque por error existiera alguna asociada a ese ID.
        Cliente clienteParaPromociones = null;
        if (ventaDTO.clienteId() != null) {
            final Cliente cliente = clienteRepository.findById(ventaDTO.clienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            venta.setCliente(cliente);
            venta.setVentaAnonima(false);
            clienteParaPromociones = cliente;
        } else {
            final Cliente clienteAnonimo = clienteRepository.findById(1L)
                    .orElseGet(() -> {
                        final Cliente anonimo = new Cliente();
                        anonimo.setId(1L);
                        anonimo.setNombre("PÚBLICO GENERAL");
                        anonimo.setTelefono("ANÓNIMO");
                        return clienteRepository.save(anonimo);
                    });
            venta.setCliente(clienteAnonimo);
            venta.setVentaAnonima(true);
        }

        // Consolida productos repetidos en el request (misma línea escaneada varias
        // veces) antes de evaluar stock y promociones, para que un escalón de cantidad
        // se resuelva una sola vez por producto y el beneficio no se evada ni duplique.
        // También recolecta, por producto, las referencias de autorización de
        // descuento manual (T6) que haya traído alguna de sus líneas.
        final Map<Long, Integer> cantidadPorProducto = new LinkedHashMap<>();
        final Map<Long, Set<String>> autorizacionesPorProducto = new LinkedHashMap<>();
        for (final DetalleVentaRequestDTO detalleDTO : ventaDTO.detalles()) {
            cantidadPorProducto.merge(detalleDTO.productoId(), detalleDTO.cantidad(), Integer::sum);
            if (detalleDTO.autorizacionDescuento() != null && !detalleDTO.autorizacionDescuento().isBlank()) {
                autorizacionesPorProducto
                        .computeIfAbsent(detalleDTO.productoId(), id -> new LinkedHashSet<>())
                        .add(detalleDTO.autorizacionDescuento());
            }
        }

        final List<DetalleVenta> detalles = new ArrayList<>();
        // Acumulan, línea por línea, el precio de lista sin descuento y el descuento
        // total aplicado. `Venta.total` se calcula al final como su diferencia; nunca
        // se confía en un total enviado por el cliente HTTP.
        BigDecimal subtotalListaAcumulado = BigDecimal.ZERO.setScale(ESCALA_MONETARIA);
        BigDecimal descuentoAcumulado = BigDecimal.ZERO.setScale(ESCALA_MONETARIA);

        for (final Map.Entry<Long, Integer> entrada : cantidadPorProducto.entrySet()) {
            final Long productoId = entrada.getKey();
            final int cantidadTotal = entrada.getValue();

            // Se vuelve a leer el producto desde persistencia (nunca desde el request)
            // para obtener precio, stock y datos vigentes dentro de esta transacción.
            final Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + productoId));

            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no está activo");
            }

            final BigDecimal precioCatalogo = producto.getPrecioVenta();
            if (precioCatalogo == null || precioCatalogo.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "El producto " + producto.getNombre() + " no tiene un precio de venta válido");
            }

            if (!producto.isCantidadDesconocida() && producto.getStockActual() < cantidadTotal) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (Stock actual: " + producto.getStockActual() + ", solicitado: " + cantidadTotal + ")");
            }

            // Si cualquier línea posterior falla (producto inactivo, precio inválido o
            // stock insuficiente), la transacción de crearVenta hace rollback completo,
            // incluido este descuento de stock ya aplicado.
            producto.setStockActual(producto.getStockActual() - cantidadTotal);
            productoRepository.save(producto);

            // Fotografía histórica de la línea: precio de lista, tipo/porcentaje/monto
            // de descuento, precio final y subtotal quedan fijos aquí y ya no cambian
            // aunque la promoción se modifique o desactive después.
            final DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadTotal);
            detalle.setPrecioListaUnitario(precioCatalogo);

            // Un producto con más de una referencia de autorización distinta entre sus
            // líneas es una solicitud ambigua (¿cuál de las dos gana la línea
            // consolidada?): se rechaza en vez de elegir una arbitrariamente.
            final Set<String> autorizaciones = autorizacionesPorProducto.getOrDefault(productoId, Set.of());
            if (autorizaciones.size() > 1) {
                throw new IllegalArgumentException("El producto " + producto.getNombre()
                        + " tiene más de una autorización de descuento manual distinta en la misma venta");
            }

            if (autorizaciones.isEmpty()) {
                // Sin autorización manual: delega en el motor de T4 la selección de la
                // única promoción automática ganadora (por cantidad de
                // producto/categoría o por cliente) para la cantidad consolidada.
                final ResultadoPromocion resultado = motorPromocionesService.evaluar(
                        producto, cantidadTotal, clienteParaPromociones, venta.getFechaVenta());
                aplicarPromocionAutomatica(detalle, resultado, cantidadTotal);
            } else {
                // El descuento manual (T6) reemplaza la promoción automática de la
                // línea; nunca se evalúan ni se acumulan ambas. `consumir` revalida
                // todo el contexto (producto, cantidad, vendedor, tienda, carrito) y
                // marca la autorización como usada dentro de esta misma transacción.
                final AutorizacionDescuento autorizacion = autorizacionDescuentoService.consumir(
                        autorizaciones.iterator().next(), producto, cantidadTotal, usuario, ventaDTO.carritoId());
                aplicarDescuentoManual(detalle, producto, cantidadTotal, autorizacion);
            }

            // subtotalLista = subtotal ya descontado + lo descontado; evita recalcular
            // precio × cantidad por separado en cada una de las dos ramas anteriores.
            final BigDecimal subtotalLista = detalle.getSubtotal().add(detalle.getMontoDescuento());
            subtotalListaAcumulado = subtotalListaAcumulado.add(subtotalLista);
            descuentoAcumulado = descuentoAcumulado.add(detalle.getMontoDescuento());

            detalles.add(detalle);
        }

        venta.setDetalles(detalles);
        venta.setSubtotal(subtotalListaAcumulado);
        venta.setDescuento(descuentoAcumulado);
        venta.setTotal(subtotalListaAcumulado.subtract(descuentoAcumulado));

        final Venta ventaGuardada = ventaRepository.save(venta);

        // El acumulado y la promoción VIP solo aplican a clientes identificados; una
        // venta anónima nunca modifica el registro placeholder "PÚBLICO GENERAL".
        if (!ventaGuardada.isVentaAnonima() && ventaGuardada.getCliente() != null) {
            final Cliente cliente = ventaGuardada.getCliente();
            cliente.setTotalCompras(cliente.getTotalCompras().add(ventaGuardada.getTotal()));
            clienteRepository.save(cliente);
            verificarPromocionesCliente(cliente);
        }

        return ventaGuardada;
    }

    // Control de acceso horizontal por tienda (T8): un ADMINISTRADOR ve todas las
    // ventas; un VENDEDOR solo las de su propia tienda. Ningún endpoint de lectura
    // acepta un tiendaId provisto por el cliente HTTP como autoridad — siempre se
    // deriva del usuario autenticado.

    @Transactional(readOnly = true)
    public List<Venta> getAllVentas(final Usuario usuario) {
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            return ventaRepository.findAll();
        }
        return ventaRepository.findByTiendaId(tiendaDeUsuarioONingunaVenta(usuario));
    }

    /**
     * Un VENDEDOR que pide una venta de otra tienda recibe el mismo 404 que si no
     * existiera, nunca un 403: confirmar que el ID existe en otra tienda sería en sí
     * mismo una fuga de información (IDOR).
     */
    @Transactional(readOnly = true)
    public Venta getVentaById(final Long id, final Usuario usuario) {
        final Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
        if (usuario.getRol() != RolUsuario.ADMINISTRADOR
                && !venta.getTienda().getId().equals(tiendaDeUsuarioONingunaVenta(usuario))) {
            throw new ResourceNotFoundException("Venta no encontrada con ID: " + id);
        }
        return venta;
    }

    @Transactional(readOnly = true)
    public List<Venta> getVentasDelDia(final Long tiendaId) {
        return ventaRepository.findVentasDelDia(tiendaId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Venta> getVentasPorCliente(final Long clienteId, final Usuario usuario) {
        final List<Venta> ventas = clienteId == 1L
                ? ventaRepository.findByVentaAnonimaTrue()
                : ventaRepository.findByClienteId(clienteId);
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            return ventas;
        }
        final Long tiendaId = tiendaDeUsuarioONingunaVenta(usuario);
        return ventas.stream().filter(v -> v.getTienda().getId().equals(tiendaId)).toList();
    }

    /** -1L es un id de tienda inalcanzable: un VENDEDOR sin tienda asignada no ve ninguna venta. */
    private Long tiendaDeUsuarioONingunaVenta(final Usuario usuario) {
        return usuario.getTienda() != null ? usuario.getTienda().getId() : -1L;
    }

    private void aplicarPromocionAutomatica(final DetalleVenta detalle, final ResultadoPromocion resultado,
                                             final int cantidadTotal) {
        detalle.setTipoDescuento(resultado.tipo());
        detalle.setPorcentajeDescuento(resultado.porcentaje());
        detalle.setMontoDescuento(resultado.montoDescuento());
        // Precio unitario final derivado del subtotal ya descontado (no al revés),
        // para que el subtotal de la línea siga siendo la fuente de verdad.
        detalle.setPrecioUnitarioFinal(resultado.subtotalFinal()
                .divide(BigDecimal.valueOf(cantidadTotal), ESCALA_MONETARIA, RoundingMode.HALF_UP));
        detalle.setSubtotal(resultado.subtotalFinal());

        // Referencia excluyente a la promoción ganadora, coherente con tipoDescuento
        // (ver chk_detalle_promocion_tipo en V9): solo una de las dos se completa.
        if (resultado.tipo() == TipoDescuento.CANTIDAD) {
            detalle.setPromocionProducto(promocionRepository.getReferenceById(resultado.promocionId()));
        } else if (resultado.tipo() == TipoDescuento.CLIENTE) {
            detalle.setPromocionCliente(promocionClienteRepository.getReferenceById(resultado.promocionId()));
        }
    }

    private void aplicarDescuentoManual(final DetalleVenta detalle, final Producto producto,
                                         final int cantidadTotal, final AutorizacionDescuento autorizacion) {
        final BigDecimal subtotalLista = producto.getPrecioVenta()
                .multiply(BigDecimal.valueOf(cantidadTotal))
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        final BigDecimal montoDescuento = subtotalLista.multiply(autorizacion.getPorcentaje())
                .divide(BigDecimal.valueOf(100), ESCALA_MONETARIA, RoundingMode.HALF_UP);
        final BigDecimal subtotalFinal = subtotalLista.subtract(montoDescuento);

        detalle.setTipoDescuento(TipoDescuento.MANUAL);
        detalle.setPorcentajeDescuento(autorizacion.getPorcentaje());
        detalle.setMontoDescuento(montoDescuento);
        detalle.setPrecioUnitarioFinal(subtotalFinal
                .divide(BigDecimal.valueOf(cantidadTotal), ESCALA_MONETARIA, RoundingMode.HALF_UP));
        detalle.setSubtotal(subtotalFinal);
        detalle.setAutorizadoPor(autorizacion.getAutorizador());
        detalle.setMotivoDescuento(autorizacion.getMotivo());
        detalle.setAutorizacionDescuento(autorizacion);
    }

    private void verificarPromocionesCliente(final Cliente cliente) {
        if (cliente.getTotalCompras().compareTo(UMBRAL_CLIENTE_VIP) > 0
                && "Regular".equals(cliente.getNivel())) {
            cliente.setNivel("VIP");
            final PromocionCliente promocion = new PromocionCliente();
            promocion.setCliente(cliente);
            promocion.setDescripcion("Cliente VIP - 10% de descuento en todas sus compras");
            promocion.setPorcentajeDescuento(DESCUENTO_CLIENTE_VIP);
            promocion.setFechaInicio(LocalDate.now());
            promocion.setFechaFin(LocalDate.now().plusMonths(6));
            promocionClienteRepository.save(promocion);
        }
    }
}
