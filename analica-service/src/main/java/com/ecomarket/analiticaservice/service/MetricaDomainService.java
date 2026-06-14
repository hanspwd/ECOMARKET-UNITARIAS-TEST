package com.ecomarket.analiticaservice.service;

import com.ecomarket.analiticaservice.exception.NoExisteEnBdException;
import com.ecomarket.analiticaservice.model.entity.MetricaDashboard;
import com.ecomarket.analiticaservice.repository.MetricaDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricaDomainService {

    private final MetricaDashboardRepository metricaRepository;

    @Transactional(readOnly = true)
    public MetricaDashboard buscarPorId(Long id) {
        return metricaRepository.findById(id)
                .orElseThrow(() -> new NoExisteEnBdException("Metrica con ID " + id + " no encontrada."));
    }

    @Transactional(readOnly = true)
    public List<MetricaDashboard> listarTodas() {
        return metricaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MetricaDashboard buscarPorClave(String claveMetrica) {
        return metricaRepository.findByClaveMetrica(claveMetrica)
                .orElseThrow(() -> new NoExisteEnBdException("Metrica con clave '" + claveMetrica + "' no encontrada."));
    }

    @Transactional
    public MetricaDashboard crear(String claveMetrica, Double valorNumerico, String valorTexto) {
        MetricaDashboard metrica = new MetricaDashboard();
        metrica.setClaveMetrica(claveMetrica);
        metrica.setValorNumerico(valorNumerico);
        metrica.setValorTexto(valorTexto);
        metrica.setUltimaActualizacion(LocalDateTime.now());
        return metricaRepository.save(metrica);
    }

    @Transactional
    public MetricaDashboard actualizar(Long id, Double valorNumerico, String valorTexto) {
        MetricaDashboard metrica = buscarPorId(id);
        metrica.setValorNumerico(valorNumerico);
        metrica.setValorTexto(valorTexto);
        metrica.setUltimaActualizacion(LocalDateTime.now());
        return metricaRepository.save(metrica);
    }
}
