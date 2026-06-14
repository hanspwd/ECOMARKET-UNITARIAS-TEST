package com.ecomarket.analiticaservice.service;

import com.ecomarket.analiticaservice.exception.NoExisteEnBdException;
import com.ecomarket.analiticaservice.model.entity.RespaldoBaseDatos;
import com.ecomarket.analiticaservice.model.reference.EstadoRespaldo;
import com.ecomarket.analiticaservice.repository.RespaldoBaseDatosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RespaldoDomainService {

    private final RespaldoBaseDatosRepository respaldoRepository;

    @Transactional(readOnly = true)
    public RespaldoBaseDatos buscarPorId(Long id) {
        return respaldoRepository.findById(id)
                .orElseThrow(() -> new NoExisteEnBdException("Respaldo con ID " + id + " no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<RespaldoBaseDatos> listarTodos() {
        return respaldoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RespaldoBaseDatos> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return respaldoRepository.findByFechaRespaldoBetween(inicio, fin);
    }

    @Transactional
    public RespaldoBaseDatos crear(RespaldoBaseDatos respaldo) {
        return respaldoRepository.save(respaldo);
    }

    @Transactional
    public RespaldoBaseDatos crearRespaldo(EstadoRespaldo estado, Double tamanoMb, String ruta) {
        RespaldoBaseDatos respaldo = new RespaldoBaseDatos();
        respaldo.setFechaRespaldo(LocalDateTime.now());
        respaldo.setEstado(estado);
        respaldo.setTamanoMegabytes(tamanoMb);
        respaldo.setRutaAlmacenamiento(ruta);
        return respaldoRepository.save(respaldo);
    }

    @Transactional
    public RespaldoBaseDatos actualizarEstado(Long respaldoId, EstadoRespaldo estado) {
        RespaldoBaseDatos respaldo = buscarPorId(respaldoId);
        respaldo.setEstado(estado);
        return respaldoRepository.save(respaldo);
    }
}
