<script setup lang="ts">
type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  value: number;
  accent?: Accent;
  height?: number;
}

const props = withDefaults(defineProps<Props>(), {
  accent: 'cyan',
  height: 6
});

const fill: Record<Accent, string> = {
  cyan: 'bg-primary',
  magenta: 'bg-primary',
  green: 'bg-primary',
  violet: 'bg-primary',
  amber: 'bg-primary'
};
</script>

<template>
  <div class="w-full overflow-hidden border border-zinc-800 bg-zinc-950" :style="{ height: `${props.height}px` }">
    <div
      class="h-full transition-[width] duration-500"
      :class="fill[props.accent]"
      :style="{ width: `${Math.min(100, Math.max(0, props.value))}%` }"
    />
  </div>
</template>
