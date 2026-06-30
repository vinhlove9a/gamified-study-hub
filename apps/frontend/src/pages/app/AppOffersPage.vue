<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Toggle from '@/components/app/Toggle.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface VipTier {
  id: string;
  name: string;
  tagline: string;
  icon: string;
  accent: Accent;
  monthly: number; // coin / month
  features: string[];
  popular?: boolean;
  current?: boolean;
}

interface FlashDeal {
  id: string;
  name: string;
  desc: string;
  icon: string;
  accent: Accent;
  oldPrice: number;
  newPrice: number;
  discount: number;
  endsAt: number; // epoch ms
  remaining: number; // seconds, ticked
}

interface Coupon {
  id: string;
  code: string;
  desc: string;
  accent: Accent;
  copied: boolean;
}

interface Bundle {
  id: string;
  name: string;
  desc: string;
  icon: string;
  accent: Accent;
  price: string;
  items: string[];
  left: number;
  total: number;
}

// ── State ────────────────────────────────────────────────────────────────────
const annual = ref<boolean>(false); // false = monthly, true = annual
const coins = ref<number>(1250);

let ticker: ReturnType<typeof setInterval> | null = null;

// ── Mock data ────────────────────────────────────────────────────────────────
const tiers: VipTier[] = [
  {
    id: 'bac',
    name: 'Bạc',
    tagline: 'Khởi đầu hành trình VIP',
    icon: 'shield',
    accent: 'cyan',
    monthly: 200,
    current: true,
    features: ['Bỏ quảng cáo', 'Đổi băng chuỗi 1 lần/tuần', '+10% coin nhiệm vụ', 'Khung avatar cơ bản'],
  },
  {
    id: 'vang',
    name: 'Vàng',
    tagline: 'Lựa chọn được yêu thích nhất',
    icon: 'crown',
    accent: 'amber',
    monthly: 450,
    popular: true,
    features: ['Tất cả quyền lợi Bạc', '+25% coin nhiệm vụ', '2 vé quay miễn phí/tuần', 'Khung avatar cao cấp', 'Huy hiệu Vàng độc quyền'],
  },
  {
    id: 'kimcuong',
    name: 'Kim cương',
    tagline: 'Trải nghiệm tối thượng',
    icon: 'gem',
    accent: 'magenta',
    monthly: 900,
    features: ['Tất cả quyền lợi Vàng', '+50% coin nhiệm vụ', '5 vé quay miễn phí/tuần', 'Quà sinh nhật đặc biệt', 'Ưu tiên hỗ trợ', 'Hiệu ứng hồ sơ Kim cương'],
  },
];

const flashDeals = ref<FlashDeal[]>([
  { id: 'f1', name: 'Combo x2 XP (24h)', desc: 'Nhân đôi XP cả ngày', icon: 'zap', accent: 'amber', oldPrice: 300, newPrice: 150, discount: 50, endsAt: 0, remaining: 2 * 3600 + 41 * 60 + 12 },
  { id: 'f2', name: 'Gói 10 vé quay', desc: 'Săn jackpot thả ga', icon: 'gift', accent: 'green', oldPrice: 1000, newPrice: 650, discount: 35, endsAt: 0, remaining: 5 * 3600 + 8 * 60 + 47 },
  { id: 'f3', name: 'Mở khoá thư viện VIP', desc: '120+ tài liệu độc quyền', icon: 'unlock', accent: 'violet', oldPrice: 800, newPrice: 480, discount: 40, endsAt: 0, remaining: 0 * 3600 + 53 * 60 + 5 },
]);

const coupons = ref<Coupon[]>([
  { id: 'c1', code: 'HOCTAP2026', desc: 'Giảm 15% mọi gói VIP', accent: 'cyan', copied: false },
  { id: 'c2', code: 'NEWBIE100', desc: 'Tặng 100 coin cho người mới', accent: 'green', copied: false },
  { id: 'c3', code: 'JACKPOT5', desc: 'Tặng 5 vé quay miễn phí', accent: 'amber', copied: false },
]);

const bundles: Bundle[] = [
  {
    id: 'b1',
    name: 'Gói Tân sinh viên',
    desc: 'Mọi thứ cần để bắt đầu',
    icon: 'rocket',
    accent: 'cyan',
    price: '99.000đ',
    items: ['2.000 coin', '10 gem', 'VIP Bạc 1 tháng', '5 vé quay'],
    left: 23,
    total: 100,
  },
  {
    id: 'b2',
    name: 'Gói Ôn thi',
    desc: 'Tăng tốc trước kỳ thi',
    icon: 'graduation',
    accent: 'violet',
    price: '199.000đ',
    items: ['5.000 coin', '25 gem', 'VIP Vàng 1 tháng', 'x2 XP 7 ngày', '15 vé quay'],
    left: 8,
    total: 50,
  },
];

// ── Derived ──────────────────────────────────────────────────────────────────
const ANNUAL_DISCOUNT = 0.2; // 20% off when paying yearly
function tierPrice(tier: VipTier): number {
  if (annual.value) return Math.round(tier.monthly * 12 * (1 - ANNUAL_DISCOUNT));
  return tier.monthly;
}
function tierPeriod(): string {
  return annual.value ? 'coin / năm' : 'coin / tháng';
}
function tierSaving(tier: VipTier): number {
  return Math.round(tier.monthly * 12 * ANNUAL_DISCOUNT);
}

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};
const accentText: Record<Accent, string> = {
  cyan: 'text-cyan-300',
  magenta: 'text-fuchsia-300',
  green: 'text-emerald-300',
  violet: 'text-violet-300',
  amber: 'text-amber-300',
};

function formatCountdown(seconds: number): string {
  const s = Math.max(0, seconds);
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = s % 60;
  const pad = (n: number): string => String(n).padStart(2, '0');
  return `${pad(h)}:${pad(m)}:${pad(sec)}`;
}

// ── Actions ──────────────────────────────────────────────────────────────────
function copyCoupon(coupon: Coupon): void {
  if (typeof navigator !== 'undefined' && navigator.clipboard) {
    void navigator.clipboard.writeText(coupon.code).catch(() => {
      /* clipboard may be unavailable; UI feedback below still applies */
    });
  }
  coupons.value = coupons.value.map((c) =>
    c.id === coupon.id ? { ...c, copied: true } : { ...c, copied: false }
  );
  const id = coupon.id;
  setTimeout(() => {
    coupons.value = coupons.value.map((c) => (c.id === id ? { ...c, copied: false } : c));
  }, 1800);
}

onMounted(() => {
  ticker = setInterval(() => {
    flashDeals.value = flashDeals.value.map((d) => ({
      ...d,
      remaining: d.remaining > 0 ? d.remaining - 1 : 0,
    }));
  }, 1000);
});

onUnmounted(() => {
  if (ticker) clearInterval(ticker);
});
</script>

<template>
  <div class="space-y-6">
    <!-- Hero -->
    <GlassPanel glow="magenta" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">VIP · Ưu đãi</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Ưu đãi & gói VIP</span> 👑
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Nâng cấp trải nghiệm học tập với các gói VIP, săn flash deal giới hạn và
            dùng mã giảm giá độc quyền. Số dư của bạn: <span class="font-semibold text-amber-300">{{ coins.toLocaleString('vi-VN') }} coin</span>.
          </p>
        </div>

        <!-- Billing toggle -->
        <div class="flex shrink-0 items-center gap-3 rounded-sm border border-white/[0.08] bg-white/[0.03] px-4 py-3">
          <span class="text-sm font-medium" :class="!annual ? 'text-foreground' : 'text-muted-foreground'">Hàng tháng</span>
          <Toggle :model-value="annual" @update:model-value="(v) => (annual = v)" />
          <span class="text-sm font-medium" :class="annual ? 'text-foreground' : 'text-muted-foreground'">
            Hàng năm
            <span class="ml-1 rounded-sm bg-emerald-500/20 px-1.5 py-0.5 text-[0.65rem] font-bold text-emerald-300">-20%</span>
          </span>
        </div>
      </div>
    </GlassPanel>

    <!-- VIP tiers -->
    <section class="grid grid-cols-1 gap-5 lg:grid-cols-3">
      <div
        v-for="tier in tiers"
        :key="tier.id"
        class="gradient-border relative flex flex-col rounded-sm p-5"
        :class="tier.popular ? 'ring-1 ring-amber-400/40' : ''"
      >
        <span
          v-if="tier.popular"
          class="popular-ribbon absolute -top-3 left-1/2 -translate-x-1/2 overflow-hidden rounded-sm border border-amber-300/40 bg-amber-500/20 px-3 py-1 text-[0.65rem] font-bold uppercase tracking-wider text-amber-100"
        >
          ⭐ Phổ biến nhất
        </span>
        <span
          v-if="tier.current"
          class="absolute right-4 top-4 rounded-sm bg-cyan-500/20 px-2.5 py-0.5 text-[0.65rem] font-bold uppercase tracking-wide text-cyan-200"
        >Đang dùng</span>

        <div class="flex items-center gap-3">
          <span class="flex h-12 w-12 items-center justify-center rounded-sm" :class="accentChip[tier.accent]">
            <AppIcon :name="tier.icon" class="h-6 w-6" />
          </span>
          <div>
            <h3 class="text-lg font-bold text-foreground">VIP {{ tier.name }}</h3>
            <p class="text-xs text-muted-foreground">{{ tier.tagline }}</p>
          </div>
        </div>

        <div class="mt-5">
          <p class="flex items-baseline gap-1.5">
            <Transition name="price-flip" mode="out-in">
              <span :key="annual ? 'y' : 'm'" class="text-3xl font-bold tabular-nums" :class="accentText[tier.accent]">
                {{ tierPrice(tier).toLocaleString('vi-VN') }}
              </span>
            </Transition>
            <span class="text-sm text-muted-foreground">{{ tierPeriod() }}</span>
          </p>
          <p v-if="annual" class="mt-1 text-xs font-medium text-emerald-300">
            Tiết kiệm {{ tierSaving(tier).toLocaleString('vi-VN') }} coin / năm
          </p>
          <p v-else class="mt-1 text-xs text-muted-foreground">Thanh toán theo tháng, huỷ bất kỳ lúc nào</p>
        </div>

        <ul class="mt-5 flex-1 space-y-2.5">
          <li v-for="feat in tier.features" :key="feat" class="flex items-start gap-2 text-sm">
            <span class="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-sm" :class="accentChip[tier.accent]">
              <AppIcon name="check" class="h-3 w-3" />
            </span>
            <span class="text-muted-foreground">{{ feat }}</span>
          </li>
        </ul>

        <button
          type="button"
          class="mt-6 w-full rounded-sm px-4 py-2.5 text-sm font-semibold transition-all"
          :class="tier.current
            ? 'cursor-default border border-white/[0.1] bg-white/[0.04] text-muted-foreground'
            : 'cosmic-btn'"
          :disabled="tier.current"
        >
          {{ tier.current ? 'Gói hiện tại' : `Nâng cấp lên ${tier.name}` }}
        </button>
      </div>
    </section>

    <!-- Flash deals -->
    <SectionPanel title="Flash deal" subtitle="Giá sốc · số lượng & thời gian có hạn" icon="zap" accent="amber">
      <template #header-actions>
        <span class="inline-flex items-center gap-1.5 rounded-sm bg-rose-500/15 px-2.5 py-1 text-xs font-semibold text-rose-300">
          <span class="pulse-glow-magenta h-2 w-2 rounded-sm bg-rose-400" /> LIVE
        </span>
      </template>
      <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
        <div
          v-for="deal in flashDeals"
          :key="deal.id"
          class="flex flex-col rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:border-white/[0.12]"
        >
          <div class="flex items-center justify-between">
            <span class="flex h-11 w-11 items-center justify-center rounded-sm" :class="accentChip[deal.accent]">
              <AppIcon :name="deal.icon" class="h-5 w-5" />
            </span>
            <span class="rounded-sm bg-rose-500/20 px-2 py-1 text-xs font-bold text-rose-300">-{{ deal.discount }}%</span>
          </div>
          <p class="mt-3 text-sm font-semibold text-foreground">{{ deal.name }}</p>
          <p class="text-xs text-muted-foreground">{{ deal.desc }}</p>

          <div class="mt-3 flex items-baseline gap-2">
            <span class="text-xl font-bold tabular-nums text-amber-200">{{ deal.newPrice }}</span>
            <span class="text-sm text-muted-foreground line-through">{{ deal.oldPrice }}</span>
            <span class="text-xs text-muted-foreground">coin</span>
          </div>

          <div class="mt-3 flex items-center gap-2 rounded-sm border border-white/[0.06] bg-black/20 px-3 py-2">
            <AppIcon name="clock" class="h-4 w-4 text-rose-300" />
            <span v-if="deal.remaining > 0" class="font-mono text-sm font-bold tabular-nums text-rose-200">
              {{ formatCountdown(deal.remaining) }}
            </span>
            <span v-else class="text-sm font-semibold text-muted-foreground">Đã kết thúc</span>
          </div>

          <button
            type="button"
            class="cosmic-btn mt-3 w-full rounded-sm px-3 py-2 text-sm disabled:cursor-not-allowed"
            :disabled="deal.remaining <= 0"
          >
            {{ deal.remaining > 0 ? 'Mua ngay' : 'Hết hạn' }}
          </button>
        </div>
      </div>
    </SectionPanel>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Coupons -->
      <div class="lg:col-span-1">
        <SectionPanel title="Mã giảm giá" subtitle="Sao chép & dùng khi thanh toán" icon="gift" accent="green">
          <ul class="space-y-3">
            <li
              v-for="coupon in coupons"
              :key="coupon.id"
              class="flex items-center gap-3 rounded-sm border border-dashed border-white/[0.12] bg-white/[0.02] p-3"
            >
              <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-sm" :class="accentChip[coupon.accent]">
                <AppIcon name="badge" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="font-mono text-sm font-bold tracking-wider" :class="accentText[coupon.accent]">{{ coupon.code }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ coupon.desc }}</p>
              </div>
              <button
                type="button"
                class="shrink-0 rounded-sm border px-2.5 py-1.5 text-xs font-semibold transition-colors"
                :class="coupon.copied
                  ? 'border-emerald-400/40 bg-emerald-500/15 text-emerald-300'
                  : 'border-white/[0.1] bg-white/[0.04] text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-200'"
                @click="copyCoupon(coupon)"
              >
                <AppIcon :name="coupon.copied ? 'check' : 'file-text'" class="mr-1 inline h-3.5 w-3.5" />
                {{ coupon.copied ? 'Đã sao chép' : 'Sao chép' }}
              </button>
            </li>
          </ul>
        </SectionPanel>
      </div>

      <!-- Bundles -->
      <div class="lg:col-span-2">
        <SectionPanel title="Gói tổng hợp giới hạn" subtitle="Tiết kiệm hơn khi mua combo" icon="layers" accent="violet">
          <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div
              v-for="bundle in bundles"
              :key="bundle.id"
              class="flex flex-col rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:border-white/[0.12]"
            >
              <div class="flex items-center gap-3">
                <span class="flex h-11 w-11 items-center justify-center rounded-sm" :class="accentChip[bundle.accent]">
                  <AppIcon :name="bundle.icon" class="h-5 w-5" />
                </span>
                <div>
                  <p class="text-sm font-bold text-foreground">{{ bundle.name }}</p>
                  <p class="text-xs text-muted-foreground">{{ bundle.desc }}</p>
                </div>
              </div>
              <ul class="mt-3 space-y-1.5">
                <li v-for="item in bundle.items" :key="item" class="flex items-center gap-2 text-sm text-muted-foreground">
                  <AppIcon name="check-circle" class="h-3.5 w-3.5 text-emerald-300" /> {{ item }}
                </li>
              </ul>
              <div class="mt-3">
                <div class="mb-1 flex items-center justify-between text-[0.7rem] text-muted-foreground">
                  <span>Còn lại</span>
                  <span class="font-semibold" :class="bundle.left <= 10 ? 'text-rose-300' : 'text-foreground'">
                    {{ bundle.left }}/{{ bundle.total }} suất
                  </span>
                </div>
                <div class="h-1.5 w-full overflow-hidden rounded-sm bg-white/[0.06]">
                  <div
                    class="h-full rounded-sm from-violet-400 to-fuchsia-500"
                    :style="{ width: `${(bundle.left / bundle.total) * 100}%` }"
                  />
                </div>
              </div>
              <div class="mt-4 flex items-center justify-between">
                <span class="text-lg font-bold text-foreground">{{ bundle.price }}</span>
                <button type="button" class="cosmic-btn rounded-sm px-4 py-2 text-sm">Mua gói</button>
              </div>
            </div>
          </div>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.popular-ribbon::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(110deg, transparent 30%, oklch(0.95 0.05 85 / 35%) 50%, transparent 70%);
  background-size: 220% 100%;
  animation: ribbon-sweep 2.6s linear infinite;
  pointer-events: none;
}
@keyframes ribbon-sweep {
  0% { background-position: 180% 0; }
  100% { background-position: -80% 0; }
}

.price-flip-enter-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.price-flip-leave-active { transition: opacity 0.18s ease, transform 0.18s ease; }
.price-flip-enter-from { opacity: 0; transform: translateY(-8px); }
.price-flip-leave-to { opacity: 0; transform: translateY(8px); }
</style>
