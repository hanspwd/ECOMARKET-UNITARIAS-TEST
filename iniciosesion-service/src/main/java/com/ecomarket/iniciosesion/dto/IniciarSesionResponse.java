package com.ecomarket.iniciosesion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarSesionResponse {

    private String token;
    private Long usuarioId;
    private String correo;
    private String rol;
    private long expiracionMs;
}
