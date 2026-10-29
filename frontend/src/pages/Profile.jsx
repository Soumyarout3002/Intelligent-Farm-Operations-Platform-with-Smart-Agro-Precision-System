
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getMyProfile, updateMyProfile } from '../services/userService';
import { updateSessionUser } from '../services/authService';
import { useToast } from '../components/Toast';
import { cap } from '../config/permissions';
import { isMobile } from '../utils/validation';

export default function Profile() {
  const { user, refresh } = useAuth();
  const toast = useToast();

  const [name, setName] = useState(user?.name || '');
  const [mobile, setMobile] = useState(user?.mobile || '');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // Load the latest profile from MySQL-backed API on page load.
  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      try {
        const profile = await getMyProfile();

        if (cancelled) return;

        setName(profile.name || '');
        setMobile(profile.mobile || '');

        // Update browser session so other components can use fresh data.
        updateSessionUser(profile);
        refresh();
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadProfile();

    return () => {
      cancelled = true;
    };
  }, [refresh]);

  async function save(e) {
    e.preventDefault();
    setError('');

    if (!name.trim()) {
      setError('Name is required.');
      return;
    }

    if (mobile && !isMobile(mobile)) {
      setError('Enter a valid mobile number.');
      return;
    }

    setSaving(true);

    try {
      const updatedProfile = await updateMyProfile({
        name: name.trim(),
        mobile
      });

      setName(updatedProfile.name || '');
      setMobile(updatedProfile.mobile || '');

      // Keep stored user information and navbar in sync.
      updateSessionUser(updatedProfile);
      refresh();

      toast('Profile updated.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <div className="page-head">
        <h1>User profile</h1>
      </div>

      <div className="card narrow">
        <p>
          <span className={`badge ${user?.role}`}>
            {cap(user?.role)}
          </span>{' '}
          <span className="badge active">{user?.status}</span>
        </p>

        {error && <div className="alert error">{error}</div>}

        {loading ? (
          <p>Loading profile...</p>
        ) : (
          <form onSubmit={save} noValidate>
            <div className="field">
              <label htmlFor="pn">Full name</label>
              <input
                id="pn"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>

            <div className="field">
              <label htmlFor="pe">Email</label>
              <input
                id="pe"
                value={user?.email || ''}
                disabled
              />
            </div>

            <div className="field">
              <label htmlFor="pm">Mobile</label>
              <input
                id="pm"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
              />
            </div>

            {user?.organization && (
              <p className="muted">
                Organization: {user.organization}
              </p>
            )}

            <button className="btn primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save profile'}
            </button>
          </form>
        )}
      </div>
    </>
  );
}
