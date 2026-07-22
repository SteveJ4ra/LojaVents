# Requisitos de software actualizados de LojaVents

**Versión documental:** 1.0  
**Fecha:** 2026-07-20  
**Base de verdad:** código, configuración, migraciones e infraestructura actualmente implementados.  
**Ámbito:** catálogo funcional y no funcional vigente; no implica cambios en el software.

## 1. Propósito

Este documento define de manera formal y verificable los requisitos de software que corresponden a la implementación actual de LojaVents. Sustituye la interpretación funcional de la línea base anterior, conserva su trazabilidad y elimina afirmaciones que no están respaldadas por el producto existente.

Está destinado a servir como base para actualizar posteriormente el documento oficial de requisitos y los diagramas UML, sin modificar el ADR aceptado ni rediseñar la aplicación.

## 2. Alcance

LojaVents es una aplicación web para consultar locales de eventos en Loja, comprobar su disponibilidad, realizar reservas y administrar las operaciones asociadas según los roles de cada cuenta.

El alcance vigente incluye:

- catálogo público, filtros generales, previsualización y detalle de locales;
- registro, autenticación, cierre de sesión local y perfil;
- favoritos, reservas y reseñas verificadas;
- solicitud y aprobación del rol propietario;
- creación, edición, imágenes, publicación y bloqueos horarios de locales;
- consulta de reservas y paneles para propietarios;
- consulta de usuarios, moderación de locales, solicitudes e indicadores para administradores;
- procesamiento interno simulado del resultado de pago;
- persistencia transaccional, auditoría y almacenamiento de imágenes/documentos.

No forman parte del alcance actual el cobro financiero real, PayPal, facturación, cancelaciones, reembolsos, correo electrónico, notificaciones remotas, CRUD administrativo completo ni infraestructura productiva cloud demostrada.

## 3. Arquitectura y contexto general

LojaVents mantiene una arquitectura **Cliente-Servidor**:

- El cliente es una SPA Angular construida desde `frontend/src`.
- Nginx sirve el frontend, termina TLS local y actúa como proxy de `/api/`.
- El servidor es una API REST Spring Boot desplegada como un único monolito modular.
- PostgreSQL conserva el dominio transaccional mediante JPA y migraciones Flyway.
- MongoDB conserva eventos de auditoría; GridFS conserva imágenes de locales y documentos de verificación.
- Docker Compose integra Nginx/frontend, backend, PostgreSQL y MongoDB en el entorno verificado.

El árbol `frontend/lojavents-frontend` no es una segunda aplicación activa; se considera una duplicación técnica detectada. Los inicializadores de demostración forman parte de la configuración de arranque observada, pero no son una funcionalidad ofrecida a los usuarios.

## 4. Actores y roles

| Actor/rol | Definición y permisos generales |
|---|---|
| **Visitante** | Persona sin sesión autenticada. Puede usar inicio, catálogo, filtros, detalle, reseñas públicas y compartir enlaces; puede acceder a registro/login. |
| **Usuario autenticado** | Cuenta `Usuario` con JWT vigente y estado activo. Es la identidad base sobre la que se asignan roles. |
| **Cliente** | Rol inicial de una cuenta registrada. Puede reservar, consultar reservas propias, usar favoritos, publicar reseñas elegibles, gestionar perfil y solicitar rol propietario. |
| **Propietario** | Rol adicional obtenido tras solicitud y aprobación administrativa. Puede administrar únicamente sus locales, imágenes y bloqueos, y consultar reservas/panel propios. |
| **Administrador** | Rol con acceso a consulta de usuarios, cambios de estado, moderación de locales, revisión de solicitudes y panel administrativo. No representa un CRUD completo. |

`Cliente`, `Propietario` y `Administrador` no son subclases persistentes. Una misma cuenta puede contener más de un rol. El visitante no corresponde a una cuenta autenticada.

## 5. Requisitos funcionales

### RF-001 - Inicio de sesión

| Campo | Especificación |
|---|---|
| Actor principal | Usuario registrado |
| Prioridad | Alta |
| Descripción | El sistema debe permitir iniciar sesión mediante correo y contraseña, validar que la cuenta esté activa y emitir un JWT con la identidad y roles autorizados. |
| Precondiciones | La cuenta debe existir y su estado debe ser `ACTIVO`. |
| Resultado | Se almacena localmente el token y el usuario de sesión; la cuenta puede acceder a rutas protegidas según sus roles. |
| Reglas relacionadas | RN-001, RN-002, RN-028, RN-029 |

**Criterios de aceptación**

1. Con credenciales válidas y cuenta activa, la API debe devolver token Bearer, expiración y usuario.
2. Ante correo inexistente o contraseña incorrecta, debe devolverse un único error de credenciales inválidas sin identificar cuál dato falló.
3. Una cuenta suspendida o inactiva no debe iniciar sesión ni usar recursos protegidos.
4. El JWT debe incluir sujeto, correo, roles, emisor y expiración.

### RF-002 - Cierre de sesión en el cliente

| Campo | Especificación |
|---|---|
| Actor principal | Usuario autenticado |
| Prioridad | Alta |
| Descripción | El sistema cliente debe permitir cerrar la sesión eliminando el JWT y los datos locales del usuario y regresando a una ruta pública. |
| Precondiciones | Debe existir una sesión almacenada en el navegador. |
| Resultado | El cliente deja de considerar autenticado al usuario. |
| Reglas relacionadas | RN-001 |

**Criterios de aceptación**

1. Al cerrar sesión deben eliminarse las claves locales de token y usuario.
2. Las rutas protegidas deben volver a requerir inicio de sesión.
3. No debe afirmarse que el JWT queda revocado inmediatamente en el servidor; no existe ese mecanismo.

### RF-003 - Búsqueda y filtros generales del catálogo

| Campo | Especificación |
|---|---|
| Actor principal | Visitante o usuario autenticado |
| Prioridad | Media |
| Descripción | El sistema debe permitir buscar locales activos y aplicar filtros generales por texto, tipo de evento, fecha, cantidad de asistentes y precio máximo por hora. |
| Precondiciones | El catálogo público debe haberse cargado. |
| Resultado | Se muestra el subconjunto de locales que satisface los filtros generales. |
| Reglas relacionadas | RN-008, RN-011, RN-012, RN-014 |

**Criterios de aceptación**

1. La búsqueda textual debe considerar nombre, descripción corta, sector, dirección y tipos de evento.
2. El filtro de asistentes debe excluir locales cuya capacidad sea inferior.
3. El filtro de precio debe excluir precios superiores al máximo indicado.
4. El filtro por fecha debe tratarse como filtro general del catálogo; seleccionar únicamente una fecha no garantiza una franja exacta disponible.
5. La disponibilidad exacta debe validarse posteriormente con fecha, hora, duración, bloqueos y reservas completadas.

### RF-004 - Página inicial

| Campo | Especificación |
|---|---|
| Actor principal | Visitante o usuario autenticado |
| Prioridad | Alta |
| Descripción | El sistema debe mostrar una página inicial con acceso al catálogo, búsqueda general, locales destacados e información resumida del proceso de reserva. |
| Precondiciones | Ninguna. |
| Resultado | El actor puede iniciar la exploración o dirigirse a acciones disponibles. |
| Reglas relacionadas | RN-011 |

**Criterios de aceptación**

1. Debe existir acceso visible a explorar locales.
2. Deben mostrarse locales activos marcados como destacados cuando existan.
3. El formulario inicial debe transferir sus filtros a la vista de exploración.

### RF-005 - Previsualización de locales

| Campo | Especificación |
|---|---|
| Actor principal | Visitante o usuario autenticado |
| Prioridad | Alta |
| Descripción | El sistema debe presentar una previsualización de cada local público sin depender de una composición visual específica. |
| Precondiciones | El local debe estar activo. |
| Resultado | El actor puede comparar información básica y abrir el detalle. |
| Reglas relacionadas | RN-011, RN-025 |

**Criterios de aceptación**

1. La previsualización debe identificar nombre, imagen principal, sector, descripción corta, capacidad y precio por hora.
2. Debe mostrar la calificación disponible del local.
3. Debe permitir navegar al detalle.
4. El control de favorito debe exigir autenticación para modificar datos.

### RF-006 - Detalle público del local

| Campo | Especificación |
|---|---|
| Actor principal | Visitante o usuario autenticado |
| Prioridad | Alta |
| Descripción | El sistema debe mostrar el detalle de un local activo con información descriptiva, imágenes, capacidad, precio, tipos de evento, amenidades, reglas, política registrada y reseñas. |
| Precondiciones | El identificador debe corresponder a un local activo. |
| Resultado | Se presenta la información pública y las acciones permitidas para el estado de autenticación. |
| Reglas relacionadas | RN-011, RN-025, RN-026 |

**Criterios de aceptación**

1. Un local inexistente o inactivo no debe exponerse como detalle público disponible.
2. Deben cargarse las reseñas asociadas; si no existen, debe mostrarse un estado vacío.
3. Debe existir acceso a compartir para cualquier actor.
4. Reservar y modificar favoritos debe solicitar autenticación cuando no exista sesión.

### RF-007 - Registro de cliente

| Campo | Especificación |
|---|---|
| Actor principal | Visitante |
| Prioridad | Alta |
| Descripción | El sistema debe permitir crear una cuenta mediante nombres completos, correo, teléfono y contraseña, asignarle el rol inicial `CLIENTE` y devolver una sesión JWT. |
| Precondiciones | El correo no debe pertenecer a otra cuenta. |
| Resultado | Se persiste una cuenta activa con contraseña protegida y sesión iniciada. |
| Reglas relacionadas | RN-002, RN-028, RN-029 |

**Criterios de aceptación**

1. Los nombres deben contener entre 3 y 120 caracteres.
2. El correo debe tener formato válido, máximo 180 caracteres y ser único sin distinguir mayúsculas.
3. El teléfono debe contener 9 o 10 dígitos.
4. La contraseña debe contener entre 6 y 72 caracteres y almacenarse con BCrypt, nunca en texto plano.
5. La cuenta nueva debe recibir `CLIENTE`, estado `ACTIVO` y verificación propietaria `NO_SOLICITADA`.

### RF-008 - Consulta y estado de cuentas

| Campo | Especificación |
|---|---|
| Actor principal | Administrador |
| Prioridad | Media |
| Descripción | El sistema debe permitir consultar las cuentas registradas y cambiar entre estados activos o suspendidos dentro de las restricciones administrativas vigentes. |
| Precondiciones | Sesión activa con rol `ADMINISTRADOR`. |
| Resultado | La lista refleja el estado persistido de cada cuenta y el cambio autorizado. |
| Reglas relacionadas | RN-001, RN-006 |

**Criterios de aceptación**

1. Deben mostrarse usuarios ordenados por fecha de creación descendente.
2. El administrador debe poder suspender una cuenta activa y activar una cuenta no activa mediante la interfaz vigente.
3. No debe poder cambiar el estado de su propia cuenta administradora.
4. No debe poder cambiar el estado de otra cuenta con rol administrador.
5. No se deben documentar creación, edición o eliminación administrativa porque no existen.

### RF-009 - Moderación y publicación de locales

| Campo | Especificación |
|---|---|
| Actor principal | Administrador |
| Prioridad | Media |
| Descripción | El sistema debe permitir consultar todos los locales y aprobar su publicación o desactivarlos. |
| Precondiciones | Sesión activa con rol `ADMINISTRADOR`. |
| Resultado | El local queda activo y sin revisión pendiente al aprobarse, o inactivo al desactivarse. |
| Reglas relacionadas | RN-009, RN-010, RN-011 |

**Criterios de aceptación**

1. La consulta administrativa debe incluir locales activos, inactivos y pendientes de revisión.
2. Al aprobar, `activo` debe ser verdadero y `pendienteRevision` falso.
3. Al desactivar, el local debe dejar de aparecer en el catálogo público.
4. No se deben documentar edición o eliminación administrativa inexistentes.

### RF-010 - Solicitud del rol propietario

| Campo | Especificación |
|---|---|
| Actor principal | Cliente |
| Prioridad | Alta |
| Descripción | El sistema debe permitir solicitar el rol `PROPIETARIO` mediante identificación, descripción/notas y un documento de respaldo. |
| Precondiciones | Cuenta activa, sin rol propietario y sin otra solicitud pendiente. |
| Resultado | Se crea una solicitud `PENDIENTE`, se almacena el documento y la cuenta queda con verificación propietaria pendiente. |
| Reglas relacionadas | RN-003, RN-004, RN-005, RN-030 |

**Criterios de aceptación**

1. La identificación debe contener entre 10 y 30 caracteres.
2. Las notas deben contener entre 15 y 1200 caracteres.
3. El documento debe ser PDF, PNG o JPEG y no superar 5 MB.
4. El sistema debe impedir una solicitud si la cuenta ya es propietaria o tiene otra pendiente.
5. El cliente debe poder consultar su solicitud más reciente.

### RF-011 - Consulta de reservas propias

| Campo | Especificación |
|---|---|
| Actor principal | Cliente |
| Prioridad | Alta |
| Descripción | El sistema debe permitir consultar las reservas pertenecientes a la cuenta autenticada, con sus datos, importes y estado persistido. |
| Precondiciones | Sesión activa. |
| Resultado | Se muestra el historial propio ordenado por fecha de creación descendente. |
| Reglas relacionadas | RN-019, RN-020, RN-022, RN-023 |

**Criterios de aceptación**

1. La API no debe devolver reservas de otros clientes.
2. Deben distinguirse al menos `EN_PROCESO`, `COMPLETADA` y `RECHAZADA` cuando existan.
3. `CANCELADA` puede aparecer como valor de modelo, pero el sistema no debe ofrecer una acción de cancelación.
4. Las reservas elegibles deben permitir iniciar el flujo de reseña.

### RF-012 - Creación y procesamiento interno de reserva

| Campo | Especificación |
|---|---|
| Actor principal | Cliente |
| Prioridad | Alta |
| Descripción | El sistema debe permitir seleccionar local, fecha, hora, duración, asistentes y dirección de facturación; validar capacidad/disponibilidad; calcular importes; crear una reserva en proceso y someterla al procesamiento interno simulado. |
| Precondiciones | Sesión activa, local activo, aceptación de reglas y política registrada. |
| Resultado | La reserva queda `COMPLETADA` si el resultado interno es aprobado o `RECHAZADA` si es rechazado. No se realiza cobro real. |
| Reglas relacionadas | RN-011 a RN-022 |

**Criterios de aceptación**

1. La duración debe estar entre 1 y 12 horas y los asistentes no deben superar la capacidad.
2. La fecha/hora no debe ser pasada y la disponibilidad exacta debe considerar bloqueos y reservas completadas superpuestas.
3. El subtotal debe ser precio por hora multiplicado por duración; la tarifa de servicio debe ser 8 %; el total debe ser su suma.
4. Antes del procesamiento debe persistirse una reserva `EN_PROCESO`.
5. Antes de aprobar, el servidor debe volver a validar disponibilidad bajo el mecanismo de concurrencia actual.
6. El resultado debe persistir una referencia y mensaje de pago interno.
7. No debe invocarse ni mencionarse como existente PayPal, una pasarela externa o un cobro financiero real.

### RF-013 - Gestión de locales propios

| Campo | Especificación |
|---|---|
| Actor principal | Propietario |
| Prioridad | Alta |
| Descripción | El sistema debe permitir crear, consultar y editar locales propios, cargar imágenes y solicitar cambios de publicación conforme al proceso de revisión. |
| Precondiciones | Sesión activa con rol `PROPIETARIO`; para modificar, el local debe pertenecer a la cuenta. |
| Resultado | La información queda persistida; una creación o edición queda inactiva y pendiente de revisión administrativa. |
| Reglas relacionadas | RN-007 a RN-011, RN-031 |

**Criterios de aceptación**

1. Deben validarse nombre, descripciones, ubicación, precio, capacidad, catálogos, reglas, política e imágenes.
2. Tipos de evento y amenidades deben pertenecer a los catálogos canónicos vigentes.
3. Solo el propietario asociado debe consultar o modificar el local por endpoints de propietario.
4. Crear o editar debe ejecutar `solicitarRevision`: despublicar y marcar pendiente.
5. El propietario debe poder desactivar un local activo o solicitar revisión para volver a activarlo.
6. No debe ofrecerse eliminación de locales porque no existe.

### RF-014 - Bloqueos de disponibilidad

| Campo | Especificación |
|---|---|
| Actor principal | Propietario |
| Prioridad | Media |
| Descripción | El sistema debe permitir agregar y eliminar bloqueos de una fecha y rango horario para locales propios. |
| Precondiciones | Sesión propietaria y propiedad del local. |
| Resultado | El bloqueo se incorpora o elimina del calendario persistido del local. |
| Reglas relacionadas | RN-007, RN-015, RN-016 |

**Criterios de aceptación**

1. El bloqueo debe contener fecha, hora inicial, hora final y motivo.
2. La hora final debe ser posterior a la inicial.
3. El sistema debe rechazar superposición con otro bloqueo del mismo local y fecha.
4. Un bloqueo existente debe impedir una reserva cuya franja se superponga.

### RF-015 - Publicación de reseñas verificadas

| Campo | Especificación |
|---|---|
| Actor principal | Cliente |
| Prioridad | Media |
| Descripción | El sistema debe permitir publicar una calificación y comentario para una reserva propia completada cuya fecha de evento haya transcurrido. |
| Precondiciones | Sesión activa, reserva propia `COMPLETADA`, evento pasado y sin reseña previa. |
| Resultado | Se guarda una reseña y se actualizan la calificación media y el contador del local. |
| Reglas relacionadas | RN-023 a RN-025 |

**Criterios de aceptación**

1. La calificación debe estar entre 1 y 5.
2. El comentario debe contener entre 10 y 2000 caracteres en la entrada vigente.
3. Solo debe existir una reseña por reserva.
4. No debe aceptarse antes de la fecha posterior al evento ni para una reserva no completada.
5. La calificación del local debe recalcularse con todas sus reseñas persistidas.

### RF-016 - Compartir enlace público del local

| Campo | Especificación |
|---|---|
| Actor principal | Visitante o usuario autenticado |
| Prioridad | Media |
| Descripción | El sistema debe permitir compartir la URL pública del detalle mediante Web Share API o copiarla al portapapeles como alternativa. |
| Precondiciones | Debe mostrarse un local público; el navegador debe permitir alguna de las capacidades. |
| Resultado | El navegador comparte o copia el enlace y la interfaz informa el resultado. |
| Reglas relacionadas | Ninguna regla de dominio adicional. |

**Criterios de aceptación**

1. Si `navigator.share` está disponible, debe intentarse la compartición nativa.
2. Si no está disponible, debe intentarse copiar la URL al portapapeles.
3. Un fallo debe generar un mensaje temporal y no alterar el local.

### RF-017 - Favoritos

| Campo | Especificación |
|---|---|
| Actor principal | Cliente |
| Prioridad | Media |
| Descripción | El sistema debe permitir agregar, eliminar y consultar locales favoritos mediante controles de catálogo/detalle y una sección privada. |
| Precondiciones | Sesión activa; para agregar, el local debe estar activo. |
| Resultado | La relación cliente-local se crea o elimina sin duplicados. |
| Reglas relacionadas | RN-026 |

**Criterios de aceptación**

1. Agregar repetidamente el mismo local debe mantener una sola relación.
2. El cliente debe poder consultar IDs y detalles de sus favoritos.
3. Eliminar un favorito inexistente debe producir estado final “no favorito” sin duplicar efectos.
4. Los locales inactivos no deben mostrarse en la lista activa de favoritos, aunque la relación persistida pueda conservarse.

### RF-018 - Perfil, contraseña y baja lógica

| Campo | Especificación |
|---|---|
| Actor principal | Usuario autenticado |
| Prioridad | Alta |
| Descripción | El sistema debe permitir editar nombres y teléfono, cambiar la contraseña y dar de baja lógicamente la propia cuenta. |
| Precondiciones | Sesión activa. Para contraseña, debe proporcionarse la contraseña actual. |
| Resultado | Se actualiza el perfil/hash o la cuenta pasa a `INACTIVO`; no se eliminan físicamente sus datos. |
| Reglas relacionadas | RN-001, RN-006, RN-027 a RN-029 |

**Criterios de aceptación**

1. Solo nombres y teléfono deben ser editables por el flujo vigente; el correo permanece sin edición.
2. El cambio de contraseña debe validar la actual y exigir una nueva distinta, de 6 a 72 caracteres.
3. La baja debe cambiar el estado a `INACTIVO`, cerrar la sesión local e impedir acceso protegido posterior.
4. Una cuenta administradora no debe poder darse de baja desde el perfil.

### RF-019 - Panel administrativo

| Campo | Especificación |
|---|---|
| Actor principal | Administrador |
| Prioridad | Media |
| Descripción | El sistema debe permitir consultar indicadores consolidados de usuarios, locales, reservas, importes, reseñas, solicitudes pendientes, métricas mensuales, locales principales y actividad reciente. |
| Precondiciones | Sesión activa con rol `ADMINISTRADOR`. |
| Resultado | Se muestra el panel calculado a partir de los datos persistidos. |
| Reglas relacionadas | RN-006, RN-020, RN-025 |

**Criterios de aceptación**

1. Los conteos deben distinguir estados de usuarios, locales y reservas.
2. Los importes aprobados y tarifas deben sumar únicamente reservas `COMPLETADA`.
3. Deben mostrarse hasta los elementos recientes/principales que devuelve el servicio vigente.
4. La actividad debe provenir de los eventos de auditoría disponibles.

### RF-020 - Panel del propietario

| Campo | Especificación |
|---|---|
| Actor principal | Propietario |
| Prioridad | Media |
| Descripción | El sistema debe permitir consultar indicadores de los locales y reservas pertenecientes al propietario autenticado. |
| Precondiciones | Sesión activa con rol `PROPIETARIO`. |
| Resultado | Se muestran totales, ingresos aprobados, métricas, reservas recientes y próximas limitadas al propietario. |
| Reglas relacionadas | RN-007, RN-020 |

**Criterios de aceptación**

1. Solo deben incluirse locales cuyo propietario sea el sujeto JWT.
2. Solo deben incluirse reservas de esos locales.
3. Próximas reservas debe considerar reservas completadas con fecha no pasada.
4. Ingresos debe sumar totales de reservas completadas.

### RF-021 - Reservas recibidas por el propietario

| Campo | Especificación |
|---|---|
| Actor principal | Propietario |
| Prioridad | Alta |
| Descripción | El sistema debe permitir consultar las reservas realizadas sobre los locales del propietario autenticado. |
| Precondiciones | Sesión activa con rol `PROPIETARIO`. |
| Resultado | Se muestra una lista ordenada con local, cliente, fecha/hora, duración, asistentes, total, referencia/mensaje interno y estado. |
| Reglas relacionadas | RN-007, RN-020, RN-021 |

**Criterios de aceptación**

1. No deben mostrarse reservas de locales pertenecientes a otro propietario.
2. La lista debe ordenarse por creación descendente.
3. El ingreso aprobado mostrado debe sumar únicamente reservas completadas.
4. Este flujo es de consulta; no debe ofrecer aprobación, rechazo, cancelación o reembolso manual.

### RF-022 - Revisión administrativa de solicitudes de propietario

| Campo | Especificación |
|---|---|
| Actor principal | Administrador |
| Prioridad | Alta |
| Descripción | El sistema debe permitir consultar solicitudes de propietario, abrir su documento almacenado y aprobarlas o rechazarlas. |
| Precondiciones | Sesión administradora y solicitud `PENDIENTE`. |
| Resultado | La solicitud, cuenta y roles quedan sincronizados con la decisión y se registra auditoría. |
| Reglas relacionadas | RN-003 a RN-006, RN-030 |

**Criterios de aceptación**

1. Debe permitirse filtrar solicitudes por estado mediante la API; la vista vigente carga pendientes.
2. El endpoint administrativo de consulta del documento debe exigir rol `ADMINISTRADOR`; actualmente esto no garantiza aislamiento completo porque el lector público de imágenes acepta identificadores válidos de GridFS sin distinguir el tipo de archivo.
3. Aprobar debe asignar rol `PROPIETARIO` y estados de solicitud/verificación aprobados.
4. Rechazar debe establecer estados rechazados y conservar el comentario normalizado cuando exista.
5. Una solicitud ya revisada no debe procesarse nuevamente.

## 6. Reglas de negocio

| ID | Descripción normativa | Entidades/RF relacionados | Evidencia o condición principal |
|---|---|---|---|
| RN-001 | Solo una cuenta `ACTIVO` puede iniciar sesión y acceder con JWT a recursos protegidos. | `Usuario`; RF-001, RF-002, RF-018 | `AuthApplicationService`, `ActiveAccountFilter` |
| RN-002 | Toda cuenta registrada debe iniciar con rol `CLIENTE`, estado `ACTIVO` y verificación propietaria `NO_SOLICITADA`. | `Usuario`; RF-007 | Constructor y valores iniciales de `Usuario` |
| RN-003 | El rol `PROPIETARIO` se añade a una cuenta únicamente tras la aprobación administrativa de una solicitud; una cuenta puede conservar varios roles. | `Usuario`, `SolicitudPropietario`; RF-010, RF-022 | `agregarRol`, `reviewOwnerRequest` |
| RN-004 | Un usuario no puede mantener más de una solicitud de propietario `PENDIENTE`. | `SolicitudPropietario`; RF-010 | validación de servicio e índice parcial SQL |
| RN-005 | Una solicitud pendiente puede pasar a `APROBADA` o `RECHAZADA` una sola vez, registrando revisor y fecha. | `SolicitudPropietario`; RF-010, RF-022 | métodos `aprobar/rechazar` y validación de estado |
| RN-006 | Un administrador no puede cambiar el estado de su propia cuenta ni el de otra cuenta administradora, y no puede darse de baja desde su perfil. | `Usuario`; RF-008, RF-018 | `UserAccountApplicationService` |
| RN-007 | Un propietario solo puede consultar/modificar locales y reservas vinculados a su propio identificador. | `Usuario`, `LocalEvento`, `Reserva`; RF-013, RF-014, RF-020, RF-021 | consultas por propietario y `requireOwnerVenue` |
| RN-008 | Los tipos de evento y amenidades enviados para un local deben pertenecer a los catálogos canónicos vigentes y se persisten como valores, no como entidades administrables. | `LocalEvento`; RF-003, RF-013 | `VenueCatalog`, colecciones JPA |
| RN-009 | Crear o editar un local debe dejarlo inactivo y `pendienteRevision`; solicitar activación también lo envía a revisión. | `LocalEvento`; RF-009, RF-013 | `solicitarRevision`, `VenueApplicationService` |
| RN-010 | Solo la aprobación administrativa publica un local pendiente; desactivarlo debe retirar la revisión pendiente. | `LocalEvento`; RF-009 | `aprobarRevision`, `cambiarEstado` |
| RN-011 | Solo los locales activos pueden aparecer en catálogo/detalle público, agregarse a favoritos o aceptar reservas. | `LocalEvento`; RF-003 a RF-006, RF-012, RF-017 | repositorio de activos y servicios |
| RN-012 | Los asistentes de una reserva no pueden superar la capacidad del local y deben ser al menos uno. | `Reserva`, `LocalEvento`; RF-003, RF-012 | DTO, servicio y constraints SQL |
| RN-013 | La duración de una reserva debe ser un número entero entre 1 y 12 horas. | `Reserva`; RF-012 | DTO, servicio y V3 |
| RN-014 | Una reserva no puede iniciar en una fecha pasada ni, si es hoy, en una hora que no sea posterior a la actual de `America/Guayaquil`. | `Reserva`; RF-012 | `ReservationAvailabilityService` |
| RN-015 | La disponibilidad exacta debe rechazar cualquier superposición con bloqueos del local o reservas `COMPLETADA`, considerando fechas adyacentes para intervalos que crucen día. | `Reserva`, `BloqueDisponibilidad`; RF-012, RF-014 | cálculo de minutos y consultas de tres fechas |
| RN-016 | Un bloqueo debe tener hora inicial anterior a la final y no superponerse con otro bloqueo del mismo local/fecha. | `BloqueDisponibilidad`; RF-014 | servicio y check SQL |
| RN-017 | El subtotal de una reserva debe ser `precioHora * duracionHoras`, redondeado a dos decimales. | `Reserva`, `LocalEvento`; RF-012 | `ReservationApplicationService` |
| RN-018 | La tarifa de servicio debe ser 8 % del subtotal y el total debe ser subtotal más tarifa, a dos decimales. | `Reserva`; RF-012, RF-019, RF-020 | constante `SERVICE_FEE_RATE` |
| RN-019 | Toda reserva nueva debe persistirse inicialmente con estado `EN_PROCESO`. | `Reserva`; RF-011, RF-012 | valor inicial de entidad y `createDraft` |
| RN-020 | El procesamiento interno aprobado debe cambiar la reserva a `COMPLETADA`; un resultado interno rechazado debe cambiarla a `RECHAZADA` y conservar motivo. | `Reserva`, `PagoSimulado`; RF-011, RF-012, RF-019 a RF-021 | `aprobarPago`, `rechazarPago` |
| RN-021 | Cada reserva puede tener como máximo un `PagoSimulado` con referencia única y solo puede procesarse desde `EN_PROCESO`. | `Reserva`, `PagoSimulado`; RF-012, RF-021 | relación uno a uno, unique SQL y validación |
| RN-022 | `CANCELADA` existe como estado persistible, pero ninguna operación vigente debe presentarse como transición de cancelación o reembolso. | `Reserva`; RF-011, RF-012 | ausencia de método/endpoint de cancelación |
| RN-023 | Una reseña solo puede crearse para una reserva propia `COMPLETADA`, cuya fecha sea anterior al día actual y que aún no tenga reseña. | `Reserva`, `Resena`; RF-011, RF-015 | `ReviewApplicationService` |
| RN-024 | La calificación debe estar entre 1 y 5 y el comentario debe respetar los límites de entrada/persistencia vigentes. | `Resena`; RF-015 | DTO y V4 |
| RN-025 | Al crear una reseña debe recalcularse el promedio a un decimal y el total de reseñas del local. | `Resena`, `LocalEvento`; RF-005, RF-006, RF-015, RF-019 | `recalculateRating` |
| RN-026 | Debe existir como máximo un favorito por par cliente-local; las consultas funcionales deben ocultar locales inactivos. | `Favorito`; RF-006, RF-017 | unique constraint y filtros de servicio |
| RN-027 | Dar de baja una cuenta debe cambiarla a `INACTIVO`; no debe eliminar físicamente el usuario ni sus relaciones. | `Usuario`; RF-018 | `deactivateOwnAccount` |
| RN-028 | Para cambiar contraseña debe validarse la contraseña actual y la nueva debe ser diferente, de 6 a 72 caracteres, antes de aplicar BCrypt. | `Usuario`; RF-001, RF-007, RF-018 | DTO y servicio de cuentas |
| RN-029 | El correo de usuario debe ser único sin distinción práctica de mayúsculas y se normaliza a minúsculas en registro/login. | `Usuario`; RF-001, RF-007 | repositorio, servicio y constraint unique |
| RN-030 | El documento de verificación debe ser PDF/PNG/JPEG, máximo 5 MB y almacenarse en GridFS. Su flujo funcional de consulta es administrativo, aunque el lector público de medios no diferencia actualmente entre imágenes y documentos. | `SolicitudPropietario`; RF-010, RF-022 | `MediaStorageService`, `MediaController`, controladores |
| RN-031 | Las imágenes de local deben ser PNG/JPEG/WEBP, máximo 8 MB por archivo; la petición completa también está limitada por Nginx a 10 MB. | `LocalEvento`; RF-013 | `MediaStorageService`, `nginx.conf` |

## 7. Requisitos no funcionales

| ID | Categoría | Descripción normativa | Criterio de aceptación/verificación | Evidencia actual | Estado | Prioridad |
|---|---|---|---|---|---|---|
| RNF-001 | Usabilidad | La interfaz debe ofrecer navegación por rutas, formularios con validación, estados de carga/error disponibles y mensajes temporales para operaciones principales. | Inspección funcional de rutas y formularios; las cualidades subjetivas requieren pruebas con usuarios separadas. | Formularios reactivos, guards, empty states y toasts; hay mejoras pendientes | Parcialmente verificable | Alta |
| RNF-002 | Compatibilidad | La compatibilidad con Chrome, Edge y Firefox solo debe declararse para versiones que hayan superado una matriz de pruebas documentada. | Ejecutar flujos críticos y registrar versiones/resultados por navegador. | Angular/ES2022; no existe matriz cross-browser observada | No verificable con el repositorio | Media |
| RNF-003 | Rendimiento y escalabilidad | No debe afirmarse un tiempo máximo de respuesta ni capacidad de carga sin escenario, volumen, concurrencia y percentil medidos; los listados deben operar dentro de los límites del dataset probado. | Prueba de carga reproducible con endpoints, dataset, concurrencia, p95/p99 y errores definidos. | Índices/cache estática; listas y paneles sin paginación y agregaciones en memoria | No verificable con el repositorio | Media |
| RNF-004 | Integridad y concurrencia de reservas | La confirmación de una reserva debe volver a comprobar disponibilidad y proteger la operación frente a confirmaciones concurrentes conflictivas. | Dos solicitudes superpuestas concurrentes no deben terminar ambas `COMPLETADA`. | Lock pesimista de local, transacción, consultas de conflicto y `@Version` | Verificado por implementación; no probado bajo carga | Alta |
| RNF-005 | Seguridad de contraseñas | Las contraseñas deben almacenarse únicamente como hashes BCrypt y nunca incluirse en respuestas de API. | Inspección de persistencia/DTO y pruebas de registro/cambio. | `BCryptPasswordEncoder`, campo `passwordHash`, DTO sin hash | Verificado | Alta |
| RNF-006 | Autenticación | La API debe autenticar recursos protegidos mediante JWT Bearer firmado con HS256, emisor y expiración configurados. | Solicitud sin token inválida; token válido permite acceso hasta expiración según cuenta. | `JwtService`, resource server y configuración | Verificado | Alta |
| RNF-007 | Autorización | La API debe autorizar prefijos administrativos/propietarios por roles y negar recursos protegidos a cuentas ausentes, suspendidas o inactivas. | Pruebas por rol/estado deben obtener respuestas permitidas o 403 según corresponda. | `SecurityConfig`, `ActiveAccountFilter`, comprobaciones de propiedad | Verificado | Alta |
| RNF-008 | Validación e integridad de datos | Las entradas deben validarse mediante Bean Validation/reglas de servicio y el esquema debe aplicar constraints, FKs, unicidad y checks relevantes. | Entradas inválidas deben rechazarse sin persistencia; migraciones deben validar esquema al arranque. | DTO, servicios, JPA, Flyway V1-V9 y `ddl-auto=validate` | Verificado | Alta |
| RNF-009 | Manejo de errores | Los errores de negocio y validación deben devolverse en formato `ProblemDetail` cuando los cubre el handler, y no deben incluir stack traces o excepciones internas. | Verificar `code`, `detail`, campos de validación y ausencia de stack en respuestas cubiertas. | `GlobalExceptionHandler` y `server.error.*`; no cubre explícitamente todos los errores | Parcialmente verificable | Alta |
| RNF-010 | Manejo de archivos | Documentos e imágenes deben respetar tipos/tamaños de RN-030/RN-031 y nombres sin saltos de línea. | Rechazar archivos vacíos, de tipo no permitido o sobredimensionados. | `MediaStorageService`, Nginx 10 MB | Verificado | Alta |
| RNF-011 | Persistencia | PostgreSQL debe conservar el dominio transaccional; MongoDB debe conservar auditoría y GridFS; el esquema SQL debe evolucionar mediante Flyway. | Reinicio con volúmenes debe conservar datos; Flyway debe aplicar/validar V1-V9. | JPA/Mongo/GridFS, migraciones y volúmenes Compose | Parcialmente verificable; no se ejecutó prueba de recuperación | Alta |
| RNF-012 | Privacidad | La ruta funcional para descargar documentos de verificación debe estar restringida a administradores. El sistema todavía debe separar en el lector de GridFS los documentos privados de las imágenes públicas; tampoco existe una política verificada de cifrado o retención. | La API administrativa debe rechazar a quien no tenga rol admin; una prueba de seguridad debe evidenciar que el lector público `/api/v1/imagenes/{id}` aún puede resolver cualquier identificador válido de GridFS. | Regla `/admin/**` y permiso público `/imagenes/**`; `MediaController` usa el lector genérico | Cumplimiento parcial; riesgo de acceso por identificador pendiente de corrección | Alta |
| RNF-013 | Configuración de secretos | En Docker Compose, credenciales SQL/Mongo y secreto JWT deben recibirse desde variables externas; el JWT debe tener al menos 32 bytes. | Compose debe fallar o el backend rechazar un secreto JWT demasiado corto; valores no deben fijarse en imagen. | `.env.example`, variables Compose, validación de `SecretKey` | Verificado para Compose; existe fallback de desarrollo en `application.yml` | Alta |
| RNF-014 | Portabilidad | El sistema debe poder construirse y ejecutarse en el entorno local integrado mediante Docker Compose con los cuatro servicios configurados. | Construir imágenes y superar healthchecks/prueba de humo documentada. | Dockerfiles, Compose y script PowerShell; producción no demostrada | Parcialmente verificable en repositorio | Media |
| RNF-015 | Observabilidad | El sistema debe proporcionar salud básica y registrar eventos de auditoría para las operaciones implementadas; no debe presentarse como monitoreo completo. | `/api/v1/sistema/salud` consulta ambas bases; eventos auditables se almacenan y panel admin puede leer recientes. | System API, Actuator y `AuditEvent` | Parcialmente verificable | Media |
| RNF-016 | Disponibilidad | El despliegue local debe usar healthchecks/reinicio configurados donde existen; no se declara SLA de 99 % ni alta disponibilidad. | Verificar healthchecks de PostgreSQL, MongoDB y Nginx y reinicio `unless-stopped`; registrar que backend no tiene healthcheck Compose. | `docker-compose.yml`, graceful shutdown | Parcialmente verificable | Media |
| RNF-017 | Mantenibilidad y pruebas | El código debe conservar validación estricta de TypeScript, migraciones versionadas y pruebas automatizadas; el nivel de cobertura debe declararse solo con resultados medidos. | Compilación strict, migraciones ordenadas y ejecución de suites; cobertura no afirmada sin reporte. | TS strict; un test frontend básico, test backend vacío y humo manual | Parcialmente verificable | Media |

No existen evidencias suficientes para declarar los valores antiguos de 99 % de disponibilidad, tres segundos máximos, 999 reservas sin pérdida, compatibilidad garantizada entre navegadores o interfaz completamente intuitiva.

## 8. Exclusiones y limitaciones actuales

### 8.1 Funciones no implementadas

- Cobro financiero real mediante pasarela externa.
- PayPal o cualquier proveedor de pagos externo.
- Generación, consulta o descarga de facturas.
- Cancelación de reservas y transición funcional a `CANCELADA`.
- Reembolsos.
- Envío de correo electrónico.
- Notificaciones remotas, push, SMS o mensajería; los toasts son locales.
- CRUD administrativo completo de usuarios o locales.
- Eliminación de locales.

### 8.2 Restricciones técnicas actuales

- El pago es un procesamiento interno simulado; la UI vigente envía el modo de aprobación.
- El cierre de sesión elimina datos locales, pero no revoca inmediatamente el JWT servidor.
- El filtro de catálogo por fecha no sustituye la disponibilidad exacta por franja.
- Los listados no implementan paginación generalizada.
- Los tipos de evento y amenidades son catálogos canónicos en código, no entidades administrables.
- Crear o editar un local lo despublica hasta una aprobación administrativa.
- El endpoint público de imágenes utiliza un lector genérico de GridFS; si se conoce un identificador válido, el backend no comprueba que el objeto sea realmente una imagen y no un documento de verificación.
- `frontend/src` es el frontend activo; existe una copia técnica anidada no activa.
- Los inicializadores de demostración se ejecutan en la configuración actual sin un perfil específico observado.

### 8.3 Propiedades no verificables con el repositorio

- Infraestructura productiva cloud, dominio/certificado público o CI/CD.
- Alta disponibilidad o SLA.
- Backups automatizados, restauración probada, RPO o RTO.
- Retención de auditoría, imágenes o documentos.
- Rendimiento/latencia bajo carga y capacidad máxima.
- Compatibilidad garantizada con versiones concretas de navegadores.
- Usabilidad demostrada mediante evaluación con usuarios.
- Cobertura de pruebas suficiente.

### 8.4 Arquitectura fuera de alcance

- Migración del frontend a otro framework.
- Sustitución de Spring Boot, PostgreSQL o MongoDB.
- Conversión del monolito modular en microservicios.
- Diseño de una topología productiva no presente en el repositorio.

## 9. Mejoras de usabilidad pendientes fuera del catálogo vigente

Las siguientes mejoras conocidas no se consideran funcionalidades implementadas ni criterios actualmente cumplidos:

- proporcionar mensajes más claros cuando una acción falla;
- explicar por qué no puede continuarse en determinados formularios;
- mejorar la intuición de algunos flujos;
- adaptar mejor la navegación visible al rol;
- evitar que el administrador vea opciones de cliente o propietario que no necesita utilizar.

Estas mejoras deberán especificarse y validarse en una iniciativa posterior de interfaz; no alteran los RF vigentes de este documento.

## 10. Matriz de trazabilidad respecto al documento anterior

### 10.1 Requisitos funcionales anteriores

| Requisito anterior | Requisito actualizado | Estado | Motivo | Evidencia principal | Observaciones |
|---|---|---|---|---|---|
| RF-001 | RF-001 | Ampliado | Añade cuenta activa, JWT y error de credenciales unificado | auth backend/frontend | Mantiene inicio de sesión |
| RF-002 | RF-002 | Reformulado | El cierre ocurre en cliente y no revoca JWT | `AuthService.logout` | No se inventa endpoint servidor |
| RF-003 | RF-003 | Reformulado | Separa filtros generales de disponibilidad exacta | `VenueService`, `ReservationAvailabilityService` | Fecha sola no garantiza franja |
| RF-004 | RF-004 | Conservado | La página inicial existe con catálogo e información | `features/public/home` | Redacción orientada al flujo actual |
| RF-005 | RF-005 | Ampliado | Precisa datos básicos sin fijar diseño | `VenueCard` | Solo locales activos |
| RF-006 | RF-006 | Ampliado | Incorpora imágenes, reglas, amenidades, reseñas y acciones | detalle + endpoints públicos | Acciones dependen de sesión |
| RF-007 | RF-007 | Ampliado | Añade validaciones, rol inicial, BCrypt y sesión JWT | registro y `Usuario` | Mismos datos esenciales |
| RF-008 | RF-008 | Reformulado | Limita administración a consulta/estado | admin usuarios | Retira interpretación CRUD |
| RF-009 | RF-009 | Reformulado | Define moderación y publicación | admin locales | Retira interpretación CRUD |
| RF-010 | RF-010 y RF-022 | Dividido | Separa solicitud del cliente y revisión administrativa | módulo `user`, GridFS | Conserva el proceso formal completo |
| RF-011 | RF-011 | Ampliado | Precisa propiedad, estados y ausencia de cancelación | reservas propias | Historial vigente |
| RF-012 | RF-012 | Reformulado | Expone fases, cálculos y pago interno simulado | módulo `reservation`, wizard | No hay cobro real |
| RF-013 | RF-013 | Ampliado | Añade creación/edición/imágenes/estado/revisión, sin eliminación | owner locales | Edición despublica |
| RF-014 | RF-014 | Reformulado | Actor corregido a propietario y bloqueo por rango | bloqueos | Añade regla de no superposición |
| RF-015 | RF-015 | Ampliado | Añade propiedad, fecha pasada, unicidad y promedio | reseñas | Calificación 1..5 |
| RF-016 | RF-016 | Ampliado | Precisa Web Share y portapapeles | `ShareService` | Sigue siendo público |
| RF-017 | RF-017 | Ampliado | Añade consulta, unicidad y ocultación de inactivos | favoritos | Mantiene alta/baja |
| RF-018 | RF-018 | Ampliado | Separa perfil, contraseña y baja lógica | perfil backend/frontend | No hay eliminación física |

Ningún RF-001 a RF-018 fue eliminado silenciosamente. Todos se conservan o se reformulan según la implementación; RF-010 se dividió para representar a sus dos actores.

### 10.2 Requisitos funcionales añadidos

| Nuevo requisito | Funcionalidad de origen | Evidencia | Razón por la que no estaba cubierta |
|---|---|---|---|
| RF-019 | Panel administrativo e indicadores/actividad | `AdminDashboardController`, dashboard Angular | RF-008/RF-009 solo trataban cuentas/locales |
| RF-020 | Panel del propietario | `OwnerDashboardController`, dashboard Angular | RF-013 solo trataba información de locales |
| RF-021 | Reservas recibidas por propietario | `/propietario/reservas`, vista owner | RF-011 se limita a reservas propias del cliente |
| RF-022 | Revisión administrativa de solicitudes | admin solicitudes/documento | RF-010 describía principalmente la solicitud del cliente |

El procesamiento interno simulado se incorporó en RF-012; las imágenes en RF-013; la moderación en RF-009/RF-013; y los bloqueos en RF-014. No se duplicaron como requisitos nuevos.

### 10.3 Requisitos no funcionales anteriores

| Requisito anterior | Requisito actualizado | Estado | Motivo | Evidencia principal | Observaciones |
|---|---|---|---|---|---|
| RNF-001 (interfaz intuitiva) | RNF-001 | Reformulado | Sustituye una cualidad subjetiva por capacidades observables; usabilidad completa requiere usuarios | rutas, formularios, toasts | La afirmación “intuitiva” se retira como cumplimiento |
| RNF-002 (Chrome/Edge/Firefox recientes) | RNF-002 | Reformulado | Exige matriz con versiones/resultados antes de garantizar compatibilidad | Angular/ES2022, sin pruebas cross-browser | No se declara cumplido |
| RNF-003 (99 % y 3 s) | RNF-003 y RNF-016 | Dividido y reformulado | Separa rendimiento de disponibilidad y retira métricas no medidas | índices/cache; healthchecks/restart | 99 % y 3 s quedan retirados como garantías actuales |
| RNF-004 (999 reservas sin pérdida) | RNF-004 y RNF-008 | Reemplazado conceptualmente | Sustituye número sin escenario por concurrencia e integridad verificables | transacciones, locks, constraints | 999 queda retirado como capacidad demostrada |
| RNF-005 (hash de contraseñas) | RNF-005 | Conservado y precisado | Identifica BCrypt y no exposición | `SecurityConfig`, DTO | Cumplimiento verificado |

### 10.4 RNF añadidos por evidencia actual

| Requisitos | Origen/evidencia | Justificación |
|---|---|---|
| RNF-006, RNF-007 | JWT, roles, filtro de cuenta | Autenticación/autorización activa no documentada con precisión |
| RNF-008, RNF-009 | Validaciones, constraints y `ProblemDetail` | Integridad y manejo de errores observables |
| RNF-010 a RNF-012 | límites de archivos, persistencia dual y protección parcial de documentos | Capacidades activas y riesgo de acceso por identificador documentado |
| RNF-013, RNF-014 | variables externas y Docker Compose | Configuración/portabilidad implementadas |
| RNF-015 a RNF-017 | salud/auditoría, disponibilidad parcial y mantenibilidad/pruebas | Propiedades técnicas relevantes sin garantías exageradas |

## 11. Evidencias y rutas consultadas

### Documentos de etapas anteriores

- `docs/actualizacion/01-analisis-arquitectura-actual.md`.
- `docs/actualizacion/02-matriz-trazabilidad-y-cambios.md`.
- `C:/Users/steve/Desktop/Diagramas a corregir/ADR.pdf`.
- `C:/Users/steve/Desktop/Diagramas a corregir/Requisitos_lojaVents.pdf`.

### Backend

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
- `backend/src/main/resources/application.yml`.
- `backend/src/main/resources/db/migration/V1__crear_usuarios.sql` a `V9__normalizar_tipos_evento.sql`.
- `backend/pom.xml` y `backend/Dockerfile`.

### Frontend e infraestructura

- `frontend/src/app/app.routes.ts`, `app.config.ts` y `app.ts`.
- `frontend/src/app/core/**`, `features/**`, `shared/**` y `layout/**`.
- `frontend/src/environments/environment.ts`.
- `frontend/angular.json`, `package.json`, `tsconfig*.json`, `Dockerfile`, `nginx.conf` y `proxy.conf.json`.
- `docker-compose.yml`, `.env.example` y `scripts/probar-sistema.ps1`.

## 12. Decisiones aún no verificables para una versión definitiva

El catálogo anterior describe fielmente el producto actual. Sin embargo, las siguientes definiciones operativas siguen sin evidencia suficiente y deben resolverse antes de declarar una especificación productiva definitiva:

1. Semántica exacta permitida para reservas que cruzan medianoche; el código y su mensaje no son plenamente consistentes.
2. Política de reactivación y retención de datos tras la baja lógica de una cuenta.
3. Política de privacidad, cifrado y retención para documentos, imágenes y auditoría.
4. Entorno productivo objetivo, gestión de secretos y certificados públicos.
5. SLA, objetivos de latencia/capacidad y escenarios de prueba de carga.
6. Matriz de navegadores/dispositivos y evaluación de usabilidad con usuarios.
7. Política de backup/restauración con RPO y RTO.
8. Límites/paginación esperados para crecimiento de catálogos, reservas, usuarios y paneles.

Estas decisiones no autorizan a afirmar funcionalidades o garantías inexistentes mientras permanezcan sin resolver.
