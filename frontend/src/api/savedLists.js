const HEADERS = { 'ngrok-skip-browser-warning': 'true', 'Content-Type': 'application/json' };

export async function fetchSavedLists() {
  const res = await fetch('/saved-lists', { headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to fetch saved lists: ${res.status}`);
  return res.json();
}

export async function createSavedList(name) {
  const res = await fetch('/saved-lists', {
    method: 'POST',
    headers: HEADERS,
    body: JSON.stringify({ name }),
  });
  if (!res.ok) throw new Error(`Failed to create list: ${res.status}`);
  return res.json();
}

export async function deleteSavedList(listId) {
  const res = await fetch(`/saved-lists/${listId}`, { method: 'DELETE', headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to delete list: ${res.status}`);
}

export async function fetchListEntries(listId) {
  const res = await fetch(`/saved-lists/${listId}/entries`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to fetch list entries: ${res.status}`);
  return res.json();
}

export async function fetchListPropertyIds(listId) {
  const res = await fetch(`/saved-lists/${listId}/property-ids`, { headers: HEADERS });
  if (!res.ok) throw new Error(`Failed to fetch property IDs: ${res.status}`);
  return res.json();
}

export async function addToList(listId, propertyId) {
  const res = await fetch(`/saved-lists/${listId}/entries`, {
    method: 'POST',
    headers: HEADERS,
    body: JSON.stringify({ propertyId }),
  });
  if (!res.ok) throw new Error(`Failed to add to list: ${res.status}`);
}

export async function removeFromList(listId, propertyId) {
  const res = await fetch(`/saved-lists/${listId}/entries/${propertyId}`, {
    method: 'DELETE',
    headers: HEADERS,
  });
  if (!res.ok) throw new Error(`Failed to remove from list: ${res.status}`);
}
