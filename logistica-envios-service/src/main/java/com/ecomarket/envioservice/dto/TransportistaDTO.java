package com.ecomarket.envioservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransportistaDTO {

    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String correo;
}
