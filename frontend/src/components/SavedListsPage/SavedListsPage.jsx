import { useState, useEffect } from 'react';
import { fetchSavedLists, createSavedList, deleteSavedList, fetchListEntries, removeFromList } from '../../api/savedLists';
import './SavedListsPage.css';

function sharpenUrl(url) {
  if (!url) return url;
  return url.replace(/_max_\d+x\d+/, '_max_656x437');
}

export default function SavedListsPage({ activeList, onSetActive }) {
  const [lists, setLists] = useState([]);
  const [newName, setNewName] = useState('');
  const [viewingList, setViewingList] = useState(null);
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadLists();
  }, []);

  function loadLists() {
    fetchSavedLists()
      .then(setLists)
      .catch(err => setError(err.message));
  }

  async function handleCreate(e) {
    e.preventDefault();
    if (!newName.trim()) return;
    try {
      const created = await createSavedList(newName.trim());
      setLists(prev => [created, ...prev]);
      setNewName('');
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(listId, listName) {
    if (!window.confirm(`Delete "${listName}"? This will remove all saved properties in this list.`)) return;
    try {
      await deleteSavedList(listId);
      setLists(prev => prev.filter(l => l.id !== listId));
      if (activeList?.id === listId) onSetActive(null);
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleView(list) {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchListEntries(list.id);
      setEntries(data);
      setViewingList(list);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRemoveEntry(propertyId) {
    try {
      await removeFromList(viewingList.id, propertyId);
      setEntries(prev => prev.filter(p => p.id !== propertyId));
      setLists(prev => prev.map(l =>
        l.id === viewingList.id ? { ...l, propertyCount: l.propertyCount - 1 } : l
      ));
    } catch (err) {
      setError(err.message);
    }
  }

  if (viewingList) {
    return (
      <div className="saved-lists-page">
        <div className="sl-detail-header">
          <button className="sl-back-btn" onClick={() => setViewingList(null)}>← Back</button>
          <h2 className="sl-detail-title">{viewingList.name}</h2>
        </div>
        {error && <div className="sl-error">{error}</div>}
        {loading && <div className="sl-status">Loading...</div>}
        {!loading && entries.length === 0 && (
          <div className="sl-status">No properties in this list.</div>
        )}
        <div className="sl-entry-list">
          {entries.map(p => {
            const imgSrc = sharpenUrl(p.mainImageUrl || p.imageUrls?.[0]);
            const isReduced = p.addedOrReduced?.toLowerCase().startsWith('reduced');
            const isNewHome = p.preOwned === 'New Home';
            const parts = [
              p.bedrooms > 0 && `${p.bedrooms} bed`,
              p.bathrooms > 0 && `${p.bathrooms} bath`,
              p.propertySubType,
            ].filter(Boolean);
            return (
              <div key={p.id} className="sl-entry-card">
                <div className="sl-entry-photo">
                  {imgSrc
                    ? <img src={imgSrc} alt={p.displayAddress} loading="lazy" />
                    : <div className="sl-entry-no-photo">No photo</div>
                  }
                  {(isReduced || isNewHome) && (
                    <div className="sl-entry-badges">
                      {isReduced && <span className="sl-badge sl-badge--reduced">Reduced</span>}
                      {isNewHome && <span className="sl-badge sl-badge--new">New Home</span>}
                    </div>
                  )}
                </div>
                <div className="sl-entry-body">
                  <div className="sl-entry-price">
                    {p.priceDisplay || (p.price ? `£${p.price.toLocaleString()}` : 'POA')}
                  </div>
                  <div className="sl-entry-address">{p.displayAddress}</div>
                  {parts.length > 0 && (
                    <div className="sl-entry-specs">{parts.join(' · ')}</div>
                  )}
                  {p.summary && (
                    <div className="sl-entry-summary">{p.summary}</div>
                  )}
                  <div className="sl-entry-footer">
                    <span className="sl-entry-agent">{p.branchName}</span>
                    <button className="sl-remove-btn" onClick={() => handleRemoveEntry(p.id)}>
                      Remove
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  }

  return (
    <div className="saved-lists-page">
      <h2 className="sl-heading">Saved Lists</h2>
      {error && <div className="sl-error">{error}</div>}
      <form className="sl-create-form" onSubmit={handleCreate}>
        <input
          className="sl-name-input"
          type="text"
          placeholder="New list name"
          value={newName}
          onChange={e => setNewName(e.target.value)}
        />
        <button className="sl-create-btn" type="submit">Create</button>
      </form>
      {lists.length === 0 && (
        <div className="sl-status">No saved lists yet.</div>
      )}
      <div className="sl-list">
        {lists.map(l => (
          <div
            key={l.id}
            className={`sl-row${activeList?.id === l.id ? ' sl-row--active' : ''}`}
          >
            <div className="sl-row-info">
              <span className="sl-row-name">{l.name}</span>
              <span className="sl-row-count">{l.propertyCount} {l.propertyCount === 1 ? 'property' : 'properties'}</span>
            </div>
            <div className="sl-row-actions">
              <button className="sl-action-btn" onClick={() => handleView(l)}>View</button>
              <button
                className={`sl-action-btn${activeList?.id === l.id ? ' sl-action-btn--active' : ''}`}
                onClick={() => onSetActive(activeList?.id === l.id ? null : { id: l.id, name: l.name })}
              >
                {activeList?.id === l.id ? 'Active' : 'Set active'}
              </button>
              <button className="sl-action-btn sl-action-btn--danger" onClick={() => handleDelete(l.id, l.name)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
