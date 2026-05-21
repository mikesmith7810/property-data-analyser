import { useState, useEffect } from 'react';
import { fetchListings } from '../../api/listings';
import { addToList, removeFromList } from '../../api/savedLists';
import PropertyCard from '../PropertyCard/PropertyCard';
import './PropertyList.css';

export default function PropertyList({ filters, onSelect, activeList, savedPropertyIds, onSavedPropertyIdsChange }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchListings({ ...filters, page: 0, size: 500 })
      .then(setData)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [filters]);

  async function handleToggleSave(propertyId) {
    if (!activeList) return;
    if (savedPropertyIds.has(propertyId)) {
      await removeFromList(activeList.id, propertyId);
      onSavedPropertyIdsChange(prev => {
        const next = new Set(prev);
        next.delete(propertyId);
        return next;
      });
    } else {
      await addToList(activeList.id, propertyId);
      onSavedPropertyIdsChange(prev => new Set([...prev, propertyId]));
    }
  }

  if (loading) return <div className="status">Loading...</div>;
  if (error) return <div className="status error">{error}</div>;
  if (!data) return null;

  return (
    <div className="property-list">
      <div className="list-meta">{data.totalCount.toLocaleString()} properties found</div>
      {data.listings.length === 0 ? (
        <div className="status">No properties match your filters.</div>
      ) : (
        <div className="card-grid">
          {data.listings.map(p => (
            <PropertyCard
              key={p.id}
              property={p}
              onClick={() => onSelect(p.id)}
              showSaveCheckbox={activeList !== null}
              isSaved={savedPropertyIds?.has(p.id) ?? false}
              onToggleSave={() => handleToggleSave(p.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
}
