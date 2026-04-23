# FLUXA — Gestión Financiera Personal

> Control total sobre ingresos, gastos, compromisos y deudas.  
> Inteligencia para optimizar el pago de créditos con estrategias Avalanche y Snowball.

[![CI](https://github.com/feml1976/fluxa/actions/workflows/ci.yml/badge.svg)](https://github.com/feml1976/fluxa/actions/workflows/ci.yml)

---

## Módulos implementados

| Módulo | Descripción | Estado |
|---|---|---|
| **Auth & Usuarios** | Registro, login JWT, roles USER/ADMIN | ✅ Completo |
| **Ingresos** | Fuentes fijas y variables, estado mensual | ✅ Completo |
| **Compromisos Fijos** | Arriendo, servicios, seguros — pendiente/pagado/vencido | ✅ Completo |
| **Gastos Variables** | Categorías personalizables, presupuesto manual y sugerido | ✅ Completo |
| **Créditos y Deudas** | Tarjetas, créditos tradicionales, hipotecario, vehículo | ✅ Completo |
| **Dashboard** | Flujo neto, % comprometido, proyecciones 3/6/12 meses, Avalanche/Snowball | ✅ Completo |
| **Notificaciones** | Alertas por email: vencimientos, mora, cupo agotado | ✅ Completo |
| **Importación** | CSV/Excel con validación previa y log de errores | ✅ Completo |

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 21 LTS · Spring Boot 3.5 · Maven 3.9 |
| Seguridad | Spring Security 6 · JWT HS512 (jjwt 0.12) |
| Persistencia | Spring Data JPA · Hibernate 6 · Flyway (9 migraciones) |
| Frontend | React 18 · TypeScript 5 · Vite · Material-UI v5 |
| Estado | Zustand · TanStack Query v5 |
| Base de datos | PostgreSQL 18 (Docker) |
| Infraestructura | Docker Compose (dev) · Docker Compose (prod) |
| CI/CD | GitHub Actions → ghcr.io |
| API Docs | Swagger UI (springdoc-openapi 2.8) |
| Métricas | Micrometer · Spring Actuator |

---

## Prerrequisitos

- **Java 21** (JDK) — [Temurin](https://adoptium.net/)
- **Maven 3.9+**
- **Node.js 20+**
- **Docker Desktop 4.x** (corriendo)
- **Git Bash** (Windows) o terminal Unix

---

## Inicio rápido — Desarrollo local

### 1. Clonar

```bash
git clone https://github.com/feml1976/fluxa.git
cd fluxa
```

### 2. Variables de entorno

```bash
cp .env.example .env
# Editar .env — los valores de POSTGRES_PASSWORD y JWT_SECRET son obligatorios
```

Las variables mínimas para desarrollo local (el resto tiene valores por defecto):

```
POSTGRES_PASSWORD=fluxa_local_pass
JWT_SECRET=<mínimo-64-caracteres>
```

### 3. Levantar base de datos

```bash
docker-compose up -d
docker-compose ps          # verificar que postgres esté healthy
```

### 4. Configurar perfil local del backend

Crear el archivo `backend/src/main/resources/application-local.yml` (está en `.gitignore`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fluxa_db
    username: fluxa_user
    password: fluxa_local_pass
  jpa:
    show-sql: true
  mail:
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}

fluxa:
  jwt:
    secret: fluxa-local-secret-minimo-64-caracteres-para-firma-hs512-desarrollo
  cors:
    allowed-origins: http://localhost:5173

management:
  health:
    mail:
      enabled: false
```

### 5. Ejecutar backend

```bash
cd backend
mvn clean spring-boot:run
```

Flyway aplica las migraciones automáticamente al arrancar.

### 6. Ejecutar frontend

```bash
cd frontend
npm install
npm run dev
```

---

## URLs en desarrollo local

| Servicio | URL |
|---|---|
| Frontend (Vite) | http://localhost:5173 |
| Backend (API) | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |
| pgAdmin (opcional) | http://localhost:5050 |

Para levantar pgAdmin:

```bash
docker-compose --profile tools up -d
# Usuario: admin@fluxa.local  |  Password: el definido en .env
```

---

## Tests

### Tests unitarios

```bash
cd backend
mvn test -Dtest="CreditAnalysisUseCaseTest,CreditUseCaseEaTest"
```

Cubren el motor de análisis de créditos (27 casos): alertas GREEN/YELLOW/RED, cálculo EA, tabla de amortización, estrategia de pago, guards de división por cero.

### Tests de integración

```bash
cd backend
mvn test -Dtest="AuthIntegrationTest"
```

Levanta Spring con H2 en modo PostgreSQL (`@SpringBootTest`) y valida el flujo completo de autenticación (9 escenarios).

### Suite completa

```bash
cd backend
mvn clean test
```

---

## Despliegue en producción (Docker Compose)

Requiere las siguientes variables en el `.env` del servidor:

```
POSTGRES_PASSWORD=<contraseña-segura>
JWT_SECRET=<mínimo-64-caracteres-aleatorios>
CORS_ALLOWED_ORIGINS=https://tu-dominio.com
MAIL_USERNAME=<usuario-smtp>       # opcional
MAIL_PASSWORD=<contraseña-smtp>    # opcional
```

```bash
# Construir e iniciar los 3 servicios (postgres + backend + frontend)
docker-compose -f docker-compose.prod.yml up -d --build

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f

# Detener
docker-compose -f docker-compose.prod.yml down
```

El frontend queda expuesto en el puerto `80`. nginx actúa como proxy inverso y enruta `/api/` al backend (puerto `8080` interno, no expuesto al exterior).

---

## CI/CD

El pipeline de GitHub Actions tiene dos etapas:

**CI** (`.github/workflows/ci.yml`) — se dispara en cada PR y push a `main`:
- Backend: compila y ejecuta la suite de tests (`mvn clean test`)
- Frontend: verifica tipos (`tsc --noEmit`) y lint

**CD** (`.github/workflows/cd.yml`) — se dispara cuando CI pasa en `main`:
- Construye y publica imágenes Docker en GitHub Container Registry:
  - `ghcr.io/feml1976/fluxa-backend:latest`
  - `ghcr.io/feml1976/fluxa-frontend:latest`

---

## Estructura del monorepo

```
fluxa/
├── backend/                  ← Java 21 + Spring Boot 3.5
│   ├── src/main/java/com/fml/fluxa/
│   │   ├── auth/             ← Autenticación y usuarios
│   │   ├── income/           ← Ingresos
│   │   ├── commitment/       ← Compromisos fijos
│   │   ├── expense/          ← Gastos variables
│   │   ├── credit/           ← Créditos y deudas
│   │   ├── dashboard/        ← Dashboard y proyecciones
│   │   ├── notification/     ← Notificaciones por email
│   │   ├── importing/        ← Importación CSV/Excel
│   │   └── shared/           ← Utilidades transversales
│   └── src/main/resources/db/migration/   ← Scripts Flyway V1–V6
├── frontend/                 ← React 18 + TypeScript + Vite
│   └── src/modules/          ← Un módulo por bounded context
├── docker/                   ← Scripts de inicialización PostgreSQL
├── docker-compose.yml        ← Infraestructura local (dev)
├── docker-compose.prod.yml   ← Infraestructura de producción
└── .github/workflows/        ← CI/CD pipelines
```

---

## Reglas de negocio destacadas

- **Moneda:** COP exclusivamente. Tipos `NUMERIC(15,2)` en BD, formato `$1.500.000` en UI.
- **Zona horaria:** `America/Bogota` (COT, UTC-5).
- **Soft delete:** ninguna entidad se elimina físicamente (`deleted_at IS NULL` en todas las queries).
- **Ownership:** cada endpoint valida que el recurso pertenece al usuario autenticado (prevención de IDOR).
- **JWT:** access token 15 min · refresh token 7 días · firma HS512.
- **Indicador de salud financiera:**
  - `< 40%` comprometido → VERDE
  - `40–60%` → AMARILLO
  - `> 60%` → ROJO

---

## Convenciones de commits

Proyecto usa [Conventional Commits](https://www.conventionalcommits.org/) en español:

```
feat: agregar módulo de importación CSV
fix: corregir cálculo de tasa EA en créditos
refactor: extraer lógica de alertas a CreditAlertService
test: agregar tests de integración para módulo de ingresos
docs: actualizar README con instrucciones de producción
chore: actualizar dependencia springdoc a 2.8.6
```

---

*FLUXA v1.0.0 — Francisco Montoya · 2026*
