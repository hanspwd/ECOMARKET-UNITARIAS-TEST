package com.ecomarket.soporteservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.soporteservice.model.reference.EstadoTicket;

public interface EstadoTicketRepository extends JpaRepository<EstadoTicket, Long>{
    
    Optional<EstadoTicket> findByNombre(String nombre);
}
