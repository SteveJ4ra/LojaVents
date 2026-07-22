# Bloque 8 — Frontend Docker, Nginx y proxy inverso

Este bloque agrega el frontend Angular al mismo `docker-compose.yml` del proyecto.
Nginx sirve la aplicación compilada y reenvía las solicitudes `/api/**` al backend.

## Resultado

- Frontend: `http://localhost`
- API mediante Nginx: `http://localhost/api/v1/**`
- Backend directo para diagnóstico: `http://localhost:8080`
- Salud de Nginx: `http://localhost/nginx-health`

## Inicio

```powershell
docker compose down
docker compose up -d --build
```

## Comprobación

```powershell
docker compose ps
Invoke-WebRequest http://localhost/nginx-health
Invoke-RestMethod http://localhost/api/v1/sistema/salud | ConvertTo-Json
```

Ya no es necesario ejecutar `npm.cmd start` para usar la aplicación completa.
El servidor de desarrollo en el puerto 4200 puede seguir utilizándose cuando se trabaje
específicamente en Angular.
