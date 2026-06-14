package com.ecomarket.envioservice.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.envioservice.model.reference.EstadoEnvio;
import com.ecomarket.envioservice.service.EstadoEnvioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/estado-envio")
@RequiredArgsConstructor
public class EstadoEnvioController {

    private final EstadoEnvioService estadoEnvioService;

    @GetMapping
    public List<EstadoEnvio> getAll() {
        return estadoEnvioService.readAll();
    }

    @PostMapping
    public ResponseEntity<EstadoEnvio> create(@Valid @RequestBody EstadoEnvio estadoEnvio) {
        return ResponseEntity.status(201).body(estadoEnvioService.create(estadoEnvio));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        estadoEnvioService.delete(id);
        return ResponseEntity.ok("El estado envio con id " + id + " ha sido eliminado con exito.");
    }
}
