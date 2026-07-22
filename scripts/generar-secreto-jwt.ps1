param(
    [switch]$ActualizarEnv
)

$ErrorActionPreference = "Stop"

$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()

try {
    $rng.GetBytes($bytes)
}
finally {
    $rng.Dispose()
}

$secret = [Convert]::ToBase64String($bytes)
Write-Host "Nuevo secreto JWT:"
Write-Host $secret

if (-not $ActualizarEnv) {
    Write-Host ""
    Write-Host "Para escribirlo automáticamente en .env ejecuta:"
    Write-Host "  .\scripts\generar-secreto-jwt.ps1 -ActualizarEnv"
    exit 0
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$envPath = Join-Path $projectRoot ".env"

if (-not (Test-Path $envPath)) {
    throw "No existe el archivo .env en la raíz del proyecto."
}

$content = Get-Content $envPath -Raw

if ($content -match "(?m)^JWT_SECRET=") {
    $content = [regex]::Replace(
        $content,
        "(?m)^JWT_SECRET=.*$",
        "JWT_SECRET=$secret"
    )
}
else {
    $content = $content.TrimEnd() + "`r`nJWT_SECRET=$secret`r`n"
}

$utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($envPath, $content, $utf8WithoutBom)
Write-Host "JWT_SECRET fue actualizado en $envPath"
