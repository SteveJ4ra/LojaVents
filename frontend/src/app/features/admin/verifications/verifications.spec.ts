import { of } from 'rxjs';
import { vi } from 'vitest';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { OwnerVerificationRequest } from '../../../shared/models/user.model';
import { Verifications } from './verifications';

describe('Verifications', () => {
  it('does not approve a request when the optional comment prompt is cancelled', () => {
    const reviewOwnerRequest = vi.fn(() => of({} as OwnerVerificationRequest));
    const component = new Verifications(
      { reviewOwnerRequest } as unknown as UserService,
      { show: vi.fn() } as unknown as NotificationService
    );
    const request = { id: 'request-1' } as OwnerVerificationRequest;
    const promptSpy = vi.spyOn(window, 'prompt').mockReturnValue(null);

    component.approve(request);

    expect(reviewOwnerRequest).not.toHaveBeenCalled();
    expect(component.reviewingId()).toBeNull();
    promptSpy.mockRestore();
  });
});
