import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { expenseApi } from '../api/expenseApi';
import type { BudgetPlanRequest, VariableExpenseRequest } from '../types/expense.types';

export const useMonthlyExpense = (month: number, year: number) =>
  useQuery({
    queryKey: ['expenses', 'monthly', month, year],
    queryFn: () => expenseApi.getMonthlySummary(month, year),
  });

export const useCreateExpense = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: VariableExpenseRequest) => expenseApi.createExpense(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expenses'] }),
  });
};

export const useUpdateExpense = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: VariableExpenseRequest }) =>
      expenseApi.updateExpense(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expenses'] }),
  });
};

export const useDeleteExpense = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => expenseApi.deleteExpense(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expenses'] }),
  });
};

export const useSaveOrUpdateBudget = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: BudgetPlanRequest) => expenseApi.saveOrUpdateBudget(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expenses'] }),
  });
};

export const useDeleteBudget = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => expenseApi.deleteBudget(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['expenses'] }),
  });
};
