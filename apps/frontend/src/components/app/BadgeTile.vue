<script setup lang="ts">
import AppIcon from '@/components/app/icons/AppIcon.vue';

type Rarity = 'common' | 'rare' | 'epic' | 'legendary';

interface Props {
  name: string;
  icon: string;
  description?: string;
  unlocked?: boolean;
  rarity?: Rarity;
  progress?: number;
}

const props = withDefaults(defineProps<Props>(), {
  description: undefined,
  unlocked: false,
  rarity: 'common',
  progress: undefined
});

const rarityRing: Record<Rarity, string> = {
  common: 'from-slate-400/30 to-slate-500/20 text-slate-200',
  rare: 'from-cyan-400/40 to-blue-500/30 text-cyan-200 shadow-[0_0_22px_oklch(0.78_0.18_195/30%)]',
  epic: 'from-fuchsia-400/40 to-purple-500/30 text-fuchsia-200 shadow-[0_0_22px_oklch(0.68_0.22_350/30%)]',
  legendary: 'from-amber-300/50 to-orange-500/30 text-amber-200 shadow-[0_0_26px_oklch(0.8_0.16_85/40%)]'
};

const rarityLabel: Record<Rarity, string> = {
  common: 'Thường',
  rare: 'Hiếm',
  epic: 'Sử thi',
  legendary: 'Huyền thoại'
};
</script>

<template>
  <div
    class="flex flex-col items-center rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4 text-center transition-colors hover:border-white/[0.12]"
    :class="props.unlocked ? '' : 'opacity-70'"
  >
    <span
      class="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br"
      :class="props.unlocked ? rarityRing[props.rarity] : 'from-white/5 to-white/[0.02] text-muted-foreground'"
    >
      <AppIcon :name="props.unlocked ? props.icon : 'lock'" class="h-7 w-7" />
    </span>
    <p class="mt-2.5 line-clamp-1 text-sm font-semibold text-foreground">{{ props.name }}</p>
    <p v-if="props.description" class="mt-0.5 line-clamp-2 text-xs text-muted-foreground">{{ props.description }}</p>

    <span
      v-if="props.unlocked"
      class="mt-2 rounded-full bg-white/[0.06] px-2 py-0.5 text-[0.65rem] font-medium text-muted-foreground"
    >
      {{ rarityLabel[props.rarity] }}
    </span>
    <div v-else-if="props.progress !== undefined" class="mt-2.5 w-full">
      <div class="h-1.5 w-full overflow-hidden rounded-full bg-white/[0.06]">
        <div class="h-full rounded-full bg-cyan-500/70" :style="{ width: `${props.progress}%` }" />
      </div>
      <p class="mt-1 text-[0.65rem] text-muted-foreground">{{ props.progress }}%</p>
    </div>
  </div>
</template>
