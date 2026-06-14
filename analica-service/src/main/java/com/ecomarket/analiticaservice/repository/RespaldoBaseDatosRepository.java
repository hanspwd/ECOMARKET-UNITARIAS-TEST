package com.ecomarket.analiticaservice.repository;

import com.ecomarket.analiticaservice.model.entity.RespaldoBaseDatos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RespaldoBaseDatosRepository extends JpaRepository<RespaldoBaseDatos, Long> {
    List<RespaldoBaseDatos> findByFechaRespaldoBetween(LocalDateTime inicio, LocalDateTime fin);
}
