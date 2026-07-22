# 1. Propósito y alcance

Este documento justifica el contenido de `05-diagrama-componentes.puml`. El diagrama presenta los componentes de alto nivel implementados en LojaVents y conserva la arquitectura Cliente-Servidor observada: una SPA Angular, Nginx como servidor web y proxy, una API Spring Boot organizada como monolito modular y dos responsabilidades de persistencia diferenciadas.

El alcance es exclusivamente lógico. No se representan contenedores Docker, redes, puertos, volúmenes, hosts, nodos físicos ni artefactos desplegables; esos elementos corresponden al diagrama de despliegue. Tampoco se muestran paquetes, clases, interfaces, controladores, servicios, repositorios, DTO, entidades, métodos o pantallas individuales.

# 2. Componentes representados

El diagrama contiene **12 componentes visibles** distribuidos en tres agrupaciones y un componente técnico intermedio:

| Agrupación o posición | Componente |
|---|---|
| Frontend | SPA Angular |
| Entre frontend y backend | Nginx |
| Backend LojaVents | API REST |
| Backend LojaVents | Autenticación y usuarios |
| Backend LojaVents | Locales y disponibilidad |
| Backend LojaVents | Reservas y pago simulado |
| Backend LojaVents | Favoritos y reseñas |
| Backend LojaVents | Paneles |
| Backend LojaVents | Auditoría |
| Backend LojaVents | Medios |
| Persistencia | PostgreSQL |
| Persistencia | MongoDB / GridFS |

Las ocho áreas internas están encerradas en una única agrupación `Backend LojaVents`. Son componentes lógicos de un mismo backend Spring Boot, no aplicaciones autónomas.

# 3. Responsabilidad de cada componente

| Componente | Responsabilidad actual resumida |
|---|---|
| SPA Angular | Interfaz web pública y autenticada para clientes, propietarios y administradores. Consume la API mediante rutas relativas bajo `/api/v1`. |
| Nginx | Sirve los archivos construidos de la SPA, aplica el fallback de navegación y reenvía las solicitudes `/api/` al backend. |
| API REST | Frontera HTTP del backend. Recibe solicitudes y coordina su entrada hacia las áreas internas correspondientes. |
| Autenticación y usuarios | Registro, inicio de sesión, JWT, roles, perfil, estados de cuenta y solicitudes/revisión del rol propietario. |
| Locales y disponibilidad | Catálogo, detalle, administración y moderación de locales, bloqueos y comprobación temporal de disponibilidad. |
| Reservas y pago simulado | Creación y consulta de reservas, validaciones temporales, cálculo económico, estados y procesamiento interno simulado. No realiza cobros financieros reales. |
| Favoritos y reseñas | Gestión de favoritos, reseñas verificadas y calificaciones asociadas a usuarios, locales y reservas. |
| Paneles | Indicadores administrativos y del propietario, agregados y actividad reciente. |
| Auditoría | Registro y consulta de eventos operativos almacenados en MongoDB. |
| Medios | Validación, almacenamiento y lectura de imágenes de locales y documentos de solicitudes mediante GridFS. |
| PostgreSQL | Persistencia transaccional del dominio mediante JPA y esquema versionado con Flyway. |
| MongoDB / GridFS | MongoDB conserva eventos de auditoría; GridFS conserva imágenes y documentos binarios. |

El componente `Reservas y pago simulado` refleja literalmente la implementación vigente. No existe integración activa con PayPal, una pasarela externa, tarjetas, transferencias, facturación ni un proveedor financiero.

# 4. Dependencias principales representadas

El diagrama contiene **18 dependencias visibles**, todas sin frases explicativas sobre las flechas:

1. `SPA Angular` depende de `Nginx` como punto de acceso web.
2. `Nginx` reenvía el consumo de API hacia `API REST`.
3. `API REST` dirige operaciones hacia `Autenticación y usuarios`.
4. `API REST` dirige operaciones hacia `Locales y disponibilidad`.
5. `API REST` dirige operaciones hacia `Reservas y pago simulado`.
6. `API REST` dirige operaciones hacia `Favoritos y reseñas`.
7. `API REST` dirige operaciones hacia `Paneles`.
8. `Reservas y pago simulado` utiliza `Locales y disponibilidad` para validar el local y su disponibilidad.
9. `Favoritos y reseñas` utiliza `Reservas y pago simulado` para verificar condiciones de reseña.
10. `Paneles` utiliza `Auditoría` para presentar actividad reciente.
11. `Autenticación y usuarios` utiliza `Medios` para documentos de solicitudes de propietario.
12. `Locales y disponibilidad` utiliza `Medios` para imágenes de locales.
13. `Autenticación y usuarios` persiste su dominio en PostgreSQL.
14. `Locales y disponibilidad` persiste su dominio en PostgreSQL.
15. `Reservas y pago simulado` persiste su dominio en PostgreSQL.
16. `Favoritos y reseñas` persiste su dominio en PostgreSQL.
17. `Auditoría` persiste eventos en MongoDB.
18. `Medios` almacena binarios mediante GridFS.

Las dependencias entre componentes internos representan invocaciones o imports dentro del mismo proceso Java. No representan HTTP interno, comunicación de red ni despliegues independientes.

# 5. Dependencias omitidas por legibilidad

Para evitar cruces y saturación se omitieron del dibujo las siguientes relaciones verificadas:

- `Locales y disponibilidad` también utiliza `Autenticación y usuarios` para identificar propietarios y administradores.
- `Reservas y pago simulado` también utiliza `Autenticación y usuarios` para identificar clientes.
- `Favoritos y reseñas` también depende de `Autenticación y usuarios` y `Locales y disponibilidad`.
- `Paneles` también agrega datos de `Autenticación y usuarios`, `Locales y disponibilidad`, `Reservas y pago simulado` y `Favoritos y reseñas`.
- Autenticación, usuarios, locales, reservas y participación registran eventos mediante `Auditoría`; el dibujo conserva únicamente la relación de paneles con auditoría para evitar múltiples flechas convergentes.
- La API también expone operaciones relacionadas con medios, auditoría administrativa y salud; se omitieron esas flechas secundarias.
- Se omitieron dependencias hacia manejo común de errores, seguridad/configuración y capacidades internas que no constituyen componentes visibles de este nivel.

Las omisiones son únicamente decisiones gráficas. No significan que las relaciones hayan sido eliminadas del sistema.

# 6. Cambios frente al diagrama anterior

1. Se conserva la separación general entre frontend, backend y persistencia, además de la API REST.
2. `Frontend/Vistas` se reemplaza por la SPA Angular activa y se incorpora Nginx como componente técnico real.
3. Los componentes internos quedan encerrados en un único `Backend LojaVents`, evitando que parezcan servicios desplegables por separado.
4. Las interfaces `IUsuarioService`, `ILocalService`, `IReservaService`, `IPagoService`, `IDataAccess`, `ITransactionData`, `INonStructuredData` e `IPaymentGateway` se eliminan porque no representan interfaces equivalentes implementadas.
5. “Gestión de Pagos” se integra en `Reservas y pago simulado`; se eliminan la pasarela externa y PayPal.
6. Se incorpora autenticación y autorización dentro de `Autenticación y usuarios`.
7. “Gestión de Locales” se amplía conceptualmente con disponibilidad y bloqueos.
8. Se añaden las áreas activas `Favoritos y reseñas`, `Paneles`, `Auditoría` y `Medios`.
9. La persistencia genérica se sustituye por PostgreSQL para el dominio transaccional y MongoDB/GridFS para auditoría y binarios.
10. Se eliminan listas de responsabilidades e interfaces visibles dentro del diagrama para conservar una presentación académica y legible.

# 7. Diferencia entre componente y microservicio

Los componentes internos del diagrama son agrupaciones lógicas de responsabilidades dentro del código. Todos forman parte del mismo proyecto backend, se compilan en el mismo artefacto Spring Boot, comparten proceso de ejecución y se despliegan conjuntamente.

Por ello, las flechas internas no representan protocolos remotos ni comunicación entre servicios distribuidos. Un microservicio tendría ciclo de despliegue, proceso, frontera de datos y contrato de red propios; esas características no existen para los módulos internos actuales de LojaVents. El rectángulo común `Backend LojaVents` deja visible esta diferencia.

# 8. Evidencias y rutas consultadas

## Frontend y Nginx

- `frontend/src/**`
- `frontend/src/environments/environment.ts`
- `frontend/src/app/core/services/**`
- `frontend/nginx.conf`
- `frontend/angular.json`
- `frontend/Dockerfile`

La configuración Angular utiliza `/api/v1`; Nginx sirve el directorio de la SPA, aplica fallback a `index.html` y reenvía `/api/` hacia el backend.

## Backend y API

- `backend/src/main/java/ec/edu/unl/lojavents/auth/**`
- `backend/src/main/java/ec/edu/unl/lojavents/user/**`
- `backend/src/main/java/ec/edu/unl/lojavents/venue/**`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/**`
- `backend/src/main/java/ec/edu/unl/lojavents/engagement/**`
- `backend/src/main/java/ec/edu/unl/lojavents/dashboard/**`
- `backend/src/main/java/ec/edu/unl/lojavents/audit/**`
- `backend/src/main/java/ec/edu/unl/lojavents/storage/**`
- `backend/src/main/java/ec/edu/unl/lojavents/system/**`
- `backend/src/main/java/ec/edu/unl/lojavents/config/**`
- `backend/src/main/java/ec/edu/unl/lojavents/common/**`

Se revisaron controladores REST, servicios de aplicación, imports entre módulos y la configuración de seguridad para delimitar la frontera API y las dependencias internas.

## Persistencia y pago

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/**`
- `backend/pom.xml`
- `backend/src/main/java/ec/edu/unl/lojavents/*/repository/**`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/PagoSimulado.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/application/ReservationApplicationService.java`
- `backend/src/main/java/ec/edu/unl/lojavents/audit/domain/AuditEvent.java`
- `backend/src/main/java/ec/edu/unl/lojavents/audit/repository/AuditEventRepository.java`
- `backend/src/main/java/ec/edu/unl/lojavents/storage/MediaStorageService.java`

La evidencia muestra repositorios JPA para el dominio transaccional, `MongoRepository` para auditoría, `GridFsTemplate` para medios y una entidad `PagoSimulado` procesada internamente.

## Documentación de contraste

- `docs/actualizacion/01-analisis-arquitectura-actual.md`
- `docs/actualizacion/02-matriz-trazabilidad-y-cambios.md`
- `docs/actualizacion/03-requisitos-software-actualizados.md`
- `docs/actualizacion/04-diagrama-paquetes.puml`
- `docs/actualizacion/04-notas-diagrama-paquetes.md`
- `Diagrama de Componentes.jpeg`
- `ADR.pdf`
- `Requisitos_lojaVents.pdf`

Los documentos anteriores aportaron contexto y trazabilidad; el código y la configuración activa determinaron la estructura final. El diagrama de componentes no contiene información de despliegue y no anticipa una arquitectura futura.
