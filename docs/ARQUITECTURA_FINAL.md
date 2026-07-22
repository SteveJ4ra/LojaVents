# Arquitectura final de LojaVents

## Flujo principal

```text
Navegador
   |
   | HTTPS :443
   v
Nginx + Angular
   |
   | /api/*
   v
Spring Boot :8080
   |                    |
   | JPA/Flyway         | Spring Data MongoDB
   v                    v
PostgreSQL :5432      MongoDB :27017
```

## Responsabilidades

### Angular

Presenta las vistas públicas y privadas:

- autenticación;
- catálogo y detalle de locales;
- reservas y pagos simulados;
- favoritos y reseñas;
- perfil y solicitud de propietario;
- paneles de propietario y administración.

### Nginx

- sirve la compilación estática de Angular;
- mantiene las rutas del SPA;
- redirige HTTP hacia HTTPS;
- termina la conexión TLS;
- actúa como proxy inverso de `/api/`;
- añade encabezados de seguridad básicos.

### Spring Boot

- expone la API REST;
- aplica autenticación JWT y autorización por roles;
- valida las reglas de negocio;
- calcula precios;
- controla reservas, disponibilidad y pagos simulados;
- registra auditoría.

### PostgreSQL

Almacena información transaccional:

- usuarios y roles;
- locales y disponibilidad;
- reservas y pagos;
- favoritos y reseñas;
- solicitudes de propietario.

### MongoDB

Almacena eventos de auditoría y actividad.

## Exposición de puertos

Solo Nginx publica puertos hacia Windows:

- `80`: redirección a HTTPS;
- `443`: aplicación segura.

Backend, PostgreSQL y MongoDB permanecen accesibles únicamente dentro de la red de Docker.
