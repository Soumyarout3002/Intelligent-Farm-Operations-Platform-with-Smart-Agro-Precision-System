import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { updateUser } from '../services/userService';
import { useToast } from '../components/Toast';
import { cap } from '../config/permissions';
import { isMobile } from '../utils/validation';

export default function Profile() {
  const { user, refresh } = useAuth();
  const toast = useToast();
  const [name, setName] = useState(user.name);
  const [mobile, setMobile] = useState(user.mobile || '');
  const [error, setError] = useState('');

  async function save(e) {
    e.preventDefault(); setError('');
    if (!name.trim()) return setError('Name is required.');
    if (mobile && !isMobile(mobile)) return setError('Enter a valid mobile number.');
    try { await updateUser(user.id, { name: name.trim(), mobile }, user.id); refresh(); toast('Profile updated.'); }
    catch (err) { setError(err.message); }
  }
  return (
    <>
      <div className="page-head"><h1>User profile</h1></div>
      <div className="card narrow">
        <p><span className={`badge ${user.role}`}>{cap(user.role)}</span> <span className="badge active">{user.status}</span></p>
        {error && <div className="alert error">{error}</div>}
        <form onSubmit={save} noValidate>
          <div className="field"><label htmlFor="pn">Full name</label><input id="pn" value={name} onChange={(e) => setName(e.target.value)} /></div>
          <div className="field"><label htmlFor="pe">Email</label><input id="pe" value={user.email} disabled /></div>
          <div className="field"><label htmlFor="pm">Mobile</label><input id="pm" value={mobile} onChange={(e) => setMobile(e.target.value)} /></div>
          {user.organization && <p className="muted">Organization: {user.organization}</p>}
          <button className="btn primary">Save profile</button>
        </form>
      </div>
    </>
  );
}
