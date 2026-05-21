import './PropertyCard.css';

function sharpenUrl(url) {
  if (!url) return url;
  return url.replace(/_max_\d+x\d+/, '_max_656x437');
}

export default function PropertyCard({ property: p, onClick, showSaveCheckbox, isSaved, onToggleSave }) {
  const isReduced = p.addedOrReduced?.toLowerCase().startsWith('reduced');
  const isNewHome = p.preOwned === 'New Home';
  const imgSrc = sharpenUrl(p.mainImageUrl || p.imageUrls?.[0]);
  const extraImgs = [p.imageUrls?.[1], p.imageUrls?.[2], p.imageUrls?.[3]].filter(Boolean);

  return (
    <div
      role="button"
      tabIndex={0}
      className="property-card"
      onClick={onClick}
      onKeyDown={e => { if (e.key === 'Enter') onClick(); }}
    >
      <div className="card-img-wrap">
        {imgSrc ? (
          <img src={imgSrc} alt={p.displayAddress} loading="lazy" className="card-img" />
        ) : (
          <div className="card-img-placeholder">No image</div>
        )}
        <div className="card-badges-left">
          {isReduced && <span className="badge badge-reduced">Reduced</span>}
          {isNewHome && <span className="badge badge-new">New Home</span>}
        </div>
        {p.soldSTC && <span className="badge badge-stc">Sold STC</span>}
      </div>
      {extraImgs.length > 0 && (
        <div className="card-img-strip">
          {extraImgs.map((url, i) => (
            <img key={i} src={sharpenUrl(url)} alt="" loading="lazy" className="card-img-strip-thumb" />
          ))}
        </div>
      )}
      <div className="card-body">
        <div className="card-price">
          {p.priceDisplay || (p.price ? `£${p.price.toLocaleString()}` : 'POA')}
        </div>
        <div className="card-address">{p.displayAddress}</div>
        <div className="card-meta">
          {p.bedrooms > 0 && <span>{p.bedrooms} bed</span>}
          {p.bathrooms > 0 && <span>{p.bathrooms} bath</span>}
          {p.propertySubType && <span>{p.propertySubType}</span>}
        </div>
        <div className="card-agent">{p.branchName}</div>
        {showSaveCheckbox && (
          <label className="card-save-checkbox" onClick={e => e.stopPropagation()}>
            <input type="checkbox" checked={isSaved} onChange={onToggleSave} />
            Save
          </label>
        )}
      </div>
    </div>
  );
}
