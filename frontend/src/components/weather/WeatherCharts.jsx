import { useState } from 'react';

// Lightweight SVG charts (no chart library is installed in this project).
const W = 640;
const H = 240;
const PAD = { top: 16, right: 16, bottom: 30, left: 40 };
const innerW = W - PAD.left - PAD.right;
const innerH = H - PAD.top - PAD.bottom;

const shortDate = (iso) => {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d).toLocaleDateString(undefined, { day: 'numeric', month: 'short' });
};

function niceMax(value, step) {
  return Math.max(step, Math.ceil(value / step) * step);
}

// Show roughly 6 x-axis labels regardless of the range.
const labelEvery = (n) => Math.max(1, Math.ceil(n / 6));

function Readout({ children }) {
  return <div className="wx-readout" aria-live="polite">{children}</div>;
}

export function TemperatureChart({ days }) {
  const [hover, setHover] = useState(null);
  if (!days.length) return null;
  const lo = Math.floor(Math.min(...days.map((d) => d.minTemp)) / 5) * 5;
  const hi = Math.ceil(Math.max(...days.map((d) => d.maxTemp)) / 5) * 5;
  const x = (i) => PAD.left + (days.length === 1 ? innerW / 2 : (i / (days.length - 1)) * innerW);
  const y = (v) => PAD.top + innerH - ((v - lo) / (hi - lo || 1)) * innerH;
  const line = (field) => days.map((d, i) => `${i ? 'L' : 'M'}${x(i).toFixed(1)},${y(d[field]).toFixed(1)}`).join(' ');
  const ticks = [];
  for (let v = lo; v <= hi; v += 5) ticks.push(v);
  const active = hover !== null ? days[hover] : null;

  return (
    <div>
      <Readout>
        {active
          ? <><b>{shortDate(active.date)}</b> · High {active.maxTemp}°C · Low {active.minTemp}°C</>
          : 'Hover or tap the chart to see daily values'}
      </Readout>
      <svg viewBox={`0 0 ${W} ${H}`} className="wx-chart" role="img" aria-label="Daily high and low temperature chart">
        {ticks.map((v) => (
          <g key={v}>
            <line x1={PAD.left} x2={W - PAD.right} y1={y(v)} y2={y(v)} className="wx-grid" />
            <text x={PAD.left - 8} y={y(v) + 4} textAnchor="end" className="wx-axis">{v}°</text>
          </g>
        ))}
        {days.map((d, i) => (i % labelEvery(days.length) === 0 ? (
          <text key={d.date} x={x(i)} y={H - 8} textAnchor="middle" className="wx-axis">{shortDate(d.date)}</text>
        ) : null))}
        <path d={line('maxTemp')} className="wx-line wx-line-max" />
        <path d={line('minTemp')} className="wx-line wx-line-min" />
        {hover !== null && (
          <g>
            <line x1={x(hover)} x2={x(hover)} y1={PAD.top} y2={PAD.top + innerH} className="wx-cursor" />
            <circle cx={x(hover)} cy={y(active.maxTemp)} r="4" className="wx-dot-max" />
            <circle cx={x(hover)} cy={y(active.minTemp)} r="4" className="wx-dot-min" />
          </g>
        )}
        {days.map((d, i) => (
          <rect key={d.date} x={x(i) - innerW / days.length / 2} y={PAD.top} width={innerW / days.length} height={innerH}
            fill="transparent" tabIndex={0} onMouseEnter={() => setHover(i)} onFocus={() => setHover(i)}
            onTouchStart={() => setHover(i)} onMouseLeave={() => setHover(null)} onBlur={() => setHover(null)}
            aria-label={`${shortDate(d.date)}: high ${d.maxTemp} degrees, low ${d.minTemp} degrees`} />
        ))}
      </svg>
      <div className="wx-legend"><span><i className="wx-sw wx-sw-max" /> Daily high</span><span><i className="wx-sw wx-sw-min" /> Daily low</span></div>
    </div>
  );
}

export function RainfallChart({ days }) {
  const [hover, setHover] = useState(null);
  if (!days.length) return null;
  const max = niceMax(Math.max(...days.map((d) => d.rainfallMm)), 10);
  const slot = innerW / days.length;
  const bw = Math.max(3, slot * 0.62);
  const y = (v) => PAD.top + innerH - (v / max) * innerH;
  const ticks = [0, max / 2, max];
  const active = hover !== null ? days[hover] : null;

  return (
    <div>
      <Readout>
        {active
          ? <><b>{shortDate(active.date)}</b> · Rainfall {active.rainfallMm} mm</>
          : 'Hover or tap the chart to see daily values'}
      </Readout>
      <svg viewBox={`0 0 ${W} ${H}`} className="wx-chart" role="img" aria-label="Daily rainfall chart">
        {ticks.map((v) => (
          <g key={v}>
            <line x1={PAD.left} x2={W - PAD.right} y1={y(v)} y2={y(v)} className="wx-grid" />
            <text x={PAD.left - 8} y={y(v) + 4} textAnchor="end" className="wx-axis">{Math.round(v)}</text>
          </g>
        ))}
        {days.map((d, i) => {
          const cx = PAD.left + slot * i + slot / 2;
          return (
            <g key={d.date}>
              {d.rainfallMm > 0 && (
                <rect x={cx - bw / 2} y={y(d.rainfallMm)} width={bw} height={PAD.top + innerH - y(d.rainfallMm)} rx="2"
                  className={`wx-bar ${hover === i ? 'on' : ''}`} />
              )}
              {i % labelEvery(days.length) === 0 && <text x={cx} y={H - 8} textAnchor="middle" className="wx-axis">{shortDate(d.date)}</text>}
              <rect x={cx - slot / 2} y={PAD.top} width={slot} height={innerH} fill="transparent" tabIndex={0}
                onMouseEnter={() => setHover(i)} onFocus={() => setHover(i)} onTouchStart={() => setHover(i)}
                onMouseLeave={() => setHover(null)} onBlur={() => setHover(null)}
                aria-label={`${shortDate(d.date)}: ${d.rainfallMm} millimetres of rain`} />
            </g>
          );
        })}
      </svg>
      <div className="wx-legend"><span><i className="wx-sw wx-sw-rain" /> Rainfall (mm per day)</span></div>
    </div>
  );
}
