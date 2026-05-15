import { useState, useEffect } from 'react';
import { fetchListing } from '../../api/listings';
import ImageGallery from '../ImageGallery/ImageGallery';
import { formatDate, formatAddedOrReduced } from '../../utils/dates';
import './PropertyDetail.css';

export default function PropertyDetail({ id }) {
  const [listing, setListing] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);
    setListing(null);
    fetchListing(id)
      .then(setListing)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="status">Loading property...</div>;
  if (error) return <div className="status error">{error}</div>;
  if (!listing) return null;

  const p = listing;
  const allImages = [...(p.imageUrls ?? []), ...(p.floorplanUrls ?? [])];
  const isReduced = p.addedOrReduced?.toLowerCase().startsWith('reduced');
  const rmUrl = `https://www.rightmove.co.uk${p.propertyUrl ?? ''}`;

  return (
    <div className="property-detail">
      <ImageGallery images={allImages} />

      <div className="detail-body">
        <div className="detail-badges">
          {isReduced && <span className="badge badge-reduced">Reduced</span>}
          {p.soldSTC && <span className="badge badge-stc">Sold STC</span>}
          {p.preOwned === 'New Home' && <span className="badge badge-new">New Home</span>}
        </div>

        <h2 className="detail-price">
          {p.priceDisplay || (p.price ? `£${p.price.toLocaleString()}` : 'POA')}
          {p.priceQualifier && <span className="detail-qualifier"> {p.priceQualifier}</span>}
        </h2>

        <h3 className="detail-address">{p.displayAddress}</h3>

        <div className="detail-meta">
          {p.bedrooms > 0 && <span>{p.bedrooms} bedrooms</span>}
          {p.bathrooms > 0 && <span>{p.bathrooms} bathrooms</span>}
          {p.propertySubType && <span>{p.propertySubType}</span>}
          {p.displaySize && <span>{p.displaySize}</span>}
        </div>

        {p.summary && <p className="detail-summary">{p.summary}</p>}

        {p.keyFeatures?.length > 0 && (
          <ul className="detail-features">
            {p.keyFeatures.map((f, i) => <li key={i}>{f}</li>)}
          </ul>
        )}

        <div className="detail-info-grid">
          {p.branchName && (
            <div className="info-row">
              <span className="info-label">Agent</span>
              <span>{p.branchName}</span>
            </div>
          )}
          {p.firstVisibleDate && (
            <div className="info-row">
              <span className="info-label">Listed</span>
              <span>{formatDate(p.firstVisibleDate)}</span>
            </div>
          )}
          {p.addedOrReduced && (
            <div className="info-row">
              <span className="info-label">Status</span>
              <span>{formatAddedOrReduced(p.addedOrReduced)}</span>
            </div>
          )}
          {p.listingUpdateReason && (
            <div className="info-row">
              <span className="info-label">Update</span>
              <span>{p.listingUpdateReason}</span>
            </div>
          )}
          {p.tenureType && (
            <div className="info-row">
              <span className="info-label">Tenure</span>
              <span>
                {p.tenureType}
                {p.yearsRemainingOnLease > 0 && ` (${p.yearsRemainingOnLease} yrs remaining)`}
              </span>
            </div>
          )}
          {p.contactTelephone && (
            <div className="info-row">
              <span className="info-label">Contact</span>
              <a href={`tel:${p.contactTelephone}`}>{p.contactTelephone}</a>
            </div>
          )}
        </div>

        {p.nearestStations?.length > 0 && (
          <div className="detail-stations">
            <h4>Nearest stations</h4>
            <ul>
              {p.nearestStations.map((s, i) => (
                <li key={i}>{s.name} — {s.distance} {s.unit}</li>
              ))}
            </ul>
          </div>
        )}

        <div className="detail-actions">
          <a className="btn-primary" href={rmUrl} target="_blank" rel="noopener noreferrer">
            View on Rightmove
          </a>
        </div>
      </div>
    </div>
  );
}
