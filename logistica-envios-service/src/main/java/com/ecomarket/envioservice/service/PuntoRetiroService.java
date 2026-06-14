package com.ecomarket.envioservice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ecomarket.envioservice.exception.EnvioEstadoInvalidoException;
import com.ecomarket.envioservice.exception.NoExisteEnBdException;
import com.ecomarket.envioservice.model.entity.PuntoRetiro;
import com.ecomarket.envioservice.repository.PuntoRetiroRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PuntoRetiroService {

    private final PuntoRetiroRepository puntoRetiroRepository;

    public List<PuntoRetiro> readAll() {
        return puntoRetiroRepository.findAll();
    }

    public List<PuntoRetiro> readActivos() {
        return puntoRetiroRepository.findByActivoTrue();
    }

    public PuntoRetiro findById(Long id) {
        return puntoRetiroRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("El punto de retiro con id " + id + " no existe en la DB."));
    }

    public PuntoRetiro create(PuntoRetiro puntoRetiro) {
        return puntoRetiroRepository.save(puntoRetiro);
    }

    public PuntoRetiro update(Long id, PuntoRetiro datosNuevos) {
        PuntoRetiro existente = findById(id);
        existente.setNombre(datosNuevos.getNombre());
        existente.setCapacidadActual(datosNuevos.getCapacidadActual());
        existente.setCapacidadMaxima(datosNuevos.getCapacidadMaxima());
        existente.setActivo(datosNuevos.getActivo());
        return puntoRetiroRepository.save(existente);
    }

    public void delete(Long id) {
        PuntoRetiro existente = puntoRetiroRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("El punto de retiro con id " + id + " no se puede borrar debido a que no existe en la DB.");
        }
        puntoRetiroRepository.deleteById(id);
    }

    public void verificarDisponibilidad(PuntoRetiro puntoRetiro) {
        if (!puntoRetiro.verificarDisponibilidad()) {
            throw new EnvioEstadoInvalidoException("El punto de retiro " + puntoRetiro.getNombre() + " no esta disponible actualmente.");
        }
    }
}
