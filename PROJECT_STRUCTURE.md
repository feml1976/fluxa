# FLUXA — Estructura del Monorepo
> **Versión:** 1.0.0 | **Tipo:** Monorepo (Backend + Frontend)  
> **Repo:** https://github.com/feml1976/fluxa.git

---

## Árbol Completo del Proyecto

```
fluxa/                                          ← Raíz del monorepo
│
├── CLAUDE.md                                   ← Configuración Claude Code (este proyecto)
├── .claudeignore                               ← Archivos ignorados por Claude Code
├── .gitignore                                  ← Archivos ignorados por Git
├── .env.example                                ← Plantilla de variables de entorno
├── docker-compose.yml                          ← PostgreSQL 18 local
├── README.md                                   ← Documentación general del proyecto
│
├── docs/                                       ← Documentación técnica
│   ├── FLUXA_MASTER_PROMPT.md                  ← Requerimiento completo del sistema
│   ├── PROJECT_STRUCTURE.md                    ← Este archivo
│   └── adr/                                    ← Architecture Decision Records
│       └── ADR-001-monorepo-structure.md
│
├── backend/                                    ← Aplicación Java / Spring Boot 3.5
│   ├── pom.xml                                 ← Maven — dependencias y build
│   └── src/
│       ├── main/
│       │   ├── java/com/fml/fluxa/
│       │   │   │
│       │   │   ├── FluxxaApplication.java      ← Main class — @SpringBootApplication
│       │   │   │
│       │   │   ├── auth/                       ← BC: Autenticación y Usuarios
│       │   │   │   ├── domain/
│       │   │   │   │   ├── model/
│       │   │   │   │   │   ├── User.java
│       │   │   │   │   │   ├── Group.java
│       │   │   │   │   │   └── PasswordResetToken.java
│       │   │   │   │   ├── port/
│       │   │   │   │   │   ├── in/             ← Puertos de entrada (use cases)
│       │   │   │   │   │   └── out/            ← Puertos de salida (repositorios)
│       │   │   │   │   └── service/
│       │   │   │   │       └── AuthDomainService.java
│       │   │   │   ├── application/
│       │   │   │   │   ├── usecase/
│       │   │   │   │   │   ├── LoginUseCase.java
│       │   │   │   │   │   ├── RegisterUseCase.java
│       │   │   │   │   │   └── PasswordResetUseCase.java
│       │   │   │   │   └── dto/
│       │   │   │   │       ├── LoginRequest.java      ← record
│       │   │   │   │       ├── LoginResponse.java     ← record
│       │   │   │   │       ├── RegisterRequest.java   ← record
│       │   │   │   │       └── UserResponse.java      ← record
│       │   │   │   └── infrastructure/
│       │   │   │       ├── persistence/
│       │   │   │       │   ├── UserJpaRepository.java
│       │   │   │       │   ├── GroupJpaRepository.java
│       │   │   │       │   └── UserRepositoryAdapter.java
│       │   │   │       ├── web/
│       │   │   │       │   └── AuthController.java
│       │   │   │       └── config/
│       │   │   │           ├── SecurityConfig.java
│       │   │   │           └── JwtConfig.java
│       │   │   │
│       │   │   ├── income/                     ← BC: Ingresos
│       │   │   │   ├── domain/model/
│       │   │   │   │   ├── IncomeSource.java
│       │   │   │   │   ├── IncomeRecord.java
│       │   │   │   │   └── IncomeCategory.java
│       │   │   │   ├── application/
│       │   │   │   │   ├── usecase/
│       │   │   │   │   └── dto/
│       │   │   │   └── infrastructure/
│       │   │   │       ├── persistence/
│       │   │   │       └── web/
│       │   │   │           └── IncomeController.java
│       │   │   │
│       │   │   ├── commitment/                 ← BC: Compromisos Fijos
│       │   │   │   ├── domain/model/
│       │   │   │   │   ├── FixedCommitment.java
│       │   │   │   │   └── CommitmentRecord.java
│       │   │   │   ├── application/
│       │   │   │   │   ├── usecase/
│       │   │   │   │   └── dto/
│       │   │   │   └── infrastructure/
│       │   │   │       ├── persistence/
│       │   │   │       └── web/
│       │   │   │           └── CommitmentController.java
│       │   │   │
│       │   │   ├── expense/                    ← BC: Gastos Variables
│       │   │   │   ├── domain/model/
│       │   │   │   │   ├── VariableExpense.java
│       │   │   │   │   ├── ExpenseCategory.java
│       │   │   │   │   └── BudgetPlan.java
│       │   │   │   ├── application/
│       │   │   │   │   ├── usecase/
│       │   │   │   │   └── dto/
│       │   │   │   └── infrastructure/
│       │   │   │       ├── persistence/
│       │   │   │       └── web/
│       │   │   │           └── ExpenseController.java
│       │   │   │
│       │   │   ├── credit/                     ← BC: Créditos y Deudas (NÚCLEO)
│       │   │   │   ├── domain/
│       │   │   │   │   ├── model/
│       │   │   │   │   │   ├── CreditCard.java
│       │   │   │   │   │   ├── TraditionalCredit.java
│       │   │   │   │   │   ├── MortgageCredit.java
│       │   │   │   │   │   ├── VehicleCredit.java
│       │   │   │   │   │   └── CreditPayment.java
│       │   │   │   │   ├── port/
│       │   │   │   │   │   ├── in/
│       │   │   │   │   │   └── out/
│       │   │   │   │   └── service/
│       │   │   │   │       ├── CreditAnalysisService.java   ← Motor inteligente
│       │   │   │   │       ├── AmortizationCalculator.java  ← Cálculos financieros
│       │   │   │   │       └── PaymentStrategyService.java  ← Avalanche vs Snowball
│       │   │   │   ├── application/
│       │   │   │   │   ├── usecase/
│       │   │   │   │   └── dto/
│       │   │   │   └── infrastructure/
│       │   │   │       ├── persistence/
│       │   │   │       └── web/
│       │   │   │           ├── CreditCardController.java
│       │   │   │           └── TraditionalCreditController.java
│       │   │   │
│       │   │   ├── dashboard/                  ← BC: Dashboard y Reportes
│       │   │   │   ├── application/
│       │   │   │   │   ├── usecase/
│       │   │   │   │   │   └── GetDashboardSummaryUseCase.java
│       │   │   │   │   └── dto/
│       │   │   │   │       └── DashboardSummaryResponse.java
│       │   │   │   └── infrastructure/
│       │   │   │       └── web/
│       │   │   │           └── DashboardController.java
│       │   │   │
│       │   │   ├── notification/               ← BC: Notificaciones (Fase 4)
│       │   │   │   ├── domain/model/
│       │   │   │   │   └── NotificationConfig.java
│       │   │   │   ├── application/
│       │   │   │   │   └── usecase/
│       │   │   │   │       └── SendPaymentAlertUseCase.java
│       │   │   │   └── infrastructure/
│       │   │   │       ├── email/
│       │   │   │       │   └── EmailService.java
│       │   │   │       └── scheduler/
│       │   │   │           └── AlertScheduler.java       ← @Scheduled
│       │   │   │
│       │   │   └── shared/                     ← Transversal
│       │   │       ├── domain/
│       │   │       │   └── exception/
│       │   │       │       ├── FluxaException.java
│       │   │       │       ├── ResourceNotFoundException.java
│       │   │       │       └── UnauthorizedException.java
│       │   │       ├── infrastructure/
│       │   │       │   └── web/
│       │   │       │       ├── GlobalExceptionHandler.java
│       │   │       │       ├── ApiResponse.java          ← record — wrapper estándar
│       │   │       │       └── PageResponse.java         ← record — paginación
│       │   │       └── util/
│       │   │           ├── MoneyFormatter.java           ← Formato COP
│       │   │           ├── DateUtils.java                ← Zona horaria Bogotá
│       │   │           └── FinancialCalculator.java      ← EA, MV, amortización
│       │   │
│       │   └── resources/
│       │       ├── application.yml                       ← Config base (sin credenciales)
│       │       ├── application-local.yml                 ← Config local (en .gitignore)
│       │       ├── application-prod.yml                  ← Config producción
│       │       ├── db/
│       │       │   └── migration/                        ← Scripts Flyway
│       │       │       ├── V1__create_users_groups.sql
│       │       │       ├── V2__create_income_tables.sql
│       │       │       ├── V3__create_commitment_tables.sql
│       │       │       ├── V4__create_expense_tables.sql
│       │       │       ├── V5__create_credit_tables.sql
│       │       │       └── V6__create_notification_tables.sql
│       │       └── templates/
│       │           └── email/                            ← Plantillas HTML de emails
│       │               ├── payment-alert.html
│       │               └── welcome.html
│       │
│       └── test/                                         ← Tests (estructura espejo)
│           └── java/com/fml/fluxa/
│
│
└── frontend/                                   ← Aplicación React 18 / TypeScript
    ├── package.json
    ├── tsconfig.json                           ← strict: true
    ├── vite.config.ts
    ├── index.html
    └── src/
        ├── main.tsx                            ← Entry point
        ├── App.tsx                             ← Router principal
        │
        ├── modules/
        │   ├── auth/
        │   │   ├── api/
        │   │   │   └── authApi.ts              ← Axios calls
        │   │   ├── hooks/
        │   │   │   ├── useAuth.ts
        │   │   │   └── useLogin.ts
        │   │   ├── components/
        │   │   │   ├── LoginForm.tsx
        │   │   │   └── RegisterForm.tsx
        │   │   ├── pages/
        │   │   │   ├── LoginPage.tsx
        │   │   │   └── RegisterPage.tsx
        │   │   └── types/
        │   │       └── auth.types.ts
        │   │
        │   ├── income/
        │   │   ├── api/incomeApi.ts
        │   │   ├── hooks/
        │   │   │   ├── useIncomes.ts
        │   │   │   └── useIncomeForm.ts
        │   │   ├── components/
        │   │   │   ├── IncomeList.tsx
        │   │   │   ├── IncomeForm.tsx
        │   │   │   └── IncomeSummaryCard.tsx
        │   │   ├── pages/IncomePage.tsx
        │   │   └── types/income.types.ts
        │   │
        │   ├── commitment/
        │   │   ├── api/commitmentApi.ts
        │   │   ├── hooks/
        │   │   ├── components/
        │   │   │   ├── CommitmentList.tsx
        │   │   │   ├── CommitmentForm.tsx
        │   │   │   └── CommitmentStatusBadge.tsx
        │   │   ├── pages/CommitmentPage.tsx
        │   │   └── types/commitment.types.ts
        │   │
        │   ├── expense/
        │   │   ├── api/expenseApi.ts
        │   │   ├── hooks/
        │   │   │   ├── useExpenses.ts
        │   │   │   └── useBudgetAnalysis.ts
        │   │   ├── components/
        │   │   │   ├── ExpenseList.tsx
        │   │   │   ├── ExpenseForm.tsx
        │   │   │   └── BudgetProgressBar.tsx
        │   │   ├── pages/ExpensePage.tsx
        │   │   └── types/expense.types.ts
        │   │
        │   ├── credit/
        │   │   ├── api/
        │   │   │   ├── creditCardApi.ts
        │   │   │   └── traditionalCreditApi.ts
        │   │   ├── hooks/
        │   │   │   ├── useCreditPortfolio.ts
        │   │   │   ├── useCreditCardAnalysis.ts    ← % utilización, alertas
        │   │   │   ├── usePaymentStrategy.ts       ← Avalanche vs Snowball
        │   │   │   └── useExtraPaymentSimulator.ts ← Simulador abono
        │   │   ├── components/
        │   │   │   ├── CreditCard/
        │   │   │   │   ├── CreditCardSummary.tsx
        │   │   │   │   ├── CreditCardForm.tsx
        │   │   │   │   └── CreditCardAlerts.tsx
        │   │   │   ├── TraditionalCredit/
        │   │   │   │   ├── TraditionalCreditSummary.tsx
        │   │   │   │   └── AmortizationTable.tsx
        │   │   │   ├── DebtRanking.tsx             ← Mayor saldo / mayor tasa
        │   │   │   ├── PaymentStrategyCard.tsx     ← Avalanche vs Snowball
        │   │   │   └── ExtraPaymentSimulator.tsx
        │   │   ├── pages/
        │   │   │   ├── CreditPortfolioPage.tsx
        │   │   │   └── CreditDetailPage.tsx
        │   │   └── types/credit.types.ts
        │   │
        │   └── dashboard/
        │       ├── api/dashboardApi.ts
        │       ├── hooks/
        │       │   └── useDashboard.ts
        │       ├── components/
        │       │   ├── FlowSummaryCard.tsx         ← Flujo neto mensual
        │       │   ├── HealthIndicator.tsx          ← Semáforo % comprometido
        │       │   ├── UpcomingPayments.tsx         ← Próximos vencimientos
        │       │   ├── TopExpensesChart.tsx         ← Recharts — top 5
        │       │   ├── DebtEvolutionChart.tsx       ← Recharts — línea mensual
        │       │   └── IncomeVsExpenseChart.tsx     ← Recharts — barras
        │       ├── pages/DashboardPage.tsx
        │       └── types/dashboard.types.ts
        │
        └── shared/
            ├── api/
            │   ├── axiosConfig.ts              ← Interceptors, base URL, auth header
            │   └── apiTypes.ts                 ← ApiResponse<T>, PageResponse<T>
            ├── components/
            │   ├── Layout/
            │   │   ├── AppLayout.tsx           ← Sidebar + Header + Content
            │   │   ├── Sidebar.tsx
            │   │   └── Header.tsx
            │   ├── ProtectedRoute.tsx          ← Guard de autenticación
            │   ├── LoadingSpinner.tsx
            │   ├── ConfirmDialog.tsx
            │   ├── EmptyState.tsx
            │   └── AlertBanner.tsx             ← Alertas financieras
            ├── hooks/
            │   ├── useCurrentUser.ts
            │   └── useNotification.ts
            ├── store/
            │   ├── authStore.ts                ← Zustand — sesión del usuario
            │   └── notificationStore.ts        ← Zustand — alertas globales
            ├── types/
            │   └── global.types.ts
            └── utils/
                ├── currencyFormatter.ts        ← Formato COP: $1.500.000
                ├── dateFormatter.ts            ← DD/MM/YYYY | America/Bogota
                └── financialCalculator.ts      ← EA, MV, amortización (TS)
```

---

## Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes Java | lowercase | `com.fml.fluxa.credit.domain` |
| Clases Java | PascalCase | `CreditAnalysisService` |
| Records DTO | PascalCase + sufijo | `CreditSummaryResponse` |
| Variables Java | camelCase | `currentBalance` |
| Constantes Java | UPPER_SNAKE | `MAX_LOGIN_ATTEMPTS` |
| Tablas PostgreSQL | snake_case plural | `credit_cards` |
| Columnas PostgreSQL | snake_case singular | `current_balance` |
| Archivos Flyway | `VN__descripcion.sql` | `V1__create_users.sql` |
| Componentes React | PascalCase | `CreditCardSummary` |
| Hooks React | camelCase + `use` | `useCreditCardAnalysis` |
| Archivos TypeScript | camelCase / PascalCase | `creditCard.types.ts` |
| Variables TS | camelCase | `utilizationPercentage` |

---

## Scripts de Base de Datos — Orden de Migración Flyway

```
V1  → users, groups, password_reset_tokens
V2  → income_categories, income_sources, income_records
V3  → expense_categories, fixed_commitments, commitment_records
V4  → variable_expenses, budget_plans
V5  → credit_cards, traditional_credits, mortgage_credits,
       vehicle_credits, credit_payments
V6  → notification_configs, notification_logs
V7  → import_logs (Fase 4)
```

> **REGLA CRÍTICA:** Un script Flyway ejecutado **NUNCA** se modifica.
> Para correcciones, crear un nuevo script `VN+1__fix_descripcion.sql`.

---

*FLUXA — PROJECT_STRUCTURE.md v1.0.0 | Abril 2026*
