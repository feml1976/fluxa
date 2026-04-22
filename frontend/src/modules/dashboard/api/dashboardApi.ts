import api from '../../../shared/api/axiosConfig';
import type { DashboardSummaryResponse } from '../types/dashboard.types';

export const dashboardApi = {
  getSummary: (month: number, year: number) =>
    api.get<{ data: DashboardSummaryResponse }>('/dashboard', { params: { month, year } })
      .then(r => r.data.data),
};
