<script setup lang="ts">
import GlassPanel from '@/components/app/GlassPanel.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import Sparkline from '@/components/app/Sparkline.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  label: string;
  value: string;
  unit?: string;
  icon: string;
  accent?: Accent;
  trend?: number;
  caption?: string;
  sparkline?: number[];
}

const props = withDefaults(defineProps<Props>(), {
  unit: undefined,
  accent: 'cyan',
  trend: undefined,
  caption: undefined,
  sparkline: undefined
});

const iconChip: Record<Accent, string> = {
  cyan: 'border-zinc-800 bg-zinc-950 text-primary',
  magenta: 'border-zinc-800 bg-zinc-950 text-primary',
  green: 'border-zinc-800 bg-zinc-950 text-primary',
  violet: 'border-zinc-800 bg-zinc-950 text-primary',
  amber: 'border-zinc-800 bg-zinc-950 text-primary'
};

const valueGlow: Record<Accent, string> = {
  cyan: 'text-zinc-50',
  magenta: 'text-zinc-50',
  green: 'text-zinc-50',
  violet: 'text-zinc-50',
  amber: 'text-zinc-50'
};
</script>

<template>
  <GlassPanel :glow="props.accent" hover padded>
    <div class="mb-3 flex items-start justify-between">
      <span class="flex h-11 w-11 items-center justify-center rounded-sm border" :class="iconChip[props.accent]">
        <AppIcon :name="props.icon" class="h-5 w-5" />
      </span>
      <span
        v-if="typeof props.trend === 'number'"
        class="inline-flex items-center gap-1 rounded-sm border border-zinc-800 px-2 py-1 text-xs font-medium text-zinc-400"
      >
        <AppIcon :name="props.trend >= 0 ? 'trending-up' : 'trending-down'" class="h-3 w-3" />
        {{ Math.abs(props.trend) }}%
      </span>
    </div>

    <p class="text-[0.7rem] font-medium uppercase tracking-wider text-muted-foreground">{{ props.label }}</p>
    <p class="mt-1 flex items-baseline gap-1.5">
      <span class="text-2xl font-bold tabular-nums" :class="valueGlow[props.accent]">{{ props.value }}</span>
      <span v-if="props.unit" class="text-sm text-muted-foreground">{{ props.unit }}</span>
    </p>
    <p v-if="props.caption" class="mt-1 text-xs text-muted-foreground">{{ props.caption }}</p>

    <Sparkline v-if="props.sparkline" :data="props.sparkline" :accent="props.accent" class="mt-3" :height="32" />
  </GlassPanel>
</template>
