<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';
import { areaPath, makeYScale, monotonePath, niceTicks } from './charts/chartMath';
import { useElementSize } from './charts/useElementSize';

interface StreamPoint {
  t: string;
  uplink: number;
  downlink: number;
}

const UP = 'oklch(0.78 0.18 195)';
const DN = 'oklch(0.75 0.18 145)';

function generateInitial(): StreamPoint[] {
  return Array.from({ length: 24 }, (_, i) => {
    const noise1 = Math.abs(Math.sin(i * 12.9898) * 43758.5453) % 1;
    const noise2 = Math.abs(Math.sin(i * 78.233) * 12543.123) % 1;
    return {
      t: `${String(i).padStart(2, '0')}:00`,
      uplink: 40 + Math.sin(i * 0.6) * 18 + noise1 * 8,
      downlink: 60 + Math.cos(i * 0.4) * 25 + noise2 * 10
    };
  });
}

const data = ref<StreamPoint[]>(generateInitial());
let timer: ReturnType<typeof setInterval> | undefined;

const latest = computed(() => data.value[data.value.length - 1]);

const plotRef = ref<HTMLDivElement | null>(null);
const { width, height } = useElementSize(plotRef);

const M = { top: 12, right: 14, bottom: 22, left: 40 };

const layout = computed(() => {
  const w = width.value;
  const h = height.value;
  const n = data.value.length;
  const plotLeft = M.left;
  const plotRight = w - M.right;
  const plotTop = M.top;
  const plotBottom = h - M.bottom;
  const plotW = Math.max(0, plotRight - plotLeft);

  const xAt = (i: number) => plotLeft + (n > 1 ? (i / (n - 1)) * plotW : 0);

  const all = data.value.flatMap((d) => [d.uplink, d.downlink]);
  // Areas fill to a 0 baseline, so anchor the domain at 0 (matches Recharts AreaChart).
  const axis = niceTicks(0, Math.max(...all));
  const y = makeYScale(axis.min, axis.max, plotTop, plotBottom);

  const upPts = data.value.map((d, i) => ({ x: xAt(i), y: y(d.uplink) }));
  const dnPts = data.value.map((d, i) => ({ x: xAt(i), y: y(d.downlink) }));

  return {
    w, h, plotLeft, plotRight, plotTop, plotBottom, xAt, axis, y,
    upLine: monotonePath(upPts),
    dnLine: monotonePath(dnPts),
    upArea: areaPath(upPts, plotBottom),
    dnArea: areaPath(dnPts, plotBottom)
  };
});

const fmt = (v: number) => (Number.isInteger(v) ? String(v) : Number(v.toFixed(1)).toString());

// --- Hover tooltip ---
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

const rows = computed(() =>
  hoverIndex.value === null
    ? []
    : [
        { key: 'downlink', color: DN, value: data.value[hoverIndex.value].downlink },
        { key: 'uplink', color: UP, value: data.value[hoverIndex.value].uplink }
      ]
);

onMounted(() => {
  timer = setInterval(() => {
    const next = data.value.slice(1);
    const lastT = parseInt(data.value[data.value.length - 1].t);
    const newT = (lastT + 1) % 24;
    next.push({
      t: `${String(newT).padStart(2, '0')}:00`,
      uplink: 40 + Math.sin(Date.now() / 4000) * 18 + Math.random() * 8,
      downlink: 60 + Math.cos(Date.now() / 3000) * 25 + Math.random() * 10
    });
    data.value = next;
  }, 3000);
});
onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <GlassCard glow="green" class="p-6">
    <div class="flex items-start justify-between mb-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <DashboardIcon name="waves" class="w-4 h-4 text-emerald-300" />
          <h3 class="text-sm font-semibold text-foreground tracking-wide">DATA STREAM</h3>
        </div>
        <p class="text-xs text-muted-foreground font-mono">Uplink / Downlink throughput</p>
      </div>
      <div class="flex items-center gap-3 text-xs font-mono">
        <div class="flex items-center gap-1.5">
          <span class="w-2 h-2 rounded-full bg-cyan-400 pulse-glow-cyan" />
          <span class="text-muted-foreground">UP</span>
          <span class="text-cyan-300 font-semibold">{{ latest?.uplink.toFixed(0) }}</span>
        </div>
        <div class="flex items-center gap-1.5">
          <span class="w-2 h-2 rounded-full bg-emerald-400 pulse-glow-green" />
          <span class="text-muted-foreground">DN</span>
          <span class="text-emerald-300 font-semibold">{{ latest?.downlink.toFixed(0) }}</span>
        </div>
      </div>
    </div>

    <div ref="plotRef" class="h-56 -ml-2 relative">
      <svg :width="layout.w" :height="layout.h" class="overflow-visible">
        <defs>
          <linearGradient id="gradUp" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="oklch(0.78 0.18 195)" stop-opacity="0.6" />
            <stop offset="100%" stop-color="oklch(0.78 0.18 195)" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="gradDn" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="oklch(0.75 0.18 145)" stop-opacity="0.6" />
            <stop offset="100%" stop-color="oklch(0.75 0.18 145)" stop-opacity="0" />
          </linearGradient>
        </defs>

        <!-- Horizontal grid -->
        <line
          v-for="tick in layout.axis.ticks"
          :key="`g-${tick}`"
          :x1="layout.plotLeft"
          :x2="layout.plotRight"
          :y1="layout.y(tick)"
          :y2="layout.y(tick)"
          stroke="oklch(0.5 0.1 280 / 12%)"
          stroke-width="1"
          stroke-dasharray="3 3"
        />

        <!-- Y axis -->
        <text
          v-for="tick in layout.axis.ticks"
          :key="`y-${tick}`"
          :x="layout.plotLeft - 6"
          :y="layout.y(tick)"
          class="chart-mono"
          fill="oklch(0.7 0.03 240 / 60%)"
          font-size="9"
          text-anchor="end"
          dominant-baseline="middle"
        >
          {{ fmt(tick) }} Mb
        </text>

        <!-- X axis labels (interval 3 → every 4th) -->
        <template v-for="(d, i) in data" :key="`x-${i}`">
          <text
            v-if="i % 4 === 0"
            :x="layout.xAt(i)"
            :y="layout.plotBottom + 14"
            class="chart-mono"
            fill="oklch(0.7 0.03 240 / 60%)"
            font-size="9"
            text-anchor="middle"
          >
            {{ d.t }}
          </text>
        </template>

        <!-- Downlink behind, uplink in front (source draw order) -->
        <path :d="layout.dnArea" fill="url(#gradDn)" />
        <path :d="layout.dnLine" fill="none" stroke="oklch(0.75 0.18 145)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        <path :d="layout.upArea" fill="url(#gradUp)" />
        <path :d="layout.upLine" fill="none" stroke="oklch(0.78 0.18 195)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />

        <!-- Hover cursor + dots -->
        <template v-if="hoverIndex !== null">
          <line :x1="layout.xAt(hoverIndex)" :x2="layout.xAt(hoverIndex)" :y1="layout.plotTop" :y2="layout.plotBottom" stroke="oklch(0.7 0.05 240 / 30%)" stroke-width="1" />
          <circle :cx="layout.xAt(hoverIndex)" :cy="layout.y(data[hoverIndex].downlink)" r="3.5" :fill="DN" stroke="white" stroke-width="1" />
          <circle :cx="layout.xAt(hoverIndex)" :cy="layout.y(data[hoverIndex].uplink)" r="3.5" :fill="UP" stroke="white" stroke-width="1" />
        </template>

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

      <div
        v-if="hoverIndex !== null"
        class="glass-panel rounded-lg p-3 text-xs chart-mono pointer-events-none absolute z-10"
        :style="tooltipStyle"
      >
        <p class="text-muted-foreground mb-2">{{ data[hoverIndex].t }}</p>
        <div v-for="r in rows" :key="r.key" class="flex items-center gap-2 mb-1">
          <span class="w-2 h-2 rounded-full" :style="{ background: r.color, boxShadow: `0 0 8px ${r.color}` }" />
          <span class="text-muted-foreground capitalize">{{ r.key }}:</span>
          <span class="text-foreground font-semibold">{{ r.value.toFixed(1) }} Mb/s</span>
        </div>
      </div>
    </div>
  </GlassCard>
</template>
