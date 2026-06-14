package com.ecomarket.iniciosesion.controller;

import com.ecomarket.iniciosesion.service.LoginCuentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginCuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    

    @MockitoBean
    private LoginCuentaService loginCuentaService;

    @Test
    void testCrearCredencial() throws Exception {
        // lo vemos juntos
    }
}