import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertTriangle, CalendarClock, CheckCircle2, Droplets, Lightbulb, Pencil, PlusCircle, RefreshCw, SkipForward, Trash2, Tractor, Waves, X } from 'lucide-react';
import { useFarm } from '../context/FarmContext';
import { useToast } from '../components/Toast';
import ConfirmDialog from '../components/ConfirmDialog';
import * as svc from '../services/irrigationService';
import '../styles/irrigation.css';

const fmtDateTime = (v) => (v ? new Date(v).toLocaleString(undefined, { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—');
const fmtLiters = (n) => `${Number(n || 0).toLocaleString(undefined, { maximumFractionDigits: 1 })} L`;
const todayStr = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`; };
const EMPTY = { date: '', time: '', water: '', notes: '' };

export default function IrrigationScheduling() {
  const { selectedFarm, status: farmStatus, error: farmError, reload: reloadFarms } = useFarm();
  const toast = useToast();
  const farmId = selectedFarm?.id;

  const [schedules, setSchedules] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [loadState, setLoadState] = useState('idle'); // idle | loading | ready | error
  const [loadError, setLoadError] = useState('');
  const [suggestError, setSuggestError] = useState('');
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [filter, setFilter] = useState('ALL');
  const [confirm, setConfirm] = useState(null); // { type, schedule }
  const [busy, setBusy] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  // Reload whenever the globally selected farm changes. Old farm data is cleared first
  // so it can never appear under the new farm.
  useEffect(() => {
    setSchedules([]); setSuggestions([]); setEditingId(null); setForm(EMPTY); setErrors({}); setFilter('ALL');
    if (!farmId) { setLoadState('idle'); return undefined; }
    let ignore = false;
    setLoadState('loading'); setLoadError(''); setSuggestError('');
    svc.listSchedules(farmId)
      .then((list) => { if (!ignore) { setSchedules(Array.isArray(list) ? list : []); setLoadState('ready'); } })
      .catch((e) => { if (!ignore) { setLoadError(e.message); setLoadState('error'); } });
    svc.listSuggestions(farmId)
      .then((list) => { if (!ignore) setSuggestions(Array.isArray(list) ? list : []); })
      .catch((e) => { if (!ignore) setSuggestError(e.message); });
    return () => { ignore = true; };
  }, [farmId, reloadKey]);

  const refresh = useCallback(() => setReloadKey((k) => k + 1), []);

  const stats = useMemo(() => {
    const now = Date.now();
    const scheduled = schedules.filter((s) => s.status === 'SCHEDULED');
    return {
      total: schedules.length,
      active: scheduled.length,
      completed: schedules.filter((s) => s.status === 'COMPLETED').length,
      skipped: schedules.filter((s) => s.status === 'SKIPPED').length,
      upcoming: scheduled.filter((s) => new Date(s.scheduledAt).getTime() >= now).length,
      overdue: scheduled.filter((s) => new Date(s.scheduledAt).getTime() < now).length,
      liters: schedules.reduce((sum, s) => sum + (Number(s.waterAmountLiters) || 0), 0),
    };
  }, [schedules]);

  const visible = useMemo(() => {
    const list = filter === 'ALL' ? schedules : schedules.filter((s) => s.status === filter);
    return [...list].sort((a, b) => new Date(b.scheduledAt) - new Date(a.scheduledAt));
  }, [schedules, filter]);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const validate = () => {
    const e = {};
    if (!form.date) e.date = 'Choose the irrigation date.';
    if (!form.time) e.time = 'Choose the start time.';
    if (form.date && form.time && new Date(`${form.date}T${form.time}`).getTime() <= Date.now()) e.time = 'Date and time must be in the future.';
    if (form.water === '' || !(Number(form.water) > 0)) e.water = 'Enter a water amount greater than 0.';
    if (form.notes.length > 500) e.notes = 'Notes cannot exceed 500 characters.';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    if (!farmId || !validate()) return;
    const body = { scheduledAt: `${form.date}T${form.time}:00`, waterAmountLiters: Number(form.water), notes: form.notes.trim() || null };
    setSaving(true);
    try {
      if (editingId) await svc.updateSchedule(farmId, editingId, body);
      else await svc.createSchedule(farmId, body);
      toast(editingId ? 'Irrigation schedule updated.' : 'Irrigation schedule created.');
      setForm(EMPTY); setEditingId(null); setErrors({});
      refresh();
    } catch (e) { toast(e.message, 'error'); } finally { setSaving(false); }
  };

  const startEdit = (s) => {
    const d = new Date(s.scheduledAt);
    const pad = (n) => String(n).padStart(2, '0');
    setEditingId(s.id);
    setForm({ date: `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`, time: `${pad(d.getHours())}:${pad(d.getMinutes())}`, water: String(s.waterAmountLiters ?? ''), notes: s.notes || '' });
    setErrors({});
    document.getElementById('irr-form')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };
  const cancelEdit = () => { setEditingId(null); setForm(EMPTY); setErrors({}); };

  const runConfirm = async () => {
    if (!confirm) return;
    setBusy(true);
    try {
      const { type, schedule } = confirm;
      if (type === 'delete') { await svc.deleteSchedule(farmId, schedule.id); toast('Schedule deleted.'); if (editingId === schedule.id) cancelEdit(); }
      else { await svc.recordIrrigation(farmId, schedule.id, { action: type }); toast(type === 'COMPLETED' ? 'Marked as completed.' : 'Marked as skipped.'); }
      setConfirm(null); refresh();
    } catch (e) { toast(e.message, 'error'); setConfirm(null); } finally { setBusy(false); }
  };

  // ---- farm-level states ----
  if (farmStatus === 'loading') return <div className="irr-page"><div className="card irr-state">Loading farms…</div></div>;
  if (farmStatus === 'error') return <div className="irr-page"><div className="alert error">Could not load your farms. {farmError} <button className="btn ghost sm" onClick={reloadFarms}>Try again</button></div></div>;
  if (!selectedFarm) {
    return (
      <div className="irr-page"><div className="empty big"><Tractor size={36} /><h2>No farms available</h2>
        <p>Add a farm in Farm Management to schedule irrigation for it.</p><Link className="btn primary" to="/farms">Go to Farm Management</Link></div></div>
    );
  }

  return (
    <div className="irr-page">
      <div className="page-head">
        <div>
          <h1>Irrigation Scheduling</h1>
          <p className="muted">Plan and track irrigation for <strong>{selectedFarm.name}</strong>. Change the farm from the selector in the top bar.</p>
        </div>
        <button className="btn ghost" onClick={refresh} disabled={loadState === 'loading'}><RefreshCw size={16} /> Refresh</button>
      </div>

      {/* A. SUMMARY */}
      <section aria-label="Irrigation summary" className="irr-summary">
        <div className="irr-stat navy"><Droplets size={20} /><div><strong>{stats.total}</strong><span>Total schedules</span></div></div>
        <div className="irr-stat teal"><CalendarClock size={20} /><div><strong>{stats.active}</strong><span>Active (scheduled)</span></div></div>
        <div className="irr-stat green"><CheckCircle2 size={20} /><div><strong>{stats.completed}</strong><span>Completed</span></div></div>
        <div className="irr-stat amber"><AlertTriangle size={20} /><div><strong>{stats.upcoming}<small> / {stats.overdue} overdue</small></strong><span>Upcoming</span></div></div>
        <div className="irr-stat slate"><Waves size={20} /><div><strong>{fmtLiters(stats.liters)}</strong><span>Water planned</span></div></div>
      </section>

      {loadState === 'error' && <div className="alert error">Could not load irrigation schedules. {loadError} <button className="btn ghost sm" onClick={refresh}>Try again</button></div>}

      <div className="irr-layout">
        {/* B. INPUT */}
        <section className="irr-panel input" id="irr-form" aria-labelledby="irr-input-title">
          <header><span className="irr-tag">INPUT</span><h2 id="irr-input-title">{editingId ? 'Edit irrigation schedule' : 'Create irrigation schedule'}</h2></header>
          <form onSubmit={submit} noValidate>
            <div className="irr-farm-chip"><Tractor size={14} /> Farm: <strong>{selectedFarm.name}</strong></div>
            <div className="grid-form">
              <div className="field"><label htmlFor="irr-date">Irrigation date *</label>
                <input id="irr-date" type="date" min={todayStr()} value={form.date} onChange={set('date')} aria-invalid={!!errors.date} />
                {errors.date && <p className="err">{errors.date}</p>}</div>
              <div className="field"><label htmlFor="irr-time">Start time *</label>
                <input id="irr-time" type="time" value={form.time} onChange={set('time')} aria-invalid={!!errors.time} />
                {errors.time && <p className="err">{errors.time}</p>}</div>
              <div className="field full"><label htmlFor="irr-water">Water amount (litres) *</label>
                <input id="irr-water" type="number" min="0" step="any" inputMode="decimal" placeholder="e.g. 5000" value={form.water} onChange={set('water')} aria-invalid={!!errors.water} />
                {errors.water && <p className="err">{errors.water}</p>}</div>
              <div className="field full"><label htmlFor="irr-notes">Notes (optional)</label>
                <textarea id="irr-notes" rows={3} maxLength={500} placeholder="e.g. Drip line, north block" value={form.notes} onChange={set('notes')} aria-invalid={!!errors.notes} />
                {errors.notes && <p className="err">{errors.notes}</p>}</div>
            </div>
            <div className="form-actions" style={{ justifyContent: 'flex-start' }}>
              <button className="btn irr-submit" type="submit" disabled={saving}><PlusCircle size={16} /> {saving ? 'Saving…' : editingId ? 'Save changes' : 'Create schedule'}</button>
              {editingId && <button type="button" className="btn ghost" onClick={cancelEdit}><X size={16} /> Cancel edit</button>}
            </div>
          </form>
          <p className="irr-help">Irrigation is scheduled per farm. Scheduled dates must be in the future, and only <b>Scheduled</b> entries can be edited or deleted.</p>
        </section>

        {/* Backend weather-based suggestions */}
        <section className="irr-panel result" aria-labelledby="irr-sugg-title">
          <header><span className="irr-tag out">RESULT</span><h2 id="irr-sugg-title">Weather-based suggestions</h2></header>
          {suggestError && <div className="alert error">Could not load suggestions. {suggestError}</div>}
          {!suggestError && loadState === 'loading' && <p className="muted">Loading…</p>}
          {!suggestError && loadState !== 'loading' && suggestions.length === 0 && <p className="muted">No suggestions for this farm right now.</p>}
          <ul className="irr-sugg">
            {suggestions.map((s, i) => (
              <li key={`${s.date}-${i}`} className={`prio-${String(s.priority || '').toLowerCase()}`}>
                <Lightbulb size={16} />
                <div><strong>{s.recommendation}</strong><small>{s.date}{s.priority ? ` · ${s.priority} priority` : ''}</small><p>{s.reason}</p></div>
              </li>
            ))}
          </ul>
        </section>
      </div>

      {/* C. OUTPUT */}
      <section className="irr-panel result wide" aria-labelledby="irr-out-title">
        <header>
          <span className="irr-tag out">OUTPUT</span><h2 id="irr-out-title">Irrigation schedules</h2>
          <div className="irr-filter" role="group" aria-label="Filter by status">
            {['ALL', 'SCHEDULED', 'COMPLETED', 'SKIPPED'].map((f) => (
              <button key={f} type="button" className={filter === f ? 'on' : ''} onClick={() => setFilter(f)}>{f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}</button>
            ))}
          </div>
        </header>
        {loadState === 'loading' && <p className="muted">Loading schedules…</p>}
        {loadState === 'ready' && visible.length === 0 && (
          <div className="empty">{schedules.length === 0 ? 'No irrigation schedules for this farm yet. Create one using the form above.' : 'No schedules match this filter.'}</div>
        )}
        {visible.length > 0 && (
          <div className="table-wrap">
            <table className="irr-table">
              <thead><tr><th>Date &amp; time</th><th>Farm</th><th>Water</th><th>Status</th><th>Notes</th><th>Actions</th></tr></thead>
              <tbody>
                {visible.map((s) => {
                  const open = s.status === 'SCHEDULED';
                  const overdue = open && new Date(s.scheduledAt).getTime() < Date.now();
                  return (
                    <tr key={s.id}>
                      <td>{fmtDateTime(s.scheduledAt)}{overdue && <span className="irr-overdue"> · overdue</span>}</td>
                      <td>{s.farmName || selectedFarm.name}</td>
                      <td>{fmtLiters(s.waterAmountLiters)}</td>
                      <td><span className={`irr-status ${String(s.status).toLowerCase()}`}>{s.status}</span></td>
                      <td className="irr-notes" title={s.notes || ''}>{s.notes || '—'}</td>
                      <td>
                        {open ? (
                          <div className="row-actions">
                            <button className="icon-btn" aria-label="Mark completed" title="Mark completed" onClick={() => setConfirm({ type: 'COMPLETED', schedule: s })}><CheckCircle2 size={17} /></button>
                            <button className="icon-btn" aria-label="Mark skipped" title="Mark skipped" onClick={() => setConfirm({ type: 'SKIPPED', schedule: s })}><SkipForward size={17} /></button>
                            <button className="icon-btn" aria-label="Edit" title="Edit" onClick={() => startEdit(s)}><Pencil size={17} /></button>
                            <button className="icon-btn danger" aria-label="Delete" title="Delete" onClick={() => setConfirm({ type: 'delete', schedule: s })}><Trash2 size={17} /></button>
                          </div>
                        ) : <span className="muted">—</span>}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {confirm && (
        <ConfirmDialog
          title={confirm.type === 'delete' ? 'Delete schedule?' : confirm.type === 'COMPLETED' ? 'Mark as completed?' : 'Mark as skipped?'}
          message={<p>{confirm.type === 'delete' ? 'This irrigation schedule will be permanently removed.' : 'The schedule will be recorded and can no longer be edited.'} ({fmtDateTime(confirm.schedule.scheduledAt)}, {fmtLiters(confirm.schedule.waterAmountLiters)})</p>}
          confirmLabel={confirm.type === 'delete' ? 'Delete' : 'Confirm'}
          busy={busy} onConfirm={runConfirm} onCancel={() => setConfirm(null)}
        />
      )}
    </div>
  );
}
