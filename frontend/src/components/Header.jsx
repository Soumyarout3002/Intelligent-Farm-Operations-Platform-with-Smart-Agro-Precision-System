import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Bell, LogOut, Menu } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { cap } from '../config/permissions';
import { categoryMeta } from '../config/notificationCategories';
import { useNotificationStore, loadNotifications, markAsRead, markAllAsRead, unreadCount } from '../store/notificationStore';
import { timeAgo } from './notifications/NotificationItem';
import '../styles/notifications.css';

export default function Header({ onMenu }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const popRef = useRef(null);
  const { items, loading, loaded, error } = useNotificationStore();
  const unread = unreadCount(items);

  // Load once so the bell shows the unread count on every page.
  useEffect(() => { loadNotifications(user); }, [user]);

  // Close the panel on outside click or Escape.
  useEffect(() => {
    if (!open) return undefined;
    const onDown = (e) => { if (popRef.current && !popRef.current.contains(e.target)) setOpen(false); };
    const onKey = (e) => { if (e.key === 'Escape') setOpen(false); };
    document.addEventListener('mousedown', onDown);
    document.addEventListener('keydown', onKey);
    return () => { document.removeEventListener('mousedown', onDown); document.removeEventListener('keydown', onKey); };
  }, [open]);

  const openItem = (n) => {
    markAsRead(user, n.id).catch(() => {});
    setOpen(false);
    navigate(n.link || '/notifications');
  };

  return (
    <header className="topbar">
      <button className="icon-btn menu-btn" aria-label="Open menu" onClick={onMenu}><Menu size={22} /></button>
      <div className="grow" />
      <div className="notif" ref={popRef}>
        <button className="icon-btn" aria-label={unread ? `Notifications, ${unread} unread` : 'Notifications'} aria-expanded={open} onClick={() => setOpen((o) => !o)}>
          <Bell size={20} />
          {unread > 0 && <span className="notif-badge">{unread > 99 ? '99+' : unread}</span>}
        </button>
        {open && (
          <div className="notif-pop nt-pop">
            <div className="nt-pop-head">
              <strong>Notifications{unread > 0 ? ` (${unread} unread)` : ''}</strong>
              <button onClick={() => markAllAsRead(user).catch(() => {})} disabled={unread === 0}>Mark all as read</button>
            </div>
            {loading && !loaded && <div className="nt-pop-empty">Loading…</div>}
            {error && <div className="nt-pop-empty">{error}</div>}
            {loaded && items.length === 0 && <div className="nt-pop-empty">No notifications yet.</div>}
            {items.length > 0 && (
              <ul className="nt-pop-list">
                {items.slice(0, 5).map((n) => {
                  const { icon: Icon, tone } = categoryMeta(n.category);
                  return (
                    <li key={n.id} className={n.read ? 'read' : 'unread'}>
                      <button onClick={() => openItem(n)}>
                        <span className={`nt-icon tone-${tone}`}><Icon size={16} /></span>
                        <span><strong>{n.title}</strong><small>{timeAgo(n.createdAt)}</small></span>
                      </button>
                    </li>
                  );
                })}
              </ul>
            )}
            <Link className="nt-pop-foot" to="/notifications" onClick={() => setOpen(false)}>View all notifications</Link>
          </div>
        )}
      </div>
      <div className="me">
        <span className="avatar">{user.name.charAt(0).toUpperCase()}</span>
        <div><strong>{user.name}</strong><small>{cap(user.role)}</small></div>
      </div>
      <button className="btn ghost sm" onClick={logout}><LogOut size={16} /> Logout</button>
    </header>
  );
}
