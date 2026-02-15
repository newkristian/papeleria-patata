package com.kristianconk.api_papeleria.inventario;

import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import com.kristianconk.api_papeleria.producto.Producto;
import com.kristianconk.api_papeleria.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// InventarioMovimiento.java
@Data
@Entity
@Table(name = "inventario_movimientos")
public class InventarioMovimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    private String motivo;

    @Column(nullable = false)
    private Double costoUnitario;

    @CreationTimestamp
    private LocalDateTime fechaMovimiento;

    // Getters y setters
}


