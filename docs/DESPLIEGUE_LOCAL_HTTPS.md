# Despliegue local con HTTPS

## 1. Requisitos

- Docker Desktop funcionando.
- Los archivos `.env`, `backend/` y `frontend/` presentes.
- Puertos 80 y 443 disponibles.

## 2. Generar certificados

```powershell
cd C:\Users\steve\Desktop\LojaVents
.\scripts\generar-certificado-local.ps1
```

El script genera:

```text
certs/
├── lojavents-dev-ca.crt
├── lojavents-dev-ca.key
├── localhost.crt
└── localhost.key
```

## 3. Confiar en el certificado

```powershell
.\scripts\confiar-certificado-local.ps1
```

Cierra y vuelve a abrir Chrome o Edge.

El certificado es solo para desarrollo. No debe utilizarse en un servidor público.

## 4. Revisar secretos

Para generar un nuevo secreto JWT:

```powershell
.\scripts\generar-secreto-jwt.ps1 -ActualizarEnv
```

También se recomienda cambiar las contraseñas de PostgreSQL y MongoDB antes de una entrega final.

## 5. Construir y ejecutar

```powershell
docker compose down
docker compose up -d --build
```

## 6. Comprobar servicios

```powershell
docker compose ps
```

Deben aparecer cuatro contenedores activos:

```text
lojavents-frontend
lojavents-backend
lojavents-postgres
lojavents-mongodb
```

## 7. Ejecutar pruebas automáticas

```powershell
.\scripts\probar-sistema.ps1
```

## 8. Abrir la aplicación

```text
https://localhost
```

Al consultar `http://localhost`, Nginx redirige automáticamente hacia HTTPS.

## 9. Comandos de mantenimiento

Ver registros:

```powershell
docker compose logs -f
```

Ver únicamente el backend:

```powershell
docker compose logs -f backend
```

Reconstruir después de cambiar código:

```powershell
docker compose up -d --build
```

Apagar conservando datos:

```powershell
docker compose down
```

Eliminar también los datos:

```powershell
docker compose down -v
```

El último comando es destructivo.
