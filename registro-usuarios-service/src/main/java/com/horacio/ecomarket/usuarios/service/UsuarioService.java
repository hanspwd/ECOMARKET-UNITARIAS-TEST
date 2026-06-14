package com.horacio.ecomarket.usuarios.service;

import com.horacio.ecomarket.usuarios.dto.UsuarioDTO;
import com.horacio.ecomarket.usuarios.model.Usuario;
import com.horacio.ecomarket.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;

    public Usuario registrar(UsuarioDTO dto) {

        repository.findByCorreo(dto.getCorreo())
                .ifPresent(u -> {
                    throw new RuntimeException("El correo ya está registrado");
                });

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .password(dto.getPassword())
                .telefono(dto.getTelefono())
                .fechaCreacion(LocalDateTime.now())
                .build();

        return repository.save(usuario);
    }
}