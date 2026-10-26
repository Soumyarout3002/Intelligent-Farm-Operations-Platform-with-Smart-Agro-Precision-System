import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Tractor, Ruler, Layers, Users, Plus, UserPlus, Wheat, Sprout, CloudSun, Droplets, Wind } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { listFarms } from '../services/farmService';
import { listUsers } from '../services/userService';
import { can, cap } from '../config/permissions';
import SunriseScene from '../components/SunriseScene';

const ACTIVITIES = ['Irrigation schedule reviewed (Plot 3)', 'Soil sample submitted', 'Harvest planning meeting noted', 'Seed inventory updated'];
const TASKS = [['Inspect irrigation lines', 'Today'], ['Review soil test results', 'Tomorrow'], ['Update crop calendar', 'This week'], ['Check storage inventory', 'This week']];
const toHa = (f) => (f.unit === 'hectares' ? f.area : f.area * 0.4047);

function Bars({ items, max, cls = '', suffix = '' }) {
  if (!items.length) return <p className="muted">No data yet.</p>;
  return (
    <div className="bars">
      {items.map(([label, value]) => (
        <div className="bar-row" key={label}>
          <span title={label}>{label.length > 14 ? `${label.slice(0, 13)}…` : label}</span>
          <div className="bar-track"><div className={`bar-fill ${cls}`} style={{ width: `${max ? Math.max(4, (value / max) * 100) : 0}%` }} /></div>
          <span>{value}{suffix}</span>
        </div>
      ))}
    </div>
  );
}

export default function Overview() {
  const { user } = useAuth();
  const [farms, setFarms] = useState([]);
  const [users, setUsers] = useState([]);
  const [done, setDone] = useState({});
  useEffect(() => { listFarms().then(setFarms); listUsers().then(setUsers); }, []);

  const isAdmin = can(user.role, 'users');
  const stats = useMemo(() => {
    const count = (key) => Object.entries(farms.reduce((m, f) => ({ ...m, [f[key]]: (m[f[key]] || 0) + 1 }), {})).sort((a, b) => b[1] - a[1]);
    const byArea = [...farms].sort((a, b) => toHa(b) - toHa(a)).slice(0, 5).map((f) => [f.name, +toHa(f).toFixed(1)]);
    return { status: count('status'), crops: count('crop').slice(0, 5), soil: count('soil'), byArea,
      recent: [...farms].sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 4) };
  }, [farms]);

  const hectares = farms.reduce((s, f) => s + toHa(f), 0);
  const plots = farms.reduce((s, f) => s + f.plots, 0);
  const cards = [
    { label: 'Total farms', value: farms.length, icon: Tractor },
    { label: 'Active farms', value: farms.filter((f) => f.status === 'active').length, icon: Sprout },
    { label: 'Total area (ha)', value: hectares.toFixed(1), icon: Ruler },
    { label: 'Total plots', value: plots, icon: Layers },
    { label: 'Distinct crops', value: new Set(farms.map((f) => f.crop)).size, icon: Wheat },
    ...(isAdmin ? [{ label: 'Users', value: users.length, icon: Users }] : []),
  ];
  const tasksDone = Object.values(done).filter(Boolean).length;
  const roleCounts = ['farmer', 'manager', 'admin'].map((r) => [cap(r), users.filter((u) => u.role === r).length]);
  const statusCls = { active: 'teal', inactive: 'red', fallow: 'amber' };

  return (
    <>
      <div className="hero">
        <SunriseScene className="hero-scene" />
        <div className="hero-text"><h1>Welcome, {user.name}!</h1><p>{cap(user.role)} dashboard · {new Date().toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</p></div>
        <span className="tagpill">Demo build</span>
      </div>
      

      <div className="cards">
        {cards.map((c) => <div className="card stat" key={c.label}><c.icon size={22} /><div><strong>{c.value}</strong><span>{c.label}</span></div></div>)}
      </div>

      <div className="grid3 section">
        <div className="card"><h3>Farms by status</h3><Bars items={stats.status} max={farms.length} cls="teal" /></div>
        <div className="card"><h3>Crop distribution</h3><Bars items={stats.crops} max={stats.crops[0]?.[1]} /></div>
        <div className="card"><h3>Largest farms (ha)</h3><Bars items={stats.byArea} max={stats.byArea[0]?.[1]} cls="amber" /></div>
      </div>

      <div className="grid3 section">
        <div className="card">
          <h3>Weather <small className="tagpill">Demo data</small></h3>
          <div className="weather"><CloudSun size={42} color="#e09a1f" /><div><div className="temp">28°C</div><span className="muted">Partly cloudy</span></div></div>
          <div className="mini-grid"><div><Droplets size={16} /><b>62%</b>Humidity</div><div><Wind size={16} /><b>11 km/h</b>Wind</div><div><Droplets size={16} /><b>20%</b>Rain chance</div></div>
        </div>
        <div className="card">
          <h3>Task checklist <small className="tagpill">Demo</small></h3>
          <div className="progress-line"><span>Progress</span><span>{tasksDone}/{TASKS.length}</span></div>
          <div className="bar-track" style={{ marginBottom: 8 }}><div className="bar-fill teal" style={{ width: `${(tasksDone / TASKS.length) * 100}%` }} /></div>
          {TASKS.map(([t, when]) => (
            <label key={t} className={`task ${done[t] ? 'done' : ''}`}>
              <input type="checkbox" checked={!!done[t]} onChange={(e) => setDone((d) => ({ ...d, [t]: e.target.checked }))} /><span>{t}</span><small>{when}</small>
            </label>
          ))}
        </div>
        <div className="card">
          <h3>Recently added farms</h3>
          {stats.recent.length === 0 ? <p className="muted">No farms yet.</p> : (
            <ul className="plain">{stats.recent.map((f) => <li key={f.id}><Link to={`/farms/${f.id}`}>{f.name}</Link><span className={`badge ${f.status}`}>{f.status}</span></li>)}</ul>
          )}
        </div>
      </div>

      <div className="two-col">
        {isAdmin ? (
          <div className="card"><h3>Users by role</h3><Bars items={roleCounts} max={Math.max(1, ...roleCounts.map((r) => r[1]))} />
            <p className="muted" style={{ marginTop: 12 }}>Active accounts: {users.filter((u) => u.status === 'active').length} of {users.length}</p></div>
        ) : (
          <div className="card"><h3>Soil types</h3><Bars items={stats.soil} max={stats.soil[0]?.[1]} /></div>
        )}
        <div className="card"><h3>Recent activities <small className="tagpill">Demo data</small></h3><ul className="plain">{ACTIVITIES.map((a) => <li key={a}>{a}</li>)}</ul></div>
      </div>

      <div className="card">
        <h3>Quick actions</h3>
        <div className="actions-row">
          <Link className="btn primary" to="/farms"><Plus size={16} /> Manage farms</Link>
          {isAdmin && <Link className="btn ghost" to="/users"><UserPlus size={16} /> Manage users</Link>}
          <Link className="btn ghost" to="/profile">View profile</Link>
        </div>
      </div>
    </>
  );
}
