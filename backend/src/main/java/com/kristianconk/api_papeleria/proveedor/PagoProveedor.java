package com.kristianconk.api_papeleria.proveedor;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// PagoProveedor.java
@Data
@Entity
@Table(name = "pagos_proveedor")
public class PagoProveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalVentas;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal comisionTienda;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montoPagar; // totalVentas - comisionTienda

    @Column(nullable = false)
    private LocalDate fechaPago;

    private String referenciaPago;
    private boolean pagado = false;
}
