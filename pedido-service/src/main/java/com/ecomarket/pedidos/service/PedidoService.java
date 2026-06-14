package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.model.Pedido;
import java.util.List;

public interface PedidoService {
    Pedido generarPedidoDesdeCarrito(Long clienteId, Long carritoId);
    Pedido actualizarEstado(Long pedidoId, Long nuevoEstadoId);
    List<Pedido> obtenerHistorialCliente(Long clienteId);
    Pedido buscarPorId(Long pedidoId);
}
