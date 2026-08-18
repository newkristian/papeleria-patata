package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.producto.foto.ProductoFoto;
import com.kristianconk.api_papeleria.proveedor.Proveedor;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Producto.java
@Data
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoBarras;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false)
    private Double costoCompra;

    @Column(nullable = false)
    private Double porcentajeGanancia; // Calculado por el sistema

    @Column(nullable = false)
    private Double precioVenta; // Calculado automáticamente

    private Integer stockMinimo = 5;
    private Integer stockActual = 0;
    private String unidadMedida; // pieza, caja, paquete, etc.

    private boolean activo = true;

    @Column(nullable = false)
    private boolean cantidadDesconocida = false;


    // En Producto.java, agregar:
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC, fechaSubida DESC")
    private List<ProductoFoto> fotos = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    private void calcularPrecioVenta() {
        this.precioVenta = this.costoCompra * (1 + (this.porcentajeGanancia / 100));
    }

    // Getters y setters
}
