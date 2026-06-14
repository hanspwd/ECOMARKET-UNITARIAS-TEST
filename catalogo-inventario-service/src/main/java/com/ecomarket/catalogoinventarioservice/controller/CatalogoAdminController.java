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
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.catalogoinventarioservice.model.CategoriaProducto;
import com.ecomarket.catalogoinventarioservice.model.EspecificacionTecnica;
import com.ecomarket.catalogoinventarioservice.model.EstadoDisponibilidad;
import com.ecomarket.catalogoinventarioservice.service.CatalogoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalogo-admin")
@RequiredArgsConstructor
public class CatalogoAdminController {

    private final CatalogoService catalogoService;

    // --- CategoriaProducto ---

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaProducto>> listarCategorias() {
        return ResponseEntity.ok(catalogoService.listarCategorias());
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<CategoriaProducto> obtenerCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.obtenerCategoria(id));
    }

    @PostMapping("/categoria")
    public ResponseEntity<CategoriaProducto> crearCategoria(@Valid @RequestBody CategoriaProducto categoria) {
        return ResponseEntity.ok(catalogoService.crearCategoria(categoria));
    }

    @PutMapping("/categoria/{id}")
    public ResponseEntity<CategoriaProducto> editarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaProducto categoria) {
        return ResponseEntity.ok(catalogoService.editarCategoria(id, categoria));
    }

    @DeleteMapping("/categoria/{id}")
    public ResponseEntity<Boolean> eliminarCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarCategoria(id));
    }

    // --- EstadoDisponibilidad ---

    @GetMapping("/estados")
    public ResponseEntity<List<EstadoDisponibilidad>> listarEstados() {
        return ResponseEntity.ok(catalogoService.listarEstados());
    }

    @GetMapping("/estado/{id}")
    public ResponseEntity<EstadoDisponibilidad> obtenerEstado(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.obtenerEstado(id));
    }

    @PostMapping("/estado")
    public ResponseEntity<EstadoDisponibilidad> crearEstado(@Valid @RequestBody EstadoDisponibilidad estado) {
        return ResponseEntity.ok(catalogoService.crearEstado(estado));
    }

    @PutMapping("/estado/{id}")
    public ResponseEntity<EstadoDisponibilidad> editarEstado(@PathVariable Long id, @Valid @RequestBody EstadoDisponibilidad estado) {
        return ResponseEntity.ok(catalogoService.editarEstado(id, estado));
    }

    @DeleteMapping("/estado/{id}")
    public ResponseEntity<Boolean> eliminarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarEstado(id));
    }

    // --- EspecificacionTecnica ---

    @GetMapping("/especificaciones")
    public ResponseEntity<List<EspecificacionTecnica>> listarEspecificaciones() {
        return ResponseEntity.ok(catalogoService.listarEspecificaciones());
    }

    @GetMapping("/especificaciones/producto/{productoId}")
    public ResponseEntity<List<EspecificacionTecnica>> listarEspecificacionesPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(catalogoService.listarEspecificacionesPorProducto(productoId));
    }

    @GetMapping("/especificaciones/{id}")
    public ResponseEntity<EspecificacionTecnica> obtenerEspecificacion(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.obtenerEspecificacion(id));
    }

    @PostMapping("/especificaciones")
    public ResponseEntity<EspecificacionTecnica> crearEspecificacion(@Valid @RequestBody EspecificacionTecnica especificacion) {
        return ResponseEntity.ok(catalogoService.crearEspecificacion(especificacion));
    }

    @PutMapping("/especificaciones/{id}")
    public ResponseEntity<EspecificacionTecnica> editarEspecificacion(@PathVariable Long id, @Valid @RequestBody EspecificacionTecnica especificacion) {
        return ResponseEntity.ok(catalogoService.editarEspecificacion(id, especificacion));
    }

    @DeleteMapping("/especificaciones/{id}")
    public ResponseEntity<Boolean> eliminarEspecificacion(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarEspecificacion(id));
    }
}
