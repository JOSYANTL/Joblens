export function toLocalDateTimeInput(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
}

export function toIsoInstant(localDateTime: string): string | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})$/.exec(localDateTime);
  if (!match) return null;
  const date = new Date(localDateTime);
  if (Number.isNaN(date.getTime()) || date.getFullYear() !== Number(match[1]) ||
      date.getMonth() + 1 !== Number(match[2]) || date.getDate() !== Number(match[3]) ||
      date.getHours() !== Number(match[4]) || date.getMinutes() !== Number(match[5])) return null;
  return date.toISOString();
}
