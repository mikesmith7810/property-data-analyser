import { useRef, useState, useEffect } from 'react';
import FilterBar from './components/FilterBar/FilterBar';
import PropertyList from './components/PropertyList/PropertyList';
import PropertyDetail from './components/PropertyDetail/PropertyDetail';
import SyncPage from './components/SyncPage/SyncPage';
import AnalysisPage from './components/AnalysisPage/AnalysisPage';
import SavedListsPage from './components/SavedListsPage/SavedListsPage';
import { fetchListPropertyIds } from './api/savedLists';
import './App.css';

export default function App() {
  const [view, setView] = useState('list');
  const [selectedId, setSelectedId] = useState(null);
  const [filters, setFilters] = useState({ agentName: '', town: '', reduced: false, newHome: false, vacant: false, minBeds: 0, sortBy: 'daysOnMarket' });
  const [activeList, setActiveList] = useState(null);
  const [savedListPropertyIds, setSavedListPropertyIds] = useState(new Set());
  const savedScroll = useRef(0);

  useEffect(() => {
    if (!activeList) { setSavedListPropertyIds(new Set()); return; }
    fetchListPropertyIds(activeList.id)
      .then(ids => setSavedListPropertyIds(new Set(ids)))
      .catch(() => {});
  }, [activeList]);

  function openDetail(id) {
    savedScroll.current = window.scrollY;
    setSelectedId(id);
    setView('detail');
    window.scrollTo(0, 0);
  }

  function goToList() {
    setView('list');
    setSelectedId(null);
    requestAnimationFrame(() => window.scrollTo(0, savedScroll.current));
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-inner">
          <h1 className="header-title">Property Search</h1>
          <nav className="header-tabs">
            <button
              className={`header-tab${view === 'list' || view === 'detail' ? ' active' : ''}`}
              onClick={goToList}
            >
              Search
            </button>
            <button
              className={`header-tab${view === 'analysis' ? ' active' : ''}`}
              onClick={() => setView('analysis')}
            >
              Analysis
            </button>
            <button
              className={`header-tab${view === 'saved' ? ' active' : ''}`}
              onClick={() => setView('saved')}
            >
              Saved Lists
            </button>
            <button
              className={`header-tab${view === 'sync' ? ' active' : ''}`}
              onClick={() => setView('sync')}
            >
              Sync
            </button>
          </nav>
          {view === 'detail' && (
            <button className="btn-back" onClick={goToList}>← Back</button>
          )}
        </div>
      </header>
      <main className="app-main">
        {view === 'sync' ? (
          <SyncPage />
        ) : view === 'analysis' ? (
          <AnalysisPage />
        ) : view === 'saved' ? (
          <SavedListsPage activeList={activeList} onSetActive={setActiveList} />
        ) : view === 'detail' ? (
          <PropertyDetail id={selectedId} />
        ) : null}
        {view !== 'sync' && view !== 'analysis' && view !== 'saved' && view !== 'detail' && (
          <FilterBar filters={filters} onChange={setFilters} />
        )}
        {view === 'list' && activeList && (
          <div className="active-list-banner">
            Saving to: <strong>{activeList.name}</strong>
            <button className="active-list-clear" onClick={() => setActiveList(null)}>Clear</button>
          </div>
        )}
        <div style={{ display: view === 'list' ? 'block' : 'none' }}>
          <PropertyList
            filters={filters}
            onSelect={openDetail}
            activeList={activeList}
            savedPropertyIds={savedListPropertyIds}
            onSavedPropertyIdsChange={setSavedListPropertyIds}
          />
        </div>
      </main>
    </div>
  );
}
