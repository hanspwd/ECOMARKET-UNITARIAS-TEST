package com.ecomarket.catalogoinventarioservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.catalogoinventarioservice.model.EstadoDisponibilidad;

public interface  EstadoDisponibilidadRepository extends JpaRepository<EstadoDisponibilidad, Long>{
    
}
