import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Activity, BarChart3, Coins, Droplets, Filter, Layers, Ruler, Sprout, Tractor, Wheat, Wrench } from 'lucide-react';
import { useFarm } from '../context/FarmContext';
import * as svc from '../services/analyticsService';
import '../styles/analytics.css';

const PALETTE = ['#0f766e', '#1e3a8a', '#2d8a4f', '#b45309', '#475569', '#7c3aed'];
const num = (v) => Number(v ?? 0);
const fmt = (v, d = 1) => num(v).toLocaleString(undefined, { maximumFractionDigits: d });
const money = (v) => num(v).toLocaleString(undefined, { maximumFractionDigits: 0 });

// Loads one analytics section; resets immediately when its inputs change so stale data never shows.
function useSection(loader, deps) {
  const [state, setState] = useState({ status: 'loading', data: null, error: '' });
  useEffect(() => {
    if (!loader) { setState({ status: 'idle', data: null, error: '' }); return undefined; }
    let ignore = false;
    setState({ status: 'loading', data: null, error: '' });
    loader()
      .then((data) => { if (!ignore) setState({ status: 'ready', data, error: '' }); })
      .catch((e) => { if (!ignore) setState({ status: 'error', data: null, error: e.message }); });
    return () => { ignore = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
  return state;
}

/* ---------- small SVG charts (no extra dependencies) ---------- */
function BarChart({ items, unit, color = PALETTE[0] }) {
  const W = 520, H = 220, padL = 44, padB = 44, padT = 12;
  const max = Math.max(...items.map((i) => i.value), 1);
  const bw = (W - padL - 10) / items.length;
  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="an-svg" role="img" aria-label={`Bar chart${unit ? ` in ${unit}` : ''}`}>
      {[0, 0.5, 1].map((t) => { const y = padT + (H - padT - padB) * (1 - t); return (
        <g key={t}><line x1={padL} x2={W - 6} y1={y} y2={y} stroke="#d7e0da" /><text x={padL - 6} y={y + 4} textAnchor="end" fontSize="10" fill="#475569">{fmt(max * t, 1)}</text></g>); })}
      {items.map((it, i) => {
        const h = ((H - padT - padB) * it.value) / max; const x = padL + i * bw + bw * 0.15;
        return (<g key={it.label}>
          <rect x={x} y={H - padB - h} width={bw * 0.7} height={h} rx="3" fill={color}><title>{`${it.label}: ${fmt(it.value, 2)}${unit ? ` ${unit}` : ''}`}</title></rect>
          <text x={x + bw * 0.35} y={H - padB + 14} textAnchor="middle" fontSize="10" fill="#334155">{it.label.length > 10 ? `${it.label.slice(0, 9)}…` : it.label}</text>
        </g>);
      })}
      {unit && <text x={padL} y={H - 6} fontSize="10" fill="#475569">Unit: {unit}</text>}
    </svg>
  );
}

function LineChart({ points, unit, color = PALETTE[1] }) {
  const W = 520, H = 220, padL = 44, padB = 34, padT = 12;
  const max = Math.max(...points.map((p) => p.value), 1);
  const step = points.length > 1 ? (W - padL - 14) / (points.length - 1) : 0;
  const xy = points.map((p, i) => [padL + i * step, padT + (H - padT - padB) * (1 - p.value / max)]);
  const every = Math.max(1, Math.ceil(points.length / 6));
  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="an-svg" role="img" aria-label={`Line chart${unit ? ` in ${unit}` : ''}`}>
      {[0, 0.5, 1].map((t) => { const y = padT + (H - padT - padB) * (1 - t); return (
        <g key={t}><line x1={padL} x2={W - 6} y1={y} y2={y} stroke="#d7e0da" /><text x={padL - 6} y={y + 4} textAnchor="end" fontSize="10" fill="#475569">{fmt(max * t, 0)}</text></g>); })}
      <polyline fill="none" stroke={color} strokeWidth="2.5" points={xy.map((p) => p.join(',')).join(' ')} />
      {xy.map((p, i) => (<g key={points[i].label}><circle cx={p[0]} cy={p[1]} r="3.5" fill={color}><title>{`${points[i].label}: ${fmt(points[i].value, 1)}${unit ? ` ${unit}` : ''}`}</title></circle>
        {i % every === 0 && <text x={p[0]} y={H - padB + 14} textAnchor="middle" fontSize="10" fill="#334155">{points[i].label.slice(5) || points[i].label}</text>}</g>))}
      {unit && <text x={padL} y={H - 6} fontSize="10" fill="#475569">Unit: {unit}</text>}
    </svg>
  );
}

function Donut({ items, unit }) {
  const total = items.reduce((s, i) => s + i.value, 0);
  const R = 70, C = 2 * Math.PI * R; let acc = 0;
  return (
    <div className="an-donut">
      <svg viewBox="0 0 200 200" role="img" aria-label="Donut chart">
        <circle cx="100" cy="100" r={R} fill="none" stroke="#e2e8f0" strokeWidth="26" />
        {items.map((it, i) => { const frac = total ? it.value / total : 0; const dash = `${frac * C} ${C}`; const off = -acc * C; acc += frac;
          return <circle key={it.label} cx="100" cy="100" r={R} fill="none" stroke={PALETTE[i % PALETTE.length]} strokeWidth="26" strokeDasharray={dash} strokeDashoffset={off} transform="rotate(-90 100 100)"><title>{`${it.label}: ${fmt(it.value, 0)}`}</title></circle>; })}
        <text x="100" y="98" textAnchor="middle" fontSize="18" fontWeight="700" fill="#0f172a">{money(total)}</text>
        <text x="100" y="116" textAnchor="middle" fontSize="10" fill="#475569">{unit || 'total'}</text>
      </svg>
      <ul>{items.map((it, i) => <li key={it.label}><i style={{ background: PALETTE[i % PALETTE.length] }} />{it.label}<b>{money(it.value)}{total ? ` (${Math.round((it.value / total) * 100)}%)` : ''}</b></li>)}</ul>
    </div>
  );
}

function Panel({ tone, title, subtitle, state, empty, children }) {
  return (
    <section className={`an-panel ${tone}`}>
      <header><h3>{title}</h3>{subtitle && <small>{subtitle}</small>}</header>
      {state.status === 'loading' && <div className="an-note">Loading…</div>}
      {state.status === 'error' && <div className="alert error">Could not load this section. {state.error}</div>}
      {state.status === 'ready' && empty ? <div className="an-note">{empty}</div> : state.status === 'ready' && children}
    </section>
  );
}

const Kpi = ({ icon: Icon, label, value, sub, tone }) => (
  <div className={`an-kpi ${tone}`}><Icon size={20} /><div><strong>{value}</strong><span>{label}</span>{sub && <small>{sub}</small>}</div></div>
);

export default function AgriculturalAnalytics() {
  const { selectedFarm, status: farmStatus, error: farmError, reload: reloadFarms } = useFarm();
  const farmId = selectedFarm?.id;
  const [draft, setDraft] = useState({ cropName: '', startDate: '', endDate: '' });
  const [applied, setApplied] = useState({ cropName: '', startDate: '', endDate: '' });
  const [formError, setFormError] = useState('');

  // New farm => start from clean filters so one farm's filters never carry over silently.
  useEffect(() => { const clean = { cropName: '', startDate: '', endDate: '' }; setDraft(clean); setApplied(clean); setFormError(''); }, [farmId]);

  const filters = useMemo(() => ({ farmId, ...applied }), [farmId, applied]);
  const dep = [farmId, applied.cropName, applied.startDate, applied.endDate];
  const dash = useSection(farmId ? () => svc.getDashboard(filters) : null, dep);
  const yld = useSection(farmId ? () => svc.getYield(filters) : null, dep);
  const cost = useSection(farmId ? () => svc.getCostProfit(filters) : null, dep);
  const water = useSection(farmId ? () => svc.getWaterUsage(filters) : null, dep);
  const mach = useSection(farmId ? () => svc.getMachinery(filters) : null, dep);

  const apply = (e) => {
    e.preventDefault();
    if (draft.startDate && draft.endDate && draft.startDate > draft.endDate) { setFormError('Start date cannot be after end date.'); return; }
    setFormError(''); setApplied({ ...draft, cropName: draft.cropName.trim() });
  };
  const reset = () => { const clean = { cropName: '', startDate: '', endDate: '' }; setDraft(clean); setApplied(clean); setFormError(''); };
  const filtered = applied.cropName || applied.startDate || applied.endDate;

  if (farmStatus === 'loading') return <div className="an-page"><div className="card">Loading farms…</div></div>;
  if (farmStatus === 'error') return <div className="an-page"><div className="alert error">Could not load your farms. {farmError} <button className="btn ghost sm" onClick={reloadFarms}>Try again</button></div></div>;
  if (!selectedFarm) return (
    <div className="an-page"><div className="empty big"><Tractor size={36} /><h2>No farms available</h2><p>Analytics are calculated per farm. Add a farm to see them here.</p><Link className="btn primary" to="/farms">Go to Farm Management</Link></div></div>
  );

  const d = dash.data;
  const yieldItems = (yld.data || []).map((y) => ({ label: y.season ? `${y.cropName} (${y.season})` : y.cropName, value: num(y.yieldPerAcre) }));
  const yieldUnit = (yld.data || [])[0]?.quantityUnit;
  const costItems = d ? [
    { label: 'Equipment', value: num(d.totalEquipmentCost) }, { label: 'Labour', value: num(d.totalLabourCost) }, { label: 'Inventory', value: num(d.totalInventoryCost) },
  ].filter((i) => i.value > 0) : [];
  const waterPts = (water.data?.dailyTrend || []).map((p) => ({ label: String(p.date), value: num(p.totalWaterLiters) }));
  const machItems = (mach.data?.machineBreakdown || []).map((m) => ({ label: m.equipmentName || `#${m.equipmentId}`, value: num(m.totalOperatingHours) }));

  return (
    <div className="an-page">
      <div className="page-head"><div><h1>Agricultural Analytics</h1>
        <p className="muted">Live figures from the backend for <strong>{selectedFarm.name}</strong>. Change the farm from the selector in the top bar.</p></div></div>

      {/* A. SUMMARY */}
      <section aria-label="Summary" className="an-kpis">
        {dash.status === 'loading' && <div className="an-note full">Loading summary…</div>}
        {dash.status === 'error' && <div className="alert error full">Could not load the summary. {dash.error}</div>}
        {d && <>
          <Kpi icon={Tractor} tone="navy" label="Total farms" value={d.totalFarms} />
          <Kpi icon={Ruler} tone="teal" label="Cultivated area" value={fmt(d.totalCultivatedArea, 2)} sub="as reported by backend" />
          <Kpi icon={Layers} tone="green" label="Total plots" value={d.totalPlots} />
          <Kpi icon={Sprout} tone="green" label="Active crop cycles" value={d.activeCropCycles} />
          <Kpi icon={Wheat} tone="amber" label="Total harvest" value={fmt(d.totalHarvestQuantity, 1)} sub={`Avg yield/acre ${fmt(d.averageYieldPerAcre, 2)}`} />
          <Kpi icon={Coins} tone="slate" label="Operational cost" value={money(d.totalOperationalCost)} sub={`Planned budget ${money(d.totalPlannedBudget)}`} />
          <Kpi icon={Droplets} tone="navy" label="Water used (L)" value={fmt(d.totalWaterUsedLiters, 0)} />
          <Kpi icon={Wrench} tone="slate" label="Machinery hours" value={fmt(d.totalMachineryHours, 1)} />
          <Kpi icon={Activity} tone="teal" label="Revenue" value={d.revenueDataAvailable ? money(d.totalRevenue) : 'Not tracked'} sub={d.revenueDataAvailable ? `Net profit ${money(d.netProfit)}` : 'No revenue data in system'} />
        </>}
      </section>

      {/* B. FILTERS */}
      <section className="an-filters" aria-label="Filters">
        <header><span className="an-tag"><Filter size={13} /> FILTERS</span></header>
        <form onSubmit={apply} noValidate>
          <div className="field"><label>Farm</label><div className="an-farm-chip"><Tractor size={14} /> {selectedFarm.name}</div></div>
          <div className="field"><label htmlFor="an-crop">Crop name</label><input id="an-crop" placeholder="e.g. Wheat" value={draft.cropName} onChange={(e) => setDraft({ ...draft, cropName: e.target.value })} /></div>
          <div className="field"><label htmlFor="an-from">From date</label><input id="an-from" type="date" value={draft.startDate} onChange={(e) => setDraft({ ...draft, startDate: e.target.value })} /></div>
          <div className="field"><label htmlFor="an-to">To date</label><input id="an-to" type="date" value={draft.endDate} onChange={(e) => setDraft({ ...draft, endDate: e.target.value })} /></div>
          <div className="an-filter-actions"><button className="btn an-apply" type="submit">Apply filters</button><button className="btn ghost" type="button" onClick={reset} disabled={!filtered && !draft.cropName && !draft.startDate && !draft.endDate}>Reset</button></div>
        </form>
        {formError && <p className="err">{formError}</p>}
        {filtered && <p className="an-active">Filters applied: {[applied.cropName && `crop “${applied.cropName}”`, applied.startDate && `from ${applied.startDate}`, applied.endDate && `to ${applied.endDate}`].filter(Boolean).join(', ')}</p>}
      </section>

      {/* C. OUTPUT */}
      <div className="an-grid">
        <Panel tone="green" title="Yield per acre by crop" subtitle={yieldUnit ? `Unit: ${yieldUnit} per acre` : undefined} state={yld} empty={(yld.data || []).length === 0 ? 'No harvest data for these filters.' : null}>
          <BarChart items={yieldItems} unit={yieldUnit ? `${yieldUnit}/acre` : undefined} color={PALETTE[2]} />
        </Panel>
        <Panel tone="amber" title="Cost distribution" subtitle="Equipment, labour and inventory cost" state={dash} empty={costItems.length === 0 ? 'No cost data recorded for these filters.' : null}>
          <Donut items={costItems} />
        </Panel>
        <Panel tone="navy" title="Daily water usage" subtitle="Litres per day from irrigation schedules" state={water} empty={waterPts.length === 0 ? 'No irrigation water data for these filters.' : null}>
          <LineChart points={waterPts} unit="litres" />
          {water.data && <p className="an-foot">Total {fmt(water.data.totalWaterUsedLiters, 0)} L · {water.data.totalSchedules} schedules ({water.data.completedSchedules} completed) · avg {fmt(water.data.averageWaterPerSchedule, 0)} L per schedule</p>}
        </Panel>
        <Panel tone="slate" title="Machinery operating hours" subtitle="Hours per machine" state={mach} empty={machItems.length === 0 ? 'No machinery usage recorded for these filters.' : null}>
          <BarChart items={machItems} unit="hours" color={PALETTE[4]} />
          {mach.data && <p className="an-foot">{fmt(mach.data.totalOperatingHours, 1)} h · {fmt(mach.data.totalFuelConsumed, 1)} fuel · cost {money(mach.data.totalMachineryCost)} · {mach.data.totalUsageRecords} usage records</p>}
        </Panel>
      </div>

      <Panel tone="teal" title="Cost &amp; budget by crop" subtitle="Per crop and season" state={cost} empty={(cost.data || []).length === 0 ? 'No cost data for these filters.' : null}>
        <div className="table-wrap"><table className="an-table"><thead><tr><th>Crop</th><th>Season</th><th>Cycles</th><th>Planned budget</th><th>Equipment</th><th>Labour</th><th>Inventory</th><th>Other</th><th>Total cost</th><th>Budget used</th></tr></thead>
          <tbody>{(cost.data || []).map((c, i) => (<tr key={`${c.cropName}-${c.season}-${i}`}><td>{c.cropName}</td><td>{c.season || '—'}</td><td>{c.cropCycleCount}</td><td>{money(c.plannedBudget)}</td><td>{money(c.equipmentCost)}</td><td>{money(c.labourCost)}</td><td>{money(c.inventoryCost)}</td><td>{money(c.otherActivityCost)}</td><td><b>{money(c.totalCost)}</b></td><td>{c.budgetUtilizationPercent == null ? '—' : `${fmt(c.budgetUtilizationPercent, 1)}%`}</td></tr>))}</tbody></table></div>
      </Panel>

      <Panel tone="navy" title="Machines" subtitle="Usage by machine" state={mach} empty={(mach.data?.machineBreakdown || []).length === 0 ? 'No machinery usage recorded for these filters.' : null}>
        <div className="table-wrap"><table className="an-table"><thead><tr><th>Machine</th><th>Type</th><th>Farm</th><th>Hours</th><th>Fuel</th><th>Cost</th><th>Uses</th></tr></thead>
          <tbody>{(mach.data?.machineBreakdown || []).map((m) => (<tr key={m.equipmentId}><td>{m.equipmentName}</td><td>{m.equipmentType}</td><td>{m.farmName}</td><td>{fmt(m.totalOperatingHours, 1)}</td><td>{fmt(m.totalFuelConsumed, 1)}</td><td>{money(m.totalCost)}</td><td>{m.usageCount}</td></tr>))}</tbody></table></div>
      </Panel>
      <p className="an-foot"><BarChart3 size={13} /> All values come from the Analytics API; nothing is estimated in the browser.</p>
    </div>
  );
}
