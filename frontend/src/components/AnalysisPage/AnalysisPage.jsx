import { useEffect, useState } from 'react';
import { fetchSyncLocations } from '../../api/sync';
import { fetchAvgPriceByBedrooms } from '../../api/analysis';
import './AnalysisPage.css';

function fmt(n) {
  return `£${Math.round(n).toLocaleString('en-GB')}`;
}

function BarChart({ data, scaleMax }) {
  if (!data?.length) return null;

  return (
    <div className="bar-chart">
      {data.map(d => (
        <div key={d.bedrooms} className="bar-col">
          <div className="bar-price">{fmt(d.avgPrice)}</div>
          <div
            className="bar-fill"
            style={{ height: `${Math.round((d.avgPrice / scaleMax) * 100)}%` }}
          />
          <div className="bar-label">
            {d.bedrooms} {d.bedrooms === 1 ? 'bed' : 'beds'}
          </div>
          <div className="bar-count">{d.count} {d.count === 1 ? 'property' : 'properties'}</div>
        </div>
      ))}
    </div>
  );
}

export default function AnalysisPage() {
  const [locations, setLocations] = useState([]);
  const [town, setTown] = useState('');
  const [allData, setAllData] = useState({});
  const [globalMax, setGlobalMax] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchSyncLocations()
      .then(locs => {
        setLocations(locs);
        if (locs.length > 0) setTown(locs[0].name);

        // Fetch all locations in parallel to establish a consistent scale
        setLoading(true);
        Promise.all(
          [...locs.map(l => fetchAvgPriceByBedrooms(l.name)), fetchAvgPriceByBedrooms('')]
        ).then(results => {
          const map = {};
          locs.forEach((l, i) => { map[l.name] = results[i]; });
          map[''] = results[locs.length];
          setAllData(map);

          const max = Math.max(
            1,
            ...Object.values(map).flat().map(r => r.avgPrice)
          );
          setGlobalMax(max);
        }).catch(err => setError(err.message))
          .finally(() => setLoading(false));
      })
      .catch(() => {});
  }, []);

  const data = allData[town] ?? null;

  return (
    <div className="analysis-page">
      <h2 className="analysis-title">Analysis</h2>

      <section className="analysis-section">
        <div className="analysis-section-header">
          <h3>Average price by bedrooms</h3>
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
        </div>

        {loading && <div className="analysis-loading">Loading…</div>}
        {error && <div className="analysis-error">{error}</div>}

        {data && !loading && (
          <>
            <BarChart data={data} scaleMax={globalMax} />
            <table className="analysis-table">
              <thead>
                <tr>
                  <th>Bedrooms</th>
                  <th>Avg price</th>
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
