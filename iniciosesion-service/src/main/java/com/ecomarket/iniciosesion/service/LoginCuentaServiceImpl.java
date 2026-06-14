package com.ecomarket.iniciosesion.service;

import com.ecomarket.iniciosesion.dto.*;
import com.ecomarket.iniciosesion.exception.*;
import com.ecomarket.iniciosesion.model.Credencial;
import com.ecomarket.iniciosesion.model.SesionJWT;
import com.ecomarket.iniciosesion.model.TokenRecuperacion;
import com.ecomarket.iniciosesion.repository.CredencialRepository;
import com.ecomarket.iniciosesion.repository.SesionJWTRepository;
import com.ecomarket.iniciosesion.repository.TokenRecuperacionRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LoginCuentaServiceImpl implements LoginCuentaService {

    private final CredencialRepository credencialRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final SesionJWTRepository sesionJWTRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ── Crear credencial ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public MensajeResponse crearCredencial(CrearCredencialRequest request) {
        if (credencialRepository.existsByCorreoAcceso(request.getCorreo())) {
            throw new CorreoDuplicadoException(
                    "El correo '" + request.getCorreo() + "' ya está registrado.");
        }

        String rol = request.getRol() != null ? request.getRol() : "ROLE_USER";

        Credencial credencial = Credencial.builder()
                .usuarioId(request.getUsuarioId())
                .correoAcceso(request.getCorreo())
                .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
                .cuentaBloqueada(false)
                .rolAcceso(rol)
                .build();

        credencialRepository.save(credencial);
        return MensajeResponse.de("Credencial creada exitosamente.");
    }

    // ── Iniciar sesión ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public IniciarSesionResponse iniciarSesion(IniciarSesionRequest request) {
        Credencial credencial = credencialRepository
                .findByCorreoAcceso(request.getCorreo())
                .orElseThrow(() -> new AutenticacionException("Correo o contraseña incorrectos."));

        if (Boolean.TRUE.equals(credencial.getCuentaBloqueada())) {
            throw new CuentaBloqueadaException("La cuenta está bloqueada. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getContrasena(), credencial.getContrasenaHash())) {
            throw new AutenticacionException("Correo o contraseña incorrectos.");
        }

        credencial.setFechaUltimoLogin(LocalDateTime.now());
        credencialRepository.save(credencial);

        String rol = credencial.getRolAcceso() != null ? credencial.getRolAcceso() : "ROLE_USER";
        List<String> roles = List.of(rol);
        String token = jwtUtil.generarToken(credencial.getUsuarioId(), credencial.getCorreoAcceso(), roles);

        registrarLog(credencial.getUsuarioId(), "INICIO_SESION", "Usuario inició sesión exitosamente con correo: " + credencial.getCorreoAcceso());

        return IniciarSesionResponse.builder()
                .token(token)
                .usuarioId(credencial.getUsuarioId())
                .correo(credencial.getCorreoAcceso())
                .rol(rol)
                .expiracionMs(jwtUtil.getExpirationMs())
                .build();
    }

    // ── Cerrar sesión (blacklist) ─────────────────────────────────────────────

    @Override
    @Transactional
    public MensajeResponse cerrarSesion(CerrarSesionRequest request) {
        String token = request.getToken();

        if (!jwtUtil.esTokenValido(token)) {
            throw new TokenInvalidoException("El token proporcionado no es válido.");
        }

        if (sesionJWTRepository.existsByToken(token)) {
            return MensajeResponse.de("La sesión ya estaba cerrada.");
        }

        Claims claims = jwtUtil.validarYObtenerClaims(token);
        List<String> roles = jwtUtil.obtenerRoles(token);
        String rolAcceso = (roles != null && !roles.isEmpty()) ? roles.get(0) : "ROLE_USER";

        SesionJWT sesion = SesionJWT.builder()
                .token(token)
                .usuarioId(jwtUtil.obtenerUsuarioId(token))
                .rolAcceso(rolAcceso)
                .fechaEmision(claims.getIssuedAt()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .fechaExpiracion(claims.getExpiration()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        sesionJWTRepository.save(sesion);
        return MensajeResponse.de("Sesión cerrada exitosamente.");
    }

    // ── Autenticar JWT ────────────────────────────────────────────────────────

    @Override
    public AutenticarJWTResponse autenticarJWT(AutenticarJWTRequest request) {
        String token = request.getToken();

        if (!jwtUtil.esTokenValido(token)) {
            return AutenticarJWTResponse.builder().valido(false).build();
        }

        if (sesionJWTRepository.existsByToken(token)) {
            // Token en blacklist (logout previo)
            return AutenticarJWTResponse.builder().valido(false).build();
        }

        return AutenticarJWTResponse.builder()
                .valido(true)
                .usuarioId(jwtUtil.obtenerUsuarioId(token))
                .correo(jwtUtil.obtenerCorreo(token))
                .roles(jwtUtil.obtenerRoles(token))
                .build();
    }

    // ── Cambiar correo ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public MensajeResponse cambiarCorreo(CambiarCorreoRequest request) {
        Credencial credencial = credencialRepository
                .findByUsuarioId(request.getUsuarioId())
                .orElseThrow(() -> new CredencialNotFoundException(
                        "No se encontraron credenciales para el usuario " + request.getUsuarioId()));

        if (!passwordEncoder.matches(request.getContrasenaActual(), credencial.getContrasenaHash())) {
            throw new AutenticacionException("Contraseña incorrecta.");
        }

        if (credencialRepository.existsByCorreoAcceso(request.getNuevoCorreo())) {
            throw new CorreoDuplicadoException(
                    "El correo '" + request.getNuevoCorreo() + "' ya está en uso.");
        }

        credencial.setCorreoAcceso(request.getNuevoCorreo());
        credencialRepository.save(credencial);
        return MensajeResponse.de("Correo actualizado exitosamente.");
    }

    // ── Cambiar contraseña ────────────────────────────────────────────────────

    @Override
    @Transactional
    public MensajeResponse cambiarContrasena(CambiarContrasenaRequest request) {
        Credencial credencial = credencialRepository
                .findByUsuarioId(request.getUsuarioId())
                .orElseThrow(() -> new CredencialNotFoundException(
                        "No se encontraron credenciales para el usuario " + request.getUsuarioId()));

        if (!passwordEncoder.matches(request.getContrasenaActual(), credencial.getContrasenaHash())) {
            throw new AutenticacionException("Contraseña actual incorrecta.");
        }

        credencial.setContrasenaHash(passwordEncoder.encode(request.getNuevaContrasena()));
        credencialRepository.save(credencial);
        return MensajeResponse.de("Contraseña actualizada exitosamente.");
    }

    // ── Recuperar credenciales (genera token) ─────────────────────────────────

    @Override
    @Transactional
    public MensajeResponse recuperarCredenciales(RecuperarCredencialesRequest request) {
        Credencial credencial = credencialRepository
                .findByCorreoAcceso(request.getCorreo())
                .orElseThrow(() -> new CredencialNotFoundException(
                        "No existe una cuenta asociada a ese correo."));

        String codigo = generarCodigoAlfanumerico();

        TokenRecuperacion tokenRec = TokenRecuperacion.builder()
                .credencial(credencial)
                .codigoAlfanumerico(codigo)
                .expiracion(LocalDateTime.now().plusHours(2))
                .consumido(false)
                .build();

        tokenRecuperacionRepository.save(tokenRec);

        // En producción se enviaría por email. Por ahora se devuelve en la respuesta.
        return MensajeResponse.de("Código de recuperación generado: " + codigo
                + " (válido por 2 horas). En producción, se enviaría al correo registrado.");
    }

    // ── Restablecer con token ─────────────────────────────────────────────────

    @Override
    @Transactional
    public MensajeResponse restablecerConToken(RestablecerConTokenRequest request) {
        TokenRecuperacion tokenRec = tokenRecuperacionRepository
                .buscarTokenActivo(request.getCodigo(), LocalDateTime.now())
                .orElseThrow(() -> new TokenInvalidoException(
                        "El código es inválido, ya fue usado o expiró."));

        Credencial credencial = tokenRec.getCredencial();
        credencial.setContrasenaHash(passwordEncoder.encode(request.getNuevaContrasena()));
        credencial.setCuentaBloqueada(false);
        credencialRepository.save(credencial);

        tokenRec.setConsumido(true);
        tokenRecuperacionRepository.save(tokenRec);

        return MensajeResponse.de("Contraseña restablecida exitosamente.");
    }

    // ── Inhabilitar credenciales ──────────────────────────────────────────────

    @Override
    @Transactional
    public MensajeResponse inhabilitarCredenciales(InhabilitarCredencialesRequest request) {
        Credencial credencial = credencialRepository
                .findByUsuarioId(request.getUsuarioId())
                .orElseThrow(() -> new CredencialNotFoundException(
                        "No se encontraron credenciales para el usuario " + request.getUsuarioId()));

        credencial.setCuentaBloqueada(true);
        credencialRepository.save(credencial);
        return MensajeResponse.de("Credenciales inhabilitadas. La cuenta ha sido bloqueada.");
    }

    // ── Utilitario interno ────────────────────────────────────────────────────

    private String generarCodigoAlfanumerico() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void registrarLog(Long usuarioId, String accion, String detalles) {
        Map<String, Object> log = new HashMap<>();
        log.put("microservicio", "iniciosesion-service");
        log.put("accion", accion);
        log.put("usuarioId", usuarioId);
        log.put("detalles", detalles);
        log.put("fecha", LocalDateTime.now());

        try {
            restTemplate.postForEntity("http://localhost:8084/api/analitica/logs", log, String.class);
        } catch (Exception e) {
            // Log error but don't break business flow
        }
    }
}
