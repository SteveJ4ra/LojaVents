# Matriz de pruebas finales

| N.º | Módulo | Prueba | Resultado esperado |
|---:|---|---|---|
| 1 | Infraestructura | Abrir `http://localhost` | Redirección a `https://localhost` |
| 2 | Nginx | Consultar `/nginx-health` | HTTP 200 y texto `OK` |
| 3 | SPA | Recargar `/locales` o `/admin/usuarios` | Angular carga sin error 404 |
| 4 | API | Consultar `/api/v1/sistema/salud` | Estado `OK` |
| 5 | Autenticación | Iniciar sesión con credenciales válidas | JWT y datos del usuario |
| 6 | Autenticación | Usar una contraseña incorrecta | Acceso rechazado |
| 7 | Seguridad | Abrir una ruta administrativa como cliente | Acceso denegado |
| 8 | Cliente | Marcar un local como favorito | Se conserva después de recargar |
| 9 | Cliente | Crear reserva con pago exitoso | Reserva completada |
| 10 | Cliente | Simular pago rechazado | Reserva rechazada registrada |
| 11 | Disponibilidad | Repetir fecha y horario ocupado | Operación rechazada |
| 12 | Reseñas | Reseñar una reserva completada pasada | Reseña publicada |
| 13 | Reseñas | Repetir reseña de la misma reserva | Operación rechazada |
| 14 | Propietario | Crear o editar un local | Cambio persistente |
| 15 | Propietario | Crear un bloqueo | Horario deja de estar disponible |
| 16 | Propietario | Consultar panel | Métricas reales |
| 17 | Administración | Aprobar solicitud de propietario | Usuario obtiene ambos roles |
| 18 | Administración | Suspender una cuenta | Sesión y acceso rechazados |
| 19 | Administración | Consultar panel | Métricas y actividad reales |
| 20 | Persistencia | Reiniciar Docker | Los datos permanecen |

## Evidencias recomendadas

- `docker compose ps`.
- Navegador mostrando HTTPS.
- Inicio de sesión.
- Reserva completada.
- Reserva rechazada.
- Panel de propietario.
- Panel administrativo.
- Solicitud de propietario aprobada.
- Resultado de `.\scripts\probar-sistema.ps1`.
