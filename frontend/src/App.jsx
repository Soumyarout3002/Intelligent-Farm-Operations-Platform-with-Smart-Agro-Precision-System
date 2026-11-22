
import FarmResourcePlanning from './pages/FarmResourcePlanning';
import EquipmentBookings from './pages/EquipmentBookings';
import EquipmentManagement from './pages/EquipmentManagement';
import InventoryManagement from './pages/InventoryManagement';

import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { can } from './config/permissions';

import DashboardLayout from './layouts/DashboardLayout';

import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import AcceptInvitation from './pages/auth/AcceptInvitation';

import Overview from './pages/Overview';
import UserManagement from './pages/UserManagement';
import FarmManagement from './pages/FarmManagement';
import FarmDetails from './pages/FarmDetails';
import Profile from './pages/Profile';
import AccessDenied from './pages/AccessDenied';
import ActivityHistory from './pages/ActivityHistory';
import WeatherMonitoring from './pages/WeatherMonitoring';
import AIAssistant from './pages/aiAssistant';
import Notifications from './pages/Notifications';
import IrrigationScheduling from './pages/IrrigationScheduling';
import AgriculturalAnalytics from './pages/AgriculturalAnalytics';
import FieldWorkerTasks from './pages/FieldWorkerTasks';

function Guard({ perm, children }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return can(user.role, perm) ? children : <AccessDenied />;
}

function PublicOnly({ children }) {
  const { user } = useAuth();
  return user ? <Navigate to="/overview" replace /> : children;
}

export default function App() {
  return (
    <Routes>
      {/* Public authentication routes */}
      <Route
        path="/login"
        element={
          <PublicOnly>
            <Login />
          </PublicOnly>
        }
      />

      <Route
        path="/register"
        element={
          <PublicOnly>
            <Register />
          </PublicOnly>
        }
      />

      <Route
        path="/forgot-password"
        element={
          <PublicOnly>
            <ForgotPassword />
          </PublicOnly>
        }
      />

      <Route
        path="/reset-password"
        element={
          <PublicOnly>
            <ResetPassword />
          </PublicOnly>
        }
      />

      {/* Invitation acceptance route */}
      <Route
        path="/accept-invitation"
        element={
          <PublicOnly>
            <AcceptInvitation />
          </PublicOnly>
        }
      />

      {/* Protected dashboard routes */}
      <Route element={<DashboardLayout />}>
        <Route
          path="/overview"
          element={
            <Guard perm="overview">
              <Overview />
            </Guard>
          }
        />

        <Route
          path="/users"
          element={
            <Guard perm="users">
              <UserManagement />
            </Guard>
          }
        />

        <Route
          path="/farms"
          element={
            <Guard perm="farms">
              <FarmManagement />
            </Guard>
          }
        />

        <Route
          path="/farms/:id"
          element={
            <Guard perm="farms">
              <FarmDetails />
            </Guard>
          }
        />

        <Route
          path="/profile"
          element={
            <Guard perm="profile">
              <Profile />
            </Guard>
          }
        />

        <Route
          path="/inventory"
          element={
            <Guard perm="inventory">
              <InventoryManagement />
            </Guard>
          }
        />

        <Route
          path="/equipment"
          element={
            <Guard perm="equipment">
              <EquipmentManagement />
            </Guard>
          }
        />

        <Route
          path="/equipment/bookings"
          element={
            <Guard perm="equipment">
              <EquipmentBookings />
            </Guard>
          }
        />

        <Route
          path="/resources"
          element={
            <Guard perm="resources">
              <FarmResourcePlanning />
            </Guard>
          }
        />

        <Route
          path="/weather"
          element={
            <Guard perm="weather">
              <WeatherMonitoring />
            </Guard>
          }
        />

        <Route
          path="/field-work"
          element={
            <Guard perm="fieldWork">
              <FieldWorkerTasks />
            </Guard>
          }
        />

        <Route
          path="/irrigation"
          element={
            <Guard perm="irrigation">
              <IrrigationScheduling />
            </Guard>
          }
        />

        <Route
          path="/analytics"
          element={
            <Guard perm="analytics">
              <AgriculturalAnalytics />
            </Guard>
          }
        />

        <Route
          path="/ai-assistant"
          element={
            <Guard perm="aiAssistant">
              <AIAssistant />
            </Guard>
          }
        />

        <Route
          path="/notifications"
          element={
            <Guard perm="notifications">
              <Notifications />
            </Guard>
          }
        />
      </Route>

      {/* Activity history */}
      <Route
        path="/activity-history"
        element={
          <Guard perm="users">
            <ActivityHistory />
          </Guard>
        }
      />

      {/* Default routes */}
      <Route path="/" element={<Navigate to="/overview" replace />} />
      <Route path="*" element={<Navigate to="/overview" replace />} />
    </Routes>
  );
}
