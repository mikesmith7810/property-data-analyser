import './Pagination.css';

export default function Pagination({ page, totalPages, onChange }) {
  return (
    <div className="pagination">
      <button
        className="page-btn"
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
      >
        ← Previous
      </button>
      <span className="page-info">{page + 1} of {totalPages}</span>
      <button
        className="page-btn"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Next →
      </button>
    </div>
  );
}
