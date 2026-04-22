import type { ExpenseCategoryType } from './expenseCategory.types';

export type CommitmentFrequency = 'MONTHLY' | 'BIMONTHLY' | 'QUARTERLY' | 'ANNUAL';
export type CommitmentStatus = 'PENDING' | 'PAID' | 'OVERDUE';

export interface ExpenseCategoryDto {
  id: number;
  name: string;
  color: string;
  icon: string | null;
  type: ExpenseCategoryType;
}

export interface FixedCommitmentDto {
  id: number;
  name: string;
  description: string | null;
  estimatedAmount: number;
  dueDay: number;
  frequency: CommitmentFrequency;
  alertDaysBefore: number;
  isActive: boolean;
  categoryId: number | null;
}

export interface CommitmentRecordDto {
  id: number;
  commitmentId: number;
  commitmentName: string;
  periodMonth: number;
  periodYear: number;
  estimatedAmount: number;
  actualAmount: number | null;
  dueDate: string;
  paidDate: string | null;
  status: CommitmentStatus;
  receiptReference: string | null;
  notes: string | null;
}

export interface MonthlyCommitmentSummary {
  month: number;
  year: number;
  totalEstimated: number;
  totalPaid: number;
  pendingCount: number;
  paidCount: number;
  overdueCount: number;
  records: CommitmentRecordDto[];
}

export interface FixedCommitmentRequest {
  name: string;
  description?: string;
  estimatedAmount: number;
  dueDay: number;
  frequency: CommitmentFrequency;
  alertDaysBefore?: number;
  categoryId?: number;
}

export interface RegisterPaymentRequest {
  actualAmount: number;
  paidDate?: string;
  receiptReference?: string;
  notes?: string;
}
