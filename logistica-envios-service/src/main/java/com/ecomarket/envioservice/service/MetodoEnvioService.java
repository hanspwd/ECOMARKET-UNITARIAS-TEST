package com.ecomarket.envioservice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ecomarket.envioservice.exception.NoExisteEnBdException;
import com.ecomarket.envioservice.exception.YaExisteEnBdException;
import com.ecomarket.envioservice.model.reference.MetodoEnvio;
import com.ecomarket.envioservice.repository.MetodoEnvioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MetodoEnvioService {

    private final MetodoEnvioRepository metodoEnvioRepository;

    public List<MetodoEnvio> readAll() {
        return metodoEnvioRepository.findAll();
    }

    public MetodoEnvio findById(Long id) {
        return metodoEnvioRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("El metodo envio con id " + id + " no existe en la DB."));
    }

    public MetodoEnvio create(MetodoEnvio metodoEnvio) {
        MetodoEnvio existente = metodoEnvioRepository.findByNombre(metodoEnvio.getNombre()).orElse(null);
        if (existente != null) {
            throw new YaExisteEnBdException("El metodo envio con nombre " + metodoEnvio.getNombre() + " ya existe en BD.");
        }
        return metodoEnvioRepository.save(metodoEnvio);
    }

    public void delete(Long id) {
        MetodoEnvio existente = metodoEnvioRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("El metodo envio con id " + id + " no se puede borrar debido a que no existe en la DB.");
        }
        metodoEnvioRepository.deleteById(id);
    }
}
