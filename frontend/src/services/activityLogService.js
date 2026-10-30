
import { request } from './api';
import { getToken } from './authService';

export async function getActivityLogs({
  userId = '',
  actionType = '',
  startDate = '',
  endDate = '',
  page = 0,
  size = 10,
} = {}) {
  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  if (userId) params.append('userId', userId);
  if (actionType) params.append('actionType', actionType);
  if (startDate) params.append('startDate', startDate);
  if (endDate) params.append('endDate', endDate);

  return request(`/api/admin/activity-logs?${params}`, { token });
}

export async function getActivityLogById(id) {
  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  return request(`/api/admin/activity-logs/${id}`, { token });
}
