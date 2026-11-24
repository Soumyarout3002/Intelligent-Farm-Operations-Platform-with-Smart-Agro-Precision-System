import { request } from './api';
import { getToken } from './authService';

// Real backend: /api/analytics/* (see AnalyticsController). Only supported filters are sent.
function token() {
  const t = getToken();
  if (!t) throw new Error('Please log in to view analytics.');
  return t;
}
function qs(params) {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && String(v).trim() !== '') p.set(k, v); });
  const s = p.toString();
  return s ? `?${s}` : '';
}
const get = (path, params) => request(`/api/analytics/${path}${qs(params)}`, { token: token() });

export const getDashboard = (f) => get('dashboard', { farmId: f.farmId, cropName: f.cropName, startDate: f.startDate, endDate: f.endDate });
export const getYield = (f) => get('yield', { farmId: f.farmId, cropName: f.cropName, startDate: f.startDate, endDate: f.endDate });
export const getCostProfit = (f) => get('cost-profit', { farmId: f.farmId, cropName: f.cropName, startDate: f.startDate, endDate: f.endDate });
export const getWaterUsage = (f) => get('water-usage', { farmId: f.farmId, startDate: f.startDate, endDate: f.endDate });
export const getMachinery = (f) => get('machinery', { farmId: f.farmId, cropName: f.cropName, startDate: f.startDate, endDate: f.endDate });
