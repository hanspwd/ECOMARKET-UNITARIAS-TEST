package com.ecomarket.soporteservice.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mensaje_chat")
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del ticket es obligatorio.")
    @Column(nullable = false)
    private Long ticketId;

    @NotNull(message = "El ID del remitente es obligatorio.")
    @Column(nullable = false)
    private Long remitenteId;

    @NotNull(message = "Debe indicar si el remitente es cliente.")
    @Column(nullable = false)
    private Boolean esCliente;

    @NotBlank(message = "El contenido del mensaje no puede estar vacio.")
    @Size(min = 1, max = 2000, message = "El contenido debe tener entre 1 y 2000 caracteres.")
    @Column(nullable = false, length = 2000)
    private String contenido;

    @NotNull(message = "La fecha de envio es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @NotNull(message = "Debe indicar si el mensaje fue leido.")
    @Column(nullable = false)
    private Boolean leido;
}
