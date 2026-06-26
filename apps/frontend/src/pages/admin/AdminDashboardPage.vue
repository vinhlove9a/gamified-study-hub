<script setup lang="ts">
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import DashboardTopBar from '@/components/dashboard/DashboardTopBar.vue';
import StatCard from '@/components/dashboard/StatCard.vue';
import TelemetryChart from '@/components/dashboard/TelemetryChart.vue';
import PowerDonut from '@/components/dashboard/PowerDonut.vue';
import DataStreamChart from '@/components/dashboard/DataStreamChart.vue';
import MissionList from '@/components/dashboard/MissionList.vue';
import CelestialGrid from '@/components/dashboard/CelestialGrid.vue';
import ActivityLog from '@/components/dashboard/ActivityLog.vue';
import OrbitalRadar from '@/components/dashboard/OrbitalRadar.vue';

const stats = [
  { label: 'Active Satellites', value: '42', unit: '/ 42', icon: 'satellite', trend: 2.4, trendLabel: '2 new since last orbit', glow: 'cyan' as const, sparkline: [38, 39, 40, 39, 41, 40, 42, 42] },
  { label: 'Data Throughput', value: '184.7', unit: 'Mb/s', icon: 'gauge', trend: 12.8, trendLabel: 'Peak 24-hour average', glow: 'magenta' as const, sparkline: [120, 145, 138, 165, 158, 178, 172, 184] },
  { label: 'Orbital Velocity', value: '7.66', unit: 'km/s', icon: 'orbit', trend: -0.3, trendLabel: 'Within nominal range', glow: 'green' as const, sparkline: [7.72, 7.68, 7.65, 7.69, 7.66, 7.64, 7.67, 7.66] },
  { label: 'Core Temp', value: '-156', unit: '°C', icon: 'thermometer', trend: 1.1, trendLabel: 'Thermal control stable', glow: 'cyan' as const, sparkline: [-158, -157, -156, -158, -155, -156, -157, -156] }
];
</script>

<template>
  <div class="cosmic-dashboard relative min-h-screen flex flex-col">
    <!-- Animated cosmic background -->
    <StarfieldCanvas />

    <!-- Top bar -->
    <DashboardTopBar />

    <!-- Main content -->
    <main class="flex-1 px-4 md:px-8 py-6 space-y-6 relative z-10">
      <!-- Hero KPI row -->
      <section>
        <div class="flex items-baseline justify-between mb-4">
          <div>
            <h2 class="text-xl md:text-2xl font-bold text-foreground tracking-tight">Mission Overview</h2>
            <p class="text-sm text-muted-foreground font-mono mt-0.5">
              Real-time spacecraft telemetry · Last sync 2s ago
            </p>
          </div>
          <div class="hidden md:flex items-center gap-2 text-[10px] font-mono uppercase tracking-widest text-muted-foreground">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 pulse-glow-green" />
            ALL SYSTEMS NOMINAL
          </div>
        </div>

        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            v-for="stat in stats"
            :key="stat.label"
            :label="stat.label"
            :value="stat.value"
            :unit="stat.unit"
            :icon="stat.icon"
            :trend="stat.trend"
            :trend-label="stat.trendLabel"
            :glow="stat.glow"
            :sparkline="stat.sparkline"
          />
        </div>
      </section>

      <!-- Charts row 1: Telemetry (2 cols) + Power Donut (1 col) -->
      <section class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <TelemetryChart />
        <PowerDonut />
      </section>

      <!-- Charts row 2: Data stream (2 cols) + Orbital radar (1 col) -->
      <section class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <DataStreamChart />
        <OrbitalRadar />
      </section>

      <!-- Operations row: Missions (2 cols) + Celestial grid (1 col) -->
      <section class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <MissionList />
        <CelestialGrid />
      </section>

      <!-- Final row: Activity log full width -->
      <section class="grid grid-cols-1 gap-4">
        <ActivityLog />
      </section>
    </main>

    <!-- Footer -->
    <footer class="mt-auto px-4 md:px-8 py-4 border-t border-white/[0.05] backdrop-blur-sm bg-background/40 relative z-10">
      <div class="flex flex-col md:flex-row items-center justify-between gap-3 text-xs font-mono text-muted-foreground">
        <div class="flex items-center gap-3">
          <span class="flex items-center gap-1.5">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 pulse-glow-green" />
            MISSION CONTROL · DEEPWORKSPACE v3.14.2
          </span>
        </div>
        <div class="flex items-center gap-4">
          <span>SOL DAY · 2457</span>
          <span>·</span>
          <span>DSN LATENCY · 1.24s</span>
          <span>·</span>
          <span>SECURE LINK ESTABLISHED</span>
        </div>
      </div>
    </footer>
  </div>
</template>
