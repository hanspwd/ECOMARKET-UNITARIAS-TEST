package com.ecomarket.iniciosesion.controller;

import com.ecomarket.iniciosesion.dto.*;
import com.ecomarket.iniciosesion.service.LoginCuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sesion")
@RequiredArgsConstructor
public class LoginCuentaController {

    private final LoginCuentaService loginCuentaService;

    // ── POST /api/sesion/credencial  (crear credencial al registrar usuario) ──

    @PostMapping("/credencial")
    public ResponseEntity<MensajeResponse> crearCredencial(
            @Valid @RequestBody CrearCredencialRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loginCuentaService.crearCredencial(request));
    }

    // ── POST /api/sesion/login ─────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<IniciarSesionResponse> iniciarSesion(
            @Valid @RequestBody IniciarSesionRequest request) {
        return ResponseEntity.ok(loginCuentaService.iniciarSesion(request));
    }

    // ── POST /api/sesion/logout ────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<MensajeResponse> cerrarSesion(
            @Valid @RequestBody CerrarSesionRequest request) {
        return ResponseEntity.ok(loginCuentaService.cerrarSesion(request));
    }

    // ── POST /api/sesion/validar ───────────────────────────────────────────────

    @PostMapping("/validar")
    public ResponseEntity<AutenticarJWTResponse> autenticarJWT(
            @Valid @RequestBody AutenticarJWTRequest request) {
        return ResponseEntity.ok(loginCuentaService.autenticarJWT(request));
    }

    // ── PUT /api/sesion/correo ─────────────────────────────────────────────────

    @PutMapping("/correo")
    public ResponseEntity<MensajeResponse> cambiarCorreo(
            @Valid @RequestBody CambiarCorreoRequest request) {
        return ResponseEntity.ok(loginCuentaService.cambiarCorreo(request));
    }

    // ── PUT /api/sesion/contrasena ─────────────────────────────────────────────

    @PutMapping("/contrasena")
    public ResponseEntity<MensajeResponse> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaRequest request) {
        return ResponseEntity.ok(loginCuentaService.cambiarContrasena(request));
    }

    // ── POST /api/sesion/recuperar ─────────────────────────────────────────────

    @PostMapping("/recuperar")
    public ResponseEntity<MensajeResponse> recuperarCredenciales(
            @Valid @RequestBody RecuperarCredencialesRequest request) {
        return ResponseEntity.ok(loginCuentaService.recuperarCredenciales(request));
    }

    // ── POST /api/sesion/restablecer ───────────────────────────────────────────

    @PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecerConToken(
            @Valid @RequestBody RestablecerConTokenRequest request) {
        return ResponseEntity.ok(loginCuentaService.restablecerConToken(request));
    }

    // ── DELETE /api/sesion/inhabilitar ─────────────────────────────────────────

    @DeleteMapping("/inhabilitar")
    public ResponseEntity<MensajeResponse> inhabilitarCredenciales(
            @Valid @RequestBody InhabilitarCredencialesRequest request) {
        return ResponseEntity.ok(loginCuentaService.inhabilitarCredenciales(request));
    }
}
