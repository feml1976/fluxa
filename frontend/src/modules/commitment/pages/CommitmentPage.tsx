import { useState } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, IconButton,
  CircularProgress, Alert, Tabs, Tab, Select,
  MenuItem, FormControl, InputLabel, Grid,
} from '@mui/material';
import PaymentIcon from '@mui/icons-material/Payment';
import { useMonthlyCommitments, useCommitments } from '../hooks/useCommitment';
import CommitmentStatusBadge from '../components/CommitmentStatusBadge';
import { formatCOP } from '@/shared/utils/currencyFormatter';

const MONTHS = [
  'Enero','Febrero','Marzo','Abril','Mayo','Junio',
  'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre',
];

const CommitmentPage: React.FC = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());
  const [tab, setTab] = useState(0);

  const { data: monthly, isLoading: loadingMonthly } = useMonthlyCommitments(month, year);
  const { data: commitments, isLoading: loadingList } = useCommitments();

  const years = Array.from({ length: 3 }, (_, i) => now.getFullYear() - 1 + i);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Compromisos Fijos</Typography>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Resumen mensual" />
        <Tab label="Mis compromisos" />
      </Tabs>

      {tab === 0 && (
        <>
          <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel>Mes</InputLabel>
              <Select value={month} label="Mes" onChange={(e) => setMonth(Number(e.target.value))}>
                {MONTHS.map((m, i) => <MenuItem key={i} value={i + 1}>{m}</MenuItem>)}
              </Select>
            </FormControl>
            <FormControl size="small" sx={{ minWidth: 100 }}>
              <InputLabel>Año</InputLabel>
              <Select value={year} label="Año" onChange={(e) => setYear(Number(e.target.value))}>
                {years.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
              </Select>
            </FormControl>
          </Box>

          {loadingMonthly ? <CircularProgress /> : monthly && (
            <>
              <Grid container spacing={2} sx={{ mb: 3 }}>
                {[
                  { label: 'Total estimado', value: formatCOP(monthly.totalEstimated), color: 'text.primary' },
                  { label: 'Total pagado',   value: formatCOP(monthly.totalPaid),      color: 'success.main' },
                  { label: 'Pendientes',     value: String(monthly.pendingCount),       color: 'warning.main' },
                  { label: 'Vencidos',       value: String(monthly.overdueCount),       color: 'error.main'   },
                ].map(({ label, value, color }) => (
                  <Grid item xs={6} sm={3} key={label}>
                    <Paper sx={{ p: 2 }}>
                      <Typography variant="body2" color="text.secondary">{label}</Typography>
                      <Typography variant="h6" fontWeight="bold" color={color}>{value}</Typography>
                    </Paper>
                  </Grid>
                ))}
              </Grid>

              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Compromiso</TableCell>
                      <TableCell align="right">Estimado</TableCell>
                      <TableCell align="right">Pagado</TableCell>
                      <TableCell>Vencimiento</TableCell>
                      <TableCell>Estado</TableCell>
                      <TableCell align="center">Pagar</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {monthly.records.map(r => (
                      <TableRow key={r.id}>
                        <TableCell>{r.commitmentName}</TableCell>
                        <TableCell align="right">{formatCOP(r.estimatedAmount)}</TableCell>
                        <TableCell align="right">{r.actualAmount != null ? formatCOP(r.actualAmount) : '—'}</TableCell>
                        <TableCell>{r.dueDate}</TableCell>
                        <TableCell><CommitmentStatusBadge status={r.status} /></TableCell>
                        <TableCell align="center">
                          {r.status !== 'PAID' && (
                            <IconButton size="small" color="primary">
                              <PaymentIcon fontSize="small" />
                            </IconButton>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                    {monthly.records.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6} align="center">
                          <Alert severity="info" sx={{ m: 1 }}>
                            No hay compromisos registrados para este período
                          </Alert>
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </>
          )}
        </>
      )}

      {tab === 1 && (
        loadingList ? <CircularProgress /> : (
          <TableContainer component={Paper}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell align="right">Monto estimado</TableCell>
                  <TableCell>Día de pago</TableCell>
                  <TableCell>Frecuencia</TableCell>
                  <TableCell>Estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {commitments?.map(c => (
                  <TableRow key={c.id}>
                    <TableCell>{c.name}</TableCell>
                    <TableCell align="right">{formatCOP(c.estimatedAmount)}</TableCell>
                    <TableCell>Día {c.dueDay}</TableCell>
                    <TableCell>{c.frequency}</TableCell>
                    <TableCell>
                      <Typography variant="body2" color={c.isActive ? 'success.main' : 'text.secondary'}>
                        {c.isActive ? 'Activo' : 'Inactivo'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )
      )}
    </Box>
  );
};

export default CommitmentPage;
