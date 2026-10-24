import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthShell from './AuthShell';
import { requestPasswordReset } from '../../services/authService';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  async function submit(e) {
    e.preventDefault(); setMsg(''); setError('');
    try { setMsg(await requestPasswordReset(email)); } catch (err) { setError(err.message); }
  }
  return (
    <AuthShell>
      <h2>Forgot password</h2>
      <p className="muted">Enter your email to request a password reset.</p>
      {msg && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      <form onSubmit={submit} noValidate>
        <div className="field">
          <label htmlFor="fp-email">Email address</label>
          <input id="fp-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <button className="btn primary block">Send reset link</button>
      </form>
      <p className="center"><Link to="/login">Back to sign in</Link></p>
    </AuthShell>
  );
}
