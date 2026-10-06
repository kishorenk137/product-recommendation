@echo off
title Customer Insights - Public Server Tunnel
cls
echo ==============================================================================
echo   Personalized Customer Insights Through Purchase Patterns & Preference
echo   Instant Public Server Tunnel (Powered by Cloudflare)
echo ==============================================================================
echo.
echo Checking connection to local web application on port 8080...
echo.

if not exist "%~dp0cloudflared.exe" (
    echo Downloading Cloudflare Tunnel binary...
    curl.exe -sL -o "%~dp0cloudflared.exe" https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe
)

echo.
echo Starting secure public tunnel to http://localhost:8080 ...
echo A public https://*.trycloudflare.com URL will appear below.
echo You can share that URL with anyone on the internet!
echo.
echo Press Ctrl+C at any time to stop the public tunnel.
echo ==============================================================================
echo.

"%~dp0cloudflared.exe" tunnel --url http://localhost:8080
pause
