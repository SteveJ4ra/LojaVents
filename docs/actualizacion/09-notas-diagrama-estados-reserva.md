# Diagrama de estados: Reserva

## 1. Propósito y alcance

Este documento respalda `09-diagrama-estados-reserva.puml`. Representa solamente el ciclo de vida implementado de la entidad `Reserva`. No modela el flujo técnico del frontend, la disponibilidad, persistencia, auditoría ni la máquina de estados de `PagoSimulado`.

## 2. Fuente de verdad consultada

Prevalece el código Java y Angular vigente. Se revisaron el dominio de reservas, `ReservationApplicationService`, los endpoints de reserva, el asistente Angular, migraciones Flyway, pruebas y `docs/DECISIONES_FUTURAS_DOMINIO.md`. Los documentos antiguos que aún usan “completada” se consideran históricos y no definen el diagrama.

## 3. Enum `EstadoReserva` vigente

`EstadoReserva` contiene exactamente `EN_PROCESO`, `CONFIRMADA`, `RECHAZADA` y `CANCELADA`.

## 4. Estado inicial

Una `Reserva` nueva inicia en `EN_PROCESO`. El atributo se inicializa como `private EstadoReserva estado = EstadoReserva.EN_PROCESO`; el constructor no lo modifica. `ReservationApplicationService.createDraft` construye y persiste esa reserva antes de solicitar el pago.

## 5. Estados alcanzables

Los estados alcanzables por operaciones funcionales son `EN_PROCESO`, `CONFIRMADA` y `RECHAZADA`. `CONFIRMADA` se alcanza solo mediante `aprobarPago(pago)`; `RECHAZADA`, solo mediante `rechazarPago(pago, motivo)`.

## 6. Estados terminales

`CONFIRMADA` y `RECHAZADA` son terminales operativos: el código no expone ninguna transición saliente desde ellos. No se dibuja un pseudestado final `[*]` porque ambas reservas siguen persistidas, pueden consultarse y el modelo no define un cierre o destrucción de su ciclo de vida. `CANCELADA` no es alcanzable por el comportamiento actual.

## 7. Transiciones reales

| Estado origen | Evento u operación | Guarda | Acción | Estado destino | Evidencia |
|---|---|---|---|---|---|
| Pseudestado inicial | creación de `Reserva` | — | Valor inicial del atributo `estado` | `EN_PROCESO` | `Reserva.java`, atributo `estado`; `ReservationApplicationService.createDraft` |
| `EN_PROCESO` | `aprobarPago(pago)` | Resultado de pago aprobado y disponibilidad reconfirmada por el servicio de aplicación | Asocia `PagoSimulado`, limpia motivo y actualiza fecha | `CONFIRMADA` | `Reserva.aprobarPago`; `ReservationApplicationService.simulatePayment` |
| `EN_PROCESO` | `rechazarPago(pago, motivo)` | Resultado de pago rechazado | Asocia `PagoSimulado`, conserva motivo y actualiza fecha | `RECHAZADA` | `Reserva.rechazarPago`; `ReservationApplicationService.simulatePayment` |

## 8. Eventos y operaciones que disparan transiciones

Las únicas operaciones públicas de `Reserva` que cambian `estado` son `aprobarPago(PagoSimulado)` y `rechazarPago(PagoSimulado, String)`. `ReservationApplicationService.simulatePayment` determina el resultado mediante `outcomeFor(request.mode())` y llama a una de ellas. La creación del borrador ocurre en `createDraft`; por eso el pseudestado inicial apunta directamente a `EN_PROCESO` sin inventar una operación adicional de dominio.

## 9. Guardas y acciones

La aprobación se ejecuta solo en la rama de resultado aprobado. Antes de esa llamada, el servicio exige que la reserva esté en `EN_PROCESO`, bloquea el local y reconfirma disponibilidad. La rama rechazada se ejecuta para `REJECT_FUNDS`, `REJECT_PROVIDER` o `REJECT_APPLICATION`. Estas condiciones pertenecen a la aplicación, no a guardas declaradas dentro de `Reserva`; por ese motivo el diagrama conserva las transiciones directas y deja las precondiciones en estas notas.

## 10. Resultado del pago aprobado

`PagoSimulado` se crea con `EstadoPagoSimulado.APROBADO`. `Reserva.aprobarPago` lo asocia y asigna `EstadoReserva.CONFIRMADA`. `CONFIRMADA` significa pago aprobado, reserva confirmada y horario ocupado para las comprobaciones de disponibilidad; no significa que el evento haya ocurrido.

## 11. Resultado del pago rechazado

`PagoSimulado` se crea con `EstadoPagoSimulado.RECHAZADO`. `Reserva.rechazarPago` lo asocia, guarda el motivo y asigna `EstadoReserva.RECHAZADA`. La reserva rechazada se persiste por cascada junto con el pago; no se elimina ni se destruye.

## 12. Situación actual de `CANCELADA`

`CANCELADA` existe en el enum, DTO, modelo de frontend, indicadores y paneles. No se encontró método de dominio, endpoint REST, operación de servicio, vista ni transición administrativa que lleve una reserva a ese estado. Se omite del flujo dibujado para no representar una transición inexistente.

## 13. Ausencia de `COMPLETADA`

`COMPLETADA` no pertenece al enum actual. La migración `V13__renombrar_reserva_completada_a_confirmada.sql` convierte datos históricos de `COMPLETADA` a `CONFIRMADA`. No aparece en el diagrama.

## 14. Ausencia de `FINALIZADA`

`FINALIZADA` no está implementado como enum, método, endpoint ni transición. `DECISIONES_FUTURAS_DOMINIO.md` únicamente la documenta como una posible decisión futura, junto con las reglas temporales que aún deberían definirse. No aparece en el diagrama.

## 15. Reintentos de pago

No hay contador de intentos, límite de cuatro intentos, operación que devuelva `RECHAZADA` a `EN_PROCESO` ni endpoint de reintento. `simulatePayment` rechaza procesar una reserva cuyo estado no sea `EN_PROCESO`. Los botones de “Reintentar” de algunos paneles recargan datos y no corresponden al pago de una reserva.

## 16. Diferencias frente al diagrama anterior

El diagrama vigente reemplaza `COMPLETADO` por `CONFIRMADA`, elimina el rombo de intentos, el bucle de reintentos, las flechas de cancelación y los pseudestados finales separados. La máquina resultante representa las dos únicas transiciones de estado realmente codificadas.

## 17. Elementos eliminados del diagrama anterior

- `COMPLETADO` y cualquier referencia a “reserva completada” como estado vigente.
- Contador, límite de cuatro intentos y retorno desde rechazo al procesamiento.
- Cancelación, destrucción de reserva y reembolso.
- Estados finales decorativos sin una operación de cierre real.

## 18. Evidencias exactas del código

- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/EstadoReserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/Reserva.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/PagoSimulado.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/EstadoPagoSimulado.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/ModoPagoSimulado.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/application/ReservationApplicationService.java`
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/api/ReservationController.java`
- `frontend/src/app/features/booking/booking-wizard/booking-wizard.ts`
- `frontend/src/app/core/services/booking.service.ts`
- `frontend/src/app/core/services/payment.service.ts`
- `frontend/src/app/features/customer/bookings/my-bookings.ts`
- `frontend/src/app/features/owner/bookings/owner-bookings.ts`
- `backend/src/main/resources/db/migration/V13__renombrar_reserva_completada_a_confirmada.sql`
- `docs/DECISIONES_FUTURAS_DOMINIO.md`

## 19. Pruebas consultadas

- `backend/src/test/java/ec/edu/unl/lojavents/reservation/domain/ReservaTest.java`: verifica que `aprobarPago` deja la reserva en `CONFIRMADA` y asocia un pago aprobado.
- `backend/src/test/java/ec/edu/unl/lojavents/reservation/application/ReservationApplicationServiceTest.java`: verifica que no se puede procesar el pago de una reserva ajena.
- `backend/src/test/java/ec/edu/unl/lojavents/reservation/application/ReservationAvailabilityServiceTest.java`: verifica que una reserva `CONFIRMADA` bloquea un período solapado.
- `backend/src/test/java/ec/edu/unl/lojavents/reservation/api/dto/DtoCompatibilityTest.java`: verifica la respuesta `CONFIRMADA` tras aprobación.
- `backend/src/test/java/ec/edu/unl/lojavents/migration/DomainRefactorMigrationTest.java`: verifica la migración de `COMPLETADA` a `CONFIRMADA`.

## 20. Incertidumbres o estados sin transición implementada

No se encontró una implementación verificable para alcanzar `CANCELADA`, cancelar una reserva confirmada o en proceso, reembolsar, reintentar pago, finalizar automáticamente una reserva o aplicar una transición administrativa. La ausencia de estas operaciones se documenta, no se suplanta con transiciones hipotéticas.
