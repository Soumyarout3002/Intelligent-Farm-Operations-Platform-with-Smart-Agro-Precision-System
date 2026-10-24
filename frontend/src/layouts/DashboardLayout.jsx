import { useState } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import { useAuth } from '../context/AuthContext';

export default function DashboardLayout() {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  if (!user) return <Navigate to="/login" replace />;
  return (
    <div className="shell">
      <Sidebar role={user.role} open={open} onClose={() => setOpen(false)} />
      <div className="main">
        <Header onMenu={() => setOpen(true)} />
        <main className="content"><Outlet /></main>
      </div>
    </div>
  );
}
