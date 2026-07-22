# Análisis de la arquitectura actual de LojaVents

**Fecha del análisis:** 2026-07-20  
**Alcance:** implementación, configuración e infraestructura actualmente presentes en el repositorio.  
**Método:** inspección estática del código activo y de sus archivos de construcción/despliegue. No se usó documentación previa como evidencia funcional y no se ejecutaron etapas posteriores ni diagramas UML.

## Convenciones de certeza

- **Verificado:** existe evidencia directa y conectada en código o configuración activa.
- **Inferido:** conclusión razonable a partir de varias evidencias, pero no demostrada mediante ejecución o entorno productivo.
- **No verificable:** el repositorio no contiene evidencia suficiente para confirmarlo.

# 1. Resumen ejecutivo

**Verificado.** LojaVents está implementado como una aplicación web de tres niveles: una SPA Angular, una API REST Spring Boot y dos mecanismos de persistencia. PostgreSQL conserva el dominio transaccional mediante JPA y migraciones Flyway; MongoDB conserva eventos de auditoría y archivos mediante GridFS. El despliegue local integrado usa Docker Compose con cuatro servicios: `frontend`, `backend`, `postgres` y `mongodb`.

El backend sigue una organización modular por funcionalidad (`auth`, `user`, `venue`, `reservation`, `engagement`, `dashboard`, `storage`, `audit`, `system`) y, dentro de varios módulos, separa `api`, `application`, `domain` y `repository`. No es una arquitectura hexagonal estricta: los servicios de aplicación consumen repositorios Spring Data directamente, varios módulos comparten entidades y DTO de otros módulos, y no hay puertos/adaptadores propios.

El frontend usa componentes standalone y carga diferida por rutas. Sus servicios raíz combinan comunicación HTTP y estado en memoria con Angular Signals. La autenticación se conserva en `localStorage`; un interceptor adjunta el JWT y guards funcionales controlan navegación. La autorización efectiva también se aplica en el backend mediante Spring Security.

Se verificaron flujos activos de registro e inicio de sesión, catálogo y detalle de locales, disponibilidad, creación de reservas, procesamiento interno de pagos, favoritos, reseñas, perfil, solicitud de rol propietario, administración de usuarios/locales/solicitudes, paneles y gestión de locales/bloqueos por propietarios. No se verificó un flujo de cancelación de reservas ni una pasarela de pago externa.

Principales hallazgos que condicionan la documentación futura:

- Existe un segundo proyecto Angular completo bajo `frontend/lojavents-frontend`, pero la construcción activa apunta a `frontend/src`.
- Los inicializadores de datos de demostración están activos sin `@Profile`; por tanto, forman parte del arranque de cualquier perfil observado.
- El pago implementado y persistido es interno (`PagoSimulado`) y no existe dependencia ni llamada a PayPal u otra pasarela.
- `EstadoReserva.CANCELADA` se modela y se reporta, pero no existe operación que lleve una reserva a ese estado.
- La búsqueda pública existe en backend, pero el frontend descarga la lista completa y filtra localmente.
- La cobertura automatizada observada es mínima; el script de humo es manual y depende de Docker.

# 2. Tecnologías detectadas

| Área | Tecnología/versión | Certeza | Evidencia |
|---|---|---:|---|
| Backend | Java 21 | Verificado | `backend/pom.xml`, `backend/Dockerfile` |
| Backend | Spring Boot 4.1.0 | Verificado | parent Maven en `backend/pom.xml` |
| API | Spring MVC / REST JSON | Verificado | `spring-boot-starter-web`, controladores `@RestController` |
| Validación | Jakarta Bean Validation | Verificado | `spring-boot-starter-validation`, anotaciones en DTO |
| Seguridad | Spring Security, OAuth2 Resource Server, JWT HS256, BCrypt | Verificado | `backend/pom.xml`, `SecurityConfig.java`, `JwtService.java` |
| Persistencia SQL | Spring Data JPA / Hibernate | Verificado | dependencias, entidades JPA y repositorios |
| Base SQL | PostgreSQL 17 Alpine | Verificado | `docker-compose.yml`, driver PostgreSQL |
| Migraciones | Flyway para PostgreSQL | Verificado | `application.yml`, `db/migration/V1...V9` |
| Persistencia documental | Spring Data MongoDB | Verificado | dependencia, `AuditEvent`, `AuditEventRepository` |
| Archivos | MongoDB GridFS | Verificado | `MediaStorageService.java` |
| Base documental | MongoDB 8.0 | Verificado | `docker-compose.yml` |
| Observabilidad | Spring Boot Actuator (health/info) | Verificado | dependencia y `application.yml` |
| Frontend | Angular 21.2, componentes standalone | Verificado | `frontend/package.json`, `main.ts`, componentes |
| Lenguaje frontend | TypeScript 5.9, objetivo ES2022 | Verificado | `package.json`, `tsconfig.json` |
| Reactividad | Angular Signals y RxJS 7.8 | Verificado | servicios frontend, `package.json` |
| Formularios | Angular Reactive Forms | Verificado | componentes de formularios y dependencia `@angular/forms` |
| Pruebas frontend | Vitest 4 + jsdom 28 | Verificado | `package.json`, `tsconfig.spec.json` |
| Servidor web | Nginx 1.29 Alpine | Verificado | `frontend/Dockerfile`, `nginx.conf` |
| Construcción frontend | Node 22 Alpine + npm 10.9.2 | Verificado | `frontend/Dockerfile`, `package.json` |
| Contenedores | Docker Compose, red bridge y volúmenes nombrados | Verificado | `docker-compose.yml` |
| TLS local | Certificado y clave montados desde `./certs` | Verificado como configuración | `docker-compose.yml`, `nginx.conf`; no se evaluó validez/confianza del certificado |
| Contenedor servlet | Implementación transitiva del starter web | Inferido | no está fijada de forma directa en `pom.xml`; no se generó árbol de dependencias resuelto |

No se detectaron NgRx, GraphQL, WebSocket, colas, cache distribuida, servicio de correo, analítica externa, mapas, PayPal ni SDK de pago externo en las dependencias o llamadas activas inspeccionadas.

# 3. Estructura general del repositorio

## 3.1 Módulos y rutas principales

| Ruta | Responsabilidad observada | Estado |
|---|---|---|
| `backend/` | API Spring Boot, dominio, servicios, persistencia, migraciones e imágenes iniciales | Activo |
| `frontend/` | Proyecto Angular construido por Docker y servido por Nginx | Activo |
| `frontend/lojavents-frontend/` | Segundo árbol Angular completo | Duplicado/ambiguo; no referenciado por el `angular.json` activo |
| `docs/` | Documentación existente | No usada como fuente de verdad en este análisis |
| `scripts/` | Generación de secreto/certificados y prueba manual del sistema | Auxiliar activo por ejecución manual |
| `certs/` | Certificado y clave TLS local montados por Compose | Recurso de despliegue local |
| `docker-compose.yml` | Orquestación local de cuatro contenedores | Activo |
| `.env` | Valores locales consumidos por Compose | Presente; contenido no inspeccionado para evitar exponer secretos |
| `.env.example` | Contrato de variables de entorno | Activo como plantilla |

## 3.2 Construcción

- **Backend:** Maven produce un JAR ejecutable. El Dockerfile usa una etapa Maven/Temurin 21 y una etapa final JRE 21 no privilegiada.
- **Frontend:** npm ejecuta `ng build`; el `defaultConfiguration` de Angular es `production`. Nginx recibe `dist/lojavents-frontend/browser`.
- **Exclusiones aplicadas al análisis:** no se trataron como código propio `.git`, `node_modules`, `target`, `dist`, binarios PNG ni contenido generado. Las imágenes solo se inventariaron como recursos.
- **Repositorio Git:** no se detectó una raíz Git en `C:\Users\steve\Desktop\LojaVents`; por ello no fue posible determinar historial, ramas ni si ciertos secretos están versionados.

## 3.3 Duplicación detectada

**Verificado.** `frontend/angular.json` establece `sourceRoot: src`; `frontend/Dockerfile` se ejecuta con contexto `./frontend`. En consecuencia, `frontend/src` es el árbol compilado. El árbol `frontend/lojavents-frontend` contiene otro `angular.json` y fuentes con apariencia de proyecto completo, pero no es un proyecto declarado dentro del `angular.json` superior. Sí entra innecesariamente en el contexto `COPY . .` de Docker, salvo exclusión externa no observada.

# 4. Arquitectura del backend

## 4.1 Estilo y flujo

**Verificado.** El backend es un monolito modular desplegado como un único JAR. El flujo predominante es:

`Controller REST -> servicio de aplicación -> repositorio Spring Data -> PostgreSQL/MongoDB`

Los DTO de entrada aplican validación declarativa; los servicios aplican reglas de negocio y transacciones; las entidades encapsulan transiciones simples. Las respuestas se forman mediante métodos estáticos `from(...)` en DTO. No se observó una capa separada de mapeo ni interfaces propias para abstraer repositorios.

## 4.2 Puntos de entrada HTTP

| Área | Endpoints verificados | Acceso efectivo según `SecurityConfig` |
|---|---|---|
| Autenticación | `POST /api/v1/auth/registro`, `POST /login` | Público |
| Sesión | `POST /api/v1/auth/refresh`, `GET /me` | JWT válido |
| Salud | `GET /api/v1/sistema/salud`, `GET /actuator/health` | Público a nivel Spring Security |
| Catálogo | `GET /api/v1/locales`, `/tipos-evento`, `/{id}`, `/{id}/disponibilidad`, `/{id}/resenas` | Público |
| Imágenes | `GET /api/v1/imagenes/{id}` | Público |
| Reservas cliente | `POST /api/v1/reservas`, `POST /{id}/pago-simulado`, `GET /mias`, `POST /{id}/resena` | Cualquier cuenta autenticada activa |
| Favoritos | `GET /api/v1/favoritos`, `/ids`, `POST/DELETE /{venueId}` | Cualquier cuenta autenticada activa |
| Perfil | `PUT /api/v1/perfil`, `PUT /password`, `DELETE /perfil` | Cualquier cuenta autenticada activa |
| Solicitud propietario | `GET /api/v1/solicitud-propietario/me`, `POST /solicitud-propietario` multipart | Cualquier cuenta autenticada activa |
| Propietario | panel, reservas, CRUD parcial de locales, estado, bloqueos y carga de imágenes bajo `/api/v1/propietario/**` | Rol `PROPIETARIO` |
| Administración | panel, usuarios/estado, locales/estado y solicitudes/documento/revisión bajo `/api/v1/admin/**` | Rol `ADMINISTRADOR` |

No existen endpoints verificados para eliminar un local, cancelar una reserva, reembolsar, modificar una reserva, aprobar/rechazar reservas manualmente ni cerrar sesión en servidor.

## 4.3 Servicios y responsabilidades

- `AuthApplicationService`: registro, login, consulta de usuario y reemisión de JWT.
- `JwtService`: crea tokens HS256 con `sub`, `email`, `roles`, emisor y expiración.
- `UserAccountApplicationService`: perfil, contraseña, baja lógica, administración de estados y ciclo de solicitud de propietario.
- `VenueApplicationService`: búsqueda, detalle, catálogo, locales del propietario, alta/edición, revisión administrativa y bloqueos.
- `ReservationAvailabilityService`: valida actividad, fecha/hora, duración, bloqueos y reservas completadas superpuestas.
- `ReservationApplicationService`: borrador, cálculo económico, procesamiento interno de pago y consultas por cliente/propietario.
- `FavoriteApplicationService`: consulta y alta/baja idempotente de favoritos visibles.
- `ReviewApplicationService`: reseña única de una reserva completada pasada y recálculo de promedio.
- `DashboardApplicationService`: métricas agregadas en memoria para administrador y propietario.
- `MediaStorageService`: validación, escritura y lectura de documentos/imágenes en GridFS.

## 4.4 Persistencia y transacciones

- JPA escanea solo los dominios `user`, `venue`, `reservation` y `engagement`.
- Repositorios JPA están habilitados para esos cuatro módulos; auditoría usa `MongoRepository`.
- `spring.jpa.open-in-view=false`; los servicios marcan operaciones con `@Transactional`.
- Flyway mantiene el esquema y Hibernate usa `ddl-auto=validate`.
- Se usan `@EntityGraph` para varias consultas de reservas, favoritos, reseñas y solicitudes.
- La aprobación de pago bloquea el local con `PESSIMISTIC_WRITE` y `Reserva` añade `@Version`.
- **Inferido/riesgo:** las escrituras SQL y Mongo de una misma operación no forman una transacción distribuida observada; una falla de Mongo podría afectar la operación o dejar consistencia parcial según el punto exacto del fallo.

## 4.5 Seguridad

- Sesiones stateless y autenticación Bearer JWT.
- Firma simétrica HS256; secreto mínimo de 32 bytes, emisor validado y expiración configurable (120 minutos por defecto).
- Contraseñas con BCrypt.
- Roles tomados del claim `roles` y convertidos a autoridades `ROLE_*`.
- `ActiveAccountFilter` consulta PostgreSQL en cada petición JWT y rechaza cuentas ausentes/inactivas con 403.
- CORS permite únicamente orígenes localhost/127.0.0.1 observados.
- CSRF está desactivado; es coherente con Bearer stateless, aunque el token en `localStorage` mantiene impacto ante XSS.
- Cabeceras de seguridad se configuran tanto en Spring como en Nginx.

## 4.6 Manejo de errores

`GlobalExceptionHandler` transforma `ApiException` y errores de validación de cuerpo a `ProblemDetail` con `code`; la validación incluye un mapa `fields`. `application.yml` evita exponer stack trace, excepción y detalles internos. No hay handler global explícito para todas las excepciones inesperadas ni uniformidad demostrada para errores producidos directamente por Spring Security o parámetros de consulta.

## 4.7 Dependencias internas relevantes

- `auth -> user, audit, common`
- `user -> storage, audit, auth.api.dto, common`
- `venue -> user, audit, reservation (solo disponibilidad desde controller), common`
- `reservation -> user, venue, audit, common`
- `engagement -> user, venue, reservation, audit, common`
- `dashboard -> user, venue, reservation, engagement, audit`
- `system -> user, audit`
- `storage -> common, Mongo GridFS`

Esto confirma modularidad por funcionalidad, pero también acoplamiento transversal de entidades y DTO entre módulos.

# 5. Arquitectura del frontend

## 5.1 Estructura activa

- `app.config.ts`: configura Router, restauración de scroll, HttpClient e interceptor JWT.
- `app.routes.ts`: declara rutas standalone y carga diferida con `loadComponent`.
- `core/guards`, `core/interceptors`, `core/services`: infraestructura y estado compartido.
- `features`: áreas `public`, `auth`, `venues`, `booking`, `customer`, `owner`, `admin`, `errors`.
- `layout`: barra de navegación, pie y layout de portales.
- `shared/components` y `shared/models`: componentes reutilizables y contratos TypeScript.

## 5.2 Rutas y vistas funcionales

| Audiencia | Rutas/vistas verificadas |
|---|---|
| Pública | `/`, `/locales`, `/locales/:id`, `/login`, `/registro` |
| Autenticada | `/reservar/:id`, `/favoritos`, `/mis-reservas`, `/perfil`, `/convertirme-en-propietario` |
| Propietario | `/propietario`, `/locales`, `/reservas`, alta/edición de local, `/disponibilidad` |
| Administrador | `/admin`, `/usuarios`, `/locales`, `/verificaciones` |
| Errores | `/403` y wildcard de página no encontrada |

El asistente de reserva tiene cinco pasos visibles: datos del evento, dirección de facturación, aceptación de reglas/política, pago y confirmación. La UI activa solo llama al modo `APPROVE`; los modos de rechazo existen en modelos/backend y son invocables por API, pero no tienen control visible detectado.

## 5.3 Comunicación y estado

- `environment.apiBaseUrl` es `/api/v1`; Nginx reenvía `/api/` a `backend:8080` y el servidor de desarrollo usa `proxy.conf.json` hacia `localhost:8080`.
- Los servicios de dominio (`VenueService`, `BookingService`, `FavoriteService`, `ReviewService`, `UserService`, `DashboardService`) usan `HttpClient` y Signals como cache/estado de sesión de la SPA.
- No se detectó store global externo ni persistencia de ese estado, salvo sesión y un `MockStoreService` no consumido.
- `VenueService` carga todos los locales públicos, reintenta tres veces y aplica búsqueda/filtros en memoria. El endpoint backend con filtros y `/tipos-evento` no es consumido por el servicio activo.
- `NotificationService` implementa toasts en memoria; no es un sistema de notificaciones persistentes o remotas.
- `ShareService` usa Web Share API y, como alternativa, Clipboard API del navegador.

## 5.4 Autenticación y autorización

- Token y usuario se guardan en `localStorage` con claves propias de LojaVents.
- Al crear `AuthService`, si existe token se llama a `/auth/refresh`; ese endpoint exige el JWT actual, por lo que no representa un refresh token independiente.
- El interceptor agrega `Authorization: Bearer` a URLs API y limpia sesión/redirige en 401.
- `authGuard` comprueba estado local; `roleGuard` comprueba roles locales y redirige a 403.
- La seguridad de datos no depende solo del cliente: el backend restringe prefijos y comprueba propiedad de locales/reservas.

## 5.5 Código auxiliar o no utilizado

- `core/data/mock-data.ts` y `MockStoreService` existen, pero `environment.useMockApi=false` y no se detectó ninguna inyección/referencia al store.
- `environment.production=false` es el único environment observado y no hay `fileReplacements` en `angular.json`, incluso cuando Docker ejecuta la configuración de compilación `production`.

# 6. Modelo de dominio persistente

## 6.1 Alcance confirmado

Se detectaron ocho clases `@Entity`. No se detectaron `@Embeddable`, `@MappedSuperclass` ni herencia entre entidades. `AuditEvent` es `@Document` de MongoDB y se documenta aparte, pero no se incorpora al futuro diagrama de clases JPA solicitado.

## 6.2 Entidades JPA

### `ec.edu.unl.lojavents.user.domain.Usuario`

- **Ruta:** `backend/src/main/java/ec/edu/unl/lojavents/user/domain/Usuario.java`.
- **Propósito:** cuenta autenticable con estado, roles y estado de verificación de propietario.
- **ID:** UUID generado; tabla `usuarios`.
- **Atributos:** nombres, email único, `passwordHash`, teléfono, `estado`, `estadoVerificacionPropietario`, `creadoEn`.
- **Colección:** `roles` es `@ElementCollection` eager en `usuario_roles`; clave compuesta `(usuario_id, rol)` por migración.
- **Enums directos:** `EstadoUsuario`, `EstadoVerificacionPropietario`, `Rol`.
- **Reglas:** una cuenta nueva recibe `CLIENTE`; agregar `PROPIETARIO` marca verificación `APROBADA`; email es único.
- **Ambigüedad:** V1 conserva una columna `activo`; V5 añade `estado` y migra su valor, pero la entidad actual ya no mapea `activo`.

### `ec.edu.unl.lojavents.user.domain.SolicitudPropietario`

- **Ruta:** `.../user/domain/SolicitudPropietario.java`.
- **Propósito:** solicitud y revisión administrativa del rol propietario.
- **ID:** UUID; tabla `solicitudes_propietario`.
- **Atributos:** identificación, metadatos/referencia del documento GridFS, notas, estado, comentario, fechas de creación/revisión.
- **Relaciones:** muchas solicitudes a un `Usuario` solicitante; muchas solicitudes pueden ser revisadas por un `Usuario` administrador opcional.
- **Enum directo:** `EstadoSolicitudPropietario` (`PENDIENTE`, `APROBADA`, `RECHAZADA`).
- **Restricción:** índice parcial SQL impide más de una solicitud pendiente por usuario.
- **Reglas:** aprobar/rechazar fija revisor, comentario normalizado y fecha; el servicio sincroniza rol y estado de verificación del usuario.

### `ec.edu.unl.lojavents.venue.domain.LocalEvento`

- **Ruta:** `.../venue/domain/LocalEvento.java`.
- **Propósito:** local publicable/reservable administrado por un propietario.
- **ID:** UUID; tabla `locales`.
- **Atributos:** nombre, descripciones, sector, dirección, precio/hora, capacidad, calificación, total de reseñas, destacado, activo, pendiente de revisión, política y timestamps.
- **Relaciones:** muchos locales a un `Usuario` propietario; uno a muchos `BloqueDisponibilidad`, con cascade total y orphan removal.
- **Colecciones de valor:** tipos, amenidades, reglas ordenadas e imágenes ordenadas en tablas separadas.
- **Restricciones:** precio y capacidad positivos en SQL; longitudes en JPA/DTO.
- **Reglas:** crear/editar/solicitar activación despublica y deja pendiente; aprobación administrativa publica; reseñas actualizan promedio y contador.
- **Ambigüedad:** tipos y amenidades son `String`, no enums; su catálogo canónico vive en `VenueCatalog`, por fuera de la entidad.

### `ec.edu.unl.lojavents.venue.domain.BloqueDisponibilidad`

- **Ruta:** `.../venue/domain/BloqueDisponibilidad.java`.
- **Propósito:** intervalo no reservable definido por el propietario.
- **ID:** UUID; tabla `local_bloqueos`.
- **Atributos:** fecha, hora inicial, hora final, motivo.
- **Relación:** muchos bloqueos a un `LocalEvento` obligatorio.
- **Restricción:** SQL exige `hora_inicio < hora_fin`; el servicio también rechaza superposición con otro bloqueo de la misma fecha.

### `ec.edu.unl.lojavents.reservation.domain.Reserva`

- **Ruta:** `.../reservation/domain/Reserva.java`.
- **Propósito:** solicitud de reserva, datos de facturación, importes, aceptación de políticas y resultado.
- **ID:** UUID; tabla `reservas`.
- **Atributos:** fecha/hora, duración, asistentes, dirección de facturación, subtotal, tarifa 8 %, total, estado, motivo, aceptaciones, reseña enviada, timestamps y versión optimista.
- **Relaciones:** muchas reservas a un `Usuario` cliente; muchas a un `LocalEvento`; una a cero/uno `PagoSimulado` mediante `mappedBy`, cascade y orphan removal.
- **Enum directo:** `EstadoReserva` (`EN_PROCESO`, `COMPLETADA`, `RECHAZADA`, `CANCELADA`).
- **Restricciones:** duración 1..12, asistentes positivos, importes no negativos; el DTO limita asistentes a 10 000.
- **Reglas:** el servidor calcula importes; pago aprobado completa y pago rechazado rechaza; disponibilidad se vuelve a verificar bajo bloqueo pesimista antes de aprobar.
- **Ambigüedad:** no se detectó transición a `CANCELADA`; `@Version` existe, pero el flujo crítico bloquea el local, no la reserva.

### `ec.edu.unl.lojavents.reservation.domain.PagoSimulado`

- **Ruta:** `.../reservation/domain/PagoSimulado.java`.
- **Propósito:** resultado persistido del procesamiento interno de pago.
- **ID:** UUID; tabla `pagos_simulados`.
- **Atributos:** estado, modo, referencia única, mensaje y fecha de proceso.
- **Relación:** uno a uno obligatorio y único con `Reserva`; esta entidad posee la FK `reserva_id`.
- **Enums directos:** `EstadoPagoSimulado` y `ModoPagoSimulado`.
- **Reglas:** una reserva solo admite un resultado; los modos producen aprobación o tres tipos de rechazo deterministas.
- **Confirmación:** no representa una transacción de una pasarela externa y no se detecta cobro real.

### `ec.edu.unl.lojavents.engagement.domain.Favorito`

- **Ruta:** `.../engagement/domain/Favorito.java`.
- **Propósito:** entidad asociativa entre cliente y local.
- **ID:** UUID; tabla `favoritos`.
- **Relaciones:** muchos favoritos a un `Usuario` cliente y muchos a un `LocalEvento`.
- **Restricción:** unicidad `(cliente_id, local_id)`; FKs con borrado en cascada en SQL.
- **Reglas:** alta idempotente en servicio; solo se agregan/muestran locales activos.

### `ec.edu.unl.lojavents.engagement.domain.Resena`

- **Ruta:** `.../engagement/domain/Resena.java`.
- **Propósito:** valoración verificada de un local originada en una reserva.
- **ID:** UUID; tabla `resenas`.
- **Relaciones:** una a una obligatoria y única con `Reserva`; muchas reseñas a un `Usuario` cliente y a un `LocalEvento`.
- **Atributos:** calificación, comentario y timestamps.
- **Restricciones:** calificación 1..5; comentario recortado de al menos 10 caracteres en SQL y máximo 2 000 en DTO.
- **Reglas:** solo reserva completada, propia y con fecha pasada; una reseña por reserva; recalcula promedio del local.

## 6.3 Enumeraciones directamente persistidas

| Enum | Valores | Entidad |
|---|---|---|
| `Rol` | `CLIENTE`, `PROPIETARIO`, `ADMINISTRADOR` | `Usuario.roles` |
| `EstadoUsuario` | `ACTIVO`, `SUSPENDIDO`, `INACTIVO` | `Usuario.estado` |
| `EstadoVerificacionPropietario` | `NO_SOLICITADA`, `PENDIENTE`, `APROBADA`, `RECHAZADA` | `Usuario` |
| `EstadoSolicitudPropietario` | `PENDIENTE`, `APROBADA`, `RECHAZADA` | `SolicitudPropietario` |
| `EstadoReserva` | `EN_PROCESO`, `COMPLETADA`, `RECHAZADA`, `CANCELADA` | `Reserva` |
| `EstadoPagoSimulado` | `APROBADO`, `RECHAZADO` | `PagoSimulado` |
| `ModoPagoSimulado` | `APPROVE`, `REJECT_FUNDS`, `REJECT_PROVIDER`, `REJECT_APPLICATION` | `PagoSimulado` |

`DecisionSolicitudPropietario` se usa en un DTO/servicio, no como atributo de entidad; por ello no pertenece al núcleo persistente de esta sección.

## 6.4 Persistencia Mongo fuera del modelo JPA

`AuditEvent` (`audit/domain/AuditEvent.java`) es un `@Document(collection="audit_events")` con ID String, tipo, actor, mensaje, mapa de datos y timestamp. Registra operaciones de autenticación, cuentas, locales, reservas/pagos, favoritos y reseñas. GridFS usa la misma conexión Mongo para imágenes y documentos; los metadatos incluyen `kind` y, para semillas, `seedKey`.

# 7. Paquetes principales y agrupaciones recomendadas

## 7.1 Backend observado

| Paquete raíz | Subpaquetes relevantes | Responsabilidad |
|---|---|---|
| `auth` | `api`, `application` | autenticación, sesión y JWT |
| `user` | `api`, `application`, `domain`, `repository` | cuentas y solicitudes de propietario |
| `venue` | `api`, `application`, `domain`, `repository` | catálogo y gestión de locales |
| `reservation` | `api`, `application`, `domain`, `repository` | disponibilidad, reservas y pago interno |
| `engagement` | `api`, `application`, `domain`, `repository` | favoritos y reseñas |
| `dashboard` | `api`, `application` | métricas y actividad |
| `audit` | `domain`, `repository` | eventos MongoDB |
| `storage` | sin subcapa | GridFS y endpoints de medios |
| `system` | `api` | salud funcional |
| `config` | sin subcapa | seguridad, persistencia e inicialización |
| `common` | `api` | errores transversales |

## 7.2 Agrupación recomendada para un futuro diagrama de paquetes

Sin cambiar código, una representación legible debería agrupar:

1. **Presentación/API:** todos los `*.api` y DTO.
2. **Aplicación:** servicios de casos de uso por módulo.
3. **Dominio transaccional:** `user.domain`, `venue.domain`, `reservation.domain`, `engagement.domain`.
4. **Persistencia:** repositorios JPA, `audit` Mongo y `storage` GridFS.
5. **Infraestructura transversal:** `config`, `common`, seguridad y migraciones.
6. **Frontend:** `core`, `features`, `layout`, `shared`.

Esta agrupación muestra dependencias reales sin convertir cada carpeta o DTO en un paquete del diagrama.

# 8. Componentes arquitectónicos detectados

| Componente candidato | Responsabilidad | Evidencia |
|---|---|---|
| SPA LojaVents | UI pública y portales por rol | `frontend/src`, rutas Angular |
| Nginx | TLS local, estáticos, fallback SPA y proxy `/api` | `nginx.conf` |
| API LojaVents | entrada REST y coordinación de casos de uso | controladores Spring |
| Autenticación/autorización | emisión/validación JWT, BCrypt, roles y cuenta activa | `auth`, `SecurityConfig`, filtro |
| Gestión de usuarios | perfil, estados y rol propietario | módulo `user` |
| Catálogo de locales | publicación, moderación, filtros y bloqueos | módulo `venue` |
| Reservas/disponibilidad | precio, solapamiento, estados y pago interno | módulo `reservation` |
| Participación | favoritos y reseñas verificadas | módulo `engagement` |
| Paneles | métricas administrativas y del propietario | módulo `dashboard` |
| Auditoría | eventos operativos documentales | módulo `audit`, MongoDB |
| Medios | imágenes/documentos en GridFS | módulo `storage` |
| PostgreSQL | dominio transaccional | entidades, repositorios, Flyway |
| MongoDB/GridFS | auditoría y binarios | repositorio Mongo, `GridFsTemplate` |

No hay evidencia de componentes independientes desplegables dentro del backend: todos los módulos Java forman el mismo proceso.

# 9. Infraestructura y despliegue detectados

## 9.1 Topología Docker verificada

| Servicio | Imagen/construcción | Red/puertos | Persistencia/dependencias |
|---|---|---|---|
| `postgres` | `postgres:17-alpine` | solo `expose 5432` en red bridge | volumen `postgres_data`; healthcheck `pg_isready` |
| `mongodb` | `mongo:8.0` | solo `expose 27017` | volumen `mongo_data`; healthcheck con `mongosh` |
| `backend` | build `./backend` | `expose 8080`, sin publicación al host | espera DB saludables; variables SQL/Mongo/JWT |
| `frontend` | build `./frontend`, Nginx | publica host 80 y 443 | monta `./certs` read-only; depende de backend iniciado |

Todos usan `restart: unless-stopped` y la red `lojavents-network` bridge. Backend y frontend tienen `init: true`, `no-new-privileges` y periodos de parada. PostgreSQL y MongoDB no se publican al host.

## 9.2 Tráfico

1. HTTP host `:80` responde salud interna o redirige a HTTPS.
2. HTTPS host `:443` termina TLS en Nginx.
3. Nginx sirve Angular y reenvía `/api/` a `backend:8080`.
4. Backend accede por DNS Compose a `postgres:5432` y `mongodb:27017`.

`/actuator/health` está permitido por Spring, pero Nginx solo reenvía `/api/` y backend no publica 8080; por tanto no es accesible desde el host mediante la topología observada. El endpoint `/api/v1/sistema/salud` sí atraviesa Nginx y consulta ambas bases.

## 9.3 Configuración y entornos

- Compose obtiene credenciales y JWT desde `.env`; `.env.example` define el contrato.
- `SPRING_PROFILES_ACTIVE=docker` se pasa al backend, pero no existe un `application-docker.yml` ni `@Profile` observado.
- Angular cuenta con un único `environment.ts`; no hay reemplazo de archivo por build.
- `proxy.conf.json` habilita desarrollo Angular fuera de Docker contra `localhost:8080`.
- La configuración HTTPS es explícitamente local: nombres localhost, certificados montados y HSTS comentado.

## 9.4 Almacenamiento y límites

- Volúmenes nombrados conservan PostgreSQL y MongoDB al recrear contenedores.
- Imágenes: PNG/JPEG/WEBP, máximo 8 MB por archivo.
- Documentos de solicitud: PDF/PNG/JPEG, máximo 5 MB.
- Nginx limita el cuerpo total a 10 MB; una carga múltiple puede alcanzar ese límite antes de los límites individuales.
- Imágenes GridFS se entregan con cache pública de 30 días; Nginx configura cache de 7 días para estáticos del frontend.

## 9.5 Producción

**No verificable.** No se detectaron manifiestos cloud/Kubernetes, dominio público, balanceador, gestor de secretos, certificado productivo, CI/CD, backup, restauración, replicación, alta disponibilidad ni configuración de escalado. Docker Compose demuestra un despliegue local/integrado, no una arquitectura productiva completa.

# 10. Funcionalidades verificadas

| Funcionalidad | Estado y reglas observadas | Evidencia principal |
|---|---|---|
| Registro cliente | crea usuario `CLIENTE`, normaliza email, cifra contraseña y devuelve JWT | `AuthApplicationService`, `/auth/registro`, pantalla registro |
| Login/sesión | valida estado y BCrypt; emite/reemite JWT | `AuthApplicationService`, `JwtService`, `AuthService` |
| Catálogo público | lista solo activos, ordena destacados/nombre, detalle y tipos | `VenueApplicationService`, rutas públicas |
| Búsqueda/filtros | backend admite texto/tipo/aforo/precio/fecha; frontend filtra lista cargada | `PublicVenueController`, `VenueService.search` |
| Disponibilidad | fecha/hora, 1..12 horas, actividad, bloqueos y reservas completadas; consulta días adyacentes | `ReservationAvailabilityService` |
| Reserva | crea borrador, exige políticas, valida capacidad/disponibilidad y calcula subtotal + 8 % | `ReservationApplicationService`, wizard |
| Pago | resultado interno determinista, referencia única, nueva validación bajo lock; UI solo aprueba | `PagoSimulado`, endpoint y wizard |
| Mis reservas | lista reservas del JWT | `/reservas/mias`, pantalla cliente |
| Reservas recibidas | lista por propietario de local | `/propietario/reservas`, pantalla propietario |
| Favoritos | IDs/listado y alta/baja; solo locales activos | `FavoriteApplicationService`, vistas públicas/cliente |
| Reseñas | lectura pública; creación única para reserva propia completada y pasada; recalcula promedio | `ReviewApplicationService`, detalle/mis reservas |
| Perfil | actualiza nombre/teléfono, cambia contraseña y baja lógica | `UserAccountApplicationService`, `/perfil` |
| Solicitud propietario | multipart con identificación, notas y documento; impide duplicado pendiente | módulo `user`, GridFS, vista cliente |
| Revisión propietario | admin filtra, abre documento, aprueba/rechaza; aprobación añade rol | endpoints/vista de verificaciones |
| Gestión usuarios | admin lista y cambia estado; protege cuentas admin y cambio propio | `AdminUserController`, servicio, vista admin |
| Gestión locales propietario | lista, detalle, crea/edita, solicita revisión, desactiva, carga imágenes | módulo `venue`, vistas propietario |
| Moderación locales | admin lista todos y aprueba/desactiva | `AdminVenueController`, vista admin |
| Bloqueos | propietario agrega/elimina rangos sin superposición | `VenueApplicationService`, disponibilidad UI |
| Panel propietario | totales, ingresos, métricas mensuales/por local y próximas reservas | `DashboardApplicationService`, vista |
| Panel admin | usuarios, locales, reservas, ingresos/tarifas, reseñas, solicitudes y actividad | `DashboardApplicationService`, vista |
| Compartir | Web Share o copia al portapapeles | `ShareService`, detalle de local |
| Notificaciones UI | toasts temporales locales | `NotificationService`, `ToastContainer` |
| Datos iniciales | crea cuentas, 50 locales, imágenes GridFS y una reserva reseñable si faltan | dos `CommandLineRunner` sin perfil |
| Prueba de humo | redirección, HTTPS, SPA, salud, login y `/auth/me` | `scripts/probar-sistema.ps1` |

## Funcionalidades parciales o no conectadas completamente

- Los modos de rechazo de pago están implementados en API, pero la UI activa solo ofrece aprobación.
- `/api/v1/locales/tipos-evento` está implementado, pero la UI deriva tipos de la lista cargada.
- El filtro de fecha del catálogo solo excluye cualquier fecha con bloqueo, sin considerar hora/duración ni reservas confirmadas; el chequeo completo ocurre en disponibilidad.
- `CANCELADA` está modelado y contado, pero no existe flujo de cancelación.
- `MockStoreService` y datos mock no participan en el flujo activo.

# 11. Aspectos no funcionales observables

## Seguridad e integridad

**Implementado:** JWT firmado, expiración/emisor, BCrypt, roles en servidor, comprobación de cuenta activa, CORS restringido, TLS local, cabeceras, límites de archivo, Bean Validation, constraints SQL, Flyway, lock pesimista y versión optimista.

**Riesgos observables:** JWT en `localStorage`; secreto de desarrollo por defecto en `application.yml`; cuentas/credenciales fijas de semilla; inicializadores sin perfil; documentos aceptados por MIME declarado sin escaneo de contenido/antivirus; ausencia de rate limiting, bloqueo por intentos y revocación de tokens.

## Rendimiento y escalabilidad

- Índices SQL cubren email, estados, propietario, fechas y reservas.
- `@EntityGraph` reduce cargas perezosas en consultas relevantes.
- Estáticos comprimidos/cacheados; frontend usa hash de salida y presupuestos de bundle.
- **Límite:** catálogo, paneles y varias listas no tienen paginación; filtros y agregaciones se realizan en memoria. Los inicializadores llaman repetidamente `findAll()` al poblar el catálogo.
- El filtro de cuenta activa implica una consulta PostgreSQL por petición autenticada.
- No se detectó cache de aplicación, CDN, procesamiento asíncrono ni escalado horizontal validado.

## Disponibilidad y portabilidad

- Healthchecks de bases y Nginx, reinicio automático, graceful shutdown y volúmenes persistentes.
- Versiones de base declaradas para dependencias y contenedores, además de `package-lock.json` para npm. No hay digests inmutables de imágenes ni lockfile Maven completo, por lo que la reproducibilidad exacta no está garantizada.
- `frontend` espera `service_started`, no salud de backend; además Compose no define healthcheck de backend.
- Configuración CORS/TLS está ligada a localhost. La portabilidad productiva no está demostrada.

## Mantenibilidad

- Modularidad funcional, DTO con validación, TypeScript/Angular strict y migraciones versionadas.
- Acoplamiento entre módulos por entidades y DTO; `storage` y `config` tienen responsabilidades agrupadas sin subcapas.
- Dos árboles Angular y código mock no usado aumentan ambigüedad.
- Strings de catálogo representan conceptos cerrados sin tipo de dominio persistente.

## Pruebas

- Backend: un test vacío (`pruebaBasica`) sin cargar contexto ni afirmar comportamiento.
- Frontend: un test que solo comprueba creación del shell.
- Script PowerShell de humo: cubre infraestructura y autenticación básica, pero no es una suite automatizada conectada a CI observada.
- No se ejecutaron tests/builds en esta etapa para no producir artefactos fuera del único archivo autorizado; el estado de ejecución actual no se afirma.

## Observabilidad y errores

- Actuator expone `health,info`; detalle de salud oculto.
- Endpoint funcional de salud cuenta usuarios PostgreSQL y eventos Mongo.
- Auditoría Mongo para operaciones principales.
- Niveles de seguridad Spring en INFO y errores HTTP sin stack trace.
- No se detectaron métricas exportadas, tracing, correlación de requests, logging estructurado, alertas ni retención de auditoría.

## Copias de seguridad

**No verificable.** Los volúmenes aportan persistencia local, pero no equivalen a backup. No se hallaron scripts, políticas, pruebas de restauración ni retención.

# 12. Integraciones y dependencias externas

## En tiempo de ejecución

- PostgreSQL y MongoDB son servicios externos al proceso Java, aunque forman parte del Compose del sistema.
- El navegador usa Web Share API y Clipboard API cuando están disponibles.
- No se detectaron llamadas HTTP salientes desde backend ni frontend hacia servicios de terceros.
- No existe integración real con una pasarela de pago; el flujo es interno.

## En construcción/distribución

- Maven Central o repositorios Maven configurados por defecto para dependencias Java.
- Registro npm para Angular/RxJS/herramientas.
- Registro de imágenes para Maven, Temurin, Node, Nginx, PostgreSQL y MongoDB.

La disponibilidad, autenticación y políticas de esos registros no están definidas en el repositorio.

# 13. Elementos ambiguos o no verificables

1. **Frontend canónico:** la evidencia de build favorece `frontend/src`, pero no se conoce la intención del árbol anidado.
2. **Perfil `docker`:** se activa, pero no tiene configuración específica observada.
3. **Bandera `production`:** el build optimizado conserva `environment.production=false`.
4. **Inicialización:** no hay switch/perfil para desactivar semillas; no se conoce si esto es intencional en producción.
5. **Pago:** no se conoce si el diseño final seguirá siendo interno o migrará a una pasarela.
6. **Cancelación:** existe estado/política, pero no operación de negocio.
7. **Ventana nocturna:** el código permite `requestedEnd` hasta 36 horas relativas aunque el mensaje habla de no terminar después de medianoche.
8. **Filtro por fecha:** la búsqueda considera bloqueos diarios completos, distinto del chequeo horario real.
9. **Consistencia SQL/Mongo:** no se observó transacción distribuida ni política de reintento/compensación de auditoría.
10. **MongoDB:** el nombre por defecto `lojavents_logs` sugiere logs, pero también aloja GridFS.
11. **Actuator:** permitido por seguridad, pero no expuesto por el proxy del despliegue observado.
12. **TLS:** existen archivos en `certs`, pero no se verificó emisor, expiración, confianza ni rotación.
13. **Datos existentes:** el código de semillas es verificable; la cantidad/estado actual de registros depende de los volúmenes y no se inspeccionó.
14. **Producción/operación:** no hay evidencia suficiente de hosting, CI/CD, backups, SLO, monitoreo o recuperación.
15. **Codificación visualizada:** PowerShell mostró mojibake en algunas lecturas, mientras `rg` recuperó tildes correctamente; no se concluye corrupción de archivos sin una prueba binaria/ejecución específica.

# 14. Riesgos para la actualización documental

- Documentar el frontend anidado como activo duplicaría clases, rutas y servicios que el build principal no compila.
- Convertir nombres de carpetas en una arquitectura ideal ocultaría los acoplamientos reales entre módulos.
- Incluir `AuditEvent` en el diagrama JPA incumpliría el alcance: es documento Mongo, no entidad.
- Representar `DecisionSolicitudPropietario` como estado persistido sería incorrecto; solo es una decisión de entrada.
- Presentar PayPal/cobro real, cancelación, reembolso, notificaciones remotas, correo o backup como implementados inventaría capacidades.
- Tratar los 50 locales/cuentas como datos garantizados confundiría lógica de semilla con estado persistente actual.
- Omitir `pendienteRevision` perdería la moderación introducida por V8 y los servicios actuales.
- Usar solo anotaciones JPA omitiría constraints importantes definidos por Flyway; usar solo V1 omitiría la evolución hasta V9.
- Dibujar SQL y Mongo como una única unidad transaccional sugeriría garantías no implementadas.
- Presentar el Docker Compose local como producción escondería dependencias de localhost, certificados y ausencia de operación productiva.
- La ausencia de Git impide fechar versiones o identificar formalmente código antiguo por historial; la clasificación se basa en referencias activas de build.

# 15. Preguntas que requieren confirmación humana

1. ¿Se confirma `frontend/src` como único frontend canónico y puede clasificarse `frontend/lojavents-frontend` como copia obsoleta?
2. ¿Los dos inicializadores de demostración deben ejecutarse en todos los entornos o solo en desarrollo/demostración?
3. ¿El procesamiento interno de pago es el alcance definitivo o se prevé una pasarela externa?
4. ¿Debe existir cancelación/reembolso de reservas y qué transiciones/reglas debe aplicar?
5. ¿La ventana de reserva puede cruzar medianoche? Si sí, ¿hasta qué hora del día siguiente?
6. ¿El filtro de fecha del catálogo debe considerar horario/duración y reservas, o basta excluir días bloqueados?
7. ¿Es intencional despublicar un local activo inmediatamente al editarlo y exigir nueva aprobación?
8. ¿MongoDB debe alojar conjuntamente auditoría, documentos e imágenes, y qué políticas de retención corresponden a cada tipo?
9. ¿Cuál es el objetivo de despliegue productivo (dominio, plataforma, certificados, secretos y escalado)?
10. ¿Qué objetivos de backup/restauración y retención se requieren para PostgreSQL, MongoDB y GridFS?
11. ¿Qué nivel de observabilidad, auditoría, privacidad y acceso a documentos de identidad es obligatorio?
12. ¿Debe `/actuator/health` exponerse al proxy/monitor o permanecer solo en la red interna?
13. ¿La renovación de sesión debe evolucionar a refresh token revocable o se mantiene la reemisión con JWT vigente?
14. ¿Qué paginación/límites se esperan para catálogos, usuarios, reservas, solicitudes y paneles?
15. ¿Deben conservarse la columna histórica `usuarios.activo`, el código mock y los modos de rechazo no visibles en UI?

# 16. Archivos y rutas utilizados como evidencia

## Construcción y despliegue

- `docker-compose.yml`
- `.env.example` (se confirmó la existencia de `.env`, sin leer sus secretos)
- `.gitignore`
- `backend/pom.xml`
- `backend/Dockerfile`
- `backend/src/main/resources/application.yml`
- `frontend/package.json`, `frontend/package-lock.json`
- `frontend/angular.json`, `frontend/tsconfig*.json`
- `frontend/Dockerfile`, `frontend/nginx.conf`, `frontend/proxy.conf.json`
- `frontend/src/environments/environment.ts`
- `scripts/probar-sistema.ps1`

## Backend

- `backend/src/main/java/ec/edu/unl/lojavents/**`
- En particular: controladores `*/api/*Controller.java`, servicios `*/application/*Service.java`, entidades `*/domain/*.java`, repositorios `*/repository/*.java`.
- `config/SecurityConfig.java`, `ActiveAccountFilter.java`, `PersistenceConfig.java`, `DemoDataInitializer.java`, `CatalogDemoInitializer.java`.
- `common/api/GlobalExceptionHandler.java`, `ApiException.java`.
- `storage/MediaStorageService.java`, `MediaController.java`, `OwnerMediaController.java`.
- `backend/src/main/resources/db/migration/V1__crear_usuarios.sql` hasta `V9__normalizar_tipos_evento.sql`.
- `backend/src/test/java/ec/edu/unl/lojavents/LojaVentsApplicationTests.java`.

## Frontend

- `frontend/src/main.ts`
- `frontend/src/app/app.config.ts`, `app.routes.ts`, `app.ts`
- `frontend/src/app/core/guards/**`, `core/interceptors/**`, `core/services/**`, `core/data/**`
- `frontend/src/app/features/**`
- `frontend/src/app/layout/**`
- `frontend/src/app/shared/components/**`, `shared/models/**`
- `frontend/src/app/app.spec.ts`
- `frontend/lojavents-frontend/angular.json` y su estructura, únicamente como evidencia de duplicación; no como fuente de comportamiento activo.

Los README, ADR, diagramas y documentos previos encontrados en la raíz o `docs/` no fueron utilizados para afirmar la implementación actual.
