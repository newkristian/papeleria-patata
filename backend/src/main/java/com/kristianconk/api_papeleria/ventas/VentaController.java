package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.cliente.ClienteResponseDTO;
import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.producto.ProductoResponseDTO;
import com.kristianconk.api_papeleria.tienda.Tienda;
import com.kristianconk.api_papeleria.tienda.TiendaResponseDTO;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.usuario.UsuarioResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crearVenta(@RequestBody VentaRequestDTO ventaDTO,
                                            @AuthenticationPrincipal Usuario usuario) {

        Venta ventaCreada = ventaService.crearVenta(ventaDTO, usuario);
        VentaResponseDTO response = mapToResponseDTO(ventaCreada);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private VentaResponseDTO mapToResponseDTO(Venta venta) {
        return new VentaResponseDTO(
                venta.getId(),
                venta.getFolio(),
                mapUsuario(venta.getUsuario()),
                mapTienda(venta.getTienda()),
                venta.getCliente() != null ? mapCliente(venta.getCliente()) : ClienteResponseDTO.anonimo(),
                venta.isVentaAnonima(),
                venta.getFechaVenta(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getTotal(),
                venta.getMetodoPago(),
                venta.getEstado(),
                mapDetalles(venta.getDetalles())
        );
    }

    private UsuarioResponseDTO mapUsuario(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getRol()
        );
    }

    private TiendaResponseDTO mapTienda(Tienda tienda) {
        return new TiendaResponseDTO(
                tienda.getId(),
                tienda.getNombre(),
                tienda.getDireccion(),
                tienda.getTelefono()
        );
    }

    private ClienteResponseDTO mapCliente(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getTotalCompras(),
                cliente.getNivel()
        );
    }

    private List<DetalleVentaResponseDTO> mapDetalles(List<DetalleVenta> detalles) {
        return detalles.stream()
                .map(this::mapDetalle)
                .collect(Collectors.toList());
    }

    private DetalleVentaResponseDTO mapDetalle(DetalleVenta detalle) {
        return new DetalleVentaResponseDTO(
                detalle.getId(),
                mapProducto(detalle.getProducto()),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }

    private ProductoResponseDTO mapProducto(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getCodigoBarras(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria().getNombre(),
                producto.getProveedor().getNombre(),
                producto.getPrecioVenta(),
                producto.getStockActual()
        );
    }
}
