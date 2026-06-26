<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import Toggle from '@/components/app/Toggle.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

const { push } = useToast();

interface NewDoc {
  id: number;
  title: string;
  categoryId: string;
  category: string;
  icon: string;
  author: string;
  accent: Accent;
  minutesAgo: number;
  readMinutes: number;
  coins: number;
  bookmarked: boolean;
}

const categories: { id: string; label: string; icon: string }[] = [
  { id: 'all', label: 'Tất cả', icon: 'grid' },
  { id: 'math', label: 'Toán', icon: 'chart' },
  { id: 'lang', label: 'Ngoại ngữ', icon: 'globe' },
  { id: 'code', label: 'Lập trình', icon: 'command' },
  { id: 'science', label: 'Khoa học', icon: 'rocket' },
  { id: 'skill', label: 'Kỹ năng', icon: 'lightbulb' }
];
const activeCategory = ref<string>('all');
const notify = ref<boolean>(true);

let seq = 200;
const docs = ref<NewDoc[]>([
  {
    id: 1,
    title: 'Bộ đề ôn thi tốt nghiệp môn Toán 2026',
    categoryId: 'math',
    category: 'Toán',
    icon: 'chart',
    author: 'Nguyễn Minh Quân',
    accent: 'cyan',
    minutesAgo: 4,
    readMinutes: 28,
    coins: 50,
    bookmarked: false
  },
  {
    id: 2,
    title: '500 từ vựng TOEIC theo chủ đề công sở',
    categoryId: 'lang',
    category: 'Ngoại ngữ',
    icon: 'globe',
    author: 'Lê Thu Hà',
    accent: 'magenta',
    minutesAgo: 18,
    readMinutes: 15,
    coins: 35,
    bookmarked: false
  },
  {
    id: 3,
    title: 'Tối ưu thuật toán với lập trình động',
    categoryId: 'code',
    category: 'Lập trình',
    icon: 'command',
    author: 'Trần Đức Anh',
    accent: 'green',
    minutesAgo: 47,
    readMinutes: 32,
    coins: 55,
    bookmarked: true
  },
  {
    id: 4,
    title: 'Thí nghiệm vật lý lượng tử trực quan',
    categoryId: 'science',
    category: 'Khoa học',
    icon: 'rocket',
    author: 'Phạm Hải Yến',
    accent: 'violet',
    minutesAgo: 95,
    readMinutes: 22,
    coins: 45,
    bookmarked: false
  },
  {
    id: 5,
    title: 'Sơ đồ tư duy cho người mới bắt đầu',
    categoryId: 'skill',
    category: 'Kỹ năng',
    icon: 'lightbulb',
    author: 'Đỗ Khánh Linh',
    accent: 'amber',
    minutesAgo: 320,
    readMinutes: 12,
    coins: 30,
    bookmarked: false
  },
  {
    id: 6,
    title: 'Ngữ pháp tiếng Anh nâng cao cho IELTS',
    categoryId: 'lang',
    category: 'Ngoại ngữ',
    icon: 'globe',
    author: 'Lê Thu Hà',
    accent: 'magenta',
    minutesAgo: 600,
    readMinutes: 19,
    coins: 38,
    bookmarked: false
  },
  {
    id: 7,
    title: 'Hình học không gian: phương pháp toạ độ',
    categoryId: 'math',
    category: 'Toán',
    icon: 'chart',
    author: 'Nguyễn Minh Quân',
    accent: 'cyan',
    minutesAgo: 1500,
    readMinutes: 26,
    coins: 42,
    bookmarked: false
  },
  {
    id: 8,
    title: 'Cấu trúc dữ liệu cây và đồ thị',
    categoryId: 'code',
    category: 'Lập trình',
    icon: 'command',
    author: 'Trần Đức Anh',
    accent: 'green',
    minutesAgo: 2100,
    readMinutes: 35,
    coins: 60,
    bookmarked: false
  },
  {
    id: 9,
    title: 'Lịch sử các nền văn minh cổ đại',
    categoryId: 'skill',
    category: 'Kỹ năng',
    icon: 'book-open',
    author: 'Vũ Quốc Bảo',
    accent: 'amber',
    minutesAgo: 4000,
    readMinutes: 24,
    coins: 33,
    bookmarked: false
  },
  {
    id: 10,
    title: 'Hoá học hữu cơ: chuỗi phản ứng',
    categoryId: 'science',
    category: 'Khoa học',
    icon: 'rocket',
    author: 'Hoàng Anh Tú',
    accent: 'violet',
    minutesAgo: 5200,
    readMinutes: 30,
    coins: 48,
    bookmarked: false
  }
]);

const visibleCount = ref<number>(8);

const newToday = computed<number>(() => docs.value.filter((d) => d.minutesAgo < 1440).length);

const filtered = computed<NewDoc[]>(() =>
  docs.value
    .filter((d) => activeCategory.value === 'all' || d.categoryId === activeCategory.value)
    .sort((a, b) => a.minutesAgo - b.minutesAgo)
);
const visible = computed<NewDoc[]>(() => filtered.value.slice(0, visibleCount.value));
const hasMore = computed<boolean>(() => visibleCount.value < filtered.value.length);

interface Bucket {
  id: string;
  label: string;
  items: NewDoc[];
}
const buckets = computed<Bucket[]>(() => {
  const defs: { id: string; label: string; max: number }[] = [
    { id: 'live', label: 'Vừa cập nhật', max: 30 },
    { id: 'today', label: 'Hôm nay', max: 1440 },
    { id: 'yesterday', label: 'Hôm qua', max: 2880 },
    { id: 'week', label: 'Trong tuần', max: Number.POSITIVE_INFINITY }
  ];
  const out: Bucket[] = [];
  let prevMax = 0;
  for (const def of defs) {
    const items = visible.value.filter((d) => d.minutesAgo >= prevMax && d.minutesAgo < def.max);
    if (items.length) out.push({ id: def.id, label: def.label, items });
    prevMax = def.max;
  }
  return out;
});

function timeAgo(min: number): string {
  if (min < 60) return `${min} phút trước`;
  if (min < 1440) return `${Math.floor(min / 60)} giờ trước`;
  return `${Math.floor(min / 1440)} ngày trước`;
}

function toggleBookmark(doc: NewDoc): void {
  doc.bookmarked = !doc.bookmarked;
  if (doc.bookmarked) push('Đã lưu vào danh sách của bạn', { icon: 'bookmark', accent: 'cyan' });
}

const freshTemplates: Omit<NewDoc, 'id' | 'minutesAgo' | 'bookmarked'>[] = [
  {
    title: 'Mẹo giải nhanh phương trình lượng giác',
    categoryId: 'math',
    category: 'Toán',
    icon: 'chart',
    author: 'Nguyễn Minh Quân',
    accent: 'cyan',
    readMinutes: 14,
    coins: 36
  },
  {
    title: 'Phát âm chuẩn 44 âm tiếng Anh',
    categoryId: 'lang',
    category: 'Ngoại ngữ',
    icon: 'globe',
    author: 'Mai Phương Thảo',
    accent: 'magenta',
    readMinutes: 17,
    coins: 40
  },
  {
    title: 'Clean code với nguyên lý SOLID',
    categoryId: 'code',
    category: 'Lập trình',
    icon: 'command',
    author: 'Trần Đức Anh',
    accent: 'green',
    readMinutes: 25,
    coins: 52
  }
];
let refreshIdx = 0;
function refresh(): void {
  const tpl = freshTemplates[refreshIdx % freshTemplates.length];
  refreshIdx += 1;
  seq += 1;
  docs.value.unshift({ ...tpl, id: seq, minutesAgo: 1, bookmarked: false });
  if (visibleCount.value < filtered.value.length) visibleCount.value += 1;
  push('Đã tải nội dung mới nhất', { icon: 'refresh', accent: 'green' });
}

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
const accentDot: Record<Accent, string> = {
  cyan: 'bg-cyan-400',
  magenta: 'bg-fuchsia-400',
  green: 'bg-emerald-400',
  violet: 'bg-violet-400',
  amber: 'bg-amber-400'
};
</script>

<template>
  <div class="space-y-6">
    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">
            <span class="fx-glow-pulse inline-block h-2 w-2 rounded-full bg-emerald-400" /> Cập nhật
            trực tiếp
          </p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Mới</span> cập nhật
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            <span class="font-semibold text-emerald-300"><CountUp :value="newToday" /></span> tài
            liệu mới được thêm trong 24 giờ qua.
          </p>
        </div>
        <button
          type="button"
          class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-xl px-4 py-2.5 text-sm"
          @click="refresh"
        >
          <AppIcon name="refresh" class="h-4 w-4" /> Làm mới
        </button>
      </div>
    </GlassPanel>

    <!-- TOOLBAR -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div class="cosmic-scroll -mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="inline-flex shrink-0 items-center gap-1.5 rounded-xl border px-3.5 py-2 text-sm font-medium transition-all"
          :class="
            activeCategory === cat.id
              ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200 shadow-[inset_0_0_0_1px_oklch(0.78_0.18_195/30%)]'
              : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
          "
          @click="activeCategory = cat.id"
        >
          <AppIcon :name="cat.icon" class="h-4 w-4" /> {{ cat.label }}
        </button>
      </div>
      <label
        class="inline-flex shrink-0 cursor-pointer items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.03] px-3 py-2 text-xs text-muted-foreground"
      >
        <AppIcon name="bell" class="h-4 w-4" /> Thông báo khi có tài liệu mới
        <Toggle
          :model-value="notify"
          @update:model-value="
            (v) => {
              notify = v;
              push(v ? 'Đã bật thông báo cập nhật' : 'Đã tắt thông báo', {
                icon: 'bell',
                accent: v ? 'cyan' : 'rose'
              });
            }
          "
        />
      </label>
    </div>

    <!-- TIMELINE -->
    <div v-for="bucket in buckets" :key="bucket.id" class="space-y-3">
      <div class="flex items-center gap-3">
        <h2 class="text-sm font-semibold text-foreground">{{ bucket.label }}</h2>
        <span class="rounded-full bg-white/[0.06] px-2 py-0.5 text-xs text-muted-foreground">{{
          bucket.items.length
        }}</span>
        <div class="h-px flex-1 bg-gradient-to-r from-white/[0.1] to-transparent" />
      </div>
      <transition-group name="new-row" tag="ul" class="space-y-2.5">
        <li
          v-for="doc in bucket.items"
          :key="doc.id"
          class="fx-lift fx-sheen group relative flex items-center gap-3 overflow-hidden rounded-xl border border-white/[0.06] bg-white/[0.02] py-3 pl-4 pr-3"
        >
          <span class="absolute inset-y-0 left-0 w-1" :class="accentDot[doc.accent]" />
          <span
            class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl"
            :class="accentChip[doc.accent]"
          >
            <AppIcon :name="doc.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
              <span
                v-if="doc.minutesAgo < 30"
                class="fx-glow-pulse rounded-full bg-emerald-500/20 px-2 py-0.5 text-[0.6rem] font-bold uppercase tracking-wide text-emerald-300"
                >Mới</span
              >
              <span class="text-[0.7rem] font-semibold uppercase tracking-wide text-cyan-300">{{
                doc.category
              }}</span>
            </div>
            <p class="mt-0.5 truncate text-sm font-semibold text-foreground">{{ doc.title }}</p>
            <p class="mt-0.5 flex items-center gap-2 truncate text-xs text-muted-foreground">
              {{ doc.author }} · <AppIcon name="clock" class="h-3 w-3" />
              {{ timeAgo(doc.minutesAgo) }}
            </p>
          </div>
          <div class="hidden shrink-0 items-center gap-3 sm:flex">
            <span class="inline-flex items-center gap-1 text-xs text-muted-foreground"
              ><AppIcon name="clock" class="h-3.5 w-3.5" /> {{ doc.readMinutes }}p</span
            >
            <span
              class="inline-flex items-center gap-1 rounded-full bg-amber-500/10 px-2 py-0.5 text-xs font-semibold text-amber-300"
              ><AppIcon name="coin" class="h-3.5 w-3.5" /> {{ doc.coins }}</span
            >
          </div>
          <button
            type="button"
            class="shrink-0 rounded-lg p-2 transition-colors"
            :class="doc.bookmarked ? 'text-cyan-300' : 'text-muted-foreground hover:text-cyan-300'"
            :aria-label="doc.bookmarked ? 'Bỏ lưu' : 'Lưu tài liệu'"
            @click="toggleBookmark(doc)"
          >
            <AppIcon
              name="bookmark"
              class="h-4 w-4"
              :class="doc.bookmarked ? 'fill-current' : ''"
            />
          </button>
        </li>
      </transition-group>
    </div>

    <div v-if="hasMore" class="flex justify-center">
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-xl border border-white/[0.1] bg-white/[0.03] px-5 py-2.5 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
        @click="visibleCount += 4"
      >
        <AppIcon name="chevron-down" class="h-4 w-4" /> Tải thêm tài liệu
      </button>
    </div>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.new-row-enter-active {
  transition:
    opacity 0.4s ease,
    transform 0.4s ease;
}
.new-row-enter-from {
  opacity: 0;
  transform: translateX(-16px);
}
@media (prefers-reduced-motion: reduce) {
  .new-row-enter-active {
    transition: none;
  }
}
</style>
