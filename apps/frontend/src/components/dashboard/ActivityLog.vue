<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import GlassCard from './GlassCard.vue';
import DashboardIcon from './icons/DashboardIcon.vue';

type LogType = 'tx' | 'rx' | 'alert' | 'sys';
type Level = 'info' | 'warn' | 'critical';

interface LogEntry {
  id: number;
  time: string;
  type: LogType;
  source: string;
  message: string;
  level: Level;
}

const templates: Omit<LogEntry, 'id' | 'time'>[] = [
  { type: 'tx', source: 'DSN-14 Canberra', message: 'Uplink confirmed · 2.4 GB packet to ARTEMIS-VII', level: 'info' },
  { type: 'rx', source: 'ISS Live Feed', message: 'Telemetry batch received · 18,432 frames', level: 'info' },
  { type: 'rx', source: 'Voyager-2', message: 'Signal acquired · -118 dBm · bit rate 160 bps', level: 'info' },
  { type: 'alert', source: 'Thermal Monitor', message: 'Radiator panel 3 temp +4.2°C above nominal', level: 'warn' },
  { type: 'sys', source: 'Navigation', message: 'Course correction Δv = 0.014 m/s applied', level: 'info' },
  { type: 'tx', source: 'DSN-43 Goldstone', message: 'Handshake complete · X-band locked', level: 'info' },
  { type: 'rx', source: 'Starlink G9-41', message: 'Deployment confirmed · 22/22 sats nominal', level: 'info' },
  { type: 'alert', source: 'Solar Activity', message: 'M2.4 class flare detected · geomagnetic storm watch', level: 'critical' },
  { type: 'rx', source: 'Mars Recon Orbiter', message: 'HiRISE image captured · 0.3 m/pixel resolution', level: 'info' },
  { type: 'sys', source: 'Power Systems', message: 'Battery charge cycled · 94% → 96%', level: 'info' },
  { type: 'tx', source: 'DSN-63 Madrid', message: 'Command sequence uploaded · 1,204 instructions', level: 'info' },
  { type: 'alert', source: 'Micrometeoroid', message: 'Impact detected on shield array S4 · no damage', level: 'warn' }
];

const typeConfig: Record<LogType, { icon: string; className: string; label: string }> = {
  tx: { icon: 'arrow-up-right', className: 'text-cyan-300 bg-cyan-500/10', label: 'TX' },
  rx: { icon: 'arrow-down-right', className: 'text-emerald-300 bg-emerald-500/10', label: 'RX' },
  alert: { icon: 'alert-triangle', className: 'text-amber-300 bg-amber-500/10', label: 'AL' },
  sys: { icon: 'radio', className: 'text-fuchsia-300 bg-fuchsia-500/10', label: 'SY' }
};

const levelConfig: Record<Level, string> = {
  info: 'text-muted-foreground',
  warn: 'text-amber-300/80',
  critical: 'text-rose-300/90'
};

const stamp = (d: number) =>
  new Date(d).toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });

const logs = ref<LogEntry[]>([]);
let timer: ReturnType<typeof setInterval> | undefined;

onMounted(() => {
  const seedNow = Date.now();
  logs.value = templates.slice(0, 8).map((t, i) => ({ ...t, id: seedNow - i, time: stamp(seedNow - i * 47000) }));

  timer = setInterval(() => {
    const template = templates[Math.floor(Math.random() * templates.length)];
    const newLog: LogEntry = { ...template, id: Date.now(), time: stamp(Date.now()) };
    logs.value = [newLog, ...logs.value].slice(0, 20);
  }, 4500);
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <GlassCard class="p-6">
    <div class="flex items-center justify-between mb-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <DashboardIcon name="radio" class="w-4 h-4 text-cyan-300" />
          <h3 class="text-sm font-semibold text-foreground tracking-wide">COMM LOG</h3>
          <span class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-300 text-[10px] font-mono uppercase tracking-wider">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 pulse-glow-green" />
            STREAMING
          </span>
        </div>
        <p class="text-xs text-muted-foreground font-mono">Deep space network activity</p>
      </div>
      <span class="text-[10px] font-mono text-muted-foreground">{{ logs.length }} events</span>
    </div>

    <div class="space-y-2 max-h-[420px] overflow-y-auto cosmic-scroll pr-1">
      <div
        v-for="(log, idx) in logs"
        :key="log.id"
        class="flex items-start gap-3 p-2.5 rounded-lg bg-white/[0.02] border border-white/[0.03]"
        :class="idx === 0 ? 'border-cyan-500/30 bg-cyan-500/[0.03] scan-line' : ''"
      >
        <div class="w-7 h-7 rounded-md flex items-center justify-center shrink-0" :class="typeConfig[log.type].className">
          <DashboardIcon :name="(typeConfig[log.type].icon as any)" class="w-3.5 h-3.5" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-0.5">
            <span class="text-[10px] font-mono text-muted-foreground tabular-nums">{{ log.time }}</span>
            <span class="text-[10px] font-mono text-cyan-200/70">{{ log.source }}</span>
          </div>
          <p class="text-xs leading-snug" :class="levelConfig[log.level]">{{ log.message }}</p>
        </div>
        <span
          class="text-[9px] font-mono font-bold tracking-wider shrink-0 px-1.5 py-0.5 rounded"
          :class="typeConfig[log.type].className"
        >
          {{ typeConfig[log.type].label }}
        </span>
      </div>
    </div>
  </GlassCard>
</template>
