
import { request } from './api';
import { getToken } from './authService';

export const SOILS = [
  'Alluvial',
  'Black',
  'Red',
  'Loamy',
  'Sandy',
  'Clay',
  'Laterite',
];

export const UNITS = ['acres', 'hectares', 'sq meters'];

// The backend currently creates farms with ACTIVE status.
export const STATUSES = ['active'];

function getAuthToken() {
  const token = getToken();

  if (!token) {
    throw new Error('Please log in to manage your farms.');
  }

  return token;
}

// Convert backend response into the field names used by the frontend.
function mapFarm(farm) {
  const unitMap = {
    ACRE: 'acres',
    HECTARE: 'hectares',
    SQ_METER: 'sq meters',
  };

  return {
    id: farm.id,
    name: farm.name,
    location: farm.location,
    area: Number(farm.area),
    unit: unitMap[farm.areaUnit] || farm.areaUnit,
    soil: farm.soilType || '',
    crop: farm.primaryCrop || '',
    status: (farm.status || 'ACTIVE').toLowerCase(),
    notes: farm.notes || '',
    createdAt: farm.createdAt,
    updatedAt: farm.updatedAt,
    ownerId: farm.ownerId,
    ownerName: farm.ownerName,
    plots: Array.isArray(farm.plots) ? farm.plots.length : (farm.plotCount ?? 0),
  };
}

// Convert frontend form fields into the backend request format.
function toApiPayload(data) {
  const unitMap = {
    acres: 'ACRE',
    hectares: 'HECTARE',
    'sq meters': 'SQ_METER',
  };

  return {
    name: data.name.trim(),
    location: data.location.trim(),
    area: Number(data.area),
    areaUnit: unitMap[data.unit] || data.unit,
    soilType: data.soil || null,
    primaryCrop: data.crop.trim(),
    notes: data.notes?.trim() || '',
  };
}

export async function listFarms() {
  const farms = await request('/api/farms', {
    token: getAuthToken(),
  });

  const mapped = farms.map(mapFarm);
  return Promise.all(mapped.map(async (farm) => {
    try {
      const plots = await request(`/api/farms/${farm.id}/plots`, { token: getAuthToken() });
      return { ...farm, plots: Array.isArray(plots) ? plots.length : 0 };
    } catch {
      return { ...farm, plots: 0 };
    }
  }));
}

export async function getFarm(id) {
  const farm = await request(`/api/farms/${id}`, {
    token: getAuthToken(),
  });

  return mapFarm(farm);
}

export async function createFarm(data) {
  const farm = await request('/api/farms', {
    method: 'POST',
    body: toApiPayload(data),
    token: getAuthToken(),
  });

  return mapFarm(farm);
}

export async function updateFarm(id, data) {
  const farm = await request(`/api/farms/${id}`, {
    method: 'PUT',
    body: toApiPayload(data),
    token: getAuthToken(),
  });

  return mapFarm(farm);
}

export async function deleteFarm(id) {
  return request(`/api/farms/${id}`, {
    method: 'DELETE',
    token: getAuthToken(),
  });
}
