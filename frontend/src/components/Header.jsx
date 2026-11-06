import { useState } from 'react';
import { Bell, LogOut, Menu } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { cap } from '../config/permissions';

const NOTES = ['Welcome to FarmVerse Nexus (demo).', 'Sample notification: review farm records.', 'Real notifications need backend integration.'];

export default function Header({ onMenu }) {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  return (
    <header className="topbar">
      <button className="icon-btn menu-btn" aria-label="Open menu" onClick={onMenu}><Menu size={22} /></button>
      <div className="grow" />
      <div className="notif">
        <button className="icon-btn" aria-label="Notifications" onClick={() => setOpen((o) => !o)}><Bell size={20} /><i className="dot" /></button>
        {open && (
          <div className="notif-pop">
            <strong>Notifications (demo)</strong>
            {NOTES.map((n) => <p key={n}>{n}</p>)}
          </div>
        )}
      </div>
      <div className="me">
        <span className="avatar">{user.name.charAt(0).toUpperCase()}</span>
        <div><strong>{user.name}</strong><small>{cap(user.role)}</small></div>
      </div>
      <button className="btn ghost sm" onClick={logout}><LogOut size={16} /> Logout</button>
    </header>
  );
}
