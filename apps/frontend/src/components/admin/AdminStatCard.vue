<script setup lang="ts">
/**
 * KPI tile: icon, label, animated value (CountUp) with optional unit, a trend
 * delta chip, and a sparkline. Mirrors the dashboard StatCard but uses the
 * richer AppIcon set and is study-hub flavoured.
 */
import { computed } from 'vue';
import GlassCard from '@/components/dashboard/GlassCard.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import CountUp from '@/components/app/CountUp.vue';
import Sparkline from '@/components/app/Sparkline.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

const props = withDefaults(
  defineProps<{
    label: string;
    value: number;
    prefix?: string;
    suffix?: string;
    unit?: string;
    decimals?: number;
    icon: string;
    accent?: Accent;
    /** Percentage change vs previous period. Positive = up. */
    trend?: number;
    trendLabel?: string;
    sparkline?: number[];
  }>(),
  { accent: 'cyan', prefix: '', suffix: '', unit: '', decimals: 0 }
);

const accentText: Record<Accent, string> = {
  cyan: 'text-cyan-300',
  magenta: 'text-fuchsia-300',
  green: 'text-emerald-300',
  violet: 'text-violet-300',
  amber: 'text-amber-300'
};
const accentBg: Record<Accent, string> = {
  cyan: 'bg-cyan-500/10 border-cyan-500/20',
  magenta: 'bg-fuchsia-500/10 border-fuchsia-500/20',
  green: 'bg-emerald-500/10 border-emerald-500/20',
  violet: 'bg-violet-500/10 border-violet-500/20',
  amber: 'bg-amber-500/10 border-amber-500/20'
};

const up = computed(() => (props.trend ?? 0) >= 0);
</script>

<template>
  <GlassCard :glow="accent === 'magenta' ? 'magenta' : accent === 'green' ? 'green' : 'cyan'" class="p-5">
    <div class="flex items-start justify-between">
      <span
        class="flex h-10 w-10 items-center justify-center rounded-xl border"
        :class="[accentBg[accent], accentText[accent]]"
      >
        <AppIcon :name="icon" class="h-5 w-5" />
      </span>
      <span
        v-if="trend !== undefined"
        class="flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold"
        :class="up ? 'bg-emerald-500/10 text-emerald-300' : 'bg-rose-500/10 text-rose-300'"
      >
        <AppIcon :name="up ? 'trending-up' : 'trending-down'" class="h-3.5 w-3.5" />
        {{ up ? '+' : '' }}{{ trend }}%
      </span>
    </div>

    <p class="mt-4 text-xs font-medium uppercase tracking-wider text-muted-foreground">{{ label }}</p>
    <div class="mt-1 flex items-baseline gap-1.5">
      <span class="text-2xl font-bold tabular-nums text-foreground">
        <CountUp :value="value" :prefix="prefix" :suffix="suffix" :decimals="decimals" />
      </span>
      <span v-if="unit" class="text-sm font-mono text-muted-foreground">{{ unit }}</span>
    </div>
    <p v-if="trendLabel" class="mt-0.5 text-[11px] text-muted-foreground">{{ trendLabel }}</p>

    <div v-if="sparkline?.length" class="mt-3 -mb-1">
      <Sparkline :data="sparkline" :accent="accent" :height="32" />
    </div>
  </GlassCard>
</template>
