import apiClient from '@/shared/api/axiosConfig';
import type { ApiResponse } from '@/shared/types/global.types';
import type { LoginRequest, LoginResponse, RegisterRequest, UserDto } from '../types/auth.types';

export const authApi = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const res = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', data);
    return res.data.data!;
  },

  register: async (data: RegisterRequest): Promise<UserDto> => {
    const res = await apiClient.post<ApiResponse<UserDto>>('/auth/register', data);
    return res.data.data!;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },

  me: async (): Promise<UserDto> => {
    const res = await apiClient.get<ApiResponse<UserDto>>('/auth/me');
    return res.data.data!;
  },
};
