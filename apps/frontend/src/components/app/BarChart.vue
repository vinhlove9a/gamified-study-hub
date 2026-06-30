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
  cyan: 'bg-primary',
  magenta: 'bg-primary',
  green: 'bg-primary',
  violet: 'bg-primary',
  amber: 'bg-primary'
};
</script>

<template>
  <div class="flex items-end justify-between gap-2" :style="{ height: `${props.height}px` }">
    <div v-for="bar in props.data" :key="bar.label" class="flex flex-1 flex-col items-center justify-end gap-1.5">
      <div class="flex w-full flex-1 items-end justify-center">
        <div
          class="w-full max-w-[20px] border border-zinc-800 transition-all duration-500"
          :class="bar.active ? activeBar[props.accent] : 'bg-zinc-800'"
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
