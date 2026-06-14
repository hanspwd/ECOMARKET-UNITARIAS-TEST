package com.ecomarket.iniciosesion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponse {

    private String mensaje;

    public static MensajeResponse de(String mensaje) {
        return new MensajeResponse(mensaje);
    }
}
