const HEADERS = { 'ngrok-skip-browser-warning': 'true' };

export async function fetchListings({ agentName, town, reduced, newHome, vacant, minBeds, sortBy, page = 0, size = 20 } = {}) {
  const params = new URLSearchParams();
  if (agentName)    params.set('agentName', agentName);
  if (town)         params.set('town', town);
  if (reduced)      params.set('reduced', 'true');
  if (newHome)      params.set('newHome', 'true');
  if (vacant)       params.set('vacant', 'true');
  if (minBeds > 0)  params.set('beds', minBeds);
  if (sortBy)       params.set('sortBy', sortBy);
  params.set('page', page);
  params.set('size', size);
  const res = await fetch(`/property/listings?${params}`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to fetch listings: ${res.status}`);
  return res.json();
}

export async function fetchListing(id) {
  const res = await fetch(`/property/listings/${id}`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Listing ${id} not found`);
  return res.json();
}

export async function fetchAgents() {
  const res = await fetch(`/property/agents`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to fetch agents: ${res.status}`);
  return res.json();
}

export async function fetchTowns() {
  const res = await fetch(`/property/towns`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to fetch towns: ${res.status}`);
  return res.json();
}
