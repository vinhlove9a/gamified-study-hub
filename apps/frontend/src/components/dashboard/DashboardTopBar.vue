<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import DashboardIcon from './icons/DashboardIcon.vue';

type PillColor = 'emerald' | 'cyan' | 'magenta';

const now = ref<Date | null>(null);
const utc = ref<Date | null>(null);
let timer: ReturnType<typeof setInterval> | undefined;

const formatTime = (d: Date) =>
  d.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });

const formatDate = (d: Date) =>
  d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: '2-digit' });

const pills: { icon: string; label: string; value: string; color: PillColor; pulse?: boolean }[] = [
  { icon: 'satellite', label: 'SAT LINK', value: '42/42', color: 'emerald', pulse: true },
  { icon: 'wifi', label: 'DSN', value: '3 ONLINE', color: 'cyan', pulse: true },
  { icon: 'cpu', label: 'CPU LOAD', value: '34%', color: 'cyan' },
  { icon: 'battery', label: 'PWR', value: '96%', color: 'emerald' }
];

const colorMap: Record<PillColor, string> = {
  emerald: 'text-emerald-300 bg-emerald-500/10 border-emerald-500/20',
  cyan: 'text-cyan-300 bg-cyan-500/10 border-cyan-500/20',
  magenta: 'text-fuchsia-300 bg-fuchsia-500/10 border-fuchsia-500/20'
};
const dotColor: Record<PillColor, string> = {
  emerald: 'bg-emerald-400 pulse-glow-green',
  cyan: 'bg-cyan-400 pulse-glow-cyan',
  magenta: 'bg-fuchsia-400 pulse-glow-magenta'
};

onMounted(() => {
  const update = () => {
    const d = new Date();
    now.value = d;
    // Shift by the local timezone offset to render wall-clock UTC time.
    utc.value = new Date(d.getTime() + d.getTimezoneOffset() * 60000);
  };
  update();
  timer = setInterval(update, 1000);
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <header class="sticky top-0 z-30 backdrop-blur-xl bg-background/60 border-b border-white/[0.05]">
    <div class="px-4 md:px-8 py-3 flex items-center justify-between gap-4">
      <!-- Logo + mission name -->
      <div class="flex items-center gap-3 min-w-0">
        <div class="relative w-10 h-10 shrink-0">
          <div class="absolute inset-0 rounded-full bg-gradient-to-br from-cyan-500/20 to-fuchsia-500/20 border border-cyan-500/30" />
          <div class="absolute inset-1.5 rounded-full bg-gradient-to-br from-cyan-400 to-fuchsia-500 shadow-[0_0_20px_oklch(0.78_0.18_195/50%)]" />
          <div class="absolute inset-0 orbit-ring rotate-slow" />
        </div>
        <div class="min-w-0">
          <h1 class="text-base font-bold text-foreground leading-tight tracking-wide">
            <span class="glow-cyan text-cyan-100">DEEP</span>
            <span class="text-fuchsia-200">WORKSPACE</span>
          </h1>
          <p class="text-[10px] text-muted-foreground font-mono uppercase tracking-wider truncate">
            Mission Control · Orbit View
          </p>
        </div>
      </div>

      <!-- System status pills - hidden on mobile -->
      <div class="hidden lg:flex items-center gap-2">
        <div
          v-for="pill in pills"
          :key="pill.label"
          class="flex items-center gap-2 px-2.5 py-1.5 rounded-lg border"
          :class="colorMap[pill.color]"
        >
          <DashboardIcon :name="(pill.icon as any)" class="w-3.5 h-3.5" />
          <div class="flex flex-col leading-tight">
            <span class="text-[9px] uppercase tracking-wider text-muted-foreground font-mono">
              {{ pill.label }}
            </span>
            <span class="text-xs font-mono font-semibold">{{ pill.value }}</span>
          </div>
          <span v-if="pill.pulse" class="w-1.5 h-1.5 rounded-full" :class="dotColor[pill.color]" />
        </div>
      </div>

      <!-- Right cluster -->
      <div class="flex items-center gap-2">
        <!-- Live UTC clock -->
        <div class="hidden md:flex flex-col items-end px-3 py-1.5 rounded-lg bg-white/[0.03] border border-white/[0.05]">
          <span class="text-[9px] uppercase tracking-widest text-muted-foreground font-mono">UTC</span>
          <span class="text-sm font-mono font-semibold text-cyan-200 tabular-nums leading-tight">
            {{ utc ? formatTime(utc) : '--:--:--' }}
          </span>
        </div>

        <!-- Local clock -->
        <div class="hidden sm:flex flex-col items-end px-3 py-1.5 rounded-lg bg-white/[0.03] border border-white/[0.05]">
          <span class="text-[9px] uppercase tracking-widest text-muted-foreground font-mono">
            LOCAL{{ now ? ` · ${formatDate(now).split(',')[0]}` : '' }}
          </span>
          <span class="text-sm font-mono font-semibold text-foreground tabular-nums leading-tight">
            {{ now ? formatTime(now) : '--:--:--' }}
          </span>
        </div>

        <!-- Search button -->
        <button class="w-9 h-9 rounded-lg bg-white/[0.03] hover:bg-white/[0.06] border border-white/[0.05] text-muted-foreground hover:text-foreground flex items-center justify-center transition-colors">
          <DashboardIcon name="search" class="w-4 h-4" />
        </button>

        <!-- Notifications -->
        <button class="relative w-9 h-9 rounded-lg bg-white/[0.03] hover:bg-white/[0.06] border border-white/[0.05] text-muted-foreground hover:text-foreground flex items-center justify-center transition-colors">
          <DashboardIcon name="bell" class="w-4 h-4" />
          <span class="absolute top-1.5 right-1.5 w-1.5 h-1.5 rounded-full bg-fuchsia-400 pulse-glow-magenta" />
        </button>

        <!-- User avatar -->
        <button class="flex items-center gap-2 pl-2 pr-1 py-1 rounded-lg bg-white/[0.03] hover:bg-white/[0.06] border border-white/[0.05] transition-colors">
          <div class="hidden xl:block text-left">
            <p class="text-xs font-medium text-foreground leading-tight">Cmdr. R. Vega</p>
            <p class="text-[10px] text-muted-foreground font-mono">FLIGHT DIRECTOR</p>
          </div>
          <div class="w-7 h-7 rounded-full bg-gradient-to-br from-cyan-500 to-fuchsia-500 flex items-center justify-center text-xs font-bold text-white shrink-0">
            RV
          </div>
          <DashboardIcon name="chevron-down" class="w-3 h-3 text-muted-foreground hidden xl:block" />
        </button>
      </div>
    </div>
  </header>
</template>
