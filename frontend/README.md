# LojaVents Frontend

Frontend funcional de **LojaVents**, desarrollado con Angular 21, TypeScript, HTML5 y SCSS.

## Ejecutar

```bash
npm install
npm start
```

Abrir `http://localhost:4200`.

## Cuentas demo

| Rol | Correo | Contraseña |
|---|---|---|
| Cliente | `cliente@lojavents.ec` | `123456` |
| Propietario + cliente | `propietario@lojavents.ec` | `123456` |
| Administrador | `admin@lojavents.ec` | `123456` |

## Incluye

- Inicio, búsqueda, filtros y detalle de locales.
- Registro, inicio/cierre de sesión, favoritos y perfil.
- Reserva por pasos y pago simulado aprobado o rechazado.
- Historial de reservas y reseñas.
- Solicitud de rol de propietario.
- Panel de propietario: locales y disponibilidad.
- Panel administrativo: usuarios, verificaciones y locales.
- Guards de autenticación y rol.
- Interceptor preparado para el backend.
- Nginx, Docker y ejemplo de HTTPS.

Los datos se simulan con `localStorage`. Cuando Spring Boot esté disponible, los servicios de `src/app/core/services` pueden usar `HttpClient`.

## Estructura

```text
src/app/
├── core/
│   ├── data/
│   ├── guards/
│   ├── interceptors/
│   └── services/
├── shared/
│   ├── components/
│   └── models/
├── layout/
└── features/
    ├── public/
    ├── auth/
    ├── venues/
    ├── booking/
    ├── customer/
    ├── owner/
    ├── admin/
    └── errors/
```

## Construcción para Nginx

```bash
npm run build
```

Resultado: `dist/lojavents-frontend/browser`.
