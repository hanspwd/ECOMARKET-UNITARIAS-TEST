package com.ecomarket.analiticaservice.service;

import com.ecomarket.analiticaservice.exception.NoExisteEnBdException;
import com.ecomarket.analiticaservice.model.entity.AlertaSistema;
import com.ecomarket.analiticaservice.model.reference.NivelAlerta;
import com.ecomarket.analiticaservice.repository.AlertaSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaDomainService {

    private final AlertaSistemaRepository alertaRepository;

    @Transactional(readOnly = true)
    public AlertaSistema buscarPorId(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new NoExisteEnBdException("Alerta con ID " + id + " no encontrada."));
    }

    @Transactional(readOnly = true)
    public List<AlertaSistema> listarTodas() {
        return alertaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AlertaSistema> buscarPorResuelta(Boolean resuelta) {
        return alertaRepository.findByResuelta(resuelta);
    }

    @Transactional(readOnly = true)
    public List<AlertaSistema> buscarPorModulo(String moduloOrigen) {
        return alertaRepository.findByModuloOrigen(moduloOrigen);
    }

    @Transactional
    public AlertaSistema crear(NivelAlerta nivel, String mensaje, String moduloOrigen) {
        AlertaSistema alerta = new AlertaSistema();
        alerta.setNivel(nivel);
        alerta.setMensaje(mensaje);
        alerta.setModuloOrigen(moduloOrigen);
        alerta.setFechaAlerta(LocalDateTime.now());
        alerta.setResuelta(false);
        return alertaRepository.save(alerta);
    }

    @Transactional
    public AlertaSistema marcarResuelta(Long id) {
        AlertaSistema alerta = buscarPorId(id);
        alerta.setResuelta(true);
        return alertaRepository.save(alerta);
    }
}
