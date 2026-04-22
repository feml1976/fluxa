import api from '../../../shared/api/axiosConfig';
import type {
  CreditAnalysisResponse,
  CreditPaymentRequest,
  CreditPaymentResponse,
  CreditRequest,
  CreditResponse,
  CreditSummaryResponse,
} from '../types/credit.types';

export const creditApi = {
  create: (data: CreditRequest) =>
    api.post<{ data: CreditResponse }>('/credits', data).then(r => r.data.data),

  list: () =>
    api.get<{ data: CreditResponse[] }>('/credits').then(r => r.data.data),

  getSummary: () =>
    api.get<{ data: CreditSummaryResponse }>('/credits/summary').then(r => r.data.data),

  getById: (id: number) =>
    api.get<{ data: CreditResponse }>(`/credits/${id}`).then(r => r.data.data),

  update: (id: number, data: CreditRequest) =>
    api.put<{ data: CreditResponse }>(`/credits/${id}`, data).then(r => r.data.data),

  delete: (id: number) =>
    api.delete(`/credits/${id}`),

  analyze: (id: number) =>
    api.get<{ data: CreditAnalysisResponse }>(`/credits/${id}/analysis`).then(r => r.data.data),

  registerPayment: (id: number, data: CreditPaymentRequest) =>
    api.post<{ data: CreditPaymentResponse }>(`/credits/${id}/payments`, data).then(r => r.data.data),

  listPayments: (id: number) =>
    api.get<{ data: CreditPaymentResponse[] }>(`/credits/${id}/payments`).then(r => r.data.data),
};
