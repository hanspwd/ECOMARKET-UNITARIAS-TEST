package com.ecomarket.carritocompraservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.carritocompraservice.dto.AnadirProductoRequestDTO;
import com.ecomarket.carritocompraservice.model.Carrito;
import com.ecomarket.carritocompraservice.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    @Autowired
    private CarritoService carritoService;

    // Retorna todos los carritos
    @GetMapping
    public ResponseEntity<List<Carrito>> listarCarritos() {
        return ResponseEntity.ok(carritoService.listarTodos());
    }

    // Obtiene el carrito activo del cliente, creándolo si no existe
    @GetMapping("/{clienteId}")
    public ResponseEntity<Carrito> obtenerCarrito(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.obtenerCarritoActivo(clienteId));
    }

    // Agrega un producto al carrito del cliente con su precio y cantidad
    // CAMBIO: body ya no incluye precioUnitario
    @PostMapping
    public ResponseEntity<Carrito> anadirProducto(
            @RequestBody AnadirProductoRequestDTO dto) {
        return ResponseEntity.ok(carritoService.anadirProducto(
                dto.getClienteId(),
                dto.getProductoId(),
                dto.getCantidad()));
    }

    // Elimina un item específico del carrito por su ID de item
    @DeleteMapping("/{clienteId}/item/{itemId}")
    public ResponseEntity<Carrito> removerProducto(
            @PathVariable Long clienteId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(carritoService.removerProducto(clienteId, itemId));
    }

    // Asigna el tipo de envío seleccionado al carrito del cliente
    @PutMapping("/{clienteId}/envio")
    public ResponseEntity<Carrito> seleccionarEnvio(
            @PathVariable Long clienteId,
            @RequestParam Long tipoEnvioId) {
        return ResponseEntity.ok(carritoService.seleccionarTipoEnvio(clienteId, tipoEnvioId));
    }

    // Elimina todos los items del carrito activo del cliente
    @DeleteMapping("/{clienteId}/vaciar")
    public ResponseEntity<Boolean> vaciarCarrito(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.vaciarCarrito(clienteId));
    }

    // Marca el carrito como inactivo e inicia el proceso de compra, retorna el ID del carrito
    @PostMapping("/{clienteId}/checkout")
    public ResponseEntity<Long> iniciarCompra(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.iniciarProcesoCompra(clienteId));
    }
}
