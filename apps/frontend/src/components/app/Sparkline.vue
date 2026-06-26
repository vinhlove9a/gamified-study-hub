<script setup lang="ts">
import { computed } from 'vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  data: number[];
  accent?: Accent;
  fill?: boolean;
  height?: number;
}

const props = withDefaults(defineProps<Props>(), {
  accent: 'cyan',
  fill: true,
  height: 36
});

const stroke: Record<Accent, string> = {
  cyan: 'oklch(0.78 0.18 195)',
  magenta: 'oklch(0.68 0.22 350)',
  green: 'oklch(0.75 0.18 145)',
  violet: 'oklch(0.62 0.2 290)',
  amber: 'oklch(0.8 0.16 85)'
};

// Stable per-instance gradient id (no Math.random / Date in scripts elsewhere, but
// fine in component code — kept deterministic via a module counter for safety).
const gradientId = `spark-grad-${(uid += 1)}`;

const linePath = computed(() => {
  const data = props.data;
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

const areaPath = computed(() => (linePath.value ? `${linePath.value} L100,32 L0,32 Z` : null));
</script>

<script lang="ts">
let uid = 0;
</script>

<template>
  <div class="relative w-full" :style="{ height: `${props.height}px` }">
    <svg v-if="linePath" viewBox="0 0 100 32" preserveAspectRatio="none" class="h-full w-full">
      <defs>
        <linearGradient :id="gradientId" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" :stop-color="stroke[props.accent]" stop-opacity="0.3" />
          <stop offset="100%" :stop-color="stroke[props.accent]" stop-opacity="0" />
        </linearGradient>
      </defs>
      <path v-if="props.fill && areaPath" :d="areaPath" :fill="`url(#${gradientId})`" />
      <path
        :d="linePath"
        fill="none"
        :stroke="stroke[props.accent]"
        stroke-width="1.5"
        stroke-linecap="round"
        stroke-linejoin="round"
        vector-effect="non-scaling-stroke"
      />
    </svg>
  </div>
</template>
