import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Tractor, Ruler, Layers, Users, Plus, UserPlus, Wheat, Sprout, CloudSun, Droplets, Wind } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { listFarms } from '../services/farmService';
import { listUsers } from '../services/userService';
import { can, cap } from '../config/permissions';
import SunriseScene from '../components/SunriseScene';
import { getActivityLogs } from '../services/activityLogService';


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
    const [activities, setActivities] = useState([]);
  const [activityLoading, setActivityLoading] = useState(false);
  const [activityError, setActivityError] = useState('');

  const isAdmin = can(user.role, 'users');
  
  useEffect(() => {
    listFarms()
      .then(setFarms)
      .catch(console.error);

    if (isAdmin) {
      listUsers()
        .then(setUsers)
        .catch(console.error);

      setActivityLoading(true);
      setActivityError('');

      getActivityLogs({ page: 0, size: 5 })
        .then((data) => {
          setActivities(data.content || []);
        })
        .catch((error) => {
          setActivityError(error.message);
        })
        .finally(() => {
          setActivityLoading(false);
        });
    }
  }, [isAdmin]);

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
  const roleCounts = ['farmer', 'field_worker', 'admin'].map((r) => [cap(r), users.filter((u) => u.role === r).length]);
  const statusCls = { active: 'teal', inactive: 'red', fallow: 'amber' };

  return (
    <>
      <div className="hero">
        <SunriseScene className="hero-scene" />
        <div className="hero-text"><h1>Welcome, {user.name}!</h1><p>{cap(user.role)} dashboard · {new Date().toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</p></div>
        <span className="tagpill">Demo build</span>
      </div>
      <div className="alert info">
  Farm figures, weather and tasks may contain demo data.
  Recent activities are fetched from the backend.
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
        



<div
  className="card recent-activity-card"
  style={{ padding: '22px 24px', minWidth: 0 }}
>
  {/* Header */}
  <div
    style={{
      display: 'flex',
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      gap: 12,
      marginBottom: 16,
    }}
  >
    <div>
      <h3 style={{ margin: 0, fontSize: 19 }}>
        Recent Activities
      </h3>
      <p style={{ margin: '4px 0 0', fontSize: 12, color: '#87968d' }}>
        Latest system events
      </p>
    </div>

    {isAdmin && (
      <Link
        to="/activity-history"
        style={{
          color: '#42b879',
          textDecoration: 'none',
          fontSize: 13,
          fontWeight: 600,
          whiteSpace: 'nowrap',
        }}
      >
        View all →
      </Link>
    )}
  </div>

  {/* Activity List */}
  {activityLoading ? (
    <p className="muted">Loading activities...</p>
  ) : activityError ? (
    <p className="alert error">{activityError}</p>
  ) : activities.length === 0 ? (
    <p className="muted">No activities recorded yet.</p>
  ) : (
    <div>
      {activities.map((activity, index) => {
        const action = activity.actionType || '';
        const color = action.includes('DELETED')
          ? '#42b879'
          : action.includes('DEACTIVATED')
          ? '#42b879'
          : action.includes('ACTIVATED') || action.includes('CREATED')
          ? '#42b879'
          : '#42b879';

        return (
          <div
            key={activity.id}
            style={{
              display: 'grid',
              gridTemplateColumns: 'minmax(130px, 0.9fr) minmax(0, 2fr) 115px',
              alignItems: 'center',
              gap: 14,
              padding: '12px 0',
              borderBottom:
                index === activities.length - 1
                  ? 'none'
                  : '1px solid rgba(255,255,255,0.10)',
            }}
          >
            {/* Action */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 9, minWidth: 0 }}>
              <span
                style={{
                  width: 8,
                  height: 8,
                  minWidth: 8,
                  borderRadius: '50%',
                  background: color,
                }}
              />
              <strong
                style={{
                  fontSize: 11,
                  lineHeight: 1.5,
                  overflowWrap: 'anywhere',
                }}
              >
                {action.replaceAll('_', ' ')}
              </strong>
            </div>

            {/* Description */}
            <span
              style={{
                fontSize: 12,
                lineHeight: 1.5,
                color: '#bdcbc2',
                minWidth: 0,
              }}
            >
              {activity.description}
            </span>

            {/* Date */}
            <time
              style={{
                fontSize: 11,
                color: '#87968d',
                textAlign: 'right',
                whiteSpace: 'nowrap',
              }}
            >
              {new Date(activity.createdAt).toLocaleDateString()}
              <br />
              {new Date(activity.createdAt).toLocaleTimeString([], {
                hour: '2-digit',
                minute: '2-digit',
              })}
            </time>
          </div>
        );
      })}
    </div>
  )}
</div>



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
