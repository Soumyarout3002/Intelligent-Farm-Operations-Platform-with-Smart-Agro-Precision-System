
import { useEffect, useState } from 'react';
import { listGisFarms } from '../services/gisService';
import {
  listCropCycles,
  createCropCycle,
  updateCropCycle,
  deleteCropCycle,
  listGrowthStages,
  createGrowthStage,
  listCropActivities,
  createCropActivity,
} from '../services/cropService';

const cardStyle = {
  background: '#fff',
  border: '1px solid #e5eaf1',
  borderRadius: '14px',
  padding: '20px',
  marginBottom: '18px',
  boxShadow: '0 3px 12px rgba(30,50,80,0.04)',
};

const inputStyle = {
  width: '100%',
  padding: '11px',
  border: '1px solid #d7deea',
  borderRadius: '8px',
  boxSizing: 'border-box',
  fontSize: '14px',
};

const buttonStyle = {
  padding: '10px 15px',
  border: 0,
  borderRadius: '8px',
  fontWeight: 600,
  cursor: 'pointer',
};

const stages = [
  'GERMINATION',
  'SEEDLING',
  'TILLERING',
  'FLOWERING',
  'FRUITING',
  'MATURITY',
  'HARVEST',
];

const activities = [
  'IRRIGATION',
  'FERTILIZATION',
  'SEEDING',
  'WEEDING',
  'PRUNING',
  'PEST_CONTROL',
  'SOIL_PREPARATION',
  'SPRAYING',
  'OTHER',
];

const today = new Date().toISOString().slice(0, 10);

function displayDate(value) {
  if (!value) return 'Not set';

  return new Date(`${value}T00:00:00`).toLocaleDateString();
}

function displayName(value) {
  return (value || '')
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

export default function CropLifecycleTracking() {
  const [farms, setFarms] = useState([]);
  const [farmId, setFarmId] = useState('');
  const [plotId, setPlotId] = useState('');
  const [cycles, setCycles] = useState([]);
  const [cycleId, setCycleId] = useState('');
  const [growthStages, setGrowthStages] = useState([]);
  const [cropActivities, setCropActivities] = useState([]);

  const [cropName, setCropName] = useState('');
  const [variety, setVariety] = useState('');
  const [season, setSeason] = useState('');
  const [sowingDate, setSowingDate] = useState(today);
  const [expectedHarvestDate, setExpectedHarvestDate] = useState('');
  const [notes, setNotes] = useState('');

  const [stageName, setStageName] = useState('GERMINATION');
  const [stageStartDate, setStageStartDate] = useState(today);
  const [stageEndDate, setStageEndDate] = useState('');
  const [observations, setObservations] = useState('');

  const [activityType, setActivityType] = useState('IRRIGATION');
  const [activityTitle, setActivityTitle] = useState('');
  const [activityDate, setActivityDate] = useState(today);
  const [activityDescription, setActivityDescription] = useState('');
  const [quantity, setQuantity] = useState('');
  const [performedBy, setPerformedBy] = useState('');

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const selectedFarm = farms.find(
    (farm) => String(farm.farmId) === farmId
  );

  const plots = selectedFarm?.plots || [];

  const selectedCycle = cycles.find(
    (cycle) => String(cycle.id ?? cycle.cycleId) === cycleId
  );

  async function loadFarms() {
    setLoading(true);
    setError('');

    try {
      const data = await listGisFarms();
      setFarms(data);

      if (data.length > 0) {
        setFarmId(String(data[0].farmId));
      }
    } catch (err) {
      setError(err.message || 'Unable to load farms.');
    } finally {
      setLoading(false);
    }
  }

  async function loadCycles() {
    if (!farmId || !plotId) {
      setCycles([]);
      setCycleId('');
      return;
    }

    try {
      const data = await listCropCycles(farmId, plotId);
      setCycles(data);

      if (data.length > 0) {
        setCycleId(String(data[0].id ?? data[0].cycleId));
      } else {
        setCycleId('');
      }
    } catch (err) {
      setError(err.message || 'Unable to load crop cycles.');
    }
  }

  async function loadCycleDetails() {
    if (!farmId || !plotId || !cycleId) {
      setGrowthStages([]);
      setCropActivities([]);
      return;
    }

    try {
      const [stageData, activityData] = await Promise.all([
        listGrowthStages(farmId, plotId, cycleId),
        listCropActivities(farmId, plotId, cycleId),
      ]);

      setGrowthStages(stageData);
      setCropActivities(activityData);
    } catch (err) {
      setError(err.message || 'Unable to load crop details.');
    }
  }

  useEffect(() => {
    loadFarms();
  }, []);

  useEffect(() => {
    if (!farmId) {
      setPlotId('');
      return;
    }

    const farm = farms.find(
      (item) => String(item.farmId) === farmId
    );

    const farmPlots = farm?.plots || [];

    setPlotId(
      farmPlots.length > 0 ? String(farmPlots[0].plotId) : ''
    );
  }, [farmId, farms]);

  useEffect(() => {
    loadCycles();
  }, [farmId, plotId]);

  useEffect(() => {
    loadCycleDetails();
  }, [farmId, plotId, cycleId]);

  async function handleCreateCycle(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (!farmId || !plotId) {
      setError('Please select a farm and plot first.');
      return;
    }

    if (!cropName.trim() || !sowingDate) {
      setError('Crop name and sowing date are required.');
      return;
    }

    if (
      expectedHarvestDate &&
      expectedHarvestDate < sowingDate
    ) {
      setError('Expected harvest date cannot be before sowing date.');
      return;
    }

    setSaving(true);

    try {
      const result = await createCropCycle(farmId, plotId, {
        cropName: cropName.trim(),
        variety: variety.trim(),
        season: season.trim(),
        sowingDate,
        expectedHarvestDate: expectedHarvestDate || null,
        status: 'ACTIVE',
        notes: notes.trim(),
      });

      setMessage('Crop cycle created successfully.');
      setCropName('');
      setVariety('');
      setSeason('');
      setExpectedHarvestDate('');
      setNotes('');

      await loadCycles();

      const newId = result.id ?? result.cycleId;

      if (newId) {
        setCycleId(String(newId));
      }
    } catch (err) {
      setError(err.message || 'Unable to create crop cycle.');
    } finally {
      setSaving(false);
    }
  }

  async function handleCompleteCycle() {
    if (!selectedCycle) return;

    const id = selectedCycle.id ?? selectedCycle.cycleId;

    setSaving(true);
    setError('');
    setMessage('');

    try {
      await updateCropCycle(farmId, plotId, id, {
        cropName: selectedCycle.cropName,
        variety: selectedCycle.variety || '',
        season: selectedCycle.season || '',
        sowingDate: selectedCycle.sowingDate,
        expectedHarvestDate: selectedCycle.expectedHarvestDate || null,
        status: 'COMPLETED',
        notes: selectedCycle.notes || '',
      });

      setMessage('Crop cycle marked as completed.');
      await loadCycles();
    } catch (err) {
      setError(err.message || 'Unable to update crop cycle.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteCycle() {
    if (!selectedCycle) return;

    const confirmed = window.confirm(
      'Are you sure you want to delete this crop cycle?'
    );

    if (!confirmed) return;

    const id = selectedCycle.id ?? selectedCycle.cycleId;

    setSaving(true);
    setError('');
    setMessage('');

    try {
      await deleteCropCycle(farmId, plotId, id);
      setMessage('Crop cycle deleted.');
      setCycleId('');
      await loadCycles();
    } catch (err) {
      setError(err.message || 'Unable to delete crop cycle.');
    } finally {
      setSaving(false);
    }
  }

  async function handleAddStage(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (!cycleId) {
      setError('Select a crop cycle first.');
      return;
    }

    if (stageEndDate && stageEndDate < stageStartDate) {
      setError('Stage end date cannot be before its start date.');
      return;
    }

    setSaving(true);

    try {
      await createGrowthStage(farmId, plotId, cycleId, {
        stageName,
        startDate: stageStartDate,
        endDate: stageEndDate || null,
        observations: observations.trim(),
      });

      setMessage('Growth stage added successfully.');
      setObservations('');
      setStageEndDate('');
      await loadCycleDetails();
    } catch (err) {
      setError(err.message || 'Unable to add growth stage.');
    } finally {
      setSaving(false);
    }
  }

  async function handleAddActivity(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (!cycleId || !activityTitle.trim()) {
      setError('Select a crop cycle and enter an activity title.');
      return;
    }

    setSaving(true);

    try {
      await createCropActivity(farmId, plotId, cycleId, {
        activityType,
        title: activityTitle.trim(),
        activityDate,
        description: activityDescription.trim(),
        quantity: quantity === '' ? null : Number(quantity),
        performedBy: performedBy.trim(),
      });

      setMessage('Crop activity recorded successfully.');
      setActivityTitle('');
      setActivityDescription('');
      setQuantity('');
      setPerformedBy('');
      await loadCycleDetails();
    } catch (err) {
      setError(err.message || 'Unable to record crop activity.');
    } finally {
      setSaving(false);
    }
  }

  const field = (label, content) => (
    <div>
      <label style={{
        display: 'block',
        fontWeight: 600,
        fontSize: '13px',
        marginBottom: '7px',
      }}>
        {label}
      </label>
      {content}
    </div>
  );

  const input = (value, setter, type = 'text', placeholder = '') => (
    <input
      type={type}
      value={value}
      onChange={(event) => setter(event.target.value)}
      placeholder={placeholder}
      style={inputStyle}
    />
  );

  return (
    <div style={{ padding: '24px', color: '#243047' }}>
      <h1 style={{ margin: '0 0 6px', fontSize: '27px' }}>
        Crop Lifecycle Tracking
      </h1>

      <p style={{ color: '#718096', marginBottom: '22px' }}>
        Track crop cycles, growth stages, and farm activities.
      </p>

      {error && (
        <div style={{
          background: '#fff0f0',
          color: '#b42318',
          padding: '12px',
          borderRadius: '9px',
          marginBottom: '15px',
        }}>
          {error}
        </div>
      )}

      {message && (
        <div style={{
          background: '#eaf8ef',
          color: '#187443',
          padding: '12px',
          borderRadius: '9px',
          marginBottom: '15px',
        }}>
          {message}
        </div>
      )}

      {loading ? (
        <div style={cardStyle}>Loading farms...</div>
      ) : farms.length === 0 ? (
        <div style={cardStyle}>
          <h3>No farms available</h3>
          <p>Add a farm and at least one plot in Farm Management first.</p>
        </div>
      ) : (
        <>
          <div style={cardStyle}>
            <h3 style={{ marginTop: 0 }}>Select Farm and Plot</h3>

            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: '15px',
            }}>
              {field(
                'Farm',
                <select
                  value={farmId}
                  onChange={(event) => setFarmId(event.target.value)}
                  style={inputStyle}
                >
                  {farms.map((farm) => (
                    <option key={farm.farmId} value={farm.farmId}>
                      {farm.farmName}
                    </option>
                  ))}
                </select>
              )}

              {field(
                'Plot',
                <select
                  value={plotId}
                  onChange={(event) => setPlotId(event.target.value)}
                  style={inputStyle}
                >
                  {plots.map((plot) => (
                    <option key={plot.plotId} value={plot.plotId}>
                      {plot.plotName || plot.plotCode || `Plot ${plot.plotId}`}
                    </option>
                  ))}
                </select>
              )}
            </div>
          </div>

          {!plotId ? (
            <div style={cardStyle}>
              <h3>No plots found</h3>
              <p>Add a plot to the selected farm before creating a crop cycle.</p>
            </div>
          ) : (
            <>
              <div style={cardStyle}>
                <h3 style={{ marginTop: 0 }}>Create New Crop Cycle</h3>

                <form onSubmit={handleCreateCycle}>
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(190px, 1fr))',
                    gap: '15px',
                  }}>
                    {field('Crop Name *', input(cropName, setCropName, 'text', 'e.g. Wheat'))}
                    {field('Variety', input(variety, setVariety, 'text', 'Crop variety'))}
                    {field('Season', input(season, setSeason, 'text', 'e.g. Rabi'))}
                    {field('Sowing Date *', input(sowingDate, setSowingDate, 'date'))}
                    {field('Expected Harvest Date', input(expectedHarvestDate, setExpectedHarvestDate, 'date'))}
                  </div>

                  <div style={{ marginTop: '15px' }}>
                    {field(
                      'Notes',
                      <textarea
                        value={notes}
                        onChange={(event) => setNotes(event.target.value)}
                        rows="3"
                        style={inputStyle}
                        placeholder="Additional crop information"
                      />
                    )}
                  </div>

                  <button
                    type="submit"
                    disabled={saving}
                    style={{
                      ...buttonStyle,
                      background: '#218653',
                      color: '#fff',
                      marginTop: '15px',
                    }}
                  >
                    {saving ? 'Saving...' : '+ Create Crop Cycle'}
                  </button>
                </form>
              </div>

              <div style={cardStyle}>
                <h3 style={{ marginTop: 0 }}>Crop Cycles</h3>

                {cycles.length === 0 ? (
                  <p style={{ color: '#718096' }}>
                    No crop cycles available for this plot.
                  </p>
                ) : (
                  <>
                    <div style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                      gap: '12px',
                    }}>
                      {cycles.map((cycle) => {
                        const id = String(cycle.id ?? cycle.cycleId);
                        const active = id === cycleId;

                        return (
                          <button
                            key={id}
                            type="button"
                            onClick={() => setCycleId(id)}
                            style={{
                              textAlign: 'left',
                              padding: '15px',
                              borderRadius: '10px',
                              border: active
                                ? '2px solid #218653'
                                : '1px solid #e5eaf1',
                              background: active ? '#edf8f0' : '#fff',
                              cursor: 'pointer',
                            }}
                          >
                            <strong>{cycle.cropName}</strong>
                            <p style={{
                              margin: '7px 0',
                              fontSize: '13px',
                              color: '#718096',
                            }}>
                              {displayDate(cycle.sowingDate)}
                            </p>
                            <span style={{
                              fontSize: '12px',
                              fontWeight: 600,
                            }}>
                              {displayName(cycle.status)}
                            </span>
                          </button>
                        );
                      })}
                    </div>

                    {selectedCycle && (
                      <div style={{
                        display: 'flex',
                        flexWrap: 'wrap',
                        gap: '10px',
                        marginTop: '16px',
                      }}>
                        <button
                          type="button"
                          disabled={saving || selectedCycle.status === 'COMPLETED'}
                          onClick={handleCompleteCycle}
                          style={{
                            ...buttonStyle,
                            background: '#e8f7ee',
                            color: '#187443',
                          }}
                        >
                          Mark as Completed
                        </button>

                        <button
                          type="button"
                          disabled={saving}
                          onClick={handleDeleteCycle}
                          style={{
                            ...buttonStyle,
                            background: '#fff0f0',
                            color: '#b42318',
                          }}
                        >
                          Delete Cycle
                        </button>
                      </div>
                    )}
                  </>
                )}
              </div>

              {cycleId && (
                <>
                  <div style={cardStyle}>
                    <h3 style={{ marginTop: 0 }}>Growth Stage Tracking</h3>

                    <form onSubmit={handleAddStage}>
                      <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                        gap: '15px',
                      }}>
                        {field(
                          'Growth Stage',
                          <select
                            value={stageName}
                            onChange={(event) => setStageName(event.target.value)}
                            style={inputStyle}
                          >
                            {stages.map((stage) => (
                              <option key={stage} value={stage}>
                                {displayName(stage)}
                              </option>
                            ))}
                          </select>
                        )}

                        {field('Start Date', input(stageStartDate, setStageStartDate, 'date'))}
                        {field('End Date', input(stageEndDate, setStageEndDate, 'date'))}
                      </div>

                      <div style={{ marginTop: '15px' }}>
                        {field(
                          'Observations',
                          <textarea
                            value={observations}
                            onChange={(event) => setObservations(event.target.value)}
                            rows="2"
                            style={inputStyle}
                            placeholder="Enter growth observations"
                          />
                        )}
                      </div>

                      <button
                        type="submit"
                        disabled={saving}
                        style={{
                          ...buttonStyle,
                          background: '#2457a7',
                          color: '#fff',
                          marginTop: '15px',
                        }}
                      >
                        + Add Growth Stage
                      </button>
                    </form>

                    <div style={{ marginTop: '20px' }}>
                      {growthStages.length === 0 ? (
                        <p style={{ color: '#718096' }}>
                          No growth stages recorded yet.
                        </p>
                      ) : (
                        growthStages.map((stage, index) => (
                          <div
                            key={stage.id ?? stage.stageId ?? index}
                            style={{
                              borderLeft: '3px solid #218653',
                              padding: '10px 14px',
                              marginBottom: '12px',
                              background: '#f6faf7',
                              borderRadius: '0 8px 8px 0',
                            }}
                          >
                            <strong>{displayName(stage.stageName)}</strong>
                            <p style={{
                              margin: '5px 0',
                              color: '#718096',
                              fontSize: '13px',
                            }}>
                              {displayDate(stage.startDate)}
                              {' — '}
                              {displayDate(stage.endDate)}
                            </p>
                            {stage.observations && (
                              <p style={{ margin: 0, fontSize: '13px' }}>
                                {stage.observations}
                              </p>
                            )}
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  <div style={cardStyle}>
                    <h3 style={{ marginTop: 0 }}>Crop Activity Log</h3>

                    <form onSubmit={handleAddActivity}>
                      <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                        gap: '15px',
                      }}>
                        {field(
                          'Activity Type',
                          <select
                            value={activityType}
                            onChange={(event) => setActivityType(event.target.value)}
                            style={inputStyle}
                          >
                            {activities.map((activity) => (
                              <option key={activity} value={activity}>
                                {displayName(activity)}
                              </option>
                            ))}
                          </select>
                        )}

                        {field('Activity Title *', input(activityTitle, setActivityTitle, 'text', 'e.g. Morning irrigation'))}
                        {field('Activity Date', input(activityDate, setActivityDate, 'date'))}
                        {field('Quantity', input(quantity, setQuantity, 'number', 'Optional'))}
                        {field('Performed By', input(performedBy, setPerformedBy, 'text', 'Worker name'))}
                      </div>

                      <div style={{ marginTop: '15px' }}>
                        {field(
                          'Description',
                          <textarea
                            value={activityDescription}
                            onChange={(event) => setActivityDescription(event.target.value)}
                            rows="2"
                            style={inputStyle}
                            placeholder="Describe the farm activity"
                          />
                        )}
                      </div>

                      <p style={{
                        color: '#956000',
                        background: '#fff8e8',
                        padding: '10px',
                        borderRadius: '8px',
                        fontSize: '12px',
                      }}>
                        Note: Fertilization, seeding, pest control and spraying
                        may require inventory item and warehouse details when
                        stock is deducted. This form currently records basic
                        activity details only.
                      </p>

                      <button
                        type="submit"
                        disabled={saving}
                        style={{
                          ...buttonStyle,
                          background: '#2457a7',
                          color: '#fff',
                        }}
                      >
                        + Record Activity
                      </button>
                    </form>

                    <div style={{ marginTop: '20px' }}>
                      {cropActivities.length === 0 ? (
                        <p style={{ color: '#718096' }}>
                          No crop activities recorded yet.
                        </p>
                      ) : (
                        <div style={{ overflowX: 'auto' }}>
                          <table style={{
                            width: '100%',
                            borderCollapse: 'collapse',
                            fontSize: '13px',
                          }}>
                            <thead>
                              <tr style={{
                                background: '#f5f7fa',
                                textAlign: 'left',
                              }}>
                                <th style={{ padding: '10px' }}>Activity</th>
                                <th style={{ padding: '10px' }}>Type</th>
                                <th style={{ padding: '10px' }}>Date</th>
                                <th style={{ padding: '10px' }}>Performed By</th>
                              </tr>
                            </thead>
                            <tbody>
                              {cropActivities.map((activity, index) => (
                                <tr
                                  key={activity.id ?? activity.activityId ?? index}
                                  style={{ borderTop: '1px solid #e5eaf1' }}
                                >
                                  <td style={{ padding: '10px' }}>
                                    {activity.title}
                                  </td>
                                  <td style={{ padding: '10px' }}>
                                    {displayName(activity.activityType)}
                                  </td>
                                  <td style={{ padding: '10px' }}>
                                    {displayDate(activity.activityDate)}
                                  </td>
                                  <td style={{ padding: '10px' }}>
                                    {activity.performedBy || '—'}
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      )}
                    </div>
                  </div>
                </>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
}
