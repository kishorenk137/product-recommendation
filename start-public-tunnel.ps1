# PowerShell script for launching instant public tunnel
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "  Personalized Customer Insights - Instant Public HTTPS Tunnel" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

$exePath = Join-Path $PSScriptRoot "cloudflared.exe"

if (!(Test-Path $exePath)) {
    Write-Host "Downloading Cloudflare Tunnel standalone executable..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe" -OutFile $exePath
}

Write-Host "Connecting tunnel to http://localhost:8080 ..." -ForegroundColor Cyan
Write-Host "Your public HTTPS URL will be displayed below. Share it with anyone!" -ForegroundColor Green
Write-Host "Press Ctrl+C to terminate the public tunnel.`n" -ForegroundColor DarkGray

& $exePath tunnel --url http://localhost:8080
