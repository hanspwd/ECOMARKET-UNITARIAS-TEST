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

import com.ecomarket.soporteservice.dto.MensajeChatRequestDTO;
import com.ecomarket.soporteservice.model.entity.MensajeChat;
import com.ecomarket.soporteservice.service.MensajeChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/mensajes-chat")
public class MensajeChatController {

    @Autowired
    private MensajeChatService mensajeChatService;

    @GetMapping
    public List<MensajeChat> obtenerHistorialChat(@RequestParam(required = false) Long ticketId) {
        if (ticketId != null) {
            return mensajeChatService.obtenerHistorialChat(ticketId);
        }
        return mensajeChatService.readAllMensajes();
    }

    @GetMapping("{id}")
    public MensajeChat obtenerMensajePorId(@PathVariable Long id) {
        return mensajeChatService.findMensajeById(id);
    }

    @PostMapping
    public ResponseEntity<MensajeChat> enviarMensajeChat(@Valid @RequestBody MensajeChatRequestDTO dto) {
        MensajeChat mensaje = mensajeChatService.enviarMensajeChat(
            dto.getTicketId(), dto.getRemitenteId(), dto.getEsCliente(), dto.getContenido());
        return ResponseEntity.status(201).body(mensaje);
    }

    @PatchMapping("{id}/leido")
    public ResponseEntity<String> marcarComoLeido(@PathVariable Long id) {
        mensajeChatService.marcarComoLeido(id);
        return ResponseEntity.ok("El mensaje con id " + id + " ha sido marcado como leido.");
    }

    @PatchMapping("marcar-leidos/{ticketId}")
    public ResponseEntity<String> marcarTodosComoLeidos(@PathVariable Long ticketId) {
        mensajeChatService.marcarTodosComoLeidos(ticketId);
        return ResponseEntity.ok("Todos los mensajes del ticket " + ticketId + " han sido marcados como leidos.");
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> eliminarMensaje(@PathVariable Long id) {
        mensajeChatService.deleteMensajeById(id);
        return ResponseEntity.ok("El mensaje con id " + id + " ha sido eliminado con exito.");
    }
}
