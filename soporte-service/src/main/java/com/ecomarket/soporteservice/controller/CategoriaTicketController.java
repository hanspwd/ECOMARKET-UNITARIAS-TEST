package com.ecomarket.soporteservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.soporteservice.model.reference.CategoriaTicket;
import com.ecomarket.soporteservice.service.CategoriaTicketService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/v1/categoria-ticket")
public class CategoriaTicketController {
    
    @Autowired
    private CategoriaTicketService categoriaTicketService;

    @GetMapping
    public List<CategoriaTicket> getAllCategoriaTicket() {
        return categoriaTicketService.readAllCategoriaTicket();
    }

    @PostMapping
    public ResponseEntity<?> postCategoriaTicket(@Valid @RequestBody CategoriaTicket categoriaTicket) {
        return ResponseEntity.status(201).body(categoriaTicketService.createCategoriaTicket(categoriaTicket));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteCategoriaTicketById(@PathVariable Long id) {
        categoriaTicketService.deleteCategoriaTicketById(id);
        return ResponseEntity.ok("La categoria ticket con id " + id + " ha sido eliminada con exito.");

    }
    
}
