package com.kristianconk.api_papeleria.categoria;

import com.kristianconk.api_papeleria.producto.Producto;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// Categoria.java
@Data
@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos = new ArrayList<>();

}
