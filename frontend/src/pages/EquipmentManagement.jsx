
import { useMemo, useState } from 'react';
import {
  Tractor,
  Plus,
  Search,
  Wrench,
  CalendarDays,
  Trash2,
  X,
  Pencil,
  Eye,
  MapPin,
  Gauge,
  CheckCircle2,
  Clock3,
  LayoutGrid,
  List,
  ArrowLeft,
  Save
} from 'lucide-react';

import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/equipment.css';

// Keep the same storage key to preserve your equipment records.
const STORAGE_KEY = 'fvn_equipment_v2';

// Each type now points to an actual local image file.

const EQUIPMENT_IMAGES = {
  Tractor: '/equipment/tractor.jpg',
  Harvester: '/equipment/harvester.jpg',
  Irrigation: '/equipment/irrigation.jpg',
  Sprayer: '/equipment/sprayer.jpg',
  Other: '/equipment/other.jpg',
};


const initialEquipment = [
  {
    id: 1,
    name: 'Mahindra Tractor',
    type: 'Tractor',
    farm: 'Green Valley Farm',
    status: 'Available',
    maintenance: '2026-11-15',
    model: 'Mahindra 575 DI',
    power: '45 HP',
    fuel: 'Diesel',
    year: '2023',
    condition: 'Excellent',
    description:
      'A powerful agricultural tractor used for ploughing, cultivation and transportation.'
  },
  {
    id: 2,
    name: 'Water Pump',
    type: 'Irrigation',
    farm: 'Riverbend Paddy Fields',
    status: 'In Use',
    maintenance: '2026-11-20',
    model: 'Agricultural Water Pump',
    power: '5 HP',
    fuel: 'Electric',
    year: '2024',
    condition: 'Good',
    description:
      'An irrigation pump used to supply water to agricultural fields.'
  },
  {
    id: 3,
    name: 'Combine Harvester',
    type: 'Harvester',
    farm: 'Green Valley Farm',
    status: 'Maintenance',
    maintenance: '2026-10-30',
    model: 'Combine Harvester',
    power: '150 HP',
    fuel: 'Diesel',
    year: '2022',
    condition: 'Under Service',
    description:
      'A harvesting machine used to cut, thresh and collect crops efficiently.'
  }
];

const emptyForm = {
  name: '',
  type: 'Tractor',
  farm: '',
  status: 'Available',
  maintenance: '',
  model: '',
  power: '',
  fuel: 'Diesel',
  year: '',
  condition: 'Good',
  description: '',
  image: ''
};

const statuses = [
  'Available',
  'In Use',
  'Maintenance'
];

const types = [
  'Tractor',
  'Harvester',
  'Irrigation',
  'Sprayer',
  'Other'
];

function readEquipment() {
  try {
    const saved = JSON.parse(
      localStorage.getItem(STORAGE_KEY)
    );

    return Array.isArray(saved)
      ? saved
      : initialEquipment;
  } catch {
    return initialEquipment;
  }
}

// Equipment images with automatic fallback.
function EquipmentImage({ item }) {
  const [failed, setFailed] = useState(false);

  const defaultImage =
    EQUIPMENT_IMAGES[item.type] ||
    EQUIPMENT_IMAGES.Other;

  // Old image URLs are ignored so previous wrong
  // Unsplash images do not continue appearing.
  // Local images are used unless the user enters
  // a new custom image URL.
  const customImage =
    item.image &&
    !item.image.includes('images.unsplash.com')
      ? item.image
      : '';

  const imageSource = customImage || defaultImage;

  return (
    <div className="equipment-image-wrap">
      {!failed ? (
        <img
          key={imageSource}
          src={imageSource}
          alt={`${item.name} machinery`}
          loading="lazy"
          onError={() => setFailed(true)}
        />
      ) : (
        <div className="equipment-image-fallback">
          <Tractor size={48} />
          <span>{item.type}</span>
          <small>Image unavailable</small>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ status }) {
  const statusClass =
    status === 'Available'
      ? 'available'
      : status === 'In Use'
        ? 'in-use'
        : 'maintenance';

  return (
    <span className={`equipment-status ${statusClass}`}>
      {status}
    </span>
  );
}

export default function EquipmentManagement() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const canManage =
    user?.role === 'admin' ||
    user?.role === 'farmer';

  const isFieldWorker =
    user?.role === 'fieldWorker';

  const [equipment, setEquipment] = useState(
    readEquipment
  );

  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('All');
  const [typeFilter, setTypeFilter] = useState('All');
  const [viewMode, setViewMode] = useState('grid');

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [selected, setSelected] = useState(null);

  function saveEquipment(updated) {
    setEquipment(updated);

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify(updated)
    );
  }

  // Field Workers only see equipment belonging
  // to their assigned farm.
  const visibleEquipment = useMemo(() => {
    if (!isFieldWorker) {
      return equipment;
    }

    const assignedFarm = (
      user?.assignedFarm || ''
    ).trim().toLowerCase();

    if (!assignedFarm) {
      return [];
    }

    return equipment.filter(
      (item) =>
        item.farm.trim().toLowerCase() ===
        assignedFarm
    );
  }, [equipment, isFieldWorker, user?.assignedFarm]);

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();

    return visibleEquipment.filter((item) => {
      const matchesSearch =
        `${item.name} ${item.type} ${item.farm} ${item.model || ''}`
          .toLowerCase()
          .includes(query);

      const matchesStatus =
        filter === 'All' ||
        item.status === filter;

      const matchesType =
        typeFilter === 'All' ||
        item.type === typeFilter;

      return (
        matchesSearch &&
        matchesStatus &&
        matchesType
      );
    });
  }, [
    visibleEquipment,
    search,
    filter,
    typeFilter
  ]);

  const stats = [
    {
      label: 'Total Equipment',
      value: visibleEquipment.length,
      Icon: Tractor,
      kind: 'total'
    },
    {
      label: 'Available',
      value: visibleEquipment.filter(
        (item) => item.status === 'Available'
      ).length,
      Icon: CheckCircle2,
      kind: 'available'
    },
    {
      label: 'In Use',
      value: visibleEquipment.filter(
        (item) => item.status === 'In Use'
      ).length,
      Icon: Clock3,
      kind: 'in-use'
    },
    {
      label: 'Under Maintenance',
      value: visibleEquipment.filter(
        (item) => item.status === 'Maintenance'
      ).length,
      Icon: Wrench,
      kind: 'maintenance'
    }
  ];

  function openAdd() {
    if (!canManage) return;

    setEditingId(null);
    setForm({ ...emptyForm });
    setShowForm(true);
  }

  function openEdit(item) {
    if (!canManage) return;

    setEditingId(item.id);
    setForm({
      ...emptyForm,
      ...item
    });

    setSelected(null);
    setShowForm(true);
  }

  function submitEquipment(event) {
    event.preventDefault();

    if (!canManage) return;

    const data = {
      ...form,
      name: form.name.trim(),
      farm: form.farm.trim()
    };

    if (!data.name || !data.farm) return;

    if (editingId !== null) {
      saveEquipment(
        equipment.map((item) =>
          item.id === editingId
            ? {
                ...item,
                ...data,
                id: item.id
              }
            : item
        )
      );
    } else {
      saveEquipment([
        ...equipment,
        {
          ...data,
          id: Date.now()
        }
      ]);
    }

    setShowForm(false);
    setEditingId(null);
    setForm({ ...emptyForm });
  }

  function deleteEquipment(id) {
    if (!canManage) return;

    if (window.confirm('Delete this equipment?')) {
      saveEquipment(
        equipment.filter(
          (item) => item.id !== id
        )
      );

      setSelected(null);
    }
  }

  function updateStatus(id, status) {
    if (!canManage) return;

    saveEquipment(
      equipment.map((item) =>
        item.id === id
          ? { ...item, status }
          : item
      )
    );

    setSelected((previous) =>
      previous?.id === id
        ? { ...previous, status }
        : previous
    );
  }

  function updateForm(name, value) {
    setForm((previous) => ({
      ...previous,
      [name]: value
    }));
  }

  function openBookings() {
    navigate('/equipment/bookings');
  }

  return (
    <div className="equipment-page">
      <div className="equipment-heading">
        <div>
          <div className="equipment-eyebrow">
            <Tractor size={16} />
            SMART FARM OPERATIONS
          </div>

          <h1>Equipment Management</h1>

          <p>
            Monitor farm machinery, track availability,
            and manage equipment maintenance.
          </p>
        </div>

        {canManage && (
          <button
            className="equipment-add"
            onClick={openAdd}
          >
            <Plus size={18} />
            Add Equipment
          </button>
        )}
      </div>

      <div className="equipment-stats">
        {stats.map(
          ({ label, value, Icon, kind }) => (
            <div
              className={`equipment-stat equipment-stat-${kind}`}
              key={label}
            >
              <div className="equipment-stat-icon">
                <Icon size={25} />
              </div>

              <div>
                <h2>{value}</h2>
                <p>{label}</p>
              </div>
            </div>
          )
        )}
      </div>

      <div className="equipment-section-heading">
        <div>
          <h2>Equipment Collection</h2>
          <p>{filtered.length} equipment record(s)</p>
        </div>

        <div className="equipment-view-switch">
          <button
            className={
              viewMode === 'grid' ? 'active' : ''
            }
            onClick={() => setViewMode('grid')}
            title="Grid view"
            aria-label="Grid view"
          >
            <LayoutGrid size={18} />
          </button>

          <button
            className={
              viewMode === 'list' ? 'active' : ''
            }
            onClick={() => setViewMode('list')}
            title="Table view"
            aria-label="Table view"
          >
            <List size={18} />
          </button>
        </div>
      </div>

      <div className="equipment-toolbar">
        <div className="equipment-search">
          <Search size={18} />

          <input
            placeholder="Search equipment, type or farm..."
            value={search}
            onChange={(event) =>
              setSearch(event.target.value)
            }
          />
        </div>

        <select
          value={typeFilter}
          onChange={(event) =>
            setTypeFilter(event.target.value)
          }
          aria-label="Filter by equipment type"
        >
          <option value="All">All Types</option>

          {types.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>

        <select
          value={filter}
          onChange={(event) =>
            setFilter(event.target.value)
          }
          aria-label="Filter by equipment status"
        >
          <option value="All">All Statuses</option>

          {statuses.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
      </div>

      {isFieldWorker && (
        <div className="equipment-worker-notice">
          <MapPin size={18} />
          Showing equipment for your assigned farm:
          <strong>
            {user?.assignedFarm || 'Not assigned'}
          </strong>
        </div>
      )}

      {filtered.length === 0 ? (
        <div className="equipment-empty">
          <Tractor size={42} />
          <h3>No equipment found</h3>
          <p>
            Try changing your search or filters.
          </p>
        </div>
      ) : viewMode === 'grid' ? (
        <div className="equipment-grid">
          {filtered.map((item) => (
            <article
              className="equipment-card"
              key={item.id}
            >
              <EquipmentImage item={item} />

              <div className="equipment-card-content">
                <div className="equipment-card-top">
                  <span className="equipment-type">
                    {item.type}
                  </span>

                  <StatusBadge status={item.status} />
                </div>

                <h3>{item.name}</h3>

                <div className="equipment-meta">
                  <span>
                    <MapPin size={15} />
                    {item.farm}
                  </span>

                  <span>
                    <Gauge size={15} />
                    {item.power || 'Not specified'}
                  </span>

                  <span>
                    <CalendarDays size={15} />
                    Service: {item.maintenance || 'Not set'}
                  </span>
                </div>

                <div className="equipment-card-actions">
                  <button
                    className="equipment-outline-btn"
                    onClick={() => setSelected(item)}
                  >
                    <Eye size={15} />
                    View Details
                  </button>

                  {canManage && (
                    <button
                      className="equipment-outline-btn"
                      onClick={() => openEdit(item)}
                    >
                      <Pencil size={15} />
                      Edit
                    </button>
                  )}

                  {item.status === 'Available' && (
                    <button
                      className="equipment-book-btn"
                      onClick={openBookings}
                    >
                      <CalendarDays size={15} />
                      Book
                    </button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <div className="equipment-table-wrap">
          <table className="equipment-table">
            <thead>
              <tr>
                <th>Equipment</th>
                <th>Type</th>
                <th>Farm</th>
                <th>Status</th>
                <th>Next Maintenance</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {filtered.map((item) => (
                <tr key={item.id}>
                  <td>
                    <strong>{item.name}</strong>
                  </td>

                  <td>{item.type}</td>
                  <td>{item.farm}</td>

                  <td>
                    {canManage ? (
                      <select
                        value={item.status}
                        onChange={(event) =>
                          updateStatus(
                            item.id,
                            event.target.value
                          )
                        }
                      >
                        {statuses.map((status) => (
                          <option key={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <StatusBadge status={item.status} />
                    )}
                  </td>

                  <td>
                    {item.maintenance || 'Not scheduled'}
                  </td>

                  <td>
                    <div className="equipment-row-actions">
                      <button
                        onClick={() => setSelected(item)}
                        title="View details"
                      >
                        <Eye size={17} />
                      </button>

                      {canManage && (
                        <>
                          <button
                            onClick={() => openEdit(item)}
                            title="Edit equipment"
                          >
                            <Pencil size={17} />
                          </button>

                          <button
                            onClick={() =>
                              deleteEquipment(item.id)
                            }
                            title="Delete equipment"
                          >
                            <Trash2 size={17} />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selected && (
        <div
          className="equipment-overlay"
          onClick={() => setSelected(null)}
        >
          <div
            className="equipment-modal equipment-details-modal"
            role="dialog"
            aria-modal="true"
            aria-label="Equipment details"
            onClick={(event) =>
              event.stopPropagation()
            }
          >
            <div className="equipment-modal-header">
              <button
                className="equipment-back-btn"
                onClick={() => setSelected(null)}
              >
                <ArrowLeft size={17} />
                Back
              </button>

              <button
                type="button"
                onClick={() => setSelected(null)}
                aria-label="Close details"
              >
                <X size={20} />
              </button>
            </div>

            <EquipmentImage item={selected} />

            <div className="equipment-details-title">
              <div>
                <h2>{selected.name}</h2>
                <p>{selected.type}</p>
              </div>

              <StatusBadge status={selected.status} />
            </div>

            <div className="equipment-details-grid">
              <div>
                <span>Assigned Farm</span>
                <strong>{selected.farm}</strong>
              </div>

              <div>
                <span>Model</span>
                <strong>
                  {selected.model || 'Not specified'}
                </strong>
              </div>

              <div>
                <span>Power</span>
                <strong>
                  {selected.power || 'Not specified'}
                </strong>
              </div>

              <div>
                <span>Fuel Type</span>
                <strong>
                  {selected.fuel || 'Not specified'}
                </strong>
              </div>

              <div>
                <span>Manufacturing Year</span>
                <strong>
                  {selected.year || 'Not specified'}
                </strong>
              </div>

              <div>
                <span>Condition</span>
                <strong>
                  {selected.condition || 'Not specified'}
                </strong>
              </div>

              <div>
                <span>Next Maintenance</span>
                <strong>
                  {selected.maintenance || 'Not scheduled'}
                </strong>
              </div>
            </div>

            <div className="equipment-description">
              <h3>Description</h3>

              <p>
                {selected.description ||
                  'No description available.'}
              </p>
            </div>

            <div className="equipment-detail-actions">
              {selected.status === 'Available' && (
                <button
                  className="equipment-book-btn"
                  onClick={openBookings}
                >
                  <CalendarDays size={17} />
                  Book Equipment
                </button>
              )}

              {canManage && (
                <>
                  <button
                    className="equipment-outline-btn"
                    onClick={() => openEdit(selected)}
                  >
                    <Pencil size={17} />
                    Edit Details
                  </button>

                  <button
                    className="equipment-outline-btn"
                    onClick={() =>
                      updateStatus(
                        selected.id,
                        'Maintenance'
                      )
                    }
                  >
                    <Wrench size={17} />
                    Mark Maintenance
                  </button>

                  <button
                    className="equipment-delete"
                    onClick={() =>
                      deleteEquipment(selected.id)
                    }
                  >
                    <Trash2 size={17} />
                    Delete
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {showForm && canManage && (
        <div
          className="equipment-overlay"
          onClick={() => setShowForm(false)}
        >
          <form
            className="equipment-modal equipment-form-modal"
            onSubmit={submitEquipment}
            onClick={(event) =>
              event.stopPropagation()
            }
          >
            <div className="equipment-modal-header">
              <h2>
                {editingId !== null
                  ? 'Edit Equipment'
                  : 'Add New Equipment'}
              </h2>

              <button
                type="button"
                onClick={() => setShowForm(false)}
                aria-label="Close form"
              >
                <X size={20} />
              </button>
            </div>

            <div className="equipment-form-grid">
              <div>
                <label>Equipment Name</label>
                <input
                  required
                  value={form.name}
                  onChange={(event) =>
                    updateForm(
                      'name',
                      event.target.value
                    )
                  }
                  placeholder="Enter equipment name"
                />
              </div>

              <div>
                <label>Equipment Type</label>
                <select
                  value={form.type}
                  onChange={(event) =>
                    updateForm(
                      'type',
                      event.target.value
                    )
                  }
                >
                  {types.map((type) => (
                    <option key={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label>Farm Name</label>
                <input
                  required
                  value={form.farm}
                  onChange={(event) =>
                    updateForm(
                      'farm',
                      event.target.value
                    )
                  }
                  placeholder="Enter farm name"
                />
              </div>

              <div>
                <label>Status</label>
                <select
                  value={form.status}
                  onChange={(event) =>
                    updateForm(
                      'status',
                      event.target.value
                    )
                  }
                >
                  {statuses.map((status) => (
                    <option key={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label>Model</label>
                <input
                  value={form.model}
                  onChange={(event) =>
                    updateForm(
                      'model',
                      event.target.value
                    )
                  }
                  placeholder="e.g. Mahindra 575 DI"
                />
              </div>

              <div>
                <label>Power</label>
                <input
                  value={form.power}
                  onChange={(event) =>
                    updateForm(
                      'power',
                      event.target.value
                    )
                  }
                  placeholder="e.g. 45 HP"
                />
              </div>

              <div>
                <label>Fuel Type</label>
                <select
                  value={form.fuel}
                  onChange={(event) =>
                    updateForm(
                      'fuel',
                      event.target.value
                    )
                  }
                >
                  <option>Diesel</option>
                  <option>Petrol</option>
                  <option>Electric</option>
                  <option>Manual</option>
                  <option>Other</option>
                </select>
              </div>

              <div>
                <label>Manufacturing Year</label>
                <input
                  type="number"
                  min="1950"
                  max="2100"
                  value={form.year}
                  onChange={(event) =>
                    updateForm(
                      'year',
                      event.target.value
                    )
                  }
                  placeholder="2024"
                />
              </div>

              <div>
                <label>Condition</label>
                <select
                  value={form.condition}
                  onChange={(event) =>
                    updateForm(
                      'condition',
                      event.target.value
                    )
                  }
                >
                  <option>Excellent</option>
                  <option>Good</option>
                  <option>Needs Repair</option>
                  <option>Under Service</option>
                </select>
              </div>

              <div>
                <label>Next Maintenance Date</label>
                <input
                  type="date"
                  value={form.maintenance}
                  onChange={(event) =>
                    updateForm(
                      'maintenance',
                      event.target.value
                    )
                  }
                />
              </div>

              <div className="equipment-full-field">
                <label>Equipment Image URL</label>
                <input
                  type="url"
                  value={form.image || ''}
                  onChange={(event) =>
                    updateForm(
                      'image',
                      event.target.value
                    )
                  }
                  placeholder="Optional custom image URL"
                />
              </div>

              <div className="equipment-full-field">
                <label>Description</label>
                <textarea
                  rows={3}
                  value={form.description || ''}
                  onChange={(event) =>
                    updateForm(
                      'description',
                      event.target.value
                    )
                  }
                  placeholder="Describe this equipment..."
                />
              </div>
            </div>

            <button
              type="submit"
              className="equipment-add equipment-save-btn"
            >
              <Save size={18} />
              {editingId !== null
                ? 'Save Changes'
                : 'Save Equipment'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
