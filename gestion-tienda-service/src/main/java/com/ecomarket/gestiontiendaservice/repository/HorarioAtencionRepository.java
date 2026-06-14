package com.ecomarket.gestiontiendaservice.repository;

import com.ecomarket.gestiontiendaservice.model.HorarioAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long> {
    List<HorarioAtencion> findBySucursalId(Long sucursalId);
}