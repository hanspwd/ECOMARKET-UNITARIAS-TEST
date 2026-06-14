package com.ecomarket.envioservice.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.envioservice.dto.ActualizarEstadoRequestDTO;
import com.ecomarket.envioservice.dto.CrearEnvioRequestDTO;
import com.ecomarket.envioservice.dto.PlanificarRutaRequestDTO;
import com.ecomarket.envioservice.dto.RegistrarRecepcionRequestDTO;
import com.ecomarket.envioservice.dto.SeleccionarPuntoRetiroRequestDTO;
import com.ecomarket.envioservice.model.entity.Envio;
import com.ecomarket.envioservice.model.entity.HistorialEnvio;
import com.ecomarket.envioservice.model.entity.RutaTransporte;
import com.ecomarket.envioservice.model.reference.EstadoEnvio;
import com.ecomarket.envioservice.service.EnvioService;
import com.ecomarket.envioservice.service.EnvioDomainService;
import com.ecomarket.envioservice.service.RutaTransporteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/logistica-envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioService envioService;

    private final EnvioDomainService envioDomainService;

    private final RutaTransporteService rutaTransporteService;

    @PostMapping("envios")
    public ResponseEntity<Envio> crearEnvio(@Valid @RequestBody CrearEnvioRequestDTO dto) throws Exception {
        Envio envio = envioService.crearEnvio(
            dto.getPedidoId(), dto.getClienteId(), dto.getMetodoEnvioId(), dto.getDireccionId());
        return ResponseEntity.status(201).body(envio);
    }

    @GetMapping("envios")
    public List<Envio> listarEnvios(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long estadoId) {
        return envioService.listarEnvios(clienteId, estadoId);
    }

    @GetMapping("envios/{id}")
    public Envio obtenerEnvio(@PathVariable Long id) {
        return envioService.obtenerEnvioPorId(id);
    }

    @GetMapping("envios/{id}/estado")
    public EstadoEnvio consultarEstadoEnvio(@PathVariable Long id) {
        return envioService.consultarEstadoEnvio(id);
    }

    @PatchMapping("envios/{id}/estado")
    public ResponseEntity<HistorialEnvio> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoRequestDTO dto) {
        HistorialEnvio historial = envioService.actualizarEstado(id, dto.getNuevoEstadoId(), dto.getObservacion());
        return ResponseEntity.ok(historial);
    }

    @PostMapping("envios/{id}/cancelar")
    public ResponseEntity<Boolean> cancelarEnvio(@PathVariable Long id) {
        Boolean resultado = envioService.cancelarEnvio(id);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("envios/{id}/recepcion")
    public ResponseEntity<Envio> registrarRecepcion(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarRecepcionRequestDTO dto) {
        Envio envio = envioService.registrarRecepcion(id, dto.getFirmaRecibe());
        return ResponseEntity.ok(envio);
    }

    @PostMapping("envios/{id}/seleccionar-punto-retiro")
    public ResponseEntity<Envio> seleccionarPuntoRetiro(
            @PathVariable Long id,
            @Valid @RequestBody SeleccionarPuntoRetiroRequestDTO dto) {
        Envio envio = envioService.seleccionarPuntoRetiro(id, dto.getPuntoRetiroId(), dto.getFirmaRecibe());
        return ResponseEntity.ok(envio);
    }

    @GetMapping("envios/{id}/historial")
    public List<HistorialEnvio> obtenerHistorial(@PathVariable Long id) {
        return envioService.obtenerHistorialEnvio(id);
    }

    @PostMapping("rutas")
    public ResponseEntity<RutaTransporte> planificarRuta(@Valid @RequestBody PlanificarRutaRequestDTO dto) throws Exception {
        RutaTransporte ruta = envioService.planificarRuta(dto.getTransportistaId(), dto.getEnviosIds());
        return ResponseEntity.status(201).body(ruta);
    }

    @GetMapping("rutas")
    public List<RutaTransporte> listarRutas() {
        return rutaTransporteService.readAll();
    }

    @DeleteMapping("envios/{id}")
    public ResponseEntity<String> eliminarEnvio(@PathVariable Long id) {
        envioDomainService.delete(id);
        return ResponseEntity.ok("El envio con id " + id + " ha sido eliminado con exito.");
    }
}
