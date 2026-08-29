package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.ClienteRepository;
import com.kristianconk.api_papeleria.cliente.PromocionCliente;
import com.kristianconk.api_papeleria.cliente.PromocionClienteRepository;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.utils.FolioGenerador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PromocionClienteRepository promocionClienteRepository;
    private final FolioGenerador folioGenerador;

    public Venta crearVenta(final VentaRequestDTO ventaDTO, final Usuario usuario) {
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

        if (ventaDTO.clienteId() != null) {
            final Cliente cliente = clienteRepository.findById(ventaDTO.clienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            venta.setCliente(cliente);
            venta.setVentaAnonima(false);
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

        final List<DetalleVenta> detalles = new ArrayList<>();
        double subtotalAcumulado = 0.0;

        for (final DetalleVentaRequestDTO detalleDTO : ventaDTO.detalles()) {
            final Producto producto = productoRepository.findById(detalleDTO.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + detalleDTO.productoId()));

            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no está activo");
            }

            final Double precioCatalogo = producto.getPrecioVenta();
            if (precioCatalogo == null || !Double.isFinite(precioCatalogo) || precioCatalogo <= 0.0) {
                throw new IllegalStateException(
                        "El producto " + producto.getNombre() + " no tiene un precio de venta válido");
            }

            if (!producto.isCantidadDesconocida() && producto.getStockActual() < detalleDTO.cantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (Stock actual: " + producto.getStockActual() + ", solicitado: " + detalleDTO.cantidad() + ")");
            }

            producto.setStockActual(producto.getStockActual() - detalleDTO.cantidad());
            productoRepository.save(producto);

            final DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.cantidad());
            detalle.setPrecioUnitario(precioCatalogo);

            final double detalleSubtotal = detalleDTO.cantidad() * precioCatalogo;
            detalle.setSubtotal(detalleSubtotal);
            subtotalAcumulado += detalleSubtotal;

            detalles.add(detalle);
        }

        venta.setDetalles(detalles);
        venta.setSubtotal(subtotalAcumulado);
        venta.setDescuento(0.0);
        venta.setTotal(subtotalAcumulado);

        final Venta ventaGuardada = ventaRepository.save(venta);

        if (!ventaGuardada.isVentaAnonima() && ventaGuardada.getCliente() != null) {
            final Cliente cliente = ventaGuardada.getCliente();
            cliente.setTotalCompras(cliente.getTotalCompras() + ventaGuardada.getTotal());
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
        if (cliente.getTotalCompras() > 5000.0 && "Regular".equals(cliente.getNivel())) {
            cliente.setNivel("VIP");
            final PromocionCliente promocion = new PromocionCliente();
            promocion.setCliente(cliente);
            promocion.setDescripcion("Cliente VIP - 10% de descuento en todas sus compras");
            promocion.setPorcentajeDescuento(10.0);
            promocion.setFechaInicio(LocalDate.now());
            promocion.setFechaFin(LocalDate.now().plusMonths(6));
            promocionClienteRepository.save(promocion);
        }
    }
}
