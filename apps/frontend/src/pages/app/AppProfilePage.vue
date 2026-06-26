<script setup lang="ts">
import { computed } from 'vue';
import { useAuthSession } from '@/features/auth/authSession';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import Avatar from '@/components/app/Avatar.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import BarChart from '@/components/app/BarChart.vue';
import BadgeTile from '@/components/app/BadgeTile.vue';

const { currentUser } = useAuthSession();

const displayName = computed(() => currentUser.value?.fullName?.trim() || 'Người học');
const email = computed(() => currentUser.value?.email || 'hocvien@example.com');

// ── Types ────────────────────────────────────────────────────────────────────

interface StatItem {
  label: string;
  value: string;
  unit?: string;
  icon: string;
  accent: 'amber' | 'cyan' | 'violet' | 'green' | 'magenta';
  trend?: number;
  caption?: string;
  sparkline?: number[];
}

interface SubjectMastery {
  name: string;
  pct: number;
  accent: 'cyan' | 'green' | 'violet' | 'amber' | 'magenta';
}

interface BarData {
  label: string;
  value: number;
  active?: boolean;
}

interface BadgeItem {
  name: string;
  icon: string;
  description?: string;
  unlocked: boolean;
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
  progress?: number;
}

interface ActivityItem {
  icon: string;
  tone: string;
  text: string;
  time: string;
}

interface NextReward {
  icon: string;
  label: string;
  tone: string;
}

interface FavSubject {
  label: string;
  icon: string;
  accent: string;
}

// ── Mock data ────────────────────────────────────────────────────────────────

const level = { current: 7, xp: 1240, next: 2000 };
const xpPct = Math.round((level.xp / level.next) * 100);

const stats: StatItem[] = [
  { label: 'Điểm XP', value: '1.240', icon: 'zap', accent: 'violet', trend: 15, caption: 'Cấp 7', sparkline: [400, 560, 700, 820, 960, 1050, 1160, 1240] },
  { label: 'Số dư coin', value: '1.250', icon: 'coin', accent: 'amber', trend: 8, caption: '+50 hôm nay', sparkline: [900, 980, 1010, 1080, 1120, 1180, 1210, 1250] },
  { label: 'Chuỗi học', value: '12', unit: 'ngày', icon: 'flame', accent: 'magenta', trend: 4, caption: 'Kỷ lục: 21 ngày', sparkline: [6, 7, 8, 9, 10, 11, 11, 12] },
  { label: 'Tài liệu đã đọc', value: '48', icon: 'book-open', accent: 'cyan', trend: 12, caption: '+3 tuần này', sparkline: [30, 33, 36, 38, 41, 44, 46, 48] },
  { label: 'Xếp hạng', value: '#3', icon: 'trophy', accent: 'amber', trend: 2, caption: 'Trong nhóm', sparkline: [9, 8, 7, 6, 5, 4, 4, 3] },
  { label: 'Thành tựu', value: '9', unit: '/15', icon: 'award', accent: 'violet', trend: 1, caption: '3 tuần qua', sparkline: [4, 5, 5, 6, 7, 8, 8, 9] }
];

const nextRewards: NextReward[] = [
  { icon: 'coin', label: '+200 coin', tone: 'amber' },
  { icon: 'gem', label: 'Huy hiệu "Tinh thông"', tone: 'violet' },
  { icon: 'zap', label: 'Bội số XP ×1.5 (3 ngày)', tone: 'cyan' },
  { icon: 'unlock', label: 'Mở khoá khoá học VIP', tone: 'green' }
];

const subjects: SubjectMastery[] = [
  { name: 'Toán học', pct: 78, accent: 'cyan' },
  { name: 'Tiếng Anh', pct: 64, accent: 'green' },
  { name: 'Tin học', pct: 85, accent: 'violet' },
  { name: 'Vật lý', pct: 52, accent: 'amber' },
  { name: 'Lịch sử', pct: 41, accent: 'magenta' }
];

const weekly: BarData[] = [
  { label: 'T2', value: 45 },
  { label: 'T3', value: 30 },
  { label: 'T4', value: 60 },
  { label: 'T5', value: 20 },
  { label: 'T6', value: 50 },
  { label: 'T7', value: 70 },
  { label: 'CN', value: 35, active: true }
];
const weekTotal = weekly.reduce((sum, d) => sum + d.value, 0);

const recentBadges: BadgeItem[] = [
  { name: 'Khởi đầu', icon: 'rocket', description: 'Đọc tài liệu đầu tiên', unlocked: true, rarity: 'common' },
  { name: 'Chăm chỉ', icon: 'flame', description: 'Chuỗi 7 ngày liên tiếp', unlocked: true, rarity: 'rare' },
  { name: 'Bậc thầy', icon: 'crown', description: 'Đạt 1000 XP', unlocked: true, rarity: 'epic' },
  { name: 'Huyền thoại', icon: 'gem', description: 'Đạt cấp độ 10', unlocked: false, rarity: 'legendary', progress: 65 }
];

const recentActivity: ActivityItem[] = [
  { icon: 'check-circle', tone: 'green', text: 'Hoàn thành nhiệm vụ "Hoàn thành 1 bài quiz"', time: '10 phút trước' },
  { icon: 'coin', tone: 'amber', text: 'Nhận 50 coin từ phần thưởng hằng ngày', time: '1 giờ trước' },
  { icon: 'book-open', tone: 'cyan', text: 'Đọc xong "Lịch sử Việt Nam hiện đại"', time: '3 giờ trước' },
  { icon: 'trophy', tone: 'violet', text: 'Thăng hạng lên Bạc trên bảng xếp hạng', time: 'Hôm qua' },
  { icon: 'star', tone: 'amber', text: 'Mở khoá thành tựu "Bậc thầy"', time: '2 ngày trước' }
];

const favSubjects: FavSubject[] = [
  { label: 'Toán học', icon: 'graduation', accent: 'cyan' },
  { label: 'Tin học', icon: 'zap', accent: 'violet' },
  { label: 'Tiếng Anh', icon: 'globe', accent: 'green' },
  { label: 'Vật lý', icon: 'sparkles', accent: 'amber' },
  { label: 'Lập trình', icon: 'layers', accent: 'magenta' },
  { label: 'Toán rời rạc', icon: 'grid', accent: 'cyan' }
];

const activityTone: Record<string, string> = {
  green: 'bg-emerald-500/15 text-emerald-300',
  amber: 'bg-amber-500/15 text-amber-300',
  cyan: 'bg-cyan-500/15 text-cyan-300',
  violet: 'bg-violet-500/15 text-violet-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300'
};

const subjectAccent: Record<string, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300 border-cyan-400/20',
  violet: 'bg-violet-500/15 text-violet-300 border-violet-400/20',
  green: 'bg-emerald-500/15 text-emerald-300 border-emerald-400/20',
  amber: 'bg-amber-500/15 text-amber-300 border-amber-400/20',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300 border-fuchsia-400/20'
};

const rewardTone: Record<string, string> = {
  amber: 'bg-amber-500/15 text-amber-300',
  violet: 'bg-violet-500/15 text-violet-300',
  cyan: 'bg-cyan-500/15 text-cyan-300',
  green: 'bg-emerald-500/15 text-emerald-300'
};
</script>

<template>
  <div class="space-y-6">
    <!-- ── 1. Profile header ───────────────────────────────────────────────── -->
    <GlassPanel glow="cyan" :padded="false" class="overflow-hidden">
      <!-- Decorative banner strip -->
      <div class="h-[72px] w-full bg-gradient-to-r from-cyan-900/40 via-violet-900/30 to-fuchsia-900/40" />

      <div class="px-5 pb-5 sm:px-6 sm:pb-6">
        <!-- Avatar row — pulled up over the banner -->
        <div class="-mt-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div class="flex items-end gap-4">
            <div class="ring-background rounded-full ring-4">
              <Avatar :name="displayName" :size="80" accent="cyan" />
            </div>
            <div class="mb-1 min-w-0">
              <h1 class="text-xl font-bold text-foreground sm:text-2xl">{{ displayName }}</h1>
              <p class="text-sm text-muted-foreground">{{ email }}</p>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex shrink-0 items-center gap-2">
            <button
              type="button"
              class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-3.5 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40"
            >
              <AppIcon name="edit" class="h-4 w-4" />
              Chỉnh sửa hồ sơ
            </button>
            <button
              type="button"
              class="inline-flex items-center justify-center rounded-xl border border-white/10 bg-white/[0.03] p-2 text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-300"
              aria-label="Chia sẻ hồ sơ"
            >
              <AppIcon name="share" class="h-4 w-4" />
            </button>
          </div>
        </div>

        <!-- Bio + pills -->
        <p class="mt-3 max-w-xl text-sm text-muted-foreground">
          Đam mê học tập và khám phá kiến thức mới mỗi ngày. Yêu thích toán học và lập trình.
        </p>

        <div class="mt-3 flex flex-wrap items-center gap-2">
          <span class="rounded-full bg-violet-500/20 px-2.5 py-1 text-xs font-semibold text-violet-200">
            Cấp 7
          </span>
          <span class="rounded-full bg-cyan-500/15 px-2.5 py-1 text-xs font-medium text-cyan-300">
            Học viên
          </span>
          <span class="inline-flex items-center gap-1 rounded-full bg-white/[0.06] px-2.5 py-1 text-xs text-muted-foreground">
            <AppIcon name="calendar" class="h-3 w-3" />
            Tham gia từ tháng 9, 2024
          </span>
        </div>
      </div>
    </GlassPanel>

    <!-- ── 2. Stat row ─────────────────────────────────────────────────────── -->
    <section class="grid grid-cols-2 gap-4 md:grid-cols-4 xl:grid-cols-6">
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

    <!-- ── 3. Main grid ────────────────────────────────────────────────────── -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">

      <!-- ── LEFT (col-span-2) ─────────────────────────────────────────────── -->
      <div class="space-y-6 lg:col-span-2">

        <!-- 3a. Level progress -->
        <SectionPanel
          title="Tiến độ cấp độ"
          subtitle="Bạn đang ở Cấp 7 · tiếp tục học để lên Cấp 8"
          icon="zap"
          accent="violet"
        >
          <div class="flex flex-col items-center gap-6 sm:flex-row sm:items-start">
            <!-- Ring -->
            <div class="shrink-0">
              <RingProgress :value="xpPct" :size="120" accent="violet">
                <span class="text-[0.6rem] uppercase tracking-wider text-muted-foreground">Cấp</span>
                <span class="text-3xl font-bold text-foreground">{{ level.current }}</span>
                <span class="text-[0.65rem] text-muted-foreground">{{ xpPct }}%</span>
              </RingProgress>
            </div>

            <!-- XP detail + next-level rewards -->
            <div class="min-w-0 flex-1 space-y-4">
              <div>
                <div class="mb-1.5 flex items-center justify-between text-xs font-medium text-muted-foreground">
                  <span>{{ level.xp.toLocaleString('vi-VN') }} / {{ level.next.toLocaleString('vi-VN') }} XP</span>
                  <span>Còn {{ (level.next - level.xp).toLocaleString('vi-VN') }} XP</span>
                </div>
                <ProgressBar :value="xpPct" accent="violet" :height="8" />
              </div>

              <div>
                <p class="mb-2.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Phần thưởng khi lên Cấp 8
                </p>
                <ul class="space-y-2">
                  <li
                    v-for="reward in nextRewards"
                    :key="reward.label"
                    class="flex items-center gap-2.5"
                  >
                    <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg" :class="rewardTone[reward.tone]">
                      <AppIcon :name="reward.icon" class="h-3.5 w-3.5" />
                    </span>
                    <span class="text-sm text-muted-foreground">{{ reward.label }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </SectionPanel>

        <!-- 3b. Subject mastery -->
        <SectionPanel
          title="Mức độ thành thạo theo môn"
          subtitle="Dựa trên tài liệu đã đọc và bài kiểm tra"
          icon="graduation"
          accent="cyan"
        >
          <div class="space-y-4">
            <div v-for="s in subjects" :key="s.name">
              <div class="mb-1.5 flex items-center justify-between text-xs">
                <span class="font-medium text-foreground">{{ s.name }}</span>
                <span class="tabular-nums text-muted-foreground">{{ s.pct }}%</span>
              </div>
              <ProgressBar :value="s.pct" :accent="s.accent" />
            </div>
          </div>
        </SectionPanel>

        <!-- 3c. Study time chart -->
        <SectionPanel
          title="Thời gian học"
          :subtitle="`Tuần này · Tổng ${weekTotal} phút`"
          icon="chart"
          accent="violet"
        >
          <BarChart :data="weekly" accent="violet" :height="140" />
          <p class="mt-3 text-right text-xs text-muted-foreground">
            Trung bình {{ Math.round(weekTotal / weekly.length) }} phút/ngày
          </p>
        </SectionPanel>
      </div>

      <!-- ── RIGHT (col-span-1) ────────────────────────────────────────────── -->
      <div class="space-y-6">

        <!-- 3d. Recent achievements -->
        <SectionPanel
          title="Thành tựu gần đây"
          subtitle="3/4 đã mở khoá"
          icon="award"
          accent="violet"
          action-label="Xem tất cả"
        >
          <div class="grid grid-cols-2 gap-3">
            <BadgeTile
              v-for="badge in recentBadges"
              :key="badge.name"
              :name="badge.name"
              :icon="badge.icon"
              :description="badge.description"
              :unlocked="badge.unlocked"
              :rarity="badge.rarity"
              :progress="badge.progress"
            />
          </div>
        </SectionPanel>

        <!-- 3e. Recent activity -->
        <SectionPanel
          title="Hoạt động gần đây"
          icon="clock"
          accent="cyan"
        >
          <ul class="space-y-3.5">
            <li v-for="(item, i) in recentActivity" :key="i" class="flex gap-3">
              <span
                class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg"
                :class="activityTone[item.tone]"
              >
                <AppIcon :name="item.icon" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="line-clamp-2 text-sm leading-snug text-muted-foreground">{{ item.text }}</p>
                <p class="mt-0.5 text-[0.7rem] text-muted-foreground/70">{{ item.time }}</p>
              </div>
            </li>
          </ul>
        </SectionPanel>

        <!-- 3f. Favourite subjects -->
        <SectionPanel
          title="Môn học yêu thích"
          icon="heart"
          accent="magenta"
        >
          <div class="flex flex-wrap gap-2">
            <span
              v-for="sub in favSubjects"
              :key="sub.label"
              class="inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium transition-colors"
              :class="subjectAccent[sub.accent]"
            >
              <AppIcon :name="sub.icon" class="h-3 w-3" />
              {{ sub.label }}
            </span>
          </div>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
