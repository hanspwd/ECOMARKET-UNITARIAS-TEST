package com.ecomarket.catalogoinventarioservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.catalogoinventarioservice.model.CategoriaProducto;

public interface CategoriaProductoRepository  extends JpaRepository<CategoriaProducto, Long>{
    
}
