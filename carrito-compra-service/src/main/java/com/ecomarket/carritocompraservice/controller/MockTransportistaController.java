package com.ecomarket.carritocompraservice.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mock/transportistas")
public class MockTransportistaController {

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerTransportista(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
            "usuarioId", id,
            "nombre", "Transportista",
            "apellido", "Mock",
            "correo", "transportista" + id + "@ecomarket.cl"
        ));
    }
}
