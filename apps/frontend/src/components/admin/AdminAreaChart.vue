<script setup lang="ts">
/**
 * Responsive multi-series area + line chart with a hover crosshair tooltip.
 * Pure-SVG, built on the shared chartMath helpers + useElementSize — no chart
 * library. Mirrors the technique used by the dashboard DataStreamChart so it
 * matches the cosmic theme. Data is static (mock) but the layout is reactive.
 */
import { computed, ref } from 'vue';
import { areaPath, makeYScale, monotonePath, niceTicks } from '@/components/dashboard/charts/chartMath';
import { useElementSize } from '@/components/dashboard/charts/useElementSize';

interface Series {
  key: string;
  name: string;
  /** Any CSS colour — typically an oklch() accent. */
  color: string;
  values: number[];
  /** Fill the area under the line (default true for the first series only). */
  area?: boolean;
}

const props = withDefaults(
  defineProps<{
    labels: string[];
    series: Series[];
    height?: number;
    /** Anchor the y-domain at 0 (good for counts / revenue). */
    zeroBased?: boolean;
    /** Number formatting for the tooltip + axis. */
    decimals?: number;
    prefix?: string;
    suffix?: string;
  }>(),
  { height: 240, zeroBased: true, decimals: 0, prefix: '', suffix: '' }
);

const plotRef = ref<HTMLDivElement | null>(null);
const { width } = useElementSize(plotRef);
const H = computed(() => props.height);

const M = { top: 14, right: 16, bottom: 24, left: 44 };

const layout = computed(() => {
  const w = width.value;
  const h = H.value;
  const n = props.labels.length;
  const plotLeft = M.left;
  const plotRight = w - M.right;
  const plotTop = M.top;
  const plotBottom = h - M.bottom;
  const plotW = Math.max(0, plotRight - plotLeft);

  const xAt = (i: number) => plotLeft + (n > 1 ? (i / (n - 1)) * plotW : 0);

  const all = props.series.flatMap((s) => s.values);
  const lo = props.zeroBased ? 0 : Math.min(...all);
  const axis = niceTicks(lo, Math.max(...all, 1));
  const y = makeYScale(axis.min, axis.max, plotTop, plotBottom);

  const built = props.series.map((s, idx) => {
    const pts = s.values.map((v, i) => ({ x: xAt(i), y: y(v) }));
    return {
      ...s,
      area: s.area ?? idx === 0,
      line: monotonePath(pts),
      areaPath: areaPath(pts, plotBottom),
      pts
    };
  });

  return { w, h, plotLeft, plotRight, plotTop, plotBottom, xAt, axis, y, built };
});

const fmt = (v: number) =>
  `${props.prefix}${Number(v.toFixed(props.decimals)).toLocaleString('vi-VN')}${props.suffix}`;

// hover
const hoverIndex = ref<number | null>(null);
const mouseX = ref(0);
function onMove(e: MouseEvent) {
  const l = layout.value;
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
  const x = e.clientX - rect.left;
  mouseX.value = x;
  const n = props.labels.length;
  const ratio = (x - l.plotLeft) / Math.max(1, l.plotRight - l.plotLeft);
  hoverIndex.value = Math.max(0, Math.min(n - 1, Math.round(ratio * (n - 1))));
}
function onLeave() {
  hoverIndex.value = null;
}

const tipStyle = computed(() => {
  const flip = mouseX.value > width.value - 160;
  return {
    left: `${mouseX.value + (flip ? -12 : 12)}px`,
    transform: flip ? 'translateX(-100%)' : 'none'
  };
});

const uid = Math.random().toString(36).slice(2, 8);
</script>

<template>
  <div>
    <!-- Legend -->
    <div class="mb-2 flex flex-wrap items-center gap-x-4 gap-y-1">
      <span v-for="s in series" :key="s.key" class="flex items-center gap-1.5 text-xs text-muted-foreground">
        <span class="h-2 w-2 rounded-full" :style="{ background: s.color }" />
        {{ s.name }}
      </span>
    </div>

    <div ref="plotRef" class="relative w-full" :style="{ height: `${H}px` }">
      <svg :width="layout.w" :height="layout.h" class="overflow-visible" @mousemove="onMove" @mouseleave="onLeave">
        <defs>
          <linearGradient v-for="s in layout.built" :id="`grad-${uid}-${s.key}`" :key="s.key" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" :stop-color="s.color" stop-opacity="0.28" />
            <stop offset="100%" :stop-color="s.color" stop-opacity="0" />
          </linearGradient>
        </defs>

        <!-- gridlines -->
        <g>
          <line
            v-for="t in layout.axis.ticks"
            :key="t"
            :x1="layout.plotLeft"
            :x2="layout.plotRight"
            :y1="layout.y(t)"
            :y2="layout.y(t)"
            stroke="oklch(0.7 0.1 280 / 12%)"
            stroke-width="1"
          />
          <text
            v-for="t in layout.axis.ticks"
            :key="`l-${t}`"
            :x="layout.plotLeft - 8"
            :y="layout.y(t) + 3"
            text-anchor="end"
            class="fill-current font-mono text-[9px] text-muted-foreground"
          >
            {{ Number(t).toLocaleString('vi-VN') }}
          </text>
        </g>

        <!-- areas + lines -->
        <g v-for="s in layout.built" :key="s.key">
          <path v-if="s.area" :d="s.areaPath" :fill="`url(#grad-${uid}-${s.key})`" />
          <path :d="s.line" fill="none" :stroke="s.color" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </g>

        <!-- x labels -->
        <text
          v-for="(lab, i) in labels"
          :key="`x-${i}`"
          :x="layout.xAt(i)"
          :y="layout.h - 6"
          text-anchor="middle"
          class="fill-current font-mono text-[9px] text-muted-foreground"
          :style="{ opacity: i % Math.ceil(labels.length / 8) === 0 ? 0.8 : 0 }"
        >
          {{ lab }}
        </text>

        <!-- hover crosshair + dots -->
        <g v-if="hoverIndex !== null">
          <line
            :x1="layout.xAt(hoverIndex)"
            :x2="layout.xAt(hoverIndex)"
            :y1="layout.plotTop"
            :y2="layout.plotBottom"
            stroke="oklch(0.78 0.18 195 / 40%)"
            stroke-width="1"
            stroke-dasharray="3 3"
          />
          <circle
            v-for="s in layout.built"
            :key="`d-${s.key}`"
            :cx="layout.xAt(hoverIndex)"
            :cy="s.pts[hoverIndex].y"
            r="3.5"
            :fill="s.color"
            stroke="oklch(0.08 0.02 280)"
            stroke-width="1.5"
          />
        </g>
      </svg>

      <!-- tooltip -->
      <div
        v-if="hoverIndex !== null"
        class="glass-panel pointer-events-none absolute top-2 z-10 rounded-xl px-3 py-2"
        :style="tipStyle"
      >
        <p class="mb-1 font-mono text-[10px] uppercase tracking-wider text-muted-foreground">{{ labels[hoverIndex] }}</p>
        <div v-for="s in series" :key="s.key" class="flex items-center justify-between gap-4 text-xs">
          <span class="flex items-center gap-1.5 text-muted-foreground">
            <span class="h-1.5 w-1.5 rounded-full" :style="{ background: s.color }" />
            {{ s.name }}
          </span>
          <span class="font-mono font-semibold tabular-nums text-foreground">{{ fmt(s.values[hoverIndex]) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
