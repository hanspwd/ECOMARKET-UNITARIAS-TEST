package com.ecomarket.soporteservice.Service;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.model.entity.Resena;
import com.ecomarket.soporteservice.repository.ResenaRepository;
import com.ecomarket.soporteservice.service.ResenaService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResenaService")
class ResenaServiceTest {

    @Mock
    private ResenaRepository repo;

    @InjectMocks
    private ResenaService service;

    private Resena resena(Long id, Long productoId, Long clienteId, Integer estrellas) {
        Resena r = new Resena();
        r.setId(id);
        r.setProductoId(productoId);
        r.setClienteId(clienteId);
        r.setCalificacionEstrellas(estrellas);
        r.setComentario("Buen producto para el hogar");
        r.setFechaPublicacion(LocalDateTime.now());
        r.setModeracionAprobado(false);
        return r;
    }

    @Nested
    @DisplayName("dejarResena")
    class DejarResena {

        @Test
        @DisplayName("crea reseña con moderacionAprobado=false y fecha asignada")
        void creaResena() {
            when(repo.save(any())).thenAnswer(inv -> {
                Resena r = inv.getArgument(0);
                r.setId(1L);
                return r;
            });

            Resena resultado = service.dejarResena(10L, 5L, 8, "  Muy buen producto  ");

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getComentario()).isEqualTo("Muy buen producto"); // trim
            assertThat(resultado.getModeracionAprobado()).isFalse();
            assertThat(resultado.getFechaPublicacion()).isNotNull();
            assertThat(resultado.getProductoId()).isEqualTo(10L);
            assertThat(resultado.getClienteId()).isEqualTo(5L);
            assertThat(resultado.getCalificacionEstrellas()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("readAllResenas")
    class ReadAll {

        @Test
        @DisplayName("retorna lista completa")
        void retornaLista() {
            when(repo.findAll()).thenReturn(List.of(
                    resena(1L, 1L, 1L, 9),
                    resena(2L, 2L, 2L, 7)));
            assertThat(service.readAllResenas()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("readResenasByProductoId")
    class ByProducto {

        @Test
        @DisplayName("retorna reseñas del producto")
        void retornaResenasProducto() {
            when(repo.findByProductoId(10L)).thenReturn(List.of(resena(1L, 10L, 5L, 8)));
            assertThat(service.readResenasByProductoId(10L)).hasSize(1);
        }

        @Test
        @DisplayName("retorna vacío si el producto no tiene reseñas")
        void retornaVacio() {
            when(repo.findByProductoId(99L)).thenReturn(List.of());
            assertThat(service.readResenasByProductoId(99L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("readResenasByClienteId")
    class ByCliente {

        @Test
        @DisplayName("retorna reseñas del cliente")
        void retornaResenasCliente() {
            when(repo.findByClienteId(5L)).thenReturn(List.of(
                    resena(1L, 10L, 5L, 8),
                    resena(2L, 11L, 5L, 6)));
            assertThat(service.readResenasByClienteId(5L)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findResenaById")
    class FindById {

        @Test
        @DisplayName("retorna la reseña cuando existe")
        void retornaResena() {
            when(repo.findById(1L)).thenReturn(Optional.of(resena(1L, 10L, 5L, 9)));
            assertThat(service.findResenaById(1L).getCalificacionEstrellas()).isEqualTo(9);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findResenaById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("aprobarModeracion")
    class Aprobar {

        @Test
        @DisplayName("cambia moderacionAprobado a true")
        void aprueba() {
            Resena r = resena(1L, 10L, 5L, 8);
            when(repo.findById(1L)).thenReturn(Optional.of(r));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.aprobarModeracion(1L);

            assertThat(r.getModeracionAprobado()).isTrue();
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando la reseña no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.aprobarModeracion(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("rechazarModeracion")
    class Rechazar {

        @Test
        @DisplayName("cambia moderacionAprobado a false")
        void rechaza() {
            Resena r = resena(1L, 10L, 5L, 8);
            r.setModeracionAprobado(true); // estaba aprobada
            when(repo.findById(1L)).thenReturn(Optional.of(r));
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.rechazarModeracion(1L);

            assertThat(r.getModeracionAprobado()).isFalse();
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando la reseña no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.rechazarModeracion(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("deleteResenaById")
    class Delete {

        @Test
        @DisplayName("elimina reseña existente")
        void elimina() {
            when(repo.findById(1L)).thenReturn(Optional.of(resena(1L, 10L, 5L, 8)));
            doNothing().when(repo).deleteById(1L);

            assertThatCode(() -> service.deleteResenaById(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoExisteEnBdException cuando no existe")
        void lanzaExcepcion() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteResenaById(99L))
                    .isInstanceOf(NoExisteEnBdException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).deleteById(any());
        }
    }
}