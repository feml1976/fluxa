import React, { useState } from 'react';
import {
  Box, Card, CardContent, Chip, CircularProgress,
  Grid, LinearProgress, MenuItem, Paper, Select,
  Stack, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography,
} from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { useDashboardSummary } from '../hooks/useDashboard';
import type { HealthStatus } from '../types/dashboard.types';
import { formatCOP } from '../../../shared/utils/currencyFormatter';

const HEALTH_COLORS: Record<HealthStatus, string> = {
  GREEN: '#2e7d32',
  YELLOW: '#ed6c02',
  RED: '#d32f2f',
};

const HEALTH_LABELS: Record<HealthStatus, string> = {
  GREEN: 'Saludable',
  YELLOW: 'Precaución',
  RED: 'Riesgo',
};

const PIE_COLORS = ['#1976d2', '#388e3c', '#f57c00', '#7b1fa2', '#c62828'];

export const DashboardPage: React.FC = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());

  const { data, isLoading } = useDashboardSummary(month, year);

  const months = Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: new Date(2000, i, 1).toLocaleString('es-CO', { month: 'long' }),
  }));

  if (isLoading) {
    return <Box display="flex" justifyContent="center" mt={6}><CircularProgress /></Box>;
  }

  const d = data!;
  const healthColor = HEALTH_COLORS[d.healthStatus];
  const netFlowPositive = d.netFlow >= 0;

  const pieData = d.topExpenses.map(e => ({ name: e.categoryName, value: e.total }));

  return (
    <Box p={3}>
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
              % comprometido: {d.commitmentRatio}% del ingreso esperado
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

      {/* Tarjetas principales */}
      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Stack direction="row" justifyContent="space-between">
                <Box>
                  <Typography variant="body2" color="text.secondary">Ingresos Recibidos</Typography>
                  <Typography variant="h6" fontWeight={700} color="success.main">
                    {formatCOP(d.totalIncome)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Esperado: {formatCOP(d.totalIncomeExpected)}
                  </Typography>
                </Box>
                <TrendingUpIcon color="success" sx={{ fontSize: 40, opacity: 0.3 }} />
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Stack direction="row" justifyContent="space-between">
                <Box>
                  <Typography variant="body2" color="text.secondary">Compromisos Fijos</Typography>
                  <Typography variant="h6" fontWeight={700} color="warning.main">
                    {formatCOP(d.totalCommitments)}
                  </Typography>
                  <Stack direction="row" spacing={1} mt={0.5}>
                    <Chip label={`${d.pendingCommitmentsCount} pend.`} size="small" color="warning" />
                    {d.overdueCommitmentsCount > 0 && (
                      <Chip label={`${d.overdueCommitmentsCount} venc.`} size="small" color="error" />
                    )}
                  </Stack>
                </Box>
                <TrendingDownIcon color="warning" sx={{ fontSize: 40, opacity: 0.3 }} />
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Stack direction="row" justifyContent="space-between">
                <Box>
                  <Typography variant="body2" color="text.secondary">Gastos Variables</Typography>
                  <Typography variant="h6" fontWeight={700} color="error.main">
                    {formatCOP(d.totalExpenses)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Presupuestado: {formatCOP(d.totalExpensesPlanned)}
                  </Typography>
                </Box>
                <TrendingDownIcon color="error" sx={{ fontSize: 40, opacity: 0.3 }} />
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ borderLeft: `4px solid ${netFlowPositive ? '#2e7d32' : '#d32f2f'}` }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary">Flujo Neto</Typography>
              <Typography variant="h6" fontWeight={700}
                color={netFlowPositive ? 'success.main' : 'error.main'}>
                {formatCOP(d.netFlow)}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Ingresos − Compromisos − Gastos
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Gráfico top gastos */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={600} mb={2}>
                Top 5 Categorías de Gasto
              </Typography>
              {pieData.length > 0 ? (
                <ResponsiveContainer width="100%" height={260}>
                  <PieChart>
                    <Pie data={pieData} cx="50%" cy="50%" outerRadius={90}
                      dataKey="value" nameKey="name" label={({ percent }) => `${(percent * 100).toFixed(1)}%`}>
                      {pieData.map((_, i) => (
                        <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(v: number) => formatCOP(v)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <Typography variant="body2" color="text.secondary" align="center" mt={4}>
                  Sin gastos en este período
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Próximos vencimientos */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={600} mb={2}>
                Próximos Vencimientos (7 días)
              </Typography>
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
    </Box>
  );
};
