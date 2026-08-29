package com.kristianconk.api_papeleria.ventas;

import com.kristianconk.api_papeleria.enums.TipoDescuento;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

// DetalleVenta.java
@Data
@Entity
@Table(name = "detalles_venta")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_lista_unitario", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioListaUnitario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDescuento tipoDescuento = TipoDescuento.NINGUNO;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precioUnitarioFinal;

    @ManyToOne
    @JoinColumn(name = "autorizado_por_usuario_id")
    private Usuario autorizadoPor;

    @Column(length = 500)
    private String motivoDescuento;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    // Getters y setters
}
