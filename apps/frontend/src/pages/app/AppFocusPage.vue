<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';

type Phase = 'focus' | 'short-break' | 'long-break';
type SessionState = 'idle' | 'running' | 'paused' | 'completed';

interface SessionRecord {
  id: number;
  phase: Phase;
  durationSec: number;
  subject: string;
  startedAt: string;
  completed: boolean;
}

interface Preset {
  id: string;
  label: string;
  focus: number;
  shortBreak: number;
  longBreak: number;
  longBreakEvery: number;
}

const presets: Preset[] = [
  { id: 'classic', label: '25/5', focus: 25, shortBreak: 5, longBreak: 15, longBreakEvery: 4 },
  { id: 'deep', label: '50/10', focus: 50, shortBreak: 10, longBreak: 20, longBreakEvery: 3 },
  { id: 'sprint', label: '15/3', focus: 15, shortBreak: 3, longBreak: 10, longBreakEvery: 5 }
];

const activePreset = ref<Preset>(presets[0]);
const phase = ref<Phase>('focus');
const state = ref<SessionState>('idle');
const elapsedSec = ref(0);
const completedFocusCount = ref(0);
const totalFocusToday = ref(0);
const currentSubject = ref('Toán học');
const distractionCount = ref(0);
const subjects = ['Toán học', 'Ngoại ngữ', 'Lập trình', 'Khoa học', 'Lịch sử', 'Khác'];

const lessonQueue = [
  { id: 'L01', title: 'Khái niệm hàm số', status: 'done' },
  { id: 'L02', title: 'Đồ thị hàm bậc nhất', status: 'active' },
  { id: 'L03', title: 'Hệ số góc', status: 'pending' },
  { id: 'Q01', title: 'Quiz kiểm tra nhanh', status: 'pending' },
  { id: 'R01', title: 'Ghi chú ôn tập', status: 'pending' }
];

const phaseDurations = computed(() => ({
  focus: activePreset.value.focus * 60,
  'short-break': activePreset.value.shortBreak * 60,
  'long-break': activePreset.value.longBreak * 60
}));

const currentDuration = computed(() => phaseDurations.value[phase.value]);
const remainingSec = computed(() => Math.max(0, currentDuration.value - elapsedSec.value));
const progressPct = computed(() => Math.min(100, (elapsedSec.value / currentDuration.value) * 100));
const phaseLabel = computed(() => phase.value === 'focus' ? 'Tập trung' : phase.value === 'short-break' ? 'Nghỉ ngắn' : 'Nghỉ dài');

const formattedTime = computed(() => {
  const m = Math.floor(remainingSec.value / 60);
  const s = remainingSec.value % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

const formattedTotal = computed(() => {
  const h = Math.floor(totalFocusToday.value / 3600);
  const m = Math.floor((totalFocusToday.value % 3600) / 60);
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
});

const nextPhase = computed<Phase>(() => {
  if (phase.value === 'focus') {
    return (completedFocusCount.value + 1) % activePreset.value.longBreakEvery === 0 ? 'long-break' : 'short-break';
  }
  return 'focus';
});

const nextPhaseLabel = computed(() => nextPhase.value === 'focus' ? 'Tập trung' : nextPhase.value === 'short-break' ? 'Nghỉ ngắn' : 'Nghỉ dài');

const sessionHistory = ref<SessionRecord[]>([
  { id: 1, phase: 'focus', durationSec: 1500, subject: 'Toán học', startedAt: '08:30', completed: true },
  { id: 2, phase: 'short-break', durationSec: 300, subject: '', startedAt: '08:55', completed: true },
  { id: 3, phase: 'focus', durationSec: 1500, subject: 'Toán học', startedAt: '09:00', completed: true },
  { id: 4, phase: 'short-break', durationSec: 300, subject: '', startedAt: '09:25', completed: true },
  { id: 5, phase: 'focus', durationSec: 1500, subject: 'Ngoại ngữ', startedAt: '09:30', completed: true },
  { id: 6, phase: 'short-break', durationSec: 300, subject: '', startedAt: '09:55', completed: true },
  { id: 7, phase: 'focus', durationSec: 1500, subject: 'Ngoại ngữ', startedAt: '10:00', completed: false }
]);

const completedToday = computed(() => sessionHistory.value.filter((s) => s.completed && s.phase === 'focus').length);
const totalSessionsToday = computed(() => sessionHistory.value.filter((s) => s.phase === 'focus').length);
const completionRate = computed(() => totalSessionsToday.value === 0 ? 0 : Math.round((completedToday.value / totalSessionsToday.value) * 100));

const weeklyData = ref([
  { day: 'T2', sessions: 5, minutes: 125 },
  { day: 'T3', sessions: 7, minutes: 175 },
  { day: 'T4', sessions: 3, minutes: 75 },
  { day: 'T5', sessions: 8, minutes: 200 },
  { day: 'T6', sessions: 6, minutes: 150 },
  { day: 'T7', sessions: 4, minutes: 100, isToday: true },
  { day: 'CN', sessions: 0, minutes: 0 }
]);
const weeklyMax = computed(() => Math.max(...weeklyData.value.map((d) => d.minutes), 1));
const weeklyTotal = computed(() => weeklyData.value.reduce((sum, day) => sum + day.minutes, 0));
const weeklySessions = computed(() => weeklyData.value.reduce((sum, day) => sum + day.sessions, 0));

let tickTimer: number | null = null;

function startTimer(): void {
  if (tickTimer) return;
  if (state.value === 'completed') resetTimer();
  state.value = 'running';
  tickTimer = window.setInterval(() => {
    elapsedSec.value += 1;
    if (elapsedSec.value >= currentDuration.value) completePhase();
  }, 1000);
}

function pauseTimer(): void {
  state.value = 'paused';
  if (tickTimer) {
    window.clearInterval(tickTimer);
    tickTimer = null;
  }
}

function resumeTimer(): void {
  startTimer();
}

function resetTimer(): void {
  state.value = 'idle';
  elapsedSec.value = 0;
  if (tickTimer) {
    window.clearInterval(tickTimer);
    tickTimer = null;
  }
}

function skipPhase(): void {
  completePhase();
}

function registerDistraction(): void {
  distractionCount.value++;
}

function completePhase(): void {
  if (tickTimer) {
    window.clearInterval(tickTimer);
    tickTimer = null;
  }
  state.value = 'completed';
  const record: SessionRecord = {
    id: sessionHistory.value.length + 1,
    phase: phase.value,
    durationSec: elapsedSec.value,
    subject: phase.value === 'focus' ? currentSubject.value : '',
    startedAt: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
    completed: elapsedSec.value >= currentDuration.value
  };
  sessionHistory.value.unshift(record);
  if (phase.value === 'focus' && record.completed) {
    completedFocusCount.value++;
    totalFocusToday.value += elapsedSec.value;
  }
  window.setTimeout(() => {
    phase.value = nextPhase.value;
    elapsedSec.value = 0;
    state.value = 'idle';
  }, 800);
}

function selectPreset(preset: Preset): void {
  activePreset.value = preset;
  resetTimer();
}

onUnmounted(() => {
  if (tickTimer) window.clearInterval(tickTimer);
});
</script>

<template>
  <div class="min-h-[calc(100vh-9rem)] border border-border bg-zinc-950 text-zinc-100">
    <header class="grid gap-3 border-b border-border px-4 py-3 lg:grid-cols-[1fr_auto] lg:items-center">
      <div>
        <p class="text-xs font-semibold uppercase text-zinc-500">Study workspace</p>
        <h1 class="mt-1 text-xl font-semibold">Computer-based learning session</h1>
      </div>
      <div class="grid grid-cols-3 border border-zinc-800 text-center text-xs">
        <div class="border-r border-zinc-800 px-3 py-2">
          <p class="font-mono text-zinc-100">{{ completedToday }}/{{ totalSessionsToday }}</p>
          <p class="text-zinc-500">Phiên</p>
        </div>
        <div class="border-r border-zinc-800 px-3 py-2">
          <p class="font-mono text-zinc-100">{{ formattedTotal }}</p>
          <p class="text-zinc-500">Tổng</p>
        </div>
        <div class="px-3 py-2">
          <p class="font-mono text-zinc-100">{{ distractionCount }}</p>
          <p class="text-zinc-500">Phân tâm</p>
        </div>
      </div>
    </header>

    <div class="grid min-h-[calc(100vh-15rem)] lg:grid-cols-[260px_1fr_320px]">
      <aside class="border-b border-border bg-zinc-950 lg:border-b-0 lg:border-r">
        <div class="border-b border-border px-4 py-3">
          <h2 class="text-xs font-semibold uppercase text-zinc-500">Lesson navigator</h2>
        </div>
        <ol class="divide-y divide-border">
          <li
            v-for="item in lessonQueue"
            :key="item.id"
            class="grid grid-cols-[48px_1fr] gap-3 px-4 py-3 text-sm"
            :class="item.status === 'active' ? 'bg-zinc-900 text-primary' : item.status === 'done' ? 'text-zinc-300' : 'text-zinc-500'"
          >
            <span class="font-mono text-xs">{{ item.id }}</span>
            <span class="font-medium">{{ item.title }}</span>
          </li>
        </ol>
      </aside>

      <main class="flex min-h-[36rem] flex-col">
        <div class="grid grid-cols-2 border-b border-border text-sm md:grid-cols-4">
          <div class="border-b border-border px-4 py-3 md:border-b-0 md:border-r">
            <p class="text-xs uppercase text-zinc-500">Môn học</p>
            <select v-model="currentSubject" class="mt-1 w-full border border-zinc-800 bg-zinc-950 px-2 py-1 text-sm text-zinc-100 focus:border-primary focus:outline-none" :disabled="state === 'running'">
              <option v-for="subject in subjects" :key="subject" :value="subject" class="bg-zinc-950">{{ subject }}</option>
            </select>
          </div>
          <div class="border-b border-border px-4 py-3 md:border-b-0 md:border-r">
            <p class="text-xs uppercase text-zinc-500">Chế độ</p>
            <p class="mt-2 font-semibold text-zinc-100">{{ phaseLabel }}</p>
          </div>
          <div class="border-r border-border px-4 py-3">
            <p class="text-xs uppercase text-zinc-500">Tiếp theo</p>
            <p class="mt-2 font-semibold text-zinc-100">{{ nextPhaseLabel }}</p>
          </div>
          <div class="px-4 py-3">
            <p class="text-xs uppercase text-zinc-500">Trạng thái</p>
            <p class="mt-2 font-semibold text-zinc-100">{{ state }}</p>
          </div>
        </div>

        <section class="flex flex-1 flex-col justify-between p-4">
          <div class="border border-zinc-800 bg-zinc-950">
            <div class="border-b border-zinc-800 px-4 py-3">
              <p class="text-xs font-semibold uppercase text-zinc-500">Active task</p>
              <h2 class="mt-1 text-lg font-semibold text-zinc-100">Đồ thị hàm bậc nhất</h2>
            </div>
            <div class="grid gap-4 p-4 xl:grid-cols-[1fr_280px]">
              <div class="space-y-4 text-sm leading-7 text-zinc-300">
                <p>Hoàn thành phiên tập trung này trước khi chuyển sang quiz. Ghi chú chính cần kiểm tra: hệ số góc, giao điểm trục tung và cách đọc xu hướng tăng/giảm trên đồ thị.</p>
                <div class="border border-zinc-800 bg-zinc-900/50 p-3">
                  <p class="font-mono text-xs uppercase text-zinc-500">Prompt</p>
                  <p class="mt-2">Tóm tắt bằng 3 ý: cách xác định hàm số bậc nhất từ hai điểm, ý nghĩa hệ số góc và lỗi thường gặp khi vẽ đồ thị.</p>
                </div>
              </div>

              <div class="border border-zinc-800 bg-zinc-950 p-4 text-center">
                <p class="text-xs font-semibold uppercase text-zinc-500">Remaining time</p>
                <p class="mt-4 font-mono text-6xl font-semibold tabular-nums text-zinc-100">{{ formattedTime }}</p>
                <p class="mt-2 text-xs text-zinc-500">Phiên {{ completedFocusCount + 1 }} - {{ Math.floor(progressPct) }}%</p>
              </div>
            </div>
          </div>

          <div class="mt-4">
            <div class="mb-2 flex justify-between text-xs text-zinc-500">
              <span>Session progress</span>
              <span class="font-mono">{{ Math.floor(progressPct) }}%</span>
            </div>
            <div class="h-2 border border-zinc-800 bg-zinc-950">
              <div class="h-full bg-primary" :style="{ width: `${progressPct}%` }" />
            </div>
          </div>
        </section>
      </main>

      <aside class="border-t border-border bg-zinc-950 lg:border-l lg:border-t-0">
        <section class="border-b border-border p-4">
          <h2 class="text-xs font-semibold uppercase text-zinc-500">Controls</h2>
          <div class="mt-3 grid gap-2">
            <button v-if="state === 'idle'" type="button" class="inline-flex items-center justify-center gap-2 border border-primary bg-primary px-3 py-2 text-sm font-semibold text-primary-foreground" @click="startTimer">
              <AppIcon name="play" class="h-4 w-4" />
              Bắt đầu
            </button>
            <button v-if="state === 'running'" type="button" class="inline-flex items-center justify-center gap-2 border border-zinc-700 px-3 py-2 text-sm font-semibold text-zinc-100 hover:border-primary hover:text-primary" @click="pauseTimer">
              <AppIcon name="pause" class="h-4 w-4" />
              Tạm dừng
            </button>
            <button v-if="state === 'paused'" type="button" class="inline-flex items-center justify-center gap-2 border border-primary bg-primary px-3 py-2 text-sm font-semibold text-primary-foreground" @click="resumeTimer">
              <AppIcon name="play" class="h-4 w-4" />
              Tiếp tục
            </button>
            <button type="button" class="inline-flex items-center justify-center gap-2 border border-zinc-700 px-3 py-2 text-sm font-semibold text-zinc-300 hover:border-zinc-500" @click="resetTimer">
              <AppIcon name="refresh" class="h-4 w-4" />
              Đặt lại
            </button>
            <button type="button" class="inline-flex items-center justify-center gap-2 border border-zinc-700 px-3 py-2 text-sm font-semibold text-zinc-300 hover:border-zinc-500" @click="skipPhase">
              <AppIcon name="arrow-right" class="h-4 w-4" />
              Bỏ qua
            </button>
            <button type="button" class="inline-flex items-center justify-center gap-2 border border-zinc-700 px-3 py-2 text-sm font-semibold text-zinc-300 hover:border-zinc-500" @click="registerDistraction">
              <AppIcon name="alert" class="h-4 w-4" />
              Ghi nhận phân tâm
            </button>
          </div>
        </section>

        <section class="border-b border-border p-4">
          <h2 class="text-xs font-semibold uppercase text-zinc-500">Preset</h2>
          <div class="mt-3 grid grid-cols-3 border border-zinc-800">
            <button
              v-for="preset in presets"
              :key="preset.id"
              type="button"
              class="border-r border-zinc-800 px-2 py-2 text-sm last:border-r-0"
              :class="activePreset.id === preset.id ? 'bg-zinc-900 text-primary' : 'text-zinc-400 hover:bg-zinc-900'"
              @click="selectPreset(preset)"
            >
              {{ preset.label }}
            </button>
          </div>
        </section>

        <section class="border-b border-border p-4">
          <h2 class="text-xs font-semibold uppercase text-zinc-500">Weekly load</h2>
          <p class="mt-1 text-sm text-zinc-300">{{ weeklySessions }} phiên - {{ weeklyTotal }} phút</p>
          <div class="mt-4 flex h-28 items-end gap-1">
            <div v-for="day in weeklyData" :key="day.day" class="flex flex-1 flex-col items-center gap-1">
              <div class="flex h-20 w-full items-end border border-zinc-800">
                <div class="w-full bg-zinc-700" :class="day.isToday ? 'bg-primary' : ''" :style="{ height: `${Math.max(4, (day.minutes / weeklyMax) * 100)}%` }" />
              </div>
              <span class="text-[0.65rem] text-zinc-500">{{ day.day }}</span>
            </div>
          </div>
        </section>

        <section class="p-4">
          <div class="mb-3 flex items-center justify-between">
            <h2 class="text-xs font-semibold uppercase text-zinc-500">Session log</h2>
            <span class="font-mono text-xs text-zinc-500">{{ completionRate }}%</span>
          </div>
          <ul class="max-h-72 divide-y divide-border overflow-y-auto border border-zinc-800">
            <li v-for="session in sessionHistory" :key="session.id" class="grid grid-cols-[58px_1fr_52px] gap-2 px-3 py-2 text-xs">
              <span class="font-mono text-zinc-500">{{ session.startedAt }}</span>
              <span :class="session.completed ? 'text-zinc-300' : 'text-zinc-600'">
                {{ session.phase === 'focus' ? 'Tập trung' : session.phase === 'short-break' ? 'Nghỉ ngắn' : 'Nghỉ dài' }}{{ session.subject ? ` - ${session.subject}` : '' }}
              </span>
              <span class="text-right font-mono text-zinc-500">{{ Math.floor(session.durationSec / 60) }}m</span>
            </li>
          </ul>
        </section>
      </aside>
    </div>
  </div>
</template>
