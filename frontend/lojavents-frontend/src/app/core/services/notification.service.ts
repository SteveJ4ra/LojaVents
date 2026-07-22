import { Injectable, signal } from '@angular/core';

export type NotificationKind = 'success' | 'error' | 'info';

export interface AppNotification {
  id: string;
  message: string;
  kind: NotificationKind;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  readonly notifications = signal<AppNotification[]>([]);

  show(message: string, kind: NotificationKind = 'info', duration = 3500): void {
    const item: AppNotification = { id: crypto.randomUUID(), message, kind };
    this.notifications.update(items => [...items, item]);
    window.setTimeout(() => this.dismiss(item.id), duration);
  }

  dismiss(id: string): void {
    this.notifications.update(items => items.filter(item => item.id !== id));
  }
}
