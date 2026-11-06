// DEMO farm store (LocalStorage). Backend: GET/POST /farms, GET/PUT/DELETE /farms/:id
import { read, write } from '../utils/storage';
import { validateFarm } from '../utils/validation';

const KEY = 'fvn_farms';
const COUNTER = 'fvn_farm_counter';

export const SOILS = ['Alluvial', 'Black', 'Red', 'Loamy', 'Sandy', 'Clay', 'Laterite'];
export const STATUSES = ['active', 'inactive', 'fallow'];
export const UNITS = ['acres', 'hectares'];

const seed = [
  ['Green Valley Farm', 'Mandya, Karnataka', 24, 'acres', 'Red', 'Sugarcane', 6, 'active'],
  ['Sunrise Orchards', 'Nashik, Maharashtra', 12.5, 'hectares', 'Black', 'Grapes', 4, 'active'],
  ['Riverbend Paddy Fields', 'Thanjavur, Tamil Nadu', 40, 'acres', 'Alluvial', 'Rice', 10, 'active'],
  ['Highland Tea Estate', 'Munnar, Kerala', 18, 'hectares', 'Loamy', 'Tea', 5, 'inactive'],
  ['Dry Meadow Plot', 'Anantapur, Andhra Pradesh', 8, 'acres', 'Sandy', 'Groundnut', 2, 'fallow'],
].map((r, i) => ({ id: `FV-${String(i + 1).padStart(4, '0')}`, name: r[0], location: r[1], area: r[2], unit: r[3], soil: r[4],
  crop: r[5], plots: r[6], status: r[7], notes: 'Sample demo farm.', createdAt: new Date(Date.now() - (i + 1) * 864e5 * 9).toISOString() }));

function load() {
  let farms = read(KEY, null);
  if (!Array.isArray(farms)) { farms = seed; write(KEY, farms); write(COUNTER, seed.length); }
  return farms;
}
const dup = (farms, d, ignoreId) =>
  farms.some((f) => f.id !== ignoreId && f.name.trim().toLowerCase() === d.name.trim().toLowerCase() && f.location.trim().toLowerCase() === d.location.trim().toLowerCase());

const clean = (d) => ({ name: d.name.trim(), location: d.location.trim(), area: Number(d.area), unit: d.unit, soil: d.soil,
  crop: d.crop.trim(), plots: Number(d.plots), status: d.status, notes: (d.notes || '').trim() });

export const listFarms = async () => load();
export const getFarm = async (id) => load().find((f) => f.id === id) || null;

export async function createFarm(d) {
  if (Object.keys(validateFarm(d)).length) throw new Error('Please fix the invalid farm fields.');
  const farms = load();
  if (dup(farms, d)) throw new Error('A farm with this name and location already exists.');
  const n = Number(read(COUNTER, farms.length)) + 1;
  const farm = { id: `FV-${String(n).padStart(4, '0')}`, ...clean(d), createdAt: new Date().toISOString() };
  write(COUNTER, n);
  write(KEY, [farm, ...farms]);
  return farm;
}
export async function updateFarm(id, d) {
  if (Object.keys(validateFarm(d)).length) throw new Error('Please fix the invalid farm fields.');
  const farms = load();
  if (!farms.some((f) => f.id === id)) throw new Error('Farm not found.');
  if (dup(farms, d, id)) throw new Error('Another farm with this name and location already exists.');
  let out;
  write(KEY, farms.map((f) => (f.id === id ? (out = { ...f, ...clean(d) }) : f)));
  return out;
}
export async function deleteFarm(id) {
  const farms = load();
  if (!farms.some((f) => f.id === id)) throw new Error('Farm not found.');
  write(KEY, farms.filter((f) => f.id !== id));
}
