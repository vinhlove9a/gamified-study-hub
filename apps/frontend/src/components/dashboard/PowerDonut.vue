<script setup lang="ts">
import { computed, ref } from 'vue';
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';

interface Segment {
  name: string;
  value: number;
  color: string;
  icon: string;
}

const powerData: Segment[] = [
  { name: 'Solar Array', value: 42, color: 'oklch(0.72 0.2 90)', icon: 'sun' },
  { name: 'Battery', value: 28, color: 'oklch(0.78 0.18 195)', icon: 'battery' },
  { name: 'RTG', value: 18, color: 'oklch(0.68 0.22 350)', icon: 'zap' },
  { name: 'Auxiliary', value: 12, color: 'oklch(0.75 0.18 145)', icon: 'cpu' }
];

const total = powerData.reduce((sum, d) => sum + d.value, 0);

const CX = 100;
const CY = 100;
const RI = 56;
const RO = 78;
const CR = 4; // cornerRadius
const PAD = 3; // paddingAngle in degrees
const VIEW = 200;

// Inject alpha into an oklch literal (the source's `${color}20` hex suffix was invalid
// CSS; this reproduces the intended ~12% tinted icon background).
const withAlpha = (color: string, alpha: string) => color.replace(')', ` / ${alpha})`);

const pt = (r: number, a: number) => ({ x: CX + r * Math.sin(a), y: CY - r * Math.cos(a) });

/** Annular sector with rounded corners — the geometry Recharts' Pie produced from
 *  innerRadius/outerRadius + cornerRadius + paddingAngle. */
function segmentPath(a0: number, a1: number): string {
  const r0 = (a0 * Math.PI) / 180;
  const r1 = (a1 * Math.PI) / 180;
  const span = r1 - r0;
  const dOut = Math.min(CR / RO, span / 2 - 0.001);
  const dIn = Math.min(CR / RI, span / 2 - 0.001);

  const oS = pt(RO, r0 + dOut);
  const oE = pt(RO, r1 - dOut);
  const iE = pt(RI, r1 - dIn);
  const iS = pt(RI, r0 + dIn);
  const oSEdge = pt(RO - CR, r0);
  const oEEdge = pt(RO - CR, r1);
  const iEEdge = pt(RI + CR, r1);
  const iSEdge = pt(RI + CR, r0);
  const large = span > Math.PI ? 1 : 0;

  return [
    `M ${oSEdge.x.toFixed(2)} ${oSEdge.y.toFixed(2)}`,
    `A ${CR} ${CR} 0 0 1 ${oS.x.toFixed(2)} ${oS.y.toFixed(2)}`,
    `A ${RO} ${RO} 0 ${large} 1 ${oE.x.toFixed(2)} ${oE.y.toFixed(2)}`,
    `A ${CR} ${CR} 0 0 1 ${oEEdge.x.toFixed(2)} ${oEEdge.y.toFixed(2)}`,
    `L ${iEEdge.x.toFixed(2)} ${iEEdge.y.toFixed(2)}`,
    `A ${CR} ${CR} 0 0 1 ${iE.x.toFixed(2)} ${iE.y.toFixed(2)}`,
    `A ${RI} ${RI} 0 ${large} 0 ${iS.x.toFixed(2)} ${iS.y.toFixed(2)}`,
    `A ${CR} ${CR} 0 0 1 ${iSEdge.x.toFixed(2)} ${iSEdge.y.toFixed(2)}`,
    'Z'
  ].join(' ');
}

const segments = computed(() => {
  const sweepTotal = 360 - PAD * powerData.length; // angle left after inter-segment gaps
  let cursor = 0;
  return powerData.map((d) => {
    const arc = (d.value / total) * sweepTotal;
    const a0 = cursor;
    const a1 = cursor + arc;
    cursor = a1 + PAD;
    return { ...d, d: segmentPath(a0, a1) };
  });
});

const hovered = ref<Segment | null>(null);
const mouse = ref({ x: 0, y: 0 });

const onMove = (seg: Segment, e: MouseEvent) => {
  hovered.value = seg;
  const rect = (e.currentTarget as SVGElement).ownerSVGElement!.parentElement!.getBoundingClientRect();
  mouse.value = { x: e.clientX - rect.left, y: e.clientY - rect.top };
};
</script>

<template>
  <GlassCard glow="magenta" class="p-6">
    <div class="flex items-center gap-2 mb-1">
      <DashboardIcon name="zap" class="w-4 h-4 text-fuchsia-300" />
      <h3 class="text-sm font-semibold text-foreground tracking-wide">POWER DISTRIBUTION</h3>
    </div>
    <p class="text-xs text-muted-foreground font-mono mb-4">Energy source allocation</p>

    <div class="relative h-44">
      <svg :viewBox="`0 0 ${VIEW} ${VIEW}`" class="w-full h-full" @mouseleave="hovered = null">
        <defs>
          <filter id="donutGlow">
            <feGaussianBlur stdDeviation="2" result="coloredBlur" />
            <feMerge>
              <feMergeNode in="coloredBlur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>
        <path
          v-for="seg in segments"
          :key="seg.name"
          :d="seg.d"
          :fill="seg.color"
          filter="url(#donutGlow)"
          class="cursor-pointer transition-opacity"
          :style="{ opacity: hovered && hovered.name !== seg.name ? 0.55 : 1 }"
          @mousemove="onMove(seg, $event)"
        />
      </svg>

      <!-- Center label -->
      <div class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
        <span class="text-2xl font-bold font-mono glow-magenta text-fuchsia-50">{{ total }}%</span>
        <span class="text-[10px] uppercase tracking-widest text-muted-foreground font-mono">Capacity</span>
      </div>

      <!-- Tooltip -->
      <div
        v-if="hovered"
        class="glass-panel rounded-lg p-2.5 text-xs font-mono pointer-events-none absolute z-10"
        :style="{ left: `${mouse.x + 12}px`, top: `${Math.max(0, mouse.y - 12)}px` }"
      >
        <div class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full" :style="{ background: hovered.color, boxShadow: `0 0 6px ${hovered.color}` }" />
          <span class="text-muted-foreground">{{ hovered.name }}:</span>
          <span class="text-foreground font-semibold">{{ hovered.value }}%</span>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-2 gap-2 mt-4">
      <div
        v-for="entry in powerData"
        :key="entry.name"
        class="flex items-center gap-2 p-2 rounded-lg bg-white/[0.02] border border-white/[0.04]"
      >
        <div
          class="w-7 h-7 rounded-md flex items-center justify-center shrink-0"
          :style="{ background: withAlpha(entry.color, '12%'), color: entry.color }"
        >
          <DashboardIcon :name="(entry.icon as any)" class="w-3.5 h-3.5" />
        </div>
        <div class="min-w-0">
          <p class="text-[10px] text-muted-foreground uppercase tracking-wide truncate">{{ entry.name }}</p>
          <p class="text-sm font-mono font-semibold" :style="{ color: entry.color }">{{ entry.value }}%</p>
        </div>
      </div>
    </div>
  </GlassCard>
</template>
