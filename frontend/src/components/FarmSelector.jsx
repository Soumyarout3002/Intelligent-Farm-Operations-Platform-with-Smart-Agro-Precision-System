import { useLocation } from 'react-router-dom';
import { RefreshCw, Tractor } from 'lucide-react';
import { useFarm } from '../context/FarmContext';

// Pages that genuinely work on one farm at a time and therefore use the shared selection.
export const FARM_SCOPED_PATHS = ['/overview', '/inventory', '/equipment', '/resources', '/weather', '/irrigation', '/analytics'];

export default function FarmSelector() {
  const { pathname } = useLocation();
  const { farms, status, error, selectedFarmId, setSelectedFarmId, reload } = useFarm();

  if (!FARM_SCOPED_PATHS.some((p) => pathname === p || pathname.startsWith(`${p}/`))) return null;

  return (
    <div className="farm-selector" role="group" aria-label="Active farm">
      <Tractor size={16} aria-hidden="true" />
      {status === 'loading' && <span className="farm-selector-note">Loading farms…</span>}
      {status === 'error' && (
        <>
          <span className="farm-selector-note err-text" title={error}>Farms unavailable</span>
          <button className="icon-btn" onClick={reload} aria-label="Retry loading farms"><RefreshCw size={15} /></button>
        </>
      )}
      {status === 'ready' && farms.length === 0 && <span className="farm-selector-note">No farms available</span>}
      {status === 'ready' && farms.length > 0 && (
        <select aria-label="Active farm" value={selectedFarmId} onChange={(e) => setSelectedFarmId(e.target.value)}>
          {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
        </select>
      )}
    </div>
  );
}
