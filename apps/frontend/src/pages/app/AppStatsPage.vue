<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import BarChart from '@/components/app/BarChart.vue';
import Tabs from '@/components/app/Tabs.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type RangeId = '7' | '30' | '90';

interface KpiDef {
  key: string;
  label: string;
  icon: string;
  accent: Accent;
  unit?: string;
  decimals: number;
}

interface RangeData {
  series: number[];
  kpi: Record<string, number>;
  trend: Record<string, number>;
  sparkline: Record<string, number[]>;
  weekday: { label: string; value: number; active?: boolean }[];
}

const rangeTabs = [
  { id: '7', label: '7 ngày' },
  { id: '30', label: '30 ngày' },
  { id: '90', label: '90 ngày' }
] as const;

const activeRange = ref<RangeId>('7');

const kpiDefs: KpiDef[] = [
  { key: 'hours', label: 'Tổng giờ học', icon: 'clock', accent: 'cyan', unit: 'giờ', decimals: 1 },
  { key: 'sessions', label: 'Số phiên', icon: 'calendar', accent: 'violet', unit: 'phiên', decimals: 0 },
  { key: 'accuracy', label: 'Độ chính xác', icon: 'target', accent: 'green', unit: '%', decimals: 0 },
  { key: 'rank', label: 'Xếp hạng', icon: 'trophy', accent: 'amber', unit: '', decimals: 0 }
];

const dataByRange: Record<RangeId, RangeData> = {
  '7': {
    series: [42, 65, 30, 78, 55, 90, 48],
    kpi: { hours: 6.8, sessions: 14, accuracy: 87, rank: 3 },
    trend: { hours: 12, sessions: 8, accuracy: 4, rank: 2 },
    sparkline: {
      hours: [3, 4, 3.5, 5, 4.5, 6, 6.8],
      sessions: [8, 9, 10, 11, 12, 13, 14],
      accuracy: [78, 80, 82, 83, 85, 86, 87],
      rank: [9, 8, 7, 6, 5, 4, 3]
    },
    weekday: [
      { label: 'T2', value: 42 },
      { label: 'T3', value: 65 },
      { label: 'T4', value: 30 },
      { label: 'T5', value: 78 },
      { label: 'T6', value: 55 },
      { label: 'T7', value: 90, active: true },
      { label: 'CN', value: 48 }
    ]
  },
  '30': {
    series: [40, 52, 35, 60, 48, 75, 55, 62, 38, 70, 58, 80, 45, 66, 52, 88, 60, 72, 50, 84, 62, 78, 55, 92, 68, 74, 58, 96, 70, 82],
    kpi: { hours: 28.4, sessions: 61, accuracy: 84, rank: 5 },
    trend: { hours: 18, sessions: 14, accuracy: 6, rank: 3 },
    sparkline: {
      hours: [18, 20, 22, 23, 25, 26, 28.4],
      sessions: [42, 46, 50, 53, 56, 58, 61],
      accuracy: [76, 78, 80, 81, 82, 83, 84],
      rank: [12, 11, 9, 8, 7, 6, 5]
    },
    weekday: [
      { label: 'T2', value: 58 },
      { label: 'T3', value: 72 },
      { label: 'T4', value: 49 },
      { label: 'T5', value: 81 },
      { label: 'T6', value: 64 },
      { label: 'T7', value: 95, active: true },
      { label: 'CN', value: 53 }
    ]
  },
  '90': {
    series: [
      35, 48, 30, 55, 42, 68, 50, 58, 33, 62, 52, 74, 40, 60, 47, 80, 55, 66, 45, 78,
      56, 70, 50, 84, 62, 68, 53, 88, 64, 76, 58, 92, 70, 78, 60, 94, 72, 82, 64, 90,
      55, 70, 48, 86, 66, 74, 56, 96, 68, 80, 60, 88, 50, 72, 44, 84, 62, 76, 54, 92,
      66, 70, 52, 90, 64, 78, 56, 94, 70, 82, 60, 88, 50, 74, 46, 86, 64, 76, 58, 92,
      68, 80, 62, 96, 72, 84, 64, 90, 56, 78
    ],
    kpi: { hours: 92.6, sessions: 186, accuracy: 82, rank: 8 },
    trend: { hours: 24, sessions: 20, accuracy: -2, rank: 5 },
    sparkline: {
      hours: [60, 66, 72, 78, 83, 88, 92.6],
      sessions: [120, 135, 148, 160, 170, 178, 186],
      accuracy: [85, 84, 84, 83, 83, 82, 82],
      rank: [20, 18, 15, 13, 11, 9, 8]
    },
    weekday: [
      { label: 'T2', value: 62 },
      { label: 'T3', value: 75 },
      { label: 'T4', value: 54 },
      { label: 'T5', value: 84 },
      { label: 'T6', value: 70 },
      { label: 'T7', value: 98, active: true },
      { label: 'CN', value: 58 }
    ]
  }
};

const current = computed<RangeData>(() => dataByRange[activeRange.value]);

// ── Count-up KPI numbers ──────────────────────────────────────────
const displayKpi = ref<Record<string, number>>({ hours: 0, sessions: 0, accuracy: 0, rank: 0 });
let rafId: number | null = null;

function animateKpis(): void {
  if (rafId !== null) cancelAnimationFrame(rafId);
  const targets = current.value.kpi;
  const start = performance.now();
  const duration = 800;
  const from: Record<string, number> = { ...displayKpi.value };
  const step = (t: number): void => {
    const p = Math.min(1, (t - start) / duration);
    const eased = 1 - Math.pow(1 - p, 3);
    const next: Record<string, number> = {};
    for (const def of kpiDefs) {
      const f = from[def.key] ?? 0;
      next[def.key] = f + (targets[def.key] - f) * eased;
    }
    displayKpi.value = next;
    if (p < 1) {
      rafId = requestAnimationFrame(step);
    } else {
      rafId = null;
    }
  };
  rafId = requestAnimationFrame(step);
}

function fmtKpi(def: KpiDef): string {
  const raw = displayKpi.value[def.key] ?? 0;
  if (def.key === 'rank') return `#${Math.round(raw)}`;
  if (def.decimals === 0) return Math.round(raw).toLocaleString('vi-VN');
  return raw.toLocaleString('vi-VN', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
}

// ── SVG line/area chart geometry ──────────────────────────────────
const CHART_W = 640;
const CHART_H = 220;
const PAD_X = 8;
const PAD_Y = 16;

const linePoints = computed<{ x: number; y: number }[]>(() => {
  const s = current.value.series;
  const max = Math.max(...s, 1);
  const min = Math.min(...s, 0);
  const range = max - min || 1;
  const innerW = CHART_W - PAD_X * 2;
  const innerH = CHART_H - PAD_Y * 2;
  return s.map((v, i) => {
    const x = PAD_X + (s.length === 1 ? 0 : (i / (s.length - 1)) * innerW);
    const y = PAD_Y + innerH - ((v - min) / range) * innerH;
    return { x: Math.round(x * 100) / 100, y: Math.round(y * 100) / 100 };
  });
});

const linePath = computed<string>(() => {
  const pts = linePoints.value;
  if (pts.length === 0) return '';
  return `M${pts.map((p) => `${p.x},${p.y}`).join(' L')}`;
});

const areaPath = computed<string>(() => {
  const pts = linePoints.value;
  if (pts.length === 0) return '';
  const first = pts[0];
  const last = pts[pts.length - 1];
  return `${linePath.value} L${last.x},${CHART_H - PAD_Y} L${first.x},${CHART_H - PAD_Y} Z`;
});

// approximate polyline length for stroke-dashoffset draw-on
const lineLength = computed<number>(() => {
  const pts = linePoints.value;
  let len = 0;
  for (let i = 1; i < pts.length; i += 1) {
    const dx = pts[i].x - pts[i - 1].x;
    const dy = pts[i].y - pts[i - 1].y;
    len += Math.sqrt(dx * dx + dy * dy);
  }
  return Math.ceil(len) + 10;
});

const chartTotal = computed<number>(() => current.value.series.reduce((sum, v) => sum + v, 0));
const chartPeak = computed<number>(() => Math.max(...current.value.series));

// draw-on trigger: bump a key so the <path> remounts and replays the keyframe
const drawKey = ref<number>(0);

// ── Donut: subject distribution ───────────────────────────────────
interface Slice {
  label: string;
  value: number;
  color: string;
  accent: Accent;
}
const subjectSlices: Slice[] = [
  { label: 'Toán học', value: 32, color: 'oklch(0.78 0.18 195)', accent: 'cyan' },
  { label: 'Ngoại ngữ', value: 24, color: 'oklch(0.75 0.18 145)', accent: 'green' },
  { label: 'Lập trình', value: 22, color: 'oklch(0.62 0.2 290)', accent: 'violet' },
  { label: 'Khoa học', value: 14, color: 'oklch(0.68 0.22 350)', accent: 'magenta' },
  { label: 'Lịch sử', value: 8, color: 'oklch(0.8 0.16 85)', accent: 'amber' }
];
const sliceTotal = subjectSlices.reduce((sum, s) => sum + s.value, 0);

interface DonutArc {
  label: string;
  pct: number;
  color: string;
  dash: number;
  gap: number;
  offset: number;
}
const donutR = 52;
const donutC = 2 * Math.PI * donutR;
const donutArcs = computed<DonutArc[]>(() => {
  let acc = 0;
  return subjectSlices.map((s) => {
    const pct = (s.value / sliceTotal) * 100;
    const dash = (pct / 100) * donutC;
    const arc: DonutArc = {
      label: s.label,
      pct: Math.round(pct),
      color: s.color,
      dash,
      gap: donutC - dash,
      offset: -((acc / 100) * donutC)
    };
    acc += pct;
    return arc;
  });
});

// ── Mastery by subject ────────────────────────────────────────────
const mastery: { name: string; pct: number; accent: Accent }[] = [
  { name: 'Toán học', pct: 82, accent: 'cyan' },
  { name: 'Ngoại ngữ', pct: 68, accent: 'green' },
  { name: 'Lập trình', pct: 74, accent: 'violet' },
  { name: 'Khoa học', pct: 55, accent: 'magenta' },
  { name: 'Lịch sử', pct: 47, accent: 'amber' }
];

// ── Best time of day (24 bars) ────────────────────────────────────
const hourly: number[] = [
  2, 1, 0, 0, 0, 1, 4, 9, 14, 18, 16, 12, 10, 13, 17, 15, 12, 18, 26, 38, 44, 36, 22, 8
];
const hourlyMax = Math.max(...hourly);
const peakHour = computed<number>(() => hourly.indexOf(hourlyMax));

function hourBarClass(v: number): string {
  if (v === hourlyMax) return 'bg-gradient-to-t from-amber-500/80 to-amber-300 shadow-[0_0_10px_oklch(0.8_0.16_85/55%)]';
  if (v >= hourlyMax * 0.6) return 'bg-cyan-400/70';
  if (v >= hourlyMax * 0.3) return 'bg-cyan-500/40';
  return 'bg-white/[0.08]';
}

// ── Insight cards ─────────────────────────────────────────────────
const insights = [
  { icon: 'moon', accent: 'violet' as Accent, title: 'Học tốt nhất vào buổi tối', body: 'Bạn tập trung cao nhất khoảng 20:00–21:00, chiếm 38% thời lượng.' },
  { icon: 'flame', accent: 'amber' as Accent, title: 'Chuỗi dài nhất: 21 ngày', body: 'Kỷ lục cá nhân của bạn — hãy thử phá vỡ nó trong tháng này!' },
  { icon: 'trending-up', accent: 'green' as Accent, title: 'Độ chính xác đang tăng', body: 'Tăng 4% so với kỳ trước nhờ làm quiz đều đặn hơn.' },
  { icon: 'target', accent: 'cyan' as Accent, title: 'Toán là môn mạnh nhất', body: 'Chiếm 32% thời lượng học và mức thành thạo 82%.' }
];

function trendOf(key: string): number {
  return current.value.trend[key] ?? 0;
}

watch(activeRange, () => {
  drawKey.value += 1;
  animateKpis();
});

onMounted(() => {
  drawKey.value += 1;
  animateKpis();
});

onUnmounted(() => {
  if (rafId !== null) cancelAnimationFrame(rafId);
});
</script>

<template>
  <div class="space-y-6">
    <!-- Hero -->
    <GlassPanel glow="violet" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Phân tích học tập</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Thống kê</span> chi tiết 📊
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Theo dõi tiến độ, thói quen và hiệu suất học tập của bạn theo thời gian.
          </p>
        </div>
        <Tabs :tabs="[...rangeTabs]" :model-value="activeRange" @update:model-value="(id) => (activeRange = id as RangeId)" />
      </div>
    </GlassPanel>

    <!-- KPI count-up tiles -->
    <section class="grid grid-cols-2 gap-4 xl:grid-cols-4">
      <GlassPanel v-for="def in kpiDefs" :key="def.key" :glow="def.accent" hover padded>
        <div class="mb-3 flex items-start justify-between">
          <span
            class="flex h-11 w-11 items-center justify-center rounded-xl"
            :class="{
              'bg-cyan-500/10 text-cyan-300': def.accent === 'cyan',
              'bg-fuchsia-500/10 text-fuchsia-300': def.accent === 'magenta',
              'bg-emerald-500/10 text-emerald-300': def.accent === 'green',
              'bg-violet-500/10 text-violet-300': def.accent === 'violet',
              'bg-amber-500/10 text-amber-300': def.accent === 'amber'
            }"
          >
            <AppIcon :name="def.icon" class="h-5 w-5" />
          </span>
          <span
            class="inline-flex items-center gap-1 rounded-md px-2 py-1 text-xs font-medium"
            :class="trendOf(def.key) >= 0 ? 'bg-emerald-500/10 text-emerald-300' : 'bg-rose-500/10 text-rose-300'"
          >
            <AppIcon :name="trendOf(def.key) >= 0 ? 'trending-up' : 'trending-down'" class="h-3 w-3" />
            {{ Math.abs(trendOf(def.key)) }}%
          </span>
        </div>
        <p class="text-[0.7rem] font-medium uppercase tracking-wider text-muted-foreground">{{ def.label }}</p>
        <p class="mt-1 flex items-baseline gap-1.5">
          <span class="text-2xl font-bold tabular-nums text-foreground">{{ fmtKpi(def) }}</span>
          <span v-if="def.unit" class="text-sm text-muted-foreground">{{ def.unit }}</span>
        </p>
        <div class="mt-3 flex h-8 items-end gap-0.5">
          <span
            v-for="(v, i) in current.sparkline[def.key]"
            :key="i"
            class="flex-1 rounded-sm bg-white/[0.1]"
            :style="{ height: `${Math.max(12, (v / Math.max(...current.sparkline[def.key])) * 100)}%` }"
          />
        </div>
      </GlassPanel>
    </section>

    <!-- Line chart + donut -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <div class="lg:col-span-2">
        <SectionPanel
          title="Số phút học theo thời gian"
          :subtitle="`Tổng ${chartTotal} phút · cao nhất ${chartPeak} phút/ngày`"
          icon="chart"
          accent="cyan"
        >
          <div class="relative w-full">
            <svg :viewBox="`0 0 ${CHART_W} ${CHART_H}`" preserveAspectRatio="none" class="h-56 w-full">
              <defs>
                <linearGradient id="stats-area" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="oklch(0.78 0.18 195)" stop-opacity="0.32" />
                  <stop offset="100%" stop-color="oklch(0.78 0.18 195)" stop-opacity="0" />
                </linearGradient>
              </defs>
              <!-- gridlines -->
              <line
                v-for="g in [0.25, 0.5, 0.75]"
                :key="g"
                :x1="PAD_X"
                :x2="CHART_W - PAD_X"
                :y1="PAD_Y + (CHART_H - PAD_Y * 2) * g"
                :y2="PAD_Y + (CHART_H - PAD_Y * 2) * g"
                stroke="oklch(0.5 0.02 280 / 12%)"
                stroke-width="1"
                stroke-dasharray="4 6"
              />
              <path :d="areaPath" fill="url(#stats-area)" />
              <path
                :key="drawKey"
                class="stats-line"
                :d="linePath"
                fill="none"
                stroke="oklch(0.78 0.18 195)"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
                vector-effect="non-scaling-stroke"
                :style="{ '--chart-len': lineLength } as Record<string, number>"
              />
            </svg>
          </div>
          <div class="mt-3 flex items-center justify-between text-xs text-muted-foreground">
            <span>Bắt đầu kỳ</span>
            <span class="inline-flex items-center gap-1.5">
              <span class="h-2 w-2 rounded-full bg-cyan-400" /> Phút học / ngày
            </span>
            <span>Hôm nay</span>
          </div>
        </SectionPanel>
      </div>

      <div>
        <SectionPanel title="Phân bổ theo môn" subtitle="Tỉ lệ thời lượng học" icon="graduation" accent="violet">
          <div class="flex flex-col items-center">
            <div class="relative h-40 w-40">
              <svg viewBox="0 0 120 120" class="h-full w-full -rotate-90">
                <circle cx="60" cy="60" :r="donutR" fill="none" stroke="oklch(0.3 0.03 280 / 40%)" stroke-width="14" />
                <circle
                  v-for="(arc, i) in donutArcs"
                  :key="i"
                  cx="60"
                  cy="60"
                  :r="donutR"
                  fill="none"
                  :stroke="arc.color"
                  stroke-width="14"
                  :stroke-dasharray="`${arc.dash} ${arc.gap}`"
                  :stroke-dashoffset="arc.offset"
                  class="donut-arc"
                  :style="{ filter: `drop-shadow(0 0 4px ${arc.color})`, animationDelay: `${i * 90}ms` }"
                />
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <span class="text-2xl font-bold text-foreground">{{ subjectSlices.length }}</span>
                <span class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">môn học</span>
              </div>
            </div>
            <ul class="mt-4 w-full space-y-2">
              <li v-for="(s, i) in subjectSlices" :key="s.label" class="flex items-center gap-2.5 text-sm">
                <span class="h-3 w-3 shrink-0 rounded-sm" :style="{ backgroundColor: s.color }" />
                <span class="min-w-0 flex-1 truncate text-muted-foreground">{{ s.label }}</span>
                <span class="shrink-0 font-semibold tabular-nums text-foreground">{{ donutArcs[i].pct }}%</span>
              </li>
            </ul>
          </div>
        </SectionPanel>
      </div>
    </div>

    <!-- Weekday bars + mastery -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <SectionPanel title="Phút học theo thứ" subtitle="Trung bình mỗi ngày trong tuần" icon="calendar" accent="green">
        <BarChart :data="current.weekday" accent="green" :height="160" />
      </SectionPanel>

      <SectionPanel title="Mức độ thành thạo" subtitle="Theo từng môn học" icon="graduation" accent="cyan">
        <div class="space-y-3.5">
          <div v-for="m in mastery" :key="m.name">
            <div class="mb-1 flex items-center justify-between text-xs">
              <span class="font-medium text-foreground">{{ m.name }}</span>
              <span class="tabular-nums text-muted-foreground">{{ m.pct }}%</span>
            </div>
            <ProgressBar :value="m.pct" :accent="m.accent" :height="8" />
          </div>
        </div>
      </SectionPanel>
    </div>

    <!-- Best time of day -->
    <SectionPanel
      title="Khung giờ học hiệu quả"
      :subtitle="`Cao điểm lúc ${peakHour}:00 · phân bố 24 giờ`"
      icon="clock"
      accent="amber"
    >
      <div class="flex h-32 items-end gap-1">
        <div v-for="(v, h) in hourly" :key="h" class="flex flex-1 flex-col items-center justify-end gap-1">
          <div class="flex w-full flex-1 items-end justify-center">
            <div
              class="w-full rounded-sm transition-all duration-500"
              :class="hourBarClass(v)"
              :style="{ height: `${Math.max(4, (v / hourlyMax) * 100)}%` }"
              :title="`${h}:00 — ${v} phiên`"
            />
          </div>
          <span v-if="h % 3 === 0" class="text-[0.6rem] tabular-nums text-muted-foreground">{{ h }}</span>
        </div>
      </div>
    </SectionPanel>

    <!-- Insights -->
    <SectionPanel title="Nhận định" subtitle="Tổng hợp từ dữ liệu học tập của bạn" icon="lightbulb" accent="violet">
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div
          v-for="card in insights"
          :key="card.title"
          class="rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:border-white/[0.12]"
        >
          <span
            class="flex h-10 w-10 items-center justify-center rounded-xl"
            :class="{
              'bg-cyan-500/10 text-cyan-300': card.accent === 'cyan',
              'bg-fuchsia-500/10 text-fuchsia-300': card.accent === 'magenta',
              'bg-emerald-500/10 text-emerald-300': card.accent === 'green',
              'bg-violet-500/10 text-violet-300': card.accent === 'violet',
              'bg-amber-500/10 text-amber-300': card.accent === 'amber'
            }"
          >
            <AppIcon :name="card.icon" class="h-5 w-5" />
          </span>
          <p class="mt-3 text-sm font-semibold text-foreground">{{ card.title }}</p>
          <p class="mt-1 text-xs leading-relaxed text-muted-foreground">{{ card.body }}</p>
        </div>
      </div>
    </SectionPanel>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.stats-line {
  stroke-dasharray: var(--chart-len);
  stroke-dashoffset: var(--chart-len);
  animation: draw-on 1.1s ease forwards;
}
@keyframes draw-on {
  to {
    stroke-dashoffset: 0;
  }
}
.donut-arc {
  animation: donut-in 0.7s ease both;
  transform-origin: 60px 60px;
}
@keyframes donut-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
