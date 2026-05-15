const HEADERS = { 'ngrok-skip-browser-warning': 'true' };

export async function fetchSyncLocations() {
  const res = await fetch(`/property/sync-locations`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to fetch sync locations: ${res.status}`);
  return res.json();
}

export async function runSync({ locationId, locationType, minBedrooms, maxPrice }) {
  const params = new URLSearchParams({ locationId, locationType });
  if (minBedrooms) params.set('minBedrooms', minBedrooms);
  if (maxPrice)    params.set('maxPrice', maxPrice);
  const res = await fetch(`/property/sync?${params}`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Sync failed: ${res.status}`);
  return res.json();
}
