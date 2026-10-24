import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './components/Toast';
import { seedUsers } from './services/userService';
import './styles/global.css';
import './styles/auth.css';
import './styles/dashboard.css';

// Seed demo accounts first so the session lookup and login always have data.
seedUsers().finally(() => {
  ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    </React.StrictMode>
  );
});
