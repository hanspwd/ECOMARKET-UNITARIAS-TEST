package com.ecomarket.carritocompraservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.carritocompraservice.dto.CompraRequestDTO;
import com.ecomarket.carritocompraservice.dto.CompraResultDTO;
import com.ecomarket.carritocompraservice.service.CompraOrchestratorService;

@RestController
@RequestMapping("/api/compra")
public class CompraController {

    @Autowired
    private CompraOrchestratorService compraOrchestratorService;

    @PostMapping("/finalizar")
    public ResponseEntity<CompraResultDTO> finalizarCompra(@RequestBody CompraRequestDTO request) {
        CompraResultDTO resultado = compraOrchestratorService.ejecutarCompra(request);
        return ResponseEntity.ok(resultado);
    }
}
