import { createContext, useCallback, useContext, useState } from 'react';
import { CheckCircle2, AlertCircle, X } from 'lucide-react';

const ToastContext = createContext(() => {});
export const useToast = () => useContext(ToastContext);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const dismiss = (id) => setToasts((t) => t.filter((x) => x.id !== id));
  const toast = useCallback((message, type = 'success') => {
    const id = Date.now() + Math.random();
    setToasts((t) => [...t, { id, message, type }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 4000);
  }, []);
  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="toast-stack" role="status" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast ${t.type}`}>
            {t.type === 'error' ? <AlertCircle size={18} /> : <CheckCircle2 size={18} />}
            <span>{t.message}</span>
            <button className="icon-btn" aria-label="Dismiss" onClick={() => dismiss(t.id)}><X size={14} /></button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
