package com.ecomarket.gestiontiendaservice.service;

import com.ecomarket.gestiontiendaservice.model.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public interface GestionTiendaService {

    @Autowired
    Sucursal registrarSucursal(String nombre, String direccion, String telefono, Long garanteId);

    @Autowired
    Sucursal obtenerDatosSucursal(Long sucursalId);

    @Autowired
    List<Sucursal> listarSucursalesActivas();

    @Autowired
    PermisoPOS configurarPermisoPOS(PermisoPOS permisoPOS);

    @Autowired
    TareaPersonal asignarTareaPersonal(Long empleadoId, Long sucursalId, String titulo,
                                       String descripcionTarea, LocalDateTime limite);
    @Autowired
    TareaPersonal actualizarEstadoTarea(Long tareaId, EstadoTareaPersonal nuevoEstado);

    @Autowired
    ReglamentoInterno establecerReglamento(ReglamentoInterno reglamentoInterno);

    @Autowired
    Boolean administrarHorario(Long sucursalId, List<HorarioAtencion> horarios);

    @Autowired
    List<HorarioAtencion> consultarHorariosTienda(Long sucursalId);
}