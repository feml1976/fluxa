import api from '../../../shared/api/axiosConfig';
import type {
  BudgetPlanRequest,
  BudgetPlanResponse,
  MonthlyExpenseSummary,
  VariableExpenseRequest,
  VariableExpenseResponse,
} from '../types/expense.types';

export const expenseApi = {
  createExpense: (data: VariableExpenseRequest) =>
    api.post<{ data: VariableExpenseResponse }>('/expenses', data).then(r => r.data.data),

  listExpenses: (month: number, year: number) =>
    api.get<{ data: VariableExpenseResponse[] }>('/expenses', { params: { month, year } }).then(r => r.data.data),

  updateExpense: (id: number, data: VariableExpenseRequest) =>
    api.put<{ data: VariableExpenseResponse }>(`/expenses/${id}`, data).then(r => r.data.data),

  deleteExpense: (id: number) =>
    api.delete(`/expenses/${id}`),

  saveOrUpdateBudget: (data: BudgetPlanRequest) =>
    api.post<{ data: BudgetPlanResponse }>('/expenses/budgets', data).then(r => r.data.data),

  listBudgets: (month: number, year: number) =>
    api.get<{ data: BudgetPlanResponse[] }>('/expenses/budgets', { params: { month, year } }).then(r => r.data.data),

  deleteBudget: (id: number) =>
    api.delete(`/expenses/budgets/${id}`),

  getMonthlySummary: (month: number, year: number) =>
    api.get<{ data: MonthlyExpenseSummary }>('/expenses/monthly', { params: { month, year } }).then(r => r.data.data),
};
