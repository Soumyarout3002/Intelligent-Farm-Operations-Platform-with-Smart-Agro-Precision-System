
import { request } from './api';
import { getToken } from './authService';

function authToken() {
  const token = getToken();

  if (!token) {
    throw new Error('Please log in to use Crop Lifecycle Tracking.');
  }

  return token;
}

const basePath = (farmId, plotId) =>
  `/api/farms/${farmId}/plots/${plotId}/crop-cycles`;

// Crop cycle operations
export async function listCropCycles(farmId, plotId) {
  const result = await request(basePath(farmId, plotId), {
    token: authToken(),
  });

  return Array.isArray(result) ? result : [];
}

export async function getCropCycle(farmId, plotId, cycleId) {
  return request(`${basePath(farmId, plotId)}/${cycleId}`, {
    token: authToken(),
  });
}

export async function createCropCycle(farmId, plotId, data) {
  return request(basePath(farmId, plotId), {
    method: 'POST',
    body: data,
    token: authToken(),
  });
}

export async function updateCropCycle(farmId, plotId, cycleId, data) {
  return request(`${basePath(farmId, plotId)}/${cycleId}`, {
    method: 'PUT',
    body: data,
    token: authToken(),
  });
}

export async function deleteCropCycle(farmId, plotId, cycleId) {
  return request(`${basePath(farmId, plotId)}/${cycleId}`, {
    method: 'DELETE',
    token: authToken(),
  });
}

// Growth stage operations
export async function listGrowthStages(farmId, plotId, cycleId) {
  const result = await request(
    `${basePath(farmId, plotId)}/${cycleId}/growth-stages`,
    { token: authToken() }
  );

  return Array.isArray(result) ? result : [];
}

export async function createGrowthStage(farmId, plotId, cycleId, data) {
  return request(
    `${basePath(farmId, plotId)}/${cycleId}/growth-stages`,
    {
      method: 'POST',
      body: data,
      token: authToken(),
    }
  );
}

export async function updateGrowthStage(
  farmId, plotId, cycleId, stageId, data
) {
  return request(
    `${basePath(farmId, plotId)}/${cycleId}/growth-stages/${stageId}`,
    {
      method: 'PUT',
      body: data,
      token: authToken(),
    }
  );
}

export async function deleteGrowthStage(
  farmId, plotId, cycleId, stageId
) {
  return request(
    `${basePath(farmId, plotId)}/${cycleId}/growth-stages/${stageId}`,
    {
      method: 'DELETE',
      token: authToken(),
    }
  );
}

// Crop activity operations
export async function listCropActivities(farmId, plotId, cycleId) {
  const result = await request(
    `${basePath(farmId, plotId)}/${cycleId}/activities`,
    { token: authToken() }
  );

  return Array.isArray(result) ? result : [];
}

export async function createCropActivity(farmId, plotId, cycleId, data) {
  return request(
    `${basePath(farmId, plotId)}/${cycleId}/activities`,
    {
      method: 'POST',
      body: data,
      token: authToken(),
    }
  );
}

export async function updateCropActivity(
  farmId, plotId, cycleId, activityId, data
) {
  return request(
    `${basePath(farmId, plotId)}/${cycleId}/activities/${activityId}`,
    {
      method: 'PUT',
      body: data,
      token: authToken(),
    }
  );
}

export async function deleteCropActivity(
  farmId, plotId, cycleId, activityId
) {
  return request(
    `${basePath(farmId, plotId)}/${cycleId}/activities/${activityId}`,
    {
      method: 'DELETE',
      token: authToken(),
    }
  );
}
