import { useState } from 'react';
import Field from './Field';
import { validateFarm } from '../utils/validation';
import { SOILS, STATUSES, UNITS } from '../services/farmService';

const FIELDS = [
  { name: 'name', label: 'Farm name', full: true },
  { name: 'location', label: 'Location', full: true },
  { name: 'area', label: 'Farm area', type: 'number' },
  { name: 'unit', label: 'Area unit', type: 'select', options: UNITS },
  { name: 'soil', label: 'Soil type', type: 'select', options: SOILS },
  { name: 'crop', label: 'Primary crop' },
  { name: 'plots', label: 'Number of plots', type: 'number' },
  { name: 'status', label: 'Farm status', type: 'select', options: STATUSES },
  { name: 'notes', label: 'Notes / description', type: 'textarea', full: true },
];
const EMPTY = { name: '', location: '', area: '', unit: 'acres', soil: '', crop: '', plots: '', status: 'active', notes: '' };

export default function FarmForm({ initial, onSubmit, onCancel }) {
  const [values, setValues] = useState({ ...EMPTY, ...(initial || {}) });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState('');

  const change = (n, v) => { setValues((p) => ({ ...p, [n]: v })); setErrors((p) => ({ ...p, [n]: undefined })); };
  async function submit(e) {
    e.preventDefault();
    const errs = validateFarm(values);
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true); setFormError('');
    try { await onSubmit(values); } catch (err) { setFormError(err.message); setBusy(false); }
  }
  return (
    <form onSubmit={submit} noValidate>
      {formError && <div className="alert error">{formError}</div>}
      <div className="grid-form">
        {FIELDS.map((f) => <Field key={f.name} field={f} value={values[f.name]} error={errors[f.name]} onChange={change} />)}
      </div>
      <div className="form-actions">
        <button type="button" className="btn ghost" onClick={onCancel}>Cancel</button>
        <button type="submit" className="btn primary" disabled={busy}>{busy ? 'Saving…' : initial ? 'Save changes' : 'Add farm'}</button>
      </div>
    </form>
  );
}
