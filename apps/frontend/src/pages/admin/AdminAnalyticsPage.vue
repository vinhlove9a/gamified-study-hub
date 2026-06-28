<script setup lang="ts">
/**
 * Trang Phân tích cho admin console (Deep Space / DEEPWORKSPACE).
 * Tất cả số liệu là mock. Tương tác thật: bộ chọn khoảng thời gian,
 * chuyển chỉ số biểu đồ (segmented), tooltip cohort heatmap.
 */
import { computed, ref } from 'vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdminStatCard from '@/components/admin/AdminStatCard.vue';
import AdminPill from '@/components/admin/AdminPill.vue';
import AdminAreaChart from '@/components/admin/AdminAreaChart.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

// ── Màu series (oklch) ───────────────────────────────────────────────
const COLOR = {
  cyan: 'oklch(0.78 0.18 195)',
  magenta: 'oklch(0.68 0.22 350)',
  green: 'oklch(0.75 0.18 145)',
  violet: 'oklch(0.62 0.2 290)',
  amber: 'oklch(0.8 0.16 85)'
} as const;

// ── 1. Bộ chọn khoảng thời gian ──────────────────────────────────────
interface RangeOption {
  key: string;
  label: string;
  /** Số điểm dữ liệu sinh ra cho biểu đồ. */
  points: number;
  /** Hàm tạo nhãn trục X theo chỉ số điểm. */
  tick: (i: number, n: number) => string;
}

const ranges: RangeOption[] = [
  { key: '7d', label: '7 ngày', points: 7, tick: (i) => `${i + 1}` },
  { key: '30d', label: '30 ngày', points: 30, tick: (i) => `${i + 1}` },
  { key: '90d', label: '90 ngày', points: 30, tick: (i) => `T${i + 1}` },
  { key: '12m', label: '12 tháng', points: 12, tick: (i) => `Th${i + 1}` }
];
const range = ref<string>('30d');
const activeRange = computed(() => ranges.find((r) => r.key === range.value) ?? ranges[1]);

// Hệ số nhân theo khoảng (giúp số liệu "thật" đổi theo lựa chọn)
const rangeScale: Record<string, number> = { '7d': 0.6, '30d': 1, '90d': 2.4, '12m': 8 };

// ── 2. KPI ───────────────────────────────────────────────────────────
interface Kpi {
  label: string;
  value: number;
  prefix?: string;
  suffix?: string;
  unit?: string;
  decimals?: number;
  icon: string;
  accent: Accent;
  trend: number;
  trendLabel: string;
  spark: number[];
}

const kpis: Kpi[] = [
  { label: 'DAU', value: 3892, icon: 'flame', accent: 'cyan', trend: 8.1, trendLabel: 'Hoạt động hằng ngày', spark: [3100, 3260, 3180, 3420, 3510, 3680, 3740, 3892] },
  { label: 'WAU', value: 9140, icon: 'users', accent: 'violet', trend: 5.4, trendLabel: 'Hoạt động hằng tuần', spark: [8200, 8350, 8480, 8640, 8790, 8920, 9020, 9140] },
  { label: 'MAU', value: 12428, icon: 'globe', accent: 'magenta', trend: 12.4, trendLabel: 'Hoạt động hằng tháng', spark: [10800, 11020, 11240, 11500, 11780, 12010, 12230, 12428] },
  { label: 'Giữ chân D7', value: 42, suffix: '%', icon: 'repeat', accent: 'green', trend: 3.2, trendLabel: 'Quay lại sau 7 ngày', spark: [36, 37, 38, 39, 40, 41, 41, 42] },
  { label: 'Thời lượng phiên TB', value: 14, unit: 'phút', icon: 'clock', accent: 'amber', trend: -1.8, trendLabel: 'Mỗi phiên học', spark: [15, 15, 14, 15, 14, 14, 13, 14] }
];

// ── 3. Panel "Mức độ tương tác": segmented đổi chỉ số ────────────────
interface Metric {
  key: string;
  label: string;
  decimals: number;
  prefix: string;
  suffix: string;
  /** Định nghĩa các chuỗi: base, biên độ, pha, độ dốc, màu. */
  series: { key: string; name: string; color: string; base: number; amp: number; phase: number; slope: number; area: boolean }[];
}

const metrics: Metric[] = [
  {
    key: 'sessions',
    label: 'Phiên & DAU',
    decimals: 0,
    prefix: '',
    suffix: '',
    series: [
      { key: 'dau', name: 'DAU', color: COLOR.cyan, base: 3200, amp: 320, phase: 0, slope: 22, area: true },
      { key: 'sessions', name: 'Phiên', color: COLOR.magenta, base: 5200, amp: 540, phase: 1.4, slope: 38, area: true }
    ]
  },
  {
    key: 'duration',
    label: 'Thời lượng',
    decimals: 1,
    prefix: '',
    suffix: ' ph',
    series: [
      { key: 'avg', name: 'TB / phiên', color: COLOR.amber, base: 13.5, amp: 1.4, phase: 0.6, slope: 0.04, area: true },
      { key: 'median', name: 'Trung vị', color: COLOR.violet, base: 10.8, amp: 1.1, phase: 2, slope: 0.03, area: false }
    ]
  },
  {
    key: 'lessons',
    label: 'Bài học',
    decimals: 0,
    prefix: '',
    suffix: '',
    series: [
      { key: 'started', name: 'Bắt đầu', color: COLOR.cyan, base: 2400, amp: 280, phase: 0.3, slope: 30, area: true },
      { key: 'completed', name: 'Hoàn thành', color: COLOR.green, base: 1500, amp: 200, phase: 1.8, slope: 24, area: true }
    ]
  }
];
const metric = ref<string>('sessions');
const activeMetric = computed(() => metrics.find((m) => m.key === metric.value) ?? metrics[0]);

// Sinh sóng tất định (không random) để series ổn định khi re-render
function wave(n: number, base: number, amp: number, phase: number, slope: number, decimals: number) {
  const arr: number[] = [];
  for (let i = 0; i < n; i++) {
    const v = base + slope * i + Math.sin(i * 0.5 + phase) * amp + Math.cos(i * 0.23) * amp * 0.4;
    arr.push(decimals > 0 ? Number(v.toFixed(decimals)) : Math.round(v));
  }
  return arr;
}

const chartLabels = computed(() => {
  const r = activeRange.value;
  return Array.from({ length: r.points }, (_, i) => r.tick(i, r.points));
});

const chartSeries = computed(() => {
  const m = activeMetric.value;
  const n = activeRange.value.points;
  const scale = rangeScale[range.value] ?? 1;
  return m.series.map((s) => ({
    key: s.key,
    name: s.name,
    color: s.color,
    // Số đếm phóng theo khoảng thời gian; tỷ lệ (thời lượng) giữ nguyên scale
    values: wave(n, m.key === 'duration' ? s.base : s.base * scale, m.key === 'duration' ? s.amp : s.amp * scale, s.phase, m.key === 'duration' ? s.slope : s.slope * scale, m.decimals),
    area: s.area
  }));
});

// ── 4. Thiết bị / Nền tảng ───────────────────────────────────────────
interface Platform {
  label: string;
  value: number;
  accent: Accent;
}
const platforms: Platform[] = [
  { label: 'Di động', value: 58, accent: 'cyan' },
  { label: 'Web', value: 34, accent: 'violet' },
  { label: 'Máy tính bảng', value: 8, accent: 'magenta' }
];

// ── 5. Phễu chuyển đổi ───────────────────────────────────────────────
interface FunnelStep {
  label: string;
  value: number;
  icon: string;
  accent: Accent;
}
const funnelRaw: FunnelStep[] = [
  { label: 'Đăng ký', value: 12428, icon: 'user', accent: 'cyan' },
  { label: 'Kích hoạt', value: 9140, icon: 'zap', accent: 'violet' },
  { label: 'Tải tài liệu đầu', value: 6210, icon: 'download', accent: 'magenta' },
  { label: 'Hoàn thành nhiệm vụ', value: 4080, icon: 'check-circle', accent: 'green' },
  { label: 'Nâng cấp VIP', value: 1840, icon: 'crown', accent: 'amber' }
];
const funnelBarFill: Record<Accent, string> = {
  cyan: 'from-cyan-400/80 to-cyan-500/30',
  magenta: 'from-fuchsia-400/80 to-fuchsia-500/30',
  green: 'from-emerald-400/80 to-emerald-500/30',
  violet: 'from-violet-400/80 to-violet-500/30',
  amber: 'from-amber-300/80 to-amber-400/30'
};
const funnel = computed(() => {
  const top = funnelRaw[0].value;
  return funnelRaw.map((s, i) => {
    const prev = i === 0 ? s.value : funnelRaw[i - 1].value;
    return {
      ...s,
      widthPct: Math.round((s.value / top) * 100),
      convPct: Math.round((s.value / top) * 100),
      dropPct: i === 0 ? 0 : Math.round((1 - s.value / prev) * 100)
    };
  });
});

// ── 6. Cohort retention heatmap ──────────────────────────────────────
const cohortWeeks = Array.from({ length: 8 }, (_, i) => `Tuần ${i + 1}`);
const cohortCols = Array.from({ length: 8 }, (_, i) => `T${i}`);
// Sinh dữ liệu tất định: cột 0 luôn 100%, giảm dần; cohort cũ giữ chân tốt hơn
function cohortValue(rowIdx: number, colIdx: number): number {
  if (colIdx === 0) return 100;
  const base = 64 - colIdx * 6.5; // suy giảm theo tuần sau đăng ký
  const maturity = (7 - rowIdx) * 1.4; // cohort càng cũ giữ chân nhỉnh hơn
  const wobble = Math.cos(rowIdx * 0.7 + colIdx) * 3;
  return Math.max(6, Math.round(base + maturity + wobble));
}
interface CohortCell {
  value: number;
  bg: string;
}
const cohortRows = computed<{ week: string; size: number; cells: CohortCell[] }[]>(() =>
  cohortWeeks.map((week, r) => ({
    week,
    size: 1820 - r * 110,
    cells: cohortCols.map((_, c) => {
      const v = cohortValue(r, c);
      // Opacity theo giá trị: 0% → ~8%, 100% → ~85%
      const op = Math.round(8 + (v / 100) * 77);
      return { value: v, bg: `oklch(0.78 0.18 195 / ${op}%)` };
    })
  }))
);
const legendStops = [15, 35, 55, 75, 95];

// ── 7. Hiệu suất nội dung ────────────────────────────────────────────
interface ContentRow {
  subject: string;
  type: string;
  views: number;
  downloads: number;
  completion: number;
  accent: Accent;
}
const contentRows: ContentRow[] = [
  { subject: 'Toán cao cấp', type: 'Đề cương', views: 18420, downloads: 6210, completion: 88, accent: 'cyan' },
  { subject: 'Lập trình Python', type: 'Giáo trình', views: 14210, downloads: 5180, completion: 74, accent: 'magenta' },
  { subject: 'IELTS 7.0+', type: 'Flashcard', views: 12890, downloads: 4720, completion: 81, accent: 'green' },
  { subject: 'Vật lý đại cương', type: 'Slide', views: 9340, downloads: 3110, completion: 62, accent: 'violet' },
  { subject: 'Kinh tế vĩ mô', type: 'Bài giảng', views: 7120, downloads: 2240, completion: 57, accent: 'amber' },
  { subject: 'Hóa hữu cơ', type: 'Sổ tay', views: 6480, downloads: 1980, completion: 69, accent: 'cyan' },
  { subject: 'Triết học Mác-Lênin', type: 'Đề cương', views: 5210, downloads: 1430, completion: 45, accent: 'violet' }
];

function fmt(n: number) {
  return n.toLocaleString('vi-VN');
}
</script>

<template>
  <div class="space-y-6">
    <!-- 1. Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2">
          <span class="h-1.5 w-1.5 rounded-full bg-cyan-400 pulse-glow-green" />
          <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">Phân tích · Hành vi người học</p>
        </div>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Phân tích &amp; Đo lường 📊</h2>
      </div>
      <div class="flex items-center gap-2">
        <div class="flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
          <button
            v-for="r in ranges"
            :key="r.key"
            class="rounded-lg px-3 py-1.5 text-xs font-medium transition-colors"
            :class="range === r.key ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
            @click="range = r.key"
          >
            {{ r.label }}
          </button>
        </div>
        <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
          <AppIcon name="download" class="h-4 w-4" /> Xuất báo cáo
        </button>
      </div>
    </div>

    <!-- 2. KPI row -->
    <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-5">
      <AdminStatCard
        v-for="k in kpis"
        :key="k.label"
        :label="k.label"
        :value="k.value"
        :prefix="k.prefix"
        :suffix="k.suffix"
        :unit="k.unit"
        :decimals="k.decimals"
        :icon="k.icon"
        :accent="k.accent"
        :trend="k.trend"
        :trend-label="k.trendLabel"
        :sparkline="k.spark"
      />
    </div>

    <!-- 3 + 4. Engagement + Platform -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <AdminPanel
        class="lg:col-span-2"
        title="Mức độ tương tác"
        :subtitle="activeRange.label"
        icon="trending-up"
        accent="cyan"
        glow="cyan"
      >
        <template #actions>
          <div class="flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
            <button
              v-for="m in metrics"
              :key="m.key"
              class="rounded-lg px-2.5 py-1 text-[11px] font-medium transition-colors"
              :class="metric === m.key ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
              @click="metric = m.key"
            >
              {{ m.label }}
            </button>
          </div>
        </template>
        <AdminAreaChart
          :labels="chartLabels"
          :series="chartSeries"
          :height="280"
          :decimals="activeMetric.decimals"
          :prefix="activeMetric.prefix"
          :suffix="activeMetric.suffix"
        />
      </AdminPanel>

      <AdminPanel title="Thiết bị / Nền tảng" subtitle="Tỷ trọng phiên" icon="layers" accent="violet">
        <div class="flex flex-col items-center">
          <div class="relative">
            <RingProgress :value="58" :size="132" :stroke="12" accent="cyan" />
            <span class="absolute inset-0 flex flex-col items-center justify-center">
              <span class="text-2xl font-bold font-mono text-foreground">58%</span>
              <span class="text-[11px] text-muted-foreground">Di động</span>
            </span>
          </div>
        </div>
        <ul class="mt-4 space-y-3">
          <li v-for="p in platforms" :key="p.label">
            <div class="mb-1 flex items-center justify-between text-xs">
              <span class="text-muted-foreground">{{ p.label }}</span>
              <span class="font-mono font-semibold text-foreground">{{ p.value }}%</span>
            </div>
            <ProgressBar :value="p.value" :accent="p.accent" />
          </li>
        </ul>
      </AdminPanel>
    </div>

    <!-- 5 + 6. Funnel + Cohort -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-5">
      <!-- Funnel -->
      <AdminPanel class="lg:col-span-2" title="Phễu chuyển đổi" subtitle="Từ đăng ký đến VIP" icon="filter" accent="magenta">
        <template #actions>
          <AdminPill tone="amber" dot>14.8% → VIP</AdminPill>
        </template>
        <ul class="space-y-3">
          <li v-for="(s, i) in funnel" :key="s.label">
            <div class="mb-1.5 flex items-center gap-2 text-xs">
              <span class="flex h-6 w-6 shrink-0 items-center justify-center rounded-lg bg-white/[0.04] text-muted-foreground">
                <AppIcon :name="s.icon" class="h-3.5 w-3.5" />
              </span>
              <span class="min-w-0 flex-1 truncate text-foreground">{{ s.label }}</span>
              <span class="font-mono font-semibold text-foreground">{{ fmt(s.value) }}</span>
              <span class="font-mono text-muted-foreground">{{ s.convPct }}%</span>
            </div>
            <div class="relative h-7 overflow-hidden rounded-lg bg-white/[0.04]">
              <div
                class="flex h-full items-center rounded-lg bg-gradient-to-r px-2 transition-[width] duration-500"
                :class="funnelBarFill[s.accent]"
                :style="{ width: `${s.widthPct}%` }"
              >
                <span class="truncate font-mono text-[10px] font-semibold text-foreground/90">{{ fmt(s.value) }}</span>
              </div>
            </div>
            <p v-if="i > 0" class="mt-1 text-right font-mono text-[10px] text-rose-300">
              <AppIcon name="trending-down" class="mr-0.5 inline h-3 w-3" />rớt {{ s.dropPct }}%
            </p>
          </li>
        </ul>
      </AdminPanel>

      <!-- Cohort heatmap -->
      <AdminPanel class="lg:col-span-3" title="Bản đồ giữ chân (Cohort)" subtitle="% người học quay lại theo tuần đăng ký" icon="grid" accent="cyan">
        <template #actions>
          <div class="flex items-center gap-1.5 text-[10px] text-muted-foreground">
            <span>Thấp</span>
            <span
              v-for="op in legendStops"
              :key="op"
              class="h-3 w-4 rounded-sm"
              :style="{ background: `oklch(0.78 0.18 195 / ${op}%)` }"
            />
            <span>Cao</span>
          </div>
        </template>
        <div class="overflow-x-auto">
          <table class="w-full border-separate" style="border-spacing: 3px">
            <thead>
              <tr>
                <th class="px-2 py-1 text-left font-mono text-[10px] font-normal text-muted-foreground">Cohort</th>
                <th
                  v-for="c in cohortCols"
                  :key="c"
                  class="px-1 py-1 text-center font-mono text-[10px] font-normal text-muted-foreground"
                >
                  {{ c }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in cohortRows" :key="row.week">
                <td class="whitespace-nowrap px-2 py-1 text-left">
                  <span class="block text-[11px] text-foreground">{{ row.week }}</span>
                  <span class="block font-mono text-[9px] text-muted-foreground">{{ fmt(row.size) }} ng</span>
                </td>
                <td v-for="(cell, ci) in row.cells" :key="ci" class="p-0">
                  <div
                    class="group relative flex h-9 min-w-[34px] items-center justify-center rounded-md transition-transform duration-150 hover:scale-[1.12] hover:ring-1 hover:ring-cyan-300/50"
                    :style="{ background: cell.bg }"
                    :title="`${row.week} · ${cohortCols[ci]}: ${cell.value}% giữ chân`"
                  >
                    <span class="font-mono text-[10px] font-semibold text-foreground/90">{{ cell.value }}</span>
                    <span
                      class="pointer-events-none absolute bottom-full left-1/2 z-10 mb-1 -translate-x-1/2 whitespace-nowrap rounded-lg border border-white/10 bg-[oklch(0.12_0.03_280)] px-2 py-1 text-[10px] text-foreground opacity-0 shadow-lg transition-opacity group-hover:opacity-100"
                    >
                      {{ row.week }} · {{ cohortCols[ci] }} — <span class="font-mono font-semibold text-cyan-300">{{ cell.value }}%</span>
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </AdminPanel>
    </div>

    <!-- 7. Content performance -->
    <AdminPanel title="Hiệu suất nội dung" subtitle="Top môn học & loại tài liệu" icon="file-text" accent="green">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-white/[0.06] text-left text-[11px] uppercase tracking-wider text-muted-foreground">
              <th class="px-3 py-2 font-medium">#</th>
              <th class="px-3 py-2 font-medium">Môn học</th>
              <th class="px-3 py-2 font-medium">Loại</th>
              <th class="px-3 py-2 text-right font-medium">Lượt xem</th>
              <th class="px-3 py-2 text-right font-medium">Lượt tải</th>
              <th class="px-3 py-2 font-medium">Tỷ lệ hoàn thành</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(c, i) in contentRows"
              :key="c.subject"
              class="border-b border-white/[0.04] transition-colors hover:bg-white/[0.03]"
            >
              <td class="px-3 py-2.5 font-mono text-xs text-muted-foreground">{{ String(i + 1).padStart(2, '0') }}</td>
              <td class="px-3 py-2.5 font-medium text-foreground">{{ c.subject }}</td>
              <td class="px-3 py-2.5">
                <AdminPill :tone="c.accent">{{ c.type }}</AdminPill>
              </td>
              <td class="px-3 py-2.5 text-right font-mono text-foreground">{{ fmt(c.views) }}</td>
              <td class="px-3 py-2.5 text-right font-mono text-muted-foreground">{{ fmt(c.downloads) }}</td>
              <td class="px-3 py-2.5">
                <div class="flex items-center gap-2">
                  <ProgressBar :value="c.completion" :accent="c.accent" class="flex-1" />
                  <span class="w-9 shrink-0 text-right font-mono text-xs text-foreground">{{ c.completion }}%</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </AdminPanel>
  </div>
</template>
