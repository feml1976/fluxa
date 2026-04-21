# FLUXA — Prompt Maestro de Desarrollo
> **Versión:** 1.0.0 | **Fecha:** Abril 2026 | **Autor:** Francisco Montoya  
> **Clasificación:** Documento de Requerimiento + Prompt de Ingeniería  
> **Destinatario:** Claude (AI Development Assistant)

---

## 1. ROL

```
Actúa como un Arquitecto de Software Senior y Mentor de Ingeniería (Senior Staff Engineer)
con especialización en aplicaciones financieras personales.

Tu perfil combina:
- Diseño de sistemas financieros con lógica de amortización, tasas de interés y flujos de caja.
- Dominio de Clean Architecture, DDD (Domain-Driven Design) y Arquitectura Hexagonal.
- Experiencia en desarrollo full-stack: Java 21 / Spring Boot 3.5 (backend) y
  React 18 / TypeScript / Material-UI (frontend).
- Conocimiento de normativa financiera colombiana (tasas EA/MV, 4x1000, regulación SFC).
- Capacidad para traducir requerimientos de negocio en decisiones técnicas justificadas.

Entorno de trabajo: corporativo de alta criticidad donde la mantenibilidad,
la seguridad y la trazabilidad son pilares no negociables.
```

---

## 2. CONTEXTO

### 2.1 El Problema

Francisco Montoya, ingeniero de software y arquitecto senior, necesita una herramienta
digital para **gestionar sus finanzas personales y familiares** con un nivel de detalle
equivalente al que aplica en su vida profesional: trazabilidad total, datos confiables
y decisiones basadas en métricas reales.

El problema actual es la **ausencia de visibilidad financiera integrada**:
- Los ingresos, compromisos, gastos y deudas se gestionan de forma dispersa.
- No existe un motor que analice inteligentemente el portafolio de deudas.
- Las tarjetas de crédito muestran información compleja (pago mínimo, pago mínimo
  alterno, diferidos, avances) que el usuario no puede interpretar de forma consolidada.
- No hay alertas proactivas sobre fechas de vencimiento o riesgos financieros.

### 2.2 La Solución: FLUXA

**FLUXA** es una aplicación web responsive de gestión financiera personal y familiar,
orientada a dar control total sobre ingresos, gastos, compromisos y deudas, con
inteligencia incorporada para optimizar decisiones de pago.

**Principios de diseño:**
- **Claridad sobre complejidad:** Traducir datos financieros complejos en indicadores simples.
- **Acción sobre información:** Cada dato debe derivar en una recomendación accionable.
- **Seguridad primero:** Datos financieros sensibles con autenticación robusta y cifrado.
- **Multi-usuario controlado:** Privacidad por usuario, visibilidad total solo para el ADMIN.

### 2.3 Contexto Técnico del Desarrollador

| Atributo | Valor |
|---|---|
| IDE | Visual Studio Code / IntelliJ IDEA |
| OS de desarrollo | Windows 11 Pro |
| Asistente IA | Claude Code (CLI) |
| Control de versiones | Git + GitHub |
| Documentación | Español latinoamericano |
| Comentarios de código | Español latinoamericano |

---

## 3. REQUERIMIENTO

### 3.1 Información General del Sistema

| Atributo | Valor |
|---|---|
| **Nombre** | FLUXA |
| **Tipo** | Aplicación Web Responsive (browser-first) |
| **Usuarios** | Multi-usuario (personal + familiar / grupo) |
| **Moneda** | COP (Peso Colombiano) exclusivamente |
| **Idioma UI** | Español latinoamericano |
| **Autenticación** | Requerida (JWT) |
| **Historial** | Desde el mes de activación del sistema |

### 3.2 Roles y Permisos

| Rol | Permisos |
|---|---|
| `ADMIN` | CRUD total sobre todos los usuarios del grupo. Visualiza datos de todos. Configura el sistema. |
| `USER` | CRUD sobre sus propios datos exclusivamente. No puede ver datos de otros usuarios. |

**Regla de negocio:** Un grupo representa un núcleo familiar u hogar. Un usuario pertenece
a un solo grupo. El ADMIN es el único que puede gestionar la membresía del grupo.

### 3.3 Stack Tecnológico Obligatorio

#### Backend
```
- Lenguaje:       Java 21 (LTS)
- Framework:      Spring Boot 3.5
- Build:          Maven
- Seguridad:      Spring Security + JWT (jjwt)
- Persistencia:   Spring Data JPA + Hibernate
- Email:          Spring Mail (SMTP)
- PDF:            iText 7 o QuestPDF
- Excel:          Apache POI
- Resiliencia:    Resilience4j
- Logging:        Logback + SLF4J (JSON estructurado en producción)
- Métricas:       Micrometer + Actuator
```

#### Frontend
```
- Lenguaje:       TypeScript
- Framework:      React 18
- Build:          Vite
- UI Library:     Material-UI (MUI) v5+
- Estado global:  Zustand
- Estado server:  React Query (TanStack Query v5)
- HTTP Client:    Axios
- Gráficos:       Recharts o Chart.js
- Formularios:    React Hook Form + Zod
```

#### Base de Datos
```
- Motor:          PostgreSQL 18
- Migraciones:    Flyway (scripts .sql, sin herramientas ORM de generación)
- Convención:     snake_case, tablas en plural, columnas en singular, idioma inglés
- Auditoría:      created_at, created_by, updated_at, updated_by, deleted_at, deleted_by
- Soft Delete:    Implementado con deleted_at IS NULL en todas las entidades principales
```

#### Arquitectura
```
- Patrón:         Monolito Modular con Bounded Contexts (DDD)
- Capas:          Presentación → Aplicación → Dominio ← Infraestructura
- Estilo:         Arquitectura Hexagonal (Ports & Adapters)
- Estructura pkg: com.fml.fluxa.[módulo].[capa]
```

### 3.4 Módulos del Sistema

---

#### MÓDULO M1 — Autenticación y Usuarios

**Objetivo:** Gestionar el acceso seguro al sistema y la administración de usuarios por grupo.

**Funcionalidades:**
- Registro de nuevo usuario con validación de email
- Login con generación de JWT (access token + refresh token)
- Recuperación de contraseña vía email (token temporal de 15 minutos)
- Cierre de sesión (invalidación del refresh token)
- Gestión de perfil de usuario (nombre, email, foto de perfil)
- ADMIN: crear, desactivar y asignar roles a usuarios del grupo

**Modelo de datos clave:**

```
users
  id, email, password_hash, first_name, last_name,
  role (ADMIN | USER), group_id, is_active,
  created_at, updated_at, deleted_at

groups
  id, name, description, created_at, updated_at

password_reset_tokens
  id, user_id, token_hash, expires_at, used_at, created_at
```

**Reglas de negocio:**
- Email único en todo el sistema.
- Contraseña: mínimo 8 caracteres, al menos 1 mayúscula, 1 número, 1 carácter especial.
- El JWT expira en 15 minutos. El refresh token en 7 días.
- Un usuario desactivado no puede hacer login, pero sus datos se conservan.

---

#### MÓDULO M2 — Ingresos

**Objetivo:** Registrar y proyectar todas las fuentes de ingreso del usuario.

**Tipos de ingreso:**
- **Fijo recurrente:** Salario mensual, arriendo recibido, pensión. Se replica automáticamente cada mes.
- **Variable / irregular:** Honorarios, comisiones, bonificaciones, ventas. Se registra manualmente cuando ocurre.

**Funcionalidades:**
- CRUD de ingresos con categorización
- Configuración de periodicidad para ingresos fijos (mensual, quincenal, semanal)
- Estado mensual por ingreso: `ESPERADO` | `RECIBIDO` | `PARCIAL` | `NO_RECIBIDO`
- Vista comparativa: ingreso esperado vs ingreso real por mes
- Proyección de ingresos para los próximos 3/6/12 meses

**Modelo de datos clave:**

```
income_categories
  id, user_id, name, color, icon, created_at, updated_at, deleted_at

income_sources
  id, user_id, category_id, name, description, type (FIXED | VARIABLE),
  expected_amount, frequency (MONTHLY | BIWEEKLY | WEEKLY | ONE_TIME),
  start_date, end_date, is_active,
  created_at, updated_at, deleted_at

income_records
  id, user_id, source_id, amount, received_date, period_month, period_year,
  status (EXPECTED | RECEIVED | PARTIAL | NOT_RECEIVED),
  notes, created_at, updated_at
```

---

#### MÓDULO M3 — Compromisos Fijos

**Objetivo:** Controlar gastos recurrentes obligatorios (arriendo, servicios, seguros, suscripciones).

**Funcionalidades:**
- CRUD de compromisos con categorización
- Configuración de día de pago y periodicidad
- Estado mensual: `PENDIENTE` | `PAGADO` | `VENCIDO`
- Registro del valor real pagado (puede diferir del estimado)
- Alertas configurables X días antes del vencimiento
- Vista mensual consolidada de compromisos

**Categorías sugeridas (personalizables):**
Arriendo / Hipoteca, Servicios públicos (agua, luz, gas, internet), Seguros
(vida, vehículo, hogar), Suscripciones digitales, Transporte, Educación, Salud (EPS, medicina prepagada)

**Modelo de datos clave:**

```
expense_categories
  id, user_id, name, color, icon, type (FIXED | VARIABLE),
  created_at, updated_at, deleted_at

fixed_commitments
  id, user_id, category_id, name, description, estimated_amount,
  due_day (1-31), frequency (MONTHLY | BIMONTHLY | QUARTERLY | ANNUAL),
  alert_days_before (default: 5), is_active,
  created_at, updated_at, deleted_at

commitment_records
  id, user_id, commitment_id, period_month, period_year,
  estimated_amount, actual_amount, due_date, paid_date,
  status (PENDING | PAID | OVERDUE), receipt_reference, notes,
  created_at, updated_at
```

---

#### MÓDULO M4 — Gastos Variables

**Objetivo:** Registrar gastos no recurrentes con control presupuestario por categoría.

**Funcionalidades:**
- Registro rápido de gasto con categoría, monto, fecha y descripción
- Presupuesto mensual por categoría (definido manualmente por el usuario)
- Sugerencia automática de presupuesto basada en el promedio de los últimos 3 meses
- Comparativo planeado vs ejecutado por categoría y por mes
- Etiquetas libres (tags) para clasificación adicional
- Vista de Top 5 categorías de mayor gasto mensual

**Modelo de datos clave:**

```
variable_expenses
  id, user_id, category_id, amount, expense_date, description,
  tags (JSONB), receipt_url, created_at, updated_at, deleted_at

budget_plans
  id, user_id, category_id, planned_amount, period_month, period_year,
  suggested_amount, created_at, updated_at
```

**Regla de negocio — Sugerencia de presupuesto:**
```
suggested_amount = AVG(gasto real en la misma categoría en los últimos 3 meses)
Redondear al múltiplo de $10.000 COP más cercano hacia arriba.
Si no hay historial, suggested_amount = NULL (no sugerir).
```

---

#### MÓDULO M5 — Créditos y Deudas *(Módulo Crítico)*

**Objetivo:** Gestión completa del portafolio de deudas con análisis inteligente de
optimización de pagos.

##### 5.1 Sub-tipos de Crédito

**A) Tarjeta de Crédito** (cuota variable / rotativa)

Campos específicos:
```
financial_entity       — Nombre del banco / entidad
card_number_last4      — Últimos 4 dígitos
credit_limit_purchases — Cupo total compras (ej: $2.500.000)
credit_limit_advances  — Cupo total avances (ej: $2.500.000)
available_limit        — Cupo disponible actual
current_balance        — Saldo total a pagar
previous_balance       — Saldo anterior (mes anterior)
purchases_period       — Compras realizadas en el período
advances_period        — Avances en efectivo del período
deferred_balance       — Saldo de compras diferidas activas
interest_current       — Intereses corrientes del período
interest_overdue       — Intereses de mora (ALERTA si > 0)
other_charges          — Otros cargos (4x1000, comisiones, etc.)
minimum_payment        — Pago mínimo exigido
alternate_minimum_pay  — Pago mínimo alterno (extiende a 36 cuotas — TRAMPA)
total_payment          — Pago total para quedar a paz y salvo
billing_date           — Fecha de facturación (corte)
payment_due_date       — Fecha límite de pago
interest_rate_purchases— Tasa de interés compras (MV y EA)
interest_rate_advances — Tasa de interés avances (generalmente mayor)
```

**B) Crédito Tradicional / Libre Inversión** (cuota fija)

```
financial_entity       — Entidad financiera
initial_amount         — Valor inicial del crédito
current_balance        — Saldo capital pendiente
monthly_payment        — Cuota mensual fija
interest_rate_mv       — Tasa mensual vencida (%)
interest_rate_ea       — Tasa efectiva anual (%)
total_installments     — Número total de cuotas
paid_installments      — Cuotas pagadas
remaining_installments — Cuotas restantes (calculado)
payment_due_day        — Día de pago mensual
disbursement_date      — Fecha de desembolso
next_payment_date      — Fecha próximo pago
```

**C) Crédito Hipotecario**

```
(Hereda todos los campos de Crédito Tradicional)
property_description   — Descripción del inmueble
property_value         — Valor comercial del inmueble
ltv_ratio              — Relación préstamo/valor (calculado)
```

**D) Crédito Vehículo**

```
(Hereda todos los campos de Crédito Tradicional)
vehicle_description    — Descripción del vehículo (marca, modelo, año)
vehicle_plate          — Placa
vehicle_commercial_value — Valor comercial actual
```

##### 5.2 Registro de Pagos y Abonos

```
credit_payments
  id, user_id, credit_id, credit_type, payment_date, payment_amount,
  capital_amount, interest_amount, other_charges, is_extraordinary_payment,
  notes, created_at, updated_at
```

**Regla:** Un abono extraordinario a capital reduce el saldo sin contar como cuota mensual.
Recalcular proyección de liquidación al registrar un abono extraordinario.

##### 5.3 Motor de Análisis Inteligente de Deudas

El sistema debe calcular y presentar de forma proactiva:

| Indicador | Descripción | Frecuencia de cálculo |
|---|---|---|
| **Meses/años restantes** | Tiempo estimado para liquidar cada crédito | En tiempo real |
| **Costo total proyectado** | Capital + intereses totales a pagar | En tiempo real |
| **Mayor saldo** | Crédito con mayor deuda pendiente | Mensual |
| **Mayor tasa** | Crédito con mayor costo financiero (EA) | Mensual |
| **Mayor riesgo** | Crédito con mora detectada (interest_overdue > 0) | Inmediato |
| **% Utilización cupo** | Para tarjetas: saldo / cupo total × 100 | Mensual |

##### 5.4 Estrategias de Pago Recomendadas

El sistema debe recomendar y comparar dos estrategias:

**Avalanche (Catarata):** Pagar primero el crédito con mayor tasa de interés.
- Minimiza el costo financiero total.
- Recomendada cuando el objetivo es pagar menos intereses.

**Snowball (Bola de nieve):** Pagar primero el crédito con menor saldo pendiente.
- Genera motivación psicológica al liquidar deudas completas más rápido.
- Recomendada cuando el usuario necesita "victorias rápidas".

El sistema debe mostrar: con cada estrategia, cuánto dinero se ahorra y cuánto tiempo
se gana vs. el pago mínimo actual.

##### 5.5 Simulador de Abono Extraordinario

Entrada del usuario: `¿Cuánto dinero extra puedo abonar al mes? $X`

Salida esperada por el sistema:
- Si aplicas $X al crédito [NOMBRE]:
  - Nueva fecha estimada de liquidación: [FECHA]
  - Cuotas que te ahorras: [N]
  - Dinero que ahorras en intereses: $[MONTO]

##### 5.6 Alertas Automáticas de Tarjeta de Crédito

| Condición | Tipo de Alerta | Nivel |
|---|---|---|
| `interest_overdue > 0` | Mora detectada | 🔴 CRÍTICO |
| `available_limit = 0` | Cupo agotado | 🔴 CRÍTICO |
| `(balance / limit) > 0.80` | Cupo > 80% utilizado | 🟡 ADVERTENCIA |
| `alternate_minimum_pay` seleccionado | Trampa de extensión 36 cuotas | 🟡 ADVERTENCIA |
| `payment_due_date` en X días | Vencimiento próximo | 🔵 INFORMATIVO |

---

#### MÓDULO M6 — Dashboard y Reportes

**Objetivo:** Vista consolidada e inteligente del estado financiero completo.

##### Panel Principal (vista mensual)

| Widget | Descripción |
|---|---|
| **Flujo neto del mes** | Ingresos recibidos − Compromisos pagados − Gastos variables − Cuotas créditos |
| **Ingresos vs Gastos** | Gráfico de barras comparativo mensual |
| **% Ingresos comprometidos** | (Compromisos + Cuotas) / Ingresos × 100. Semáforo: < 40% verde, 40–60% amarillo, > 60% rojo |
| **Estado de compromisos** | Pendientes / Pagados / Vencidos del mes actual |
| **Deuda total consolidada** | Suma de todos los saldos de crédito activos |
| **Próximos vencimientos** | Lista de pagos en los próximos 7 días |
| **Top 5 gastos** | Categorías con mayor gasto del mes |

##### Reportes y Proyecciones

- Proyección de flujo de caja a 3 / 6 / 12 meses
- Evolución histórica de deuda total (gráfico de línea mensual)
- Evolución de ingresos y gastos (gráfico de barras mes a mes)
- Reporte de créditos: estado completo de cada deuda con análisis
- Exportación a **PDF** (reporte ejecutivo) y **Excel** (datos detallados)

---

#### MÓDULO M7 — Notificaciones y Alertas

**Objetivo:** Comunicación proactiva con el usuario sobre eventos financieros relevantes.

**Canal inicial:** Email (SMTP configurado en la aplicación).
**Canal futuro (no en MVP):** Push notifications, WhatsApp.

| Tipo de Alerta | Trigger | Configuración |
|---|---|---|
| Vencimiento de pago | X días antes de `payment_due_date` | Usuario configura: 3, 5 o 7 días |
| Compromiso próximo | X días antes del `due_day` mensual | Usuario configura: 3, 5 o 7 días |
| Presupuesto superado | Gastos variables > 90% del presupuesto de la categoría | Configurable por categoría |
| Mora detectada | `interest_overdue > 0` en cualquier crédito | Inmediato |
| Cupo agotado | `available_limit = 0` en tarjeta | Inmediato |

**Plantillas de email:** HTML responsivas. Incluir el nombre del usuario, el detalle del
evento y un CTA (call-to-action) con enlace directo a la sección relevante en FLUXA.

**Modelo de datos:**

```
notification_configs
  id, user_id, notification_type, days_before, is_active, created_at, updated_at

notification_logs
  id, user_id, notification_type, sent_at, channel, status (SENT | FAILED), reference_id
```

---

#### MÓDULO M8 — Importación de Datos

**Objetivo:** Facilitar la carga masiva de datos desde extractos bancarios o registros previos.

**Formatos soportados:** Excel (.xlsx) y CSV (.csv).

**Tipos de importación:**
1. Importación de ingresos
2. Importación de gastos variables
3. Importación de pagos de crédito

**Flujo de importación:**

```
1. Usuario descarga plantilla modelo (.xlsx o .csv)
2. Usuario diligencia la plantilla con sus datos
3. Usuario sube el archivo al sistema
4. Sistema valida estructura y datos (sin guardar aún)
5. Sistema muestra preview con filas válidas e inválidas
6. Usuario confirma la importación de las filas válidas
7. Sistema persiste los datos y genera log de importación
```

**Validaciones obligatorias antes de confirmar:**
- Formato de fecha correcto (DD/MM/YYYY)
- Montos positivos, sin caracteres especiales
- Categorías deben existir en el sistema del usuario
- Sin registros duplicados en el mismo período

**Modelo de datos:**

```
import_logs
  id, user_id, import_type, file_name, total_rows, valid_rows, invalid_rows,
  status (PENDING | VALIDATED | COMPLETED | FAILED),
  error_detail (JSONB), created_at, completed_at
```

### 3.5 Fases de Entrega — MVP

| Fase | Módulos | Estimación |
|---|---|---|
| **Fase 1 — Core** | M1 (Auth) + M2 (Ingresos) + M3 (Compromisos) + M4 (Gastos) + M6 Dashboard básico | Sprint 1–3 |
| **Fase 2 — Créditos** | M5 completo (todos los tipos + motor de análisis + alertas de riesgo) | Sprint 4–6 |
| **Fase 3 — Inteligencia** | Proyecciones + Simulador + Estrategias Avalanche/Snowball + Reportes | Sprint 7–8 |
| **Fase 4 — Automatización** | M7 (Notificaciones email) + M8 (Importación CSV/Excel) + Exportación PDF/Excel | Sprint 9–10 |

---

## 4. ESTÁNDARES DE INGENIERÍA (Reglas Estrictas)

### Backend (Java)

```java
// ✅ CORRECTO — Inyección por constructor únicamente
@Service
public class CreditAnalysisService {
    private final CreditRepository creditRepository;
    private final PaymentCalculator paymentCalculator;

    public CreditAnalysisService(
        CreditRepository creditRepository,
        PaymentCalculator paymentCalculator
    ) {
        this.creditRepository = creditRepository;
        this.paymentCalculator = paymentCalculator;
    }
}

// ❌ INCORRECTO — Nunca usar @Autowired en campo
@Autowired
private CreditRepository creditRepository;
```

```java
// ✅ CORRECTO — DTOs como Records (inmutabilidad)
public record CreditSummaryResponse(
    Long creditId,
    String name,
    BigDecimal currentBalance,
    BigDecimal interestRateEa,
    Integer remainingInstallments,
    BigDecimal projectedTotalCost
) {}

// ❌ INCORRECTO — DTO como clase mutable con getters/setters
public class CreditSummaryResponse {
    private Long creditId;
    public Long getCreditId() { return creditId; }
    public void setCreditId(Long creditId) { this.creditId = creditId; }
}
```

```java
// ✅ Lombok permitido solo: @Getter, @Setter, @Builder (en entidades JPA)
// ❌ NO usar: @Data, @Value, @AllArgsConstructor (en entidades)
```

**Estructura de paquetes:**
```
com.fml.fluxa.
├── auth.         (dominio: autenticación)
│   ├── domain.
│   ├── application.
│   └── infrastructure.
├── income.       (dominio: ingresos)
├── commitment.   (dominio: compromisos fijos)
├── expense.      (dominio: gastos variables)
├── credit.       (dominio: créditos — núcleo del negocio)
│   ├── domain.model.
│   ├── domain.service.
│   ├── application.usecase.
│   ├── application.dto.
│   └── infrastructure.persistence.
├── dashboard.    (dominio: consolidación y reportes)
├── notification. (dominio: alertas y emails)
└── shared.       (utilitarios transversales)
```

### Frontend (TypeScript / React)

```typescript
// ✅ CORRECTO — Componentes funcionales con interfaces de props
interface CreditCardSummaryProps {
  credit: CreditCardDto;
  onPaymentRegister: (creditId: number) => void;
}

const CreditCardSummary: React.FC<CreditCardSummaryProps> = ({
  credit,
  onPaymentRegister
}) => {
  // lógica de negocio en hook personalizado
  const { utilizationPercentage, riskLevel } = useCreditCardAnalysis(credit);

  return (/* JSX */);
};

// ❌ INCORRECTO — Lógica de negocio dentro del componente directamente
// ❌ INCORRECTO — Componentes de clase
```

```typescript
// ✅ Lógica extraída a hooks personalizados
const useCreditCardAnalysis = (credit: CreditCardDto) => {
  const utilizationPercentage = (credit.currentBalance / credit.creditLimitPurchases) * 100;
  const riskLevel = utilizationPercentage > 80 ? 'HIGH' : utilizationPercentage > 50 ? 'MEDIUM' : 'LOW';
  return { utilizationPercentage, riskLevel };
};
```

### Base de Datos (PostgreSQL)

```sql
-- ✅ CORRECTO — Convención snake_case, auditoría completa, soft delete
CREATE TABLE credit_cards (
    id                        BIGSERIAL PRIMARY KEY,
    user_id                   BIGINT NOT NULL REFERENCES users(id),
    financial_entity          VARCHAR(100) NOT NULL,
    card_number_last4         CHAR(4) NOT NULL,
    credit_limit_purchases    NUMERIC(15,2) NOT NULL DEFAULT 0,
    credit_limit_advances     NUMERIC(15,2) NOT NULL DEFAULT 0,
    current_balance           NUMERIC(15,2) NOT NULL DEFAULT 0,
    minimum_payment           NUMERIC(15,2),
    alternate_minimum_payment NUMERIC(15,2),
    total_payment             NUMERIC(15,2),
    billing_date              DATE,
    payment_due_date          DATE,
    interest_rate_mv          NUMERIC(8,4) NOT NULL,  -- Tasa mensual vencida %
    interest_rate_ea          NUMERIC(8,4) NOT NULL,  -- Tasa efectiva anual %
    interest_overdue          NUMERIC(15,2) NOT NULL DEFAULT 0,
    is_active                 BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by                BIGINT REFERENCES users(id),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by                BIGINT REFERENCES users(id),
    deleted_at                TIMESTAMPTZ,
    deleted_by                BIGINT REFERENCES users(id)
);

-- Índices obligatorios
CREATE INDEX idx_credit_cards_user_id ON credit_cards(user_id);
CREATE INDEX idx_credit_cards_deleted_at ON credit_cards(deleted_at) WHERE deleted_at IS NULL;

-- ✅ Soft delete siempre por filtro
-- WHERE deleted_at IS NULL   ← OBLIGATORIO en todas las queries

-- ❌ INCORRECTO — Nunca usar DELETE físico en entidades principales
DELETE FROM credit_cards WHERE id = 1;  -- PROHIBIDO
```

---

## 5. EJEMPLOS — QUÉ HACER Y QUÉ NO HACER

### ✅ QUÉ HACER

**DO-1: Calcular tasa EA desde MV correctamente**
```java
// Fórmula: EA = (1 + MV/100)^12 - 1
public BigDecimal calculateEaFromMv(BigDecimal monthlyRate) {
    double mv = monthlyRate.doubleValue() / 100;
    double ea = Math.pow(1 + mv, 12) - 1;
    return BigDecimal.valueOf(ea * 100).setScale(4, RoundingMode.HALF_UP);
}
```

**DO-2: Presentar el riesgo del Pago Mínimo Alterno**
```
Al registrar o visualizar una tarjeta con `alternate_minimum_payment` disponible,
el sistema SIEMPRE debe mostrar una advertencia visual:
"⚠️ El Pago Mínimo Alterno extiende tu deuda a hasta 36 cuotas adicionales.
Esto puede incrementar el costo total de tu deuda en $XX.XXX."
```

**DO-3: Proyectar la liquidación de un crédito tradicional**
```
Para crédito con cuota fija:
  saldo_restante / cuota_mensual = meses_restantes (aproximado)
Para proyección con abono extraordinario:
  Simular amortización mes a mes descontando (cuota + abono_extra) del capital
  hasta saldo = 0. Contar los meses resultantes.
```

**DO-4: Indicador de salud financiera**
```
porcentaje_comprometido = (sum(cuotas_credito) + sum(compromisos_fijos)) / sum(ingresos_mes) * 100

VERDE   → < 40%  : Situación saludable
AMARILLO → 40–60% : Precaución — revisar gastos
ROJO    → > 60%  : Alerta — capacidad de pago comprometida
```

### ❌ QUÉ NO HACER

**DON'T-1: No hacer DELETE físico de registros financieros**
```java
// ❌ NUNCA
creditRepository.deleteById(creditId);

// ✅ SIEMPRE soft delete
credit.setDeletedAt(Instant.now());
credit.setDeletedBy(currentUserId);
creditRepository.save(credit);
```

**DON'T-2: No usar float/double para valores monetarios**
```java
// ❌ NUNCA — pérdida de precisión con decimales
double totalDebt = 1500000.50 + 875000.25;

// ✅ SIEMPRE BigDecimal para dinero
BigDecimal totalDebt = new BigDecimal("1500000.50")
    .add(new BigDecimal("875000.25"));
```

**DON'T-3: No exponer datos de un usuario a otro USER**
```java
// ❌ NUNCA — vulnerabilidad IDOR crítica
public CreditDto getCreditById(Long creditId) {
    return creditRepository.findById(creditId).orElseThrow();
}

// ✅ SIEMPRE validar que el recurso pertenece al usuario autenticado
public CreditDto getCreditById(Long creditId, Long authenticatedUserId) {
    return creditRepository
        .findByIdAndUserIdAndDeletedAtIsNull(creditId, authenticatedUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Crédito no encontrado"));
}
```

**DON'T-4: No introducir librerías externas sin justificación técnica documentada**
```
Antes de agregar cualquier dependencia nueva al pom.xml, presentar:
- ¿Qué problema resuelve?
- ¿Por qué no puede resolverse con el stack actual?
- ¿Cuál es el impacto en el tamaño del artefacto y en la seguridad?
```

**DON'T-5: No almacenar números de tarjeta completos**
```
Solo se permite almacenar los últimos 4 dígitos del número de tarjeta.
card_number_last4 CHAR(4) — MÁXIMO.
Nunca almacenar el número completo, CVV ni fecha de vencimiento de la tarjeta física.
```

---

## 6. SALIDA ESPERADA

### 6.1 Artefacto Principal Requerido

**Formato:** Documento Markdown (`.md`) — entregado como archivo descargable.

**Nombre del archivo:** `FLUXA_PLAN_DESARROLLO_FASE_[N].md`

### 6.2 Estructura Obligatoria del Plan de Desarrollo

El documento generado debe incluir las siguientes secciones en este orden:

```markdown
# FLUXA — Plan de Desarrollo Fase [N]

## 1. Alcance de la Fase
   - Módulos incluidos
   - Módulos excluidos (fuera de alcance)
   - Criterios de aceptación de la fase

## 2. Arquitectura de la Fase
   - Diagrama de componentes (Mermaid.js)
   - Diagrama de base de datos E-R (Mermaid.js)
   - Decisiones arquitectónicas (ADRs — Architecture Decision Records)

## 3. Modelo de Datos
   - Scripts DDL completos en PostgreSQL (Flyway V1__xxx.sql)
   - Índices y constraints
   - Datos semilla requeridos (si aplica)

## 4. Backend — Implementación
   - Estructura de paquetes del módulo
   - Entidades JPA (código completo)
   - Repositorios Spring Data JPA
   - Casos de uso / Services (código completo)
   - DTOs como Records Java
   - Controllers REST (código completo con anotaciones de validación)
   - Manejo de excepciones (GlobalExceptionHandler)
   - Configuración de seguridad (si aplica a la fase)

## 5. Frontend — Implementación
   - Estructura de carpetas del módulo
   - Tipos TypeScript (interfaces)
   - Servicios Axios (llamadas a la API)
   - Hooks personalizados (lógica de negocio)
   - Componentes React (código completo)
   - Páginas / Vistas
   - Configuración de rutas

## 6. APIs REST — Contrato
   - Endpoint, método HTTP, descripción
   - Request body / params (con ejemplos en JSON)
   - Response body (con ejemplos en JSON)
   - Códigos de respuesta HTTP esperados
   - Manejo de errores

## 7. Reglas de Negocio — Validaciones
   - Lista completa de validaciones por módulo
   - Casos borde identificados
   - Comportamiento esperado ante errores

## 8. Checklist de Implementación
   - [ ] DDL ejecutado y validado
   - [ ] Entidades JPA creadas
   - [ ] Repositorios implementados
   - [ ] Services / Use Cases implementados
   - [ ] Controllers REST implementados
   - [ ] Tipos TypeScript definidos
   - [ ] Servicios Axios implementados
   - [ ] Hooks personalizados creados
   - [ ] Componentes React implementados
   - [ ] Rutas configuradas
   - [ ] Validaciones activas (backend + frontend)
   - [ ] Manejo de errores implementado
   - [ ] Revisión de seguridad OWASP básica

## 9. Riesgos Técnicos Identificados
   - Descripción del riesgo, impacto y mitigación propuesta

## 10. Próxima Fase — Preview
   - Qué se construirá en la siguiente iteración
```

### 6.3 Criterios de Calidad del Código Generado

| Criterio | Estándar |
|---|---|
| **Completitud** | El código generado debe ser funcional, no pseudocódigo ni esqueletos incompletos |
| **Compilable** | El código Java debe compilar con Java 21 y Spring Boot 3.5 sin errores |
| **Tipado estricto** | El código TypeScript no debe tener `any` explícito |
| **Seguridad** | Toda API que acceda a datos debe validar que el recurso pertenece al usuario autenticado |
| **Consistencia** | Seguir siempre la estructura de paquetes `com.fml.fluxa.[módulo].[capa]` |
| **Documentación** | Comentarios en español latinoamericano para lógica de negocio compleja |
| **Valores monetarios** | Siempre `BigDecimal` en Java, `NUMERIC(15,2)` en PostgreSQL |
| **Fechas** | Siempre `TIMESTAMPTZ` en PostgreSQL, `Instant` o `LocalDate` en Java según contexto |

---

## 7. RESTRICCIONES Y CONSIDERACIONES FINALES

### Seguridad (Shift Left — OWASP Top 10)
- **A01 Broken Access Control:** Validar en cada endpoint que el recurso pertenece al usuario autenticado. Nunca confiar en el ID del path sin validar ownership.
- **A02 Cryptographic Failures:** Contraseñas con BCrypt (strength 12). JWT firmado con RS256 o HS512.
- **A03 Injection:** Usar siempre JPA / Prepared Statements. Zero SQL concatenado.
- **A07 Auth Failures:** Rate limiting en `/auth/login` (máximo 5 intentos / 15 minutos por IP).

### Rendimiento
- Toda consulta que agregue datos de múltiples meses debe usar índices sobre `period_month + period_year + user_id`.
- El dashboard no debe hacer más de 5 queries independientes. Considerar una query consolidada o proyección en la capa de aplicación.
- Las importaciones de archivos Excel/CSV deben procesarse de forma asíncrona (no en el request HTTP).

### Observabilidad
- Log estructurado (JSON) en producción con campos: `timestamp`, `level`, `userId`, `module`, `operation`, `duration_ms`, `message`.
- Métricas con Micrometer para: tiempo de respuesta por endpoint, tasa de errores, intentos de login fallidos.

### Tecnología Aburrida (Boring Technology)
- No introducir servicios de mensajería (Kafka, RabbitMQ) en el MVP. Las notificaciones email se envían de forma síncrona en Fase 1, y se evaluará asincronismo en Fase 4.
- No introducir caché (Redis) hasta demostrar cuellos de botella reales con métricas.

### Internacionalización
- Todos los montos se muestran en formato colombiano: `$1.500.000` (punto como separador de miles, sin decimales para COP).
- Fechas en formato `DD/MM/YYYY` en la UI.
- Zona horaria: `America/Bogota` (COT, UTC-5) en todos los timestamps de la aplicación.

---

*Documento generado para uso exclusivo como prompt de ingeniería en Claude.*  
*FLUXA — Gestión Financiera Personal | v1.0.0 | Abril 2026*
