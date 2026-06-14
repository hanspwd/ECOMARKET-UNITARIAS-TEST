package com.ecomarket.soporteservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.soporteservice.model.reference.CategoriaTicket;

public interface CategoriaTicketRepository extends JpaRepository<CategoriaTicket, Long>{
    
    Optional<CategoriaTicket> findByNombre(String nombre);

}
