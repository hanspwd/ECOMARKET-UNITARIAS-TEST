package com.ecomarket.soporteservice.model.entity;

import java.time.LocalDateTime;

import com.ecomarket.soporteservice.model.reference.CanalNotificacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "notificacion")
public class Notificacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del destinatario es obligatorio.")
    @Column(nullable = false)
    private Long destinatarioId;

    @NotNull(message = "El canal de notificacion es obligatorio.")
    @ManyToOne
    @JoinColumn(name = "canal_id")
    private CanalNotificacion canal;

    @NotBlank(message = "El titulo no puede estar vacio.")
    @Size(max = 200, message = "El titulo no puede exceder los 200 caracteres.")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El cuerpo no puede estar vacio.")
    @Size(max = 2000, message = "El cuerpo no puede exceder los 2000 caracteres.")
    @Column(nullable = false, length = 2000)
    private String cuerpo;

    @NotNull(message = "La fecha de envio es obligatoria.")
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEnvioNotificacion;

    @NotNull(message = "Debe indicar si la notificacion fue enviada con exito.")
    @Column(nullable = false)
    private Boolean enviadaConExito;


}
