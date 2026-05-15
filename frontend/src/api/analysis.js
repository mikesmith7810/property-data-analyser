const HEADERS = { 'ngrok-skip-browser-warning': 'true' };

export async function fetchAvgPriceByBedrooms(town) {
  const params = new URLSearchParams();
  if (town) params.set('town', town);
  const res = await fetch(`/property/analysis/avg-price-by-bedrooms?${params}`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Analysis failed: ${res.status}`);
  return res.json();
}
