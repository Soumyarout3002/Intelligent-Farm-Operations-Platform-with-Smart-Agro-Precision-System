import { useMemo, useState } from "react";
import {
  Sprout,
  Users,
  ClipboardList,
  Tractor,
  Wallet,
  CalendarDays,
  ArrowRight,
  CheckCircle2,
  Search,
  Plus,
  Trash2,
  Droplets,
  Package,
  Wrench,
} from "lucide-react";
import "../styles/resourcePlanning.css";

const initialWorkers = [
  {
    id: 1,
    name: "Ravi Kumar",
    role: "Field Supervisor",
    farm: "Green Valley Farm",
    attendance: "Present",
    avatar: "👨🏽‍🌾",
  },
  {
    id: 2,
    name: "Sita Devi",
    role: "Irrigation Worker",
    farm: "Sunrise Orchards",
    attendance: "Present",
    avatar: "👩🏽‍🌾",
  },
  {
    id: 3,
    name: "Arjun Rao",
    role: "Equipment Operator",
    farm: "Green Valley Farm",
    attendance: "Absent",
    avatar: "👨🏽‍🌾",
  },
];

const initialTasks = [
  {
    id: 1,
    name: "Irrigation",
    farm: "Green Valley Farm",
    status: "In Progress",
    due: "Nov 3, 2026",
    icon: "💧",
  },
  {
    id: 2,
    name: "Soil preparation",
    farm: "Riverbend Fields",
    status: "Pending",
    due: "Nov 4, 2026",
    icon: "🌱",
  },
  {
    id: 3,
    name: "Harvest check",
    farm: "Green Valley Farm",
    status: "Completed",
    due: "Nov 5, 2026",
    icon: "🚜",
  },
];

const initialExpenses = [
  { name: "Seeds", amount: 12500, color: "seeds" },
  { name: "Labor", amount: 18000, color: "labor" },
  { name: "Maintenance", amount: 8000, color: "maintenance" },
  { name: "Others", amount: 4000, color: "others" },
];

const TOTAL_BUDGET = 100000;

export default function FarmResourcePlanning() {
  const [workers, setWorkers] = useState(initialWorkers);
  const [tasks] = useState(initialTasks);
  const [expenses, setExpenses] = useState(initialExpenses);
  const [workerName, setWorkerName] = useState("");
  const [workerRole, setWorkerRole] = useState("");
  const [workerFarm, setWorkerFarm] = useState("");
  const [search, setSearch] = useState("");
  const [showExpenseForm, setShowExpenseForm] = useState(false);
  const [expenseName, setExpenseName] = useState("");
  const [expenseAmount, setExpenseAmount] = useState("");

  const presentCount = workers.filter(
    (worker) => worker.attendance === "Present"
  ).length;

  const spent = expenses.reduce((sum, expense) => sum + expense.amount, 0);
  const remaining = TOTAL_BUDGET - spent;
  const attendanceRate = workers.length
    ? Math.round((presentCount / workers.length) * 100)
    : 0;

  const filteredWorkers = useMemo(() => {
    const term = search.toLowerCase();
    return workers.filter((worker) =>
      `${worker.name} ${worker.role} ${worker.farm}`.toLowerCase().includes(term)
    );
  }, [workers, search]);

  function addWorker(event) {
    event.preventDefault();

    if (!workerName.trim() || !workerRole.trim() || !workerFarm.trim()) {
      return;
    }

    setWorkers((current) => [
      ...current,
      {
        id: Date.now(),
        name: workerName.trim(),
        role: workerRole.trim(),
        farm: workerFarm.trim(),
        attendance: "Present",
        avatar: "👩🏽‍🌾",
      },
    ]);

    setWorkerName("");
    setWorkerRole("");
    setWorkerFarm("");
  }

  function updateAttendance(id, attendance) {
    setWorkers((current) =>
      current.map((worker) =>
        worker.id === id ? { ...worker, attendance } : worker
      )
    );
  }

  function deleteWorker(id) {
    setWorkers((current) => current.filter((worker) => worker.id !== id));
  }

  function addExpense(event) {
    event.preventDefault();

    const amount = Number(expenseAmount);
    if (!expenseName.trim() || !amount || amount <= 0) return;

    setExpenses((current) => [
      ...current,
      { name: expenseName.trim(), amount, color: "others" },
    ]);

    setExpenseName("");
    setExpenseAmount("");
    setShowExpenseForm(false);
  }

  return (
    <main className="farm-resource-page">
      <header className="farm-page-heading">
        <div className="farm-heading-mark">
          <Sprout size={34} />
        </div>

        <div className="farm-heading-copy">
          <h1>Farm Resource Planning</h1>
          <p>Plan people, tasks, and farm resources in one place</p>
        </div>

        <div className="farm-page-date">
          <CalendarDays size={17} />
          <span>Nov 1, 2026</span>
        </div>
      </header>

      <section className="farm-stat-grid">
        <article className="farm-stat-card workers-stat">
          <div className="farm-stat-icon">👩🏽‍🌾</div>
          <div>
            <p>Workers</p>
            <strong>{workers.length.toString().padStart(2, "0")}</strong>
            <span>Registered team</span>
          </div>
        </article>

        <article className="farm-stat-card tasks-stat">
          <div className="farm-stat-icon">
            <ClipboardList size={32} />
          </div>
          <div>
            <p>Tasks</p>
            <strong>{tasks.length.toString().padStart(2, "0")}</strong>
            <span>{tasks.filter((task) => task.status !== "Completed").length} pending</span>
          </div>
        </article>

        <article className="farm-stat-card resources-stat">
          <div className="farm-stat-icon">
            <Tractor size={36} />
          </div>
          <div>
            <p>Resources Used</p>
            <strong>68%</strong>
            <span>On track</span>
          </div>
        </article>

        <article className="farm-stat-card budget-stat">
          <div className="farm-stat-icon">
            <Wallet size={34} />
          </div>
          <div>
            <p>Budget</p>
            <strong>₹{spent.toLocaleString("en-IN")}</strong>
            <span>₹{remaining.toLocaleString("en-IN")} remaining</span>
          </div>
        </article>
      </section>

      <section className="farm-dashboard-grid">
        <article className="farm-card field-card">
          <div className="farm-card-heading">
            <div>
              <h2><Sprout size={23} /> Field Activity</h2>
              <p>Current tasks across your farms</p>
            </div>
            <button className="farm-soft-button" type="button">
              View All <ArrowRight size={16} />
            </button>
          </div>

          <div className="field-activity-content">
            <img
              className="field-activity-image"
              src="/equipment/tractor.jpg"
              alt="Tractor working in a farm field"
            />

            <div className="field-table-wrap">
              <table className="field-table">
                <thead>
                  <tr>
                    <th>Task</th>
                    <th>Farm</th>
                    <th>Status</th>
                    <th>Due Date</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map((task) => (
                    <tr key={task.id}>
                      <td><span className="task-emoji">{task.icon}</span>{task.name}</td>
                      <td>{task.farm}</td>
                      <td>
                        <span className={`task-status ${task.status.toLowerCase().replace(" ", "-")}`}>
                          {task.status}
                        </span>
                      </td>
                      <td>{task.due}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </article>

        <article className="farm-card workforce-card">
          <div className="farm-card-heading">
            <div>
              <h2><Users size={24} /> Today’s Workforce</h2>
              <p>Attendance overview</p>
            </div>
          </div>

          <div className="workforce-people">
            {workers.slice(0, 3).map((worker) => (
              <div className="workforce-person" key={worker.id}>
                <div className="workforce-avatar">{worker.avatar}</div>
                <strong>{worker.name}</strong>
                <span>{worker.role}</span>
                <span className={`attendance-badge ${worker.attendance.toLowerCase()}`}>
                  {worker.attendance}
                </span>
              </div>
            ))}
          </div>

          <div className="attendance-rate">
            <div className="attendance-rate-label">
              <Users size={22} />
              <span>Attendance Rate</span>
              <strong>{attendanceRate}%</strong>
            </div>
            <div className="farm-progress-track">
              <span style={{ width: `${attendanceRate}%` }} />
            </div>
          </div>
        </article>

        <article className="farm-card resource-allocation-card">
          <div className="farm-card-heading">
            <div>
              <h2><Sprout size={23} /> Resource Allocation</h2>
              <p>Optimal use of farm resources</p>
            </div>
            <button className="farm-soft-button" type="button">
              Manage <ArrowRight size={16} />
            </button>
          </div>

          <div className="allocation-content">
            <img
              src="/equipment/irrigation.jpg"
              alt="Sprinkler watering crops"
              className="allocation-image"
            />

            <div className="allocation-list">
              <div className="allocation-item">
                <Droplets size={22} />
                <div className="allocation-item-content">
                  <div><span>Irrigation Resources</span><strong>80%</strong></div>
                  <div className="farm-progress-track"><span style={{ width: "80%" }} /></div>
                </div>
              </div>

              <div className="allocation-item">
                <Sprout size={22} />
                <div className="allocation-item-content">
                  <div><span>Seeds Inventory</span><strong>72%</strong></div>
                  <div className="farm-progress-track"><span style={{ width: "72%" }} /></div>
                </div>
              </div>

              <div className="allocation-item">
                <Wrench size={22} />
                <div className="allocation-item-content">
                  <div><span>Equipment Availability</span><strong>90%</strong></div>
                  <div className="farm-progress-track"><span style={{ width: "90%" }} /></div>
                </div>
              </div>
            </div>
          </div>
        </article>

        <article className="farm-card farm-budget-card">
          <div className="farm-card-heading">
            <div>
              <h2><Wallet size={22} /> Farm Budget</h2>
              <p>Track your expenses and planning</p>
            </div>
            <button
              className="farm-soft-button"
              type="button"
              onClick={() => setShowExpenseForm((show) => !show)}
            >
              Add Expense <Plus size={16} />
            </button>
          </div>

          {showExpenseForm && (
            <form className="expense-form" onSubmit={addExpense}>
              <input
                value={expenseName}
                onChange={(event) => setExpenseName(event.target.value)}
                placeholder="Expense name"
                aria-label="Expense name"
              />
              <input
                type="number"
                min="1"
                value={expenseAmount}
                onChange={(event) => setExpenseAmount(event.target.value)}
                placeholder="Amount in ₹"
                aria-label="Expense amount"
              />
              <button type="submit">Save</button>
            </form>
          )}

          <div className="budget-content">
            <div
              className="budget-donut"
              style={{
                background: `conic-gradient(#f8bc35 0 ${(spent / TOTAL_BUDGET) * 100}%, #398c4b ${(spent / TOTAL_BUDGET) * 100}% 100%)`,
              }}
            >
              <div>
                <strong>₹{spent.toLocaleString("en-IN")}</strong>
                <span>Spent</span>
              </div>
            </div>

            <div className="expense-list">
              {expenses.map((expense, index) => (
                <div className="expense-row" key={`${expense.name}-${index}`}>
                  <span className={`expense-dot ${expense.color}`} />
                  <span>{expense.name}</span>
                  <strong>₹{expense.amount.toLocaleString("en-IN")}</strong>
                </div>
              ))}
            </div>

            <div className="budget-total">
              <span>Total Budget</span>
              <strong>₹{TOTAL_BUDGET.toLocaleString("en-IN")}</strong>
              <div className="budget-remaining">
                <span>Remaining</span>
                <strong>₹{remaining.toLocaleString("en-IN")}</strong>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section className="farm-card worker-management-card">
        <div className="farm-card-heading">
          <div>
            <h2><Users size={23} /> Worker Management</h2>
            <p>Register workers and mark their daily attendance.</p>
          </div>
          <span className="worker-count">{workers.length} workers</span>
        </div>

        <form className="worker-add-form" onSubmit={addWorker}>
          <input value={workerName} onChange={(e) => setWorkerName(e.target.value)} placeholder="Worker name" />
          <input value={workerRole} onChange={(e) => setWorkerRole(e.target.value)} placeholder="Worker role" />
          <input value={workerFarm} onChange={(e) => setWorkerFarm(e.target.value)} placeholder="Assigned farm" />
          <button type="submit"><Plus size={17} /> Add Worker</button>
        </form>

        <label className="worker-search">
          <Search size={17} />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search workers..." />
        </label>

        <div className="worker-table-wrap">
          <table className="worker-table">
            <thead>
              <tr>
                <th>Worker</th>
                <th>Role</th>
                <th>Assigned Farm</th>
                <th>Attendance</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredWorkers.map((worker) => (
                <tr key={worker.id}>
                  <td><strong>{worker.name}</strong></td>
                  <td>{worker.role}</td>
                  <td>{worker.farm}</td>
                  <td>
                    <span className={`attendance-badge ${worker.attendance.toLowerCase()}`}>
                      {worker.attendance}
                    </span>
                  </td>
                  <td>
                    <div className="worker-row-actions">
                      <button type="button" onClick={() => updateAttendance(worker.id, "Present")}>Present</button>
                      <button type="button" onClick={() => updateAttendance(worker.id, "Absent")}>Absent</button>
                      <button className="worker-delete-button" type="button" onClick={() => deleteWorker(worker.id)} aria-label={`Delete ${worker.name}`}>
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {filteredWorkers.length === 0 && (
                <tr><td colSpan="5" className="no-workers">No workers found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}