<script setup lang="ts">
import AppIcon from '@/components/app/icons/AppIcon.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Tone = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  title: string;
  category: string;
  meta: string;
  readMinutes?: number;
  progress?: number;
  coins?: number;
  rating?: number;
  locked?: boolean;
  bookmarked?: boolean;
  tone?: Tone;
}

const props = withDefaults(defineProps<Props>(), {
  readMinutes: undefined,
  progress: undefined,
  coins: undefined,
  rating: undefined,
  locked: false,
  bookmarked: false,
  tone: 'cyan'
});

const cover: Record<Tone, string> = {
  cyan: 'border-zinc-800 bg-zinc-900',
  magenta: 'border-zinc-800 bg-zinc-900',
  green: 'border-zinc-800 bg-zinc-900',
  violet: 'border-zinc-800 bg-zinc-900',
  amber: 'border-zinc-800 bg-zinc-900'
};
</script>

<template>
  <article
    class="group flex flex-col overflow-hidden rounded-sm border border-zinc-800 bg-zinc-950 transition-colors hover:border-zinc-700"
  >
    <div class="relative h-24 border-b" :class="cover[props.tone]">
      <span class="absolute left-3 top-3 text-white/90">
        <AppIcon name="file-text" class="h-6 w-6" />
      </span>
      <button
        type="button"
        class="absolute right-2.5 top-2.5 flex h-7 w-7 items-center justify-center rounded-sm border border-zinc-700 bg-zinc-950 text-zinc-300 transition-colors hover:text-primary"
        :aria-label="props.bookmarked ? 'Đã lưu' : 'Lưu tài liệu'"
      >
        <AppIcon name="bookmark" class="h-4 w-4" :class="props.bookmarked ? 'text-primary' : ''" />
      </button>
      <span
        v-if="props.locked"
        class="absolute bottom-2 right-2.5 inline-flex items-center gap-1 rounded-sm border border-zinc-700 bg-zinc-950 px-1.5 py-0.5 text-[0.65rem] font-semibold text-zinc-300"
      >
        <AppIcon name="crown" class="h-3 w-3" /> VIP
      </span>
    </div>

    <div class="flex flex-1 flex-col p-3.5">
      <span class="text-[0.7rem] font-semibold uppercase tracking-wide text-zinc-500">{{ props.category }}</span>
      <h3 class="mt-1 line-clamp-2 text-sm font-semibold leading-snug text-foreground">{{ props.title }}</h3>
      <p class="mt-1 text-xs text-muted-foreground">{{ props.meta }}</p>

      <div v-if="props.progress !== undefined" class="mt-3">
        <ProgressBar :value="props.progress" accent="cyan" />
        <p class="mt-1 text-[0.7rem] text-muted-foreground">{{ props.progress }}% hoàn thành</p>
      </div>

      <div class="mt-auto flex items-center justify-between gap-2 pt-3">
        <div class="flex items-center gap-2.5">
          <span v-if="props.readMinutes" class="inline-flex items-center gap-1 text-xs text-muted-foreground">
            <AppIcon name="clock" class="h-3.5 w-3.5" /> {{ props.readMinutes }}p
          </span>
          <span v-if="props.rating" class="inline-flex items-center gap-1 text-xs text-zinc-400">
            <AppIcon name="star" class="h-3.5 w-3.5" /> {{ props.rating }}
          </span>
        </div>
        <span
          v-if="props.coins"
          class="inline-flex items-center gap-1 rounded-sm border border-zinc-800 px-2 py-0.5 text-xs font-semibold text-zinc-400"
        >
          <AppIcon name="coin" class="h-3.5 w-3.5" /> {{ props.coins }}
        </span>
      </div>
    </div>
  </article>
</template>
