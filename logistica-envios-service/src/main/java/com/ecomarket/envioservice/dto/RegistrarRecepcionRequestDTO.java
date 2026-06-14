package com.ecomarket.envioservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrarRecepcionRequestDTO {

    @NotBlank(message = "La firma de quien recibe es obligatoria.")
    private String firmaRecibe;
}
