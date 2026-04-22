import React, { useState } from 'react';
import {
  Box, Button, Card, CardContent, Chip, CircularProgress,
  Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, Grid, IconButton, InputLabel, LinearProgress,
  MenuItem, Paper, Select, Stack, Tab, Table,
  TableBody, TableCell, TableContainer, TableHead, TableRow,
  Tabs, TextField, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  useCreateExpense, useDeleteBudget, useDeleteExpense,
  useMonthlyExpense, useSaveOrUpdateBudget, useUpdateExpense,
} from '../hooks/useExpense';
import type { VariableExpenseResponse } from '../types/expense.types';
import { formatCOP } from '../../../shared/utils/currencyFormatter';

const expenseSchema = z.object({
  categoryId: z.number({ required_error: 'Requerido' }),
  amount: z.number({ required_error: 'Requerido' }).positive('Debe ser mayor a 0'),
  expenseDate: z.string().min(1, 'Requerido'),
  description: z.string().optional(),
});
type ExpenseForm = z.infer<typeof expenseSchema>;

const CATEGORY_SEEDS = [
  { id: 6, name: 'Educación' },
  { id: 7, name: 'Entretenimiento' },
  { id: 8, name: 'Ropa y Accesorios' },
  { id: 9, name: 'Salud y Bienestar' },
  { id: 10, name: 'Tecnología' },
];

export const ExpensePage: React.FC = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());
  const [tab, setTab] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<VariableExpenseResponse | null>(null);

  const { data: summary, isLoading } = useMonthlyExpense(month, year);
  const createExpense = useCreateExpense();
  const updateExpense = useUpdateExpense();
  const deleteExpense = useDeleteExpense();
  const deleteBudget = useDeleteBudget();
  useSaveOrUpdateBudget();

  const { control, handleSubmit, reset } = useForm<ExpenseForm>({
    resolver: zodResolver(expenseSchema),
    defaultValues: { expenseDate: now.toISOString().slice(0, 10) },
  });

  const openCreate = () => {
    setEditing(null);
    reset({ expenseDate: now.toISOString().slice(0, 10) });
    setDialogOpen(true);
  };

  const openEdit = (e: VariableExpenseResponse) => {
    setEditing(e);
    reset({
      categoryId: e.categoryId,
      amount: e.amount,
      expenseDate: e.expenseDate,
      description: e.description ?? '',
    });
    setDialogOpen(true);
  };

  const onSubmit = (form: ExpenseForm) => {
    const payload = {
      categoryId: form.categoryId,
      amount: form.amount,
      expenseDate: form.expenseDate,
      description: form.description,
    };
    if (editing) {
      updateExpense.mutate({ id: editing.id, data: payload }, { onSuccess: () => setDialogOpen(false) });
    } else {
      createExpense.mutate(payload, { onSuccess: () => setDialogOpen(false) });
    }
  };

  const months = Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: new Date(2000, i, 1).toLocaleString('es-CO', { month: 'long' }),
  }));

  const years = [2024, 2025, 2026, 2027];
  const spent = summary?.totalSpent ?? 0;
  const planned = summary?.totalPlanned ?? 0;
  const pct = planned > 0 ? Math.min((spent / planned) * 100, 100) : 0;

  return (
    <Box p={3}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Gastos Variables</Typography>
        <Stack direction="row" spacing={2}>
          <Select size="small" value={month} onChange={e => setMonth(Number(e.target.value))}>
            {months.map(m => <MenuItem key={m.value} value={m.value}>{m.label}</MenuItem>)}
          </Select>
          <Select size="small" value={year} onChange={e => setYear(Number(e.target.value))}>
            {years.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
          </Select>
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
            Nuevo Gasto
          </Button>
        </Stack>
      </Stack>

      {/* Tarjetas resumen */}
      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary">Total Gastado</Typography>
              <Typography variant="h5" fontWeight={700} color="error.main">
                {formatCOP(spent)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary">Presupuestado</Typography>
              <Typography variant="h5" fontWeight={700} color="primary.main">
                {formatCOP(planned)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="body2" color="text.secondary">% Ejecutado</Typography>
              <Typography variant="h5" fontWeight={700}
                color={pct > 100 ? 'error.main' : pct > 80 ? 'warning.main' : 'success.main'}>
                {pct.toFixed(1)}%
              </Typography>
              <LinearProgress
                variant="determinate"
                value={Math.min(pct, 100)}
                color={pct > 100 ? 'error' : pct > 80 ? 'warning' : 'success'}
                sx={{ mt: 1 }}
              />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Gastos" />
        <Tab label="Presupuestos" />
      </Tabs>

      {isLoading ? (
        <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>
      ) : tab === 0 ? (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Fecha</TableCell>
                <TableCell>Categoría</TableCell>
                <TableCell>Descripción</TableCell>
                <TableCell align="right">Monto</TableCell>
                <TableCell align="center">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(summary?.expenses ?? []).map(e => (
                <TableRow key={e.id}>
                  <TableCell>{e.expenseDate}</TableCell>
                  <TableCell><Chip label={e.categoryName} size="small" /></TableCell>
                  <TableCell>{e.description ?? '—'}</TableCell>
                  <TableCell align="right">{formatCOP(e.amount)}</TableCell>
                  <TableCell align="center">
                    <IconButton size="small" onClick={() => openEdit(e)}><EditIcon fontSize="small" /></IconButton>
                    <IconButton size="small" color="error"
                      onClick={() => deleteExpense.mutate(e.id)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {(summary?.expenses ?? []).length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">Sin gastos en este período</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      ) : (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Categoría</TableCell>
                <TableCell align="right">Presupuestado</TableCell>
                <TableCell align="right">Gastado</TableCell>
                <TableCell align="right">Sugerido</TableCell>
                <TableCell align="right">% Ejec.</TableCell>
                <TableCell align="center">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(summary?.budgets ?? []).map(b => {
                const bPct = b.plannedAmount > 0 ? (b.spentAmount / b.plannedAmount) * 100 : 0;
                return (
                  <TableRow key={b.id}>
                    <TableCell>{b.categoryName}</TableCell>
                    <TableCell align="right">{formatCOP(b.plannedAmount)}</TableCell>
                    <TableCell align="right">{formatCOP(b.spentAmount)}</TableCell>
                    <TableCell align="right">
                      {b.suggestedAmount != null ? formatCOP(b.suggestedAmount) : '—'}
                    </TableCell>
                    <TableCell align="right"
                      sx={{ color: bPct > 100 ? 'error.main' : bPct > 80 ? 'warning.main' : 'success.main' }}>
                      {bPct.toFixed(1)}%
                    </TableCell>
                    <TableCell align="center">
                      <IconButton size="small" color="error"
                        onClick={() => deleteBudget.mutate(b.id)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                );
              })}
              {(summary?.budgets ?? []).length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center">Sin presupuestos en este período</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Dialog nuevo/editar gasto */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="xs" fullWidth>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>{editing ? 'Editar Gasto' : 'Nuevo Gasto'}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} mt={1}>
              <Controller name="categoryId" control={control} render={({ field, fieldState }) => (
                <FormControl fullWidth error={!!fieldState.error}>
                  <InputLabel>Categoría</InputLabel>
                  <Select {...field} label="Categoría" value={field.value ?? ''} onChange={e => field.onChange(Number(e.target.value))}>
                    {CATEGORY_SEEDS.map(c => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
                  </Select>
                </FormControl>
              )} />
              <Controller name="amount" control={control} render={({ field, fieldState }) => (
                <TextField {...field} label="Monto" type="number" fullWidth
                  error={!!fieldState.error} helperText={fieldState.error?.message}
                  onChange={e => field.onChange(Number(e.target.value))} />
              )} />
              <Controller name="expenseDate" control={control} render={({ field, fieldState }) => (
                <TextField {...field} label="Fecha" type="date" fullWidth
                  InputLabelProps={{ shrink: true }}
                  error={!!fieldState.error} helperText={fieldState.error?.message} />
              )} />
              <Controller name="description" control={control} render={({ field }) => (
                <TextField {...field} label="Descripción" fullWidth multiline rows={2} />
              )} />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancelar</Button>
            <Button type="submit" variant="contained"
              disabled={createExpense.isPending || updateExpense.isPending}>
              Guardar
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
