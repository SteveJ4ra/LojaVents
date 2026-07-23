export const ECUADOR_TIME_ZONE = 'America/Guayaquil';

function partsFor(value: Date): Record<string, string> {
  return Object.fromEntries(new Intl.DateTimeFormat('en-CA', {
    timeZone: ECUADOR_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23'
  }).formatToParts(value).map(part => [part.type, part.value]));
}

export function formatEcuadorDateTime(value: string | Date | null | undefined): string {
  if (!value) return '';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  const parts = partsFor(date);
  return `${parts['year']}-${parts['month']}-${parts['day']} ${parts['hour']}:${parts['minute']}:${parts['second']}`;
}

export function formatEcuadorDate(value: string): string {
  const [year, month, day] = value.split('-').map(Number);
  if (!year || !month || !day) return value;
  return new Intl.DateTimeFormat('es-EC', { dateStyle: 'long' })
    .format(new Date(year, month - 1, day));
}

export function ecuadorToday(): string {
  const parts = partsFor(new Date());
  return `${parts['year']}-${parts['month']}-${parts['day']}`;
}

export function reservationHasFinished(
  date: string,
  startTime: string,
  durationHours: number,
  now = new Date()
): boolean {
  const normalizedTime = startTime.length === 5 ? `${startTime}:00` : startTime;
  const start = new Date(`${date}T${normalizedTime}-05:00`);
  if (Number.isNaN(start.getTime()) || !Number.isInteger(durationHours)) return false;
  return start.getTime() + durationHours * 60 * 60 * 1000 < now.getTime();
}
