package com.ecomarket.analiticaservice.controller;

import com.ecomarket.analiticaservice.dto.MetricaRequestDTO;
import com.ecomarket.analiticaservice.model.entity.MetricaDashboard;
import com.ecomarket.analiticaservice.service.AnaliticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/metricas")
@RequiredArgsConstructor
public class MetricaController {

    private final AnaliticaService analiticaService;

    @GetMapping
    public ResponseEntity<List<MetricaDashboard>> listar() {
        return ResponseEntity.ok(analiticaService.listarMetricas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetricaDashboard> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(analiticaService.obtenerMetricaPorId(id));
    }

    @GetMapping("/clave/{clave}")
    public ResponseEntity<MetricaDashboard> obtenerPorClave(@PathVariable String clave) {
        return ResponseEntity.ok(analiticaService.obtenerMetricaPorClave(clave));
    }

    @PostMapping
    public ResponseEntity<MetricaDashboard> crear(@Valid @RequestBody MetricaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.crearMetrica(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetricaDashboard> actualizar(@PathVariable Long id, @Valid @RequestBody MetricaRequestDTO request) {
        return ResponseEntity.ok(analiticaService.actualizarMetrica(id, request));
    }
}
