param(
    [Parameter(Mandatory=$true)]
    [string]$TagName,
    [string]$ReleaseName = "FitTracker $TagName",
    [string]$ReleaseNotes = "Nueva versión de FitTracker con mejoras y actualizaciones.",
    [string]$ApkPath = "FitTracker.apk"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ApkPath)) {
    Write-Error "No se encuentra el APK en $ApkPath. Compílalo primero con .\gradlew assembleDebug."
}

# Obtener token de GitHub desde Git Credential Manager
$credOutput = "protocol=https`nhost=github.com" | git credential fill
$token = ($credOutput | Select-String "password=").ToString().Replace("password=", "").Trim()

if (-not $token) {
    Write-Error "No se pudo obtener el token de autenticación de GitHub."
}

$repo = "hugh0ah/gym-app"
$headers = @{
    "Authorization" = "Bearer $token"
    "Accept"        = "application/vnd.github+json"
    "User-Agent"    = "FitTracker-Release-Agent"
}

Write-Host "Creando release $TagName en $repo..."
$releaseBody = @{
    tag_name         = $TagName
    name             = $ReleaseName
    body             = $ReleaseNotes
    draft            = $false
    prerelease       = $false
} | ConvertTo-Json

$createResponse = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases" -Method Post -Headers $headers -Body $releaseBody

$releaseId = $createResponse.id
$uploadUrl = $createResponse.upload_url -replace '\{\?name,label\}', "?name=FitTracker.apk"

Write-Host "Subiendo APK a la release..."
$apkBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $ApkPath).Path)

$uploadHeaders = @{
    "Authorization"  = "Bearer $token"
    "Content-Type"   = "application/vnd.android.package-archive"
    "User-Agent"     = "FitTracker-Release-Agent"
}

$uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes

Write-Host "Release publicada con éxito!" -ForegroundColor Green
Write-Host "URL de la release: $($createResponse.html_url)" -ForegroundColor Cyan
Write-Host "Descarga directa: $($uploadResponse.browser_download_url)" -ForegroundColor Yellow
