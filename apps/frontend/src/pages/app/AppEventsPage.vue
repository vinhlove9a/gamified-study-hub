<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Tabs from '@/components/app/Tabs.vue';
import Avatar from '@/components/app/Avatar.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type EventStatus = 'upcoming' | 'live' | 'past';
type EventType = 'webinar' | 'contest' | 'workshop' | 'live';
type FilterTab = 'upcoming' | 'live' | 'past';

interface AppEvent {
  id: number;
  title: string;
  type: EventType;
  status: EventStatus;
  host: string;
  hostAccent: Accent;
  dateLabel: string;
  day: string;
  month: string;
  dateKey: string;
  time: string;
  attendees: number;
  registered: boolean;
}

interface TypeMeta {
  label: string;
  icon: string;
  accent: Accent;
}

interface Countdown {
  days: number;
  hours: number;
  minutes: number;
  seconds: number;
}

// ─── Type metadata ───────────────────────────────────────────────────────────

const typeMeta: Record<EventType, TypeMeta> = {
  webinar: { label: 'Webinar', icon: 'users', accent: 'cyan' },
  contest: { label: 'Cuộc thi', icon: 'trophy', accent: 'amber' },
  workshop: { label: 'Workshop', icon: 'lightbulb', accent: 'violet' },
  live: { label: 'Live', icon: 'play', accent: 'magenta' },
};

const statusMeta: Record<EventStatus, { label: string }> = {
  upcoming: { label: 'Sắp diễn ra' },
  live: { label: 'Đang LIVE' },
  past: { label: 'Đã kết thúc' },
};

// ─── Live clock ──────────────────────────────────────────────────────────────

const now = ref<number>(Date.now());
let clockId: number | null = null;

// Featured event target: 5 days, 8 hours from mount.
const featuredTarget = ref<number>(Date.now());

const featuredCountdown = computed<Countdown>(() => {
  const diff = Math.max(0, featuredTarget.value - now.value);
  const totalSeconds = Math.floor(diff / 1000);
  return {
    days: Math.floor(totalSeconds / 86400),
    hours: Math.floor((totalSeconds % 86400) / 3600),
    minutes: Math.floor((totalSeconds % 3600) / 60),
    seconds: totalSeconds % 60,
  };
});

function pad(n: number): string {
  return String(n).padStart(2, '0');
}

const featuredRegistered = ref<boolean>(false);
const featuredAttendees = ref<number>(1840);

function toggleFeaturedRegister(): void {
  featuredRegistered.value = !featuredRegistered.value;
  featuredAttendees.value += featuredRegistered.value ? 1 : -1;
}

// ─── Events ────────────────────────────────────────────────────────────────

const events = ref<AppEvent[]>([
  {
    id: 1,
    title: 'Bứt phá điểm số mùa thi cùng chuyên gia',
    type: 'webinar',
    status: 'upcoming',
    host: 'Nguyễn Minh Quân',
    hostAccent: 'cyan',
    dateLabel: 'Thứ Năm, 02/07',
    day: '02',
    month: 'TH7',
    dateKey: '2026-07-02',
    time: '19:30',
    attendees: 642,
    registered: false,
  },
  {
    id: 2,
    title: 'Đấu trường Toán học — Vòng chung kết',
    type: 'contest',
    status: 'live',
    host: 'Ban tổ chức StudyHub',
    hostAccent: 'amber',
    dateLabel: 'Hôm nay',
    day: '25',
    month: 'TH6',
    dateKey: '2026-06-25',
    time: '20:00',
    attendees: 1280,
    registered: true,
  },
  {
    id: 3,
    title: 'Workshop: Sơ đồ tư duy cho người bận rộn',
    type: 'workshop',
    status: 'upcoming',
    host: 'Đỗ Khánh Linh',
    hostAccent: 'violet',
    dateLabel: 'Thứ Năm, 02/07',
    day: '02',
    month: 'TH7',
    dateKey: '2026-07-02',
    time: '14:00',
    attendees: 318,
    registered: false,
  },
  {
    id: 4,
    title: 'Live coding: Giải thuật phỏng vấn cùng senior',
    type: 'live',
    status: 'live',
    host: 'Trần Đức Anh',
    hostAccent: 'magenta',
    dateLabel: 'Hôm nay',
    day: '25',
    month: 'TH6',
    dateKey: '2026-06-25',
    time: '21:00',
    attendees: 904,
    registered: false,
  },
  {
    id: 5,
    title: 'Hỏi đáp luyện thi IELTS Speaking band 7+',
    type: 'webinar',
    status: 'upcoming',
    host: 'Lê Thu Hà',
    hostAccent: 'green',
    dateLabel: 'Thứ Bảy, 04/07',
    day: '04',
    month: 'TH7',
    dateKey: '2026-07-04',
    time: '10:00',
    attendees: 511,
    registered: false,
  },
  {
    id: 6,
    title: 'Workshop: Phương pháp Feynman thực chiến',
    type: 'workshop',
    status: 'upcoming',
    host: 'Đỗ Khánh Linh',
    hostAccent: 'cyan',
    dateLabel: 'Thứ Bảy, 04/07',
    day: '04',
    month: 'TH7',
    dateKey: '2026-07-04',
    time: '15:30',
    attendees: 276,
    registered: false,
  },
  {
    id: 7,
    title: 'Hội thảo định hướng nghề khoa học dữ liệu',
    type: 'webinar',
    status: 'past',
    host: 'Phạm Hải Yến',
    hostAccent: 'violet',
    dateLabel: 'Thứ Hai, 16/06',
    day: '16',
    month: 'TH6',
    dateKey: '2026-06-16',
    time: '19:00',
    attendees: 1432,
    registered: true,
  },
  {
    id: 8,
    title: 'Cuộc thi sáng tạo ghi nhớ siêu tốc',
    type: 'contest',
    status: 'past',
    host: 'Ban tổ chức StudyHub',
    hostAccent: 'amber',
    dateLabel: 'Thứ Sáu, 13/06',
    day: '13',
    month: 'TH6',
    dateKey: '2026-06-13',
    time: '20:30',
    attendees: 2104,
    registered: false,
  },
]);

function toggleRegister(id: number): void {
  const ev = events.value.find((e) => e.id === id);
  if (!ev || ev.status === 'past') return;
  ev.registered = !ev.registered;
  ev.attendees += ev.registered ? 1 : -1;
}

// ─── Filter tabs ──────────────────────────────────────────────────────────

const activeTab = ref<FilterTab>('upcoming');

const tabs = computed<{ id: string; label: string; badge: number }[]>(() => [
  { id: 'upcoming', label: 'Sắp diễn ra', badge: events.value.filter((e) => e.status === 'upcoming').length },
  { id: 'live', label: 'Đang live', badge: events.value.filter((e) => e.status === 'live').length },
  { id: 'past', label: 'Đã qua', badge: events.value.filter((e) => e.status === 'past').length },
]);

const filteredEvents = computed<AppEvent[]>(() =>
  events.value.filter((e) => e.status === activeTab.value),
);

// Group filtered events by date heading (only meaningful for upcoming).
const groupedEvents = computed<{ key: string; label: string; items: AppEvent[] }[]>(() => {
  const groups = new Map<string, { label: string; items: AppEvent[] }>();
  for (const ev of filteredEvents.value) {
    const existing = groups.get(ev.dateKey);
    if (existing) {
      existing.items.push(ev);
    } else {
      groups.set(ev.dateKey, { label: ev.dateLabel, items: [ev] });
    }
  }
  return Array.from(groups.entries())
    .map(([key, value]) => ({ key, label: value.label, items: value.items }))
    .sort((a, b) => a.key.localeCompare(b.key));
});

// ─── My calendar (registered events) ─────────────────────────────────────────

const myCalendar = computed<AppEvent[]>(() =>
  events.value
    .filter((e) => e.registered && e.status !== 'past')
    .sort((a, b) => a.dateKey.localeCompare(b.dateKey)),
);

// ─── KPI ──────────────────────────────────────────────────────────────────

const kpis = computed<{ label: string; value: string; icon: string; accent: Accent }[]>(() => [
  { label: 'Sự kiện sắp tới', value: String(events.value.filter((e) => e.status === 'upcoming').length), icon: 'calendar', accent: 'cyan' },
  { label: 'Đang phát trực tiếp', value: String(events.value.filter((e) => e.status === 'live').length), icon: 'play', accent: 'magenta' },
  { label: 'Bạn đã đăng ký', value: String(events.value.filter((e) => e.registered).length), icon: 'check-circle', accent: 'green' },
  { label: 'Tổng người tham gia', value: '8.5k', icon: 'users', accent: 'amber' },
]);

// ─── Lifecycle ───────────────────────────────────────────────────────────────

onMounted(() => {
  featuredTarget.value = Date.now() + (5 * 86400 + 8 * 3600 + 42 * 60 + 17) * 1000;
  clockId = window.setInterval(() => {
    now.value = Date.now();
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

const typeBadge: Record<Accent, string> = {
  cyan: 'border-cyan-400/30 bg-cyan-500/10 text-cyan-200',
  magenta: 'border-fuchsia-400/30 bg-fuchsia-500/10 text-fuchsia-200',
  green: 'border-emerald-400/30 bg-emerald-500/10 text-emerald-200',
  violet: 'border-violet-400/30 bg-violet-500/10 text-violet-200',
  amber: 'border-amber-400/30 bg-amber-500/10 text-amber-200',
};

const countdownUnits = computed<{ label: string; value: string }[]>(() => [
  { label: 'Ngày', value: pad(featuredCountdown.value.days) },
  { label: 'Giờ', value: pad(featuredCountdown.value.hours) },
  { label: 'Phút', value: pad(featuredCountdown.value.minutes) },
  { label: 'Giây', value: pad(featuredCountdown.value.seconds) },
]);
</script>

<template>
  <div class="space-y-6">
    <!-- ───────────────── PAGE HEADER ───────────────── -->
    <div>
      <p class="cosmic-badge">Lịch sự kiện cộng đồng</p>
      <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
        <span class="cosmic-gradient-text">Sự kiện</span> sắp diễn ra
      </h1>
      <p class="mt-1 text-sm text-muted-foreground">
        Webinar, cuộc thi, workshop và livestream cùng các chuyên gia hàng đầu.
      </p>
    </div>

    <!-- ───────────────── FEATURED EVENT HERO ───────────────── -->
    <GlassPanel glow="magenta" class="event-hero relative p-6 sm:p-8">
      <div class="relative z-10 grid grid-cols-1 gap-6 lg:grid-cols-2 lg:items-center">
        <div>
          <span class="inline-flex items-center gap-1.5 rounded-full bg-fuchsia-500/15 px-3 py-1 text-xs font-semibold text-fuchsia-200">
            <AppIcon name="star" class="h-3.5 w-3.5" /> Sự kiện nổi bật
          </span>
          <h2 class="mt-3 text-xl font-bold text-foreground sm:text-2xl">
            Đại hội học tập 2026: Ngày hội tri thức trực tuyến
          </h2>
          <p class="mt-2 text-sm text-muted-foreground">
            Hơn 20 diễn giả, 12 phiên chuyên đề và phần thưởng lên tới 1.000.000 coin cho học viên xuất sắc.
          </p>

          <div class="mt-4 flex flex-wrap items-center gap-4">
            <div class="flex items-center gap-2">
              <Avatar name="StudyHub Team" accent="magenta" :size="36" />
              <div>
                <p class="text-sm font-medium text-foreground">Ban tổ chức StudyHub</p>
                <p class="text-xs text-muted-foreground">{{ featuredAttendees.toLocaleString('vi-VN') }} người quan tâm</p>
              </div>
            </div>
          </div>

          <button
            type="button"
            class="mt-5 inline-flex items-center gap-2 rounded-xl px-5 py-3 text-sm font-semibold transition-all"
            :class="featuredRegistered
              ? 'border border-emerald-400/40 bg-emerald-500/15 text-emerald-200'
              : 'cosmic-btn'"
            @click="toggleFeaturedRegister"
          >
            <AppIcon :name="featuredRegistered ? 'check-circle' : 'bell'" class="h-5 w-5" />
            {{ featuredRegistered ? 'Đã đăng ký nhắc nhở' : 'Đăng ký tham gia' }}
          </button>
        </div>

        <!-- Countdown -->
        <div class="rounded-2xl border border-white/[0.08] bg-white/[0.03] p-5">
          <p class="text-center text-xs uppercase tracking-wider text-muted-foreground">Bắt đầu sau</p>
          <div class="mt-3 grid grid-cols-4 gap-2 sm:gap-3">
            <div
              v-for="unit in countdownUnits"
              :key="unit.label"
              class="rounded-xl border border-white/[0.06] bg-white/[0.03] py-3 text-center"
            >
              <p class="text-2xl font-bold tabular-nums text-foreground sm:text-3xl">{{ unit.value }}</p>
              <p class="mt-0.5 text-[0.65rem] uppercase text-muted-foreground">{{ unit.label }}</p>
            </div>
          </div>
          <p class="mt-3 text-center text-xs text-muted-foreground">
            <AppIcon name="calendar" class="mr-1 inline h-3.5 w-3.5" /> 30/06/2026 · 19:00 (GMT+7)
          </p>
        </div>
      </div>
    </GlassPanel>

    <!-- ───────────────── KPI TILES ───────────────── -->
    <section class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <GlassPanel v-for="kpi in kpis" :key="kpi.label" :glow="kpi.accent" hover padded>
        <div class="flex items-center gap-3">
          <span class="flex h-11 w-11 items-center justify-center rounded-xl" :class="accentChip[kpi.accent]">
            <AppIcon :name="kpi.icon" class="h-5 w-5" />
          </span>
          <div>
            <p class="text-xl font-bold tabular-nums text-foreground">{{ kpi.value }}</p>
            <p class="text-[0.7rem] text-muted-foreground">{{ kpi.label }}</p>
          </div>
        </div>
      </GlassPanel>
    </section>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- ───────────────── EVENT LIST ───────────────── -->
      <div class="space-y-5 lg:col-span-2">
        <Tabs :tabs="tabs" :model-value="activeTab" @update:model-value="(id: string) => (activeTab = id as FilterTab)" />

        <div v-for="group in groupedEvents" :key="group.key" class="space-y-3">
          <h3 class="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
            <AppIcon name="calendar" class="h-4 w-4 text-cyan-300" />
            {{ group.label }}
          </h3>

          <article
            v-for="ev in group.items"
            :key="ev.id"
            class="flex flex-col gap-4 rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:border-white/[0.12] sm:flex-row sm:items-center"
          >
            <!-- date badge -->
            <div class="flex w-14 shrink-0 flex-col items-center justify-center rounded-xl border border-white/[0.08] bg-white/[0.03] py-2">
              <span class="text-lg font-bold tabular-nums text-foreground">{{ ev.day }}</span>
              <span class="text-[0.6rem] uppercase tracking-wide text-muted-foreground">{{ ev.month }}</span>
            </div>

            <!-- info -->
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <span
                  class="inline-flex items-center gap-1 rounded-md border px-2 py-0.5 text-[0.65rem] font-semibold"
                  :class="typeBadge[typeMeta[ev.type].accent]"
                >
                  <AppIcon :name="typeMeta[ev.type].icon" class="h-3 w-3" />
                  {{ typeMeta[ev.type].label }}
                </span>
                <span
                  class="inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-[0.65rem] font-semibold"
                  :class="ev.status === 'live'
                    ? 'bg-fuchsia-500/15 text-fuchsia-200 pulse-glow-magenta'
                    : ev.status === 'upcoming'
                      ? 'bg-cyan-500/10 text-cyan-200'
                      : 'bg-white/[0.06] text-muted-foreground'"
                >
                  <span
                    v-if="ev.status === 'live'"
                    class="h-1.5 w-1.5 animate-pulse rounded-full bg-fuchsia-300"
                  />
                  {{ statusMeta[ev.status].label }}
                </span>
              </div>

              <h4 class="mt-1.5 text-sm font-semibold leading-snug text-foreground">{{ ev.title }}</h4>

              <div class="mt-2 flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                <span class="inline-flex items-center gap-1.5">
                  <Avatar :name="ev.host" :accent="ev.hostAccent" :size="20" />
                  {{ ev.host }}
                </span>
                <span class="inline-flex items-center gap-1">
                  <AppIcon name="clock" class="h-3.5 w-3.5" /> {{ ev.time }}
                </span>
                <span class="inline-flex items-center gap-1">
                  <AppIcon name="users" class="h-3.5 w-3.5" /> {{ ev.attendees.toLocaleString('vi-VN') }}
                </span>
              </div>
            </div>

            <!-- register -->
            <button
              v-if="ev.status !== 'past'"
              type="button"
              class="inline-flex shrink-0 items-center justify-center gap-1.5 rounded-xl border px-4 py-2 text-sm font-semibold transition-colors"
              :class="ev.registered
                ? 'border-emerald-400/40 bg-emerald-500/15 text-emerald-200'
                : 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200 hover:bg-cyan-500/25'"
              @click="toggleRegister(ev.id)"
            >
              <AppIcon :name="ev.registered ? 'check' : 'plus'" class="h-4 w-4" />
              {{ ev.registered ? 'Đã đăng ký' : ev.status === 'live' ? 'Vào ngay' : 'Đăng ký' }}
            </button>
            <span
              v-else
              class="inline-flex shrink-0 items-center gap-1.5 rounded-xl border border-white/[0.08] px-4 py-2 text-sm text-muted-foreground"
            >
              <AppIcon name="play" class="h-4 w-4" /> Xem lại
            </span>
          </article>
        </div>

        <p v-if="filteredEvents.length === 0" class="rounded-2xl border border-white/[0.06] bg-white/[0.02] py-10 text-center text-sm text-muted-foreground">
          Hiện chưa có sự kiện nào trong mục này.
        </p>
      </div>

      <!-- ───────────────── MY CALENDAR ───────────────── -->
      <div>
        <SectionPanel
          title="Lịch của tôi"
          :subtitle="`${myCalendar.length} sự kiện đã đăng ký`"
          icon="bookmark"
          accent="green"
        >
          <ul v-if="myCalendar.length > 0" class="space-y-3">
            <li
              v-for="ev in myCalendar"
              :key="ev.id"
              class="flex items-center gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] px-3 py-2.5"
            >
              <span
                class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl"
                :class="accentChip[typeMeta[ev.type].accent]"
              >
                <AppIcon :name="typeMeta[ev.type].icon" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ ev.title }}</p>
                <p class="text-xs text-muted-foreground">{{ ev.dateLabel }} · {{ ev.time }}</p>
              </div>
              <span
                v-if="ev.status === 'live'"
                class="inline-flex items-center gap-1 rounded-md bg-fuchsia-500/15 px-1.5 py-0.5 text-[0.6rem] font-semibold text-fuchsia-200 pulse-glow-magenta"
              >
                LIVE
              </span>
            </li>
          </ul>
          <div v-else class="py-6 text-center">
            <span class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-white/[0.04] text-muted-foreground">
              <AppIcon name="calendar" class="h-6 w-6" />
            </span>
            <p class="mt-3 text-sm text-muted-foreground">Bạn chưa đăng ký sự kiện nào.</p>
            <p class="text-xs text-muted-foreground/70">Đăng ký để nhận nhắc nhở nhé!</p>
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
.event-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 1rem;
  background: radial-gradient(circle at 85% 15%, oklch(0.68 0.22 350 / 12%), transparent 55%);
  pointer-events: none;
}
</style>
