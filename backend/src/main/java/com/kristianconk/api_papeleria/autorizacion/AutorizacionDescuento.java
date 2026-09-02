package com.kristianconk.api_papeleria.autorizacion;

import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.tienda.Tienda;
import com.kristianconk.api_papeleria.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// AutorizacionDescuento.java
@Data
@Entity
@Table(name = "autorizaciones_descuento")
public class AutorizacionDescuento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Solo se persiste el hash; el token en claro se entrega una única vez al
    // solicitarla y no se puede reconstruir a partir de esta columna.
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne
    @JoinColumn(name = "autorizador_usuario_id", nullable = false)
    private Usuario autorizador;

    @ManyToOne
    @JoinColumn(name = "vendedor_usuario_id", nullable = false)
    private Usuario vendedor;

    @ManyToOne
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "carrito_id", nullable = false, length = 100)
    private String carritoId;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean consumida = false;

    @Column(name = "fecha_consumo")
    private LocalDateTime fechaConsumo;
}
