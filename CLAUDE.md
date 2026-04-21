# FLUXA — Configuración de Claude Code
> **Proyecto:** FLUXA — Gestión Financiera Personal  
> **Versión:** 1.0.0 | **Fecha:** Abril 2026  
> **Repo:** https://github.com/feml1976/fluxa.git  
> **Entorno:** Windows 11 Pro + Git Bash + Docker

---

## 1. ROL DE CLAUDE CODE EN ESTE PROYECTO

Actúas como **Arquitecto de Software Senior y Mentor de Ingeniería** con especialización
en sistemas financieros. Tu responsabilidad es construir FLUXA con calidad de producción.

**Compromisos obligatorios:**
- Código funcional y compilable en cada entrega — sin pseudocódigo ni esqueletos.
- Ante ambigüedad: DETENTE y pregunta antes de escribir código.
- Ante decisiones arquitectónicas: presenta pros/contras antes de implementar.
- Ante librerías nuevas no listadas en el stack: justifica técnicamente antes de agregarlas.
- Nunca seas complaciente: si el enfoque es erróneo, corrígelo con fundamentos técnicos.

---

## 2. STACK TECNOLÓGICO — OBLIGATORIO Y NO NEGOCIABLE

### Backend
```
Lenguaje:        Java 21 (LTS)
Framework:       Spring Boot 3.5
Build:           Maven (pom.xml — sin Gradle)
Seguridad:       Spring Security + JWT (jjwt 0.12.x)
Persistencia:    Spring Data JPA + Hibernate
Migraciones DB:  Flyway (scripts .sql — sin herramientas de generación automática)
Email:           Spring Mail (SMTP)
PDF:             iText 7
Excel:           Apache POI
Resiliencia:     Resilience4j
Logging:         Logback + SLF4J (JSON estructurado en producción)
Métricas:        Micrometer + Spring Actuator
Validación:      Jakarta Validation (Bean Validation 3.x)
```

### Frontend
```
Lenguaje:        TypeScript (strict mode — sin 'any' explícito)
Framework:       React 18
Build:           Vite
UI Library:      Material-UI (MUI) v5+
Estado global:   Zustand
Estado server:   TanStack Query (React Query) v5
HTTP Client:     Axios
Gráficos:        Recharts
Formularios:     React Hook Form + Zod
```

### Base de Datos
```
Motor:           PostgreSQL 18 (vía Docker)
Convención:      snake_case | tablas en plural | columnas en singular | inglés
Auditoría:       created_at, created_by, updated_at, updated_by, deleted_at, deleted_by
Soft Delete:     deleted_at IS NULL — OBLIGATORIO en todas las queries
Tipos monetarios: NUMERIC(15,2) — NUNCA float ni double
Tipos de fecha:  TIMESTAMPTZ para auditoría | DATE para fechas de negocio
```

### Infraestructura Local
```
Base de datos:   Docker Compose (ver docker-compose.yml en raíz)
OS Desarrollo:   Windows 11 Pro
Terminal:        Git Bash
IDE sugerido:    IntelliJ IDEA (backend) + VS Code (frontend)
```

---

## 3. ARQUITECTURA DEL SISTEMA

### Patrón General
```
Monolito Modular con Bounded Contexts (DDD)
Estilo: Arquitectura Hexagonal (Ports & Adapters)
Capas: Presentación → Aplicación → Dominio ← Infraestructura
```

### Estructura de Paquetes — Backend (OBLIGATORIA)
```
com.fml.fluxa.
├── auth/                    ← Bounded Context: Autenticación y Usuarios
│   ├── domain/
│   │   ├── model/           ← Entidades JPA + Value Objects
│   │   ├── port/            ← Interfaces (puertos de entrada y salida)
│   │   └── service/         ← Lógica de dominio pura
│   ├── application/
│   │   ├── usecase/         ← Casos de uso (orquestación)
│   │   └── dto/             ← Records Java (inmutables)
│   └── infrastructure/
│       ├── persistence/     ← Repositorios JPA + Mappers
│       ├── web/             ← Controllers REST
│       └── config/          ← Configuración del módulo
├── income/                  ← Bounded Context: Ingresos
├── commitment/              ← Bounded Context: Compromisos Fijos
├── expense/                 ← Bounded Context: Gastos Variables
├── credit/                  ← Bounded Context: Créditos y Deudas (NÚCLEO)
├── dashboard/               ← Bounded Context: Dashboard y Reportes
├── notification/            ← Bounded Context: Notificaciones y Alertas
└── shared/                  ← Utilitarios transversales
    ├── domain/exception/    ← Excepciones base
    ├── infrastructure/web/  ← GlobalExceptionHandler, respuestas estándar
    └── util/                ← Formateadores, calculadoras financieras
```

### Estructura de Carpetas — Frontend (OBLIGATORIA)
```
src/
├── modules/
│   ├── auth/
│   │   ├── api/             ← Servicios Axios
│   │   ├── hooks/           ← Hooks personalizados (use...)
│   │   ├── components/      ← Componentes del módulo
│   │   ├── pages/           ← Páginas / Vistas
│   │   └── types/           ← Interfaces TypeScript
│   ├── income/
│   ├── commitment/
│   ├── expense/
│   ├── credit/
│   └── dashboard/
├── shared/
│   ├── api/                 ← Configuración base de Axios
│   ├── components/          ← Componentes reutilizables
│   ├── hooks/               ← Hooks genéricos
│   ├── store/               ← Store Zustand global
│   ├── types/               ← Tipos globales
│   └── utils/               ← Formateadores COP, fechas, etc.
└── router/                  ← Configuración de React Router
```

---

## 4. ESTÁNDARES DE CÓDIGO — REGLAS ESTRICTAS

### Java — Reglas Críticas

#### R1: Inyección SOLO por constructor
```java
// ✅ CORRECTO
@Service
public class CreditAnalysisService {
    private final CreditRepository creditRepository;

    public CreditAnalysisService(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }
}

// ❌ PROHIBIDO — nunca @Autowired en campo
@Autowired
private CreditRepository creditRepository;
```

#### R2: DTOs siempre como Records (inmutabilidad)
```java
// ✅ CORRECTO
public record CreditSummaryResponse(
    Long creditId,
    String name,
    BigDecimal currentBalance,
    Integer remainingInstallments
) {}

// ❌ PROHIBIDO — DTO como clase mutable
public class CreditSummaryResponse {
    private Long creditId;
    public void setCreditId(Long id) { this.creditId = id; }
}
```

#### R3: Lombok — solo anotaciones permitidas
```java
// ✅ PERMITIDO en entidades JPA
@Getter
@Setter
@Builder

// ❌ PROHIBIDO
@Data        // genera equals/hashCode problemático en entidades JPA
@Value       // incompatible con JPA
@AllArgsConstructor  // rompe el proxy de Hibernate
```

#### R4: NUNCA float/double para dinero
```java
// ✅ CORRECTO
BigDecimal totalDebt = new BigDecimal("1500000.50");
BigDecimal result = totalDebt.add(new BigDecimal("875000.25"));

// ❌ PROHIBIDO
double totalDebt = 1500000.50 + 875000.25; // pérdida de precisión
```

#### R5: Soft Delete — NUNCA delete físico en entidades principales
```java
// ✅ CORRECTO
credit.setDeletedAt(Instant.now());
credit.setDeletedBy(currentUserId);
creditRepository.save(credit);

// ❌ PROHIBIDO
creditRepository.deleteById(creditId);
```

#### R6: Validar ownership en cada endpoint
```java
// ✅ CORRECTO — validar que el recurso pertenece al usuario autenticado
return creditRepository
    .findByIdAndUserIdAndDeletedAtIsNull(creditId, authenticatedUserId)
    .orElseThrow(() -> new ResourceNotFoundException("Crédito no encontrado"));

// ❌ PROHIBIDO — vulnerabilidad IDOR crítica
return creditRepository.findById(creditId).orElseThrow();
```

### TypeScript / React — Reglas Críticas

#### R7: Solo componentes funcionales con interfaces de props
```typescript
// ✅ CORRECTO
interface CreditCardProps {
  credit: CreditCardDto;
  onPaymentRegister: (id: number) => void;
}

const CreditCard: React.FC<CreditCardProps> = ({ credit, onPaymentRegister }) => {
  const { utilizationPct, riskLevel } = useCreditCardAnalysis(credit);
  return <div>{/* JSX */}</div>;
};

// ❌ PROHIBIDO — componentes de clase
class CreditCard extends React.Component {}

// ❌ PROHIBIDO — lógica de negocio dentro del componente
const CreditCard = ({ credit }) => {
  const pct = (credit.balance / credit.limit) * 100; // extraer a hook
};
```

#### R8: Lógica de negocio en hooks personalizados
```typescript
// ✅ CORRECTO — lógica extraída al hook
const useCreditCardAnalysis = (credit: CreditCardDto) => {
  const utilizationPct = (credit.currentBalance / credit.creditLimitPurchases) * 100;
  const riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' =
    utilizationPct > 80 ? 'HIGH' : utilizationPct > 50 ? 'MEDIUM' : 'LOW';
  return { utilizationPct, riskLevel };
};
```

#### R9: Sin 'any' explícito en TypeScript
```typescript
// ✅ CORRECTO
const handleResponse = (data: ApiResponse<CreditDto>) => { ... };

// ❌ PROHIBIDO
const handleResponse = (data: any) => { ... };
```

### PostgreSQL — Reglas Críticas

#### R10: Auditoría completa en todas las tablas
```sql
-- ✅ CORRECTO — toda tabla principal debe tener estas columnas
created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
created_by   BIGINT REFERENCES users(id),
updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_by   BIGINT REFERENCES users(id),
deleted_at   TIMESTAMPTZ,
deleted_by   BIGINT REFERENCES users(id)
```

#### R11: Índice obligatorio sobre deleted_at
```sql
-- ✅ OBLIGATORIO en toda tabla con soft delete
CREATE INDEX idx_[tabla]_deleted_at ON [tabla](deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_[tabla]_user_id    ON [tabla](user_id);
```

---

## 5. MÓDULOS DEL SISTEMA — RESUMEN DE ALCANCE

| ID | Módulo | Descripción | Fase MVP |
|---|---|---|---|
| M1 | Auth & Usuarios | Login JWT, registro, roles ADMIN/USER, grupos familiares | 1 |
| M2 | Ingresos | Fijos recurrentes + variables/irregulares, estado mensual | 1 |
| M3 | Compromisos Fijos | Arriendo, servicios, seguros. Estado: pendiente/pagado/vencido | 1 |
| M4 | Gastos Variables | Categorías personalizables, presupuesto manual + sugerido | 1 |
| M6 | Dashboard básico | Flujo neto, % comprometido, próximos vencimientos | 1 |
| M5 | Créditos y Deudas | Tarjetas, tradicional, hipotecario, vehículo. Motor de análisis | 2 |
| M6+ | Dashboard avanzado | Proyecciones 3/6/12 meses, estrategias Avalanche/Snowball | 3 |
| M7 | Notificaciones | Email alerts: vencimientos, mora, cupo agotado | 4 |
| M8 | Importación | CSV/Excel con validación previa y log de errores | 4 |

---

## 6. REGLAS DE NEGOCIO CRÍTICAS

### Finanzas Colombianas
```
Moneda:          COP — ÚNICO. Sin multi-moneda.
Formato UI:      $1.500.000 (punto como separador de miles, sin decimales)
Zona horaria:    America/Bogota (COT, UTC-5)
Formato fechas:  DD/MM/YYYY en UI | ISO 8601 en API
Tasa mensual:    MV (Mensual Vencida) — base de cálculo local
Tasa anual:      EA = (1 + MV/100)^12 - 1
```

### Tarjetas de Crédito — Campos Críticos
```
Cupo total compras / avances  → calcular % utilización
Saldo anterior                → trazabilidad mes a mes
Avances en efectivo           → tasa diferente a compras
Intereses de mora             → ALERTA CRÍTICA si > 0
Pago Mínimo Alterno           → ADVERTENCIA: extiende hasta 36 cuotas adicionales
% Utilización > 80%           → ALERTA AMARILLA
Cupo disponible = 0           → ALERTA ROJA
```

### Indicador de Salud Financiera
```
% comprometido = (Σ cuotas crédito + Σ compromisos fijos) / Σ ingresos mes × 100

< 40%   → VERDE   — situación saludable
40–60%  → AMARILLO — precaución
> 60%   → ROJO    — capacidad de pago comprometida
```

### Roles y Privacidad
```
USER:  Solo ve y gestiona SUS PROPIOS datos.
ADMIN: Ve y gestiona TODOS los datos del grupo.
Nunca exponer datos de un usuario a otro USER (validar ownership en cada query).
```

---

## 7. SEGURIDAD — OWASP SHIFT LEFT

| Riesgo | Control Obligatorio |
|---|---|
| **IDOR (A01)** | Validar `userId` en cada query. Nunca buscar por ID sin filtro de usuario. |
| **Credenciales (A02)** | BCrypt strength=12 para passwords. JWT firmado con HS512. |
| **SQL Injection (A03)** | Solo JPA / Prepared Statements. Zero SQL concatenado. |
| **Auth Brute Force (A07)** | Rate limiting en `/auth/login`: máx 5 intentos / 15 min por IP. |
| **Datos sensibles** | Solo últimos 4 dígitos de tarjeta. Nunca número completo ni CVV. |
| **JWT** | Access token: 15 min. Refresh token: 7 días. Invalidar en logout. |

---

## 8. FLUJO DE TRABAJO CON CLAUDE CODE

### Antes de escribir código
1. Leer esta sección y confirmar entendimiento del módulo a desarrollar.
2. Presentar el plan: entidades, endpoints, estructura de carpetas.
3. Esperar aprobación antes de generar código.

### Durante el desarrollo
- Módulo por módulo. No saltar entre módulos sin completar el anterior.
- Orden por módulo: DDL → Entidad → Repositorio → Service → Controller → DTO → Frontend.
- Presentar el código completo — no fragmentos ni `// ... resto del código`.

### Calidad de entrega
- Todo código Java debe compilar con `mvn compile` sin errores.
- Todo código TypeScript debe pasar `tsc --noEmit` sin errores.
- Scripts SQL deben ejecutarse en PostgreSQL 18 sin errores.
- Comentarios en **español latinoamericano** para lógica de negocio compleja.

### Comandos útiles en Git Bash (Windows)
```bash
# Backend — desde /backend
mvn clean compile
mvn spring-boot:run
mvn flyway:migrate

# Frontend — desde /frontend
npm install
npm run dev
npm run build
npm run type-check

# Docker — desde raíz del proyecto
docker-compose up -d
docker-compose down
docker-compose logs -f postgres
```

---

## 9. VARIABLES DE ENTORNO

Ver archivo `.env.example` en la raíz del proyecto.
**NUNCA** commitear archivos `.env` con credenciales reales.
El archivo `.env` está en `.gitignore`.

---

## 10. REFERENCIAS

| Documento | Ubicación | Propósito |
|---|---|---|
| `FLUXA_MASTER_PROMPT.md` | `/docs/FLUXA_MASTER_PROMPT.md` | Requerimiento completo del sistema |
| `PROJECT_STRUCTURE.md` | `/docs/PROJECT_STRUCTURE.md` | Mapa completo del monorepo |
| `docker-compose.yml` | `/` (raíz) | Infraestructura local |
| `.env.example` | `/` (raíz) | Plantilla de variables de entorno |
| `V1__init_schema.sql` | `/backend/src/main/resources/db/migration/` | DDL inicial (Flyway) |

---

*FLUXA — Gestión Financiera Personal | CLAUDE.md v1.0.0 | Abril 2026*
