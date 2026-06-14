package com.ecomarket.gestiontiendaservice.controller;

import com.ecomarket.gestiontiendaservice.model.*;
import com.ecomarket.gestiontiendaservice.service.GestionTiendaService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tienda")
@RequiredArgsConstructor
public class GestionTiendaController {

    private final GestionTiendaService gestionTiendaService;

    @PostMapping("/sucursales")
    public ResponseEntity<Sucursal> registrarSucursal(
            @RequestParam String nombre,
            @RequestParam String direccion,
            @RequestParam String telefono,
            @RequestParam Long garanteId) {
        return ResponseEntity.ok(gestionTiendaService.registrarSucursal(nombre, direccion, telefono, garanteId));
    }

    @GetMapping("/sucursales/{sucursalId}")
    public ResponseEntity<Sucursal> obtenerSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(gestionTiendaService.obtenerDatosSucursal(sucursalId));
    }

    @GetMapping("/sucursales/activas")
    public ResponseEntity<List<Sucursal>> listarSucursalesActivas() {
        return ResponseEntity.ok(gestionTiendaService.listarSucursalesActivas());
    }

    @PostMapping("/permisos-pos")
    public ResponseEntity<PermisoPOS> configurarPermisoPOS(@RequestBody PermisoPOS permisoPOS) {
        return ResponseEntity.ok(gestionTiendaService.configurarPermisoPOS(permisoPOS));
    }

    @PostMapping("/tareas")
    public ResponseEntity<TareaPersonal> asignarTarea(
            @RequestParam Long empleadoId,
            @RequestParam Long sucursalId,
            @RequestParam String titulo,
            @RequestParam String descripcionTarea,
            @RequestParam LocalDateTime limite) {
        return ResponseEntity.ok(gestionTiendaService.asignarTareaPersonal(
                empleadoId, sucursalId, titulo, descripcionTarea, limite));
    }

    @PatchMapping("/tareas/{tareaId}/estado")
    public ResponseEntity<TareaPersonal> actualizarEstadoTarea(
            @PathVariable Long tareaId,
            @RequestBody EstadoTareaPersonal nuevoEstado) {
        return ResponseEntity.ok(gestionTiendaService.actualizarEstadoTarea(tareaId, nuevoEstado));
    }

    @PostMapping("/sucursales/{sucursalId}/reglamento")
    public ResponseEntity<ReglamentoInterno> establecerReglamento(
            @RequestBody ReglamentoInterno reglamentoInterno) {
        return ResponseEntity.ok(gestionTiendaService.establecerReglamento(reglamentoInterno));
    }

    @PutMapping("/sucursales/{sucursalId}/horarios")
    public ResponseEntity<Boolean> administrarHorario(
            @PathVariable Long sucursalId,
            @RequestBody List<HorarioAtencion> horarios) {
        return ResponseEntity.ok(gestionTiendaService.administrarHorario(sucursalId, horarios));
    }

    @GetMapping("/sucursales/{sucursalId}/horarios")
    public ResponseEntity<List<HorarioAtencion>> consultarHorarios(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(gestionTiendaService.consultarHorariosTienda(sucursalId));
    }
}
