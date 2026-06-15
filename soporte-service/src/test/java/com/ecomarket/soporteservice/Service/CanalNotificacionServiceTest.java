package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.exception.YaExisteEnBdException;
import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.repository.CanalNotificacionRepository;
import com.ecomarket.soporteservice.service.CanalNotificacionService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CanalNotificacionService")
class CanalNotificacionServiceTest {

    @Mock
    private CanalNotificacionRepository repo;

    @InjectMocks
    private CanalNotificacionService service;

    private CanalNotificacion canal(Long id, String nombre) {
        CanalNotificacion c = new CanalNotificacion();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    @Nested
    @DisplayName("readAllCanalNotificacion")
    class ReadAll {

        @Test
        @DisplayName("retorna lista completa")
        void retornaLista() {
            when(repo.findAll()).thenReturn(List.of(canal(1L, "EMAIL"), canal(2L, "SMS")));
            assertThat(service.readAllCanalNotificacion()).hasSize(2);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay registros")
        void retornaVacio() {
            when(repo.findAll()).thenReturn(List.of());
            assertThat(service.readAllCanalNotificacion()).isEmpty();
        }
    }

    @Nested
    @DisplayName("createCanalNotificacion")
    class Create {

        @Test
        @DisplayName("crea y retorna el canal cuando el nombre no existe")
        void creaCanal() {
            CanalNotificacion nuevo = canal(null, "PUSH");
            when(repo.findByNombre("PUSH")).thenReturn(Optional.empty());
            when(repo.save(any())).thenReturn(canal(3L, "PUSH"));

            CanalNotificacion resultado = service.createCanalNotificacion(nuevo);

            assertThat(resultado.getId()).isEqualTo(3L);
            assertThat(resultado.getNombre()).isEqualTo("PUSH");
        }

        @Test
        @DisplayName("lanza YaExisteEnBdException cuando el nombre ya existe")
        void lanzaExcepcionDuplicado() {
            when(repo.findByNombre("EMAIL")).thenReturn(Optional.of(canal(1L, "EMAIL")));

            assertThatThrownBy(() -> service.createCanalNotificacion(canal(null, "EMAIL")))
                    .isInstanceOf(YaExisteEnBdException.class)
                    .hasMessageContaining("EMAIL");

            verify(repo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteCanalNotificacionById")
    class Delete {

        @Test
        @DisplayName("elimina canal existente sin lanzar excepción")
        void eliminaCanal() {
            when(repo.findById(1L)).thenReturn(Optional.of(canal(1L, "SMS")));
            doNothing().when(repo).deleteById(1L);

            assertThatCode(() -> service.deleteCanalNotificacionById(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando el canal no existe")
        void lanzaExcepcionNoExiste() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteCanalNotificacionById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getCanalNotificacionById")
    class GetById {

        @Test
        @DisplayName("retorna el canal cuando existe")
        void retornaCanal() {
            when(repo.findById(1L)).thenReturn(Optional.of(canal(1L, "EMAIL")));
            assertThat(service.getCanalNotificacionById(1L).getNombre()).isEqualTo("EMAIL");
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getCanalNotificacionById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }
}