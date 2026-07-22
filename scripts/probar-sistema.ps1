param(
    [string]$Email = "cliente@lojavents.ec",
    [string]$Password = "123456"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

function Invoke-CurlStatus {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url,
        [switch]$Insecure
    )

    $args = @("-s", "-o", "NUL", "-w", "%{http_code}")
    if ($Insecure) {
        $args += "-k"
    }
    $args += $Url

    $status = (& curl.exe @args).Trim()

    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo consultar $Url"
    }

    return $status
}

Write-Host "1. Verificando redirección HTTP a HTTPS..."
$httpStatus = Invoke-CurlStatus -Url "http://localhost/"
if ($httpStatus -notin @("301", "302", "307", "308")) {
    throw "Se esperaba una redirección HTTP, pero se recibió $httpStatus"
}
Write-Host "   Correcto: HTTP $httpStatus"

Write-Host "2. Verificando Nginx por HTTPS..."
$nginxStatus = Invoke-CurlStatus -Url "https://localhost/nginx-health" -Insecure
if ($nginxStatus -ne "200") {
    throw "Nginx HTTPS respondió con HTTP $nginxStatus"
}
Write-Host "   Correcto: HTTP 200"

Write-Host "3. Verificando la ruta SPA /locales..."
$routeStatus = Invoke-CurlStatus -Url "https://localhost/locales" -Insecure
if ($routeStatus -ne "200") {
    throw "La ruta SPA respondió con HTTP $routeStatus"
}
Write-Host "   Correcto: HTTP 200"

Write-Host "4. Verificando la API pública..."
$healthRaw = & curl.exe -k -s "https://localhost/api/v1/sistema/salud"
if ($LASTEXITCODE -ne 0) {
    throw "No fue posible consultar la API."
}

$health = $healthRaw | ConvertFrom-Json
if ($health.estado -ne "OK") {
    throw "La API no devolvió estado OK."
}
Write-Host "   Correcto: $($health.servicio)"

Write-Host "5. Verificando inicio de sesión JWT..."

$payload = @{
    email = $Email
    password = $Password
} | ConvertTo-Json -Compress

try {
    $login = Invoke-RestMethod `
        -Uri "https://localhost/api/v1/auth/login" `
        -Method Post `
        -ContentType "application/json; charset=utf-8" `
        -Body $payload
}
catch {
    throw "No fue posible iniciar sesión. $($_.Exception.Message)"
}

if ([string]::IsNullOrWhiteSpace($login.accessToken)) {
    throw "El inicio de sesión no devolvió un accessToken."
}

Write-Host "   Correcto: token JWT recibido"

Write-Host "6. Verificando endpoint autenticado..."

try {
    $me = Invoke-RestMethod `
        -Uri "https://localhost/api/v1/auth/me" `
        -Method Get `
        -Headers @{
            Authorization = "Bearer $($login.accessToken)"
        }
}
catch {
    throw "No fue posible consultar /api/v1/auth/me. $($_.Exception.Message)"
}

if ($me.email -ne $Email) {
    throw "El usuario autenticado no coincide con el usuario esperado."
}

Write-Host "   Correcto: $($me.email)"

Write-Host ""
Write-Host "Todas las pruebas esenciales finalizaron correctamente."
Write-Host ""
docker compose ps
