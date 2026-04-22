import React from 'react';
import {
  Alert, Box, Chip, LinearProgress, Paper, Stack,
  Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Typography,
} from '@mui/material';
import { useCreditAnalysis } from '../hooks/useCredit';
import type { CreditResponse } from '../types/credit.types';
import { formatCOP } from '../../../shared/utils/currencyFormatter';

interface Props {
  credit: CreditResponse;
}

export const CreditAnalysisPanel: React.FC<Props> = ({ credit }) => {
  const { data: analysis, isLoading } = useCreditAnalysis(credit.id, true);

  if (isLoading) return <LinearProgress />;
  if (!analysis) return null;

  return (
    <Box mt={2}>
      {/* Alertas */}
      {analysis.alerts.map((a, i) => (
        <Alert
          key={i}
          severity={analysis.alertLevel === 'RED' ? 'error' : analysis.alertLevel === 'YELLOW' ? 'warning' : 'info'}
          sx={{ mb: 1 }}
        >
          {a}
        </Alert>
      ))}

      {credit.type === 'CREDIT_CARD' && analysis.utilizationPct !== null && (
        <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
          <Typography variant="subtitle2" gutterBottom>Utilización del cupo</Typography>
          <Stack direction="row" justifyContent="space-between" mb={1}>
            <Typography variant="body2">{analysis.utilizationPct.toFixed(1)}%</Typography>
            <Chip
              size="small"
              label={analysis.utilizationPct >= 100 ? 'Agotado' : analysis.utilizationPct >= 80 ? 'Alta' : 'Normal'}
              color={analysis.utilizationPct >= 100 ? 'error' : analysis.utilizationPct >= 80 ? 'warning' : 'success'}
            />
          </Stack>
          <LinearProgress
            variant="determinate"
            value={Math.min(analysis.utilizationPct, 100)}
            color={analysis.utilizationPct >= 100 ? 'error' : analysis.utilizationPct >= 80 ? 'warning' : 'success'}
          />

          {analysis.monthsToPayMinimum !== null && (
            <Box mt={2}>
              <Typography variant="body2" color="text.secondary">
                Pagando el mínimo: {analysis.monthsToPayMinimum} meses para liquidar •{' '}
                <strong>{formatCOP(analysis.totalInterestWithMinimum ?? 0)}</strong> en intereses
              </Typography>
            </Box>
          )}
          {analysis.monthsToPayMinimum === null && (
            <Alert severity="error" sx={{ mt: 1 }}>
              El pago mínimo no cubre los intereses — la deuda nunca se liquidará con ese valor
            </Alert>
          )}
        </Paper>
      )}

      {credit.type !== 'CREDIT_CARD' && analysis.remainingInstallments !== null && (
        <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
          <Stack direction="row" spacing={4}>
            <Box>
              <Typography variant="caption" color="text.secondary">Cuotas restantes</Typography>
              <Typography variant="h6" fontWeight={700}>{analysis.remainingInstallments}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">Fecha proyectada de pago</Typography>
              <Typography variant="h6" fontWeight={700}>{analysis.projectedPayoffDate}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">Interés restante total</Typography>
              <Typography variant="h6" fontWeight={700} color="error.main">
                {formatCOP(analysis.totalRemainingInterest ?? 0)}
              </Typography>
            </Box>
          </Stack>
        </Paper>
      )}

      {/* Tabla de amortización (primeras 12 filas) */}
      {analysis.amortizationTable && analysis.amortizationTable.length > 0 && (
        <Box>
          <Typography variant="subtitle2" gutterBottom>Tabla de amortización (próximas cuotas)</Typography>
          <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 300 }}>
            <Table size="small" stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell>
                  <TableCell align="right">Cuota</TableCell>
                  <TableCell align="right">Interés</TableCell>
                  <TableCell align="right">Capital</TableCell>
                  <TableCell align="right">Saldo</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {analysis.amortizationTable.slice(0, 24).map(row => (
                  <TableRow key={row.installmentNumber}>
                    <TableCell>{row.installmentNumber}</TableCell>
                    <TableCell align="right">{formatCOP(row.installmentAmount)}</TableCell>
                    <TableCell align="right" sx={{ color: 'error.light' }}>
                      {formatCOP(row.interestPortion)}
                    </TableCell>
                    <TableCell align="right" sx={{ color: 'success.main' }}>
                      {formatCOP(row.capitalPortion)}
                    </TableCell>
                    <TableCell align="right">{formatCOP(row.remainingBalance)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}
    </Box>
  );
};
