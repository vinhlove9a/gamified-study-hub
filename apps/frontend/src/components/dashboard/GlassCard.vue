<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    glow?: 'cyan' | 'magenta' | 'green' | 'none';
    hover?: boolean;
  }>(),
  { glow: 'none', hover: true }
);

const glowOverlay: Record<string, string> = {
  cyan: 'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-cyan-500/5 before:to-transparent before:pointer-events-none',
  magenta:
    'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-fuchsia-500/5 before:to-transparent before:pointer-events-none',
  green:
    'before:absolute before:inset-0 before:rounded-2xl before:bg-gradient-to-br before:from-emerald-500/5 before:to-transparent before:pointer-events-none',
  none: ''
};

const classes = computed(() => [
  'glass-panel rounded-2xl relative overflow-hidden',
  props.hover ? 'glass-panel-hover' : '',
  glowOverlay[props.glow]
]);
</script>

<template>
  <div :class="classes">
    <slot />
  </div>
</template>
