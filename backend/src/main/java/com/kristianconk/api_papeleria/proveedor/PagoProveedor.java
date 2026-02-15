package com.kristianconk.api_papeleria.proveedor;

import jakarta.persistence.*;
import lombok.Data;

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

    @Column(nullable = false)
    private Double totalVentas;

    @Column(nullable = false)
    private Double comisionTienda;

    @Column(nullable = false)
    private Double montoPagar; // totalVentas - comisionTienda

    @Column(nullable = false)
    private LocalDate fechaPago;

    private String referenciaPago;
    private boolean pagado = false;

    // Getters y setters
}
