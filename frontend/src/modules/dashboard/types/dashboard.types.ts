export type HealthStatus = 'GREEN' | 'YELLOW' | 'RED';

export interface UpcomingPaymentDto {
  commitmentId: number;
  name: string;
  estimatedAmount: number;
  dueDate: string;
  daysUntilDue: number;
}

export interface TopExpenseDto {
  categoryId: number;
  categoryName: string;
  total: number;
  percentage: number;
}

export interface DashboardSummaryResponse {
  month: number;
  year: number;
  totalIncome: number;
  totalIncomeExpected: number;
  totalCommitments: number;
  totalCommitmentsPaid: number;
  pendingCommitmentsCount: number;
  overdueCommitmentsCount: number;
  totalExpenses: number;
  totalExpensesPlanned: number;
  netFlow: number;
  commitmentRatio: number;
  healthStatus: HealthStatus;
  upcomingPayments: UpcomingPaymentDto[];
  topExpenses: TopExpenseDto[];
}
