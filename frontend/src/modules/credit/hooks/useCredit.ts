import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { creditApi } from '../api/creditApi';
import type { CreditPaymentRequest, CreditRequest } from '../types/credit.types';

export const useCreditList = () =>
  useQuery({ queryKey: ['credits'], queryFn: creditApi.list });

export const useCreditSummary = () =>
  useQuery({ queryKey: ['credits', 'summary'], queryFn: creditApi.getSummary });

export const useCreditById = (id: number) =>
  useQuery({ queryKey: ['credits', id], queryFn: () => creditApi.getById(id), enabled: id > 0 });

export const useCreditAnalysis = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: ['credits', id, 'analysis'],
    queryFn: () => creditApi.analyze(id),
    enabled: enabled && id > 0,
  });

export const useCreditPayments = (id: number, enabled: boolean) =>
  useQuery({
    queryKey: ['credits', id, 'payments'],
    queryFn: () => creditApi.listPayments(id),
    enabled: enabled && id > 0,
  });

export const useCreateCredit = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreditRequest) => creditApi.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['credits'] }),
  });
};

export const useUpdateCredit = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: CreditRequest }) => creditApi.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['credits'] }),
  });
};

export const useDeleteCredit = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => creditApi.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['credits'] }),
  });
};

export const useRegisterCreditPayment = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: CreditPaymentRequest }) =>
      creditApi.registerPayment(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['credits'] }),
  });
};
