import { useState } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, IconButton, Button,
  CircularProgress, Alert, Tabs, Tab, Select,
  MenuItem, FormControl, InputLabel, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Stack, Chip,
  Tooltip,
} from '@mui/material';
import EditIcon   from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon    from '@mui/icons-material/Add';
import PaidIcon   from '@mui/icons-material/Paid';
import {
  useMonthlyIncome, useIncomeSources,
  useCreateIncomeSource, useUpdateIncomeSource, useDeleteIncomeSource,
  useUpdateIncomeRecord,
} from '../hooks/useIncome';
import IncomeStatusBadge from '../components/IncomeStatusBadge';
import { formatCOP } from '@/shared/utils/currencyFormatter';
import { extractApiError } from '@/shared/utils/apiError';
import type {
  IncomeSourceDto, IncomeSourceRequest,
  IncomeRecordDto, UpdateIncomeRecordRequest,
  IncomeType, IncomeFrequency, IncomeStatus,
} from '../types/income.types';

// ── Constantes de UI ──────────────────────────────────────────────
const MONTHS = [
  'Enero','Febrero','Marzo','Abril','Mayo','Junio',
  'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre',
];
const TYPE_OPTS: { value: IncomeType; label: string }[] = [
  { value: 'FIXED',    label: 'Fijo' },
  { value: 'VARIABLE', label: 'Variable' },
];
const FREQ_OPTS: { value: IncomeFrequency; label: string }[] = [
  { value: 'MONTHLY',   label: 'Mensual' },
  { value: 'BIWEEKLY',  label: 'Quincenal' },
  { value: 'WEEKLY',    label: 'Semanal' },
  { value: 'ONE_TIME',  label: 'Única vez' },
];
const STATUS_OPTS: { value: IncomeStatus; label: string }[] = [
  { value: 'EXPECTED',     label: 'Esperado' },
  { value: 'RECEIVED',     label: 'Recibido' },
  { value: 'PARTIAL',      label: 'Parcial' },
  { value: 'NOT_RECEIVED', label: 'No recibido' },
];

const EMPTY_SOURCE: IncomeSourceRequest = {
  name: '', description: '', type: 'FIXED',
  frequency: 'MONTHLY', expectedAmount: 0,
  startDate: new Date().toISOString().slice(0, 10),
};

// ── Diálogo: crear / editar fuente ───────────────────────────────
interface SourceDialogProps {
  open: boolean;
  editing: IncomeSourceDto | null;
  onClose: () => void;
  month: number; year: number;
}

const SourceDialog: React.FC<SourceDialogProps> = ({ open, editing, onClose }) => {
  const [form, setForm] = useState<IncomeSourceRequest>(EMPTY_SOURCE);
  const [error, setError] = useState<string | null>(null);
  const create = useCreateIncomeSource();
  const update = useUpdateIncomeSource();

  const set = (field: keyof IncomeSourceRequest) =>
    (e: React.ChangeEvent<HTMLInputElement | { value: unknown }>) =>
      setForm(f => ({ ...f, [field]: e.target.value }));

  const handleOpen = () => {
    setError(null);
    setForm(editing
      ? { name: editing.name, description: editing.description ?? '',
          type: editing.type, frequency: editing.frequency,
          expectedAmount: editing.expectedAmount,
          startDate: editing.startDate, endDate: editing.endDate ?? undefined }
      : EMPTY_SOURCE);
  };

  const handleSubmit = async () => {
    setError(null);
    try {
      if (editing) {
        await update.mutateAsync({ id: editing.id, data: form });
      } else {
        await create.mutateAsync(form);
      }
      onClose();
    } catch (e) {
      setError(extractApiError(e, 'Error al guardar la fuente'));
    }
  };

  const isPending = create.isPending || update.isPending;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth
      TransitionProps={{ onEntering: handleOpen }}>
      <DialogTitle>{editing ? 'Editar fuente de ingreso' : 'Nueva fuente de ingreso'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} mt={1}>
          {error && <Alert severity="error">{error}</Alert>}

          <TextField label="Nombre *" value={form.name}
            onChange={set('name')} fullWidth size="small" />

          <TextField label="Descripción" value={form.description ?? ''}
            onChange={set('description')} fullWidth size="small" />

          <Stack direction="row" spacing={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Tipo *</InputLabel>
              <Select value={form.type} label="Tipo *"
                onChange={e => setForm(f => ({ ...f, type: e.target.value as IncomeType }))}>
                {TYPE_OPTS.map(o => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
              </Select>
            </FormControl>

            <FormControl fullWidth size="small">
              <InputLabel>Frecuencia *</InputLabel>
              <Select value={form.frequency} label="Frecuencia *"
                onChange={e => setForm(f => ({ ...f, frequency: e.target.value as IncomeFrequency }))}>
                {FREQ_OPTS.map(o => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
              </Select>
            </FormControl>
          </Stack>

          <TextField label="Monto esperado (COP) *" type="number"
            value={form.expectedAmount} size="small"
            onChange={e => setForm(f => ({ ...f, expectedAmount: Number(e.target.value) }))}
            fullWidth inputProps={{ min: 0 }} />

          <Stack direction="row" spacing={2}>
            <TextField label="Fecha inicio *" type="date" size="small"
              value={form.startDate} onChange={set('startDate')}
              fullWidth InputLabelProps={{ shrink: true }} />
            <TextField label="Fecha fin" type="date" size="small"
              value={form.endDate ?? ''} onChange={set('endDate')}
              fullWidth InputLabelProps={{ shrink: true }} />
          </Stack>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancelar</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={isPending || !form.name}>
          {isPending ? <CircularProgress size={20} /> : editing ? 'Guardar' : 'Crear'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// ── Diálogo: registrar ingreso del mes ───────────────────────────
interface RecordDialogProps {
  open: boolean;
  record: IncomeRecordDto | null;
  month: number; year: number;
  onClose: () => void;
}

const RecordDialog: React.FC<RecordDialogProps> = ({ open, record, month, year, onClose }) => {
  const [form, setForm] = useState<UpdateIncomeRecordRequest>({
    amount: 0, status: 'RECEIVED', receivedDate: '', notes: '',
  });
  const [error, setError] = useState<string | null>(null);
  const update = useUpdateIncomeRecord(month, year);

  const handleOpen = () => {
    setError(null);
    if (record) {
      setForm({
        amount: record.amount,
        status: record.status,
        receivedDate: record.receivedDate ?? new Date().toISOString().slice(0, 10),
        notes: record.notes ?? '',
      });
    }
  };

  const handleSubmit = async () => {
    setError(null);
    try {
      await update.mutateAsync({ id: record!.id, data: form });
      onClose();
    } catch (e) {
      setError(extractApiError(e, 'Error al registrar el ingreso'));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth
      TransitionProps={{ onEntering: handleOpen }}>
      <DialogTitle>Registrar ingreso — {record?.sourceName}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} mt={1}>
          {error && <Alert severity="error">{error}</Alert>}

          <TextField label="Monto recibido (COP) *" type="number"
            value={form.amount} size="small"
            onChange={e => setForm(f => ({ ...f, amount: Number(e.target.value) }))}
            fullWidth inputProps={{ min: 0 }} />

          <FormControl fullWidth size="small">
            <InputLabel>Estado *</InputLabel>
            <Select value={form.status} label="Estado *"
              onChange={e => setForm(f => ({ ...f, status: e.target.value as IncomeStatus }))}>
              {STATUS_OPTS.map(o => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
            </Select>
          </FormControl>

          <TextField label="Fecha recibido" type="date" size="small"
            value={form.receivedDate ?? ''} InputLabelProps={{ shrink: true }}
            onChange={e => setForm(f => ({ ...f, receivedDate: e.target.value }))}
            fullWidth />

          <TextField label="Notas" value={form.notes ?? ''} size="small"
            onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
            fullWidth multiline rows={2} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={update.isPending}>Cancelar</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={update.isPending}>
          {update.isPending ? <CircularProgress size={20} /> : 'Guardar'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// ── Página principal ──────────────────────────────────────────────
const IncomePage: React.FC = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear]   = useState(now.getFullYear());
  const [tab, setTab]     = useState(0);

  const [sourceDialog, setSourceDialog] = useState(false);
  const [editingSource, setEditingSource] = useState<IncomeSourceDto | null>(null);
  const [recordDialog, setRecordDialog] = useState(false);
  const [editingRecord, setEditingRecord] = useState<IncomeRecordDto | null>(null);

  const { data: monthly,  isLoading: loadingMonthly  } = useMonthlyIncome(month, year);
  const { data: sources,  isLoading: loadingSources  } = useIncomeSources();
  const deleteSource = useDeleteIncomeSource();

  const years = Array.from({ length: 3 }, (_, i) => now.getFullYear() - 1 + i);

  const openNew    = () => { setEditingSource(null); setSourceDialog(true); };
  const openEdit   = (s: IncomeSourceDto) => { setEditingSource(s); setSourceDialog(true); };
  const openRecord = (r: IncomeRecordDto) => { setEditingRecord(r); setRecordDialog(true); };
  const handleDelete = (id: number) => {
    if (window.confirm('¿Eliminar esta fuente de ingreso?')) deleteSource.mutate(id);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" fontWeight="bold" gutterBottom>Ingresos</Typography>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Resumen mensual" />
        <Tab label="Fuentes de ingreso" />
      </Tabs>

      {/* ── TAB 0: Resumen mensual ── */}
      {tab === 0 && (
        <>
          <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
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
          </Box>

          {loadingMonthly ? <CircularProgress /> : monthly && (
            <>
              <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
                <Paper sx={{ p: 2, flex: 1 }}>
                  <Typography variant="body2" color="text.secondary">Ingreso Esperado</Typography>
                  <Typography variant="h6" fontWeight="bold">
                    {formatCOP(monthly.totalExpected)}
                  </Typography>
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
                      <TableCell align="center">Registrar</TableCell>
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
                          <Tooltip title="Registrar ingreso">
                            <IconButton size="small" color="primary"
                              onClick={() => openRecord(r)}>
                              <PaidIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))}
                    {monthly.records.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} align="center">
                          <Alert severity="info" sx={{ m: 1 }}>
                            No hay registros para este período. Crea una fuente de ingreso en la pestaña "Fuentes de ingreso".
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

      {/* ── TAB 1: Fuentes de ingreso ── */}
      {tab === 1 && (
        <>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>
              Nueva fuente
            </Button>
          </Box>

          {loadingSources ? <CircularProgress /> : (
            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Nombre</TableCell>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Frecuencia</TableCell>
                    <TableCell align="right">Monto esperado</TableCell>
                    <TableCell>Estado</TableCell>
                    <TableCell align="center">Acciones</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sources?.map(s => (
                    <TableRow key={s.id}>
                      <TableCell>{s.name}</TableCell>
                      <TableCell>{s.type === 'FIXED' ? 'Fijo' : 'Variable'}</TableCell>
                      <TableCell>
                        {FREQ_OPTS.find(f => f.value === s.frequency)?.label ?? s.frequency}
                      </TableCell>
                      <TableCell align="right">{formatCOP(s.expectedAmount)}</TableCell>
                      <TableCell>
                        <Chip size="small"
                          label={s.isActive ? 'Activo' : 'Inactivo'}
                          color={s.isActive ? 'success' : 'default'} />
                      </TableCell>
                      <TableCell align="center">
                        <Tooltip title="Editar">
                          <IconButton size="small" onClick={() => openEdit(s)}>
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Eliminar">
                          <IconButton size="small" color="error"
                            onClick={() => handleDelete(s.id)}>
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                  {(sources?.length ?? 0) === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Alert severity="info" sx={{ m: 1 }}>
                          No hay fuentes de ingreso. Haz clic en "Nueva fuente" para comenzar.
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
      <SourceDialog
        open={sourceDialog}
        editing={editingSource}
        month={month} year={year}
        onClose={() => setSourceDialog(false)}
      />
      <RecordDialog
        open={recordDialog}
        record={editingRecord}
        month={month} year={year}
        onClose={() => setRecordDialog(false)}
      />
    </Box>
  );
};

export default IncomePage;
