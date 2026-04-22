export interface VariableExpenseResponse {
  id: number;
  categoryId: number;
  categoryName: string;
  amount: number;
  expenseDate: string;
  description: string | null;
  tags: string[];
  receiptUrl: string | null;
}

export interface VariableExpenseRequest {
  categoryId: number;
  amount: number;
  expenseDate: string;
  description?: string;
  tags?: string[];
  receiptUrl?: string;
}

export interface BudgetPlanResponse {
  id: number;
  categoryId: number;
  categoryName: string;
  plannedAmount: number;
  suggestedAmount: number | null;
  spentAmount: number;
  periodMonth: number;
  periodYear: number;
}

export interface BudgetPlanRequest {
  categoryId: number;
  plannedAmount: number;
  month: number;
  year: number;
}

export interface MonthlyExpenseSummary {
  month: number;
  year: number;
  totalSpent: number;
  totalPlanned: number;
  expenses: VariableExpenseResponse[];
  budgets: BudgetPlanResponse[];
}
