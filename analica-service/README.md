# EcoMarket - Microservicio de Analítica (analiticaservice)

Microservicio de analítica, reportes y monitoreo del proyecto EcoMarket. Genera reportes consultando datos de todos los microservicios del ecosistema, gestiona alertas del sistema, respaldos de base de datos y métricas para dashboard.

## Características Principales

- **Reportes Integrados:** Generación de reportes consultando datos de usuarios, pedidos, inventario, pagos, carrito, soporte y envíos.
- **Reporte Completo:** Endpoint que consolida datos de todos los microservicios en un solo reporte.
- **Tolerancia a Fallos:** Si un microservicio externo no está disponible, el reporte se genera igual con los datos disponibles, registrando en log el servicio caído.
- **Alertas del Sistema:** Gestión de alertas por módulo con niveles (info, warning, error) y estado de resolución.
- **Respaldos de Base de Datos:** Registro de respaldos con tamaño, ruta de almacenamiento y estado.
- **Métricas de Dashboard:** Almacenamiento de métricas clave-valor para paneles de monitoreo en tiempo real.
- **Integración con Todos los Microservicios:** Se comunica vía `RestTemplate` con usuarios, pedidos, inventario, pagos, carrito, soporte y envíos.

## Tecnologías Utilizadas

- **Lenguaje:** Java 25
- **Framework:** Spring Boot 4.0.6
- **Gestor de Dependencias:** Maven
- **Comunicación Inter-servicios:** `RestTemplate` con timeouts y tolerancia a fallos
- **Base de datos:** MySQL 8.0
- **Mock API:** json-server (simula todos los microservicios del ecosistema)

## Estructura del Proyecto

```
analiticaservice/
  src/
    main/
      java/com/ecomarket/analiticaservice/
        config/RestTemplateConfig.java       - Configuracion RestTemplate con timeouts
        controller/
          ReporteController.java             - Controlador de reportes (8 endpoints)
          AlertaController.java              - CRUD alertas del sistema
          RespaldoController.java            - CRUD respaldos de base de datos
          MetricaController.java             - CRUD metricas de dashboard
        dto/
          ClienteDTO.java, PedidoDTO.java, ProductoDTO.java
          InventarioStockDTO.java, PagoDTO.java, CarritoDTO.java
          TicketSoporteDTO.java, EnvioDTO.java
          ReporteRequestDTO.java, ReporteFechaRequestDTO.java
          ReporteResultadoDTO.java, AlertaRequestDTO.java
          RespaldoRequestDTO.java, MetricaRequestDTO.java
        exception/
          GlobalExceptionHandler.java        - Manejador global de excepciones
          NoExisteEnBdException.java         - Recurso no encontrado
        model/
          entity/
            Reporte.java, AlertaSistema.java
            RespaldoBaseDatos.java, MetricaDashboard.java
          reference/
            TipoReporte.java, EstadoReporte.java
            NivelAlerta.java, EstadoRespaldo.java
        repository/                          - Interfaces JPA
        service/
          AnaliticaService.java              - Orquestador principal
          ReporteDomainService.java          - Servicio de dominio de reportes
          AlertaDomainService.java           - Servicio de dominio de alertas
          RespaldoDomainService.java         - Servicio de dominio de respaldos
          MetricaDomainService.java          - Servicio de dominio de metricas
  mock-data/db.json                          - Datos mock para todos los microservicios
```

## API Endpoints

### Reportes - `/api/v1/reportes`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/reportes` | Lista todos los reportes generados |
| GET | `/api/v1/reportes/{id}` | Obtiene un reporte por ID |
| GET | `/api/v1/reportes/solicitante/{id}` | Lista reportes por ID de solicitante |
| GET | `/api/v1/reportes/rango?inicio=&fin=` | Lista reportes por rango de fechas |
| POST | `/api/v1/reportes` | Genera un reporte genérico |
| POST | `/api/v1/reportes/rango` | Genera un reporte por rango de fechas |
| POST | `/api/v1/reportes/usuarios/{id}` | Reporte de usuarios/clientes |
| POST | `/api/v1/reportes/pedidos/{id}?fechaInicio=&fechaFin=` | Reporte de pedidos |
| POST | `/api/v1/reportes/inventario/{id}` | Reporte de inventario y productos |
| POST | `/api/v1/reportes/pagos/{id}` | Reporte de pagos |
| POST | `/api/v1/reportes/carrito/{id}` | Reporte de carrito de compras |
| POST | `/api/v1/reportes/soporte/{id}` | Reporte de tickets de soporte |
| POST | `/api/v1/reportes/envios/{id}` | Reporte de envíos |
| POST | `/api/v1/reportes/completo/{id}` | Reporte consolidado de todos los servicios |

### Alertas - `/api/v1/alertas`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/alertas` | Lista todas las alertas |
| GET | `/api/v1/alertas/{id}` | Obtiene una alerta por ID |
| GET | `/api/v1/alertas/estado?resuelta=` | Filtra alertas por estado |
| POST | `/api/v1/alertas` | Crea una nueva alerta |
| PATCH | `/api/v1/alertas/{id}/resolver` | Marca una alerta como resuelta |

### Respaldos - `/api/v1/respaldos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/respaldos` | Lista todos los respaldos |
| GET | `/api/v1/respaldos/{id}` | Obtiene un respaldo por ID |
| POST | `/api/v1/respaldos` | Ejecuta un nuevo respaldo |

### Métricas - `/api/v1/metricas`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/metricas` | Lista todas las métricas |
| GET | `/api/v1/metricas/{id}` | Obtiene una métrica por ID |
| GET | `/api/v1/metricas/clave/{clave}` | Busca métrica por clave |
| POST | `/api/v1/metricas` | Crea una nueva métrica |
| PUT | `/api/v1/metricas/{id}` | Actualiza una métrica existente |

---

## Despliegue con Docker Compose (Recomendado)

### Prerrequisitos
- Docker y Docker Compose instalados.
- Puertos `8084`, `8082` y `3308` disponibles.

### Pasos

1. **Construir e iniciar todos los servicios:**
   ```bash
   docker-compose up -d
   ```

   Esto levanta 3 contenedores:
   - `api_analitica_java` - Microservicio Spring Boot en puerto `8084`
   - `db_analitica` - MySQL 8.0 en puerto `3308`
   - `mock_analitica_api` - json-server con datos mock en puerto `8082`

2. **Verificar que los servicios estén funcionando:**
   ```bash
   docker-compose ps
   ```

3. **Probar la API:**
   ```bash
   curl http://localhost:8084/api/v1/reportes
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
| analitica-api | 8084 | API Spring Boot del microservicio |
| analitica-db | 3308 | Base de datos MySQL |
| mock-server | 8082 | Mock de todos los microservicios del ecosistema |

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
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS analitica_db;"
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

4. **Compilar y ejecutar el microservicio:**

   ```bash
   mvn clean package -DskipTests
   java -jar target/analiticaservice-0.0.1-SNAPSHOT.jar
   ```

   O usando Maven directamente:
   ```bash
   mvn spring-boot:run
   ```

5. **Verificar que funciona:**
   ```bash
   curl http://localhost:8084/api/v1/reportes
   ```

---

## Configuración de Base de Datos

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | URL de conexión JDBC | `jdbc:mysql://localhost:3306/analitica_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de BD | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Password de BD | `root_pass` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia DDL | `update` |

---

## Mock de Datos de Prueba

El archivo `mock-data/db.json` contiene datos simulados para todos los microservicios del ecosistema:

| Endpoint | Registros | Descripción |
|----------|-----------|-------------|
| `/clientes` | 3 | Usuarios/clientes de la plataforma |
| `/pedidos` | 4 | Pedidos realizados |
| `/productos` | 4 | Productos del catálogo |
| `/inventario` | 4 | Stock por sucursal |
| `/pagos` | 4 | Pagos asociados a pedidos |
| `/carrito` | 4 | Items en carritos de compra |
| `/soporte` | 3 | Tickets de soporte |
| `/envios` | 3 | Envíos registrados |

Esto permite probar **todas las funcionalidades** del microservicio sin necesidad de levantar ninguno de los otros microservicios reales. Cada llamada externa tiene manejo de errores con timeout de 10 segundos: si un servicio no responde, el reporte se genera igual con los datos disponibles y se registra una advertencia en los logs.

---

## Notas Importantes

- Los IDs de tipo de reporte son: 1=Usuarios, 2=Pedidos, 3=Inventario, 4=Pagos, 5=Carrito, 6=Soporte, 7=Envíos, 8=Completo
- Los IDs de estado de reporte son: 1=Pendiente, 2=Completado, 3=Fallido
- Los IDs de nivel de alerta son: 1=Info, 2=Warning, 3=Error
- Los IDs de estado de respaldo son: 1=En Progreso, 2=Completado, 3=Fallido
- El endpoint `/completo` consulta todos los microservicios simultáneamente; los que no respondan se omiten y se registran en log
- Las métricas del dashboard usan clave única para evitar duplicados

---

## Pruebas

```bash
mvn test
```
