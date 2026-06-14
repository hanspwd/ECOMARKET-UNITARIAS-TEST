package com.ecomarket.iniciosesion.repository;

import com.ecomarket.iniciosesion.model.SesionJWT;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionJWTRepository extends JpaRepository<SesionJWT, Long> {

    boolean existsByToken(String token);

    void deleteByUsuarioId(Long usuarioId);
}
