package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.exception.YaExisteEnBdException;
import com.ecomarket.soporteservice.model.reference.EstadoTicket;
import com.ecomarket.soporteservice.repository.EstadoTicketRepository;
import com.ecomarket.soporteservice.service.EstadoTicketService;

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
@DisplayName("EstadoTicketService")
class EstadoTicketServiceTest {

    @Mock
    private EstadoTicketRepository repo;

    @InjectMocks
    private EstadoTicketService service;

    private EstadoTicket estado(Long id, String nombre) {
        EstadoTicket e = new EstadoTicket();
        e.setId(id);
        e.setNombre(nombre);
        return e;
    }

    @Nested
    @DisplayName("readAllEstadoTicket")
    class ReadAll {

        @Test
        @DisplayName("retorna lista completa de estados")
        void retornaLista() {
            when(repo.findAll()).thenReturn(List.of(
                    estado(1L, "ABIERTO"), estado(2L, "EN_PROCESO"), estado(3L, "RESUELTO")));
            assertThat(service.readAllEstadoTicket()).hasSize(3);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay estados")
        void retornaVacio() {
            when(repo.findAll()).thenReturn(List.of());
            assertThat(service.readAllEstadoTicket()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findEstadoTicketById")
    class FindById {

        @Test
        @DisplayName("retorna el estado cuando existe")
        void retornaEstado() {
            when(repo.findById(1L)).thenReturn(Optional.of(estado(1L, "ABIERTO")));
            assertThat(service.findEstadoTicketById(1L).getNombre()).isEqualTo("ABIERTO");
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findEstadoTicketById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("createEstadoTicket")
    class Create {

        @Test
        @DisplayName("crea estado cuando el nombre no existe")
        void creaEstado() {
            when(repo.findByNombre("ESCALADO")).thenReturn(Optional.empty());
            when(repo.save(any())).thenReturn(estado(5L, "ESCALADO"));

            EstadoTicket resultado = service.createEstadoTicket(estado(null, "ESCALADO"));
            assertThat(resultado.getId()).isEqualTo(5L);
            assertThat(resultado.getNombre()).isEqualTo("ESCALADO");
        }

        @Test
        @DisplayName("lanza YaExisteEnBdException cuando el nombre ya existe")
        void lanzaDuplicado() {
            when(repo.findByNombre("ABIERTO")).thenReturn(Optional.of(estado(1L, "ABIERTO")));

            assertThatThrownBy(() -> service.createEstadoTicket(estado(null, "ABIERTO")))
                    .isInstanceOf(YaExisteEnBdException.class)
                    .hasMessageContaining("ABIERTO");

            verify(repo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteEstadoTicket")
    class Delete {

        @Test
        @DisplayName("elimina estado existente sin excepción")
        void elimina() {
            when(repo.findById(1L)).thenReturn(Optional.of(estado(1L, "ABIERTO")));
            doNothing().when(repo).deleteById(1L);

            assertThatCode(() -> service.deleteEstadoTicket(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteEstadoTicket(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).deleteById(any());
        }
    }
}