
import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import AuthShell from './AuthShell';
import Field from '../../components/Field';
import { useAuth } from '../../context/AuthContext';
import { validateFields } from '../../utils/validation';
import { UNITS } from '../../services/farmService';

const languages = [
  'English',
  'Hindi',
  'Kannada',
  'Tamil',
  'Telugu',
  'Marathi',
];

const common = [
  { name: 'name', label: 'Full name', required: true },
  { name: 'email', label: 'Email address', type: 'email', required: true },
  { name: 'mobile', label: 'Mobile number', type: 'tel', required: true },
  {
    name: 'password',
    label: 'Password (min 8 chars)',
    type: 'password',
    required: true,
  },
  {
    name: 'confirmPassword',
    label: 'Confirm password',
    type: 'password',
    required: true,
  },
];

const farmerFields = [
  ...common,
  { name: 'farmName', label: 'Farm name', required: true },
  { name: 'farmLocation', label: 'Farm location', required: true },
  {
    name: 'farmArea',
    label: 'Farm area',
    type: 'number',
    required: true,
  },
  {
    name: 'farmUnit',
    label: 'Area unit',
    type: 'select',
    options: UNITS,
    required: true,
  },
  { name: 'primaryCrop', label: 'Primary crop', required: true },
  {
    name: 'language',
    label: 'Preferred language',
    type: 'select',
    options: languages,
    required: true,
  },
  {
    name: 'terms',
    label: 'I accept the Terms & Conditions and Privacy Policy',
    type: 'checkbox',
    required: true,
  },
];

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();

  const invite = params.get('invite');
  const adminSetup = params.get('admin') === 'setup';

  const [values, setValues] = useState({
    farmUnit: 'acres',
    language: 'English',
  });

  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  // Old invitation links are redirected to the dedicated acceptance page.
  useEffect(() => {
    if (invite) {
      navigate(
        `/accept-invitation?token=${encodeURIComponent(invite)}`,
        { replace: true }
      );
    }
  }, [invite, navigate]);

  function change(name, value) {
    if (name === 'mobile') {
      value = String(value ?? '').replace(/\D/g, '').slice(0, 10);
    }

    setValues((previous) => ({
      ...previous,
      [name]: value,
    }));

    setErrors((previous) => ({
      ...previous,
      [name]: undefined,
    }));
  }

  async function submit(event) {
    event.preventDefault();

    setError('');

    const validation = validateFields(farmerFields, values);

    if (!/^\d{10}$/.test(String(values.mobile || ''))) {
      validation.mobile = 'Mobile number must be exactly 10 digits.';
    }

    if (values.password !== values.confirmPassword) {
      validation.confirmPassword = 'Passwords do not match.';
    }

    if (String(values.password || '').length < 8 ||
        String(values.password || '').length > 72) {
      validation.password = 'Password must contain 8 to 72 characters.';
    }

    const area = Number(values.farmArea);

    if (!Number.isFinite(area) || area <= 0) {
      validation.farmArea = 'Enter a valid farm area greater than zero.';
    }

    setErrors(validation);

    if (Object.keys(validation).length > 0) {
      return;
    }

    setBusy(true);

    try {
      await register('farmer', values);

      // Save the farm details for the existing post-registration flow.
      sessionStorage.setItem(
        'fvn_pending_farm',
        JSON.stringify({
          email: values.email.trim().toLowerCase(),
          name: values.farmName.trim(),
          location: values.farmLocation.trim(),
          area: values.farmArea,
          unit: values.farmUnit,
          soil: '',
          crop: values.primaryCrop.trim(),
          notes: '',
        })
      );

      navigate('/login', {
        state: { registered: true },
        replace: true,
      });
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
      setBusy(false);
    }
  }

  // Do not allow public first-Admin setup.
  if (adminSetup) {
    return (
      <AuthShell>
        <h2>Admin account setup</h2>

        <div className="alert info">
          An Admin account already exists. New Admin accounts must be
          created through an invitation sent by an existing Admin.
        </div>

        <Link className="link-btn" to="/login">
          <ArrowLeft size={16} /> Back to Login
        </Link>
      </AuthShell>
    );
  }

  if (invite) {
    return (
      <AuthShell>
        <p>Redirecting to invitation acceptance...</p>
      </AuthShell>
    );
  }

  return (
    <AuthShell wide>
      <Link className="link-btn" to="/login">
        <ArrowLeft size={16} /> Back to Login
      </Link>

      <h2>Farmer registration</h2>

      <p className="muted">
        Create your Farmer account and enter your farm details.
      </p>

      {error && (
        <div className="alert error" role="alert">
          {error}
        </div>
      )}

      <form onSubmit={submit} noValidate>
        <div className="grid-form">
          {farmerFields.map((field) => (
            <Field
              key={field.name}
              field={field}
              value={values[field.name]}
              error={errors[field.name]}
              onChange={change}
            />
          ))}
        </div>

        <button
          className="btn primary block"
          disabled={busy}
        >
          {busy ? 'Creating account...' : 'Create Farmer Account'}
        </button>
      </form>

      <p className="center">
        Already registered? <Link to="/login">Sign in</Link>
      </p>

      <p className="center muted">
        Are you a Field Worker or Admin? You need an invitation from an
        existing Admin.
      </p>
    </AuthShell>
  );
}
