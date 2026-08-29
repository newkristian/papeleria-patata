package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.enums.EstadoVenta;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.tienda.Tienda;
import com.kristianconk.api_papeleria.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Venta.java
@Data
@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String folio;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;  // Puede ser null para venta anónima

    @Column(nullable = false)
    private boolean ventaAnonima = false;  // Bandera para identificar rápidamente

    @Column(nullable = false)
    private LocalDateTime fechaVenta;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime fechaCreacion;
}


