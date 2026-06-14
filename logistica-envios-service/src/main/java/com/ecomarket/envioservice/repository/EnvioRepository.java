package com.ecomarket.envioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.envioservice.model.entity.Envio;

public interface EnvioRepository extends JpaRepository<Envio, Long>{

    List<Envio> findByClienteId(Long clienteId);

    List<Envio> findByPedidoId(Long pedidoId);

    List<Envio> findByEstadoActualId(Long estadoId);
}
