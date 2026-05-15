const BASE = 'http://localhost:8080';

export async function fetchSyncLocations() {
  const res = await fetch(`${BASE}/property/sync-locations`);
  if (!res.ok) throw new Error(`Failed to fetch sync locations: ${res.status}`);
  return res.json();
}

export async function runSync({ locationId, locationType, minBedrooms, maxPrice }) {
  const params = new URLSearchParams({ locationId, locationType });
  if (minBedrooms) params.set('minBedrooms', minBedrooms);
  if (maxPrice)    params.set('maxPrice', maxPrice);
  const res = await fetch(`${BASE}/property/sync?${params}`);
  if (!res.ok) throw new Error(`Sync failed: ${res.status}`);
  return res.json();
}
