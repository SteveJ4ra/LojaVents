# LojaVents — Bloque 3: catálogo y gestión de locales

Este bloque agrega persistencia real de locales en PostgreSQL y conecta Angular con la API.

## Incluye

- Migración Flyway `V2__crear_catalogo_locales.sql`.
- Entidades JPA para locales y bloqueos de disponibilidad.
- Catálogo público, detalle y filtros.
- CRUD del propietario.
- Activación/desactivación por propietario y administrador.
- Bloqueos de fechas y horas.
- Auditoría de operaciones en MongoDB.
- Datos iniciales de tres locales.
- Angular conectado al backend para catálogo y gestión.

## Aplicación

Desde la carpeta raíz del proyecto:

```powershell
docker compose down

Expand-Archive `
  -Path .\LojaVents-Bloque-3-Catalogo-Locales.zip `
  -DestinationPath . `
  -Force

docker compose up -d --build --force-recreate backend
```

No uses `docker compose down -v`: Flyway aplicará la migración V2 sobre la base existente.

## Verificación

```powershell
docker compose logs -f --tail=180 backend
```

Debe aparecer:

```text
Successfully applied 1 migration
Started LojaVentsApplication
```

Luego:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/locales | ConvertTo-Json -Depth 8
```

Para ejecutar Angular:

```powershell
cd .\frontend
npm.cmd install
npm.cmd start
```

Abre `http://localhost:4200`.
