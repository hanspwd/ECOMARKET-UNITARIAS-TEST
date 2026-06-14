package com.ecomarket.soporteservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.exception.YaExisteEnBdException;
import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.repository.CanalNotificacionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CanalNotificacionService {
    
    @Autowired
    private CanalNotificacionRepository canalNotificacionRepository;

    public List<CanalNotificacion> readAllCanalNotificacion() {
        return canalNotificacionRepository.findAll();
    }

    public CanalNotificacion createCanalNotificacion(CanalNotificacion canalNotificacion) {
        CanalNotificacion existente = canalNotificacionRepository.findByNombre(canalNotificacion.getNombre()).orElse(null);
        if(existente != null) {
            throw new YaExisteEnBdException("El canal notificacion " + canalNotificacion.getNombre() + " ya existe en BD.");
        }
        return canalNotificacionRepository.save(canalNotificacion);
    }

    public void deleteCanalNotificacionById(Long id) {
        
        CanalNotificacion existente = canalNotificacionRepository.findById(id).orElse(null);
        if(existente == null) {
            throw new NoExisteEnBdException("El canal notificacion con id " + id + " no se puede borrar debido a que no existe en la BD.");
        }
        canalNotificacionRepository.deleteById(id);
    }

    public CanalNotificacion getCanalNotificacionById(Long id) {
        return canalNotificacionRepository.findById(id).orElseThrow(
            () -> new NoExisteEnBdException("El canal notificacion con id " + id + " no existe en la DB."));
    }
 
 }
