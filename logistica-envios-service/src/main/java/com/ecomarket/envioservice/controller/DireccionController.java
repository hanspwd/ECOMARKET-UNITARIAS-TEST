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

import com.ecomarket.envioservice.model.entity.Direccion;
import com.ecomarket.envioservice.service.DireccionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/direcciones")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;

    @GetMapping
    public List<Direccion> getAll() {
        return direccionService.readAll();
    }

    @GetMapping("{id}")
    public Direccion getById(@PathVariable Long id) {
        return direccionService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Direccion> create(@Valid @RequestBody Direccion direccion) {
        return ResponseEntity.status(201).body(direccionService.create(direccion));
    }

    @PutMapping("{id}")
    public ResponseEntity<Direccion> update(@PathVariable Long id, @Valid @RequestBody Direccion direccion) {
        return ResponseEntity.ok(direccionService.update(id, direccion));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        direccionService.delete(id);
        return ResponseEntity.ok("La direccion con id " + id + " ha sido eliminada con exito.");
    }
}
