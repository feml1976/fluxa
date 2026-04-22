import { Chip } from '@mui/material';
import type { CommitmentStatus } from '../types/commitment.types';

const CONFIG: Record<CommitmentStatus, { label: string; color: 'success' | 'warning' | 'error' }> = {
  PAID:    { label: 'Pagado',   color: 'success' },
  PENDING: { label: 'Pendiente', color: 'warning' },
  OVERDUE: { label: 'Vencido',  color: 'error'   },
};

interface Props { status: CommitmentStatus; size?: 'small' | 'medium'; }

const CommitmentStatusBadge: React.FC<Props> = ({ status, size = 'small' }) => {
  const { label, color } = CONFIG[status];
  return <Chip label={label} color={color} size={size} />;
};

export default CommitmentStatusBadge;
