import { useEffect, useState } from 'react';
import { fetchSyncLocations } from '../../api/sync';
import { fetchAvgPriceByBedrooms } from '../../api/analysis';
import './AnalysisPage.css';

function fmt(n) {
  return `£${Math.round(n).toLocaleString('en-GB')}`;
}

const BEDROOM_SLOTS = [1, 2, 3, 4, 5, 6];

function BarChart({ data }) {
  if (!data?.length) return null;
  const byBeds = Object.fromEntries(data.map(d => [d.bedrooms, d]));
  const max = Math.max(...data.map(d => d.avgPrice));

  return (
    <div className="bar-chart">
      {BEDROOM_SLOTS.map(beds => {
        const d = byBeds[beds];
        return (
          <div key={beds} className="bar-col">
            <div className="bar-area">
              {d && (
                <>
                  <div className="bar-price">{fmt(d.avgPrice)}</div>
                  <div
                    className="bar-fill"
                    style={{ height: `${((d.avgPrice / max) * 100).toFixed(2)}%` }}
                  />
                </>
              )}
            </div>
            <div className="bar-label">{beds} {beds === 1 ? 'bed' : 'beds'}</div>
            {d && <div className="bar-count">{d.count} {d.count === 1 ? 'property' : 'properties'}</div>}
          </div>
        );
      })}
    </div>
  );
}

export default function AnalysisPage() {
  const [locations, setLocations] = useState([]);
  const [town, setTown] = useState('');
  const [newHome, setNewHome] = useState(false);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchSyncLocations()
      .then(locs => {
        setLocations(locs);
        if (locs.length > 0) setTown(locs[0].name);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (town === null) return;
    setLoading(true);
    setError(null);
    fetchAvgPriceByBedrooms(town, newHome)
      .then(setData)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [town, newHome]);

  return (
    <div className="analysis-page">
      <h2 className="analysis-title">Analysis</h2>

      <section className="analysis-section">
        <div className="analysis-section-header">
          <h3>Median price by bedrooms</h3>
          <div className="analysis-controls">
            <select
              value={town}
              onChange={e => setTown(e.target.value)}
              className="analysis-select"
            >
              <option value="">All locations</option>
              {locations.map(l => (
                <option key={l.locationId} value={l.name}>{l.name}</option>
              ))}
            </select>
            <label className="analysis-checkbox-label">
              <input
                type="checkbox"
                checked={newHome}
                onChange={e => setNewHome(e.target.checked)}
              />
              New homes only
            </label>
          </div>
        </div>

        {loading && <div className="analysis-loading">Loading…</div>}
        {error && <div className="analysis-error">{error}</div>}

        {data && !loading && (
          <>
            <BarChart data={data} />
            <table className="analysis-table">
              <thead>
                <tr>
                  <th>Bedrooms</th>
                  <th>Median price</th>
                  <th>Properties</th>
                </tr>
              </thead>
              <tbody>
                {data.map(d => (
                  <tr key={d.bedrooms}>
                    <td>{d.bedrooms}</td>
                    <td>{fmt(d.avgPrice)}</td>
                    <td>{d.count}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        )}
      </section>
    </div>
  );
}
