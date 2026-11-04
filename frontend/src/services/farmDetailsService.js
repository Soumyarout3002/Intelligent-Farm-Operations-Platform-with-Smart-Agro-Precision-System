
import { API_BASE_URL } from './api';
import { getToken } from './authService';
import { request } from './api';

const token = () => {
  const value = getToken();
  if (!value) throw new Error('Please log in again.');
  return value;
};

// Plot operations
export const listPlots = (farmId) =>
  request(`/api/farms/${farmId}/plots`, { token: token() });

export const createPlot = (farmId, data) =>
  request(`/api/farms/${farmId}/plots`, {
    method: 'POST',
    body: data,
    token: token(),
  });

export const updatePlot = (farmId, plotId, data) =>
  request(`/api/farms/${farmId}/plots/${plotId}`, {
    method: 'PUT',
    body: data,
    token: token(),
  });

export const deletePlot = (farmId, plotId) =>
  request(`/api/farms/${farmId}/plots/${plotId}`, {
    method: 'DELETE',
    token: token(),
  });

// Soil record operations
export const listSoilRecords = (farmId, plotId) =>
  request(`/api/farms/${farmId}/plots/${plotId}/soil-records`, {
    token: token(),
  });

export const createSoilRecord = (farmId, plotId, data) =>
  request(`/api/farms/${farmId}/plots/${plotId}/soil-records`, {
    method: 'POST',
    body: data,
    token: token(),
  });

// NEW: Update an existing soil record
export const updateSoilRecord = (farmId, plotId, recordId, data) =>
  request(
    `/api/farms/${farmId}/plots/${plotId}/soil-records/${recordId}`,
    {
      method: 'PUT',
      body: data,
      token: token(),
    }
  );

// Document operations
export const listDocuments = (farmId) =>
  request(`/api/farms/${farmId}/documents`, { token: token() });

export const uploadDocument = async (farmId, file, description = '') => {
  const form = new FormData();
  form.append('file', file);
  form.append('description', description);

  const response = await fetch(
    `${API_BASE_URL}/api/farms/${farmId}/documents`,
    {
      method: 'POST',
      headers: { Authorization: `Bearer ${token()}` },
      body: form,
    }
  );

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.message || `Upload failed (${response.status})`);
  }

  return data;
};

export async function downloadDocument(
  farmId,
  documentId,
  fileName = 'farm-document'
) {
  const response = await fetch(
    `${API_BASE_URL}/api/farms/${farmId}/documents/${documentId}/download`,
    {
      headers: { Authorization: `Bearer ${token()}` },
    }
  );

  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.message || `Download failed (${response.status})`);
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');

  anchor.href = url;
  anchor.download = fileName;

  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();

  URL.revokeObjectURL(url);
}

export const deleteDocument = (farmId, documentId) =>
  request(`/api/farms/${farmId}/documents/${documentId}`, {
    method: 'DELETE',
    token: token(),
  });

// Field worker operations
export const listWorkers = (farmId) =>
  request(`/api/farms/${farmId}/workers`, { token: token() });

export const listAvailableWorkers = (farmId) =>
  request(`/api/farms/${farmId}/workers/available`, { token: token() });

export const assignWorker = (farmId, data) =>
  request(`/api/farms/${farmId}/workers`, {
    method: 'POST',
    body: data,
    token: token(),
  });

export const removeWorker = (farmId, assignmentId) =>
  request(`/api/farms/${farmId}/workers/${assignmentId}`, {
    method: 'DELETE',
    token: token(),
  });
