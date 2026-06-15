package com.ecomarket.soporteservice.controller;

import com.ecomarket.soporteservice.exception.NoExisteEnBdException;
import com.ecomarket.soporteservice.exception.YaExisteEnBdException;
import com.ecomarket.soporteservice.model.reference.CanalNotificacion;
import com.ecomarket.soporteservice.service.CanalNotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CanalNotificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CanalNotificacionControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean CanalNotificacionService service;

    private CanalNotificacion canal(Long id, String nombre) {
        return new CanalNotificacion(id, nombre);
    }

    @Test
    @DisplayName("GET /api/v1/canal-notificacion → 200 lista")
    void getAll() throws Exception {
        when(service.readAllCanalNotificacion()).thenReturn(List.of(canal(1L,"EMAIL"), canal(2L,"SMS")));
        mvc.perform(get("/api/v1/canal-notificacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/canal-notificacion → 201 creado")
    void postCanal() throws Exception {
        when(service.createCanalNotificacion(any())).thenReturn(canal(3L,"PUSH"));
        mvc.perform(post("/api/v1/canal-notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(canal(null,"PUSH"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @DisplayName("POST /api/v1/canal-notificacion nombre vacío → 400")
    void postCanalInvalido() throws Exception {
        mvc.perform(post("/api/v1/canal-notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(canal(null,""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST duplicado → 409 (YaExisteEnBdException)")
    void postDuplicado() throws Exception {
        when(service.createCanalNotificacion(any())).thenThrow(new YaExisteEnBdException("EMAIL ya existe."));
        mvc.perform(post("/api/v1/canal-notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(canal(null,"EMAIL"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/v1/canal-notificacion/1 → 200")
    void deleteExistente() throws Exception {
        doNothing().when(service).deleteCanalNotificacionById(1L);
        mvc.perform(delete("/api/v1/canal-notificacion/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1")));
    }

    @Test
    @DisplayName("DELETE inexistente → 404 (NoExisteEnBdException)")
    void deleteInexistente() throws Exception {
        doThrow(new NoExisteEnBdException("99 no existe.")).when(service).deleteCanalNotificacionById(99L);
        mvc.perform(delete("/api/v1/canal-notificacion/99"))
                .andExpect(status().isNotFound());
    }
}