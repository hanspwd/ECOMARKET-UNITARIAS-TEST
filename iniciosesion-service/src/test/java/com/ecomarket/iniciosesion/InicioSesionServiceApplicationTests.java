package com.ecomarket.iniciosesion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // <--- 1. Le decimos a Spring que use tu application-test.properties
class InicioSesionServiceApplicationTests {

    @Test
    @DisplayName("El contexto de Spring Boot carga correctamente (Cubre línea 7)")
    void contextLoads() {
        // Al tener el perfil 'test', Spring ahora sí podrá levantar el contexto 
        // usando tu base de datos en memoria (H2) o tus configuraciones simuladas.
    }

    @Test
    @DisplayName("El método main se ejecuta correctamente (Cubre línea 10)")
    void mainTest() {
        // 2. Le inyectamos el perfil 'test' también a los argumentos del main
        InicioSesionServiceApplication.main(new String[] {
            "--spring.main.web-application-type=none",
            "--spring.profiles.active=test"
        });
    }
}