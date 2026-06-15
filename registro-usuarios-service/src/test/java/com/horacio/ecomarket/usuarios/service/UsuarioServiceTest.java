package com.horacio.ecomarket.usuarios.service;

import com.horacio.ecomarket.usuarios.dto.UsuarioDTO;
import com.horacio.ecomarket.usuarios.model.Usuario;
import com.horacio.ecomarket.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioService.
 * Sin Spring, sin BD — Mockito puro.
 *
 * Ejecutar:
 *   mvn test -pl registro-usuarios-service -Dtest=UsuarioServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    // ── Helper: DTO de prueba ─────────────────────────────────────────────────
    private UsuarioDTO dtoValido() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre("Horacio Navarrete");
        dto.setCorreo("hocx@eco.cl");
        dto.setPassword("segura123");
        dto.setTelefono("+56912345678");
        return dto;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // registrar
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("registrar")
    class Registrar {

        @Test
        @DisplayName("registra usuario correctamente cuando el correo no existe")
        void registraUsuarioExitosamente() {
            when(repository.findByCorreo("hocx@eco.cl")).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            Usuario resultado = service.registrar(dtoValido());

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Horacio Navarrete");
            assertThat(resultado.getCorreo()).isEqualTo("hocx@eco.cl");
            verify(repository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("asigna fechaCreacion automáticamente al registrar")
        void asignaFechaCreacion() {
            when(repository.findByCorreo(anyString())).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = service.registrar(dtoValido());

            assertThat(resultado.getFechaCreacion()).isNotNull();
        }

        @Test
        @DisplayName("mapea todos los campos del DTO al modelo correctamente")
        void mapeaCamposDelDTO() {
            when(repository.findByCorreo(anyString())).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = service.registrar(dtoValido());

            assertThat(resultado.getNombre()).isEqualTo("Horacio Navarrete");
            assertThat(resultado.getCorreo()).isEqualTo("hocx@eco.cl");
            assertThat(resultado.getPassword()).isEqualTo("segura123");
            assertThat(resultado.getTelefono()).isEqualTo("+56912345678");
        }

        @Test
        @DisplayName("lanza RuntimeException cuando el correo ya está registrado")
        void lanzaExcepcionCorreoDuplicado() {
            Usuario yaExiste = Usuario.builder()
                    .id(99L).correo("hocx@eco.cl").nombre("Otro").build();

            when(repository.findByCorreo("hocx@eco.cl")).thenReturn(Optional.of(yaExiste));

            assertThatThrownBy(() -> service.registrar(dtoValido()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("El correo ya está registrado");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("no guarda nada si el correo ya existe")
        void noGuardaSiCorreoExiste() {
            when(repository.findByCorreo(anyString()))
                    .thenReturn(Optional.of(Usuario.builder().id(1L).build()));

            assertThatThrownBy(() -> service.registrar(dtoValido()))
                    .isInstanceOf(RuntimeException.class);

            verify(repository, never()).save(any());
        }
    }
}