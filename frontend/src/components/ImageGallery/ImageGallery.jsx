import { useState, useRef } from 'react';
import './ImageGallery.css';

export default function ImageGallery({ images }) {
  const [current, setCurrent] = useState(0);
  const touchStartX = useRef(null);

  if (!images?.length) return null;

  function prev() {
    setCurrent(i => (i - 1 + images.length) % images.length);
  }

  function next() {
    setCurrent(i => (i + 1) % images.length);
  }

  function onTouchStart(e) {
    touchStartX.current = e.touches[0].clientX;
  }

  function onTouchEnd(e) {
    if (touchStartX.current === null) return;
    const delta = touchStartX.current - e.changedTouches[0].clientX;
    if (delta > 50) next();
    else if (delta < -50) prev();
    touchStartX.current = null;
  }

  return (
    <div className="gallery">
      <div
        className="gallery-main"
        onTouchStart={onTouchStart}
        onTouchEnd={onTouchEnd}
      >
        <img src={images[current]} alt={`Photo ${current + 1} of ${images.length}`} />
        {images.length > 1 && (
          <>
            <button className="gallery-btn gallery-prev" onClick={prev} aria-label="Previous image">
              ‹
            </button>
            <button className="gallery-btn gallery-next" onClick={next} aria-label="Next image">
              ›
            </button>
          </>
        )}
        <div className="gallery-counter">{current + 1} / {images.length}</div>
      </div>
      {images.length > 1 && (
        <div className="gallery-thumbs">
          {images.map((url, i) => (
            <button
              key={i}
              className={`gallery-thumb${i === current ? ' active' : ''}`}
              onClick={() => setCurrent(i)}
              aria-label={`View photo ${i + 1}`}
            >
              <img src={url} alt="" loading="lazy" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
