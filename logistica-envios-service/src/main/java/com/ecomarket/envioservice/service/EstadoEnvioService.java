package com.ecomarket.envioservice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ecomarket.envioservice.exception.NoExisteEnBdException;
import com.ecomarket.envioservice.exception.YaExisteEnBdException;
import com.ecomarket.envioservice.model.reference.EstadoEnvio;
import com.ecomarket.envioservice.repository.EstadoEnvioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EstadoEnvioService {

    private final EstadoEnvioRepository estadoEnvioRepository;

    public List<EstadoEnvio> readAll() {
        return estadoEnvioRepository.findAll();
    }

    public EstadoEnvio findById(Long id) {
        return estadoEnvioRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("El estado envio con id " + id + " no existe en la DB."));
    }

    public EstadoEnvio create(EstadoEnvio estadoEnvio) {
        EstadoEnvio existente = estadoEnvioRepository.findByNombre(estadoEnvio.getNombre()).orElse(null);
        if (existente != null) {
            throw new YaExisteEnBdException("El estado envio con nombre " + estadoEnvio.getNombre() + " ya existe en BD.");
        }
        return estadoEnvioRepository.save(estadoEnvio);
    }

    public void delete(Long id) {
        EstadoEnvio existente = estadoEnvioRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("El estado envio con id " + id + " no se puede borrar debido a que no existe en la DB.");
        }
        estadoEnvioRepository.deleteById(id);
    }
}
