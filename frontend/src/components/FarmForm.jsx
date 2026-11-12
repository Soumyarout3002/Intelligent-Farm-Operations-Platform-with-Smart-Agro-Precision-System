
import { useState } from 'react';
import { Sprout } from 'lucide-react';
import Field from './Field';
import { validateFarm } from '../utils/validation';
import { SOILS, UNITS } from '../services/farmService';

const FIELDS = [
  { name: 'name', label: 'Farm name', full: true, required: true },
  { name: 'location', label: 'Location', full: true, required: true },
  { name: 'area', label: 'Farm area', type: 'number', required: true },
  { name: 'unit', label: 'Area unit', type: 'select', options: UNITS, required: true },
  { name: 'soil', label: 'Soil type', type: 'select', options: SOILS },
  { name: 'crop', label: 'Primary crop', required: true },
  { name: 'latitude', label: 'Latitude', type: 'number' },
  { name: 'longitude', label: 'Longitude', type: 'number' },
  { name: 'notes', label: 'Notes / description', type: 'textarea', full: true },
];

const EMPTY = {
  name: '',
  location: '',
  area: '',
  unit: 'acres',
  soil: '',
  crop: '',
  latitude: '',
  longitude: '',
  notes: '',
};

export default function FarmForm({ initial, onSubmit, onCancel }) {
  const [values, setValues] = useState({ ...EMPTY, ...(initial || {}) });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState('');

  const change = (n, v) => {
    setValues((p) => ({ ...p, [n]: v }));
    setErrors((p) => ({ ...p, [n]: undefined }));
  };

  async function submit(e) {
    e.preventDefault();

    const errs = validateFarm(values);

    if (values.latitude !== '' && values.latitude != null) {
      const latitude = Number(values.latitude);
      if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90) {
        errs.latitude = 'Latitude must be between -90 and 90';
      }
    }

    if (values.longitude !== '' && values.longitude != null) {
      const longitude = Number(values.longitude);
      if (!Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
        errs.longitude = 'Longitude must be between -180 and 180';
      }
    }

    if ((values.latitude === '') !== (values.longitude === '')) {
      errs.latitude = 'Enter both latitude and longitude';
      errs.longitude = 'Enter both latitude and longitude';
    }

    setErrors(errs);

    if (Object.keys(errs).length) return;

    setBusy(true);
    setFormError('');

    try {
      await onSubmit(values);
    } catch (err) {
      setFormError(err.message);
      setBusy(false);
    }
  }

  return (
    <>
      <style>{`
        .farm-form {
          background: #f0f8ef;
          border: 1px solid #d5e9d2;
          border-radius: 16px;
          padding: 22px;
          color: #183d27;
        }

        .farm-form-header {
          display: flex;
          align-items: center;
          gap: 13px;
          background: #e0f1dc;
          border: 1px solid #cce4c7;
          border-radius: 12px;
          padding: 15px 18px;
          margin-bottom: 22px;
        }

        .farm-form-icon {
          width: 42px;
          height: 42px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #238342;
          color: white;
          flex-shrink: 0;
        }

        .farm-form-header h3 {
          margin: 0;
          font-size: 19px;
          color: #174b2a;
        }

        .farm-form-header p {
          margin: 4px 0 0;
          color: #54745b;
          font-size: 13px;
        }

        .farm-form .grid-form {
          gap: 17px;
        }

        .farm-form label {
          color: #244e31;
          font-weight: 600;
        }

        .farm-form input:not([type="checkbox"]),
        .farm-form select,
        .farm-form textarea {
          background: #ffffff;
          border: 1px solid #bfd8bd;
          border-radius: 9px;
          color: #243b2b;
          transition: border-color 0.2s, box-shadow 0.2s;
        }

        .farm-form input:focus,
        .farm-form select:focus,
        .farm-form textarea:focus {
          outline: none;
          border-color: #278747;
          box-shadow: 0 0 0 3px rgba(39, 135, 71, 0.12);
        }

        .farm-form input::placeholder,
        .farm-form textarea::placeholder {
          color: #91a398;
        }

        .farm-form .form-actions {
          display: flex;
          justify-content: flex-end;
          gap: 12px;
          margin-top: 24px;
          padding-top: 18px;
          border-top: 1px solid #d5e8d2;
        }

        .farm-form .btn.primary {
          background: #218343;
          border-color: #218343;
          color: white;
        }

        .farm-form .btn.primary:hover {
          background: #176b34;
          border-color: #176b34;
        }

        .farm-form .btn.ghost {
          background: #ffffff;
          color: #28633a;
          border: 1px solid #9fc7a1;
        }

        .farm-form .btn.ghost:hover {
          background: #e2f1df;
        }

        .farm-form .alert.error {
          margin-bottom: 16px;
        }

        @media (max-width: 600px) {
          .farm-form {
            padding: 14px;
          }

          .farm-form .form-actions {
            flex-wrap: wrap;
          }
        }
      `}</style>

      <form className="farm-form" onSubmit={submit} noValidate>
        <div className="farm-form-header">
          <div className="farm-form-icon">
            <Sprout size={23} />
          </div>

          <div>
            <h3>{initial ? 'Edit farm details' : 'Add farm'}</h3>
            <p>
              {initial
                ? 'Update your farm information'
                : 'Register a new farm in FarmVerse Nexus'}
            </p>
          </div>
        </div>

        {formError && (
          <div className="alert error" role="alert">
            {formError}
          </div>
        )}

        <div className="grid-form">
          {FIELDS.map((f) => (
            <Field
              key={f.name}
              field={f}
              value={values[f.name]}
              error={errors[f.name]}
              onChange={change}
            />
          ))}
        </div>

        <div className="form-actions">
          <button type="button" className="btn ghost" onClick={onCancel}>
            Cancel
          </button>

          <button type="submit" className="btn primary" disabled={busy}>
            {busy
              ? 'Saving...'
              : initial
                ? 'Save changes'
                : 'Add farm'}
          </button>
        </div>
      </form>
    </>
  );
}
