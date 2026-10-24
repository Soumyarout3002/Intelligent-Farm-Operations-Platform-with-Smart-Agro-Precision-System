import { Link } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';

export default function AccessDenied() {
  return (
    <div className="empty big">
      <ShieldAlert size={48} />
      <h2>Access denied</h2>
      <p>Your role does not have permission to view this page.<br /><small>(Frontend demo restriction. The backend must enforce real permissions.)</small></p>
      <Link className="btn primary" to="/overview">Back to overview</Link>
    </div>
  );
}
