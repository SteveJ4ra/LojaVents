# Matriz de trazabilidad y cambios documentales de LojaVents

**Fecha:** 2026-07-20  
**Etapa:** segunda - comparación entre implementación actual y documentación anterior.  
**Alcance:** ADR, RF-001 a RF-018, RNF-001 a RNF-005 y diagramas anteriores de clases, componentes, despliegue y paquetes.  
**Restricción aplicada:** este archivo no actualiza requisitos, no modifica el ADR, no genera UML/PlantUML y no reemplaza diagramas.

## Convenciones

- **IMPLEMENTADO:** el flujo esperado existe con evidencia conectada de backend y frontend cuando ambos son necesarios.
- **IMPLEMENTADO PARCIALMENTE:** existe una parte útil del flujo, pero falta alcance o una regla relevante.
- **IMPLEMENTADO DE FORMA DIFERENTE:** el objetivo existe, pero el diseño o mecanismo difiere de la línea base.
- **IMPLEMENTADO PERO NO DOCUMENTADO:** existe activamente en código y no está expresado con claridad en RF/RNF anteriores.
- **DOCUMENTADO PERO NO IMPLEMENTADO:** aparece en documentos anteriores sin flujo activo equivalente.
- **OBSOLETO:** elemento anterior reemplazado por otro diseño.
- **CONTRADICTORIO:** dos fuentes sostienen afirmaciones incompatibles.
- **NO VERIFICABLE:** hay configuración o intención, pero no evidencia suficiente de cumplimiento.
- **DECISIÓN HUMANA PENDIENTE:** la fuente de verdad técnica no permite decidir intención de producto/documentación.

La jerarquía aplicada fue: código/configuración/migraciones/infraestructura activa; inventario de etapa 1; ADR aceptado; requisitos y diagramas anteriores. El ADR se evalúa, pero no se propone modificarlo.

# 1. Resumen ejecutivo de la comparación

La decisión principal del ADR, arquitectura cliente-servidor con API, DTO, servicios y repositorios, se cumple en términos generales. La implementación es un monolito modular: Angular/Nginx como cliente web, una API Spring Boot y persistencia dual en PostgreSQL y MongoDB/GridFS. No es un conjunto de microservicios.

De los 18 requisitos funcionales anteriores:

- 12 se clasifican como **IMPLEMENTADO**: RF-001, RF-004, RF-005, RF-006, RF-007, RF-010, RF-011, RF-013, RF-015, RF-016, RF-017 y RF-018.
- 3 se clasifican como **IMPLEMENTADO DE FORMA DIFERENTE**: RF-002, RF-012 y RF-014.
- 3 se clasifican como **IMPLEMENTADO PARCIALMENTE**: RF-003, RF-008 y RF-009.

De los cinco RNF, solo RNF-005 tiene cumplimiento técnico directamente verificable. RNF-001 a RNF-004 describen cualidades o métricas sin pruebas suficientes. Hay mecanismos relacionados -interfaz implementada, healthchecks, transacciones e índices-, pero no demuestran usabilidad, compatibilidad entre navegadores, 99 % de disponibilidad, respuesta menor a tres segundos ni capacidad de 999 reservas.

Contradicciones de mayor impacto:

1. ADR y diagramas incluyen pago/facturación; el código solo persiste `PagoSimulado` y no existe `Factura`.
2. El despliegue anterior incluye PayPal; no hay SDK, llamada HTTP ni pasarela externa activa.
3. El diagrama de clases usa herencia `Usuario -> Cliente/Administrador`; el código usa una sola entidad `Usuario` con colección de roles.
4. El diagrama de disponibilidad modela estados y una entidad genérica; el código solo persiste bloqueos y calcula disponibilidad desde bloqueos/reservas.
5. El diagrama de paquetes muestra capas y clases individuales; el backend real se organiza principalmente por módulos funcionales.
6. La base no relacional anterior carece de responsabilidad explícita; MongoDB actual almacena auditoría y GridFS.

Antes de producir documentación definitiva deben resolverse al menos: destino del pago y facturación, cancelaciones/reembolsos, frontend canónico, semántica temporal de disponibilidad/filtros, política de revisión de locales, alcance Mongo/GridFS y topología productiva.

# 2. Cumplimiento y desviaciones del ADR

| Tema del ADR | Decisión/afirmación anterior | Evidencia actual | Cumplimiento | Desviación | Impacto documental | Rutas relevantes |
|---|---|---|---|---|---|---|
| Cliente-servidor | Cliente presenta UI; servidor procesa reglas/datos | Angular separado de API Spring Boot; Nginx actúa como frontera web | IMPLEMENTADO | Ninguna estructural | Conservar decisión; futuros diagramas deben mostrar SPA, proxy y backend | `frontend/src`, `backend/src/main/java`, `docker-compose.yml` |
| Separación cliente/servidor | Cliente centrado en presentación y servidor en negocio | Reglas críticas se revalidan en servicios; cliente también filtra catálogo y valida formularios | IMPLEMENTADO PARCIALMENTE | Búsqueda/filtros se duplican y el frontend no consume los filtros del endpoint | Documentar servidor como autoridad; registrar filtrado local como implementación actual | `VenueService.search`, `VenueApplicationService.search` |
| Comunicación mediante API | Solicitudes/respuestas por API | REST JSON bajo `/api/v1`; Nginx reenvía `/api/` | IMPLEMENTADO | Desarrollo usa proxy local; producción no verificable | Conservar API REST y añadir Nginx/proxy | `*Controller.java`, `nginx.conf`, `proxy.conf.json` |
| DTO | No exponer entidades directamente | Records de request/response; controllers retornan DTO/colecciones, no entidades JPA | IMPLEMENTADO | Algunos DTO se comparten entre módulos Java | Mantener patrón DTO; no dibujar DTO en diagrama de dominio | `*/api/dto/*.java` |
| Patrón Servicio | Organizar lógica por autenticación, clientes, locales, reservas, pagos, reseñas, disponibilidad y admin | Servicios de aplicación por módulo funcional | IMPLEMENTADO | No hay interfaces de servicio como las dibujadas; pago está dentro de `reservation` | Componentes futuros deben representar responsabilidades, no interfaces inexistentes | `*/application/*Service.java` |
| Patrón Repositorio | Abstraer persistencia | Repositorios Spring Data JPA/Mongo consumidos por servicios | IMPLEMENTADO | Son interfaces Spring Data concretas; no hay puertos propios independientes del framework | Representar persistencia Spring Data, sin prometer independencia tecnológica total | `*/repository/*.java`, `PersistenceConfig.java` |
| Centralización de reglas | Servidor valida negocio y consistencia | Capacidad, disponibilidad, propiedad, estados, pagos y reseñas se validan en backend | IMPLEMENTADO PARCIALMENTE | Parte del filtrado y controles de UX vive también en Angular; el filtro de fecha difiere del chequeo real | Distinguir validación UX de reglas autoritativas; armonizar en futura especificación | servicios backend y `frontend/src/app/core/services` |
| Autenticación y seguridad | Servidor gestiona autenticación y protege datos | JWT HS256, BCrypt, roles, cuenta activa, CORS, TLS local, guards/interceptor | IMPLEMENTADO | Logout no revoca token; token vive en `localStorage`; no hay rate limit | Añadir seguridad técnica y límites observados a RNF; no afirmar seguridad absoluta | `SecurityConfig.java`, `JwtService.java`, `AuthService`, `auth.interceptor.ts` |
| Persistencia | Servidor se comunica con base de datos; cliente no accede directamente | PostgreSQL para dominio; MongoDB para auditoría y GridFS; cliente solo usa API | IMPLEMENTADO DE FORMA DIFERENTE | ADR habla de “la base de datos” sin separar responsabilidades duales | Componentes/despliegue deben mostrar ambos motores y responsabilidades | `application.yml`, `PersistenceConfig.java`, `MediaStorageService.java` |
| Pagos | Servidor registra/gestiona pagos | `PagoSimulado`, endpoint interno, resultado determinista y persistencia SQL | IMPLEMENTADO DE FORMA DIFERENTE; DECISIÓN HUMANA PENDIENTE | No hay cobro real ni gateway; UI solo usa aprobación | Requisitos, dominio, componentes y despliegue dependen de decidir el alcance final | módulo `reservation`, V3, `PaymentService` |
| Facturación | Servidor genera facturas | Solo se capturan campos de dirección y montos dentro de `Reserva` | DOCUMENTADO PERO NO IMPLEMENTADO | No hay entidad, servicio, endpoint, numeración ni documento fiscal | No incluir `Factura` en diagramas actuales; decidir si será requisito futuro | `Reserva.java`, `CreateReservationRequest.java`; ausencia de módulo factura |
| Reseñas | Servidor registra/controla reseñas | Reseña única, propia, completada y posterior al evento; promedio recalculado | IMPLEMENTADO | Regla actual es más estricta que el ADR | Requisitos futuros deben incorporar fecha pasada, unicidad y recalculo | módulo `engagement` |
| Propietarios | Gestionar propietarios y disponibilidad | Solicitud formal, documento, revisión admin, rol; locales y bloqueos protegidos por rol | IMPLEMENTADO DE FORMA DIFERENTE | Propietario no es subclase: es rol adicional de `Usuario` | Actualizar clases, paquetes y actores; conservar intención del ADR | módulos `user`, `venue`, `SecurityConfig.java` |
| Administración | Acciones administrativas según rol | Panel, estados de cuenta, moderación de locales y revisión de propietarios | IMPLEMENTADO PARCIALMENTE | “Administrar” no equivale a CRUD total; no hay edición/eliminación administrativa general | Precisar alcance en RF-008/RF-009 y componentes | `admin/**`, controladores `/api/v1/admin/**` |
| Monolito frente a distribuido | Se descartan múltiples servicios independientes | Un solo JAR y contenedor backend con módulos internos | IMPLEMENTADO | Diagramas anteriores pueden sugerir componentes independientes, pero no procesos separados | Evitar representar módulos Java como microservicios | `backend/Dockerfile`, `docker-compose.yml` |

# 3. Matriz de trazabilidad de requisitos funcionales

## 3.1 Resumen de clasificación

| ID | Redacción resumida | Actor anterior | Clasificación principal |
|---|---|---|---|
| RF-001 | Iniciar sesión con credenciales guardadas | Usuario | IMPLEMENTADO |
| RF-002 | Cerrar sesión | Usuario | IMPLEMENTADO DE FORMA DIFERENTE |
| RF-003 | Buscar/filtrar por tipo y fecha | Visitante/cliente | IMPLEMENTADO PARCIALMENTE |
| RF-004 | Pantalla de bienvenida/información general | Visitante/cliente | IMPLEMENTADO |
| RF-005 | Resultados con previsualización | Visitante/cliente | IMPLEMENTADO |
| RF-006 | Información adicional del local | Visitante/cliente | IMPLEMENTADO |
| RF-007 | Registro con email, contraseña, nombres y teléfono | Visitante | IMPLEMENTADO |
| RF-008 | Administrar cuentas de clientes | Administrador | IMPLEMENTADO PARCIALMENTE |
| RF-009 | Administrar lista de locales | Administrador | IMPLEMENTADO PARCIALMENTE |
| RF-010 | Registrarse como propietario mediante verificación | Cliente | IMPLEMENTADO |
| RF-011 | Historial de reservas activas y pasadas | Cliente | IMPLEMENTADO |
| RF-012 | Reserva directa e inmediata en horario disponible | Cliente | IMPLEMENTADO DE FORMA DIFERENTE |
| RF-013 | Gestionar información del local | Cliente propietario | IMPLEMENTADO |
| RF-014 | Marcar fechas/rangos no disponibles | “Cliente” propietario | IMPLEMENTADO DE FORMA DIFERENTE |
| RF-015 | Calificar/reseñar tras completar reserva | Cliente | IMPLEMENTADO |
| RF-016 | Compartir perfil mediante URL | Visitante/cliente | IMPLEMENTADO |
| RF-017 | Agregar/remover y gestionar favoritos | Cliente | IMPLEMENTADO |
| RF-018 | Editar perfil y dar de baja cuenta | Cliente | IMPLEMENTADO |

## 3.2 Trazabilidad detallada RF-001 a RF-006

| ID | Evidencia backend | Evidencia frontend | Reglas encontradas | Diferencias, ausencia o parcialidad | Recomendación para actualización futura | Pregunta pendiente |
|---|---|---|---|---|---|---|
| RF-001 | `POST /api/v1/auth/login`; normaliza email, valida BCrypt/estado y emite JWT | `/login`, `AuthService.login`, interceptor y almacenamiento de sesión | Cuenta debe existir y estar `ACTIVO`; error genérico para credenciales | Añade JWT, roles, expiración y filtro de cuenta activa no descritos | Mantener RF y añadir reglas de estado, token y errores sin revelar credencial incorrecta | ¿Se mantiene JWT en `localStorage` y reemisión con token vigente? |
| RF-002 | No existe endpoint de logout, lista de revocación ni invalidación | Navbar llama `AuthService.logout`, elimina token/usuario y navega a `/` | Cierre solo del estado local | Cumple experiencia visible, pero el JWT copiado sigue válido hasta expirar | Redactar como cierre de sesión cliente o añadir requisito de revocación si se exige cierre servidor | ¿El cierre debe revocar inmediatamente el token? |
| RF-003 | `GET /locales` admite `text`, `eventType`, `attendees`, `maxPrice`, `date` | Home y explorar ofrecen tipo, fecha, asistentes, texto y precio; `VenueService.search` filtra en memoria | Solo locales activos; tipo exacto normalizado; capacidad/precio; fecha excluye cualquier día bloqueado | UI no consume filtros backend. Fecha no considera hora/duración ni reservas confirmadas; no equivale a disponibilidad real | Separar “búsqueda de catálogo” de “verificación de franja”; definir semántica de fecha | ¿Fecha debe comprobar bloqueos completos, una franja o disponibilidad real? |
| RF-004 | Catálogo público soporta datos mostrados | `/` contiene hero, búsqueda, destacados y explicación en cuatro pasos | Acceso público | La pantalla es una experiencia operativa y no solo texto general | Mantener RF orientado a inicio/catálogo, evitando lenguaje de landing genérica | Ninguna bloqueante |
| RF-005 | `VenueResponse` ofrece resumen, precio, capacidad, imágenes y calificación | `VenueCard` se usa en inicio, explorar y favoritos | Solo locales activos en vistas públicas | Previsualización incluye más información que la redacción | Especificar campos mínimos sin fijar diseño visual excesivo | ¿La calificación sin reseñas debe mostrarse como 0 o “sin reseñas”? |
| RF-006 | `GET /locales/{id}` y `GET /{id}/resenas`; detalle solo si activo | `/locales/:id` carga local/reseñas y ofrece reservar, favorito y compartir | Local inactivo devuelve no encontrado; imágenes públicas por API | Incluye política, reglas, amenidades, reseñas y disponibilidad indirecta | Actualizar alcance del detalle y reglas de visibilidad | Ninguna bloqueante |

## 3.3 Trazabilidad detallada RF-007 a RF-012

| ID | Evidencia backend | Evidencia frontend | Reglas encontradas | Diferencias, ausencia o parcialidad | Recomendación para actualización futura | Pregunta pendiente |
|---|---|---|---|---|---|---|
| RF-007 | `POST /auth/registro`, `RegisterRequest`, `Usuario`; email único y password BCrypt | `/registro`, formulario reactivo y mensajes | Nombres 3..120; email válido/único; teléfono 9..10 dígitos; contraseña 6..72; rol inicial `CLIENTE` | El registro inicia sesión automáticamente al devolver JWT | Mantener campos y documentar validaciones/rol/sesión inicial | ¿Se requiere verificación de correo o teléfono? No existe actualmente |
| RF-008 | `GET /admin/usuarios`, `PATCH /{id}/estado`; protege cuenta propia y otras admin | `/admin/usuarios` lista y alterna `ACTIVO/SUSPENDIDO` | Solo `ADMINISTRADOR`; no puede cambiarse a sí mismo ni otra cuenta admin | “Gestionar” se limita a listar y activar/suspender; no crea, edita, elimina ni filtra/pagina | Reescribir con operaciones exactas o decidir CRUD adicional | ¿Qué acciones administrativas sobre cuentas deben formar parte del alcance? |
| RF-009 | `GET /admin/locales`, `PATCH /{id}/estado` | `/admin/locales` lista, aprueba/publica o desactiva | Activar aprueba revisión; desactivar limpia pendiente | No hay edición/eliminación por admin ni historial/comentario de moderación | Definir como moderación/estado, no como gestión CRUD general | ¿Admin debe editar/eliminar o solo moderar? |
| RF-010 | Solicitud multipart, GridFS, unicidad pendiente, estados y revisión admin | `/convertirme-en-propietario`; admin abre documento y aprueba/rechaza | Cuenta no puede ser ya propietaria; una pendiente; formatos/tamaño; aprobación agrega rol | Implementación es más detallada: documento, notas y estados; “registrarse” no crea otra cuenta | Cambiar lenguaje a “solicitar rol” y especificar ciclo y privacidad documental | ¿Qué retención/acceso se aplica al documento de identidad? |
| RF-011 | `GET /reservas/mias` ordena todas por creación | `/mis-reservas` muestra estados y permite reseñar las elegibles | Solo reservas del sujeto JWT | No hay operación de cancelación; “activas/pasadas” se muestran por lista/estado, no por contrato formal de segmentación | Mantener consulta e indicar estados/criterios temporales | ¿Debe incluir filtros, paginación y cancelación? |
| RF-012 | Crea `EN_PROCESO`, valida capacidad/disponibilidad, calcula 8 %, luego procesa pago interno y completa/rechaza | Wizard de cinco pasos; UI solo envía `APPROVE` | Acepta reglas/política; 1..12 h; fecha futura; lock pesimista al aprobar | No es una única operación: borrador + pago interno. No hay cobro externo. “Inmediata” termina como `COMPLETADA` tras aprobación | Separar selección, creación y confirmación; decidir si pago interno es definitivo | ¿Pago interno o pasarela real? ¿Qué significa “reserva confirmada”? |

## 3.4 Trazabilidad detallada RF-013 a RF-018

| ID | Evidencia backend | Evidencia frontend | Reglas encontradas | Diferencias, ausencia o parcialidad | Recomendación para actualización futura | Pregunta pendiente |
|---|---|---|---|---|---|---|
| RF-013 | CRUD de alta/lectura/edición y estado bajo `/propietario/locales`; carga GridFS | Panel, lista, formulario nuevo/editar, imágenes | Solo dueño; catálogos canónicos; crear/editar envía a revisión y despublica | No hay eliminación; cambios requieren moderación no citada | Expresar operaciones exactas y revisión posterior a cambios | ¿Editar un local activo debe despublicarlo inmediatamente? |
| RF-014 | POST/DELETE bloqueos; valida rango y superposición | `/propietario/disponibilidad` elige local, fecha, horas y motivo | Solo rol/propiedad; inicio menor a fin; sin solapamiento | Actor real es `PROPIETARIO`, no un cliente genérico; persiste bloqueos, no estados por slot | Corregir actor y modelar “bloqueo de disponibilidad” | ¿Se permitirán bloqueos recurrentes o de varios días? |
| RF-015 | POST reseña; reserva propia `COMPLETADA`, fecha pasada, única; 1..5 y comentario 10..2000 | Formulario desde mis reservas; lectura pública en detalle | Recalcula promedio/contador del local | “Completada” no basta: evento debe haber pasado; una por reserva | Añadir fecha pasada, unicidad y recalculo como reglas | ¿Se permitirá editar/eliminar/moderar reseñas? No existe |
| RF-016 | No requiere backend específico; detalle tiene URL pública | `ShareService` usa Web Share o Clipboard desde `/locales/:id` | Acceso público; depende de capacidades/permisos del navegador | Cumple sin servicio de compartición servidor | Mantener como capacidad frontend y contemplar fallback | ¿Se requiere compartir a redes específicas? |
| RF-017 | GET IDs/lista, POST/DELETE; entidad única cliente-local | Controles en cards/detalle y sección `/favoritos` | Requiere autenticación; alta idempotente; solo locales activos visibles | Sección privada es ruta propia, no necesariamente dentro de perfil | Mantener y aclarar comportamiento si el local se desactiva | ¿Favoritos de locales inactivos deben ocultarse o conservarse visibles? |
| RF-018 | PUT perfil, PUT contraseña, DELETE baja lógica | `/perfil` permite datos, contraseña y baja | Contraseña actual; nueva distinta; admin no puede darse de baja; estado pasa a `INACTIVO` | Cumple el texto; además permite cambiar contraseña. La baja es lógica, no hay reactivación de usuario y el email no se edita | Separar perfil, contraseña y baja; definir retención/reactivación | ¿Quién y cómo reactiva una cuenta inactiva? ¿Se conserva toda su información? |

# 4. Funcionalidades implementadas no documentadas

| Funcionalidad activa | Evidencia | Relación con RF previos | Tratamiento documental recomendado |
|---|---|---|---|
| Procesamiento interno de pago y tarifa de servicio del 8 % | `ReservationApplicationService`, `PagoSimulado`, V3, wizard | Implícito en reserva; no existe RF de pago | **DECISIÓN HUMANA PENDIENTE**; si permanece, requisito funcional + reglas económicas; si es solo mecanismo técnico, aclararlo sin prometer cobro real |
| Panel administrativo | `/admin/dashboard`, métricas y actividad | No cubierto por RF-008/RF-009 | Crear requisito de consulta de indicadores si es funcionalidad de producto |
| Panel de propietario | `/propietario/dashboard`, ingresos, próximos eventos, métricas | No cubierto claramente por RF-013 | Crear requisito de consulta de panel/reportes |
| Reservas recibidas del propietario | `/propietario/reservas` y vista | RF-011 solo habla del cliente | Crear requisito funcional separado o ampliar actor propietario |
| Carga y almacenamiento de imágenes | `/propietario/imagenes`, GridFS, formulario | Complementa RF-013 | Regla/capacidad dentro de gestión de local; límites en RNF |
| Documento de verificación en GridFS | solicitud y descarga admin | Complementa RF-010 | Regla funcional + privacidad/retención no funcional |
| Auditoría MongoDB | `AuditEvent` y escrituras de operaciones | No aparece en RF/RNF | Restricción arquitectónica/RNF de trazabilidad; no convertir cada evento en RF |
| Moderación y estado pendiente de locales | `pendienteRevision`, V8, admin y propietario | RF-009/RF-013 son genéricos | Requisito funcional y reglas de transición |
| Validación de concurrencia en disponibilidad | lock pesimista de local y `@Version` en reserva | Complementa RF-012 | Regla de integridad/concurrencia y RNF, no pantalla nueva |
| Catálogos canónicos de tipos/amenidades | `VenueCatalog`, V9 | Complementa RF-003/RF-013 | Regla de dominio; decidir si catálogo será configurable |
| Salud del sistema | `/api/v1/sistema/salud`, Actuator, healthchecks | No cubierto | Detalle operativo/RNF de observabilidad |
| Datos iniciales automáticos | dos `CommandLineRunner` | No es función del usuario | Detalle de entorno/desarrollo; requiere perfil o decisión explícita |
| Seguridad por roles y cuenta activa | `SecurityConfig`, guards, filtro | Implícita en actores | RNF de autorización y matriz de permisos |
| Manejo uniforme parcial de errores | `ProblemDetail`, `GlobalExceptionHandler` | No cubierto | RNF/contrato de API |
| Notificaciones temporales UI | `NotificationService`, toasts | No cubierto | Detalle de UX; no confundir con notificaciones remotas |
| Compartición por APIs del navegador | `ShareService` | Implementa RF-016 | Detalle técnico del RF, no requisito adicional |

# 5. Matriz de requisitos no funcionales

## 5.1 RNF-001 a RNF-005

| ID | Categoría/declaración | Criterio anterior | Evidencia técnica | Cumplimiento verificable | No medido/contradicción | Riesgo de mantener texto | Propuesta conceptual futura |
|---|---|---|---|---|---|---|---|
| RNF-001 | Usabilidad | Interfaz intuitiva, rápida y sencilla | Rutas por audiencia, formularios, estados vacíos, toasts, layouts y navegación | NO VERIFICABLE | No hay pruebas con usuarios, tasa de éxito, tiempo por tarea ni accesibilidad auditada | Afirmación subjetiva imposible de aceptar/rechazar | Definir tareas críticas, muestra de usuarios, tasa de éxito, tiempo y criterios WCAG |
| RNF-002 | Usabilidad/compatibilidad | Versiones estables recientes de Chrome, Edge y Firefox | Angular moderno, TypeScript ES2022; no se observó matriz de navegadores ni E2E | NO VERIFICABLE | No hay versiones objetivo, BrowserStack/Playwright ni resultados cross-browser | “Más recientes” cambia continuamente y carece de evidencia | Fijar versiones/rango, dispositivos y suite de compatibilidad reproducible |
| RNF-003 | Rendimiento + disponibilidad | 99 % y máximo 3 s en operación normal | healthchecks DB/Nginx, restart, índices, gzip/cache y graceful shutdown | NO VERIFICABLE | No hay definición de periodo/SLO, monitor, percentil, carga, dataset ni prueba de latencia; mezcla dos atributos | Podría declararse cumplido solo por configuración, lo cual sería falso | Separar disponibilidad y rendimiento; definir ventana, exclusiones, p95/p99, endpoint, concurrencia y monitoreo |
| RNF-004 | Capacidad/integridad | Procesar al menos 999 reservas sin pérdida | Transacciones, constraints, Flyway, lock pesimista, versión e índices | NO VERIFICABLE | No hay prueba de carga/volumen, concurrencia, criterio de “procesar” ni verificación de pérdida | El número 999 no tiene condiciones ni evidencia | Definir escenario, concurrencia, tasa, duración, dataset, éxito, consistencia y recuperación |
| RNF-005 | Seguridad | Hashear contraseñas antes de almacenar | BCrypt en registro/cambio y `passwordHash` persistido | IMPLEMENTADO | No se especifica factor/coste, migración ni política futura | Bajo para cumplimiento básico; incompleto como política de credenciales | Mantener hashing y añadir algoritmo adaptable, parámetros, límites y no exposición |

## 5.2 Aspectos no funcionales relevantes no cubiertos

| Aspecto | Estado actual | Clasificación | Tratamiento futuro |
|---|---|---|---|
| Autenticación/autorización | JWT HS256, roles, BCrypt, cuenta activa | IMPLEMENTADO PERO NO DOCUMENTADO | RNF + matriz de permisos; decidir revocación/refresh |
| Integridad relacional | FKs, unicidad, checks, transacciones, locks | IMPLEMENTADO PERO NO DOCUMENTADO | RNF de integridad y reglas de concurrencia |
| Persistencia | Volúmenes PostgreSQL/Mongo y Flyway | IMPLEMENTADO PERO NO DOCUMENTADO | RNF de durabilidad; no confundir volumen con backup |
| Auditoría | Eventos Mongo para operaciones principales | IMPLEMENTADO PERO NO DOCUMENTADO | RNF de trazabilidad con retención/acceso |
| Privacidad | Documento de identidad almacenado y visible a admin | IMPLEMENTADO PARCIALMENTE | Definir finalidad, cifrado, retención, borrado y acceso |
| Límites de archivo | documento 5 MB; imagen 8 MB; Nginx 10 MB por request | IMPLEMENTADO PERO NO DOCUMENTADO | Restricción técnica/RNF; aclarar carga múltiple |
| Manejo de errores | `ProblemDetail`, validaciones, ocultación de stack | IMPLEMENTADO PARCIALMENTE | Unificar errores de seguridad, query params e inesperados |
| Observabilidad | salud funcional, Actuator, logs INFO, auditoría | IMPLEMENTADO PARCIALMENTE | Definir métricas, alertas, logs, tracing y acceso al health |
| Backups/recuperación | solo volúmenes | NO VERIFICABLE | RNF con RPO, RTO, frecuencia y prueba de restauración |
| Escalabilidad | listas/agregaciones/filtros en memoria, sin paginación | NO VERIFICABLE | Límites, paginación y pruebas con volumen |
| Portabilidad | Docker Compose local y variables | IMPLEMENTADO PARCIALMENTE | Diferenciar entorno local de producción; secretos/certificados |
| Mantenibilidad | módulos funcionales, strict TS, migraciones | IMPLEMENTADO PERO NO DOCUMENTADO | Métricas/proceso de calidad y eliminación de duplicados |
| Pruebas | test backend vacío, shell frontend y script humo manual | IMPLEMENTADO PARCIALMENTE | Estrategia unitaria, integración, E2E, seguridad y carga |
| Disponibilidad | reinicio/healthchecks parciales | IMPLEMENTADO PARCIALMENTE | Backend healthcheck, monitoreo externo, SLO medido |

# 6. Cambios necesarios en el diagrama de clases

El futuro diagrama debe limitarse al dominio persistente JPA relevante. No debe incluir controllers, services, repositories, DTO, frontend ni `AuditEvent` en el núcleo.

| Elemento anterior | Situación real | Acción futura | Clasificación/justificación |
|---|---|---|---|
| `Usuario` | Entidad `Usuario` con UUID, nombres, email, hash, teléfono, estados, roles y fecha | Se conserva con atributos/estados reales | CAMBIA DE ESTRUCTURA |
| Herencia `Usuario -> Cliente` | No existe entidad/subclase `Cliente` | Eliminar herencia; representar rol `CLIENTE` en `Usuario.roles` | OBSOLETO |
| Herencia `Usuario -> Administrador` | No existe entidad/subclase `Administrador` | Eliminar herencia; representar `ADMINISTRADOR` como rol | OBSOLETO |
| `Cliente.esPropietario` | Propietario es rol adicional y estado de verificación | Reemplazar booleano por `Rol.PROPIETARIO` y estado de verificación | IMPLEMENTADO DE FORMA DIFERENTE |
| `Local` | Entidad real `LocalEvento` | Renombrar y reconstruir atributos/colecciones | CAMBIA DE NOMBRE Y ESTRUCTURA |
| `Dirección` | Sector/dirección son strings dentro de `LocalEvento`; facturación son strings en `Reserva` | Eliminar como clase persistente | DOCUMENTADO PERO NO IMPLEMENTADO |
| `TipoEvento` | `Set<String>` en tabla de colección; catálogo canónico en código | Eliminar como entidad/clase; mostrar colección de valor si procede | IMPLEMENTADO DE FORMA DIFERENTE; DECISIÓN sobre tipado futuro |
| `EstadoLocal` | Dos booleanos `activo` y `pendienteRevision`; `destacado` separado | Eliminar enum anterior y reflejar flags/reglas actuales | OBSOLETO |
| `Disponibilidad` | No existe entidad con estados; existe `BloqueDisponibilidad` | Reemplazar por `BloqueDisponibilidad` (fecha, inicio, fin, motivo) | CAMBIA DE NOMBRE Y SEMÁNTICA |
| `EstadoDisponibilidad` | Disponibilidad se calcula con actividad, bloqueos y reservas | Eliminar enum | DOCUMENTADO PERO NO IMPLEMENTADO |
| `SolicitudPropietario` | Entidad real con documento GridFS, notas, estado, solicitante y revisor | Se conserva, cambia atributos y relaciones | CAMBIA DE ESTRUCTURA |
| `EstadoVerificacion` | Se divide en estado de solicitud y estado de verificación del usuario | Reemplazar por `EstadoSolicitudPropietario` y `EstadoVerificacionPropietario` | CAMBIA DE ESTRUCTURA |
| `Reserva` | Entidad real con importes, facturación, aceptaciones, estado, pago y versión | Se conserva con campos/relaciones reales | CAMBIA DE ESTRUCTURA |
| `EstadoReserva` | Valores reales `EN_PROCESO`, `COMPLETADA`, `RECHAZADA`, `CANCELADA` | Corregir nombres/género y señalar que `CANCELADA` no tiene transición activa | CAMBIA DE VALORES; DECISIÓN HUMANA PENDIENTE |
| `DetallePago` | No existe; la entidad real es `PagoSimulado` uno a uno | Reemplazar por `PagoSimulado` si se mantiene alcance actual | SE REEMPLAZA |
| `MetodoPago` | No hay tarjeta/transferencia; existen modo/estado internos | Eliminar y añadir `ModoPagoSimulado`/`EstadoPagoSimulado` | OBSOLETO respecto del código |
| `Factura` | No existe entidad ni generación de factura | Eliminar del diagrama actual | DOCUMENTADO PERO NO IMPLEMENTADO; contradice ADR |
| `Favorito` | Entidad asociativa `Usuario`-`LocalEvento`, UUID y fecha | Se conserva con multiplicidades y unicidad reales | CAMBIA DE ESTRUCTURA |
| `Reseña` | Entidad `Resena`, una por reserva, vinculada a cliente/local | Se conserva; corregir nombre de clase real, campos y relaciones | CAMBIA DE ESTRUCTURA |
| `Rol` | Enum persistido faltante en diagrama | Añadir `Rol` y colección de roles de `Usuario` | SE AÑADE PORQUE FALTA |
| `EstadoUsuario` | Enum persistido faltante | Añadir | SE AÑADE PORQUE FALTA |
| `EstadoPagoSimulado`, `ModoPagoSimulado` | Enums persistidos faltantes | Añadir si el pago interno queda vigente | SE AÑADEN PORQUE FALTAN |
| Colecciones de local | Tipos, amenidades, reglas e imágenes en tablas de colección | Añadir como atributos multivaluados/colecciones de valor, no como entidades inventadas | SE AÑADEN PORQUE FALTAN |

## 6.1 Entidades y relaciones que debe representar la siguiente etapa

- `Usuario 1 -> 0..* SolicitudPropietario` como solicitante; otra relación opcional como revisor.
- `Usuario 1 -> 0..* LocalEvento` como propietario.
- `LocalEvento 1 *-- 0..* BloqueDisponibilidad` con cascade/orphan removal.
- `Usuario 1 -> 0..* Reserva` como cliente; `LocalEvento 1 -> 0..* Reserva`.
- `Reserva 1 *-- 0..1 PagoSimulado`, con FK única del pago.
- `Favorito` como entidad asociativa entre `Usuario` y `LocalEvento`, única por par.
- `Resena` vinculada uno a uno con `Reserva` y muchas a uno con `Usuario`/`LocalEvento`.

No hay herencia entre entidades, `@Embeddable` ni `@MappedSuperclass`. `AuditEvent` puede aparecer en un diagrama técnico complementario de persistencia Mongo, no en el núcleo de dominio.

# 7. Cambios necesarios en el diagrama de componentes

| Componente anterior | Acción | Componente real/ajuste | Motivo |
|---|---|---|---|
| Frontend/Vistas | Conservar y renombrar | SPA Angular 21 | Existe como único cliente web activo |
| API REST | Conservar | API Spring Boot `/api/v1` | Frontera HTTP real |
| Gestión de Usuarios | Conservar/dividir responsabilidad | Autenticación/autorización + gestión de usuarios/propietarios | Seguridad y solicitud de rol son responsabilidades explícitas |
| Gestión de Locales | Conservar | Catálogo y gestión/moderación de locales | Incluye imágenes y revisión |
| Gestión de Reservas | Conservar/dividir | Reservas + disponibilidad | Disponibilidad tiene servicio/reglas propios dentro del monolito |
| Gestión de Pagos | Renombrar/condicionar | Procesamiento interno de pago en módulo `reservation` | No es servicio independiente ni gateway real |
| Persistencia | Dividir | Persistencia JPA/PostgreSQL; auditoría Mongo; GridFS | Responsabilidades y tecnologías distintas |
| Base relacional | Renombrar | PostgreSQL | Motor verificado |
| Base no relacional | Renombrar/desglosar | MongoDB para `AuditEvent` + GridFS para medios | El diagrama anterior no muestra responsabilidades |
| Pasarela de pago | Eliminar por ahora | Ninguna | No hay integración externa |
| Interfaces `IUsuarioService`, `ILocalService`, etc. | Eliminar | Dependencias internas concretas de servicios/controladores | No existen interfaces equivalentes en código |
| Nginx | Añadir | Servidor web/proxy TLS | Componente desplegado y frontera del sistema |
| Favoritos y reseñas | Añadir/agrupar | Módulo de participación (`engagement`) | Funcionalidad persistente activa |
| Paneles | Añadir | Módulo `dashboard` | Función activa para admin/propietario |
| Auditoría | Añadir | `AuditEventRepository`/Mongo | Componente técnico transversal |
| Almacenamiento de medios | Añadir | `MediaStorageService`/GridFS | Imágenes y documentos |
| Salud/observabilidad | Añadir como componente técnico opcional | System API + Actuator | Operación activa parcial |

El futuro diagrama debe encerrar los módulos internos dentro de un único **Backend LojaVents** desplegable. Las líneas entre módulos representan dependencias internas, no red ni microservicios.

# 8. Cambios necesarios en el diagrama de despliegue

| Elemento anterior | Estado frente a implementación | Cambio requerido |
|---|---|---|
| Navegador web | Vigente | Conservar como dispositivo cliente |
| HTTPS `:443` hacia Nginx | Vigente en Compose local | Conservar; añadir redirección desde host `:80` |
| Servidor frontend/Nginx | Vigente | Representar contenedor `lojavents-frontend`, estáticos Angular y certificado montado |
| Angular `dist` | Vigente | Nombrar artefacto construido `dist/lojavents-frontend/browser` |
| Backend Spring Boot/JVM | Vigente | Representar contenedor `lojavents-backend` y `/app/app.jar` |
| “Tomcat embebido” | Inferido, no fijado directamente | Preferir “servidor HTTP embebido de Spring Boot” o confirmar árbol de dependencias antes de nombrarlo |
| JAR `reservacion-locales-backend.jar` | Nombre incorrecto | Reemplazar por artefacto Maven `lojavents-backend-0.0.7-SNAPSHOT.jar`, copiado como `app.jar` |
| Comunicación Nginx-backend `:8080` | Vigente dentro de red Docker | Conservar como HTTP interno `/api/`; backend no publica puerto al host |
| PostgreSQL `:5432` | Vigente interno | Mostrar contenedor, healthcheck y volumen `postgres_data` |
| MongoDB `:27017` | Vigente interno | Mostrar contenedor, healthcheck, volumen `mongo_data` y GridFS |
| PayPal/pasarela HTTPS | Inexistente | Eliminar del despliegue actual; solo añadir en futuro si se implementa |
| Red Docker | Falta | Añadir `lojavents-network` bridge |
| Volúmenes | Faltan | Añadir `postgres_data`, `mongo_data` y montaje read-only `./certs` |
| Healthchecks | Faltan | Añadir PostgreSQL, MongoDB y Nginx; señalar ausencia de healthcheck backend |
| Dependencias de arranque | Faltan | Backend espera DB saludables; frontend solo espera backend iniciado |
| Seguridad de contenedor | Falta | Añadir `no-new-privileges`, usuario no root del backend e `init` donde corresponda |
| Entorno productivo | No verificable | Titular el diagrama como despliegue local/integrado Docker Compose; no inventar cloud/dominio/SLA |

El diagrama futuro debe distinguir host, contenedores y red interna. PostgreSQL, MongoDB y backend no están publicados al host; solo Nginx expone 80/443.

# 9. Cambios necesarios en el diagrama de paquetes

## 9.1 Diferencia estructural

El diagrama anterior representa `Cliente` y `Servidor` por capas (`API`, `DTO`, `Servicios`, `Dominio`, `Repositorios`, `Seguridad`) y muestra clases individuales. El repositorio real combina:

- frontend por responsabilidad (`core`, `features`, `layout`, `shared`);
- backend principalmente por módulos funcionales (`auth`, `user`, `venue`, `reservation`, `engagement`, `dashboard`, `audit`, `storage`, `system`);
- subcapas repetidas (`api`, `application`, `domain`, `repository`) dentro de varios módulos;
- infraestructura transversal (`config`, `common`, migraciones).

Por ello, la estructura anterior es **OBSOLETA** como mapa del código actual, aunque su idea de separar presentación, aplicación, dominio y persistencia sigue siendo útil como agrupación secundaria.

## 9.2 Acciones requeridas

| Elemento anterior | Acción futura |
|---|---|
| Paquete `Cliente` con vistas/controladores UI individuales | Reemplazar por agrupaciones Angular `core`, `features`, `layout`, `shared` |
| Paquete `Servidor` monolítico por capas | Reemplazar por módulos funcionales del backend |
| Paquetes `API`, `Servicios`, `Dominio`, `Repositorios` globales | Mostrar como subcapas internas comunes cuando aporten legibilidad, no como únicas raíces |
| Paquete `DTO` global | Integrar conceptualmente dentro de `*.api`; los DTO están distribuidos por módulo |
| `Seguridad` como paquete con clase `Rol` | Separar `auth/config` técnico de `Rol` persistido en `user.domain` |
| `Base de datos` como paquete | No tratar base de datos como paquete de código; reservarla para componentes/despliegue |
| Clases `LocalController`, `ReservaService`, etc. | Eliminar del diagrama de paquetes |
| Módulos faltantes | Añadir `engagement`, `dashboard`, `audit`, `storage`, `system`, `config`, `common` |
| Frontend duplicado | Representar solo `frontend/src` hasta confirmación humana |

## 9.3 Agrupación legible recomendada

1. **Frontend Angular:** `core`, `features`, `layout`, `shared`.
2. **Backend - módulos de negocio:** `auth`, `user`, `venue`, `reservation`, `engagement`, `dashboard`.
3. **Backend - infraestructura:** `audit`, `storage`, `system`, `config`, `common`.
4. **Subcapas relevantes:** `api -> application -> domain/repository`, aclarando dependencias transversales reales.

No se debe listar cada controller, DTO, servicio, entidad o repositorio en el futuro diagrama.

# 10. Contradicciones cruzadas entre documentos

| Tema | ADR | Requisitos anteriores | Diagramas anteriores | Código actual | Clasificación/impacto |
|---|---|---|---|---|---|
| Pago | Servidor registra/gestiona pagos | No existe RF específico de pago | Componente gateway y PayPal; `DetallePago`/métodos | Pago interno simulado persistido; sin gateway | CONTRADICTORIO; bloquea requisitos, clases, componentes y despliegue |
| Factura | Servidor genera facturas | No existe RF de facturación | Clase `Factura` | No existe implementación | DOCUMENTADO PERO NO IMPLEMENTADO; decisión de alcance |
| Usuario/actores | Cliente, propietario y admin como tipos de usuario | Mezcla “cliente propietario” y “Cliente” para disponibilidad | Herencia `Usuario/Cliente/Administrador`, booleano propietario | Una entidad Usuario con roles combinables | OBSOLETO/CONTRADICTORIO; actualizar actores y clase |
| Local | Responsabilidad general de administrar locales | RF-009/RF-013 genéricos | `Local`, `EstadoLocal`, `Dirección`, `TipoEvento` | `LocalEvento`, flags, strings y colecciones | IMPLEMENTADO DE FORMA DIFERENTE |
| Disponibilidad | Servidor la maneja | RF-003 fecha y RF-014 bloqueos | Entidad/estado `Disponibilidad` | Bloqueos persistidos + cálculo por reservas | CONTRADICTORIO en modelo; separar bloqueo de disponibilidad calculada |
| Reserva/cancelación | Coordina reservas | RF-011/12, sin cancelación explícita | Estado `CANCELADO` | `CANCELADA` existe, sin transición/endpoint | IMPLEMENTADO PARCIALMENTE; decisión humana |
| Reseña | Servidor controla | Solo después de reserva completada | Relación y atributos incompletos | Además exige evento pasado, unicidad y recalcula promedio | Documento incompleto, código más restrictivo |
| Favoritos | Mencionados como acción cliente | RF-017 detallado | Entidad con multiplicidades ambiguas | Entidad asociativa única Usuario-LocalEvento | Diagrama debe corregirse, RF sigue vigente |
| Administración | Acciones según rol | “Gestionar” cuentas/locales | Admin gestiona clases/relaciones genéricas | Cambia estados, modera locales y solicitudes; no CRUD completo | IMPLEMENTADO PARCIALMENTE; precisar verbos |
| Persistencia | “Base de datos” singular | No especificada | Relacional y no relacional genéricas | PostgreSQL transaccional; Mongo auditoría+GridFS | IMPLEMENTADO DE FORMA DIFERENTE; diagramas incompletos |
| Arquitectura | Cliente-servidor, no distribuida | No describe tecnología | Componentes pueden parecer servicios independientes | Un único backend modular/JAR | Mantener ADR; evitar lectura de microservicios |
| RNF-003 | Servidor crítico, sin métrica | Mezcla 99 % y 3 s bajo “Rendimiento” | Despliegue sin mecanismos/SLA | Mecanismos parciales, ninguna medición | NO VERIFICABLE; separar atributos |
| RNF-004 | Consistencia como fuerza | 999 reservas sin pérdida | Sin infraestructura de prueba/HA | Constraints/transacciones, sin prueba 999 | NO VERIFICABLE |
| Funciones no documentadas | No detalla paneles/auditoría/medios | RF no las incluye | Diagramas no las muestran | Activas | IMPLEMENTADO PERO NO DOCUMENTADO |

# 11. Decisiones humanas pendientes

| # | Decisión/pregunta | Por qué importa | Código actual | Documentación previa | Documentos afectados | ¿Bloquea? | Opciones sin escoger |
|---:|---|---|---|---|---|---|---|
| 1 | ¿Cuál frontend es canónico? | Evita documentar dos aplicaciones | Build usa `frontend/src`; árbol anidado no referenciado | Diagramas solo dicen “Frontend/Cliente” | Paquetes, componentes, evidencias | Sí, paquetes/componentes | Conservar raíz; migrar al anidado; eliminar/archivar duplicado |
| 2 | ¿Semillas solo en desarrollo? | Cambia comportamiento de arranque y seguridad | Dos runners sin `@Profile`, cuentas/locales automáticos | No mencionado | Despliegue, RNF, operación | Sí para despliegue final | Siempre activas; perfil demo; comando/manual separado |
| 3 | ¿Pago interno o pasarela real? | Define flujo, modelo e infraestructura | `PagoSimulado`; UI solo aprueba; sin red externa | ADR dice pagos; componentes/despliegue muestran gateway/PayPal | Requisitos, clases, componentes, despliegue | Sí, crítico | Mantener interno; integrar proveedor; eliminar paso de pago |
| 4 | ¿Debe existir facturación? | ADR y clase antigua la prometen | Solo dirección/montos en `Reserva`; no factura | ADR “generar facturas”; clase `Factura` | Requisitos, clases, componentes | Sí, crítico | Excluir formalmente; factura simple; integración fiscal definida |
| 5 | ¿Cancelación/reembolso forma parte del alcance? | Estado/política existen sin operación | `CANCELADA` sin transición; sin endpoint/reembolso | Diagrama incluye cancelado; RF no define flujo | Requisitos, clases, estados, UI | Sí | No incluir; cancelación sin reembolso; cancelación con reglas/reembolso |
| 6 | ¿Puede una reserva cruzar medianoche? | Afecta validación y solapamientos | Límite relativo de 36 h contradice mensaje | No lo define | RF-003/RF-012, reglas, pruebas | Sí para requisitos detallados | No cruzar; cruzar hasta hora fija; duración máxima sobre fechas |
| 7 | ¿Qué significa filtrar por fecha? | El resultado actual puede aparentar disponibilidad falsa | Catálogo excluye día bloqueado; chequeo completo usa hora/reservas | RF-003 solo dice fecha | RF-003, UX, componentes | Sí | Filtro informativo; disponibilidad de todo el día; exigir hora/duración |
| 8 | ¿Editar un local obliga a nueva revisión/despublicación? | Determina continuidad y moderación | Crear/editar desactiva y marca pendiente | RF-009/RF-013 no lo dicen | Requisitos, estados, clases | Sí | Revisión total; revisión solo de campos sensibles; publicación hasta aprobar |
| 9 | ¿Responsabilidad y retención de Mongo/GridFS? | Auditoría, imágenes e identidad comparten motor | DB `lojavents_logs` aloja eventos y GridFS | Diagrama solo dice no relacional/Mongo | Componentes, despliegue, RNF privacidad | Sí para diseño técnico final | Una DB; DB/colecciones separadas; servicios de almacenamiento separados |
| 10 | ¿Cuál es el despliegue productivo? | Compose local no define producción | Localhost, TLS local, sin cloud/CI/CD/secret manager | Diagrama presenta “servidores” y PayPal sin entorno | Despliegue, RNF disponibilidad | Sí, crítico para despliegue | Documentar solo local; definir VM; definir plataforma gestionada/contenedores |
| 11 | ¿RPO/RTO y retención de backups? | Volumen no equivale a respaldo | No hay backup/restauración | No mencionado | RNF, despliegue/operación | No para clases; sí para RNF final | Sin garantía académica explícita; backups periódicos; política con RPO/RTO |
| 12 | ¿Privacidad/observabilidad requeridas? | Hay documentos de identidad y auditoría | Acceso admin, logs/eventos, sin retención/cifrado definido | No mencionado | RNF, componentes, despliegue | Sí para RNF final | Política mínima; controles/retención; requisitos regulatorios definidos |
| 13 | ¿Exponer Actuator o mantenerlo interno? | Afecta monitoreo y superficie de ataque | Permitido en Spring, no proxied por Nginx | No mencionado | Despliegue, observabilidad | No | Interno; proxy autenticado; monitor por red Docker |
| 14 | ¿Refresh token revocable o JWT vigente? | Cambia seguridad y sesión | `/refresh` exige JWT actual; logout local | RF-001/2 no detalla | RF-001/2, seguridad, clases técnicas | No para dominio; sí para seguridad final | Mantener; refresh token rotatorio; sesiones servidor |
| 15 | ¿Qué paginación/límites deben existir? | Listas/agregaciones en memoria no escalan | Sin paginación en catálogo, usuarios, reservas, solicitudes | RF no define volumen salvo 999 reservas | RF/RNF rendimiento y API | No para diagramas iniciales | Límites fijos; paginación offset; cursor por recurso |
| 16 | ¿Conservar deuda histórica/no usada? | Aumenta ambigüedad documental | columna `usuarios.activo`, mocks, modos de rechazo sin UI | Diagramas/requisitos reflejan conceptos antiguos distintos | Paquetes, clases, mantenimiento | No, pero debe registrarse | Conservar documentado como legado; migrar/eliminar en trabajo posterior; activar flujo faltante |
| 17 | ¿Qué alcance exacto tiene la administración? | “Gestionar” puede significar CRUD total | Solo estados, moderación y revisión | RF-008/009 son amplios | Requisitos, casos de uso | Sí para requisitos | Mantener alcance actual; añadir edición; añadir eliminación/auditoría |
| 18 | ¿Catálogos de tipo/amenidad son fijos o administrables? | Define si son valores o entidades | Listas canónicas hard-coded; tipos persistidos como String | Diagrama usa `TipoEvento` como clase | Requisitos, clases, admin | Sí para clase final | Mantener valores cerrados; enums; entidades administrables |

# 12. Orden recomendado para las siguientes etapas

1. **Resolver decisiones bloqueantes:** pago, factura, cancelación, actor/alcance administrativo, disponibilidad temporal, revisión de locales, catálogo fijo/administrable, frontend canónico, Mongo/GridFS y despliegue objetivo.
2. **Actualizar requisitos funcionales:** conservar RF vigentes, precisar RF parciales/diferentes e incorporar solo funciones activas aceptadas como producto.
3. **Actualizar RNF:** separar metas, definir métricas/pruebas y añadir seguridad, integridad, privacidad, auditoría, backup y observabilidad acordados.
4. **Crear diagrama de clases del dominio persistente:** ocho entidades JPA y enums directos; excluir factura/PayPal/herencia si no se decide implementarlos.
5. **Crear diagrama de componentes:** un backend modular, Angular/Nginx, persistencia dual y sin microservicios ficticios.
6. **Crear diagrama de despliegue:** primero local Docker Compose verificado; solo añadir producción si se define.
7. **Crear diagrama de paquetes:** módulos funcionales y agrupaciones, sin clases individuales.
8. **Revisión cruzada final:** comprobar nombres, actores, estados, multiplicidades, responsabilidades de datos y desviaciones del ADR sin modificarlo.

# 13. Evidencias y rutas consultadas

## Documentación anterior

- `C:/Users/steve/Desktop/Diagramas a corregir/ADR.pdf` (3 páginas, revisión textual y visual).
- `C:/Users/steve/Desktop/Diagramas a corregir/Requisitos_lojaVents.pdf` (3 páginas; RF-001..RF-018 y RNF-001..RNF-005).
- `C:/Users/steve/Desktop/Diagramas a corregir/Diagrama de Clases.jpg`.
- `C:/Users/steve/Desktop/Diagramas a corregir/Diagrama de Componentes.jpeg`.
- `C:/Users/steve/Desktop/Diagramas a corregir/DiagramaDespligue.png`.
- `C:/Users/steve/Desktop/Diagramas a corregir/DiagramaPaquetes.jpg`.

## Inventario previo

- `docs/actualizacion/01-analisis-arquitectura-actual.md`.

## Código/configuración de contraste

- `backend/pom.xml`, `backend/Dockerfile`, `backend/src/main/resources/application.yml`.
- `backend/src/main/resources/db/migration/V1__crear_usuarios.sql` a `V9__normalizar_tipos_evento.sql`.
- `backend/src/main/java/ec/edu/unl/lojavents/auth/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/user/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/venue/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/engagement/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/dashboard/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/audit/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/storage/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/system/**`.
- `backend/src/main/java/ec/edu/unl/lojavents/config/**` y `common/**`.
- `frontend/angular.json`, `frontend/package.json`, `frontend/tsconfig*.json`, `frontend/Dockerfile`, `frontend/nginx.conf`, `frontend/proxy.conf.json`.
- `frontend/src/app/app.routes.ts`, `app.config.ts`, `core/**`, `features/**`, `shared/**` y `layout/**`.
- `frontend/src/environments/environment.ts`.
- `docker-compose.yml`, `.env.example`, `scripts/probar-sistema.ps1`.

Las afirmaciones de implementación provienen del código y configuración activos. ADR, requisitos y diagramas se usaron únicamente como línea base para medir vigencia, desviación y contradicción.
