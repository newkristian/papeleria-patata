package com.kristianconk.api_papeleria.tienda;

import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.ventas.Venta;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// Tienda.java (papelería)
@Data
@Entity
@Table(name = "tiendas")
public class Tienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String direccion;
    private String telefono;
    private String email;
    private String rfc;

    @OneToMany(mappedBy = "tienda")
    private List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "tienda")
    private List<Venta> ventas = new ArrayList<>();

    // Getters y setters
}
