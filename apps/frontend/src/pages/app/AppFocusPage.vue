<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Phase = 'focus' | 'short-break' | 'long-break';
type SessionState = 'idle' | 'running' | 'paused' | 'completed';

interface SessionRecord { id: number; phase: Phase; durationSec: number; subject: string; startedAt: string; completed: boolean; }
interface Preset { id: string; label: string; focus: number; shortBreak: number; longBreak: number; longBreakEvery: number; }

const presets: Preset[] = [
  { id: 'classic', label: 'Cổ điển 25/5', focus: 25, shortBreak: 5, longBreak: 15, longBreakEvery: 4 },
  { id: 'deep', label: 'Deep work 50/10', focus: 50, shortBreak: 10, longBreak: 20, longBreakEvery: 3 },
  { id: 'sprint', label: 'Sprint 15/3', focus: 15, shortBreak: 3, longBreak: 10, longBreakEvery: 5 },
];

const activePreset = ref<Preset>(presets[0]);
const phase = ref<Phase>('focus');
const state = ref<SessionState>('idle');
const elapsedSec = ref(0);
const completedFocusCount = ref(0);
const totalFocusToday = ref(0);
const currentSubject = ref('Toán học');
const subjects = ['Toán học', 'Ngoại ngữ', 'Lập trình', 'Khoa học', 'Lịch sử', 'Khác'];
const distractionCount = ref(0);

const phaseDurations = computed(() => ({
  focus: activePreset.value.focus * 60,
  'short-break': activePreset.value.shortBreak * 60,
  'long-break': activePreset.value.longBreak * 60,
}));

const currentDuration = computed(() => phaseDurations.value[phase.value]);
const remainingSec = computed(() => Math.max(0, currentDuration.value - elapsedSec.value));
const progressPct = computed(() => Math.min(100, (elapsedSec.value / currentDuration.value) * 100));

const phaseLabel = computed(() => phase.value === 'focus' ? 'Tập trung' : phase.value === 'short-break' ? 'Nghỉ ngắn' : 'Nghỉ dài');
const phaseColor = computed(() => phase.value === 'focus' ? '#22D3EE' : phase.value === 'short-break' ? '#10B981' : '#7C3AED');

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
    const nextIsLong = (completedFocusCount.value + 1) % activePreset.value.longBreakEvery === 0;
    return nextIsLong ? 'long-break' : 'short-break';
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
  { id: 7, phase: 'focus', durationSec: 1500, subject: 'Ngoại ngữ', startedAt: '10:00', completed: false },
]);

const completedToday = computed(() => sessionHistory.value.filter(s => s.completed && s.phase === 'focus').length);
const totalSessionsToday = computed(() => sessionHistory.value.filter(s => s.phase === 'focus').length);
const completionRate = computed(() => totalSessionsToday.value === 0 ? 0 : Math.round((completedToday.value / totalSessionsToday.value) * 100));

const weeklyData = ref([
  { day: 'T2', sessions: 5, minutes: 125 },
  { day: 'T3', sessions: 7, minutes: 175 },
  { day: 'T4', sessions: 3, minutes: 75 },
  { day: 'T5', sessions: 8, minutes: 200 },
  { day: 'T6', sessions: 6, minutes: 150 },
  { day: 'T7', sessions: 4, minutes: 100, isToday: true },
  { day: 'CN', sessions: 0, minutes: 0 },
]);
const weeklyMax = computed(() => Math.max(...weeklyData.value.map(d => d.minutes), 1));
const weeklyTotal = computed(() => weeklyData.value.reduce((s, d) => s + d.minutes, 0));
const weeklySessions = computed(() => weeklyData.value.reduce((s, d) => s + d.sessions, 0));

let tickTimer: number | null = null;

function startTimer() {
  if (state.value === 'completed') resetTimer();
  state.value = 'running';
  tickTimer = window.setInterval(() => {
    elapsedSec.value += 1;
    if (elapsedSec.value >= currentDuration.value) completePhase();
  }, 1000);
}
function pauseTimer() {
  state.value = 'paused';
  if (tickTimer) { window.clearInterval(tickTimer); tickTimer = null; }
}
function resumeTimer() { startTimer(); }
function resetTimer() {
  state.value = 'idle';
  elapsedSec.value = 0;
  if (tickTimer) { window.clearInterval(tickTimer); tickTimer = null; }
}
function skipPhase() { completePhase(); }
function registerDistraction() { distractionCount.value++; }

function completePhase() {
  if (tickTimer) { window.clearInterval(tickTimer); tickTimer = null; }
  state.value = 'completed';
  const record: SessionRecord = {
    id: sessionHistory.value.length + 1,
    phase: phase.value,
    durationSec: elapsedSec.value,
    subject: phase.value === 'focus' ? currentSubject.value : '',
    startedAt: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }),
    completed: elapsedSec.value >= currentDuration.value,
  };
  sessionHistory.value.unshift(record);
  if (phase.value === 'focus' && record.completed) {
    completedFocusCount.value++;
    totalFocusToday.value += elapsedSec.value;
  }
  setTimeout(() => {
    phase.value = nextPhase.value;
    elapsedSec.value = 0;
    state.value = 'idle';
  }, 800);
}

function selectPreset(p: Preset) {
  activePreset.value = p;
  resetTimer();
}

onMounted(() => {});
onUnmounted(() => { if (tickTimer) window.clearInterval(tickTimer); });

const circumference = 2 * Math.PI * 130;
const strokeDashoffset = computed(() => circumference - (progressPct.value / 100) * circumference);
</script>

<template>
  <div class="space-y-6">
    <GlassPanel :glow="phase === 'focus' ? 'cyan' : phase === 'short-break' ? 'green' : 'violet'" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Pomodoro Deep Work</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">Tập trung</h1>
          <p class="mt-1 text-sm text-muted-foreground">Hẹn giờ học sâu, theo dõi phiên và xây dựng chuỗi tập trung.</p>
        </div>
        <div class="flex items-center gap-3">
          <div class="rounded-xl border border-cyan-400/20 bg-cyan-500/10 px-4 py-2.5 text-center"><p class="text-lg font-bold leading-none text-cyan-200 tabular-nums">{{ completedToday }}/{{ totalSessionsToday }}</p><p class="mt-0.5 text-[0.65rem] text-muted-foreground">Phiên hôm nay</p></div>
          <div class="rounded-xl border border-violet-400/20 bg-violet-500/10 px-4 py-2.5 text-center"><p class="text-lg font-bold leading-none text-violet-200 tabular-nums">{{ formattedTotal }}</p><p class="mt-0.5 text-[0.65rem] text-muted-foreground">Tổng thời gian</p></div>
        </div>
      </div>
    </GlassPanel>

    <!-- Preset selector -->
    <div class="flex flex-wrap gap-2">
      <button v-for="p in presets" :key="p.id" type="button" class="rounded-xl border px-4 py-2 text-sm font-medium transition-all" :class="activePreset.id === p.id ? 'border-cyan-400/40 bg-cyan-500/10 text-cyan-300' : 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-white/15 hover:text-foreground'" @click="selectPreset(p)">{{ p.label }}</button>
    </div>

    <!-- Timer -->
    <GlassPanel :glow="phase === 'focus' ? 'cyan' : phase === 'short-break' ? 'green' : 'violet'" class="p-6 sm:p-8">
      <div class="flex flex-col items-center gap-6">
        <div class="flex items-center gap-2">
          <span class="h-2 w-2 rounded-full transition-colors" :style="{ backgroundColor: phaseColor, boxShadow: `0 0 8px ${phaseColor}` }" />
          <span class="text-sm font-semibold uppercase tracking-wider" :style="{ color: phaseColor }">{{ phaseLabel }}</span>
          <span class="text-xs text-muted-foreground">→ Tiếp: {{ nextPhaseLabel }}</span>
        </div>

        <div class="relative">
          <svg width="280" height="280" viewBox="0 0 280 280" class="-rotate-90">
            <circle cx="140" cy="140" r="130" fill="none" stroke="rgba(255,255,255,0.04)" stroke-width="8" />
            <circle cx="140" cy="140" r="130" fill="none" :stroke="phaseColor" stroke-width="8" stroke-linecap="round" :stroke-dasharray="circumference" :stroke-dashoffset="strokeDashoffset" :style="{ filter: `drop-shadow(0 0 6px ${phaseColor}80)`, transition: 'stroke-dashoffset 1s linear, stroke 0.3s ease' }" />
          </svg>
          <div class="absolute inset-0 flex flex-col items-center justify-center">
            <span class="font-mono text-5xl font-bold tabular-nums text-foreground">{{ formattedTime }}</span>
            <span class="mt-1 text-xs text-muted-foreground">{{ Math.floor(progressPct) }}% · phiên {{ completedFocusCount + 1 }}</span>
          </div>
        </div>

        <!-- Controls -->
        <div class="flex items-center gap-3">
          <button v-if="state === 'idle'" type="button" class="cosmic-btn flex items-center gap-2 rounded-xl px-6 py-3 text-sm" @click="startTimer"><AppIcon name="play" class="h-5 w-5" />Bắt đầu</button>
          <button v-if="state === 'running'" type="button" class="flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-6 py-3 text-sm font-medium text-foreground transition-colors hover:border-white/20" @click="pauseTimer"><AppIcon name="pause" class="h-5 w-5" />Tạm dừng</button>
          <button v-if="state === 'paused'" type="button" class="cosmic-btn flex items-center gap-2 rounded-xl px-6 py-3 text-sm" @click="resumeTimer"><AppIcon name="play" class="h-5 w-5" />Tiếp tục</button>
          <button type="button" class="flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3 text-sm font-medium text-muted-foreground transition-colors hover:border-white/20 hover:text-foreground" @click="resetTimer"><AppIcon name="refresh" class="h-4 w-4" />Đặt lại</button>
          <button type="button" class="flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-3 text-sm font-medium text-muted-foreground transition-colors hover:border-white/20 hover:text-foreground" @click="skipPhase"><AppIcon name="skip" class="h-4 w-4" />Bỏ qua</button>
        </div>

        <!-- Subject tag + distraction -->
        <div class="flex flex-wrap items-center justify-center gap-2">
          <span class="text-xs text-muted-foreground">Đang học:</span>
          <select v-model="currentSubject" class="rounded-lg border border-white/[0.08] bg-white/[0.03] px-2.5 py-1 text-xs font-medium text-foreground focus:border-cyan-400/40 focus:outline-none" :disabled="state === 'running'">
            <option v-for="s in subjects" :key="s" :value="s" class="bg-slate-900">{{ s }}</option>
          </select>
          <span class="mx-1 text-muted-foreground/30">|</span>
          <button type="button" class="inline-flex items-center gap-1 rounded-lg border border-rose-400/20 bg-rose-500/10 px-2.5 py-1 text-xs font-medium text-rose-300 transition-colors hover:bg-rose-500/20" @click="registerDistraction"><AppIcon name="alert" class="h-3 w-3" />Phân tâm ({{ distractionCount }})</button>
        </div>
      </div>
    </GlassPanel>

    <!-- Weekly chart + stats -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <div class="lg:col-span-2">
        <SectionPanel title="Tuần này" :subtitle="`${weeklySessions} phiên · ${weeklyTotal} phút`" icon="chart" accent="cyan">
          <div class="flex h-40 items-end gap-2">
            <div v-for="d in weeklyData" :key="d.day" class="flex flex-1 flex-col items-center gap-2">
              <span class="text-xs font-semibold tabular-nums" :class="d.minutes > 0 ? 'text-foreground' : 'text-muted-foreground/40'">{{ d.minutes }}</span>
              <div class="flex w-full flex-1 items-end justify-center">
                <div class="w-full max-w-[2.5rem] rounded-t-md transition-all duration-500" :class="d.isToday ? 'bg-gradient-to-t from-cyan-500 to-cyan-300' : 'bg-white/[0.08]'" :style="{ height: `${Math.max(4, (d.minutes / weeklyMax) * 100)}%` }" :title="`${d.day}: ${d.sessions} phiên, ${d.minutes} phút`" />
              </div>
              <span class="text-[0.7rem]" :class="d.isToday ? 'font-bold text-cyan-300' : 'text-muted-foreground'">{{ d.day }}</span>
            </div>
          </div>
        </SectionPanel>
      </div>
      <div class="space-y-4">
        <div class="rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4">
          <div class="flex items-center justify-between"><span class="text-xs text-muted-foreground">Tỉ lệ hoàn thành</span><span class="text-lg font-bold tabular-nums text-emerald-300">{{ completionRate }}%</span></div>
          <ProgressBar :value="completionRate" accent="green" :height="6" />
        </div>
        <div class="rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4">
          <div class="flex items-center justify-between"><span class="text-xs text-muted-foreground">Phiên tập trung</span><span class="text-lg font-bold tabular-nums text-cyan-300">{{ completedToday }}</span></div>
          <p class="mt-1 text-[0.7rem] text-muted-foreground">Mục tiêu: 8 phiên/ngày</p>
        </div>
        <div class="rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4">
          <div class="flex items-center justify-between"><span class="text-xs text-muted-foreground">Lần phân tâm</span><span class="text-lg font-bold tabular-nums text-rose-300">{{ distractionCount }}</span></div>
          <p class="mt-1 text-[0.7rem] text-muted-foreground">Cố gắng giữ dưới 3 lần/phên</p>
        </div>
      </div>
    </div>

    <!-- Session history -->
    <SectionPanel title="Lịch sử phiên" subtitle="Hôm nay" icon="clock" accent="violet">
      <div v-if="sessionHistory.length === 0" class="py-8 text-center text-sm text-muted-foreground">Chưa có phiên nào. Bắt đầu phiên đầu tiên!</div>
      <div v-else class="space-y-2">
        <div v-for="s in sessionHistory" :key="s.id" class="flex items-center gap-3 rounded-xl border px-3 py-2.5 transition-colors" :class="s.completed ? 'border-white/[0.04] bg-white/[0.01]' : 'border-rose-400/15 bg-rose-500/[0.04]'">
          <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg" :class="s.phase === 'focus' ? 'bg-cyan-500/10 text-cyan-300' : s.phase === 'short-break' ? 'bg-emerald-500/10 text-emerald-300' : 'bg-violet-500/10 text-violet-300'">
            <AppIcon :name="s.phase === 'focus' ? 'target' : 'clock'" class="h-4 w-4" />
          </span>
          <div class="min-w-0 flex-1">
            <p class="text-sm font-medium text-foreground">{{ s.phase === 'focus' ? 'Tập trung' : s.phase === 'short-break' ? 'Nghỉ ngắn' : 'Nghỉ dài' }}{{ s.subject ? ' · ' + s.subject : '' }}</p>
            <p class="text-xs text-muted-foreground">{{ s.startedAt }} · {{ Math.floor(s.durationSec / 60) }} phút</p>
          </div>
          <span v-if="s.completed" class="inline-flex items-center gap-1 rounded-md bg-emerald-500/10 px-2 py-1 text-xs font-semibold text-emerald-300"><AppIcon name="check-circle" class="h-3 w-3" />Hoàn thành</span>
          <span v-else class="inline-flex items-center gap-1 rounded-md bg-rose-500/10 px-2 py-1 text-xs font-semibold text-rose-300"><AppIcon name="x" class="h-3 w-3" />Dở dang</span>
        </div>
      </div>
    </SectionPanel>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.</p>
  </div>
</template>
