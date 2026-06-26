<script setup lang="ts">
import { computed } from 'vue';
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';

interface TrackedObject {
  angle: number;
  distance: number;
  name: string;
  color: string;
  size: number;
}

const objects: TrackedObject[] = [
  { angle: 30, distance: 35, name: 'SAT-01', color: 'oklch(0.78 0.18 195)', size: 3 },
  { angle: 110, distance: 55, name: 'SAT-02', color: 'oklch(0.75 0.18 145)', size: 2.5 },
  { angle: 200, distance: 25, name: 'ISS', color: 'oklch(0.68 0.22 350)', size: 4 },
  { angle: 280, distance: 70, name: 'DEB-12', color: 'oklch(0.72 0.2 90)', size: 2 },
  { angle: 75, distance: 80, name: 'SAT-03', color: 'oklch(0.6 0.25 290)', size: 2.5 },
  { angle: 340, distance: 45, name: 'SAT-04', color: 'oklch(0.78 0.18 195)', size: 3 }
];

const rings = [25, 50, 75, 95];

const pad = (deg: number) => String(deg).padStart(3, '0');

const bearings = computed(() =>
  [0, 90, 180, 270].map((deg) => {
    const rad = (deg * Math.PI) / 180;
    return { deg, x: 100 + Math.cos(rad) * 92, y: 100 + Math.sin(rad) * 92 };
  })
);

const placed = computed(() =>
  objects.map((o) => {
    const rad = (o.angle * Math.PI) / 180;
    return { ...o, x: 100 + Math.cos(rad) * o.distance, y: 100 + Math.sin(rad) * o.distance };
  })
);
</script>

<template>
  <GlassCard glow="cyan" class="p-6">
    <div class="flex items-center justify-between mb-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <DashboardIcon name="radar" class="w-4 h-4 text-cyan-300" />
          <h3 class="text-sm font-semibold text-foreground tracking-wide">ORBITAL RADAR</h3>
        </div>
        <p class="text-xs text-muted-foreground font-mono">LEO · Real-time track · {{ objects.length }} objects</p>
      </div>
      <div class="flex items-center gap-1.5 px-2 py-1 rounded-md bg-emerald-500/10 text-emerald-300 text-[10px] font-mono uppercase tracking-wider">
        <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 pulse-glow-green" />
        TRACKING
      </div>
    </div>

    <div class="relative aspect-square max-w-[260px] mx-auto">
      <svg viewBox="0 0 200 200" class="w-full h-full absolute inset-0">
        <defs>
          <radialGradient id="radarGlow" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stop-color="oklch(0.78 0.18 195 / 30%)" />
            <stop offset="60%" stop-color="oklch(0.78 0.18 195 / 5%)" />
            <stop offset="100%" stop-color="transparent" />
          </radialGradient>
          <linearGradient id="sweepGrad" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="oklch(0.78 0.18 195)" stop-opacity="0" />
            <stop offset="100%" stop-color="oklch(0.78 0.18 195)" stop-opacity="0.5" />
          </linearGradient>
        </defs>

        <!-- Background glow -->
        <circle cx="100" cy="100" r="95" fill="url(#radarGlow)" />

        <!-- Concentric circles -->
        <circle
          v-for="r in rings"
          :key="r"
          cx="100"
          cy="100"
          :r="r"
          fill="none"
          stroke="oklch(0.78 0.18 195 / 18%)"
          stroke-width="0.5"
          :stroke-dasharray="r === 95 ? '0' : '2 2'"
        />

        <!-- Cross-hairs -->
        <line x1="5" y1="100" x2="195" y2="100" stroke="oklch(0.78 0.18 195 / 15%)" stroke-width="0.4" />
        <line x1="100" y1="5" x2="100" y2="195" stroke="oklch(0.78 0.18 195 / 15%)" stroke-width="0.4" />

        <!-- Bearing marks -->
        <text
          v-for="b in bearings"
          :key="b.deg"
          :x="b.x"
          :y="b.y"
          fill="oklch(0.7 0.05 240 / 60%)"
          font-size="6"
          font-family="monospace"
          text-anchor="middle"
          dominant-baseline="middle"
        >
          {{ pad(b.deg) }}°
        </text>

        <!-- Sweep line group - rotating -->
        <g style="transform-origin: 100px 100px; animation: rotate-slow 6s linear infinite">
          <path d="M 100 100 L 195 100 A 95 95 0 0 0 100 5 Z" fill="url(#sweepGrad)" opacity="0.6" />
          <line x1="100" y1="100" x2="195" y2="100" stroke="oklch(0.78 0.18 195)" stroke-width="1" opacity="0.8" />
        </g>

        <!-- Center Earth -->
        <circle cx="100" cy="100" r="10" fill="oklch(0.4 0.15 220)" stroke="oklch(0.78 0.18 195 / 50%)" stroke-width="0.5" />
        <circle cx="100" cy="100" r="10" fill="url(#radarGlow)" opacity="0.6" />

        <!-- Tracked objects -->
        <g v-for="(o, i) in placed" :key="i">
          <circle :cx="o.x" :cy="o.y" :r="o.size + 4" :fill="o.color" opacity="0.15" />
          <circle :cx="o.x" :cy="o.y" :r="o.size" :fill="o.color" :style="{ filter: `drop-shadow(0 0 4px ${o.color})` }" />
          <text
            :x="o.x"
            :y="o.y - o.size - 2"
            fill="oklch(0.85 0.05 240)"
            font-size="5"
            font-family="monospace"
            text-anchor="middle"
            opacity="0.7"
          >
            {{ o.name }}
          </text>
        </g>
      </svg>
    </div>

    <!-- Object legend -->
    <div class="mt-4 grid grid-cols-2 gap-1.5 text-[10px] font-mono">
      <div v-for="o in objects.slice(0, 4)" :key="o.name" class="flex items-center gap-1.5">
        <span class="w-1.5 h-1.5 rounded-full" :style="{ background: o.color, boxShadow: `0 0 4px ${o.color}` }" />
        <span class="text-muted-foreground">{{ o.name }}</span>
        <span class="text-foreground/60 ml-auto">{{ pad(o.angle) }}°</span>
      </div>
    </div>
  </GlassCard>
</template>
