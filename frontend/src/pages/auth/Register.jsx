import { useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Tractor, ArrowLeft, ShieldCheck } from 'lucide-react';
import AuthShell from './AuthShell';
import Field from '../../components/Field';
import { useAuth } from '../../context/AuthContext';
import { validateFields } from '../../utils/validation';
import { UNITS } from '../../services/farmService';
import { createFirstAdmin, registerInvited } from '../../services/authService';

const languages = ['English', 'Hindi', 'Kannada', 'Tamil', 'Telugu', 'Marathi'];
const common = [
  { name: 'name', label: 'Full name', required: true },
  { name: 'email', label: 'Email address', type: 'email', required: true },
  { name: 'mobile', label: 'Mobile number', type: 'tel', required: true },
  { name: 'password', label: 'Password (min 8 chars)', type: 'password', required: true },
  { name: 'confirmPassword', label: 'Confirm password', type: 'password', required: true },
  { name: 'language', label: 'Preferred language', type: 'select', options: languages, required: true },
];
const farmerFields = [...common.slice(0,5),
  { name: 'farmName', label: 'Farm name', required: true },
  { name: 'farmLocation', label: 'Farm location', required: true },
  { name: 'farmArea', label: 'Farm area', type: 'number', required: true },
  { name: 'farmUnit', label: 'Area unit', type: 'select', options: UNITS, required: true },
  { name: 'primaryCrop', label: 'Primary crop', required: true },
  common[5],
  { name: 'terms', label: 'I accept the Terms & Conditions and Privacy Policy', type: 'checkbox', required: true },
];
const adminFields = [...common, { name: 'adminSetupSecret', label: 'First Admin setup secret', type: 'password', required: true }];

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const invite = params.get('invite');
  const adminSetup = params.get('admin') === 'setup';
  const mode = adminSetup ? 'admin' : invite ? 'invited' : 'farmer';
  const fields = useMemo(() => mode === 'admin' ? adminFields : mode === 'farmer' ? farmerFields : common, [mode]);
  const [values, setValues] = useState({ farmUnit: 'acres', language: 'English' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

 const change = (name, value) => {
  if (name === 'mobile') {
    value = String(value ?? '').replace(/\D/g, '').slice(0, 10);
  }

  setValues(v => ({ ...v, [name]: value }));
  setErrors(e => ({ ...e, [name]: undefined }));
};

  async function submit(e) {
    e.preventDefault();
  const validation = validateFields(fields, values);

  if (!/^\d{10}$/.test(String(values.mobile || ''))) {
    validation.mobile = 'Mobile number must be exactly 10 digits.';
  }

setErrors(validation);
setError('');

if (Object.keys(validation).length) return;
    setBusy(true);
    try {
      if (mode === 'admin') await createFirstAdmin(values);
      else if (mode === 'invited') await registerInvited(values, invite);
      else await register('farmer', values);

      if (mode === 'farmer') {
        sessionStorage.setItem('fvn_pending_farm', JSON.stringify({
          email: values.email.trim().toLowerCase(), name: values.farmName.trim(),
          location: values.farmLocation.trim(), area: values.farmArea,
          unit: values.farmUnit, soil: '', crop: values.primaryCrop.trim(), notes: '',
        }));
      }
      navigate('/login', { state: { registered: true }, replace: true });
    } catch (err) { setError(err.message); setBusy(false); }
  }

  const title = mode === 'admin' ? 'Initial Admin setup' : mode === 'invited' ? 'Complete your invitation' : 'Farmer registration';
  return <AuthShell wide>
    <Link className="link-btn" to="/register"><ArrowLeft size={16}/> Back</Link>
    <h2>{title}</h2>
    {mode === 'admin' && <div className="alert info"><ShieldCheck size={16}/> Only the first Admin account can be created with the setup secret. Later Admins must be invited.</div>}
    {mode === 'invited' && <div className="alert info">Your role is assigned by the invitation. Use the email address to which the invitation was sent.</div>}
    {error && <div className="alert error" role="alert">{error}</div>}
    <form onSubmit={submit} noValidate><div className="grid-form">
      {fields.map(f => <Field key={f.name} field={f} value={values[f.name]} error={errors[f.name]} onChange={change}/>)}
    </div><button className="btn primary block" disabled={busy}>{busy ? 'Creating account…' : mode === 'admin' ? 'Create first Admin' : 'Create account'}</button></form>
    <p className="center">Already registered? <Link to="/login">Sign in</Link></p>
    {mode === 'farmer' && <p className="center muted">Are you setting up the organisation? <Link to="/register?admin=setup">Initial Admin setup</Link></p>}
  </AuthShell>;
}
