package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.usuario.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public Producto crearProducto(Producto producto, Usuario usuario) {
        // Solo inventarista puede crear/actualizar productos
        if (usuario.getRol() != RolUsuario.INVENTARISTA && usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new AccesoDenegadoException("No tiene permisos para crear productos");
        }

        // El sistema calcula el porcentaje de ganancia basado en reglas de negocio
        if (producto.getCostoCompra() < 50) {
            producto.setPorcentajeGanancia(50.0); // 50% para productos baratos
        } else if (producto.getCostoCompra() < 200) {
            producto.setPorcentajeGanancia(40.0);
        } else {
            producto.setPorcentajeGanancia(30.0);
        }

        return productoRepository.save(producto);
    }
}
