$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$caCert = Join-Path $projectRoot "certs\lojavents-dev-ca.crt"

if (-not (Test-Path $caCert)) {
    throw "No existe $caCert. Ejecuta primero .\scripts\generar-certificado-local.ps1"
}

$imported = Import-Certificate `
    -FilePath $caCert `
    -CertStoreLocation "Cert:\CurrentUser\Root"

if (-not $imported) {
    throw "Windows no pudo importar el certificado."
}

Write-Host "Certificado de desarrollo agregado a los certificados de confianza del usuario actual."
Write-Host "Cierra y vuelve a abrir Chrome o Edge antes de probar https://localhost"
