package com.ecomarket.envioservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.envioservice.model.reference.MetodoEnvio;

public interface MetodoEnvioRepository extends JpaRepository<MetodoEnvio, Long>{

    Optional<MetodoEnvio> findByNombre(String nombre);
}
