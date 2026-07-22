# Contrato API sugerido

Prefijo recomendado: `/api/v1`.

## Autenticación
- `POST /auth/login`
- `POST /auth/register`
- `GET /auth/me`

## Locales y disponibilidad
- `GET /locales`
- `GET /locales/{id}`
- `POST /locales`
- `PUT /locales/{id}`
- `POST /locales/{id}/bloqueos`
- `DELETE /locales/{id}/bloqueos/{bloqueoId}`

## Reservas y pagos
- `POST /reservas/cotizacion`
- `POST /reservas`
- `GET /reservas/me`
- `GET /propietarios/me/reservas`
- `POST /pagos/orden`
- `POST /pagos/{id}/capturar`

## Usuarios, propietarios y administración
- `POST /propietarios/solicitudes`
- `GET /admin/usuarios`
- `PATCH /admin/usuarios/{id}/estado`
- `PATCH /admin/propietarios/{id}/aprobar`

## Favoritos y reseñas
- `GET /favoritos`
- `POST /favoritos/{localId}`
- `DELETE /favoritos/{localId}`
- `POST /locales/{id}/resenas`

El backend debe volver a validar disponibilidad y costos antes de registrar una reserva como `COMPLETADA`.
