# LojaVents — Bloque 6: cuentas y solicitudes de propietario

Este parche convierte en persistentes las pantallas de perfil, solicitudes de propietario y administración de usuarios.

## Incluye

- Edición real de nombres y teléfono.
- Cambio de contraseña con validación de la contraseña actual.
- Baja voluntaria de la cuenta.
- Estados de cuenta `ACTIVO`, `SUSPENDIDO` e `INACTIVO`.
- Bloqueo inmediato de peticiones JWT para cuentas no activas.
- Solicitud real del rol `PROPIETARIO`.
- Aprobación o rechazo por un administrador.
- Conservación del rol `CLIENTE` al obtener `PROPIETARIO`.
- Renovación del JWT al recargar la aplicación para reflejar cambios de roles.
- Listado y activación/suspensión real de usuarios.
- Auditoría de operaciones en MongoDB.
- Migración Flyway `V5__crear_gestion_cuentas_y_propietarios.sql`.

## Aplicación

Desde la raíz del proyecto:

```powershell
docker compose down

Expand-Archive `
  -Path .\LojaVents-Bloque-6-Cuentas-Propietarios.zip `
  -DestinationPath . `
  -Force

docker compose up -d --build --force-recreate backend
```

No uses `docker compose down -v`, porque borraría los datos.

Revisa el arranque:

```powershell
docker compose logs -f --tail=200 backend
```

Debe aplicarse la migración 5 y aparecer `Started LojaVentsApplication`.

Luego inicia Angular sin reinstalar dependencias:

```powershell
cd .\frontend
npm.cmd start
```

## Pruebas sugeridas

1. Registra una cuenta de prueba.
2. Edita nombre y teléfono desde `/perfil` y recarga.
3. Cambia la contraseña y vuelve a iniciar sesión con la nueva.
4. Envía una solicitud desde `/convertirme-en-propietario`.
5. Entra como `admin@lojavents.ec` / `123456` y revísala en `/admin/verificaciones`.
6. Aprueba la solicitud y recarga la sesión del solicitante: debe aparecer el panel de propietario.
7. Desde `/admin/usuarios`, suspende una cuenta de prueba y comprueba que ya no puede usar la API ni iniciar sesión.
8. Reactívala desde la misma pantalla administrativa.

La referencia del documento de identidad almacena el nombre del archivo, no su contenido binario. Es una simulación académica de verificación documental.
