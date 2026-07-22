import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ShareService {
  async share(title: string, url: string): Promise<'shared' | 'copied'> {
    if (navigator.share) {
      await navigator.share({ title, url });
      return 'shared';
    }
    await navigator.clipboard.writeText(url);
    return 'copied';
  }
}
