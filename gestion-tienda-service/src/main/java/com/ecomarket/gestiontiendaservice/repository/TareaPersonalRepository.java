package com.ecomarket.gestiontiendaservice.repository;

import com.ecomarket.gestiontiendaservice.model.TareaPersonal;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface TareaPersonalRepository extends JpaRepository<TareaPersonal, Long> {
    List<TareaPersonal> findByEmpleadoId(Long empleadoId);
    List<TareaPersonal> findBySucursalId(Long sucursalId);
}