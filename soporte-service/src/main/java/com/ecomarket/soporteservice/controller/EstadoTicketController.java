package com.ecomarket.soporteservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.soporteservice.model.reference.EstadoTicket;
import com.ecomarket.soporteservice.service.EstadoTicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/estado-ticket")
public class EstadoTicketController {
        
    @Autowired
    private EstadoTicketService estadoTicketService;

    @GetMapping
    public List<EstadoTicket> getAllEstadoTicket() {
        return estadoTicketService.readAllEstadoTicket();
    }

    @PostMapping
    public ResponseEntity<?> postEstadoTicket(@Valid @RequestBody EstadoTicket estadoTicket) {
        return ResponseEntity.status(201).body(estadoTicketService.createEstadoTicket(estadoTicket));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteEstadoTicketById(@PathVariable Long id) {
        estadoTicketService.deleteEstadoTicket(id);
        return ResponseEntity.ok("El estado ticket con id " + id + " ha sido eliminado con exito.");

    }

}
