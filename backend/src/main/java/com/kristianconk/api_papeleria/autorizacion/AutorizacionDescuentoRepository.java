package com.kristianconk.api_papeleria.autorizacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AutorizacionDescuentoRepository extends JpaRepository<AutorizacionDescuento, Long> {

    Optional<AutorizacionDescuento> findByTokenHash(String tokenHash);

    /**
     * Marca la autorización como consumida solo si sigue vigente y no ha sido usada,
     * en una única sentencia UPDATE condicional. Devuelve la cantidad de filas
     * afectadas: 1 si esta llamada ganó el consumo, 0 si ya estaba consumida o expiró
     * (incluyendo el caso de dos consumos concurrentes, donde solo uno puede ganar).
     */
    @Modifying
    @Query("UPDATE AutorizacionDescuento a SET a.consumida = true, a.fechaConsumo = :ahora "
            + "WHERE a.id = :id AND a.consumida = false AND a.fechaExpiracion > :ahora")
    int consumirSiVigente(@Param("id") Long id, @Param("ahora") LocalDateTime ahora);
}
