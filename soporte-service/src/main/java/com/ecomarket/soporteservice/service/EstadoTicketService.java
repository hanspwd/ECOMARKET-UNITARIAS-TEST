package com.ecomarket.soporteservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.exception.YaExisteEnBdException;
import com.ecomarket.soporteservice.model.reference.EstadoTicket;
import com.ecomarket.soporteservice.repository.EstadoTicketRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EstadoTicketService {
    
    @Autowired
    private EstadoTicketRepository estadoTicketRepository;

    public List<EstadoTicket> readAllEstadoTicket() {
        return estadoTicketRepository.findAll();
    }

    public EstadoTicket findEstadoTicketById(Long id) {
        return estadoTicketRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("El estado ticket con id " + id + " no existe en la DB."));
    }

    public EstadoTicket createEstadoTicket(EstadoTicket estadoTicket) {
        EstadoTicket existente = estadoTicketRepository.findByNombre(estadoTicket.getNombre()).orElse(null);
        if(existente != null) {
            throw new YaExisteEnBdException("El canal notificacion " + estadoTicket.getNombre() + " ya existe en BD.");
        }
        return estadoTicketRepository.save(estadoTicket);
    }

    public void deleteEstadoTicket(Long id) {

        EstadoTicket existente = estadoTicketRepository.findById(id).orElse(null);
        if(existente == null) {
            throw new NoExisteEnBdException("El estado ticket con id " + id + " no se puede borrar debido a que no existe en la BD.");
        }
        estadoTicketRepository.deleteById(id);

    }

}
