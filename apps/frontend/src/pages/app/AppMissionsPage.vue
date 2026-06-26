<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import MissionItem from '@/components/app/MissionItem.vue';
import Tabs from '@/components/app/Tabs.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

// ─── Interfaces ───────────────────────────────────────────────────────────────

interface DayLoginReward {
  day: number;
  label: string;
  reward: number;
  icon: string;
  status: 'claimed' | 'claimable' | 'locked';
}

interface DailyMission {
  title: string;
  description: string;
  reward: number;
  current: number;
  total: number;
  status: 'done' | 'active' | 'locked';
}

interface WeeklyMission {
  title: string;
  description: string;
  reward: number;
  current: number;
  total: number;
  status: 'done' | 'active' | 'locked';
}

interface MilestoneMission {
  icon: string;
  title: string;
  description: string;
  current: number;
  target: number;
  reward: number;
  accent: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
  done: boolean;
}

interface StreakDay {
  label: string;
  active: boolean;
  today: boolean;
}

// ─── State ────────────────────────────────────────────────────────────────────

const activeTab = ref<string>('daily');

// ─── Tab config ───────────────────────────────────────────────────────────────

const tabs = [
  { id: 'daily', label: 'Hằng ngày', badge: '3' },
  { id: 'weekly', label: 'Hằng tuần', badge: '2' },
  { id: 'milestone', label: 'Cột mốc' },
];

// ─── Stats ────────────────────────────────────────────────────────────────────

const pageStats = [
  {
    label: 'Nhiệm vụ hôm nay',
    value: '2/4',
    icon: 'target',
    accent: 'cyan' as const,
    caption: 'Còn 2 nhiệm vụ nữa',
    sparkline: [1, 2, 1, 3, 2, 3, 2],
  },
  {
    label: 'Coin kiếm hôm nay',
    value: '80',
    unit: 'coin',
    icon: 'coin',
    accent: 'amber' as const,
    trend: 14,
    caption: '+80 so với hôm qua',
    sparkline: [20, 30, 50, 40, 70, 60, 80],
  },
  {
    label: 'Chuỗi đăng nhập',
    value: '7',
    unit: 'ngày',
    icon: 'flame',
    accent: 'magenta' as const,
    trend: 0,
    caption: 'Kỷ lục: 21 ngày',
    sparkline: [1, 2, 3, 4, 5, 6, 7],
  },
  {
    label: 'Tỉ lệ hoàn thành',
    value: '84',
    unit: '%',
    icon: 'check-circle',
    accent: 'green' as const,
    trend: 5,
    caption: 'Tuần này',
    sparkline: [60, 65, 70, 72, 78, 80, 84],
  },
];

// ─── Daily login rewards ───────────────────────────────────────────────────────

const loginRewards: DayLoginReward[] = [
  { day: 1, label: 'Ngày 1', reward: 20,  icon: 'coin',  status: 'claimed'   },
  { day: 2, label: 'Ngày 2', reward: 30,  icon: 'coin',  status: 'claimed'   },
  { day: 3, label: 'Ngày 3', reward: 40,  icon: 'gift',  status: 'claimed'   },
  { day: 4, label: 'Ngày 4', reward: 50,  icon: 'coin',  status: 'claimed'   },
  { day: 5, label: 'Ngày 5', reward: 60,  icon: 'coin',  status: 'claimed'   },
  { day: 6, label: 'Ngày 6', reward: 80,  icon: 'gift',  status: 'claimable' },
  { day: 7, label: 'Ngày 7', reward: 150, icon: 'gem',   status: 'locked'    },
];

// ─── Daily missions ────────────────────────────────────────────────────────────

const dailyMissions: DailyMission[] = [
  {
    title: 'Đọc 3 tài liệu mới',
    description: 'Khám phá tài liệu gợi ý hôm nay',
    reward: 50,
    current: 3,
    total: 3,
    status: 'done',
  },
  {
    title: 'Hoàn thành 1 bài quiz',
    description: 'Củng cố kiến thức đã học',
    reward: 30,
    current: 1,
    total: 1,
    status: 'done',
  },
  {
    title: 'Học liên tục 30 phút',
    description: 'Duy trì sự tập trung liên tục',
    reward: 40,
    current: 18,
    total: 30,
    status: 'active',
  },
  {
    title: 'Chia sẻ 1 tài liệu',
    description: 'Chia sẻ tài liệu với bạn bè',
    reward: 25,
    current: 0,
    total: 1,
    status: 'active',
  },
  {
    title: 'Mở khoá bộ sưu tập VIP',
    description: 'Cần đạt cấp độ 8 để mở khoá',
    reward: 100,
    current: 0,
    total: 1,
    status: 'locked',
  },
];

const dailyDone = computed(() => dailyMissions.filter((m) => m.status === 'done').length);

// ─── Weekly missions ───────────────────────────────────────────────────────────

const weeklyMissions: WeeklyMission[] = [
  {
    title: 'Đọc 15 tài liệu trong tuần',
    description: 'Mở rộng kiến thức đa lĩnh vực',
    reward: 200,
    current: 9,
    total: 15,
    status: 'active',
  },
  {
    title: 'Hoàn thành 5 bài quiz',
    description: 'Kiểm tra độ nắm vững bài học',
    reward: 150,
    current: 5,
    total: 5,
    status: 'done',
  },
  {
    title: 'Học đủ 5 ngày trong tuần',
    description: 'Duy trì thói quen học mỗi ngày',
    reward: 300,
    current: 4,
    total: 5,
    status: 'active',
  },
  {
    title: 'Đăng 2 tài liệu lên cộng đồng',
    description: 'Đóng góp cho cộng đồng học tập',
    reward: 180,
    current: 0,
    total: 2,
    status: 'locked',
  },
];

const weeklyDone = computed(() => weeklyMissions.filter((m) => m.status === 'done').length);

// ─── Milestone missions ────────────────────────────────────────────────────────

const milestoneMissions: MilestoneMission[] = [
  {
    icon: 'book-open',
    title: 'Nhà khám phá tri thức',
    description: 'Đọc tổng cộng 100 tài liệu trên hệ thống',
    current: 48,
    target: 100,
    reward: 500,
    accent: 'cyan',
    done: false,
  },
  {
    icon: 'flame',
    title: 'Kiên trì bất khuất',
    description: 'Duy trì chuỗi học 30 ngày liên tiếp',
    current: 7,
    target: 30,
    reward: 800,
    accent: 'magenta',
    done: false,
  },
  {
    icon: 'crown',
    title: 'Huyền thoại học đường',
    description: 'Đạt cấp độ 10 trên hệ thống',
    current: 7,
    target: 10,
    reward: 1000,
    accent: 'amber',
    done: false,
  },
  {
    icon: 'users',
    title: 'Người tiên phong',
    description: 'Đăng đủ 20 tài liệu lên cộng đồng',
    current: 20,
    target: 20,
    reward: 600,
    accent: 'green',
    done: true,
  },
];

// ─── Streak strip ─────────────────────────────────────────────────────────────

const streakDays: StreakDay[] = [
  { label: 'T2', active: true,  today: false },
  { label: 'T3', active: true,  today: false },
  { label: 'T4', active: true,  today: false },
  { label: 'T5', active: false, today: false },
  { label: 'T6', active: true,  today: false },
  { label: 'T7', active: true,  today: false },
  { label: 'CN', active: true,  today: true  },
];

const streakCount = computed(() => streakDays.filter((d) => d.active).length);

// ─── Accent helpers ────────────────────────────────────────────────────────────

const milestoneAccentClasses: Record<string, string> = {
  cyan:    'bg-cyan-500/10 border-cyan-400/20 text-cyan-300',
  magenta: 'bg-fuchsia-500/10 border-fuchsia-400/20 text-fuchsia-300',
  amber:   'bg-amber-500/10 border-amber-400/20 text-amber-300',
  green:   'bg-emerald-500/10 border-emerald-400/20 text-emerald-300',
  violet:  'bg-violet-500/10 border-violet-400/20 text-violet-300',
};

const milestoneIconClasses: Record<string, string> = {
  cyan:    'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  amber:   'bg-amber-500/15 text-amber-300',
  green:   'bg-emerald-500/15 text-emerald-300',
  violet:  'bg-violet-500/15 text-violet-300',
};
</script>

<template>
  <div class="space-y-6">
    <!-- Page header -->
    <GlassPanel glow="magenta" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Nhiệm vụ &amp; Phần thưởng</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Nhiệm vụ</span>
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Hoàn thành nhiệm vụ để nhận coin và XP mỗi ngày
          </p>
        </div>
        <div class="flex shrink-0 items-center gap-3">
          <div class="flex items-center gap-2 rounded-xl border border-amber-400/20 bg-amber-500/10 px-4 py-2.5">
            <AppIcon name="coin" class="h-5 w-5 text-amber-300" />
            <div>
              <p class="text-sm font-bold leading-none text-amber-200">1.250</p>
              <p class="mt-0.5 text-[0.65rem] text-muted-foreground">Số dư coin</p>
            </div>
          </div>
          <div class="flex items-center gap-2 rounded-xl border border-fuchsia-400/20 bg-fuchsia-500/10 px-4 py-2.5">
            <AppIcon name="flame" class="h-5 w-5 text-fuchsia-300" />
            <div>
              <p class="text-sm font-bold leading-none text-fuchsia-200">7 ngày</p>
              <p class="mt-0.5 text-[0.65rem] text-muted-foreground">Chuỗi đăng nhập</p>
            </div>
          </div>
        </div>
      </div>
    </GlassPanel>

    <!-- KPI stats row -->
    <section class="grid grid-cols-2 gap-4 md:grid-cols-4">
      <StatPanel
        v-for="stat in pageStats"
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

    <!-- Daily login rewards -->
    <SectionPanel
      title="Phần thưởng đăng nhập hằng ngày"
      subtitle="Đăng nhập mỗi ngày để nhận thêm coin"
      icon="gift"
      accent="amber"
    >
      <div class="grid grid-cols-7 gap-2 sm:gap-3">
        <div
          v-for="day in loginRewards"
          :key="day.day"
          class="relative flex flex-col items-center gap-1.5 rounded-xl border px-1.5 py-3 text-center transition-all"
          :class="{
            'border-emerald-400/30 bg-emerald-500/[0.07]': day.status === 'claimed',
            'border-cyan-400/50 bg-cyan-500/[0.12] ring-1 ring-cyan-400/30': day.status === 'claimable',
            'border-white/[0.06] bg-white/[0.02] opacity-50': day.status === 'locked',
          }"
        >
          <!-- claimed check overlay -->
          <span
            v-if="day.status === 'claimed'"
            class="absolute right-1.5 top-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-emerald-500/20"
          >
            <AppIcon name="check" class="h-2.5 w-2.5 text-emerald-400" />
          </span>

          <span class="text-[0.6rem] font-semibold uppercase tracking-wide text-muted-foreground">
            {{ day.label }}
          </span>

          <span
            class="flex h-9 w-9 items-center justify-center rounded-xl"
            :class="{
              'bg-emerald-500/15 text-emerald-300': day.status === 'claimed',
              'bg-cyan-500/20 text-cyan-300': day.status === 'claimable',
              'bg-white/[0.04] text-muted-foreground': day.status === 'locked',
            }"
          >
            <AppIcon :name="day.icon" class="h-5 w-5" />
          </span>

          <span
            class="inline-flex items-center gap-0.5 text-xs font-bold"
            :class="{
              'text-emerald-300': day.status === 'claimed',
              'text-amber-300': day.status === 'claimable',
              'text-muted-foreground': day.status === 'locked',
            }"
          >
            <AppIcon name="coin" class="h-3 w-3" />{{ day.reward }}
          </span>

          <button
            v-if="day.status === 'claimable'"
            type="button"
            class="cosmic-btn mt-0.5 rounded-lg px-2.5 py-1 text-[0.65rem] font-semibold"
          >
            Nhận
          </button>

          <AppIcon
            v-if="day.status === 'locked'"
            name="lock"
            class="mt-0.5 h-3.5 w-3.5 text-muted-foreground/50"
          />
        </div>
      </div>
    </SectionPanel>

    <!-- Mission tabs -->
    <div class="space-y-4">
      <Tabs v-model="activeTab" :tabs="tabs" />

      <!-- Hằng ngày -->
      <div v-show="activeTab === 'daily'">
        <SectionPanel
          title="Nhiệm vụ hằng ngày"
          subtitle="Làm mới lúc 00:00 mỗi ngày"
          icon="target"
          accent="cyan"
        >
          <template #header-actions>
            <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs font-semibold text-muted-foreground">
              {{ dailyDone }}/{{ dailyMissions.length }} hoàn thành
            </span>
          </template>
          <div class="space-y-2.5">
            <MissionItem
              v-for="mission in dailyMissions"
              :key="mission.title"
              :title="mission.title"
              :description="mission.description"
              :reward="mission.reward"
              :current="mission.current"
              :total="mission.total"
              :status="mission.status"
            />
          </div>
        </SectionPanel>
      </div>

      <!-- Hằng tuần -->
      <div v-show="activeTab === 'weekly'">
        <SectionPanel
          title="Nhiệm vụ hằng tuần"
          subtitle="Làm mới vào thứ Hai mỗi tuần"
          icon="calendar"
          accent="violet"
        >
          <template #header-actions>
            <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs font-semibold text-muted-foreground">
              {{ weeklyDone }}/{{ weeklyMissions.length }} hoàn thành
            </span>
          </template>
          <div class="space-y-2.5">
            <MissionItem
              v-for="mission in weeklyMissions"
              :key="mission.title"
              :title="mission.title"
              :description="mission.description"
              :reward="mission.reward"
              :current="mission.current"
              :total="mission.total"
              :status="mission.status"
            />
          </div>
        </SectionPanel>
      </div>

      <!-- Cột mốc -->
      <div v-show="activeTab === 'milestone'">
        <SectionPanel
          title="Cột mốc thành tựu"
          subtitle="Mục tiêu dài hạn, phần thưởng lớn"
          icon="trophy"
          accent="amber"
        >
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div
              v-for="ms in milestoneMissions"
              :key="ms.title"
              class="relative flex flex-col gap-4 rounded-xl border p-4 transition-all hover:border-white/[0.12]"
              :class="[
                ms.done
                  ? 'border-emerald-400/30 bg-emerald-500/[0.05]'
                  : 'border-white/[0.06] bg-white/[0.02]',
              ]"
            >
              <!-- Done badge -->
              <span
                v-if="ms.done"
                class="absolute right-3 top-3 flex items-center gap-1 rounded-full bg-emerald-500/15 px-2 py-0.5 text-[0.65rem] font-semibold text-emerald-300"
              >
                <AppIcon name="check-circle" class="h-3 w-3" />Đã đạt
              </span>

              <div class="flex items-start gap-3">
                <span
                  class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl"
                  :class="milestoneIconClasses[ms.accent]"
                >
                  <AppIcon :name="ms.icon" class="h-6 w-6" />
                </span>
                <div class="min-w-0 flex-1">
                  <p class="font-semibold text-foreground">{{ ms.title }}</p>
                  <p class="mt-0.5 text-xs text-muted-foreground">{{ ms.description }}</p>
                </div>
              </div>

              <div>
                <div class="mb-1.5 flex items-center justify-between text-xs">
                  <span class="text-muted-foreground">
                    Tiến độ: <span class="font-semibold text-foreground">{{ ms.current }}/{{ ms.target }}</span>
                  </span>
                  <span class="tabular-nums text-muted-foreground">
                    {{ Math.round((ms.current / ms.target) * 100) }}%
                  </span>
                </div>
                <ProgressBar
                  :value="Math.round((ms.current / ms.target) * 100)"
                  :accent="ms.accent === 'magenta' ? 'magenta' : ms.accent"
                />
              </div>

              <div class="flex items-center justify-between">
                <span
                  class="inline-flex items-center gap-1 rounded-full border px-2.5 py-1 text-xs font-semibold"
                  :class="milestoneAccentClasses[ms.accent]"
                >
                  <AppIcon name="coin" class="h-3.5 w-3.5" />
                  {{ ms.reward }} coin
                </span>
                <button
                  v-if="!ms.done"
                  type="button"
                  class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-3 py-1.5 text-xs font-medium text-foreground transition-colors hover:border-cyan-400/40"
                >
                  Chi tiết <AppIcon name="arrow-right" class="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
          </div>
        </SectionPanel>
      </div>
    </div>

    <!-- Streak strip -->
    <SectionPanel
      title="Chuỗi học tuần này"
      :subtitle="`${streakCount}/7 ngày hoạt động`"
      icon="flame"
      accent="magenta"
    >
      <div class="flex items-center gap-3 sm:gap-4">
        <div class="flex flex-1 items-center gap-2 sm:gap-3">
          <div
            v-for="day in streakDays"
            :key="day.label"
            class="flex flex-1 flex-col items-center gap-2 rounded-xl border px-1 py-3 transition-all"
            :class="{
              'border-fuchsia-400/40 bg-fuchsia-500/[0.12]': day.active && !day.today,
              'border-cyan-400/50 bg-cyan-500/[0.15] ring-1 ring-cyan-400/30': day.today,
              'border-white/[0.06] bg-white/[0.02] opacity-40': !day.active,
            }"
          >
            <span class="text-[0.6rem] font-semibold uppercase tracking-wide text-muted-foreground">
              {{ day.label }}
            </span>
            <AppIcon
              name="flame"
              class="h-5 w-5"
              :class="{
                'text-fuchsia-400': day.active && !day.today,
                'text-cyan-300': day.today,
                'text-muted-foreground/40': !day.active,
              }"
            />
            <span
              v-if="day.today"
              class="text-[0.55rem] font-semibold uppercase tracking-wider text-cyan-400"
            >
              Hôm nay
            </span>
          </div>
        </div>

        <div class="hidden shrink-0 flex-col items-center gap-1 sm:flex">
          <span class="text-3xl font-bold text-foreground">{{ streakCount }}</span>
          <span class="text-xs text-muted-foreground">ngày liên tiếp</span>
          <span class="mt-1 rounded-full bg-fuchsia-500/15 px-2.5 py-1 text-[0.65rem] font-semibold text-fuchsia-300">
            Kỷ lục: 21 ngày
          </span>
        </div>
      </div>
    </SectionPanel>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
