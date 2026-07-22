# Certificados locales

Esta carpeta se monta en el contenedor Nginx.

Los archivos reales se generan en la computadora del desarrollador mediante:

```powershell
.\scripts\generar-certificado-local.ps1
```

Después se puede confiar en la autoridad local mediante:

```powershell
.\scripts\confiar-certificado-local.ps1
```

No se deben publicar las claves privadas:

- `localhost.key`
- `lojavents-dev-ca.key`

El certificado local es únicamente para desarrollo y demostración. Un despliegue público debe utilizar un dominio real y un certificado emitido por una autoridad reconocida.
