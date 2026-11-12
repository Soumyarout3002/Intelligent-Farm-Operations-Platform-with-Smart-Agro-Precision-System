import { Link } from 'react-router-dom';
import { Check, ArrowRight } from 'lucide-react';
import { categoryMeta } from '../../config/notificationCategories';

export const formatDateTime = (iso) => {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleString(undefined, { day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: '2-digit' });
};

export function timeAgo(iso) {
  const diff = Date.now() - new Date(iso).getTime();
  if (!Number.isFinite(diff)) return '';
  const min = Math.floor(diff / 60000);
  if (min < 1) return 'Just now';
  if (min < 60) return `${min} min ago`;
  const hr = Math.floor(min / 60);
  if (hr < 24) return `${hr} hr ago`;
  const day = Math.floor(hr / 24);
  return day === 1 ? 'Yesterday' : `${day} days ago`;
}

export default function NotificationItem({ item, onRead, busy }) {
  const meta = categoryMeta(item.category);
  const Icon = meta.icon;
  return (
    <li className={`nt-item ${item.read ? 'read' : 'unread'} sev-${item.severity}`}>
      <span className={`nt-icon tone-${meta.tone}`}><Icon size={20} /></span>
      <div className="nt-body">
        <div className="nt-title">
          {!item.read && <i className="nt-unread-dot" aria-hidden="true" />}
          <strong>{item.title}</strong>
          {!item.read && <span className="sr-only"> (unread)</span>}
        </div>
        <p>{item.description}</p>
        <div className="nt-meta">
          <span className={`nt-cat tone-${meta.tone}`}>{meta.short}</span>
          <time dateTime={item.createdAt} title={formatDateTime(item.createdAt)}>{formatDateTime(item.createdAt)} · {timeAgo(item.createdAt)}</time>
        </div>
      </div>
      <div className="nt-actions">
        {item.link && <Link className="btn ghost sm" to={item.link} onClick={() => !item.read && onRead(item.id)}>View <ArrowRight size={14} /></Link>}
        {!item.read && <button className="btn ghost sm" onClick={() => onRead(item.id)} disabled={busy} aria-label={`Mark "${item.title}" as read`}><Check size={14} /> Mark as read</button>}
      </div>
    </li>
  );
}
