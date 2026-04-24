# Manual de Usuario — FLUXA
### Gestión Financiera Personal

**Versión:** 1.0 | **Fecha:** Abril 2026 | **Audiencia:** Usuario final

---

## Tabla de contenido

1. [¿Qué es FLUXA?](#1-qué-es-fluxa)
2. [Registro como usuario nuevo](#2-registro-como-usuario-nuevo)
3. [Inicio de sesión](#3-inicio-de-sesión)
4. [Navegación general](#4-navegación-general)
5. [Dashboard — Panel financiero](#5-dashboard--panel-financiero)
6. [Ingresos](#6-ingresos)
7. [Compromisos Fijos](#7-compromisos-fijos)
8. [Gastos Variables](#8-gastos-variables)
9. [Créditos y Deudas](#9-créditos-y-deudas)
10. [Importación de Gastos](#10-importación-de-gastos)
11. [Notificaciones](#11-notificaciones)
12. [Glosario Financiero](#12-glosario-financiero)
13. [Preguntas Frecuentes](#13-preguntas-frecuentes)

---

## 1. ¿Qué es FLUXA?

FLUXA es una aplicación web de gestión financiera personal pensada para el contexto colombiano. Le permite:

- Registrar sus **ingresos** fijos y variables mes a mes.
- Controlar sus **compromisos fijos** (arriendo, servicios públicos, seguros, etc.) y marcarlos como pagados.
- Llevar un registro detallado de sus **gastos variables** del día a día.
- Gestionar sus **créditos y deudas** (tarjetas de crédito, créditos personales, hipotecarios y de vehículo).
- Ver un **dashboard** con su salud financiera, flujo neto y proyecciones a futuro.
- **Importar** gastos masivamente desde un archivo CSV o Excel.
- Recibir **alertas por correo electrónico** ante vencimientos próximos, mora en tarjetas o cupo agotado.

Todos los valores están expresados en **pesos colombianos (COP)**. No hay soporte para otras monedas.

---

## 2. Registro como usuario nuevo

### Pasos para crear su cuenta

1. Abra su navegador web y vaya a la dirección: `http://localhost:5173` (o la URL que le haya indicado su administrador).
2. Verá la pantalla de inicio de sesión. Busque el enlace **"Regístrate aquí"** que aparece debajo del botón de ingreso y haga clic en él.
3. Será redirigido al formulario de registro. Complete los siguientes campos:

| Campo | Descripción | Ejemplo |
|---|---|---|
| **Nombre completo** | Su nombre y apellido | `Francisco Montoya` |
| **Correo electrónico** | Será su usuario de acceso | `francisco@gmail.com` |
| **Contraseña** | Mínimo 8 caracteres. Debe incluir al menos una letra mayúscula, una minúscula, un número y un carácter especial (`@`, `$`, `!`, `%`, `*`, `?`, `&`) | `MiClave2026!` |
| **Confirmar contraseña** | Repita la contraseña exactamente igual | `MiClave2026!` |

4. Haga clic en el botón azul **"Crear cuenta"**.
5. Si todo está correcto, será redirigido automáticamente a la pantalla de inicio de sesión y verá el mensaje:
   > **Cuenta creada exitosamente. Ya puede iniciar sesión.**

### Errores comunes al registrarse

| Mensaje de error | Causa | Solución |
|---|---|---|
| `El correo ya está registrado` | Ya existe una cuenta con ese email | Use otro correo o recupere su contraseña |
| `La contraseña no cumple los requisitos` | Falta mayúscula, número o carácter especial | Revise que su contraseña incluya todos los requisitos |
| `Las contraseñas no coinciden` | Los dos campos de contraseña son distintos | Vuelva a escribir la contraseña con cuidado |
| `No se puede conectar con el servidor` | El backend no está iniciado | Contacte a su administrador |

---

## 3. Inicio de sesión

1. En la pantalla de inicio de sesión, ingrese su **correo electrónico** y su **contraseña**.
2. Haga clic en **"Iniciar sesión"**.
3. Si las credenciales son correctas, será llevado automáticamente al **Dashboard**.

> **Tip:** Si olvidó su contraseña, contacte a su administrador de sistema para que la restablezca.

---

## 4. Navegación general

Una vez dentro de la aplicación, verá una **barra lateral izquierda** con el menú de navegación. Las opciones disponibles son:

| Opción del menú | ¿Para qué sirve? |
|---|---|
| **Dashboard** | Panel principal con resumen financiero, gráficas y proyecciones |
| **Ingresos** | Registrar fuentes de ingreso y los ingresos recibidos cada mes |
| **Compromisos** | Gestionar pagos fijos recurrentes y marcarlos como pagados |
| **Gastos** | Registrar gastos del día a día por categoría |
| **Créditos** | Gestionar tarjetas de crédito, préstamos y créditos de cualquier tipo |
| **Importar** | Cargar gastos masivamente desde un archivo CSV o Excel |
| **Notificaciones** | Ver historial de alertas enviadas por correo |

Para cambiar de módulo, simplemente haga clic en la opción deseada en el menú lateral.

---

## 5. Dashboard — Panel financiero

El Dashboard es la pantalla principal. Muestra un **resumen completo de su situación financiera** para el mes y año seleccionados.

### 5.1 Selector de período

En la esquina superior derecha del Dashboard encontrará dos listas desplegables:
- **Mes:** seleccione el mes que desea analizar (p. ej.: `abril`).
- **Año:** seleccione el año (p. ej.: `2026`).

El panel se actualiza automáticamente al cambiar el período.

### 5.2 Indicador de salud financiera

Es la barra coloreada que aparece justo debajo del selector de período. Indica qué porcentaje de sus ingresos esperados está comprometido (compromisos + gastos + cuotas de créditos).

| Color | Significado | % comprometido |
|---|---|---|
| **Verde** | Situación saludable | Menos del 40% |
| **Amarillo** | Precaución — revise sus gastos | Entre 40% y 60% |
| **Rojo** | Riesgo — capacidad de pago comprometida | Más del 60% |

**Ejemplo:** Si su ingreso esperado es **$5.000.000** y tiene compromisos + gastos por **$2.200.000**, su porcentaje comprometido es el **44%** → indicador en **Amarillo**.

### 5.3 Tarjetas KPI (indicadores clave)

Debajo del indicador de salud verá cuatro tarjetas con los datos del mes seleccionado:

| Tarjeta | Qué muestra |
|---|---|
| **Ingresos Recibidos** | Total de ingresos que ya marcó como recibidos. Debajo aparece el ingreso esperado. |
| **Compromisos Fijos** | Total de sus compromisos del mes. Muestra cuántos están pendientes y cuántos vencidos. |
| **Gastos Variables** | Total gastado en el mes. Debajo aparece el presupuesto planeado. |
| **Flujo Neto** | Resultado: Ingresos − Compromisos − Gastos − Créditos. Verde si es positivo, rojo si es negativo. |

**Ejemplo práctico:**

```
Ingresos recibidos:    $4.800.000
Compromisos fijos:     $1.500.000  (2 pendientes / 0 vencidos)
Gastos variables:      $1.200.000  (presupuestado: $1.400.000)
Flujo neto:            $2.100.000  ✅ (positivo — dinero disponible)
```

### 5.4 Top 5 categorías de gasto

Gráfico circular (torta) que muestra las cinco categorías donde más gastó en el período. Cada porción muestra el nombre de la categoría y su participación porcentual.

**Ejemplo:** Si gastó $500.000 en Alimentación de un total de $1.200.000, la categoría ocuparía el 41,7% del gráfico.

### 5.5 Próximos vencimientos

Tabla que muestra los compromisos fijos que vencen en los **próximos 7 días**, con la siguiente información:

| Columna | Descripción |
|---|---|
| Compromiso | Nombre del pago pendiente |
| Monto | Valor estimado a pagar |
| Vence en | Etiqueta que indica cuántos días faltan |

Las etiquetas de días cambian de color según la urgencia:
- **Rojo:** vence hoy o mañana.
- **Naranja:** vence en 2 o 3 días.
- **Gris:** vence en 4 a 7 días.

### 5.6 Proyección de flujo neto

Gráfico de barras apiladas que muestra la proyección de sus finanzas para los próximos 3, 6 o 12 meses (seleccionable con la lista desplegable de la esquina superior derecha del bloque).

**¿Cómo se calculan las proyecciones?**
- **Ingresos proyectados:** se usan las fuentes de ingreso fijo activas, ajustando frecuencias (quincenal × 2, semanal × 4).
- **Compromisos proyectados:** total de compromisos fijos activos, prorrateados al mes.
- **Gastos variables proyectados:** promedio de lo gastado en los últimos 3 meses.
- **Obligaciones de crédito:** cuotas mensuales de créditos activos + pago mínimo de tarjetas.

Sobre el gráfico se muestran tres indicadores:
- **Ingreso prom. proyectado:** cuánto espera recibir en promedio por mes.
- **Egreso prom. proyectado:** cuánto tiene comprometido en promedio por mes.
- **Flujo neto prom.:** lo que quedaría disponible en promedio.

### 5.7 Estrategias de pago de deuda

Si tiene créditos activos, el Dashboard calcula automáticamente cuánto ahorraría en intereses usando dos estrategias:

| Estrategia | Descripción |
|---|---|
| **Avalanche** | Prioriza el crédito con la **tasa de interés más alta**. Es la más eficiente matemáticamente para reducir el costo total de los intereses. |
| **Snowball** | Prioriza el crédito con el **saldo más bajo**. Genera motivación rápida al liquidar deudas pequeñas primero. |

Para cada estrategia verá: deuda total, interés total a pagar, meses estimados para quedar libre de deudas y el ahorro comparado con pagar solo el mínimo.

---

## 6. Ingresos

### ¿Qué se registra en Ingresos?

Todo el dinero que **entra** a su bolsillo: salario, honorarios, arriendos que recibe, pensiones, comisiones, ventas ocasionales, etc.

El módulo tiene dos secciones (pestañas):

### 6.1 Pestaña: Resumen mensual

Muestra todos los ingresos esperados y recibidos en el mes seleccionado. Para cada fila puede ver:
- Nombre de la fuente de ingreso
- Monto esperado
- Monto recibido
- Estado: `EXPECTED` (esperado), `RECEIVED` (recibido), `PARTIAL` (parcial) o `NOT_RECEIVED` (no recibido)

**Cómo registrar un ingreso recibido:**

1. Ubique la fila del ingreso que desea registrar.
2. Haga clic en el ícono de moneda/pago al final de la fila.
3. Se abrirá el formulario **"Registrar ingreso"**. Complete:
   - **Monto recibido:** valor real que llegó a su cuenta.
   - **Estado:** seleccione el estado correcto.
   - **Fecha de recepción:** la fecha en que efectivamente recibió el dinero.
   - **Notas:** observación opcional.
4. Haga clic en **"Guardar"**.

**Ejemplo:**
```
Fuente: Salario Bancolombia
Monto esperado: $4.500.000
Monto recibido: $4.500.000
Estado: RECEIVED
Fecha de recepción: 30/04/2026
Notas: Nómina quincenal — segunda quincena abril
```

### 6.2 Pestaña: Fuentes de ingreso

Lista de todas las fuentes registradas. Desde aquí puede crear, editar y eliminar fuentes.

**Cómo crear una nueva fuente de ingreso:**

1. Haga clic en el botón azul **"Nueva fuente"** (esquina superior derecha).
2. Se abrirá el diálogo. Complete los campos:

| Campo | Descripción | Ejemplo |
|---|---|---|
| **Nombre** | Identificador de la fuente | `Salario empresa` |
| **Descripción** | Detalle opcional | `Nómina mensual Transer S.A.` |
| **Tipo** | `FIXED` (fijo) o `VARIABLE` | `FIXED` |
| **Frecuencia** | Con qué periodicidad llega | `MONTHLY` |
| **Monto esperado** | Valor que espera recibir | `4500000` |
| **Fecha inicio** | Desde cuándo aplica | `01/01/2026` |
| **Fecha fin** | Hasta cuándo aplica (opcional) | *(dejar vacío si es indefinido)* |

3. Haga clic en **"Guardar"**.

**Frecuencias disponibles:**

| Opción | Significado |
|---|---|
| `MONTHLY` | Mensual — una vez al mes |
| `BIWEEKLY` | Quincenal — dos veces al mes |
| `WEEKLY` | Semanal — cuatro veces al mes |
| `ONE_TIME` | Único — ingreso puntual, no recurrente |

**Ejemplo completo: tres fuentes de ingreso típicas**

```
1. Fuente: Salario principal
   Tipo: FIXED | Frecuencia: MONTHLY | Monto: $4.500.000
   Inicio: 01/01/2026 | Activo: Sí

2. Fuente: Arriendo bodega
   Tipo: FIXED | Frecuencia: MONTHLY | Monto: $800.000
   Inicio: 01/03/2026 | Activo: Sí

3. Fuente: Venta productos
   Tipo: VARIABLE | Frecuencia: ONE_TIME | Monto: $350.000
   Inicio: 15/04/2026 | Activo: Sí
```

**Editar una fuente:** haga clic en el ícono de lápiz (✏️) al final de la fila.

**Eliminar una fuente:** haga clic en el ícono de papelera (🗑️). Esta acción es permanente.

---

## 7. Compromisos Fijos

### ¿Qué se registra en Compromisos?

Todo pago **recurrente y obligatorio** que debe hacer cada mes (o cada bimestre, trimestre o año): arriendo, cuota de administración, servicios públicos, internet, seguro de vida, seguro del vehículo, cuota del colegio, etc.

> **Diferencia clave:** Los compromisos son pagos que ya tiene pactados de antemano y que tienen fecha de vencimiento. Se diferencian de los gastos variables (que son libres, no tienen fecha fija ni monto predeterminado).

El módulo tiene dos pestañas:

### 7.1 Pestaña: Mis compromisos

Lista de todos sus compromisos registrados.

**Cómo crear un nuevo compromiso:**

1. Haga clic en **"Nuevo compromiso"** (esquina superior derecha).
2. Complete el formulario:

| Campo | Descripción | Ejemplo |
|---|---|---|
| **Nombre** | Nombre del compromiso | `Arriendo apartamento` |
| **Descripción** | Detalle opcional | `Apartamento Laureles, contrato 2025` |
| **Monto estimado** | Valor que paga normalmente | `1200000` |
| **Día de pago** | Día del mes en que vence | `5` |
| **Frecuencia** | Cada cuánto se repite | `Mensual` |
| **Alerta (días antes)** | Con cuántos días de anticipación quiere recibir alerta | `3` |

3. Haga clic en **"Guardar"**.

**Frecuencias disponibles:**

| Opción | Significado |
|---|---|
| `Mensual` | Se genera un registro cada mes |
| `Bimestral` | Cada dos meses (ej.: seguros de vehículo) |
| `Trimestral` | Cada tres meses (ej.: declaración de renta) |
| `Anual` | Una vez al año (ej.: póliza hogar) |

**Ejemplo: compromisos típicos de un hogar en Medellín**

```
1. Arriendo apartamento
   Monto: $1.200.000 | Día vence: 5 | Frecuencia: Mensual | Alerta: 3 días antes

2. EPM (energía y agua)
   Monto: $180.000 | Día vence: 20 | Frecuencia: Mensual | Alerta: 5 días antes

3. Internet Claro
   Monto: $85.000 | Día vence: 15 | Frecuencia: Mensual | Alerta: 3 días antes

4. Seguro vehículo SURA
   Monto: $720.000 | Día vence: 10 | Frecuencia: Bimestral | Alerta: 7 días antes

5. Cuota administración
   Monto: $250.000 | Día vence: 1 | Frecuencia: Mensual | Alerta: 2 días antes
```

**Editar:** ícono de lápiz en la fila del compromiso.

**Eliminar:** ícono de papelera. El compromiso se desactiva con borrado lógico (no se eliminan los registros históricos).

### 7.2 Pestaña: Resumen mensual

Muestra los registros generados para el mes y año seleccionados. Para cada compromiso aparece: nombre, monto, fecha de vencimiento y estado.

**Estados posibles:**

| Estado | Significado |
|---|---|
| `PENDING` | Pendiente de pago |
| `PAID` | Pagado |
| `OVERDUE` | Vencido — pasó la fecha y no se pagó |

**Cómo registrar el pago de un compromiso:**

1. Ubique el compromiso que ya pagó.
2. Haga clic en el ícono de pago (billete) al final de la fila.
3. Se abrirá el formulario **"Registrar pago"**. Complete:
   - **Monto pagado:** el valor real que pagó (puede diferir del estimado).
   - **Fecha de pago:** día en que efectuó el pago.
   - **Referencia:** número de comprobante o referencia del banco (opcional).
   - **Notas:** observación adicional (opcional).
4. Haga clic en **"Guardar"**. El estado del compromiso cambia automáticamente a `PAID`.

**Ejemplo:**

```
Compromiso: EPM (energía y agua)
Monto estimado: $180.000
Monto pagado: $174.350   ← el recibo llegó un poco menos
Fecha de pago: 18/04/2026
Referencia: 20261804-EPM-00234
Notas: Pago virtual desde app EPM
```

> **Consejo:** Registre los pagos el mismo día que los realiza para mantener su historial al día. El Dashboard actualiza los indicadores en tiempo real.

---

## 8. Gastos Variables

### ¿Qué se registra en Gastos?

Todo gasto **no planeado de antemano** y que varía mes a mes: mercado, gasolina, restaurantes, ropa, entretenimiento, medicamentos, peluquería, domicilios, etc.

### 8.1 Resumen del mes

En la parte superior verá tres tarjetas:

| Tarjeta | Descripción |
|---|---|
| **Total Gastado** | Suma de todos los gastos registrados en el período |
| **Presupuestado** | Total de presupuestos asignados por categoría para el período |
| **% Ejecutado** | Qué porcentaje del presupuesto ya se consumió |

La barra de progreso del % Ejecutado cambia de color:
- **Verde:** menos del 80% ejecutado.
- **Naranja:** entre 80% y 100%.
- **Rojo:** se pasó del presupuesto.

### 8.2 Pestaña: Gastos

Tabla con todos los gastos registrados en el mes. Columnas: Fecha, Categoría, Descripción, Monto y Acciones.

**Cómo registrar un nuevo gasto:**

1. Haga clic en el botón azul **"Nuevo Gasto"** (esquina superior derecha).
2. Complete el formulario:

| Campo | Descripción | Ejemplo |
|---|---|---|
| **Categoría** | Tipo de gasto | `Alimentación` |
| **Monto** | Valor del gasto | `185000` |
| **Fecha** | Día en que ocurrió | `08/04/2026` |
| **Descripción** | Detalle opcional | `Mercado Éxito semana 2` |

3. Haga clic en **"Guardar"**.

**Categorías disponibles por defecto:**

| Categoría | Ejemplos de gastos |
|---|---|
| Educación | Matrícula, útiles, cursos online |
| Entretenimiento | Cine, conciertos, plataformas streaming |
| Ropa y Accesorios | Ropa, zapatos, bolsos |
| Salud y Bienestar | Medicamentos, consultas médicas, gimnasio |
| Tecnología | Accesorios, reparaciones de equipos |

**Editar un gasto:** ícono de lápiz (✏️) en la fila. Útil para corregir un monto o categoría mal ingresado.

**Eliminar un gasto:** ícono de papelera (🗑️).

**Ejemplo: registro de gastos de una semana típica**

```
08/04  Alimentación    Mercado Éxito semana 2          $210.000
09/04  Entretenimiento Cine y pop-corn con la familia  $95.000
10/04  Salud           Medicamentos farmacia            $67.000
11/04  Entretenimiento Domicilio pizza Friday            $38.000
12/04  Ropa            Tenis nuevos                    $180.000
```

### 8.3 Pestaña: Presupuestos

Muestra el presupuesto por categoría junto con lo gastado real y un presupuesto **sugerido** (calculado automáticamente por el sistema con base en el historial).

| Columna | Descripción |
|---|---|
| Categoría | Nombre de la categoría |
| Presupuestado | Monto que usted asignó para esa categoría en el mes |
| Gastado | Cuánto ha gastado en esa categoría |
| Sugerido | Recomendación del sistema con base en meses anteriores |
| % Ejec. | Porcentaje del presupuesto consumido |

**Eliminar un presupuesto:** ícono de papelera. Eliminar el presupuesto no borra los gastos — solo elimina el límite asignado.

> **Consejo:** Compare el monto sugerido con el que usted asignó. Si el sugerido es mayor, el sistema detectó que históricamente usted gasta más de lo que presupuesta en esa categoría.

---

## 9. Créditos y Deudas

### ¿Qué se registra en Créditos?

Todas las deudas financieras vigentes: tarjetas de crédito, créditos de consumo (personal), crédito hipotecario (vivienda) y crédito de vehículo.

> **Importante:** registrar sus créditos aquí permite que el Dashboard calcule correctamente su flujo neto, la salud financiera y las estrategias de pago de deuda (Avalanche / Snowball).

### 9.1 Resumen de créditos

En la parte superior verá cuatro tarjetas:

| Tarjeta | Descripción |
|---|---|
| **Deuda Total** | Suma de saldos actuales de todos los créditos activos |
| **Obligaciones/mes** | Total de cuotas y pagos mínimos que debe hacer cada mes |
| **Tarjetas con mora** | Cantidad de tarjetas que tienen intereses de mora activos |
| **Tarjetas sin cupo** | Cantidad de tarjetas con cupo disponible en cero |

### 9.2 Filtros por tipo

Puede filtrar la lista de créditos usando las pestañas:
- **Todos** — muestra todos los créditos.
- **Tarjetas** — solo tarjetas de crédito.
- **Personal** — créditos de consumo personal.
- **Hipotecario** — crédito de vivienda.
- **Vehículo** — crédito de automóvil o moto.

### 9.3 Cómo registrar un nuevo crédito

Haga clic en **"Nuevo Crédito"** (esquina superior derecha).

#### Campos comunes a todos los tipos:

| Campo | Descripción | Ejemplo |
|---|---|---|
| **Tipo** | Tipo de crédito | `Tarjeta de Crédito` |
| **Nombre** | Nombre que le dará al crédito | `Visa Bancolombia` |
| **Tasa MV (%)** | Tasa mensual vencida | `2.1` |
| **Saldo actual** | Deuda vigente | `3500000` |
| **Fecha apertura** | Cuándo abrió el crédito | `15/03/2022` |

#### Campos adicionales para créditos no tarjeta (Personal / Hipotecario / Vehículo):

| Campo | Descripción | Ejemplo |
|---|---|---|
| **Cuota mensual** | Valor de la cuota que paga cada mes | `850000` |
| **Plazo (cuotas)** | Total de cuotas del crédito | `48` |
| **Cuotas pagadas** | Cuántas cuotas ya ha cancelado | `12` |

#### Campos adicionales exclusivos de Tarjeta de Crédito:

| Campo | Descripción | Ejemplo |
|---|---|---|
| **Últimos 4 dígitos** | Número de identificación de la tarjeta | `5231` |
| **Franquicia** | VISA / MASTERCARD / AMEX / DINERS / OTHER | `VISA` |
| **Cupo compras** | Cupo total autorizado para compras | `10000000` |
| **Disponible compras** | Cupo que le queda para compras | `6500000` |
| **Cupo avances** | Cupo total para retiros en efectivo | `3000000` |
| **Disponible avances** | Cupo que le queda para avances | `3000000` |
| **Saldo anterior** | Saldo del último extracto | `3500000` |
| **Pago mínimo** | Valor del pago mínimo según el extracto | `175000` |
| **Pago Mínimo Alterno** | Si lo tiene, el valor del pago mínimo alterno | `520000` |
| **Intereses de mora** | Valor de intereses de mora (0 si está al día) | `0` |
| **Día de pago** | Día del mes en que vence el pago | `25` |

### Alertas automáticas en tarjetas

El sistema analiza cada tarjeta y muestra una etiqueta de alerta:

| Alerta | Condición | Acción recomendada |
|---|---|---|
| Sin alerta | Todo en orden | Continúe normalmente |
| ⚠️ Amarilla | Utilización superior al 80% | Evite más gastos en la tarjeta |
| 🔴 Roja (mora) | Tiene intereses de mora activos | Pague inmediatamente el mínimo |
| 🔴 Roja (cupo) | Cupo disponible = $0 | No puede realizar más compras |

### 9.4 Ejemplo completo: tarjeta Davivienda Mastercard

```
Tipo: Tarjeta de Crédito
Nombre: Mastercard Davivienda
Tasa MV: 2.4%
Saldo actual: $2.800.000
Fecha apertura: 10/06/2021
Últimos 4 dígitos: 8847
Franquicia: MASTERCARD
Cupo compras: $5.000.000
Disponible compras: $2.200.000   → Utilización: 56%
Cupo avances: $1.500.000
Disponible avances: $1.500.000
Saldo anterior: $2.800.000
Pago mínimo: $140.000
Pago Mínimo Alterno: $415.000
Intereses de mora: $0
Día de pago: 20
```

### 9.5 Ejemplo completo: crédito vehículo Bancolombia

```
Tipo: Vehículo
Nombre: Crédito Toyota Yaris
Tasa MV: 1.45%
Saldo actual: $28.500.000
Fecha apertura: 01/09/2023
Cuota mensual: $790.000
Plazo (cuotas): 60
Cuotas pagadas: 19
Cuotas restantes: 41   ← calculado automáticamente
```

### 9.6 Registrar un pago

1. En la tarjeta del crédito, haga clic en el ícono de pago (💳).
2. Complete el formulario:
   - **Mes / Año:** período al que corresponde el pago.
   - **Monto pagado:** valor que efectivamente pagó.
   - **Fecha de pago:** día en que realizó el pago.
   - **Notas:** referencia del comprobante (opcional).
3. Haga clic en **"Registrar Pago"**.

### 9.7 Ver análisis detallado de un crédito

En cada crédito hay un ícono de flecha hacia abajo (▼) que despliega un panel de análisis con:
- Proyección de cuotas restantes.
- Interés total que pagará si sigue el plan actual.
- Recomendaciones específicas para ese crédito.

---

## 10. Importación de Gastos

La importación le permite cargar muchos gastos de una sola vez desde un archivo preparado en Excel o en texto plano (CSV). Es útil cuando lleva un registro en una hoja de cálculo o cuando quiere cargar el historial de meses anteriores.

### 10.1 Formato del archivo

El archivo debe tener exactamente **4 columnas** en este orden:

| Columna | Nombre | Descripción |
|---|---|---|
| 1 | `tipo` | Siempre debe decir `gasto` (en minúscula) |
| 2 | `descripcion` | Descripción del gasto |
| 3 | `monto` | Valor numérico sin puntos ni signos (ej: `185000`) |
| 4 | `fecha` | Fecha en formato `DD/MM/YYYY` o `YYYY-MM-DD` |

**Ejemplo de archivo CSV válido:**

```csv
tipo,descripcion,monto,fecha
gasto,Mercado Éxito semana 1,185000,01/04/2026
gasto,Gasolina vehículo,120000,03/04/2026
gasto,Almuerzo restaurante,28500,04/04/2026
gasto,Domicilio Rappi,35000,05/04/2026
gasto,Medicamentos farmacia,67000,07/04/2026
gasto,Mercado Éxito semana 2,210000,08/04/2026
```

**Reglas importantes:**
- La primera fila debe ser el encabezado: `tipo,descripcion,monto,fecha`.
- El tipo `gasto` es el único admitido en esta versión.
- El monto no debe llevar puntos, comas ni el símbolo `$`.
- No deje filas vacías en el medio del archivo.

### 10.2 Pasos para importar

1. Prepare su archivo en Excel (`.xlsx`) o texto plano (`.csv`) con el formato descrito.
2. Vaya a la opción **"Importar"** en el menú lateral.
3. Haga clic en **"Seleccionar archivo"** y elija su archivo.
4. Una vez seleccionado el archivo, aparece el botón **"Vista previa"** — haga clic en él.
5. El sistema analiza el archivo y muestra una tabla con el resultado:
   - **Filas totales:** cuántas filas leyó.
   - **Filas válidas:** cuántas están listas para importar.
   - **Filas con error:** cuántas tienen problemas (aparecen en rojo con el detalle del error).
6. Revise las filas con error. Los problemas más comunes son:
   - Fecha en formato incorrecto.
   - Monto vacío o con caracteres no numéricos.
   - Tipo diferente a `gasto`.
7. Si está conforme con las filas válidas, haga clic en **"Importar N gasto(s)"**.
8. Verá el mensaje de confirmación con el número de gastos importados exitosamente.

> **Consejo:** Corrija los errores en el archivo original, guárdelo y vuelva a cargarlo. No hay riesgo de duplicados si repite la importación para las filas que ya estaban marcadas como válidas — el sistema las crea como registros nuevos.

---

## 11. Notificaciones

FLUXA envía alertas automáticas por correo electrónico ante eventos importantes:

| Tipo de alerta | Cuándo se envía |
|---|---|
| **Vencimiento próximo** | Cuando un compromiso fijo vence en los próximos días (según el número de días de alerta que configuró) |
| **Compromiso vencido** | Cuando un compromiso llegó a su fecha de pago y sigue en estado Pendiente |
| **Intereses de mora** | Cuando una tarjeta de crédito tiene intereses de mora activos (valor > $0) |
| **Cupo agotado** | Cuando el cupo disponible de una tarjeta es $0 |

### 11.1 Pantalla de Notificaciones

Al ingresar a la opción **"Notificaciones"** del menú lateral verá:

1. **Botón "Enviar correo de prueba":** envía un correo de prueba a la dirección registrada en su cuenta para verificar que las notificaciones están funcionando.

2. **Historial de envíos:** tabla con todos los correos enviados:

| Columna | Descripción |
|---|---|
| Fecha | Cuándo se envió el correo |
| Tipo | Qué tipo de alerta fue |
| Referencia | A qué compromiso o tarjeta hace referencia |
| Asunto | Asunto del correo enviado |
| Estado | `Enviado` (verde) o `Error` (rojo) |

### 11.2 ¿Qué hacer si el estado aparece en Error?

Si el correo de prueba aparece en estado **Error**, puede deberse a:
- El servidor de correo no está configurado correctamente (contacte a su administrador).
- Problemas temporales de red.

Si las notificaciones automáticas llegan con `Error`, verifique que el correo registrado en su cuenta sea correcto y esté activo.

---

## 12. Glosario Financiero

| Término | Definición |
|---|---|
| **Flujo neto** | Resultado de restar a sus ingresos todos los compromisos, gastos y cuotas. Si es positivo, le sobra dinero; si es negativo, está gastando más de lo que gana. |
| **Tasa MV (Mensual Vencida)** | Tasa de interés expresada por mes. Es el estándar colombiano. Una MV del 2% significa que por cada $100.000 de deuda, el banco cobra $2.000 de interés al mes. |
| **Tasa EA (Efectiva Anual)** | Equivalente anual de la tasa MV. Se calcula como `(1 + MV/100)^12 - 1`. Con MV del 2%, la EA es aproximadamente 26.8%. |
| **Cupo disponible** | Diferencia entre el cupo total autorizado de una tarjeta y el saldo usado. Si su cupo es $5.000.000 y usó $2.000.000, el cupo disponible es $3.000.000. |
| **% Utilización** | Porcentaje del cupo de una tarjeta que ya está ocupado. Más del 80% es señal de riesgo. |
| **Pago mínimo** | El monto más bajo que puede pagar en su tarjeta para no entrar en mora. Pagar solo el mínimo extiende la deuda hasta por 36 meses adicionales. |
| **Pago Mínimo Alterno** | Opción de pago que ofrece el banco: mayor que el mínimo, menor que el total. Reduce la deuda más rápido que el mínimo. |
| **Intereses de mora** | Interés adicional que cobra el banco cuando no paga a tiempo. Siempre es más alto que la tasa normal. |
| **Estrategia Avalanche** | Táctica de pago de deudas: primero liquida el crédito con la tasa más alta. Minimiza el interés total pagado. |
| **Estrategia Snowball** | Táctica de pago de deudas: primero liquida el crédito con el saldo más pequeño. Genera motivación al ver deudas eliminadas. |
| **Compromiso fijo** | Pago recurrente con monto y fecha relativamente estables (arriendo, servicios, seguros). |
| **Gasto variable** | Gasto sin monto ni fecha fija, que varía según hábitos de consumo (mercado, gasolina, restaurantes). |
| **Soft delete** | Técnica que FLUXA usa internamente: cuando elimina un compromiso o crédito, este se "desactiva" en lugar de borrarse permanentemente, preservando el historial. |
| **COP** | Peso colombiano. Moneda oficial de Colombia. Todos los valores en FLUXA están en COP. |

---

## 13. Preguntas Frecuentes

**¿Puedo usar FLUXA desde el celular?**
Sí, la interfaz es responsiva y funciona desde el navegador del celular. No existe aún una aplicación móvil nativa.

---

**¿Qué pasa si olvido registrar un pago de un compromiso y ya venció?**
El sistema cambia automáticamente el estado a `OVERDUE` cuando pasa la fecha de vencimiento sin registrar pago. Puede igualmente registrar el pago después: haga clic en el ícono de pago del registro vencido, ingrese la fecha real en que pagó y guarde. El historial quedará registrado correctamente.

---

**¿Puedo registrar gastos de meses anteriores?**
Sí. Desde la pantalla de Gastos cambie el selector de mes y año al período deseado y use el botón **"Nuevo Gasto"**. Elija la fecha exacta del gasto en el formulario.

---

**¿Puedo registrar el mismo gasto dos veces si lo importé por archivo?**
Sí, FLUXA no detecta duplicados automáticamente. Si importa un archivo que ya importó antes, se crearán registros duplicados. Revise su historial antes de importar por segunda vez.

---

**¿Cómo cambio mi contraseña?**
En la versión actual, el cambio de contraseña debe ser solicitado al administrador del sistema.

---

**¿Qué diferencia hay entre "Pago mínimo" y "Pago Mínimo Alterno" en tarjeta?**
El **pago mínimo** es el valor más bajo que acepta el banco sin generar mora. El **Pago Mínimo Alterno** es una opción intermedia que algunos bancos ofrecen: es mayor que el mínimo pero menor que el pago total. Pagar el alterno reduce la deuda más rápido que el mínimo, pero menos que pagar el saldo total.

---

**¿Por qué el Dashboard muestra $0 en todos los campos?**
Probablemente acaba de crear su cuenta y aún no tiene información registrada. Comience por:
1. Agregar sus fuentes de ingreso (módulo **Ingresos**).
2. Registrar sus compromisos fijos (módulo **Compromisos**).
3. Anotar sus gastos del mes actual (módulo **Gastos**).
Después de ingresar datos, el Dashboard se actualizará automáticamente.

---

**¿Las proyecciones del Dashboard son exactas?**
Son estimaciones basadas en datos históricos. La proyección de ingresos usa sus fuentes de ingreso fijo activas. La proyección de gastos variables usa el promedio de los últimos 3 meses con datos. Si el mes anterior fue atípicamente alto o bajo, la proyección puede no ser representativa de meses futuros.

---

**¿Puedo eliminar un crédito que ya liquidé?**
Sí. Use el ícono de papelera en la tarjeta del crédito. El sistema lo marca como inactivo (no se borra definitivamente), por lo que el historial de pagos se conserva. El crédito eliminado deja de aparecer en el listado y deja de sumarse a sus obligaciones mensuales.

---

*FLUXA — Manual de Usuario v1.0 | Abril 2026*
*Para soporte técnico, contacte al administrador del sistema.*
