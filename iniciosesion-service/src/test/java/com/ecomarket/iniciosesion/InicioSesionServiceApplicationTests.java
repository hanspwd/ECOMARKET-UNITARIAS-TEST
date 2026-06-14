package com.ecomarket.iniciosesion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InicioSesionServiceApplicationTests {

    @Test
    void contextLoads() {
        // Esta prueba ya la tienes y es la que te dio el 40%
    }

    // AGREGAR ESTO PARA LLEGAR AL 100%
    @Test
    void mainIniciaCorrectamente() {
        // Llamamos directamente al método main pasándole un array vacío
        InicioSesionServiceApplication.main(new String[] {});
    }
}