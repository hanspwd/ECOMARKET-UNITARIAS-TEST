package com.horacio.ecomarket.usuarios.repository;

import com.horacio.ecomarket.usuarios.model.EstadoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoPerfilRepository extends JpaRepository<EstadoPerfil, Long> {

    Optional<EstadoPerfil> findByNombre(String nombre);
}
