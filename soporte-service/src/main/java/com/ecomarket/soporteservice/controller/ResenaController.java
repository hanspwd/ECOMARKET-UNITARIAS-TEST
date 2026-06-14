package com.ecomarket.soporteservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.soporteservice.dto.ResenaRequestDTO;
import com.ecomarket.soporteservice.model.entity.Resena;
import com.ecomarket.soporteservice.service.ResenaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    @GetMapping
    public List<Resena> getAllResenas(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long clienteId) {
        if (productoId != null) {
            return resenaService.readResenasByProductoId(productoId);
        }
        if (clienteId != null) {
            return resenaService.readResenasByClienteId(clienteId);
        }
        return resenaService.readAllResenas();
    }

    @GetMapping("{id}")
    public Resena getResenaById(@PathVariable Long id) {
        return resenaService.findResenaById(id);
    }

    @PostMapping
    public ResponseEntity<Resena> dejarResena(@Valid @RequestBody ResenaRequestDTO dto) {
        Resena resena = resenaService.dejarResena(
            dto.getProductoId(), dto.getClienteId(), dto.getCalificacionEstrellas(), dto.getComentario());
        return ResponseEntity.status(201).body(resena);
    }

    @PatchMapping("{id}/aprobar")
    public ResponseEntity<String> aprobarResena(@PathVariable Long id) {
        resenaService.aprobarModeracion(id);
        return ResponseEntity.ok("La resena con id " + id + " ha sido aprobada.");
    }

    @PatchMapping("{id}/rechazar")
    public ResponseEntity<String> rechazarResena(@PathVariable Long id) {
        resenaService.rechazarModeracion(id);
        return ResponseEntity.ok("La resena con id " + id + " ha sido rechazada.");
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteResenaById(@PathVariable Long id) {
        resenaService.deleteResenaById(id);
        return ResponseEntity.ok("La resena con id " + id + " ha sido eliminada con exito.");
    }
}
