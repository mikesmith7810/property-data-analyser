import { useEffect, useRef, useState } from 'react';
import { fetchAgents } from '../../api/listings';
import './FilterBar.css';

export default function FilterBar({ filters, onChange }) {
  const [agents, setAgents] = useState([]);
  const debounceRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    fetchAgents().then(setAgents).catch(() => {});
  }, []);

  function handleAgent(e) {
    const value = e.target.value;
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      onChange({ ...filters, agentName: value });
    }, 400);
  }

  function clearAgent() {
    if (inputRef.current) inputRef.current.value = '';
    onChange({ ...filters, agentName: '' });
  }

  return (
    <div className="filter-bar">
      <div className="filter-agent-wrap">
        <input
          ref={inputRef}
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

      <label className="filter-check">
        <input
          type="checkbox"
          checked={filters.reduced}
          onChange={e => onChange({ ...filters, reduced: e.target.checked })}
        />
        Reduced only
      </label>

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
