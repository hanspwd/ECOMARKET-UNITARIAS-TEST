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
import com.ecomarket.soporteservice.dto.NotificacionRequestDTO;
import com.ecomarket.soporteservice.dto.ResenaRequestDTO;
import com.ecomarket.soporteservice.dto.SoporteTicketRequestDTO;
import com.ecomarket.soporteservice.model.entity.MensajeChat;
import com.ecomarket.soporteservice.model.entity.Notificacion;
import com.ecomarket.soporteservice.model.entity.Resena;
import com.ecomarket.soporteservice.model.entity.TicketSoporte;
import com.ecomarket.soporteservice.service.NotificacionService;
import com.ecomarket.soporteservice.service.ResenaService;
import com.ecomarket.soporteservice.service.SoporteService;
import com.ecomarket.soporteservice.service.TicketSoporteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/soporte")
public class SoporteController {

    @Autowired
    private SoporteService soporteService;

    @Autowired
    private TicketSoporteService ticketSoporteService;

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private NotificacionService notificacionService;

    @PostMapping("enviar-notificacion-push")
    public ResponseEntity<Notificacion> enviarNotificacionPush(@Valid @RequestBody NotificacionRequestDTO dto) {
        Notificacion notificacion = soporteService.enviarNotificacionPush(
            dto.getDestinatarioId(), dto.getTitulo(), dto.getMensaje(), dto.getCanalId());
        return ResponseEntity.ok(notificacion);
    }

    @PostMapping("ingresar-ticket")
    public ResponseEntity<TicketSoporte> ingresarTicket(@Valid @RequestBody SoporteTicketRequestDTO dto) throws Exception {
        TicketSoporte ticket = soporteService.ingresarTicket(
            dto.getClienteId(), dto.getCategoriaId(), dto.getAsunto(), dto.getPedidoId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("tickets")
    public List<TicketSoporte> obtenerTickets(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long estadoId) {
        if (clienteId != null) {
            return ticketSoporteService.readTicketsByClienteId(clienteId);
        }
        if (estadoId != null) {
            return ticketSoporteService.readTicketsByEstadoId(estadoId);
        }
        return ticketSoporteService.readAllTickets();
    }

    @GetMapping("tickets/{id}")
    public TicketSoporte obtenerTicketPorId(@PathVariable Long id) {
        return ticketSoporteService.findTicketById(id);
    }

    @PatchMapping("tickets/{id}/estado/{nuevoEstadoId}")
    public ResponseEntity<TicketSoporte> actualizarEstadoTicket(
            @PathVariable Long id, @PathVariable Long nuevoEstadoId) {
        TicketSoporte ticket = ticketSoporteService.actualizarEstadoTicket(id, nuevoEstadoId);
        return ResponseEntity.ok(ticket);
    }

    @PatchMapping("tickets/{id}/asignar/{empleadoId}")
    public ResponseEntity<TicketSoporte> asignarTicketEmpleado(
            @PathVariable Long id, @PathVariable Long empleadoId) {
        TicketSoporte ticket = soporteService.asignarTicketEmpleado(id, empleadoId);
        return ResponseEntity.ok(ticket);
    }

    @PatchMapping("tickets/{id}/solucionar")
    public ResponseEntity<TicketSoporte> solucionarTicket(
            @PathVariable Long id, @RequestBody String solucionResumen) {
        TicketSoporte ticket = soporteService.solucionarTicket(id, solucionResumen);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("tickets/{id}")
    public ResponseEntity<String> eliminarTicket(@PathVariable Long id) {
        ticketSoporteService.deleteTicketById(id);
        return ResponseEntity.ok("El ticket con id " + id + " ha sido eliminado con exito.");
    }

    @GetMapping("tickets/{id}/mensajes")
    public List<MensajeChat> obtenerHistorialChat(@PathVariable Long id) {
        return soporteService.obtenerHistorialChat(id);
    }

    @PostMapping("enviar-mensaje-chat")
    public ResponseEntity<MensajeChat> enviarMensajeChat(@Valid @RequestBody MensajeChatRequestDTO dto) {
        MensajeChat mensaje = soporteService.enviarMensajeChat(
            dto.getTicketId(), dto.getRemitenteId(), dto.getEsCliente(), dto.getContenido());
        return ResponseEntity.status(201).body(mensaje);
    }

    @GetMapping("notificaciones")
    public List<Notificacion> obtenerNotificaciones(
            @RequestParam(required = false) Long destinatarioId) {
        if (destinatarioId != null) {
            return notificacionService.readNotificacionesByDestinatarioId(destinatarioId);
        }
        return notificacionService.readAllNotificacion();
    }

    @GetMapping("notificaciones/{id}")
    public Notificacion obtenerNotificacionPorId(@PathVariable Long id) {
        return notificacionService.findNotificacionById(id);
    }

    @DeleteMapping("notificaciones/{id}")
    public ResponseEntity<String> eliminarNotificacion(@PathVariable Long id) {
        notificacionService.deleteNotificacionById(id);
        return ResponseEntity.ok("La notificacion con id " + id + " ha sido eliminada con exito.");
    }

    @PostMapping("dejar-resena")
    public ResponseEntity<Resena> dejarResena(@Valid @RequestBody ResenaRequestDTO dto) {
        Resena resena = soporteService.dejarResena(
            dto.getProductoId(), dto.getClienteId(), dto.getCalificacionEstrellas(), dto.getComentario());
        return ResponseEntity.status(201).body(resena);
    }

    @GetMapping("resenas")
    public List<Resena> obtenerResenas(
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

    @PatchMapping("resenas/{id}/aprobar")
    public ResponseEntity<String> aprobarResena(@PathVariable Long id) {
        resenaService.aprobarModeracion(id);
        return ResponseEntity.ok("La resena con id " + id + " ha sido aprobada.");
    }

    @PatchMapping("resenas/{id}/rechazar")
    public ResponseEntity<String> rechazarResena(@PathVariable Long id) {
        resenaService.rechazarModeracion(id);
        return ResponseEntity.ok("La resena con id " + id + " ha sido rechazada.");
    }

    @DeleteMapping("resenas/{id}")
    public ResponseEntity<String> eliminarResena(@PathVariable Long id) {
        resenaService.deleteResenaById(id);
        return ResponseEntity.ok("La resena con id " + id + " ha sido eliminada con exito.");
    }
}
