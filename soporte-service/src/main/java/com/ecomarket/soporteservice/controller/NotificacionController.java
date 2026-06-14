package com.ecomarket.soporteservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.soporteservice.model.entity.Notificacion;
import com.ecomarket.soporteservice.service.NotificacionService;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public List<Notificacion> getAllNotificaciones(@RequestParam(required = false) Long destinatarioId) {
        if (destinatarioId != null) {
            return notificacionService.readNotificacionesByDestinatarioId(destinatarioId);
        }
        return notificacionService.readAllNotificacion();
    }

    @GetMapping("{id}")
    public Notificacion getNotificacionById(@PathVariable Long id) {
        return notificacionService.findNotificacionById(id);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteNotificacionById(@PathVariable Long id) {
        notificacionService.deleteNotificacionById(id);
        return ResponseEntity.ok("La notificacion con id " + id + " ha sido eliminada con exito.");
    }
}
