package com.horacio.ecomarket.usuarios.service;

import com.horacio.ecomarket.usuarios.model.Permiso;
import com.horacio.ecomarket.usuarios.model.PerfilUsuario;
import com.horacio.ecomarket.usuarios.model.Rol;
import com.horacio.ecomarket.usuarios.repository.PerfilUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistroUsuarioServiceImpl implements RegistroUsuarioService {

    private final PerfilUsuarioRepository repository;
    //private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public PerfilUsuario registrarCuenta(PerfilUsuario perfilUsuario, String contrasenaInicial) {
        repository.findByCorreo(perfilUsuario.getCorreo())
                .ifPresent(u -> {
                    throw new RuntimeException("El correo ya está registrado: " + perfilUsuario.getCorreo());
                });

        perfilUsuario.setFechaCreacion(LocalDateTime.now());
        PerfilUsuario guardado = repository.save(perfilUsuario);

        // Crear credenciales en iniciosesion-service
        Map<String, Object> request = new HashMap<>();
        request.put("usuarioId", guardado.getId());
        request.put("correo", perfilUsuario.getCorreo());
        request.put("contrasena", contrasenaInicial);
        String rolNombre = perfilUsuario.getRol() != null ? "ROLE_" + perfilUsuario.getRol().getNombre() : "ROLE_USER";
        request.put("rol", rolNombre);

        String url = "http://localhost:8086/api/sesion/credencial";
        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear credenciales de acceso: " + e.getMessage());
        }

        registrarLog(guardado.getId(), "REGISTRO_USUARIO", "Usuario registrado exitosamente con correo: " + perfilUsuario.getCorreo());

        return guardado;
    }

    @Override
    @Transactional
    public PerfilUsuario modificarDatosUsuario(Long id, PerfilUsuario datosNuevos) {
        PerfilUsuario existente = buscarPorId(id);

        existente.setNombre(datosNuevos.getNombre());
        existente.setTelefono(datosNuevos.getTelefono());

        // Actualizar correo sólo si cambió y no está tomado
        if (!existente.getCorreo().equals(datosNuevos.getCorreo())) {
            repository.findByCorreo(datosNuevos.getCorreo())
                    .ifPresent(u -> {
                        throw new RuntimeException("El correo ya está en uso: " + datosNuevos.getCorreo());
                    });
            existente.setCorreo(datosNuevos.getCorreo());
        }

        if (datosNuevos.getRol() != null) {
            existente.setRol(datosNuevos.getRol());
        }

        if (datosNuevos.getEstadoPerfil() != null) {
            existente.setEstadoPerfil(datosNuevos.getEstadoPerfil());
        }

        return repository.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilUsuario> listarUsuarios() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilUsuario> listarPorRol(Rol rolUsuario) {
        return repository.findByRol(rolUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilUsuario buscarPorId(Long usuarioId) {
        return repository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilUsuario buscarPorCorreo(String correo) {
        return repository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
    }

    @Override
    @Transactional
    public Boolean configurarPermisos(Long usuarioId, List<Permiso> nuevosPermisos) {
        PerfilUsuario usuario = buscarPorId(usuarioId);
        usuario.getPermisos().clear();
        usuario.getPermisos().addAll(nuevosPermisos);
        repository.save(usuario);
        return true;
    }

    @Override
    @Transactional
    public Boolean eliminarUsuario(Long usuarioId) {
        PerfilUsuario usuario = buscarPorId(usuarioId);
        repository.delete(usuario);
        registrarLog(usuarioId, "ELIMINACION_USUARIO", "Usuario eliminado con ID: " + usuarioId);
        return true;
    }

    private void registrarLog(Long usuarioId, String accion, String detalles) {
        Map<String, Object> log = new HashMap<>();
        log.put("microservicio", "registro-usuarios-service");
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
