package com.ecomarket.carritocompraservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.carritocompraservice.model.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long>{
    Optional<Carrito> findByClienteIdAndActivoTrue(Long clienteId);
}
