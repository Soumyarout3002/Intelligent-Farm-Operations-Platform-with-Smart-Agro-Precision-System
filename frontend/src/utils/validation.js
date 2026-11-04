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

  if (!v.name?.trim()) {
    e.name = 'Farm name is required.';
  } else if (v.name.trim().length < 3) {
    e.name = 'Farm name must be at least 3 characters.';
  } else if (v.name.trim().length > 150) {
    e.name = 'Farm name cannot exceed 150 characters.';
  }

  if (!v.location?.trim()) {
    e.location = 'Location is required.';
  } else if (v.location.trim().length > 255) {
    e.location = 'Location cannot exceed 255 characters.';
  }

  const area = Number(v.area);

  if (v.area === '' || Number.isNaN(area) || area < 0.01) {
    e.area = 'Area must be at least 0.01.';
  } else if (area > 9999999999.99) {
    e.area = 'Area is too large.';
  } else if (!/^\d+(\.\d{1,2})?$/.test(String(v.area))) {
    e.area = 'Area can have at most 2 decimal places.';
  }

  if (!v.unit || !['acres', 'hectares', 'sq meters'].includes(v.unit)) {
    e.unit = 'Select a valid area unit.';
  }

  if (v.soil && v.soil.length > 50) {
    e.soil = 'Soil type cannot exceed 50 characters.';
  }

  if (!v.crop?.trim()) {
    e.crop = 'Primary crop is required.';
  } else if (v.crop.trim().length > 100) {
    e.crop = 'Primary crop cannot exceed 100 characters.';
  }

  if ((v.notes || '').length > 500) {
    e.notes = 'Notes must be 500 characters or fewer.';
  }

  return e;
}

