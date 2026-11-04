
import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Download,
  Plus,
  Trash2,
  Upload,
  Pencil,
  X,
} from 'lucide-react';

import {
  getFarm,
  updateFarm,
  deleteFarm,
} from '../services/farmService';

import FarmForm from '../components/FarmForm';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { useToast } from '../components/Toast';
import { useAuth } from '../context/AuthContext';
import { normalizeRole } from '../config/permissions';

import {
  listPlots,
  createPlot,
  deletePlot,
  listSoilRecords,
  createSoilRecord,
  updateSoilRecord,
  listDocuments,
  uploadDocument,
  downloadDocument,
  deleteDocument,
  listWorkers,
  listAvailableWorkers,
  assignWorker,
  removeWorker,
} from '../services/farmDetailsService';

// Return the current local date and time for datetime-local inputs.
const getLocalDateTime = () => {
  const now = new Date();
  const offset = now.getTimezoneOffset();
  return new Date(now.getTime() - offset * 60000)
    .toISOString()
    .slice(0, 16);
};

// Convert an existing date into the format required by datetime-local.
const formatDateTime = (value) => {
  if (!value) return '';

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return String(value).slice(0, 16);
  }

  const offset = date.getTimezoneOffset();

  return new Date(date.getTime() - offset * 60000)
    .toISOString()
    .slice(0, 16);
};

const emptySoilForm = () => ({
  phLevel: '',
  nitrogen: '',
  phosphorus: '',
  potassium: '',
  organicMatter: '',
  moisture: '',
  recordedAt: getLocalDateTime(),
  notes: '',
});

export default function FarmDetails() {
  const { id } = useParams();
  const toast = useToast();
  const { user } = useAuth();

  const role = normalizeRole(user?.role);
  const isWorker = role === 'field_worker';

  const [farm, setFarm] = useState(null);
  const [loadError, setLoadError] = useState('');

  const [plots, setPlots] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [available, setAvailable] = useState([]);

  const [selectedPlot, setSelectedPlot] = useState('');
  const [soilRecords, setSoilRecords] = useState([]);

  const [tab, setTab] = useState('plots');

  const [edit, setEdit] = useState(false);
  const [del, setDel] = useState(false);

  const [plotForm, setPlotForm] = useState({
    name: '',
    plotCode: '',
    area: '',
    areaUnit: 'ACRE',
    soilType: '',
    description: '',
  });

  const [soilForm, setSoilForm] = useState(emptySoilForm);
  const [editingSoilId, setEditingSoilId] = useState(null);
  const [soilSaving, setSoilSaving] = useState(false);

  const [workerId, setWorkerId] = useState('');
  const [file, setFile] = useState(null);
  const [description, setDescription] = useState('');

  const canManage =
    role === 'admin' ||
    (role === 'farmer' &&
      String(user?.id) === String(farm?.ownerId));

  // Load farm, plots and other farm information.
  const load = useCallback(async () => {
    setLoadError('');

    try {
      const f = await getFarm(id);
      setFarm(f);

      const p = await listPlots(id);
      setPlots(p);
    } catch (e) {
      setLoadError(e.message || 'Unable to load this farm.');
      setFarm(false);
      return;
    }

    if (role !== 'field_worker') {
      try {
        const [d, w] = await Promise.all([
          listDocuments(id),
          listWorkers(id),
        ]);

        setDocuments(d);
        setWorkers(w);

        try {
          setAvailable(await listAvailableWorkers(id));
        } catch {
          setAvailable([]);
        }
      } catch (e) {
        toast(e.message, 'error');
      }
    } else {
      setDocuments([]);
      setWorkers([]);
      setAvailable([]);
    }
  }, [id, role, toast]);

  useEffect(() => {
    load();
  }, [load]);

  // Automatically select the first plot when opening the Soil tab.
  useEffect(() => {
    if (tab !== 'soil') return;

    if (plots.length === 0) {
      setSelectedPlot('');
      setSoilRecords([]);
      return;
    }

    if (
      !selectedPlot ||
      !plots.some((p) => String(p.id) === String(selectedPlot))
    ) {
      setSelectedPlot(String(plots[0].id));
      return;
    }

    let cancelled = false;

    async function loadSoilRecords() {
      try {
        const records = await listSoilRecords(id, selectedPlot);

        if (!cancelled) {
          setSoilRecords(records);
        }
      } catch (e) {
        if (!cancelled) {
          setSoilRecords([]);
          toast(e.message, 'error');
        }
      }
    }

    loadSoilRecords();

    return () => {
      cancelled = true;
    };
  }, [id, tab, plots, selectedPlot, toast]);

  if (farm === null) {
    return <p className="muted">Loading farm details…</p>;
  }

  if (farm === false) {
    return (
      <div className="empty big">
        <h2>Unable to open farm</h2>
        <p>{loadError}</p>
        <Link className="btn primary" to="/farms">
          Back to farms
        </Link>
      </div>
    );
  }

  async function save(v) {
    setFarm(await updateFarm(farm.id, v));
    setEdit(false);
    toast('Farm updated successfully.');
  }

  async function remove() {
    try {
      await deleteFarm(farm.id);
      toast('Farm deleted.');
      window.location.assign('/farms');
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  async function addPlot(e) {
    e.preventDefault();

    try {
      await createPlot(id, {
        ...plotForm,
        area: Number(plotForm.area),
      });

      setPlotForm({
        name: '',
        plotCode: '',
        area: '',
        areaUnit: 'ACRE',
        soilType: '',
        description: '',
      });

      await load();
      toast('Plot added.');
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  // Add a new soil record or update an existing one.
  async function saveSoil(e) {
    e.preventDefault();

    if (!selectedPlot) {
      toast('Please select a plot first.', 'error');
      return;
    }

    const payload = Object.fromEntries(
      Object.entries(soilForm).map(([key, value]) => [
        key,
        value === '' ? null : value,
      ])
    );

    for (const key of [
      'phLevel',
      'nitrogen',
      'phosphorus',
      'potassium',
      'organicMatter',
      'moisture',
    ]) {
      if (payload[key] !== null) {
        payload[key] = Number(payload[key]);
      }
    }

    payload.recordedAt = payload.recordedAt || null;

    setSoilSaving(true);

    try {
      if (editingSoilId !== null) {
        await updateSoilRecord(
          id,
          selectedPlot,
          editingSoilId,
          payload
        );

        toast('Soil record updated successfully.');
      } else {
        await createSoilRecord(id, selectedPlot, payload);

        toast('Soil test saved successfully.');
      }

      const records = await listSoilRecords(id, selectedPlot);
      setSoilRecords(records);

      setEditingSoilId(null);
      setSoilForm(emptySoilForm);
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      setSoilSaving(false);
    }
  }

  // Fill the form with the selected record's existing values.
  function startSoilEdit(record) {
    setEditingSoilId(record.id);

    setSoilForm({
      phLevel: record.phLevel ?? '',
      nitrogen: record.nitrogen ?? '',
      phosphorus: record.phosphorus ?? '',
      potassium: record.potassium ?? '',
      organicMatter: record.organicMatter ?? '',
      moisture: record.moisture ?? '',
      recordedAt: formatDateTime(record.recordedAt),
      notes: record.notes ?? '',
    });

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  function cancelSoilEdit() {
    setEditingSoilId(null);
    setSoilForm(emptySoilForm());
  }

  async function addDocument(e) {
    e.preventDefault();

    if (!file) {
      return toast('Choose a document first.', 'error');
    }

    try {
      await uploadDocument(id, file, description);

      setFile(null);
      setDescription('');
      setDocuments(await listDocuments(id));

      toast('Document uploaded.');
    } catch (err) {
      toast(err.message, 'error');
    }
  }

  async function addWorker(e) {
    e.preventDefault();

    if (!workerId) return;

    try {
      await assignWorker(id, {
        workerId: Number(workerId),
        assignmentRole: 'Field Worker',
        status: 'ACTIVE',
      });

      setWorkers(await listWorkers(id));
      setWorkerId('');

      toast('Field worker assigned.');
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  const inputStyle = {
    width: '100%',
    padding: '10px',
    border: '1px solid var(--line, #d9e2d9)',
    borderRadius: 8,
    margin: '4px 0 10px',
  };

  const tabs = isWorker
    ? ['plots', 'soil']
    : ['plots', 'soil', 'documents', 'workers'];

  return (
    <>
      <Link className="link-btn" to="/farms">
        <ArrowLeft size={16} /> Back to farms
      </Link>

      <div className="page-head">
        <div>
          <h1>{farm.name}</h1>
          <span className={`badge ${farm.status}`}>
            {farm.status}
          </span>
          <p className="muted">
            {farm.location} · {farm.area} {farm.unit} ·{' '}
            {farm.crop || 'No primary crop'}
          </p>
        </div>

        {canManage && (
          <div className="actions-row">
            <button
              className="btn ghost"
              onClick={() => setEdit(true)}
            >
              Edit farm
            </button>

            <button
              className="btn danger"
              onClick={() => setDel(true)}
            >
              Delete farm
            </button>
          </div>
        )}
      </div>

      <div className="card">
        <dl className="details">
          {[
            ['Owner', farm.ownerName || '—'],
            ['Farm ID', farm.id],
            ['Location', farm.location],
            ['Area', `${farm.area} ${farm.unit}`],
            ['Soil type', farm.soil || '—'],
            ['Primary crop', farm.crop || '—'],
            ['Plots', plots.length],
            [
              'Created',
              farm.createdAt
                ? new Date(farm.createdAt).toLocaleString()
                : '—',
            ],
          ].map(([key, value]) => (
            <div key={key}>
              <dt>{key}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>

        <p>{farm.notes || 'No additional notes.'}</p>
      </div>

      <div className="toolbar" style={{ marginTop: 20 }}>
        {tabs.map((t) => (
          <button
            key={t}
            className={`btn ${tab === t ? 'primary' : 'ghost'}`}
            onClick={() => setTab(t)}
          >
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {/* PLOTS TAB */}
      {tab === 'plots' && (
        <section className="card">
          <h2>Farm plots</h2>

          {plots.length === 0 ? (
            <p className="muted">No plots added yet.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Code</th>
                    <th>Area</th>
                    <th>Soil type</th>
                    <th>Status</th>
                    {canManage && <th>Action</th>}
                  </tr>
                </thead>

                <tbody>
                  {plots.map((p) => (
                    <tr key={p.id}>
                      <td>{p.name}</td>
                      <td>{p.plotCode || '—'}</td>
                      <td>{p.area} {p.areaUnit}</td>
                      <td>{p.soilType || '—'}</td>
                      <td>{p.status}</td>

                      {canManage && (
                        <td>
                          <button
                            className="btn danger"
                            onClick={async () => {
                              try {
                                await deletePlot(id, p.id);
                                await load();
                                toast('Plot deleted.');
                              } catch (e) {
                                toast(e.message, 'error');
                              }
                            }}
                          >
                            <Trash2 size={14} />
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {canManage && (
            <form
              onSubmit={addPlot}
              className="grid-form"
              style={{ marginTop: 18 }}
            >
              <h3 style={{ gridColumn: '1/-1' }}>Add plot</h3>

              {[
                ['name', 'Plot name', 'text'],
                ['plotCode', 'Plot code', 'text'],
                ['area', 'Area', 'number'],
                ['soilType', 'Soil type', 'text'],
                ['description', 'Description', 'text'],
              ].map(([key, label, type]) => (
                <label key={key}>
                  {label}
                  <input
                    required={key === 'name' || key === 'area'}
                    type={type}
                    style={inputStyle}
                    value={plotForm[key]}
                    onChange={(e) =>
                      setPlotForm((v) => ({
                        ...v,
                        [key]: e.target.value,
                      }))
                    }
                  />
                </label>
              ))}

              <label>
                Area unit
                <select
                  style={inputStyle}
                  value={plotForm.areaUnit}
                  onChange={(e) =>
                    setPlotForm((v) => ({
                      ...v,
                      areaUnit: e.target.value,
                    }))
                  }
                >
                  <option value="ACRE">Acres</option>
                  <option value="HECTARE">Hectares</option>
                  <option value="SQ_METER">Square metres</option>
                </select>
              </label>

              <button className="btn primary">
                <Plus size={15} /> Add plot
              </button>
            </form>
          )}
        </section>
      )}

      {/* SOIL TAB */}
      {tab === 'soil' && (
        <section className="card">
          <h2>Soil test history</h2>

          <label>
            Select plot
            <select
              style={inputStyle}
              value={selectedPlot}
              onChange={(e) => {
                setSelectedPlot(e.target.value);
                setEditingSoilId(null);
                setSoilForm(emptySoilForm());
              }}
            >
              <option value="">Choose plot</option>
              {plots.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </label>

          {plots.length === 0 && (
            <p className="muted">
              No plots are available for soil testing.
            </p>
          )}

          {selectedPlot && (
            <>
              <form onSubmit={saveSoil} className="grid-form">
                <h3 style={{ gridColumn: '1/-1' }}>
                  {editingSoilId !== null
                    ? 'Edit soil record'
                    : 'Add new soil test'}
                </h3>

                {[
                  ['phLevel', 'pH'],
                  ['nitrogen', 'Nitrogen'],
                  ['phosphorus', 'Phosphorus'],
                  ['potassium', 'Potassium'],
                  ['organicMatter', 'Organic matter'],
                  ['moisture', 'Moisture'],
                ].map(([key, label]) => (
                  <label key={key}>
                    {label}
                    <input
                      type="number"
                      step="0.01"
                      style={inputStyle}
                      value={soilForm[key]}
                      onChange={(e) =>
                        setSoilForm((v) => ({
                          ...v,
                          [key]: e.target.value,
                        }))
                      }
                    />
                  </label>
                ))}

                <label>
                  Recorded at
                  <input
                    type="datetime-local"
                    style={inputStyle}
                    value={soilForm.recordedAt}
                    onChange={(e) =>
                      setSoilForm((v) => ({
                        ...v,
                        recordedAt: e.target.value,
                      }))
                    }
                  />
                </label>

                <label>
                  Notes
                  <input
                    style={inputStyle}
                    value={soilForm.notes}
                    onChange={(e) =>
                      setSoilForm((v) => ({
                        ...v,
                        notes: e.target.value,
                      }))
                    }
                  />
                </label>

                <div
                  className="actions-row"
                  style={{ gridColumn: '1/-1' }}
                >
                  <button
                    type="submit"
                    className="btn primary"
                    disabled={soilSaving}
                  >
                    {soilSaving
                      ? 'Saving...'
                      : editingSoilId !== null
                        ? 'Save changes'
                        : 'Save soil test'}
                  </button>

                  {editingSoilId !== null && (
                    <button
                      type="button"
                      className="btn ghost"
                      onClick={cancelSoilEdit}
                      disabled={soilSaving}
                    >
                      <X size={15} /> Cancel editing
                    </button>
                  )}
                </div>
              </form>

              <h3 style={{ marginTop: 20 }}>Previous records</h3>

              {soilRecords.length === 0 ? (
                <p className="muted">No soil test records.</p>
              ) : (
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>pH</th>
                        <th>N</th>
                        <th>P</th>
                        <th>K</th>
                        <th>Organic matter</th>
                        <th>Moisture</th>
                        <th>Action</th>
                      </tr>
                    </thead>

                    <tbody>
                      {soilRecords.map((r) => (
                        <tr key={r.id}>
                          <td>
                            {r.recordedAt
                              ? new Date(r.recordedAt).toLocaleDateString()
                              : '—'}
                          </td>

                          <td>{r.phLevel ?? '—'}</td>
                          <td>{r.nitrogen ?? '—'}</td>
                          <td>{r.phosphorus ?? '—'}</td>
                          <td>{r.potassium ?? '—'}</td>
                          <td>{r.organicMatter ?? '—'}</td>
                          <td>{r.moisture ?? '—'}</td>

                          <td>
                            <button
                              type="button"
                              className="btn ghost"
                              title="Edit soil record"
                              onClick={() => startSoilEdit(r)}
                            >
                              <Pencil size={14} /> Edit
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}
        </section>
      )}

      {/* DOCUMENTS TAB */}
      {tab === 'documents' && (
        <section className="card">
          <h2>Land documents</h2>

          {canManage && (
            <form onSubmit={addDocument} className="grid-form">
              <label>
                Choose file (PDF, JPG, PNG, DOC, DOCX; max 10 MB)
                <input
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
                  style={inputStyle}
                  onChange={(e) =>
                    setFile(e.target.files?.[0] || null)
                  }
                />
              </label>

              <label>
                Description
                <input
                  style={inputStyle}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </label>

              <button className="btn primary">
                <Upload size={15} /> Upload
              </button>
            </form>
          )}

          {documents.length === 0 ? (
            <p className="muted">No documents uploaded.</p>
          ) : (
            documents.map((d) => (
              <div
                key={d.id}
                className="row-between"
                style={{
                  padding: '10px 0',
                  borderBottom: '1px solid var(--line)',
                }}
              >
                <span>
                  {d.fileName}{' '}
                  <small className="muted">{d.description}</small>
                </span>

                <div className="actions-row">
                  <button
                    className="btn ghost"
                    onClick={() =>
                      downloadDocument(id, d.id, d.fileName)
                        .catch((e) => toast(e.message, 'error'))
                    }
                  >
                    <Download size={15} /> Download
                  </button>

                  {canManage && (
                    <button
                      className="btn danger"
                      onClick={async () => {
                        try {
                          await deleteDocument(id, d.id);
                          setDocuments(await listDocuments(id));
                          toast('Document deleted.');
                        } catch (e) {
                          toast(e.message, 'error');
                        }
                      }}
                    >
                      <Trash2 size={14} />
                    </button>
                  )}
                </div>
              </div>
            ))
          )}
        </section>
      )}

      {/* WORKERS TAB */}
      {tab === 'workers' && (
        <section className="card">
          <h2>Assigned field workers</h2>

          {canManage && (
            <form
              onSubmit={addWorker}
              className="row"
              style={{ gap: 10, marginBottom: 16 }}
            >
              <select
                style={inputStyle}
                value={workerId}
                onChange={(e) => setWorkerId(e.target.value)}
              >
                <option value="">Select active field worker</option>

                {available.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.name} ({w.email})
                  </option>
                ))}
              </select>

              <button className="btn primary">Assign</button>
            </form>
          )}

          {workers.length === 0 ? (
            <p className="muted">No field workers assigned.</p>
          ) : (
            workers.map((w) => (
              <div
                key={w.id}
                className="row-between"
                style={{
                  padding: '10px 0',
                  borderBottom: '1px solid var(--line)',
                }}
              >
                <span>
                  <strong>{w.workerName}</strong>
                  <small className="muted">
                    {' '}
                    {w.workerEmail} · {w.status}
                  </small>
                </span>

                {canManage && (
                  <button
                    className="btn danger"
                    onClick={async () => {
                      try {
                        await removeWorker(id, w.id);
                        setWorkers(await listWorkers(id));
                        toast('Assignment removed.');
                      } catch (e) {
                        toast(e.message, 'error');
                      }
                    }}
                  >
                    <Trash2 size={14} />
                  </button>
                )}
              </div>
            ))
          )}
        </section>
      )}

      {/* EDIT FARM MODAL */}
      {edit && (
        <Modal
          wide
          title="Edit farm"
          onClose={() => setEdit(false)}
        >
          <FarmForm
            initial={farm}
            onSubmit={save}
            onCancel={() => setEdit(false)}
          />
        </Modal>
      )}

      {/* DELETE FARM CONFIRMATION */}
      {del && (
        <ConfirmDialog
          title="Delete farm"
          onCancel={() => setDel(false)}
          onConfirm={remove}
          message={
            <p>
              Delete <strong>{farm.name}</strong>? Remove dependent
              plots first.
            </p>
          }
        />
      )}
    </>
  );
}
