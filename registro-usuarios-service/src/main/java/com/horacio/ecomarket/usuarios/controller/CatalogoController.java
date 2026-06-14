package com.horacio.ecomarket.usuarios.controller;

import com.horacio.ecomarket.usuarios.model.EstadoPerfil;
import com.horacio.ecomarket.usuarios.model.Permiso;
import com.horacio.ecomarket.usuarios.model.Rol;
import com.horacio.ecomarket.usuarios.repository.EstadoPerfilRepository;
import com.horacio.ecomarket.usuarios.repository.PermisoRepository;
import com.horacio.ecomarket.usuarios.repository.RolRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class CatalogoController {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final EstadoPerfilRepository estadoPerfilRepository;

    // ── Roles ────────────────────────────────────────────────────────────────

    @GetMapping("/roles")
    public ResponseEntity<List<Rol>> listarRoles() {
        return ResponseEntity.ok(rolRepository.findAll());
    }

    @PostMapping("/roles")
    public ResponseEntity<Rol> crearRol(@Valid @RequestBody Rol rol) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolRepository.save(rol));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<Rol> actualizarRol(@PathVariable Long id, @Valid @RequestBody Rol datos) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        rol.setNombre(datos.getNombre());
        rol.setDescripcion(datos.getDescripcion());
        return ResponseEntity.ok(rolRepository.save(rol));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        rolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Permisos ─────────────────────────────────────────────────────────────

    @GetMapping("/permisos")
    public ResponseEntity<List<Permiso>> listarPermisos() {
        return ResponseEntity.ok(permisoRepository.findAll());
    }

    @PostMapping("/permisos")
    public ResponseEntity<Permiso> crearPermiso(@Valid @RequestBody Permiso permiso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoRepository.save(permiso));
    }

    @PutMapping("/permisos/{id}")
    public ResponseEntity<Permiso> actualizarPermiso(@PathVariable Long id, @Valid @RequestBody Permiso datos) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));
        permiso.setNombre(datos.getNombre());
        permiso.setDescripcion(datos.getDescripcion());
        return ResponseEntity.ok(permisoRepository.save(permiso));
    }

    @DeleteMapping("/permisos/{id}")
    public ResponseEntity<Void> eliminarPermiso(@PathVariable Long id) {
        permisoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── EstadoPerfil ─────────────────────────────────────────────────────────

    @GetMapping("/estados-perfil")
    public ResponseEntity<List<EstadoPerfil>> listarEstadosPerfil() {
        return ResponseEntity.ok(estadoPerfilRepository.findAll());
    }

    @PostMapping("/estados-perfil")
    public ResponseEntity<EstadoPerfil> crearEstadoPerfil(@Valid @RequestBody EstadoPerfil estado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estadoPerfilRepository.save(estado));
    }

    @PutMapping("/estados-perfil/{id}")
    public ResponseEntity<EstadoPerfil> actualizarEstadoPerfil(@PathVariable Long id, @Valid @RequestBody EstadoPerfil datos) {
        EstadoPerfil estado = estadoPerfilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EstadoPerfil no encontrado con ID: " + id));
        estado.setNombre(datos.getNombre());
        return ResponseEntity.ok(estadoPerfilRepository.save(estado));
    }

    @DeleteMapping("/estados-perfil/{id}")
    public ResponseEntity<Void> eliminarEstadoPerfil(@PathVariable Long id) {
        estadoPerfilRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
