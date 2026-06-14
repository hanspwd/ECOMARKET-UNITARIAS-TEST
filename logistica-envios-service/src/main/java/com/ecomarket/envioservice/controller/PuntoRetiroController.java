package com.ecomarket.envioservice.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.envioservice.model.entity.PuntoRetiro;
import com.ecomarket.envioservice.service.PuntoRetiroService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/puntos-retiro")
@RequiredArgsConstructor
public class PuntoRetiroController {

    private final PuntoRetiroService puntoRetiroService;

    @GetMapping
    public List<PuntoRetiro> getAll() {
        return puntoRetiroService.readAll();
    }

    @GetMapping("activos")
    public List<PuntoRetiro> getActivos() {
        return puntoRetiroService.readActivos();
    }

    @GetMapping("{id}")
    public PuntoRetiro getById(@PathVariable Long id) {
        return puntoRetiroService.findById(id);
    }

    @PostMapping
    public ResponseEntity<PuntoRetiro> create(@Valid @RequestBody PuntoRetiro puntoRetiro) {
        return ResponseEntity.status(201).body(puntoRetiroService.create(puntoRetiro));
    }

    @PutMapping("{id}")
    public ResponseEntity<PuntoRetiro> update(@PathVariable Long id, @Valid @RequestBody PuntoRetiro puntoRetiro) {
        return ResponseEntity.ok(puntoRetiroService.update(id, puntoRetiro));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        puntoRetiroService.delete(id);
        return ResponseEntity.ok("El punto de retiro con id " + id + " ha sido eliminado con exito.");
    }
}
