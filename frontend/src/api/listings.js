const BASE = 'http://localhost:8080';

export async function fetchListings({ agentName, reduced, sortBy, page = 0, size = 20 } = {}) {
  const params = new URLSearchParams();
  if (agentName) params.set('agentName', agentName);
  if (reduced)   params.set('reduced', 'true');
  if (sortBy)    params.set('sortBy', sortBy);
  params.set('page', page);
  params.set('size', size);
  const res = await fetch(`${BASE}/property/listings?${params}`);
  if (!res.ok) throw new Error(`Failed to fetch listings: ${res.status}`);
  return res.json();
}

export async function fetchListing(id) {
  const res = await fetch(`${BASE}/property/listings/${id}`);
  if (!res.ok) throw new Error(`Listing ${id} not found`);
  return res.json();
}

export async function fetchAgents() {
  const res = await fetch(`${BASE}/property/agents`);
  if (!res.ok) throw new Error(`Failed to fetch agents: ${res.status}`);
  return res.json();
}
