package com.ecomarket.envioservice.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ecomarket.envioservice.exception.NoExisteEnBdException;
import com.ecomarket.envioservice.model.entity.RutaTransporte;
import com.ecomarket.envioservice.repository.RutaTransporteRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RutaTransporteService {

    private final RutaTransporteRepository rutaTransporteRepository;

    public List<RutaTransporte> readAll() {
        return rutaTransporteRepository.findAll();
    }

    public RutaTransporte findById(Long id) {
        return rutaTransporteRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("La ruta de transporte con id " + id + " no existe en la DB."));
    }

    public List<RutaTransporte> readByTransportistaId(Long transportistaId) {
        return rutaTransporteRepository.findByTransportistaId(transportistaId);
    }

    public RutaTransporte save(RutaTransporte rutaTransporte) {
        return rutaTransporteRepository.save(rutaTransporte);
    }

    public void delete(Long id) {
        RutaTransporte existente = rutaTransporteRepository.findById(id).orElse(null);
        if (existente == null) {
            throw new NoExisteEnBdException("La ruta de transporte con id " + id + " no se puede borrar debido a que no existe en la DB.");
        }
        rutaTransporteRepository.deleteById(id);
    }
}
