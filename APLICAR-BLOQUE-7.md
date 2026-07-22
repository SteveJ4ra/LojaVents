# LojaVents — Bloque 7: paneles y estadísticas reales

Este bloque reemplaza los valores calculados en el navegador por indicadores obtenidos directamente desde PostgreSQL y MongoDB.

## Incluye

- Panel administrativo con usuarios, locales, reservas, reseñas, solicitudes e ingresos simulados.
- Estado de cuentas activas, suspendidas e inactivas.
- Evolución de reservas e ingresos de los últimos seis meses.
- Locales con mayor actividad.
- Actividad reciente tomada de MongoDB.
- Panel de propietario con ingresos, próximos eventos y rendimiento por local.
- Índices de PostgreSQL para optimizar las consultas de los paneles.
- Corrección del import de `BearerTokenAuthenticationFilter` del bloque anterior.

## Aplicación

1. Detener Angular con `Ctrl + C`.
2. Desde la raíz de LojaVents:

```powershell
docker compose down
```

3. Descomprimir el ZIP en la raíz con `-Force`.
4. Reconstruir:

```powershell
docker compose up -d --build --force-recreate backend
```

5. Revisar:

```powershell
docker compose logs -f --tail=220 backend
```

Flyway debe aplicar `V6__optimizar_consultas_paneles.sql` y el backend debe terminar en `Started LojaVentsApplication`.

6. Iniciar Angular sin reinstalar paquetes:

```powershell
cd .\frontend
npm.cmd start
```

## Rutas de prueba

- Propietario: `http://localhost:4200/propietario`
- Administrador: `http://localhost:4200/admin`

## Endpoints

- `GET /api/v1/propietario/dashboard`
- `GET /api/v1/admin/dashboard`

Ambos requieren JWT y el rol correspondiente.
