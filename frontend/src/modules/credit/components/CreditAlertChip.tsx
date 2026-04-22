import React from 'react';
import { Chip } from '@mui/material';
import type { CreditAlertLevel } from '../types/credit.types';

interface Props {
  level: CreditAlertLevel;
}

const CONFIG: Record<CreditAlertLevel, { label: string; color: 'success' | 'warning' | 'error' }> = {
  GREEN:  { label: 'Saludable', color: 'success' },
  YELLOW: { label: 'Precaución', color: 'warning' },
  RED:    { label: 'Alerta', color: 'error' },
};

export const CreditAlertChip: React.FC<Props> = ({ level }) => {
  const { label, color } = CONFIG[level];
  return <Chip label={label} color={color} size="small" />;
};
