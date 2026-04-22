import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { commitmentApi } from '../api/commitmentApi';
import type { FixedCommitmentRequest, RegisterPaymentRequest } from '../types/commitment.types';

const KEYS = {
  categories: ['commitment', 'categories'] as const,
  commitments: ['commitment', 'list'] as const,
  monthly: (m: number, y: number) => ['commitment', 'monthly', m, y] as const,
};

export const useExpenseCategories = () =>
  useQuery({ queryKey: KEYS.categories, queryFn: () => commitmentApi.listCategories('FIXED') });

export const useCommitments = () =>
  useQuery({ queryKey: KEYS.commitments, queryFn: commitmentApi.listCommitments });

export const useMonthlyCommitments = (month: number, year: number) =>
  useQuery({
    queryKey: KEYS.monthly(month, year),
    queryFn: () => commitmentApi.getMonthly(month, year),
  });

export const useCreateCommitment = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: FixedCommitmentRequest) => commitmentApi.createCommitment(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.commitments }),
  });
};

export const useUpdateCommitment = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: FixedCommitmentRequest }) =>
      commitmentApi.updateCommitment(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.commitments }),
  });
};

export const useDeleteCommitment = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => commitmentApi.deleteCommitment(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.commitments }),
  });
};

export const useRegisterPayment = (month: number, year: number) => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: RegisterPaymentRequest }) =>
      commitmentApi.registerPayment(id, month, year, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEYS.monthly(month, year) }),
  });
};
