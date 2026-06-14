package com.ecomarket.envioservice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ecomarket.envioservice.model.entity.HistorialEnvio;
import com.ecomarket.envioservice.repository.HistorialEnvioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HistorialEnvioService {

    private final HistorialEnvioRepository historialEnvioRepository;

    public List<HistorialEnvio> findHistorialByEnvioId(Long envioId) {
        return historialEnvioRepository.findByEnvioIdOrderByFechaActualizacionAsc(envioId);
    }

    public HistorialEnvio save(HistorialEnvio historialEnvio) {
        return historialEnvioRepository.save(historialEnvio);
    }
}
