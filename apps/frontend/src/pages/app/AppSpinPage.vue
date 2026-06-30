<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type RewardKind = 'coin' | 'gem' | 'ticket' | 'jackpot' | 'empty';

interface Segment {
  id: number;
  label: string;
  short: string;
  icon: string;
  kind: RewardKind;
  amount: number;
  color: string; // wheel wedge fill
  weight: number; // odds weighting
  accent: Accent;
}

interface WinRecord {
  id: number;
  label: string;
  icon: string;
  amount: number;
  kind: RewardKind;
  accent: Accent;
  time: string;
}

interface Confetto {
  id: number;
  left: number;
  delay: number;
  duration: number;
  color: string;
  rotate: number;
  size: number;
}

// ── Wheel definition (8 segments) ───────────────────────────────────────────
const segments: Segment[] = [
  { id: 0, label: '50 coin', short: '50', icon: 'coin', kind: 'coin', amount: 50, color: 'oklch(0.78 0.18 195 / 85%)', weight: 22, accent: 'cyan' },
  { id: 1, label: 'Chúc may mắn', short: 'Tiếc quá!', icon: 'sparkles', kind: 'empty', amount: 0, color: 'oklch(0.22 0.04 280 / 90%)', weight: 16, accent: 'violet' },
  { id: 2, label: '120 coin', short: '120', icon: 'coin', kind: 'coin', amount: 120, color: 'oklch(0.8 0.16 85 / 88%)', weight: 18, accent: 'amber' },
  { id: 3, label: '1 vé quay', short: '+1 vé', icon: 'gift', kind: 'ticket', amount: 1, color: 'oklch(0.75 0.18 145 / 88%)', weight: 12, accent: 'green' },
  { id: 4, label: '5 gem', short: '5 gem', icon: 'gem', kind: 'gem', amount: 5, color: 'oklch(0.68 0.22 350 / 88%)', weight: 10, accent: 'magenta' },
  { id: 5, label: '300 coin', short: '300', icon: 'coin', kind: 'coin', amount: 300, color: 'oklch(0.62 0.2 290 / 90%)', weight: 8, accent: 'violet' },
  { id: 6, label: 'Chúc may mắn', short: 'Tiếc quá!', icon: 'sparkles', kind: 'empty', amount: 0, color: 'oklch(0.2 0.05 280 / 92%)', weight: 12, accent: 'violet' },
  { id: 7, label: 'JACKPOT 1000', short: 'JACKPOT', icon: 'crown', kind: 'jackpot', amount: 1000, color: 'oklch(0.82 0.18 60 / 95%)', weight: 2, accent: 'amber' },
];

const segCount = segments.length;
const arc = 360 / segCount; // 45deg per wedge

// ── State ────────────────────────────────────────────────────────────────────
const coins = ref<number>(1250);
const gems = ref<number>(8);
const tickets = ref<number>(3);
const freeSpinUsed = ref<boolean>(false);
const spinning = ref<boolean>(false);
const rotation = ref<number>(0); // cumulative, always increasing
const winner = ref<Segment | null>(null);
const showModal = ref<boolean>(false);
const history = ref<WinRecord[]>([
  { id: -1, label: '50 coin', icon: 'coin', amount: 50, kind: 'coin', accent: 'cyan', time: '08:12' },
  { id: -2, label: 'Chúc may mắn', icon: 'sparkles', amount: 0, kind: 'empty', accent: 'violet', time: '07:48' },
]);
const confetti = ref<Confetto[]>([]);
const spinCost = 100;

let recordSeq = 0;
let historySeq = 0;
let timers: ReturnType<typeof setTimeout>[] = [];

// ── Derived ──────────────────────────────────────────────────────────────────
const canFreeSpin = computed<boolean>(() => !freeSpinUsed.value);
const totalSpinsLeft = computed<number>(() => (canFreeSpin.value ? 1 : 0) + tickets.value);
const wheelGradient = computed<string>(() => {
  const stops = segments
    .map((s, i) => `${s.color} ${i * arc}deg ${(i + 1) * arc}deg`)
    .join(', ');
  return `conic-gradient(from -${arc / 2}deg, ${stops})`;
});

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

const confettiPalette = [
  'oklch(0.78 0.18 195)',
  'oklch(0.68 0.22 350)',
  'oklch(0.8 0.16 85)',
  'oklch(0.75 0.18 145)',
  'oklch(0.62 0.2 290)',
];

// ── Logic ──────────────────────────────────────────────────────────────────
function labelTransform(id: number): string {
  return `translate(-50%, -100%) rotate(${id * arc}deg)`;
}

function pickWinningIndex(): number {
  const totalWeight = segments.reduce((sum, s) => sum + s.weight, 0);
  let roll = Math.random() * totalWeight;
  for (const s of segments) {
    roll -= s.weight;
    if (roll <= 0) return s.id;
  }
  return segments.length - 1;
}

function nowLabel(): string {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function spawnConfetti(): void {
  recordSeq += 1;
  const batch = recordSeq * 1000;
  const pieces: Confetto[] = Array.from({ length: 40 }, (_, i) => ({
    id: batch + i,
    left: Math.random() * 100,
    delay: Math.random() * 0.35,
    duration: 1.1 + Math.random() * 0.9,
    color: confettiPalette[i % confettiPalette.length],
    rotate: Math.random() * 360,
    size: 6 + Math.random() * 8,
  }));
  confetti.value = pieces;
  const t = setTimeout(() => {
    confetti.value = [];
  }, 2400);
  timers.push(t);
}

function spin(useFree: boolean): void {
  if (spinning.value || totalSpinsLeft.value <= 0) return;
  if (useFree) {
    freeSpinUsed.value = true;
  } else {
    tickets.value -= 1;
  }

  spinning.value = true;
  winner.value = null;
  showModal.value = false;

  const targetIndex = pickWinningIndex();
  // Pointer is fixed at the top (12 o'clock). Wheel rotates; we want the chosen
  // wedge centre to land under the pointer. Segment i centre sits at angle
  // (i * arc) from the wheel's 0deg. To bring it to the top we subtract that.
  const segmentCenter = targetIndex * arc;
  const fullTurns = 5 + Math.floor(Math.random() * 2); // 5..6 turns
  const current = rotation.value;
  const currentMod = ((current % 360) + 360) % 360;
  // Solve for delta so that (current + delta) mod 360 == (360 - segmentCenter) mod 360
  const desiredMod = ((360 - segmentCenter) % 360 + 360) % 360;
  let deltaToAlign = desiredMod - currentMod;
  if (deltaToAlign < 0) deltaToAlign += 360;
  rotation.value = current + fullTurns * 360 + deltaToAlign;

  const settle = setTimeout(() => {
    const seg = segments[targetIndex];
    winner.value = seg;
    showModal.value = true;
    // apply reward
    if (seg.kind === 'coin' || seg.kind === 'jackpot') coins.value += seg.amount;
    else if (seg.kind === 'gem') gems.value += seg.amount;
    else if (seg.kind === 'ticket') tickets.value += seg.amount;
    historySeq += 1;
    history.value.unshift({
      id: historySeq,
      label: seg.label,
      icon: seg.icon,
      amount: seg.amount,
      kind: seg.kind,
      accent: seg.accent,
      time: nowLabel(),
    });
    history.value = history.value.slice(0, 8);
    if (seg.kind !== 'empty') spawnConfetti();
    spinning.value = false;
  }, 4100);
  timers.push(settle);
}

function buyTicket(): void {
  if (coins.value < spinCost || spinning.value) return;
  coins.value -= spinCost;
  tickets.value += 1;
}

function closeModal(): void {
  showModal.value = false;
}

onUnmounted(() => {
  timers.forEach((t) => clearTimeout(t));
  timers = [];
});
</script>

<template>
  <div class="space-y-6">
    <!-- Hero -->
    <GlassPanel glow="amber" class="relative p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Trò chơi · Phần thưởng</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Vòng quay may mắn</span> 🎡
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Mỗi ngày một lượt quay miễn phí. Dùng coin để mua thêm vé và săn giải
            <span class="font-semibold text-amber-300">JACKPOT 1000 coin</span>!
          </p>
          <div class="mt-4 flex flex-wrap gap-3">
            <div class="flex items-center gap-2 rounded-sm border border-amber-400/25 bg-amber-500/10 px-3 py-2">
              <AppIcon name="coin" class="h-5 w-5 text-amber-300" />
              <span class="text-lg font-bold tabular-nums text-amber-100">{{ coins.toLocaleString('vi-VN') }}</span>
            </div>
            <div class="flex items-center gap-2 rounded-sm border border-fuchsia-400/25 bg-fuchsia-500/10 px-3 py-2">
              <AppIcon name="gem" class="h-5 w-5 text-fuchsia-300" />
              <span class="text-lg font-bold tabular-nums text-fuchsia-100">{{ gems }}</span>
            </div>
            <div class="flex items-center gap-2 rounded-sm border border-cyan-400/25 bg-cyan-500/10 px-3 py-2">
              <AppIcon name="gift" class="h-5 w-5 text-cyan-300" />
              <span class="text-lg font-bold tabular-nums text-cyan-100">{{ tickets }}</span>
              <span class="text-xs text-muted-foreground">vé quay</span>
            </div>
          </div>
        </div>
        <div class="shrink-0 rounded-sm border border-white/[0.08] bg-white/[0.03] px-5 py-4 text-center">
          <p class="text-[0.7rem] uppercase tracking-wider text-muted-foreground">Lượt quay còn lại</p>
          <p class="mt-1 text-4xl font-bold tabular-nums text-amber-200 glow-magenta">{{ totalSpinsLeft }}</p>
          <p class="mt-1 text-xs text-muted-foreground">
            {{ canFreeSpin ? '1 miễn phí + ' : '' }}{{ tickets }} vé
          </p>
        </div>
      </div>
    </GlassPanel>

    <!-- Jackpot banner -->
    <div class="scan-line relative overflow-hidden rounded-sm border border-amber-400/30 from-amber-500/15 via-fuchsia-500/10 to-violet-500/15 p-4">
      <div class="jackpot-shimmer pointer-events-none absolute inset-0" />
      <div class="relative flex flex-wrap items-center justify-between gap-3">
        <div class="flex items-center gap-3">
          <span class="float-slow flex h-11 w-11 items-center justify-center rounded-sm bg-amber-400/20 text-amber-200">
            <AppIcon name="crown" class="h-6 w-6" />
          </span>
          <div>
            <p class="text-sm font-bold text-amber-100">Giải JACKPOT tuần này</p>
            <p class="text-xs text-muted-foreground">Cơ hội trúng 1.000 coin trong mỗi lượt quay</p>
          </div>
        </div>
        <p class="text-2xl font-bold tabular-nums text-amber-200 glow-magenta sm:text-3xl">1.000 🪙</p>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Wheel column -->
      <div class="lg:col-span-2">
        <GlassPanel glow="violet" class="flex flex-col items-center p-6 sm:p-8">
          <div class="relative mx-auto aspect-square w-full max-w-[420px]">
            <!-- Pointer -->
            <div class="pointer-events-none absolute left-1/2 top-[-6px] z-20 -translate-x-1/2">
              <div class="pointer-glow h-0 w-0 border-x-[14px] border-t-[26px] border-x-transparent border-t-amber-300" />
            </div>
            <!-- Outer glow ring -->
            <div class="absolute inset-[-10px] rounded-sm border border-amber-300/30" :class="spinning ? 'pulse-glow-magenta' : ''" />
            <!-- Wheel -->
            <div
              class="absolute inset-0 rounded-sm border-4 border-white/10"
              :style="{
                background: wheelGradient,
                transform: `rotate(${rotation}deg)`,
                transition: spinning ? 'transform 4s cubic-bezier(0.16,1,0.3,1)' : 'none',
              }"
            >
              <!-- Segment labels -->
              <div
                v-for="seg in segments"
                :key="seg.id"
                class="absolute left-1/2 top-1/2 origin-bottom"
                :style="{ height: '50%', transform: labelTransform(seg.id) }"
              >
                <div class="flex -translate-x-1/2 flex-col items-center gap-1 pt-5 text-center text-white">
                  <AppIcon :name="seg.icon" class="h-5 w-5 drop-shadow" />
                  <span class="text-[0.7rem] font-bold leading-tight drop-shadow" style="text-shadow: 0 1px 3px rgba(0,0,0,0.6)">{{ seg.short }}</span>
                </div>
              </div>
            </div>
            <!-- Center hub / spin button -->
            <div class="absolute left-1/2 top-1/2 z-10 -translate-x-1/2 -translate-y-1/2">
              <button
                type="button"
                class="cosmic-btn flex h-24 w-24 flex-col items-center justify-center rounded-sm text-base disabled:cursor-not-allowed"
                :disabled="spinning || totalSpinsLeft <= 0"
                @click="spin(canFreeSpin)"
              >
                <AppIcon name="refresh" class="h-5 w-5" :class="spinning ? 'rotate-slow' : ''" />
                <span class="mt-1 text-sm font-extrabold tracking-wide">{{ spinning ? '...' : 'QUAY' }}</span>
              </button>
            </div>
          </div>

          <!-- Action buttons -->
          <div class="mt-7 flex w-full max-w-[420px] flex-col gap-3 sm:flex-row">
            <button
              type="button"
              class="flex-1 rounded-sm border px-4 py-2.5 text-sm font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-50"
              :class="canFreeSpin && !spinning
                ? 'border-emerald-400/40 bg-emerald-500/10 text-emerald-200 hover:bg-emerald-500/20'
                : 'border-white/[0.08] bg-white/[0.03] text-muted-foreground'"
              :disabled="!canFreeSpin || spinning"
              @click="spin(true)"
            >
              <AppIcon name="gift" class="mr-1.5 inline h-4 w-4" />
              {{ canFreeSpin ? 'Quay miễn phí hôm nay' : 'Đã dùng lượt miễn phí' }}
            </button>
            <button
              type="button"
              class="flex-1 rounded-sm border border-cyan-400/30 bg-cyan-500/10 px-4 py-2.5 text-sm font-semibold text-cyan-200 transition-colors hover:bg-cyan-500/20 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="tickets <= 0 || spinning"
              @click="spin(false)"
            >
              <AppIcon name="refresh" class="mr-1.5 inline h-4 w-4" />
              Quay bằng vé ({{ tickets }})
            </button>
          </div>
          <button
            type="button"
            class="mt-3 inline-flex items-center gap-1.5 text-xs font-medium text-amber-300 transition-colors hover:text-amber-200 disabled:opacity-50"
            :disabled="coins < spinCost || spinning"
            @click="buyTicket"
          >
            <AppIcon name="plus" class="h-3.5 w-3.5" /> Mua thêm vé · {{ spinCost }} coin
          </button>
        </GlassPanel>
      </div>

      <!-- Right column -->
      <div class="space-y-6">
        <SectionPanel title="Các giải thưởng" subtitle="8 ô · tỉ lệ trúng khác nhau" icon="star" accent="amber">
          <ul class="space-y-2">
            <li
              v-for="seg in segments"
              :key="seg.id"
              class="flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-2"
            >
              <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm" :class="accentChip[seg.accent]">
                <AppIcon :name="seg.icon" class="h-4 w-4" />
              </span>
              <span class="min-w-0 flex-1 truncate text-sm font-medium text-foreground">{{ seg.label }}</span>
              <span class="shrink-0 text-xs tabular-nums text-muted-foreground">{{ seg.weight }}%</span>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Lịch sử trúng thưởng" subtitle="Hôm nay" icon="clock" accent="cyan" flush>
          <ul v-if="history.length" class="divide-y divide-white/[0.05]">
            <li v-for="rec in history" :key="rec.id" class="flex items-center gap-3 px-5 py-2.5">
              <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm" :class="accentChip[rec.accent]">
                <AppIcon :name="rec.icon" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ rec.label }}</p>
                <p class="text-[0.7rem] text-muted-foreground">{{ rec.time }}</p>
              </div>
              <span
                v-if="rec.kind !== 'empty'"
                class="shrink-0 text-sm font-semibold"
                :class="rec.kind === 'jackpot' ? 'text-amber-300' : 'text-emerald-300'"
              >+{{ rec.amount }}</span>
              <span v-else class="shrink-0 text-xs text-muted-foreground">—</span>
            </li>
          </ul>
          <p v-else class="px-5 py-6 text-center text-sm text-muted-foreground">Chưa có lượt quay nào hôm nay.</p>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>

    <!-- Win modal -->
    <Transition name="win-pop">
      <div
        v-if="showModal && winner"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
      >
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="closeModal" />
        <!-- Confetti -->
        <div class="pointer-events-none absolute inset-0 overflow-hidden">
          <span
            v-for="c in confetti"
            :key="c.id"
            class="confetto absolute top-[-20px] rounded-sm"
            :style="{
              left: `${c.left}%`,
              width: `${c.size}px`,
              height: `${c.size * 1.6}px`,
              background: c.color,
              animationDelay: `${c.delay}s`,
              animationDuration: `${c.duration}s`,
              transform: `rotate(${c.rotate}deg)`,
            }"
          />
        </div>
        <GlassPanel :glow="winner.accent" class="relative z-10 w-full max-w-sm p-6 text-center">
          <span
            class="mx-auto flex h-20 w-20 items-center justify-center rounded-sm"
            :class="[accentChip[winner.accent], winner.kind === 'empty' ? '' : 'pulse-glow-cyan']"
          >
            <AppIcon :name="winner.icon" class="h-10 w-10" />
          </span>
          <p class="mt-4 text-sm uppercase tracking-wider text-muted-foreground">
            {{ winner.kind === 'empty' ? 'Rất tiếc!' : winner.kind === 'jackpot' ? 'JACKPOT! 🎉' : 'Chúc mừng! 🎉' }}
          </p>
          <h3 class="mt-1 text-2xl font-bold text-foreground">{{ winner.label }}</h3>
          <p class="mt-2 text-sm text-muted-foreground">
            {{ winner.kind === 'empty'
              ? 'Lần sau may mắn hơn nhé! Hãy thử thêm một vé.'
              : `Phần thưởng đã được cộng vào tài khoản của bạn.` }}
          </p>
          <button type="button" class="cosmic-btn mt-5 w-full rounded-sm px-4 py-2.5 text-sm" @click="closeModal">
            Tuyệt vời!
          </button>
        </GlassPanel>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.pointer-glow {
  filter: drop-shadow(0 0 8px oklch(0.8 0.16 85 / 80%));
}

.jackpot-shimmer {
  background: linear-gradient(110deg, transparent 30%, oklch(0.95 0.05 85 / 22%) 50%, transparent 70%);
  background-size: 220% 100%;
  animation: jackpot-sweep 3s linear infinite;
}
@keyframes jackpot-sweep {
  0% { background-position: 180% 0; }
  100% { background-position: -80% 0; }
}

.confetto {
  animation-name: confetto-fall;
  animation-timing-function: cubic-bezier(0.3, 0.6, 0.6, 1);
  animation-fill-mode: forwards;
}
@keyframes confetto-fall {
  0% { transform: translateY(0) rotate(0deg); opacity: 1; }
  100% { transform: translateY(105vh) rotate(540deg); opacity: 0.2; }
}

.win-pop-enter-active { transition: opacity 0.25s ease; }
.win-pop-leave-active { transition: opacity 0.2s ease; }
.win-pop-enter-from,
.win-pop-leave-to { opacity: 0; }
</style>
