package com.ecomarket.iniciosesion.repository;

import com.ecomarket.iniciosesion.model.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    @Query("SELECT t FROM TokenRecuperacion t " +
           "WHERE t.codigoAlfanumerico = :codigo " +
           "AND t.consumido = false " +
           "AND t.expiracion > :ahora")
    Optional<TokenRecuperacion> buscarTokenActivo(
            @Param("codigo") String codigo,
            @Param("ahora") LocalDateTime ahora);
}
