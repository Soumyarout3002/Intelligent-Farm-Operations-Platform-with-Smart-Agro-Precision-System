
import { useEffect } from 'react';
import { X } from 'lucide-react';

export default function Modal({ title, onClose, children, wide }) {
  useEffect(() => {
    const onKey = (e) => {
      if (e.key === 'Escape') onClose();
    };

    document.addEventListener('keydown', onKey);

    return () => {
      document.removeEventListener('keydown', onKey);
    };
  }, [onClose]);

  return (
    <div
      className="modal-backdrop"
      onMouseDown={(e) =>
        e.target === e.currentTarget && onClose()
      }
    >
      <div
        className={`modal farm-modal ${wide ? 'wide' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        style={{
          background: '#f3faf2',
          border: '1px solid #c8dfc5',
          borderRadius: '18px',
          overflow: 'hidden',
          boxShadow: '0 20px 60px rgba(30, 80, 40, 0.20)',
        }}
      >
        <div
          className="modal-head"
          style={{
            background: 'linear-gradient(135deg, #dcefd9, #c5e4c5)',
            borderBottom: '1px solid #c5dfc2',
            padding: '20px 24px',
          }}
        >
          <h3
            style={{
              color: '#245b35',
              fontSize: '20px',
              fontWeight: 700,
              margin: 0,
            }}
          >
            {title}
          </h3>

          <button
            className="icon-btn"
            aria-label="Close"
            onClick={onClose}
            style={{
              background: '#eaf5e8',
              color: '#285c38',
              border: '1px solid #b9d8b7',
              borderRadius: '8px',
              cursor: 'pointer',
            }}
          >
            <X size={18} />
          </button>
        </div>

        <div
          className="modal-body"
          style={{
            background: '#f3faf2',
            padding: '24px',
          }}
        >
          {children}
        </div>
      </div>
    </div>
  );
}
