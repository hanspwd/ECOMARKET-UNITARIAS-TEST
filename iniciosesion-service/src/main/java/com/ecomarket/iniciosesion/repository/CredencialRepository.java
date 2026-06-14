package com.ecomarket.iniciosesion.repository;

import com.ecomarket.iniciosesion.model.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByCorreoAcceso(String correoAcceso);

    Optional<Credencial> findByUsuarioId(Long usuarioId);

    boolean existsByCorreoAcceso(String correoAcceso);
}
