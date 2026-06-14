package com.ecomarket.procesopagoservice.service;

import com.ecomarket.procesopagoservice.model.FacturaElectronica;
import com.ecomarket.procesopagoservice.model.MetodoPagoTransaccion;
import com.ecomarket.procesopagoservice.model.TransaccionPago;

public interface PagoService {
    TransaccionPago iniciarPago(Long pedidoId, Long clienteId, Double monto, MetodoPagoTransaccion metodo);
    TransaccionPago anadirCuponDescuento(Long transaccionId, Long cuponId);
    TransaccionPago procesarConTransbank(Long transaccionId, String token);
    Boolean procesarReembolso(Long transaccionId, String motivo);
    FacturaElectronica generarFactura(Long transaccionId, Long rut, String giro);
    Boolean enviarBoletaEmail(Long transaccionId, String correoDestino);
    TransaccionPago obtenerTransaccion(Long transaccionId);
}
