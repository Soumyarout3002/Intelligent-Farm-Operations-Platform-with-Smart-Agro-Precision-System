import { useCallback, useEffect, useMemo, useState } from 'react';
import { Plus, Search, Eye, Pencil, Trash2 } from 'lucide-react';
import { listUsers, createUser, updateUser, setStatus, deleteUser } from '../services/userService';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import Field from '../components/Field';
import { ROLES, cap } from '../config/permissions';
import { validateFields } from '../utils/validation';

const STATUS = ['active', 'inactive'];

function UserForm({ initial, isSelf, onSubmit, onCancel }) {
  const editing = !!initial;
  const fields = [
    { name: 'name', label: 'Full name', required: true },
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'mobile', label: 'Mobile number', type: 'tel' },
    ...(editing
  ? [{ name: 'role', label: 'Role', type: 'select', options: ROLES, disabled: true }]
  : [{ name: 'role', label: 'Role', type: 'select', options: ROLES, required: true }]),
    ...(editing ? [] : [{ name: 'status', label: 'Status', type: 'select', options: STATUS, required: true }, { name: 'password', label: 'Temporary password', type: 'password', required: true }]),
  ];
  const [values, setValues] = useState(initial || { role: 'farmer', status: 'active' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  async function submit(e) {
    e.preventDefault();
    const errs = validateFields(fields, values);
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setBusy(true); setError('');
    try { await onSubmit(values); } catch (err) { setError(err.message); setBusy(false); }
  }
  return (
    <form onSubmit={submit} noValidate>
      {error && <div className="alert error">{error}</div>}
      {isSelf && <div className="alert info">This is your own account: role and status cannot be changed here.</div>}
      <div className="grid-form">{fields.map((f) => <Field key={f.name} field={f} value={values[f.name]} error={errors[f.name]} onChange={(n, v) => { setValues((p) => ({ ...p, [n]: v })); setErrors((p) => ({ ...p, [n]: undefined })); }} />)}</div>
      <div className="form-actions"><button type="button" className="btn ghost" onClick={onCancel}>Cancel</button><button className="btn primary" disabled={busy}>{busy ? 'Saving…' : editing ? 'Save changes' : 'Add user'}</button></div>
    </form>
  );
}

export default function UserManagement() {
  const { user: me, refresh } = useAuth();
  const toast = useToast();
  const [users, setUsers] = useState([]);
  const [q, setQ] = useState('');
  const [role, setRole] = useState('');
  const [modal, setModal] = useState(null); // {type:'add'|'edit'|'view'|'delete', user}
  const reload = useCallback(() => listUsers().then(setUsers), []);
  useEffect(() => { reload(); }, [reload]);

  const shown = useMemo(() => {
    const s = q.trim().toLowerCase();
    return users.filter((u) => (!s || u.name.toLowerCase().includes(s) || u.email.toLowerCase().includes(s)) && (!role || u.role === role));
  }, [users, q, role]);

  const close = () => setModal(null);
  async function save(values) {
    if (modal.type === 'add') { await createUser(values); toast('User added.'); }
    else { await updateUser(modal.user.id, { name: values.name, email: values.email, mobile: values.mobile, role: values.role, organization: values.organization }, me.id); toast('User updated.'); if (modal.user.id === me.id) refresh(); }
    close(); reload();
  }
  async function changeStatus(u, s) {
    try { await setStatus(u.id, s, me.id); toast(`${u.name} is now ${s}.`); reload(); } catch (e) { toast(e.message, 'error'); }
  }
  async function remove() {
    try { await deleteUser(modal.user.id, me.id); toast('User deleted.'); close(); reload(); } catch (e) { toast(e.message, 'error'); close(); }
  }

  return (
    <>
      <div className="page-head"><div><h1>User & Access Management</h1>
      <p className="muted">
        {shown.length} user(s). Manage registered users and their access.
      </p></div>
        <button className="btn primary" onClick={() => setModal({ type: 'add' })}><Plus size={16} /> Add user</button></div>
      <div className="toolbar">
        <div className="search"><Search size={16} /><input aria-label="Search users" placeholder="Search by name or email" value={q} onChange={(e) => setQ(e.target.value)} /></div>
        <select aria-label="Filter by role" value={role} onChange={(e) => setRole(e.target.value)}><option value="">All roles</option>{ROLES.map((r) => <option key={r} value={r}>{cap(r)}</option>)}</select>
      </div>
      {shown.length === 0 ? <div className="empty"><p>No users match your search.</p></div> : (
        <div className="table-wrap card"><table>
          <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>{shown.map((u) => {
            const self = u.id === me.id;
            return (
              <tr key={u.id}>
                <td><strong>{u.name}</strong>{self && <small className="tagpill">You</small>}</td><td>{u.email}</td>
                <td><span className={`badge ${u.role}`}>{cap(u.role)}</span></td>
                <td><select className="status-select" aria-label={`Status for ${u.name}`} value={u.status} disabled={self} onChange={(e) => changeStatus(u, e.target.value)}>{STATUS.map((s) => <option key={s} value={s}>{cap(s)}</option>)}</select></td>
                <td className="row-actions">
                  <button className="icon-btn" aria-label={`View ${u.name}`} onClick={() => setModal({ type: 'view', user: u })}><Eye size={16} /></button>
                  <button className="icon-btn" aria-label={`Edit ${u.name}`} onClick={() => setModal({ type: 'edit', user: u })}><Pencil size={16} /></button>
                  <button className="icon-btn danger" aria-label={`Delete ${u.name}`} disabled={self} title={self ? 'You cannot delete your own account' : ''} onClick={() => setModal({ type: 'delete', user: u })}><Trash2 size={16} /></button>
                </td>
              </tr>);
          })}</tbody></table></div>
      )}
      {modal?.type === 'view' && (
        <Modal title="User details" onClose={close}>
          <dl className="details one">{[['Name', modal.user.name], ['Email', modal.user.email], ['Mobile', modal.user.mobile || '—'], ['Role', cap(modal.user.role)], ['Status', cap(modal.user.status)],
            ['Organization', modal.user.organization || '—'], ['Created', new Date(modal.user.createdAt).toLocaleString()]].map(([k, v]) => <div key={k}><dt>{k}</dt><dd>{v}</dd></div>)}</dl>
        </Modal>)}
      {(modal?.type === 'add' || modal?.type === 'edit') && (
        <Modal wide title={modal.type === 'add' ? 'Add user' : 'Edit user'} onClose={close}>
          <UserForm initial={modal.user} isSelf={modal.user?.id === me.id} onSubmit={save} onCancel={close} />
        </Modal>)}
      {modal?.type === 'delete' && <ConfirmDialog title="Delete user" message={<p>Delete <strong>{modal.user.name}</strong> ({modal.user.email})? This cannot be undone.</p>} onCancel={close} onConfirm={remove} />}
    </>
  );
}
