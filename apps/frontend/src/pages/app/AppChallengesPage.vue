<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Tabs from '@/components/app/Tabs.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import Avatar from '@/components/app/Avatar.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type ChallengeTab = 'daily' | 'weekly' | 'seasonal';
type Difficulty = 'easy' | 'medium' | 'hard';
type RewardKind = 'coin' | 'xp' | 'gem';

interface Reward {
  kind: RewardKind;
  amount: number;
}

interface Challenge {
  id: number;
  group: ChallengeTab;
  title: string;
  description: string;
  icon: string;
  accent: Accent;
  current: number;
  total: number;
  reward: Reward;
  difficulty: Difficulty;
  participants: number;
  // seconds remaining for time-bound challenges; null = no live timer
  secondsLeft: number | null;
  claimed: boolean;
}

interface LeaderRow {
  rank: number;
  name: string;
  points: number;
  accent: Accent;
}

interface Countdown {
  days: number;
  hours: number;
  minutes: number;
  seconds: number;
}

// ─── Reward metadata ──────────────────────────────────────────────────────────

const rewardMeta: Record<RewardKind, { icon: string; label: string; accent: Accent }> = {
  coin: { icon: 'coin', label: 'coin', accent: 'amber' },
  xp: { icon: 'zap', label: 'XP', accent: 'cyan' },
  gem: { icon: 'gem', label: 'gem', accent: 'violet' },
};

const difficultyMeta: Record<Difficulty, { label: string; class: string }> = {
  easy: { label: 'Dễ', class: 'bg-emerald-500/10 text-emerald-300' },
  medium: { label: 'Trung bình', class: 'bg-amber-500/10 text-amber-300' },
  hard: { label: 'Khó', class: 'bg-rose-500/10 text-rose-300' },
};

// ─── Live clock ──────────────────────────────────────────────────────────────

let clockId: number | null = null;

// Featured season target.
const seasonTotalSeconds = ref<number>(11 * 86400 + 6 * 3600 + 23 * 60 + 41);

const seasonCountdown = computed<Countdown>(() => {
  const t = Math.max(0, seasonTotalSeconds.value);
  return {
    days: Math.floor(t / 86400),
    hours: Math.floor((t % 86400) / 3600),
    minutes: Math.floor((t % 3600) / 60),
    seconds: t % 60,
  };
});

function pad(n: number): string {
  return String(n).padStart(2, '0');
}

// ─── Wallet ──────────────────────────────────────────────────────────────────

const coinBalance = ref<number>(3250);
const xpBalance = ref<number>(12480);
const gemBalance = ref<number>(42);
const claimPulseId = ref<number | null>(null);

// ─── Challenges ────────────────────────────────────────────────────────────

const challenges = ref<Challenge[]>([
  {
    id: 1,
    group: 'daily',
    title: 'Học 15 thẻ ghi nhớ',
    description: 'Ôn tập bộ thẻ bất kỳ trong hôm nay.',
    icon: 'layers',
    accent: 'cyan',
    current: 15,
    total: 15,
    reward: { kind: 'coin', amount: 50 },
    difficulty: 'easy',
    participants: 4820,
    secondsLeft: 6 * 3600 + 12 * 60,
    claimed: false,
  },
  {
    id: 2,
    group: 'daily',
    title: 'Hoàn thành 1 phiên tập trung',
    description: 'Một phiên Pomodoro 25 phút không gián đoạn.',
    icon: 'target',
    accent: 'green',
    current: 0,
    total: 1,
    reward: { kind: 'xp', amount: 120 },
    difficulty: 'easy',
    participants: 3110,
    secondsLeft: 6 * 3600 + 12 * 60,
    claimed: false,
  },
  {
    id: 3,
    group: 'daily',
    title: 'Đạt 90% trong 1 bài quiz',
    description: 'Làm bài kiểm tra nhanh và đạt điểm cao.',
    icon: 'check-circle',
    accent: 'magenta',
    current: 1,
    total: 1,
    reward: { kind: 'coin', amount: 80 },
    difficulty: 'medium',
    participants: 1976,
    secondsLeft: 6 * 3600 + 12 * 60,
    claimed: true,
  },
  {
    id: 4,
    group: 'weekly',
    title: 'Duy trì chuỗi 7 ngày',
    description: 'Học liên tục mỗi ngày trong tuần này.',
    icon: 'flame',
    accent: 'amber',
    current: 5,
    total: 7,
    reward: { kind: 'gem', amount: 3 },
    difficulty: 'medium',
    participants: 2540,
    secondsLeft: 2 * 86400 + 4 * 3600,
    claimed: false,
  },
  {
    id: 5,
    group: 'weekly',
    title: 'Đọc 10 tài liệu mới',
    description: 'Khám phá tài liệu ở các chủ đề khác nhau.',
    icon: 'book-open',
    accent: 'cyan',
    current: 10,
    total: 10,
    reward: { kind: 'coin', amount: 300 },
    difficulty: 'medium',
    participants: 1820,
    secondsLeft: 2 * 86400 + 4 * 3600,
    claimed: false,
  },
  {
    id: 6,
    group: 'weekly',
    title: 'Top 100 bảng xếp hạng tuần',
    description: 'Tích lũy đủ điểm để lọt vào top 100.',
    icon: 'trophy',
    accent: 'violet',
    current: 740,
    total: 1000,
    reward: { kind: 'xp', amount: 500 },
    difficulty: 'hard',
    participants: 5630,
    secondsLeft: 2 * 86400 + 4 * 3600,
    claimed: false,
  },
  {
    id: 7,
    group: 'seasonal',
    title: 'Bậc thầy mùa hè: 50 bài học',
    description: 'Hoàn thành 50 bài học trong mùa sự kiện.',
    icon: 'graduation',
    accent: 'magenta',
    current: 38,
    total: 50,
    reward: { kind: 'gem', amount: 20 },
    difficulty: 'hard',
    participants: 8740,
    secondsLeft: null,
    claimed: false,
  },
  {
    id: 8,
    group: 'seasonal',
    title: 'Nhà sưu tầm huy hiệu',
    description: 'Mở khóa 15 huy hiệu trong mùa này.',
    icon: 'badge',
    accent: 'amber',
    current: 15,
    total: 15,
    reward: { kind: 'coin', amount: 1000 },
    difficulty: 'hard',
    participants: 3290,
    secondsLeft: null,
    claimed: false,
  },
  {
    id: 9,
    group: 'seasonal',
    title: 'Chiến binh tập trung',
    description: 'Tích lũy 600 phút tập trung sâu.',
    icon: 'rocket',
    accent: 'cyan',
    current: 420,
    total: 600,
    reward: { kind: 'xp', amount: 1500 },
    difficulty: 'medium',
    participants: 6120,
    secondsLeft: null,
    claimed: false,
  },
]);

function claimReward(id: number): void {
  const ch = challenges.value.find((c) => c.id === id);
  if (!ch || ch.claimed || ch.current < ch.total) return;
  ch.claimed = true;
  if (ch.reward.kind === 'coin') coinBalance.value += ch.reward.amount;
  else if (ch.reward.kind === 'xp') xpBalance.value += ch.reward.amount;
  else gemBalance.value += ch.reward.amount;

  claimPulseId.value = id;
  window.setTimeout(() => {
    if (claimPulseId.value === id) claimPulseId.value = null;
  }, 900);
}

function isComplete(ch: Challenge): boolean {
  return ch.current >= ch.total;
}

function progressPct(ch: Challenge): number {
  return ch.total === 0 ? 0 : Math.round((ch.current / ch.total) * 100);
}

function timeChip(ch: Challenge): string | null {
  if (ch.secondsLeft === null) return null;
  const t = Math.max(0, ch.secondsLeft);
  const d = Math.floor(t / 86400);
  const h = Math.floor((t % 86400) / 3600);
  const m = Math.floor((t % 3600) / 60);
  const s = t % 60;
  if (d > 0) return `${d}n ${pad(h)}:${pad(m)}:${pad(s)}`;
  return `${pad(h)}:${pad(m)}:${pad(s)}`;
}

// ─── Filter tabs ──────────────────────────────────────────────────────────

const activeTab = ref<ChallengeTab>('daily');

const tabs = computed<{ id: string; label: string; badge: number }[]>(() => [
  { id: 'daily', label: 'Hằng ngày', badge: challenges.value.filter((c) => c.group === 'daily').length },
  { id: 'weekly', label: 'Hằng tuần', badge: challenges.value.filter((c) => c.group === 'weekly').length },
  { id: 'seasonal', label: 'Theo mùa', badge: challenges.value.filter((c) => c.group === 'seasonal').length },
]);

const visibleChallenges = computed<Challenge[]>(() =>
  challenges.value.filter((c) => c.group === activeTab.value),
);

// ─── Overall season progress ─────────────────────────────────────────────────

const seasonalChallenges = computed<Challenge[]>(() => challenges.value.filter((c) => c.group === 'seasonal'));
const seasonProgress = computed<number>(() => {
  const list = seasonalChallenges.value;
  if (list.length === 0) return 0;
  const sum = list.reduce((acc, c) => acc + Math.min(1, c.current / c.total), 0);
  return Math.round((sum / list.length) * 100);
});

// ─── Mini leaderboard ──────────────────────────────────────────────────────

const leaderboard: LeaderRow[] = [
  { rank: 1, name: 'Trần Đức Anh', points: 9840, accent: 'amber' },
  { rank: 2, name: 'Lê Thu Hà', points: 9120, accent: 'cyan' },
  { rank: 3, name: 'Đỗ Khánh Linh', points: 8760, accent: 'violet' },
];

const rankBadge: Record<number, string> = {
  1: 'bg-amber-500/20 text-amber-300',
  2: 'bg-slate-400/20 text-slate-200',
  3: 'bg-orange-700/30 text-orange-300',
};

// ─── KPI ──────────────────────────────────────────────────────────────────

const activeCount = computed<number>(() => challenges.value.filter((c) => !c.claimed && !isComplete(c)).length);
const completedCount = computed<number>(() => challenges.value.filter((c) => isComplete(c)).length);
const claimedCount = computed<number>(() => challenges.value.filter((c) => c.claimed).length);

const kpis = computed<{ label: string; value: string; icon: string; accent: Accent }[]>(() => [
  { label: 'Đang tham gia', value: String(activeCount.value), icon: 'route', accent: 'cyan' },
  { label: 'Đã hoàn thành', value: String(completedCount.value), icon: 'check-circle', accent: 'green' },
  { label: 'Thưởng đã nhận', value: String(claimedCount.value), icon: 'gift', accent: 'amber' },
]);

// ─── Lifecycle ───────────────────────────────────────────────────────────────

onMounted(() => {
  clockId = window.setInterval(() => {
    if (seasonTotalSeconds.value > 0) seasonTotalSeconds.value -= 1;
    for (const ch of challenges.value) {
      if (ch.secondsLeft !== null && ch.secondsLeft > 0) ch.secondsLeft -= 1;
    }
  }, 1000);
});

onUnmounted(() => {
  if (clockId !== null) {
    window.clearInterval(clockId);
    clockId = null;
  }
});

// ─── Accent helpers ──────────────────────────────────────────────────────────

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

const seasonUnits = computed<{ label: string; value: string }[]>(() => [
  { label: 'Ngày', value: pad(seasonCountdown.value.days) },
  { label: 'Giờ', value: pad(seasonCountdown.value.hours) },
  { label: 'Phút', value: pad(seasonCountdown.value.minutes) },
  { label: 'Giây', value: pad(seasonCountdown.value.seconds) },
]);
</script>

<template>
  <div class="space-y-6">
    <!-- ───────────────── PAGE HEADER ───────────────── -->
    <div>
      <p class="cosmic-badge">Thử thách & phần thưởng</p>
      <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
        <span class="cosmic-gradient-text">Thử thách</span> chinh phục mỗi ngày
      </h1>
      <p class="mt-1 text-sm text-muted-foreground">
        Hoàn thành mục tiêu, leo bảng xếp hạng và nhận thưởng coin, XP, gem.
      </p>
    </div>

    <!-- ───────────────── FEATURED SEASONAL HERO ───────────────── -->
    <GlassPanel glow="violet" class="season-hero relative p-6 sm:p-8">
      <div class="relative z-10 grid grid-cols-1 gap-6 lg:grid-cols-[1fr_auto_1fr] lg:items-center">
        <!-- left: title + countdown -->
        <div>
          <span class="inline-flex items-center gap-1.5 rounded-sm bg-violet-500/15 px-3 py-1 text-xs font-semibold text-violet-200">
            <AppIcon name="crown" class="h-3.5 w-3.5" /> Mùa giải đang diễn ra
          </span>
          <h2 class="mt-3 text-xl font-bold text-foreground sm:text-2xl">Mùa hè rực sáng: Hành trình tri thức</h2>
          <p class="mt-1.5 text-sm text-muted-foreground">Hoàn thành các nhiệm vụ theo mùa để mở khóa phần thưởng huyền thoại.</p>

          <p class="mt-4 text-xs uppercase tracking-wider text-muted-foreground">Mùa giải kết thúc sau</p>
          <div class="mt-2 grid max-w-md grid-cols-4 gap-2">
            <div
              v-for="unit in seasonUnits"
              :key="unit.label"
              class="rounded-sm border border-white/[0.06] bg-white/[0.03] py-2.5 text-center"
            >
              <p class="text-xl font-bold tabular-nums text-foreground sm:text-2xl">{{ unit.value }}</p>
              <p class="text-[0.6rem] uppercase text-muted-foreground">{{ unit.label }}</p>
            </div>
          </div>

          <!-- reward preview -->
          <div class="mt-4 flex flex-wrap items-center gap-2">
            <span class="text-xs text-muted-foreground">Phần thưởng cuối mùa:</span>
            <span class="inline-flex items-center gap-1 rounded-sm bg-amber-500/10 px-2.5 py-1 text-xs font-semibold text-amber-300">
              <AppIcon name="coin" class="h-3.5 w-3.5" /> 5.000
            </span>
            <span class="inline-flex items-center gap-1 rounded-sm bg-violet-500/10 px-2.5 py-1 text-xs font-semibold text-violet-300">
              <AppIcon name="gem" class="h-3.5 w-3.5" /> 50
            </span>
            <span class="inline-flex items-center gap-1 rounded-sm bg-fuchsia-500/10 px-2.5 py-1 text-xs font-semibold text-fuchsia-300">
              <AppIcon name="award" class="h-3.5 w-3.5" /> Huy hiệu Huyền thoại
            </span>
          </div>
        </div>

        <!-- center: overall ring -->
        <div class="flex justify-center">
          <RingProgress :value="seasonProgress" :size="140" :stroke="12" accent="violet">
            <span class="text-3xl font-bold tabular-nums text-foreground">{{ seasonProgress }}%</span>
            <span class="text-xs text-muted-foreground">tiến độ mùa</span>
          </RingProgress>
        </div>

        <!-- right: mini leaderboard -->
        <div class="rounded-sm border border-white/[0.08] bg-white/[0.03] p-4">
          <h3 class="flex items-center gap-1.5 text-sm font-semibold text-foreground">
            <AppIcon name="trophy" class="h-4 w-4 text-amber-300" /> Top 3 mùa giải
          </h3>
          <ul class="mt-3 space-y-2">
            <li
              v-for="row in leaderboard"
              :key="row.rank"
              class="flex items-center gap-2.5 rounded-sm border border-white/[0.06] bg-white/[0.02] px-2.5 py-2"
            >
              <span
                class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm text-xs font-bold"
                :class="rankBadge[row.rank]"
              >
                {{ row.rank }}
              </span>
              <Avatar :name="row.name" :accent="row.accent" :size="28" />
              <span class="min-w-0 flex-1 truncate text-sm font-medium text-foreground">{{ row.name }}</span>
              <span class="shrink-0 text-xs font-semibold tabular-nums text-cyan-300">{{ row.points.toLocaleString('vi-VN') }}</span>
            </li>
          </ul>
        </div>
      </div>
    </GlassPanel>

    <!-- ───────────────── KPI + WALLET ───────────────── -->
    <section class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
      <GlassPanel v-for="kpi in kpis" :key="kpi.label" :glow="kpi.accent" hover padded>
        <div class="flex items-center gap-3">
          <span class="flex h-11 w-11 items-center justify-center rounded-sm" :class="accentChip[kpi.accent]">
            <AppIcon :name="kpi.icon" class="h-5 w-5" />
          </span>
          <div>
            <p class="text-xl font-bold tabular-nums text-foreground">{{ kpi.value }}</p>
            <p class="text-[0.7rem] text-muted-foreground">{{ kpi.label }}</p>
          </div>
        </div>
      </GlassPanel>

      <GlassPanel glow="amber" hover padded class="col-span-2 sm:col-span-3 lg:col-span-1">
        <p class="text-[0.7rem] font-medium uppercase tracking-wider text-muted-foreground">Số dư của bạn</p>
        <div class="mt-2 flex flex-wrap items-center gap-3">
          <span class="inline-flex items-center gap-1.5 text-sm font-bold text-amber-300">
            <AppIcon name="coin" class="h-4 w-4" /> {{ coinBalance.toLocaleString('vi-VN') }}
          </span>
          <span class="inline-flex items-center gap-1.5 text-sm font-bold text-cyan-300">
            <AppIcon name="zap" class="h-4 w-4" /> {{ xpBalance.toLocaleString('vi-VN') }}
          </span>
          <span class="inline-flex items-center gap-1.5 text-sm font-bold text-violet-300">
            <AppIcon name="gem" class="h-4 w-4" /> {{ gemBalance }}
          </span>
        </div>
      </GlassPanel>
    </section>

    <!-- ───────────────── CHALLENGE LIST ───────────────── -->
    <SectionPanel title="Danh sách thử thách" subtitle="Chọn mục tiêu phù hợp với bạn" icon="target" accent="cyan" flush>
      <template #header-actions>
        <Tabs :tabs="tabs" :model-value="activeTab" @update:model-value="(id: string) => (activeTab = id as ChallengeTab)" />
      </template>

      <div class="grid grid-cols-1 gap-4 px-5 pb-5 md:grid-cols-2 xl:grid-cols-3">
        <article
          v-for="ch in visibleChallenges"
          :key="ch.id"
          class="flex flex-col rounded-sm border bg-white/[0.02] p-4 transition-colors"
          :class="isComplete(ch) && !ch.claimed
            ? 'border-emerald-400/30 hover:border-emerald-400/50'
            : 'border-white/[0.06] hover:border-white/[0.12]'"
        >
          <div class="flex items-start gap-3">
            <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm" :class="accentChip[ch.accent]">
              <AppIcon :name="ch.icon" class="h-5 w-5" />
            </span>
            <div class="min-w-0 flex-1">
              <div class="flex items-center justify-between gap-2">
                <h4 class="truncate text-sm font-semibold text-foreground">{{ ch.title }}</h4>
                <span
                  class="shrink-0 rounded-sm px-2 py-0.5 text-[0.6rem] font-semibold"
                  :class="difficultyMeta[ch.difficulty].class"
                >
                  {{ difficultyMeta[ch.difficulty].label }}
                </span>
              </div>
              <p class="mt-0.5 text-xs text-muted-foreground">{{ ch.description }}</p>
            </div>
          </div>

          <!-- progress -->
          <div class="mt-4">
            <div class="mb-1.5 flex items-center justify-between text-xs">
              <span class="text-muted-foreground">Tiến độ</span>
              <span class="font-semibold tabular-nums text-foreground">{{ ch.current }}/{{ ch.total }}</span>
            </div>
            <ProgressBar :value="progressPct(ch)" :accent="isComplete(ch) ? 'green' : ch.accent" />
          </div>

          <!-- meta row -->
          <div class="mt-3 flex flex-wrap items-center gap-2 text-xs">
            <span
              class="inline-flex items-center gap-1 rounded-sm px-2 py-0.5 font-semibold"
              :class="accentChip[rewardMeta[ch.reward.kind].accent]"
            >
              <AppIcon :name="rewardMeta[ch.reward.kind].icon" class="h-3.5 w-3.5" />
              {{ ch.reward.amount }} {{ rewardMeta[ch.reward.kind].label }}
            </span>
            <span class="inline-flex items-center gap-1 text-muted-foreground">
              <AppIcon name="users" class="h-3.5 w-3.5" /> {{ ch.participants.toLocaleString('vi-VN') }}
            </span>
            <span
              v-if="timeChip(ch)"
              class="ml-auto inline-flex items-center gap-1 rounded-sm bg-white/[0.04] px-2 py-0.5 font-medium tabular-nums text-muted-foreground"
            >
              <AppIcon name="clock" class="h-3.5 w-3.5" /> {{ timeChip(ch) }}
            </span>
          </div>

          <!-- action -->
          <div class="mt-4">
            <button
              v-if="isComplete(ch) && !ch.claimed"
              type="button"
              class="claim-btn flex w-full items-center justify-center gap-2 rounded-sm border border-emerald-400/40 bg-emerald-500/15 px-4 py-2.5 text-sm font-semibold text-emerald-200 transition-colors hover:bg-emerald-500/25"
              :class="{ 'claim-pulsing': claimPulseId === ch.id }"
              @click="claimReward(ch.id)"
            >
              <AppIcon name="gift" class="h-4 w-4" /> Nhận thưởng
            </button>
            <span
              v-else-if="ch.claimed"
              class="flex w-full items-center justify-center gap-2 rounded-sm border border-white/[0.08] bg-white/[0.02] px-4 py-2.5 text-sm font-semibold text-muted-foreground"
            >
              <AppIcon name="check-circle" class="h-4 w-4 text-emerald-300" /> Đã nhận
            </span>
            <span
              v-else
              class="flex w-full items-center justify-center gap-2 rounded-sm border border-white/[0.06] bg-white/[0.02] px-4 py-2.5 text-sm font-medium text-muted-foreground"
            >
              <AppIcon name="route" class="h-4 w-4" /> Còn {{ ch.total - ch.current }} để hoàn thành
            </span>
          </div>
        </article>
      </div>

      <p v-if="visibleChallenges.length === 0" class="px-5 pb-6 text-center text-sm text-muted-foreground">
        Chưa có thử thách nào trong mục này.
      </p>
    </SectionPanel>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.season-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 1rem;
  background: radial-gradient(circle at 15% 20%, oklch(0.62 0.2 290 / 14%), transparent 55%);
  pointer-events: none;
}

.claim-btn {
  animation: claim-glow 2s ease-in-out infinite;
}

@keyframes claim-glow {
  0%,
  100% {
    box-shadow: 0 0 0 0 oklch(0.75 0.18 145 / 0%);
  }
  50% {
    box-shadow: 0 0 18px 0 oklch(0.75 0.18 145 / 45%);
  }
}

.claim-pulsing {
  animation: claim-pop 0.6s ease-out;
}

@keyframes claim-pop {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.06);
  }
  100% {
    transform: scale(1);
  }
}

@media (prefers-reduced-motion: reduce) {
  .claim-btn,
  .claim-pulsing {
    animation: none;
  }
}
</style>
