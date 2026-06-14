# EcoMarket - Microservicio de Envíos (envioservice)

Microservicio de logística y envíos del proyecto EcoMarket. Gestiona el ciclo de vida completo de los envíos, incluyendo rutas de transporte, puntos de retiro, direcciones y seguimiento de historial.

## Características Principales

- **Gestión de Envíos:** Creación, cancelación y recepción de envíos con diferentes métodos y estados.
- **Rutas de Transporte:** Planificación de rutas asignando transportistas a envíos.
- **Puntos de Retiro:** Administración de puntos de retiro disponibles y selección por parte del cliente.
- **Direcciones:** CRUD de direcciones de despacho asociadas a clientes.
- **Historial de Envíos:** Registro de cada cambio de estado con observaciones.
- **Integración con Clientes, Pedidos y Transportistas:** Comunicación vía `RestTemplate` con mock-server para validación de datos.

## Tecnologías Utilizadas

- **Lenguaje:** Java 25
- **Framework:** Spring Boot 4.0.6
- **Gestor de Dependencias:** Maven
- **Comunicación Inter-servicios:** `RestTemplate`
- **Base de datos:** MySQL 8.0
- **Mock API:** json-server (para simular clientes, pedidos y transportistas)

## Estructura del Proyecto

```
envioservice/
  src/
    main/
      java/com/ecomarket/envioservice/
        config/RestTemplateConfig.java      - Configuracion RestTemplate con timeouts
        controller/
          EnvioController.java              - Controlador principal de envios
          DireccionController.java          - CRUD direcciones
          EstadoEnvioController.java        - CRUD estados de envio
          MetodoEnvioController.java        - CRUD metodos de envio
          PuntoRetiroController.java        - CRUD puntos de retiro
        dto/
          ClienteDTO.java, PedidoDTO.java, TransportistaDTO.java
          CrearEnvioRequestDTO.java, ActualizarEstadoRequestDTO.java
          PlanificarRutaRequestDTO.java, SeleccionarPuntoRetiroRequestDTO.java
          RegistrarRecepcionRequestDTO.java, ErrorResponseDTO.java
        exception/
          GlobalExceptionHandler.java       - Manejador global de excepciones
          NoExisteEnBdException.java        - Recurso no encontrado
          YaExisteEnBdException.java        - Conflicto por duplicado
          EnvioEstadoInvalidoException.java - Transicion de estado no permitida
          PedidoClienteIncompatibleException.java
        model/
          entity/
            Envio.java, HistorialEnvio.java, RutaTransporte.java
            Direccion.java, PuntoRetiro.java
          reference/
            EstadoEnvio.java, MetodoEnvio.java
        repository/                         - Interfaces JPA
        service/                            - Logica de negocio
  mock-data/db.json                         - Datos mock para json-server
```

## API Endpoints

### Servicio Central (EnvioController) - `/api/v1/envios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/envios` | Crea un nuevo envío (valida cliente, pedido y dirección) |
| GET | `/api/v1/envios` | Lista envíos (filtro por `?clienteId=` o `?estadoId=`) |
| GET | `/api/v1/envios/{id}` | Obtiene un envío por ID |
| GET | `/api/v1/envios/{id}/estado` | Consulta el estado actual de un envío |
| PATCH | `/api/v1/envios/{id}/estado` | Actualiza el estado de un envío |
| PATCH | `/api/v1/envios/{id}/cancelar` | Cancela un envío |
| POST | `/api/v1/envios/{id}/recepcion` | Registra la recepción del envío |
| POST | `/api/v1/envios/{id}/punto-retiro` | Selecciona un punto de retiro |
| POST | `/api/v1/envios/{id}/ruta` | Planifica una ruta de transporte |
| GET | `/api/v1/envios/{id}/historial` | Obtiene el historial de cambios del envío |

### Catalogos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET/POST/DELETE | `/api/v1/estados-envio` | CRUD estados de envío |
| GET/POST/DELETE | `/api/v1/metodos-envio` | CRUD métodos de envío |
| GET/POST/DELETE | `/api/v1/direcciones` | CRUD direcciones |
| GET/POST/DELETE | `/api/v1/puntos-retiro` | CRUD puntos de retiro |

---

## Despliegue con Docker Compose (Recomendado)

### Prerrequisitos
- Docker y Docker Compose instalados.
- Puertos `8083`, `8082` y `3307` disponibles.

### Pasos

1. **Construir e iniciar todos los servicios:**
   ```bash
   docker-compose up -d
   ```

   Esto levanta 3 contenedores:
   - `api_envio_java` - Microservicio Spring Boot en puerto `8083`
   - `db_envio` - MySQL 8.0 en puerto `3307`
   - `mock_envio_api` - json-server con datos mock en puerto `8082`

2. **Verificar que los servicios estén funcionando:**
   ```bash
   docker-compose ps
   ```

3. **Probar la API:**
   ```bash
   curl http://localhost:8083/api/v1/envios
   ```

4. **Detener los servicios:**
   ```bash
   docker-compose down
   ```

   Para eliminar también los volúmenes de la base de datos:
   ```bash
   docker-compose down -v
   ```

### Servicios en Docker Compose

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| envio-api | 8083 | API Spring Boot del microservicio |
| envio-db | 3307 | Base de datos MySQL |
| mock-server | 8082 | Mock de clientes, pedidos y transportistas |

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
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS envio_db;"
   ```

3. **Iniciar el mock de datos con json-server:**

   Instala json-server globalmente:
   ```bash
   npm install -g json-server
   ```

   Inicia el mock (desde la raíz del proyecto):
   ```bash
   json-server --watch mock-data/db.json --port 8082
   ```

   > **Importante:** Las URLs en `EnvioService.java` apuntan a `http://mock-server:8082`. Para entorno local sin Docker, cámbialas a `http://localhost:8082`.

4. **Compilar y ejecutar el microservicio:**

   ```bash
   mvn clean package -DskipTests
   java -jar target/envioservice-0.0.1-SNAPSHOT.jar
   ```

   O usando Maven directamente:
   ```bash
   mvn spring-boot:run
   ```

5. **Verificar que funciona:**
   ```bash
   curl http://localhost:8083/api/v1/envios
   ```

---

## Configuración de Base de Datos

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | URL de conexión JDBC | `jdbc:mysql://localhost:3306/envio_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de BD | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Password de BD | `root_pass` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia DDL | `update` |

---

## Mock de Datos de Prueba

El archivo `mock-data/db.json` contiene datos simulados para que el microservicio funcione de forma autónoma sin depender de otros servicios reales:

- **`/clientes`**: 10 clientes simulados (ids 1-10)
- **`/pedidos`**: 10 pedidos simulados vinculados a clientes
- **`/transportistas`**: 3 transportistas disponibles

Si los microservicios reales de usuarios, pedidos o transportistas no están disponibles, el mock-server permite probar todas las funcionalidades del microservicio sin necesidad de levantarlos. Cada llamada externa incluye manejo de errores con timeout de 10 segundos, registrando en logs si algún servicio mock no responde.

---

## Notas Importantes

- Los IDs de estado de envío predefinidos son: 1=Pendiente, 2=En Tránsito, 3=En Punto de Retiro, 4=Entregado, 5=Cancelado
- El costo de envío se calcula automáticamente: método "PuntoRetiro" es gratis ($0), el resto $5,000
- La fecha estimada de entrega varía según el método de envío (2 días para punto de retiro, 5 días para domicilio)
- Un envío en estado final (Entregado o Cancelado) no puede modificarse
- Si el servicio mock no está disponible, se muestra un mensaje claro indicando que no se pudo validar el dato correspondiente

---

## Pruebas

```bash
mvn test
```
