import { NavLink } from 'react-router-dom';
import { Sprout } from 'lucide-react';
import { NAV_ITEMS, can } from '../config/permissions';

export default function Sidebar({ role, open, onClose }) {
  return (
    <>
      {open && <div className="scrim" onClick={onClose} />}
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <div className="brand"><Sprout size={26} /><div><strong>FarmVerse</strong><span>Nexus</span></div></div>
        <nav>
          {NAV_ITEMS.filter((i) => can(role, i.key)).map(({ key, label, path, icon: Icon }) => (
            <NavLink key={key} to={path} onClick={onClose} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Icon size={18} /> {label}
            </NavLink>
          ))}
        </nav>
        <p className="side-note">Frontend demo build. Sample data only.</p>
      </aside>
    </>
  );
}
