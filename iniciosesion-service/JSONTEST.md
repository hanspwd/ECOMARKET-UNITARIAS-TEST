POST /api/sesion/credencial
{
  "usuarioId": 1,
  "correo": "admin@ecomarket.cl",
  "contrasena": "Admin1234!"
}
jsonPOST /api/sesion/login
{
  "correo": "admin@ecomarket.cl",
  "contrasena": "Admin1234!"
}
jsonPOST /api/sesion/validar
{
  "token": "PEGA_AQUI_EL_TOKEN_DEL_LOGIN"
}
jsonPOST /api/sesion/logout
{
  "token": "PEGA_AQUI_EL_TOKEN_DEL_LOGIN"
}
jsonPUT /api/sesion/correo
{
  "usuarioId": 1,
  "nuevoCorreo": "nuevo@ecomarket.cl",
  "contrasenaActual": "Admin1234!"
}
jsonPUT /api/sesion/contrasena
{
  "usuarioId": 1,
  "contrasenaActual": "Admin1234!",
  "nuevaContrasena": "NuevaClave2024!"
}
jsonPOST /api/sesion/recuperar
{
  "correo": "admin@ecomarket.cl"
}
jsonPOST /api/sesion/restablecer
{
  "codigo": "PEGA_AQUI_EL_CODIGO_DE_RECUPERAR",
  "nuevaContrasena": "Restablecida2024!"
}
jsonDELETE /api/sesion/inhabilitar
{
  "usuarioId": 1
}