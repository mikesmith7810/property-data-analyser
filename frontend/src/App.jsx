import { useState } from 'react';
import FilterBar from './components/FilterBar/FilterBar';
import PropertyList from './components/PropertyList/PropertyList';
import PropertyDetail from './components/PropertyDetail/PropertyDetail';
import './App.css';

export default function App() {
  const [view, setView] = useState('list');
  const [selectedId, setSelectedId] = useState(null);
  const [filters, setFilters] = useState({ agentName: '', reduced: false, sortBy: 'daysOnMarket' });

  function openDetail(id) {
    setSelectedId(id);
    setView('detail');
  }

  function backToList() {
    setView('list');
    setSelectedId(null);
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-inner">
          <h1 className="header-title">Property Search</h1>
          {view === 'detail' && (
            <button className="btn-back" onClick={backToList}>← Back to results</button>
          )}
        </div>
      </header>
      <main className="app-main">
        {view === 'list' ? (
          <>
            <FilterBar filters={filters} onChange={setFilters} />
            <PropertyList filters={filters} onSelect={openDetail} />
          </>
        ) : (
          <PropertyDetail id={selectedId} />
        )}
      </main>
    </div>
  );
}
