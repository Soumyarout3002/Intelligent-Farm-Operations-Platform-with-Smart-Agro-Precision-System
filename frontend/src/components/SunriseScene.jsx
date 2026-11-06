// Pure-SVG sunrise over a cornfield (no external images, so it never fails to load).
const HORIZON = 300;
const W = 1600;
const H = 900;

function rng(seed) {
  let a = seed;
  return () => {
    a |= 0; a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

const GREENS = ['#a8d24a', '#8fc13c', '#76ab2f', '#5f9526', '#4a7f1f', '#c4df5f'];

// Build the plants once: far rows are small and hazy, near rows are large.
const plants = (() => {
  const r = rng(7);
  const out = [];
  const layers = [[110, 0.02, 0.28], [70, 0.28, 0.55], [34, 0.55, 0.8], [12, 0.8, 1]];
  layers.forEach(([count, t0, t1]) => {
    for (let i = 0; i < count; i++) {
      const t = t0 + r() * (t1 - t0);
      const by = HORIZON + 8 + Math.pow(t, 1.7) * (H - HORIZON + 40);
      const spread = 0.25 + t * 1.5;
      const bx = W / 2 + (r() - 0.5) * W * spread * 1.15;
      const size = 10 + Math.pow(t, 1.4) * 240;
      const leaves = [];
      const n = 5 + Math.floor(r() * 3);
      for (let k = 0; k < n; k++) {
        const a = ((k / (n - 1)) - 0.5) * 1.9 + (r() - 0.5) * 0.25;
        const L = size * (0.85 + r() * 0.6);
        const ex = bx + Math.sin(a) * L;
        const ey = by - Math.cos(a) * L * 0.9 + (Math.abs(a) > 0.35 ? L * (0.2 + Math.abs(a) * 0.35) : 0);
        const mx = (bx + ex) / 2, my = (by + ey) / 2;
        const px = Math.cos(a) * L * 0.1, py = Math.sin(a) * L * 0.1;
        const f = (v) => v.toFixed(1);
        leaves.push({
          d: `M${f(bx)} ${f(by)}Q${f(mx + px)} ${f(my + py)} ${f(ex)} ${f(ey)}Q${f(mx - px)} ${f(my - py)} ${f(bx)} ${f(by)}Z`,
          fill: GREENS[Math.floor(r() * GREENS.length)],
          op: 0.55 + t * 0.45,
        });
      }
      out.push({ t, leaves });
    }
  });
  return out.sort((a, b) => a.t - b.t);
})();

export default function SunriseScene({ className = '' }) {
  const sx = 280;
  return (
    <svg className={className} viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="xMidYMid slice" aria-hidden="true" focusable="false">
      <defs>
        <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#6d8fb5" /><stop offset="0.35" stopColor="#b7c5cf" />
          <stop offset="0.7" stopColor="#f6d795" /><stop offset="1" stopColor="#ffbf4d" />
        </linearGradient>
        <radialGradient id="sun" cx="50%" cy="50%" r="50%">
          <stop offset="0" stopColor="#fffef2" /><stop offset="0.12" stopColor="#fff3b8" />
          <stop offset="0.35" stopColor="#ffd45e" stopOpacity="0.75" /><stop offset="1" stopColor="#ffc94d" stopOpacity="0" />
        </radialGradient>
        <linearGradient id="field" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#9bb04a" /><stop offset="0.25" stopColor="#5f8a2c" /><stop offset="1" stopColor="#1c4720" />
        </linearGradient>
        <radialGradient id="haze" cx="0.17" cy="0.33" r="0.55">
          <stop offset="0" stopColor="#ffd56a" stopOpacity="0.85" /><stop offset="1" stopColor="#ffe08a" stopOpacity="0" />
        </radialGradient>
        <linearGradient id="vignette" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0.6" stopColor="#0d2a12" stopOpacity="0" /><stop offset="1" stopColor="#0d2a12" stopOpacity="0.55" />
        </linearGradient>
        <filter id="soft" x="-20%" y="-50%" width="140%" height="200%"><feGaussianBlur stdDeviation="14" /></filter>
        <filter id="soft2"><feGaussianBlur stdDeviation="3" /></filter>
      </defs>

      <rect width={W} height={H} fill="url(#sky)" />
      <circle cx={sx} cy={HORIZON - 6} r="460" fill="url(#sun)" />
      <circle cx={sx} cy={HORIZON - 6} r="40" fill="#fffef5" filter="url(#soft2)" />

      <g filter="url(#soft)">
        <ellipse cx="760" cy="90" rx="320" ry="38" fill="#e9dccb" opacity=".55" />
        <ellipse cx="1150" cy="60" rx="300" ry="34" fill="#f3c98a" opacity=".6" />
        <ellipse cx="1420" cy="150" rx="240" ry="30" fill="#f0b97a" opacity=".6" />
        <ellipse cx="520" cy="170" rx="260" ry="26" fill="#9db3c8" opacity=".55" />
        <ellipse cx="980" cy="215" rx="340" ry="22" fill="#f8d894" opacity=".7" />
        <ellipse cx="120" cy="60" rx="220" ry="30" fill="#f1cf9a" opacity=".6" />
        <ellipse cx="1000" cy="270" rx="500" ry="20" fill="#8896a8" opacity=".35" />
      </g>

      <rect y={HORIZON} width={W} height={H - HORIZON} fill="url(#field)" />
      <rect y={HORIZON} width={W} height={H - HORIZON} fill="url(#haze)" />
      {[-900, -500, -250, -80, 80, 250, 500, 900].map((o) => (
        <line key={o} x1={W / 2 + o * 0.12} y1={HORIZON} x2={W / 2 + o * 2.4} y2={H} stroke="#2a4d1a" strokeOpacity=".3" strokeWidth="2" />
      ))}

      {plants.map((p, i) => (
        <g key={i}>{p.leaves.map((l, j) => <path key={j} d={l.d} fill={l.fill} fillOpacity={l.op} />)}</g>
      ))}

      <rect y={HORIZON} width={W} height={H - HORIZON} fill="url(#haze)" opacity=".45" />
      <rect width={W} height={H} fill="url(#vignette)" />
    </svg>
  );
}
