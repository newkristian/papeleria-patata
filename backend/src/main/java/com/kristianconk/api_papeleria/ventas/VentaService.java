package com.kristianconk.api_papeleria.ventas;

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
import java.util.List;
import java.util.Map;

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
        final Map<Long, Integer> cantidadPorProducto = new LinkedHashMap<>();
        for (final DetalleVentaRequestDTO detalleDTO : ventaDTO.detalles()) {
            cantidadPorProducto.merge(detalleDTO.productoId(), detalleDTO.cantidad(), Integer::sum);
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

            // Delega en el motor de T4 la selección de la única promoción ganadora
            // (por cantidad de producto/categoría o por cliente) para la cantidad
            // consolidada de esta línea.
            final ResultadoPromocion resultado = motorPromocionesService.evaluar(
                    producto, cantidadTotal, clienteParaPromociones, venta.getFechaVenta());

            // Fotografía histórica de la línea: precio de lista, tipo/porcentaje/monto
            // de descuento, precio final y subtotal quedan fijos aquí y ya no cambian
            // aunque la promoción se modifique o desactive después.
            final DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadTotal);
            detalle.setPrecioListaUnitario(precioCatalogo);
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

            subtotalListaAcumulado = subtotalListaAcumulado.add(resultado.subtotalLista());
            descuentoAcumulado = descuentoAcumulado.add(resultado.montoDescuento());

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

    @Transactional(readOnly = true)
    public List<Venta> getAllVentas() {
        return ventaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Venta getVentaById(final Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Venta> getVentasDelDia(final Long tiendaId) {
        return ventaRepository.findVentasDelDia(tiendaId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Venta> getVentasPorCliente(final Long clienteId) {
        if (clienteId == 1L) {
            return ventaRepository.findByVentaAnonimaTrue();
        }
        return ventaRepository.findByClienteId(clienteId);
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
