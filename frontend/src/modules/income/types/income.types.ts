export type IncomeType = 'FIXED' | 'VARIABLE';
export type IncomeFrequency = 'MONTHLY' | 'BIWEEKLY' | 'WEEKLY' | 'ONE_TIME';
export type IncomeStatus = 'EXPECTED' | 'RECEIVED' | 'PARTIAL' | 'NOT_RECEIVED';

export interface IncomeCategoryDto {
  id: number;
  name: string;
  color: string;
  icon: string | null;
}

export interface IncomeSourceDto {
  id: number;
  name: string;
  description: string | null;
  type: IncomeType;
  expectedAmount: number;
  frequency: IncomeFrequency;
  startDate: string;
  endDate: string | null;
  isActive: boolean;
  categoryId: number | null;
}

export interface IncomeRecordDto {
  id: number;
  sourceId: number;
  sourceName: string;
  amount: number;
  receivedDate: string | null;
  periodMonth: number;
  periodYear: number;
  status: IncomeStatus;
  notes: string | null;
}

export interface MonthlyIncomeSummary {
  month: number;
  year: number;
  totalExpected: number;
  totalReceived: number;
  records: IncomeRecordDto[];
}

export interface IncomeSourceRequest {
  name: string;
  description?: string;
  type: IncomeType;
  expectedAmount: number;
  frequency: IncomeFrequency;
  startDate: string;
  endDate?: string;
  categoryId?: number;
}

export interface UpdateIncomeRecordRequest {
  amount: number;
  status: IncomeStatus;
  receivedDate?: string;
  notes?: string;
}
