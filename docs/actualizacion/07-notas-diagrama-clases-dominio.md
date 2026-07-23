# Diagrama de clases del dominio

## 1. Propósito y alcance

Este diagrama reconstruye el modelo de dominio persistente vigente de LojaVents a partir del código de la rama `refactor/domain-value-objects`. No es una propuesta futura ni una vista de controladores, servicios, repositorios, DTO, seguridad o Frontend.

## 2. Entidades representadas

Las ocho entidades JPA actuales son `Usuario`, `SolicitudPropietario`, `LocalEvento`, `BloqueDisponibilidad`, `Reserva`, `PagoSimulado`, `Favorito` y `Resena`. Los roles de cliente, propietario y administrador son valores de `Rol`; no son subclases de `Usuario`.

## 3. Objetos de valor incorporados

Se utiliza de manera uniforme el estereotipo `<<embeddable>>`, porque estas clases están implementadas con `@Embeddable` y se almacenan dentro de la entidad que las contiene:

- `DireccionLocal`: sector y dirección del local.
- `PeriodoReserva`: fecha, hora de inicio y duración de la reserva.
- `DatosFacturacion`: ciudad, sector y dirección de facturación.
- `ImporteReserva`: subtotal, tarifa de servicio y total.
- `DocumentoIdentidad`: tipo y número de documento.
- `ReferenciaPublicaReserva`: referencia pública opaca de una reserva.

## 4. Atributos mostrados y omitidos

El diagrama incluye los atributos escalares que permiten comprender identidad, contenido, importes, trazabilidad y restricciones de cada objeto. Las asociaciones sustituyen los atributos de referencia a otras entidades, y los datos de los embeddables se muestran en su propia clase para no duplicarlos en la entidad contenedora.

Se omiten getters, setters, constructores, anotaciones JPA, nombres físicos de columnas y detalles de infraestructura. Los atributos persistidos cuyo tipo es una enumeración se omiten deliberadamente de la caja de su clase por la convención académica explicada en la sección 6. `Reserva.resenaEnviada` sí se representa porque continúa implementado.

## 5. Operaciones incluidas

Solo aparecen operaciones públicas reales que cambian estado, protegen invariantes o gestionan composición. Destacan la aprobación o rechazo de solicitudes, los cambios de publicación y bloqueos de `LocalEvento`, la aprobación o rechazo de pagos de `Reserva`, y las operaciones de intervalo de `PeriodoReserva`. La firma extensa de `LocalEvento.actualizar(...)` se abrevia con puntos suspensivos; el método existe y actualiza los datos mostrados del local. `ReferenciaPublicaReserva.generar()` y `desde(...)` son fábricas públicas reales que validan el formato de la referencia.

## 6. Convención académica de enumeraciones

Cada enumeración se representa una sola vez como un elemento UML independiente. Toda entidad o embeddable que la almacena se conecta mediante una dependencia discontinua dirigida (`Clase ..> Enum`). Por ello no se dibuja simultáneamente el atributo almacenado de tipo enum. Los parámetros de operaciones pueden conservar su tipo enum porque no representan un atributo persistido.

Las nueve enumeraciones vigentes son `Rol`, `EstadoUsuario`, `EstadoVerificacionPropietario`, `EstadoSolicitudPropietario`, `TipoDocumentoIdentidad`, `EstadoPublicacionLocal`, `EstadoReserva`, `EstadoPagoSimulado` y `ModoPagoSimulado`.

## 7. Relaciones y multiplicidades

- Una `SolicitudPropietario` tiene un `Usuario` solicitante; un usuario puede tener `0..*` solicitudes. El revisor de una solicitud es opcional y un usuario puede revisar `0..*` solicitudes.
- Un `LocalEvento` tiene un propietario y un usuario puede poseer `0..*` locales. Cada bloqueo pertenece a un local.
- Una `Reserva` tiene un cliente y un local; tanto un cliente como un local pueden participar en `0..*` reservas.
- `Favorito` conecta exactamente un cliente con un local; un cliente y un local pueden aparecer en `0..*` favoritos.
- Una `Resena` pertenece a una reserva, a su cliente y a su local. Una reserva puede tener `0..1` reseña; un usuario y un local pueden tener `0..*` reseñas.

## 8. Composiciones

Las composiciones reflejan valores embebidos y relaciones con ciclo de vida dependiente: `SolicitudPropietario *-- DocumentoIdentidad`, `LocalEvento *-- DireccionLocal`, `LocalEvento *-- BloqueDisponibilidad`, y `Reserva *-- PeriodoReserva`, `DatosFacturacion`, `ImporteReserva`, `ReferenciaPublicaReserva` y `PagoSimulado`. Los cuatro primeros valores de reserva se almacenan embebidos. Los bloqueos y el pago usan cascada y eliminación de huérfanos en sus relaciones JPA.

## 9. Restricción de duración

`PeriodoReserva.duracionHoras` es un entero entre 1 y 12, inclusive. Esta regla está protegida por `PeriodoReserva.DURACION_MINIMA_HORAS` y `DURACION_MAXIMA_HORAS`, y las operaciones `inicio()`, `fin()` y `seSolapaCon(...)` usan ese mismo periodo.

## 10. Estado de la reserva

`EstadoReserva` contiene `EN_PROCESO`, `CONFIRMADA`, `RECHAZADA` y `CANCELADA`. La migración V13 reemplaza los datos históricos `COMPLETADA` por `CONFIRMADA`; `COMPLETADA` no pertenece al modelo vigente.

`CONFIRMADA` significa que el pago fue aprobado, la reserva fue confirmada y el horario quedó bloqueado. No significa que el evento haya ocurrido. `FINALIZADA` no está implementado y no se muestra como valor ni como transición.

## 11. Publicación de locales y compatibilidad

El estado conceptual del local es `EstadoPublicacionLocal`: `PENDIENTE_REVISION`, `PUBLICADO` e `INACTIVO`. V12 lo incorpora y V14 mantiene temporalmente sincronizadas las columnas heredadas `activo` y `pendiente_revision`. Por esa razón, los campos derivados `active` y `pendingReview` de `VenueResponse` continúan por compatibilidad, pero no se muestran como estados independientes del dominio.

## 12. Referencia pública de reserva

`ReferenciaPublicaReserva` es un embeddable compuesto por `Reserva`. Se genera aleatoriamente con el prefijo `LV-`, es opaca, única e independiente de la clave primaria UUID. No reemplaza las verificaciones de autorización. El UUID interno sigue presente temporalmente en algunos contratos, como `ReservationResponse`, para compatibilidad.

## 13. Documento de identidad histórico

Las solicitudes nuevas construyen `DocumentoIdentidad` con un tipo obligatorio. V11 solo clasifica automáticamente documentos históricos con diez dígitos inequívocos como cédula; los demás pueden conservar tipo nulo hasta su revisión. Esta tolerancia histórica no modifica la regla de creación para solicitudes nuevas.

## 14. Restricciones importantes fuera del dibujo

El diagrama no carga cada caja con todas las restricciones: el correo de usuario es único, un favorito es único por cliente y local, un pago y una reseña son únicos por reserva, la referencia del pago es única y los importes no pueden ser negativos. `ImporteReserva` exige además que subtotal más tarifa de servicio sea igual al total y que los importes tengan dos decimales. `DocumentoIdentidad` valida el formato según su tipo y `LocalEvento.aprobarRevision()` solo publica un local pendiente de revisión.

## 15. Diferencias frente al diagrama anterior

La versión anterior mostraba datos de los embeddables dentro de las entidades, no incluía los seis objetos de valor implementados, mantenía `COMPLETADA`, no mostraba `EstadoPublicacionLocal` ni `TipoDocumentoIdentidad`, y duplicaba enums como atributos y dependencias. Esta versión corrige esos puntos, añade las nueve enumeraciones reales, las composiciones vigentes y las operaciones de dominio existentes.

## 16. Evidencia consultada

Entidades y embeddables:

- `backend/src/main/java/ec/edu/unl/lojavents/user/domain/Usuario.java`
- `backend/src/main/java/ec/edu/unl/lojavents/user/domain/SolicitudPropietario.java`
- `backend/src/main/java/ec/edu/unl/lojavents/user/domain/DocumentoIdentidad.java`
- `backend/src/main/java/ec/edu/unl/lojavents/venue/domain/LocalEvento.java`
- `backend/src/main/java/ec/edu/unl/lojavents/venue/domain/DireccionLocal.java`
- `backend/src/main/java/ec/edu/unl/lojavents/venue/domain/BloqueDisponibilidad.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/Reserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/PeriodoReserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/DatosFacturacion.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/ImporteReserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/ReferenciaPublicaReserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/PagoSimulado.java`
- `backend/src/main/java/ec/edu/unl/lojavents/engagement/domain/Favorito.java`
- `backend/src/main/java/ec/edu/unl/lojavents/engagement/domain/Resena.java`

Enumeraciones: `backend/src/main/java/ec/edu/unl/lojavents/user/domain/{Rol,EstadoUsuario,EstadoVerificacionPropietario,EstadoSolicitudPropietario,TipoDocumentoIdentidad}.java`, `backend/src/main/java/ec/edu/unl/lojavents/venue/domain/EstadoPublicacionLocal.java` y `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/{EstadoReserva,EstadoPagoSimulado,ModoPagoSimulado}.java`.

Migraciones: `backend/src/main/resources/db/migration/V1__crear_usuarios.sql` a `V14__sincronizar_estado_publicacion_heredado.sql`, con contraste específico en V10, V11, V12, V13 y V14.

Compatibilidad y pruebas: `backend/src/main/java/ec/edu/unl/lojavents/reservation/api/dto/ReservationResponse.java`, `backend/src/main/java/ec/edu/unl/lojavents/user/api/dto/OwnerRequestResponse.java`, `backend/src/main/java/ec/edu/unl/lojavents/venue/api/dto/VenueResponse.java`, `backend/src/test/java/ec/edu/unl/lojavents/migration/DomainRefactorMigrationTest.java`, `backend/src/test/java/ec/edu/unl/lojavents/user/domain/DocumentoIdentidadTest.java`, `backend/src/test/java/ec/edu/unl/lojavents/reservation/domain/{PeriodoReservaTest,ImporteReservaTest,ReferenciaPublicaReservaTest,ReservaTest}.java`, `backend/src/test/java/ec/edu/unl/lojavents/venue/domain/LocalEventoTest.java` y `backend/src/test/java/ec/edu/unl/lojavents/reservation/api/dto/DtoCompatibilityTest.java`.

Decisiones posteriores: `docs/DECISIONES_FUTURAS_DOMINIO.md`.

## 17. Decisiones futuras fuera del modelo vigente

La posible incorporación futura de `FINALIZADA` depende de definir una transición, el tratamiento de zona horaria y el mecanismo que la ejecutaría. Esa decisión está documentada como futura y no se representa en este diagrama.
