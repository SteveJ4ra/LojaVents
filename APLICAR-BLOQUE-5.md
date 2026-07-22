# LojaVents — Bloque 5: favoritos y reseñas verificadas

Este bloque incorpora favoritos persistentes, reseñas verificadas y el cálculo real de la calificación de cada local.

## Aplicación

1. Detener Angular con `Ctrl + C`.
2. Desde la raíz del proyecto ejecutar `docker compose down`.
3. Descomprimir este ZIP en la raíz con `-Force`.
4. Ejecutar `docker compose up -d --build --force-recreate backend`.
5. Revisar `docker compose logs -f --tail=180 backend`.
6. Iniciar Angular con `npm.cmd start`; no se agregaron dependencias npm.

Flyway aplicará `V4__crear_favoritos_y_resenas.sql`.

Se crea automáticamente una reserva pasada para `cliente@lojavents.ec` cuando no existe ninguna elegible, de modo que la publicación de reseñas pueda probarse inmediatamente.
