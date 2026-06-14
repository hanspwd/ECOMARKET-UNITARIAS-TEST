package com.ecomarket.procesopagoservice.controller;

import com.ecomarket.procesopagoservice.model.FacturaElectronica;
import com.ecomarket.procesopagoservice.model.MetodoPagoTransaccion;
import com.ecomarket.procesopagoservice.model.TransaccionPago;
import com.ecomarket.procesopagoservice.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/iniciar")
    public ResponseEntity<TransaccionPago> iniciarPago(
            @RequestParam Long pedidoId,
            @RequestParam Long clienteId,
            @RequestParam Double monto,
            @RequestBody MetodoPagoTransaccion metodo) {
        return ResponseEntity.ok(pagoService.iniciarPago(pedidoId, clienteId, monto, metodo));
    }

    @GetMapping("/{transaccionId}")
    public ResponseEntity<TransaccionPago> obtenerTransaccion(@PathVariable Long transaccionId) {
        return ResponseEntity.ok(pagoService.obtenerTransaccion(transaccionId));
    }

    @PostMapping("/{transaccionId}/cupon/{cuponId}")
    public ResponseEntity<TransaccionPago> anadirCupon(
            @PathVariable Long transaccionId,
            @PathVariable Long cuponId) {
        return ResponseEntity.ok(pagoService.anadirCuponDescuento(transaccionId, cuponId));
    }

    @PostMapping("/{transaccionId}/transbank")
    public ResponseEntity<TransaccionPago> procesarTransbank(
            @PathVariable Long transaccionId,
            @RequestParam String token) {
        return ResponseEntity.ok(pagoService.procesarConTransbank(transaccionId, token));
    }

    @PostMapping("/{transaccionId}/reembolso")
    public ResponseEntity<Boolean> procesarReembolso(
            @PathVariable Long transaccionId,
            @RequestParam String motivo) {
        return ResponseEntity.ok(pagoService.procesarReembolso(transaccionId, motivo));
    }

    @PostMapping("/{transaccionId}/factura")
    public ResponseEntity<FacturaElectronica> generarFactura(
            @PathVariable Long transaccionId,
            @RequestParam Long rut,
            @RequestParam String giro) {
        return ResponseEntity.ok(pagoService.generarFactura(transaccionId, rut, giro));
    }

    @PostMapping("/{transaccionId}/email")
    public ResponseEntity<Boolean> enviarBoletaEmail(
            @PathVariable Long transaccionId,
            @RequestParam(required = false, defaultValue = "") String correoDestino) {
        return ResponseEntity.ok(pagoService.enviarBoletaEmail(transaccionId, correoDestino));
    }
}
