import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  MapPin, RefreshCw, Droplets, Wind, CloudRain, Umbrella, Thermometer, AlertTriangle,
  ShieldCheck, Tractor, Lightbulb,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { listFarms } from '../services/farmService';
import {
  WEATHER_SAMPLE_MODE, getCurrentWeather, getForecast, getWeatherAlerts, getWeatherHistory,
} from '../services/weatherService';
import WeatherIcon, { conditionMeta } from '../components/weather/WeatherIcon';
import { TemperatureChart, RainfallChart } from '../components/weather/WeatherCharts';
import '../styles/weather.css';

const HISTORY_RANGES = [7, 14, 30];
const SEVERITY_LABEL = { advisory: 'Advisory', warning: 'Warning', critical: 'Severe' };

// Small helper: run an async loader and track loading / error / data separately for each
// dashboard section, so one failing request never blanks the whole page.
function useAsync(loader, reloadKey) {
  const [state, setState] = useState({ status: 'loading', data: null, error: '' });
  useEffect(() => {
    if (!loader) return undefined;
    let ignore = false;
    setState((prev) => ({ ...prev, status: 'loading', error: '' }));
    loader()
      .then((data) => { if (!ignore) setState({ status: 'ready', data, error: '' }); })
      .catch((error) => { if (!ignore) setState({ status: 'error', data: null, error: error.message || 'Something went wrong.' }); });
    return () => { ignore = true; };
  }, [loader, reloadKey]);
  return state;
}

const parseIso = (iso) => { const [y, m, d] = iso.split('-').map(Number); return new Date(y, m - 1, d); };
const dayLabel = (iso, index) => {
  if (index === 0) return 'Today';
  if (index === 1) return 'Tomorrow';
  return parseIso(iso).toLocaleDateString(undefined, { weekday: 'short' });
};
const dateLabel = (iso) => parseIso(iso).toLocaleDateString(undefined, { day: 'numeric', month: 'short' });
const fullDay = (iso) => parseIso(iso).toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'short' });
const timeLabel = (iso) => new Date(iso).toLocaleString(undefined, { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' });
const num = (v, digits = 0) => (Number.isFinite(Number(v)) ? Number(v).toFixed(digits) : '–');

function SectionError({ message, onRetry }) {
  return (
    <div className="alert error wx-error" role="alert">
      <span>{message}</span>
      <button className="btn ghost sm" onClick={onRetry}>Try again</button>
    </div>
  );
}

function Skeleton({ height = 120 }) {
  return <div className="wx-skeleton" style={{ height }} aria-busy="true" aria-label="Loading" />;
}

function CurrentWeather({ state, farm, onRetry }) {
  if (state.status === 'loading') return <Skeleton height={190} />;
  if (state.status === 'error') return <SectionError message={`Could not load current weather. ${state.error}`} onRetry={onRetry} />;
  const c = state.data;
  const { label } = conditionMeta(c.condition);
  const tiles = [
    { label: 'Humidity', value: `${num(c.humidityPercent)}%`, icon: Droplets, tone: 'teal' },
    { label: 'Wind speed', value: `${num(c.windSpeedKmh)} km/h`, icon: Wind, tone: 'slate' },
    { label: 'Rainfall today', value: `${num(c.rainfallMm, 1)} mm`, icon: CloudRain, tone: 'blue' },
    { label: 'Chance of rain', value: c.rainProbability != null ? `${num(c.rainProbability)}%` : '–', icon: Umbrella, tone: 'blue' },
  ];
  return (
    <div className="wx-current">
      <div className="card wx-hero">
        <div className="wx-hero-icon"><WeatherIcon condition={c.condition} size={72} /></div>
        <div className="wx-hero-main">
          <span className="wx-hero-place"><MapPin size={14} /> {farm.name}{farm.location ? ` · ${farm.location}` : ''}</span>
          <div className="wx-hero-temp">{num(c.temperatureC)}<sup>°C</sup></div>
          <strong>{label}</strong>
          <span className="wx-hero-sub">Feels like {num(c.feelsLikeC)}°C · Updated {timeLabel(c.observedAt)}</span>
        </div>
        {c.source === 'sample' && <span className="tagpill wx-sample-pill">Sample data</span>}
      </div>
      <div className="wx-tiles">
        {tiles.map(({ label: l, value, icon: Icon, tone }) => (
          <div className="card wx-tile" key={l}>
            <span className={`wx-tile-icon tone-${tone}`}><Icon size={20} /></span>
            <div><strong>{value}</strong><span>{l}</span></div>
          </div>
        ))}
      </div>
    </div>
  );
}

function Forecast({ state, onRetry }) {
  return (
    <section className="card">
      <div className="row-between"><h3>7-day forecast {state.data?.source === 'sample' && <small className="tagpill">Sample data</small>}</h3></div>
      {state.status === 'loading' && <Skeleton height={170} />}
      {state.status === 'error' && <SectionError message={`Could not load the forecast. ${state.error}`} onRetry={onRetry} />}
      {state.status === 'ready' && (state.data.days.length === 0
        ? <div className="empty">No forecast data is available for this farm yet.</div>
        : (
          <div className="wx-forecast" role="list">
            {state.data.days.slice(0, 7).map((d, i) => (
              <div className={`wx-day ${i === 0 ? 'today' : ''}`} role="listitem" key={d.date}
                aria-label={`${fullDay(d.date)}: ${conditionMeta(d.condition).label}, high ${num(d.maxTemp)} degrees, low ${num(d.minTemp)} degrees, ${num(d.rainProbability)} percent chance of rain`}>
                <strong>{dayLabel(d.date, i)}</strong>
                <small>{dateLabel(d.date)}</small>
                <WeatherIcon condition={d.condition} size={34} />
                <span className="wx-day-cond">{conditionMeta(d.condition).label}</span>
                <span className="wx-day-temp"><b>{num(d.maxTemp)}°</b> / {num(d.minTemp)}°</span>
                <span className="wx-day-rain"><Umbrella size={13} /> {num(d.rainProbability)}%</span>
                <span className="wx-day-mm">{num(d.rainfallMm, 1)} mm</span>
              </div>
            ))}
          </div>
        ))}
    </section>
  );
}

function Alerts({ state, onRetry }) {
  const alerts = state.data?.alerts || [];
  return (
    <section className="card">
      <h3>Weather alerts {state.data?.source === 'sample' && <small className="tagpill">Sample data</small>}</h3>
      {state.status === 'loading' && <Skeleton height={120} />}
      {state.status === 'error' && <SectionError message={`Could not load alerts. ${state.error}`} onRetry={onRetry} />}
      {state.status === 'ready' && alerts.length === 0 && (
        <div className="wx-all-clear"><ShieldCheck size={26} /><div><strong>No active weather alerts</strong><span>No heavy rain, heatwave or strong wind expected in the coming days.</span></div></div>
      )}
      {state.status === 'ready' && alerts.length > 0 && (
        <ul className="wx-alerts">
          {alerts.map((a) => (
            <li key={a.id} className={`wx-alert sev-${a.severity}`}>
              <AlertTriangle size={20} />
              <div>
                <div className="wx-alert-head"><strong>{a.title}</strong><span className={`wx-sev sev-${a.severity}`}>{SEVERITY_LABEL[a.severity] || 'Alert'}</span></div>
                <p>{a.message}</p>
                {a.advice && <p className="wx-advice">{a.advice}</p>}
                {a.date && <small>{fullDay(a.date)}</small>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

// Plain-language tips derived from the forecast (also the rain outlook Irrigation will reuse).
function Outlook({ state }) {
  const tips = useMemo(() => {
    if (state.status !== 'ready') return [];
    const days = state.data.days.slice(0, 7);
    const out = [];
    const rainy = days.findIndex((d, i) => i > 0 && (d.rainProbability >= 60 || d.rainfallMm >= 10));
    out.push(rainy >= 0
      ? `Rain is likely on ${dayLabel(days[rainy].date, rainy)} (${days[rainy].rainProbability}% chance) - consider skipping irrigation that day.`
      : 'No significant rain expected in the next week - keep your regular irrigation schedule.');
    const hot = days.find((d) => d.maxTemp >= 38);
    if (hot) out.push(`High of ${num(hot.maxTemp)}°C on ${fullDay(hot.date)} - water early in the morning or in the evening.`);
    const windy = days.find((d) => d.windSpeedKmh >= 30);
    if (windy) out.push(`Windy on ${fullDay(windy.date)} (${num(windy.windSpeedKmh)} km/h) - avoid spraying that day.`);
    const dry = days.filter((d) => d.rainProbability < 25).length;
    if (dry >= 5) out.push('Mostly dry week - a good window for sowing, spraying and harvesting.');
    return out;
  }, [state]);

  return (
    <section className="card">
      <h3><Lightbulb size={17} className="wx-h-icon" /> Farm planning tips</h3>
      {state.status === 'loading' && <Skeleton height={100} />}
      {state.status === 'error' && <p className="muted">Tips need the forecast, which could not be loaded.</p>}
      {state.status === 'ready' && <ul className="wx-tips">{tips.map((t) => <li key={t}>{t}</li>)}</ul>}
    </section>
  );
}

function History({ state, range, onRange, onRetry }) {
  const days = state.data?.days || [];
  const summary = useMemo(() => {
    if (!days.length) return null;
    const total = days.reduce((s, d) => s + d.rainfallMm, 0);
    const wettest = days.reduce((a, b) => (b.rainfallMm > a.rainfallMm ? b : a));
    return {
      avgHigh: days.reduce((s, d) => s + d.maxTemp, 0) / days.length,
      avgLow: days.reduce((s, d) => s + d.minTemp, 0) / days.length,
      total,
      rainyDays: days.filter((d) => d.rainfallMm > 0).length,
      wettest,
    };
  }, [days]);

  return (
    <section className="card">
      <div className="row-between wx-hist-head">
        <h3>Weather history {state.data?.source === 'sample' && <small className="tagpill">Sample data</small>}</h3>
        <div className="wx-seg" role="group" aria-label="History range">
          {HISTORY_RANGES.map((r) => (
            <button key={r} type="button" aria-pressed={range === r} className={range === r ? 'on' : ''} onClick={() => onRange(r)}>Last {r} days</button>
          ))}
        </div>
      </div>
      {state.status === 'loading' && <Skeleton height={260} />}
      {state.status === 'error' && <SectionError message={`Could not load weather history. ${state.error}`} onRetry={onRetry} />}
      {state.status === 'ready' && days.length === 0 && <div className="empty">No historical weather is available for this farm yet.</div>}
      {state.status === 'ready' && summary && (
        <>
          <div className="wx-hist-stats">
            <div><span>Average high</span><strong>{num(summary.avgHigh, 1)}°C</strong></div>
            <div><span>Average low</span><strong>{num(summary.avgLow, 1)}°C</strong></div>
            <div><span>Total rainfall</span><strong>{num(summary.total, 1)} mm</strong></div>
            <div><span>Rainy days</span><strong>{summary.rainyDays} of {days.length}</strong></div>
            <div><span>Wettest day</span><strong>{summary.wettest.rainfallMm > 0 ? `${dateLabel(summary.wettest.date)} · ${num(summary.wettest.rainfallMm, 1)} mm` : 'None'}</strong></div>
          </div>
          <div className="wx-charts">
            <div><h4><Thermometer size={15} /> Temperature trend</h4><TemperatureChart days={days} /></div>
            <div><h4><CloudRain size={15} /> Rainfall trend</h4><RainfallChart days={days} /></div>
          </div>
        </>
      )}
    </section>
  );
}

export default function WeatherMonitoring() {
  const { user } = useAuth();
  const [farms, setFarms] = useState([]);
  const [farmsState, setFarmsState] = useState('loading');
  const [farmsError, setFarmsError] = useState('');
  const [farmId, setFarmId] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [range, setRange] = useState(30);
  const [farmsReload, setFarmsReload] = useState(0);

  // Farms come from the existing Farm Management API - users never re-enter farm details.
  // The backend already limits this list by role (farmer: own, field worker: assigned, admin: all).
  useEffect(() => {
    let ignore = false;
    setFarmsState('loading');
    listFarms()
      .then((list) => {
        if (ignore) return;
        setFarms(list);
        setFarmId((current) => (list.some((f) => String(f.id) === current) ? current : list[0] ? String(list[0].id) : ''));
        setFarmsState('ready');
      })
      .catch((error) => { if (!ignore) { setFarmsError(error.message); setFarmsState('error'); } });
    return () => { ignore = true; };
  }, [farmsReload]);

  const farm = useMemo(() => farms.find((f) => String(f.id) === farmId) || null, [farms, farmId]);

  const loadCurrent = useCallback(() => getCurrentWeather(farm), [farm]);
  const loadForecast = useCallback(() => getForecast(farm, 7), [farm]);
  const loadAlerts = useCallback(() => getWeatherAlerts(farm), [farm]);
  const loadHistory = useCallback(() => getWeatherHistory(farm, range), [farm, range]);

  // reloadKey re-runs every loader when the user presses Refresh or "Try again".
  const current = useAsync(farm ? loadCurrent : null, reloadKey);
  const forecast = useAsync(farm ? loadForecast : null, reloadKey);
  const alerts = useAsync(farm ? loadAlerts : null, reloadKey);
  const history = useAsync(farm ? loadHistory : null, reloadKey);
  const refresh = () => setReloadKey((k) => k + 1);

  const canAddFarm = user.role !== 'field_worker';
  const topAlert = alerts.status === 'ready' ? alerts.data.alerts.find((a) => a.severity === 'critical') : null;

  return (
    <div className="wx-page">
      <div className="page-head">
        <div>
          <h1>Weather Monitoring</h1>
          <p className="muted">Current conditions, forecast, alerts and history for your farms.</p>
        </div>
        <div className="actions-row wx-controls">
          <div className="field wx-farm-select">
            <label htmlFor="wx-farm">Farm</label>
            <select id="wx-farm" value={farmId} onChange={(e) => setFarmId(e.target.value)} disabled={farmsState !== 'ready' || farms.length === 0}>
              {farms.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
            </select>
          </div>
          <button className="btn ghost" onClick={refresh} disabled={!farm}><RefreshCw size={16} /> Refresh</button>
        </div>
      </div>

      {WEATHER_SAMPLE_MODE && (
        <div className="alert info" role="note">
          <b>Sample data.</b> The weather service is not connected yet, so the values below are generated demo data and are <b>not live weather</b>.
        </div>
      )}

      {farmsState === 'loading' && <Skeleton height={190} />}
      {farmsState === 'error' && <SectionError message={`Could not load your farms. ${farmsError}`} onRetry={() => setFarmsReload((k) => k + 1)} />}
      {farmsState === 'ready' && farms.length === 0 && (
        <div className="empty big">
          <Tractor size={36} />
          <h2>No farms to show weather for</h2>
          <p>{canAddFarm ? 'Add a farm in Farm Management and its weather will appear here.' : 'You have not been assigned to a farm yet. Ask your farmer or admin to assign you.'}</p>
          {canAddFarm && <Link className="btn primary" to="/farms">Go to Farm Management</Link>}
        </div>
      )}

      {farm && (
        <>
          {topAlert && (
            <div className="alert error wx-banner" role="alert"><AlertTriangle size={18} /> <b>{topAlert.title}.</b> {topAlert.message}</div>
          )}
          <CurrentWeather state={current} farm={farm} onRetry={refresh} />
          <Forecast state={forecast} onRetry={refresh} />
          <div className="two-col wx-two">
            <Alerts state={alerts} onRetry={refresh} />
            <Outlook state={forecast} />
          </div>
          <History state={history} range={range} onRange={setRange} onRetry={refresh} />
        </>
      )}
    </div>
  );
}
