<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';
import { makeYScale, monotonePath, niceTicks } from './charts/chartMath';
import { useElementSize } from './charts/useElementSize';

interface TelemetryPoint {
  t: string;
  velocity: number;
  altitude: number;
  signal: number;
}

const TIME_LABELS = ['T-0', 'T+2m', 'T+4m', 'T+6m', 'T+8m', 'T+10m', 'T+12m', 'T+14m'];

const COLORS = {
  velocity: 'oklch(0.78 0.18 195)',
  altitude: 'oklch(0.68 0.22 350)',
  signal: 'oklch(0.75 0.18 145)'
};

const SERIES = [
  { key: 'velocity' as const, name: 'Velocity (km/s)', color: COLORS.velocity, dashed: false },
  { key: 'altitude' as const, name: 'Altitude (km)', color: COLORS.altitude, dashed: false },
  { key: 'signal' as const, name: 'Signal (%)', color: COLORS.signal, dashed: true }
];

function seedData(): TelemetryPoint[] {
  return TIME_LABELS.map((t, i) => {
    // Deterministic pseudo-noise so the seed is stable (kept from the source).
    const n1 = Math.abs(Math.sin(i * 12.9898) * 43758.5453) % 1;
    const n2 = Math.abs(Math.sin(i * 78.233) * 12543.123) % 1;
    const n3 = Math.abs(Math.sin(i * 39.346) * 24634.634) % 1;
    return {
      t,
      velocity: 7.6 + Math.sin(i * 0.7) * 0.3 + n1 * 0.15,
      altitude: 408 + Math.cos(i * 0.5) * 4 + n2 * 2,
      signal: 88 + Math.sin(i * 0.9) * 6 + n3 * 3
    };
  });
}

const data = ref<TelemetryPoint[]>(seedData());
const streaming = ref(true);
let timer: ReturnType<typeof setInterval> | undefined;

const startStream = () => {
  timer = setInterval(() => {
    const next = data.value.slice(1);
    next.push({
      t: TIME_LABELS[TIME_LABELS.length - 1],
      velocity: 7.6 + Math.sin(Date.now() / 5000) * 0.3 + Math.random() * 0.15,
      altitude: 408 + Math.cos(Date.now() / 7000) * 4 + Math.random() * 2,
      signal: 88 + Math.sin(Date.now() / 3000) * 6 + Math.random() * 3
    });
    data.value = next;
  }, 2500);
};

const toggleStream = () => {
  streaming.value = !streaming.value;
  if (streaming.value) startStream();
  else if (timer) clearInterval(timer);
};

const plotRef = ref<HTMLDivElement | null>(null);
const { width, height } = useElementSize(plotRef);

// Pixel layout — reserves gutters for the dual y-axes, x labels and the legend
// row (the space Recharts auto-allocated for axes/legend).
const M = { top: 12, right: 44, bottom: 22, left: 44, legend: 30 };

const layout = computed(() => {
  const w = width.value;
  const h = height.value;
  const n = data.value.length;
  const plotLeft = M.left;
  const plotRight = w - M.right;
  const plotTop = M.top;
  const plotBottom = h - M.bottom - M.legend;
  const plotW = Math.max(0, plotRight - plotLeft);

  const xAt = (i: number) => plotLeft + (n > 1 ? (i / (n - 1)) * plotW : 0);

  const velocities = data.value.map((d) => d.velocity);
  const rightVals = data.value.flatMap((d) => [d.altitude, d.signal]);
  const left = niceTicks(Math.min(...velocities), Math.max(...velocities));
  const right = niceTicks(Math.min(...rightVals), Math.max(...rightVals));
  const leftY = makeYScale(left.min, left.max, plotTop, plotBottom);
  const rightY = makeYScale(right.min, right.max, plotTop, plotBottom);

  const paths = {
    velocity: monotonePath(data.value.map((d, i) => ({ x: xAt(i), y: leftY(d.velocity) }))),
    altitude: monotonePath(data.value.map((d, i) => ({ x: xAt(i), y: rightY(d.altitude) }))),
    signal: monotonePath(data.value.map((d, i) => ({ x: xAt(i), y: rightY(d.signal) })))
  };

  return { w, h, plotLeft, plotRight, plotTop, plotBottom, xAt, left, right, leftY, rightY, paths };
});

const yFor = (key: 'velocity' | 'altitude' | 'signal', v: number) =>
  key === 'velocity' ? layout.value.leftY(v) : layout.value.rightY(v);

const fmt = (v: number) => (Number.isInteger(v) ? String(v) : Number(v.toFixed(2)).toString());

// --- Hover tooltip (replaces Recharts <Tooltip>) ---
const hoverIndex = ref<number | null>(null);
const mouse = ref({ x: 0, y: 0 });

const onMove = (e: MouseEvent) => {
  const l = layout.value;
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
  const x = e.clientX - rect.left;
  mouse.value = { x, y: e.clientY - rect.top };
  const n = data.value.length;
  const ratio = (x - l.plotLeft) / Math.max(1, l.plotRight - l.plotLeft);
  hoverIndex.value = Math.max(0, Math.min(n - 1, Math.round(ratio * (n - 1))));
};
const onLeave = () => (hoverIndex.value = null);

const tooltipStyle = computed(() => {
  const flip = mouse.value.x > width.value - 150;
  return {
    left: `${mouse.value.x + (flip ? -12 : 12)}px`,
    top: `${Math.max(0, mouse.value.y - 12)}px`,
    transform: flip ? 'translateX(-100%)' : 'none'
  };
});

onMounted(() => {
  if (streaming.value) startStream();
});
onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <GlassCard class="p-6 col-span-1 lg:col-span-2">
    <div class="flex items-start justify-between mb-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <DashboardIcon name="activity" class="w-4 h-4 text-cyan-300" />
          <h3 class="text-sm font-semibold text-foreground tracking-wide">ORBITAL TELEMETRY</h3>
          <span class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-300 text-[10px] font-mono uppercase tracking-wider">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 pulse-glow-green" />
            LIVE
          </span>
        </div>
        <p class="text-xs text-muted-foreground font-mono">ISS · Altitude · Velocity · Signal</p>
      </div>
      <button
        class="text-xs px-3 py-1.5 rounded-lg bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-300 font-mono transition-colors flex items-center gap-1.5"
        @click="toggleStream"
      >
        <DashboardIcon name="radio" class="w-3 h-3" />
        {{ streaming ? 'PAUSE' : 'RESUME' }}
      </button>
    </div>

    <div ref="plotRef" class="h-72 -ml-2 relative">
      <svg :width="layout.w" :height="layout.h" class="overflow-visible">
        <defs>
          <linearGradient id="lineVel" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="oklch(0.78 0.18 195)" stop-opacity="1" />
            <stop offset="100%" stop-color="oklch(0.78 0.18 195)" stop-opacity="0.3" />
          </linearGradient>
          <linearGradient id="lineAlt" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="oklch(0.68 0.22 350)" stop-opacity="1" />
            <stop offset="100%" stop-color="oklch(0.68 0.22 350)" stop-opacity="0.3" />
          </linearGradient>
          <linearGradient id="lineSig" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="oklch(0.75 0.18 145)" stop-opacity="1" />
            <stop offset="100%" stop-color="oklch(0.75 0.18 145)" stop-opacity="0.3" />
          </linearGradient>
        </defs>

        <!-- Horizontal grid (CartesianGrid, dashed, no verticals) -->
        <line
          v-for="tick in layout.left.ticks"
          :key="`g-${tick}`"
          :x1="layout.plotLeft"
          :x2="layout.plotRight"
          :y1="layout.leftY(tick)"
          :y2="layout.leftY(tick)"
          stroke="oklch(0.5 0.1 280 / 12%)"
          stroke-width="1"
          stroke-dasharray="3 3"
        />

        <!-- Left Y axis -->
        <line :x1="layout.plotLeft" :x2="layout.plotLeft" :y1="layout.plotTop" :y2="layout.plotBottom" stroke="oklch(0.5 0.1 280 / 15%)" stroke-width="1" />
        <text
          v-for="tick in layout.left.ticks"
          :key="`ly-${tick}`"
          :x="layout.plotLeft - 6"
          :y="layout.leftY(tick)"
          class="chart-mono"
          fill="oklch(0.7 0.03 240 / 60%)"
          font-size="10"
          text-anchor="end"
          dominant-baseline="middle"
        >
          {{ fmt(tick) }}
        </text>

        <!-- Right Y axis -->
        <text
          v-for="tick in layout.right.ticks"
          :key="`ry-${tick}`"
          :x="layout.plotRight + 6"
          :y="layout.rightY(tick)"
          class="chart-mono"
          fill="oklch(0.7 0.03 240 / 60%)"
          font-size="10"
          text-anchor="start"
          dominant-baseline="middle"
        >
          {{ fmt(tick) }}
        </text>

        <!-- X axis labels -->
        <text
          v-for="(d, i) in data"
          :key="`x-${i}`"
          :x="layout.xAt(i)"
          :y="layout.plotBottom + 14"
          class="chart-mono"
          fill="oklch(0.7 0.03 240 / 60%)"
          font-size="10"
          text-anchor="middle"
        >
          {{ d.t }}
        </text>

        <!-- Reference line (velocity 7.66) -->
        <line
          :x1="layout.plotLeft"
          :x2="layout.plotRight"
          :y1="layout.leftY(7.66)"
          :y2="layout.leftY(7.66)"
          stroke="oklch(0.78 0.18 195 / 30%)"
          stroke-width="1"
          stroke-dasharray="2 4"
        />

        <!-- Hover cursor -->
        <line
          v-if="hoverIndex !== null"
          :x1="layout.xAt(hoverIndex)"
          :x2="layout.xAt(hoverIndex)"
          :y1="layout.plotTop"
          :y2="layout.plotBottom"
          stroke="oklch(0.7 0.05 240 / 30%)"
          stroke-width="1"
        />

        <!-- Series lines. Solid lines get a one-time draw-on (pathLength normalised
             to 1); the element persists across stream updates so it animates only once. -->
        <path :d="layout.paths.velocity" fill="none" stroke="url(#lineVel)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" path-length="1" class="draw-line" />
        <path :d="layout.paths.altitude" fill="none" stroke="url(#lineAlt)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" path-length="1" class="draw-line" />
        <path :d="layout.paths.signal" fill="none" stroke="url(#lineSig)" stroke-width="2" stroke-dasharray="4 3" stroke-linecap="round" stroke-linejoin="round" />

        <!-- Active dots -->
        <template v-if="hoverIndex !== null">
          <circle :cx="layout.xAt(hoverIndex)" :cy="yFor('velocity', data[hoverIndex].velocity)" r="4" :fill="COLORS.velocity" stroke="white" stroke-width="1" />
          <circle :cx="layout.xAt(hoverIndex)" :cy="yFor('altitude', data[hoverIndex].altitude)" r="4" :fill="COLORS.altitude" stroke="white" stroke-width="1" />
          <circle :cx="layout.xAt(hoverIndex)" :cy="yFor('signal', data[hoverIndex].signal)" r="4" :fill="COLORS.signal" stroke="white" stroke-width="1" />
        </template>

        <!-- Mouse capture layer -->
        <rect
          :x="layout.plotLeft"
          :y="layout.plotTop"
          :width="Math.max(0, layout.plotRight - layout.plotLeft)"
          :height="Math.max(0, layout.plotBottom - layout.plotTop)"
          fill="transparent"
          @mousemove="onMove"
          @mouseleave="onLeave"
        />
      </svg>

      <!-- Legend -->
      <div class="absolute left-0 right-0 bottom-0 flex items-center justify-center gap-4 chart-mono" style="font-size: 11px">
        <span v-for="s in SERIES" :key="s.key" class="flex items-center gap-1.5 text-muted-foreground">
          <span class="w-2 h-2 rounded-full" :style="{ background: s.color }" />
          {{ s.name }}
        </span>
      </div>

      <!-- Tooltip -->
      <div
        v-if="hoverIndex !== null"
        class="glass-panel rounded-lg p-3 text-xs chart-mono pointer-events-none absolute z-10"
        :style="tooltipStyle"
      >
        <p class="text-muted-foreground mb-2">{{ data[hoverIndex].t }}</p>
        <div v-for="s in SERIES" :key="s.key" class="flex items-center gap-2 mb-1">
          <span class="w-2 h-2 rounded-full" :style="{ background: s.color, boxShadow: `0 0 8px ${s.color}` }" />
          <span class="text-muted-foreground capitalize">{{ s.key }}:</span>
          <span class="text-foreground font-semibold">{{ data[hoverIndex][s.key].toFixed(2) }}</span>
        </div>
      </div>
    </div>
  </GlassCard>
</template>

<style scoped>
/* One-time line draw using a normalised pathLength (set via path-length="1"). */
.draw-line {
  stroke-dasharray: 1;
  stroke-dashoffset: 1;
  animation: telemetry-draw 0.8s ease-out forwards;
}

@keyframes telemetry-draw {
  to {
    stroke-dashoffset: 0;
  }
}
</style>
