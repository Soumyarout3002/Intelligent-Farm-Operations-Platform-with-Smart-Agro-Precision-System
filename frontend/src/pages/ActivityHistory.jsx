
import { useEffect, useState } from 'react';
import {
  getActivityLogs,
  getActivityLogById,
} from '../services/activityLogService';

const ACTION_TYPES = [
  'USER_CREATED',
  'USER_UPDATED',
  'USER_ACTIVATED',
  'USER_DEACTIVATED',
  'USER_DELETED',
];

const parseJson = (value) => {
  if (!value) return null;
  if (typeof value !== 'string') return value;

  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
};

const formatDate = (value) =>
  value ? new Date(value).toLocaleString() : 'N/A';

const buttonStyle = {
  padding: '9px 15px',
  borderRadius: 7,
  border: '1px solid #444',
  cursor: 'pointer',
  background: '#202820',
  color: '#e5eee7',
};

const inputStyle = {
  padding: 11,
  borderRadius: 7,
  border: '1px solid #444',
  background: '#151515',
  color: '#eee',
  minWidth: 0,
  width: '100%',
};

export default function ActivityHistory() {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [userId, setUserId] = useState('');
  const [actionType, setActionType] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [selectedActivity, setSelectedActivity] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');

  useEffect(() => {
    let cancelled = false;

    const loadActivities = async () => {
      setLoading(true);
      setError('');

      try {
        const data = await getActivityLogs({
          ...filters,
          page,
          size,
        });

        if (cancelled) return;

        setActivities(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      } catch (err) {
        if (!cancelled) {
          setError(err.message || 'Unable to load activity history.');
          setActivities([]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    loadActivities();

    return () => {
      cancelled = true;
    };
  }, [filters, page, size]);

  const applyFilters = (e) => {
    e.preventDefault();

    if (startDate && endDate && startDate > endDate) {
      setError('Start date cannot be after end date.');
      return;
    }

    setFilters({
      userId,
      actionType,
      startDate,
      endDate,
    });

    setPage(0);
  };

  const clearFilters = () => {
    setUserId('');
    setActionType('');
    setStartDate('');
    setEndDate('');
    setFilters({});
    setPage(0);
    setError('');
  };

  const openDetails = async (id) => {
    setDetailLoading(true);
    setDetailError('');
    setSelectedActivity(null);

    try {
      const data = await getActivityLogById(id);
      setSelectedActivity(data);
    } catch (err) {
      setDetailError(err.message || 'Unable to load activity details.');
    } finally {
      setDetailLoading(false);
    }
  };

  const activityDetails = selectedActivity;

  return (
    <div style={{ display: 'grid', gap: 20 }}>
      <div>
        <h1>Activity History</h1>
        <p className="muted">
          View and monitor administrative activities across FarmVerse Nexus.
        </p>
      </div>

      {/* Filters */}
      <div className="card">
        <h3>Filter Activities</h3>

        <form onSubmit={applyFilters}>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
              gap: 14,
              alignItems: 'end',
            }}
          >
            <label>
              <span>User ID</span>
              <input
                style={inputStyle}
                type="number"
                min="1"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="Enter user ID"
              />
            </label>

            <label>
              <span>Action Type</span>
              <select
                style={inputStyle}
                value={actionType}
                onChange={(e) => setActionType(e.target.value)}
              >
                <option value="">All Actions</option>
                {ACTION_TYPES.map((action) => (
                  <option key={action} value={action}>
                    {action.replaceAll('_', ' ')}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span>Start Date</span>
              <input
                style={inputStyle}
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </label>

            <label>
              <span>End Date</span>
              <input
                style={inputStyle}
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </label>
          </div>

          <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
            <button type="submit" className="btn primary">
              Apply Filters
            </button>
            <button
              type="button"
              className="btn ghost"
              onClick={clearFilters}
            >
              Clear Filters
            </button>
          </div>
        </form>
      </div>

      {/* Activity Table */}
      <div className="card">
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 10,
          }}
        >
          <h3>Activity Records</h3>
          <span className="muted">
            Total Records: {totalElements}
          </span>
        </div>

        {loading ? (
          <p className="muted">Loading activity records...</p>
        ) : error ? (
          <p className="alert error">{error}</p>
        ) : activities.length === 0 ? (
          <p className="muted">No activity records found.</p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  {['ID', 'Action', 'Actor', 'Description', 'Date & Time', 'Details'].map(
                    (heading) => (
                      <th
                        key={heading}
                        style={{
                          padding: 12,
                          textAlign: 'left',
                          borderBottom: '1px solid #444',
                        }}
                      >
                        {heading}
                      </th>
                    )
                  )}
                </tr>
              </thead>

              <tbody>
                {activities.map((activity) => (
                  <tr key={activity.id}>
                    <td style={{ padding: 12 }}>{activity.id}</td>

                    <td style={{ padding: 12 }}>
                      <strong>
                        {(activity.actionType || '').replaceAll('_', ' ')}
                      </strong>
                    </td>

                    <td style={{ padding: 12 }}>
                      {activity.actorName || 'Unknown'}
                      <br />
                      <small className="muted">
                        {activity.actorRole || 'N/A'}
                      </small>
                    </td>

                    <td style={{ padding: 12 }}>
                      {activity.description || 'N/A'}
                    </td>

                    <td style={{ padding: 12 }}>
                      {formatDate(activity.createdAt)}
                    </td>

                    <td style={{ padding: 12 }}>
                      <button
                        style={buttonStyle}
                        onClick={() => openDetails(activity.id)}
                      >
                        View Details
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 12,
            marginTop: 20,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span className="muted">Rows:</span>
            <select
              style={{ ...inputStyle, width: 75 }}
              value={size}
              onChange={(e) => {
                setSize(Number(e.target.value));
                setPage(0);
              }}
            >
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <span className="muted">
              Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
            </span>

            <button
              style={buttonStyle}
              disabled={page === 0 || loading}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              Previous
            </button>

            <button
              style={buttonStyle}
              disabled={page + 1 >= totalPages || loading}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Activity Details Modal */}
      {(detailLoading || detailError || activityDetails) && (
        <div
          onClick={() => {
            if (!detailLoading) {
              setSelectedActivity(null);
              setDetailError('');
            }
          }}
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,0.75)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            padding: 16,
            zIndex: 1000,
          }}
        >
          <div
            className="card"
            onClick={(e) => e.stopPropagation()}
            style={{
              width: '100%',
              maxWidth: 650,
              maxHeight: '85vh',
              overflowY: 'auto',
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <h2>Activity Details</h2>
              <button
                style={buttonStyle}
                onClick={() => {
                  setSelectedActivity(null);
                  setDetailError('');
                }}
              >
                Close
              </button>
            </div>

            {detailLoading ? (
              <p>Loading details...</p>
            ) : detailError ? (
              <p className="alert error">{detailError}</p>
            ) : activityDetails ? (
              <div style={{ display: 'grid', gap: 12 }}>
                <p><strong>Activity ID:</strong> {activityDetails.id}</p>
                <p><strong>Action:</strong> {activityDetails.actionType}</p>
                <p><strong>Actor:</strong> {activityDetails.actorName}</p>
                <p><strong>Actor ID:</strong> {activityDetails.actorUserId}</p>
                <p><strong>Role:</strong> {activityDetails.actorRole}</p>
                <p><strong>Entity Type:</strong> {activityDetails.entityType}</p>
                <p><strong>Entity ID:</strong> {activityDetails.entityId}</p>
                <p><strong>Description:</strong> {activityDetails.description}</p>
                <p><strong>IP Address:</strong> {activityDetails.ipAddress}</p>
                <p><strong>Date:</strong> {formatDate(activityDetails.createdAt)}</p>

                <div>
                  <strong>Old Values</strong>
                  <pre style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}>
                    {JSON.stringify(parseJson(activityDetails.oldValues), null, 2) || 'N/A'}
                  </pre>
                </div>

                <div>
                  <strong>New Values</strong>
                  <pre style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}>
                    {JSON.stringify(parseJson(activityDetails.newValues), null, 2) || 'N/A'}
                  </pre>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}
    </div>
  );
}
