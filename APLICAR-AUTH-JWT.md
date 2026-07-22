# LojaVents — Bloque 2: autenticación JWT

Este parche incorpora:

- Registro real en PostgreSQL.
- Inicio de sesión real con contraseñas BCrypt.
- Emisión y validación de JWT.
- Roles `CLIENTE`, `PROPIETARIO` y `ADMINISTRADOR`.
- Endpoint autenticado `GET /api/v1/auth/me`.
- Auditoría de registro e inicio de sesión en MongoDB.
- Conexión del login y registro de Angular con Spring Boot.
- Interceptor Angular que agrega `Authorization: Bearer <token>`.

## Aplicación

Desde la carpeta raíz `C:\Users\steve\Desktop\LojaVents`:

```powershell
docker compose down
```

Descomprime este ZIP dentro de esa misma carpeta usando `-Force`.

No reemplaces tu archivo `.env`. Solo agrega la expiración si todavía no existe:

```powershell
if (-not (Select-String -Path .\.env -Pattern '^JWT_EXPIRATION_MINUTES=' -Quiet)) {
  Add-Content .\.env 'JWT_EXPIRATION_MINUTES=120'
}
```

El valor `JWT_SECRET` debe tener al menos 32 caracteres. El secreto que ya se creó en el bloque anterior cumple ese requisito.

Reconstruye el backend:

```powershell
docker compose up -d --build --force-recreate backend
```

Revisa los registros:

```powershell
docker compose logs -f --tail=150 backend
```

Cuando aparezca `Started LojaVentsApplication`, sal de los logs con `Ctrl + C`.

## Comprobaciones

```powershell
docker compose ps
Invoke-RestMethod http://localhost:8080/api/v1/sistema/salud | ConvertTo-Json
```

Ahora deben existir tres usuarios de demostración:

- `cliente@lojavents.ec` / `123456`
- `propietario@lojavents.ec` / `123456`
- `admin@lojavents.ec` / `123456`

Prueba el login por API:

```powershell
$body = @{
  email = 'cliente@lojavents.ec'
  password = '123456'
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/v1/auth/login `
  -ContentType 'application/json' `
  -Body $body

$login | ConvertTo-Json -Depth 5

Invoke-RestMethod `
  -Uri http://localhost:8080/api/v1/auth/me `
  -Headers @{ Authorization = "Bearer $($login.accessToken)" } |
  ConvertTo-Json -Depth 5
```

## Ejecutar Angular

En otra terminal:

```powershell
cd C:\Users\steve\Desktop\LojaVents\frontend
npm.cmd install
npm.cmd start
```

Abre `http://localhost:4200` y prueba inicio de sesión y registro.

## Estado del proyecto

En este bloque, autenticación y usuarios de sesión ya son reales. El catálogo, favoritos, reservas, solicitudes de propietario y paneles administrativos todavía conservan datos simulados; se migrarán a la API en los siguientes bloques.
