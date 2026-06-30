<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import TiltSpotlightCard from '@/components/app/TiltSpotlightCard.vue';
import Sparkline from '@/components/app/Sparkline.vue';
import Avatar from '@/components/app/Avatar.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Range = 'today' | 'week' | 'month';

const { push } = useToast();

const ranges: { id: Range; label: string }[] = [
  { id: 'today', label: 'Hôm nay' },
  { id: 'week', label: 'Tuần này' },
  { id: 'month', label: 'Tháng này' }
];
const activeRange = ref<Range>('week');

interface TrendDoc {
  id: number;
  title: string;
  category: string;
  icon: string;
  author: string;
  accent: Accent;
  spark: number[];
  views: Record<Range, number>;
  trend: Record<Range, number>;
}

const docs: TrendDoc[] = [
  {
    id: 1,
    title: 'Làm chủ đạo hàm từ cơ bản đến nâng cao',
    category: 'Toán',
    icon: 'chart',
    author: 'Nguyễn Minh Quân',
    accent: 'cyan',
    spark: [12, 18, 16, 24, 30, 28, 42],
    views: { today: 2400, week: 12400, month: 38200 },
    trend: { today: 32, week: 18, month: 9 }
  },
  {
    id: 2,
    title: 'Chiến lược chinh phục TOEIC 800+ trong 60 ngày',
    category: 'Ngoại ngữ',
    icon: 'globe',
    author: 'Lê Thu Hà',
    accent: 'magenta',
    spark: [20, 22, 28, 26, 34, 40, 46],
    views: { today: 3100, week: 11800, month: 41200 },
    trend: { today: 45, week: 22, month: 14 }
  },
  {
    id: 3,
    title: 'Thuật toán & cấu trúc dữ liệu với Python',
    category: 'Lập trình',
    icon: 'command',
    author: 'Trần Đức Anh',
    accent: 'green',
    spark: [8, 14, 20, 22, 30, 36, 50],
    views: { today: 2800, week: 15700, month: 52800 },
    trend: { today: 28, week: 31, month: 21 }
  },
  {
    id: 4,
    title: 'Nhập môn cơ học lượng tử cho người mới',
    category: 'Khoa học',
    icon: 'rocket',
    author: 'Phạm Hải Yến',
    accent: 'violet',
    spark: [6, 10, 9, 14, 18, 16, 22],
    views: { today: 1500, week: 6300, month: 19400 },
    trend: { today: 12, week: 8, month: -4 }
  },
  {
    id: 5,
    title: 'Kỹ thuật ghi nhớ Feynman & sơ đồ tư duy',
    category: 'Kỹ năng',
    icon: 'lightbulb',
    author: 'Đỗ Khánh Linh',
    accent: 'amber',
    spark: [10, 16, 22, 20, 28, 32, 38],
    views: { today: 2050, week: 11200, month: 33600 },
    trend: { today: 19, week: 16, month: 11 }
  },
  {
    id: 6,
    title: 'Viết luận tiếng Anh học thuật chuẩn IELTS',
    category: 'Ngoại ngữ',
    icon: 'globe',
    author: 'Lê Thu Hà',
    accent: 'magenta',
    spark: [9, 11, 14, 13, 18, 21, 24],
    views: { today: 1280, week: 5900, month: 22800 },
    trend: { today: 7, week: 12, month: 5 }
  },
  {
    id: 7,
    title: 'Dòng chảy lịch sử Việt Nam thế kỷ 20',
    category: 'Lịch sử',
    icon: 'book-open',
    author: 'Vũ Quốc Bảo',
    accent: 'cyan',
    spark: [14, 12, 10, 11, 9, 8, 7],
    views: { today: 760, week: 4800, month: 26400 },
    trend: { today: -8, week: -3, month: 2 }
  },
  {
    id: 8,
    title: 'Tích phân ứng dụng trong vật lý hiện đại',
    category: 'Toán',
    icon: 'chart',
    author: 'Nguyễn Minh Quân',
    accent: 'green',
    spark: [7, 9, 12, 15, 14, 18, 20],
    views: { today: 980, week: 7700, month: 24100 },
    trend: { today: 11, week: 9, month: 6 }
  }
];

const ranked = computed<TrendDoc[]>(() =>
  [...docs].sort((a, b) => b.views[activeRange.value] - a.views[activeRange.value])
);
const podium = computed<TrendDoc[]>(() => ranked.value.slice(0, 3));
const rest = computed<TrendDoc[]>(() => ranked.value.slice(3));

const totalReads = computed<number>(() =>
  docs.reduce((sum, d) => sum + d.views[activeRange.value], 0)
);

const kpis = computed<
  { label: string; value: number; suffix?: string; icon: string; accent: Accent }[]
>(() => [
  { label: 'Lượt đọc', value: totalReads.value, icon: 'eye', accent: 'cyan' },
  { label: 'Chủ đề nóng', value: 24, icon: 'flame', accent: 'magenta' },
  { label: 'Người học tích cực', value: 1820, icon: 'users', accent: 'green' },
  { label: 'Tác giả mới nổi', value: 12, icon: 'rocket', accent: 'amber' }
]);

// ─── Topics ──────────────────────────────────────────────────────────────────
interface Topic {
  id: string;
  label: string;
  growth: number;
  heat: number;
  accent: Accent;
}
const topics: Topic[] = [
  { id: 'thpt', label: 'Ôn thi THPT', growth: 64, heat: 92, accent: 'cyan' },
  { id: 'toeic', label: 'TOEIC 800+', growth: 48, heat: 86, accent: 'magenta' },
  { id: 'dsa', label: 'Thuật toán', growth: 41, heat: 80, accent: 'green' },
  { id: 'memory', label: 'Ghi nhớ sâu', growth: 33, heat: 72, accent: 'amber' },
  { id: 'quantum', label: 'Vật lý lượng tử', growth: 27, heat: 64, accent: 'violet' },
  { id: 'essay', label: 'Viết luận', growth: 19, heat: 55, accent: 'cyan' }
];

// ─── Rising creators ───────────────────────────────────────────────────────
interface Creator {
  id: number;
  name: string;
  field: string;
  growth: number;
  accent: Accent;
  followed: boolean;
}
const creators = ref<Creator[]>([
  {
    id: 1,
    name: 'Hoàng Anh Tú',
    field: 'Hoá học hữu cơ',
    growth: 320,
    accent: 'green',
    followed: false
  },
  {
    id: 2,
    name: 'Mai Phương Thảo',
    field: 'Luyện thi IELTS',
    growth: 285,
    accent: 'magenta',
    followed: false
  },
  {
    id: 3,
    name: 'Lý Gia Hân',
    field: 'Sinh học phân tử',
    growth: 210,
    accent: 'cyan',
    followed: false
  }
]);
function toggleFollow(c: Creator): void {
  c.followed = !c.followed;
  if (c.followed) push(`Đang theo dõi ${c.name}`, { icon: 'user', accent: c.accent });
}

// ─── helpers ───────────────────────────────────────────────────────────────
function fmt(n: number): string {
  return n >= 1000 ? `${(n / 1000).toFixed(1)}k` : String(n);
}

const podiumStyle: { ring: string; badge: string; glow: Accent; medal: string }[] = [
  {
    ring: 'from-amber-300/50 to-orange-500/30',
    badge: 'bg-amber-400 text-amber-950',
    glow: 'amber',
    medal: '🥇'
  },
  {
    ring: 'from-slate-300/40 to-slate-500/30',
    badge: 'bg-slate-200 text-slate-800',
    glow: 'cyan',
    medal: '🥈'
  },
  {
    ring: 'from-orange-400/40 to-amber-700/30',
    badge: 'bg-orange-400 text-orange-950',
    glow: 'magenta',
    medal: '🥉'
  }
];

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
const accentText: Record<Accent, string> = {
  cyan: 'text-cyan-300',
  magenta: 'text-fuchsia-300',
  green: 'text-emerald-300',
  violet: 'text-violet-300',
  amber: 'text-amber-300'
};
const barFill: Record<Accent, string> = {
  cyan: 'from-cyan-400 to-cyan-500',
  magenta: 'from-fuchsia-400 to-fuchsia-500',
  green: 'from-emerald-400 to-emerald-500',
  violet: 'from-violet-400 to-violet-500',
  amber: 'from-amber-300 to-amber-400'
};
</script>

<template>
  <div class="space-y-6">
    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p class="cosmic-badge"><AppIcon name="flame" class="h-3.5 w-3.5" /> Thịnh hành</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            Đang <span class="cosmic-gradient-text">bùng nổ</span> trong cộng đồng
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Bảng xếp hạng tài liệu, chủ đề và tác giả được quan tâm nhất.
          </p>
        </div>
        <div
          class="inline-flex shrink-0 items-center gap-1 rounded-sm border border-white/[0.08] bg-white/[0.03] p-1"
        >
          <button
            v-for="r in ranges"
            :key="r.id"
            type="button"
            class="rounded-sm px-3 py-1.5 text-xs font-semibold transition-colors"
            :class="
              activeRange === r.id
                ? 'bg-cyan-500/15 text-cyan-200'
                : 'text-muted-foreground hover:text-foreground'
            "
            @click="activeRange = r.id"
          >
            {{ r.label }}
          </button>
        </div>
      </div>
    </GlassPanel>

    <!-- KPIs -->
    <section class="fx-stagger grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-4">
      <GlassPanel v-for="k in kpis" :key="k.label" :glow="k.accent" hover padded>
        <div class="flex items-center gap-3">
          <span
            class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm"
            :class="accentChip[k.accent]"
          >
            <AppIcon :name="k.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0">
            <p class="text-xl font-bold text-foreground"><CountUp :value="k.value" /></p>
            <p class="truncate text-[0.7rem] text-muted-foreground">{{ k.label }}</p>
          </div>
        </div>
      </GlassPanel>
    </section>

    <!-- PODIUM -->
    <section class="grid grid-cols-1 gap-4 md:grid-cols-3">
      <TiltSpotlightCard
        v-for="(doc, i) in podium"
        :key="doc.id"
        :glow="podiumStyle[i].glow"
        class="p-5"
      >
        <div class="flex items-center justify-between">
          <span
            class="flex h-12 w-12 items-center justify-center rounded-sm text-2xl"
            :class="podiumStyle[i].ring"
          >
            {{ podiumStyle[i].medal }}
          </span>
          <span
            class="inline-flex items-center gap-1 rounded-sm px-2 py-1 text-xs font-semibold"
            :class="
              doc.trend[activeRange] >= 0
                ? 'bg-emerald-500/10 text-emerald-300'
                : 'bg-rose-500/10 text-rose-300'
            "
          >
            <AppIcon
              :name="doc.trend[activeRange] >= 0 ? 'trending-up' : 'trending-down'"
              class="h-3.5 w-3.5"
            />
            {{ Math.abs(doc.trend[activeRange]) }}%
          </span>
        </div>
        <span
          class="mt-4 inline-flex items-center gap-1 text-[0.7rem] font-semibold uppercase tracking-wide"
          :class="accentText[doc.accent]"
        >
          <AppIcon :name="doc.icon" class="h-3.5 w-3.5" /> {{ doc.category }}
        </span>
        <h3
          class="mt-1 line-clamp-2 min-h-[2.6rem] text-sm font-semibold leading-snug text-foreground"
        >
          {{ doc.title }}
        </h3>
        <p class="mt-2 text-xs text-muted-foreground">{{ doc.author }}</p>
        <div class="mt-3 flex items-end justify-between">
          <p class="flex items-baseline gap-1 text-2xl font-bold text-foreground">
            <CountUp :value="doc.views[activeRange]" />
            <span class="text-xs font-normal text-muted-foreground">lượt đọc</span>
          </p>
          <AppIcon name="eye" class="h-5 w-5 text-muted-foreground" />
        </div>
      </TiltSpotlightCard>
    </section>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- RANKED LIST -->
      <div class="lg:col-span-2">
        <SectionPanel
          title="Bảng xếp hạng đầy đủ"
          :subtitle="`Top tài liệu · ${ranges.find((r) => r.id === activeRange)?.label}`"
          icon="trophy"
          accent="cyan"
        >
          <ul class="space-y-2">
            <li
              v-for="(doc, i) in rest"
              :key="doc.id"
              class="fx-lift flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-3"
            >
              <span
                class="w-7 shrink-0 text-center text-lg font-bold text-muted-foreground tabular-nums"
                >{{ i + 4 }}</span
              >
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-sm"
                :class="accentChip[doc.accent]"
              >
                <AppIcon :name="doc.icon" class="h-5 w-5" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-semibold text-foreground">{{ doc.title }}</p>
                <p class="truncate text-xs text-muted-foreground">
                  {{ doc.category }} · {{ doc.author }}
                </p>
              </div>
              <div class="hidden w-20 shrink-0 sm:block">
                <Sparkline :data="doc.spark" :accent="doc.accent" :height="28" />
              </div>
              <div class="w-16 shrink-0 text-right">
                <p class="text-sm font-semibold text-foreground tabular-nums">
                  {{ fmt(doc.views[activeRange]) }}
                </p>
                <p
                  class="inline-flex items-center gap-0.5 text-[0.7rem]"
                  :class="doc.trend[activeRange] >= 0 ? 'text-emerald-300' : 'text-rose-300'"
                >
                  <AppIcon
                    :name="doc.trend[activeRange] >= 0 ? 'trending-up' : 'trending-down'"
                    class="h-3 w-3"
                  />
                  {{ Math.abs(doc.trend[activeRange]) }}%
                </p>
              </div>
            </li>
          </ul>
        </SectionPanel>
      </div>

      <!-- SIDE -->
      <div class="space-y-6">
        <SectionPanel
          title="Chủ đề bùng nổ"
          subtitle="Tăng trưởng tìm kiếm"
          icon="fire"
          accent="magenta"
        >
          <ul class="space-y-3">
            <li v-for="t in topics" :key="t.id">
              <div class="flex items-center justify-between text-sm">
                <span class="font-medium text-foreground">{{ t.label }}</span>
                <span
                  class="inline-flex items-center gap-0.5 text-xs font-semibold text-emerald-300"
                >
                  <AppIcon name="arrow-up" class="h-3 w-3" /> {{ t.growth }}%
                </span>
              </div>
              <div class="mt-1.5 h-2 w-full overflow-hidden rounded-sm bg-white/[0.06]">
                <div
                  class="h-full rounded-sm
                  :class="barFill[t.accent]"
                  :style="{ width: `${t.heat}%` }"
                />
              </div>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel
          title="Tác giả mới nổi"
          subtitle="Tăng follower nhanh nhất"
          icon="rocket"
          accent="green"
        >
          <ul class="space-y-3">
            <li v-for="c in creators" :key="c.id" class="flex items-center gap-3">
              <Avatar :name="c.name" :accent="c.accent" :size="40" />
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ c.name }}</p>
                <p class="truncate text-xs text-muted-foreground">
                  {{ c.field }} · <span class="text-emerald-300">+{{ c.growth }}/tuần</span>
                </p>
              </div>
              <button
                type="button"
                class="shrink-0 rounded-sm border px-3 py-1.5 text-xs font-semibold transition-colors"
                :class="
                  c.followed
                    ? 'border-white/[0.12] bg-white/[0.04] text-muted-foreground'
                    : 'border-emerald-400/40 bg-emerald-500/15 text-emerald-200 hover:bg-emerald-500/25'
                "
                @click="toggleFollow(c)"
              >
                {{ c.followed ? 'Đang theo dõi' : 'Theo dõi' }}
              </button>
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
