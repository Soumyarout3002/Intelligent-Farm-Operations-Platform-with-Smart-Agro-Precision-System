import { request } from './api';
import { getToken } from './authService';

const token = () => {
  const value = getToken();
  if (!value) throw new Error('Please log in to access irrigation.');
  return value;
};

export const getIrrigationSchedules = (farmId) =>
  request(`/api/farms/${farmId}/irrigation/schedules`, { token: token() });

export const getIrrigationSuggestions = (farmId) =>
  request(`/api/farms/${farmId}/irrigation/suggestions`, { token: token() });

export const createIrrigationSchedule = (farmId, body) =>
  request(`/api/farms/${farmId}/irrigation/schedules`, {
    method: 'POST', body, token: token(),
  });

export const recordIrrigation = (farmId, scheduleId, body) =>
  request(`/api/farms/${farmId}/irrigation/schedules/${scheduleId}/record`, {
    method: 'POST', body, token: token(),
  });
