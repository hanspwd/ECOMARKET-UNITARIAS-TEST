package com.ecomarket.analiticaservice.repository;

import com.ecomarket.analiticaservice.model.entity.MetricaDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetricaDashboardRepository extends JpaRepository<MetricaDashboard, Long> {
    Optional<MetricaDashboard> findByClaveMetrica(String claveMetrica);
}
