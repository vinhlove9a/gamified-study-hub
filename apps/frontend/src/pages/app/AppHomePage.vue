<script setup lang="ts">
import { computed } from 'vue';
import { useAuthSession } from '@/features/auth/authSession';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import DocumentCard from '@/components/app/DocumentCard.vue';
import MissionItem from '@/components/app/MissionItem.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import BarChart from '@/components/app/BarChart.vue';
import BadgeTile from '@/components/app/BadgeTile.vue';
import Avatar from '@/components/app/Avatar.vue';

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

const dailyGoal = { done: 35, target: 60 };
const dailyPct = Math.round((dailyGoal.done / dailyGoal.target) * 100);

const level = { current: 7, xp: 1240, next: 2000 };
const xpPct = Math.round((level.xp / level.next) * 100);

const quickActions = [
  { icon: 'upload', label: 'Tải tài liệu' },
  { icon: 'folder', label: 'Tạo bộ sưu tập' },
  { icon: 'target', label: 'Đặt mục tiêu' },
  { icon: 'users', label: 'Tìm bạn học' }
];

const stats = [
  { label: 'Số dư coin', value: '1.250', icon: 'coin', accent: 'amber' as const, trend: 8, caption: '+50 hôm nay', sparkline: [900, 980, 1010, 1080, 1120, 1180, 1210, 1250] },
  { label: 'Chuỗi học', value: '12', unit: 'ngày', icon: 'flame', accent: 'magenta' as const, trend: 4, caption: 'Kỷ lục: 21 ngày', sparkline: [6, 7, 8, 9, 10, 11, 11, 12] },
  { label: 'Tài liệu đã đọc', value: '48', icon: 'book-open', accent: 'cyan' as const, trend: 12, caption: '+3 tuần này', sparkline: [30, 33, 36, 38, 41, 44, 46, 48] },
  { label: 'Nhiệm vụ', value: '27', icon: 'check-circle', accent: 'green' as const, trend: 6, caption: '84% hoàn thành', sparkline: [12, 15, 17, 19, 22, 24, 25, 27] },
  { label: 'Điểm XP', value: '1.240', icon: 'zap', accent: 'violet' as const, trend: 15, caption: 'Cấp 7', sparkline: [400, 560, 700, 820, 960, 1050, 1160, 1240] },
  { label: 'Xếp hạng', value: '#3', icon: 'trophy', accent: 'amber' as const, trend: 2, caption: 'Trong nhóm bạn bè', sparkline: [9, 8, 7, 6, 5, 4, 4, 3] }
];

const continueDocs = [
  { title: 'Giải tích 1 — Giới hạn & Liên tục', category: 'Toán học', meta: 'Chương 3 · 24 trang', progress: 62, readMinutes: 12, tone: 'cyan' as const },
  { title: 'Ngữ pháp tiếng Anh B2', category: 'Ngoại ngữ', meta: 'Unit 7 · Bài tập', progress: 38, readMinutes: 18, tone: 'green' as const },
  { title: 'Lịch sử Việt Nam hiện đại', category: 'Lịch sử', meta: 'Phần 2 · 31 trang', progress: 80, readMinutes: 9, tone: 'amber' as const }
];

const missions = [
  { title: 'Đọc 3 tài liệu mới', description: 'Khám phá nội dung gợi ý', reward: 50, current: 2, total: 3, status: 'active' as const },
  { title: 'Hoàn thành 1 bài quiz', description: 'Củng cố kiến thức', reward: 30, current: 1, total: 1, status: 'done' as const },
  { title: 'Học liên tục 30 phút', description: 'Duy trì sự tập trung', reward: 40, current: 18, total: 30, status: 'active' as const },
  { title: 'Mở khoá bộ sưu tập VIP', description: 'Cần đạt cấp độ 8', reward: 100, current: 0, total: 1, status: 'locked' as const }
];
const missionsDone = computed(() => missions.filter((m) => m.status === 'done').length);

const recommended = [
  { title: 'Cấu trúc dữ liệu & Giải thuật', category: 'Tin học', meta: 'PDF · 56 trang', readMinutes: 25, coins: 20, rating: 4.8, tone: 'violet' as const },
  { title: 'Hoá học hữu cơ — Tổng ôn', category: 'Hoá học', meta: 'Slide · 40 trang', readMinutes: 15, coins: 15, rating: 4.6, tone: 'green' as const },
  { title: 'Tư duy phản biện', category: 'Kỹ năng', meta: 'PDF · 28 trang', readMinutes: 20, coins: 30, rating: 4.9, locked: true, tone: 'magenta' as const }
];

const collections = [
  { name: 'Ôn thi cuối kỳ', count: 18, icon: 'flame', accent: 'amber' },
  { name: 'Tiếng Anh', count: 12, icon: 'book-open', accent: 'cyan' },
  { name: 'Lập trình', count: 9, icon: 'zap', accent: 'violet' },
  { name: 'Yêu thích', count: 24, icon: 'star', accent: 'magenta' }
];
const collectionAccent: Record<string, string> = {
  amber: 'bg-amber-500/15 text-amber-300',
  cyan: 'bg-cyan-500/15 text-cyan-300',
  violet: 'bg-violet-500/15 text-violet-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300'
};

const subjects = [
  { name: 'Toán học', pct: 78, accent: 'cyan' as const },
  { name: 'Ngoại ngữ', pct: 64, accent: 'green' as const },
  { name: 'Khoa học', pct: 52, accent: 'violet' as const },
  { name: 'Lịch sử', pct: 41, accent: 'amber' as const }
];

const weekly = [
  { label: 'T2', value: 45 },
  { label: 'T3', value: 30 },
  { label: 'T4', value: 60 },
  { label: 'T5', value: 20 },
  { label: 'T6', value: 50 },
  { label: 'T7', value: 70 },
  { label: 'CN', value: 35, active: true }
];
const weekTotal = weekly.reduce((sum, d) => sum + d.value, 0);

const leaderboard = [
  { rank: 1, name: 'Minh Anh', coins: 3120, accent: 'amber' as const },
  { rank: 2, name: 'Hoàng Long', coins: 2870, accent: 'cyan' as const },
  { rank: 3, name: 'Bạn', coins: 1250, me: true, accent: 'violet' as const },
  { rank: 4, name: 'Thu Hà', coins: 1180, accent: 'green' as const },
  { rank: 5, name: 'Quốc Bảo', coins: 990, accent: 'magenta' as const }
];
const rankColor: Record<number, string> = {
  1: 'bg-amber-400/20 text-amber-300',
  2: 'bg-slate-300/15 text-slate-200',
  3: 'bg-orange-400/20 text-orange-300'
};

const achievements = [
  { name: 'Khởi đầu', icon: 'rocket', rarity: 'common' as const, unlocked: true },
  { name: 'Chăm chỉ', icon: 'flame', rarity: 'rare' as const, unlocked: true },
  { name: 'Bậc thầy', icon: 'crown', rarity: 'epic' as const, unlocked: true },
  { name: 'Huyền thoại', icon: 'gem', rarity: 'legendary' as const, unlocked: false, progress: 65 }
];

const recentActivity = [
  { icon: 'check-circle', tone: 'green', text: 'Hoàn thành nhiệm vụ "Hoàn thành 1 bài quiz"', time: '10 phút trước' },
  { icon: 'coin', tone: 'amber', text: 'Nhận 50 coin từ phần thưởng hằng ngày', time: '1 giờ trước' },
  { icon: 'book-open', tone: 'cyan', text: 'Đọc xong "Lịch sử Việt Nam hiện đại"', time: '3 giờ trước' },
  { icon: 'trophy', tone: 'violet', text: 'Thăng hạng lên Bạc trên bảng xếp hạng', time: 'Hôm qua' }
];
const activityTone: Record<string, string> = {
  green: 'bg-emerald-500/15 text-emerald-300',
  amber: 'bg-amber-500/15 text-amber-300',
  cyan: 'bg-cyan-500/15 text-cyan-300',
  violet: 'bg-violet-500/15 text-violet-300'
};
</script>

<template>
  <div class="space-y-6">
    <!-- Welcome banner -->
    <GlassPanel glow="violet" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Bảng điều khiển học tập</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            {{ greeting }}, <span class="cosmic-gradient-text">{{ firstName }}</span> 👋
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Bạn đã học <span class="font-semibold text-foreground">{{ dailyGoal.done }}/{{ dailyGoal.target }} phút</span>
            hôm nay. Cố thêm chút nữa để giữ chuỗi học nhé!
          </p>

          <div class="mt-4 max-w-sm">
            <div class="mb-1.5 flex items-center justify-between text-xs font-medium text-muted-foreground">
              <span>Mục tiêu hằng ngày</span><span>{{ dailyPct }}%</span>
            </div>
            <ProgressBar :value="dailyPct" accent="cyan" :height="8" />
          </div>

          <div class="mt-5 flex flex-wrap gap-2">
            <button
              v-for="action in quickActions"
              :key="action.label"
              type="button"
              class="inline-flex items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] px-3.5 py-2 text-sm font-medium text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
            >
              <AppIcon :name="action.icon" class="h-4 w-4" /> {{ action.label }}
            </button>
          </div>
        </div>

        <!-- Level ring + streak -->
        <div class="flex shrink-0 items-center gap-5">
          <RingProgress :value="xpPct" :size="116" accent="violet">
            <span class="text-[0.6rem] uppercase tracking-wider text-muted-foreground">Cấp</span>
            <span class="text-3xl font-bold text-foreground">{{ level.current }}</span>
            <span class="text-[0.65rem] text-muted-foreground">{{ xpPct }}%</span>
          </RingProgress>
          <div class="hidden sm:block">
            <div class="flex items-center gap-2 rounded-xl border border-orange-400/25 bg-orange-500/10 px-3 py-2">
              <AppIcon name="flame" class="h-6 w-6 text-orange-300" />
              <div>
                <p class="text-lg font-bold leading-none text-foreground">12 ngày</p>
                <p class="text-[0.7rem] text-muted-foreground">Chuỗi học 🔥</p>
              </div>
            </div>
            <p class="mt-2 text-[0.7rem] text-muted-foreground">Còn {{ (level.next - level.xp).toLocaleString('vi-VN') }} XP để lên cấp 8</p>
          </div>
        </div>
      </div>
    </GlassPanel>

    <!-- KPI stats -->
    <section class="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-6">
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

    <!-- Main grid -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Left -->
      <div class="space-y-6 lg:col-span-2">
        <SectionPanel title="Tiếp tục học" subtitle="Quay lại nơi bạn đã dừng" icon="play" accent="cyan" action-label="Tất cả">
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            <DocumentCard v-for="doc in continueDocs" :key="doc.title" v-bind="doc" />
          </div>
        </SectionPanel>

        <SectionPanel title="Thời gian học tuần này" :subtitle="`Tổng ${weekTotal} phút`" icon="chart" accent="violet">
          <BarChart :data="weekly" accent="violet" :height="140" />
        </SectionPanel>

        <SectionPanel title="Nhiệm vụ hôm nay" subtitle="Hoàn thành để nhận coin" icon="target" accent="magenta" action-label="Tất cả">
          <template #header-actions>
            <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs font-semibold text-muted-foreground">
              {{ missionsDone }}/{{ missions.length }} xong
            </span>
          </template>
          <div class="space-y-2.5">
            <MissionItem v-for="mission in missions" :key="mission.title" v-bind="mission" />
          </div>
        </SectionPanel>

        <SectionPanel title="Gợi ý cho bạn" subtitle="Dựa trên môn học bạn quan tâm" icon="sparkles" accent="green" action-label="Khám phá">
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            <DocumentCard v-for="doc in recommended" :key="doc.title" v-bind="doc" />
          </div>
        </SectionPanel>

        <SectionPanel title="Bộ sưu tập của bạn" subtitle="Sắp xếp tài liệu theo chủ đề" icon="folder" accent="cyan" action-label="Quản lý">
          <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
            <button
              v-for="col in collections"
              :key="col.name"
              type="button"
              class="flex flex-col items-start gap-2 rounded-xl border border-white/[0.06] bg-white/[0.02] p-3.5 text-left transition-colors hover:border-white/[0.12]"
            >
              <span class="flex h-10 w-10 items-center justify-center rounded-xl" :class="collectionAccent[col.accent]">
                <AppIcon :name="col.icon" class="h-5 w-5" />
              </span>
              <span class="line-clamp-1 text-sm font-semibold text-foreground">{{ col.name }}</span>
              <span class="text-xs text-muted-foreground">{{ col.count }} tài liệu</span>
            </button>
            <button
              type="button"
              class="flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-white/[0.1] bg-white/[0.01] p-3.5 text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-300"
            >
              <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-white/[0.04]">
                <AppIcon name="plus" class="h-5 w-5" />
              </span>
              <span class="text-sm font-medium">Tạo mới</span>
            </button>
          </div>
        </SectionPanel>
      </div>

      <!-- Right -->
      <div class="space-y-6">
        <SectionPanel title="Mức độ thành thạo" subtitle="Theo môn học" icon="graduation" accent="cyan">
          <div class="space-y-3.5">
            <div v-for="s in subjects" :key="s.name">
              <div class="mb-1 flex items-center justify-between text-xs">
                <span class="font-medium text-foreground">{{ s.name }}</span>
                <span class="tabular-nums text-muted-foreground">{{ s.pct }}%</span>
              </div>
              <ProgressBar :value="s.pct" :accent="s.accent" />
            </div>
          </div>
        </SectionPanel>

        <SectionPanel title="Bảng xếp hạng" subtitle="Tuần này · bạn bè" icon="trophy" accent="amber" action-label="Đầy đủ" flush>
          <ul class="divide-y divide-white/[0.05]">
            <li
              v-for="user in leaderboard"
              :key="user.rank"
              class="flex items-center gap-3 px-5 py-2.5"
              :class="user.me ? 'bg-cyan-500/[0.06]' : ''"
            >
              <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold" :class="rankColor[user.rank] ?? 'bg-white/[0.06] text-muted-foreground'">
                {{ user.rank }}
              </span>
              <Avatar :name="user.name" :size="32" :accent="user.accent" />
              <span class="min-w-0 flex-1 truncate text-sm font-medium" :class="user.me ? 'text-cyan-200' : 'text-foreground'">
                {{ user.name }}<span v-if="user.me" class="text-muted-foreground"> (bạn)</span>
              </span>
              <span class="inline-flex shrink-0 items-center gap-1 text-sm font-semibold text-amber-300">
                <AppIcon name="coin" class="h-3.5 w-3.5" /> {{ user.coins.toLocaleString('vi-VN') }}
              </span>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Thành tựu" subtitle="3/4 đã mở khoá" icon="award" accent="violet" action-label="Xem">
          <div class="grid grid-cols-2 gap-3">
            <BadgeTile v-for="a in achievements" :key="a.name" v-bind="a" />
          </div>
        </SectionPanel>

        <SectionPanel title="Hoạt động gần đây" icon="clock" accent="cyan">
          <ul class="space-y-3.5">
            <li v-for="(item, i) in recentActivity" :key="i" class="flex gap-3">
              <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg" :class="activityTone[item.tone]">
                <AppIcon :name="item.icon" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="text-sm leading-snug text-muted-foreground">{{ item.text }}</p>
                <p class="mt-0.5 text-[0.7rem] text-muted-foreground/70">{{ item.time }}</p>
              </div>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Nhóm học tập" icon="users" accent="magenta">
          <div class="flex flex-col items-center justify-center py-6 text-center">
            <span class="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/[0.04] text-muted-foreground">
              <AppIcon name="inbox" class="h-7 w-7" />
            </span>
            <p class="mt-3 text-sm font-medium text-foreground">Bạn chưa tham gia nhóm nào</p>
            <p class="mt-1 max-w-[15rem] text-xs text-muted-foreground">
              Tham gia nhóm để học cùng bạn bè, chia sẻ tài liệu và thi đua.
            </p>
            <button type="button" class="cosmic-btn mt-4 inline-flex items-center gap-1.5 rounded-xl px-4 py-2 text-sm">
              <AppIcon name="plus" class="h-4 w-4" /> Khám phá nhóm
            </button>
          </div>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
