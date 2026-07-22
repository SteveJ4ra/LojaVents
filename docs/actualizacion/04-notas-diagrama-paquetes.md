# 1. Propósito y alcance

Este documento justifica el diagrama `04-diagrama-paquetes.puml`, cuya finalidad es representar la organización actual del código de LojaVents. El alcance se limita a paquetes, carpetas, agrupaciones arquitectónicas y dependencias generales respaldadas por imports o configuración activa.

La vista conserva la arquitectura Cliente-Servidor. El cliente es la aplicación Angular canónica ubicada en `frontend/src`; el servidor es el paquete raíz Java `ec.edu.unl.lojavents`. Los módulos internos del backend pertenecen a un único artefacto Spring Boot y, por tanto, no se interpretan como microservicios ni como unidades desplegables independientes.

No se muestran clases, interfaces, métodos, atributos, endpoints concretos ni elementos UML de componentes. Tampoco se muestran bases de datos, Docker, Nginx, servidores o contenedores porque corresponden a diagramas de componentes o despliegue, no a un diagrama de paquetes.

# 2. Paquetes representados

El diagrama contiene **17 cajas de paquete o agrupación**: dos raíces arquitectónicas y quince paquetes o carpetas principales existentes.

| Agrupación | Paquetes o carpetas visibles | Responsabilidad resumida |
|---|---|---|
| Frontend Angular: `frontend/src` | `core`, `features`, `layout`, `shared` | infraestructura del cliente, funciones por área, composición visual y elementos compartidos |
| Backend Spring Boot: `ec.edu.unl.lojavents` | `auth`, `user`, `venue`, `reservation`, `engagement`, `dashboard` | módulos funcionales y de negocio |
| Backend Spring Boot: `ec.edu.unl.lojavents` | `audit`, `storage`, `system`, `config`, `common` | capacidades transversales, almacenamiento, salud, configuración y errores comunes |

Los estereotipos distinguen las agrupaciones funcionales y transversales sin introducir paquetes ficticios. Los nombres Java se conservan exactamente como aparecen en el repositorio.

# 3. Paquetes o carpetas omitidos por legibilidad

- Las áreas `public`, `auth`, `venues`, `booking`, `customer`, `owner`, `admin` y `errors` se mencionan en una nota vinculada a `features`, pero no se dibujan como ocho cajas adicionales.
- Los subpaquetes repetidos `api`, `application`, `domain` y `repository` se resumen en una nota. No todos los módulos contienen las cuatro subcapas, por lo que dibujarlas como una plantilla uniforme habría falseado la estructura y duplicado información.
- Los archivos raíz de arranque y enrutamiento Angular, `environments`, recursos estáticos y archivos de configuración no se representan como paquetes.
- `frontend/lojavents-frontend` se omite porque es una duplicación técnica y no el frontend canónico activo. No se representa como una segunda aplicación.
- Las carpetas de pruebas, recursos Java, migraciones y archivos de configuración individuales quedan fuera del dibujo principal.
- No se muestran clases individuales del diagrama anterior porque el objetivo UML de esta etapa es describir paquetes y dependencias generales, no el modelo de clases.

# 4. Dependencias principales representadas

## Frontend

- `features` utiliza `core`, `shared` y `layout`.
- `layout` utiliza `core` para consultar estado de autenticación.
- `core` utiliza modelos definidos en `shared`.
- La raíz `frontend/src` consume lógicamente la API REST `/api/v1` del backend. Esta flecha no convierte ambos proyectos en un mismo paquete ni representa acceso directo a persistencia.

## Backend

- `auth` depende de `user` para autenticar y construir la identidad de usuarios.
- `user` depende de `storage` para almacenar documentos de solicitudes de propietario.
- `venue` depende de `user` para propietarios y de `reservation` para disponibilidad exacta.
- `reservation` depende de `user` y `venue` para asociar clientes y locales.
- `engagement` depende de `venue` y `reservation` para favoritos, reseñas y validación de reservas.
- `dashboard` depende de `engagement` y `reservation` para agregar métricas.
- `system` depende de `user` y `audit` para sus comprobaciones de salud.
- `storage` depende de `common` para reutilizar el manejo de errores API.

Todas las flechas internas representan dependencias de código observadas. No indican comunicación de red, procesos independientes ni relaciones entre bases de datos.

# 5. Dependencias omitidas del dibujo

La matriz de imports contiene más relaciones que las dibujadas. Se omitieron las siguientes para limitar cruces y mantener una lectura clara en una sola imagen:

- `auth` también depende de `audit` y `common`.
- `user` también depende de elementos API de `auth`, además de `audit` y `common`.
- `venue` también depende de `audit` y `common`.
- `reservation` también depende de `audit` y `common`.
- `engagement` también depende de `user`, `audit` y `common`.
- `dashboard` también depende de `user`, `venue` y `audit`.
- `config` depende de `audit`, `reservation`, `storage`, `user` y `venue` mediante configuración de seguridad e inicializadores activos.
- En el frontend, `shared` contiene una dependencia puntual hacia `core`; se omitió para evitar una flecha bidireccional junto a la dependencia más frecuente `core` hacia `shared`.
- Se omiten las dependencias internas entre `api`, `application`, `domain` y `repository` de cada módulo, así como imports dentro del mismo paquete raíz.

Estas omisiones son decisiones gráficas y no implican que las dependencias hayan dejado de existir.

# 6. Cambios frente al diagrama anterior

1. Se conserva la separación general entre cliente y servidor, pero se reemplaza la organización global por capas con la estructura modular real del repositorio.
2. Se eliminan las clases individuales, como controladores, servicios, DTO, repositorios, entidades, vistas y roles concretos.
3. `API`, `DTO`, `Servicios`, `Dominio` y `Repositorios` dejan de figurar como paquetes globales; en el código actual son subpaquetes repetidos dentro de varios módulos funcionales.
4. Se elimina “Base de datos” como supuesto paquete. La persistencia física corresponde a otros diagramas arquitectónicos.
5. “Seguridad” deja de mostrarse como un único paquete autónomo, ya que sus responsabilidades están distribuidas principalmente entre `auth`, `config` y `user`.
6. Se incorporan los paquetes reales antes omitidos: `engagement`, `dashboard`, `audit`, `storage`, `system`, `config` y `common`.
7. El frontend se actualiza a las carpetas canónicas `core`, `features`, `layout` y `shared`, sin representar la copia técnica anidada como una aplicación activa.

# 7. Decisiones de agrupación y legibilidad

- Se utiliza un único diagrama y orientación horizontal para mantener la relación Cliente-Servidor y permitir lectura en una imagen.
- El frontend y el backend son las dos cajas raíz. Dentro del backend, los estereotipos `funcional` y `transversal` diferencian responsabilidades sin añadir un tercer nivel de contenedores visuales.
- Se mantuvieron 17 cajas visibles, por debajo del límite aproximado de 20 indicado para esta etapa.
- Las áreas internas de `features` y las subcapas frecuentes del backend se consignan en notas en vez de repetirse como cajas.
- El estilo es monocromático, sin colores personalizados ni decoración ajena al propósito técnico.
- El backend se rotula expresamente como un único monolito modular. Ningún paquete se presenta como microservicio.
- No se genera SVG porque en el entorno no están disponibles PlantUML, un JAR local de PlantUML ni Graphviz. Conforme a las instrucciones, no se instaló ni descargó ninguna herramienta.

# 8. Evidencias y rutas consultadas

## Estructura del backend

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

Se revisaron las declaraciones `import ec.edu.unl.lojavents...` de los archivos Java. Como evidencias representativas se consultaron `AuthApplicationService`, `UserAccountApplicationService`, `VenueApplicationService`, `PublicVenueController`, `ReservationApplicationService`, `ReviewApplicationService`, `FavoriteApplicationService`, `DashboardApplicationService`, `SystemController` y `MediaStorageService`.

## Estructura del frontend

- `frontend/src/app/core/**`
- `frontend/src/app/features/**`
- `frontend/src/app/layout/**`
- `frontend/src/app/shared/**`
- `frontend/src/app/app.routes.ts`
- `frontend/src/environments/environment.ts`

Se revisaron los imports relativos de los archivos TypeScript para comprobar las dependencias entre las cuatro carpetas principales y la configuración activa de consumo de API.

## Documentación de contraste

- `docs/actualizacion/01-analisis-arquitectura-actual.md`
- `docs/actualizacion/02-matriz-trazabilidad-y-cambios.md`
- `docs/actualizacion/03-requisitos-software-actualizados.md`
- `DiagramaPaquetes.jpg`
- `ADR.pdf`
- `Requisitos_lojaVents.pdf`

La documentación previa se utilizó como contexto y trazabilidad. El código y la estructura activa del repositorio prevalecieron cuando fue necesario determinar qué debía aparecer en el nuevo diagrama.
