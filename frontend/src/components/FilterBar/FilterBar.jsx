import { useEffect, useRef, useState } from 'react';
import { fetchAgents, fetchTowns } from '../../api/listings';
import './FilterBar.css';

export default function FilterBar({ filters, onChange }) {
  const [agents, setAgents] = useState([]);
  const [towns, setTowns] = useState([]);
  const agentDebounce = useRef(null);
  const agentRef = useRef(null);

  useEffect(() => {
    fetchAgents().then(setAgents).catch(() => {});
    fetchTowns().then(setTowns).catch(() => {});
  }, []);

  function handleAgent(e) {
    const value = e.target.value;
    clearTimeout(agentDebounce.current);
    agentDebounce.current = setTimeout(() => {
      onChange({ ...filters, agentName: value });
    }, 400);
  }

  function clearAgent() {
    if (agentRef.current) agentRef.current.value = '';
    onChange({ ...filters, agentName: '' });
  }

  return (
    <div className="filter-bar">
      <div className="filter-agent-wrap">
        <input
          ref={agentRef}
          type="text"
          list="agent-list"
          placeholder="Search by estate agent..."
          defaultValue={filters.agentName}
          onChange={handleAgent}
          className="filter-input"
        />
        {filters.agentName && (
          <button className="filter-clear" onClick={clearAgent} aria-label="Clear agent">✕</button>
        )}
        <datalist id="agent-list">
          {agents.map(a => (
            <option key={a.name} value={a.name}>{a.name} ({a.count})</option>
          ))}
        </datalist>
      </div>

      <select
        value={filters.town}
        onChange={e => onChange({ ...filters, town: e.target.value })}
        className="filter-select"
      >
        <option value="">All towns</option>
        {towns.map(t => (
          <option key={t} value={t}>{t}</option>
        ))}
      </select>

      <label className="filter-check">
        <input
          type="checkbox"
          checked={filters.reduced}
          onChange={e => onChange({ ...filters, reduced: e.target.checked })}
        />
        Reduced only
      </label>

      <label className="filter-check">
        <input
          type="checkbox"
          checked={filters.newHome}
          onChange={e => onChange({ ...filters, newHome: e.target.checked })}
        />
        New homes only
      </label>

      <label className="filter-check">
        <input
          type="checkbox"
          checked={filters.vacant}
          onChange={e => onChange({ ...filters, vacant: e.target.checked })}
        />
        Chain free / vacant
      </label>

      <select
        value={filters.minBeds}
        onChange={e => onChange({ ...filters, minBeds: Number(e.target.value) })}
        className="filter-select"
      >
        <option value={0}>Any beds</option>
        <option value={1}>1 bed</option>
        <option value={2}>2 beds</option>
        <option value={3}>3 beds</option>
        <option value={4}>4 beds</option>
        <option value={5}>5 beds</option>
        <option value={6}>6 beds</option>
      </select>

      <select
        value={filters.sortBy}
        onChange={e => onChange({ ...filters, sortBy: e.target.value })}
        className="filter-select"
      >
        <option value="daysOnMarket">Longest on market</option>
        <option value="priceAsc">Price: low to high</option>
        <option value="priceDesc">Price: high to low</option>
      </select>
    </div>
  );
}
