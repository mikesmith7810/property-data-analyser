import { useRef } from 'react';
import './FilterBar.css';

export default function FilterBar({ filters, onChange }) {
  const debounceRef = useRef(null);

  function handleAgent(e) {
    const value = e.target.value;
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      onChange({ ...filters, agentName: value });
    }, 400);
  }

  return (
    <div className="filter-bar">
      <input
        type="text"
        placeholder="Search by estate agent..."
        defaultValue={filters.agentName}
        onChange={handleAgent}
        className="filter-input"
      />
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
