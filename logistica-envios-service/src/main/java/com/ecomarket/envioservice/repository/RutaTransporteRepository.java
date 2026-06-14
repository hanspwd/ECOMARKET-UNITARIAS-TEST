package com.ecomarket.envioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.envioservice.model.entity.RutaTransporte;

public interface RutaTransporteRepository extends JpaRepository<RutaTransporte, Long>{

    List<RutaTransporte> findByTransportistaId(Long transportistaId);
}
