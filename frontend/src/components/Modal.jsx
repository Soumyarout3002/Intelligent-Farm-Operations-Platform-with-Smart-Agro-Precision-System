import { useEffect } from 'react';

export default function Modal({
  children,
  title,
  onClose,
  wide = false,
}) {
  // Prevent the background page from scrolling while modal is open.
  useEffect(() => {
    const previousOverflow = document.body.style.overflow;

    document.body.style.overflow = 'hidden';

    // Close modal when Escape is pressed.
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose?.();
      }
    };

    window.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [onClose]);

  const overlayStyle = {
    position: 'fixed',
    inset: 0,
    zIndex: 9999,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    padding: '16px',
    overflowY: 'auto',
  };

  const modalStyle = {
    width: '100%',
    maxWidth: wide ? '850px' : '600px',
    maxHeight: '90vh',
    backgroundColor: '#ffffff',
    borderRadius: '16px',
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
    boxShadow: '0 20px 60px rgba(0, 0, 0, 0.2)',
    margin: 'auto',
  };

  const headerStyle = {
    flexShrink: 0,
    padding: '18px 24px',
    backgroundColor: '#edf7ed',
    borderBottom: '1px solid #e0e9e0',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  };

  const bodyStyle = {
    flex: 1,
    minHeight: 0,
    overflowY: 'auto',
    overscrollBehavior: 'contain',
    WebkitOverflowScrolling: 'touch',
    padding: '24px',
  };

  const closeButtonStyle = {
    border: 'none',
    background: 'transparent',
    fontSize: '25px',
    cursor: 'pointer',
    color: '#334155',
    padding: '0 5px',
  };

  return (
    <div
      style={overlayStyle}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose?.();
        }
      }}
    >
      <div
        style={modalStyle}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        {/* Fixed modal header */}
        <div style={headerStyle}>
          <h2
            id="modal-title"
            style={{
              margin: 0,
              fontSize: '20px',
              fontWeight: 600,
              color: '#183d25',
            }}
          >
            {title}
          </h2>

          <button
            type="button"
            onClick={onClose}
            aria-label="Close modal"
            style={closeButtonStyle}
          >
            ×
          </button>
        </div>

        {/* Only this section scrolls */}
        <div style={bodyStyle}>
          {children}
        </div>
      </div>
    </div>
  );
}