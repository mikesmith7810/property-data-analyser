import './PropertyCard.css';

export default function PropertyCard({ property: p, onClick }) {
  const isReduced = p.addedOrReduced?.toLowerCase().startsWith('reduced');
  const isNewHome = p.preOwned === 'New Home';
  const imgSrc = p.mainImageUrl || p.imageUrls?.[0];

  return (
    <button className="property-card" onClick={onClick}>
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
      </div>
    </button>
  );
}
