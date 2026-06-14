package com.ecomarket.analiticaservice.controller;

import com.ecomarket.analiticaservice.dto.RespaldoRequestDTO;
import com.ecomarket.analiticaservice.model.entity.RespaldoBaseDatos;
import com.ecomarket.analiticaservice.service.AnaliticaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/respaldos")
@RequiredArgsConstructor
public class RespaldoController {

    private final AnaliticaService analiticaService;

    @GetMapping
    public ResponseEntity<List<RespaldoBaseDatos>> listar() {
        return ResponseEntity.ok(analiticaService.listarRespaldos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespaldoBaseDatos> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(analiticaService.obtenerRespaldo(id));
    }

    @PostMapping
    public ResponseEntity<RespaldoBaseDatos> ejecutar(@Valid @RequestBody RespaldoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(analiticaService.ejecutarRespaldo(request));
    }
}
