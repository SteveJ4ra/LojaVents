param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$certDir = Join-Path $projectRoot "certs"
$caCert = Join-Path $certDir "lojavents-dev-ca.crt"
$caKey = Join-Path $certDir "lojavents-dev-ca.key"
$serverCert = Join-Path $certDir "localhost.crt"
$serverKey = Join-Path $certDir "localhost.key"
$extFile = Join-Path $certDir "localhost.ext"

New-Item -ItemType Directory -Path $certDir -Force | Out-Null

$requiredFiles = @($caCert, $caKey, $serverCert, $serverKey)
$alreadyExists = ($requiredFiles | Where-Object { Test-Path $_ }).Count -eq $requiredFiles.Count

if ($alreadyExists -and -not $Force) {
    Write-Host "Los certificados locales ya existen."
    Write-Host "Usa -Force para generarlos nuevamente."
    exit 0
}

@"
authorityKeyIdentifier=keyid,issuer
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=@alt_names

[alt_names]
DNS.1=localhost
IP.1=127.0.0.1
"@ | Set-Content -Path $extFile -Encoding ascii

$dockerMount = $certDir.Replace("\", "/")

Write-Host "Generando la autoridad certificadora local y el certificado HTTPS..."

docker run --rm `
    -v "${dockerMount}:/certs" `
    alpine:3.22 `
    sh -c @"
set -eu
apk add --no-cache openssl >/dev/null

rm -f /certs/lojavents-dev-ca.srl /certs/localhost.csr

openssl req -x509 -nodes -newkey rsa:3072 -sha256 -days 3650 \
  -keyout /certs/lojavents-dev-ca.key \
  -out /certs/lojavents-dev-ca.crt \
  -subj "/C=EC/ST=Loja/L=Loja/O=LojaVents/OU=Desarrollo/CN=LojaVents Development CA" \
  -addext "basicConstraints=critical,CA:TRUE" \
  -addext "keyUsage=critical,keyCertSign,cRLSign"

openssl req -nodes -newkey rsa:2048 -sha256 \
  -keyout /certs/localhost.key \
  -out /certs/localhost.csr \
  -subj "/C=EC/ST=Loja/L=Loja/O=LojaVents/OU=Desarrollo/CN=localhost"

openssl x509 -req \
  -in /certs/localhost.csr \
  -CA /certs/lojavents-dev-ca.crt \
  -CAkey /certs/lojavents-dev-ca.key \
  -CAcreateserial \
  -out /certs/localhost.crt \
  -days 825 \
  -sha256 \
  -extfile /certs/localhost.ext

rm -f /certs/localhost.csr /certs/localhost.ext /certs/lojavents-dev-ca.srl
"@

if ($LASTEXITCODE -ne 0) {
    throw "No se pudieron generar los certificados."
}

Write-Host ""
Write-Host "Certificados generados correctamente:"
Write-Host "  $serverCert"
Write-Host "  $serverKey"
Write-Host "  $caCert"
Write-Host ""
Write-Host "Siguiente paso:"
Write-Host "  .\scripts\confiar-certificado-local.ps1"
