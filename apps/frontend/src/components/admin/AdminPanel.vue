<script setup lang="ts">
/**
 * Glass surface with a standard header (icon + title + optional subtitle) and an
 * `actions` slot on the right. The body is the default slot. Used as the base
 * container for every admin section card so spacing/typography stay consistent.
 */
import GlassCard from '@/components/dashboard/GlassCard.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

withDefaults(
  defineProps<{
    title?: string;
    subtitle?: string;
    icon?: string;
    accent?: Accent;
    glow?: 'cyan' | 'magenta' | 'green' | 'none';
    /** Remove inner padding when the body manages its own (e.g. a full-bleed table). */
    flush?: boolean;
  }>(),
  { accent: 'cyan', glow: 'none', flush: false }
);

const accentText: Record<Accent, string> = {
  cyan: 'text-cyan-300',
  magenta: 'text-fuchsia-300',
  green: 'text-emerald-300',
  violet: 'text-violet-300',
  amber: 'text-amber-300'
};
const accentBg: Record<Accent, string> = {
  cyan: 'bg-cyan-500/10 border-cyan-500/20',
  magenta: 'bg-fuchsia-500/10 border-fuchsia-500/20',
  green: 'bg-emerald-500/10 border-emerald-500/20',
  violet: 'bg-violet-500/10 border-violet-500/20',
  amber: 'bg-amber-500/10 border-amber-500/20'
};
</script>

<template>
  <GlassCard :glow="glow" class="flex flex-col">
    <header
      v-if="title || $slots.actions || $slots.header"
      class="flex items-start justify-between gap-3 px-5 pt-5"
      :class="flush ? 'pb-4' : ''"
    >
      <slot name="header">
        <div class="flex min-w-0 items-center gap-3">
          <span
            v-if="icon"
            class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border"
            :class="[accentBg[accent], accentText[accent]]"
          >
            <AppIcon :name="icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0">
            <h3 class="truncate text-sm font-semibold tracking-wide text-foreground">{{ title }}</h3>
            <p v-if="subtitle" class="mt-0.5 truncate text-xs text-muted-foreground">{{ subtitle }}</p>
          </div>
        </div>
      </slot>
      <div v-if="$slots.actions" class="flex shrink-0 items-center gap-2">
        <slot name="actions" />
      </div>
    </header>

    <div class="min-w-0 flex-1" :class="flush ? '' : 'p-5'">
      <slot />
    </div>
  </GlassCard>
</template>
