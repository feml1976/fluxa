import React, { useState } from 'react';
import {
  Box, Card, CardContent, Chip, CircularProgress,
  Grid, LinearProgress, MenuItem, Paper, Select,
  Stack, Tab, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Tabs, Typography,
} from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import {
  Bar, BarChart, CartesianGrid, Cell, Legend,
  Line, Pie, PieChart, ResponsiveContainer,
  Tooltip, XAxis, YAxis,
} from 'recharts';
import {
  useDashboardProjections,
  useDashboardSummary,
  useDebtStrategies,
} from '../hooks/useDashboard';
import type { HealthStatus } from '../types/dashboard.types';
import { formatCOP } from '../../../shared/utils/currencyFormatter';

const HEALTH_COLORS: Record<HealthStatus, string> = {
  GREEN:  '#2e7d32',
  YELLOW: '#ed6c02',
  RED:    '#d32f2f',
};
const HEALTH_LABELS: Record<HealthStatus, string> = {
  GREEN:  'Saludable',
  YELLOW: 'Precaución',
  RED:    'Riesgo',
};
const PIE_COLORS = ['#1976d2', '#388e3c', '#f57c00', '#7b1fa2', '#c62828'];

export const DashboardPage: React.FC = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear]   = useState(now.getFullYear());
  const [projMonths, setProjMonths] = useState(6);
  const [strategyTab, setStrategyTab] = useState(0);

  const { data, isLoading, isError }  = useDashboardSummary(month, year);
  const { data: projections }        = useDashboardProjections(projMonths);
  const { data: strategies }         = useDebtStrategies();

  const months = Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: new Date(2000, i, 1).toLocaleString('es-CO', { month: 'long' }),
  }));

  if (isLoading) {
    return <Box display="flex" justifyContent="center" mt={6}><CircularProgress /></Box>;
  }

  if (isError || !data) {
    return (
      <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center" mt={8} gap={2}>
        <WarningAmberIcon sx={{ fontSize: 48, color: 'warning.main' }} />
        <Typography variant="h6" color="text.secondary">
          No se pudo cargar el Dashboard
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Verifica que el backend esté corriendo y vuelve a intentarlo.
        </Typography>
      </Box>
    );
  }

  const d = data;
  const healthColor  = HEALTH_COLORS[d.healthStatus];
  const netFlowPos   = d.netFlow >= 0;
  const pieData      = d.topExpenses.map(e => ({ name: e.categoryName, value: e.total }));
  const currentStrategy = strategies?.[strategyTab];

  return (
    <Box p={3}>
      {/* Cabecera con selector de período */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Dashboard Financiero</Typography>
        <Stack direction="row" spacing={2}>
          <Select size="small" value={month} onChange={e => setMonth(Number(e.target.value))}>
            {months.map(m => <MenuItem key={m.value} value={m.value}>{m.label}</MenuItem>)}
          </Select>
          <Select size="small" value={year} onChange={e => setYear(Number(e.target.value))}>
            {[2024, 2025, 2026, 2027].map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
          </Select>
        </Stack>
      </Stack>

      {/* Indicador de salud financiera */}
      <Paper sx={{ p: 2, mb: 3, borderLeft: `6px solid ${healthColor}` }}>
        <Stack direction="row" alignItems="center" spacing={2}>
          {d.healthStatus === 'GREEN'
            ? <CheckCircleOutlineIcon sx={{ color: healthColor, fontSize: 36 }} />
            : <WarningAmberIcon sx={{ color: healthColor, fontSize: 36 }} />}
          <Box>
            <Typography variant="h6" fontWeight={700} color={healthColor}>
              Salud Financiera: {HEALTH_LABELS[d.healthStatus]}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              % comprometido (compromisos + gastos + créditos): <strong>{d.commitmentRatio}%</strong> del ingreso esperado
            </Typography>
          </Box>
        </Stack>
        <LinearProgress
          variant="determinate"
          value={Math.min(Number(d.commitmentRatio), 100)}
          sx={{ mt: 2, height: 8, borderRadius: 4,
            '& .MuiLinearProgress-bar': { backgroundColor: healthColor } }}
        />
      </Paper>

      {/* Tarjetas KPI */}
      <Grid container spacing={2} mb={3}>
        {[
          { label: 'Ingresos Recibidos', value: d.totalIncome, sub: `Esperado: ${formatCOP(d.totalIncomeExpected)}`, color: 'success.main', icon: <TrendingUpIcon color="success" sx={{ fontSize: 40, opacity: 0.3 }} /> },
          { label: 'Compromisos Fijos',  value: d.totalCommitments, sub: `${d.pendingCommitmentsCount} pend. / ${d.overdueCommitmentsCount} venc.`, color: 'warning.main', icon: <TrendingDownIcon color="warning" sx={{ fontSize: 40, opacity: 0.3 }} /> },
          { label: 'Gastos Variables',   value: d.totalExpenses, sub: `Presupuestado: ${formatCOP(d.totalExpensesPlanned)}`, color: 'error.main', icon: <TrendingDownIcon color="error" sx={{ fontSize: 40, opacity: 0.3 }} /> },
        ].map(kpi => (
          <Grid item xs={12} sm={6} md={3} key={kpi.label}>
            <Card>
              <CardContent>
                <Stack direction="row" justifyContent="space-between">
                  <Box>
                    <Typography variant="body2" color="text.secondary">{kpi.label}</Typography>
                    <Typography variant="h6" fontWeight={700} color={kpi.color}>{formatCOP(kpi.value)}</Typography>
                    <Typography variant="caption" color="text.secondary">{kpi.sub}</Typography>
                  </Box>
                  {kpi.icon}
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ borderLeft: `4px solid ${netFlowPos ? '#2e7d32' : '#d32f2f'}` }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary">Flujo Neto</Typography>
              <Typography variant="h6" fontWeight={700} color={netFlowPos ? 'success.main' : 'error.main'}>
                {formatCOP(d.netFlow)}
              </Typography>
              <Typography variant="caption" color="text.secondary">Ingresos − Todo comprometido</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Fila: Pie gasto + Vencimientos */}
      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={600} mb={2}>Top 5 Categorías de Gasto</Typography>
              {pieData.length > 0 ? (
                <ResponsiveContainer width="100%" height={240}>
                  <PieChart>
                    <Pie data={pieData} cx="50%" cy="50%" outerRadius={85} dataKey="value" nameKey="name"
                      label={({ percent }) => `${(percent * 100).toFixed(1)}%`}>
                      {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                    </Pie>
                    <Tooltip formatter={(v: number) => formatCOP(v)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <Typography variant="body2" color="text.secondary" align="center" mt={4}>Sin gastos en este período</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={600} mb={2}>Próximos Vencimientos (7 días)</Typography>
              {d.upcomingPayments.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Compromiso</TableCell>
                        <TableCell align="right">Monto</TableCell>
                        <TableCell align="center">Vence en</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {d.upcomingPayments.map(p => (
                        <TableRow key={p.commitmentId}>
                          <TableCell>{p.name}</TableCell>
                          <TableCell align="right">{formatCOP(p.estimatedAmount)}</TableCell>
                          <TableCell align="center">
                            <Chip
                              label={p.daysUntilDue === 0 ? 'Hoy' : `${p.daysUntilDue}d`}
                              size="small"
                              color={p.daysUntilDue <= 1 ? 'error' : p.daysUntilDue <= 3 ? 'warning' : 'default'}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography variant="body2" color="text.secondary" align="center" mt={4}>
                  Sin vencimientos en los próximos 7 días
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Proyecciones */}
      {projections && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="subtitle1" fontWeight={600}>
                Proyección de Flujo Neto
              </Typography>
              <Select size="small" value={projMonths} onChange={e => setProjMonths(Number(e.target.value))}>
                <MenuItem value={3}>3 meses</MenuItem>
                <MenuItem value={6}>6 meses</MenuItem>
                <MenuItem value={12}>12 meses</MenuItem>
              </Select>
            </Stack>

            <Grid container spacing={2} mb={2}>
              {[
                { label: 'Ingreso prom. proyectado', value: projections.avgProjectedIncome, color: 'success.main' },
                { label: 'Egreso prom. proyectado',  value: projections.avgProjectedExpenses, color: 'error.main' },
                { label: 'Flujo neto prom.',          value: projections.avgProjectedNetFlow,
                  color: projections.avgProjectedNetFlow >= 0 ? 'success.main' : 'error.main' },
              ].map(item => (
                <Grid item xs={4} key={item.label}>
                  <Box textAlign="center">
                    <Typography variant="caption" color="text.secondary">{item.label}</Typography>
                    <Typography variant="body1" fontWeight={700} color={item.color}>
                      {formatCOP(item.value)}
                    </Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>

            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={projections.projections} margin={{ top: 5, right: 20, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                <YAxis tickFormatter={v => `$${(v / 1000000).toFixed(1)}M`} tick={{ fontSize: 11 }} />
                <Tooltip formatter={(v: number) => formatCOP(v)} />
                <Legend />
                <Bar dataKey="projectedIncome" name="Ingresos" fill="#2e7d32" />
                <Bar dataKey="projectedCommitments" name="Compromisos" fill="#ed6c02" stackId="out" />
                <Bar dataKey="projectedVariableExpenses" name="Gastos var." fill="#d32f2f" stackId="out" />
                <Bar dataKey="projectedCreditObligations" name="Créditos" fill="#7b1fa2" stackId="out" />
                <Line type="monotone" dataKey="projectedNetFlow" name="Flujo neto"
                  stroke="#1976d2" strokeWidth={2} dot={false} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {/* Estrategias de deuda */}
      {strategies && strategies.length > 0 && (
        <Card>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={600} mb={1}>
              Estrategias de Pago de Deuda
            </Typography>
            <Tabs value={strategyTab} onChange={(_, v) => setStrategyTab(v)} sx={{ mb: 2 }}>
              {strategies.map((s, i) => <Tab key={i} label={s.strategyName} />)}
            </Tabs>

            {currentStrategy && (
              <>
                <Typography variant="body2" color="text.secondary" mb={2}>
                  {currentStrategy.description}
                </Typography>

                <Grid container spacing={2} mb={2}>
                  {[
                    { label: 'Deuda total', value: formatCOP(currentStrategy.totalDebt) },
                    { label: 'Interés total a pagar', value: formatCOP(currentStrategy.totalInterestToPay), color: 'error.main' },
                    { label: 'Meses para liberarte', value: `${currentStrategy.estimatedMonthsToFreedom} meses` },
                    { label: 'Ahorro vs solo mínimos', value: formatCOP(currentStrategy.interestSavedVsMinimum), color: 'success.main' },
                  ].map(item => (
                    <Grid item xs={6} sm={3} key={item.label}>
                      <Paper variant="outlined" sx={{ p: 1.5, textAlign: 'center' }}>
                        <Typography variant="caption" color="text.secondary" display="block">{item.label}</Typography>
                        <Typography variant="body2" fontWeight={700} color={item.color ?? 'inherit'}>{item.value}</Typography>
                      </Paper>
                    </Grid>
                  ))}
                </Grid>

                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>#</TableCell>
                        <TableCell>Crédito</TableCell>
                        <TableCell>Tipo</TableCell>
                        <TableCell align="right">Saldo</TableCell>
                        <TableCell align="right">Tasa MV</TableCell>
                        <TableCell align="right">Cuota/Mín.</TableCell>
                        <TableCell align="right">Meses</TableCell>
                        <TableCell align="right">Interés total</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {currentStrategy.items.map(item => (
                        <TableRow key={item.creditId}>
                          <TableCell>
                            <Chip label={item.priority} size="small" color="primary" />
                          </TableCell>
                          <TableCell>{item.creditName}</TableCell>
                          <TableCell>
                            <Chip label={item.creditType.replace('_', ' ')} size="small" variant="outlined" />
                          </TableCell>
                          <TableCell align="right">{formatCOP(item.currentBalance)}</TableCell>
                          <TableCell align="right">{item.interestRateMv}%</TableCell>
                          <TableCell align="right">{formatCOP(item.monthlyPayment)}</TableCell>
                          <TableCell align="right">
                            {item.estimatedMonths < 0 ? '∞' : item.estimatedMonths}
                          </TableCell>
                          <TableCell align="right" sx={{ color: 'error.main' }}>
                            {formatCOP(item.totalInterestToPay)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </>
            )}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
