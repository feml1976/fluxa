import { useState } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, IconButton,
  CircularProgress, Alert, Tabs, Tab, Select,
  MenuItem, FormControl, InputLabel,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import { useMonthlyIncome, useIncomeSources } from '../hooks/useIncome';
import IncomeStatusBadge from '../components/IncomeStatusBadge';
import { formatCOP } from '@/shared/utils/currencyFormatter';

const MONTHS = [
  'Enero','Febrero','Marzo','Abril','Mayo','Junio',
  'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre',
];

const IncomePage: React.FC = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());
  const [tab, setTab] = useState(0);

  const { data: monthly, isLoading: loadingMonthly } = useMonthlyIncome(month, year);
  const { data: sources, isLoading: loadingSources } = useIncomeSources();

  const years = Array.from({ length: 3 }, (_, i) => now.getFullYear() - 1 + i);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Ingresos</Typography>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Resumen mensual" />
        <Tab label="Fuentes de ingreso" />
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
              <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
                <Paper sx={{ p: 2, flex: 1 }}>
                  <Typography variant="body2" color="text.secondary">Ingreso Esperado</Typography>
                  <Typography variant="h6" fontWeight="bold">{formatCOP(monthly.totalExpected)}</Typography>
                </Paper>
                <Paper sx={{ p: 2, flex: 1 }}>
                  <Typography variant="body2" color="text.secondary">Ingreso Recibido</Typography>
                  <Typography variant="h6" fontWeight="bold" color="success.main">
                    {formatCOP(monthly.totalReceived)}
                  </Typography>
                </Paper>
              </Box>

              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fuente</TableCell>
                      <TableCell align="right">Monto</TableCell>
                      <TableCell>Estado</TableCell>
                      <TableCell>Fecha recibido</TableCell>
                      <TableCell align="center">Acción</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {monthly.records.map(r => (
                      <TableRow key={r.id}>
                        <TableCell>{r.sourceName}</TableCell>
                        <TableCell align="right">{formatCOP(r.amount)}</TableCell>
                        <TableCell><IncomeStatusBadge status={r.status} /></TableCell>
                        <TableCell>{r.receivedDate ?? '—'}</TableCell>
                        <TableCell align="center">
                          <IconButton size="small"><EditIcon fontSize="small" /></IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                    {monthly.records.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} align="center">
                          <Alert severity="info" sx={{ m: 1 }}>
                            No hay registros para este período
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
        loadingSources ? <CircularProgress /> : (
          <TableContainer component={Paper}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Tipo</TableCell>
                  <TableCell>Frecuencia</TableCell>
                  <TableCell align="right">Monto esperado</TableCell>
                  <TableCell>Estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {sources?.map(s => (
                  <TableRow key={s.id}>
                    <TableCell>{s.name}</TableCell>
                    <TableCell>{s.type === 'FIXED' ? 'Fijo' : 'Variable'}</TableCell>
                    <TableCell>{s.frequency}</TableCell>
                    <TableCell align="right">{formatCOP(s.expectedAmount)}</TableCell>
                    <TableCell>
                      <Typography variant="body2" color={s.isActive ? 'success.main' : 'text.secondary'}>
                        {s.isActive ? 'Activo' : 'Inactivo'}
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

export default IncomePage;
