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
  cyan: 'bg-gradient-to-r from-cyan-400 to-cyan-500 shadow-[0_0_10px_oklch(0.78_0.18_195/45%)]',
  magenta: 'bg-gradient-to-r from-fuchsia-400 to-fuchsia-500 shadow-[0_0_10px_oklch(0.68_0.22_350/45%)]',
  green: 'bg-gradient-to-r from-emerald-400 to-emerald-500 shadow-[0_0_10px_oklch(0.75_0.18_145/45%)]',
  violet: 'bg-gradient-to-r from-violet-400 to-violet-500 shadow-[0_0_10px_oklch(0.62_0.2_290/45%)]',
  amber: 'bg-gradient-to-r from-amber-300 to-amber-400 shadow-[0_0_10px_oklch(0.8_0.16_85/45%)]'
};
</script>

<template>
  <div class="w-full overflow-hidden rounded-full bg-white/[0.06]" :style="{ height: `${props.height}px` }">
    <div
      class="h-full rounded-full transition-[width] duration-500"
      :class="fill[props.accent]"
      :style="{ width: `${Math.min(100, Math.max(0, props.value))}%` }"
    />
  </div>
</template>
