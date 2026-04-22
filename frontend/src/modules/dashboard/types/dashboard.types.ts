export type HealthStatus = 'GREEN' | 'YELLOW' | 'RED';

export interface MonthlyProjectionDto {
  month: number;
  year: number;
  label: string;
  projectedIncome: number;
  projectedCommitments: number;
  projectedVariableExpenses: number;
  projectedCreditObligations: number;
  projectedNetFlow: number;
}

export interface ProjectionsResponse {
  months: number;
  projections: MonthlyProjectionDto[];
  avgProjectedIncome: number;
  avgProjectedExpenses: number;
  avgProjectedNetFlow: number;
}

export interface DebtStrategyItem {
  priority: number;
  creditId: number;
  creditName: string;
  creditType: string;
  currentBalance: number;
  interestRateMv: number;
  monthlyPayment: number;
  estimatedMonths: number;
  totalInterestToPay: number;
}

export interface DebtStrategyResponse {
  strategyName: string;
  description: string;
  items: DebtStrategyItem[];
  totalDebt: number;
  totalInterestToPay: number;
  estimatedMonthsToFreedom: number;
  interestSavedVsMinimum: number;
}

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
