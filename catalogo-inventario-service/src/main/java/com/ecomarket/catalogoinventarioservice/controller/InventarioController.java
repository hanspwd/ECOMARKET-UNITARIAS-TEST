package com.ecomarket.catalogoinventarioservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.catalogoinventarioservice.model.InventarioStock;
import com.ecomarket.catalogoinventarioservice.service.InventarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {
    private final InventarioService inventarioService;

    // Retorna todo el inventario
    @GetMapping
    public ResponseEntity<List<InventarioStock>> listarInventario() {
        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    // Verifica si hay stock suficiente de un producto para la cantidad solicitada
    @GetMapping("/disponibilidad/{productoId}")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @PathVariable Long productoId,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(inventarioService.verificarDisponibilidad(productoId, cantidad));
    }

    // Retorna el stock de un producto en todas las sucursales
    @GetMapping("/global/{productoId}")
    public ResponseEntity<List<InventarioStock>> consultarGlobal(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.consultarInventarioGlobal(productoId));
    }

    // Retorna el stock de un producto en una sucursal específica
    @GetMapping("/sucursal/{sucursalId}/producto/{productoId}")
    public ResponseEntity<List<InventarioStock>> consultarPorSucursal(
            @PathVariable Long sucursalId,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.consultarInventarioPorSucursal(sucursalId, productoId));
    }

    // Reserva una cantidad de stock de un producto, descontándola del disponible
    @PostMapping("/reservar/{productoId}")
    public ResponseEntity<Boolean> reservarStock(
            @PathVariable Long productoId,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(inventarioService.reservarStock(productoId, cantidad));
    }

    // Libera stock previamente reservado, devolviéndolo al disponible
    @PostMapping("/liberar/{productoId}")
    public ResponseEntity<Boolean> liberarStock(
            @PathVariable Long productoId,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(inventarioService.liberarStock(productoId, cantidad));
    }

    // Ajusta directamente la cantidad disponible de un producto en una sucursal
    @PutMapping("/ajustar/{productoId}/sucursal/{sucursalId}")
    public ResponseEntity<InventarioStock> ajustarStock(
            @PathVariable Long productoId,
            @PathVariable Long sucursalId,
            @RequestParam Integer nuevaCantidad) {
        return ResponseEntity.ok(inventarioService.ajustarStock(productoId, sucursalId, nuevaCantidad));
    }
}
