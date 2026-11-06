
import { NavLink } from 'react-router-dom';
import { Sprout, Map, Wheat } from 'lucide-react';
import { NAV_ITEMS, can } from '../config/permissions';

export default function Sidebar({ role, open, onClose }) {
  return (
    <>
      {open && <div className="scrim" onClick={onClose} />}

      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <div className="brand">
          <Sprout size={26} />
          <div>
            <strong>FarmVerse</strong>
            <span>Nexus</span>
          </div>
        </div>

        <nav>
          {NAV_ITEMS.filter((item) => can(role, item.key)).map(
            ({ key, label, path, icon: Icon }) => (
              <NavLink
                key={key}
                to={path}
                onClick={onClose}
                className={({ isActive }) =>
                  `nav-link ${isActive ? 'active' : ''}`
                }
              >
                <Icon size={18} />
                {label}
              </NavLink>
            )
          )}

          {/* GIS Farm Mapping */}
          {can(role, 'farms') && (
            <NavLink
              to="/gis-mapping"
              onClick={onClose}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'active' : ''}`
              }
            >
              <Map size={18} />
              GIS Farm Mapping
            </NavLink>
          )}

          {/* Crop Lifecycle Tracking */}
          {can(role, 'farms') && (
            <NavLink
              to="/crop-lifecycle"
              onClick={onClose}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'active' : ''}`
              }
            >
              <Wheat size={18} />
              Crop Lifecycle Tracking
            </NavLink>
          )}
        </nav>

        <p className="side-note">Frontend demo build. Sample data only.</p>
      </aside>
    </>
  );
}
