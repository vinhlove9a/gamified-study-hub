<script setup lang="ts">
import { ref } from 'vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdminStatCard from '@/components/admin/AdminStatCard.vue';
import AdminPill from '@/components/admin/AdminPill.vue';
import AdminAreaChart from '@/components/admin/AdminAreaChart.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import BarChart from '@/components/app/BarChart.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

const periods = ['Hôm nay', '7 ngày', '30 ngày', 'Quý này'];
const period = ref('30 ngày');

const stats = [
  { label: 'Tổng người dùng', value: 12428, icon: 'users', accent: 'cyan' as const, trend: 12.4, trendLabel: '+248 trong 24h', spark: [10800, 11020, 11240, 11500, 11780, 12010, 12230, 12428] },
  { label: 'Hoạt động hôm nay', value: 3892, icon: 'flame', accent: 'amber' as const, trend: 8.1, trendLabel: 'DAU / 31% tổng', spark: [3100, 3260, 3180, 3420, 3510, 3680, 3740, 3892] },
  { label: 'Tài liệu', value: 48211, icon: 'file-text', accent: 'green' as const, trend: 4.7, trendLabel: '+612 tuần này', spark: [44200, 45100, 45800, 46400, 47000, 47500, 47900, 48211] },
  { label: 'Doanh thu tháng', value: 86.4, prefix: '₫', suffix: 'M', decimals: 1, icon: 'coin', accent: 'magenta' as const, trend: 18.2, trendLabel: 'VIP + cửa hàng', spark: [58, 62, 66, 70, 74, 79, 83, 86.4] }
];

const days = Array.from({ length: 30 }, (_, i) => `${i + 1}`);
function wave(base: number, amp: number, phase: number, slope: number) {
  return days.map((_, i) => Math.round(base + slope * i + Math.sin(i * 0.5 + phase) * amp + Math.cos(i * 0.23) * amp * 0.4));
}
const growthSeries = [
  { key: 'new', name: 'Người dùng mới', color: 'oklch(0.78 0.18 195)', values: wave(180, 40, 0, 4), area: true },
  { key: 'active', name: 'Đang hoạt động', color: 'oklch(0.68 0.22 350)', values: wave(420, 70, 1.5, 6), area: true }
];

const trafficSources = [
  { label: 'Tìm kiếm', value: 42, accent: 'cyan' as const },
  { label: 'Trực tiếp', value: 28, accent: 'violet' as const },
  { label: 'Mạng xã hội', value: 19, accent: 'magenta' as const },
  { label: 'Giới thiệu', value: 11, accent: 'green' as const }
];

const weeklyActive = [
  { label: 'T2', value: 3420 },
  { label: 'T3', value: 3680, active: false },
  { label: 'T4', value: 3910 },
  { label: 'T5', value: 4120, active: true },
  { label: 'T6', value: 3780 },
  { label: 'T7', value: 2940 },
  { label: 'CN', value: 2610 }
];

const pendingDocs = [
  { id: 1, title: 'Đề cương Giải tích 2 — FPTU', author: 'Nguyễn Minh Anh', cat: 'Toán học', size: '2.4 MB', time: '5 phút' },
  { id: 2, title: 'Slide Kiến trúc máy tính (Ch.3)', author: 'Trần Quốc Bảo', cat: 'CNTT', size: '8.1 MB', time: '18 phút' },
  { id: 3, title: '500 từ vựng TOEIC theo chủ đề', author: 'Lê Thu Hà', cat: 'Tiếng Anh', size: '1.2 MB', time: '32 phút' },
  { id: 4, title: 'Tổng hợp công thức Vật lý 12', author: 'Phạm Đức Huy', cat: 'Vật lý', size: '3.7 MB', time: '1 giờ' }
];

const topContent = [
  { title: 'Bộ đề thi thử THPT QG 2026', views: 18420, accent: 'cyan' as const, pct: 100 },
  { title: 'Flashcard 3000 từ IELTS', views: 14210, accent: 'magenta' as const, pct: 77 },
  { title: 'Giáo trình Lập trình Python', views: 11890, accent: 'green' as const, pct: 64 },
  { title: 'Sổ tay công thức Hóa hữu cơ', views: 9340, accent: 'violet' as const, pct: 51 },
  { title: 'Đề cương Kinh tế vĩ mô', views: 7120, accent: 'amber' as const, pct: 39 }
];

const activity = [
  { icon: 'user', tone: 'cyan', text: '248 người dùng mới đăng ký', meta: 'trong 24 giờ qua', time: 'vừa xong' },
  { icon: 'flag', tone: 'rose', text: 'Báo cáo vi phạm: bình luận spam', meta: 'từ @hoangnam · forum', time: '3 phút' },
  { icon: 'coin', tone: 'amber', text: 'Giao dịch VIP +₫299K', meta: 'gói Premium 12 tháng', time: '11 phút' },
  { icon: 'trophy', tone: 'violet', text: 'Nhiệm vụ "Học 7 ngày" hoàn thành', meta: '1.204 người dùng', time: '26 phút' },
  { icon: 'file-text', tone: 'green', text: 'Tài liệu được duyệt & xuất bản', meta: 'Đề cương Triết học', time: '41 phút' },
  { icon: 'shield', tone: 'cyan', text: 'Quản trị viên đăng nhập', meta: 'admin@studyhub.vn · IP 14.x', time: '1 giờ' }
];
const tone: Record<string, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  rose: 'bg-rose-500/15 text-rose-300',
  amber: 'bg-amber-500/15 text-amber-300',
  violet: 'bg-violet-500/15 text-violet-300',
  green: 'bg-emerald-500/15 text-emerald-300'
};

const health = [
  { label: 'API', value: 99, unit: '42ms', accent: 'green' as const },
  { label: 'Cơ sở dữ liệu', value: 96, unit: '11ms', accent: 'cyan' as const },
  { label: 'Lưu trữ', value: 73, unit: '2.1 TB', accent: 'amber' as const },
  { label: 'Hàng đợi', value: 88, unit: '14 job', accent: 'violet' as const }
];

const quickActions = [
  { icon: 'megaphone', label: 'Gửi thông báo', accent: 'cyan' },
  { icon: 'file-text', label: 'Duyệt nội dung', accent: 'green' },
  { icon: 'users', label: 'Quản lý người dùng', accent: 'violet' },
  { icon: 'trophy', label: 'Tạo nhiệm vụ', accent: 'magenta' },
  { icon: 'download', label: 'Xuất báo cáo', accent: 'amber' },
  { icon: 'settings', label: 'Cấu hình', accent: 'cyan' }
];
const qaTone: Record<string, string> = {
  cyan: 'text-cyan-300 group-hover:border-cyan-500/40',
  green: 'text-emerald-300 group-hover:border-emerald-500/40',
  violet: 'text-violet-300 group-hover:border-violet-500/40',
  magenta: 'text-fuchsia-300 group-hover:border-fuchsia-500/40',
  amber: 'text-amber-300 group-hover:border-amber-500/40'
};
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2">
          <span class="h-1.5 w-1.5 rounded-full bg-emerald-400 pulse-glow-green" />
          <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">Trung tâm điều hành · Thời gian thực</p>
        </div>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Chào mừng trở lại, Quản trị viên 👋</h2>
      </div>
      <div class="flex items-center gap-2">
        <div class="flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
          <button
            v-for="p in periods"
            :key="p"
            class="rounded-lg px-3 py-1.5 text-xs font-medium transition-colors"
            :class="period === p ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
            @click="period = p"
          >
            {{ p }}
          </button>
        </div>
        <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
          <AppIcon name="download" class="h-4 w-4" /> Xuất
        </button>
      </div>
    </div>

    <!-- KPI row -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <AdminStatCard
        v-for="s in stats"
        :key="s.label"
        :label="s.label"
        :value="s.value"
        :prefix="s.prefix"
        :suffix="s.suffix"
        :decimals="s.decimals"
        :icon="s.icon"
        :accent="s.accent"
        :trend="s.trend"
        :trend-label="s.trendLabel"
        :sparkline="s.spark"
      />
    </div>

    <!-- Charts row -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <AdminPanel
        class="lg:col-span-2"
        title="Tăng trưởng người dùng"
        subtitle="30 ngày gần nhất"
        icon="chart"
        accent="cyan"
        glow="cyan"
      >
        <template #actions>
          <AdminPill tone="green" dot>+12.4%</AdminPill>
        </template>
        <AdminAreaChart :labels="days" :series="growthSeries" :height="260" />
      </AdminPanel>

      <AdminPanel title="Nguồn truy cập" subtitle="Tỷ trọng phiên" icon="compass" accent="violet">
        <div class="flex flex-col items-center">
          <RingProgress :value="42" :size="132" :stroke="12" accent="cyan" />
          <p class="-mt-[88px] mb-[56px] text-center">
            <span class="block text-2xl font-bold text-foreground">42%</span>
            <span class="text-[11px] text-muted-foreground">Tìm kiếm</span>
          </p>
        </div>
        <ul class="mt-2 space-y-3">
          <li v-for="t in trafficSources" :key="t.label">
            <div class="mb-1 flex items-center justify-between text-xs">
              <span class="text-muted-foreground">{{ t.label }}</span>
              <span class="font-mono font-semibold text-foreground">{{ t.value }}%</span>
            </div>
            <ProgressBar :value="t.value" :accent="t.accent" />
          </li>
        </ul>
      </AdminPanel>
    </div>

    <!-- Operations row -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <!-- Pending moderation -->
      <AdminPanel class="lg:col-span-2" title="Tài liệu chờ duyệt" subtitle="38 mục trong hàng đợi" icon="file-text" accent="green">
        <template #actions>
          <RouterLink to="/admin/content" class="flex items-center gap-1 text-xs font-medium text-cyan-300 hover:text-cyan-200">
            Xem tất cả <AppIcon name="arrow-right" class="h-3.5 w-3.5" />
          </RouterLink>
        </template>
        <ul class="space-y-2">
          <li
            v-for="d in pendingDocs"
            :key="d.id"
            class="flex items-center gap-3 rounded-xl border border-white/[0.05] bg-white/[0.02] px-3 py-2.5 transition-colors hover:bg-white/[0.04]"
          >
            <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-300">
              <AppIcon name="file-text" class="h-4 w-4" />
            </span>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-foreground">{{ d.title }}</p>
              <p class="truncate text-xs text-muted-foreground">{{ d.author }} · {{ d.size }} · {{ d.time }} trước</p>
            </div>
            <AdminPill tone="cyan">{{ d.cat }}</AdminPill>
            <div class="flex shrink-0 items-center gap-1.5">
              <button class="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-300 transition-colors hover:bg-emerald-500/20" title="Duyệt">
                <AppIcon name="check" class="h-4 w-4" />
              </button>
              <button class="flex h-8 w-8 items-center justify-center rounded-lg bg-rose-500/10 text-rose-300 transition-colors hover:bg-rose-500/20" title="Từ chối">
                <AppIcon name="x" class="h-4 w-4" />
              </button>
            </div>
          </li>
        </ul>
      </AdminPanel>

      <!-- System health -->
      <AdminPanel title="Tình trạng hệ thống" subtitle="Uptime 99.98%" icon="shield" accent="cyan">
        <div class="grid grid-cols-2 gap-3">
          <div v-for="h in health" :key="h.label" class="rounded-xl border border-white/[0.05] bg-white/[0.02] p-3 text-center">
            <div class="relative mx-auto w-fit">
              <RingProgress :value="h.value" :size="72" :stroke="7" :accent="h.accent" />
              <span class="absolute inset-0 flex items-center justify-center text-sm font-bold text-foreground">{{ h.value }}%</span>
            </div>
            <p class="mt-1.5 text-xs font-medium text-foreground">{{ h.label }}</p>
            <p class="font-mono text-[10px] text-muted-foreground">{{ h.unit }}</p>
          </div>
        </div>
      </AdminPanel>
    </div>

    <!-- Bottom row: weekly active + top content + activity -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <AdminPanel title="Người học theo ngày" subtitle="Tuần này" icon="flame" accent="amber">
        <BarChart :data="weeklyActive" accent="amber" :height="150" />
        <div class="mt-3 flex items-center justify-between rounded-xl border border-white/[0.05] bg-white/[0.02] px-3 py-2 text-xs">
          <span class="text-muted-foreground">Đỉnh ngày T5</span>
          <span class="font-mono font-semibold text-foreground">4.120 người</span>
        </div>
      </AdminPanel>

      <AdminPanel title="Nội dung nổi bật" subtitle="Theo lượt xem" icon="trending-up" accent="magenta">
        <ul class="space-y-3">
          <li v-for="(c, i) in topContent" :key="c.title">
            <div class="mb-1 flex items-center gap-2 text-xs">
              <span class="font-mono text-muted-foreground">{{ String(i + 1).padStart(2, '0') }}</span>
              <span class="min-w-0 flex-1 truncate text-foreground">{{ c.title }}</span>
              <span class="font-mono font-semibold text-foreground">{{ c.views.toLocaleString('vi-VN') }}</span>
            </div>
            <ProgressBar :value="c.pct" :accent="c.accent" />
          </li>
        </ul>
      </AdminPanel>

      <AdminPanel title="Hoạt động gần đây" subtitle="Nhật ký thời gian thực" icon="clock" accent="cyan">
        <ul class="space-y-3">
          <li v-for="(a, i) in activity" :key="i" class="flex gap-3">
            <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg" :class="tone[a.tone]">
              <AppIcon :name="a.icon" class="h-4 w-4" />
            </span>
            <div class="min-w-0 flex-1">
              <p class="text-sm text-foreground">{{ a.text }}</p>
              <p class="truncate text-xs text-muted-foreground">{{ a.meta }}</p>
            </div>
            <span class="shrink-0 font-mono text-[10px] text-muted-foreground">{{ a.time }}</span>
          </li>
        </ul>
      </AdminPanel>
    </div>

    <!-- Quick actions -->
    <AdminPanel title="Thao tác nhanh" icon="zap" accent="cyan">
      <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <button
          v-for="a in quickActions"
          :key="a.label"
          class="group flex flex-col items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.02] px-3 py-4 text-center transition-all hover:-translate-y-0.5 hover:bg-white/[0.05]"
          :class="qaTone[a.accent]"
        >
          <AppIcon :name="a.icon" class="h-6 w-6 transition-transform group-hover:scale-110" />
          <span class="text-xs font-medium text-foreground">{{ a.label }}</span>
        </button>
      </div>
    </AdminPanel>
  </div>
</template>
