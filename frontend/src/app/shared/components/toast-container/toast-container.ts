import { Component } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="toast-stack" aria-live="polite">
      @for (item of notifications.notifications(); track item.id) {
        <button class="toast" [class]="item.kind" (click)="notifications.dismiss(item.id)">
          {{ item.message }}
        </button>
      }
    </div>
  `,
  styles: [`
    .toast-stack { position:fixed; top:82px; right:20px; z-index:1000; display:grid; gap:10px; width:min(360px, calc(100vw - 40px)); }
    .toast { border:0; color:white; border-radius:12px; padding:14px 16px; text-align:left; box-shadow:var(--shadow-md); cursor:pointer; font:inherit; }
    .success { background:#166534; }
    .error { background:#991b1b; }
    .info { background:#1e3a8a; }
  `]
})
export class ToastContainer {
  constructor(readonly notifications: NotificationService) {}
}
