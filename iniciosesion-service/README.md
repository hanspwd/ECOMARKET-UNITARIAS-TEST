# iniciosesion-service

Microservicio REST de autenticación para **EcoMarket**. Gestiona credenciales de acceso, emisión y validación de tokens JWT, cierre de sesión con blacklist, recuperación y restablecimiento de contraseña, y bloqueo de cuentas.

---

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| JJWT | 0.12.6 |
| Spring Security Crypto | (BCrypt) |
| MySQL | 8+ |
| Lombok | última compatible |
| Maven | 3.9+ |

---

## Requisitos previos

- **JDK 21** instalado y en el PATH.
- **MySQL 8+** corriendo en `localhost:3306`.
- **Maven 3.9+**.

---

## Configuración

Archivo: `src/main/resources/application.properties`

```properties
server.port=8082

spring.datasource.url=jdbc:mysql://localhost:3306/iniciosesion_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=12345678901234
jwt.expiration-ms=86400000
```

> Ajusta `spring.datasource.password` según tu instalación. La base de datos `iniciosesion_db` se crea automáticamente.  
> `jwt.expiration-ms=86400000` equivale a **24 horas**.  
> **Para producción** cambia `jwt.secret` por una cadena larga y aleatoria (mínimo 32 caracteres).

---

## Levantar el servicio

```bash
cd iniciosesion-service
mvn spring-boot:run
```

El servicio queda disponible en: `http://localhost:8082`

Para compilar el JAR:

```bash
mvn clean package -DskipTests
java -jar target/iniciosesion-service-0.0.1-SNAPSHOT.jar
```

---

## Flujo de uso típico

```
1. Crear credencial  →  POST /api/sesion/credencial
2. Iniciar sesión    →  POST /api/sesion/login           (devuelve JWT)
3. Validar token     →  POST /api/sesion/validar
4. Cerrar sesión     →  POST /api/sesion/logout          (invalida el JWT)
```

---

## Endpoints disponibles

Base URL: `http://localhost:8082/api/sesion`

---

### 1. Crear credencial

**POST** `/api/sesion/credencial`

Vincula un `usuarioId` (del `usuario-service`) a un correo y contraseña. Llamar una vez al registrar un usuario nuevo.

Body:
```json
{
  "usuarioId": 1,
  "correo": "juan@ecomarket.cl",
  "contrasena": "Clave1234!"
}
```

> `contrasena` requiere mínimo 8 caracteres.

Respuesta — `201 Created`:
```json
{ "mensaje": "Credencial creada exitosamente." }
```

---

### 2. Iniciar sesión

**POST** `/api/sesion/login`

Body:
```json
{
  "correo": "juan@ecomarket.cl",
  "contrasena": "Clave1234!"
}
```

Respuesta — `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuarioId": 1,
  "correo": "juan@ecomarket.cl",
  "rol": "ROLE_USER",
  "expiracionMs": 86400000
}
```

> Guarda el `token`; lo necesitarás en los siguientes endpoints.

---

### 3. Validar token JWT

**POST** `/api/sesion/validar`

Body:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Respuesta — `200 OK` (token válido):
```json
{
  "valido": true,
  "usuarioId": 1,
  "correo": "juan@ecomarket.cl",
  "roles": ["ROLE_USER"]
}
```

Respuesta cuando el token es inválido o está en la blacklist:
```json
{ "valido": false }
```

---

### 4. Cerrar sesión

**POST** `/api/sesion/logout`

Agrega el token a la blacklist (tabla `sesiones_jwt`). Cualquier validación posterior del mismo token retornará `valido: false`.

Body:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Respuesta — `200 OK`:
```json
{ "mensaje": "Sesión cerrada exitosamente." }
```

---

### 5. Cambiar correo

**PUT** `/api/sesion/correo`

Body:
```json
{
  "usuarioId": 1,
  "nuevoCorreo": "nuevo@ecomarket.cl",
  "contrasenaActual": "Clave1234!"
}
```

Respuesta — `200 OK`:
```json
{ "mensaje": "Correo actualizado exitosamente." }
```

---

### 6. Cambiar contraseña

**PUT** `/api/sesion/contrasena`

Body:
```json
{
  "usuarioId": 1,
  "contrasenaActual": "Clave1234!",
  "nuevaContrasena": "NuevaClave2024!"
}
```

> `nuevaContrasena` requiere mínimo 8 caracteres.

Respuesta — `200 OK`:
```json
{ "mensaje": "Contraseña actualizada exitosamente." }
```

---

### 7. Recuperar credenciales (olvidé mi contraseña)

**POST** `/api/sesion/recuperar`

Body:
```json
{
  "correo": "juan@ecomarket.cl"
}
```

Respuesta — `200 OK`:
```json
{
  "mensaje": "Código de recuperación generado: abc123XYZ... (válido por 2 horas). En producción, se enviaría al correo registrado."
}
```

> En desarrollo el código se devuelve directamente en la respuesta. Cópialo para usar en el siguiente endpoint.

---

### 8. Restablecer contraseña con código

**POST** `/api/sesion/restablecer`

Body:
```json
{
  "codigo": "abc123XYZ...",
  "nuevaContrasena": "Restablecida2024!"
}
```

Respuesta — `200 OK`:
```json
{ "mensaje": "Contraseña restablecida exitosamente." }
```

> El código es de un solo uso y expira a las 2 horas de generarse.

---

### 9. Inhabilitar credenciales (bloquear cuenta)

**DELETE** `/api/sesion/inhabilitar`

Body:
```json
{
  "usuarioId": 1
}
```

Respuesta — `200 OK`:
```json
{ "mensaje": "Credenciales inhabilitadas. La cuenta ha sido bloqueada." }
```

> Una cuenta bloqueada no puede iniciar sesión. Para desbloquearla usa el flujo de recuperación/restablecimiento.

---

## Pruebas rápidas con curl

```bash
BASE="http://localhost:8082/api/sesion"

# 1. Crear credencial
curl -s -X POST $BASE/credencial \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"correo":"juan@ecomarket.cl","contrasena":"Clave1234!"}' | jq .

# 2. Login (guarda el token)
TOKEN=$(curl -s -X POST $BASE/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"juan@ecomarket.cl","contrasena":"Clave1234!"}' | jq -r '.token')
echo "Token: $TOKEN"

# 3. Validar token
curl -s -X POST $BASE/validar \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}" | jq .

# 4. Cambiar correo
curl -s -X PUT $BASE/correo \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"nuevoCorreo":"nuevo@ecomarket.cl","contrasenaActual":"Clave1234!"}' | jq .

# 5. Cambiar contraseña
curl -s -X PUT $BASE/contrasena \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"contrasenaActual":"Clave1234!","nuevaContrasena":"NuevaClave2024!"}' | jq .

# 6. Recuperar contraseña (devuelve el código en dev)
CODIGO=$(curl -s -X POST $BASE/recuperar \
  -H "Content-Type: application/json" \
  -d '{"correo":"nuevo@ecomarket.cl"}' | jq -r '.mensaje' | grep -oP '(?<=generado: )[^ ]+')
echo "Código: $CODIGO"

# 7. Restablecer con código
curl -s -X POST $BASE/restablecer \
  -H "Content-Type: application/json" \
  -d "{\"codigo\":\"$CODIGO\",\"nuevaContrasena\":\"Restablecida2024!\"}" | jq .

# 8. Cerrar sesión (invalida el token)
curl -s -X POST $BASE/logout \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}" | jq .

# 9. Validar token tras logout (debe retornar valido: false)
curl -s -X POST $BASE/validar \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}" | jq .

# 10. Bloquear cuenta
curl -s -X DELETE $BASE/inhabilitar \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1}' | jq .
```

> `jq` es opcional; retíralo si no lo tienes instalado.

---

## Errores comunes

| Situación | HTTP | Mensaje |
|---|---|---|
| Correo o contraseña incorrectos | 401 | `"Correo o contraseña incorrectos."` |
| Cuenta bloqueada | 403 | `"La cuenta está bloqueada. Contacte al administrador."` |
| Correo ya registrado | 409 | `"El correo '...' ya está registrado."` |
| Credencial no encontrada | 404 | `"No se encontraron credenciales para el usuario ..."` |
| Token inválido o expirado | 401 | `"El token proporcionado no es válido."` |
| Código de recuperación inválido/expirado/usado | 401 | `"El código es inválido, ya fue usado o expiró."` |

---

## Relación con usuario-service

Este servicio es independiente del `usuario-service` (puerto 8081). La vinculación se hace a través del campo `usuarioId`:

```
usuario-service  →  crea el perfil (PerfilUsuario) y obtiene su ID
iniciosesion-service  →  recibe ese ID y crea las credenciales de acceso
```

Flujo recomendado al registrar un usuario nuevo:
1. `POST http://localhost:8081/api/usuarios/registro` → obtén el `id` del perfil creado.
2. `POST http://localhost:8082/api/sesion/credencial` → crea las credenciales usando ese `id`.

---

## Estructura del proyecto

```
iniciosesion-service/
├── src/main/java/com/ecomarket/iniciosesion/
│   ├── controller/     # LoginCuentaController (endpoints REST)
│   ├── service/        # LoginCuentaService + impl, JwtUtil
│   ├── model/          # Credencial, SesionJWT, TokenRecuperacion
│   ├── dto/            # Request/Response por operación
│   ├── repository/     # Repositorios JPA
│   └── exception/      # Excepciones personalizadas + GlobalExceptionHandler
└── src/main/resources/
    └── application.properties
```

---

## Ejecutar tests

```bash
mvn test
```

---

## Notas

- Las contraseñas se almacenan hasheadas con **BCrypt**.
- El logout funciona por **blacklist**: el token sigue siendo criptográficamente válido pero queda marcado como invalidado en la tabla `sesiones_jwt`.
- Los códigos de recuperación expiran en **2 horas** y son de **un solo uso**.
- El rol actualmente se fija como `ROLE_USER`; la implementación está preparada para cargarlo desde base de datos en versiones futuras.
