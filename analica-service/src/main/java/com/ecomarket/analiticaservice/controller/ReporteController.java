package com.ecomarket.analiticaservice.controller;

import com.ecomarket.analiticaservice.dto.ReporteFechaRequestDTO;
import com.ecomarket.analiticaservice.dto.ReporteRequestDTO;
import com.ecomarket.analiticaservice.model.entity.Reporte;
import com.ecomarket.analiticaservice.service.AnaliticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final AnaliticaService analiticaService;

    @GetMapping
    public ResponseEntity<List<Reporte>> listar() {
        return ResponseEntity.ok(analiticaService.listarReportes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reporte> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(analiticaService.obtenerReporte(id));
    }

    @GetMapping("/solicitante/{solicitanteId}")
    public ResponseEntity<List<Reporte>> listarPorSolicitante(@PathVariable Long solicitanteId) {
        return ResponseEntity.ok(analiticaService.listarReportesPorSolicitante(solicitanteId));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<Reporte>> listarPorRango(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        return ResponseEntity.ok(analiticaService.listarReportesPorRangoFechas(inicio, fin));
    }

    @PostMapping
    public ResponseEntity<Reporte> generar(@Valid @RequestBody ReporteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReporte(request));
    }

    @PostMapping("/rango")
    public ResponseEntity<Reporte> generarPorRango(@Valid @RequestBody ReporteFechaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReportePorRango(request));
    }

    @PostMapping("/usuarios/{solicitanteId}")
    public ResponseEntity<Reporte> generarReporteUsuarios(@PathVariable Long solicitanteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReporteUsuarios(solicitanteId));
    }

    @PostMapping("/pedidos/{solicitanteId}")
    public ResponseEntity<Reporte> generarReportePedidos(
            @PathVariable Long solicitanteId,
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(analiticaService.generarReportePedidos(solicitanteId, fechaInicio, fechaFin));
    }

    @PostMapping("/inventario/{solicitanteId}")
    public ResponseEntity<Reporte> generarReporteInventario(@PathVariable Long solicitanteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReporteInventario(solicitanteId));
    }

    @PostMapping("/pagos/{solicitanteId}")
    public ResponseEntity<Reporte> generarReportePagos(@PathVariable Long solicitanteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReportePagos(solicitanteId));
    }

    @PostMapping("/carrito/{solicitanteId}")
    public ResponseEntity<Reporte> generarReporteCarrito(@PathVariable Long solicitanteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReporteCarrito(solicitanteId));
    }

    @PostMapping("/soporte/{solicitanteId}")
    public ResponseEntity<Reporte> generarReporteSoporte(@PathVariable Long solicitanteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReporteSoporte(solicitanteId));
    }

    @PostMapping("/envios/{solicitanteId}")
    public ResponseEntity<Reporte> generarReporteEnvios(@PathVariable Long solicitanteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReporteEnvios(solicitanteId));
    }

    @PostMapping("/completo/{solicitanteId}")
    public ResponseEntity<Reporte> generarReporteCompleto(@PathVariable Long solicitanteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.generarReporteCompleto(solicitanteId));
    }
}
