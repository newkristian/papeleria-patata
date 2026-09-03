package com.kristianconk.api_papeleria.proveedor;

import com.kristianconk.api_papeleria.producto.Producto;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Proveedor.java
@Data
@Entity
@Table(name = "proveedores")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String rfc;
    private String telefono;
    private String email;
    private String contacto;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeComision; // Comisión que cobra la papelería

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "es_sistema", nullable = false)
    private boolean sistema = false;

    @OneToMany(mappedBy = "proveedor")
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "proveedor")
    private List<PagoProveedor> pagos = new ArrayList<>();

    // Getters y setters
}
