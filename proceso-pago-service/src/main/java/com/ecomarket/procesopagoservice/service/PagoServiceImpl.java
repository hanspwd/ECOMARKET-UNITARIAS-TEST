package com.ecomarket.procesopagoservice.service;

import com.ecomarket.procesopagoservice.model.*;
import com.ecomarket.procesopagoservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación de PagoService.
 *
 * Puntos de integración con otros microservicios (a implementar cuando se unan):
 *
 *  - carrito-compra-service  → iniciarPago: obtener montos reales del carrito
 *                            → procesarConTransbank: cerrar carrito tras pago aprobado
 *
 *  - registro-usuarios-service → generarFactura: obtener nombre del cliente
 *                              → enviarBoletaEmail: obtener email si no se pasa uno
 *                              → procesarConTransbank: obtener dirección para el envío
 *
 *  - logistica-envios-service → procesarConTransbank: crear envío tras pago aprobado
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final TransaccionRepository transaccionRepository;
    private final FacturaRepository facturaRepository;
    private final CuponRepository cuponRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final RestTemplate restTemplate;

    @Override
    public TransaccionPago iniciarPago(Long pedidoId, Long clienteId, Double monto, MetodoPagoTransaccion metodo) {
        EstadoPago estadoPendiente = estadoPagoRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new RuntimeException("Estado PENDIENTE no encontrado"));

        // TODO: consultar carrito-compra-service para obtener subtotal/descuento/total reales

        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setPedidoId(pedidoId);
        transaccion.setClienteId(clienteId);
        transaccion.setMontoSubtotal(monto);
        transaccion.setMontoDescuento(0.0);
        transaccion.setMontoTotal(monto);
        transaccion.setMetodoPago(metodo);
        transaccion.setEstado(estadoPendiente);

        return transaccionRepository.save(transaccion);
    }

    @Override
    public TransaccionPago anadirCuponDescuento(Long transaccionId, Long cuponId) {
        TransaccionPago transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transaccionId));

        CuponDescuento cupon = cuponRepository.findById(cuponId)
                .orElseThrow(() -> new RuntimeException("Cupón no encontrado: " + cuponId));

        if (!cupon.esValido()) {
            throw new RuntimeException("El cupón no es válido o está expirado");
        }

        Double descuento = transaccion.getMontoSubtotal() * (cupon.getPorcentajeDescuento() / 100.0);
        if (cupon.getMontoMaximoDescuento() != null) {
            descuento = Math.min(descuento, cupon.getMontoMaximoDescuento());
        }

        transaccion.setMontoDescuento(descuento);
        transaccion.setMontoTotal(transaccion.getMontoSubtotal() - descuento);
        transaccion.setCuponUtilizadoId(cuponId);

        return transaccionRepository.save(transaccion);
    }

    @Override
    public TransaccionPago procesarConTransbank(Long transaccionId, String token) {
        TransaccionPago transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transaccionId));

        EstadoPago estadoAprobado = estadoPagoRepository.findByNombre("APROBADO")
                .orElseThrow(() -> new RuntimeException("Estado APROBADO no encontrado"));

        transaccion.setTokenTransbank(token);
        transaccion.setCodigoAutorizacion(LocalDateTime.now());
        transaccion.setEstado(estadoAprobado);

        // 1. Generar pedido en pedidoservice
        String pedidoUrl = "http://localhost:8089/api/pedidos/generar/" + transaccion.getClienteId() + "/" + transaccion.getPedidoId();
        try {
            restTemplate.postForEntity(pedidoUrl, null, String.class);
        } catch (Exception e) {
            log.error("Error al generar pedido: {}", e.getMessage());
        }

        // 2. Vaciar carrito en carritocompraservice
        String emptyCartUrl = "http://localhost:8082/api/carrito/" + transaccion.getClienteId() + "/vaciar";
        try {
            restTemplate.delete(emptyCartUrl);
        } catch (Exception e) {
            log.error("Error al vaciar carrito: {}", e.getMessage());
        }

        registrarLog(transaccion.getClienteId(), "PAGO_APROBADO", "Pago aprobado exitosamente para la transacción " + transaccionId + ". Monto: " + transaccion.getMontoTotal());

        return transaccionRepository.save(transaccion);
    }

    @Override
    public Boolean procesarReembolso(Long transaccionId, String motivo) {
        TransaccionPago transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transaccionId));

        EstadoPago estadoReembolsado = estadoPagoRepository.findByNombre("REEMBOLSADO")
                .orElseThrow(() -> new RuntimeException("Estado REEMBOLSADO no encontrado"));

        transaccion.setEstado(estadoReembolsado);
        transaccionRepository.save(transaccion);

        // TODO: POST a tabla/servicio de REMBOLSOS con motivo
        log.info("Reembolso procesado para transacción {}: {}", transaccionId, motivo);
        return true;
    }

    @Override
    public FacturaElectronica generarFactura(Long transaccionId, Long rut, String giro) {
        TransaccionPago transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transaccionId));

        // TODO: obtener nombre real del cliente desde registro-usuarios-service

        FacturaElectronica factura = new FacturaElectronica();
        factura.setTransaccionId(transaccionId);
        factura.setClienteId(transaccion.getClienteId());
        factura.setRutReceptor(String.valueOf(rut));
        factura.setRazonSocial(giro);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setFolioFiscal(System.currentTimeMillis());
        factura.setXmlDocumento("<factura><transaccion>" + transaccionId
                + "</transaccion><monto>" + transaccion.getMontoTotal() + "</monto></factura>");

        return facturaRepository.save(factura);
    }

    @Override
    public Boolean enviarBoletaEmail(Long transaccionId, String correoDestino) {
        transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transaccionId));

        // TODO: si correoDestino viene vacío, obtenerlo desde registro-usuarios-service
        // TODO: integrar con servicio de email (SendGrid, SES, JavaMail)
        log.info("Enviando boleta de transacción {} a {}", transaccionId, correoDestino);
        return true;
    }

    @Override
    public TransaccionPago obtenerTransaccion(Long transaccionId) {
        return transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transaccionId));
    }

    private void registrarLog(Long usuarioId, String accion, String detalles) {
        Map<String, Object> log = new HashMap<>();
        log.put("microservicio", "servicepago");
        log.put("accion", accion);
        log.put("usuarioId", usuarioId);
        log.put("detalles", detalles);
        log.put("fecha", LocalDateTime.now());

        try {
            restTemplate.postForEntity("http://localhost:8084/api/analitica/logs", log, String.class);
        } catch (Exception e) {
            // Log error but don't break business flow
        }
    }
}
