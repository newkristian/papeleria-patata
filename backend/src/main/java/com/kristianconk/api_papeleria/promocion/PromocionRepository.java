package com.kristianconk.api_papeleria.promocion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    List<Promocion> findByProductoId(Long productoId);

    List<Promocion> findByCategoriaId(Long categoriaId);

    long countByCategoriaId(Long categoriaId);
}
