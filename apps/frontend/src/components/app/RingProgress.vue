<script setup lang="ts">
import { computed } from 'vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  value: number;
  size?: number;
  stroke?: number;
  accent?: Accent;
}

const props = withDefaults(defineProps<Props>(), {
  size: 96,
  stroke: 9,
  accent: 'cyan'
});

const color: Record<Accent, string> = {
  cyan: 'oklch(0.78 0.18 195)',
  magenta: 'oklch(0.68 0.22 350)',
  green: 'oklch(0.75 0.18 145)',
  violet: 'oklch(0.62 0.2 290)',
  amber: 'oklch(0.8 0.16 85)'
};

const radius = computed(() => (props.size - props.stroke) / 2);
const circumference = computed(() => 2 * Math.PI * radius.value);
const offset = computed(() => circumference.value * (1 - Math.min(100, Math.max(0, props.value)) / 100));
const center = computed(() => props.size / 2);
</script>

<template>
  <div class="relative shrink-0" :style="{ width: `${props.size}px`, height: `${props.size}px` }">
    <svg :width="props.size" :height="props.size" class="-rotate-90">
      <circle
        :cx="center"
        :cy="center"
        :r="radius"
        fill="none"
        stroke="oklch(0.3 0.03 280 / 50%)"
        :stroke-width="props.stroke"
      />
      <circle
        :cx="center"
        :cy="center"
        :r="radius"
        fill="none"
        :stroke="color[props.accent]"
        :stroke-width="props.stroke"
        stroke-linecap="round"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="offset"
        :style="{ filter: `drop-shadow(0 0 6px ${color[props.accent]})`, transition: 'stroke-dashoffset 0.6s ease' }"
      />
    </svg>
    <div class="absolute inset-0 flex flex-col items-center justify-center">
      <slot />
    </div>
  </div>
</template>
