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

import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.service.CanalNotificacionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/canal-notificacion")
public class CanalNotificacionController {
    
    @Autowired
    private CanalNotificacionService canalNotificacionService;

    @GetMapping
    public List<CanalNotificacion> getAllCanalNotificacion() {
        return canalNotificacionService.readAllCanalNotificacion();
    }

    @PostMapping
    public ResponseEntity<?> postCanalNotificacion(@Valid @RequestBody CanalNotificacion canalNotificacion) {
        return ResponseEntity.status(201).body(canalNotificacionService.createCanalNotificacion(canalNotificacion));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteCanalNotificacionById(@PathVariable Long id) {
        canalNotificacionService.deleteCanalNotificacionById(id);
        return ResponseEntity.ok("El canal notificacoin con id " + id + " ha sido eliminado con exito.");

    }

}
