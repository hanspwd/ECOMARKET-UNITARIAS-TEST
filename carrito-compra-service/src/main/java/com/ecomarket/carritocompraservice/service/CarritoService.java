package com.ecomarket.carritocompraservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomarket.carritocompraservice.client.CatalogoInventarioClient;
import com.ecomarket.carritocompraservice.dto.ProductoClienteDTO;
import com.ecomarket.carritocompraservice.model.Carrito;
import com.ecomarket.carritocompraservice.model.ItemCarrito;
import com.ecomarket.carritocompraservice.repository.CarritoRepository;
import com.ecomarket.carritocompraservice.repository.ItemCarritoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CarritoService {
    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    // NUEVO: Cliente HTTP hacia catalogo-inventario-service
    @Autowired
    private CatalogoInventarioClient catalogoClient;

    public Carrito obtenerCarritoActivo(Long clienteId) {
        return carritoRepository.findByClienteIdAndActivoTrue(clienteId)
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setClienteId(clienteId);
                    return carritoRepository.save(nuevo);
                });
    }

    // CAMBIO: Obtiene precioUnitario desde catalogo-inventario-service
    // Verifica disponibilidad de stock antes de agregar
    public Carrito anadirProducto(Long clienteId, Long productoId, Integer cantidad) {

        // Verifica que el producto existe y obtiene su precio real
        ProductoClienteDTO producto = catalogoClient.obtenerProducto(productoId);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado: " + productoId);
        }

        // Verifica que haya stock disponible
        boolean hayStock = catalogoClient.verificarDisponibilidad(productoId, cantidad);
        if (!hayStock) {
            throw new RuntimeException("Stock insuficiente para producto: " + productoId);
        }

        Carrito carrito = obtenerCarritoActivo(clienteId);

        itemCarritoRepository.findByCarritoIdAndProductoId(carrito.getId(), productoId)
                .ifPresentOrElse(
                        item -> {
                            item.setCantidad(item.getCantidad() + cantidad);
                            itemCarritoRepository.save(item);
                        },
                        () -> {
                            ItemCarrito item = new ItemCarrito();
                            item.setCarrito(carrito);
                            item.setProductoId(productoId);
                            item.setCantidad(cantidad);
                            // CAMBIADO: usa precioBase del catalogo en lugar del parámetro
                            item.setPrecioUnitarioAgregado(producto.getPrecioBase());
                            itemCarritoRepository.save(item);
                        });

        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    public Carrito removerProducto(Long clienteId, Long itemId) {
        Carrito carrito = obtenerCarritoActivo(clienteId);
        itemCarritoRepository.deleteById(itemId);
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    public Carrito seleccionarMetodoPago(Long clienteId, Long metodoPagoId) {
        Carrito carrito = obtenerCarritoActivo(clienteId);
        carrito.setMetodoPagoSeleccionadoId(metodoPagoId);
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }
    
    public Carrito seleccionarTipoEnvio(Long clienteId, Long tipoEnvioId) { // METODO DE PAGO ID
        Carrito carrito = obtenerCarritoActivo(clienteId);
        carrito.setTipoEnvioSeleccionadoId(tipoEnvioId);
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // CAMBIO: Libera el stock de cada ítem en catalogo-inventario-service antes de limpiar el carrito
    public boolean vaciarCarrito(Long clienteId) {
        Carrito carrito = obtenerCarritoActivo(clienteId);

        carrito.getItems().forEach(item ->
            catalogoClient.liberarStock(item.getProductoId(), item.getCantidad())
        );

        carrito.getItems().clear();
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        carritoRepository.save(carrito);
        return true;
    }

    // CAMBIO: reserva el stock de cada ítem en catalogo-inventario-service antes de cerrar el carrito
    public List<Carrito> listarTodos() {
        return carritoRepository.findAll();
    }

    public Long iniciarProcesoCompra(Long clienteId) {
        Carrito carrito = obtenerCarritoActivo(clienteId);

        carrito.getItems().forEach(item -> {
            boolean reservado = catalogoClient.reservarStock(
                    item.getProductoId(), item.getCantidad());
            if (!reservado) {
                throw new RuntimeException(
                    "No se pudo reservar stock para producto: " + item.getProductoId());
            }
        });

        carrito.setActivo(false);
        carritoRepository.save(carrito);
        return carrito.getId();
    }
}
