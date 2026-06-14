package com.ecomarket.iniciosesion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutenticarJWTResponse {

    private boolean valido;
    private Long usuarioId;
    private String correo;
    private List<String> roles;
}
