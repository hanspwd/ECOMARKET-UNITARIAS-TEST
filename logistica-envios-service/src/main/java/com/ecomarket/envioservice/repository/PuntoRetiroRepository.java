package com.ecomarket.envioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.envioservice.model.entity.PuntoRetiro;

public interface PuntoRetiroRepository extends JpaRepository<PuntoRetiro, Long>{

    List<PuntoRetiro> findByActivoTrue();
}
