import '../styles/operations.css';
import { useCallback, useEffect, useState } from 'react';
import { BarChart3, RefreshCw, Sprout, Droplets, Tractor, Wallet } from 'lucide-react';
import { listFarms } from '../services/farmService';
import { request } from '../services/api';
import { getToken } from '../services/authService';

const metrics = [
  ['totalFarms', 'Accessible farms', Sprout], ['totalPlots', 'Total plots', BarChart3],
  ['activeCropCycles', 'Active crop cycles', Sprout], ['totalWaterUsedLiters', 'Water used (L)', Droplets],
  ['totalMachineryHours', 'Machinery hours', Tractor], ['totalOperationalCost', 'Operational cost', Wallet],
  ['totalHarvestQuantity', 'Harvest quantity', BarChart3], ['totalCultivatedArea', 'Cultivated area', Sprout],
];
const number = (value) => Number(value ?? 0).toLocaleString(undefined, { maximumFractionDigits: 2 });

export default function AgriculturalAnalytics() {
  const [farms, setFarms] = useState([]);
  const [farmId, setFarmId] = useState('');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const load = useCallback(async (id) => {
    setLoading(true); setError('');
    try {
      const token = getToken();
      if (!token) throw new Error('Please log in to view analytics.');
      const query = id ? `?farmId=${encodeURIComponent(id)}` : '';
      const result = await request(`/api/analytics/dashboard${query}`, { token });
      setData(result);
    } catch (e) { setError(e.message || 'Could not load analytics.'); setData(null); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { (async () => { try { const rows = await listFarms(); setFarms(rows); const first = rows[0] ? String(rows[0].id) : ''; setFarmId(first); await load(first); } catch (e) { setError(e.message); setLoading(false); } })(); }, [load]);
  return <div className="page-wrap"><div className="page-heading"><div><p className="eyebrow">INSIGHTS</p><h1><BarChart3 size={25}/> Agricultural Analytics</h1><p>Operational metrics calculated from records in your accessible farms.</p></div><button className="btn ghost" onClick={() => load(farmId)} disabled={loading}><RefreshCw size={16}/> Refresh</button></div>
    <section className="card" style={{marginBottom:18}}><label htmlFor="analytics-farm">Farm scope</label><select id="analytics-farm" className="form-control" value={farmId} onChange={(e) => { setFarmId(e.target.value); load(e.target.value); }}><option value="">All accessible farms</option>{farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}</select></section>
    {error && <div className="alert error" role="alert">{error}</div>}{loading ? <div className="card">Loading analytics…</div> : data && <div className="cards">{metrics.map(([key, label, Icon]) => <article className="card" key={key}><div className="stat"><Icon size={23}/><div><strong>{number(data[key])}</strong><span>{label}</span></div></div></article>)}</div>}
    {data && <section className="card" style={{marginTop:18}}><h2>Cost breakdown</h2><div className="table-wrap"><table><thead><tr><th>Category</th><th>Amount</th></tr></thead><tbody><tr><td>Equipment</td><td>{number(data.totalEquipmentCost)}</td></tr><tr><td>Labour</td><td>{number(data.totalLabourCost)}</td></tr><tr><td>Inventory</td><td>{number(data.totalInventoryCost)}</td></tr><tr><th>Total operational cost</th><th>{number(data.totalOperationalCost)}</th></tr></tbody></table></div>{!data.revenueDataAvailable && <p className="muted">Revenue and net profit are not displayed because revenue records are not available in the current backend data model.</p>}</section>}
  </div>;
}
