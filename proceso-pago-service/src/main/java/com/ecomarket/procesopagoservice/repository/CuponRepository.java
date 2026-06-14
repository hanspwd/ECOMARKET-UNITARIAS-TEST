package com.ecomarket.procesopagoservice.repository;

import com.ecomarket.procesopagoservice.model.CuponDescuento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CuponRepository extends JpaRepository<CuponDescuento, Long> {
    Optional<CuponDescuento> findByCodigo(String codigo);
}
