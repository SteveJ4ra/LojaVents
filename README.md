# LojaVents

LojaVents es una aplicación web para descubrir, publicar y reservar espacios para eventos en Loja. Incluye catálogo público de locales, flujos diferenciados para clientes, propietarios y administradores, y reservas con disponibilidad por horario.

La aplicación se ejecuta con un frontend Angular servido por Nginx y una API REST de Spring Boot. El entorno local utiliza Docker Compose y HTTPS en `localhost`.

## Funcionalidades principales

- **Exploración:** catálogo de locales, búsqueda por texto, tipo de evento, fecha, capacidad y precio; detalle con imágenes, amenidades, reglas, política de cancelación, disponibilidad y reseñas.
- **Reservas:** selección de fecha, hora, duración y asistentes; facturación, aceptación de condiciones, comprobación de disponibilidad y pago interno simulado.
- **Clientes:** registro, inicio de sesión, perfil, favoritos, reservas, reseñas después del evento y solicitud para convertirse en propietario.
- **Propietarios:** publicación y edición de locales, carga de imágenes, precio, capacidad, tipos de evento, amenidades, reglas, bloqueos horarios y reservas vinculadas.
- **Administración:** gestión de usuarios, revisión de solicitudes de propietario y locales, e indicadores de operación.

## Roles del sistema

| Rol | Alcance principal |
|---|---|
| Visitante | Explora el catálogo, consulta detalles y reseñas. |
| Cliente | Gestiona perfil, favoritos, reservas, reseñas y solicitud de propietario. |
| Propietario | Administra sus locales, disponibilidad y reservas vinculadas. |
| Administrador | Gestiona usuarios, solicitudes de propietario, locales e indicadores. |

El frontend usa guards por autenticación y rol. La API valida JWT y restringe las operaciones de reserva a usuarios autenticados que no sean administradores.

## Tecnologías

| Área | Tecnología |
|---|---|
| Frontend | Angular 21, TypeScript, SCSS, RxJS y Lucide Angular |
| Backend | Java 21 y Spring Boot 4.1.0 |
| API y seguridad | REST, Spring Security, JWT y Bean Validation |
| Persistencia transaccional | PostgreSQL 17, Spring Data JPA y Flyway |
| Auditoría y archivos | MongoDB 8, Spring Data MongoDB y GridFS |
| Infraestructura local | Docker Compose, Nginx 1.29 y certificados TLS locales |
| Pruebas | JUnit, Spring Security Test, Vitest y Angular TestBed |

## Arquitectura

El backend es un monolito modular. Sus módulos funcionales principales son `auth`, `user`, `venue`, `reservation`, `engagement` y `dashboard`; `audit`, `storage`, `system`, `config` y `common` cubren responsabilidades transversales.

El frontend se organiza en `core` para servicios, guards e interceptores; `features` para las vistas; `layout` para navegación; y `shared` para modelos, componentes, validadores y utilidades.

```text
Navegador
  └─ HTTPS :443 → Nginx + Angular
                    └─ /api/* → Spring Boot :8080
                                      ├─ PostgreSQL :5432
                                      └─ MongoDB :27017
```

Nginx redirige HTTP hacia HTTPS, sirve Angular y actúa como proxy inverso de `/api/`. PostgreSQL conserva usuarios, locales, reservas, pagos, favoritos, reseñas y solicitudes. MongoDB conserva auditoría y archivos de GridFS, como imágenes de locales y documentos de solicitudes.

## Estructura del repositorio

```text
backend/             API Spring Boot, dominio, migraciones y pruebas
frontend/            Aplicación Angular, estilos, pruebas y configuración Nginx
docs/                Arquitectura, despliegue, pruebas y documentación UML
scripts/             Certificados, secretos y comprobación del sistema
certs/               Certificados locales generados (no versionados)
docker-compose.yml   Servicios locales y red de Docker
.env.example         Plantilla de variables de entorno
```

## Requisitos previos

- Docker Desktop en ejecución, con Docker Compose disponible.
- Puertos `80` y `443` disponibles para Nginx.
- PowerShell en Windows para los scripts de certificados y verificación.

Para ejecutar pruebas fuera de Docker también se requieren Java 21, Maven, Node.js 22 y npm. Docker es la forma principal de ejecución de la aplicación completa.

## Configuración

1. Cree el archivo local de variables a partir de la plantilla:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Edite `.env` y asigne valores seguros a las contraseñas de bases de datos y a `JWT_SECRET`. El secreto JWT debe tener al menos 32 bytes. Este script puede generarlo y actualizar el archivo:

   ```powershell
   .\scripts\generar-secreto-jwt.ps1 -ActualizarEnv
   ```

3. Genere los certificados de desarrollo la primera vez que se prepare la computadora:

   ```powershell
   .\scripts\generar-certificado-local.ps1
   .\scripts\confiar-certificado-local.ps1
   ```

   El segundo comando agrega la autoridad local al almacén del usuario de Windows. Cierre y vuelva a abrir el navegador después de confiar el certificado. Los certificados generados son solo para desarrollo local.

### Variables de entorno

| Variable | Uso |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Base de datos transaccional PostgreSQL. |
| `MONGO_INITDB_ROOT_USERNAME`, `MONGO_INITDB_ROOT_PASSWORD`, `MONGO_DATABASE` | Acceso y base de datos MongoDB. |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring Boot; la plantilla usa `docker`. |
| `JWT_SECRET` | Clave de firma de JWT; debe ser secreta y tener al menos 32 bytes. |
| `JWT_EXPIRATION_MINUTES` | Duración de los tokens; la plantilla usa `120`. |

El archivo `.env`, los certificados y las claves privadas están excluidos de Git.

## Ejecución con Docker Compose

Desde la raíz del repositorio:

```powershell
docker compose up -d --build
```

El comando construye frontend y backend, inicia PostgreSQL y MongoDB, y aplica las migraciones Flyway durante el arranque del backend. Para comprobar los servicios:

```powershell
docker compose ps
```

Después de cambios de código, se reconstruye con el mismo comando. Los registros se consultan con:

```powershell
docker compose logs -f
docker compose logs -f backend
```

Para detener servicios y conservar datos:

```powershell
docker compose down
```

`docker compose down -v` también elimina los volúmenes de PostgreSQL y MongoDB; se debe usar solo cuando se desea reiniciar los datos locales.

## Acceso a la aplicación

- Aplicación: [https://localhost](https://localhost)
- HTTP local: `http://localhost` redirige a HTTPS.
- Estado de Nginx: `https://localhost/nginx-health`
- Estado público de la API: `https://localhost/api/v1/sistema/salud`

Los puertos del backend, PostgreSQL y MongoDB no se publican hacia el host; se comunican dentro de la red de Docker Compose.

## Pruebas

La comprobación de infraestructura, HTTPS, SPA, API y autenticación está disponible con:

```powershell
.\scripts\probar-sistema.ps1
```

El script requiere contenedores activos y certificado local confiable. Por defecto utiliza el usuario de demostración `cliente@lojavents.ec`; se pueden reemplazar los parámetros:

```powershell
.\scripts\probar-sistema.ps1 -Email "cliente@lojavents.ec" -Password "<contraseña>"
```

Las pruebas unitarias y de integración se ejecutan de forma separada:

```powershell
# Backend
Set-Location backend
mvn test

# Frontend
Set-Location ..\frontend
npm ci
npm test -- --watch=false
```

La compilación del frontend se verifica con `npm run build` desde `frontend/`.

## Estados de las reservas

Una reserva se crea en `EN_PROCESO`. El pago interno simulado crea un `PagoSimulado` y determina el resultado:

- Un pago aprobado cambia la reserva a `CONFIRMADA`; esto significa que el pago fue aprobado, la reserva quedó confirmada y el horario se considera ocupado.
- Un pago rechazado cambia la reserva a `RECHAZADA` y conserva la reserva junto con el motivo y el pago asociado.

`COMPLETADA` no existe en el modelo vigente; una migración convierte los datos históricos a `CONFIRMADA`. `FINALIZADA` no está implementado. `CANCELADA` existe como valor del enum, pero no hay una operación, endpoint ni interfaz funcional que haga alcanzable ese estado. No existe una integración real con PayPal ni un flujo de reintento de pago.

La aplicación muestra una referencia pública opaca con formato `LV-...`. El UUID técnico de la reserva se usa internamente para el flujo de pago y no se presenta como código público.

## Documentación técnica

La documentación vigente se encuentra en `docs/` y `docs/actualizacion/`:

- [Arquitectura local](docs/ARQUITECTURA_FINAL.md)
- [Despliegue local con HTTPS](docs/DESPLIEGUE_LOCAL_HTTPS.md)
- [Matriz de pruebas](docs/MATRIZ_PRUEBAS_FINALES.md)
- [Decisiones futuras del dominio](docs/DECISIONES_FUTURAS_DOMINIO.md)
- [Análisis y requisitos actualizados](docs/actualizacion/01-analisis-arquitectura-actual.md)
- Diagramas UML vigentes: paquetes, componentes, despliegue, clases de dominio, secuencia de reserva y estados de reserva en `docs/actualizacion/`.

No se declara una licencia en el repositorio.
