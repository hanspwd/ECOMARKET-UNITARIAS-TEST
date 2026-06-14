# 🛒 EcoMarket — Carrito de Compra Service

Microservicio encargado de gestionar el carrito de compras, el proceso de checkout y la generación de pedidos dentro del ecosistema **EcoMarket SPA**. Se comunica con otros microservicios mediante HTTP (RestTemplate) para validar stock, procesar pagos y coordinar envíos.

---

## 📋 Descripción general

Este servicio actúa como **orquestador del flujo de compra**. Cuando un cliente agrega productos, el carrito consulta en tiempo real al servicio de catálogo/inventario para verificar precio y disponibilidad. Al finalizar la compra, coordina la reserva de stock, la generación del pedido, el inicio del pago y la creación del envío.

### Flujo principal

```
Cliente agrega producto
        │
        ▼
CatalogoInventarioClient → verifica precio y stock
        │
        ▼
Carrito guarda item con precio real
        │
        ▼
/checkout → reserva stock → cierra carrito
        │
        ▼
/compra/finalizar → genera pedido → inicia pago → crea envío
```

---

## 🛠️ Stack tecnológico

| Componente        | Versión       |
|-------------------|---------------|
| Java              | 21            |
| Spring Boot       | 4.0.6         |
| Spring Data JPA   | (incluido)    |
| Spring Validation | (incluido)    |
| MySQL Connector   | (runtime)     |
| Lombok            | (opcional)    |
| Maven             | Wrapper incluido |

---

## ⚙️ Configuración y requisitos previos

### 1. Base de datos

El servicio requiere una base de datos MySQL en ejecución. Por defecto usa los siguientes valores (configurados en `application.properties`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecomarket_carrito
spring.datasource.username=root
spring.datasource.password=
```

> Si usas XAMPP, asegúrate de que el módulo MySQL esté activo. La base de datos `ecomarket_carrito` debe existir antes de levantar el servicio. JPA creará las tablas automáticamente (`ddl-auto=update`).

Crear la base de datos manualmente:
```sql
CREATE DATABASE ecomarket_carrito;
```

### 2. Microservicios dependientes

Este servicio se comunica con otros tres microservicios. Deben estar activos para el flujo completo de compra (aunque el servicio levanta sin ellos):

| Servicio                    | Puerto por defecto | Variables de config                                   |
|-----------------------------|--------------------|-------------------------------------------------------|
| catalogo-inventario-service | `8087`             | `microservicio.catalogo.url` / `microservicio.inventario.url` |
| logistica-envio-service     | `8083`             | `microservicio.envios.url`                            |
| proceso-pago-service        | `8088`             | `microservicio.pagos.url`                             |

> Si algún servicio externo no está disponible, la compra continúa y se registra una advertencia en el log (comportamiento tolerante a fallos en pago y envío).

---

## 🚀 Cómo levantar el servicio

### Opción A — Maven Wrapper (recomendado)

```bash
# En la raíz del proyecto
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

### Opción B — Compilar y ejecutar el JAR

```bash
./mvnw clean package -DskipTests
java -jar target/carritocompraservice-0.0.1-SNAPSHOT.jar
```

El servicio quedará disponible en: **`http://localhost:8082`**

---

## 📡 Endpoints disponibles

### Carrito — `/api/carrito`

| Método   | Ruta                              | Descripción                                                  |
|----------|-----------------------------------|--------------------------------------------------------------|
| `GET`    | `/api/carrito`                    | Lista todos los carritos                                    |
| `GET`    | `/api/carrito/{clienteId}`        | Obtiene el carrito activo del cliente (lo crea si no existe) |
| `POST`   | `/api/carrito`                    | Agrega un producto al carrito                               |
| `DELETE` | `/api/carrito/{clienteId}/item/{itemId}` | Elimina un ítem específico del carrito              |
| `PUT`    | `/api/carrito/{clienteId}/envio?tipoEnvioId={id}` | Selecciona el tipo de envío             |
| `DELETE` | `/api/carrito/{clienteId}/vaciar` | Vacía el carrito (libera stock reservado)                   |
| `POST`   | `/api/carrito/{clienteId}/checkout` | Inicia la compra: reserva stock y cierra el carrito       |

#### Body para `POST /api/carrito`
```json
{
  "clienteId": 1,
  "productoId": 5,
  "cantidad": 2
}
```

---

### Compra — `/api/compra`

| Método | Ruta                    | Descripción                                           |
|--------|-------------------------|-------------------------------------------------------|
| `POST` | `/api/compra/finalizar` | Orquesta el flujo completo: pedido + pago + envío    |

#### Body para `POST /api/compra/finalizar`
```json
{
  "clienteId": 1,
  "metodoPagoId": 2,
  "metodoEnvioId": 1,
  "direccionId": 3
}
```

#### Respuesta `CompraResultDTO`
```json
{
  "estado": "COMPLETADO",
  "carritoId": 10,
  "pedido": { ... },
  "transaccionPagoId": 42,
  "envioId": 7
}
```

---

### Pedido — `/api/pedido`

| Método | Ruta                                        | Descripción                              |
|--------|---------------------------------------------|------------------------------------------|
| `POST` | `/api/pedido/generar?clienteId=&carritoId=` | Genera pedido desde un carrito cerrado   |
| `GET`  | `/api/pedido`                               | Lista todos los pedidos                  |
| `GET`  | `/api/pedido/{pedidoId}`                    | Busca un pedido por ID                   |
| `GET`  | `/api/pedido/historial/{clienteId}`         | Historial de pedidos de un cliente       |
| `PUT`  | `/api/pedido/{pedidoId}/estado?nuevoEstadoId=` | Actualiza el estado del pedido        |

---

### Mock Transportista — `/api/mock/transportistas`

Endpoint de mock para pruebas de integración con el servicio de logística.

| Método | Ruta                             | Descripción                        |
|--------|----------------------------------|------------------------------------|
| `GET`  | `/api/mock/transportistas/{id}`  | Retorna datos ficticios de un transportista |

---

## 🧪 Pruebas rápidas con curl

```bash
# Ver todos los carritos
curl http://localhost:8082/api/carrito

# Obtener/crear carrito del cliente 1
curl http://localhost:8082/api/carrito/1

# Agregar producto al carrito
curl -X POST http://localhost:8082/api/carrito \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1,"productoId":5,"cantidad":2}'

# Seleccionar tipo de envío
curl -X PUT "http://localhost:8082/api/carrito/1/envio?tipoEnvioId=1"

# Vaciar carrito
curl -X DELETE http://localhost:8082/api/carrito/1/vaciar

# Checkout (reserva stock y cierra carrito)
curl -X POST http://localhost:8082/api/carrito/1/checkout

# Finalizar compra completa
curl -X POST http://localhost:8082/api/compra/finalizar \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1,"metodoPagoId":2,"metodoEnvioId":1,"direccionId":3}'

# Historial de pedidos del cliente 1
curl http://localhost:8082/api/pedido/historial/1
```

---

## 🗂️ Estructura del proyecto

```
src/main/java/com/ecomarket/carritocompraservice/
├── client/
│   ├── CatalogoInventarioClient.java   # Consulta precio y stock
│   ├── LogisticaEnvioClient.java       # Crea envíos
│   └── ProcesoPagoClient.java          # Inicia transacciones de pago
├── config/
│   └── RestTemplateConfig.java         # Bean RestTemplate
├── controller/
│   ├── CarritoController.java          # CRUD carrito
│   ├── CompraController.java           # Orquestación de compra
│   ├── MockTransportistaController.java
│   └── PedidoController.java           # Gestión de pedidos
├── dto/
│   ├── AnadirProductoRequestDTO.java
│   ├── ClienteInventarioDTO.java
│   ├── CompraRequestDTO.java
│   ├── CompraResultDTO.java
│   ├── MetodoPagoDTO.java
│   └── ProductoClienteDTO.java
├── model/
│   ├── Carrito.java
│   ├── EstadoPedido.java
│   ├── ItemCarrito.java
│   ├── ItemPedido.java
│   └── Pedido.java
├── repository/
│   ├── CarritoRepository.java
│   ├── EstadoPedidoRepository.java
│   ├── ItemCarritoRepository.java
│   ├── ItemPedidoRepository.java
│   └── PedidoRepository.java
└── service/
    ├── CarritoService.java              # Lógica del carrito
    ├── CompraOrchestratorService.java   # Orquestador del flujo de compra
    └── PedidoService.java              # Lógica de pedidos
```

---

## 🔗 Comunicación con otros microservicios

### CatalogoInventarioClient (`localhost:8087`)

| Acción              | Endpoint llamado                                          |
|---------------------|-----------------------------------------------------------|
| Obtener producto    | `GET /api/catalogo/{productoId}`                          |
| Verificar stock     | `GET /api/inventario/disponibilidad/{productoId}?cantidad=` |
| Reservar stock      | `POST /api/inventario/reservar/{productoId}?cantidad=`    |
| Liberar stock       | `POST /api/inventario/liberar/{productoId}?cantidad=`     |

### ProcesoPagoClient (`localhost:8088`)
| Acción         | Endpoint llamado                                                      |
|----------------|-----------------------------------------------------------------------|
| Iniciar pago   | `POST /api/pagos/iniciar?pedidoId=&clienteId=&monto=` + body MetodoPagoDTO |

### LogisticaEnvioClient (`localhost:8083`)
| Acción       | Endpoint llamado                      |
|--------------|---------------------------------------|
| Crear envío  | `POST /api/v1/logistica-envios/envios` |

---

## 📝 Notas para el equipo

- El precio del producto **no se envía desde el frontend**; se consulta directamente al catálogo al momento de agregar al carrito.
- Si `pagoservice` o `envioservice` no están disponibles, el flujo de compra **no se interrumpe** — se registra un `WARN` en el log y la compra queda en estado `COMPLETADO` igualmente.
- Las tablas se crean automáticamente al levantar el servicio (`ddl-auto=update`). No es necesario ejecutar scripts SQL para el schema.
- El `MockTransportistaController` es solo para pruebas de integración con logística; no contiene lógica real.