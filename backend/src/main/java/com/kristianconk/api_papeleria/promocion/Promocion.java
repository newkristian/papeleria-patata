package com.kristianconk.api_papeleria.promocion;

import com.kristianconk.api_papeleria.categoria.Categoria;
import com.kristianconk.api_papeleria.enums.TipoPromocion;
import com.kristianconk.api_papeleria.producto.Producto;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Promocion.java
@Data
@Entity
@Table(name = "promociones")
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoPromocion tipo;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private Integer prioridad = 0;

    @OneToOne(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReglaDescuentoPorCantidad reglaDescuentoPorCantidad;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
}
