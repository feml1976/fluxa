import api from '../../../shared/api/axiosConfig';
import type { NotificationLog } from '../types/notification.types';

export const notificationApi = {
  sendTest: () =>
    api.post<{ message: string }>('/notifications/test').then(r => r.data),

  getLogs: () =>
    api.get<{ data: NotificationLog[] }>('/notifications/logs').then(r => r.data.data),
};
