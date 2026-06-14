package com.ecomarket.carritocompraservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomarket.carritocompraservice.client.LogisticaEnvioClient;
import com.ecomarket.carritocompraservice.client.ProcesoPagoClient;
import com.ecomarket.carritocompraservice.dto.CompraRequestDTO;
import com.ecomarket.carritocompraservice.dto.CompraResultDTO;
import com.ecomarket.carritocompraservice.model.Carrito;
import com.ecomarket.carritocompraservice.model.Pedido;
import com.ecomarket.carritocompraservice.repository.CarritoRepository;

import jakarta.transaction.Transactional;

@Service
public class CompraOrchestratorService {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private LogisticaEnvioClient envioClient;

    @Autowired
    private ProcesoPagoClient pagoClient;

    private static final Logger log = LoggerFactory.getLogger(CompraOrchestratorService.class);

    @Transactional
    public CompraResultDTO ejecutarCompra(CompraRequestDTO request) {
        CompraResultDTO resultado = new CompraResultDTO();
        resultado.setEstado("INICIADO");

        // 1. Reservar stock del carrito y cerrarlo
        Long carritoId = carritoService.iniciarProcesoCompra(request.getClienteId());
        if (carritoId == null) {
            resultado.setEstado("ERROR: No se pudo iniciar el proceso de compra");
            return resultado;
        }
        resultado.setCarritoId(carritoId);

        Carrito carrito = carritoRepository.findById(carritoId).orElse(null);
        if (carrito == null) {
            resultado.setEstado("ERROR: Carrito no encontrado");
            return resultado;
        }

        // 2. Generar pedido desde el carrito
        Pedido pedido = pedidoService.generarPedidoDesdeCarrito(request.getClienteId(), carritoId);
        if (pedido == null) {
            resultado.setEstado("ERROR: No se pudo generar el pedido");
            return resultado;
        }
        resultado.setPedido(pedido);

        // 3. Iniciar pago en procesopagoservice
        Double montoTotal = pedido.getTotal() != null ? pedido.getTotal() : 0.0;
        Long transaccionId = pagoClient.iniciarPago(
                pedido.getId(), request.getClienteId(), montoTotal, request.getMetodoPagoId());
        if (transaccionId != null) {
            resultado.setTransaccionPagoId(transaccionId);
        } else {
            log.warn("pagoservice no disponible; la compra continua sin pago registrado");
        }

        // 4. Crear envio en envioservice
        Long envioId = envioClient.crearEnvio(
                pedido.getId(), request.getClienteId(), request.getMetodoEnvioId(), request.getDireccionId());
        if (envioId != null) {
            resultado.setEnvioId(envioId);
        } else {
            log.warn("envioservice no disponible; el pedido se creo sin envio registrado");
        }

        resultado.setEstado("COMPLETADO");
        return resultado;
    }
}
