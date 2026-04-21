# FLUXA — Gestión Financiera Personal

> Control total sobre tus ingresos, gastos, compromisos y deudas.  
> Inteligencia para optimizar el pago de tus créditos.

---

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 21 · Spring Boot 3.5 · Maven |
| Frontend | React 18 · TypeScript · Vite · Material-UI |
| Base de datos | PostgreSQL 18 |
| Infraestructura local | Docker Compose |

## Prerrequisitos

- Java 21 (JDK) instalado
- Maven 3.9+ instalado
- Node.js 20+ instalado
- Docker Desktop instalado y corriendo
- Git Bash (Windows)

## Inicio Rápido

### 1. Clonar el repositorio
```bash
git clone https://github.com/feml1976/fluxa.git
cd fluxa
```

### 2. Configurar variables de entorno
```bash
cp .env.example .env
# Editar .env con los valores reales
```

### 3. Levantar base de datos
```bash
docker-compose up -d
# Verificar que esté corriendo:
docker-compose ps
```

### 4. Ejecutar backend
```bash
cd backend
mvn clean spring-boot:run
# API disponible en: http://localhost:8080
```

### 5. Ejecutar frontend
```bash
cd frontend
npm install
npm run dev
# App disponible en: http://localhost:5173
```

### 6. pgAdmin (opcional — administrador web de PostgreSQL)
```bash
docker-compose --profile tools up -d
# Acceder en: http://localhost:5050
# Usuario: admin@fluxa.local | Password: el del .env
```

## Documentación

| Documento | Descripción |
|---|---|
| `CLAUDE.md` | Configuración de Claude Code para este proyecto |
| `docs/PROJECT_STRUCTURE.md` | Mapa completo del monorepo |
| `docs/FLUXA_MASTER_PROMPT.md` | Requerimiento completo del sistema |

## Fases de Desarrollo

| Fase | Módulos | Estado |
|---|---|---|
| **Fase 1 — Core** | Auth · Ingresos · Compromisos · Gastos · Dashboard básico | 🔲 Pendiente |
| **Fase 2 — Créditos** | Créditos y Deudas · Motor de análisis | 🔲 Pendiente |
| **Fase 3 — Inteligencia** | Proyecciones · Simulador · Estrategias de pago | 🔲 Pendiente |
| **Fase 4 — Automatización** | Notificaciones · Importación CSV/Excel · Reportes | 🔲 Pendiente |

---

*FLUXA v1.0.0 — Francisco Montoya · 2026*
