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

const accentClass: Record<Accent, string> = {
  cyan: 'border-zinc-700 bg-zinc-900 text-zinc-100',
  magenta: 'border-zinc-700 bg-zinc-900 text-zinc-100',
  green: 'border-zinc-700 bg-zinc-900 text-zinc-100',
  violet: 'border-zinc-700 bg-zinc-900 text-zinc-100',
  amber: 'border-zinc-700 bg-zinc-900 text-zinc-100'
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
    class="inline-flex shrink-0 items-center justify-center rounded-sm border font-bold"
    :class="accentClass[props.accent]"
    :style="{ width: `${props.size}px`, height: `${props.size}px`, fontSize: `${Math.round(props.size * 0.36)}px` }"
  >
    {{ initials }}
  </span>
</template>
