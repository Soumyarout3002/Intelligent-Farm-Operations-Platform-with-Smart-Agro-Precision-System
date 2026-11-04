
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  validateInvitation,
  acceptInvitation,
} from '../../services/authService';

export default function AcceptInvitation() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [invitation, setInvitation] = useState(null);
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    let active = true;

    async function checkInvitation() {
      if (!token) {
        setError('Invitation token is missing.');
        setLoading(false);
        return;
      }

      try {
        const response = await validateInvitation(token);

        if (active) {
          setInvitation(response);
        }
      } catch (err) {
        if (active) {
          setError(err.message || 'This invitation is invalid or expired.');
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    checkInvitation();

    return () => {
      active = false;
    };
  }, [token]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');

    if (password.length < 8 || password.length > 72) {
      setError('Password must contain between 8 and 72 characters.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      setSubmitting(true);

      await acceptInvitation({
        token,
        name,
        password,
      });

      setSuccess(true);
    } catch (err) {
      setError(err.message || 'Unable to accept invitation.');
    } finally {
      setSubmitting(false);
    }
  }

  const inputStyle = {
    width: '100%',
    padding: '12px',
    marginTop: '7px',
    marginBottom: '16px',
    border: '1px solid #888',
    borderRadius: '7px',
    boxSizing: 'border-box',
  };

  if (loading) {
    return <div style={{ padding: 40 }}>Validating your invitation...</div>;
  }

  if (success) {
    return (
      <div style={{ maxWidth: 450, margin: '80px auto', padding: 24 }}>
        <h2>Account created successfully!</h2>
        <p>Your FarmVerse Nexus account is ready.</p>
        <Link to="/login">Continue to Login</Link>
      </div>
    );
  }

  if (error && !invitation) {
    return (
      <div style={{ maxWidth: 450, margin: '80px auto', padding: 24 }}>
        <h2>Invitation unavailable</h2>
        <p style={{ color: 'red' }}>{error}</p>
        <Link to="/login">Go to Login</Link>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 450, margin: '60px auto', padding: 24 }}>
      <h2>Accept Your Invitation</h2>
      <p>Create your FarmVerse Nexus account.</p>

      <p>
        <strong>Invited email:</strong> {invitation?.email}
      </p>

      <p>
        <strong>Assigned role:</strong> {invitation?.role}
      </p>

      <form onSubmit={handleSubmit}>
        <label htmlFor="name">Full Name</label>
        <input
          id="name"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          maxLength={100}
          required
          style={inputStyle}
        />

        <label htmlFor="password">Create Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={8}
          maxLength={72}
          required
          style={inputStyle}
        />

        <label htmlFor="confirmPassword">Confirm Password</label>
        <input
          id="confirmPassword"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
          style={inputStyle}
        />

        {error && (
          <p style={{ color: 'red' }}>{error}</p>
        )}

        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating Account...' : 'Accept Invitation'}
        </button>
      </form>
    </div>
  );
}
