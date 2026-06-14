package com.ecomarket.gestiontiendaservice.service;

import com.ecomarket.gestiontiendaservice.model.*;
import com.ecomarket.gestiontiendaservice.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionTiendaServiceImpl implements GestionTiendaService {

    private final SucursalRepository sucursalRepository;
    private final PermisoPOSRepository permisoPOSRepository;
    private final ReglamentoInternoRepository reglamentoInternoRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final TareaPersonalRepository tareaPersonalRepository;
    private final EstadoTareaPersonalRepository estadoTareaPersonalRepository;

    @Override
    public Sucursal registrarSucursal(String nombre, String direccion, String telefono, Long garanteId) {
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(nombre);
        sucursal.setDireccion(direccion);
        sucursal.setTelefono(telefono);
        sucursal.setGarantiaCargold(garanteId);
        sucursal.setActiva(true);
        return sucursalRepository.save(sucursal);
    }

    @Override
    public Sucursal obtenerDatosSucursal(Long sucursalId) {
        return sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada: " + sucursalId));
    }

    @Override
    public List<Sucursal> listarSucursalesActivas() {
        return sucursalRepository.findByActivaTrue();
    }

    @Override
    public PermisoPOS configurarPermisoPOS(PermisoPOS permisoPOS) {
        return permisoPOSRepository.save(permisoPOS);
    }

    @Override
    public TareaPersonal asignarTareaPersonal(Long empleadoId, Long sucursalId, String titulo,
                                               String descripcionTarea, LocalDateTime limite) {
        EstadoTareaPersonal estadoPendiente = estadoTareaPersonalRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new RuntimeException("Estado PENDIENTE no encontrado"));

        TareaPersonal tarea = new TareaPersonal();
        tarea.setEmpleadoId(empleadoId);
        tarea.setSucursalId(sucursalId);
        tarea.setTitulo(titulo);
        tarea.setDescripcion(descripcionTarea);
        tarea.setEstado(estadoPendiente);
        tarea.setFechaAsignacion(LocalDateTime.now());
        tarea.setFechaLimite(limite);
        return tareaPersonalRepository.save(tarea);
    }

    @Override
    public TareaPersonal actualizarEstadoTarea(Long tareaId, EstadoTareaPersonal nuevoEstado) {
        TareaPersonal tarea = tareaPersonalRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada: " + tareaId));
        tarea.setEstado(nuevoEstado);
        return tareaPersonalRepository.save(tarea);
    }

    @Override
    public ReglamentoInterno establecerReglamento(ReglamentoInterno reglamentoInterno) {
        return reglamentoInternoRepository.save(reglamentoInterno);
    }

    @Override
    public Boolean administrarHorario(Long sucursalId, List<HorarioAtencion> horarios) {
        List<HorarioAtencion> existentes = horarioAtencionRepository.findBySucursalId(sucursalId);
        horarioAtencionRepository.deleteAll(existentes);
        horarios.forEach(h -> h.setSucursalId(sucursalId));
        horarioAtencionRepository.saveAll(horarios);
        return true;
    }

    @Override
    public List<HorarioAtencion> consultarHorariosTienda(Long sucursalId) {
        return horarioAtencionRepository.findBySucursalId(sucursalId);
    }
}