package com.ecomarket.service;

import com.ecomarket.carritocompraservice.client.CatalogoInventarioClient;
import com.ecomarket.carritocompraservice.dto.ProductoClienteDTO;
import com.ecomarket.carritocompraservice.model.Carrito;
import com.ecomarket.carritocompraservice.model.ItemCarrito;
import com.ecomarket.carritocompraservice.repository.CarritoRepository;
import com.ecomarket.carritocompraservice.repository.ItemCarritoRepository;
import com.ecomarket.carritocompraservice.service.CarritoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CarritoService.
 *
 * Ejecutar solo este servicio:
 *   mvn test -pl carrito-compra-service -Dtest=CarritoServiceTest
 *
 * CarritoService usa @Autowired en campos (field injection).
 * @InjectMocks se encarga de inyectar los mocks en esos campos.
 *
 * CatalogoInventarioClient se mockea completamente para aislar el comportamiento
 * del carrito sin depender de la red.
 */
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock private CarritoRepository         carritoRepository;
    @Mock private ItemCarritoRepository     itemCarritoRepository;
    @Mock private CatalogoInventarioClient  catalogoClient;

    @InjectMocks
    private CarritoService service;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Carrito carritoActivo(Long carritoId, Long clienteId) {
        Carrito c = new Carrito();
        c.setId(carritoId);
        c.setClienteId(clienteId);
        c.setActivo(true);
        c.setSubtotal(0.0);
        c.setItems(new ArrayList<>());
        return c;
    }

    private ProductoClienteDTO producto(Long id, Double precio) {
        return new ProductoClienteDTO(id, "SKU-" + id, "Producto " + id, precio, "Desc", null);
    }

    private ItemCarrito item(Long itemId, Long carritoId, Long productoId, int cantidad, double precio) {
        Carrito c = new Carrito();
        c.setId(carritoId);
        ItemCarrito item = new ItemCarrito();
        item.setId(itemId);
        item.setCarrito(c);
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        item.setPrecioUnitarioAgregado(precio);
        return item;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // obtenerCarritoActivo
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("obtenerCarritoActivo")
    class ObtenerCarritoActivo {

        @Test
        @DisplayName("retorna el carrito existente si el cliente ya tiene uno activo")
        void retornaCarritoExistente() {
            Carrito carrito = carritoActivo(1L, 10L);
            when(carritoRepository.findByClienteIdAndActivoTrue(10L))
                    .thenReturn(Optional.of(carrito));

            Carrito resultado = service.obtenerCarritoActivo(10L);

            assertThat(resultado.getId()).isEqualTo(1L);
            verify(carritoRepository, never()).save(any());
        }

        @Test
        @DisplayName("crea y persiste un nuevo carrito si el cliente no tiene uno activo")
        void creaCarritoNuevoSiNoExiste() {
            when(carritoRepository.findByClienteIdAndActivoTrue(20L))
                    .thenReturn(Optional.empty());
            when(carritoRepository.save(any(Carrito.class))).thenAnswer(i -> {
                Carrito c = i.getArgument(0);
                c.setId(2L);
                return c;
            });

            Carrito resultado = service.obtenerCarritoActivo(20L);

            assertThat(resultado.getClienteId()).isEqualTo(20L);
            verify(carritoRepository).save(any(Carrito.class));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // anadirProducto
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("anadirProducto")
    class AnadirProducto {

        @Test
        @DisplayName("agrega nuevo ítem con precio del catálogo cuando hay stock")
        void agregaItemNuevoConPrecioDelCatalogo() {
            Carrito carrito = carritoActivo(1L, 10L);
            ProductoClienteDTO prod = producto(100L, 4990.0);

            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(catalogoClient.obtenerProducto(100L)).thenReturn(prod);
            when(catalogoClient.verificarDisponibilidad(100L, 2)).thenReturn(true);
            when(itemCarritoRepository.findByCarritoIdAndProductoId(1L, 100L))
                    .thenReturn(Optional.empty());
            when(carritoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.anadirProducto(10L, 100L, 2);

            verify(itemCarritoRepository).save(argThat(it ->
                    it.getProductoId().equals(100L)
                    && it.getCantidad() == 2
                    && it.getPrecioUnitarioAgregado().equals(4990.0)
            ));
        }

        @Test
        @DisplayName("incrementa cantidad si el producto ya existe en el carrito")
        void incrementaCantidadSiProductoYaEstaEnCarrito() {
            Carrito carrito = carritoActivo(1L, 10L);
            ItemCarrito itemExistente = item(5L, 1L, 100L, 1, 4990.0);
            ProductoClienteDTO prod = producto(100L, 4990.0);

            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(catalogoClient.obtenerProducto(100L)).thenReturn(prod);
            when(catalogoClient.verificarDisponibilidad(100L, 3)).thenReturn(true);
            when(itemCarritoRepository.findByCarritoIdAndProductoId(1L, 100L))
                    .thenReturn(Optional.of(itemExistente));
            when(carritoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.anadirProducto(10L, 100L, 3);

            // Cantidad debe ser 1 + 3 = 4
            verify(itemCarritoRepository).save(argThat(it -> it.getCantidad() == 4));
        }

        @Test
        @DisplayName("lanza RuntimeException si el producto no existe en el catálogo")
        void productoInexistenteLanzaExcepcion() {
            when(catalogoClient.obtenerProducto(999L)).thenReturn(null);

            assertThatThrownBy(() -> service.anadirProducto(10L, 999L, 1))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("lanza RuntimeException si no hay stock suficiente")
        void stockInsuficienteLanzaExcepcion() {
            ProductoClienteDTO prod = producto(100L, 4990.0);
            when(catalogoClient.obtenerProducto(100L)).thenReturn(prod);
            when(catalogoClient.verificarDisponibilidad(100L, 50)).thenReturn(false);

            assertThatThrownBy(() -> service.anadirProducto(10L, 100L, 50))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Stock insuficiente");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // removerProducto
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removerProducto")
    class RemoverProducto {

        @Test
        @DisplayName("elimina el ítem por ID y actualiza la fecha del carrito")
        void removerItemExitosamente() {
            Carrito carrito = carritoActivo(1L, 10L);
            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(carritoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Carrito resultado = service.removerProducto(10L, 5L);

            verify(itemCarritoRepository).deleteById(5L);
            assertThat(resultado.getFechaUltimaModificacion()).isNotNull();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // seleccionarMetodoPago
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("seleccionarMetodoPago")
    class SeleccionarMetodoPago {

        @Test
        @DisplayName("persiste el metodoPagoId seleccionado en el carrito")
        void seleccionaMetodoPagoExitosamente() {
            Carrito carrito = carritoActivo(1L, 10L);
            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(carritoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Carrito resultado = service.seleccionarMetodoPago(10L, 3L);

            assertThat(resultado.getMetodoPagoSeleccionadoId()).isEqualTo(3L);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // seleccionarTipoEnvio
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("seleccionarTipoEnvio")
    class SeleccionarTipoEnvio {

        @Test
        @DisplayName("persiste el tipoEnvioId seleccionado en el carrito")
        void seleccionaTipoEnvioExitosamente() {
            Carrito carrito = carritoActivo(1L, 10L);
            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(carritoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Carrito resultado = service.seleccionarTipoEnvio(10L, 2L);

            assertThat(resultado.getTipoEnvioSeleccionadoId()).isEqualTo(2L);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // vaciarCarrito
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("vaciarCarrito")
    class VaciarCarrito {

        @Test
        @DisplayName("libera stock de cada ítem y limpia el carrito")
        void vaciaCarritoYLiberaStock() {
            Carrito carrito = carritoActivo(1L, 10L);
            ItemCarrito item1 = item(1L, 1L, 100L, 2, 4990.0);
            ItemCarrito item2 = item(2L, 1L, 200L, 1, 9990.0);
            carrito.setItems(new ArrayList<>(List.of(item1, item2)));

            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(catalogoClient.liberarStock(anyLong(), anyInt())).thenReturn(true);
            when(carritoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            boolean resultado = service.vaciarCarrito(10L);

            assertThat(resultado).isTrue();
            verify(catalogoClient).liberarStock(100L, 2);
            verify(catalogoClient).liberarStock(200L, 1);
            assertThat(carrito.getItems()).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // iniciarProcesoCompra
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("iniciarProcesoCompra")
    class IniciarProcesoCompra {

        @Test
        @DisplayName("reserva stock de todos los ítems y cierra el carrito (activo=false)")
        void reservaStockYCierraCarrito() {
            Carrito carrito = carritoActivo(1L, 10L);
            ItemCarrito item = item(1L, 1L, 100L, 2, 4990.0);
            carrito.setItems(new ArrayList<>(List.of(item)));

            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(catalogoClient.reservarStock(100L, 2)).thenReturn(true);
            when(carritoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Long carritoId = service.iniciarProcesoCompra(10L);

            assertThat(carritoId).isEqualTo(1L);
            assertThat(carrito.getActivo()).isFalse();
            verify(catalogoClient).reservarStock(100L, 2);
        }

        @Test
        @DisplayName("lanza RuntimeException si no se puede reservar stock de algún producto")
        void falloEnReservaLanzaExcepcion() {
            Carrito carrito = carritoActivo(1L, 10L);
            ItemCarrito item = item(1L, 1L, 100L, 5, 4990.0);
            carrito.setItems(new ArrayList<>(List.of(item)));

            when(carritoRepository.findByClienteIdAndActivoTrue(10L)).thenReturn(Optional.of(carrito));
            when(catalogoClient.reservarStock(100L, 5)).thenReturn(false);

            assertThatThrownBy(() -> service.iniciarProcesoCompra(10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("100");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listarTodos
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listarTodos")
    class ListarTodos {

        @Test
        @DisplayName("delega en findAll y retorna todos los carritos")
        void listaAllCarritos() {
            List<Carrito> carritos = List.of(
                    carritoActivo(1L, 10L),
                    carritoActivo(2L, 20L)
            );
            when(carritoRepository.findAll()).thenReturn(carritos);

            List<Carrito> resultado = service.listarTodos();

            assertThat(resultado).hasSize(2);
            verify(carritoRepository).findAll();
        }
    }
}