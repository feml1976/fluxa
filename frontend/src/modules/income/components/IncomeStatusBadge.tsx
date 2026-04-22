import { Chip } from '@mui/material';
import type { IncomeStatus } from '../types/income.types';

const CONFIG: Record<IncomeStatus, { label: string; color: 'success' | 'warning' | 'error' | 'default' }> = {
  RECEIVED:     { label: 'Recibido',     color: 'success' },
  PARTIAL:      { label: 'Parcial',      color: 'warning' },
  EXPECTED:     { label: 'Esperado',     color: 'default' },
  NOT_RECEIVED: { label: 'No recibido',  color: 'error'   },
};

interface Props { status: IncomeStatus; size?: 'small' | 'medium'; }

const IncomeStatusBadge: React.FC<Props> = ({ status, size = 'small' }) => {
  const { label, color } = CONFIG[status];
  return <Chip label={label} color={color} size={size} />;
};

export default IncomeStatusBadge;
