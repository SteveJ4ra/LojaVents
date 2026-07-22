# LojaVents — Bloque 4: reservas y pago simulado

Este parche agrega:

- Reservas persistidas en PostgreSQL.
- Validación de capacidad, fecha, horarios bloqueados y reservas confirmadas.
- Pago académico simulado con resultados aprobado, fondos insuficientes, error de pasarela y error de aplicación.
- Conservación de reservas rechazadas.
- Vista real de reservas del cliente.
- Vista real de reservas recibidas por el propietario.
- Auditoría en MongoDB.

## Aplicación

Desde la raíz del proyecto `LojaVents`:

```powershell
docker compose down

Expand-Archive `
  -Path .\LojaVents-Bloque-4-Reservas-Pago-Simulado.zip `
  -DestinationPath . `
  -Force

docker compose up -d --build --force-recreate backend

docker compose logs -f --tail=180 backend
```

Cuando aparezca `Started LojaVentsApplication`, pulsa `Ctrl + C` para salir de la vista de logs.

No se agregaron dependencias de npm, por lo que no es necesario ejecutar `npm install` ni `npm ci`.

```powershell
cd .\frontend
npm.cmd start
```

## Migración

Flyway aplicará:

```text
V3__crear_reservas_y_pagos_simulados.sql
```

Tablas nuevas:

```text
reservas
pagos_simulados
```
