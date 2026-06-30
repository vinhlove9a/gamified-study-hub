<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import BarChart from '@/components/app/BarChart.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type ActType = 'read' | 'quiz' | 'mission' | 'achievement' | 'focus';

const { push } = useToast();

interface Activity {
  id: number;
  type: ActType;
  title: string;
  time: string;
  day: 'today' | 'yesterday' | 'older';
  dayLabel: string;
  meta: string;
}

const typeMeta: Record<ActType, { label: string; icon: string; accent: Accent }> = {
  read: { label: 'Đọc tài liệu', icon: 'book-open', accent: 'cyan' },
  quiz: { label: 'Làm bài kiểm tra', icon: 'check-circle', accent: 'magenta' },
  mission: { label: 'Nhiệm vụ', icon: 'target', accent: 'green' },
  achievement: { label: 'Thành tựu', icon: 'award', accent: 'amber' },
  focus: { label: 'Tập trung', icon: 'clock', accent: 'violet' }
};

const filters: { id: 'all' | ActType; label: string; icon: string }[] = [
  { id: 'all', label: 'Tất cả', icon: 'list' },
  { id: 'read', label: 'Đọc', icon: 'book-open' },
  { id: 'quiz', label: 'Kiểm tra', icon: 'check-circle' },
  { id: 'mission', label: 'Nhiệm vụ', icon: 'target' },
  { id: 'focus', label: 'Tập trung', icon: 'clock' },
  { id: 'achievement', label: 'Thành tựu', icon: 'award' }
];
const activeFilter = ref<'all' | ActType>('all');
const query = ref<string>('');

const activities = ref<Activity[]>([
  {
    id: 1,
    type: 'read',
    title: 'Làm chủ đạo hàm từ cơ bản đến nâng cao',
    time: '21:14',
    day: 'today',
    dayLabel: 'Hôm nay',
    meta: '22 phút đọc'
  },
  {
    id: 2,
    type: 'focus',
    title: 'Phiên tập trung Pomodoro',
    time: '20:05',
    day: 'today',
    dayLabel: 'Hôm nay',
    meta: '50 phút · 2 phiên'
  },
  {
    id: 3,
    type: 'quiz',
    title: 'Quiz: Tích phân cơ bản',
    time: '19:30',
    day: 'today',
    dayLabel: 'Hôm nay',
    meta: 'Điểm 9/10'
  },
  {
    id: 4,
    type: 'achievement',
    title: 'Mở khoá huy hiệu "Cú đêm"',
    time: '00:22',
    day: 'today',
    dayLabel: 'Hôm nay',
    meta: 'Độ hiếm: Hiếm'
  },
  {
    id: 5,
    type: 'mission',
    title: 'Hoàn thành nhiệm vụ hằng ngày',
    time: '18:40',
    day: 'yesterday',
    dayLabel: 'Hôm qua',
    meta: '+120 coin'
  },
  {
    id: 6,
    type: 'read',
    title: 'Chiến lược chinh phục TOEIC 800+',
    time: '15:12',
    day: 'yesterday',
    dayLabel: 'Hôm qua',
    meta: '18 phút đọc'
  },
  {
    id: 7,
    type: 'quiz',
    title: 'Quiz: Từ vựng chủ đề công sở',
    time: '14:05',
    day: 'yesterday',
    dayLabel: 'Hôm qua',
    meta: 'Điểm 8/10'
  },
  {
    id: 8,
    type: 'read',
    title: 'Thuật toán & cấu trúc dữ liệu với Python',
    time: '22:48',
    day: 'older',
    dayLabel: '24/06/2026',
    meta: '30 phút đọc'
  },
  {
    id: 9,
    type: 'focus',
    title: 'Phiên học sâu buổi tối',
    time: '21:00',
    day: 'older',
    dayLabel: '24/06/2026',
    meta: '75 phút · 3 phiên'
  },
  {
    id: 10,
    type: 'mission',
    title: 'Giữ chuỗi học 7 ngày',
    time: '09:30',
    day: 'older',
    dayLabel: '23/06/2026',
    meta: '+200 coin'
  }
]);

const filteredActivities = computed<Activity[]>(() => {
  const q = query.value.trim().toLowerCase();
  return activities.value
    .filter((a) => activeFilter.value === 'all' || a.type === activeFilter.value)
    .filter((a) => q === '' || a.title.toLowerCase().includes(q));
});

interface DayGroup {
  label: string;
  items: Activity[];
}
const grouped = computed<DayGroup[]>(() => {
  const order: Activity['day'][] = ['today', 'yesterday', 'older'];
  const out: DayGroup[] = [];
  for (const day of order) {
    const dayItems = filteredActivities.value.filter((a) => a.day === day);
    const labels = [...new Set(dayItems.map((a) => a.dayLabel))];
    for (const label of labels) {
      out.push({ label, items: dayItems.filter((a) => a.dayLabel === label) });
    }
  }
  return out;
});

const weeklyMinutes: { label: string; value: number; active?: boolean }[] = [
  { label: 'T2', value: 65 },
  { label: 'T3', value: 90 },
  { label: 'T4', value: 45 },
  { label: 'T5', value: 120 },
  { label: 'T6', value: 80 },
  { label: 'T7', value: 150 },
  { label: 'CN', value: 110, active: true }
];

const stats = [
  { label: 'Tài liệu đã đọc', value: 142, icon: 'book-open', accent: 'cyan' as Accent },
  { label: 'Giờ học tích luỹ', value: 268, icon: 'clock', accent: 'violet' as Accent },
  { label: 'Ngày hoạt động', value: 64, icon: 'flame', accent: 'amber' as Accent }
];

function clearHistory(): void {
  activities.value = [];
  push('Đã xoá toàn bộ lịch sử hoạt động', { icon: 'trash', accent: 'rose' });
}

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
</script>

<template>
  <div class="space-y-6">
    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge"><AppIcon name="clock" class="h-3.5 w-3.5" /> Lịch sử hoạt động</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            Dấu chân <span class="cosmic-gradient-text">học tập</span> của bạn
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Xem lại mọi hoạt động và quay lại nơi bạn đã dừng.
          </p>
        </div>
        <button
          type="button"
          class="inline-flex shrink-0 items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-muted-foreground transition-colors hover:border-rose-400/40 hover:text-rose-300"
          @click="clearHistory"
        >
          <AppIcon name="trash" class="h-4 w-4" /> Xoá lịch sử
        </button>
      </div>
    </GlassPanel>

    <!-- STATS + CHART -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <div class="grid grid-cols-3 gap-3 lg:col-span-2 lg:grid-cols-3">
        <GlassPanel v-for="s in stats" :key="s.label" :glow="s.accent" hover padded>
          <span
            class="flex h-10 w-10 items-center justify-center rounded-sm"
            :class="accentChip[s.accent]"
          >
            <AppIcon :name="s.icon" class="h-5 w-5" />
          </span>
          <p class="mt-3 text-2xl font-bold text-foreground"><CountUp :value="s.value" /></p>
          <p class="text-[0.7rem] text-muted-foreground">{{ s.label }}</p>
        </GlassPanel>
      </div>
      <GlassPanel padded>
        <p class="text-sm font-semibold text-foreground">Phút học trong tuần</p>
        <p class="text-xs text-muted-foreground">Tổng 660 phút · trung bình 94 phút/ngày</p>
        <BarChart :data="weeklyMinutes" accent="cyan" :height="100" class="mt-3" />
      </GlassPanel>
    </div>

    <!-- FILTERS -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div class="cosmic-scroll -mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
        <button
          v-for="f in filters"
          :key="f.id"
          type="button"
          class="inline-flex shrink-0 items-center gap-1.5 rounded-sm border px-3.5 py-2 text-sm font-medium transition-all"
          :class="
            activeFilter === f.id
              ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
              : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
          "
          @click="activeFilter = f.id"
        >
          <AppIcon :name="f.icon" class="h-4 w-4" /> {{ f.label }}
        </button>
      </div>
      <div class="relative sm:w-60">
        <span
          class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
          ><AppIcon name="search" class="h-4 w-4"
        /></span>
        <input
          v-model="query"
          type="text"
          placeholder="Tìm trong lịch sử..."
          class="cosmic-input w-full !py-2 !pl-9 text-sm"
        />
      </div>
    </div>

    <!-- TIMELINE -->
    <div v-if="grouped.length" class="space-y-6">
      <div v-for="group in grouped" :key="group.label">
        <div class="mb-3 flex items-center gap-3">
          <h2 class="text-sm font-semibold text-foreground">{{ group.label }}</h2>
          <div class="h-px flex-1 from-white/[0.1] to-transparent" />
        </div>
        <ul
          class="relative space-y-2.5 before:absolute before:bottom-3 before:left-[21px] before:top-3 before:w-px before:bg-white/[0.08]"
        >
          <li
            v-for="a in group.items"
            :key="a.id"
            class="fx-lift relative flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] py-3 pl-3 pr-4"
          >
            <span
              class="z-10 flex h-9 w-9 shrink-0 items-center justify-center rounded-sm ring-4 ring-[oklch(0.13_0.025_280)]"
              :class="accentChip[typeMeta[a.type].accent]"
            >
              <AppIcon :name="typeMeta[a.type].icon" class="h-4 w-4" />
            </span>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-semibold text-foreground">{{ a.title }}</p>
              <p class="truncate text-xs text-muted-foreground">
                {{ typeMeta[a.type].label }} · {{ a.meta }}
              </p>
            </div>
            <span class="shrink-0 text-xs tabular-nums text-muted-foreground">{{ a.time }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- EMPTY -->
    <div v-else class="rounded-sm border border-dashed border-white/[0.1] py-12 text-center">
      <span
        class="mx-auto flex h-14 w-14 items-center justify-center rounded-sm bg-white/[0.04] text-muted-foreground"
      >
        <AppIcon name="clock" class="h-7 w-7" />
      </span>
      <p class="mt-3 text-sm font-medium text-foreground">Chưa có hoạt động</p>
      <p class="mt-1 text-xs text-muted-foreground">Bắt đầu học để xây dựng lịch sử của bạn.</p>
      <RouterLink
        to="/app/explore"
        class="cosmic-btn mt-4 inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm"
      >
        <AppIcon name="compass" class="h-4 w-4" /> Khám phá ngay
      </RouterLink>
    </div>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
