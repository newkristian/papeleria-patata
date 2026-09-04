package com.kristianconk.api_papeleria.proveedor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findBySistemaTrue();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Proveedor> findAllByActivoTrueAndSistemaFalseOrderByNombreAsc();

    @Query("SELECT p FROM Proveedor p WHERE p.sistema = false " +
            "AND (:patron IS NULL OR LOWER(p.nombre) LIKE :patron " +
            "OR LOWER(COALESCE(p.rfc, '')) LIKE :patron " +
            "OR LOWER(COALESCE(p.contacto, '')) LIKE :patron) " +
            "AND (:activo IS NULL OR p.activo = :activo)")
    Page<Proveedor> buscar(
            @Param("patron") String patron,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
