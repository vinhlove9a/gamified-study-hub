<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type RewardKind = 'coin' | 'gem' | 'box';
type DayState = 'claimed' | 'today' | 'locked';

interface WeekDay {
  index: number; // 1..7
  label: string;
  reward: number;
  kind: RewardKind;
  milestone?: boolean;
}

interface CalDay {
  day: number; // 1..30
  reward: number;
  kind: RewardKind;
  milestone?: boolean;
}

interface Spark {
  id: number;
  angle: number;
  distance: number;
  delay: number;
  color: string;
}

// ── State ────────────────────────────────────────────────────────────────────
// "Today" is the 4th day of a streak; days 1..3 already claimed.
const todayIndex = ref<number>(4);
const claimedToday = ref<boolean>(false);
const streak = ref<number>(3);
const bestStreak = ref<number>(21);
const coins = ref<number>(1250);
const gems = ref<number>(8);
const claiming = ref<boolean>(false);
const revealed = ref<boolean>(false);
const sparks = ref<Spark[]>([]);

let sparkSeq = 0;
let timers: ReturnType<typeof setTimeout>[] = [];

// ── Mock data ────────────────────────────────────────────────────────────────
const weekDays: WeekDay[] = [
  { index: 1, label: 'Ngày 1', reward: 20, kind: 'coin' },
  { index: 2, label: 'Ngày 2', reward: 30, kind: 'coin' },
  { index: 3, label: 'Ngày 3', reward: 40, kind: 'coin' },
  { index: 4, label: 'Ngày 4', reward: 50, kind: 'coin' },
  { index: 5, label: 'Ngày 5', reward: 2, kind: 'gem' },
  { index: 6, label: 'Ngày 6', reward: 80, kind: 'coin' },
  { index: 7, label: 'Ngày 7', reward: 200, kind: 'box', milestone: true },
];

function calKind(day: number): RewardKind {
  if (day === 7 || day === 14 || day === 30) return 'box';
  if (day % 5 === 0) return 'gem';
  return 'coin';
}
function calReward(day: number): number {
  if (day === 30) return 1000;
  if (day === 14) return 400;
  if (day === 7) return 200;
  if (day % 5 === 0) return 3;
  return 20 + day * 4;
}
const calendar: CalDay[] = Array.from({ length: 30 }, (_, i) => {
  const day = i + 1;
  return {
    day,
    reward: calReward(day),
    kind: calKind(day),
    milestone: day === 7 || day === 14 || day === 30,
  };
});

const monthClaimed = ref<number>(3); // days checked in this month

// ── Derived ──────────────────────────────────────────────────────────────────
const todayReward = computed<WeekDay>(() => weekDays[todayIndex.value - 1]);
const monthPct = computed<number>(() => Math.round((monthClaimed.value / 30) * 100));
const weekTotalPreview = computed<number>(() => weekDays.reduce((s, d) => s + (d.kind === 'coin' ? d.reward : 0), 0));

function dayState(index: number): DayState {
  if (index < todayIndex.value) return 'claimed';
  if (index === todayIndex.value) return claimedToday.value ? 'claimed' : 'today';
  return 'locked';
}
function calState(day: number): DayState {
  if (day < todayIndex.value) return 'claimed';
  if (day === todayIndex.value) return claimedToday.value ? 'claimed' : 'today';
  return 'locked';
}

const kindIcon: Record<RewardKind, string> = { coin: 'coin', gem: 'gem', box: 'gift' };
const kindAccent: Record<RewardKind, Accent> = { coin: 'amber', gem: 'magenta', box: 'violet' };
const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

const sparkPalette = [
  'oklch(0.8 0.16 85)',
  'oklch(0.78 0.18 195)',
  'oklch(0.68 0.22 350)',
  'oklch(0.75 0.18 145)',
];

// ── Logic ──────────────────────────────────────────────────────────────────
function spawnSparks(): void {
  sparkSeq += 1;
  const batch = sparkSeq * 100;
  sparks.value = Array.from({ length: 16 }, (_, i) => ({
    id: batch + i,
    angle: (360 / 16) * i + Math.random() * 12,
    distance: 60 + Math.random() * 50,
    delay: Math.random() * 0.12,
    color: sparkPalette[i % sparkPalette.length],
  }));
  const t = setTimeout(() => {
    sparks.value = [];
  }, 1200);
  timers.push(t);
}

function claim(): void {
  if (claimedToday.value || claiming.value) return;
  claiming.value = true;
  spawnSparks();
  const reward = todayReward.value;
  const t1 = setTimeout(() => {
    revealed.value = true;
    if (reward.kind === 'gem') gems.value += reward.reward;
    else coins.value += reward.reward;
    claimedToday.value = true;
    streak.value += 1;
    monthClaimed.value += 1;
    claiming.value = false;
  }, 650);
  timers.push(t1);
}

onUnmounted(() => {
  timers.forEach((t) => clearTimeout(t));
  timers = [];
});
</script>

<template>
  <div class="space-y-6">
    <!-- Hero -->
    <GlassPanel glow="amber" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Phần thưởng · Điểm danh</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Quà hằng ngày</span> 🎁
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Điểm danh mỗi ngày để nhận quà và giữ chuỗi. Càng nhiều ngày liên tiếp,
            phần thưởng càng lớn — đừng bỏ lỡ mốc ngày 7, 14 và 30!
          </p>
          <div class="mt-4 flex flex-wrap gap-3">
            <div class="flex items-center gap-2 rounded-sm border border-orange-400/30 bg-orange-500/10 px-3.5 py-2">
              <AppIcon name="flame" class="streak-flame h-6 w-6 text-orange-300" />
              <div>
                <p class="text-lg font-bold leading-none text-orange-100">{{ streak }} ngày</p>
                <p class="text-[0.7rem] text-muted-foreground">Chuỗi điểm danh</p>
              </div>
            </div>
            <div class="flex items-center gap-2 rounded-sm border border-amber-400/25 bg-amber-500/10 px-3 py-2">
              <AppIcon name="coin" class="h-5 w-5 text-amber-300" />
              <span class="text-lg font-bold tabular-nums text-amber-100">{{ coins.toLocaleString('vi-VN') }}</span>
            </div>
            <div class="flex items-center gap-2 rounded-sm border border-fuchsia-400/25 bg-fuchsia-500/10 px-3 py-2">
              <AppIcon name="gem" class="h-5 w-5 text-fuchsia-300" />
              <span class="text-lg font-bold tabular-nums text-fuchsia-100">{{ gems }}</span>
            </div>
          </div>
        </div>

        <!-- Chest / claim -->
        <div class="flex shrink-0 flex-col items-center">
          <button
            type="button"
            class="group relative flex h-32 w-32 items-center justify-center rounded-sm border transition-all disabled:cursor-not-allowed"
            :class="claimedToday
              ? 'border-emerald-400/40 bg-emerald-500/10'
              : 'border-amber-400/40 bg-amber-500/10 hover:scale-[1.03]'"
            :disabled="claimedToday || claiming"
            :aria-label="claimedToday ? 'Đã nhận quà hôm nay' : 'Nhận quà hôm nay'"
            @click="claim"
          >
            <!-- Sparks -->
            <span
              v-for="s in sparks"
              :key="s.id"
              class="spark pointer-events-none absolute left-1/2 top-1/2 h-2 w-2 rounded-sm"
              :style="{
                background: s.color,
                '--angle': `${s.angle}deg`,
                '--dist': `${s.distance}px`,
                animationDelay: `${s.delay}s`,
              }"
            />
            <AppIcon
              :name="claimedToday ? 'check-circle' : 'gift'"
              class="h-16 w-16 transition-transform"
              :class="[
                claimedToday ? 'text-emerald-300' : 'text-amber-300',
                claiming ? 'chest-bounce' : claimedToday ? '' : 'chest-pulse',
              ]"
            />
          </button>
          <button
            type="button"
            class="cosmic-btn mt-4 w-44 rounded-sm px-4 py-2.5 text-sm disabled:cursor-not-allowed"
            :disabled="claimedToday || claiming"
            @click="claim"
          >
            {{ claimedToday ? 'Đã nhận hôm nay ✓' : 'Nhận quà hôm nay' }}
          </button>
          <p v-if="revealed && claimedToday" class="mt-2 text-xs font-semibold text-emerald-300">
            +{{ todayReward.reward }} {{ todayReward.kind === 'gem' ? 'gem' : todayReward.kind === 'box' ? '(hộp quà)' : 'coin' }} 🎉
          </p>
        </div>
      </div>

      <!-- Monthly progress -->
      <div class="mt-5 border-t border-white/[0.06] pt-4">
        <div class="mb-1.5 flex items-center justify-between text-xs font-medium text-muted-foreground">
          <span>Tiến độ điểm danh tháng này</span>
          <span>{{ monthClaimed }}/30 ngày · {{ monthPct }}%</span>
        </div>
        <ProgressBar :value="monthPct" accent="amber" :height="8" />
      </div>
    </GlassPanel>

    <!-- 7-day streak row -->
    <SectionPanel title="Chuỗi 7 ngày" subtitle="Phần thưởng tăng dần mỗi ngày" icon="calendar" accent="green">
      <div class="grid grid-cols-4 gap-3 sm:grid-cols-7">
        <div
          v-for="d in weekDays"
          :key="d.index"
          class="relative flex flex-col items-center gap-2 rounded-sm border p-3 text-center transition-colors"
          :class="[
            dayState(d.index) === 'claimed' ? 'border-emerald-400/30 bg-emerald-500/10' : '',
            dayState(d.index) === 'today' ? 'border-amber-400/50 bg-amber-500/10 pulse-glow-magenta' : '',
            dayState(d.index) === 'locked' ? 'border-white/[0.06] bg-white/[0.02]' : '',
            d.milestone ? 'ring-1 ring-violet-400/40' : '',
          ]"
        >
          <span
            v-if="d.milestone"
            class="absolute -top-2 left-1/2 -translate-x-1/2 rounded-sm bg-violet-500/30 px-2 py-0.5 text-[0.6rem] font-bold uppercase tracking-wide text-violet-100"
          >Mốc</span>
          <span class="text-[0.7rem] font-medium text-muted-foreground">{{ d.label }}</span>
          <span
            class="flex h-11 w-11 items-center justify-center rounded-sm"
            :class="dayState(d.index) === 'locked' ? 'bg-white/[0.04] text-muted-foreground' : accentChip[kindAccent[d.kind]]"
          >
            <AppIcon :name="dayState(d.index) === 'claimed' ? 'check' : kindIcon[d.kind]" class="h-5 w-5" />
          </span>
          <span class="text-xs font-bold tabular-nums text-foreground">
            {{ d.reward }}<span class="text-muted-foreground"> {{ d.kind === 'gem' ? 'gem' : d.kind === 'box' ? '🎁' : '' }}</span>
          </span>
        </div>
      </div>
    </SectionPanel>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Calendar -->
      <div class="lg:col-span-2">
        <SectionPanel title="Lịch phần thưởng 30 ngày" subtitle="Mốc 7 · 14 · 30 có quà đặc biệt" icon="grid" accent="violet">
          <div class="grid grid-cols-5 gap-2.5 sm:grid-cols-6 lg:grid-cols-7">
            <div
              v-for="c in calendar"
              :key="c.day"
              class="relative flex flex-col items-center gap-1 rounded-sm border p-2 text-center transition-colors"
              :class="[
                calState(c.day) === 'claimed' ? 'border-emerald-400/25 bg-emerald-500/[0.08]' : '',
                calState(c.day) === 'today' ? 'border-amber-400/50 bg-amber-500/[0.12]' : '',
                calState(c.day) === 'locked' ? 'border-white/[0.06] bg-white/[0.02]' : '',
                c.milestone ? 'ring-1 ring-violet-400/50' : '',
              ]"
            >
              <span class="text-[0.65rem] text-muted-foreground">N{{ c.day }}</span>
              <span
                class="flex h-7 w-7 items-center justify-center rounded-sm"
                :class="[
                  calState(c.day) === 'locked' ? 'bg-white/[0.04] text-muted-foreground' : accentChip[kindAccent[c.kind]],
                  c.milestone ? '' : '',
                ]"
              >
                <AppIcon :name="calState(c.day) === 'claimed' ? 'check' : kindIcon[c.kind]" class="h-3.5 w-3.5" />
              </span>
              <span class="text-[0.65rem] font-semibold tabular-nums text-foreground">{{ c.reward }}</span>
            </div>
          </div>
          <div class="mt-4 flex flex-wrap items-center gap-4 text-xs text-muted-foreground">
            <span class="inline-flex items-center gap-1.5"><span class="h-3 w-3 rounded bg-emerald-500/40" /> Đã nhận</span>
            <span class="inline-flex items-center gap-1.5"><span class="h-3 w-3 rounded bg-amber-500/50" /> Hôm nay</span>
            <span class="inline-flex items-center gap-1.5"><span class="h-3 w-3 rounded bg-white/[0.1]" /> Chưa mở</span>
            <span class="inline-flex items-center gap-1.5"><span class="h-3 w-3 rounded ring-1 ring-violet-400/60" /> Mốc đặc biệt</span>
          </div>
        </SectionPanel>
      </div>

      <!-- Week preview -->
      <div class="space-y-6">
        <SectionPanel title="Phần thưởng tuần" subtitle="Tổng quà nếu điểm danh đủ 7 ngày" icon="gift" accent="amber">
          <div class="rounded-sm border border-amber-400/25 bg-amber-500/[0.07] p-4 text-center">
            <p class="text-3xl font-bold tabular-nums text-amber-200 glow-magenta">+{{ weekTotalPreview }}</p>
            <p class="mt-1 text-xs text-muted-foreground">coin + 2 gem + 1 hộp quà bí ẩn</p>
          </div>
          <ul class="mt-4 space-y-2">
            <li v-for="d in weekDays" :key="d.index" class="flex items-center gap-3 text-sm">
              <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm" :class="accentChip[kindAccent[d.kind]]">
                <AppIcon :name="kindIcon[d.kind]" class="h-3.5 w-3.5" />
              </span>
              <span class="flex-1 text-muted-foreground">{{ d.label }}</span>
              <span class="font-semibold text-foreground">{{ d.reward }} {{ d.kind === 'gem' ? 'gem' : d.kind === 'box' ? '🎁' : 'coin' }}</span>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Thống kê chuỗi" icon="flame" accent="magenta">
          <div class="grid grid-cols-2 gap-3">
            <div class="rounded-sm border border-white/[0.06] bg-white/[0.02] p-3 text-center">
              <p class="text-2xl font-bold tabular-nums text-foreground">{{ streak }}</p>
              <p class="text-[0.7rem] text-muted-foreground">Chuỗi hiện tại</p>
            </div>
            <div class="rounded-sm border border-white/[0.06] bg-white/[0.02] p-3 text-center">
              <p class="text-2xl font-bold tabular-nums text-amber-300">{{ bestStreak }}</p>
              <p class="text-[0.7rem] text-muted-foreground">Kỷ lục</p>
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
.streak-flame {
  filter: drop-shadow(0 0 8px oklch(0.7 0.2 50 / 70%));
  animation: flame-flicker 1.8s ease-in-out infinite;
}
@keyframes flame-flicker {
  0%, 100% { transform: scale(1); filter: drop-shadow(0 0 6px oklch(0.7 0.2 50 / 60%)); }
  50% { transform: scale(1.08); filter: drop-shadow(0 0 12px oklch(0.75 0.2 50 / 85%)); }
}

.chest-pulse { animation: chest-pulse 2.4s ease-in-out infinite; }
@keyframes chest-pulse {
  0%, 100% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.06) rotate(-2deg); }
}

.chest-bounce { animation: chest-bounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes chest-bounce {
  0% { transform: scale(1); }
  40% { transform: scale(1.3) rotate(8deg); }
  70% { transform: scale(0.92) rotate(-4deg); }
  100% { transform: scale(1) rotate(0deg); }
}

.spark {
  animation: spark-burst 0.9s ease-out forwards;
}
@keyframes spark-burst {
  0% { transform: translate(-50%, -50%) rotate(var(--angle)) translateY(0) scale(1); opacity: 1; }
  100% { transform: translate(-50%, -50%) rotate(var(--angle)) translateY(calc(var(--dist) * -1)) scale(0.2); opacity: 0; }
}
</style>
