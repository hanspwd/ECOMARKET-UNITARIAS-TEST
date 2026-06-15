package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.exception.YaExisteEnBdException;
import com.ecomarket.soporteservice.model.reference.CategoriaTicket;
import com.ecomarket.soporteservice.repository.CategoriaTicketRepository;
import com.ecomarket.soporteservice.service.CategoriaTicketService;

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
@DisplayName("CategoriaTicketService")
class CategoriaTicketServiceTest {

    @Mock
    private CategoriaTicketRepository repo;

    @InjectMocks
    private CategoriaTicketService service;

    private CategoriaTicket categoria(Long id, String nombre) {
        CategoriaTicket c = new CategoriaTicket();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    @Nested
    @DisplayName("readAllCategoriaTicket")
    class ReadAll {

        @Test
        @DisplayName("retorna lista completa")
        void retornaLista() {
            when(repo.findAll()).thenReturn(List.of(
                    categoria(1L, "ENTREGA"), categoria(2L, "PAGO")));
            assertThat(service.readAllCategoriaTicket()).hasSize(2);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay categorías")
        void retornaVacio() {
            when(repo.findAll()).thenReturn(List.of());
            assertThat(service.readAllCategoriaTicket()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findCategoriaTicketById")
    class FindById {

        @Test
        @DisplayName("retorna la categoría cuando existe")
        void retornaCategoria() {
            when(repo.findById(1L)).thenReturn(Optional.of(categoria(1L, "ENTREGA")));
            assertThat(service.findCategoriaTicketById(1L).getNombre()).isEqualTo("ENTREGA");
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findCategoriaTicketById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("createCategoriaTicket")
    class Create {

        @Test
        @DisplayName("crea categoría cuando el nombre no existe")
        void creaCategoria() {
            when(repo.findByNombre("DEVOLUCION")).thenReturn(Optional.empty());
            when(repo.save(any())).thenReturn(categoria(3L, "DEVOLUCION"));

            CategoriaTicket resultado = service.createCategoriaTicket(categoria(null, "DEVOLUCION"));
            assertThat(resultado.getId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("lanza YaExisteEnBdException cuando el nombre ya existe")
        void lanzaDuplicado() {
            when(repo.findByNombre("ENTREGA")).thenReturn(Optional.of(categoria(1L, "ENTREGA")));

            assertThatThrownBy(() -> service.createCategoriaTicket(categoria(null, "ENTREGA")))
                    .isInstanceOf(YaExisteEnBdException.class)
                    .hasMessageContaining("ENTREGA");

            verify(repo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteCategoriaTicketById")
    class Delete {

        @Test
        @DisplayName("elimina categoría existente")
        void elimina() {
            when(repo.findById(1L)).thenReturn(Optional.of(categoria(1L, "ENTREGA")));
            doNothing().when(repo).deleteById(1L);

            assertThatCode(() -> service.deleteCategoriaTicketById(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteCategoriaTicketById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).deleteById(any());
        }
    }
}