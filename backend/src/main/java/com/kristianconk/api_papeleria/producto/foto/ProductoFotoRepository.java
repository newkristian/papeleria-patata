package com.kristianconk.api_papeleria.producto.foto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductoFotoRepository extends JpaRepository<ProductoFoto, Long> {

    List<ProductoFoto> findByProductoIdOrderByOrdenAsc(Long productoId);

    Optional<ProductoFoto> findByProductoIdAndEsPrincipalTrue(Long productoId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductoFoto pf SET pf.esPrincipal = false WHERE pf.producto.id = :productoId")
    void resetPrincipalByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT COUNT(pf) FROM ProductoFoto pf WHERE pf.producto.id = :productoId")
    int countByProductoId(@Param("productoId") Long productoId);
}
