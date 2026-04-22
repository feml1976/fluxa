import apiClient from '@/shared/api/axiosConfig';
import type { ApiResponse } from '@/shared/types/global.types';
import type {
  IncomeCategoryDto, IncomeSourceDto, IncomeSourceRequest,
  MonthlyIncomeSummary, UpdateIncomeRecordRequest, IncomeRecordDto,
} from '../types/income.types';

export const incomeApi = {
  // Categorías
  listCategories: async (): Promise<IncomeCategoryDto[]> => {
    const res = await apiClient.get<ApiResponse<IncomeCategoryDto[]>>('/income/categories');
    return res.data.data!;
  },

  // Fuentes
  listSources: async (): Promise<IncomeSourceDto[]> => {
    const res = await apiClient.get<ApiResponse<IncomeSourceDto[]>>('/income/sources');
    return res.data.data!;
  },
  createSource: async (data: IncomeSourceRequest): Promise<IncomeSourceDto> => {
    const res = await apiClient.post<ApiResponse<IncomeSourceDto>>('/income/sources', data);
    return res.data.data!;
  },
  updateSource: async (id: number, data: IncomeSourceRequest): Promise<IncomeSourceDto> => {
    const res = await apiClient.put<ApiResponse<IncomeSourceDto>>(`/income/sources/${id}`, data);
    return res.data.data!;
  },
  deleteSource: async (id: number): Promise<void> => {
    await apiClient.delete(`/income/sources/${id}`);
  },

  // Registros mensuales
  getMonthly: async (month: number, year: number): Promise<MonthlyIncomeSummary> => {
    const res = await apiClient.get<ApiResponse<MonthlyIncomeSummary>>(
      `/income/monthly?month=${month}&year=${year}`
    );
    return res.data.data!;
  },
  updateRecord: async (id: number, data: UpdateIncomeRecordRequest): Promise<IncomeRecordDto> => {
    const res = await apiClient.put<ApiResponse<IncomeRecordDto>>(`/income/records/${id}`, data);
    return res.data.data!;
  },
};
