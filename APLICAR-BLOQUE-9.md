# Aplicar Bloque 9

1. Detener Angular si todavía está abierto con `npm start`.
2. Ejecutar `docker compose down`.
3. Descomprimir este ZIP en la raíz de `LojaVents`.
4. Generar los certificados:
   `.\scripts\generar-certificado-local.ps1`
5. Confiar en la autoridad local:
   `.\scripts\confiar-certificado-local.ps1`
6. Opcionalmente renovar el JWT:
   `.\scripts\generar-secreto-jwt.ps1 -ActualizarEnv`
7. Construir:
   `docker compose up -d --build`
8. Probar:
   `.\scripts\probar-sistema.ps1`
9. Abrir `https://localhost`.
