import '../styles/operations.css';
import { useCallback, useEffect, useState } from 'react';
import { ClipboardCheck, RefreshCw, CheckCircle2 } from 'lucide-react';
import { listFarms } from '../services/farmService';
import { request } from '../services/api';
import { getToken } from '../services/authService';

export default function FieldWorkerTasks() {
  const [farms, setFarms] = useState([]);
  const [farmId, setFarmId] = useState('');
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');
  const loadTasks = useCallback(async (id) => {
    if (!id) { setTasks([]); return; }
    const rows = await request(`/api/farms/${id}/tasks`, { token: getToken() });
    setTasks(Array.isArray(rows) ? rows : []);
  }, []);
  const load = useCallback(async () => {
    setLoading(true); setError('');
    try { const rows = await listFarms(); setFarms(rows); const id = farmId && rows.some(f => String(f.id) === String(farmId)) ? String(farmId) : String(rows[0]?.id || ''); setFarmId(id); await loadTasks(id); }
    catch (e) { setError(e.message || 'Could not load assigned tasks.'); }
    finally { setLoading(false); }
  }, [farmId, loadTasks]);
  useEffect(() => { load(); }, []);
  useEffect(() => { if (farmId) loadTasks(farmId).catch(e => setError(e.message)); }, [farmId, loadTasks]);
  const update = async (task, status) => {
    setBusyId(task.id); setError('');
    try { await request(`/api/farms/${farmId}/tasks/${task.id}/status?status=${status}`, { method: 'PATCH', token: getToken() }); await loadTasks(farmId); }
    catch (e) { setError(e.message || 'Could not update task.'); }
    finally { setBusyId(null); }
  };
  return <div className="page-wrap"><div className="page-heading"><div><p className="eyebrow">FIELD WORK</p><h1><ClipboardCheck size={25}/> My Field Tasks</h1><p>Tasks assigned to your Field Worker account on active farms.</p></div><button className="btn ghost" onClick={load} disabled={loading}><RefreshCw size={16}/> Refresh</button></div>
    {error && <div className="alert error" role="alert">{error}</div>}
    <section className="card" style={{marginBottom:18}}><label htmlFor="worker-farm">Assigned farm</label><select id="worker-farm" className="form-control" value={farmId} onChange={e => setFarmId(e.target.value)}><option value="">Select farm</option>{farms.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}</select></section>
    <section className="card"><h2>Task list</h2>{loading ? <p>Loading tasks…</p> : !tasks.length ? <p>No tasks have been assigned to you for this farm yet.</p> : <div className="table-wrap"><table><thead><tr><th>Task</th><th>Due date</th><th>Priority</th><th>Status</th><th>Update</th></tr></thead><tbody>{tasks.map(t => <tr key={t.id}><td><strong>{t.title}</strong><div className="muted">{t.description || ''}</div></td><td>{t.dueDate || '—'}</td><td>{t.priority}</td><td>{String(t.status).replaceAll('_',' ')}</td><td>{t.status !== 'DONE' ? <div className="actions-row"><button className="btn sm ghost" disabled={busyId === t.id} onClick={() => update(t, 'IN_PROGRESS')}>Start</button><button className="btn sm primary" disabled={busyId === t.id} onClick={() => update(t, 'DONE')}><CheckCircle2 size={14}/> Complete</button></div> : 'Completed'}</td></tr>)}</tbody></table></div>}</section>
  </div>;
}
