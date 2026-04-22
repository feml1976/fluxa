import React, { useState } from 'react';
import {
  Alert, Box, Button, Chip, CircularProgress, Paper,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Typography,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '../api/notificationApi';

const EVENT_LABELS: Record<string, string> = {
  COMMITMENT_DUE_SOON:        'Vencimiento próximo',
  COMMITMENT_OVERDUE:         'Compromiso vencido',
  CREDIT_CARD_LATE_INTEREST:  'Intereses de mora',
  CREDIT_CARD_NO_AVAILABLE:   'Cupo agotado',
  TEST:                       'Prueba',
};

export const NotificationPage: React.FC = () => {
  const qc = useQueryClient();
  const [testMsg, setTestMsg] = useState<string | null>(null);

  const { data: logs = [], isLoading } = useQuery({
    queryKey: ['notification-logs'],
    queryFn: notificationApi.getLogs,
  });

  const sendTest = useMutation({
    mutationFn: notificationApi.sendTest,
    onSuccess: () => {
      setTestMsg('Correo de prueba enviado correctamente.');
      qc.invalidateQueries({ queryKey: ['notification-logs'] });
    },
    onError: () => setTestMsg('Error al enviar el correo de prueba.'),
  });

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={1}>Notificaciones</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Alertas automáticas por email — vencimientos, mora y cupo agotado.
      </Typography>

      <Box mb={3} display="flex" alignItems="center" gap={2}>
        <Button
          variant="contained"
          startIcon={<SendIcon />}
          onClick={() => sendTest.mutate()}
          disabled={sendTest.isPending}
        >
          {sendTest.isPending ? 'Enviando…' : 'Enviar correo de prueba'}
        </Button>
        {testMsg && (
          <Alert
            severity={testMsg.startsWith('Error') ? 'error' : 'success'}
            onClose={() => setTestMsg(null)}
            sx={{ py: 0 }}
          >
            {testMsg}
          </Alert>
        )}
      </Box>

      <Typography variant="h6" fontWeight={600} mb={1}>Historial de envíos</Typography>

      {isLoading ? (
        <CircularProgress />
      ) : logs.length === 0 ? (
        <Typography color="text.secondary">Sin notificaciones registradas aún.</Typography>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700 } }}>
                <TableCell>Fecha</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell>Referencia</TableCell>
                <TableCell>Asunto</TableCell>
                <TableCell>Estado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.map(log => (
                <TableRow key={log.id} hover>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                    {new Date(log.sentAt).toLocaleString('es-CO', {
                      day: '2-digit', month: '2-digit', year: 'numeric',
                      hour: '2-digit', minute: '2-digit',
                    })}
                  </TableCell>
                  <TableCell>{EVENT_LABELS[log.eventType] ?? log.eventType}</TableCell>
                  <TableCell>{log.referenceName ?? '—'}</TableCell>
                  <TableCell sx={{ maxWidth: 260, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {log.subject}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={log.success ? 'Enviado' : 'Error'}
                      color={log.success ? 'success' : 'error'}
                      size="small"
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
};
