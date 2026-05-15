import { useState, useEffect } from 'react';
import { fetchListings } from '../../api/listings';
import PropertyCard from '../PropertyCard/PropertyCard';
import Pagination from '../Pagination/Pagination';
import './PropertyList.css';

export default function PropertyList({ filters, onSelect }) {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setPage(0);
  }, [filters]);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchListings({ ...filters, page, size: 20 })
      .then(setData)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [filters, page]);

  if (loading) return <div className="status">Loading...</div>;
  if (error) return <div className="status error">{error}</div>;
  if (!data) return null;

  const totalPages = Math.ceil(data.totalCount / data.size);

  return (
    <div className="property-list">
      <div className="list-meta">{data.totalCount.toLocaleString()} properties found</div>
      {data.listings.length === 0 ? (
        <div className="status">No properties match your filters.</div>
      ) : (
        <>
          <div className="card-grid">
            {data.listings.map(p => (
              <PropertyCard key={p.id} property={p} onClick={() => onSelect(p.id)} />
            ))}
          </div>
          {totalPages > 1 && (
            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
          )}
        </>
      )}
    </div>
  );
}
