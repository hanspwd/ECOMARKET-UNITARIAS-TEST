package com.ecomarket.soporteservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.soporteservice.model.reference.CanalNotificacion;

public interface CanalNotificacionRepository extends JpaRepository<CanalNotificacion, Long> {
    
    Optional<CanalNotificacion> findByNombre(String nombre);
}
