package com.ecomarket.analiticaservice.controller;

import com.ecomarket.analiticaservice.dto.AlertaRequestDTO;
import com.ecomarket.analiticaservice.model.entity.AlertaSistema;
import com.ecomarket.analiticaservice.service.AnaliticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AnaliticaService analiticaService;

    @GetMapping
    public ResponseEntity<List<AlertaSistema>> listar() {
        return ResponseEntity.ok(analiticaService.listarAlertas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaSistema> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(analiticaService.obtenerAlerta(id));
    }

    @GetMapping("/estado")
    public ResponseEntity<List<AlertaSistema>> listarPorEstado(@RequestParam Boolean resuelta) {
        return ResponseEntity.ok(analiticaService.listarAlertasPorEstado(resuelta));
    }

    @PostMapping
    public ResponseEntity<AlertaSistema> crear(@Valid @RequestBody AlertaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.crearAlerta(request));
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<AlertaSistema> resolver(@PathVariable Long id) {
        return ResponseEntity.ok(analiticaService.resolverAlerta(id));
    }
}
