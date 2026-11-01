import { useMemo, useRef, useState } from "react";
import "../styles/inventory.css";

const INITIAL_ITEMS = [
  { id: 1, name: "Rice Seeds", category: "Seeds", quantity: 120, unit: "kg", minimum: 30 },
  { id: 2, name: "Organic Fertilizer", category: "Fertilizers", quantity: 15, unit: "kg", minimum: 25 },
  { id: 3, name: "Neem Pesticide", category: "Pesticides", quantity: 40, unit: "L", minimum: 10 },
];

const EMPTY_FORM = { name: "", category: "Seeds", quantity: "", unit: "kg", minimum: "" };

const CATEGORIES = ["Seeds", "Fertilizers", "Pesticides", "Tools", "Other"];

// Files must live in frontend/public/inventory/
const CATEGORY_IMAGES = {
  Seeds: "/inventory/seeds.jpg",
  Fertilizers: "/inventory/fertilizer.jpg",
  Pesticides: "/inventory/pesticide.jpg",
  Tools: "/inventory/tools.jpg",
  Other: "/inventory/farm_supplies.jpg",
};

function getStockStatus(item) {
  if (Number(item.quantity) <= Number(item.minimum)) return "Low Stock";
  if (Number(item.quantity) <= Number(item.minimum) * 1.5) return "Running Low";
  return "In Stock";
}

function InventoryImage({ src, alt, className = "" }) {
  const [failed, setFailed] = useState(false);

  if (failed) {
    return (
      <div className={`inventory-image-fallback ${className}`} role="img" aria-label={alt}>
        🌱
      </div>
    );
  }

  return <img className={className} src={src} alt={alt} onError={() => setFailed(true)} />;
}

export default function InventoryManagement() {
  const [items, setItems] = useState(INITIAL_ITEMS);
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("All Categories");
  const [statusFilter, setStatusFilter] = useState("All Statuses");
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const panelRef = useRef(null);

  const lowStockCount = items.filter((i) => Number(i.quantity) <= Number(i.minimum)).length;
  const totalUnits = items.reduce((sum, i) => sum + Number(i.quantity || 0), 0);
  const categoryCount = new Set(items.map((i) => i.category)).size;

  const filteredItems = useMemo(() => {
    const text = search.toLowerCase();
    return items.filter((item) => {
      const matchesSearch =
        item.name.toLowerCase().includes(text) || item.category.toLowerCase().includes(text);
      const matchesCategory = categoryFilter === "All Categories" || item.category === categoryFilter;
      const matchesStatus = statusFilter === "All Statuses" || getStockStatus(item) === statusFilter;
      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [items, search, categoryFilter, statusFilter]);

  function openAddModal() {
    setEditingItem(null);
    setForm(EMPTY_FORM);
    setModalOpen(true);
  }

  function openEditModal(item) {
    setEditingItem(item);
    setForm({
      name: item.name,
      category: item.category,
      quantity: String(item.quantity),
      unit: item.unit,
      minimum: String(item.minimum),
    });
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
    setEditingItem(null);
    setForm(EMPTY_FORM);
  }

  function handleFormChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    const itemData = {
      name: form.name.trim(),
      category: form.category,
      quantity: Number(form.quantity),
      unit: form.unit.trim() || "units",
      minimum: Number(form.minimum),
    };

    if (
      !itemData.name ||
      form.quantity === "" ||
      form.minimum === "" ||
      itemData.quantity < 0 ||
      itemData.minimum < 0
    ) {
      return;
    }

    if (editingItem) {
      setItems((prev) => prev.map((i) => (i.id === editingItem.id ? { ...i, ...itemData } : i)));
    } else {
      setItems((prev) => [...prev, { id: Date.now(), ...itemData }]);
    }
    closeModal();
  }

  function deleteItem(id) {
    const item = items.find((entry) => entry.id === id);
    if (!item) return;
    if (window.confirm(`Delete "${item.name}" from inventory?`)) {
      setItems((prev) => prev.filter((entry) => entry.id !== id));
    }
  }

  function adjustQuantity(item, amount) {
    setItems((prev) =>
      prev.map((entry) =>
        entry.id === item.id
          ? { ...entry, quantity: Math.max(0, Number(entry.quantity) + amount) }
          : entry
      )
    );
  }

  function showCategory(category) {
    setCategoryFilter(category);
    panelRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  return (
    <main className="inventory-page">
      {/* HERO */}
      <section className="inventory-hero">
        <InventoryImage
          className="inventory-hero-img"
          src={CATEGORY_IMAGES.Other}
          alt="Sacks of seeds and grain on a farm"
        />
        <div className="inventory-hero-fade" />
        <div className="inventory-hero-copy">
          <span className="inventory-eyebrow">FARMVERSE NEXUS · SUPPLY CENTER</span>
          <h1>Inventory Management</h1>
          <p>Keep every seed, tool, and farm essential ready for the next growing season.</p>
          <button className="inventory-primary-btn" onClick={openAddModal}>
            <span aria-hidden="true">＋</span> Add Inventory
          </button>
        </div>
      </section>

      {/* STATS */}
      <section className="inventory-stats">
        <article className="inventory-stat-card green">
          <span className="inventory-stat-icon">📦</span>
          <div>
            <span className="inventory-stat-label">Total Inventory Items</span>
            <strong>{String(items.length).padStart(2, "0")}</strong>
            <small>Supplies being tracked</small>
          </div>
          <i className="inventory-leaf" aria-hidden="true">🌿</i>
        </article>

        <article className="inventory-stat-card amber">
          <span className="inventory-stat-icon">⚠️</span>
          <div>
            <span className="inventory-stat-label">Low-Stock Alerts</span>
            <strong>{String(lowStockCount).padStart(2, "0")}</strong>
            <small>{lowStockCount ? "Need replenishment" : "Stock levels look good"}</small>
          </div>
          <i className="inventory-leaf" aria-hidden="true">🌿</i>
        </article>

        <article className="inventory-stat-card blue">
          <span className="inventory-stat-icon">▦</span>
          <div>
            <span className="inventory-stat-label">Stock Categories</span>
            <strong>{String(categoryCount).padStart(2, "0")}</strong>
            <small>Supply groups in use</small>
          </div>
          <i className="inventory-leaf" aria-hidden="true">🌿</i>
        </article>

        <article className="inventory-stat-card violet">
          <span className="inventory-stat-icon">∑</span>
          <div>
            <span className="inventory-stat-label">Total Quantity</span>
            <strong>{totalUnits.toLocaleString()}</strong>
            <small>Combined item quantities</small>
          </div>
          <i className="inventory-leaf" aria-hidden="true">🌿</i>
        </article>
      </section>

      {/* FEATURE CARDS */}
      <section className="inventory-feature-grid">
        <article className="inventory-feature-card">
          <InventoryImage
            className="inventory-feature-img"
            src={CATEGORY_IMAGES.Seeds}
            alt="Seeds ready for planting"
          />
          <div className="inventory-feature-fade" />
          <div className="inventory-feature-text">
            <span>PLANTING ESSENTIALS</span>
            <h2>Seeds for a better harvest</h2>
            <p>Track your seed stock before the next sowing cycle.</p>
            <button className="inventory-outline-btn" onClick={() => showCategory("Seeds")}>
              Learn More →
            </button>
          </div>
        </article>

        <article className="inventory-feature-card">
          <InventoryImage
            className="inventory-feature-img"
            src={CATEGORY_IMAGES.Fertilizers}
            alt="Organic fertilizer for farm soil"
          />
          <div className="inventory-feature-fade" />
          <div className="inventory-feature-text">
            <span>SOIL CARE</span>
            <h2>Feed the soil, grow stronger</h2>
            <p>Keep fertilizers and crop-care supplies organized.</p>
            <button className="inventory-outline-btn" onClick={() => showCategory("Fertilizers")}>
              Learn More →
            </button>
          </div>
        </article>
      </section>

      {/* ALERT */}
      {lowStockCount > 0 && (
        <section className="inventory-alert" role="status">
          <span className="inventory-alert-icon">!</span>
          <div>
            <strong>Stock needs attention</strong>
            <p>
              {lowStockCount} item{lowStockCount === 1 ? " is" : "s are"} at or below their minimum
              stock level. Review them before your next farm activity.
            </p>
          </div>
        </section>
      )}

      {/* STOCK OVERVIEW */}
      <section className="inventory-panel" ref={panelRef}>
        <div className="inventory-panel-heading">
          <div className="inventory-panel-title">
            <span className="inventory-panel-leaf" aria-hidden="true">🌿</span>
            <div>
              <h2>Stock overview</h2>
              <p>Search, review, and update your farm supplies.</p>
            </div>
          </div>
          <span className="inventory-item-count">{filteredItems.length} items</span>
        </div>

        <div className="inventory-toolbar">
          <label className="inventory-search">
            <span aria-hidden="true">⌕</span>
            <input
              type="search"
              placeholder="Search by item or category..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </label>

          <select
            aria-label="Filter by category"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            <option>All Categories</option>
            {CATEGORIES.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>

          <select
            aria-label="Filter by stock status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option>All Statuses</option>
            <option>In Stock</option>
            <option>Running Low</option>
            <option>Low Stock</option>
          </select>
        </div>

        <div className="inventory-table-wrap">
          <table className="inventory-table">
            <thead>
              <tr>
                <th>Inventory item</th>
                <th>Category</th>
                <th>Current stock</th>
                <th>Stock level</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.map((item) => {
                const status = getStockStatus(item);
                const statusClass = status.toLowerCase().replace(" ", "-");
                const percentage =
                  item.minimum > 0
                    ? Math.min(100, (item.quantity / (item.minimum * 2)) * 100)
                    : 100;

                return (
                  <tr key={item.id}>
                    <td>
                      <div className="inventory-item-name">
                        <InventoryImage
                          className="inventory-item-thumb"
                          src={CATEGORY_IMAGES[item.category] || CATEGORY_IMAGES.Other}
                          alt=""
                        />
                        <div>
                          <strong>{item.name}</strong>
                          <small>
                            Minimum: {item.minimum} {item.unit}
                          </small>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className="inventory-category-label">{item.category}</span>
                    </td>
                    <td>
                      <strong className="inventory-quantity">
                        {item.quantity} <span>{item.unit}</span>
                      </strong>
                    </td>
                    <td>
                      <div className="inventory-stock-meter">
                        <div className="inventory-stock-track">
                          <span
                            className={`inventory-stock-fill ${statusClass}`}
                            style={{ width: `${percentage}%` }}
                          />
                        </div>
                        <small>{Math.round(percentage)}%</small>
                      </div>
                    </td>
                    <td>
                      <span className={`inventory-status ${statusClass}`}>
                        <span /> {status}
                      </span>
                    </td>
                    <td>
                      <div className="inventory-actions">
                        <button
                          className="inventory-action-btn round"
                          title="Add 1 unit"
                          aria-label={`Add one ${item.unit} to ${item.name}`}
                          onClick={() => adjustQuantity(item, 1)}
                        >
                          +
                        </button>
                        <button
                          className="inventory-action-btn round"
                          title="Remove 1 unit"
                          aria-label={`Remove one ${item.unit} from ${item.name}`}
                          onClick={() => adjustQuantity(item, -1)}
                        >
                          −
                        </button>
                        <button className="inventory-action-btn edit" onClick={() => openEditModal(item)}>
                          Edit
                        </button>
                        <button className="inventory-action-btn delete" onClick={() => deleteItem(item.id)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}

              {filteredItems.length === 0 && (
                <tr>
                  <td colSpan="6" className="inventory-empty">
                    <span>⌕</span>
                    <strong>No inventory items found</strong>
                    <p>Try changing your search or filters.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      {/* MODAL */}
      {modalOpen && (
        <div className="inventory-modal-backdrop" onMouseDown={closeModal}>
          <section
            className="inventory-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="inventory-modal-title"
            onMouseDown={(e) => e.stopPropagation()}
          >
            <div className="inventory-modal-heading">
              <div>
                <span className="inventory-eyebrow">INVENTORY DETAILS</span>
                <h2 id="inventory-modal-title">
                  {editingItem ? "Edit inventory item" : "Add inventory item"}
                </h2>
              </div>
              <button className="inventory-modal-close" onClick={closeModal} aria-label="Close">
                ×
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <label>
                Item name
                <input
                  name="name"
                  value={form.name}
                  onChange={handleFormChange}
                  placeholder="e.g. Wheat Seeds"
                  required
                />
              </label>

              <label>
                Category
                <select name="category" value={form.category} onChange={handleFormChange}>
                  {CATEGORIES.map((c) => (
                    <option key={c}>{c}</option>
                  ))}
                </select>
              </label>

              <div className="inventory-form-row">
                <label>
                  Quantity
                  <input
                    name="quantity"
                    type="number"
                    min="0"
                    step="any"
                    value={form.quantity}
                    onChange={handleFormChange}
                    placeholder="0"
                    required
                  />
                </label>
                <label>
                  Unit
                  <input
                    name="unit"
                    value={form.unit}
                    onChange={handleFormChange}
                    placeholder="kg, L, units"
                    required
                  />
                </label>
              </div>

              <label>
                Minimum stock level
                <input
                  name="minimum"
                  type="number"
                  min="0"
                  step="any"
                  value={form.minimum}
                  onChange={handleFormChange}
                  placeholder="Set alert threshold"
                  required
                />
              </label>

              <div className="inventory-modal-actions">
                <button type="button" className="inventory-secondary-btn" onClick={closeModal}>
                  Cancel
                </button>
                <button type="submit" className="inventory-primary-btn">
                  {editingItem ? "Save changes" : "Add item"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </main>
  );
}