package com.ecomarket.analiticaservice.service;

import com.ecomarket.analiticaservice.exception.NoExisteEnBdException;
import com.ecomarket.analiticaservice.model.entity.Reporte;
import com.ecomarket.analiticaservice.model.reference.EstadoReporte;
import com.ecomarket.analiticaservice.model.reference.TipoReporte;
import com.ecomarket.analiticaservice.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteDomainService {

    private final ReporteRepository reporteRepository;

    @Transactional(readOnly = true)
    public Reporte buscarPorId(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new NoExisteEnBdException("Reporte con ID " + id + " no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Reporte> listarTodos() {
        return reporteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Reporte> buscarPorSolicitante(Long solicitanteId) {
        return reporteRepository.findBySolicitanteId(solicitanteId);
    }

    @Transactional(readOnly = true)
    public List<Reporte> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return reporteRepository.findByFechaGeneracionBetween(inicio, fin);
    }

    @Transactional
    public Reporte crear(Reporte reporte) {
        return reporteRepository.save(reporte);
    }

    @Transactional
    public Reporte actualizarEstado(Long reporteId, EstadoReporte estado, String urlArchivo, Integer totalRegistros) {
        Reporte reporte = buscarPorId(reporteId);
        reporte.setEstado(estado);
        if (urlArchivo != null) {
            reporte.setUrlArchivoResultado(urlArchivo);
        }
        if (totalRegistros != null) {
            reporte.setTotalRegistrosProcesados(totalRegistros);
        }
        return reporteRepository.save(reporte);
    }

    @Transactional
    public Reporte crearReporte(Long solicitanteId, TipoReporte tipo, EstadoReporte estado) {
        Reporte reporte = new Reporte();
        reporte.setSolicitanteId(solicitanteId);
        reporte.setTipo(tipo);
        reporte.setEstado(estado);
        reporte.setFechaGeneracion(LocalDateTime.now());
        return reporteRepository.save(reporte);
    }
}
