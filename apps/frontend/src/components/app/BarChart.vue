<script setup lang="ts">
import { computed } from 'vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Bar {
  label: string;
  value: number;
  active?: boolean;
}

interface Props {
  data: Bar[];
  accent?: Accent;
  height?: number;
}

const props = withDefaults(defineProps<Props>(), {
  accent: 'cyan',
  height: 120
});

const max = computed(() => Math.max(1, ...props.data.map((d) => d.value)));

const activeBar: Record<Accent, string> = {
  cyan: 'bg-gradient-to-t from-cyan-500/80 to-cyan-300 shadow-[0_0_12px_oklch(0.78_0.18_195/50%)]',
  magenta: 'bg-gradient-to-t from-fuchsia-500/80 to-fuchsia-300 shadow-[0_0_12px_oklch(0.68_0.22_350/50%)]',
  green: 'bg-gradient-to-t from-emerald-500/80 to-emerald-300 shadow-[0_0_12px_oklch(0.75_0.18_145/50%)]',
  violet: 'bg-gradient-to-t from-violet-500/80 to-violet-300 shadow-[0_0_12px_oklch(0.62_0.2_290/50%)]',
  amber: 'bg-gradient-to-t from-amber-500/80 to-amber-300 shadow-[0_0_12px_oklch(0.8_0.16_85/50%)]'
};
</script>

<template>
  <div class="flex items-end justify-between gap-2" :style="{ height: `${props.height}px` }">
    <div v-for="bar in props.data" :key="bar.label" class="flex flex-1 flex-col items-center justify-end gap-1.5">
      <div class="flex w-full flex-1 items-end justify-center">
        <div
          class="w-full max-w-[20px] rounded-md transition-all duration-500"
          :class="bar.active ? activeBar[props.accent] : 'bg-white/[0.08]'"
          :style="{ height: `${Math.max(6, (bar.value / max) * 100)}%` }"
          :title="`${bar.value}`"
        />
      </div>
      <span class="text-[0.7rem]" :class="bar.active ? 'font-semibold text-cyan-300' : 'text-muted-foreground'">
        {{ bar.label }}
      </span>
    </div>
  </div>
</template>
