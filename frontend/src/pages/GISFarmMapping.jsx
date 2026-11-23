
import { useEffect, useMemo, useState } from 'react';

import {
  MapContainer,
  TileLayer,
  LayersControl,
  Polygon,
  CircleMarker,
  ZoomControl,
  useMapEvents,
  useMap,
} from 'react-leaflet';

import 'leaflet/dist/leaflet.css';

import {
  listGisFarms,
  getPlotBoundary,
  createPlotBoundary,
  updatePlotBoundary,
} from '../services/gisService';

const DEFAULT_CENTER = [20.5937, 78.9629];

const styles = {
  page: {
    padding: '20px',
    color: '#244536',
  },
  card: {
    background: '#ffffff',
    borderRadius: '16px',
    padding: '22px',
    marginBottom: '20px',
    boxShadow: '0 4px 16px rgba(0,0,0,0.06)',
  },
  heading: {
    color: '#185c38',
    fontSize: '22px',
    fontWeight: '700',
    marginBottom: '18px',
  },
  label: {
    display: 'block',
    fontWeight: '600',
    marginBottom: '7px',
    color: '#315744',
  },
  input: {
    width: '100%',
    padding: '11px 12px',
    border: '1px solid #cbd8ce',
    borderRadius: '8px',
    fontSize: '14px',
    outline: 'none',
    boxSizing: 'border-box',
  },
  select: {
    width: '100%',
    padding: '11px 12px',
    border: '1px solid #cbd8ce',
    borderRadius: '8px',
    background: '#fff',
    fontSize: '14px',
  },
  primaryButton: {
    background: '#269b58',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    padding: '11px 20px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  secondaryButton: {
    background: '#ffffff',
    color: '#21834b',
    border: '1px solid #269b58',
    borderRadius: '8px',
    padding: '10px 17px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  dangerButton: {
    background: '#fff0f0',
    color: '#c0392b',
    border: '1px solid #e8b4b4',
    borderRadius: '8px',
    padding: '10px 16px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  muted: {
    color: '#718477',
    fontSize: '14px',
  },
};

function getFarmId(farm) {
  return farm?.farmId ?? farm?.id ?? '';
}

function getPlotId(plot) {
  return plot?.plotId ?? plot?.id ?? '';
}

function getFarmName(farm) {
  return farm?.farmName ?? farm?.name ?? 'Unnamed Farm';
}

function getPlotName(plot) {
  return plot?.plotName ?? plot?.name ?? 'Unnamed Plot';
}

function getFarmList(response) {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.farms)) return response.farms;
  if (Array.isArray(response?.content)) return response.content;
  if (Array.isArray(response?.data)) return response.data;
  return [];
}

function parseBoundary(value) {
  if (!value) return [];

  let data = value;

  if (typeof data === 'string') {
    try {
      data = JSON.parse(data);
    } catch {
      return [];
    }
  }

  if (data?.data) data = data.data;
  if (data?.boundary) data = data.boundary;
  if (data?.boundaryGeoJson) {
    return parseBoundary(data.boundaryGeoJson);
  }

  if (data?.geometry) data = data.geometry;

  if (data?.type === 'Feature') {
    data = data.geometry;
  }

  if (data?.type === 'Polygon') {
    data = data.coordinates?.[0] ?? [];
  } else if (data?.coordinates) {
    data = data.coordinates;
  }

  if (!Array.isArray(data)) return [];

  // GeoJSON format: [longitude, latitude]
  // Leaflet format: [latitude, longitude]
  if (
    data.length > 0 &&
    Array.isArray(data[0]) &&
    typeof data[0][0] === 'number'
  ) {
    let pairs = data;

    // Handle an extra polygon nesting level.
    if (Array.isArray(pairs[0]) && Array.isArray(pairs[0][0])) {
      pairs = pairs[0];
    }

    const points = pairs
      .filter(
        (pair) =>
          Array.isArray(pair) &&
          typeof pair[0] === 'number' &&
          typeof pair[1] === 'number'
      )
      .map(([lng, lat]) => ({
        lat,
        lng,
      }));

    // Remove duplicate closing point, if present.
    if (points.length > 1) {
      const first = points[0];
      const last = points[points.length - 1];

      if (first.lat === last.lat && first.lng === last.lng) {
        points.pop();
      }
    }

    return points;
  }

  // Also accept an array of {lat, lng} objects.
  return data
    .filter(
      (point) =>
        point &&
        Number.isFinite(Number(point.lat)) &&
        Number.isFinite(Number(point.lng))
    )
    .map((point) => ({
      lat: Number(point.lat),
      lng: Number(point.lng),
    }));
}

function MapClickHandler({ onMapClick, enabled }) {
  useMapEvents({
    click(event) {
      if (enabled) {
        onMapClick({
          lat: Number(event.latlng.lat.toFixed(7)),
          lng: Number(event.latlng.lng.toFixed(7)),
        });
      }
    },
  });

  return null;
}

function MapFitBounds({ points }) {
  const map = useMap();

  useEffect(() => {
    if (points.length >= 2) {
      const bounds = points.map((point) => [
        point.lat,
        point.lng,
      ]);

      map.fitBounds(bounds, {
        padding: [35, 35],
        maxZoom: 18,
      });
    }
  }, [points, map]);

  return null;
}

export default function GISFarmMapping() {
  const [farms, setFarms] = useState([]);

  const [selectedFarmId, setSelectedFarmId] = useState('');
  const [selectedPlotId, setSelectedPlotId] = useState('');

  const [points, setPoints] = useState([]);

  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [hasSavedBoundary, setHasSavedBoundary] = useState(false);

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [mapClickEnabled, setMapClickEnabled] = useState(true);

  const selectedFarm = useMemo(
    () =>
      farms.find(
        (farm) => String(getFarmId(farm)) === String(selectedFarmId)
      ) ?? null,
    [farms, selectedFarmId]
  );

  const plots = useMemo(
    () => selectedFarm?.plots ?? [],
    [selectedFarm]
  );

  const selectedPlot = useMemo(
    () =>
      plots.find(
        (plot) => String(getPlotId(plot)) === String(selectedPlotId)
      ) ?? null,
    [plots, selectedPlotId]
  );

  const totalFarmArea = selectedFarm?.farmArea ?? selectedFarm?.area ?? 0;

  const totalPlots = plots.length;

  // Load farms from backend.
  useEffect(() => {
    let active = true;

    async function loadFarms() {
      try {
        setLoading(true);
        setError('');

        const response = await listGisFarms();
        const farmList = getFarmList(response);

        if (!active) return;

        setFarms(farmList);

        if (farmList.length > 0) {
          const firstFarm = farmList[0];
          const firstFarmId = getFarmId(firstFarm);

          setSelectedFarmId(String(firstFarmId));

          const firstPlot = firstFarm?.plots?.[0];

          if (firstPlot) {
            setSelectedPlotId(String(getPlotId(firstPlot)));
          } else {
            setSelectedPlotId('');
          }
        } else {
          setSelectedFarmId('');
          setSelectedPlotId('');
        }
      } catch (err) {
        if (active) {
          setError(err.message || 'Unable to load farms.');
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    loadFarms();

    return () => {
      active = false;
    };
  }, []);

  // Load selected plot boundary from backend.
  useEffect(() => {
    let active = true;

    async function loadBoundary() {
      if (!selectedFarmId || !selectedPlotId) {
        setPoints([]);
        setHasSavedBoundary(false);
        return;
      }

      try {
        setError('');
        setSuccess('');

        const response = await getPlotBoundary(
          selectedFarmId,
          selectedPlotId
        );

        if (!active) return;

        const boundaryValue =
          response?.boundaryGeoJson ??
          response?.boundary ??
          response?.coordinates ??
          response;

        const savedPoints = parseBoundary(boundaryValue);

        setPoints(savedPoints);
        setHasSavedBoundary(savedPoints.length >= 3);
      } catch (err) {
        if (!active) return;

        // A plot without a saved boundary can return an empty response
        // or a not-found response. Keep it ready for new points.
        setPoints([]);
        setHasSavedBoundary(false);
      }
    }

    loadBoundary();

    return () => {
      active = false;
    };
  }, [selectedFarmId, selectedPlotId]);

  function handleFarmChange(event) {
    const farmId = event.target.value;

    setSelectedFarmId(farmId);
    setPoints([]);
    setHasSavedBoundary(false);
    setSuccess('');
    setError('');

    const farm = farms.find(
      (item) => String(getFarmId(item)) === String(farmId)
    );

    const firstPlot = farm?.plots?.[0];

    setSelectedPlotId(
      firstPlot ? String(getPlotId(firstPlot)) : ''
    );
  }

  function handlePlotChange(event) {
    setSelectedPlotId(event.target.value);
    setPoints([]);
    setHasSavedBoundary(false);
    setSuccess('');
    setError('');
  }

  function addPoint(point) {
    setPoints((previous) => [...previous, point]);
    setSuccess('');
    setError('');
  }

  function handleMapClick(point) {
    addPoint(point);
  }

  function addManualCoordinate() {
    setError('');
    setSuccess('');

    const lat = Number(latitude);
    const lng = Number(longitude);

    if (latitude.trim() === '' || longitude.trim() === '') {
      setError('Please enter both Latitude and Longitude.');
      return;
    }

    if (!Number.isFinite(lat) || lat < -90 || lat > 90) {
      setError('Latitude must be between -90 and 90.');
      return;
    }

    if (!Number.isFinite(lng) || lng < -180 || lng > 180) {
      setError('Longitude must be between -180 and 180.');
      return;
    }

    addPoint({
      lat: Number(lat.toFixed(7)),
      lng: Number(lng.toFixed(7)),
    });

    setLatitude('');
    setLongitude('');
  }

  function removePoint(index) {
    setPoints((previous) =>
      previous.filter((_, pointIndex) => pointIndex !== index)
    );
    setSuccess('');
  }

  function clearPoints() {
    setPoints([]);
    setSuccess('');
    setError('');
  }

  async function saveBoundary() {
    setError('');
    setSuccess('');

    if (!selectedFarmId || !selectedPlotId) {
      setError('Please select a farm and a plot first.');
      return;
    }

    if (points.length < 3) {
      setError('Please select at least 3 boundary points on the map.');
      return;
    }

    // Backend expects GeoJSON coordinate order: [longitude, latitude].
    const coordinates = points.map((point) => [
      point.lng,
      point.lat,
    ]);

    try {
      setSaving(true);

      if (hasSavedBoundary) {
        await updatePlotBoundary(
          selectedFarmId,
          selectedPlotId,
          coordinates
        );

        setSuccess('Plot boundary updated successfully!');
      } else {
        await createPlotBoundary(
          selectedFarmId,
          selectedPlotId,
          coordinates
        );

        setHasSavedBoundary(true);
        setSuccess('Plot boundary saved successfully!');
      }
    } catch (err) {
      setError(
        err.message || 'Unable to save plot boundary. Please try again.'
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h2 style={styles.heading}>GIS Farm Mapping</h2>

        <p style={styles.muted}>
          Select your farm and plot, then mark the plot boundary directly
          on the map. GPS location is not required.
        </p>

        {loading ? (
          <p>Loading farm information...</p>
        ) : (
          <>
            {farms.length === 0 ? (
              <div
                style={{
                  padding: '15px',
                  borderRadius: '8px',
                  background: '#fff8e8',
                  color: '#8a641c',
                }}
              >
                No farms found for this account. Please add a farm in
                Farm Management first.
              </div>
            ) : (
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                  gap: '18px',
                  marginTop: '20px',
                }}
              >
                <div>
                  <label style={styles.label}>Select Farm</label>

                  <select
                    style={styles.select}
                    value={selectedFarmId}
                    onChange={handleFarmChange}
                  >
                    {farms.map((farm) => (
                      <option
                        key={getFarmId(farm)}
                        value={String(getFarmId(farm))}
                      >
                        {getFarmName(farm)}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label style={styles.label}>Select Plot</label>

                  <select
                    style={styles.select}
                    value={selectedPlotId}
                    onChange={handlePlotChange}
                    disabled={plots.length === 0}
                  >
                    {plots.length === 0 ? (
                      <option value="">No plots available</option>
                    ) : (
                      plots.map((plot) => (
                        <option
                          key={getPlotId(plot)}
                          value={String(getPlotId(plot))}
                        >
                          {getPlotName(plot)}
                        </option>
                      ))
                    )}
                  </select>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {selectedFarm && (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
            gap: '15px',
            marginBottom: '20px',
          }}
        >
          <div style={styles.card}>
            <div style={styles.muted}>Farm Name</div>
            <h3>{getFarmName(selectedFarm)}</h3>
          </div>

          <div style={styles.card}>
            <div style={styles.muted}>Total Farm Area</div>
            <h3>
              {totalFarmArea} {selectedFarm.farmAreaUnit ?? selectedFarm.areaUnit ?? ''}
            </h3>
          </div>

          <div style={styles.card}>
            <div style={styles.muted}>Total Plots</div>
            <h3>{totalPlots}</h3>
          </div>

          <div style={styles.card}>
            <div style={styles.muted}>Boundary Points</div>
            <h3>{points.length}</h3>
          </div>
        </div>
      )}

      <div style={styles.card}>
        <h2 style={styles.heading}>Plot Boundary Map</h2>

        <p style={styles.muted}>
          Click on the map to add boundary points. Select at least
          three points to create a plot boundary.
        </p>

        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            gap: '10px',
            marginBottom: '14px',
          }}
        >
          <button
            type="button"
            style={{
              ...styles.primaryButton,
              background: mapClickEnabled ? '#269b58' : '#718477',
            }}
            onClick={() => setMapClickEnabled((value) => !value)}
          >
            {mapClickEnabled ? 'Map Click: ON' : 'Map Click: OFF'}
          </button>

          <button
            type="button"
            style={styles.secondaryButton}
            onClick={clearPoints}
            disabled={points.length === 0}
          >
            Clear Points
          </button>

          <button
            type="button"
            style={styles.secondaryButton}
            onClick={() => removePoint(points.length - 1)}
            disabled={points.length === 0}
          >
            Remove Last Point
          </button>
        </div>

        <div
          style={{
            height: '480px',
            width: '100%',
            borderRadius: '12px',
            overflow: 'hidden',
            border: '1px solid #d7e2da',
          }}
        >
          <MapContainer
            center={DEFAULT_CENTER}
            zoom={5}
            scrollWheelZoom
            zoomControl={false}
            style={{
              height: '100%',
              width: '100%',
            }}
          >
            <ZoomControl position="topright" />

            <LayersControl position="topright">
              <LayersControl.BaseLayer
                checked
                name="Satellite Imagery"
              >
                <TileLayer
                  attribution="Tiles &copy; Esri"
                  url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
                />
              </LayersControl.BaseLayer>

              <LayersControl.BaseLayer name="Street Map">
                <TileLayer
                  attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors'
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
              </LayersControl.BaseLayer>
            </LayersControl>

            <MapClickHandler
              enabled={mapClickEnabled}
              onMapClick={handleMapClick}
            />

            <MapFitBounds points={points} />

            {points.length >= 3 && (
              <Polygon
                positions={points.map((point) => [
                  point.lat,
                  point.lng,
                ])}
                pathOptions={{
                  color: '#16834a',
                  fillColor: '#27ae60',
                  fillOpacity: 0.28,
                  weight: 3,
                }}
              />
            )}

            {points.map((point, index) => (
              <CircleMarker
                key={`${point.lat}-${point.lng}-${index}`}
                center={[point.lat, point.lng]}
                radius={7}
                pathOptions={{
                  color: '#ffffff',
                  weight: 2,
                  fillColor: '#ed3838',
                  fillOpacity: 1,
                }}
              />
            ))}
          </MapContainer>
        </div>

        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: '20px',
            marginTop: '15px',
            fontSize: '13px',
            color: '#617568',
          }}
        >
          <span>
            <span style={{ color: '#ed3838' }}>●</span> Boundary Points
          </span>

          <span>
            <span style={{ color: '#16834a' }}>■</span> Plot Polygon
          </span>
        </div>

        <p style={styles.muted}>
          Map imagery requires an active internet connection. Zoom into
          your farm area manually, then click the plot corners.
        </p>
      </div>

      <div style={styles.card}>
        <h2 style={styles.heading}>Add Coordinates Manually</h2>

        <p style={styles.muted}>
          You can also enter Latitude and Longitude values instead of
          clicking the map.
        </p>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            gap: '15px',
            alignItems: 'end',
          }}
        >
          <div>
            <label style={styles.label}>Latitude</label>
            <input
              type="number"
              step="any"
              style={styles.input}
              placeholder="Example: 18.5204"
              value={latitude}
              onChange={(event) => setLatitude(event.target.value)}
            />
          </div>

          <div>
            <label style={styles.label}>Longitude</label>
            <input
              type="number"
              step="any"
              style={styles.input}
              placeholder="Example: 73.8567"
              value={longitude}
              onChange={(event) => setLongitude(event.target.value)}
            />
          </div>

          <button
            type="button"
            style={styles.primaryButton}
            onClick={addManualCoordinate}
          >
            Add Coordinate
          </button>
        </div>
      </div>

      <div style={styles.card}>
        <h2 style={styles.heading}>Boundary Coordinates</h2>

        {points.length === 0 ? (
          <p style={styles.muted}>
            No boundary points added. Click on the map or enter coordinates
            manually.
          </p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table
              style={{
                width: '100%',
                borderCollapse: 'collapse',
                textAlign: 'left',
              }}
            >
              <thead>
                <tr style={{ background: '#edf7f0' }}>
                  <th style={{ padding: '12px' }}>Point</th>
                  <th style={{ padding: '12px' }}>Latitude</th>
                  <th style={{ padding: '12px' }}>Longitude</th>
                  <th style={{ padding: '12px' }}>Action</th>
                </tr>
              </thead>

              <tbody>
                {points.map((point, index) => (
                  <tr key={`${point.lat}-${point.lng}-${index}`}>
                    <td style={{ padding: '12px', borderBottom: '1px solid #e6eee8' }}>
                      Point {index + 1}
                    </td>

                    <td style={{ padding: '12px', borderBottom: '1px solid #e6eee8' }}>
                      {point.lat}
                    </td>

                    <td style={{ padding: '12px', borderBottom: '1px solid #e6eee8' }}>
                      {point.lng}
                    </td>

                    <td style={{ padding: '12px', borderBottom: '1px solid #e6eee8' }}>
                      <button
                        type="button"
                        style={styles.dangerButton}
                        onClick={() => removePoint(index)}
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '12px',
            marginTop: '22px',
          }}
        >
          <button
            type="button"
            style={{
              ...styles.primaryButton,
              opacity:
                saving || points.length < 3 || !selectedFarmId || !selectedPlotId
                  ? 0.55
                  : 1,
              cursor:
                saving || points.length < 3 || !selectedFarmId || !selectedPlotId
                  ? 'not-allowed'
                  : 'pointer',
            }}
            onClick={saveBoundary}
            disabled={
              saving ||
              points.length < 3 ||
              !selectedFarmId ||
              !selectedPlotId
            }
          >
            {saving
              ? 'Saving...'
              : hasSavedBoundary
                ? 'Update Boundary'
                : 'Save Boundary'}
          </button>

          <span style={styles.muted}>
            {points.length < 3
              ? `Add ${3 - points.length} more point(s) to save.`
              : 'Boundary is ready to save.'}
          </span>
        </div>

        {error && (
          <div
            style={{
              marginTop: '16px',
              padding: '12px',
              borderRadius: '8px',
              background: '#fff0f0',
              color: '#b42318',
            }}
          >
            {error}
          </div>
        )}

        {success && (
          <div
            style={{
              marginTop: '16px',
              padding: '12px',
              borderRadius: '8px',
              background: '#eaf8ef',
              color: '#18733c',
            }}
          >
            {success}
          </div>
        )}
      </div>
    </div>
  );
}
