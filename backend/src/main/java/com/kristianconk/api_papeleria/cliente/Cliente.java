package com.kristianconk.api_papeleria.cliente;

import com.kristianconk.api_papeleria.ventas.Venta;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Cliente.java
@Data
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String telefono;

    private String nombre;
    private String email;
    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private Double totalCompras = 0.0;

    private String nivel; // Regular, Frecuente, VIP

    @OneToMany(mappedBy = "cliente")
    private List<Venta> ventas = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<PromocionCliente> promociones = new ArrayList<>();

    // Getters y setters
}
