export const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test((v || '').trim());
export const isMobile = (v) => /^\+?\d{10,13}$/.test((v || '').replace(/[\s-]/g, ''));
export const MIN_PASSWORD = 8;

// Generic validator driven by field configs: {name,label,type,required}
export function validateFields(fields, values) {
  const errors = {};
  fields.forEach((f) => {
    const v = values[f.name];
    const empty = v === undefined || v === null || String(v).trim() === '';
    if (f.type === 'checkbox') {
      if (f.required && !v) errors[f.name] = 'You must accept this to continue.';
      return;
    }
    if (empty) {
      if (f.required) errors[f.name] = `${f.label} is required.`;
      return;
    }
    if (f.type === 'email' && !isEmail(v)) errors[f.name] = 'Enter a valid email address.';
    if (f.type === 'tel' && !isMobile(v)) errors[f.name] = 'Enter a valid mobile number (10-13 digits).';
    if (f.name === 'password' && String(v).length < MIN_PASSWORD)
      errors[f.name] = `Password must be at least ${MIN_PASSWORD} characters.`;
    if (f.name === 'confirmPassword' && v !== values.password) errors[f.name] = 'Passwords do not match.';
    if (f.type === 'number' && !(Number(v) > 0)) errors[f.name] = `${f.label} must be greater than 0.`;
  });
  return errors;
}

export function validateFarm(v) {
  const e = {};
  if (!v.name?.trim()) e.name = 'Farm name is required.';
  else if (v.name.trim().length < 3) e.name = 'Farm name must be at least 3 characters.';
  if (!v.location?.trim()) e.location = 'Location is required.';
  const area = Number(v.area);
  if (v.area === '' || Number.isNaN(area) || area <= 0) e.area = 'Area must be a number greater than 0.';
  else if (area > 100000) e.area = 'Area looks too large (max 100000).';
  if (!v.unit) e.unit = 'Select an area unit.';
  if (!v.soil) e.soil = 'Select a soil type.';
  if (!v.crop?.trim()) e.crop = 'Primary crop is required.';
  const plots = Number(v.plots);
  if (v.plots === '' || !Number.isInteger(plots) || plots < 1) e.plots = 'Plots must be a whole number of at least 1.';
  else if (plots > 1000) e.plots = 'Plots cannot exceed 1000.';
  if (!v.status) e.status = 'Select a status.';
  if ((v.notes || '').length > 500) e.notes = 'Notes must be 500 characters or fewer.';
  return e;
}
