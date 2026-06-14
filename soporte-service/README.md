# EcoMarket - Microservicio de Soporte (soporteservice)

Este repositorio contiene el microservicio de Soporte (`soporteservice`) del proyecto fullstack **EcoMarket**. Centraliza y gestiona la interacción de ayuda, atención al cliente y feedback de la plataforma.

## Características Principales

- **Gestión de Tickets:** Creación, categorización (`CategoriaTicket`) y seguimiento del ciclo de vida de solicitudes de soporte (`EstadoTicket`).
- **Chat de Soporte:** Administración del historial de mensajes de soporte vinculados a un ticket (`MensajeChat`).
- **Sistema de Reseñas:** Gestión de valoraciones y feedback de usuarios sobre productos (`Resena`).
- **Notificaciones:** Generación de alertas al usuario a través de distintos canales (`Notificacion`, `CanalNotificacion`).
- **Integración con Clientes:** Comunicación síncrona con el microservicio de usuarios/clientes vía `RestTemplate`.

## Tecnologías Utilizadas

- **Lenguaje:** Java 25
- **Framework:** Spring Boot 4.0.6
- **Gestor de Dependencias:** Maven
- **Comunicación Inter-servicios:** `RestTemplate`
- **Base de datos:** MySQL 8.0
- **Mock API:** json-server (para simular clientes y pedidos)

## Estructura del Proyecto

```
soporteservice/
  src/
    main/
      java/com/ecomarket/soporteservice/
        config/RestTemplateConfig.java     - Configuracion RestTemplate
        controller/
          SoporteController.java           - Controlador principal (operaciones de negocio)
          EstadoTicketController.java      - CRUD estados de ticket
          CategoriaTicketController.java   - CRUD categorias de ticket
          CanalNotificacionController.java - CRUD canales de notificacion
          MensajeChatController.java       - CRUD mensajes de chat
          ResenaController.java            - CRUD resenas
          NotificacionController.java      - CRUD notificaciones
        dto/
          ClienteDTO.java, PedidoDTO.java, RolDTO.java, EstadoPedidoDTO.java
          SoporteTicketRequestDTO.java, NotificacionRequestDTO.java
          MensajeChatRequestDTO.java, ResenaRequestDTO.java
          ErrorResponseDTO.java
        exception/
          GlobalExceptionHandler.java      - Manejador global de excepciones
          NoExisteEnBdException.java       - Recurso no encontrado
          YaExisteEnBdException.java       - Conflicto por duplicado
          PedidoClienteIncompatibleException.java - Pedido no pertenece al cliente
        model/
          entity/
            TicketSoporte.java             - Entidad de ticket de soporte
            MensajeChat.java               - Entidad de mensaje de chat
            Notificacion.java              - Entidad de notificacion
            Resena.java                    - Entidad de resena de producto
          reference/
            EstadoTicket.java              - Catalogo de estados
            CategoriaTicket.java           - Catalogo de categorias
            CanalNotificacion.java         - Catalogo de canales
        repository/                        - Interfaces JPA
        service/                           - Logica de negocio
  mock-data/db.json                        - Datos mock para json-server
```

## API Endpoints

### Servicio Central (SoporteController) - `/api/v1/soporte`

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/v1/soporte/enviar-notificacion-push` | Envia una notificacion push |
| POST | `/api/v1/soporte/ingresar-ticket` | Crea un nuevo ticket de soporte |
| GET | `/api/v1/soporte/tickets` | Lista tickets (filtro por `?clienteId=` o `?estadoId=`) |
| GET | `/api/v1/soporte/tickets/{id}` | Obtiene un ticket por ID |
| PATCH | `/api/v1/soporte/tickets/{id}/estado/{nuevoEstadoId}` | Actualiza el estado de un ticket |
| PATCH | `/api/v1/soporte/tickets/{id}/asignar/{empleadoId}` | Asigna un empleado a un ticket |
| DELETE | `/api/v1/soporte/tickets/{id}` | Elimina un ticket |
| GET | `/api/v1/soporte/tickets/{id}/mensajes` | Obtiene los mensajes de un ticket |
| POST | `/api/v1/soporte/mensajes-chat` | Envia un mensaje de chat |
| GET | `/api/v1/soporte/notificaciones` | Lista notificaciones (filtro por `?destinatarioId=`) |
| GET | `/api/v1/soporte/notificaciones/{id}` | Obtiene una notificacion por ID |
| DELETE | `/api/v1/soporte/notificaciones/{id}` | Elimina una notificacion |
| POST | `/api/v1/soporte/resenas` | Crea una resena |
| GET | `/api/v1/soporte/resenas` | Lista resenas (filtro por `?productoId=` o `?clienteId=`) |
| PATCH | `/api/v1/soporte/resenas/{id}/aprobar` | Aprueba moderacion de resena |
| PATCH | `/api/v1/soporte/resenas/{id}/rechazar` | Rechaza moderacion de resena |
| DELETE | `/api/v1/soporte/resenas/{id}` | Elimina una resena |

### Catalogos

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET/POST/DELETE | `/api/v1/estado-ticket` | CRUD estados de ticket |
| GET/POST/DELETE | `/api/v1/categoria-ticket` | CRUD categorias de ticket |
| GET/POST/DELETE | `/api/v1/canal-notificacion` | CRUD canales de notificacion |
| GET/POST/DELETE | `/api/v1/mensajes-chat` | CRUD mensajes de chat |
| GET/POST/DELETE | `/api/v1/resenas` | CRUD resenas |
| GET/DELETE | `/api/v1/notificaciones` | CRUD notificaciones |

---

## Despliegue con Docker Compose (Recomendado)

### Prerrequisitos
- Docker y Docker Compose instalados.
- Puerto `8081`, `8082` y `3306` disponibles.

### Pasos

1. **Clonar el repositorio:**
   ```bash
   git clone <url-del-repositorio>
   cd soporteservice
   ```

2. **Construir e iniciar todos los servicios:**
   ```bash
   docker-compose up -d
   ```

   Esto levantara 3 contenedores:
   - `api_soporte_java` - Microservicio Spring Boot en puerto `8081`
   - `db_soporte` - MySQL 8.0 en puerto `3306`
   - `mock_usuarios_api` - json-server con datos mock en puerto `8082`

3. **Verificar que los servicios esten funcionando:**
   ```bash
   docker-compose ps
   ```

4. **Probar la API:**
   ```bash
   curl http://localhost:8081/api/v1/soporte/tickets
   ```

5. **Detener los servicios:**
   ```bash
   docker-compose down
   ```

   Para eliminar tambien los volumenes de la base de datos:
   ```bash
   docker-compose down -v
   ```

### Servicios en Docker Compose

| Servicio | Puerto | Descripcion |
|----------|--------|-------------|
| soporte-api | 8081 | API Spring Boot del microservicio |
| soporte-db | 3306 | Base de datos MySQL |
| mock-server | 8082 | Mock de APIs de clientes y pedidos |

---

## Despliegue con XAMPP (Entorno Local)

### Prerrequisitos
- JDK 25 instalado (`java -version`)
- Maven instalado (`mvn -version`)
- XAMPP con MySQL activado (Apache no es necesario)

### Pasos

1. **Iniciar MySQL desde XAMPP:**
   - Abre el Panel de Control de XAMPP
   - Click en "Start" junto a MySQL

2. **Crear la base de datos:**
   - Abre phpMyAdmin (`http://localhost/phpmyadmin`)
   - Crea una base de datos llamada `soporte_db`
   - O desde la terminal:
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS soporte_db;"
   ```

   > **Nota:** Si tienes contraseña en MySQL, actualizala en `src/main/resources/application.properties`:
   > `spring.datasource.password=root_pass`

3. **Iniciar el mock de clientes/pedidos con json-server:**

   Instala json-server globalmente:
   ```bash
   npm install -g json-server
   ```

   Inicia el mock (desde la raiz del proyecto):
   ```bash
   json-server --watch mock-data/db.json --port 8082
   ```

   > **Importante:** Como los servicios no estan en Docker, las URLs en `TicketSoporteService.java` y `NotificacionService.java` apuntan a `http://mock-server:8082`. Para entorno local sin Docker, cambia esas URLs a `http://localhost:8082` en ambos archivos.

4. **Compilar y ejecutar el microservicio:**

   ```bash
   mvn clean package -DskipTests
   java -jar target/soporteservice-0.0.1-SNAPSHOT.jar
   ```

   O usando Maven directamente:
   ```bash
   mvn spring-boot:run
   ```

5. **Verificar que funciona:**
   ```bash
   curl http://localhost:8081/api/v1/soporte/tickets
   ```

---

## Configuracion de Base de Datos

Las propiedades de conexion se configuran via variables de entorno (valores por defecto entre parentesis):

| Variable | Descripcion | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | URL de conexion JDBC | `jdbc:mysql://localhost:3306/soporte_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de BD | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Password de BD | `root_pass` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia DDL | `update` |

---

## Mock de Clientes y Pedidos

El json-server (`mock-data/db.json`) proporciona datos de prueba para:

- **`/clientes`**: 50 clientes simulados con estructura `ClienteDTO`
- **`/pedidos`**: 50 pedidos simulados con estructura `PedidoDTO`

El mock se inicia automaticamente con Docker Compose o manualmente con `json-server`.

---

## Pruebas

Para ejecutar las pruebas unitarias:
```bash
mvn test
```

---

## Notas Importantes

- Los IDs de estado de ticket predefinidos son: 1=Abierto, 2=En Proceso, 3=En Espera, 4=Resuelto, 5=Cerrado
- Al crear un ticket, se valida que el cliente y pedido existan en el mock, y que el pedido pertenezca al cliente
- Las notificaciones se marcan como no enviadas si el destinatario no existe en el mock
- Las reseñas se crean con moderacion pendiente (`moderacionAprobado=false`) por defecto
