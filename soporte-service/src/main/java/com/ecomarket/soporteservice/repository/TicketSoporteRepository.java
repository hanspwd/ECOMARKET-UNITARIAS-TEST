package com.ecomarket.soporteservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.soporteservice.model.entity.TicketSoporte;

public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long>{

    List<TicketSoporte> findByClienteId(Long clienteId);

    List<TicketSoporte> findByEstadoId(Long estadoId);
    
}
