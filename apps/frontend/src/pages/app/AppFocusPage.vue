<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Mode = 'focus' | 'short' | 'long';

interface ModeDef {
  id: Mode;
  label: string;
  minutes: number;
  accent: Accent;
}

interface Task {
  id: number;
  text: string;
  done: boolean;
}

interface AmbientSound {
  id: string;
  name: string;
  icon: string;
  accent: Accent;
  playing: boolean;
  volume: number;
}

interface FocusStat {
  label: string;
  value: string;
  unit?: string;
  icon: string;
  accent: Accent;
}

// ─── Mode config ──────────────────────────────────────────────────────────────

const modes: ModeDef[] = [
  { id: 'focus', label: 'Tập trung 25′', minutes: 25, accent: 'cyan' },
  { id: 'short', label: 'Nghỉ ngắn 5′', minutes: 5, accent: 'green' },
  { id: 'long', label: 'Nghỉ dài 15′', minutes: 15, accent: 'violet' },
];

const activeMode = ref<Mode>('focus');
const currentMode = computed<ModeDef>(() => modes.find((m) => m.id === activeMode.value) ?? modes[0]);

// ─── Timer state ──────────────────────────────────────────────────────────────

const totalSeconds = ref<number>(modes[0].minutes * 60);
const secondsLeft = ref<number>(modes[0].minutes * 60);
const running = ref<boolean>(false);
const justCompleted = ref<boolean>(false);
const showBreakHint = ref<boolean>(false);

let tickId: number | null = null;
let completeId: number | null = null;

const minutesDisplay = computed<string>(() => String(Math.floor(secondsLeft.value / 60)).padStart(2, '0'));
const secondsDisplay = computed<string>(() => String(secondsLeft.value % 60).padStart(2, '0'));

const progressPct = computed<number>(() => {
  if (totalSeconds.value === 0) return 0;
  return ((totalSeconds.value - secondsLeft.value) / totalSeconds.value) * 100;
});

// SVG ring geometry
const RING_SIZE = 260;
const RING_STROKE = 14;
const ringRadius = (RING_SIZE - RING_STROKE) / 2;
const ringCircumference = 2 * Math.PI * ringRadius;
const ringCenter = RING_SIZE / 2;
const ringOffset = computed<number>(() => ringCircumference * (1 - progressPct.value / 100));

const ringColor: Record<Accent, string> = {
  cyan: 'oklch(0.78 0.18 195)',
  magenta: 'oklch(0.68 0.22 350)',
  green: 'oklch(0.75 0.18 145)',
  violet: 'oklch(0.62 0.2 290)',
  amber: 'oklch(0.8 0.16 85)',
};

// ─── Timer controls ────────────────────────────────────────────────────────────

function clearTick(): void {
  if (tickId !== null) {
    window.clearInterval(tickId);
    tickId = null;
  }
}

function selectMode(mode: Mode): void {
  activeMode.value = mode;
  resetTimer();
}

function resetTimer(): void {
  clearTick();
  running.value = false;
  showBreakHint.value = false;
  totalSeconds.value = currentMode.value.minutes * 60;
  secondsLeft.value = currentMode.value.minutes * 60;
}

function toggleTimer(): void {
  if (running.value) {
    pauseTimer();
  } else {
    startTimer();
  }
}

function startTimer(): void {
  if (running.value) return;
  if (secondsLeft.value <= 0) {
    secondsLeft.value = totalSeconds.value;
  }
  running.value = true;
  showBreakHint.value = false;
  tickId = window.setInterval(() => {
    if (secondsLeft.value > 0) {
      secondsLeft.value -= 1;
    } else {
      completeSession();
    }
  }, 1000);
}

function pauseTimer(): void {
  running.value = false;
  clearTick();
}

function completeSession(): void {
  clearTick();
  running.value = false;
  secondsLeft.value = 0;
  justCompleted.value = true;
  if (completeId !== null) window.clearTimeout(completeId);
  completeId = window.setTimeout(() => {
    justCompleted.value = false;
  }, 1600);

  if (activeMode.value === 'focus') {
    completedPomodoros.value += 1;
    showBreakHint.value = true;
  }
}

function goToBreak(): void {
  selectMode('short');
}

// ─── Pomodoro session tracker ────────────────────────────────────────────────

const POMODORO_GOAL = 4;
const completedPomodoros = ref<number>(2);
const pomodoroDots = computed<boolean[]>(() =>
  Array.from({ length: POMODORO_GOAL }, (_, i) => i < (completedPomodoros.value % POMODORO_GOAL || (completedPomodoros.value > 0 ? POMODORO_GOAL : 0))),
);

// ─── Task checklist ────────────────────────────────────────────────────────────

const tasks = ref<Task[]>([
  { id: 1, text: 'Đọc chương 3 — Cấu trúc dữ liệu', done: true },
  { id: 2, text: 'Làm 10 bài tập đạo hàm', done: false },
  { id: 3, text: 'Ôn 20 thẻ ghi nhớ TOEIC', done: false },
]);
const newTask = ref<string>('');
let taskSeq = 4;

const doneTasks = computed<number>(() => tasks.value.filter((t) => t.done).length);

function addTask(): void {
  const text = newTask.value.trim();
  if (!text) return;
  tasks.value.push({ id: taskSeq, text, done: false });
  taskSeq += 1;
  newTask.value = '';
}

function toggleTask(id: number): void {
  const t = tasks.value.find((x) => x.id === id);
  if (t) t.done = !t.done;
}

function removeTask(id: number): void {
  tasks.value = tasks.value.filter((t) => t.id !== id);
}

// ─── Ambient sounds ──────────────────────────────────────────────────────────

const sounds = ref<AmbientSound[]>([
  { id: 'rain', name: 'Mưa', icon: 'globe', accent: 'cyan', playing: true, volume: 60 },
  { id: 'ocean', name: 'Sóng biển', icon: 'globe', accent: 'green', playing: false, volume: 45 },
  { id: 'fire', name: 'Lò sưởi', icon: 'flame', accent: 'amber', playing: false, volume: 70 },
  { id: 'cafe', name: 'Quán cà phê', icon: 'message', accent: 'magenta', playing: false, volume: 35 },
  { id: 'white', name: 'Tiếng ồn trắng', icon: 'layers', accent: 'violet', playing: false, volume: 50 },
]);

function toggleSound(id: string): void {
  const s = sounds.value.find((x) => x.id === id);
  if (s) s.playing = !s.playing;
}

const activeSounds = computed<number>(() => sounds.value.filter((s) => s.playing).length);

// ─── Today stats ───────────────────────────────────────────────────────────────

const focusStats = computed<FocusStat[]>(() => [
  { label: 'Số phiên hôm nay', value: String(completedPomodoros.value), unit: 'phiên', icon: 'target', accent: 'cyan' },
  { label: 'Tổng phút tập trung', value: String(completedPomodoros.value * 25), unit: 'phút', icon: 'clock', accent: 'violet' },
  { label: 'Chuỗi ngày', value: '9', unit: 'ngày', icon: 'flame', accent: 'magenta' },
]);

// ─── Cleanup ────────────────────────────────────────────────────────────────────

onUnmounted(() => {
  clearTick();
  if (completeId !== null) window.clearTimeout(completeId);
});

// ─── Accent helpers ────────────────────────────────────────────────────────────

const modeTabActive: Record<Accent, string> = {
  cyan: 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200 shadow-[inset_0_0_0_1px_oklch(0.78_0.18_195/30%)]',
  magenta: 'border-fuchsia-400/50 bg-fuchsia-500/15 text-fuchsia-200',
  green: 'border-emerald-400/50 bg-emerald-500/15 text-emerald-200 shadow-[inset_0_0_0_1px_oklch(0.75_0.18_145/30%)]',
  violet: 'border-violet-400/50 bg-violet-500/15 text-violet-200 shadow-[inset_0_0_0_1px_oklch(0.62_0.2_290/30%)]',
  amber: 'border-amber-400/50 bg-amber-500/15 text-amber-200',
};

const soundIconClasses: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

const statIconClasses: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};
</script>

<template>
  <div class="space-y-6">
    <!-- Page header -->
    <GlassPanel glow="cyan" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Kỹ thuật Pomodoro</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Tập trung</span>
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Học sâu theo từng phiên 25 phút, xen kẽ nghỉ ngắn để giữ năng lượng bền bỉ.
          </p>
        </div>
        <div class="flex shrink-0 items-center gap-3">
          <div class="flex items-center gap-2 rounded-xl border border-cyan-400/20 bg-cyan-500/10 px-4 py-2.5">
            <AppIcon name="target" class="h-5 w-5 text-cyan-300" />
            <div>
              <p class="text-sm font-bold leading-none text-cyan-200">{{ completedPomodoros }}/{{ POMODORO_GOAL }}</p>
              <p class="mt-0.5 text-[0.65rem] text-muted-foreground">Tới nghỉ dài</p>
            </div>
          </div>
        </div>
      </div>
    </GlassPanel>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- ───────────────── TIMER COLUMN ───────────────── -->
      <div class="lg:col-span-2">
        <GlassPanel :glow="currentMode.accent" class="p-5 sm:p-6">
          <!-- mode tabs -->
          <div class="mb-6 flex flex-wrap justify-center gap-2">
            <button
              v-for="mode in modes"
              :key="mode.id"
              type="button"
              class="rounded-xl border px-4 py-2 text-sm font-medium transition-all"
              :class="activeMode === mode.id
                ? modeTabActive[mode.accent]
                : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'"
              @click="selectMode(mode.id)"
            >
              {{ mode.label }}
            </button>
          </div>

          <!-- timer orb -->
          <div class="flex flex-col items-center">
            <div
              class="timer-orb relative flex items-center justify-center"
              :class="{ 'is-running': running, 'is-complete': justCompleted }"
              :style="{ width: RING_SIZE + 'px', height: RING_SIZE + 'px' }"
            >
              <svg :width="RING_SIZE" :height="RING_SIZE" class="-rotate-90">
                <circle
                  :cx="ringCenter"
                  :cy="ringCenter"
                  :r="ringRadius"
                  fill="none"
                  stroke="oklch(0.3 0.03 280 / 45%)"
                  :stroke-width="RING_STROKE"
                />
                <circle
                  :cx="ringCenter"
                  :cy="ringCenter"
                  :r="ringRadius"
                  fill="none"
                  :stroke="ringColor[currentMode.accent]"
                  :stroke-width="RING_STROKE"
                  stroke-linecap="round"
                  :stroke-dasharray="ringCircumference"
                  :stroke-dashoffset="ringOffset"
                  :style="{
                    filter: `drop-shadow(0 0 10px ${ringColor[currentMode.accent]})`,
                    transition: 'stroke-dashoffset 1s linear',
                  }"
                />
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <span class="text-5xl font-bold tabular-nums text-foreground sm:text-6xl">
                  {{ minutesDisplay }}:{{ secondsDisplay }}
                </span>
                <span class="mt-1 text-sm text-muted-foreground">{{ currentMode.label }}</span>
                <span
                  class="mt-2 inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium"
                  :class="running
                    ? 'bg-emerald-500/15 text-emerald-300 pulse-glow-green'
                    : 'bg-white/[0.06] text-muted-foreground'"
                >
                  <span class="h-1.5 w-1.5 rounded-full" :class="running ? 'bg-emerald-400' : 'bg-muted-foreground'" />
                  {{ running ? 'Đang chạy' : 'Tạm dừng' }}
                </span>
              </div>
            </div>

            <!-- controls -->
            <div class="mt-7 flex items-center gap-3">
              <button
                type="button"
                class="cosmic-btn flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-semibold"
                @click="toggleTimer"
              >
                <AppIcon :name="running ? 'pause' : 'play'" class="h-5 w-5" />
                {{ running ? 'Tạm dừng' : 'Bắt đầu' }}
              </button>
              <button
                type="button"
                aria-label="Đặt lại bộ đếm giờ"
                class="flex h-12 w-12 items-center justify-center rounded-xl border border-white/10 bg-white/[0.03] text-foreground transition-colors hover:border-cyan-400/40"
                @click="resetTimer"
              >
                <AppIcon name="refresh" class="h-5 w-5" />
              </button>
            </div>

            <!-- break suggestion -->
            <transition name="fade-up">
              <div
                v-if="showBreakHint"
                class="mt-5 flex items-center gap-3 rounded-xl border border-emerald-400/30 bg-emerald-500/[0.08] px-4 py-3 text-sm"
              >
                <AppIcon name="check-circle" class="h-5 w-5 shrink-0 text-emerald-300" />
                <span class="text-emerald-100">Hoàn thành 1 phiên! Hãy nghỉ ngắn để nạp lại năng lượng.</span>
                <button
                  type="button"
                  class="shrink-0 rounded-lg bg-emerald-500/20 px-3 py-1.5 text-xs font-semibold text-emerald-200 transition-colors hover:bg-emerald-500/30"
                  @click="goToBreak"
                >
                  Nghỉ ngay
                </button>
              </div>
            </transition>
          </div>

          <!-- pomodoro tracker dots -->
          <div class="mt-7 flex flex-col items-center gap-2">
            <p class="text-xs text-muted-foreground">Tiến độ tới phiên nghỉ dài</p>
            <div class="flex items-center gap-3">
              <span
                v-for="(filled, i) in pomodoroDots"
                :key="i"
                class="tomato flex h-8 w-8 items-center justify-center rounded-full border text-base transition-all"
                :class="filled
                  ? 'border-rose-400/40 bg-rose-500/20 tomato-filled'
                  : 'border-white/[0.08] bg-white/[0.02] opacity-40'"
              >
                🍅
              </span>
            </div>
          </div>
        </GlassPanel>
      </div>

      <!-- ───────────────── SIDE COLUMN ───────────────── -->
      <div class="space-y-6">
        <!-- task checklist -->
        <SectionPanel
          title="Việc cần làm phiên này"
          :subtitle="`${doneTasks}/${tasks.length} hoàn thành`"
          icon="check-circle"
          accent="green"
        >
          <form class="mb-3 flex gap-2" @submit.prevent="addTask">
            <input
              v-model="newTask"
              type="text"
              placeholder="Thêm việc cần làm..."
              class="cosmic-input flex-1 text-sm"
            />
            <button
              type="submit"
              aria-label="Thêm việc"
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-cyan-400/30 bg-cyan-500/15 text-cyan-300 transition-colors hover:bg-cyan-500/25"
            >
              <AppIcon name="plus" class="h-5 w-5" />
            </button>
          </form>

          <ul class="space-y-2">
            <li
              v-for="task in tasks"
              :key="task.id"
              class="group flex items-center gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] px-3 py-2.5 transition-colors hover:border-white/[0.12]"
            >
              <button
                type="button"
                :aria-label="task.done ? 'Bỏ đánh dấu hoàn thành' : 'Đánh dấu hoàn thành'"
                class="flex h-5 w-5 shrink-0 items-center justify-center rounded-md border transition-colors"
                :class="task.done
                  ? 'border-emerald-400/60 bg-emerald-500/30 text-emerald-200'
                  : 'border-white/20 bg-transparent text-transparent hover:border-cyan-400/50'"
                @click="toggleTask(task.id)"
              >
                <AppIcon name="check" class="h-3.5 w-3.5" />
              </button>
              <span
                class="flex-1 text-sm transition-colors"
                :class="task.done ? 'text-muted-foreground line-through' : 'text-foreground'"
              >
                {{ task.text }}
              </span>
              <button
                type="button"
                aria-label="Xoá việc"
                class="shrink-0 text-muted-foreground/50 opacity-0 transition-all hover:text-rose-300 group-hover:opacity-100"
                @click="removeTask(task.id)"
              >
                <AppIcon name="trash" class="h-4 w-4" />
              </button>
            </li>
            <li v-if="tasks.length === 0" class="py-4 text-center text-xs text-muted-foreground">
              Chưa có việc nào — thêm mục tiêu cho phiên này nhé!
            </li>
          </ul>
        </SectionPanel>

        <!-- ambient sounds -->
        <SectionPanel
          title="Âm thanh nền"
          :subtitle="`${activeSounds} âm thanh đang phát`"
          icon="play"
          accent="violet"
        >
          <ul class="space-y-2.5">
            <li
              v-for="sound in sounds"
              :key="sound.id"
              class="rounded-xl border px-3 py-2.5 transition-colors"
              :class="sound.playing
                ? 'border-white/[0.14] bg-white/[0.04]'
                : 'border-white/[0.06] bg-white/[0.02]'"
            >
              <div class="flex items-center gap-3">
                <button
                  type="button"
                  :aria-label="sound.playing ? `Tắt ${sound.name}` : `Bật ${sound.name}`"
                  class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl transition-transform hover:scale-105"
                  :class="sound.playing
                    ? soundIconClasses[sound.accent] + ' ' + (sound.accent === 'green' ? 'pulse-glow-green' : sound.accent === 'magenta' ? 'pulse-glow-magenta' : 'pulse-glow-cyan')
                    : 'bg-white/[0.04] text-muted-foreground'"
                  @click="toggleSound(sound.id)"
                >
                  <AppIcon :name="sound.playing ? 'pause' : sound.icon" class="h-4.5 w-4.5" />
                </button>
                <span class="flex-1 text-sm font-medium text-foreground">{{ sound.name }}</span>
                <span class="w-9 text-right text-xs tabular-nums text-muted-foreground">{{ sound.volume }}%</span>
              </div>
              <input
                v-model.number="sound.volume"
                type="range"
                min="0"
                max="100"
                :aria-label="`Âm lượng ${sound.name}`"
                class="vol-slider mt-2.5 w-full"
                :class="sound.playing ? '' : 'opacity-40'"
              />
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <!-- today stats -->
    <section class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <GlassPanel v-for="stat in focusStats" :key="stat.label" :glow="stat.accent" hover padded>
        <div class="flex items-center gap-4">
          <span class="flex h-12 w-12 items-center justify-center rounded-xl" :class="statIconClasses[stat.accent]">
            <AppIcon :name="stat.icon" class="h-6 w-6" />
          </span>
          <div>
            <p class="text-[0.7rem] font-medium uppercase tracking-wider text-muted-foreground">{{ stat.label }}</p>
            <p class="mt-0.5 flex items-baseline gap-1.5">
              <span class="text-2xl font-bold tabular-nums text-foreground">{{ stat.value }}</span>
              <span v-if="stat.unit" class="text-sm text-muted-foreground">{{ stat.unit }}</span>
            </p>
          </div>
        </div>
      </GlassPanel>
    </section>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.timer-orb {
  border-radius: 9999px;
}

.timer-orb.is-running::before {
  content: '';
  position: absolute;
  inset: 10px;
  border-radius: 9999px;
  background: radial-gradient(circle, oklch(0.78 0.18 195 / 10%) 0%, transparent 70%);
  animation: breathe 4s ease-in-out infinite;
  pointer-events: none;
}

.timer-orb.is-complete {
  animation: complete-pulse 0.8s ease-out;
}

@keyframes breathe {
  0%,
  100% {
    transform: scale(0.92);
    opacity: 0.45;
  }
  50% {
    transform: scale(1.04);
    opacity: 0.85;
  }
}

@keyframes complete-pulse {
  0% {
    box-shadow: 0 0 0 0 oklch(0.75 0.18 145 / 50%);
  }
  100% {
    box-shadow: 0 0 0 40px oklch(0.75 0.18 145 / 0%);
  }
}

.tomato-filled {
  animation: tomato-pop 0.4s ease;
}

@keyframes tomato-pop {
  0% {
    transform: scale(0.6);
  }
  60% {
    transform: scale(1.15);
  }
  100% {
    transform: scale(1);
  }
}

.vol-slider {
  -webkit-appearance: none;
  appearance: none;
  height: 4px;
  border-radius: 9999px;
  background: rgb(255 255 255 / 0.1);
  outline: none;
  cursor: pointer;
}

.vol-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  height: 14px;
  width: 14px;
  border-radius: 9999px;
  background: oklch(0.78 0.18 195);
  box-shadow: 0 0 10px oklch(0.78 0.18 195 / 60%);
  cursor: pointer;
}

.vol-slider::-moz-range-thumb {
  height: 14px;
  width: 14px;
  border: none;
  border-radius: 9999px;
  background: oklch(0.78 0.18 195);
  box-shadow: 0 0 10px oklch(0.78 0.18 195 / 60%);
  cursor: pointer;
}

.fade-up-enter-active {
  transition: all 0.3s ease;
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

@media (prefers-reduced-motion: reduce) {
  .timer-orb.is-running::before,
  .timer-orb.is-complete,
  .tomato-filled,
  .fade-up-enter-active {
    animation: none;
    transition: none;
  }
}
</style>
