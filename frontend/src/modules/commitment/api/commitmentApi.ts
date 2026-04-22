import apiClient from '@/shared/api/axiosConfig';
import type { ApiResponse } from '@/shared/types/global.types';
import type {
  ExpenseCategoryDto, FixedCommitmentDto, FixedCommitmentRequest,
  MonthlyCommitmentSummary, RegisterPaymentRequest, CommitmentRecordDto,
} from '../types/commitment.types';
import type { ExpenseCategoryType } from '../types/expenseCategory.types';

export const commitmentApi = {
  // Categorías de gasto
  listCategories: async (type?: ExpenseCategoryType): Promise<ExpenseCategoryDto[]> => {
    const params = type ? `?type=${type}` : '';
    const res = await apiClient.get<ApiResponse<ExpenseCategoryDto[]>>(`/commitments/categories${params}`);
    return res.data.data!;
  },

  // Compromisos fijos
  listCommitments: async (): Promise<FixedCommitmentDto[]> => {
    const res = await apiClient.get<ApiResponse<FixedCommitmentDto[]>>('/commitments');
    return res.data.data!;
  },
  createCommitment: async (data: FixedCommitmentRequest): Promise<FixedCommitmentDto> => {
    const res = await apiClient.post<ApiResponse<FixedCommitmentDto>>('/commitments', data);
    return res.data.data!;
  },
  updateCommitment: async (id: number, data: FixedCommitmentRequest): Promise<FixedCommitmentDto> => {
    const res = await apiClient.put<ApiResponse<FixedCommitmentDto>>(`/commitments/${id}`, data);
    return res.data.data!;
  },
  deleteCommitment: async (id: number): Promise<void> => {
    await apiClient.delete(`/commitments/${id}`);
  },

  // Registros mensuales
  getMonthly: async (month: number, year: number): Promise<MonthlyCommitmentSummary> => {
    const res = await apiClient.get<ApiResponse<MonthlyCommitmentSummary>>(
      `/commitments/monthly?month=${month}&year=${year}`
    );
    return res.data.data!;
  },
  registerPayment: async (
    id: number, month: number, year: number, data: RegisterPaymentRequest
  ): Promise<CommitmentRecordDto> => {
    const res = await apiClient.post<ApiResponse<CommitmentRecordDto>>(
      `/commitments/${id}/pay?month=${month}&year=${year}`, data
    );
    return res.data.data!;
  },
};
