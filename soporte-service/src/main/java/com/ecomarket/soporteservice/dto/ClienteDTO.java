package com.ecomarket.soporteservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClienteDTO {

    private Long clienteId;
    private String correo;
    private String nombre;
    private String apellido;
    private RolDTO rolUsuario;

}
