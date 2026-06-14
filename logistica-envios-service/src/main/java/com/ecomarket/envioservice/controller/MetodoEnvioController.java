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

import com.ecomarket.envioservice.model.reference.MetodoEnvio;
import com.ecomarket.envioservice.service.MetodoEnvioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/metodo-envio")
@RequiredArgsConstructor
public class MetodoEnvioController {

    private final MetodoEnvioService metodoEnvioService;

    @GetMapping
    public List<MetodoEnvio> getAll() {
        return metodoEnvioService.readAll();
    }

    @PostMapping
    public ResponseEntity<MetodoEnvio> create(@Valid @RequestBody MetodoEnvio metodoEnvio) {
        return ResponseEntity.status(201).body(metodoEnvioService.create(metodoEnvio));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        metodoEnvioService.delete(id);
        return ResponseEntity.ok("El metodo envio con id " + id + " ha sido eliminado con exito.");
    }
}
