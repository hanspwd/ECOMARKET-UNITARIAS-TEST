package com.ecomarket.procesopagoservice.repository;

import com.ecomarket.procesopagoservice.model.FacturaElectronica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<FacturaElectronica, Long> {
    Optional<FacturaElectronica> findByTransaccionId(Long transaccionId);
}
