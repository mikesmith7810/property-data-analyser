import { useEffect, useState } from 'react';
import { fetchSyncLocations, runSync } from '../../api/sync';
import { formatDateTime } from '../../utils/dates';
import './SyncPage.css';

export default function SyncPage() {
  const [locations, setLocations] = useState([]);
  const [locationId, setLocationId] = useState('');
  const [locationType, setLocationType] = useState('REGION');
  const [minBedrooms, setMinBedrooms] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [syncing, setSyncing] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchSyncLocations()
      .then(locs => {
        setLocations(locs);
        if (locs.length > 0) {
          setLocationId(locs[0].locationId);
          setLocationType(locs[0].locationType);
        }
      })
      .catch(err => setError(err.message));
  }, []);

  function handleLocationChange(e) {
    const loc = locations.find(l => l.locationId === e.target.value);
    if (loc) {
      setLocationId(loc.locationId);
      setLocationType(loc.locationType);
    }
  }

  async function handleSync(e) {
    e.preventDefault();
    setSyncing(true);
    setResult(null);
    setError(null);
    try {
      const res = await runSync({ locationId, locationType, minBedrooms, maxPrice });
      setResult(res);
      fetchSyncLocations().then(setLocations).catch(() => {});
    } catch (err) {
      setError(err.message);
    } finally {
      setSyncing(false);
    }
  }

  return (
    <div className="sync-page">
      <h2 className="sync-title">Sync Properties</h2>
      <p className="sync-desc">
        Fetches all matching listings from Rightmove and saves them to the database.
        This can take several minutes for large result sets.
      </p>

      <form className="sync-form" onSubmit={handleSync}>
        <div className="sync-field">
          <label htmlFor="sync-location">Town</label>
          <select
            id="sync-location"
            value={locationId}
            onChange={handleLocationChange}
            disabled={syncing}
          >
            {locations.map(l => (
              <option key={l.locationId} value={l.locationId}>
                {l.name}{l.lastSyncedAt ? ` — last synced ${formatDateTime(l.lastSyncedAt)}` : ''}
              </option>
            ))}
          </select>
        </div>

        <div className="sync-field">
          <label htmlFor="sync-beds">Min bedrooms</label>
          <input
            id="sync-beds"
            type="number"
            min="0"
            max="10"
            placeholder="Any"
            value={minBedrooms}
            onChange={e => setMinBedrooms(e.target.value)}
            disabled={syncing}
          />
        </div>

        <div className="sync-field">
          <label htmlFor="sync-price">Max price (£)</label>
          <input
            id="sync-price"
            type="number"
            min="0"
            step="10000"
            placeholder="Any"
            value={maxPrice}
            onChange={e => setMaxPrice(e.target.value)}
            disabled={syncing}
          />
        </div>

        <button type="submit" className="sync-btn" disabled={syncing || !locationId}>
          {syncing ? 'Syncing…' : 'Start Sync'}
        </button>
      </form>

      {syncing && (
        <div className="sync-status">
          <div className="sync-spinner" />
          Fetching listings from Rightmove — this may take a few minutes…
        </div>
      )}

      {result && (
        <div className="sync-result">
          <h3>Sync complete</h3>
          <div className="sync-stats">
            <div className="sync-stat">
              <span className="stat-value">{result.searched}</span>
              <span className="stat-label">Found</span>
            </div>
            <div className="sync-stat">
              <span className="stat-value">{result.enriched}</span>
              <span className="stat-label">Saved</span>
            </div>
            {result.failed > 0 && (
              <div className="sync-stat sync-stat--warn">
                <span className="stat-value">{result.failed}</span>
                <span className="stat-label">Failed</span>
              </div>
            )}
          </div>
        </div>
      )}

      {error && <div className="sync-error">{error}</div>}
    </div>
  );
}
