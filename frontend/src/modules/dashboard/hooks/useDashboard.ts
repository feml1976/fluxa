import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/dashboardApi';

export const useDashboardSummary = (month: number, year: number) =>
  useQuery({
    queryKey: ['dashboard', month, year],
    queryFn: () => dashboardApi.getSummary(month, year),
  });

export const useDashboardProjections = (months: number) =>
  useQuery({
    queryKey: ['dashboard', 'projections', months],
    queryFn: () => dashboardApi.getProjections(months),
  });

export const useDebtStrategies = () =>
  useQuery({
    queryKey: ['dashboard', 'debt-strategy'],
    queryFn: dashboardApi.getDebtStrategies,
  });
