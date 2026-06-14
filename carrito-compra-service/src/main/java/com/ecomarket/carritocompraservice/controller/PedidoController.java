package com.ecomarket.carritocompraservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.carritocompraservice.model.Pedido;
import com.ecomarket.carritocompraservice.service.PedidoService;

@RestController
@RequestMapping("/api/pedido")
public class PedidoController {
    @Autowired
    private PedidoService pedidoService;

    // Genera un pedido formal a partir del carrito cerrado del cliente
    @PostMapping("/generar")
    public ResponseEntity<Pedido> generarPedido(
            @RequestParam Long clienteId,
            @RequestParam Long carritoId) {
        return ResponseEntity.ok(pedidoService.generarPedidoDesdeCarrito(clienteId, carritoId));
    }

    // Actualiza el estado de un pedido existente por su ID
    @PutMapping("/{pedidoId}/estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long pedidoId,
            @RequestParam Long nuevoEstadoId) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(pedidoId, nuevoEstadoId));
    }

    // Retorna todos los pedidos
    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    // Retorna el historial de pedidos de un cliente
    @GetMapping("/historial/{clienteId}")
    public ResponseEntity<List<Pedido>> historialCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.obtenerHistorialCliente(clienteId));
    }

    // Busca y retorna un pedido por su ID
    @GetMapping("/{pedidoId}")
    public ResponseEntity<Pedido> buscarPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoService.buscarPorId(pedidoId));
    }
}
