<script setup lang="ts">
import { ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Tabs from '@/components/app/Tabs.vue';

// ── Types ─────────────────────────────────────────────────────────────────────

interface StoreItem {
  name: string;
  desc: string;
  price: number;
  icon: string;
  accent: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
  owned?: boolean;
}

interface CosmeticItem {
  name: string;
  desc: string;
  price: number;
  icon: string;
  accent: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
  owned?: boolean;
}

interface UnlockItem {
  name: string;
  desc: string;
  price: number;
  icon: string;
  accent: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
  owned?: boolean;
}

interface VipBenefit {
  text: string;
}

interface VipTier {
  id: string;
  name: string;
  price: number;
  glow: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber' | 'none';
  accentClass: string;
  badgeClass: string;
  popular?: boolean;
  benefits: VipBenefit[];
}

// ── State ─────────────────────────────────────────────────────────────────────

const activeCategory = ref<string>('vatpham');

const tabs = [
  { id: 'vatpham', label: 'Vật phẩm' },
  { id: 'giaodien', label: 'Giao diện' },
  { id: 'mokhoa', label: 'Mở khoá' },
  { id: 'goivip', label: 'Gói VIP' },
];

// ── Mock data ─────────────────────────────────────────────────────────────────

const powerUps: StoreItem[] = [
  {
    name: 'x2 XP (1 giờ)',
    desc: 'Nhân đôi điểm kinh nghiệm trong 60 phút',
    price: 80,
    icon: 'zap',
    accent: 'amber',
    owned: false,
  },
  {
    name: 'Đóng băng chuỗi',
    desc: 'Bảo vệ chuỗi học của bạn trong 1 ngày',
    price: 120,
    icon: 'shield',
    accent: 'cyan',
    owned: true,
  },
  {
    name: 'Gợi ý quiz',
    desc: 'Hiện gợi ý cho câu hỏi khó nhất',
    price: 50,
    icon: 'lightbulb',
    accent: 'violet',
    owned: false,
  },
  {
    name: 'Mở lại bài',
    desc: 'Làm lại một bài kiểm tra đã hoàn thành',
    price: 200,
    icon: 'rocket',
    accent: 'magenta',
    owned: false,
  },
  {
    name: 'Tăng tốc nạp coin',
    desc: 'Tăng 50% coin nhận được trong 2 giờ',
    price: 150,
    icon: 'flame',
    accent: 'amber',
    owned: true,
  },
  {
    name: 'Học liên tục',
    desc: 'Giảm thời gian hồi phục năng lượng',
    price: 90,
    icon: 'clock',
    accent: 'green',
    owned: false,
  },
];

const cosmetics: CosmeticItem[] = [
  {
    name: 'Giao diện Thiên Hà',
    desc: 'Bầu trời đêm đầy sao lung linh',
    price: 300,
    icon: 'moon',
    accent: 'violet',
    owned: false,
  },
  {
    name: 'Khung avatar Vàng',
    desc: 'Viền vàng rực rỡ cho hồ sơ của bạn',
    price: 250,
    icon: 'star',
    accent: 'amber',
    owned: true,
  },
  {
    name: 'Giao diện Rừng Ngọc',
    desc: 'Màu xanh ngọc mát mẻ và bình yên',
    price: 280,
    icon: 'gem',
    accent: 'green',
    owned: false,
  },
  {
    name: 'Khung avatar Pha Lê',
    desc: 'Hiệu ứng lấp lánh bắt mắt',
    price: 350,
    icon: 'palette',
    accent: 'cyan',
    owned: false,
  },
  {
    name: 'Giao diện Bình Minh',
    desc: 'Sắc hồng rực rỡ của buổi bình minh',
    price: 290,
    icon: 'sun',
    accent: 'magenta',
    owned: false,
  },
  {
    name: 'Khung avatar Huyền Thoại',
    desc: 'Dành cho những người học xuất sắc',
    price: 500,
    icon: 'crown',
    accent: 'amber',
    owned: false,
  },
];

const unlocks: UnlockItem[] = [
  {
    name: 'Bộ tài liệu Toán nâng cao',
    desc: 'Trọn bộ 80 tài liệu Toán từ cơ bản đến nâng cao',
    price: 400,
    icon: 'lock',
    accent: 'cyan',
    owned: false,
  },
  {
    name: 'Kho bài tập Tiếng Anh',
    desc: 'Hơn 500 bài luyện tập IELTS / TOEIC',
    price: 350,
    icon: 'lock',
    accent: 'green',
    owned: false,
  },
  {
    name: 'Tài liệu Lập trình VIP',
    desc: 'Thuật toán, CTDL, phỏng vấn kỹ thuật',
    price: 450,
    icon: 'crown',
    accent: 'violet',
    owned: true,
  },
  {
    name: 'Bộ đề Lịch sử tổng ôn',
    desc: 'Đề thi thử và đáp án chi tiết từ 2018–2024',
    price: 300,
    icon: 'lock',
    accent: 'amber',
    owned: false,
  },
  {
    name: 'Kho Khoa học tự nhiên',
    desc: 'Vật lý, Hoá học, Sinh học toàn diện',
    price: 500,
    icon: 'crown',
    accent: 'magenta',
    owned: false,
  },
];

const vipTiers: VipTier[] = [
  {
    id: 'bac',
    name: 'Bạc',
    price: 500,
    glow: 'none',
    accentClass: 'text-slate-300',
    badgeClass: 'bg-slate-300/20 text-slate-200',
    popular: false,
    benefits: [
      { text: 'Loại bỏ quảng cáo' },
      { text: 'Tải tài liệu không giới hạn' },
      { text: 'Thêm 3 bộ sưu tập riêng' },
      { text: 'Ưu tiên hỗ trợ cơ bản' },
    ],
  },
  {
    id: 'vang',
    name: 'Vàng',
    price: 1200,
    glow: 'amber',
    accentClass: 'text-amber-300',
    badgeClass: 'bg-amber-400/20 text-amber-200',
    popular: true,
    benefits: [
      { text: 'Tất cả quyền lợi Bạc' },
      { text: 'x1.5 XP khi học mỗi ngày' },
      { text: 'Mở khoá tài liệu premium' },
      { text: 'Khung avatar đặc biệt' },
      { text: 'Ưu tiên trong bảng xếp hạng' },
    ],
  },
  {
    id: 'kimcuong',
    name: 'Kim Cương',
    price: 2500,
    glow: 'violet',
    accentClass: 'text-violet-300',
    badgeClass: 'bg-violet-400/20 text-violet-200',
    popular: false,
    benefits: [
      { text: 'Tất cả quyền lợi Vàng' },
      { text: 'x2 XP toàn thời gian' },
      { text: 'Truy cập toàn bộ kho tài liệu' },
      { text: 'AI hỗ trợ học 24/7' },
      { text: 'Huy hiệu Kim Cương độc quyền' },
    ],
  },
];

// ── Accent helpers ─────────────────────────────────────────────────────────────

const accentChip: Record<string, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};
</script>

<template>
  <div class="space-y-6">
    <!-- Balance header -->
    <GlassPanel glow="amber" :padded="true">
      <div class="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-center gap-4">
          <span class="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-amber-500/20">
            <AppIcon name="coin" class="h-8 w-8 text-amber-300" />
          </span>
          <div>
            <p class="cosmic-badge">Ví coin của bạn</p>
            <p class="mt-1 text-3xl font-bold text-foreground">
              <span class="cosmic-gradient-text">1.250</span>
              <span class="ml-1.5 text-base font-normal text-amber-300">coin</span>
            </p>
            <p class="mt-0.5 text-xs text-muted-foreground">
              Số dư của bạn · Kiếm thêm coin bằng cách học hằng ngày
            </p>
          </div>
        </div>
        <div class="flex shrink-0 flex-wrap items-center gap-3">
          <div class="inline-flex items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.03] px-3 py-2 text-xs text-muted-foreground">
            <AppIcon name="zap" class="h-3.5 w-3.5 text-amber-300" />
            +50 coin hôm nay
          </div>
          <button type="button" class="cosmic-btn rounded-xl px-4 py-2 text-sm">
            <span class="inline-flex items-center gap-2">
              <AppIcon name="plus" class="h-4 w-4" />
              Nạp thêm coin
            </span>
          </button>
        </div>
      </div>
    </GlassPanel>

    <!-- Featured promo banner -->
    <GlassPanel glow="violet" :padded="true">
      <div class="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-start gap-4">
          <span class="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-violet-500/20">
            <AppIcon name="gem" class="h-7 w-7 text-violet-300" />
          </span>
          <div>
            <p class="cosmic-badge">Ưu đãi cuối tuần</p>
            <h2 class="mt-1 text-lg font-bold text-foreground">
              Giảm <span class="cosmic-gradient-text">30%</span> cho tất cả gói VIP
            </h2>
            <p class="mt-0.5 text-sm text-muted-foreground">
              Chỉ còn <span class="font-semibold text-violet-300">2 ngày</span> · Đừng bỏ lỡ cơ hội học không giới hạn!
            </p>
          </div>
        </div>
        <button type="button" class="cosmic-btn shrink-0 rounded-xl px-4 py-2 text-sm">
          <span class="inline-flex items-center gap-2">
            <AppIcon name="crown" class="h-4 w-4" />
            Xem ngay
          </span>
        </button>
      </div>
    </GlassPanel>

    <!-- Category tabs + content -->
    <SectionPanel title="Cửa hàng" subtitle="Mua vật phẩm và nâng cấp trải nghiệm học tập" icon="store" accent="cyan">
      <template #header-actions>
        <div class="inline-flex items-center gap-1.5 rounded-full bg-white/[0.06] px-2.5 py-1 text-xs text-muted-foreground">
          <AppIcon name="coin" class="h-3.5 w-3.5 text-amber-300" />
          1.250 coin
        </div>
      </template>

      <Tabs v-model="activeCategory" :tabs="tabs" class="mb-6" />

      <!-- ── Vật phẩm ── -->
      <div v-show="activeCategory === 'vatpham'">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <div
            v-for="item in powerUps"
            :key="item.name"
            class="flex flex-col gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] p-4 transition-colors"
            :class="item.owned ? 'opacity-60' : 'hover:border-white/[0.12]'"
          >
            <div class="flex items-start justify-between gap-2">
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
                :class="accentChip[item.accent]"
              >
                <AppIcon :name="item.icon" class="h-5 w-5" />
              </span>
              <span
                v-if="item.owned"
                class="rounded-full bg-emerald-500/15 px-2.5 py-1 text-xs font-semibold text-emerald-300"
              >
                Đã sở hữu
              </span>
            </div>
            <div class="flex-1">
              <p class="text-sm font-semibold text-foreground">{{ item.name }}</p>
              <p class="mt-0.5 text-xs text-muted-foreground">{{ item.desc }}</p>
            </div>
            <div class="flex items-center justify-between gap-2">
              <span class="inline-flex items-center gap-1 text-sm font-bold text-amber-300">
                <AppIcon name="coin" class="h-3.5 w-3.5" />
                {{ item.price }}
              </span>
              <button
                type="button"
                :disabled="item.owned"
                class="rounded-xl px-3 py-1.5 text-xs font-medium transition-colors"
                :class="
                  item.owned
                    ? 'cursor-not-allowed border border-white/[0.06] text-muted-foreground'
                    : 'cosmic-btn'
                "
              >
                {{ item.owned ? 'Đã mua' : 'Mua' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Giao diện ── -->
      <div v-show="activeCategory === 'giaodien'">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <div
            v-for="item in cosmetics"
            :key="item.name"
            class="flex flex-col gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] p-4 transition-colors"
            :class="item.owned ? 'opacity-60' : 'hover:border-white/[0.12]'"
          >
            <div class="flex items-start justify-between gap-2">
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
                :class="accentChip[item.accent]"
              >
                <AppIcon :name="item.icon" class="h-5 w-5" />
              </span>
              <span
                v-if="item.owned"
                class="rounded-full bg-emerald-500/15 px-2.5 py-1 text-xs font-semibold text-emerald-300"
              >
                Đã sở hữu
              </span>
            </div>
            <div class="flex-1">
              <p class="text-sm font-semibold text-foreground">{{ item.name }}</p>
              <p class="mt-0.5 text-xs text-muted-foreground">{{ item.desc }}</p>
            </div>
            <div class="flex items-center justify-between gap-2">
              <span class="inline-flex items-center gap-1 text-sm font-bold text-amber-300">
                <AppIcon name="coin" class="h-3.5 w-3.5" />
                {{ item.price }}
              </span>
              <button
                type="button"
                :disabled="item.owned"
                class="rounded-xl px-3 py-1.5 text-xs font-medium transition-colors"
                :class="
                  item.owned
                    ? 'cursor-not-allowed border border-white/[0.06] text-muted-foreground'
                    : 'cosmic-btn'
                "
              >
                {{ item.owned ? 'Đã mua' : 'Mua' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Mở khoá ── -->
      <div v-show="activeCategory === 'mokhoa'">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <div
            v-for="item in unlocks"
            :key="item.name"
            class="flex flex-col gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] p-4 transition-colors"
            :class="item.owned ? 'opacity-60' : 'hover:border-white/[0.12]'"
          >
            <div class="flex items-start justify-between gap-2">
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
                :class="accentChip[item.accent]"
              >
                <AppIcon :name="item.owned ? 'unlock' : item.icon" class="h-5 w-5" />
              </span>
              <span
                v-if="item.owned"
                class="rounded-full bg-emerald-500/15 px-2.5 py-1 text-xs font-semibold text-emerald-300"
              >
                Đã sở hữu
              </span>
            </div>
            <div class="flex-1">
              <p class="text-sm font-semibold text-foreground">{{ item.name }}</p>
              <p class="mt-0.5 text-xs text-muted-foreground">{{ item.desc }}</p>
            </div>
            <div class="flex items-center justify-between gap-2">
              <span class="inline-flex items-center gap-1 text-sm font-bold text-amber-300">
                <AppIcon name="coin" class="h-3.5 w-3.5" />
                {{ item.price }}
              </span>
              <button
                type="button"
                :disabled="item.owned"
                class="rounded-xl px-3 py-1.5 text-xs font-medium transition-colors"
                :class="
                  item.owned
                    ? 'cursor-not-allowed border border-white/[0.06] text-muted-foreground'
                    : 'cosmic-btn'
                "
              >
                {{ item.owned ? 'Đã mở' : 'Mở khoá' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ── Gói VIP ── -->
      <div v-show="activeCategory === 'goivip'">
        <div class="grid grid-cols-1 gap-6 md:grid-cols-3">
          <GlassPanel
            v-for="tier in vipTiers"
            :key="tier.id"
            :glow="tier.glow"
            :padded="true"
            class="flex flex-col"
          >
            <div class="flex items-start justify-between gap-2">
              <div>
                <p v-if="tier.popular" class="cosmic-badge mb-2">Phổ biến nhất</p>
                <h3 class="text-xl font-bold" :class="tier.accentClass">{{ tier.name }}</h3>
              </div>
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
                :class="tier.badgeClass"
              >
                <AppIcon
                  :name="tier.id === 'kimcuong' ? 'gem' : tier.id === 'vang' ? 'crown' : 'star'"
                  class="h-5 w-5"
                />
              </span>
            </div>

            <div class="mt-3 flex items-end gap-1.5">
              <span class="text-3xl font-bold text-foreground">
                {{ tier.price.toLocaleString('vi-VN') }}
              </span>
              <span class="mb-0.5 inline-flex items-center gap-1 text-sm text-amber-300">
                <AppIcon name="coin" class="h-3.5 w-3.5" /> coin / tháng
              </span>
            </div>

            <ul class="mt-4 flex-1 space-y-2.5">
              <li
                v-for="benefit in tier.benefits"
                :key="benefit.text"
                class="flex items-start gap-2"
              >
                <AppIcon name="check" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-400" />
                <span class="text-sm text-muted-foreground">{{ benefit.text }}</span>
              </li>
            </ul>

            <button type="button" class="cosmic-btn mt-6 w-full rounded-xl px-4 py-2.5 text-sm font-semibold">
              Chọn gói {{ tier.name }}
            </button>
          </GlassPanel>
        </div>

        <p class="mt-4 text-center text-xs text-muted-foreground">
          Tất cả gói VIP có thể huỷ bất kỳ lúc nào · Coin được hoàn lại theo chính sách hoàn tiền
        </p>
      </div>
    </SectionPanel>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
