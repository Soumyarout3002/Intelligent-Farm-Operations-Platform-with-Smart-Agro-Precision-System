import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import AuthShell from './AuthShell';
import { useAuth } from '../../context/AuthContext';
import { getRememberedEmail } from '../../services/authService';
import { isEmail } from '../../utils/validation';
import { createFarm } from '../../services/farmService';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const { state } = useLocation();
  const [email, setEmail] = useState(getRememberedEmail());
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(!!getRememberedEmail());
  const [show, setShow] = useState(false);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e) {
  e.preventDefault();

  const errs = {};

  if (!email.trim()) {
    errs.email = 'Email is required.';
  } else if (!isEmail(email)) {
    errs.email = 'Enter a valid email address.';
  }

  if (!password) {
    errs.password = 'Password is required.';
  }

  setErrors(errs);
  setError('');

  if (Object.keys(errs).length) return;

  setBusy(true);

  try {
    await login({ email, password, remember });

    const pendingFarm = sessionStorage.getItem('fvn_pending_farm');

    if (pendingFarm) {
      try {
        await createFarm(JSON.parse(pendingFarm));
        sessionStorage.removeItem('fvn_pending_farm');
        navigate('/overview', { replace: true });
      } catch (farmError) {
        navigate('/farms', {
          replace: true,
          state: { farmSetupError: farmError.message }
        });
      }
    } else {
      navigate('/overview', { replace: true });
    }
  } catch (err) {
    setError(err.message);
    setBusy(false);
  }
}

  return (
    <AuthShell>
      <h2>Welcome back</h2>
      <p className="muted">Sign in to your FarmVerse Nexus account.</p>
      {state?.registered && <div className="alert success">Registration successful! Please sign in.</div>}
      {error && <div className="alert error" role="alert">{error}</div>}
      <form onSubmit={submit} noValidate>
        <div className="field">
          <label htmlFor="email">Email address</label>
          <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" aria-invalid={!!errors.email} />
          {errors.email && <p className="err">{errors.email}</p>}
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <div className="pw-wrap">
            <input id="password" type={show ? 'text' : 'password'} value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" aria-invalid={!!errors.password} />
            <button type="button" className="icon-btn" aria-label={show ? 'Hide password' : 'Show password'} onClick={() => setShow((s) => !s)}>{show ? <EyeOff size={18} /> : <Eye size={18} />}</button>
          </div>
          {errors.password && <p className="err">{errors.password}</p>}
        </div>
        <div className="row-between">
          <label className="check"><input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} /> <span>Remember me</span></label>
          <Link to="/forgot-password">Forgot password?</Link>
        </div>
        <button className="btn primary block" disabled={busy}>{busy ? 'Signing inâ€¦' : 'Sign in'}</button>
      </form>
      <p className="center">New to FarmVerse Nexus? <Link to="/register">Create an account</Link></p>

    </AuthShell>
  );
}
