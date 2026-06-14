package com.ecomarket.catalogoinventarioservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.catalogoinventarioservice.model.Producto;
import com.ecomarket.catalogoinventarioservice.service.CatalogoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class ProductoController {
    private final CatalogoService catalogoService;

    // Retorna todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(catalogoService.listarTodos());
    }

    // Retorna la lista de productos filtrados por categoría
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> navegarCatalogo(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(catalogoService.navegarCatalogo(categoriaId));
    }

    // Busca productos cuyo nombre contenga el texto ingresado
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(catalogoService.buscarCoincidenciaPorNombre(nombre));
    }

    // Retorna los detalles de un producto por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.consultarDetalles(id));
    }

    // Crea y guarda un nuevo producto en la base de datos
    @PostMapping
    public ResponseEntity<Producto> agregarProducto(@RequestBody Producto producto) {
        return ResponseEntity.ok(catalogoService.agregarProducto(producto));
    }

    // Actualiza los datos de un producto existente por su ID
    @PutMapping("/{id}")
    public ResponseEntity<Producto> editarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        return ResponseEntity.ok(catalogoService.editarProducto(id, producto));
    }

    // Elimina un producto de la base de datos por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> eliminarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarProducto(id));
    }
}
