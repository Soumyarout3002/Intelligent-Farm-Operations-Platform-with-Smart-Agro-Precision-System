
import { request } from './api';
import { getToken } from './authService';

function authToken() {
  const token = getToken();

  if (!token) {
    throw new Error('Please log in to use GIS Farm Mapping.');
  }

  return token;
}

// Fetch farms and their plot/boundary information for the logged-in user.
export async function listGisFarms() {
  const result = await request('/api/gis/farms', {
    token: authToken(),
  });

  return Array.isArray(result) ? result : [];
}

// Fetch one farm with its GIS plot information.
export async function getGisFarm(farmId) {
  return request(`/api/gis/farms/${farmId}`, {
    token: authToken(),
  });
}

// Save a new plot boundary.
// Coordinates must be [longitude, latitude] pairs.
export async function createPlotBoundary(farmId, plotId, coordinates) {
  return request(
    `/api/farms/${farmId}/plots/${plotId}/boundary`,
    {
      method: 'POST',
      body: { coordinates },
      token: authToken(),
    }
  );
}

// Update an existing plot boundary.
export async function updatePlotBoundary(farmId, plotId, coordinates) {
  return request(
    `/api/farms/${farmId}/plots/${plotId}/boundary`,
    {
      method: 'PUT',
      body: { coordinates },
      token: authToken(),
    }
  );
}

// Fetch the saved boundary of a plot.
export async function getPlotBoundary(farmId, plotId) {
  return request(
    `/api/farms/${farmId}/plots/${plotId}/boundary`,
    {
      token: authToken(),
    }
  );
}

// Delete a saved plot boundary.
export async function deletePlotBoundary(farmId, plotId) {
  return request(
    `/api/farms/${farmId}/plots/${plotId}/boundary`,
    {
      method: 'DELETE',
      token: authToken(),
    }
  );
}
