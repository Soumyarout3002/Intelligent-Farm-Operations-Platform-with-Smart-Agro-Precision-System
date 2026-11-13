import CropLifecycle from './components/CropLifecycle';
import { Navigate, Route, Routes } from 'react-router-dom';

import { useAuth } from './context/AuthContext';
import { can } from './config/permissions';

import DashboardLayout from './layouts/DashboardLayout';

import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';

import Overview from './pages/Overview';
import UserManagement from './pages/UserManagement';
import FarmManagement from './pages/FarmManagement';
import FarmDetails from './pages/FarmDetails';
import Profile from './pages/Profile';
import AccessDenied from './pages/AccessDenied';

// GIS Farm Mapping component
import GISFarmMapping from './components/GISFarmMapping';

function Guard({ perm, children }) {
  const { user } = useAuth();

  return can(user.role, perm) ? children : <AccessDenied />;
}

function PublicOnly({ children }) {
  const { user } = useAuth();

  return user ? <Navigate to="/overview" replace /> : children;
}

export default function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route
        path="/login"
        element={<PublicOnly><Login /></PublicOnly>}
      />

      <Route
        path="/register"
        element={<PublicOnly><Register /></PublicOnly>}
      />

      <Route
        path="/forgot-password"
        element={<PublicOnly><ForgotPassword /></PublicOnly>}
      />

      <Route
        path="/reset-password"
        element={<PublicOnly><ResetPassword /></PublicOnly>}
      />

      {/* Protected Dashboard Routes */}
      <Route element={<DashboardLayout />}>
        <Route
          path="/overview"
          element={<Guard perm="overview"><Overview /></Guard>}
        />

        <Route
          path="/users"
          element={<Guard perm="users"><UserManagement /></Guard>}
        />

        <Route
          path="/farms"
          element={<Guard perm="farms"><FarmManagement /></Guard>}
        />

        <Route
          path="/farms/:id"
          element={<Guard perm="farms"><FarmDetails /></Guard>}
        />

        <Route
          path="/profile"
          element={<Guard perm="profile"><Profile /></Guard>}
        />

        {/* GIS Farm Mapping Route */}
        <Route
          path="/gis-mapping"
          element={<Guard perm="farms"><GISFarmMapping /></Guard>}
        />
      </Route>

      <Route
         path="/crop-lifecycle"
          element={<Guard perm="farms"><CropLifecycle /></Guard>}
       />

      {/* Default Routes */}
      <Route
        path="/"
        element={<Navigate to="/overview" replace />}
      />

      <Route
        path="*"
        element={<Navigate to="/overview" replace />}
      />
    </Routes>
  );
}
