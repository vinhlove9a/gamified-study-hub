<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Avatar from '@/components/app/Avatar.vue';
import Tabs from '@/components/app/Tabs.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type WindowId = 'daily' | 'weekly' | 'monthly' | 'all-time';
type ScopeId = 'friends' | 'global' | 'class';

interface RankEntry { rank: number; prevRank: number; name: string; xp: number; coins: number; streak: number; accent: Accent; isMe?: boolean; tier: string; }

const windowTabs = [
  { id: 'daily', label: 'Hôm nay' },
  { id: 'weekly', label: 'Tuần' },
  { id: 'monthly', label: 'Tháng' },
  { id: 'all-time', label: 'Tất cả' },
] as const;

const scopeTabs = [
  { id: 'friends', label: 'Bạn bè' },
  { id: 'global', label: 'Toàn cầu' },
  { id: 'class', label: 'Lớp' },
] as const;

const activeWindow = ref<WindowId>('weekly');
const activeScope = ref<ScopeId>('friends');

const dataByWindow: Record<WindowId, RankEntry[]> = {
  daily: [
    { rank: 1, prevRank: 2, name: 'Minh Anh', xp: 320, coins: 85, streak: 14, accent: 'amber', tier: 'Vàng' },
    { rank: 2, prevRank: 1, name: 'Hoàng Long', xp: 285, coins: 70, streak: 8, accent: 'cyan', tier: 'Bạc' },
    { rank: 3, prevRank: 3, name: 'Bạn', xp: 240, coins: 60, streak: 12, accent: 'violet', isMe: true, tier: 'Bạc' },
    { rank: 4, prevRank: 5, name: 'Thu Hà', xp: 210, coins: 55, streak: 6, accent: 'green', tier: 'Bạc' },
    { rank: 5, prevRank: 4, name: 'Quốc Bảo', xp: 195, coins: 48, streak: 4, accent: 'magenta', tier: 'Đồng' },
    { rank: 6, prevRank: 6, name: 'Phương Linh', xp: 170, coins: 42, streak: 9, accent: 'cyan', tier: 'Đồng' },
    { rank: 7, prevRank: 8, name: 'Đức Anh', xp: 155, coins: 38, streak: 3, accent: 'amber', tier: 'Đồng' },
    { rank: 8, prevRank: 7, name: 'Khánh Vy', xp: 140, coins: 35, streak: 5, accent: 'green', tier: 'Đồng' },
  ],
  weekly: [
    { rank: 1, prevRank: 1, name: 'Minh Anh', xp: 2140, coins: 520, streak: 14, accent: 'amber', tier: 'Vàng' },
    { rank: 2, prevRank: 3, name: 'Hoàng Long', xp: 1890, coins: 450, streak: 8, accent: 'cyan', tier: 'Vàng' },
    { rank: 3, prevRank: 2, name: 'Thu Hà', xp: 1720, coins: 410, streak: 6, accent: 'green', tier: 'Bạc' },
    { rank: 4, prevRank: 4, name: 'Bạn', xp: 1560, coins: 380, streak: 12, accent: 'violet', isMe: true, tier: 'Bạc' },
    { rank: 5, prevRank: 6, name: 'Quốc Bảo', xp: 1340, coins: 320, streak: 4, accent: 'magenta', tier: 'Bạc' },
    { rank: 6, prevRank: 5, name: 'Phương Linh', xp: 1280, coins: 305, streak: 9, accent: 'cyan', tier: 'Đồng' },
    { rank: 7, prevRank: 7, name: 'Đức Anh', xp: 1120, coins: 270, streak: 3, accent: 'amber', tier: 'Đồng' },
    { rank: 8, prevRank: 8, name: 'Khánh Vy', xp: 980, coins: 235, streak: 5, accent: 'green', tier: 'Đồng' },
    { rank: 9, prevRank: 9, name: 'Bảo Trâm', xp: 870, coins: 210, streak: 2, accent: 'magenta', tier: 'Đồng' },
    { rank: 10, prevRank: 10, name: 'Tuấn Kiệt', xp: 760, coins: 185, streak: 7, accent: 'cyan', tier: 'Đồng' },
  ],
  monthly: [
    { rank: 1, prevRank: 2, name: 'Minh Anh', xp: 8420, coins: 2100, streak: 14, accent: 'amber', tier: 'Vàng' },
    { rank: 2, prevRank: 1, name: 'Hoàng Long', xp: 7980, coins: 1980, streak: 8, accent: 'cyan', tier: 'Vàng' },
    { rank: 3, prevRank: 4, name: 'Bạn', xp: 7240, coins: 1820, streak: 12, accent: 'violet', isMe: true, tier: 'Vàng' },
    { rank: 4, prevRank: 3, name: 'Thu Hà', xp: 6880, coins: 1700, streak: 6, accent: 'green', tier: 'Bạc' },
    { rank: 5, prevRank: 5, name: 'Quốc Bảo', xp: 5960, coins: 1480, streak: 4, accent: 'magenta', tier: 'Bạc' },
    { rank: 6, prevRank: 7, name: 'Phương Linh', xp: 5420, coins: 1350, streak: 9, accent: 'cyan', tier: 'Bạc' },
    { rank: 7, prevRank: 6, name: 'Đức Anh', xp: 4980, coins: 1240, streak: 3, accent: 'amber', tier: 'Đồng' },
    { rank: 8, prevRank: 8, name: 'Khánh Vy', xp: 4320, coins: 1080, streak: 5, accent: 'green', tier: 'Đồng' },
  ],
  'all-time': [
    { rank: 1, prevRank: 1, name: 'Minh Anh', xp: 42100, coins: 10500, streak: 14, accent: 'amber', tier: 'Kim Cương' },
    { rank: 2, prevRank: 2, name: 'Hoàng Long', xp: 38900, coins: 9700, streak: 8, accent: 'cyan', tier: 'Kim Cương' },
    { rank: 3, prevRank: 3, name: 'Thu Hà', xp: 34200, coins: 8500, streak: 6, accent: 'green', tier: 'Vàng' },
    { rank: 4, prevRank: 5, name: 'Bạn', xp: 31800, coins: 7900, streak: 12, accent: 'violet', isMe: true, tier: 'Vàng' },
    { rank: 5, prevRank: 4, name: 'Quốc Bảo', xp: 29600, coins: 7400, streak: 4, accent: 'magenta', tier: 'Vàng' },
    { rank: 6, prevRank: 6, name: 'Phương Linh', xp: 27200, coins: 6800, streak: 9, accent: 'cyan', tier: 'Bạc' },
    { rank: 7, prevRank: 8, name: 'Đức Anh', xp: 24800, coins: 6200, streak: 3, accent: 'amber', tier: 'Bạc' },
    { rank: 8, prevRank: 7, name: 'Khánh Vy', xp: 22100, coins: 5500, streak: 5, accent: 'green', tier: 'Bạc' },
  ],
};

const entries = computed(() => dataByWindow[activeWindow.value]);
const myEntry = computed(() => entries.value.find(e => e.isMe));
const myRank = computed(() => myEntry.value?.rank ?? 0);
const nextEntry = computed(() => entries.value.find(e => e.rank === myRank.value - 1));
const xpToNext = computed(() => nextEntry.value ? nextEntry.value.xp - (myEntry.value?.xp ?? 0) : 0);

const tierThresholds = [
  { tier: 'Kim Cương', minRank: 1, maxRank: 1, color: '#22D3EE', reward: '500 coin + 20 gem' },
  { tier: 'Vàng', minRank: 2, maxRank: 3, color: '#F59E0B', reward: '300 coin + 10 gem' },
  { tier: 'Bạc', minRank: 4, maxRank: 6, color: '#7C3AED', reward: '150 coin + 5 gem' },
  { tier: 'Đồng', minRank: 7, maxRank: 99, color: '#8B8B9F', reward: '50 coin' },
];

function getTier(rank: number) { return tierThresholds.find(t => rank >= t.minRank && rank <= t.maxRank) ?? tierThresholds[tierThresholds.length - 1]; }
function rankMovement(e: RankEntry): 'up' | 'down' | 'steady' { return e.prevRank > e.rank ? 'up' : e.prevRank < e.rank ? 'down' : 'steady'; }
function movementIcon(m: 'up' | 'down' | 'steady') { return m === 'up' ? 'trending-up' : m === 'down' ? 'trending-down' : 'minus'; }
function movementColor(m: 'up' | 'down' | 'steady') { return m === 'up' ? 'text-emerald-300' : m === 'down' ? 'text-rose-300' : 'text-muted-foreground'; }
function rankBadgeClass(rank: number) {
  if (rank === 1) return ' from-amber-300 to-amber-500 text-slate-900';
  if (rank === 2) return ' from-slate-200 to-slate-400 text-slate-900';
  if (rank === 3) return ' from-orange-300 to-orange-500 text-slate-900';
  return 'bg-white/[0.06] text-muted-foreground';
}

const top3 = computed(() => entries.value.slice(0, 3));
const restEntries = computed(() => entries.value.slice(3));
</script>

<template>
  <div class="space-y-6">
    <GlassPanel glow="amber" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Bảng xếp hạng</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">Xếp hạng</h1>
          <p class="mt-1 text-sm text-muted-foreground">Cạnh tranh với bạn bè và cộng đồng. Leo rank để nhận phần thưởng!</p>
        </div>
        <div v-if="myEntry" class="flex items-center gap-3">
          <div class="rounded-sm border border-cyan-400/20 bg-cyan-500/10 px-4 py-2.5 text-center">
            <p class="text-lg font-bold leading-none text-cyan-200">#{{ myRank }}</p>
            <p class="mt-0.5 text-[0.65rem] text-muted-foreground">Xếp hạng của bạn</p>
          </div>
          <div v-if="nextEntry" class="rounded-sm border border-violet-400/20 bg-violet-500/10 px-4 py-2.5 text-center">
            <p class="text-lg font-bold leading-none text-violet-200">{{ xpToNext.toLocaleString('vi-VN') }}</p>
            <p class="mt-0.5 text-[0.65rem] text-muted-foreground">XP đến #{{ nextEntry.rank }}</p>
          </div>
        </div>
      </div>
    </GlassPanel>

    <!-- Filters -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <Tabs :tabs="[...windowTabs]" :model-value="activeWindow" @update:model-value="(v) => (activeWindow = v as WindowId)" />
      <div class="flex gap-2">
        <button v-for="s in scopeTabs" :key="s.id" type="button" class="rounded-sm border px-3.5 py-1.5 text-xs font-medium transition-all" :class="activeScope === s.id ? 'border-cyan-400/40 bg-cyan-500/10 text-cyan-300' : 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:text-foreground'" @click="activeScope = s.id">{{ s.label }}</button>
      </div>
    </div>

    <!-- Top 3 podium -->
    <div class="grid grid-cols-3 gap-3 sm:gap-4">
      <div v-for="entry in [...top3].sort((a, b) => b.rank - a.rank)" :key="entry.name" class="flex flex-col items-center gap-2 rounded-sm border p-4 text-center" :class="entry.isMe ? 'border-cyan-400/30 bg-cyan-500/[0.06]' : 'border-white/[0.06] bg-white/[0.02]'">
        <div class="relative">
          <span class="flex h-12 w-12 items-center justify-center rounded-sm text-lg font-bold" :class="rankBadgeClass(entry.rank)">{{ entry.rank }}</span>
          <span v-if="entry.rank === 1" class="absolute -top-3 left-1/2 -translate-x-1/2 text-lg">👑</span>
        </div>
        <Avatar :name="entry.name" :size="40" :accent="entry.accent" />
        <p class="truncate text-sm font-semibold" :class="entry.isMe ? 'text-cyan-200' : 'text-foreground'">{{ entry.name }}</p>
        <p class="text-xs font-semibold tabular-nums text-amber-300">{{ entry.xp.toLocaleString('vi-VN') }} XP</p>
        <span class="inline-flex items-center gap-1 text-[0.65rem]" :class="movementColor(rankMovement(entry))">
          <AppIcon :name="movementIcon(rankMovement(entry))" class="h-3 w-3" />
          {{ rankMovement(entry) === 'steady' ? '—' : Math.abs(entry.prevRank - entry.rank) }}
        </span>
      </div>
    </div>

    <!-- Remaining entries -->
    <SectionPanel title="Bảng xếp hạng đầy đủ" icon="trophy" accent="amber" :flush="true">
      <div class="cosmic-scroll max-h-[480px] overflow-y-auto">
        <div v-for="entry in restEntries" :key="entry.name" class="flex items-center gap-3 border-b border-white/[0.03] px-5 py-3 last:border-0" :class="entry.isMe ? 'bg-cyan-500/[0.06]' : ''">
          <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm text-xs font-bold" :class="rankBadgeClass(entry.rank)">{{ entry.rank }}</span>
          <Avatar :name="entry.name" :size="36" :accent="entry.accent" />
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium" :class="entry.isMe ? 'text-cyan-200' : 'text-foreground'">{{ entry.name }}<span v-if="entry.isMe" class="text-muted-foreground"> (bạn)</span></p>
            <div class="flex items-center gap-3 text-xs text-muted-foreground">
              <span class="inline-flex items-center gap-0.5"><AppIcon name="flame" class="h-3 w-3 text-orange-300" />{{ entry.streak }}</span>
              <span class="inline-flex items-center gap-0.5"><AppIcon name="coin" class="h-3 w-3 text-amber-300" />{{ entry.coins.toLocaleString('vi-VN') }}</span>
              <span class="rounded-sm bg-white/[0.06] px-1.5 py-0.5 text-[0.6rem] font-medium" :style="{ color: getTier(entry.rank).color }">{{ entry.tier }}</span>
            </div>
          </div>
          <div class="shrink-0 text-right">
            <p class="text-sm font-bold tabular-nums text-foreground">{{ entry.xp.toLocaleString('vi-VN') }}</p>
            <span class="inline-flex items-center gap-0.5 text-[0.65rem]" :class="movementColor(rankMovement(entry))">
              <AppIcon :name="movementIcon(rankMovement(entry))" class="h-3 w-3" />{{ rankMovement(entry) === 'steady' ? '—' : Math.abs(entry.prevRank - entry.rank) }}
            </span>
          </div>
        </div>
      </div>
    </SectionPanel>

    <!-- Reward tiers -->
    <SectionPanel title="Phần thưởng theo hạng" subtitle="Phần thưởng được chốt cuối mỗi kỳ" icon="gift" accent="violet">
      <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <div v-for="tier in tierThresholds" :key="tier.tier" class="rounded-sm border p-4" :class="myRank >= tier.minRank && myRank <= tier.maxRank ? 'border-cyan-400/30 bg-cyan-500/[0.06]' : 'border-white/[0.06] bg-white/[0.02]'">
          <div class="flex items-center gap-2">
            <span class="h-3 w-3 rounded-sm" :style="{ backgroundColor: tier.color, boxShadow: `0 0 8px ${tier.color}80` }" />
            <span class="text-sm font-bold" :style="{ color: tier.color }">{{ tier.tier }}</span>
          </div>
          <p class="mt-2 text-xs text-muted-foreground">Hạng {{ tier.minRank === tier.maxRank ? tier.minRank : `${tier.minRank}–${tier.maxRank === 99 ? '∞' : tier.maxRank}` }}</p>
          <p class="mt-1.5 text-sm font-medium text-foreground">{{ tier.reward }}</p>
          <div v-if="myRank >= tier.minRank && myRank <= tier.maxRank" class="mt-2 inline-flex items-center gap-1 rounded-sm bg-emerald-500/10 px-2 py-0.5 text-[0.65rem] font-semibold text-emerald-300"><AppIcon name="check-circle" class="h-3 w-3" />Hạng hiện tại</div>
        </div>
      </div>
    </SectionPanel>

    <!-- My progress to next rank -->
    <div v-if="nextEntry && myEntry" class="rounded-sm border border-violet-400/20 bg-violet-500/[0.06] p-5">
      <div class="flex items-center justify-between gap-4">
        <div>
          <p class="text-sm font-semibold text-foreground">Tiến tới hạng #{{ nextEntry.rank }}</p>
          <p class="mt-0.5 text-xs text-muted-foreground">Còn {{ xpToNext.toLocaleString('vi-VN') }} XP để vượt {{ nextEntry.name }}</p>
        </div>
        <div class="text-right"><p class="text-2xl font-bold tabular-nums text-violet-200">{{ Math.round(((myEntry.xp - (entries.find(e => e.rank === myRank + 1)?.xp ?? 0)) / ((nextEntry.xp - (entries.find(e => e.rank === myRank + 1)?.xp ?? 0)) || 1)) * 100) }}%</p><p class="text-[0.65rem] text-muted-foreground">tiến độ</p></div>
      </div>
      <div class="mt-3"><ProgressBar :value="Math.round(((myEntry.xp - (entries.find(e => e.rank === myRank + 1)?.xp ?? 0)) / ((nextEntry.xp - (entries.find(e => e.rank === myRank + 1)?.xp ?? 0)) || 1)) * 100)" accent="violet" :height="8" /></div>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.</p>
  </div>
</template>
