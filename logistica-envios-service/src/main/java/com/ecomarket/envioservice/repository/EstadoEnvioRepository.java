package com.ecomarket.envioservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.envioservice.model.reference.EstadoEnvio;

public interface EstadoEnvioRepository extends JpaRepository<EstadoEnvio, Long>{

    Optional<EstadoEnvio> findByNombre(String nombre);
}
