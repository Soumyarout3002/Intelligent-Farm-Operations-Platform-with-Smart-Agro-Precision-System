
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  Plus, Search, Eye, Pencil, Trash2, ChevronLeft, ChevronRight,
} from 'lucide-react';
import {
  listFarms, createFarm, updateFarm, deleteFarm,
} from '../services/farmService';
import FarmForm from '../components/FarmForm';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { useToast } from '../components/Toast';
import { useAuth } from '../context/AuthContext';
import { normalizeRole } from '../config/permissions';

const PAGE = 6;

export default function FarmManagement() {
  const toast = useToast();
  const { user } = useAuth();
  const location = useLocation();
  const role = normalizeRole(user?.role);
  const isWorker = role === 'field_worker';
  const [farms, setFarms] = useState([]);
  const [q, setQ] = useState('');
  const [crop, setCrop] = useState('');
  const [sort, setSort] = useState('newest');
  const [page, setPage] = useState(1);
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    try {
      setLoading(true);
      const data = await listFarms();
      setFarms(data);
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => {
    reload();
  }, [reload]);

  const crops = useMemo(
    () => [...new Set(farms.map((f) => f.crop).filter(Boolean))].sort(),
    [farms]
  );

  const filtered = useMemo(() => {
    const search = q.trim().toLowerCase();

    const result = farms.filter((f) =>
      (!search ||
        f.name.toLowerCase().includes(search) ||
        f.location.toLowerCase().includes(search)) &&
      (!crop || f.crop === crop)
    );

    const sortFunctions = {
      newest: (a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
      name: (a, b) => a.name.localeCompare(b.name),
      area: (a, b) => b.area - a.area,
    };

    return [...result].sort(sortFunctions[sort]);
  }, [farms, q, crop, sort]);

  const pages = Math.max(1, Math.ceil(filtered.length / PAGE));
  const currentPage = Math.min(page, pages);
  const rows = filtered.slice(
    (currentPage - 1) * PAGE,
    currentPage * PAGE
  );

  async function save(values) {
    try {
      if (editing === 'new') {
        await createFarm(values);
        toast('Farm added successfully.');
      } else {
        await updateFarm(editing.id, values);
        toast('Farm updated successfully.');
      }

      setEditing(null);
      await reload();
    } catch (error) {
      throw error;
    }
  }

  async function confirmDelete() {
    setBusy(true);

    try {
      await deleteFarm(deleting.id);
      toast(`Deleted "${deleting.name}".`);
      setDeleting(null);
      await reload();
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      setBusy(false);
    }
  }

  const reset = (setter) => (event) => {
    setter(event.target.value);
    setPage(1);
  };

  return (
    <>
      {location.state?.farmSetupError && <div className="alert error" role="alert">Your account was created, but the farm could not be saved: {location.state.farmSetupError}. You can add it again below.</div>}
      <div className="page-head">
        <div>
          <h1>Farm Management</h1>
          <p className="muted">{filtered.length} farm(s)</p>
        </div>

        {!isWorker && <button
          className="btn primary"
          onClick={() => setEditing('new')}
        >
          <Plus size={16} /> Add Farm
        </button>}
      </div>

      <div className="toolbar">
        <div className="search">
          <Search size={16} />
          <input
            placeholder="Search by farm name or location"
            aria-label="Search farms"
            value={q}
            onChange={reset(setQ)}
          />
        </div>

        <select
          aria-label="Filter by crop"
          value={crop}
          onChange={reset(setCrop)}
        >
          <option value="">All crops</option>
          {crops.map((item) => (
            <option key={item}>{item}</option>
          ))}
        </select>

        <select
          aria-label="Sort"
          value={sort}
          onChange={reset(setSort)}
        >
          <option value="newest">Newest first</option>
          <option value="name">Name A-Z</option>
          <option value="area">Largest area</option>
        </select>
      </div>

      {loading ? (
        <div className="empty"><p>Loading farms...</p></div>
      ) : rows.length === 0 ? (
        <div className="empty">
          <p>
            {farms.length
              ? 'No farms match your filters.'
              : 'No farms yet. Add your first farm.'}
          </p>
        </div>
      ) : (
        <div className="table-wrap card">
          <table>
            <thead>
              <tr>
                <th>Farm</th>
                <th>ID</th>
                <th>Location</th>
                <th>Area</th>
                <th>Soil</th>
                <th>Crop</th>
                <th>Status</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {rows.map((farm) => (
                <tr key={farm.id}>
                  <td>
                    <Link to={`/farms/${farm.id}`}>
                      <strong>{farm.name}</strong>
                    </Link>
                  </td>
                  <td>{farm.id}</td>
                  <td>{farm.location}</td>
                  <td>{farm.area} {farm.unit}</td>
                  <td>{farm.soil || '-'}</td>
                  <td>{farm.crop || '-'}</td>
                  <td>
                    <span className={`badge ${farm.status}`}>
                      {farm.status}
                    </span>
                  </td>
                  <td>
                    {farm.createdAt
                      ? new Date(farm.createdAt).toLocaleDateString()
                      : '-'}
                  </td>
                  <td className="row-actions">
                    <Link
                      className="icon-btn"
                      aria-label={`View ${farm.name}`}
                      to={`/farms/${farm.id}`}
                    >
                      <Eye size={16} />
                    </Link>

                    {(role === 'admin' || (role === 'farmer' && String(farm.ownerId) === String(user?.id))) && <>
                      <button className="icon-btn" aria-label={`Edit ${farm.name}`} onClick={() => setEditing(farm)}><Pencil size={16} /></button>
                      <button className="icon-btn danger" aria-label={`Delete ${farm.name}`} onClick={() => setDeleting(farm)}><Trash2 size={16} /></button>
                    </>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {pages > 1 && (
        <div className="pager">
          <button
            className="icon-btn"
            aria-label="Previous page"
            disabled={currentPage === 1}
            onClick={() => setPage(currentPage - 1)}
          >
            <ChevronLeft size={18} />
          </button>

          <span>Page {currentPage} of {pages}</span>

          <button
            className="icon-btn"
            aria-label="Next page"
            disabled={currentPage === pages}
            onClick={() => setPage(currentPage + 1)}
          >
            <ChevronRight size={18} />
          </button>
        </div>
      )}

      {editing && (
        <Modal
          wide
          title={editing === 'new' ? 'Add farm' : 'Edit farm'}
          onClose={() => setEditing(null)}
        >
          <FarmForm
            initial={editing === 'new' ? null : editing}
            onSubmit={save}
            onCancel={() => setEditing(null)}
          />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete farm"
          busy={busy}
          onCancel={() => setDeleting(null)}
          onConfirm={confirmDelete}
          message={
            <>
              <p>
                Delete <strong>{deleting.name}</strong> ({deleting.id})?
                This cannot be undone.
              </p>
            </>
          }
        />
      )}
    </>
  );
}
