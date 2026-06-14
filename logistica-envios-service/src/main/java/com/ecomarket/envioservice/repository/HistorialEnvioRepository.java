package com.ecomarket.envioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.envioservice.model.entity.HistorialEnvio;

public interface HistorialEnvioRepository extends JpaRepository<HistorialEnvio, Long>{

    List<HistorialEnvio> findByEnvioIdOrderByFechaActualizacionAsc(Long envioId);
}
