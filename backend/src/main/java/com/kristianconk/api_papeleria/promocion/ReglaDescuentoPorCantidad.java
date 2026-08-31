package com.kristianconk.api_papeleria.promocion;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

// ReglaDescuentoPorCantidad.java
@Data
@Entity
@Table(name = "reglas_descuento_cantidad")
public class ReglaDescuentoPorCantidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "promocion_id", nullable = false, unique = true)
    private Promocion promocion;

    @Column(name = "cantidad_minima", nullable = false)
    private Integer cantidadMinima;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;
}
