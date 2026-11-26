import { request } from './api';
import { getToken } from './authService';

// Real backend: /api/farms/{farmId}/irrigation/... (see IrrigationController).
function token() {
  const t = getToken();
  if (!t) throw new Error('Please log in to manage irrigation.');
  return t;
}
const base = (farmId) => `/api/farms/${farmId}/irrigation`;

export const listSchedules = (farmId) => request(`${base(farmId)}/schedules`, { token: token() });
export const createSchedule = (farmId, body) => request(`${base(farmId)}/schedules`, { method: 'POST', body, token: token() });
export const updateSchedule = (farmId, id, body) => request(`${base(farmId)}/schedules/${id}`, { method: 'PUT', body, token: token() });
export const deleteSchedule = (farmId, id) => request(`${base(farmId)}/schedules/${id}`, { method: 'DELETE', token: token() });
// action: 'COMPLETED' | 'SKIPPED'
export const recordIrrigation = (farmId, id, body) => request(`${base(farmId)}/schedules/${id}/record`, { method: 'POST', body, token: token() });
export const listSuggestions = (farmId) => request(`${base(farmId)}/suggestions`, { token: token() });
