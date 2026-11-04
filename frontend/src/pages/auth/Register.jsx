
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Tractor, HardHat, ArrowLeft } from 'lucide-react';
import AuthShell from './AuthShell';
import Field from '../../components/Field';
import { useAuth } from '../../context/AuthContext';
import { validateFields } from '../../utils/validation';
import { UNITS } from '../../services/farmService';

const terms = {
  name: 'terms',
  label: 'I accept the Terms & Conditions and Privacy Policy',
  type: 'checkbox',
  required: true,
};

const pw = [
  { name: 'password', label: 'Password (min 8 chars)', type: 'password', required: true },
  { name: 'confirmPassword', label: 'Confirm password', type: 'password', required: true },
];

const base = [
  { name: 'name', label: 'Full name', required: true },
  { name: 'email', label: 'Email', type: 'email', required: true },
];

const mobile = {
  name: 'mobile',
  label: 'Mobile number',
  type: 'tel',
  required: true,
};

const languages = ['English', 'Hindi', 'Kannada', 'Tamil', 'Telugu', 'Marathi'];

const ROLE_CONFIG = {
  farmer: {
    title: 'Farmer',
    icon: Tractor,
    desc: 'Manage your own farm and crops.',
    fields: [
      ...base,
      mobile,
      ...pw,
      { name: 'farmName', label: 'Farm name', required: true },
      { name: 'farmLocation', label: 'Farm location', required: true },
      { name: 'farmArea', label: 'Farm area', type: 'number', required: true },
      { name: 'farmUnit', label: 'Area unit', type: 'select', options: UNITS, required: true },
      { name: 'primaryCrop', label: 'Primary crop', required: true },
      { name: 'language', label: 'Preferred language', type: 'select', options: languages, required: true },
      terms,
    ],
  },

  field_worker: {
    title: 'Field Worker',
    icon: HardHat,
    desc: 'Carry out assigned farm operations and field tasks.',
    fields: [
      ...base,
      mobile,
      ...pw,
      {
        name: 'language',
        label: 'Preferred language',
        type: 'select',
        options: languages,
        required: true,
      },
      terms,
    ],
  },
};

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [role, setRole] = useState(null);
  const [values, setValues] = useState({});
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const pick = (selectedRole) => {
    setRole(selectedRole);
    setValues(selectedRole === 'farmer' ? { farmUnit: 'acres' } : {});
    setErrors({});
    setError('');
  };

  const change = (name, value) => {
    setValues((previous) => ({ ...previous, [name]: value }));
    setErrors((previous) => ({ ...previous, [name]: undefined }));
  };

  async function submit(event) {
    event.preventDefault();

    const config = ROLE_CONFIG[role];
    const validationErrors = validateFields(config.fields, values);

    setErrors(validationErrors);
    setError('');

    if (Object.keys(validationErrors).length) return;

    setBusy(true);

    try {
      await register(role, values);

      // Preserve farm details for creation after the farmer logs in.
      if (role === 'farmer') {
        sessionStorage.setItem(
          'fvn_pending_farm',
          JSON.stringify({
            name: values.farmName.trim(),
            location: values.farmLocation.trim(),
            area: values.farmArea,
            unit: values.farmUnit,
            soil: '',
            crop: values.primaryCrop.trim(),
            notes: '',
          })
        );
      }

      navigate('/login', { state: { registered: true } });
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  if (!role) {
    return (
      <AuthShell>
        <h2>Create your account</h2>
        <p className="muted">Choose your role to get started.</p>

        <div className="role-list">
          {Object.entries(ROLE_CONFIG).map(([key, config]) => (
            <button
              key={key}
              className="role-card"
              onClick={() => pick(key)}
            >
              <config.icon size={26} />
              <div>
                <strong>{config.title}</strong>
                <span>{config.desc}</span>
              </div>
            </button>
          ))}
        </div>

        <p className="center">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </AuthShell>
    );
  }

  const config = ROLE_CONFIG[role];

  return (
    <AuthShell wide>
      <button className="link-btn" onClick={() => setRole(null)}>
        <ArrowLeft size={16} /> Change role
      </button>

      <h2>{config.title} registration</h2>

      {error && <div className="alert error" role="alert">{error}</div>}

      <form onSubmit={submit} noValidate>
        <div className="grid-form">
          {config.fields.map((field) => (
            <Field
              key={field.name}
              field={field}
              value={values[field.name]}
              error={errors[field.name]}
              onChange={change}
            />
          ))}
        </div>

        <button className="btn primary block" disabled={busy}>
          {busy ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <p className="center">
        Already registered? <Link to="/login">Sign in</Link>
      </p>
    </AuthShell>
  );
}
