
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AuthShell from './AuthShell';
import { resetPassword } from '../../services/authService';

export default function ResetPassword() {
  const [params] = useSearchParams();
  const [token, setToken] = useState(params.get('token') || '');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setMessage('');
    setError('');

    if (!token.trim()) return setError('Reset token is required.');
    if (password.length < 8) return setError('Password must be at least 8 characters.');
    if (password !== confirmPassword) return setError('Passwords do not match.');

    setLoading(true);
    try {
      const result = await resetPassword(token.trim(), password);
      setMessage(
        typeof result === 'string'
          ? result
          : result?.message || 'Password reset successful. Please log in.'
      );
    } catch (err) {
      setError(err.message || 'Unable to reset password.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell>
      <h2>Reset password</h2>
      <p className="muted">Enter your reset token and choose a new password.</p>

      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert error">{error}</div>}

      <form onSubmit={submit} noValidate>
        <div className="field">
          <label htmlFor="reset-token">Reset token</label>
          <input
            id="reset-token"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            required
          />
        </div>

        <div className="field">
          <label htmlFor="reset-password">New password</label>
          <input
            id="reset-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <div className="field">
          <label htmlFor="confirm-password">Confirm new password</label>
          <input
            id="confirm-password"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
        </div>

        <button className="btn primary block" disabled={loading}>
          {loading ? 'Resetting...' : 'Reset password'}
        </button>
      </form>

      <p className="center"><Link to="/login">Back to sign in</Link></p>
    </AuthShell>
  );
}
