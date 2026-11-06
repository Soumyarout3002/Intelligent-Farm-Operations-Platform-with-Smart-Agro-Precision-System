import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Pencil, Trash2 } from 'lucide-react';
import { getFarm, updateFarm, deleteFarm } from '../services/farmService';
import FarmForm from '../components/FarmForm';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { useToast } from '../components/Toast';

export default function FarmDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const [farm, setFarm] = useState(undefined);
  const [edit, setEdit] = useState(false);
  const [del, setDel] = useState(false);
  useEffect(() => { getFarm(id).then(setFarm); }, [id]);

  if (farm === undefined) return <p className="muted">Loading…</p>;
  if (!farm) return <div className="empty big"><h2>Farm not found</h2><Link className="btn primary" to="/farms">Back to farms</Link></div>;

  const rows = [['Farm ID', farm.id], ['Location', farm.location], ['Area', `${farm.area} ${farm.unit}`], ['Soil type', farm.soil], ['Main crop', farm.crop],
    ['Number of plots', farm.plots], ['Created', new Date(farm.createdAt).toLocaleString()]];
  async function save(v) { setFarm(await updateFarm(farm.id, v)); setEdit(false); toast('Farm updated successfully.'); }
  async function remove() {
    try { await deleteFarm(farm.id); toast('Farm deleted.'); navigate('/farms'); } catch (e) { toast(e.message, 'error'); }
  }
  return (
    <>
      <Link className="link-btn" to="/farms"><ArrowLeft size={16} /> Back to farms</Link>
      <div className="page-head">
        <div><h1>{farm.name}</h1><span className={`badge ${farm.status}`}>{farm.status}</span></div>
        <div className="actions-row">
          <button className="btn ghost" onClick={() => setEdit(true)}><Pencil size={16} /> Edit</button>
          <button className="btn danger" onClick={() => setDel(true)}><Trash2 size={16} /> Delete</button>
        </div>
      </div>
      <div className="card">
        <dl className="details">{rows.map(([k, v]) => <div key={k}><dt>{k}</dt><dd>{v}</dd></div>)}</dl>
        <h3>Notes</h3><p>{farm.notes || <span className="muted">No notes.</span>}</p>
      </div>
      {edit && <Modal wide title="Edit farm" onClose={() => setEdit(false)}><FarmForm initial={farm} onSubmit={save} onCancel={() => setEdit(false)} /></Modal>}
      {del && <ConfirmDialog title="Delete farm" onCancel={() => setDel(false)} onConfirm={remove}
        message={<><p>Delete <strong>{farm.name}</strong>? This cannot be undone.</p><p className="muted">Linked records would also be affected once related modules exist.</p></>} />}
    </>
  );
}
