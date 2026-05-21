const HEADERS = { 'ngrok-skip-browser-warning': 'true' };

export async function fetchAvgPriceByBedrooms(town, newHome = false) {
  const params = new URLSearchParams();
  if (town) params.set('town', town);
  if (newHome) params.set('newHome', 'true');
  const res = await fetch(`/property/analysis/avg-price-by-bedrooms?${params}`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Analysis failed: ${res.status}`);
  return res.json();
}
