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

    @Column(unique = true)
    private String telefono;  // Ahora puede ser null para cliente anónimo

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

    // Constructor para cliente anónimo
    public static Cliente crearAnonimo() {
        Cliente anonimo = new Cliente();
        anonimo.setId(1L); // ID fijo para cliente anónimo
        anonimo.setNombre("PÚBLICO GENERAL");
        anonimo.setTelefono("ANÓNIMO");
        return anonimo;
    }
}
