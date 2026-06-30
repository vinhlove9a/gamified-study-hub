<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useAuthSession } from '@/features/auth/authSession';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import BarChart from '@/components/app/BarChart.vue';
import CountUp from '@/components/app/CountUp.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Sparkline from '@/components/app/Sparkline.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import Tabs from '@/components/app/Tabs.vue';
import Toggle from '@/components/app/Toggle.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type TaskKind = 'all' | 'reading' | 'quiz' | 'flashcard' | 'project' | 'community';
type Priority = 'high' | 'medium' | 'low';
type ViewTab = 'today' | 'plan' | 'insight';

interface StudyTask {
  id: number;
  title: string;
  subject: string;
  kind: Exclude<TaskKind, 'all'>;
  minutes: number;
  xp: number;
  coins: number;
  progress: number;
  due: string;
  priority: Priority;
  completed: boolean;
  note: string;
}

interface Habit {
  id: string;
  label: string;
  target: string;
  icon: string;
  checked: boolean;
}

const STORAGE_KEY = 'study-hub-home-v2';
const { currentUser } = useAuthSession();

const firstName = computed(() => {
  const full = currentUser.value?.fullName?.trim();
  if (full) return full.split(/\s+/).slice(-1)[0];
  return currentUser.value?.email?.split('@')[0] ?? 'bạn';
});

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 11) return 'Chào buổi sáng';
  if (hour < 14) return 'Chào buổi trưa';
  if (hour < 18) return 'Chào buổi chiều';
  return 'Chào buổi tối';
});

const defaultTasks: StudyTask[] = [
  {
    id: 1,
    title: 'Đọc tiếp Giải tích 1 - Giới hạn và liên tục',
    subject: 'Toán học',
    kind: 'reading',
    minutes: 24,
    xp: 120,
    coins: 35,
    progress: 62,
    due: '09:30',
    priority: 'high',
    completed: false,
    note: 'Hoàn thành mục 3.4 và đánh dấu công thức quan trọng.'
  },
  {
    id: 2,
    title: 'Ôn 40 thẻ tiếng Anh B2',
    subject: 'Ngoại ngữ',
    kind: 'flashcard',
    minutes: 18,
    xp: 80,
    coins: 20,
    progress: 35,
    due: '11:00',
    priority: 'medium',
    completed: false,
    note: 'Tập trung nhóm phrasal verbs có tỉ lệ sai cao.'
  },
  {
    id: 3,
    title: 'Làm quiz Vật lý điện từ học',
    subject: 'Vật lý',
    kind: 'quiz',
    minutes: 16,
    xp: 95,
    coins: 30,
    progress: 0,
    due: '14:20',
    priority: 'high',
    completed: true,
    note: 'Mục tiêu >= 80%, xem lại câu sai ngay sau quiz.'
  },
  {
    id: 4,
    title: 'Viết ghi chú thuật toán sắp xếp',
    subject: 'Tin học',
    kind: 'project',
    minutes: 30,
    xp: 140,
    coins: 40,
    progress: 45,
    due: '16:00',
    priority: 'medium',
    completed: false,
    note: 'Tóm tắt độ phức tạp và thêm ví dụ Vue/TypeScript.'
  },
  {
    id: 5,
    title: 'Trả lời 2 câu hỏi trong nhóm ôn thi',
    subject: 'Cộng đồng',
    kind: 'community',
    minutes: 12,
    xp: 60,
    coins: 25,
    progress: 50,
    due: '20:30',
    priority: 'low',
    completed: false,
    note: 'Ưu tiên câu hỏi đã có tài liệu tham khảo.'
  }
];

const defaultHabits: Habit[] = [
  { id: 'warmup', label: 'Khởi động 5 phút', target: 'Trước phiên học đầu tiên', icon: 'sun', checked: true },
  { id: 'notes', label: 'Ghi lại 3 ý chính', target: 'Sau mỗi tài liệu dài', icon: 'edit', checked: false },
  { id: 'review', label: 'Ôn lại câu sai', target: 'Ngay sau quiz', icon: 'repeat', checked: true },
  { id: 'rest', label: 'Nghỉ mắt đúng nhịp', target: 'Mỗi 25 phút', icon: 'eye', checked: false }
];

const tasks = ref<StudyTask[]>(defaultTasks);
const habits = ref<Habit[]>(defaultHabits);
const activeTab = ref<ViewTab>('today');
const activeKind = ref<TaskKind>('all');
const activePriority = ref<'all' | Priority>('all');
const selectedPreset = ref('deep');
const toast = ref('');

const newTask = reactive({
  title: '',
  subject: 'Tự học',
  kind: 'reading' as Exclude<TaskKind, 'all'>,
  minutes: 20,
  priority: 'medium' as Priority
});

const tabs = [
  { id: 'today', label: 'Hôm nay' },
  { id: 'plan', label: 'Kế hoạch' },
  { id: 'insight', label: 'Phân tích' }
];

const kindOptions: Array<{ id: TaskKind; label: string; icon: string }> = [
  { id: 'all', label: 'Tất cả', icon: 'grid' },
  { id: 'reading', label: 'Đọc', icon: 'book-open' },
  { id: 'quiz', label: 'Quiz', icon: 'check-circle' },
  { id: 'flashcard', label: 'Thẻ', icon: 'layers' },
  { id: 'project', label: 'Dự án', icon: 'edit' },
  { id: 'community', label: 'Nhóm', icon: 'users' }
];

const priorityMeta: Record<Priority, { label: string; class: string }> = {
  high: { label: 'Cao', class: 'border-rose-400/30 bg-rose-500/10 text-rose-300' },
  medium: { label: 'Vừa', class: 'border-amber-400/30 bg-amber-500/10 text-amber-300' },
  low: { label: 'Thấp', class: 'border-zinc-700 bg-zinc-900 text-zinc-400' }
};

const kindMeta: Record<Exclude<TaskKind, 'all'>, { label: string; icon: string; accent: Accent }> = {
  reading: { label: 'Đọc tài liệu', icon: 'book-open', accent: 'cyan' },
  quiz: { label: 'Quiz', icon: 'check-circle', accent: 'green' },
  flashcard: { label: 'Flashcard', icon: 'layers', accent: 'magenta' },
  project: { label: 'Dự án', icon: 'edit', accent: 'violet' },
  community: { label: 'Cộng đồng', icon: 'users', accent: 'amber' }
};

const accentChip: Record<Accent, string> = {
  cyan: 'border-cyan-400/30 bg-cyan-500/10 text-cyan-300',
  magenta: 'border-fuchsia-400/30 bg-fuchsia-500/10 text-fuchsia-300',
  green: 'border-emerald-400/30 bg-emerald-500/10 text-emerald-300',
  violet: 'border-violet-400/30 bg-violet-500/10 text-violet-300',
  amber: 'border-amber-400/30 bg-amber-500/10 text-amber-300'
};

const weeklyBars = [
  { label: 'T2', value: 45 },
  { label: 'T3', value: 30 },
  { label: 'T4', value: 60 },
  { label: 'T5', value: 25 },
  { label: 'T6', value: 52 },
  { label: 'T7', value: 74 },
  { label: 'CN', value: 38, active: true }
];

const skillMap = [
  { label: 'Giải tích', level: 68, trend: '+8%', weak: 'Định lý kẹp', accent: 'cyan' as Accent },
  { label: 'Tiếng Anh B2', level: 54, trend: '+5%', weak: 'Collocation', accent: 'magenta' as Accent },
  { label: 'Điện từ học', level: 72, trend: '+12%', weak: 'Từ thông', accent: 'green' as Accent },
  { label: 'Thuật toán', level: 49, trend: '+3%', weak: 'Quy hoạch động', accent: 'violet' as Accent }
];

const schedule = [
  { time: '09:30', title: 'Đọc sâu', detail: 'Giải tích 1', icon: 'book-open', accent: 'cyan' as Accent },
  { time: '11:00', title: 'Ôn nhanh', detail: '40 flashcards B2', icon: 'layers', accent: 'magenta' as Accent },
  { time: '14:20', title: 'Kiểm tra', detail: 'Quiz Vật lý 15 câu', icon: 'check-circle', accent: 'green' as Accent },
  { time: '20:30', title: 'Cộng đồng', detail: 'Trả lời nhóm ôn thi', icon: 'message', accent: 'amber' as Accent }
];

const presets = [
  { id: 'quick', label: 'Nhanh', minutes: 15, blocks: 1, break: 3 },
  { id: 'deep', label: 'Sâu', minutes: 25, blocks: 3, break: 5 },
  { id: 'marathon', label: 'Dài', minutes: 45, blocks: 2, break: 10 }
];

const recommendations = [
  { title: 'Lộ trình Toán tăng tốc', text: 'Ghép 3 tài liệu, 2 quiz và 1 phiên ôn lỗi.', to: '/app/paths', icon: 'route', accent: 'cyan' as Accent },
  { title: 'Tạo bộ flashcard từ ghi chú', text: 'Chuyển ghi chú hôm nay thành thẻ ôn ngày mai.', to: '/app/flashcards', icon: 'layers', accent: 'magenta' as Accent },
  { title: 'Đặt lịch học nhóm', text: 'Mời 3 bạn cùng giải đề trong 45 phút.', to: '/app/groups', icon: 'users', accent: 'amber' as Accent }
];

const loaded = ref(false);

const filteredTasks = computed(() =>
  tasks.value.filter((task) => {
    const kindOk = activeKind.value === 'all' || task.kind === activeKind.value;
    const priorityOk = activePriority.value === 'all' || task.priority === activePriority.value;
    return kindOk && priorityOk;
  })
);

const completedTasks = computed(() => tasks.value.filter((task) => task.completed));
const openTasks = computed(() => tasks.value.filter((task) => !task.completed));
const totalMinutes = computed(() => tasks.value.reduce((sum, task) => sum + task.minutes, 0));
const doneMinutes = computed(() => completedTasks.value.reduce((sum, task) => sum + task.minutes, 0));
const dailyPct = computed(() => Math.round((doneMinutes.value / Math.max(1, totalMinutes.value)) * 100));
const earnedXp = computed(() => completedTasks.value.reduce((sum, task) => sum + task.xp, 1240));
const earnedCoins = computed(() => completedTasks.value.reduce((sum, task) => sum + task.coins, 1250));
const habitPct = computed(() => Math.round((habits.value.filter((h) => h.checked).length / habits.value.length) * 100));
const nextTask = computed(() => openTasks.value.find((task) => task.priority === 'high') ?? openTasks.value[0] ?? tasks.value[0]);
const selectedPresetData = computed(() => presets.find((preset) => preset.id === selectedPreset.value) ?? presets[1]);
const projectedFocusMinutes = computed(() => selectedPresetData.value.minutes * selectedPresetData.value.blocks);
const weeklyTotal = computed(() => weeklyBars.reduce((sum, day) => sum + day.value, 0) + doneMinutes.value);
const readinessScore = computed(() => Math.round((dailyPct.value * 0.45) + (habitPct.value * 0.25) + (Math.min(100, weeklyTotal.value / 4) * 0.3)));

const stats = computed(() => [
  {
    label: 'XP hiện tại',
    value: earnedXp.value.toLocaleString('vi-VN'),
    unit: 'XP',
    icon: 'zap',
    accent: 'cyan' as Accent,
    trend: 12,
    caption: 'Cấp 7 - còn 680 XP lên cấp 8',
    sparkline: [780, 900, 970, 1040, 1110, 1180, earnedXp.value]
  },
  {
    label: 'Coin khả dụng',
    value: earnedCoins.value.toLocaleString('vi-VN'),
    unit: 'coin',
    icon: 'coin',
    accent: 'amber' as Accent,
    trend: 9,
    caption: '+85 coin từ kế hoạch hôm nay',
    sparkline: [850, 960, 1000, 1140, 1210, earnedCoins.value]
  },
  {
    label: 'Chuỗi học',
    value: '12',
    unit: 'ngày',
    icon: 'flame',
    accent: 'green' as Accent,
    trend: 4,
    caption: 'Kỷ lục cá nhân: 21 ngày',
    sparkline: [4, 6, 7, 9, 11, 12, 12]
  },
  {
    label: 'Sẵn sàng ôn thi',
    value: `${readinessScore.value}`,
    unit: '/100',
    icon: 'shield',
    accent: 'violet' as Accent,
    trend: 7,
    caption: 'Dựa trên tiến độ, thói quen và tuần học',
    sparkline: [42, 48, 53, 57, 61, 66, readinessScore.value]
  }
]);

function persist(): void {
  if (!loaded.value || typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify({ tasks: tasks.value, habits: habits.value }));
}

function showToast(message: string): void {
  toast.value = message;
  window.setTimeout(() => {
    if (toast.value === message) toast.value = '';
  }, 2200);
}

function toggleTask(task: StudyTask): void {
  task.completed = !task.completed;
  task.progress = task.completed ? 100 : Math.min(task.progress, 70);
  showToast(task.completed ? `Đã hoàn thành: ${task.title}` : `Đã mở lại: ${task.title}`);
}

function boostTask(task: StudyTask): void {
  task.priority = 'high';
  task.progress = Math.min(95, task.progress + 15);
  showToast(`Đã đưa "${task.title}" lên ưu tiên cao`);
}

function removeTask(taskId: number): void {
  tasks.value = tasks.value.filter((task) => task.id !== taskId);
  showToast('Đã xoá nhiệm vụ khỏi kế hoạch hôm nay');
}

function addTask(): void {
  const title = newTask.title.trim();
  if (!title) {
    showToast('Nhập tên nhiệm vụ trước đã nhé');
    return;
  }
  tasks.value.unshift({
    id: Date.now(),
    title,
    subject: newTask.subject.trim() || 'Tự học',
    kind: newTask.kind,
    minutes: Math.max(5, Number(newTask.minutes) || 20),
    xp: Math.max(30, Math.round(newTask.minutes * 4)),
    coins: Math.max(10, Math.round(newTask.minutes * 1.2)),
    progress: 0,
    due: 'Tự chọn',
    priority: newTask.priority,
    completed: false,
    note: 'Nhiệm vụ tự tạo trong dashboard frontend.'
  });
  newTask.title = '';
  showToast('Đã thêm nhiệm vụ mới vào hàng đợi');
}

function resetPlan(): void {
  tasks.value = defaultTasks.map((task) => ({ ...task }));
  habits.value = defaultHabits.map((habit) => ({ ...habit }));
  showToast('Đã khôi phục kế hoạch mẫu');
}

function startFocus(): void {
  showToast(`Bắt đầu preset ${selectedPresetData.value.label}: ${projectedFocusMinutes.value} phút tập trung`);
}

onMounted(() => {
  if (typeof window === 'undefined') return;
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as { tasks?: StudyTask[]; habits?: Habit[] };
      if (Array.isArray(parsed.tasks)) tasks.value = parsed.tasks;
      if (Array.isArray(parsed.habits)) habits.value = parsed.habits;
    }
  } catch {
    tasks.value = defaultTasks.map((task) => ({ ...task }));
    habits.value = defaultHabits.map((habit) => ({ ...habit }));
  } finally {
    loaded.value = true;
  }
});

watch([tasks, habits], persist, { deep: true });
</script>

<template>
  <div class="space-y-6">
    <GlassPanel glow="cyan" class="p-5 sm:p-6">
      <div class="grid gap-6 xl:grid-cols-[1.35fr_0.65fr] xl:items-center">
        <div>
          <p class="cosmic-badge">
            <AppIcon name="command" class="h-3.5 w-3.5" />
            Trung tâm điều hành học tập
          </p>
          <div class="mt-4 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div class="min-w-0">
              <h1 class="text-2xl font-bold text-foreground sm:text-3xl">
                {{ greeting }}, {{ firstName }}.
              </h1>
              <p class="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
                Hôm nay còn {{ openTasks.length }} việc mở, {{ projectedFocusMinutes }} phút tập trung được đề xuất
                và {{ habits.filter((h) => h.checked).length }}/{{ habits.length }} thói quen đã giữ nhịp.
              </p>
            </div>
            <div class="flex flex-wrap gap-2">
              <RouterLink to="/app/focus" class="cosmic-btn inline-flex items-center gap-2 px-4 py-2 text-sm">
                <AppIcon name="play" class="h-4 w-4" />
                Vào phiên học
              </RouterLink>
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-sm border border-zinc-800 px-4 py-2 text-sm font-semibold text-zinc-200 transition-colors hover:border-primary hover:text-primary"
                @click="resetPlan"
              >
                <AppIcon name="refresh" class="h-4 w-4" />
                Làm mới demo
              </button>
            </div>
          </div>

          <div class="mt-5 grid gap-3 sm:grid-cols-3">
            <div class="border border-zinc-800 bg-zinc-950/60 p-3">
              <p class="text-[0.7rem] font-semibold uppercase tracking-wide text-muted-foreground">Mục tiêu ngày</p>
              <p class="mt-1 text-2xl font-bold tabular-nums text-foreground">{{ dailyPct }}%</p>
              <ProgressBar :value="dailyPct" accent="cyan" class="mt-3" />
            </div>
            <div class="border border-zinc-800 bg-zinc-950/60 p-3">
              <p class="text-[0.7rem] font-semibold uppercase tracking-wide text-muted-foreground">Phút đã học</p>
              <p class="mt-1 text-2xl font-bold tabular-nums text-foreground">
                <CountUp :value="doneMinutes" />/{{ totalMinutes }}
              </p>
              <p class="mt-1 text-xs text-muted-foreground">Tính từ các nhiệm vụ đã hoàn thành.</p>
            </div>
            <div class="border border-zinc-800 bg-zinc-950/60 p-3">
              <p class="text-[0.7rem] font-semibold uppercase tracking-wide text-muted-foreground">Sẵn sàng ôn thi</p>
              <p class="mt-1 text-2xl font-bold tabular-nums text-foreground">{{ readinessScore }}/100</p>
              <p class="mt-1 text-xs text-muted-foreground">Cập nhật theo tiến độ và thói quen.</p>
            </div>
          </div>
        </div>

        <div class="border border-zinc-800 bg-zinc-950/70 p-4">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">Việc nên làm tiếp</p>
              <h2 class="mt-1 text-base font-semibold text-foreground">{{ nextTask.title }}</h2>
            </div>
            <span
              class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm border"
              :class="accentChip[kindMeta[nextTask.kind].accent]"
            >
              <AppIcon :name="kindMeta[nextTask.kind].icon" class="h-5 w-5" />
            </span>
          </div>
          <p class="mt-3 text-sm leading-6 text-muted-foreground">{{ nextTask.note }}</p>
          <div class="mt-4 grid grid-cols-3 gap-2 text-center text-xs">
            <span class="border border-zinc-800 bg-zinc-900/70 px-2 py-2 text-zinc-300">{{ nextTask.minutes }} phút</span>
            <span class="border border-zinc-800 bg-zinc-900/70 px-2 py-2 text-zinc-300">+{{ nextTask.xp }} XP</span>
            <span class="border border-zinc-800 bg-zinc-900/70 px-2 py-2 text-zinc-300">+{{ nextTask.coins }} coin</span>
          </div>
          <div class="mt-4 flex gap-2">
            <button
              type="button"
              class="cosmic-btn inline-flex flex-1 items-center justify-center gap-2 px-3 py-2 text-sm"
              @click="toggleTask(nextTask)"
            >
              <AppIcon name="check" class="h-4 w-4" />
              Hoàn thành
            </button>
            <RouterLink
              to="/app/calendar"
              class="inline-flex items-center justify-center rounded-sm border border-zinc-800 px-3 py-2 text-sm font-semibold text-zinc-300 hover:border-primary hover:text-primary"
            >
              <AppIcon name="calendar" class="h-4 w-4" />
            </RouterLink>
          </div>
        </div>
      </div>
    </GlassPanel>

    <section class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatPanel
        v-for="stat in stats"
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

    <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <Tabs v-model="activeTab" :tabs="tabs" />
      <div class="flex flex-wrap gap-2">
        <RouterLink
          v-for="link in [
            { to: '/app/documents', label: 'Tài liệu', icon: 'file-text' },
            { to: '/app/notes', label: 'Ghi chú', icon: 'edit' },
            { to: '/app/quizzes', label: 'Quiz', icon: 'check-circle' },
            { to: '/app/wallet', label: 'Ví', icon: 'coin' }
          ]"
          :key="link.to"
          :to="link.to"
          class="inline-flex items-center gap-1.5 rounded-sm border border-zinc-800 px-3 py-2 text-xs font-semibold text-zinc-300 transition-colors hover:border-primary hover:text-primary"
        >
          <AppIcon :name="link.icon" class="h-3.5 w-3.5" />
          {{ link.label }}
        </RouterLink>
      </div>
    </div>

    <div v-show="activeTab === 'today'" class="grid gap-6 xl:grid-cols-[1.45fr_0.85fr]">
      <SectionPanel
        title="Hàng đợi học hôm nay"
        :subtitle="`${filteredTasks.length} mục đang hiển thị · ${completedTasks.length}/${tasks.length} đã xong`"
        icon="target"
        accent="cyan"
      >
        <div class="space-y-4">
          <div class="grid gap-2 rounded-sm border border-zinc-800 bg-zinc-950/60 p-3 lg:grid-cols-[1fr_130px_120px_110px_auto]">
            <input
              v-model="newTask.title"
              type="text"
              class="h-10 rounded-sm border border-zinc-800 bg-zinc-950 px-3 text-sm text-foreground placeholder:text-muted-foreground/60 focus:border-primary focus:outline-none"
              placeholder="Thêm nhiệm vụ nhanh..."
              @keyup.enter="addTask"
            />
            <select
              v-model="newTask.kind"
              class="h-10 rounded-sm border border-zinc-800 bg-zinc-950 px-3 text-sm text-foreground focus:border-primary focus:outline-none"
            >
              <option value="reading">Đọc</option>
              <option value="quiz">Quiz</option>
              <option value="flashcard">Flashcard</option>
              <option value="project">Dự án</option>
              <option value="community">Cộng đồng</option>
            </select>
            <input
              v-model.number="newTask.minutes"
              min="5"
              type="number"
              class="h-10 rounded-sm border border-zinc-800 bg-zinc-950 px-3 text-sm text-foreground focus:border-primary focus:outline-none"
            />
            <select
              v-model="newTask.priority"
              class="h-10 rounded-sm border border-zinc-800 bg-zinc-950 px-3 text-sm text-foreground focus:border-primary focus:outline-none"
            >
              <option value="high">Cao</option>
              <option value="medium">Vừa</option>
              <option value="low">Thấp</option>
            </select>
            <button type="button" class="cosmic-btn inline-flex items-center justify-center gap-2 px-3 py-2 text-sm" @click="addTask">
              <AppIcon name="plus" class="h-4 w-4" />
              Thêm
            </button>
          </div>

          <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
            <div class="flex flex-wrap gap-2">
              <button
                v-for="kind in kindOptions"
                :key="kind.id"
                type="button"
                class="inline-flex items-center gap-1.5 rounded-sm border px-2.5 py-1.5 text-xs font-semibold transition-colors"
                :class="activeKind === kind.id ? 'border-primary bg-primary text-primary-foreground' : 'border-zinc-800 text-zinc-400 hover:border-primary hover:text-primary'"
                @click="activeKind = kind.id"
              >
                <AppIcon :name="kind.icon" class="h-3.5 w-3.5" />
                {{ kind.label }}
              </button>
            </div>
            <div class="flex gap-2">
              <button
                v-for="priority in ['all', 'high', 'medium', 'low']"
                :key="priority"
                type="button"
                class="rounded-sm border px-2.5 py-1.5 text-xs font-semibold transition-colors"
                :class="activePriority === priority ? 'border-primary bg-primary text-primary-foreground' : 'border-zinc-800 text-zinc-400 hover:border-primary hover:text-primary'"
                @click="activePriority = priority as 'all' | Priority"
              >
                {{ priority === 'all' ? 'Mọi ưu tiên' : priorityMeta[priority as Priority].label }}
              </button>
            </div>
          </div>

          <ul class="space-y-3">
            <li
              v-for="task in filteredTasks"
              :key="task.id"
              class="border border-zinc-800 bg-zinc-950/70 p-4 transition-colors hover:border-zinc-700"
              :class="{ 'opacity-65': task.completed }"
            >
              <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <span class="inline-flex items-center gap-1 rounded-sm border px-2 py-1 text-[0.7rem] font-semibold" :class="accentChip[kindMeta[task.kind].accent]">
                      <AppIcon :name="kindMeta[task.kind].icon" class="h-3 w-3" />
                      {{ kindMeta[task.kind].label }}
                    </span>
                    <span class="rounded-sm border px-2 py-1 text-[0.7rem] font-semibold" :class="priorityMeta[task.priority].class">
                      Ưu tiên {{ priorityMeta[task.priority].label }}
                    </span>
                    <span class="text-[0.7rem] text-muted-foreground">{{ task.due }} · {{ task.subject }}</span>
                  </div>
                  <h3 class="mt-2 text-sm font-semibold text-foreground" :class="{ 'line-through decoration-zinc-500': task.completed }">
                    {{ task.title }}
                  </h3>
                  <p class="mt-1 text-xs leading-5 text-muted-foreground">{{ task.note }}</p>
                  <div class="mt-3 flex items-center gap-3">
                    <ProgressBar :value="task.completed ? 100 : task.progress" :accent="kindMeta[task.kind].accent" class="flex-1" />
                    <span class="w-10 text-right text-xs tabular-nums text-zinc-400">{{ task.completed ? 100 : task.progress }}%</span>
                  </div>
                </div>
                <div class="grid grid-cols-3 gap-2 text-center text-xs lg:w-56">
                  <span class="border border-zinc-800 bg-zinc-900/70 px-2 py-2 text-zinc-300">{{ task.minutes }}p</span>
                  <span class="border border-zinc-800 bg-zinc-900/70 px-2 py-2 text-zinc-300">+{{ task.xp }} XP</span>
                  <span class="border border-zinc-800 bg-zinc-900/70 px-2 py-2 text-zinc-300">+{{ task.coins }}</span>
                  <button
                    type="button"
                    class="col-span-1 inline-flex items-center justify-center rounded-sm border border-zinc-800 px-2 py-2 text-zinc-300 hover:border-emerald-400/50 hover:text-emerald-300"
                    :aria-label="task.completed ? 'Mở lại nhiệm vụ' : 'Hoàn thành nhiệm vụ'"
                    @click="toggleTask(task)"
                  >
                    <AppIcon :name="task.completed ? 'refresh' : 'check'" class="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    class="col-span-1 inline-flex items-center justify-center rounded-sm border border-zinc-800 px-2 py-2 text-zinc-300 hover:border-amber-400/50 hover:text-amber-300"
                    aria-label="Đưa lên ưu tiên cao"
                    @click="boostTask(task)"
                  >
                    <AppIcon name="arrow-up" class="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    class="col-span-1 inline-flex items-center justify-center rounded-sm border border-zinc-800 px-2 py-2 text-zinc-300 hover:border-rose-400/50 hover:text-rose-300"
                    aria-label="Xoá nhiệm vụ"
                    @click="removeTask(task.id)"
                  >
                    <AppIcon name="trash" class="h-4 w-4" />
                  </button>
                </div>
              </div>
            </li>
          </ul>
        </div>
      </SectionPanel>

      <div class="space-y-6">
        <SectionPanel title="Preset tập trung" subtitle="Chọn nhịp học cho phiên tiếp theo" icon="clock" accent="violet">
          <div class="grid gap-2 sm:grid-cols-3 xl:grid-cols-1">
            <button
              v-for="preset in presets"
              :key="preset.id"
              type="button"
              class="rounded-sm border p-3 text-left transition-colors"
              :class="selectedPreset === preset.id ? 'border-primary bg-primary/10 text-primary' : 'border-zinc-800 bg-zinc-950/70 text-zinc-300 hover:border-primary'"
              @click="selectedPreset = preset.id"
            >
              <span class="text-sm font-semibold">{{ preset.label }}</span>
              <span class="mt-1 block text-xs text-muted-foreground">{{ preset.blocks }} block · {{ preset.minutes }}p học · nghỉ {{ preset.break }}p</span>
            </button>
          </div>
          <button type="button" class="cosmic-btn mt-4 inline-flex w-full items-center justify-center gap-2 px-4 py-2 text-sm" @click="startFocus">
            <AppIcon name="play" class="h-4 w-4" />
            Bắt đầu {{ projectedFocusMinutes }} phút
          </button>
        </SectionPanel>

        <SectionPanel title="Checklist thói quen" :subtitle="`${habitPct}% hoàn tất`" icon="repeat" accent="green">
          <ul class="space-y-3">
            <li v-for="habit in habits" :key="habit.id" class="flex items-center justify-between gap-3 border border-zinc-800 bg-zinc-950/60 p-3">
              <div class="flex min-w-0 items-center gap-3">
                <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-sm border border-zinc-800 text-primary">
                  <AppIcon :name="habit.icon" class="h-4 w-4" />
                </span>
                <div class="min-w-0">
                  <p class="truncate text-sm font-semibold text-foreground">{{ habit.label }}</p>
                  <p class="truncate text-xs text-muted-foreground">{{ habit.target }}</p>
                </div>
              </div>
              <Toggle v-model="habit.checked" />
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <div v-show="activeTab === 'plan'" class="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
      <SectionPanel title="Lịch học thông minh" subtitle="Các mốc được gợi ý theo năng lượng trong ngày" icon="calendar" accent="amber">
        <ol class="relative space-y-3 border-l border-zinc-800 pl-4">
          <li v-for="item in schedule" :key="`${item.time}-${item.title}`" class="relative">
            <span class="absolute -left-[25px] top-1 flex h-5 w-5 items-center justify-center rounded-sm border border-zinc-800 bg-zinc-950">
              <AppIcon :name="item.icon" class="h-3 w-3 text-primary" />
            </span>
            <div class="border border-zinc-800 bg-zinc-950/70 p-3">
              <div class="flex items-center justify-between gap-3">
                <p class="text-sm font-semibold text-foreground">{{ item.title }}</p>
                <span class="font-mono text-xs text-zinc-500">{{ item.time }}</span>
              </div>
              <p class="mt-1 text-xs text-muted-foreground">{{ item.detail }}</p>
            </div>
          </li>
        </ol>
      </SectionPanel>

      <SectionPanel title="Gợi ý mở rộng chức năng" subtitle="Đi tiếp sang các trang có sẵn" icon="lightbulb" accent="cyan">
        <div class="grid gap-3 md:grid-cols-3">
          <RouterLink
            v-for="item in recommendations"
            :key="item.title"
            :to="item.to"
            class="group border border-zinc-800 bg-zinc-950/70 p-4 transition-colors hover:border-primary"
          >
            <span class="flex h-10 w-10 items-center justify-center rounded-sm border" :class="accentChip[item.accent]">
              <AppIcon :name="item.icon" class="h-5 w-5" />
            </span>
            <p class="mt-3 text-sm font-semibold text-foreground group-hover:text-primary">{{ item.title }}</p>
            <p class="mt-1 text-xs leading-5 text-muted-foreground">{{ item.text }}</p>
          </RouterLink>
        </div>
      </SectionPanel>

      <SectionPanel title="Dự báo phần thưởng" subtitle="Nếu hoàn tất toàn bộ kế hoạch hôm nay" icon="gift" accent="green">
        <div class="grid gap-4 md:grid-cols-3">
          <div class="border border-zinc-800 bg-zinc-950/70 p-4">
            <p class="text-xs uppercase tracking-wide text-muted-foreground">XP thêm</p>
            <p class="mt-1 text-2xl font-bold text-foreground">+{{ openTasks.reduce((sum, task) => sum + task.xp, 0) }}</p>
          </div>
          <div class="border border-zinc-800 bg-zinc-950/70 p-4">
            <p class="text-xs uppercase tracking-wide text-muted-foreground">Coin thêm</p>
            <p class="mt-1 text-2xl font-bold text-foreground">+{{ openTasks.reduce((sum, task) => sum + task.coins, 0) }}</p>
          </div>
          <div class="border border-zinc-800 bg-zinc-950/70 p-4">
            <p class="text-xs uppercase tracking-wide text-muted-foreground">Mở khoá gần nhất</p>
            <p class="mt-1 text-sm font-semibold text-foreground">Huy hiệu "Ngày học trọn vẹn"</p>
          </div>
        </div>
      </SectionPanel>
    </div>

    <div v-show="activeTab === 'insight'" class="grid gap-6 xl:grid-cols-[0.85fr_1.15fr]">
      <SectionPanel title="Nhịp học tuần này" :subtitle="`Tổng ${weeklyTotal} phút`" icon="chart" accent="cyan">
        <BarChart :data="weeklyBars" accent="cyan" :height="150" />
        <Sparkline :data="[120, 150, 180, 205, 240, weeklyTotal]" accent="cyan" class="mt-5" :height="42" />
      </SectionPanel>

      <SectionPanel title="Bản đồ năng lực" subtitle="Chủ đề yếu được đưa vào hàng đợi ôn tập" icon="shield" accent="violet">
        <div class="space-y-4">
          <div v-for="skill in skillMap" :key="skill.label" class="border border-zinc-800 bg-zinc-950/70 p-4">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div>
                <p class="text-sm font-semibold text-foreground">{{ skill.label }}</p>
                <p class="text-xs text-muted-foreground">Cần củng cố: {{ skill.weak }}</p>
              </div>
              <span class="rounded-sm border px-2 py-1 text-xs font-semibold" :class="accentChip[skill.accent]">{{ skill.trend }}</span>
            </div>
            <div class="mt-3 flex items-center gap-3">
              <ProgressBar :value="skill.level" :accent="skill.accent" class="flex-1" />
              <span class="w-12 text-right text-xs tabular-nums text-zinc-400">{{ skill.level }}%</span>
            </div>
          </div>
        </div>
      </SectionPanel>
    </div>

    <transition name="fade">
      <div v-if="toast" class="fixed bottom-5 right-5 z-50 max-w-sm border border-zinc-800 bg-zinc-950 px-4 py-3 text-sm text-zinc-100 shadow-xl">
        {{ toast }}
      </div>
    </transition>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}
</style>
