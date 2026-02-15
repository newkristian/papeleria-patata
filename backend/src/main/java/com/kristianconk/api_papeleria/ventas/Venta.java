package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.cliente.Cliente;
import com.kristianconk.api_papeleria.enums.EstadoVenta;
import com.kristianconk.api_papeleria.enums.MetodoPago;
import com.kristianconk.api_papeleria.tienda.Tienda;
import com.kristianconk.api_papeleria.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

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
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime fechaVenta;

    @Column(nullable = false)
    private Double subtotal = 0.0;

    private Double descuento = 0.0;
    private Double impuesto = 0.0;

    @Column(nullable = false)
    private Double total = 0.0;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    // Getters y setters
}




