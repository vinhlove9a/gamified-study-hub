<script setup lang="ts">
import { computed } from 'vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  name: string;
  size?: number;
  accent?: Accent;
}

const props = withDefaults(defineProps<Props>(), {
  size: 36,
  accent: 'cyan'
});

const gradient: Record<Accent, string> = {
  cyan: 'from-cyan-400 to-blue-500',
  magenta: 'from-fuchsia-400 to-purple-500',
  green: 'from-emerald-400 to-teal-500',
  violet: 'from-violet-400 to-indigo-500',
  amber: 'from-amber-300 to-orange-500'
};

const initials = computed(() =>
  props.name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p.charAt(0).toUpperCase())
    .join('')
);
</script>

<template>
  <span
    class="inline-flex shrink-0 items-center justify-center rounded-full bg-gradient-to-br font-bold text-white"
    :class="gradient[props.accent]"
    :style="{ width: `${props.size}px`, height: `${props.size}px`, fontSize: `${Math.round(props.size * 0.36)}px` }"
  >
    {{ initials }}
  </span>
</template>
