package com.kristianconk.api_papeleria.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromocionClienteRepository extends JpaRepository<PromocionCliente, Long> {
}
