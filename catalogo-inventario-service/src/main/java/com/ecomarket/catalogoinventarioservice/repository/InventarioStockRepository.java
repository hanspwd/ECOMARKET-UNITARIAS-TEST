package com.ecomarket.catalogoinventarioservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.catalogoinventarioservice.model.InventarioStock;

public interface  InventarioStockRepository extends JpaRepository<InventarioStock, Long>{
    List<InventarioStock> findByProductoId(Long productoId);
    List<InventarioStock> findBySucursalIdAndProductoId(Long sucursalId, Long productoId);
    Optional<InventarioStock> findTopByProductoIdAndSucursalId(Long productoId, Long sucursalId);
}
