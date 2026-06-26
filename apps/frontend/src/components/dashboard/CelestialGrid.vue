<script setup lang="ts">
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';

type Direction = 'inbound' | 'outbound' | 'stationary' | 'transit';

interface CelestialBody {
  id: string;
  name: string;
  type: string;
  distance: string;
  velocity: string;
  direction: Direction;
  size: 'sm' | 'md' | 'lg';
  color: string;
  glowColor: string;
}

const bodies: CelestialBody[] = [
  { id: 'PL-01', name: 'Mars', type: 'Terrestrial Planet', distance: '0.524 AU', velocity: '24.13 km/s', direction: 'transit', size: 'md', color: 'oklch(0.65 0.18 35)', glowColor: 'oklch(0.65 0.18 35 / 40%)' },
  { id: 'PL-02', name: 'Europa', type: 'Icy Moon', distance: '4.201 AU', velocity: '13.74 km/s', direction: 'outbound', size: 'sm', color: 'oklch(0.82 0.05 220)', glowColor: 'oklch(0.82 0.05 220 / 40%)' },
  { id: 'AS-01', name: 'Ceres', type: 'Dwarf Planet', distance: '2.770 AU', velocity: '17.91 km/s', direction: 'stationary', size: 'sm', color: 'oklch(0.7 0.04 70)', glowColor: 'oklch(0.7 0.04 70 / 40%)' },
  { id: 'CM-01', name: '67P / Churyumov', type: 'Comet', distance: '1.243 AU', velocity: '38.5 km/s', direction: 'inbound', size: 'md', color: 'oklch(0.75 0.18 195)', glowColor: 'oklch(0.75 0.18 195 / 40%)' },
  { id: 'AS-02', name: 'Vesta', type: 'Asteroid', distance: '2.362 AU', velocity: '19.34 km/s', direction: 'transit', size: 'sm', color: 'oklch(0.6 0.05 50)', glowColor: 'oklch(0.6 0.05 50 / 40%)' },
  { id: 'PL-03', name: 'Titan', type: 'Saturnian Moon', distance: '9.539 AU', velocity: '5.57 km/s', direction: 'stationary', size: 'md', color: 'oklch(0.72 0.12 70)', glowColor: 'oklch(0.72 0.12 70 / 40%)' }
];

const directionConfig: Record<Direction, { label: string; className: string; arrow: string }> = {
  inbound: { label: 'INBOUND', className: 'text-rose-300 bg-rose-500/10', arrow: '↘' },
  outbound: { label: 'OUTBOUND', className: 'text-cyan-300 bg-cyan-500/10', arrow: '↗' },
  stationary: { label: 'STATIONARY', className: 'text-amber-300 bg-amber-500/10', arrow: '⊙' },
  transit: { label: 'TRANSIT', className: 'text-emerald-300 bg-emerald-500/10', arrow: '→' }
};

const sizeConfig = { sm: 'w-8 h-8', md: 'w-12 h-12', lg: 'w-16 h-16' } as const;

// Inject an alpha into an oklch() literal, e.g. `oklch(.. 35)` -> `oklch(.. 35 / 30%)`.
const withAlpha = (color: string, alpha: string) => color.replace(')', ` / ${alpha})`);

const planetStyle = (b: CelestialBody) => ({
  background: `radial-gradient(circle at 30% 30%, ${b.color}, ${withAlpha(b.color, '30%')})`,
  boxShadow: `0 0 16px ${b.glowColor}, inset -4px -4px 8px rgba(0,0,0,0.4)`
});

const shadowStyle = (b: CelestialBody) => ({
  background: `radial-gradient(circle at 70% 70%, transparent 40%, ${withAlpha(b.color, '60%')} 100%)`
});

const atmosphereStyle = (b: CelestialBody) => ({
  background: `radial-gradient(circle, transparent 60%, ${b.glowColor} 100%)`
});
</script>

<template>
  <GlassCard glow="magenta" class="p-6">
    <div class="flex items-center justify-between mb-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <DashboardIcon name="globe-2" class="w-4 h-4 text-fuchsia-300" />
          <h3 class="text-sm font-semibold text-foreground tracking-wide">CELESTIAL TRACKING</h3>
        </div>
        <p class="text-xs text-muted-foreground font-mono">Tracked bodies · Solar system</p>
      </div>
      <div class="flex gap-1">
        <button class="w-7 h-7 rounded-md bg-fuchsia-500/10 text-fuchsia-300 flex items-center justify-center hover:bg-fuchsia-500/20 transition-colors">
          <DashboardIcon name="telescope" class="w-3.5 h-3.5" />
        </button>
        <button class="w-7 h-7 rounded-md bg-white/[0.03] text-muted-foreground flex items-center justify-center hover:bg-white/[0.06] transition-colors">
          <DashboardIcon name="radar" class="w-3.5 h-3.5" />
        </button>
      </div>
    </div>

    <div class="space-y-2.5 max-h-[420px] overflow-y-auto cosmic-scroll pr-1">
      <div
        v-for="b in bodies"
        :key="b.id"
        class="group flex items-center gap-3 p-3 rounded-xl bg-white/[0.02] border border-white/[0.04] hover:border-fuchsia-500/30 hover:bg-fuchsia-500/[0.02] transition-all"
      >
        <!-- Mini planet visual -->
        <div class="relative shrink-0">
          <div class="rounded-full relative overflow-hidden" :class="sizeConfig[b.size]" :style="planetStyle(b)">
            <div class="absolute inset-0 rounded-full opacity-50" :style="shadowStyle(b)" />
            <div class="absolute -inset-0.5 rounded-full opacity-30" :style="atmosphereStyle(b)" />
          </div>
        </div>

        <!-- Info -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-0.5">
            <h4 class="text-sm font-medium text-foreground truncate group-hover:text-fuchsia-200 transition-colors">
              {{ b.name }}
            </h4>
            <span class="text-[9px] font-mono text-muted-foreground">{{ b.id }}</span>
          </div>
          <p class="text-[11px] text-muted-foreground font-mono">{{ b.type }}</p>
          <div class="flex items-center gap-3 mt-1.5">
            <div>
              <span class="text-[9px] uppercase tracking-wider text-muted-foreground font-mono block">Distance</span>
              <span class="text-[11px] font-mono text-cyan-200">{{ b.distance }}</span>
            </div>
            <div>
              <span class="text-[9px] uppercase tracking-wider text-muted-foreground font-mono block">Velocity</span>
              <span class="text-[11px] font-mono text-emerald-200">{{ b.velocity }}</span>
            </div>
          </div>
        </div>

        <!-- Direction badge -->
        <div
          class="shrink-0 flex flex-col items-center gap-0.5 px-2 py-1.5 rounded-lg text-[9px] font-mono font-semibold tracking-wider"
          :class="directionConfig[b.direction].className"
        >
          <span class="text-base leading-none">{{ directionConfig[b.direction].arrow }}</span>
          <span>{{ directionConfig[b.direction].label }}</span>
        </div>
      </div>
    </div>
  </GlassCard>
</template>
