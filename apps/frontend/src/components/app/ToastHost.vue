<script setup lang="ts">
import { useToast, type ToastAccent } from '@/composables/useToast';
import AppIcon from '@/components/app/icons/AppIcon.vue';

const { toasts, dismiss } = useToast();

const accentClass: Record<ToastAccent, string> = {
  cyan: 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200',
  magenta: 'border-fuchsia-400/40 bg-fuchsia-500/15 text-fuchsia-200',
  green: 'border-emerald-400/40 bg-emerald-500/15 text-emerald-200',
  violet: 'border-violet-400/40 bg-violet-500/15 text-violet-200',
  amber: 'border-amber-400/40 bg-amber-500/15 text-amber-200',
  rose: 'border-rose-400/40 bg-rose-500/15 text-rose-200'
};
</script>

<template>
  <div
    class="pointer-events-none fixed bottom-5 right-5 z-[130] flex w-[min(92vw,22rem)] flex-col gap-2.5"
  >
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="glass-panel pointer-events-auto flex items-center gap-3 rounded-xl border px-4 py-3 shadow-2xl"
        :class="accentClass[t.accent]"
        role="status"
      >
        <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/10">
          <AppIcon :name="t.icon" class="h-4 w-4" />
        </span>
        <p class="flex-1 text-sm font-medium text-foreground">{{ t.message }}</p>
        <button
          type="button"
          class="shrink-0 rounded-md p-1 text-muted-foreground transition-colors hover:text-foreground"
          aria-label="Đóng"
          @click="dismiss(t.id)"
        >
          <AppIcon name="x" class="h-4 w-4" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active {
  transition:
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.35s ease;
}
.toast-leave-active {
  transition:
    transform 0.25s ease,
    opacity 0.25s ease;
  position: absolute;
  right: 0;
  width: 100%;
}
.toast-enter-from {
  transform: translateX(120%);
  opacity: 0;
}
.toast-leave-to {
  transform: translateX(120%);
  opacity: 0;
}
.toast-move {
  transition: transform 0.3s ease;
}
</style>
