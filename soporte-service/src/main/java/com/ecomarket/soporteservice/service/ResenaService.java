package com.ecomarket.soporteservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.model.entity.Resena;
import com.ecomarket.soporteservice.repository.ResenaRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    public List<Resena> readAllResenas() {
        return resenaRepository.findAll();
    }

    public List<Resena> readResenasByProductoId(Long productoId) {
        return resenaRepository.findByProductoId(productoId);
    }

    public List<Resena> readResenasByClienteId(Long clienteId) {
        return resenaRepository.findByClienteId(clienteId);
    }

    public Resena dejarResena(Long productoId, Long clienteId, Integer calificacion, String comentario) {
        Resena resena = new Resena();
        resena.setProductoId(productoId);
        resena.setClienteId(clienteId);
        resena.setCalificacionEstrellas(calificacion);
        resena.setComentario(comentario.trim());
        resena.setFechaPublicacion(LocalDateTime.now());
        resena.setModeracionAprobado(false);
        return resenaRepository.save(resena);
    }

    public Resena findResenaById(Long id) {
        return resenaRepository.findById(id)
            .orElseThrow(() -> new NoExisteEnBdException("La resena con id " + id + " no existe en la DB."));
    }

    public void aprobarModeracion(Long resenaId) {
        Resena resena = resenaRepository.findById(resenaId)
            .orElseThrow(() -> new NoExisteEnBdException("La resena con id " + resenaId + " no existe en la DB."));
        resena.setModeracionAprobado(true);
        resenaRepository.save(resena);
    }

    public void rechazarModeracion(Long resenaId) {
        Resena resena = resenaRepository.findById(resenaId)
            .orElseThrow(() -> new NoExisteEnBdException("La resena con id " + resenaId + " no existe en la DB."));
        resena.setModeracionAprobado(false);
        resenaRepository.save(resena);
    }

    public void deleteResenaById(Long id) {
        Resena existente = resenaRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("La resena con id " + id + " no se puede borrar debido a que no existe en la BD.");
        }
        resenaRepository.deleteById(id);
    }
}
