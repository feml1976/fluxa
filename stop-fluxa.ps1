# ============================================================
# FLUXA — Script de detencion
# Detiene: Frontend (Vite) + Backend (Spring Boot) + PostgreSQL (Docker)
# Uso: .\stop-fluxa.ps1
# ============================================================

Set-Location $PSScriptRoot

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  FLUXA -- Deteniendo aplicacion              " -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ── 1. Frontend (puerto 5177) ─────────────────────────────
Write-Host "[1/3] Deteniendo Frontend (puerto 5177)..." -ForegroundColor Yellow
$frontendConns = Get-NetTCPConnection -LocalPort 5177 -ErrorAction SilentlyContinue
if ($frontendConns) {
    $frontendPids = $frontendConns.OwningProcess | Sort-Object -Unique
    foreach ($pid in $frontendPids) {
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
    }
    Write-Host "      Frontend detenido." -ForegroundColor Green
} else {
    Write-Host "      Frontend no estaba corriendo en puerto 5177." -ForegroundColor DarkGray
}
Write-Host ""

# ── 2. Backend (puerto 8087) ──────────────────────────────
Write-Host "[2/3] Deteniendo Backend (puerto 8087)..." -ForegroundColor Yellow
$backendConns = Get-NetTCPConnection -LocalPort 8087 -ErrorAction SilentlyContinue
if ($backendConns) {
    $backendPids = $backendConns.OwningProcess | Sort-Object -Unique
    foreach ($pid in $backendPids) {
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
    }
    Write-Host "      Backend detenido." -ForegroundColor Green
} else {
    Write-Host "      Backend no estaba corriendo en puerto 8087." -ForegroundColor DarkGray
}
Write-Host ""

# ── 3. PostgreSQL ─────────────────────────────────────────
Write-Host "[3/3] Deteniendo PostgreSQL (Docker)..." -ForegroundColor Yellow
docker-compose down
if ($LASTEXITCODE -eq 0) {
    Write-Host "      PostgreSQL detenido." -ForegroundColor Green
} else {
    Write-Host "      Advertencia: docker-compose down retorno un error." -ForegroundColor DarkYellow
}
Write-Host ""

Write-Host "================================================" -ForegroundColor Green
Write-Host "  FLUXA detenido correctamente.               " -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Nota: cierra manualmente las ventanas de backend y frontend." -ForegroundColor DarkGray
Write-Host ""
