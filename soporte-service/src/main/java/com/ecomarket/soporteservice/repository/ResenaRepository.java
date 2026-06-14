package com.ecomarket.soporteservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.soporteservice.model.entity.Resena;

public interface ResenaRepository extends JpaRepository<Resena, Long>{

    List<Resena> findByProductoId(Long productoId);

    List<Resena> findByClienteId(Long clienteId);

    List<Resena> findByModeracionAprobado(Boolean moderacionAprobado);
    
}
