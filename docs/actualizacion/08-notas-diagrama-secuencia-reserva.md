# Diagrama de secuencia: creación y confirmación de una reserva

## 1. Propósito y alcance

Este documento respalda `08-diagrama-secuencia-reserva.puml`. Describe el flujo vigente desde que un cliente abre el asistente de reserva hasta que recibe una confirmación o un rechazo de pago. Se revisó el código Angular y Spring Boot actual; no se infiere comportamiento de versiones previas.

## 2. Flujo analizado

1. `BookingWizard` carga el local mediante `VenueService`.
2. El cliente ingresa fecha, hora, duración y asistentes; el frontend valida y consulta disponibilidad.
3. El cliente completa facturación y acepta reglas y política de cancelación.
4. `POST /api/v1/reservas` crea y persiste una `Reserva` en `EN_PROCESO`.
5. El botón actual del asistente llama a `POST /api/v1/reservas/{id}/pago-simulado` con `APPROVE`.
6. El backend vuelve a comprobar disponibilidad bajo bloqueo pesimista del local. Si el pago es aprobado, la reserva pasa a `CONFIRMADA`; si se rechaza, a `RECHAZADA`.
7. La respuesta presenta `publicReference`; el UUID `id` se conserva en el cliente solamente para invocar el endpoint de pago.

## 3. Participantes y responsabilidades

| Participante | Responsabilidad real |
|---|---|
| Cliente | Inicia el asistente, aporta datos y solicita el pago. |
| `BookingWizard` | Orquesta los cinco pasos, valida formularios, muestra errores y resultados. |
| `VenueService` | Carga el detalle público del local. |
| `BookingService` | Llama a disponibilidad, crea el borrador y delega el pago HTTP. |
| `PaymentService` | Fachada del asistente para `BookingService.processPayment`. |
| `PublicVenueController` | Expone detalle público y disponibilidad. |
| `ReservationController` | Expone creación, pago simulado y reservas propias. |
| `ReservationApplicationService` | Aplica autorización por propietario, importes, creación, transición y auditoría. |
| `ReservationAvailabilityService` | Verifica reglas temporales, bloqueos y solapamientos confirmados. |
| Repositorios | Recuperan y persisten usuario, local, reserva y auditoría. |
| `Reserva` y `PagoSimulado` | Mantienen estado y relación de pago del dominio. |

## 4. Punto de inicio y final de la interacción

El inicio es la apertura de la ruta de reserva de un local. El final es una pantalla de confirmación para `CONFIRMADA`, un mensaje de rechazo para `RECHAZADA`, o un error que mantiene al usuario en el paso correspondiente. La carga de la lista de locales y la pantalla posterior de “Mis reservas” no forman parte de la creación; solo se enlazan desde el resultado.

## 5. Validaciones de frontend

`BookingWizard` valida fecha y hora obligatorias, duración entera entre 1 y 12, asistentes enteros de al menos 1 y hasta la capacidad cargada del local. También exige ciudad, barrio y dirección de facturación no vacíos y las dos aceptaciones obligatorias. El botón “Comprobar disponibilidad” no avanza de paso si el formulario es inválido. El asistente calcula un importe estimado como horas por precio del local, más 8 % de tarifa.

## 6. Validaciones de backend

Spring valida `CreateReservationRequest` con `@Valid`: identificadores y fechas obligatorios, duración 1..12, asistentes 1..10000, dirección válida y ambas aceptaciones verdaderas. `ReservationApplicationService` convierte el sujeto JWT en UUID, exige que el usuario exista y esté activo, que el local exista y que los asistentes no superen su capacidad. `SecurityConfig` exige autenticación y excluye a administradores de `/api/v1/reservas/**`.

## 7. Comprobación de disponibilidad

`ReservationAvailabilityService.evaluate` comprueba que el local esté activo, duración 1..12, fecha futura o una hora futura para el día actual en `America/Guayaquil`, y que el fin no supere el límite implementado de 36:00 relativo al día solicitado. Después verifica solapamiento con `BloqueDisponibilidad` y con reservas `CONFIRMADA` en el día anterior, el día solicitado y el posterior. `EN_PROCESO` y `RECHAZADA` no ocupan horario.

La comprobación se ejecuta dos veces cuando se aprueba un pago: antes de crear el borrador y de nuevo justo antes de confirmar. Esta segunda comprobación usa `LocalEventoRepository.findByIdForUpdate`, anotado con `PESSIMISTIC_WRITE`.

## 8. Construcción de objetos de dominio

`new Reserva(...)` construye `PeriodoReserva`, `DatosFacturacion`, `ImporteReserva` y `ReferenciaPublicaReserva`. La referencia pública se genera con prefijo `LV-` y 32 caracteres hexadecimales aleatorios. El servicio calcula el subtotal, tarifa de servicio de 8 % y total con dos decimales; el objeto de valor comprueba que subtotal más tarifa sea igual al total.

## 9. Procesamiento de pago simulado

No existe pasarela, redirección ni llamada HTTP externa. `simulatePayment` elige el resultado desde `ModoPagoSimulado`: `APPROVE`, `REJECT_FUNDS`, `REJECT_PROVIDER` o `REJECT_APPLICATION`; genera una referencia con prefijo `PAY-OK` o `PAY-REJ`; y crea `PagoSimulado`. El asistente de producción ofrece solo `APPROVE`, pero los otros modos siguen siendo aceptados por el endpoint y están representados como una rama de rechazo real.

## 10. Transiciones de `EstadoReserva`

| Momento | Estado |
|---|---|
| Constructor y guardado del borrador | `EN_PROCESO` |
| Pago aprobado y disponibilidad reconfirmada | `CONFIRMADA` |
| Pago rechazado | `RECHAZADA` |

`COMPLETADA` fue migrado a `CONFIRMADA` en `V13`. `FINALIZADA` no existe. `CANCELADA` figura en el enum y DTO, pero no hay un endpoint o flujo funcional de cancelación en el código analizado.

## 11. Estados y resultados de `PagoSimulado`

`PagoSimulado` se crea para ambos resultados y se asocia a la reserva. Sus estados son `APROBADO` y `RECHAZADO`; su modo conserva la causa elegida y su mensaje explica el resultado. La asociación tiene `cascade = ALL`, por lo que `reservaRepository.save(reservation)` persiste también el pago asociado. Una reserva rechazada se conserva con su pago y motivo de rechazo.

## 12. Fragmentos UML utilizados

Se usan `alt` para formularios inválidos, disponibilidad no disponible, solicitud no autorizada o inválida, reserva no procesable y resultado aprobado/rechazado. No se usan `opt`, `loop` ni `ref`: no hay reintento de pago ni interacción reutilizable documentada. Se evita `destroy`, porque el rechazo solo cambia estado y persiste la entidad.

## 13. Flujos alternativos omitidos del dibujo por legibilidad

Los motivos concretos de indisponibilidad se agrupan en una rama: local inactivo, fecha pasada, hora pasada, duración fuera de rango, fin fuera del rango permitido, bloqueo del propietario y solapamiento confirmado. También se agrupan errores de usuario inexistente/inactivo, local inexistente, exceso de capacidad y conflicto de concurrencia. Todos devuelven el mensaje de `ApiException` al cliente; no progresan al pago.

## 14. Manejo de errores

Los errores de formulario no emiten HTTP. Los errores de disponibilidad previa se devuelven como `AvailabilityResponse` con `available=false` o como error HTTP. En creación y pago, `ApiException` conduce a 400, 401, 403, 404 o 409 según la causa. `BookingWizard.readError` presenta `detail` o `message` del error. Un 401 activa el interceptor de Angular, que limpia la sesión y navega al inicio de sesión.

## 15. Transacciones y concurrencia

`createDraft` y `simulatePayment` son transaccionales. La entidad `Reserva` tiene `@Version`, pero el mecanismo explícito del flujo de aprobación es el bloqueo pesimista `PESSIMISTIC_WRITE` sobre `LocalEvento`, seguido de una nueva evaluación de solapamientos confirmados. La primera comprobación previa no bloquea. No se detectó una gestión explícita de `OptimisticLockException`; un conflicto de concurrencia se propaga como error de la aplicación.

## 16. Elementos eliminados respecto al diagrama anterior

- PayPal, redirección externa y `Boundary2` genérico.
- Destrucción de la reserva al cancelar o rechazar.
- `loop (0,3)` y reintentos no implementados.
- Participantes `Actor1`, `Boundary1`, `Control1`, `Entity1` y `Entity2`.
- `COMPLETADA` como resultado de pago.

## 17. Elementos añadidos respecto al diagrama anterior

- Asistente Angular, servicios HTTP y dos endpoints reales.
- Borrador `EN_PROCESO`, pago interno y ramas de pago aprobado/rechazado.
- Facturación, aceptaciones obligatorias, importes definitivos y referencia pública opaca.
- Doble verificación de disponibilidad, bloqueos de propietario, reservas confirmadas y bloqueo pesimista.
- Auditoría de creación y resultado de pago.

## 18. Diferencias entre el modelo anterior y la aplicación vigente

El modelo vigente usa embeddables en `Reserva` y `PagoSimulado` asociado uno a uno. Una reserva existe antes del pago y no queda confirmada hasta que se aprueba; `CONFIRMADA` reúne pago aprobado, reserva confirmada y horario ocupado. El UUID técnico sigue en `ReservationResponse` para la segunda llamada interna del frontend, mientras que la interfaz presenta `ReferenciaPublicaReserva` como código público.

## 19. Rutas exactas del código consultado

- `frontend/src/app/features/booking/booking-wizard/booking-wizard.ts`
- `frontend/src/app/features/booking/booking-wizard/booking-wizard.html`
- `frontend/src/app/core/services/booking.service.ts`
- `frontend/src/app/core/services/payment.service.ts`
- `frontend/src/app/core/services/venue.service.ts`
- `frontend/src/app/core/interceptors/auth.interceptor.ts`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/api/ReservationController.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/application/ReservationApplicationService.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/application/ReservationAvailabilityService.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/api/dto/CreateReservationRequest.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/api/dto/PaymentSimulationRequest.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/api/dto/ReservationResponse.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/Reserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/PagoSimulado.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/PeriodoReserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/ReferenciaPublicaReserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/ImporteReserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/repository/ReservaRepository.java`
- `backend/src/main/java/ec/edu/unl/lojavents/venue/repository/LocalEventoRepository.java`
- `backend/src/main/java/ec/edu/unl/lojavents/venue/api/PublicVenueController.java`
- `backend/src/main/java/ec/edu/unl/lojavents/config/SecurityConfig.java`
- `backend/src/main/resources/db/migration/V13__renombrar_reserva_completada_a_confirmada.sql`

## 20. Pruebas utilizadas como evidencia

- `backend/src/test/java/ec/edu/unl/lojavents/reservation/application/ReservationAvailabilityServiceTest.java`: una reserva `CONFIRMADA` bloquea un periodo solapado.
- `backend/src/test/java/ec/edu/unl/lojavents/reservation/application/ReservationApplicationServiceTest.java`: el pago de una reserva ajena no es accesible.
- `backend/src/test/java/ec/edu/unl/lojavents/reservation/api/dto/CreateReservationRequestValidationTest.java`: se aceptan 1 y 12 horas, y se rechazan 0 y 13.
- `frontend/src/app/features/booking/booking-wizard/booking-wizard.validators.spec.ts`: duración entera en límites y rechazo de decimales.

## 21. Incertidumbres o comportamientos no implementados

No hay un flujo funcional de cancelación, reembolso, reintento de pago, pasarela externa, tarea programada ni transición a `FINALIZADA`. La API expone el UUID interno en la respuesta para permitir que Angular llame al pago; no se muestra como referencia al cliente. El diagrama no representa un controlador de excepciones global porque su implementación no cambia las interacciones centrales analizadas.

## Trazabilidad de mensajes UML

| Mensaje UML | Emisor | Receptor | Operación representada | Evidencia |
|---|---|---|---|---|
| Carga del local | `BookingWizard` / `VenueService` | `PublicVenueController` | `GET /api/v1/locales/{id}` | `booking-wizard.ts`, `venue.service.ts`, `PublicVenueController.detail` |
| Consulta previa | `BookingService` | `PublicVenueController` | `GET /locales/{id}/disponibilidad` | `booking.service.ts#checkAvailability`, `PublicVenueController.availability` |
| Evaluación | `PublicVenueController` | `ReservationAvailabilityService` | `check` / `evaluate` | `ReservationAvailabilityService.check` |
| Consulta de ocupación | `ReservationAvailabilityService` | `ReservaRepository` | `findByVenueDatesAndStatus(..., CONFIRMADA)` | `ReservationAvailabilityService.evaluate`, `ReservaRepository` |
| Creación | `BookingService` | `ReservationController` | `POST /reservas` | `booking.service.ts#create`, `ReservationController.create` |
| Borrador | `ReservationController` | `ReservationApplicationService` | `createDraft` | `ReservationController.create`, `ReservationApplicationService.createDraft` |
| Construcción | `ReservationApplicationService` | `Reserva` | constructor y objetos de valor | `ReservationApplicationService.createDraft`, `Reserva` |
| Persistencia inicial | `ReservationApplicationService` | `ReservaRepository` | `save` | `ReservationApplicationService.createDraft` |
| Pago | `PaymentService` / `BookingService` | `ReservationController` | `POST /reservas/{id}/pago-simulado` | `payment.service.ts`, `booking.service.ts#processPayment`, `ReservationController.simulatePayment` |
| Autorización de titularidad | `ReservationApplicationService` | `ReservaRepository` | `findOwnedByClient` | `ReservationApplicationService.simulatePayment` |
| Bloqueo y reconfirmación | `ReservationApplicationService` | repositorio de local / disponibilidad | `findByIdForUpdate`, `assertAvailable` | `ReservationApplicationService.simulatePayment`, `LocalEventoRepository.findByIdForUpdate` |
| Transición | `ReservationApplicationService` | `Reserva` | `aprobarPago` / `rechazarPago` | `ReservationApplicationService.simulatePayment`, `Reserva` |
| Auditoría | `ReservationApplicationService` | `AuditEventRepository` | `save(AuditEvent)` | `ReservationApplicationService.audit` |
