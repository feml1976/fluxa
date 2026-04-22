import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/dashboardApi';

export const useDashboardSummary = (month: number, year: number) =>
  useQuery({
    queryKey: ['dashboard', month, year],
    queryFn: () => dashboardApi.getSummary(month, year),
  });
