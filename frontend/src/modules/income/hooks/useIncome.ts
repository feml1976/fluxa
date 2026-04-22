import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { incomeApi } from '../api/incomeApi';
import type { IncomeSourceRequest, UpdateIncomeRecordRequest } from '../types/income.types';

const KEYS = {
  categories: ['income', 'categories'] as const,
  sources: ['income', 'sources'] as const,
  monthly: (m: number, y: number) => ['income', 'monthly', m, y] as const,
};

export const useIncomeCategories = () =>
  useQuery({ queryKey: KEYS.categories, queryFn: incomeApi.listCategories });

export const useIncomeSources = () =>
  useQuery({ queryKey: KEYS.sources, queryFn: incomeApi.listSources });

export const useMonthlyIncome = (month: number, year: number) =>
  useQuery({
    queryKey: KEYS.monthly(month, year),
    queryFn: () => incomeApi.getMonthly(month, year),
  });

export const useCreateIncomeSource = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: IncomeSourceRequest) => incomeApi.createSource(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.sources }),
  });
};

export const useUpdateIncomeSource = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: IncomeSourceRequest }) =>
      incomeApi.updateSource(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.sources }),
  });
};

export const useDeleteIncomeSource = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => incomeApi.deleteSource(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.sources }),
  });
};

export const useUpdateIncomeRecord = (month: number, year: number) => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateIncomeRecordRequest }) =>
      incomeApi.updateRecord(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.monthly(month, year) }),
  });
};
