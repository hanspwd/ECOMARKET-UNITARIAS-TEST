package com.ecomarket.carritocompraservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.carritocompraservice.model.EstadoPedido;

public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Long>{
    
}
