<script setup lang="ts">
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';

type Status = 'active' | 'standby' | 'cruise' | 'docking';

interface Mission {
  id: string;
  name: string;
  vehicle: string;
  status: Status;
  progress: number;
  eta: string;
  icon: string;
}

const missions: Mission[] = [
  { id: 'M-2451', name: 'ARTEMIS-VII Lunar Resupply', vehicle: 'SLS Block 1B', status: 'cruise', progress: 67, eta: 'T+ 04:22:18', icon: 'rocket' },
  { id: 'M-2448', name: 'Starlink G9-41 Deployment', vehicle: 'Falcon 9 B1082', status: 'active', progress: 89, eta: 'T+ 00:14:52', icon: 'satellite' },
  { id: 'M-2447', name: 'Cygnus NG-23 Docking', vehicle: 'Antares 330', status: 'docking', progress: 94, eta: 'T+ 00:02:11', icon: 'orbit' },
  { id: 'M-2445', name: 'Deep Space Comms Relay', vehicle: 'Vulcan Centaur', status: 'standby', progress: 23, eta: 'T+ 18:45:00', icon: 'radio' }
];

const statusConfig: Record<Status, { label: string; className: string; dot: string }> = {
  active: { label: 'ACTIVE', className: 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30', dot: 'bg-emerald-400 pulse-glow-green' },
  cruise: { label: 'CRUISE', className: 'bg-cyan-500/10 text-cyan-300 border-cyan-500/30', dot: 'bg-cyan-400 pulse-glow-cyan' },
  docking: { label: 'DOCKING', className: 'bg-fuchsia-500/10 text-fuchsia-300 border-fuchsia-500/30', dot: 'bg-fuchsia-400 pulse-glow-magenta' },
  standby: { label: 'STANDBY', className: 'bg-amber-500/10 text-amber-300 border-amber-500/30', dot: 'bg-amber-400' }
};
</script>

<template>
  <GlassCard class="p-6 col-span-1 lg:col-span-2">
    <div class="flex items-center justify-between mb-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <DashboardIcon name="rocket" class="w-4 h-4 text-cyan-300" />
          <h3 class="text-sm font-semibold text-foreground tracking-wide">ACTIVE OPERATIONS</h3>
        </div>
        <p class="text-xs text-muted-foreground font-mono">{{ missions.length }} missions in progress</p>
      </div>
      <button class="text-xs px-3 py-1.5 rounded-lg bg-white/[0.03] hover:bg-white/[0.06] text-muted-foreground hover:text-foreground font-mono transition-colors">
        VIEW ALL →
      </button>
    </div>

    <div class="space-y-3">
      <div
        v-for="m in missions"
        :key="m.id"
        class="group p-4 rounded-xl bg-white/[0.02] border border-white/[0.04] hover:border-cyan-500/30 hover:bg-cyan-500/[0.02] transition-all"
      >
        <div class="flex items-start gap-3 mb-3">
          <div class="w-10 h-10 rounded-lg bg-cyan-500/10 text-cyan-300 flex items-center justify-center shrink-0 shadow-[0_0_16px_oklch(0.78_0.18_195/30%)]">
            <DashboardIcon :name="(m.icon as any)" class="w-4 h-4" :stroke-width="1.8" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-0.5">
              <span class="text-[10px] font-mono text-muted-foreground">{{ m.id }}</span>
              <span
                class="flex items-center gap-1.5 px-1.5 py-0.5 rounded border text-[9px] font-mono font-semibold tracking-wider"
                :class="statusConfig[m.status].className"
              >
                <span class="w-1 h-1 rounded-full" :class="statusConfig[m.status].dot" />
                {{ statusConfig[m.status].label }}
              </span>
            </div>
            <h4 class="text-sm font-medium text-foreground truncate group-hover:text-cyan-200 transition-colors">
              {{ m.name }}
            </h4>
            <p class="text-xs text-muted-foreground font-mono mt-0.5">{{ m.vehicle }}</p>
          </div>
          <div class="text-right shrink-0">
            <p class="text-[10px] uppercase tracking-wider text-muted-foreground font-mono">ETA</p>
            <p class="text-xs font-mono text-cyan-200 font-semibold">{{ m.eta }}</p>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <!-- Progress track + gradient fill (replaces shadcn <Progress>). -->
          <div class="h-1.5 flex-1 rounded-full bg-white/[0.05] overflow-hidden">
            <div
              class="h-full rounded-full bg-gradient-to-r from-cyan-500 to-fuchsia-500 transition-all"
              :style="{ width: `${m.progress}%` }"
            />
          </div>
          <span class="text-xs font-mono text-muted-foreground tabular-nums w-9 text-right">{{ m.progress }}%</span>
        </div>
      </div>
    </div>
  </GlassCard>
</template>
