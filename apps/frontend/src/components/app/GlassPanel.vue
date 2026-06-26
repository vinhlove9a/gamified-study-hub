<script setup lang="ts">
import { computed } from 'vue';

type Glow = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber' | 'none';

interface Props {
  glow?: Glow;
  hover?: boolean;
  padded?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  glow: 'none',
  hover: false,
  padded: false
});

const glowOverlay: Record<Glow, string> = {
  cyan: 'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-cyan-500/[0.07] before:to-transparent before:pointer-events-none',
  magenta:
    'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-fuchsia-500/[0.07] before:to-transparent before:pointer-events-none',
  green:
    'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-emerald-500/[0.07] before:to-transparent before:pointer-events-none',
  violet:
    'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-violet-500/[0.08] before:to-transparent before:pointer-events-none',
  amber:
    'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-amber-500/[0.07] before:to-transparent before:pointer-events-none',
  none: ''
};

const classes = computed(() => [
  'glass-panel relative overflow-hidden rounded-2xl',
  props.hover ? 'glass-panel-hover' : '',
  props.padded ? 'p-5' : '',
  glowOverlay[props.glow]
]);
</script>

<template>
  <div :class="classes">
    <slot />
  </div>
</template>
