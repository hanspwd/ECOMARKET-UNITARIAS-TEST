package com.ecomarket.soporteservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MensajeChatRequestDTO {

    @NotNull(message = "El ID del ticket es obligatorio.")
    private Long ticketId;

    @NotNull(message = "El ID del remitente es obligatorio.")
    private Long remitenteId;

    @NotNull(message = "Debe indicar si el remitente es cliente.")
    private Boolean esCliente;

    @NotBlank(message = "El contenido del mensaje no puede estar vacio.")
    private String contenido;
}
