package com.ecomarket.procesopagoservice.repository;

import com.ecomarket.procesopagoservice.model.MetodoPagoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPagoTransaccion, Long> {
    Optional<MetodoPagoTransaccion> findByNombre(String nombre);
}
