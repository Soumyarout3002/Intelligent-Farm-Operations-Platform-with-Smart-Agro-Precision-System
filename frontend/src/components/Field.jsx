
import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

// Generic accessible form field
export default function Field({ field, value, error, onChange }) {
  const [show, setShow] = useState(false);

  const {
    name,
    label,
    type = 'text',
    options = [],
    placeholder,
    disabled,
    full
  } = field;

  const id = `f-${name}`;

  const common = {
    id,
    name,
    disabled,
    'aria-invalid': !!error,
    'aria-describedby': error ? `${id}-err` : undefined
  };

  // Display friendly names in dropdowns
  const formatOption = (option) => {
    if (option === 'fieldWorker') {
      return 'Field Worker';
    }

    return String(option)
      .replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/^./, (char) => char.toUpperCase());
  };

  let control;

  if (type === 'select') {
    control = (
      <select
        {...common}
        value={value ?? ''}
        onChange={(e) => onChange(name, e.target.value)}
      >
        <option value="">Select…</option>

        {options.map((option) => (
          <option key={option} value={option}>
            {formatOption(option)}
          </option>
        ))}
      </select>
    );
  } else if (type === 'textarea') {
    control = (
      <textarea
        {...common}
        rows={3}
        value={value ?? ''}
        placeholder={placeholder}
        onChange={(e) => onChange(name, e.target.value)}
      />
    );
  } else if (type === 'checkbox') {
    return (
      <div className="field full">
        <label className="check">
          <input
            {...common}
            type="checkbox"
            checked={!!value}
            onChange={(e) => onChange(name, e.target.checked)}
          />
          <span>{label}</span>
        </label>

        {error && (
          <p className="err" id={`${id}-err`}>
            {error}
          </p>
        )}
      </div>
    );
  } else if (type === 'password') {
    control = (
      <div className="pw-wrap">
        <input
          {...common}
          type={show ? 'text' : 'password'}
          value={value ?? ''}
          placeholder={placeholder}
          autoComplete="new-password"
          onChange={(e) => onChange(name, e.target.value)}
        />

        <button
          type="button"
          className="icon-btn"
          aria-label={show ? 'Hide password' : 'Show password'}
          onClick={() => setShow((previous) => !previous)}
        >
          {show ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>
    );
  } else {
    control = (
      <input
        {...common}
        type={type}
        step={type === 'number' ? 'any' : undefined}
        value={value ?? ''}
        placeholder={placeholder}
        onChange={(e) => onChange(name, e.target.value)}
      />
    );
  }

  return (
    <div className={`field ${full ? 'full' : ''}`}>
      <label htmlFor={id}>{label}</label>

      {control}

      {error && (
        <p className="err" id={`${id}-err`}>
          {error}
        </p>
      )}
    </div>
  );
}
