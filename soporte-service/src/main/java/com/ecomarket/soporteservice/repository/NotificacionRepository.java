package com.ecomarket.soporteservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.soporteservice.model.entity.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long>{

    List<Notificacion> findByDestinatarioId(Long destinatarioId);

    List<Notificacion> findByEnviadaConExito(Boolean enviadaConExito);
    
}
