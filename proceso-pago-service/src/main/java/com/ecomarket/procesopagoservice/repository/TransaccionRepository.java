package com.ecomarket.procesopagoservice.repository;

import com.ecomarket.procesopagoservice.model.TransaccionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<TransaccionPago, Long> {
    List<TransaccionPago> findByClienteId(Long clienteId);
    List<TransaccionPago> findByPedidoId(Long pedidoId);
}
