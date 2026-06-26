<script setup lang="ts">
import GlassPanel from '@/components/app/GlassPanel.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  title: string;
  subtitle?: string;
  icon?: string;
  accent?: Accent;
  actionLabel?: string;
  flush?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  subtitle: undefined,
  icon: undefined,
  accent: 'cyan',
  actionLabel: undefined,
  flush: false
});

defineEmits<{ (e: 'action'): void }>();

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/10 text-cyan-300 shadow-[0_0_18px_oklch(0.78_0.18_195/25%)]',
  magenta: 'bg-fuchsia-500/10 text-fuchsia-300 shadow-[0_0_18px_oklch(0.68_0.22_350/25%)]',
  green: 'bg-emerald-500/10 text-emerald-300 shadow-[0_0_18px_oklch(0.75_0.18_145/25%)]',
  violet: 'bg-violet-500/10 text-violet-300 shadow-[0_0_18px_oklch(0.6_0.2_290/25%)]',
  amber: 'bg-amber-500/10 text-amber-300 shadow-[0_0_18px_oklch(0.8_0.16_85/25%)]'
};
</script>

<template>
  <GlassPanel class="flex flex-col">
    <header class="flex items-center justify-between gap-3 px-5 pb-3 pt-4">
      <div class="flex min-w-0 items-center gap-3">
        <span
          v-if="props.icon"
          class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl"
          :class="accentChip[props.accent]"
        >
          <AppIcon :name="props.icon" class="h-5 w-5" />
        </span>
        <div class="min-w-0">
          <h2 class="truncate text-[0.95rem] font-semibold text-foreground">{{ props.title }}</h2>
          <p v-if="props.subtitle" class="truncate text-xs text-muted-foreground">{{ props.subtitle }}</p>
        </div>
      </div>

      <div class="flex shrink-0 items-center gap-2">
        <slot name="header-actions" />
        <button
          v-if="props.actionLabel"
          type="button"
          class="inline-flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-medium text-cyan-300 transition-colors hover:bg-cyan-500/10"
          @click="$emit('action')"
        >
          {{ props.actionLabel }}
          <AppIcon name="chevron-right" class="h-3.5 w-3.5" />
        </button>
      </div>
    </header>

    <div :class="props.flush ? '' : 'px-5 pb-5'">
      <slot />
    </div>
  </GlassPanel>
</template>
