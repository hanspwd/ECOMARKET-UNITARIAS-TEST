package com.ecomarket.soporteservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.model.entity.MensajeChat;
import com.ecomarket.soporteservice.repository.MensajeChatRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MensajeChatService {

    @Autowired
    private MensajeChatRepository mensajeChatRepository;

    public List<MensajeChat> readAllMensajes() {
        return mensajeChatRepository.findAll();
    }

    public List<MensajeChat> obtenerHistorialChat(Long ticketId) {
        return mensajeChatRepository.findByTicketIdOrderByFechaEnvioAsc(ticketId);
    }

    public MensajeChat enviarMensajeChat(Long ticketId, Long remitenteId, Boolean esCliente, String contenido) {
        MensajeChat mensaje = new MensajeChat();
        mensaje.setTicketId(ticketId);
        mensaje.setRemitenteId(remitenteId);
        mensaje.setEsCliente(esCliente);
        mensaje.setContenido(contenido.trim());
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(false);
        return mensajeChatRepository.save(mensaje);
    }

    public void marcarComoLeido(Long mensajeId) {
        MensajeChat mensaje = mensajeChatRepository.findById(mensajeId)
            .orElseThrow(() -> new NoExisteEnBdException("El mensaje con id " + mensajeId + " no existe en la DB."));
        mensaje.setLeido(true);
        mensajeChatRepository.save(mensaje);
    }

    public void marcarTodosComoLeidos(Long ticketId) {
        List<MensajeChat> mensajes = obtenerHistorialChat(ticketId);
        mensajes.forEach(m -> m.setLeido(true));
        mensajeChatRepository.saveAll(mensajes);
    }

    public MensajeChat findMensajeById(Long id) {
        return mensajeChatRepository.findById(id)
            .orElseThrow(() -> new NoExisteEnBdException("El mensaje con id " + id + " no existe en la DB."));
    }

    public void deleteMensajeById(Long id) {
        MensajeChat existente = mensajeChatRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("El mensaje con id " + id + " no se puede borrar debido a que no existe en la BD.");
        }
        mensajeChatRepository.deleteById(id);
    }
}
