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
  cyan: 'border-zinc-800 bg-zinc-950 text-primary',
  magenta: 'border-zinc-800 bg-zinc-950 text-primary',
  green: 'border-zinc-800 bg-zinc-950 text-primary',
  violet: 'border-zinc-800 bg-zinc-950 text-primary',
  amber: 'border-zinc-800 bg-zinc-950 text-primary'
};
</script>

<template>
  <GlassPanel class="flex flex-col">
    <header class="flex items-center justify-between gap-3 px-5 pb-3 pt-4">
      <div class="flex min-w-0 items-center gap-3">
        <span
          v-if="props.icon"
          class="flex h-9 w-9 shrink-0 items-center justify-center rounded-sm border"
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
          class="inline-flex items-center gap-1 rounded-sm border border-zinc-800 px-2 py-1 text-xs font-medium text-zinc-300 transition-colors hover:border-primary hover:text-primary"
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
