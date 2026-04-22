import React, { useState } from 'react';
import {
  Box, Button, Card, CardContent, Chip, CircularProgress,
  Collapse, Dialog, DialogActions, DialogContent, DialogTitle,
  Divider, FormControl, Grid, IconButton, InputLabel,
  MenuItem, Paper, Select, Stack, Tab, Tabs,
  TextField, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import PaymentIcon from '@mui/icons-material/Payment';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  useCreateCredit, useCreditList, useCreditSummary,
  useDeleteCredit, useRegisterCreditPayment, useUpdateCredit,
} from '../hooks/useCredit';
import { CreditAlertChip } from '../components/CreditAlertChip';
import { CreditAnalysisPanel } from '../components/CreditAnalysisPanel';
import type { CreditResponse, CreditType } from '../types/credit.types';
import { formatCOP } from '../../../shared/utils/currencyFormatter';

const TYPE_LABELS: Record<CreditType, string> = {
  CREDIT_CARD: 'Tarjeta de Crédito',
  PERSONAL: 'Crédito Personal',
  MORTGAGE: 'Hipotecario',
  VEHICLE: 'Vehículo',
};

const creditSchema = z.object({
  type: z.enum(['CREDIT_CARD', 'PERSONAL', 'MORTGAGE', 'VEHICLE']),
  name: z.string().min(1, 'Requerido').max(200),
  interestRateMv: z.number().min(0, 'Debe ser >= 0'),
  currentBalance: z.number().min(0, 'Debe ser >= 0'),
  monthlyInstallment: z.number().min(0).optional(),
  totalInstallments: z.number().min(1).int().optional(),
  paidInstallments: z.number().min(0).int().optional(),
  openingDate: z.string().min(1, 'Requerido'),
  // Campos tarjeta
  cardNumberLast4: z.string().length(4).optional(),
  brand: z.enum(['VISA', 'MASTERCARD', 'AMEX', 'DINERS', 'OTHER']).optional(),
  creditLimitPurchases: z.number().min(0).optional(),
  creditLimitAdvances: z.number().min(0).optional(),
  availablePurchases: z.number().min(0).optional(),
  availableAdvances: z.number().min(0).optional(),
  previousBalance: z.number().min(0).optional(),
  minimumPayment: z.number().min(0).optional(),
  alternateMinimumPayment: z.number().min(0).optional(),
  lateInterest: z.number().min(0).optional(),
  paymentDueDay: z.number().min(1).max(31).int().optional(),
});
type CreditForm = z.infer<typeof creditSchema>;

const paymentSchema = z.object({
  month: z.number().min(1).max(12),
  year: z.number().min(2000),
  amount: z.number().positive('Debe ser mayor a 0'),
  paymentDate: z.string().min(1, 'Requerido'),
  notes: z.string().optional(),
});
type PaymentForm = z.infer<typeof paymentSchema>;

export const CreditPage: React.FC = () => {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [payDialogOpen, setPayDialogOpen] = useState(false);
  const [editing, setEditing] = useState<CreditResponse | null>(null);
  const [payingCredit, setPayingCredit] = useState<CreditResponse | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [tabFilter, setTabFilter] = useState<CreditType | 'ALL'>('ALL');

  const { data: credits, isLoading } = useCreditList();
  const { data: summary } = useCreditSummary();
  const createCredit = useCreateCredit();
  const updateCredit = useUpdateCredit();
  const deleteCredit = useDeleteCredit();
  const registerPayment = useRegisterCreditPayment();

  const now = new Date();

  const { control, handleSubmit, reset, watch } = useForm<CreditForm>({
    resolver: zodResolver(creditSchema),
    defaultValues: { type: 'CREDIT_CARD', interestRateMv: 0, currentBalance: 0, openingDate: now.toISOString().slice(0, 10) },
  });

  const {
    control: payControl, handleSubmit: handlePaySubmit, reset: resetPay,
  } = useForm<PaymentForm>({
    resolver: zodResolver(paymentSchema),
    defaultValues: { month: now.getMonth() + 1, year: now.getFullYear(), paymentDate: now.toISOString().slice(0, 10) },
  });

  const watchType = watch('type');

  const openCreate = () => {
    setEditing(null);
    reset({ type: 'CREDIT_CARD', interestRateMv: 0, currentBalance: 0, openingDate: now.toISOString().slice(0, 10) });
    setDialogOpen(true);
  };

  const openEdit = (c: CreditResponse) => {
    setEditing(c);
    reset({
      type: c.type,
      name: c.name,
      interestRateMv: c.interestRateMv,
      currentBalance: c.currentBalance,
      monthlyInstallment: c.monthlyInstallment ?? undefined,
      totalInstallments: c.totalInstallments ?? undefined,
      paidInstallments: c.paidInstallments,
      openingDate: c.openingDate,
      ...(c.cardDetail ? {
        cardNumberLast4: c.cardDetail.cardNumberLast4,
        brand: c.cardDetail.brand,
        creditLimitPurchases: c.cardDetail.creditLimitPurchases,
        creditLimitAdvances: c.cardDetail.creditLimitAdvances,
        availablePurchases: c.cardDetail.availablePurchases,
        availableAdvances: c.cardDetail.availableAdvances,
        previousBalance: c.cardDetail.previousBalance,
        minimumPayment: c.cardDetail.minimumPayment,
        alternateMinimumPayment: c.cardDetail.alternateMinimumPayment,
        lateInterest: c.cardDetail.lateInterest,
        paymentDueDay: c.cardDetail.paymentDueDay,
      } : {}),
    });
    setDialogOpen(true);
  };

  const onSubmit = (form: CreditForm) => {
    const payload = {
      type: form.type,
      name: form.name,
      interestRateMv: form.interestRateMv,
      currentBalance: form.currentBalance,
      monthlyInstallment: form.monthlyInstallment,
      totalInstallments: form.totalInstallments,
      paidInstallments: form.paidInstallments,
      openingDate: form.openingDate,
      ...(form.type === 'CREDIT_CARD' ? {
        cardDetail: {
          cardNumberLast4: form.cardNumberLast4!,
          brand: form.brand!,
          creditLimitPurchases: form.creditLimitPurchases!,
          creditLimitAdvances: form.creditLimitAdvances!,
          availablePurchases: form.availablePurchases!,
          availableAdvances: form.availableAdvances!,
          previousBalance: form.previousBalance!,
          minimumPayment: form.minimumPayment!,
          alternateMinimumPayment: form.alternateMinimumPayment ?? 0,
          lateInterest: form.lateInterest ?? 0,
          paymentDueDay: form.paymentDueDay!,
        },
      } : {}),
    };
    if (editing) {
      updateCredit.mutate({ id: editing.id, data: payload }, { onSuccess: () => setDialogOpen(false) });
    } else {
      createCredit.mutate(payload, { onSuccess: () => setDialogOpen(false) });
    }
  };

  const onPaySubmit = (form: PaymentForm) => {
    if (!payingCredit) return;
    registerPayment.mutate(
      { id: payingCredit.id, data: { ...form } },
      { onSuccess: () => { setPayDialogOpen(false); resetPay(); } }
    );
  };

  const filtered = (credits ?? []).filter(c => tabFilter === 'ALL' || c.type === tabFilter);

  return (
    <Box p={3}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Créditos y Deudas</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
          Nuevo Crédito
        </Button>
      </Stack>

      {/* Tarjetas resumen */}
      {summary && (
        <Grid container spacing={2} mb={3}>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography variant="body2" color="text.secondary">Deuda Total</Typography>
                <Typography variant="h6" fontWeight={700} color="error.main">
                  {formatCOP(summary.totalDebt)}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography variant="body2" color="text.secondary">Obligaciones/mes</Typography>
                <Typography variant="h6" fontWeight={700} color="warning.main">
                  {formatCOP(summary.totalMonthlyObligations)}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography variant="body2" color="text.secondary">Tarjetas con mora</Typography>
                <Typography variant="h6" fontWeight={700}
                  color={summary.cardsWithLateInterest > 0 ? 'error.main' : 'success.main'}>
                  {summary.cardsWithLateInterest}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography variant="body2" color="text.secondary">Tarjetas sin cupo</Typography>
                <Typography variant="h6" fontWeight={700}
                  color={summary.cardsAtMaxCapacity > 0 ? 'error.main' : 'success.main'}>
                  {summary.cardsAtMaxCapacity}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Filtro por tipo */}
      <Tabs value={tabFilter} onChange={(_, v) => setTabFilter(v)} sx={{ mb: 2 }}>
        <Tab label="Todos" value="ALL" />
        <Tab label="Tarjetas" value="CREDIT_CARD" />
        <Tab label="Personal" value="PERSONAL" />
        <Tab label="Hipotecario" value="MORTGAGE" />
        <Tab label="Vehículo" value="VEHICLE" />
      </Tabs>

      {isLoading ? (
        <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>
      ) : (
        <Stack spacing={2}>
          {filtered.map(c => (
            <Paper key={c.id} variant="outlined" sx={{ p: 2 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                <Box flex={1}>
                  <Stack direction="row" spacing={1} alignItems="center" mb={0.5}>
                    <Typography variant="subtitle1" fontWeight={700}>{c.name}</Typography>
                    <Chip label={TYPE_LABELS[c.type]} size="small" variant="outlined" />
                    <CreditAlertChip level={c.alertLevel} />
                    {c.status !== 'ACTIVE' && (
                      <Chip label={c.status} size="small" color="default" />
                    )}
                  </Stack>

                  <Grid container spacing={2} mt={0}>
                    <Grid item xs={6} sm={3}>
                      <Typography variant="caption" color="text.secondary">Saldo actual</Typography>
                      <Typography variant="body2" fontWeight={600}>
                        {formatCOP(c.currentBalance)}
                      </Typography>
                    </Grid>
                    <Grid item xs={6} sm={3}>
                      <Typography variant="caption" color="text.secondary">Tasa MV / EA</Typography>
                      <Typography variant="body2" fontWeight={600}>
                        {c.interestRateMv}% / {c.interestRateEa}%
                      </Typography>
                    </Grid>
                    {c.type === 'CREDIT_CARD' && c.cardDetail && (
                      <>
                        <Grid item xs={6} sm={3}>
                          <Typography variant="caption" color="text.secondary">Cupo disponible</Typography>
                          <Typography variant="body2" fontWeight={600}
                            color={c.cardDetail.availablePurchases <= 0 ? 'error.main' : 'inherit'}>
                            {formatCOP(c.cardDetail.availablePurchases)}
                          </Typography>
                        </Grid>
                        <Grid item xs={6} sm={3}>
                          <Typography variant="caption" color="text.secondary">Utilización</Typography>
                          <Typography variant="body2" fontWeight={600}
                            color={c.cardDetail.utilizationPct >= 80 ? 'error.main' : 'inherit'}>
                            {c.cardDetail.utilizationPct.toFixed(1)}%
                          </Typography>
                        </Grid>
                      </>
                    )}
                    {c.type !== 'CREDIT_CARD' && c.monthlyInstallment && (
                      <>
                        <Grid item xs={6} sm={3}>
                          <Typography variant="caption" color="text.secondary">Cuota mensual</Typography>
                          <Typography variant="body2" fontWeight={600}>
                            {formatCOP(c.monthlyInstallment)}
                          </Typography>
                        </Grid>
                        <Grid item xs={6} sm={3}>
                          <Typography variant="caption" color="text.secondary">Cuotas restantes</Typography>
                          <Typography variant="body2" fontWeight={600}>
                            {c.remainingInstallments ?? '—'}
                          </Typography>
                        </Grid>
                      </>
                    )}
                  </Grid>
                </Box>

                <Stack direction="row" spacing={0.5} ml={1}>
                  <IconButton size="small" title="Registrar pago"
                    onClick={() => { setPayingCredit(c); setPayDialogOpen(true); }}>
                    <PaymentIcon fontSize="small" />
                  </IconButton>
                  <IconButton size="small" onClick={() => openEdit(c)}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                  <IconButton size="small" color="error"
                    onClick={() => deleteCredit.mutate(c.id)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                  <IconButton size="small"
                    onClick={() => setExpandedId(expandedId === c.id ? null : c.id)}>
                    {expandedId === c.id ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                  </IconButton>
                </Stack>
              </Stack>

              <Collapse in={expandedId === c.id} unmountOnExit>
                <Divider sx={{ my: 2 }} />
                <CreditAnalysisPanel credit={c} />
              </Collapse>
            </Paper>
          ))}

          {filtered.length === 0 && (
            <Typography color="text.secondary" align="center" mt={4}>
              Sin créditos registrados
            </Typography>
          )}
        </Stack>
      )}

      {/* Dialog crear/editar */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>{editing ? 'Editar Crédito' : 'Nuevo Crédito'}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} mt={1}>
              <Controller name="type" control={control} render={({ field }) => (
                <FormControl fullWidth>
                  <InputLabel>Tipo</InputLabel>
                  <Select {...field} label="Tipo">
                    {Object.entries(TYPE_LABELS).map(([k, v]) => (
                      <MenuItem key={k} value={k}>{v}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )} />
              <Controller name="name" control={control} render={({ field, fieldState }) => (
                <TextField {...field} label="Nombre" fullWidth
                  error={!!fieldState.error} helperText={fieldState.error?.message} />
              )} />
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Controller name="interestRateMv" control={control} render={({ field, fieldState }) => (
                    <TextField {...field} label="Tasa MV (%)" type="number" fullWidth
                      inputProps={{ step: '0.01' }}
                      error={!!fieldState.error} helperText={fieldState.error?.message}
                      onChange={e => field.onChange(Number(e.target.value))} />
                  )} />
                </Grid>
                <Grid item xs={6}>
                  <Controller name="currentBalance" control={control} render={({ field, fieldState }) => (
                    <TextField {...field} label="Saldo actual" type="number" fullWidth
                      error={!!fieldState.error} helperText={fieldState.error?.message}
                      onChange={e => field.onChange(Number(e.target.value))} />
                  )} />
                </Grid>
              </Grid>
              <Controller name="openingDate" control={control} render={({ field, fieldState }) => (
                <TextField {...field} label="Fecha apertura" type="date" fullWidth
                  InputLabelProps={{ shrink: true }}
                  error={!!fieldState.error} helperText={fieldState.error?.message} />
              )} />

              {watchType !== 'CREDIT_CARD' && (
                <Grid container spacing={2}>
                  <Grid item xs={4}>
                    <Controller name="monthlyInstallment" control={control} render={({ field }) => (
                      <TextField {...field} label="Cuota mensual" type="number" fullWidth
                        onChange={e => field.onChange(Number(e.target.value) || undefined)} />
                    )} />
                  </Grid>
                  <Grid item xs={4}>
                    <Controller name="totalInstallments" control={control} render={({ field }) => (
                      <TextField {...field} label="Plazo (cuotas)" type="number" fullWidth
                        onChange={e => field.onChange(Number(e.target.value) || undefined)} />
                    )} />
                  </Grid>
                  <Grid item xs={4}>
                    <Controller name="paidInstallments" control={control} render={({ field }) => (
                      <TextField {...field} label="Cuotas pagadas" type="number" fullWidth
                        onChange={e => field.onChange(Number(e.target.value) || undefined)} />
                    )} />
                  </Grid>
                </Grid>
              )}

              {/* Campos exclusivos tarjeta */}
              {watchType === 'CREDIT_CARD' && (
                <>
                  <Divider><Chip label="Datos de la tarjeta" size="small" /></Divider>
                  <Grid container spacing={2}>
                    <Grid item xs={6}>
                      <Controller name="cardNumberLast4" control={control} render={({ field, fieldState }) => (
                        <TextField {...field} label="Últimos 4 dígitos" fullWidth inputProps={{ maxLength: 4 }}
                          error={!!fieldState.error} helperText={fieldState.error?.message} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="brand" control={control} render={({ field }) => (
                        <FormControl fullWidth>
                          <InputLabel>Franquicia</InputLabel>
                          <Select {...field} label="Franquicia" value={field.value ?? ''}>
                            {['VISA','MASTERCARD','AMEX','DINERS','OTHER'].map(b =>
                              <MenuItem key={b} value={b}>{b}</MenuItem>)}
                          </Select>
                        </FormControl>
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="creditLimitPurchases" control={control} render={({ field }) => (
                        <TextField {...field} label="Cupo compras" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="availablePurchases" control={control} render={({ field }) => (
                        <TextField {...field} label="Disponible compras" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="creditLimitAdvances" control={control} render={({ field }) => (
                        <TextField {...field} label="Cupo avances" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="availableAdvances" control={control} render={({ field }) => (
                        <TextField {...field} label="Disponible avances" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="previousBalance" control={control} render={({ field }) => (
                        <TextField {...field} label="Saldo anterior" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="minimumPayment" control={control} render={({ field }) => (
                        <TextField {...field} label="Pago mínimo" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="alternateMinimumPayment" control={control} render={({ field }) => (
                        <TextField {...field} label="Pago Mínimo Alterno" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="lateInterest" control={control} render={({ field }) => (
                        <TextField {...field} label="Intereses de mora" type="number" fullWidth
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                    <Grid item xs={6}>
                      <Controller name="paymentDueDay" control={control} render={({ field }) => (
                        <TextField {...field} label="Día de pago" type="number" fullWidth
                          inputProps={{ min: 1, max: 31 }}
                          onChange={e => field.onChange(Number(e.target.value))} />
                      )} />
                    </Grid>
                  </Grid>
                </>
              )}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancelar</Button>
            <Button type="submit" variant="contained"
              disabled={createCredit.isPending || updateCredit.isPending}>
              Guardar
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Dialog registrar pago */}
      <Dialog open={payDialogOpen} onClose={() => setPayDialogOpen(false)} maxWidth="xs" fullWidth>
        <form onSubmit={handlePaySubmit(onPaySubmit)}>
          <DialogTitle>Registrar Pago — {payingCredit?.name}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} mt={1}>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Controller name="month" control={payControl} render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Mes</InputLabel>
                      <Select {...field} label="Mes" onChange={e => field.onChange(Number(e.target.value))}>
                        {Array.from({ length: 12 }, (_, i) => (
                          <MenuItem key={i + 1} value={i + 1}>
                            {new Date(2000, i, 1).toLocaleString('es-CO', { month: 'long' })}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  )} />
                </Grid>
                <Grid item xs={6}>
                  <Controller name="year" control={payControl} render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Año</InputLabel>
                      <Select {...field} label="Año" onChange={e => field.onChange(Number(e.target.value))}>
                        {[2024, 2025, 2026, 2027].map(y =>
                          <MenuItem key={y} value={y}>{y}</MenuItem>)}
                      </Select>
                    </FormControl>
                  )} />
                </Grid>
              </Grid>
              <Controller name="amount" control={payControl} render={({ field, fieldState }) => (
                <TextField {...field} label="Monto pagado" type="number" fullWidth
                  error={!!fieldState.error} helperText={fieldState.error?.message}
                  onChange={e => field.onChange(Number(e.target.value))} />
              )} />
              <Controller name="paymentDate" control={payControl} render={({ field, fieldState }) => (
                <TextField {...field} label="Fecha de pago" type="date" fullWidth
                  InputLabelProps={{ shrink: true }}
                  error={!!fieldState.error} helperText={fieldState.error?.message} />
              )} />
              <Controller name="notes" control={payControl} render={({ field }) => (
                <TextField {...field} label="Notas" fullWidth multiline rows={2} />
              )} />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setPayDialogOpen(false)}>Cancelar</Button>
            <Button type="submit" variant="contained" disabled={registerPayment.isPending}>
              Registrar Pago
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
