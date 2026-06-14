package com.ecomarket.carritocompraservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.carritocompraservice.model.ItemCarrito;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long>{
    Optional<ItemCarrito> findByCarritoIdAndProductoId(Long carritoId, Long productoId);
}
