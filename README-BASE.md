# LojaVents — Base backend e infraestructura

Este paquete agrega a la carpeta raíz del proyecto:

- `backend/`: Spring Boot + Java 21.
- `docker-compose.yml`: PostgreSQL, MongoDB y backend.
- `.env.example`: variables de entorno de desarrollo.

## Instalación

1. Copia `.env.example` como `.env`.
2. Desde la raíz `LojaVents`, ejecuta:

```powershell
docker compose up --build
```

3. Espera a que los tres servicios estén activos.
4. Abre:

```text
http://localhost:8080/api/v1/sistema/salud
```

La respuesta debe indicar `estado: OK` y mostrar conteos de PostgreSQL y MongoDB.

## Apagar

```powershell
docker compose down
```

## Borrar también los datos de prueba

```powershell
docker compose down -v
```

> Esta es la base de infraestructura. Los siguientes módulos incorporarán JWT,
> locales, reservas, favoritos, reseñas, propietario, administración y el pago simulado.
