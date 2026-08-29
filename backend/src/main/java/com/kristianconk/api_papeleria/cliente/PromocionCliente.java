package com.kristianconk.api_papeleria.cliente;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// PromocionCliente.java
@Data
@Entity
@Table(name = "promociones_cliente")
public class PromocionCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private String descripcion;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Column(precision = 19, scale = 2)
    private BigDecimal montoMinimoCompra;

    @Column(precision = 19, scale = 2)
    private BigDecimal montoDescuentoFijo;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    private boolean activa = true;

    // Getters y setters
}
