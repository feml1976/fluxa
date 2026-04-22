import React, { useRef, useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Divider, Paper,
  Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Typography,
} from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { importApi } from '../api/importApi';
import type { ImportPreviewResponse } from '../types/import.types';

export const ImportPage: React.FC = () => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<ImportPreviewResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0] ?? null;
    setFile(f);
    setPreview(null);
    setResult(null);
    setError(null);
  };

  const handlePreview = async () => {
    if (!file) return;
    setLoading(true);
    setError(null);
    try {
      const data = await importApi.preview(file);
      setPreview(data);
    } catch {
      setError('Error al procesar el archivo. Verifica que sea un .csv o .xlsx válido.');
    } finally {
      setLoading(false);
    }
  };

  const handleExecute = async () => {
    if (!file) return;
    setLoading(true);
    setError(null);
    try {
      const data = await importApi.execute(file);
      setResult(data.message);
      setPreview(null);
      setFile(null);
      if (inputRef.current) inputRef.current.value = '';
    } catch {
      setError('Error al importar los datos.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={1}>Importación de Gastos</Typography>
      <Typography variant="body2" color="text.secondary" mb={1}>
        Carga un archivo <strong>.csv</strong> o <strong>.xlsx</strong> con tus gastos.
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Columnas requeridas (en orden): <code>tipo</code> · <code>descripcion</code> · <code>monto</code> · <code>fecha</code>
        <br />Formato fecha: <code>DD/MM/YYYY</code> o <code>YYYY-MM-DD</code>. Tipo permitido: <code>gasto</code>.
      </Typography>

      <Stack direction="row" spacing={2} alignItems="center" mb={3}>
        <Button
          variant="outlined"
          startIcon={<UploadFileIcon />}
          onClick={() => inputRef.current?.click()}
        >
          {file ? file.name : 'Seleccionar archivo'}
        </Button>
        <input
          ref={inputRef}
          type="file"
          accept=".csv,.xlsx,.xls"
          style={{ display: 'none' }}
          onChange={handleFileChange}
        />
        {file && (
          <Button variant="contained" onClick={handlePreview} disabled={loading}>
            {loading ? <CircularProgress size={18} /> : 'Vista previa'}
          </Button>
        )}
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {result && <Alert severity="success" sx={{ mb: 2 }}>{result}</Alert>}

      {preview && (
        <>
          <Stack direction="row" spacing={2} mb={2} alignItems="center">
            <Chip label={`${preview.totalRows} filas`} />
            <Chip label={`${preview.validRows} válidas`} color="success" />
            {preview.invalidRows > 0 && (
              <Chip label={`${preview.invalidRows} con error`} color="error" />
            )}
          </Stack>

          <TableContainer component={Paper} variant="outlined" sx={{ mb: 3 }}>
            <Table size="small">
              <TableHead>
                <TableRow sx={{ '& th': { fontWeight: 700 } }}>
                  <TableCell>#</TableCell>
                  <TableCell>Descripción</TableCell>
                  <TableCell>Monto</TableCell>
                  <TableCell>Fecha</TableCell>
                  <TableCell>Estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {preview.rows.map(row => (
                  <TableRow
                    key={row.rowNumber}
                    sx={{ bgcolor: row.valid ? 'inherit' : '#fff3f3' }}
                  >
                    <TableCell>{row.rowNumber}</TableCell>
                    <TableCell>{row.valid ? row.resolvedDescription : row.rawData}</TableCell>
                    <TableCell>
                      {row.valid && row.resolvedAmount
                        ? `$${Number(row.resolvedAmount).toLocaleString('es-CO')}`
                        : '—'}
                    </TableCell>
                    <TableCell>{row.resolvedDate ?? '—'}</TableCell>
                    <TableCell>
                      {row.valid ? (
                        <Chip label="OK" color="success" size="small" />
                      ) : (
                        <Stack spacing={0.5}>
                          {row.errors.map((e, i) => (
                            <Typography key={i} variant="caption" color="error">{e}</Typography>
                          ))}
                        </Stack>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Divider sx={{ mb: 2 }} />

          {preview.validRows > 0 && (
            <Button
              variant="contained"
              color="success"
              startIcon={<CheckCircleIcon />}
              onClick={handleExecute}
              disabled={loading}
            >
              {loading
                ? <CircularProgress size={18} />
                : `Importar ${preview.validRows} gasto(s)`}
            </Button>
          )}
        </>
      )}
    </Box>
  );
};
