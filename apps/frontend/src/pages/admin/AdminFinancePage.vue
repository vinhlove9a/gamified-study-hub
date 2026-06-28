<script setup lang="ts">
/**
 * Trang TÀI CHÍNH & KINH TẾ ẢO — bảng điều khiển doanh thu, gói VIP, giao dịch
 * và kinh tế ảo (coin / gem / hoàn tiền). Dữ liệu mock, tương tác thật:
 * lọc/tìm giao dịch + đổi khoảng thời gian biểu đồ. Khớp style AdminOverviewPage.
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
type PillTone = 'cyan' | 'green' | 'amber' | 'rose' | 'violet' | 'magenta' | 'neutral';
type TxStatus = 'success' | 'pending' | 'failed' | 'refunded';
type TxMethod = 'momo' | 'vnpay' | 'card';

/* ---------- Helpers định dạng tiền ---------- */
const vnd = (n: number) => `₫${n.toLocaleString('vi-VN')}`;

/* ---------- Header: chọn khoảng thời gian ---------- */
const ranges = ['Tháng này', 'Quý', 'Năm'] as const;
type Range = (typeof ranges)[number];
const range = ref<Range>('Tháng này');

/* ---------- KPI ---------- */
interface Kpi {
  label: string;
  value: number;
  prefix?: string;
  suffix?: string;
  unit?: string;
  decimals?: number;
  icon: string;
  accent: Accent;
  trend?: number;
  trendLabel: string;
  spark: number[];
}
const kpis: Kpi[] = [
  {
    label: 'Doanh thu tháng',
    value: 86.4,
    prefix: '₫',
    suffix: 'M',
    decimals: 1,
    icon: 'coin',
    accent: 'amber',
    trend: 18.2,
    trendLabel: 'VIP + cửa hàng coin',
    spark: [58, 62, 66, 70, 74, 79, 83, 86.4]
  },
  {
    label: 'MRR (định kỳ)',
    value: 64.2,
    prefix: '₫',
    suffix: 'M',
    decimals: 1,
    icon: 'repeat',
    accent: 'green',
    trend: 9.4,
    trendLabel: 'Doanh thu gia hạn',
    spark: [48, 51, 53, 56, 58, 60, 62, 64.2]
  },
  {
    label: 'ARPU',
    value: 6950,
    prefix: '₫',
    unit: '/ người',
    icon: 'user',
    accent: 'cyan',
    trend: 4.1,
    trendLabel: 'Bình quân mỗi người dùng',
    spark: [6100, 6240, 6380, 6500, 6650, 6770, 6880, 6950]
  },
  {
    label: 'Người dùng VIP',
    value: 1840,
    icon: 'crown',
    accent: 'violet',
    trend: 12.6,
    trendLabel: '14.8% người dùng trả phí',
    spark: [1480, 1540, 1600, 1660, 1700, 1760, 1800, 1840]
  }
];

/* ---------- Biểu đồ doanh thu theo thời gian (đổi khoảng thực) ---------- */
interface ChartSeries {
  key: string;
  name: string;
  color: string;
  values: number[];
  area?: boolean;
}
interface ChartData {
  labels: string[];
  series: ChartSeries[];
  growth: number;
}

function wave(labels: string[], base: number, amp: number, phase: number, slope: number): number[] {
  return labels.map((_, i) =>
    Math.round((base + slope * i + Math.sin(i * 0.5 + phase) * amp + Math.cos(i * 0.23) * amp * 0.4) * 10) / 10
  );
}

const chartByRange: Record<Range, ChartData> = (() => {
  const monthLabels = Array.from({ length: 30 }, (_, i) => `${i + 1}`);
  const quarterLabels = ['T1', 'T2', 'T3'].flatMap((m) => [`${m}.1`, `${m}.2`, `${m}.3`, `${m}.4`]);
  const yearLabels = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
  return {
    'Tháng này': {
      labels: monthLabels,
      series: [
        { key: 'vip', name: 'Gói VIP', color: 'oklch(0.62 0.2 290)', values: wave(monthLabels, 1.8, 0.5, 0, 0.04), area: true },
        { key: 'shop', name: 'Cửa hàng coin', color: 'oklch(0.78 0.18 195)', values: wave(monthLabels, 1.1, 0.4, 1.5, 0.02), area: true }
      ],
      growth: 18.2
    },
    Quý: {
      labels: quarterLabels,
      series: [
        { key: 'vip', name: 'Gói VIP', color: 'oklch(0.62 0.2 290)', values: wave(quarterLabels, 11, 2.4, 0, 0.6), area: true },
        { key: 'shop', name: 'Cửa hàng coin', color: 'oklch(0.78 0.18 195)', values: wave(quarterLabels, 7.5, 1.6, 1.5, 0.4), area: true }
      ],
      growth: 24.7
    },
    Năm: {
      labels: yearLabels,
      series: [
        { key: 'vip', name: 'Gói VIP', color: 'oklch(0.62 0.2 290)', values: wave(yearLabels, 38, 6, 0, 2.4), area: true },
        { key: 'shop', name: 'Cửa hàng coin', color: 'oklch(0.78 0.18 195)', values: wave(yearLabels, 24, 4, 1.5, 1.6), area: true }
      ],
      growth: 41.3
    }
  };
})();

const chart = computed(() => chartByRange[range.value]);

/* ---------- Gói VIP ---------- */
interface VipTier {
  name: string;
  users: number;
  pct: number;
  revenue: number;
  accent: Accent;
}
const vipTiers: VipTier[] = [
  { name: 'Gói Tháng', users: 1120, pct: 61, revenue: 33360000, accent: 'cyan' },
  { name: 'Gói Quý', users: 524, pct: 28, revenue: 41920000, accent: 'violet' },
  { name: 'Gói Năm', users: 196, pct: 11, revenue: 56840000, accent: 'amber' }
];
const vipTotalUsers = vipTiers.reduce((s, t) => s + t.users, 0);
const vipRenewRate = 78;

/* ---------- Giao dịch gần đây ---------- */
interface Transaction {
  code: string;
  user: string;
  initials: string;
  product: string;
  amount: number;
  method: TxMethod;
  status: TxStatus;
  time: string;
}
const transactions: Transaction[] = [
  { code: 'TXN-9F3A21', user: 'Nguyễn Minh Anh', initials: 'MA', product: 'VIP 12 tháng', amount: 1290000, method: 'momo', status: 'success', time: '2 phút' },
  { code: 'TXN-9F3A18', user: 'Trần Quốc Bảo', initials: 'QB', product: '5.000 coin', amount: 99000, method: 'vnpay', status: 'success', time: '8 phút' },
  { code: 'TXN-9F3A14', user: 'Lê Thu Hà', initials: 'TH', product: 'Gem pack 600', amount: 199000, method: 'card', status: 'pending', time: '15 phút' },
  { code: 'TXN-9F3A0E', user: 'Phạm Đức Huy', initials: 'ĐH', product: 'VIP 3 tháng', amount: 399000, method: 'momo', status: 'success', time: '24 phút' },
  { code: 'TXN-9F39FA', user: 'Vũ Hoàng Nam', initials: 'HN', product: '12.000 coin', amount: 199000, method: 'vnpay', status: 'failed', time: '31 phút' },
  { code: 'TXN-9F39E7', user: 'Đỗ Khánh Linh', initials: 'KL', product: 'VIP 1 tháng', amount: 149000, method: 'card', status: 'success', time: '46 phút' },
  { code: 'TXN-9F39D2', user: 'Bùi Gia Bảo', initials: 'GB', product: 'Gem pack 1.500', amount: 449000, method: 'momo', status: 'refunded', time: '1 giờ' },
  { code: 'TXN-9F39C5', user: 'Hoàng Tuấn Kiệt', initials: 'TK', product: '5.000 coin', amount: 99000, method: 'vnpay', status: 'success', time: '1 giờ' },
  { code: 'TXN-9F39B0', user: 'Đặng Mỹ Duyên', initials: 'MD', product: 'VIP 12 tháng', amount: 1290000, method: 'card', status: 'pending', time: '2 giờ' },
  { code: 'TXN-9F399E', user: 'Ngô Thanh Tùng', initials: 'TT', product: '2.000 coin', amount: 49000, method: 'momo', status: 'success', time: '2 giờ' },
  { code: 'TXN-9F3988', user: 'Trịnh Bảo Châu', initials: 'BC', product: 'Gem pack 600', amount: 199000, method: 'vnpay', status: 'success', time: '3 giờ' },
  { code: 'TXN-9F3975', user: 'Lý Quang Vinh', initials: 'QV', product: 'VIP 3 tháng', amount: 399000, method: 'card', status: 'failed', time: '3 giờ' },
  { code: 'TXN-9F3960', user: 'Phan Thảo Nhi', initials: 'TN', product: '12.000 coin', amount: 199000, method: 'momo', status: 'success', time: '4 giờ' },
  { code: 'TXN-9F394C', user: 'Dương Anh Tú', initials: 'AT', product: 'VIP 1 tháng', amount: 149000, method: 'vnpay', status: 'refunded', time: '4 giờ' },
  { code: 'TXN-9F3938', user: 'Mai Hồng Quân', initials: 'HQ', product: 'Gem pack 1.500', amount: 449000, method: 'card', status: 'success', time: '5 giờ' },
  { code: 'TXN-9F3921', user: 'Cao Diệu Linh', initials: 'DL', product: '5.000 coin', amount: 99000, method: 'momo', status: 'pending', time: '5 giờ' },
  { code: 'TXN-9F390A', user: 'Hồ Nhật Minh', initials: 'NM', product: 'VIP 12 tháng', amount: 1290000, method: 'vnpay', status: 'success', time: '6 giờ' },
  { code: 'TXN-9F38F3', user: 'Tạ Phương Thảo', initials: 'PT', product: '2.000 coin', amount: 49000, method: 'card', status: 'success', time: '6 giờ' }
];

interface StatusFilter {
  key: TxStatus | 'all';
  label: string;
}
const statusFilters: StatusFilter[] = [
  { key: 'all', label: 'Tất cả' },
  { key: 'success', label: 'Thành công' },
  { key: 'pending', label: 'Đang xử lý' },
  { key: 'failed', label: 'Thất bại' },
  { key: 'refunded', label: 'Hoàn tiền' }
];

interface MethodMeta {
  label: string;
  icon: string;
  tone: PillTone;
}
const methodMeta: Record<TxMethod, MethodMeta> = {
  momo: { label: 'Momo', icon: 'phone', tone: 'magenta' },
  vnpay: { label: 'VNPay', icon: 'wifi', tone: 'cyan' },
  card: { label: 'Thẻ', icon: 'badge', tone: 'violet' }
};

interface StatusMeta {
  label: string;
  tone: PillTone;
}
const statusMeta: Record<TxStatus, StatusMeta> = {
  success: { label: 'Thành công', tone: 'green' },
  pending: { label: 'Đang xử lý', tone: 'amber' },
  failed: { label: 'Thất bại', tone: 'rose' },
  refunded: { label: 'Hoàn tiền', tone: 'violet' }
};

const search = ref('');
const statusFilter = ref<TxStatus | 'all'>('all');
const methodFilter = ref<TxMethod | 'all'>('all');
const methodFilters: { key: TxMethod | 'all'; label: string }[] = [
  { key: 'all', label: 'Mọi PT' },
  { key: 'momo', label: 'Momo' },
  { key: 'vnpay', label: 'VNPay' },
  { key: 'card', label: 'Thẻ' }
];

const filteredTx = computed(() => {
  const q = search.value.trim().toLowerCase();
  return transactions.filter((t) => {
    if (statusFilter.value !== 'all' && t.status !== statusFilter.value) return false;
    if (methodFilter.value !== 'all' && t.method !== methodFilter.value) return false;
    if (q && !t.code.toLowerCase().includes(q) && !t.user.toLowerCase().includes(q)) return false;
    return true;
  });
});

const filteredTotal = computed(() => filteredTx.value.reduce((s, t) => s + t.amount, 0));

/* ---------- Kinh tế ảo ---------- */
const coinIssued = 24_800_000;
const coinSpent = 18_120_000;
const coinCirculating = coinIssued - coinSpent;
const coinSpentPct = Math.round((coinSpent / coinIssued) * 100);

const gemSold = 412_000;
const gemSpent = 286_400;
const gemBalance = gemSold - gemSpent;
const gemSpentPct = Math.round((gemSpent / gemSold) * 100);

interface RefundItem {
  code: string;
  reason: string;
  amount: number;
  tone: PillTone;
  label: string;
}
const refunds: RefundItem[] = [
  { code: 'TXN-9F39D2', reason: 'Mua nhầm gói', amount: 449000, tone: 'violet', label: 'Đã hoàn' },
  { code: 'TXN-9F394C', reason: 'Lỗi kích hoạt VIP', amount: 149000, tone: 'violet', label: 'Đã hoàn' },
  { code: 'TXN-9F38C1', reason: 'Tranh chấp ngân hàng', amount: 199000, tone: 'amber', label: 'Đang xem xét' }
];
const refundTotal = refunds.reduce((s, r) => s + r.amount, 0);
const disputeCount = 2;
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2">
          <span class="h-1.5 w-1.5 rounded-full bg-amber-400 pulse-glow-green" />
          <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">Tài chính · Kinh tế ảo</p>
        </div>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Doanh thu &amp; Kinh tế ảo 💰</h2>
      </div>
      <div class="flex items-center gap-2">
        <div class="flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
          <button
            v-for="r in ranges"
            :key="r"
            class="rounded-lg px-3 py-1.5 text-xs font-medium transition-colors"
            :class="range === r ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
            @click="range = r"
          >
            {{ r }}
          </button>
        </div>
        <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
          <AppIcon name="file-text" class="h-4 w-4" /> Xuất hoá đơn
        </button>
      </div>
    </div>

    <!-- KPI row -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
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

    <!-- Revenue chart + VIP breakdown -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <AdminPanel
        class="lg:col-span-2"
        title="Doanh thu theo thời gian"
        :subtitle="`Đơn vị: triệu ₫ · ${range}`"
        icon="chart"
        accent="amber"
        glow="magenta"
      >
        <template #actions>
          <AdminPill tone="green" dot>+{{ chart.growth }}%</AdminPill>
        </template>
        <AdminAreaChart :labels="chart.labels" :series="chart.series" :height="280" :decimals="1" suffix=" triệu" />
      </AdminPanel>

      <AdminPanel title="Gói VIP" subtitle="Phân rã theo gói" icon="crown" accent="violet">
        <div class="flex flex-col items-center">
          <RingProgress :value="vipRenewRate" :size="132" :stroke="12" accent="violet">
            <span class="block text-2xl font-bold text-foreground">{{ vipTotalUsers.toLocaleString('vi-VN') }}</span>
            <span class="text-[11px] text-muted-foreground">người VIP</span>
          </RingProgress>
          <p class="mt-2 text-[11px] text-muted-foreground">Tỷ lệ gia hạn <span class="font-mono font-semibold text-violet-300">{{ vipRenewRate }}%</span></p>
        </div>
        <ul class="mt-4 space-y-3">
          <li v-for="t in vipTiers" :key="t.name">
            <div class="mb-1 flex items-center justify-between text-xs">
              <span class="text-muted-foreground">{{ t.name }}</span>
              <span class="font-mono font-semibold text-foreground">{{ t.users.toLocaleString('vi-VN') }} người</span>
            </div>
            <ProgressBar :value="t.pct" :accent="t.accent" />
            <p class="mt-1 text-right font-mono text-[10px] text-muted-foreground">{{ vnd(t.revenue) }}</p>
          </li>
        </ul>
      </AdminPanel>
    </div>

    <!-- Recent transactions -->
    <AdminPanel title="Giao dịch gần đây" :subtitle="`${filteredTx.length} giao dịch · ${vnd(filteredTotal)}`" icon="coin" accent="cyan" flush>
      <!-- Toolbar -->
      <div class="flex flex-col gap-3 border-b border-white/[0.06] px-5 pb-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="relative max-w-xs">
          <AppIcon name="search" class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <input
            v-model="search"
            type="text"
            placeholder="Tìm theo mã GD hoặc tên..."
            class="cosmic-input w-full rounded-xl py-2 pl-9 pr-3 text-sm"
          />
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <!-- Status segmented -->
          <div class="flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
            <button
              v-for="s in statusFilters"
              :key="s.key"
              class="rounded-lg px-2.5 py-1 text-[11px] font-medium transition-colors"
              :class="statusFilter === s.key ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
              @click="statusFilter = s.key"
            >
              {{ s.label }}
            </button>
          </div>
          <!-- Method segmented -->
          <div class="flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
            <button
              v-for="m in methodFilters"
              :key="m.key"
              class="rounded-lg px-2.5 py-1 text-[11px] font-medium transition-colors"
              :class="methodFilter === m.key ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
              @click="methodFilter = m.key"
            >
              {{ m.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="max-h-[28rem] overflow-auto">
        <table class="w-full min-w-[860px] text-sm">
          <thead class="sticky top-0 z-10 bg-[oklch(0.12_0.02_280)]/90 backdrop-blur">
            <tr class="text-left text-[11px] uppercase tracking-wider text-muted-foreground">
              <th class="px-5 py-2.5 font-medium">Mã GD</th>
              <th class="px-3 py-2.5 font-medium">Người dùng</th>
              <th class="px-3 py-2.5 font-medium">Sản phẩm</th>
              <th class="px-3 py-2.5 text-right font-medium">Số tiền</th>
              <th class="px-3 py-2.5 font-medium">Phương thức</th>
              <th class="px-3 py-2.5 font-medium">Trạng thái</th>
              <th class="px-5 py-2.5 text-right font-medium">Thời gian</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="t in filteredTx"
              :key="t.code"
              class="border-t border-white/[0.04] transition-colors hover:bg-white/[0.03]"
            >
              <td class="px-5 py-2.5">
                <span class="font-mono text-xs text-cyan-300">#{{ t.code }}</span>
              </td>
              <td class="px-3 py-2.5">
                <div class="flex items-center gap-2.5">
                  <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-white/[0.06] text-[11px] font-semibold text-foreground">{{ t.initials }}</span>
                  <span class="whitespace-nowrap text-foreground">{{ t.user }}</span>
                </div>
              </td>
              <td class="px-3 py-2.5 whitespace-nowrap text-muted-foreground">{{ t.product }}</td>
              <td class="px-3 py-2.5 text-right">
                <span class="font-mono font-semibold tabular-nums text-foreground">{{ vnd(t.amount) }}</span>
              </td>
              <td class="px-3 py-2.5">
                <AdminPill :tone="methodMeta[t.method].tone">
                  <AppIcon :name="methodMeta[t.method].icon" class="h-3 w-3" />
                  {{ methodMeta[t.method].label }}
                </AdminPill>
              </td>
              <td class="px-3 py-2.5">
                <AdminPill :tone="statusMeta[t.status].tone" dot>{{ statusMeta[t.status].label }}</AdminPill>
              </td>
              <td class="px-5 py-2.5 text-right">
                <span class="whitespace-nowrap font-mono text-[11px] text-muted-foreground">{{ t.time }} trước</span>
              </td>
            </tr>
            <tr v-if="filteredTx.length === 0">
              <td colspan="7" class="px-5 py-10 text-center text-sm text-muted-foreground">
                <AppIcon name="search" class="mx-auto mb-2 h-6 w-6 opacity-50" />
                Không tìm thấy giao dịch phù hợp.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </AdminPanel>

    <!-- Virtual economy row -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <!-- Coin -->
      <AdminPanel title="Coin lưu hành" subtitle="Phát hành so với đã tiêu" icon="coin" accent="amber">
        <div class="flex items-center gap-4">
          <RingProgress :value="coinSpentPct" :size="100" :stroke="10" accent="amber">
            <span class="block text-lg font-bold text-foreground">{{ coinSpentPct }}%</span>
            <span class="text-[10px] text-muted-foreground">đã tiêu</span>
          </RingProgress>
          <ul class="flex-1 space-y-2.5 text-sm">
            <li class="flex items-center justify-between">
              <span class="text-muted-foreground">Đã phát hành</span>
              <span class="font-mono font-semibold text-foreground">{{ coinIssued.toLocaleString('vi-VN') }}</span>
            </li>
            <li class="flex items-center justify-between">
              <span class="text-muted-foreground">Đã tiêu</span>
              <span class="font-mono font-semibold text-amber-300">{{ coinSpent.toLocaleString('vi-VN') }}</span>
            </li>
            <li class="flex items-center justify-between border-t border-white/[0.06] pt-2">
              <span class="text-muted-foreground">Đang lưu hành</span>
              <span class="font-mono font-semibold text-cyan-300">{{ coinCirculating.toLocaleString('vi-VN') }}</span>
            </li>
          </ul>
        </div>
      </AdminPanel>

      <!-- Gem -->
      <AdminPanel title="Gem" subtitle="Đã bán so với đã tiêu" icon="gem" accent="magenta">
        <div class="flex items-center gap-4">
          <RingProgress :value="gemSpentPct" :size="100" :stroke="10" accent="magenta">
            <span class="block text-lg font-bold text-foreground">{{ gemSpentPct }}%</span>
            <span class="text-[10px] text-muted-foreground">đã tiêu</span>
          </RingProgress>
          <ul class="flex-1 space-y-2.5 text-sm">
            <li class="flex items-center justify-between">
              <span class="text-muted-foreground">Đã bán</span>
              <span class="font-mono font-semibold text-foreground">{{ gemSold.toLocaleString('vi-VN') }}</span>
            </li>
            <li class="flex items-center justify-between">
              <span class="text-muted-foreground">Đã tiêu</span>
              <span class="font-mono font-semibold text-fuchsia-300">{{ gemSpent.toLocaleString('vi-VN') }}</span>
            </li>
            <li class="flex items-center justify-between border-t border-white/[0.06] pt-2">
              <span class="text-muted-foreground">Số dư ví</span>
              <span class="font-mono font-semibold text-cyan-300">{{ gemBalance.toLocaleString('vi-VN') }}</span>
            </li>
          </ul>
        </div>
      </AdminPanel>

      <!-- Refunds & disputes -->
      <AdminPanel title="Hoàn tiền &amp; tranh chấp" :subtitle="`${refunds.length} mục · ${vnd(refundTotal)}`" icon="alert" accent="violet">
        <div class="mb-3 grid grid-cols-2 gap-3">
          <div class="rounded-xl border border-white/[0.05] bg-white/[0.02] p-3 text-center">
            <p class="font-mono text-lg font-bold text-violet-300">{{ vnd(refundTotal) }}</p>
            <p class="text-[11px] text-muted-foreground">Tổng hoàn tiền</p>
          </div>
          <div class="rounded-xl border border-white/[0.05] bg-white/[0.02] p-3 text-center">
            <p class="font-mono text-lg font-bold text-amber-300">{{ disputeCount }}</p>
            <p class="text-[11px] text-muted-foreground">Tranh chấp mở</p>
          </div>
        </div>
        <ul class="space-y-2">
          <li
            v-for="r in refunds"
            :key="r.code"
            class="flex items-center gap-3 rounded-xl border border-white/[0.05] bg-white/[0.02] px-3 py-2"
          >
            <div class="min-w-0 flex-1">
              <p class="font-mono text-xs text-cyan-300">#{{ r.code }}</p>
              <p class="truncate text-[11px] text-muted-foreground">{{ r.reason }}</p>
            </div>
            <span class="shrink-0 font-mono text-xs font-semibold text-foreground">{{ vnd(r.amount) }}</span>
            <AdminPill :tone="r.tone" dot>{{ r.label }}</AdminPill>
          </li>
        </ul>
      </AdminPanel>
    </div>
  </div>
</template>
