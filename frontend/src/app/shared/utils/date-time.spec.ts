import { reservationHasFinished } from './date-time';

describe('reservationHasFinished', () => {
  it('enables reviews only after the reserved duration has elapsed in Ecuador', () => {
    const eventDate = '2026-07-22';
    const startTime = '18:00';

    expect(reservationHasFinished(eventDate, startTime, 3, new Date('2026-07-23T02:00:00Z')))
      .toBe(false);
    expect(reservationHasFinished(eventDate, startTime, 3, new Date('2026-07-23T02:01:00Z')))
      .toBe(true);
  });
});
