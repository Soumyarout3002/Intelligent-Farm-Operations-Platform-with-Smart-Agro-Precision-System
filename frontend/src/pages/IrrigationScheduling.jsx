import '../styles/operations.css';
import { useCallback, useEffect, useState } from 'react';
import { Droplets, RefreshCw, Plus, CheckCircle2, XCircle, CloudSun } from 'lucide-react';
import { listFarms } from '../services/farmService';
import { createIrrigationSchedule, getIrrigationSchedules, getIrrigationSuggestions, recordIrrigation } from '../services/irrigationService';
import { useAuth } from '../context/AuthContext';

const localDateTime = () => {
  const d = new Date(Date.now() + 60 * 60 * 1000);
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
};

export default function IrrigationScheduling() {
  const { user } = useAuth();
  const [farms, setFarms] = useState([]);
  const [farmId, setFarmId] = useState('');
  const [schedules, setSchedules] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [scheduledAt, setScheduledAt] = useState(localDateTime());
  const [waterAmountLiters, setWaterAmountLiters] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const loadFarmData = useCallback(async (id) => {
    if (!id) return;
    const [rows, tips] = await Promise.all([
      getIrrigationSchedules(id), getIrrigationSuggestions(id),
    ]);
    setSchedules(Array.isArray(rows) ? rows : []);
    setSuggestions(Array.isArray(tips) ? tips : []);
  }, []);

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const rows = await listFarms();
      setFarms(rows);
      const chosen = farmId && rows.some((f) => String(f.id) === String(farmId)) ? String(farmId) : (rows[0] ? String(rows[0].id) : '');
      setFarmId(chosen);
      if (chosen) await loadFarmData(chosen);
      else { setSchedules([]); setSuggestions([]); }
    } catch (e) { setError(e.message || 'Could not load irrigation data.'); }
    finally { setLoading(false); }
  }, [farmId, loadFarmData]);

  useEffect(() => { load(); }, []); // initial load only
  useEffect(() => { if (farmId) loadFarmData(farmId).catch((e) => setError(e.message)); }, [farmId, loadFarmData]);

  const submit = async (event) => {
    event.preventDefault(); setBusy(true); setError(''); setMessage('');
    try {
      await createIrrigationSchedule(farmId, {
        scheduledAt, waterAmountLiters: Number(waterAmountLiters), notes: notes.trim(),
      });
      setWaterAmountLiters(''); setNotes(''); setMessage('Irrigation schedule created.');
      await loadFarmData(farmId);
    } catch (e) { setError(e.message || 'Could not create schedule.'); }
    finally { setBusy(false); }
  };

  const record = async (scheduleId, action) => {
    setBusy(true); setError(''); setMessage('');
    try {
      await recordIrrigation(farmId, scheduleId, { action, notes: '' });
      setMessage(`Irrigation marked ${action.toLowerCase()}.`);
      await loadFarmData(farmId);
    } catch (e) { setError(e.message || 'Could not update irrigation record.'); }
    finally { setBusy(false); }
  };

  return <div className="page-wrap">
    <div className="page-heading"><div><p className="eyebrow">FARM OPERATIONS</p><h1><Droplets size={25} /> Irrigation Scheduling</h1><p>View schedules, record completed irrigation and review weather-based recommendations for your accessible farms.</p></div><button className="btn ghost" onClick={load} disabled={loading}><RefreshCw size={16}/> Refresh</button></div>
    {error && <div className="alert error" role="alert">{error}</div>}
    {message && <div className="alert success" role="status">{message}</div>}
    <section className="card" style={{marginBottom:18}}><label htmlFor="irrigation-farm">Select farm</label><select id="irrigation-farm" className="form-control" value={farmId} onChange={(e) => setFarmId(e.target.value)} disabled={loading || !farms.length}><option value="">Select a farm</option>{farms.map((farm) => <option key={farm.id} value={farm.id}>{farm.name} — {farm.location}</option>)}</select>{!loading && !farms.length && <p>No farms are assigned to this account yet.</p>}</section>
    {user?.role !== 'field_worker' && farmId && <form className="card" onSubmit={submit} style={{marginBottom:18}}><h2><Plus size={18}/> Create irrigation schedule</h2><div className="form-grid"><label>Date and time<input className="form-control" type="datetime-local" required value={scheduledAt} min={localDateTime()} onChange={(e) => setScheduledAt(e.target.value)}/></label><label>Water amount (litres)<input className="form-control" type="number" min="1" step="0.1" required value={waterAmountLiters} onChange={(e) => setWaterAmountLiters(e.target.value)}/></label><label className="form-span">Notes<input className="form-control" maxLength="500" value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Optional field instructions"/></label></div><button className="btn primary" disabled={busy}>{busy ? 'Saving…' : 'Save schedule'}</button></form>}
    <section className="card" style={{marginBottom:18}}><h2>Scheduled irrigation</h2>{loading ? <p>Loading schedules…</p> : !schedules.length ? <p>No irrigation schedules found for this farm.</p> : <div className="table-wrap"><table><thead><tr><th>Scheduled at</th><th>Water</th><th>Status</th><th>Notes</th><th>Action</th></tr></thead><tbody>{schedules.map((item) => <tr key={item.id}><td>{item.scheduledAt ? new Date(item.scheduledAt).toLocaleString() : '—'}</td><td>{item.waterAmountLiters ?? '—'} L</td><td>{item.status}</td><td>{item.notes || '—'}</td><td>{item.status === 'SCHEDULED' ? <div className="actions-row"><button className="btn sm primary" disabled={busy} onClick={() => record(item.id, 'COMPLETED')}><CheckCircle2 size={14}/> Done</button><button className="btn sm ghost" disabled={busy} onClick={() => record(item.id, 'SKIPPED')}><XCircle size={14}/> Skip</button></div> : 'Recorded'}</td></tr>)}</tbody></table></div>}</section>
    <section className="card"><h2><CloudSun size={18}/> Weather-based irrigation suggestions</h2>{loading ? <p>Loading recommendations…</p> : !suggestions.length ? <p>No recommendations available.</p> : <div className="cards">{suggestions.map((tip, i) => <article className="card" key={`${tip.date}-${i}`}><div className="row-between"><strong>{tip.date}</strong><span className={`tagpill ${String(tip.priority).toLowerCase()}`}>{tip.priority}</span></div><h3>{String(tip.recommendation).replaceAll('_',' ')}</h3><p>{tip.reason}</p></article>)}</div>}</section>
  </div>;
}
