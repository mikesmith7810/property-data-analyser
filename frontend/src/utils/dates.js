const FORMAT = { day: 'numeric', month: 'short', year: 'numeric' };

// Formats an ISO 8601 string (e.g. "2022-11-14T10:08:24Z") as "14 Nov 2022"
export function formatDate(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleDateString('en-GB', FORMAT);
}

// Formats a LocalDateTime string (no Z) as "14 Nov 2022, 10:08"
export function formatDateTime(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleString('en-GB', {
    ...FORMAT,
    hour: '2-digit',
    minute: '2-digit',
  });
}

// Reformats "Reduced on 06/02/2026" → "Reduced on 6 Feb 2026"
export function formatAddedOrReduced(value) {
  if (!value) return null;
  const match = value.match(/^(.*?)\s(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!match) return value;
  const [, prefix, day, month, year] = match;
  const date = new Date(`${year}-${month}-${day}`);
  return `${prefix} ${date.toLocaleDateString('en-GB', FORMAT)}`;
}
