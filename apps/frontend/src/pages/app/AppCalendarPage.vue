<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type SessionType = 'doc' | 'quiz' | 'ontap' | 'nhom';

interface StudySession {
  id: string;
  subject: string;
  time: string;
  duration: number;
  type: SessionType;
}

interface DayCell {
  key: string;
  date: number;
  inMonth: boolean;
  isToday: boolean;
  sessions: StudySession[];
  intensity: number;
}

interface KpiTile {
  label: string;
  value: string;
  unit?: string;
  icon: string;
  accent: Accent;
  trend?: number;
  caption?: string;
  sparkline?: number[];
}

const weekdayLabels = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'] as const;

const sessionTypeMeta: Record<SessionType, { label: string; icon: string; accent: Accent; dot: string; chip: string }> = {
  doc: { label: 'Đọc tài liệu', icon: 'book-open', accent: 'cyan', dot: 'bg-cyan-400', chip: 'bg-cyan-500/15 text-cyan-300' },
  quiz: { label: 'Làm quiz', icon: 'target', accent: 'magenta', dot: 'bg-fuchsia-400', chip: 'bg-fuchsia-500/15 text-fuchsia-300' },
  ontap: { label: 'Ôn tập', icon: 'flame', accent: 'amber', dot: 'bg-amber-400', chip: 'bg-amber-500/15 text-amber-300' },
  nhom: { label: 'Học nhóm', icon: 'users', accent: 'violet', dot: 'bg-violet-400', chip: 'bg-violet-500/15 text-violet-300' }
};

const subjectOptions = ['Toán học', 'Ngoại ngữ', 'Lập trình', 'Khoa học', 'Lịch sử'] as const;

const now = new Date();
const todayKey = isoKey(now);

function isoKey(d: Date): string {
  const y = d.getFullYear();
  const m = `${d.getMonth() + 1}`.padStart(2, '0');
  const day = `${d.getDate()}`.padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function keyOf(year: number, month: number, day: number): string {
  return `${year}-${`${month + 1}`.padStart(2, '0')}-${`${day}`.padStart(2, '0')}`;
}

let sid = 0;
function makeSession(subject: string, time: string, duration: number, type: SessionType): StudySession {
  sid += 1;
  return { id: `s${sid}`, subject, time, duration, type };
}

// Mock sessions keyed by ISO date, seeded around the current month.
function buildSeed(): Record<string, StudySession[]> {
  const y = now.getFullYear();
  const m = now.getMonth();
  const map: Record<string, StudySession[]> = {};
  const seedDays: Array<[number, StudySession[]]> = [
    [2, [makeSession('Toán học', '08:00', 45, 'doc'), makeSession('Ngoại ngữ', '20:00', 30, 'quiz')]],
    [4, [makeSession('Lập trình', '14:30', 60, 'doc')]],
    [5, [makeSession('Khoa học', '09:00', 25, 'ontap'), makeSession('Ngoại ngữ', '19:30', 40, 'doc'), makeSession('Toán học', '21:00', 35, 'quiz')]],
    [8, [makeSession('Lịch sử', '15:00', 50, 'doc')]],
    [9, [makeSession('Toán học', '07:30', 45, 'ontap'), makeSession('Lập trình', '22:00', 70, 'doc')]],
    [12, [makeSession('Ngoại ngữ', '18:00', 30, 'nhom')]],
    [14, [makeSession('Khoa học', '10:00', 40, 'doc'), makeSession('Toán học', '20:30', 55, 'quiz')]],
    [15, [makeSession('Lập trình', '13:00', 90, 'doc'), makeSession('Lịch sử', '21:30', 30, 'ontap')]],
    [18, [makeSession('Ngoại ngữ', '08:30', 35, 'doc')]],
    [20, [makeSession('Toán học', '16:00', 60, 'nhom'), makeSession('Khoa học', '20:00', 45, 'doc'), makeSession('Lập trình', '22:30', 40, 'quiz')]],
    [22, [makeSession('Lịch sử', '11:00', 50, 'doc')]],
    [24, [makeSession('Ngoại ngữ', '19:00', 30, 'ontap'), makeSession('Toán học', '21:00', 45, 'doc')]],
    [26, [makeSession('Lập trình', '14:00', 75, 'doc')]],
    [28, [makeSession('Khoa học', '09:30', 40, 'quiz'), makeSession('Ngoại ngữ', '20:30', 35, 'doc')]]
  ];
  for (const [day, list] of seedDays) {
    map[keyOf(y, m, day)] = list;
  }
  // a couple of sessions today regardless of seed
  map[todayKey] = [
    makeSession('Toán học', '09:00', 45, 'doc'),
    makeSession('Ngoại ngữ', '19:30', 30, 'quiz'),
    makeSession('Lập trình', '21:00', 60, 'doc')
  ];
  return map;
}

const sessions = reactive<Record<string, StudySession[]>>(buildSeed());

const viewYear = ref<number>(now.getFullYear());
const viewMonth = ref<number>(now.getMonth());
const selectedKey = ref<string>(todayKey);
const slideDir = ref<'next' | 'prev'>('next');

const monthLabel = computed<string>(() => `Tháng ${viewMonth.value + 1}, ${viewYear.value}`);

function intensityFor(list: StudySession[] | undefined): number {
  if (!list || list.length === 0) return 0;
  const mins = list.reduce((sum, s) => sum + s.duration, 0);
  if (mins >= 120) return 4;
  if (mins >= 80) return 3;
  if (mins >= 40) return 2;
  return 1;
}

// Monday-first grid.
const monthDays = computed<DayCell[]>(() => {
  const y = viewYear.value;
  const m = viewMonth.value;
  const first = new Date(y, m, 1);
  const lead = (first.getDay() + 6) % 7; // Mon=0
  const daysInMonth = new Date(y, m + 1, 0).getDate();
  const cells: DayCell[] = [];

  // leading blanks from previous month
  const prevDays = new Date(y, m, 0).getDate();
  for (let i = lead - 1; i >= 0; i -= 1) {
    const d = prevDays - i;
    const pm = m === 0 ? 11 : m - 1;
    const py = m === 0 ? y - 1 : y;
    const k = keyOf(py, pm, d);
    cells.push({ key: k, date: d, inMonth: false, isToday: false, sessions: [], intensity: 0 });
  }
  // current month
  for (let d = 1; d <= daysInMonth; d += 1) {
    const k = keyOf(y, m, d);
    const list = sessions[k];
    cells.push({
      key: k,
      date: d,
      inMonth: true,
      isToday: k === todayKey,
      sessions: list ?? [],
      intensity: intensityFor(list)
    });
  }
  // trailing blanks
  const trail = (7 - (cells.length % 7)) % 7;
  for (let d = 1; d <= trail; d += 1) {
    const nm = m === 11 ? 0 : m + 1;
    const nyr = m === 11 ? y + 1 : y;
    const k = keyOf(nyr, nm, d);
    cells.push({ key: k, date: d, inMonth: false, isToday: false, sessions: [], intensity: 0 });
  }
  return cells;
});

const intensityRing: Record<number, string> = {
  0: '',
  1: 'ring-1 ring-cyan-500/20',
  2: 'ring-1 ring-cyan-400/40',
  3: 'ring-1 ring-fuchsia-400/45',
  4: 'ring-1 ring-amber-400/55'
};

function goPrev(): void {
  slideDir.value = 'prev';
  if (viewMonth.value === 0) {
    viewMonth.value = 11;
    viewYear.value -= 1;
  } else {
    viewMonth.value -= 1;
  }
}
function goNext(): void {
  slideDir.value = 'next';
  if (viewMonth.value === 11) {
    viewMonth.value = 0;
    viewYear.value += 1;
  } else {
    viewMonth.value += 1;
  }
}
function goToday(): void {
  slideDir.value = 'next';
  viewYear.value = now.getFullYear();
  viewMonth.value = now.getMonth();
  selectedKey.value = todayKey;
}

function selectDay(cell: DayCell): void {
  if (!cell.inMonth) return;
  selectedKey.value = cell.key;
}

const selectedSessions = computed<StudySession[]>(() => sessions[selectedKey.value] ?? []);

const selectedDateLabel = computed<string>(() => {
  const parts = selectedKey.value.split('-').map((p) => Number(p));
  const d = new Date(parts[0], parts[1] - 1, parts[2]);
  const wd = ['Chủ nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'][d.getDay()];
  return `${wd}, ${parts[2]}/${parts[1]}/${parts[0]}`;
});

const selectedTotalMinutes = computed<number>(() =>
  selectedSessions.value.reduce((sum, s) => sum + s.duration, 0)
);

// Mini add-session form
const formSubject = ref<string>(subjectOptions[0]);
const formTime = ref<string>('20:00');
const formDuration = ref<number>(30);
const formType = ref<SessionType>('doc');

function addSession(): void {
  const dur = Math.max(5, Math.min(240, Math.round(formDuration.value) || 30));
  const next = makeSession(formSubject.value, formTime.value || '20:00', dur, formType.value);
  const existing = sessions[selectedKey.value];
  if (existing) {
    existing.push(next);
    existing.sort((a, b) => a.time.localeCompare(b.time));
  } else {
    sessions[selectedKey.value] = [next];
  }
}

function removeSession(id: string): void {
  const list = sessions[selectedKey.value];
  if (!list) return;
  const idx = list.findIndex((s) => s.id === id);
  if (idx >= 0) list.splice(idx, 1);
}

// Streak heatmap: 12 weeks (84 days) ending today, Mon-first columns.
interface HeatCell {
  key: string;
  minutes: number;
  level: number;
  label: string;
}

const heatmap = computed<HeatCell[][]>(() => {
  const totalDays = 12 * 7;
  const cells: HeatCell[] = [];
  // Build base from sessions, plus deterministic pseudo-random fill for older days.
  for (let i = totalDays - 1; i >= 0; i -= 1) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    const k = isoKey(d);
    let minutes = (sessions[k] ?? []).reduce((sum, s) => sum + s.duration, 0);
    if (minutes === 0) {
      // deterministic filler so older weeks look lived-in
      const seed = (d.getDate() * 13 + (d.getMonth() + 1) * 7 + d.getDay() * 3) % 11;
      minutes = seed <= 2 ? 0 : seed * 12;
    }
    const level = minutes === 0 ? 0 : minutes < 25 ? 1 : minutes < 50 ? 2 : minutes < 90 ? 3 : 4;
    const label = `${d.getDate()}/${d.getMonth() + 1}: ${minutes} phút`;
    cells.push({ key: k, minutes, level, label });
  }
  // chunk into 12 columns of 7
  const cols: HeatCell[][] = [];
  for (let c = 0; c < 12; c += 1) {
    cols.push(cells.slice(c * 7, c * 7 + 7));
  }
  return cols;
});

const heatLevelClass: Record<number, string> = {
  0: 'bg-white/[0.04]',
  1: 'bg-emerald-500/25',
  2: 'bg-emerald-500/45',
  3: 'bg-emerald-400/70',
  4: 'bg-emerald-300'
};

// Upcoming sessions (next 7 days from today, sorted)
interface Upcoming {
  key: string;
  dayLabel: string;
  session: StudySession;
}
const upcoming = computed<Upcoming[]>(() => {
  const out: Upcoming[] = [];
  for (let i = 0; i <= 9; i += 1) {
    const d = new Date(now);
    d.setDate(d.getDate() + i);
    const k = isoKey(d);
    const list = sessions[k];
    if (!list) continue;
    const wd = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'][d.getDay()];
    const dayLabel = i === 0 ? 'Hôm nay' : i === 1 ? 'Ngày mai' : `${wd} ${d.getDate()}/${d.getMonth() + 1}`;
    for (const s of [...list].sort((a, b) => a.time.localeCompare(b.time))) {
      out.push({ key: `${k}-${s.id}`, dayLabel, session: s });
    }
  }
  return out.slice(0, 6);
});

// KPI tiles
const kpis = computed<KpiTile[]>(() => {
  // sessions this week (current month days within current iso-week of today)
  let weekSessions = 0;
  let weekMinutes = 0;
  const monday = new Date(now);
  monday.setDate(now.getDate() - ((now.getDay() + 6) % 7));
  for (let i = 0; i < 7; i += 1) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const list = sessions[isoKey(d)];
    if (list) {
      weekSessions += list.length;
      weekMinutes += list.reduce((sum, s) => sum + s.duration, 0);
    }
  }
  const hours = Math.round((weekMinutes / 60) * 10) / 10;
  return [
    { label: 'Phiên tuần này', value: `${weekSessions}`, unit: 'phiên', icon: 'calendar', accent: 'cyan', trend: 12, caption: `${weekMinutes} phút`, sparkline: [3, 5, 4, 6, 5, 7, weekSessions] },
    { label: 'Tổng giờ học', value: `${hours}`, unit: 'giờ', icon: 'clock', accent: 'violet', trend: 8, caption: 'Trong 7 ngày', sparkline: [2, 3, 2.5, 4, 3.5, 5, hours] },
    { label: 'Chuỗi học', value: '12', unit: 'ngày', icon: 'flame', accent: 'amber', trend: 4, caption: 'Kỷ lục: 21 ngày', sparkline: [6, 7, 8, 9, 10, 11, 12] },
    { label: 'Tỉ lệ hoàn thành', value: '86', unit: '%', icon: 'check-circle', accent: 'green', trend: 5, caption: 'Phiên đã lên lịch', sparkline: [70, 74, 78, 80, 82, 84, 86] }
  ];
});
</script>

<template>
  <div class="space-y-6">
    <!-- Hero -->
    <GlassPanel glow="cyan" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Lịch học cá nhân</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Lịch học</span> của bạn 📅
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Theo dõi các phiên học theo ngày, giữ vững chuỗi học và lên kế hoạch ôn tập thông minh hơn.
          </p>
        </div>
        <div class="flex shrink-0 items-center gap-2">
          <button type="button" class="cosmic-btn inline-flex items-center gap-1.5 rounded-sm px-4 py-2 text-sm" @click="goToday">
            <AppIcon name="calendar" class="h-4 w-4" /> Hôm nay
          </button>
        </div>
      </div>
    </GlassPanel>

    <!-- KPIs -->
    <section class="grid grid-cols-2 gap-4 xl:grid-cols-4">
      <StatPanel
        v-for="kpi in kpis"
        :key="kpi.label"
        :label="kpi.label"
        :value="kpi.value"
        :unit="kpi.unit"
        :icon="kpi.icon"
        :accent="kpi.accent"
        :trend="kpi.trend"
        :caption="kpi.caption"
        :sparkline="kpi.sparkline"
      />
    </section>

    <!-- Calendar + side panel -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Month grid -->
      <div class="lg:col-span-2">
        <SectionPanel :title="monthLabel" subtitle="Nhấp vào một ngày để xem chi tiết" icon="calendar" accent="cyan">
          <template #header-actions>
            <div class="inline-flex items-center gap-1">
              <button
                type="button"
                aria-label="Tháng trước"
                class="flex h-8 w-8 items-center justify-center rounded-sm border border-white/[0.08] text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
                @click="goPrev"
              >
                <AppIcon name="chevron-left" class="h-4 w-4" />
              </button>
              <button
                type="button"
                aria-label="Tháng sau"
                class="flex h-8 w-8 items-center justify-center rounded-sm border border-white/[0.08] text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
                @click="goNext"
              >
                <AppIcon name="chevron-right" class="h-4 w-4" />
              </button>
            </div>
          </template>

          <!-- weekday header -->
          <div class="mb-2 grid grid-cols-7 gap-1.5">
            <div
              v-for="wd in weekdayLabels"
              :key="wd"
              class="py-1 text-center text-[0.7rem] font-semibold uppercase tracking-wider text-muted-foreground"
            >
              {{ wd }}
            </div>
          </div>

          <Transition :name="slideDir === 'next' ? 'slide-next' : 'slide-prev'" mode="out-in">
            <div :key="`${viewYear}-${viewMonth}`" class="grid grid-cols-7 gap-1.5">
              <button
                v-for="cell in monthDays"
                :key="cell.key"
                type="button"
                :disabled="!cell.inMonth"
                :aria-label="`Ngày ${cell.date}`"
                class="group relative flex aspect-square flex-col items-center justify-start rounded-sm border p-1.5 text-left transition-all"
                :class="[
                  cell.inMonth
                    ? 'border-white/[0.06] bg-white/[0.02] hover:border-cyan-400/40 hover:bg-white/[0.05]'
                    : 'cursor-default border-transparent bg-transparent opacity-40',
                  cell.inMonth ? intensityRing[cell.intensity] : '',
                  selectedKey === cell.key && cell.inMonth
                    ? 'border-cyan-400/60 bg-cyan-500/[0.08]'
                    : ''
                ]"
                @click="selectDay(cell)"
              >
                <span
                  class="flex h-6 w-6 items-center justify-center rounded-sm text-xs font-semibold tabular-nums"
                  :class="
                    cell.isToday
                      ? 'bg-cyan-400 text-slate-900'
                      : cell.inMonth
                        ? 'text-foreground'
                        : 'text-muted-foreground'
                  "
                >
                  {{ cell.date }}
                </span>
                <span v-if="cell.sessions.length" class="mt-auto flex items-center gap-0.5 pb-0.5">
                  <span
                    v-for="s in cell.sessions.slice(0, 3)"
                    :key="s.id"
                    class="h-1.5 w-1.5 rounded-sm"
                    :class="sessionTypeMeta[s.type].dot"
                  />
                  <span v-if="cell.sessions.length > 3" class="text-[0.55rem] font-semibold text-muted-foreground">
                    +{{ cell.sessions.length - 3 }}
                  </span>
                </span>
              </button>
            </div>
          </Transition>

          <!-- legend -->
          <div class="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 border-t border-white/[0.06] pt-3 text-[0.7rem] text-muted-foreground">
            <span
              v-for="(meta, key) in sessionTypeMeta"
              :key="key"
              class="inline-flex items-center gap-1.5"
            >
              <span class="h-2 w-2 rounded-sm" :class="meta.dot" /> {{ meta.label }}
            </span>
          </div>
        </SectionPanel>
      </div>

      <!-- Side panel: selected day -->
      <div>
        <SectionPanel :title="selectedDateLabel" :subtitle="`${selectedSessions.length} phiên · ${selectedTotalMinutes} phút`" icon="clock" accent="violet">
          <div v-if="selectedSessions.length" class="space-y-2.5">
            <div
              v-for="s in selectedSessions"
              :key="s.id"
              class="group flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] p-3 transition-colors hover:border-white/[0.12]"
            >
              <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-sm" :class="sessionTypeMeta[s.type].chip">
                <AppIcon :name="sessionTypeMeta[s.type].icon" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-semibold text-foreground">{{ s.subject }}</p>
                <p class="text-xs text-muted-foreground">{{ sessionTypeMeta[s.type].label }} · {{ s.duration }} phút</p>
              </div>
              <span class="shrink-0 text-xs font-semibold tabular-nums text-cyan-200">{{ s.time }}</span>
              <button
                type="button"
                aria-label="Xoá phiên học"
                class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm text-muted-foreground opacity-0 transition-all hover:bg-rose-500/10 hover:text-rose-300 group-hover:opacity-100"
                @click="removeSession(s.id)"
              >
                <AppIcon name="trash" class="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
          <div v-else class="flex flex-col items-center justify-center rounded-sm border border-dashed border-white/[0.1] py-8 text-center">
            <span class="flex h-12 w-12 items-center justify-center rounded-sm bg-white/[0.04] text-muted-foreground">
              <AppIcon name="inbox" class="h-6 w-6" />
            </span>
            <p class="mt-2 text-sm font-medium text-foreground">Chưa có phiên học</p>
            <p class="mt-0.5 text-xs text-muted-foreground">Thêm phiên đầu tiên cho ngày này.</p>
          </div>

          <!-- mini form -->
          <form class="mt-4 space-y-3 rounded-sm border border-white/[0.06] bg-white/[0.02] p-3.5" @submit.prevent="addSession">
            <p class="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <AppIcon name="plus" class="h-3.5 w-3.5" /> Thêm phiên học
            </p>
            <div>
              <label class="mb-1 block text-xs text-muted-foreground" for="cal-subject">Môn học</label>
              <select id="cal-subject" v-model="formSubject" class="cosmic-input w-full text-sm">
                <option v-for="opt in subjectOptions" :key="opt" :value="opt">{{ opt }}</option>
              </select>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="mb-1 block text-xs text-muted-foreground" for="cal-time">Giờ</label>
                <input id="cal-time" v-model="formTime" type="time" class="cosmic-input w-full text-sm" />
              </div>
              <div>
                <label class="mb-1 block text-xs text-muted-foreground" for="cal-dur">Thời lượng (phút)</label>
                <input id="cal-dur" v-model.number="formDuration" type="number" min="5" max="240" step="5" class="cosmic-input w-full text-sm" />
              </div>
            </div>
            <div>
              <label class="mb-1 block text-xs text-muted-foreground" for="cal-type">Loại</label>
              <select id="cal-type" v-model="formType" class="cosmic-input w-full text-sm">
                <option v-for="(meta, key) in sessionTypeMeta" :key="key" :value="key">{{ meta.label }}</option>
              </select>
            </div>
            <button type="submit" class="cosmic-btn w-full rounded-sm px-4 py-2 text-sm">Thêm vào lịch</button>
          </form>
        </SectionPanel>
      </div>
    </div>

    <!-- Heatmap + Upcoming -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <div class="lg:col-span-2">
        <SectionPanel title="Bản đồ nhiệt chuỗi học" subtitle="12 tuần gần nhất · phút học mỗi ngày" icon="flame" accent="green">
          <div class="cosmic-scroll overflow-x-auto pb-1">
            <div class="flex gap-1.5">
              <div v-for="(col, ci) in heatmap" :key="ci" class="flex flex-col gap-1.5">
                <span
                  v-for="hc in col"
                  :key="hc.key"
                  class="h-3.5 w-3.5 rounded-[3px] transition-transform hover:scale-125"
                  :class="heatLevelClass[hc.level]"
                  :title="hc.label"
                />
              </div>
            </div>
          </div>
          <div class="mt-4 flex items-center justify-end gap-1.5 text-[0.7rem] text-muted-foreground">
            <span>Ít</span>
            <span v-for="lvl in [0, 1, 2, 3, 4]" :key="lvl" class="h-3 w-3 rounded-[3px]" :class="heatLevelClass[lvl]" />
            <span>Nhiều</span>
          </div>
        </SectionPanel>
      </div>

      <div>
        <SectionPanel title="Sắp tới" subtitle="Phiên học đã lên lịch" icon="calendar" accent="amber" flush>
          <ul v-if="upcoming.length" class="divide-y divide-white/[0.05]">
            <li v-for="u in upcoming" :key="u.key" class="flex items-center gap-3 px-5 py-2.5">
              <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-sm" :class="sessionTypeMeta[u.session.type].chip">
                <AppIcon :name="sessionTypeMeta[u.session.type].icon" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ u.session.subject }}</p>
                <p class="text-xs text-muted-foreground">{{ u.dayLabel }} · {{ u.session.time }}</p>
              </div>
              <span class="shrink-0 text-xs font-semibold tabular-nums text-muted-foreground">{{ u.session.duration }}'</span>
            </li>
          </ul>
          <p v-else class="px-5 py-6 text-center text-sm text-muted-foreground">Không có phiên nào sắp tới.</p>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.slide-next-enter-active,
.slide-next-leave-active,
.slide-prev-enter-active,
.slide-prev-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.slide-next-enter-from {
  opacity: 0;
  transform: translateX(18px);
}
.slide-next-leave-to {
  opacity: 0;
  transform: translateX(-18px);
}
.slide-prev-enter-from {
  opacity: 0;
  transform: translateX(-18px);
}
.slide-prev-leave-to {
  opacity: 0;
  transform: translateX(18px);
}
</style>
