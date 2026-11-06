import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Search, Eye, Pencil, Trash2, ChevronLeft, ChevronRight } from 'lucide-react';
import { listFarms, createFarm, updateFarm, deleteFarm, STATUSES } from '../services/farmService';
import FarmForm from '../components/FarmForm';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { useToast } from '../components/Toast';

const PAGE = 6;
export default function FarmManagement() {
  const toast = useToast();
  const [farms, setFarms] = useState([]);
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [crop, setCrop] = useState('');
  const [sort, setSort] = useState('newest');
  const [page, setPage] = useState(1);
  const [editing, setEditing] = useState(null); // null | 'new' | farm
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);

  const reload = useCallback(() => listFarms().then(setFarms), []);
  useEffect(() => { reload(); }, [reload]);

  const crops = useMemo(() => [...new Set(farms.map((f) => f.crop))].sort(), [farms]);
  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    const out = farms.filter((f) => (!s || f.name.toLowerCase().includes(s) || f.location.toLowerCase().includes(s)) && (!status || f.status === status) && (!crop || f.crop === crop));
    const by = { newest: (a, b) => b.createdAt.localeCompare(a.createdAt), name: (a, b) => a.name.localeCompare(b.name), area: (a, b) => b.area - a.area };
    return out.sort(by[sort]);
  }, [farms, q, status, crop, sort]);
  const pages = Math.max(1, Math.ceil(filtered.length / PAGE));
  const cur = Math.min(page, pages);
  const rows = filtered.slice((cur - 1) * PAGE, cur * PAGE);

  async function save(values) {
    if (editing === 'new') { await createFarm(values); toast('Farm added successfully.'); }
    else { await updateFarm(editing.id, values); toast('Farm updated successfully.'); }
    setEditing(null); reload();
  }
  async function confirmDelete() {
    setBusy(true);
    try { await deleteFarm(deleting.id); toast(`Deleted "${deleting.name}".`); setDeleting(null); reload(); }
    catch (e) { toast(e.message, 'error'); }
    setBusy(false);
  }
  const reset = (fn) => (e) => { fn(e.target.value); setPage(1); };

  return (
    <>
      <div className="page-head">
        <div><h1>Farm Management</h1><p className="muted">{filtered.length} farm(s)</p></div>
        <button className="btn primary" onClick={() => setEditing('new')}><Plus size={16} /> Add Farm</button>
      </div>
      <div className="toolbar">
        <div className="search"><Search size={16} /><input placeholder="Search by farm name or location" aria-label="Search farms" value={q} onChange={reset(setQ)} /></div>
        <select aria-label="Filter by status" value={status} onChange={reset(setStatus)}><option value="">All statuses</option>{STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}</select>
        <select aria-label="Filter by crop" value={crop} onChange={reset(setCrop)}><option value="">All crops</option>{crops.map((c) => <option key={c}>{c}</option>)}</select>
        <select aria-label="Sort" value={sort} onChange={reset(setSort)}><option value="newest">Newest first</option><option value="name">Name A–Z</option><option value="area">Largest area</option></select>
      </div>
      {rows.length === 0 ? (
        <div className="empty"><p>{farms.length ? 'No farms match your filters.' : 'No farms yet. Add your first farm.'}</p></div>
      ) : (
        <div className="table-wrap card">
          <table>
            <thead><tr><th>Farm</th><th>ID</th><th>Location</th><th>Area</th><th>Soil</th><th>Crop</th><th>Plots</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead>
            <tbody>
              {rows.map((f) => (
                <tr key={f.id}>
                  <td><Link to={`/farms/${f.id}`}><strong>{f.name}</strong></Link></td><td>{f.id}</td><td>{f.location}</td>
                  <td>{f.area} {f.unit}</td><td>{f.soil}</td><td>{f.crop}</td><td>{f.plots}</td>
                  <td><span className={`badge ${f.status}`}>{f.status}</span></td><td>{new Date(f.createdAt).toLocaleDateString()}</td>
                  <td className="row-actions">
                    <Link className="icon-btn" aria-label={`View ${f.name}`} to={`/farms/${f.id}`}><Eye size={16} /></Link>
                    <button className="icon-btn" aria-label={`Edit ${f.name}`} onClick={() => setEditing(f)}><Pencil size={16} /></button>
                    <button className="icon-btn danger" aria-label={`Delete ${f.name}`} onClick={() => setDeleting(f)}><Trash2 size={16} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {pages > 1 && (
        <div className="pager">
          <button className="icon-btn" aria-label="Previous page" disabled={cur === 1} onClick={() => setPage(cur - 1)}><ChevronLeft size={18} /></button>
          <span>Page {cur} of {pages}</span>
          <button className="icon-btn" aria-label="Next page" disabled={cur === pages} onClick={() => setPage(cur + 1)}><ChevronRight size={18} /></button>
        </div>
      )}
      {editing && <Modal wide title={editing === 'new' ? 'Add farm' : 'Edit farm'} onClose={() => setEditing(null)}><FarmForm initial={editing === 'new' ? null : editing} onSubmit={save} onCancel={() => setEditing(null)} /></Modal>}
      {deleting && <ConfirmDialog title="Delete farm" busy={busy} onCancel={() => setDeleting(null)} onConfirm={confirmDelete}
        message={<><p>Delete <strong>{deleting.name}</strong> ({deleting.id})? This cannot be undone.</p><p className="muted">Any records linked to this farm (plots, activities) would also be affected once those modules exist.</p></>} />}
    </>
  );
}
