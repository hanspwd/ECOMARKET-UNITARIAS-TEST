# usuario-service

Microservicio REST para la gestión de usuarios del ecosistema **EcoMarket**. Permite registrar cuentas, modificar datos, asignar roles y permisos, y eliminar usuarios.

---

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Data JPA | (incluido en Boot) |
| Spring Security Crypto | (BCrypt) |
| MySQL | 8+ |
| Lombok | última compatible |
| Maven | 3.9+ |

---

## Requisitos previos

- **JDK 21** instalado y en el PATH.
- **MySQL 8+** corriendo en `localhost:3306`.
- **Maven 3.9+** (o usar el wrapper `./mvnw` si el proyecto lo incluye).

---

## Configuración

El archivo de configuración se encuentra en:

```
src/main/resources/application.properties
```

Valores por defecto:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/usuarios_db?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8081
```

> Ajusta `spring.datasource.username` y `spring.datasource.password` según tu instalación de MySQL. La base de datos `usuarios_db` se crea automáticamente si no existe.

---

## Levantar el servicio

```bash
# Clonar / descomprimir el proyecto
cd usuario-service-2

# Compilar y ejecutar
mvn spring-boot:run
```

El servicio queda disponible en: `http://localhost:8081`

Para compilar el JAR sin ejecutar:

```bash
mvn clean package -DskipTests
java -jar target/usuarios-service-0.0.1-SNAPSHOT.jar
```

---

## Endpoints disponibles

Base URL: `http://localhost:8081/api/usuarios`

### 1. Registrar usuario

**POST** `/api/usuarios/registro`

Body (JSON):
```json
{
  "nombre": "Juan Pérez",
  "correo": "juan@correo.com",
  "contrasenaInicial": "clave123",
  "telefono": "912345678",
  "rolId": 1,
  "estadoPerfilId": 1
}
```

> `rolId` y `estadoPerfilId` son opcionales. `contrasenaInicial` requiere mínimo 5 caracteres.

Respuesta exitosa — `201 Created`:
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "correo": "juan@correo.com",
  "telefono": "912345678",
  "fechaCreacion": "2025-01-01T10:00:00",
  "rol": { "id": 1, "nombre": "CLIENTE" },
  "estadoPerfil": { "id": 1, "nombre": "ACTIVO" },
  "permisos": []
}
```

---

### 2. Listar todos los usuarios

**GET** `/api/usuarios`

Respuesta — `200 OK`: array con todos los perfiles.

```bash
curl http://localhost:8081/api/usuarios
```

---

### 3. Buscar usuario por ID

**GET** `/api/usuarios/{id}`

```bash
curl http://localhost:8081/api/usuarios/1
```

---

### 4. Buscar usuario por correo

**GET** `/api/usuarios/correo/{correo}`

```bash
curl http://localhost:8081/api/usuarios/correo/juan@correo.com
```

---

### 5. Listar usuarios por rol

**GET** `/api/usuarios/rol/{rolId}`

```bash
curl http://localhost:8081/api/usuarios/rol/1
```

---

### 6. Modificar usuario

**PUT** `/api/usuarios/{id}`

Body (JSON):
```json
{
  "nombre": "Juan Actualizado",
  "correo": "nuevo@correo.com",
  "telefono": "999999999",
  "rolId": 2,
  "estadoPerfilId": 1
}
```

```bash
curl -X PUT http://localhost:8081/api/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Juan Actualizado","correo":"nuevo@correo.com","telefono":"999","rolId":2,"estadoPerfilId":1}'
```

---

### 7. Configurar permisos

**PUT** `/api/usuarios/{id}/permisos`

Body (JSON):
```json
{
  "permisoIds": [1, 2, 3]
}
```

```bash
curl -X PUT http://localhost:8081/api/usuarios/1/permisos \
  -H "Content-Type: application/json" \
  -d '{"permisoIds":[1,2]}'
```

Respuesta — `200 OK`: `true`

---

### 8. Eliminar usuario

**DELETE** `/api/usuarios/{id}`

```bash
curl -X DELETE http://localhost:8081/api/usuarios/1
```

Respuesta — `200 OK`: `true`

---

## Datos de prueba en MySQL

Antes de registrar usuarios, inserta al menos un **Rol**, un **EstadoPerfil** y un **Permiso** de referencia:

```sql
-- Roles
INSERT INTO rol (nombre, descripcion) VALUES ('ADMIN', 'Administrador del sistema');
INSERT INTO rol (nombre, descripcion) VALUES ('CLIENTE', 'Cliente registrado');
INSERT INTO rol (nombre, descripcion) VALUES ('VENDEDOR', 'Vendedor en la plataforma');

-- Estados de perfil
INSERT INTO estado_perfil (nombre) VALUES ('ACTIVO');
INSERT INTO estado_perfil (nombre) VALUES ('INACTIVO');
INSERT INTO estado_perfil (nombre) VALUES ('SUSPENDIDO');

-- Permisos
INSERT INTO permiso (nombre) VALUES ('VER_PRODUCTOS');
INSERT INTO permiso (nombre) VALUES ('CREAR_PEDIDO');
INSERT INTO permiso (nombre) VALUES ('GESTIONAR_USUARIOS');
```

> El nombre exacto de las tablas lo genera Hibernate según las entidades (`rol`, `estado_perfil`, `permiso`). Si usas `spring.jpa.hibernate.ddl-auto=update` las tablas se crean automáticamente al levantar el servicio.

---

## Pruebas rápidas con curl

```bash
BASE="http://localhost:8081/api/usuarios"

# 1. Crear usuario
curl -s -X POST $BASE/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana García","correo":"ana@test.com","contrasenaInicial":"pass1","rolId":1,"estadoPerfilId":1}' | jq .

# 2. Listar todos
curl -s $BASE | jq .

# 3. Buscar por ID
curl -s $BASE/1 | jq .

# 4. Buscar por correo
curl -s $BASE/correo/ana@test.com | jq .

# 5. Modificar
curl -s -X PUT $BASE/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Actualizada","correo":"ana@test.com","telefono":"999","rolId":2,"estadoPerfilId":1}' | jq .

# 6. Configurar permisos
curl -s -X PUT $BASE/1/permisos \
  -H "Content-Type: application/json" \
  -d '{"permisoIds":[1,2]}' | jq .

# 7. Eliminar
curl -s -X DELETE $BASE/1 | jq .
```

> `jq` es opcional; si no lo tienes instalado quita `| jq .` del final de cada comando.

---

## Ejecutar tests

```bash
mvn test
```

Los tests se encuentran en `src/test/java/com/horacio/ecomarket/usuarios/UsuariosServiceApplicationTests.java`.

---

## Estructura del proyecto

```
usuario-service-2/
├── src/
│   ├── main/java/com/horacio/ecomarket/usuarios/
│   │   ├── controller/     # UsuarioController (endpoints REST)
│   │   ├── service/        # RegistroUsuarioService + impl
│   │   ├── model/          # Usuario, PerfilUsuario, Rol, Permiso, EstadoPerfil
│   │   ├── dto/            # RegistroUsuarioDTO, ModificarUsuarioDTO, ConfigurarPermisosDTO
│   │   ├── repository/     # Repositorios JPA
│   │   ├── config/         # PasswordConfig (BCryptPasswordEncoder)
│   │   └── exception/      # GlobalExceptionHandler
│   └── resources/
│       └── application.properties
└── pom.xml
```

---

## Notas

- Las contraseñas se almacenan hasheadas con **BCrypt**.
- El correo electrónico es único por usuario; intentar registrar uno duplicado retorna error `500` con el mensaje `"El correo ya está registrado"`.
- No hay autenticación activa en este microservicio; está pensado para ser consumido internamente por un API Gateway o servicio de autenticación.