# LojaVents

Plataforma web para buscar, publicar y reservar locales para eventos en Loja.

## Tecnologías

- Angular
- Spring Boot y Java 21
- PostgreSQL
- MongoDB
- Docker Compose
- Nginx
- HTTPS local

## Inicio rápido

Desde la raíz del proyecto:

```powershell
.\scripts\generar-certificado-local.ps1
.\scripts\confiar-certificado-local.ps1
docker compose up -d --build
```

Aplicación:

```text
https://localhost
```

Verificación automatizada:

```powershell
.\scripts\probar-sistema.ps1
```

Apagar:

```powershell
docker compose down
```

No uses `docker compose down -v` salvo que quieras eliminar todos los datos locales.

## Documentación

- `docs/ARQUITECTURA_FINAL.md`
- `docs/DESPLIEGUE_LOCAL_HTTPS.md`
- `docs/MATRIZ_PRUEBAS_FINALES.md`
