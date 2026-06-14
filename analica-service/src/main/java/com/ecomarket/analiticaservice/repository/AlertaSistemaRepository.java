package com.ecomarket.analiticaservice.repository;

import com.ecomarket.analiticaservice.model.entity.AlertaSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaSistemaRepository extends JpaRepository<AlertaSistema, Long> {
    List<AlertaSistema> findByResuelta(Boolean resuelta);
    List<AlertaSistema> findByModuloOrigen(String moduloOrigen);
}
