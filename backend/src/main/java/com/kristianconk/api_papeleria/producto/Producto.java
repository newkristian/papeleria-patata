package com.kristianconk.api_papeleria.producto;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.producto.foto.ProductoFoto;
import com.kristianconk.api_papeleria.proveedor.Proveedor;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal costoCompra;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeGanancia; // Calculado por el sistema

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precioVenta; // Calculado automáticamente

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
    void calcularPrecioVenta() {
        if (costoCompra == null || porcentajeGanancia == null) {
            return;
        }
        final BigDecimal factorGanancia = BigDecimal.ONE.add(porcentajeGanancia.movePointLeft(2));
        this.precioVenta = costoCompra.multiply(factorGanancia).setScale(2, RoundingMode.HALF_UP);
    }

    // Getters y setters
}
