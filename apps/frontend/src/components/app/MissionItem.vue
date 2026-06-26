<script setup lang="ts">
import AppIcon from '@/components/app/icons/AppIcon.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Status = 'done' | 'active' | 'locked';

interface Props {
  title: string;
  description?: string;
  reward: number;
  current: number;
  total: number;
  status?: Status;
}

const props = withDefaults(defineProps<Props>(), {
  description: undefined,
  status: 'active'
});

defineEmits<{ (e: 'action'): void }>();

const pct = () => Math.min(100, Math.round((props.current / props.total) * 100));
</script>

<template>
  <div
    class="flex items-center gap-3.5 rounded-xl border border-white/[0.05] bg-white/[0.02] px-3.5 py-3 transition-colors hover:border-white/[0.1] hover:bg-white/[0.04]"
  >
    <span
      class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl"
      :class="{
        'bg-emerald-500/15 text-emerald-300 shadow-[0_0_16px_oklch(0.75_0.18_145/30%)]': props.status === 'done',
        'bg-cyan-500/15 text-cyan-300 shadow-[0_0_16px_oklch(0.78_0.18_195/30%)]': props.status === 'active',
        'bg-white/5 text-muted-foreground': props.status === 'locked'
      }"
    >
      <AppIcon
        :name="props.status === 'done' ? 'check-circle' : props.status === 'locked' ? 'lock' : 'target'"
        class="h-5 w-5"
      />
    </span>

    <div class="min-w-0 flex-1">
      <div class="flex items-center gap-2">
        <p class="truncate text-sm font-semibold text-foreground" :class="{ 'line-through opacity-50': props.status === 'done' }">
          {{ props.title }}
        </p>
        <span class="inline-flex shrink-0 items-center gap-0.5 rounded-full bg-amber-500/10 px-1.5 py-0.5 text-[0.7rem] font-semibold text-amber-300">
          <AppIcon name="coin" class="h-3 w-3" /> +{{ props.reward }}
        </span>
      </div>
      <p v-if="props.description" class="mt-0.5 truncate text-xs text-muted-foreground">{{ props.description }}</p>

      <div class="mt-2 flex items-center gap-2">
        <ProgressBar :value="pct()" :accent="props.status === 'done' ? 'green' : 'cyan'" class="flex-1" />
        <span class="shrink-0 text-[0.7rem] tabular-nums text-muted-foreground">{{ props.current }}/{{ props.total }}</span>
      </div>
    </div>

    <button
      v-if="props.status === 'done'"
      type="button"
      class="shrink-0 rounded-lg bg-emerald-500/15 px-3 py-1.5 text-xs font-semibold text-emerald-300 transition-colors hover:bg-emerald-500/25"
      @click="$emit('action')"
    >
      Nhận
    </button>
    <button
      v-else-if="props.status === 'active'"
      type="button"
      class="shrink-0 rounded-lg border border-white/10 px-3 py-1.5 text-xs font-semibold text-foreground transition-colors hover:border-cyan-400/50 hover:text-cyan-300"
      @click="$emit('action')"
    >
      Tiếp tục
    </button>
    <span v-else class="shrink-0 text-xs font-medium text-muted-foreground">Đã khoá</span>
  </div>
</template>
