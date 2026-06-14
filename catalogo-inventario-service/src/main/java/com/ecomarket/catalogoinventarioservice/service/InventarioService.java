package com.ecomarket.catalogoinventarioservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecomarket.catalogoinventarioservice.client.GestionTiendaClient;
import com.ecomarket.catalogoinventarioservice.model.InventarioStock;
import com.ecomarket.catalogoinventarioservice.repository.InventarioStockRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioStockRepository inventarioRepository;
    private final GestionTiendaClient gestionTiendaClient;

    public List<InventarioStock> listarTodos() {
        return inventarioRepository.findAll();
    }

    public boolean verificarDisponibilidad(Long productoId, Integer cantidad) {
        return inventarioRepository.findByProductoId(productoId)
                .stream().anyMatch(s -> s.hayStock(cantidad));
    }

    public List<InventarioStock> consultarInventarioGlobal(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    public List<InventarioStock> consultarInventarioPorSucursal(Long sucursalId, Long productoId) {
        return inventarioRepository.findBySucursalIdAndProductoId(sucursalId, productoId);
    }

    public boolean reservarStock(Long productoId, Integer cantidad) {
        return inventarioRepository.findByProductoId(productoId).stream()
                .filter(s -> s.hayStock(cantidad))
                .findFirst()
                .map(s -> {
                    s.setCantidadDisponible(s.getCantidadDisponible() - cantidad);
                    s.setCantidadReservada(s.getCantidadReservada() + cantidad);
                    inventarioRepository.save(s);

                    if (s.getStockMinimoAlerta() != null
                            && s.getCantidadDisponible() <= s.getStockMinimoAlerta()) {
                        gestionTiendaClient.notificarStockBajo(
                            s.getSucursalId(), productoId, s.getCantidadDisponible());
                    }

                    return true;
                }).orElse(false);
    }

    public boolean liberarStock(Long productoId, Integer cantidad) {
        return inventarioRepository.findByProductoId(productoId).stream()
                .filter(s -> s.getCantidadReservada() >= cantidad)
                .findFirst()
                .map(s -> {
                    s.setCantidadReservada(s.getCantidadReservada() - cantidad);
                    s.setCantidadDisponible(s.getCantidadDisponible() + cantidad);
                    inventarioRepository.save(s);
                    return true;
                }).orElse(false);
    }

    public InventarioStock ajustarStock(Long productoId, Long sucursalId, Integer nuevaCantidad) {
        InventarioStock stock = inventarioRepository
                .findTopByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado"));
        stock.setCantidadDisponible(nuevaCantidad);
        return inventarioRepository.save(stock);
    }
}