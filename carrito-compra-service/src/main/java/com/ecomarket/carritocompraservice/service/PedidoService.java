package com.ecomarket.carritocompraservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomarket.carritocompraservice.model.Carrito;
import com.ecomarket.carritocompraservice.model.EstadoPedido;
import com.ecomarket.carritocompraservice.model.ItemPedido;
import com.ecomarket.carritocompraservice.model.Pedido;
import com.ecomarket.carritocompraservice.repository.CarritoRepository;
import com.ecomarket.carritocompraservice.repository.EstadoPedidoRepository;
import com.ecomarket.carritocompraservice.repository.PedidoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private EstadoPedidoRepository estadoPedidoRepository;

    public Pedido generarPedidoDesdeCarrito(Long clienteId, Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));

        EstadoPedido estadoInicial = estadoPedidoRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Estado inicial no configurado"));

        List<ItemPedido> items = carrito.getItems().stream().map(ic -> {
            ItemPedido ip = new ItemPedido();
            ip.setProductoId(ic.getProductoId());
            ip.setCantidad(ic.getCantidad());
            ip.setPrecioUnitarioHistorico(ic.getPrecioUnitarioAgregado());
            return ip;
        }).collect(Collectors.toList());

        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setEstado(estadoInicial);
        pedido.setItems(items);
        pedido.setSubtotal(carrito.calcularTotal());
        pedido.setTotal(carrito.calcularTotal());
        items.forEach(i -> i.setPedido(pedido));

        return pedidoRepository.save(pedido);
    }

    public Pedido actualizarEstado(Long pedidoId, Long nuevoEstadoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));
        EstadoPedido nuevoEstado = estadoPedidoRepository.findById(nuevoEstadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + nuevoEstadoId));
        pedido.setEstado(pedido.getEstado());
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }


    public List<Pedido> obtenerHistorialCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    public Pedido buscarPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }
}
