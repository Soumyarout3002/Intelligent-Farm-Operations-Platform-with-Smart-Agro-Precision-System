import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Tractor, HardHat, ShieldCheck, ArrowLeft } from 'lucide-react';
import AuthShell from './AuthShell';
import Field from '../../components/Field';
import { useAuth } from '../../context/AuthContext';
import { validateFields } from '../../utils/validation';
import { UNITS } from '../../services/farmService';

const terms = { name: 'terms', label: 'I accept the Terms & Conditions and Privacy Policy', type: 'checkbox', required: true };
const pw = [
  { name: 'password', label: 'Password (min 8 chars)', type: 'password', required: true },
  { name: 'confirmPassword', label: 'Confirm password', type: 'password', required: true },
];
const base = [{ name: 'name', label: 'Full name', required: true }, { name: 'email', label: 'Email', type: 'email', required: true }];
const mobile = { name: 'mobile', label: 'Mobile number', type: 'tel', required: true };

const ROLE_CONFIG = {
  farmer: {
    title: 'Farmer', icon: Tractor, desc: 'Manage your own farm and crops.',
    fields: [...base, mobile, ...pw,
      { name: 'farmName', label: 'Farm name', required: true }, { name: 'farmLocation', label: 'Farm location', required: true },
      { name: 'farmArea', label: 'Farm area', type: 'number', required: true }, { name: 'farmUnit', label: 'Area unit', type: 'select', options: UNITS, required: true },
      { name: 'primaryCrop', label: 'Primary crop', required: true },
      { name: 'language', label: 'Preferred language', type: 'select', options: ['English', 'Hindi', 'Kannada', 'Tamil', 'Telugu', 'Marathi'], required: true }, terms],
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
      name: 'organization',
      label: 'Organization or farm name',
      required: true,
    },
    {
      name: 'assignedFarm',
      label: 'Assigned farm or location',
      required: true,
    },
    {
      name: 'designation',
      label: 'Designation',
      required: true,
    },
    {
      name: 'language',
      label: 'Preferred language',
      type: 'select',
      options: ['English', 'Hindi', 'Kannada', 'Tamil', 'Telugu', 'Marathi'],
      required: true,
    },
    terms,
  ],
},
  admin: {
    title: 'Admin', icon: ShieldCheck, desc: 'Manage users and platform access.',
    fields: [...base, ...pw, { name: 'organization', label: 'Organization name', required: true },
      { name: 'adminCode', label: 'Admin invitation / demo code', required: true, placeholder: 'FARMVERSE-ADMIN-2026' }, terms],
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

  const pick = (r) => { setRole(r); setValues(r === 'farmer' ? { farmUnit: 'acres' } : {}); setErrors({}); setError(''); };
  const change = (n, v) => { setValues((p) => ({ ...p, [n]: v })); setErrors((p) => ({ ...p, [n]: undefined })); };

  async function submit(e) {
    e.preventDefault();
    const cfg = ROLE_CONFIG[role];
    const errs = validateFields(cfg.fields, values);
    setErrors(errs); setError('');
    if (Object.keys(errs).length) return;
    setBusy(true);
    try {
      await register(role, values);
      navigate('/login', { state: { registered: true } });
    } catch (err) { setError(err.message); setBusy(false); }
  }

  if (!role) {
    return (
      <AuthShell>
        <h2>Create your account</h2>
        <p className="muted">Choose your role to get started.</p>
        <div className="role-list">
          {Object.entries(ROLE_CONFIG).map(([k, c]) => (
            <button key={k} className="role-card" onClick={() => pick(k)}><c.icon size={26} /><div><strong>{c.title}</strong><span>{c.desc}</span></div></button>
          ))}
        </div>
        <p className="center">Already registered? <Link to="/login">Sign in</Link></p>
      </AuthShell>
    );
  }
  const cfg = ROLE_CONFIG[role];
  return (
    <AuthShell wide>
      <button className="link-btn" onClick={() => setRole(null)}><ArrowLeft size={16} /> Change role</button>
      <h2>{cfg.title} registration</h2>
      {role === 'admin' && <div className="alert info">Demo admin code: <code>FARMVERSE-ADMIN-2026</code></div>}
      {error && <div className="alert error" role="alert">{error}</div>}
      <form onSubmit={submit} noValidate>
        <div className="grid-form">
          {cfg.fields.map((f) => <Field key={f.name} field={f} value={values[f.name]} error={errors[f.name]} onChange={change} />)}
        </div>
        <button className="btn primary block" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
      </form>
      <p className="center">Already registered? <Link to="/login">Sign in</Link></p>
    </AuthShell>
  );
}
