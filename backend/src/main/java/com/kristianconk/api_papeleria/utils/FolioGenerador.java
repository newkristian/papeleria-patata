package com.kristianconk.api_papeleria.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FolioGenerador {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String PREFIX = "POS";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyMMdd");
    // Opción 2: Contador diario reseteable
    private final AtomicLong contadorDiario = new AtomicLong(1);
    private String ultimaFecha = "";

    public String generarFolio() {
        return generarFolioConSecuencia();
    }

    // Opción 1: Usando secuencia de base de datos
    public String generarFolioConSecuencia() {
        String fecha = LocalDateTime.now().format(DATE_FORMAT);
        Long secuencia = jdbcTemplate.queryForObject(
                "SELECT nextval('seq_folio_venta')", Long.class);
        return String.format("%s-%s-%06d", PREFIX, fecha, secuencia);
    }


    public String generarFolioConContadorDiario() {
        String fechaActual = LocalDateTime.now().format(DATE_FORMAT);

        // Reiniciar contador si es un nuevo día
        if (!fechaActual.equals(ultimaFecha)) {
            contadorDiario.set(1);
            ultimaFecha = fechaActual;
        }

        long numeroFolio = contadorDiario.getAndIncrement();
        return String.format("%s-%s-%04d", PREFIX, fechaActual, numeroFolio);
    }

    // Opción 3: Usando base de datos para control diario
    public String generarFolioConDB() {
        String fecha = LocalDateTime.now().format(DATE_FORMAT);

        // Obtener y actualizar el último folio usado hoy
        String sql = """
            INSERT INTO control_folios (fecha, ultimo_folio) 
            VALUES (?, 1) 
            ON CONFLICT (fecha) 
            DO UPDATE SET ultimo_folio = control_folios.ultimo_folio + 1 
            RETURNING ultimo_folio
        """;

        Integer ultimoFolio = jdbcTemplate.queryForObject(
                sql, Integer.class, LocalDate.now());

        return String.format("%s-%s-%04d", PREFIX, fecha, ultimoFolio);
    }
}
