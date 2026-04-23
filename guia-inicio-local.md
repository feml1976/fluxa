# FLUXA — Guía de Inicio Local
> Versión: 1.0 | Fecha: Abril 2026 | Entorno: Windows 11 Pro + Git Bash

---

## 1. Prerrequisitos

Verifica que tienes instaladas las siguientes herramientas antes de comenzar:

| Herramienta | Versión requerida | Verificar con |
|---|---|---|
| Java JDK | 21 LTS | `java -version` |
| Maven | 3.9.x | `mvn -version` |
| Node.js | 18.x | `node -version` |
| npm | 10.x | `npm -version` |
| Docker Desktop | 4.x | `docker -version` |
| Git | cualquier reciente | `git -version` |

> **Importante:** Docker Desktop debe estar **corriendo** antes de levantar la base de datos.

---

## 2. Clonar el Repositorio

```bash
git clone https://github.com/feml1976/fluxa.git
cd fluxa
```

Si ya tienes el repositorio local, asegúrate de estar en la rama correcta:

```bash
git checkout main
git pull origin main
```

---

## 3. Variables de Entorno

El proyecto usa un archivo `.env` en la raíz para los valores sensibles.

### 3.1 Crear el archivo `.env`

```bash
# En Git Bash, desde la raíz del proyecto
cp .env.example .env
```

### 3.2 Valores por defecto (desarrollo local)

El archivo `.env` debe contener como mínimo:

```env
# ── Base de datos ──────────────────────────────────────
POSTGRES_DB=fluxa_db
POSTGRES_USER=fluxa_user
POSTGRES_PASSWORD=fluxa_local_pass
POSTGRES_PORT=5432

# ── Backend ────────────────────────────────────────────
DB_URL=jdbc:postgresql://localhost:5432/fluxa_db
DB_USERNAME=fluxa_user
DB_PASSWORD=fluxa_local_pass
SERVER_PORT=8080

# ── JWT (mínimo 64 caracteres) ─────────────────────────
JWT_SECRET=fluxa-dev-secret-key-para-desarrollo-local-minimo-64-caracteres-requerido
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# ── CORS ───────────────────────────────────────────────
CORS_ALLOWED_ORIGINS=http://localhost:5173

# ── Email (opcional para desarrollo) ───────────────────
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# ── pgAdmin (opcional) ─────────────────────────────────
PGADMIN_EMAIL=admin@fluxa.local
PGADMIN_PASSWORD=admin123
PGADMIN_PORT=5050
```

> **Nota:** El archivo `.env` está en `.gitignore`. Nunca lo commitees con credenciales reales.

---

## 4. Base de Datos — Docker

### 4.1 Levantar PostgreSQL

```bash
# Desde la raíz del proyecto
docker-compose up -d
```

Esto inicia el contenedor `fluxa-postgres` (PostgreSQL 18) en el puerto `5432`.

### 4.2 Verificar que está corriendo

```bash
docker ps
# Debe aparecer: fluxa-postgres   Up   0.0.0.0:5432->5432/tcp
```

También puedes verificar la salud del contenedor:

```bash
docker-compose ps
# El estado debe mostrar: healthy
```

### 4.3 pgAdmin (opcional)

Si necesitas una interfaz web para explorar la base de datos:

```bash
docker-compose --profile tools up -d
# Accede en: http://localhost:5050
# Email: admin@fluxa.local | Password: admin123
```

La conexión al servidor PostgreSQL ya viene pre-configurada.

### 4.4 Comandos útiles de Docker

```bash
# Ver logs de PostgreSQL en tiempo real
docker-compose logs -f postgres

# Detener contenedores (conserva datos)
docker-compose down

# Detener y eliminar datos (¡borra todo!)
docker-compose down -v

# Reiniciar solo PostgreSQL
docker-compose restart postgres
```

### 4.5 Conexión directa desde psql (opcional)

```bash
docker exec -it fluxa-postgres psql -U fluxa_user -d fluxa_db
```

---

## 5. Backend — Spring Boot

Todos los comandos se ejecutan desde la carpeta `backend/`.

```bash
cd backend
```

### 5.1 Compilar el proyecto

```bash
mvn clean compile
```

Esto descarga dependencias Maven y verifica que el código compila sin errores. La primera ejecución puede tardar varios minutos.

### 5.2 Ejecutar las migraciones Flyway

Las migraciones se ejecutan **automáticamente** al iniciar la aplicación. Sin embargo, si quieres ejecutarlas de forma independiente:

```bash
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/fluxa_db \
    -Dflyway.user=fluxa_user \
    -Dflyway.password=fluxa_local_pass
```

> Las migraciones crean todas las tablas del sistema: users, groups, income_sources, fixed_commitments, variable_expenses, credits, credit_cards, notification_logs, etc.

### 5.3 Iniciar el servidor

```bash
mvn spring-boot:run
```

O alternativamente, pasando el archivo `.env` directamente:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

### 5.4 Verificar que el backend está corriendo

```bash
# Health check
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}
```

También puedes abrir en el navegador: `http://localhost:8080/actuator/health`

### 5.5 Documentación interactiva — Swagger UI

Una vez el backend esté corriendo, accede a la documentación completa de la API en:

```
http://localhost:8080/swagger-ui.html
```

Desde ahí puedes explorar todos los endpoints, ver los schemas de request/response y ejecutar llamadas directamente. Para endpoints protegidos, usa el botón **Authorize** e ingresa el token JWT obtenido en `/api/v1/auth/login`.

El JSON de la especificación OpenAPI está disponible en:
```
http://localhost:8080/v3/api-docs
```

### 5.6 Endpoints principales

| Módulo | Base URL |
|---|---|
| Autenticación | `http://localhost:8080/api/v1/auth` |
| Ingresos | `http://localhost:8080/api/v1/income` |
| Compromisos Fijos | `http://localhost:8080/api/v1/commitments` |
| Gastos Variables | `http://localhost:8080/api/v1/expenses` |
| Créditos | `http://localhost:8080/api/v1/credits` |
| Dashboard | `http://localhost:8080/api/v1/dashboard` |
| Notificaciones | `http://localhost:8080/api/v1/notifications` |
| Importación | `http://localhost:8080/api/v1/import` |

### 5.7 Registro del primer usuario

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Francisco",
    "lastName": "Montoya",
    "email": "admin@fluxa.local",
    "password": "Admin1234!"
  }'
```

### 5.7 Solución de problemas comunes — Backend

| Error | Causa probable | Solución |
|---|---|---|
| `Connection refused: localhost:5432` | PostgreSQL no está corriendo | Ejecutar `docker-compose up -d` |
| `FlywayException: validate failed` | Migración inconsistente | Revisar scripts en `db/migration/` |
| `Port 8080 already in use` | Otro proceso usa el puerto | `lsof -i :8080` y terminar el proceso |
| `JWT secret too short` | Secret < 64 caracteres | Actualizar `JWT_SECRET` en `.env` |
| `ddl-auto: validate` falla | Entidad no coincide con tabla | Crear script Flyway de corrección |

---

## 6. Frontend — React + Vite

Todos los comandos se ejecutan desde la carpeta `frontend/`.

```bash
cd frontend
```

### 6.1 Variables de entorno del Frontend

El frontend usa URLs relativas (`/api/v1`) — no requiere configurar la URL del backend.
En desarrollo, Vite proxea automáticamente `/api/*` a `http://localhost:8080` (configurado en `vite.config.ts`).

### 6.2 Instalar dependencias

```bash
npm install
```

La primera instalación puede tardar 1-2 minutos.

### 6.3 Iniciar el servidor de desarrollo

```bash
npm run dev
```

La aplicación estará disponible en: `http://localhost:5173`

Vite tiene recarga automática (HMR) — los cambios en el código se reflejan al instante sin necesidad de reiniciar.

### 6.4 Verificar tipos TypeScript (sin iniciar el servidor)

```bash
npm run type-check
```

Equivale a `tsc --noEmit`. Debe ejecutarse sin errores antes de hacer commit.

### 6.5 Compilar para producción

```bash
npm run build
```

El resultado queda en `frontend/dist/`.

### 6.6 Pantallas disponibles en el Frontend

| Ruta | Descripción |
|---|---|
| `/login` | Inicio de sesión (pública) |
| `/` | Dashboard principal (protegida) |
| `/income` | Gestión de ingresos |
| `/commitments` | Compromisos fijos |
| `/expenses` | Gastos variables |
| `/credits` | Créditos y tarjetas |
| `/notifications` | Centro de notificaciones |
| `/import` | Importación CSV/Excel |

### 6.7 Solución de problemas comunes — Frontend

| Error | Causa probable | Solución |
|---|---|---|
| `401 Unauthorized` en todas las llamadas | Token expirado | Hacer logout y login nuevamente |
| `CORS error` en consola | Backend no acepta el origen | Verificar `CORS_ALLOWED_ORIGINS` en `.env` |
| `Cannot find module` | Dependencias no instaladas | Ejecutar `npm install` |
| Puerto 5173 ocupado | Otro proceso activo | Vite usa el siguiente puerto disponible automáticamente |
| Error de TypeScript en `npm run build` | Tipos incorrectos en el código | Ejecutar `npm run type-check` para ver el detalle |

---

## 7. Orden de Inicio Recomendado

Sigue este orden cada vez que inicies el entorno de desarrollo:

```
1. Iniciar Docker Desktop (si no está corriendo)
2. docker-compose up -d          ← PostgreSQL
3. cd backend && mvn spring-boot:run    ← Backend en puerto 8080
4. cd frontend && npm run dev          ← Frontend en puerto 5173
5. Abrir http://localhost:5173
```

---

## 8. Estructura de Archivos Clave

```
fluxa/
├── .env                          ← Variables de entorno (NO commitear)
├── .env.example                  ← Plantilla de variables
├── docker-compose.yml            ← Infraestructura local (solo PostgreSQL)
├── docker-compose.prod.yml       ← Producción (PostgreSQL + Backend + Frontend)
├── docker/
│   └── postgres/init.sql         ← Script de inicialización de BD
├── backend/
│   ├── Dockerfile                ← Multi-stage: Maven build → JRE 21 Alpine
│   ├── .dockerignore
│   ├── pom.xml                   ← Dependencias Maven
│   └── src/main/
│       ├── java/com/fml/fluxa/   ← Código fuente Java
│       └── resources/
│           ├── application.yml   ← Configuración de Spring Boot
│           └── db/migration/     ← Scripts Flyway (V1 → V6)
└── frontend/
    ├── Dockerfile                ← Multi-stage: Node build → nginx Alpine
    ├── .dockerignore
    ├── nginx.conf                ← SPA routing + proxy /api/ → backend
    ├── package.json              ← Dependencias npm
    ├── vite.config.ts            ← Proxy de desarrollo a localhost:8080
    └── src/
        ├── modules/              ← Módulos de la aplicación
        ├── shared/               ← Componentes y utilidades comunes
        └── router/               ← Configuración de rutas
```

---

## 9. Migraciones Flyway — Referencia

| Script | Tablas creadas |
|---|---|
| `V1__create_users_groups.sql` | `users`, `user_roles`, `groups`, `refresh_tokens`, `password_reset_tokens` |
| `V2__create_income_tables.sql` | `income_categories`, `income_sources`, `income_records` |
| `V3__create_commitment_tables.sql` | `fixed_commitments`, `commitment_records` |
| `V4__create_expense_tables.sql` | `expense_categories`, `variable_expenses`, `budget_plans` |
| `V5__create_credit_tables.sql` | `credits`, `credit_cards`, `credit_payments` |
| `V6__create_notification_logs.sql` | `notification_logs` |

---

## 10. Despliegue con Docker (Producción)

### 10.1 Variables requeridas en `.env`

Asegúrate de que tu `.env` tiene los valores de producción:

```env
POSTGRES_PASSWORD=<contraseña-segura>
JWT_SECRET=<secret-minimo-64-caracteres>
CORS_ALLOWED_ORIGINS=https://tu-dominio.com
MAIL_USERNAME=noreply@tu-dominio.com
MAIL_PASSWORD=<app-password-gmail>
FRONTEND_PORT=80
```

### 10.2 Construir e iniciar todos los servicios

```bash
# Desde la raíz del proyecto
docker-compose -f docker-compose.prod.yml up -d --build
```

Esto ejecuta en orden:
1. PostgreSQL (con health check)
2. Backend Spring Boot (espera a que PostgreSQL esté `healthy`)
3. Frontend nginx (espera a que Backend esté `healthy`)

La app queda disponible en `http://localhost` (puerto 80).

### 10.3 Comandos de gestión

```bash
# Ver logs de todos los servicios
docker-compose -f docker-compose.prod.yml logs -f

# Ver logs de un servicio específico
docker-compose -f docker-compose.prod.yml logs -f backend

# Detener (conserva datos del volumen)
docker-compose -f docker-compose.prod.yml down

# Reconstruir solo el backend después de cambios en código
docker-compose -f docker-compose.prod.yml up -d --build backend

# Eliminar todo incluyendo datos (¡irreversible!)
docker-compose -f docker-compose.prod.yml down -v
```

### 10.4 Arquitectura de producción

```
Browser
  │  HTTP :80
  ▼
nginx (frontend)
  ├── /              → sirve index.html + assets (React SPA)
  └── /api/*         → proxy a backend:8080/api/*
                            │
                            ▼
                     Spring Boot (backend:8080)
                            │
                            ▼
                     PostgreSQL (postgres:5432)
```

---

## 11. Comandos de Referencia Rápida

```bash
# ── Docker — Desarrollo ───────────────────────────────
docker-compose up -d                              # Levantar PostgreSQL
docker-compose down                               # Detener (conserva datos)
docker-compose logs -f postgres                   # Ver logs de BD
docker-compose --profile tools up -d             # Levantar con pgAdmin

# ── Docker — Producción ───────────────────────────────
docker-compose -f docker-compose.prod.yml up -d --build  # Build y levantar todo
docker-compose -f docker-compose.prod.yml logs -f        # Ver logs
docker-compose -f docker-compose.prod.yml down           # Detener

# ── Backend ────────────────────────────────────────────
cd backend
mvn clean compile                       # Solo compilar
mvn spring-boot:run                     # Compilar y ejecutar
mvn test                                # Ejecutar todos los tests
mvn test -Dtest=CreditAnalysisUseCaseTest  # Test específico

# ── Frontend ───────────────────────────────────────────
cd frontend
npm install                             # Instalar dependencias
npm run dev                             # Servidor de desarrollo (puerto 5173)
npm run type-check                      # Verificar tipos TS
npm run build                           # Build de producción (tsc + vite)
npm run lint                            # Verificar linting
```

---

*FLUXA — Guía de Inicio Local v1.1 | Abril 2026*
