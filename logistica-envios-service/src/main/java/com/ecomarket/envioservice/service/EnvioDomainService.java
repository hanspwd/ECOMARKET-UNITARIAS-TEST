package com.ecomarket.envioservice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ecomarket.envioservice.exception.NoExisteEnBdException;
import com.ecomarket.envioservice.model.entity.Envio;
import com.ecomarket.envioservice.repository.EnvioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EnvioDomainService {

    private final EnvioRepository envioRepository;

    public List<Envio> readAll() {
        return envioRepository.findAll();
    }

    public List<Envio> readByClienteId(Long clienteId) {
        return envioRepository.findByClienteId(clienteId);
    }

    public List<Envio> readByPedidoId(Long pedidoId) {
        return envioRepository.findByPedidoId(pedidoId);
    }

    public List<Envio> readByEstadoId(Long estadoId) {
        return envioRepository.findByEstadoActualId(estadoId);
    }

    public Envio findById(Long id) {
        return envioRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("El envio con id " + id + " no existe en la DB."));
    }

    public Envio save(Envio envio) {
        return envioRepository.save(envio);
    }

    public void delete(Long id) {
        Envio existente = envioRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("El envio con id " + id + " no se puede borrar debido a que no existe en la DB.");
        }
        envioRepository.deleteById(id);
    }
}
