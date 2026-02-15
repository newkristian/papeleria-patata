package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.ClienteRepository;
import com.kristianconk.api_papeleria.cliente.PromocionCliente;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoRepository;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.utils.FolioGenerador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VentaService {


    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final FolioGenerador folioGenerador;



    public Venta crearVenta(VentaRequestDTO ventaDTO, Usuario usuario) {

        Venta venta = new Venta();

        // Si viene clienteId, asignar cliente, si no, venta anónima
        if (ventaDTO.clienteId() != null) {
            Cliente cliente = clienteRepository.findById(ventaDTO.clienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            venta.setCliente(cliente);
            venta.setVentaAnonima(false);
        } else {
            venta.setVentaAnonima(true);
        }

        venta.setDescuento(ventaDTO.descuento());
        venta.setMetodoPago(ventaDTO.metodoPago());
        // Validar permisos según el rol
        if (usuario.getRol() == RolUsuario.VENDEDOR && venta.getDescuento() > 10) {
            throw new AccesoDenegadoException("Vendedor no puede aplicar descuentos mayores al 10%");
        }

        // Procesar la venta
        venta.setFolio(folioGenerador.generarFolio());
        venta.setUsuario(usuario);
        venta.setFechaVenta(LocalDateTime.now());

        // Manejar cliente anónimo
        if (venta.getCliente() == null) {
            venta.setVentaAnonima(true);
            // Opción 1: Dejar cliente como null
            // venta.setCliente(null);

            // Opción 2: Asignar cliente anónimo por defecto
            Cliente clienteAnonimo = clienteRepository.findById(1L)
                    .orElseGet(() -> {
                        Cliente anonimo = new Cliente();
                        anonimo.setId(1L);
                        anonimo.setNombre("PÚBLICO GENERAL");
                        anonimo.setTelefono("ANÓNIMO");
                        return clienteRepository.save(anonimo);
                    });
            venta.setCliente(clienteAnonimo);
        } else {
            venta.setVentaAnonima(false);
        }

        // Calcular totales
        double subtotal = venta.getDetalles().stream()
                .mapToDouble(DetalleVenta::getSubtotal)
                .sum();
        venta.setSubtotal(subtotal);
        venta.setTotal(subtotal - venta.getDescuento());

        // Actualizar inventario
        venta.getDetalles().forEach(detalle -> {
            Producto producto = detalle.getProducto();
            producto.setStockActual(producto.getStockActual() - detalle.getCantidad());
            productoRepository.save(producto);
        });

        Venta ventaGuardada = ventaRepository.save(venta);

        // Actualizar total de compras del cliente (si no es anónimo)
        if (!venta.isVentaAnonima() && venta.getCliente() != null) {
            Cliente cliente = venta.getCliente();
            cliente.setTotalCompras(cliente.getTotalCompras() + ventaGuardada.getTotal());
            clienteRepository.save(cliente);

            // Verificar si aplica promociones automáticas
            verificarPromocionesCliente(cliente);
        }

        return ventaGuardada;
    }

    public List<Venta> getVentasPorCliente(Long clienteId) {
        if (clienteId == 1L) {
            return ventaRepository.findByVentaAnonimaTrue();
        }
        return ventaRepository.findByClienteId(clienteId);
    }

    private void verificarPromocionesCliente(Cliente cliente) {
        // Si el cliente supera cierto monto de compras, asignar nivel y promociones
        if (cliente.getTotalCompras() > 5000 && "Regular".equals(cliente.getNivel())) {
            cliente.setNivel("VIP");
            // Crear promoción automática para cliente VIP
            PromocionCliente promocion = new PromocionCliente();
            promocion.setCliente(cliente);
            promocion.setDescripcion("Cliente VIP - 10% de descuento en todas sus compras");
            promocion.setPorcentajeDescuento(10.0);
            promocion.setFechaInicio(LocalDate.now());
            promocion.setFechaFin(LocalDate.now().plusMonths(6));
            // Guardar promoción
        }
    }
}