package com.ecomarket.catalogoinventarioservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomarket.catalogoinventarioservice.model.CategoriaProducto;
import com.ecomarket.catalogoinventarioservice.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long>{
    List<Producto> findByCategoria(CategoriaProducto categoria);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
