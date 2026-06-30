<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Sparkline from '@/components/app/Sparkline.vue';
import Tabs from '@/components/app/Tabs.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type TxnType = 'earn' | 'spend';
type TxnFilter = 'all' | 'earn' | 'spend';

interface Txn {
  id: number;
  label: string;
  icon: string;
  type: TxnType;
  amount: number;
  time: string;
  accent: Accent;
}

interface SourceSlice {
  label: string;
  value: number;
  accent: Accent;
}

interface TopUpPackage {
  id: string;
  coins: number;
  bonus: number;
  price: string;
  popular?: boolean;
  accent: Accent;
}

// ── State ────────────────────────────────────────────────────────────────────
const balanceTarget = 1250;
const displayBalance = ref<number>(0);
const gems = ref<number>(8);
const earnedThisMonth = 2840;
const spentThisMonth = 1610;
const activeFilter = ref<TxnFilter>('all');
const toast = ref<string>('');

let rafId = 0;
let toastTimer: ReturnType<typeof setTimeout> | null = null;

const balanceTrend: number[] = [620, 700, 880, 760, 960, 1040, 980, 1120, 1080, 1180, 1210, 1250];

const tabs = [
  { id: 'all', label: 'Tất cả' },
  { id: 'earn', label: 'Nhận' },
  { id: 'spend', label: 'Tiêu' },
];

// ── Mock data ────────────────────────────────────────────────────────────────
const transactions: Txn[] = [
  { id: 1, label: 'Hoàn thành nhiệm vụ "Đọc 3 tài liệu"', icon: 'target', type: 'earn', amount: 50, time: 'Hôm nay · 09:12', accent: 'green' },
  { id: 2, label: 'Quà điểm danh hằng ngày', icon: 'gift', type: 'earn', amount: 40, time: 'Hôm nay · 08:00', accent: 'amber' },
  { id: 3, label: 'Mua "x2 XP (1 giờ)"', icon: 'zap', type: 'spend', amount: 80, time: 'Hôm qua · 21:30', accent: 'violet' },
  { id: 4, label: 'Hoàn thành bài quiz Toán', icon: 'check-circle', type: 'earn', amount: 35, time: 'Hôm qua · 18:45', accent: 'cyan' },
  { id: 5, label: 'Mở khoá bộ sưu tập VIP', icon: 'unlock', type: 'spend', amount: 200, time: 'Hôm qua · 16:10', accent: 'magenta' },
  { id: 6, label: 'Thưởng thành tựu "Chăm chỉ"', icon: 'award', type: 'earn', amount: 120, time: '2 ngày trước', accent: 'violet' },
  { id: 7, label: 'Vòng quay may mắn', icon: 'refresh', type: 'spend', amount: 100, time: '2 ngày trước', accent: 'amber' },
  { id: 8, label: 'Đứng top 3 bảng xếp hạng tuần', icon: 'trophy', type: 'earn', amount: 150, time: '3 ngày trước', accent: 'amber' },
  { id: 9, label: 'Đổi khung avatar "Tinh vân"', icon: 'palette', type: 'spend', amount: 60, time: '4 ngày trước', accent: 'cyan' },
];

const sources: SourceSlice[] = [
  { label: 'Nhiệm vụ', value: 980, accent: 'green' },
  { label: 'Điểm danh', value: 620, accent: 'amber' },
  { label: 'Quiz', value: 740, accent: 'cyan' },
  { label: 'Thành tựu', value: 500, accent: 'violet' },
];

const packages: TopUpPackage[] = [
  { id: 'p1', coins: 500, bonus: 0, price: '20.000đ', accent: 'cyan' },
  { id: 'p2', coins: 1200, bonus: 100, price: '45.000đ', accent: 'green' },
  { id: 'p3', coins: 3000, bonus: 400, price: '99.000đ', popular: true, accent: 'amber' },
  { id: 'p4', coins: 6500, bonus: 1200, price: '199.000đ', accent: 'magenta' },
];

const quickActions = [
  { id: 'nap', label: 'Nạp coin', icon: 'plus', accent: 'green' as Accent },
  { id: 'doi', label: 'Đổi thưởng', icon: 'gift', accent: 'amber' as Accent },
  { id: 'chuyen', label: 'Chuyển', icon: 'share', accent: 'cyan' as Accent },
];

// ── Derived ──────────────────────────────────────────────────────────────────
const sourcesTotal = computed<number>(() => sources.reduce((s, x) => s + x.value, 0));
const filteredTxns = computed<Txn[]>(() =>
  activeFilter.value === 'all'
    ? transactions
    : transactions.filter((t) => t.type === activeFilter.value)
);
const netThisMonth = computed<number>(() => earnedThisMonth - spentThisMonth);

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};
const accentBar: Record<Accent, string> = {
  cyan: 'bg-cyan-400',
  magenta: 'bg-fuchsia-400',
  green: 'bg-emerald-400',
  violet: 'bg-violet-400',
  amber: 'bg-amber-400',
};

// donut: build stacked conic-gradient
const donutGradient = computed<string>(() => {
  let acc = 0;
  const colorMap: Record<Accent, string> = {
    cyan: 'oklch(0.78 0.18 195)',
    magenta: 'oklch(0.68 0.22 350)',
    green: 'oklch(0.75 0.18 145)',
    violet: 'oklch(0.62 0.2 290)',
    amber: 'oklch(0.8 0.16 85)',
  };
  const parts = sources.map((s) => {
    const start = (acc / sourcesTotal.value) * 360;
    acc += s.value;
    const end = (acc / sourcesTotal.value) * 360;
    return `${colorMap[s.accent]} ${start}deg ${end}deg`;
  });
  return `conic-gradient(${parts.join(', ')})`;
});

// ── Count-up ──────────────────────────────────────────────────────────────
function animateBalance(): void {
  const duration = 1300;
  const start = performance.now();
  const step = (now: number): void => {
    const t = Math.min(1, (now - start) / duration);
    const eased = 1 - Math.pow(1 - t, 3); // easeOutCubic
    displayBalance.value = Math.round(balanceTarget * eased);
    if (t < 1) rafId = requestAnimationFrame(step);
  };
  rafId = requestAnimationFrame(step);
}

function showToast(msg: string): void {
  toast.value = msg;
  if (toastTimer) clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.value = '';
  }, 2200);
}

function onAction(id: string, label: string): void {
  if (id === 'nap') showToast('Tính năng nạp coin sẽ sớm ra mắt 🚀');
  else if (id === 'doi') showToast('Mở cửa hàng đổi thưởng (demo)');
  else showToast('Đã mở hộp thoại chuyển coin (demo)');
  void label;
}

function buyPackage(p: TopUpPackage): void {
  showToast(`Đã chọn gói ${p.coins.toLocaleString('vi-VN')} coin · ${p.price}`);
}

onMounted(() => {
  animateBalance();
});

onUnmounted(() => {
  if (rafId) cancelAnimationFrame(rafId);
  if (toastTimer) clearTimeout(toastTimer);
});
</script>

<template>
  <div class="space-y-6">
    <!-- Hero / balance -->
    <GlassPanel glow="amber" class="p-5 sm:p-6">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0 flex-1">
          <p class="cosmic-badge">Tài chính · Ví</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Ví của tôi</span> 💰
          </h1>
          <div class="mt-4 flex flex-wrap items-end gap-6">
            <div>
              <p class="text-[0.7rem] uppercase tracking-wider text-muted-foreground">Số dư coin</p>
              <p class="mt-1 flex items-center gap-2">
                <AppIcon name="coin" class="h-8 w-8 text-amber-300" />
                <span class="text-4xl font-bold tabular-nums text-amber-100 glow-magenta sm:text-5xl">
                  {{ displayBalance.toLocaleString('vi-VN') }}
                </span>
              </p>
            </div>
            <div>
              <p class="text-[0.7rem] uppercase tracking-wider text-muted-foreground">Số dư gem</p>
              <p class="mt-1 flex items-center gap-2">
                <AppIcon name="gem" class="h-6 w-6 text-fuchsia-300" />
                <span class="text-3xl font-bold tabular-nums text-fuchsia-100">{{ gems }}</span>
              </p>
            </div>
          </div>

          <!-- Quick actions -->
          <div class="mt-5 flex flex-wrap gap-2">
            <button
              v-for="qa in quickActions"
              :key="qa.id"
              type="button"
              class="inline-flex items-center gap-2 rounded-sm border border-white/[0.08] bg-white/[0.03] px-3.5 py-2 text-sm font-medium text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
              @click="onAction(qa.id, qa.label)"
            >
              <span class="flex h-6 w-6 items-center justify-center rounded-sm" :class="accentChip[qa.accent]">
                <AppIcon :name="qa.icon" class="h-3.5 w-3.5" />
              </span>
              {{ qa.label }}
            </button>
          </div>
        </div>

        <!-- Trend + month summary -->
        <div class="w-full max-w-sm shrink-0 rounded-sm border border-white/[0.06] bg-white/[0.02] p-4">
          <div class="mb-1 flex items-center justify-between text-xs text-muted-foreground">
            <span>Xu hướng số dư · 12 tuần</span>
            <span class="font-semibold text-emerald-300">+{{ netThisMonth.toLocaleString('vi-VN') }} ròng</span>
          </div>
          <Sparkline :data="balanceTrend" accent="amber" :height="56" />
          <div class="mt-3 grid grid-cols-2 gap-3">
            <div class="rounded-sm border border-emerald-400/20 bg-emerald-500/[0.07] p-3">
              <p class="flex items-center gap-1 text-[0.7rem] text-muted-foreground">
                <AppIcon name="trending-up" class="h-3.5 w-3.5 text-emerald-300" /> Nhận tháng này
              </p>
              <p class="mt-1 text-lg font-bold tabular-nums text-emerald-200">+{{ earnedThisMonth.toLocaleString('vi-VN') }}</p>
            </div>
            <div class="rounded-sm border border-rose-400/20 bg-rose-500/[0.07] p-3">
              <p class="flex items-center gap-1 text-[0.7rem] text-muted-foreground">
                <AppIcon name="trending-down" class="h-3.5 w-3.5 text-rose-300" /> Tiêu tháng này
              </p>
              <p class="mt-1 text-lg font-bold tabular-nums text-rose-200">-{{ spentThisMonth.toLocaleString('vi-VN') }}</p>
            </div>
          </div>
        </div>
      </div>
    </GlassPanel>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Transactions -->
      <div class="lg:col-span-2">
        <SectionPanel title="Lịch sử giao dịch" subtitle="Lọc theo loại giao dịch" icon="list" accent="cyan" flush>
          <template #header-actions>
            <Tabs :tabs="tabs" :model-value="activeFilter" @update:model-value="(id) => (activeFilter = id as TxnFilter)" />
          </template>
          <ul class="divide-y divide-white/[0.05]">
            <li v-for="txn in filteredTxns" :key="txn.id" class="flex items-center gap-3 px-5 py-3">
              <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-sm" :class="accentChip[txn.accent]">
                <AppIcon :name="txn.icon" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ txn.label }}</p>
                <p class="text-[0.7rem] text-muted-foreground">{{ txn.time }}</p>
              </div>
              <span
                class="shrink-0 text-sm font-bold tabular-nums"
                :class="txn.type === 'earn' ? 'text-emerald-300' : 'text-rose-300'"
              >
                {{ txn.type === 'earn' ? '+' : '-' }}{{ txn.amount }}
              </span>
            </li>
          </ul>
          <p v-if="!filteredTxns.length" class="px-5 py-8 text-center text-sm text-muted-foreground">
            Không có giao dịch phù hợp bộ lọc.
          </p>
        </SectionPanel>
      </div>

      <!-- Earnings breakdown -->
      <div class="space-y-6">
        <SectionPanel title="Nguồn thu coin" subtitle="Theo hoạt động" icon="chart" accent="green">
          <div class="flex items-center gap-5">
            <div class="relative h-28 w-28 shrink-0 rounded-sm" :style="{ background: donutGradient }">
              <div class="absolute inset-[18%] flex flex-col items-center justify-center rounded-sm bg-background/80">
                <span class="text-lg font-bold tabular-nums text-foreground">{{ sourcesTotal.toLocaleString('vi-VN') }}</span>
                <span class="text-[0.6rem] text-muted-foreground">tổng coin</span>
              </div>
            </div>
            <ul class="min-w-0 flex-1 space-y-2.5">
              <li v-for="s in sources" :key="s.label" class="flex items-center gap-2 text-sm">
                <span class="h-2.5 w-2.5 shrink-0 rounded-sm" :class="accentBar[s.accent]" />
                <span class="flex-1 truncate text-muted-foreground">{{ s.label }}</span>
                <span class="font-semibold tabular-nums text-foreground">{{ Math.round((s.value / sourcesTotal) * 100) }}%</span>
              </li>
            </ul>
          </div>
        </SectionPanel>

        <SectionPanel title="Mẹo kiếm coin" icon="lightbulb" accent="amber">
          <ul class="space-y-2.5 text-sm">
            <li class="flex items-start gap-2">
              <AppIcon name="check-circle" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-300" />
              <span class="text-muted-foreground">Điểm danh đủ 7 ngày để nhận hộp quà 200 coin.</span>
            </li>
            <li class="flex items-start gap-2">
              <AppIcon name="check-circle" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-300" />
              <span class="text-muted-foreground">Hoàn thành nhiệm vụ hằng ngày để nhận tới 120 coin.</span>
            </li>
            <li class="flex items-start gap-2">
              <AppIcon name="check-circle" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-300" />
              <span class="text-muted-foreground">Đứng top bảng xếp hạng tuần để nhận thưởng lớn.</span>
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <!-- Top-up packages -->
    <SectionPanel title="Nạp thêm coin" subtitle="Mua coin để mở khoá vật phẩm và quay thưởng" icon="store" accent="violet">
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div
          v-for="p in packages"
          :key="p.id"
          class="package-card relative flex flex-col rounded-sm border p-4 transition-all hover:-translate-y-1"
          :class="p.popular ? 'border-amber-400/40 bg-amber-500/[0.06]' : 'border-white/[0.06] bg-white/[0.02] hover:border-white/[0.14]'"
        >
          <span
            v-if="p.popular"
            class="absolute -top-2.5 right-4 rounded-sm bg-amber-400 px-2.5 py-0.5 text-[0.65rem] font-bold uppercase tracking-wide text-black"
          >Phổ biến</span>
          <span class="flex h-12 w-12 items-center justify-center rounded-sm" :class="accentChip[p.accent]">
            <AppIcon name="coin" class="h-6 w-6" />
          </span>
          <p class="mt-3 text-2xl font-bold tabular-nums text-foreground">{{ p.coins.toLocaleString('vi-VN') }}</p>
          <p class="text-xs text-muted-foreground">coin</p>
          <p v-if="p.bonus" class="mt-1 text-xs font-semibold text-emerald-300">+{{ p.bonus }} bonus 🎁</p>
          <p v-else class="mt-1 text-xs text-muted-foreground">Không bonus</p>
          <button
            type="button"
            class="cosmic-btn mt-4 w-full rounded-sm px-3 py-2 text-sm"
            @click="buyPackage(p)"
          >
            {{ p.price }}
          </button>
        </div>
      </div>
    </SectionPanel>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>

    <!-- Toast -->
    <Transition name="toast-fade">
      <div
        v-if="toast"
        class="fixed bottom-6 left-1/2 z-50 -translate-x-1/2 rounded-sm border border-cyan-400/30 bg-background/90 px-5 py-3 text-sm font-medium text-cyan-100 backdrop-blur"
        role="status"
      >
        {{ toast }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.package-card {
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}
.package-card:hover {
  box-shadow: 0 12px 40px oklch(0 0 0 / 45%), 0 0 28px oklch(0.62 0.2 290 / 18%);
}

.toast-fade-enter-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.toast-fade-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.toast-fade-enter-from,
.toast-fade-leave-to { opacity: 0; transform: translate(-50%, 12px); }
</style>
