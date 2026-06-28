<script setup lang="ts">
/**
 * Small status / label pill used across admin tables (active, banned, pending…).
 * `tone` picks the colour; `dot` shows a leading status dot.
 */
type Tone = 'cyan' | 'green' | 'amber' | 'rose' | 'violet' | 'magenta' | 'neutral';

withDefaults(defineProps<{ tone?: Tone; dot?: boolean }>(), { tone: 'neutral', dot: false });

const tones: Record<Tone, string> = {
  cyan: 'bg-cyan-500/10 text-cyan-300 border-cyan-500/20',
  green: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/20',
  amber: 'bg-amber-500/10 text-amber-300 border-amber-500/20',
  rose: 'bg-rose-500/10 text-rose-300 border-rose-500/20',
  violet: 'bg-violet-500/10 text-violet-300 border-violet-500/20',
  magenta: 'bg-fuchsia-500/10 text-fuchsia-300 border-fuchsia-500/20',
  neutral: 'bg-white/[0.05] text-muted-foreground border-white/10'
};
const dotColor: Record<Tone, string> = {
  cyan: 'bg-cyan-400',
  green: 'bg-emerald-400',
  amber: 'bg-amber-400',
  rose: 'bg-rose-400',
  violet: 'bg-violet-400',
  magenta: 'bg-fuchsia-400',
  neutral: 'bg-white/40'
};
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 whitespace-nowrap rounded-full border px-2.5 py-0.5 text-[11px] font-medium"
    :class="tones[tone]"
  >
    <span v-if="dot" class="h-1.5 w-1.5 rounded-full" :class="dotColor[tone]" />
    <slot />
  </span>
</template>
