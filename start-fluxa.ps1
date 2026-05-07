# ============================================================
# FLUXA — Script de inicio
# Levanta: PostgreSQL (Docker) + Backend (Spring Boot) + Frontend (Vite)
# Uso: .\start-fluxa.ps1
# ============================================================

Set-Location $PSScriptRoot

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  FLUXA -- Iniciando aplicacion               " -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ── 1. PostgreSQL ─────────────────────────────────────────
Write-Host "[1/3] Levantando PostgreSQL (Docker)..." -ForegroundColor Yellow
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERROR: no se pudo iniciar Docker. Verifica que Docker Desktop este corriendo." -ForegroundColor Red
    exit 1
}

# Esperar a que el contenedor pase el healthcheck antes de continuar
Write-Host "      Esperando healthcheck de PostgreSQL..." -ForegroundColor DarkGray
$maxWait = 60
$elapsed  = 0
do {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $health = docker inspect --format="{{.State.Health.Status}}" fluxa-postgres 2>$null
} while ($health -ne "healthy" -and $elapsed -lt $maxWait)

if ($health -ne "healthy") {
    Write-Host "      ERROR: PostgreSQL no alcanzo estado healthy en ${maxWait}s. Revisa: docker logs fluxa-postgres" -ForegroundColor Red
    exit 1
}
Write-Host "      PostgreSQL healthy. ($elapsed s)" -ForegroundColor Green
Write-Host ""

# ── 2. Backend ────────────────────────────────────────────
Write-Host "[2/3] Iniciando Backend (puerto 8087)..." -ForegroundColor Yellow
Start-Process powershell `
    -WorkingDirectory "$PSScriptRoot\backend" `
    -ArgumentList "-NoExit", "-Command", "`$host.UI.RawUI.WindowTitle = 'FLUXA Backend :8087'; mvn spring-boot:run"
Write-Host "      Ventana de backend abierta." -ForegroundColor Green
Write-Host ""

# ── 3. Frontend ───────────────────────────────────────────
Write-Host "[3/3] Iniciando Frontend (puerto 5177)..." -ForegroundColor Yellow
Start-Process powershell `
    -WorkingDirectory "$PSScriptRoot\frontend" `
    -ArgumentList "-NoExit", "-Command", "`$host.UI.RawUI.WindowTitle = 'FLUXA Frontend :5177'; npm run dev"
Write-Host "      Ventana de frontend abierta." -ForegroundColor Green
Write-Host ""

# ── Resumen ───────────────────────────────────────────────
Write-Host "================================================" -ForegroundColor Green
Write-Host "  FLUXA en marcha:" -ForegroundColor Green
Write-Host "    Frontend : http://localhost:5177" -ForegroundColor Green
Write-Host "    Backend  : http://localhost:8087/api/v1" -ForegroundColor Green
Write-Host "    Swagger  : http://localhost:8087/swagger-ui.html" -ForegroundColor Green
Write-Host "    Health   : http://localhost:8087/actuator/health" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  El backend tarda ~30 segundos en estar completamente listo." -ForegroundColor DarkGray
Write-Host "  Para detener la aplicacion ejecuta: .\stop-fluxa.ps1" -ForegroundColor DarkGray
Write-Host ""
