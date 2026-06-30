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
  cyan: '',
  magenta: '',
  green: '',
  violet: '',
  amber: '',
  none: ''
};

const classes = computed(() => [
  'glass-panel relative overflow-hidden rounded-sm',
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
