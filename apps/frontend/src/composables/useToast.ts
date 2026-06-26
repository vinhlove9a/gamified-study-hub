import { ref } from 'vue';

export type ToastAccent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber' | 'rose';

export interface Toast {
  id: number;
  message: string;
  icon: string;
  accent: ToastAccent;
}

export interface ToastOptions {
  icon?: string;
  accent?: ToastAccent;
  duration?: number;
}

/** Module-level state → every `useToast()` call shares one toast stack. */
const toasts = ref<Toast[]>([]);
let seq = 0;

function dismiss(id: number): void {
  toasts.value = toasts.value.filter((t) => t.id !== id);
}

function push(message: string, options: ToastOptions = {}): number {
  seq += 1;
  const id = seq;
  toasts.value.push({
    id,
    message,
    icon: options.icon ?? 'check-circle',
    accent: options.accent ?? 'cyan'
  });
  // Cap the stack so rapid actions don't pile up.
  if (toasts.value.length > 4) toasts.value.shift();
  window.setTimeout(() => dismiss(id), options.duration ?? 2800);
  return id;
}

export function useToast(): {
  toasts: typeof toasts;
  push: typeof push;
  dismiss: typeof dismiss;
} {
  return { toasts, push, dismiss };
}
