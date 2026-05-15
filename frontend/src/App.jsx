import { useState } from 'react';
import FilterBar from './components/FilterBar/FilterBar';
import PropertyList from './components/PropertyList/PropertyList';
import PropertyDetail from './components/PropertyDetail/PropertyDetail';
import SyncPage from './components/SyncPage/SyncPage';
import AnalysisPage from './components/AnalysisPage/AnalysisPage';
import './App.css';

export default function App() {
  const [view, setView] = useState('list');
  const [selectedId, setSelectedId] = useState(null);
  const [filters, setFilters] = useState({ agentName: '', town: '', reduced: false, newHome: false, sortBy: 'daysOnMarket' });

  function openDetail(id) {
    setSelectedId(id);
    setView('detail');
  }

  function goToList() {
    setView('list');
    setSelectedId(null);
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-inner">
          <h1 className="header-title">Property Search</h1>
          <nav className="header-tabs">
            <button
              className={`header-tab${view !== 'sync' ? ' active' : ''}`}
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
        ) : view === 'detail' ? (
          <PropertyDetail id={selectedId} />
        ) : (
          <>
            <FilterBar filters={filters} onChange={setFilters} />
            <PropertyList filters={filters} onSelect={openDetail} />
          </>
        )}
      </main>
    </div>
  );
}
