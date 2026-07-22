# 1. Propósito y alcance

Esta versión amplía el diagrama de clases del dominio persistente de LojaVents para que resulte suficientemente expresivo en una entrega académica sin separarse del código vigente. La revisión se realizó sobre las ocho entidades JPA activas, sus siete enumeraciones persistidas directamente, las migraciones y el comportamiento implementado dentro de las propias entidades.

El diagrama no intenta representar todas las clases del Backend. Se excluyen controladores, servicios de aplicación, repositorios, DTO, mappers, configuración, seguridad, infraestructura, Frontend y documentos técnicos de MongoDB. `AuditEvent` continúa fuera del modelo principal porque es persistencia técnica de auditoría, no una entidad JPA del dominio de reservación.

# 2. Nuevo nivel de detalle

La versión anterior privilegiaba una vista mínima del modelo. La versión actual conserva las mismas ocho entidades y siete enumeraciones, pero incorpora:

- estados y roles dentro de las clases que los persisten;
- atributos de publicación, revisión y trazabilidad;
- datos completos de facturación y resultado de reserva;
- colecciones de valores de los locales;
- operaciones reales que expresan transiciones o responsabilidades de dominio;
- agrupación visual por módulos funcionales;
- enumeraciones en una zona separada y conectadas con dependencias dirigidas.

`Usuario` sigue siendo la única entidad de identidad. `CLIENTE`, `PROPIETARIO` y `ADMINISTRADOR` son valores de `Rol`; no existe herencia persistente para esos perfiles.

# 3. Atributos recuperados y omitidos

| Entidad | Atributos recuperados o conservados | Omisiones justificadas |
|---|---|---|
| `Usuario` | Roles, estado de cuenta, verificación propietaria, credencial persistida y fecha de creación. | No se omite ningún atributo persistente activo de la entidad. |
| `SolicitudPropietario` | Estado, metadatos del documento, comentario administrativo y fechas de solicitud/revisión. | `documentoTamano` se omite por ser metadato técnico de almacenamiento con poca relevancia visual. Las referencias a solicitante y revisor se muestran como asociaciones. |
| `LocalEvento` | Descripciones, ubicación, precio, capacidad, calificación, total de reseñas, publicación, revisión, política y colecciones de valores. | `creadoEn` se omite para limitar la altura; `actualizadoEn` conserva la trazabilidad temporal. La colección de bloqueos se representa como composición. |
| `BloqueDisponibilidad` | Fecha, rango horario y motivo. | La referencia al local se muestra como composición. |
| `Reserva` | Dirección completa de facturación, importes, estado, rechazo, aceptaciones, indicador de reseña, timestamps y versión. | Las referencias a cliente, local y pago se muestran como relaciones. No se omiten atributos escalares relevantes. |
| `PagoSimulado` | Estado, modo, referencia, mensaje y fecha de procesamiento. | La referencia a la reserva se muestra como composición. |
| `Favorito` | Identificador y fecha de creación. | Cliente y local se muestran como asociaciones. La entidad no contiene otros atributos escalares. |
| `Resena` | Calificación, comentario y timestamps. | Reserva, cliente y local se muestran como asociaciones. |

Los tipos de evento, amenidades, reglas e imágenes de `LocalEvento` son colecciones persistentes de valores `String`, no entidades. Los tipos de evento y las amenidades se validan contra catálogos canónicos en la aplicación.

# 4. Enumeraciones

Los atributos enum ahora aparecen explícitamente dentro de sus clases y las enumeraciones se mantienen como elementos UML independientes. Las dependencias discontinuas dirigidas facilitan localizar la definición de cada tipo sin sustituir el atributo que se persiste.

| Entidad | Atributos enum |
|---|---|
| `Usuario` | `roles: Set<Rol>`, `estado: EstadoUsuario`, `estadoVerificacionPropietario: EstadoVerificacionPropietario` |
| `SolicitudPropietario` | `estado: EstadoSolicitudPropietario` |
| `Reserva` | `estado: EstadoReserva` |
| `PagoSimulado` | `estado: EstadoPagoSimulado`, `modo: ModoPagoSimulado` |

Las siete enumeraciones contienen todos los valores definidos en el código. `EstadoReserva.CANCELADA` existe como valor persistible, aunque no se encontró una operación funcional activa de cancelación o reembolso. `DecisionSolicitudPropietario` no se incluye porque se usa como entrada de DTO/servicio y no como estado de una entidad.

# 5. Operaciones incluidas

Se incluyen únicamente métodos públicos reales que modifican estado o expresan comportamiento de dominio. Se omiten getters, constructores, métodos privados y enlaces técnicos.

- `Usuario`: actualización de perfil y credencial, cambio de estado, actualización de verificación y asignación de rol.
- `SolicitudPropietario`: aprobación y rechazo con usuario revisor y comentario.
- `LocalEvento`: cambio de publicación, solicitud/aprobación de revisión, destacado, calificación y gestión de bloqueos.
- `Reserva`: aprobación o rechazo del pago y marcado de reseña enviada.

El método extenso `LocalEvento.actualizar(...)` existe, pero se omite porque su firma repite gran parte de los atributos y perjudica la legibilidad. `BloqueDisponibilidad`, `PagoSimulado`, `Favorito` y `Resena` no exponen comportamiento público adicional relevante más allá de construcción y consulta; no se inventaron operaciones para ellas.

# 6. Relaciones, multiplicidades y composiciones

- Cada `SolicitudPropietario` tiene un solicitante; un `Usuario` puede registrar `0..*` solicitudes históricas.
- Cada solicitud posee `0..1` revisor; un usuario administrador puede revisar `0..*` solicitudes.
- Cada `LocalEvento` pertenece a un propietario; un usuario puede poseer `0..*` locales.
- Cada `Reserva` pertenece a un cliente y a un local; ambos pueden acumular `0..*` reservas.
- `Favorito` es una entidad asociativa: cada instancia pertenece a un usuario y un local, y ambos admiten `0..*` favoritos.
- Cada `Resena` pertenece a una reserva, un cliente y un local. Una reserva admite `0..1` reseña; un usuario y un local admiten `0..*` reseñas.

Solo se utilizan dos composiciones respaldadas por cascada total y eliminación de huérfanos:

- `LocalEvento *-- BloqueDisponibilidad`.
- `Reserva *-- PagoSimulado`.

Las migraciones también respaldan el borrado en cascada de bloqueos y pagos al desaparecer su propietario de ciclo de vida. Las demás relaciones son asociaciones sólidas.

# 7. Restricciones fuera del diagrama

- El correo de `Usuario` es único y se normaliza funcionalmente a minúsculas.
- Un usuario no puede repetir un mismo valor de `Rol`.
- Solo puede existir una solicitud de propietario `PENDIENTE` por usuario.
- Solo puede existir un `Favorito` por combinación cliente-local.
- Cada reserva admite un único `PagoSimulado`; su referencia también es única.
- Cada reserva admite una única `Resena`.
- La calificación de una reseña debe estar entre 1 y 5.
- La duración de una reserva debe estar entre 1 y 12 horas y sus importes no pueden ser negativos.
- La capacidad y el precio por hora de un local deben ser positivos.
- La hora inicial de un bloqueo debe ser anterior a su hora final.

Estas restricciones se mantienen en las notas para evitar cargar las cajas con detalles SQL.

# 8. Decisiones visuales

El diagrama utiliza orientación horizontal y cinco agrupaciones: Usuarios, Locales, Reservas y pagos, Interacción y Enumeraciones. Los tres primeros grupos forman el recorrido principal `Usuario -> LocalEvento -> Reserva -> PagoSimulado`; `Favorito` y `Resena` se ubican debajo del núcleo y los enums ocupan una franja separada.

Cada módulo usa un tono suave distinto, con fondo blanco, bordes sobrios, líneas ortogonales y sin sombras. Se emplean cuatro enlaces ocultos únicamente para estabilizar el orden horizontal de los grupos y las franjas inferiores; no representan relaciones del dominio.

# 9. Exclusiones y diferencias funcionales

Continúan excluidos `Cliente`, `Propietario` y `Administrador` como clases; `Direccion`, `TipoEvento`, `Disponibilidad`, `DetallePago`, `Factura`, `EstadoLocal`, `EstadoDisponibilidad` y `MetodoPago` tampoco existen como elementos persistentes actuales.

`LocalEvento` reemplaza al antiguo `Local`; `BloqueDisponibilidad` representa bloqueos concretos, no una disponibilidad precalculada; `PagoSimulado` sustituye a `DetallePago`; y `Favorito` se modela como entidad asociativa. No existe `Factura` ni una pasarela financiera externa implementada.

El modelo persistente admite `EstadoReserva.CANCELADA`, modos internos de rechazo de pago y campos de aceptación de políticas. Sin embargo, la interfaz activa solo inicia el modo de aprobación y no expone un flujo vigente de cancelación o reembolso. Esta diferencia entre capacidad persistente y comportamiento funcional se conserva explícitamente.

# 10. Evidencias consultadas

- `backend/src/main/java/ec/edu/unl/lojavents/user/domain/Usuario.java`.
- `backend/src/main/java/ec/edu/unl/lojavents/user/domain/SolicitudPropietario.java` y enums del módulo `user`.
- `backend/src/main/java/ec/edu/unl/lojavents/venue/domain/LocalEvento.java`.
- `backend/src/main/java/ec/edu/unl/lojavents/venue/domain/BloqueDisponibilidad.java`.
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/Reserva.java`.
- `backend/src/main/java/ec/edu/unl/lojavents/reservation/domain/PagoSimulado.java` y enums del módulo `reservation`.
- `backend/src/main/java/ec/edu/unl/lojavents/engagement/domain/Favorito.java`.
- `backend/src/main/java/ec/edu/unl/lojavents/engagement/domain/Resena.java`.
- `backend/src/main/resources/db/migration/V1__crear_usuarios.sql` a `V9__normalizar_tipos_evento.sql`.
- `docs/actualizacion/01-analisis-arquitectura-actual.md`.
- `docs/actualizacion/02-matriz-trazabilidad-y-cambios.md`.
- `docs/actualizacion/03-requisitos-software-actualizados.md`.
- `Diagrama de Clases.jpg`, utilizado como referencia del modelo anterior.
