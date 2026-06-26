<script setup lang="ts">
import { ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import Avatar from '@/components/app/Avatar.vue';
import Tabs from '@/components/app/Tabs.vue';

// ─── Interfaces ──────────────────────────────────────────────────────────────

interface RankUser {
  rank: number;
  name: string;
  level: number;
  xp: number;
  coins: number;
  trend: 'up' | 'down' | 'same';
  me?: boolean;
  accent: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
}

interface PodiumUser {
  rank: 1 | 2 | 3;
  name: string;
  level: number;
  xp: number;
  coins: number;
  accent: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
}

interface TabItem {
  id: string;
  label: string;
}

// ─── Tabs ─────────────────────────────────────────────────────────────────────

const scopeTabs: TabItem[] = [
  { id: 'friends', label: 'Bạn bè' },
  { id: 'class', label: 'Lớp học' },
  { id: 'global', label: 'Toàn cầu' },
];

const periodTabs: TabItem[] = [
  { id: 'week', label: 'Tuần này' },
  { id: 'month', label: 'Tháng này' },
  { id: 'all', label: 'Mọi lúc' },
];

const activeScope = ref<string>('friends');
const activePeriod = ref<string>('week');

// ─── Podium data (top 3) ──────────────────────────────────────────────────────

const podiumUsers: PodiumUser[] = [
  { rank: 1, name: 'Minh Anh', level: 14, xp: 9840, coins: 3120, accent: 'amber' },
  { rank: 2, name: 'Hoàng Long', level: 12, xp: 8610, coins: 2870, accent: 'cyan' },
  { rank: 3, name: 'Bạn', level: 10, xp: 7250, coins: 1250, accent: 'violet' },
];

// ─── "My rank" highlight card ─────────────────────────────────────────────────

const myRank = 3;
const myXpThisWeek = 7250;
const myCoinsThisWeek = 1250;
const myLevel = 10;

// ─── Stat panels ─────────────────────────────────────────────────────────────

const rankStats = [
  {
    label: 'Hạng của bạn',
    value: '#3',
    icon: 'trophy',
    accent: 'amber' as const,
    trend: 1,
    caption: 'Tăng 1 bậc',
    sparkline: [8, 7, 7, 6, 5, 4, 4, 3],
  },
  {
    label: 'XP tuần này',
    value: '7.250',
    unit: 'XP',
    icon: 'zap',
    accent: 'violet' as const,
    trend: 12,
    caption: '+840 so với tuần trước',
    sparkline: [4200, 5100, 5800, 6100, 6550, 6900, 7100, 7250],
  },
  {
    label: 'Coin tuần này',
    value: '1.250',
    icon: 'coin',
    accent: 'amber' as const,
    trend: 8,
    caption: '+90 hôm nay',
    sparkline: [700, 820, 920, 1000, 1080, 1150, 1200, 1250],
  },
  {
    label: 'Người tham gia',
    value: '128',
    unit: 'người',
    icon: 'users',
    accent: 'cyan' as const,
    caption: 'Trong bảng bạn bè',
    sparkline: [90, 95, 100, 108, 113, 120, 125, 128],
  },
];

// ─── Full ranking list ────────────────────────────────────────────────────────

const rankUsers: RankUser[] = [
  { rank: 1, name: 'Minh Anh', level: 14, xp: 9840, coins: 3120, trend: 'same', accent: 'amber' },
  { rank: 2, name: 'Hoàng Long', level: 12, xp: 8610, coins: 2870, trend: 'up', accent: 'cyan' },
  { rank: 3, name: 'Bạn', level: 10, xp: 7250, coins: 1250, trend: 'up', me: true, accent: 'violet' },
  { rank: 4, name: 'Thu Hà', level: 11, xp: 7080, coins: 1180, trend: 'down', accent: 'green' },
  { rank: 5, name: 'Quốc Bảo', level: 9, xp: 6540, coins: 990, trend: 'up', accent: 'magenta' },
  { rank: 6, name: 'Lan Phương', level: 10, xp: 6200, coins: 870, trend: 'same', accent: 'cyan' },
  { rank: 7, name: 'Đức Minh', level: 8, xp: 5980, coins: 750, trend: 'down', accent: 'violet' },
  { rank: 8, name: 'Thanh Hương', level: 9, xp: 5620, coins: 640, trend: 'up', accent: 'amber' },
  { rank: 9, name: 'Việt Anh', level: 8, xp: 5340, coins: 590, trend: 'down', accent: 'green' },
  { rank: 10, name: 'Ngọc Mai', level: 7, xp: 5010, coins: 520, trend: 'same', accent: 'magenta' },
  { rank: 11, name: 'Phúc Thịnh', level: 7, xp: 4780, coins: 480, trend: 'down', accent: 'cyan' },
  { rank: 12, name: 'Hải Yến', level: 6, xp: 4320, coins: 410, trend: 'up', accent: 'violet' },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────

const rankBadgeClass: Record<number, string> = {
  1: 'bg-amber-400/20 text-amber-300 ring-1 ring-amber-400/40',
  2: 'bg-slate-300/15 text-slate-200 ring-1 ring-slate-300/30',
  3: 'bg-orange-400/15 text-orange-300 ring-1 ring-orange-400/30',
};

function getRankBadgeClass(rank: number): string {
  return rankBadgeClass[rank] ?? 'bg-white/[0.06] text-muted-foreground';
}

function getTrendIcon(trend: RankUser['trend']): string {
  if (trend === 'up') return 'trending-up';
  if (trend === 'down') return 'trending-down';
  return 'minus';
}

function getTrendClass(trend: RankUser['trend']): string {
  if (trend === 'up') return 'text-emerald-400';
  if (trend === 'down') return 'text-rose-400';
  return 'text-muted-foreground';
}

// Podium layout order: 2nd left, 1st center, 3rd right
const podiumOrder: Array<PodiumUser> = [
  podiumUsers[1], // rank 2
  podiumUsers[0], // rank 1
  podiumUsers[2], // rank 3
];

function podiumHeightClass(rank: 1 | 2 | 3): string {
  if (rank === 1) return 'h-24';
  if (rank === 2) return 'h-14';
  return 'h-10';
}

function podiumAvatarSize(rank: 1 | 2 | 3): number {
  if (rank === 1) return 68;
  return 52;
}

function podiumGlowClass(rank: 1 | 2 | 3): string {
  if (rank === 1) return 'border-amber-400/40 shadow-[0_0_32px_rgba(251,191,36,0.18)]';
  if (rank === 2) return 'border-slate-300/25 shadow-[0_0_16px_rgba(203,213,225,0.08)]';
  return 'border-orange-400/20 shadow-[0_0_12px_rgba(251,146,60,0.08)]';
}

function podiumBgClass(rank: 1 | 2 | 3): string {
  if (rank === 1) return 'bg-amber-500/[0.06]';
  if (rank === 2) return 'bg-slate-300/[0.03]';
  return 'bg-orange-500/[0.03]';
}

function podiumRankLabel(rank: 1 | 2 | 3): string {
  if (rank === 1) return '🥇 Hạng 1';
  if (rank === 2) return '🥈 Hạng 2';
  return '🥉 Hạng 3';
}
</script>

<template>
  <div class="space-y-6">
    <!-- Page header -->
    <div class="flex flex-col gap-1">
      <p class="cosmic-badge">Thi đua học tập</p>
      <h1 class="text-2xl font-bold text-foreground sm:text-3xl">
        <span class="cosmic-gradient-text">Bảng xếp hạng</span>
      </h1>
      <p class="text-sm text-muted-foreground">
        So sánh thành tích, chinh phục đỉnh cao cùng cộng đồng học tập.
      </p>
    </div>

    <!-- Tab row 1: scope -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <Tabs v-model="activeScope" :tabs="scopeTabs" />
      <Tabs v-model="activePeriod" :tabs="periodTabs" />
    </div>

    <!-- Stat panels -->
    <section class="grid grid-cols-2 gap-4 md:grid-cols-4">
      <StatPanel
        v-for="stat in rankStats"
        :key="stat.label"
        :label="stat.label"
        :value="stat.value"
        :unit="stat.unit"
        :icon="stat.icon"
        :accent="stat.accent"
        :trend="stat.trend"
        :caption="stat.caption"
        :sparkline="stat.sparkline"
      />
    </section>

    <!-- Podium + My rank side by side on larger screens -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">

      <!-- Podium -->
      <div class="lg:col-span-2">
        <SectionPanel title="Top 3 xuất sắc nhất" subtitle="Người dẫn đầu trong kỳ này" icon="crown" accent="amber">
          <!-- Podium stage -->
          <div class="mt-2 flex items-end justify-center gap-3 pb-4 sm:gap-6">
            <div
              v-for="user in podiumOrder"
              :key="user.rank"
              class="flex flex-col items-center gap-3"
              :class="user.rank === 1 ? 'order-2' : user.rank === 2 ? 'order-1' : 'order-3'"
            >
              <!-- Avatar + crown for #1 -->
              <div class="flex flex-col items-center gap-1.5">
                <AppIcon
                  v-if="user.rank === 1"
                  name="crown"
                  class="h-6 w-6 text-amber-400 drop-shadow-[0_0_6px_rgba(251,191,36,0.6)]"
                />
                <div
                  class="rounded-full ring-2"
                  :class="
                    user.rank === 1
                      ? 'ring-amber-400/60 shadow-[0_0_20px_rgba(251,191,36,0.3)]'
                      : user.rank === 2
                        ? 'ring-slate-300/40'
                        : 'ring-orange-400/30'
                  "
                >
                  <Avatar :name="user.name" :size="podiumAvatarSize(user.rank)" :accent="user.accent" />
                </div>
                <span
                  class="rounded-full px-2.5 py-0.5 text-[0.65rem] font-bold"
                  :class="
                    user.rank === 1
                      ? 'bg-amber-400/20 text-amber-300'
                      : user.rank === 2
                        ? 'bg-slate-300/15 text-slate-200'
                        : 'bg-orange-400/15 text-orange-300'
                  "
                >
                  {{ podiumRankLabel(user.rank) }}
                </span>
              </div>

              <!-- Name + info -->
              <div class="flex flex-col items-center gap-0.5 text-center">
                <span
                  class="text-sm font-bold"
                  :class="user.rank === 1 ? 'text-amber-200' : 'text-foreground'"
                >
                  {{ user.name }}
                </span>
                <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs text-muted-foreground">
                  Cấp {{ user.level }}
                </span>
                <span class="mt-1 inline-flex items-center gap-1 text-xs font-semibold text-amber-300">
                  <AppIcon name="coin" class="h-3 w-3" />
                  {{ user.coins.toLocaleString('vi-VN') }}
                </span>
                <span class="text-[0.7rem] text-muted-foreground">
                  {{ user.xp.toLocaleString('vi-VN') }} XP
                </span>
              </div>

              <!-- Podium block -->
              <div
                class="w-24 rounded-t-xl border sm:w-28"
                :class="[podiumHeightClass(user.rank), podiumBgClass(user.rank), podiumGlowClass(user.rank)]"
              >
                <div class="flex h-full items-center justify-center">
                  <span
                    class="text-2xl font-black"
                    :class="
                      user.rank === 1
                        ? 'text-amber-300/60'
                        : user.rank === 2
                          ? 'text-slate-300/40'
                          : 'text-orange-400/40'
                    "
                  >
                    {{ user.rank }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </SectionPanel>
      </div>

      <!-- My rank highlight -->
      <div class="flex flex-col gap-6">
        <GlassPanel glow="cyan" class="p-5">
          <div class="flex items-start gap-3">
            <div class="relative shrink-0">
              <Avatar name="Bạn" :size="52" accent="cyan" />
              <span class="absolute -bottom-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-cyan-500/20 ring-1 ring-cyan-400/40">
                <AppIcon name="trending-up" class="h-3 w-3 text-cyan-300" />
              </span>
            </div>
            <div class="min-w-0 flex-1">
              <p class="cosmic-badge mb-1">Hạng của bạn</p>
              <div class="flex items-baseline gap-2">
                <span class="text-4xl font-black text-cyan-300">#{{ myRank }}</span>
                <span class="rounded-full bg-emerald-500/15 px-2 py-0.5 text-xs font-semibold text-emerald-300">
                  <AppIcon name="trending-up" class="mr-0.5 inline h-3 w-3" />
                  +1 bậc
                </span>
              </div>
              <p class="mt-0.5 text-xs text-muted-foreground">Trong nhóm bạn bè · Tuần này</p>
            </div>
          </div>

          <div class="mt-5 grid grid-cols-2 gap-3">
            <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
              <p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">Cấp độ</p>
              <p class="mt-1 text-lg font-bold text-foreground">{{ myLevel }}</p>
            </div>
            <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
              <p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">XP tuần này</p>
              <p class="mt-1 text-lg font-bold text-violet-300">{{ myXpThisWeek.toLocaleString('vi-VN') }}</p>
            </div>
            <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
              <p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">Coin tuần này</p>
              <p class="mt-1 inline-flex items-center gap-1 text-lg font-bold text-amber-300">
                <AppIcon name="coin" class="h-4 w-4" />
                {{ myCoinsThisWeek.toLocaleString('vi-VN') }}
              </p>
            </div>
            <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3">
              <p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">Xu hướng</p>
              <p class="mt-1 inline-flex items-center gap-1 text-lg font-bold text-emerald-300">
                <AppIcon name="trending-up" class="h-4 w-4" />
                Tăng
              </p>
            </div>
          </div>

          <button type="button" class="cosmic-btn mt-4 w-full rounded-xl px-4 py-2 text-sm">
            Xem chi tiết thành tích
          </button>
        </GlassPanel>
      </div>
    </div>

    <!-- Full ranking list -->
    <SectionPanel
      title="Bảng xếp hạng đầy đủ"
      subtitle="12 người tham gia · cập nhật theo thời gian thực"
      icon="list"
      accent="violet"
      flush
    >
      <template #header-actions>
        <button
          type="button"
          class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-3 py-1.5 text-xs text-muted-foreground transition-colors hover:border-white/20 hover:text-foreground"
        >
          <AppIcon name="filter" class="h-3.5 w-3.5" />
          Lọc
        </button>
      </template>

      <!-- Column header (hidden on mobile) -->
      <div class="hidden border-b border-white/[0.06] px-5 py-2.5 sm:flex sm:items-center sm:gap-3">
        <span class="w-9 shrink-0 text-[0.65rem] font-semibold uppercase tracking-wider text-muted-foreground">Hạng</span>
        <span class="w-10 shrink-0"></span>
        <span class="min-w-0 flex-1 text-[0.65rem] font-semibold uppercase tracking-wider text-muted-foreground">Học viên</span>
        <span class="w-16 shrink-0 text-right text-[0.65rem] font-semibold uppercase tracking-wider text-muted-foreground">XP</span>
        <span class="w-20 shrink-0 text-right text-[0.65rem] font-semibold uppercase tracking-wider text-muted-foreground">Coin</span>
        <span class="w-8 shrink-0 text-right text-[0.65rem] font-semibold uppercase tracking-wider text-muted-foreground">XH</span>
      </div>

      <!-- Rows -->
      <ul class="divide-y divide-white/[0.04]">
        <li
          v-for="user in rankUsers"
          :key="user.rank"
          class="flex items-center gap-3 px-5 py-3 transition-colors hover:bg-white/[0.02]"
          :class="user.me ? 'bg-cyan-500/[0.06]' : ''"
        >
          <!-- Rank badge -->
          <span
            class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-sm font-bold"
            :class="getRankBadgeClass(user.rank)"
          >
            {{ user.rank }}
          </span>

          <!-- Avatar -->
          <Avatar :name="user.name" :size="36" :accent="user.accent" />

          <!-- Name + level -->
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-1.5">
              <span
                class="truncate text-sm font-semibold"
                :class="user.me ? 'text-cyan-200' : 'text-foreground'"
              >
                {{ user.name }}
              </span>
              <span v-if="user.me" class="rounded-full bg-cyan-500/15 px-2 py-0.5 text-[0.65rem] font-bold text-cyan-300">
                Bạn
              </span>
              <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs text-muted-foreground">
                Cấp {{ user.level }}
              </span>
            </div>
          </div>

          <!-- XP -->
          <span class="w-16 shrink-0 text-right text-sm font-semibold text-violet-300">
            {{ user.xp.toLocaleString('vi-VN') }}
          </span>

          <!-- Coins -->
          <span class="hidden w-20 shrink-0 items-center justify-end gap-1 text-sm font-semibold text-amber-300 sm:flex">
            <AppIcon name="coin" class="h-3.5 w-3.5" />
            {{ user.coins.toLocaleString('vi-VN') }}
          </span>

          <!-- Trend -->
          <span class="flex w-8 shrink-0 justify-end" :class="getTrendClass(user.trend)">
            <AppIcon :name="getTrendIcon(user.trend)" class="h-4 w-4" />
          </span>
        </li>
      </ul>
    </SectionPanel>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
