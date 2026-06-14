package com.ecomarket.gestiontiendaservice.repository;

import com.ecomarket.gestiontiendaservice.model.ReglamentoInterno;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface ReglamentoInternoRepository extends JpaRepository<ReglamentoInterno, Long> {
    Optional<ReglamentoInterno> findTopBySucursalIdOrderByVersionDesc(Long sucursalId);
}