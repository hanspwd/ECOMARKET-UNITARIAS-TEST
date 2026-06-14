package com.ecomarket.analiticaservice.repository;

import com.ecomarket.analiticaservice.model.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findBySolicitanteId(Long solicitanteId);
    List<Reporte> findByFechaGeneracionBetween(LocalDateTime inicio, LocalDateTime fin);
}
