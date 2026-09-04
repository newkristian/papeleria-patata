package com.kristianconk.api_papeleria.producto.foto;

import com.kristianconk.api_papeleria.enums.EstadoProcesamientoFoto;
import com.kristianconk.api_papeleria.producto.Producto;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "producto_fotos")
public class ProductoFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(length = 1000)
    private String rutaArchivo;

    @Column(length = 1000)
    private String rutaMiniatura;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long tamanio;

    private Boolean esPrincipal = false;

    private Integer orden = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProcesamientoFoto estadoProcesamiento = EstadoProcesamientoFoto.COMPLETADO;

    @Column(columnDefinition = "TEXT")
    private String mensajeError;

    @CreationTimestamp
    private LocalDateTime fechaSubida;

    // Metadatos adicionales opcionales
    private Integer ancho;
    private Integer alto;
    private String descripcion;
}
