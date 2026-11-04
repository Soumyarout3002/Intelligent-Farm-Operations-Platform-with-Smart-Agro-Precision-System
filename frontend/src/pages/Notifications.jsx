import { useEffect, useMemo, useState } from 'react';
import { Bell, BellOff, CheckCheck, RefreshCw, Inbox, AlertTriangle, Mail, Smartphone, Sunrise, Save, Undo2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import { useNotificationStore, loadNotifications, markAsRead, markAllAsRead, unreadCount } from '../store/notificationStore';
import { NOTIFICATION_SAMPLE_MODE, getNotificationPreferences, saveNotificationPreferences } from '../services/notificationService';
import { categoriesForRole } from '../config/notificationCategories';
import NotificationItem from '../components/notifications/NotificationItem';
import '../styles/notifications.css';

const STATUS_FILTERS = [['all', 'All'], ['unread', 'Unread'], ['read', 'Read']];

function dayGroup(iso) {
  const d = new Date(iso);
  const startOfToday = new Date(); startOfToday.setHours(0, 0, 0, 0);
  const days = Math.round((startOfToday - new Date(d.getFullYear(), d.getMonth(), d.getDate())) / 86400000);
  if (days <= 0) return 'Today';
  if (days === 1) return 'Yesterday';
  return 'Earlier';
}

function Switch({ checked, onChange, label, disabled }) {
  return (
    <button type="button" role="switch" aria-checked={checked} aria-label={label} disabled={disabled}
      className={`nt-switch ${checked ? 'on' : ''}`} onClick={() => onChange(!checked)}><i /></button>
  );
}

function Centre({ user }) {
  const toast = useToast();
  const { items, loading, loaded, error } = useNotificationStore();
  const [status, setStatus] = useState('all');
  const [category, setCategory] = useState('all');
  const [busy, setBusy] = useState(false);
  const allowed = useMemo(() => categoriesForRole(user.role), [user.role]);

  const unread = unreadCount(items);
  const countFor = (key) => items.filter((n) => key === 'all' || n.category === key).length;
  const statusCount = { all: items.length, unread, read: items.length - unread };

  const visible = useMemo(() => items.filter((n) =>
    (status === 'all' || (status === 'unread' ? !n.read : n.read)) && (category === 'all' || n.category === category)), [items, status, category]);

  const groups = useMemo(() => {
    const order = ['Today', 'Yesterday', 'Earlier'];
    return order.map((label) => [label, visible.filter((n) => dayGroup(n.createdAt) === label)]).filter(([, list]) => list.length);
  }, [visible]);

  const run = async (action, okMessage) => {
    setBusy(true);
    try { await action(); if (okMessage) toast(okMessage); } catch (e) { toast(e.message || 'Could not update notifications.', 'error'); } finally { setBusy(false); }
  };

  return (
    <>
      <div className="cards nt-stats">
        <div className="card"><div className="stat"><Bell size={26} /><div><strong>{unread}</strong><span>Unread</span></div></div></div>
        <div className="card"><div className="stat"><Inbox size={26} /><div><strong>{items.length}</strong><span>Total notifications</span></div></div></div>
        <div className="card"><div className="stat"><AlertTriangle size={26} /><div><strong>{items.filter((n) => !n.read && n.severity !== 'info').length}</strong><span>Unread warnings</span></div></div></div>
      </div>

      <div className="card nt-filters">
        <div className="nt-filter-row">
          <div className="wx-seg" role="group" aria-label="Filter by status">
            {STATUS_FILTERS.map(([key, label]) => (
              <button key={key} type="button" aria-pressed={status === key} className={status === key ? 'on' : ''} onClick={() => setStatus(key)}>{label} <span className="nt-count">{statusCount[key]}</span></button>
            ))}
          </div>
          <div className="actions-row">
            <button className="btn ghost sm" onClick={() => loadNotifications(user)} disabled={loading}><RefreshCw size={15} /> Refresh</button>
            <button className="btn primary sm" onClick={() => run(() => markAllAsRead(user), 'All notifications marked as read.')} disabled={unread === 0 || busy}><CheckCheck size={15} /> Mark all as read</button>
          </div>
        </div>
        <div className="nt-chips" role="group" aria-label="Filter by category">
          <button type="button" aria-pressed={category === 'all'} className={`nt-chip ${category === 'all' ? 'on' : ''}`} onClick={() => setCategory('all')}>All categories <span className="nt-count">{countFor('all')}</span></button>
          {allowed.map(({ key, short, icon: Icon }) => (
            <button key={key} type="button" aria-pressed={category === key} className={`nt-chip ${category === key ? 'on' : ''}`} onClick={() => setCategory(key)}>
              <Icon size={14} /> {short} <span className="nt-count">{countFor(key)}</span>
            </button>
          ))}
        </div>
      </div>

      {loading && !loaded && <div className="wx-skeleton" style={{ height: 220 }} aria-busy="true" aria-label="Loading notifications" />}
      {error && (
        <div className="alert error wx-error" role="alert">
          <span>{error}</span>
          <button className="btn ghost sm" onClick={() => loadNotifications(user)}>Try again</button>
        </div>
      )}
      {loaded && !error && visible.length === 0 && (
        <div className="empty big">
          <BellOff size={34} />
          <h2>{items.length === 0 ? 'No notifications yet' : status === 'unread' ? "You're all caught up" : 'Nothing matches these filters'}</h2>
          <p>{items.length === 0 ? 'Weather, crop, inventory, irrigation and equipment alerts will appear here.' : 'Try a different status or category.'}</p>
          {items.length > 0 && (status !== 'all' || category !== 'all') && <button className="btn ghost" onClick={() => { setStatus('all'); setCategory('all'); }}>Clear filters</button>}
        </div>
      )}
      {groups.map(([label, list]) => (
        <section className="nt-group" key={label}>
          <h3>{label}</h3>
          <ul className="card nt-list">
            {list.map((n) => <NotificationItem key={n.id} item={n} busy={busy} onRead={(id) => run(() => markAsRead(user, id))} />)}
          </ul>
        </section>
      ))}
    </>
  );
}

function Preferences({ user }) {
  const toast = useToast();
  const [saved, setSaved] = useState(null);
  const [draft, setDraft] = useState(null);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [attempt, setAttempt] = useState(0);
  const allowed = useMemo(() => categoriesForRole(user.role), [user.role]);

  useEffect(() => {
    let ignore = false;
    setStatus('loading');
    getNotificationPreferences(user)
      .then((p) => { if (!ignore) { setSaved(p); setDraft(p); setStatus('ready'); } })
      .catch((e) => { if (!ignore) { setError(e.message || 'Could not load preferences.'); setStatus('error'); } });
    return () => { ignore = true; };
  }, [user, attempt]);

  if (status === 'loading') return <div className="wx-skeleton" style={{ height: 260 }} aria-busy="true" aria-label="Loading preferences" />;
  if (status === 'error') return <div className="alert error wx-error" role="alert"><span>{error}</span><button className="btn ghost sm" onClick={() => setAttempt((a) => a + 1)}>Try again</button></div>;

  const dirty = JSON.stringify(saved) !== JSON.stringify(draft);
  const setCat = (key, value) => setDraft((d) => ({ ...d, categories: { ...d.categories, [key]: value } }));
  const setChannel = (key, value) => setDraft((d) => ({ ...d, channels: { ...d.channels, [key]: value } }));
  const setSummary = (patch) => setDraft((d) => ({ ...d, dailySummary: { ...d.dailySummary, ...patch } }));

  const save = async () => {
    setSaving(true);
    try { await saveNotificationPreferences(user, draft); setSaved(draft); toast('Notification preferences saved.'); }
    catch (e) { toast(e.message || 'Could not save preferences.', 'error'); }
    finally { setSaving(false); }
  };

  return (
    <>
      <section className="card">
        <h3>Alert types</h3>
        <p className="muted">Choose which kinds of alerts you want to receive.</p>
        <ul className="nt-prefs">
          {allowed.map(({ key, label, hint, icon: Icon, tone }) => (
            <li key={key}>
              <span className={`nt-icon tone-${tone}`}><Icon size={20} /></span>
              <div><strong>{label}</strong><span>{hint}</span></div>
              <Switch checked={!!draft.categories[key]} onChange={(v) => setCat(key, v)} label={`${label} ${draft.categories[key] ? 'on' : 'off'}`} />
            </li>
          ))}
        </ul>
      </section>

      <section className="card">
        <h3>How you receive alerts</h3>
        <ul className="nt-prefs">
          <li><span className="nt-icon tone-green"><Bell size={20} /></span><div><strong>In-app (bell icon)</strong><span>Always on, so you never miss an alert inside FarmVerse Nexus.</span></div><Switch checked disabled onChange={() => {}} label="In-app notifications always on" /></li>
          <li><span className="nt-icon tone-blue"><Mail size={20} /></span><div><strong>Email</strong><span>Send alerts to your registered email address.</span></div><Switch checked={!!draft.channels.email} onChange={(v) => setChannel('email', v)} label={`Email alerts ${draft.channels.email ? 'on' : 'off'}`} /></li>
          <li><span className="nt-icon tone-amber"><Smartphone size={20} /></span><div><strong>SMS</strong><span>Send urgent alerts to your phone number.</span></div><Switch checked={!!draft.channels.sms} onChange={(v) => setChannel('sms', v)} label={`SMS alerts ${draft.channels.sms ? 'on' : 'off'}`} /></li>
          <li><span className="nt-icon tone-teal"><Sunrise size={20} /></span><div><strong>Daily morning summary</strong><span>One short message each morning with today's weather and tasks.</span></div><Switch checked={!!draft.dailySummary.enabled} onChange={(v) => setSummary({ enabled: v })} label={`Daily morning summary ${draft.dailySummary.enabled ? 'on' : 'off'}`} /></li>
        </ul>
        {draft.dailySummary.enabled && (
          <div className="field nt-time"><label htmlFor="nt-summary-time">Send the summary at</label><input id="nt-summary-time" type="time" value={draft.dailySummary.time} onChange={(e) => setSummary({ time: e.target.value })} /></div>
        )}
        {(draft.channels.email || draft.channels.sms || draft.dailySummary.enabled) && (
          <div className="alert info">Email, SMS and the daily summary are delivered by the backend once it is connected. Your choices are saved now.</div>
        )}
      </section>

      <div className="nt-save-bar">
        <span className="muted">{dirty ? 'You have unsaved changes.' : 'All changes saved.'}</span>
        <div className="actions-row">
          <button className="btn ghost" onClick={() => setDraft(saved)} disabled={!dirty || saving}><Undo2 size={16} /> Discard</button>
          <button className="btn primary" onClick={save} disabled={!dirty || saving}><Save size={16} /> {saving ? 'Saving…' : 'Save preferences'}</button>
        </div>
      </div>
    </>
  );
}

export default function Notifications() {
  const { user } = useAuth();
  const { items } = useNotificationStore();
  const [tab, setTab] = useState('centre');
  const unread = unreadCount(items);

  useEffect(() => { loadNotifications(user); }, [user]);

  return (
    <div className="nt-page">
      <div className="page-head">
        <div>
          <h1>Notifications {unread > 0 && <span className="nt-head-count" aria-label={`${unread} unread`}>{unread} unread</span>}</h1>
          <p className="muted">Alerts about weather, crops, stock, irrigation and equipment in one place.</p>
        </div>
      </div>

      {NOTIFICATION_SAMPLE_MODE && (
        <div className="alert info" role="note"><b>Sample notifications.</b> The notification service is not connected yet, so these are demo messages, not real farm events. Read status is saved only in this browser.</div>
      )}

      <div className="nt-tabs" role="tablist" aria-label="Notifications sections">
        <button role="tab" aria-selected={tab === 'centre'} className={tab === 'centre' ? 'on' : ''} onClick={() => setTab('centre')}><Bell size={16} /> Notification centre</button>
        <button role="tab" aria-selected={tab === 'prefs'} className={tab === 'prefs' ? 'on' : ''} onClick={() => setTab('prefs')}>Preferences</button>
      </div>

      {tab === 'centre' ? <Centre user={user} /> : <Preferences user={user} />}
    </div>
  );
}
