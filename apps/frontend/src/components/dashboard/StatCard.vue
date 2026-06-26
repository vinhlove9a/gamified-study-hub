<script setup lang="ts">
import { computed } from 'vue';
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';

type Glow = 'cyan' | 'magenta' | 'green';

const props = withDefaults(
  defineProps<{
    label: string;
    value: string;
    unit?: string;
    icon: string;
    trend?: number;
    trendLabel?: string;
    glow?: Glow;
    sparkline?: number[];
  }>(),
  { glow: 'cyan', unit: undefined, trend: undefined, trendLabel: undefined, sparkline: undefined }
);

const glowConfig: Record<Glow, {
  iconBg: string;
  iconGlow: string;
  valueClass: string;
  lineColor: string;
  fillFrom: string;
  fillTo: string;
}> = {
  cyan: {
    iconBg: 'bg-cyan-500/10 text-cyan-300',
    iconGlow: 'shadow-[0_0_20px_oklch(0.78_0.18_195/40%)]',
    valueClass: 'glow-cyan text-cyan-50',
    lineColor: 'oklch(0.78 0.18 195)',
    fillFrom: 'oklch(0.78 0.18 195 / 30%)',
    fillTo: 'oklch(0.78 0.18 195 / 0%)'
  },
  magenta: {
    iconBg: 'bg-fuchsia-500/10 text-fuchsia-300',
    iconGlow: 'shadow-[0_0_20px_oklch(0.68_0.22_350/40%)]',
    valueClass: 'glow-magenta text-fuchsia-50',
    lineColor: 'oklch(0.68 0.22 350)',
    fillFrom: 'oklch(0.68 0.22 350 / 30%)',
    fillTo: 'oklch(0.68 0.22 350 / 0%)'
  },
  green: {
    iconBg: 'bg-emerald-500/10 text-emerald-300',
    iconGlow: 'shadow-[0_0_20px_oklch(0.75_0.18_145/40%)]',
    valueClass: 'text-emerald-50',
    lineColor: 'oklch(0.75 0.18 145)',
    fillFrom: 'oklch(0.75 0.18 145 / 30%)',
    fillTo: 'oklch(0.75 0.18 145 / 0%)'
  }
};

const config = computed(() => glowConfig[props.glow]);

// Unique gradient id per instance — two cards can share the same `glow`, so a
// `spark-${glow}` id would collide in the DOM. The counter keeps fills isolated.
const gradientId = `spark-${props.glow}-${(uid += 1)}`;

const sparkPath = computed(() => {
  const data = props.sparkline;
  if (!data || data.length <= 1) return null;
  const w = 100;
  const h = 32;
  const min = Math.min(...data);
  const max = Math.max(...data);
  const range = max - min || 1;
  const points = data.map((v, i) => {
    const x = (i / (data.length - 1)) * w;
    const y = h - ((v - min) / range) * h;
    return `${x.toFixed(2)},${y.toFixed(2)}`;
  });
  return `M${points.join(' L')}`;
});

const fillPath = computed(() => (sparkPath.value ? `${sparkPath.value} L100,32 L0,32 Z` : null));
</script>

<script lang="ts">
// Module-scoped counter shared across all StatCard instances.
let uid = 0;
</script>

<template>
  <GlassCard :glow="glow" class="p-5">
    <div class="flex items-start justify-between mb-3">
      <div
        class="w-11 h-11 rounded-xl flex items-center justify-center"
        :class="[config.iconBg, config.iconGlow]"
      >
        <DashboardIcon :name="(icon as any)" class="w-5 h-5" :stroke-width="1.8" />
      </div>
      <div
        v-if="typeof trend === 'number'"
        class="flex items-center gap-1 px-2 py-1 rounded-md text-xs font-medium"
        :class="trend >= 0 ? 'bg-emerald-500/10 text-emerald-300' : 'bg-rose-500/10 text-rose-300'"
      >
        <DashboardIcon :name="trend >= 0 ? 'trending-up' : 'trending-down'" class="w-3 h-3" />
        {{ Math.abs(trend) }}%
      </div>
    </div>

    <div class="space-y-1">
      <p class="text-xs uppercase tracking-wider text-muted-foreground font-mono">
        {{ label }}
      </p>
      <div class="flex items-baseline gap-1.5">
        <span class="text-3xl font-bold font-mono tabular-nums" :class="config.valueClass">
          {{ value }}
        </span>
        <span v-if="unit" class="text-sm text-muted-foreground font-mono">{{ unit }}</span>
      </div>
      <p v-if="trendLabel" class="text-xs text-muted-foreground">{{ trendLabel }}</p>
    </div>

    <div v-if="sparkPath" class="mt-3 h-8 relative">
      <svg viewBox="0 0 100 32" preserveAspectRatio="none" class="w-full h-full">
        <path v-if="fillPath" :d="fillPath" :fill="`url(#${gradientId})`" />
        <path
          :d="sparkPath"
          fill="none"
          :stroke="config.lineColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
          vector-effect="non-scaling-stroke"
        />
        <defs>
          <linearGradient :id="gradientId" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" :stop-color="config.fillFrom" />
            <stop offset="100%" :stop-color="config.fillTo" />
          </linearGradient>
        </defs>
      </svg>
    </div>
  </GlassCard>
</template>
