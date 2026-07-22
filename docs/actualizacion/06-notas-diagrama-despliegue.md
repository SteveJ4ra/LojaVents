# 1. Propósito y alcance

Este documento complementa el diagrama de despliegue verificado de LojaVents. Representa el entorno local e integrado definido con Docker Compose y no una infraestructura productiva, distribuida o alojada en la nube.

El diagrama se limita a los nodos, entornos de ejecución, artefactos y comunicaciones que se pueden demostrar en la configuración actual del repositorio.

# 2. Topología representada

La topología parte de un cliente web que accede desde un navegador a un único host Docker. Dentro del host, Docker Compose coordina cuatro servicios conectados a la misma red interna:

- Frontend.
- Backend.
- PostgreSQL.
- MongoDB.

El servicio Frontend es el único punto de entrada publicado. Nginx entrega la aplicación Angular y redirige las solicitudes de la API al Backend. El Backend se comunica internamente con PostgreSQL y MongoDB.

# 3. Nodos y entornos de ejecución

| Elemento | Representación | Responsabilidad verificada |
|---|---|---|
| Cliente web | Dispositivo | Equipo desde el que se utiliza LojaVents. |
| Navegador | Entorno de ejecución | Ejecuta la aplicación web y establece la conexión HTTPS. |
| Host Docker | Nodo | Equipo que aloja el despliegue integrado. |
| Docker Compose | Entorno de ejecución | Coordina la construcción, el arranque y las dependencias de los servicios. |
| Red Docker | Agrupación | Representa la red puente interna `lojavents-network`. |
| Frontend | Entorno de ejecución | Contenedor que aloja Nginx y la distribución compilada de Angular. |
| Nginx | Entorno de ejecución | Publica la interfaz web, termina TLS y actúa como proxy inverso de la API. |
| Backend | Entorno de ejecución | Contenedor de la aplicación Java. |
| JVM | Entorno de ejecución | Ejecuta el artefacto Java con Eclipse Temurin 21 JRE. |
| Spring Boot | Entorno de ejecución | Aloja la API REST y la lógica de aplicación. |
| PostgreSQL | Entorno de ejecución y base de datos | Servicio PostgreSQL 17 para los datos relacionales. |
| MongoDB | Entorno de ejecución y base de datos | Servicio MongoDB 8 para documentos y archivos mediante GridFS. |

# 4. Artefactos desplegados

El frontend despliega `Angular dist`, generado durante la fase de construcción con Node.js y servido por Nginx desde su directorio de contenido estático.

El backend despliega `app.jar`, nombre asignado al artefacto compilado al copiarlo en la imagen de ejecución. El contenedor lo inicia con `java -jar /app/app.jar`.

# 5. Conexiones y puertos

| Origen | Destino | Comunicación | Exposición |
|---|---|---|---|
| Navegador | Nginx | HTTPS en el puerto 443 | Publicada en el host. |
| Nginx | Spring Boot | HTTP y REST en el puerto 8080 | Interna a la red Docker. |
| Spring Boot | PostgreSQL | JPA sobre el puerto 5432 | Interna a la red Docker. |
| Spring Boot | MongoDB/GridFS | Protocolo MongoDB en el puerto 27017 | Interna a la red Docker. |

El puerto 80 también está publicado por Nginx para redirigir el tráfico HTTP hacia HTTPS. Los puertos 8080, 5432 y 27017 se declaran para comunicación entre servicios, pero no se publican directamente en el host.

# 6. Red y volúmenes

Los cuatro servicios pertenecen a `lojavents-network`, una red Docker de tipo `bridge`. Los nombres de servicio permiten que Nginx y el Backend resuelvan sus destinos dentro de esa red.

La configuración define los siguientes volúmenes y montajes:

- `postgres_data`, persistencia de PostgreSQL en `/var/lib/postgresql/data`.
- `mongo_data`, persistencia de MongoDB en `/data/db`.
- `./certs`, montaje de solo lectura en `/etc/nginx/certs` para el certificado y la clave TLS locales.

Estos elementos se documentan aquí y se omiten del diagrama para mantener una lectura clara. Los volúmenes persistentes no sustituyen una estrategia de copias de seguridad.

# 7. Healthchecks y dependencias de arranque

PostgreSQL verifica disponibilidad con `pg_isready`. MongoDB ejecuta una comprobación mediante `mongosh`, y Nginx comprueba localmente su endpoint de salud.

El Backend espera a que PostgreSQL y MongoDB estén saludables antes de iniciar. El Frontend espera a que el Backend haya iniciado. La configuración de Docker Compose no define un healthcheck propio para el Backend; por tanto, que el contenedor esté iniciado no garantiza por sí solo que la API ya responda correctamente.

# 8. Cambios frente al diagrama anterior

- Se reemplazaron servidores físicos independientes por un único host con Docker Compose, acorde con el despliegue verificado.
- Se mostraron los cuatro servicios reales y su red interna compartida.
- Se identificaron Nginx, JVM, Spring Boot, PostgreSQL 17 y MongoDB 8/GridFS con su función de ejecución efectiva.
- Se corrigió el artefacto del Backend a `app.jar` y se añadió la distribución compilada de Angular.
- Se distinguieron los puertos publicados de los puertos únicamente internos.
- Se eliminó la pasarela PayPal y cualquier infraestructura externa no presente en la configuración.

# 9. Limitaciones del despliegue verificado

El despliegue documentado corresponde al entorno local e integrado disponible en el repositorio. No demuestra alta disponibilidad, balanceo de carga, escalado horizontal, orquestación en nube, observabilidad centralizada, copias de seguridad automatizadas ni recuperación ante desastres.

La existencia de HTTPS depende de certificados locales montados en Nginx y no acredita una gestión productiva de certificados. Tampoco se ha verificado una publicación en Internet ni una plataforma de pago externa. La ausencia de healthcheck del Backend limita la supervisión integral del conjunto desde Docker Compose.

# 10. Evidencias y rutas consultadas

- `docker-compose.yml`: servicios, red, volúmenes, puertos, healthchecks y dependencias.
- `frontend/Dockerfile`: construcción de Angular y ejecución con Nginx.
- `frontend/nginx.conf`: HTTPS, contenido estático, redirección HTTP y proxy de `/api/`.
- `backend/Dockerfile`: construcción Maven, JVM de ejecución y despliegue de `app.jar`.
- `backend/pom.xml`: aplicación Spring Boot y configuración del artefacto Java.
- Configuración de aplicación del Backend: conexiones con PostgreSQL, MongoDB y GridFS.
- Documentación técnica previa del repositorio: alcance local del despliegue y contraste con el diagrama anterior.
