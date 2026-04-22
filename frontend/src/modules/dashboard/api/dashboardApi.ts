import api from '../../../shared/api/axiosConfig';
import type {
  DashboardSummaryResponse,
  DebtStrategyResponse,
  ProjectionsResponse,
} from '../types/dashboard.types';

export const dashboardApi = {
  getSummary: (month: number, year: number) =>
    api.get<{ data: DashboardSummaryResponse }>('/dashboard', { params: { month, year } })
      .then(r => r.data.data),

  getProjections: (months: number) =>
    api.get<{ data: ProjectionsResponse }>('/dashboard/projections', { params: { months } })
      .then(r => r.data.data),

  getDebtStrategies: () =>
    api.get<{ data: DebtStrategyResponse[] }>('/dashboard/debt-strategy')
      .then(r => r.data.data),
};
