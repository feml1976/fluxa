import { useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog,
  DialogActions, DialogContent, DialogTitle, FormControl,
  Grid, IconButton, InputLabel, MenuItem, Paper, Select,
  Stack, Tab, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Tabs, TextField, Tooltip, Typography,
} from '@mui/material';
import AddIcon     from '@mui/icons-material/Add';
import EditIcon    from '@mui/icons-material/Edit';
import DeleteIcon  from '@mui/icons-material/Delete';
import PaymentIcon from '@mui/icons-material/Payment';
import {
  useCommitments, useMonthlyCommitments,
  useCreateCommitment, useUpdateCommitment, useDeleteCommitment,
  useRegisterPayment,
} from '../hooks/useCommitment';
import CommitmentStatusBadge from '../components/CommitmentStatusBadge';
import { formatCOP } from '@/shared/utils/currencyFormatter';
import { extractApiError } from '@/shared/utils/apiError';
import type {
  FixedCommitmentDto, FixedCommitmentRequest,
  CommitmentRecordDto, RegisterPaymentRequest,
  CommitmentFrequency,
} from '../types/commitment.types';

// ── Constantes ────────────────────────────────────────────────────
const MONTHS = [
  'Enero','Febrero','Marzo','Abril','Mayo','Junio',
  'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre',
];
const FREQ_OPTS: { value: CommitmentFrequency; label: string }[] = [
  { value: 'MONTHLY',    label: 'Mensual' },
  { value: 'BIMONTHLY',  label: 'Bimestral' },
  { value: 'QUARTERLY',  label: 'Trimestral' },
  { value: 'ANNUAL',     label: 'Anual' },
];
const EMPTY_FORM: FixedCommitmentRequest = {
  name: '', description: '', estimatedAmount: 0,
  dueDay: 1, frequency: 'MONTHLY', alertDaysBefore: 3,
};

// ── Diálogo: crear / editar compromiso ───────────────────────────
interface CommitmentDialogProps {
  open: boolean;
  editing: FixedCommitmentDto | null;
  onClose: () => void;
}
const CommitmentDialog: React.FC<CommitmentDialogProps> = ({ open, editing, onClose }) => {
  const [form, setForm] = useState<FixedCommitmentRequest>(EMPTY_FORM);
  const [error, setError] = useState<string | null>(null);
  const create = useCreateCommitment();
  const update = useUpdateCommitment();

  const set = (field: keyof FixedCommitmentRequest) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm(f => ({ ...f, [field]: e.target.value }));

  const handleOpen = () => {
    setError(null);
    setForm(editing
      ? {
          name: editing.name,
          description: editing.description ?? '',
          estimatedAmount: editing.estimatedAmount,
          dueDay: editing.dueDay,
          frequency: editing.frequency,
          alertDaysBefore: editing.alertDaysBefore,
          categoryId: editing.categoryId ?? undefined,
        }
      : EMPTY_FORM);
  };

  const handleSubmit = async () => {
    setError(null);
    if (!form.name.trim()) { setError('El nombre es obligatorio'); return; }
    if (form.estimatedAmount <= 0) { setError('El monto debe ser mayor a 0'); return; }
    if (form.dueDay < 1 || form.dueDay > 31) { setError('El día de pago debe estar entre 1 y 31'); return; }
    try {
      if (editing) {
        await update.mutateAsync({ id: editing.id, data: form });
      } else {
        await create.mutateAsync(form);
      }
      onClose();
    } catch (e) {
      setError(extractApiError(e, 'Error al guardar el compromiso'));
    }
  };

  const isPending = create.isPending || update.isPending;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth
      TransitionProps={{ onEntering: handleOpen }}>
      <DialogTitle>{editing ? 'Editar compromiso' : 'Nuevo compromiso fijo'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} mt={1}>
          {error && <Alert severity="error">{error}</Alert>}

          <TextField label="Nombre *" value={form.name} size="small" fullWidth
            onChange={set('name')} disabled={isPending} />

          <TextField label="Descripción" value={form.description ?? ''} size="small" fullWidth
            onChange={set('description')} disabled={isPending} />

          <TextField label="Monto estimado (COP) *" type="number" value={form.estimatedAmount}
            size="small" fullWidth inputProps={{ min: 0 }} disabled={isPending}
            onChange={e => setForm(f => ({ ...f, estimatedAmount: Number(e.target.value) }))} />

          <Stack direction="row" spacing={2}>
            <TextField label="Día de pago *" type="number" value={form.dueDay}
              size="small" fullWidth inputProps={{ min: 1, max: 31 }} disabled={isPending}
              onChange={e => setForm(f => ({ ...f, dueDay: Number(e.target.value) }))} />

            <FormControl fullWidth size="small">
              <InputLabel>Frecuencia *</InputLabel>
              <Select value={form.frequency} label="Frecuencia *" disabled={isPending}
                onChange={e => setForm(f => ({ ...f, frequency: e.target.value as CommitmentFrequency }))}>
                {FREQ_OPTS.map(o => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
              </Select>
            </FormControl>
          </Stack>

          <TextField label="Días de alerta anticipada" type="number" value={form.alertDaysBefore ?? 3}
            size="small" fullWidth inputProps={{ min: 0, max: 30 }} disabled={isPending}
            onChange={e => setForm(f => ({ ...f, alertDaysBefore: Number(e.target.value) }))} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancelar</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={isPending}>
          {isPending ? <CircularProgress size={20} /> : editing ? 'Guardar' : 'Crear'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// ── Diálogo: registrar pago ───────────────────────────────────────
interface PaymentDialogProps {
  open: boolean;
  record: CommitmentRecordDto | null;
  month: number;
  year: number;
  onClose: () => void;
}
const PaymentDialog: React.FC<PaymentDialogProps> = ({ open, record, month, year, onClose }) => {
  const [form, setForm] = useState<RegisterPaymentRequest>({ actualAmount: 0 });
  const [error, setError] = useState<string | null>(null);
  const register = useRegisterPayment(month, year);

  const handleOpen = () => {
    setError(null);
    setForm({
      actualAmount: record?.estimatedAmount ?? 0,
      paidDate: new Date().toISOString().slice(0, 10),
      receiptReference: '',
      notes: '',
    });
  };

  const handleSubmit = async () => {
    setError(null);
    if (!record) return;
    if (form.actualAmount <= 0) { setError('El monto debe ser mayor a 0'); return; }
    try {
      await register.mutateAsync({ id: record.id, data: form });
      onClose();
    } catch (e) {
      setError(extractApiError(e, 'Error al registrar el pago'));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth
      TransitionProps={{ onEntering: handleOpen }}>
      <DialogTitle>Registrar pago — {record?.commitmentName}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} mt={1}>
          {error && <Alert severity="error">{error}</Alert>}

          <TextField label="Monto pagado (COP) *" type="number" value={form.actualAmount}
            size="small" fullWidth inputProps={{ min: 0 }}
            onChange={e => setForm(f => ({ ...f, actualAmount: Number(e.target.value) }))} />

          <TextField label="Fecha de pago" type="date" size="small" fullWidth
            value={form.paidDate ?? ''} InputLabelProps={{ shrink: true }}
            onChange={e => setForm(f => ({ ...f, paidDate: e.target.value }))} />

          <TextField label="Número de comprobante" size="small" fullWidth
            value={form.receiptReference ?? ''}
            onChange={e => setForm(f => ({ ...f, receiptReference: e.target.value }))} />

          <TextField label="Notas" size="small" fullWidth multiline rows={2}
            value={form.notes ?? ''}
            onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={register.isPending}>Cancelar</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={register.isPending}>
          {register.isPending ? <CircularProgress size={20} /> : 'Registrar pago'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// ── Página principal ──────────────────────────────────────────────
const CommitmentPage: React.FC = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear]   = useState(now.getFullYear());
  const [tab, setTab]     = useState(0);

  const [commitmentDialog, setCommitmentDialog] = useState(false);
  const [editingCommitment, setEditingCommitment] = useState<FixedCommitmentDto | null>(null);
  const [paymentDialog, setPaymentDialog] = useState(false);
  const [payingRecord, setPayingRecord] = useState<CommitmentRecordDto | null>(null);

  const { data: monthly,     isLoading: loadingMonthly } = useMonthlyCommitments(month, year);
  const { data: commitments, isLoading: loadingList    } = useCommitments();
  const deleteCommitment = useDeleteCommitment();

  const years = Array.from({ length: 3 }, (_, i) => now.getFullYear() - 1 + i);

  const openNew  = () => { setEditingCommitment(null); setCommitmentDialog(true); };
  const openEdit = (c: FixedCommitmentDto) => { setEditingCommitment(c); setCommitmentDialog(true); };
  const openPay  = (r: CommitmentRecordDto) => { setPayingRecord(r); setPaymentDialog(true); };
  const handleDelete = (id: number) => {
    if (window.confirm('¿Eliminar este compromiso?')) deleteCommitment.mutate(id);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Compromisos Fijos</Typography>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Resumen mensual" />
        <Tab label="Mis compromisos" />
      </Tabs>

      {/* ── TAB 0: Resumen mensual ── */}
      {tab === 0 && (
        <>
          <Stack direction="row" spacing={2} mb={3}>
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel>Mes</InputLabel>
              <Select value={month} label="Mes"
                onChange={e => setMonth(Number(e.target.value))}>
                {MONTHS.map((m, i) => <MenuItem key={i} value={i + 1}>{m}</MenuItem>)}
              </Select>
            </FormControl>
            <FormControl size="small" sx={{ minWidth: 100 }}>
              <InputLabel>Año</InputLabel>
              <Select value={year} label="Año"
                onChange={e => setYear(Number(e.target.value))}>
                {years.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
              </Select>
            </FormControl>
          </Stack>

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
                        <TableCell align="right">
                          {r.actualAmount != null ? formatCOP(r.actualAmount) : '—'}
                        </TableCell>
                        <TableCell>{r.dueDate}</TableCell>
                        <TableCell><CommitmentStatusBadge status={r.status} /></TableCell>
                        <TableCell align="center">
                          {r.status !== 'PAID' && (
                            <Tooltip title="Registrar pago">
                              <IconButton size="small" color="primary" onClick={() => openPay(r)}>
                                <PaymentIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                    {monthly.records.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6} align="center">
                          <Alert severity="info" sx={{ m: 1 }}>
                            No hay compromisos para este período. Crea uno en la pestaña "Mis compromisos".
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

      {/* ── TAB 1: Mis compromisos ── */}
      {tab === 1 && (
        <>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>
              Nuevo compromiso
            </Button>
          </Box>

          {loadingList ? <CircularProgress /> : (
            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Nombre</TableCell>
                    <TableCell align="right">Monto estimado</TableCell>
                    <TableCell>Día de pago</TableCell>
                    <TableCell>Frecuencia</TableCell>
                    <TableCell>Estado</TableCell>
                    <TableCell align="center">Acciones</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {commitments?.map(c => (
                    <TableRow key={c.id}>
                      <TableCell>
                        <Box>
                          <Typography variant="body2">{c.name}</Typography>
                          {c.description && (
                            <Typography variant="caption" color="text.secondary">{c.description}</Typography>
                          )}
                        </Box>
                      </TableCell>
                      <TableCell align="right">{formatCOP(c.estimatedAmount)}</TableCell>
                      <TableCell>Día {c.dueDay}</TableCell>
                      <TableCell>
                        {FREQ_OPTS.find(f => f.value === c.frequency)?.label ?? c.frequency}
                      </TableCell>
                      <TableCell>
                        <Chip size="small"
                          label={c.isActive ? 'Activo' : 'Inactivo'}
                          color={c.isActive ? 'success' : 'default'} />
                      </TableCell>
                      <TableCell align="center">
                        <Tooltip title="Editar">
                          <IconButton size="small" onClick={() => openEdit(c)}>
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Eliminar">
                          <IconButton size="small" color="error" onClick={() => handleDelete(c.id)}>
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                  {(commitments?.length ?? 0) === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Alert severity="info" sx={{ m: 1 }}>
                          No hay compromisos registrados. Haz clic en "Nuevo compromiso" para comenzar.
                        </Alert>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </>
      )}

      {/* ── Diálogos ── */}
      <CommitmentDialog
        open={commitmentDialog}
        editing={editingCommitment}
        onClose={() => setCommitmentDialog(false)}
      />
      <PaymentDialog
        open={paymentDialog}
        record={payingRecord}
        month={month}
        year={year}
        onClose={() => setPaymentDialog(false)}
      />
    </Box>
  );
};

export default CommitmentPage;
