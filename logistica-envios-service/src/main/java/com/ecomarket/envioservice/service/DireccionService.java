package com.ecomarket.envioservice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ecomarket.envioservice.exception.NoExisteEnBdException;
import com.ecomarket.envioservice.model.entity.Direccion;
import com.ecomarket.envioservice.repository.DireccionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DireccionService {

    private final DireccionRepository direccionRepository;

    public List<Direccion> readAll() {
        return direccionRepository.findAll();
    }

    public Direccion findById(Long id) {
        return direccionRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("La direccion con id " + id + " no existe en la DB."));
    }

    public Direccion create(Direccion direccion) {
        return direccionRepository.save(direccion);
    }

    public Direccion update(Long id, Direccion datosNuevos) {
        Direccion existente = findById(id);
        existente.setCalle(datosNuevos.getCalle());
        existente.setNumero(datosNuevos.getNumero());
        existente.setDepartamento(datosNuevos.getDepartamento());
        existente.setCiudad(datosNuevos.getCiudad());
        existente.setCodigoPostal(datosNuevos.getCodigoPostal());
        existente.setLatitud(datosNuevos.getLatitud());
        existente.setLongitud(datosNuevos.getLongitud());
        return direccionRepository.save(existente);
    }

    public void delete(Long id) {
        Direccion existente = direccionRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("La direccion con id " + id + " no se puede borrar debido a que no existe en la DB.");
        }
        direccionRepository.deleteById(id);
    }
}
