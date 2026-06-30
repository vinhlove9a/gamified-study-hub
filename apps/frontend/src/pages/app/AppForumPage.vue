<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import Avatar from '@/components/app/Avatar.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Category = 'Toán' | 'Ngoại ngữ' | 'Lập trình' | 'Khoa học' | 'Khác';
type SortKey = 'newest' | 'hot' | 'unanswered';

interface Thread {
  id: number;
  title: string;
  excerpt: string;
  author: string;
  authorAccent: Accent;
  category: Category;
  tags: string[];
  upvotes: number;
  upvoted: boolean;
  replies: number;
  views: number;
  ageMinutes: number;
  solved: boolean;
  pinned: boolean;
}

// ─── Static config ────────────────────────────────────────────────────────────

const categories: Array<{ id: Category | 'all'; label: string; icon: string }> = [
  { id: 'all', label: 'Tất cả', icon: 'grid' },
  { id: 'Toán', label: 'Toán', icon: 'chart' },
  { id: 'Ngoại ngữ', label: 'Ngoại ngữ', icon: 'globe' },
  { id: 'Lập trình', label: 'Lập trình', icon: 'command' },
  { id: 'Khoa học', label: 'Khoa học', icon: 'lightbulb' },
  { id: 'Khác', label: 'Khác', icon: 'more' }
];

const sortOptions: Array<{ id: SortKey; label: string; icon: string }> = [
  { id: 'newest', label: 'Mới nhất', icon: 'clock' },
  { id: 'hot', label: 'Nổi bật', icon: 'flame' },
  { id: 'unanswered', label: 'Chưa giải đáp', icon: 'help' }
];

const categoryAccent: Record<Category, Accent> = {
  Toán: 'cyan',
  'Ngoại ngữ': 'green',
  'Lập trình': 'violet',
  'Khoa học': 'amber',
  Khác: 'magenta'
};

const tagPill: Record<Accent, string> = {
  cyan: 'bg-cyan-500/10 text-cyan-300',
  magenta: 'bg-fuchsia-500/10 text-fuchsia-300',
  green: 'bg-emerald-500/10 text-emerald-300',
  violet: 'bg-violet-500/10 text-violet-300',
  amber: 'bg-amber-500/10 text-amber-300'
};

const composerCategories: Category[] = ['Toán', 'Ngoại ngữ', 'Lập trình', 'Khoa học', 'Khác'];

// ─── State ────────────────────────────────────────────────────────────────────

const threads = ref<Thread[]>([
  {
    id: 1,
    title: 'Cách chứng minh bất đẳng thức Cauchy-Schwarz đơn giản nhất?',
    excerpt: 'Mình đang ôn thi và gặp khó với phần bất đẳng thức. Có ai có cách tiếp cận trực quan không?',
    author: 'Minh Anh',
    authorAccent: 'cyan',
    category: 'Toán',
    tags: ['bất-đẳng-thức', 'lớp-11'],
    upvotes: 42,
    upvoted: false,
    replies: 8,
    views: 312,
    ageMinutes: 35,
    solved: true,
    pinned: true
  },
  {
    id: 2,
    title: '[Thông báo] Quy tắc đặt câu hỏi trên diễn đàn',
    excerpt: 'Hãy đặt tiêu đề rõ ràng, chọn đúng chủ đề và thêm tag phù hợp để được giải đáp nhanh hơn nhé!',
    author: 'Ban quản trị',
    authorAccent: 'violet',
    category: 'Khác',
    tags: ['nội-quy', 'hướng-dẫn'],
    upvotes: 128,
    upvoted: false,
    replies: 3,
    views: 1890,
    ageMinutes: 2880,
    solved: false,
    pinned: true
  },
  {
    id: 3,
    title: 'Phân biệt "make" và "do" trong tiếng Anh khi nào?',
    excerpt: 'Mình hay nhầm hai động từ này. Có mẹo nào để nhớ không mọi người?',
    author: 'Thu Hà',
    authorAccent: 'green',
    category: 'Ngoại ngữ',
    tags: ['ngữ-pháp', 'từ-vựng'],
    upvotes: 27,
    upvoted: false,
    replies: 12,
    views: 540,
    ageMinutes: 120,
    solved: true,
    pinned: false
  },
  {
    id: 4,
    title: 'Vue 3: nên dùng ref hay reactive cho object lớn?',
    excerpt: 'Khi state là một object nhiều thuộc tính lồng nhau, dùng ref hay reactive sẽ tối ưu hơn?',
    author: 'Bá Lộc',
    authorAccent: 'amber',
    category: 'Lập trình',
    tags: ['vue', 'typescript'],
    upvotes: 56,
    upvoted: false,
    replies: 6,
    views: 720,
    ageMinutes: 90,
    solved: false,
    pinned: false
  },
  {
    id: 5,
    title: 'Tại sao bầu trời lại có màu xanh?',
    excerpt: 'Mình biết liên quan đến tán xạ ánh sáng nhưng chưa hiểu rõ cơ chế. Ai giải thích giúp mình với.',
    author: 'Tuấn Kiệt',
    authorAccent: 'cyan',
    category: 'Khoa học',
    tags: ['vật-lý', 'ánh-sáng'],
    upvotes: 19,
    upvoted: false,
    replies: 0,
    views: 210,
    ageMinutes: 15,
    solved: false,
    pinned: false
  },
  {
    id: 6,
    title: 'Lời giải hệ phương trình bậc hai hai ẩn?',
    excerpt: 'Mình bị bí ở bước khử ẩn. Mong mọi người chỉ giúp các bước cụ thể.',
    author: 'Quốc Bảo',
    authorAccent: 'magenta',
    category: 'Toán',
    tags: ['phương-trình', 'lớp-10'],
    upvotes: 14,
    upvoted: false,
    replies: 0,
    views: 95,
    ageMinutes: 50,
    solved: false,
    pinned: false
  },
  {
    id: 7,
    title: 'Tài liệu luyện IELTS Writing band 7.0 nào tốt?',
    excerpt: 'Mình đang ở band 6.0, muốn tìm tài liệu và lộ trình để lên 7.0 trong 3 tháng.',
    author: 'Khánh Vy',
    authorAccent: 'violet',
    category: 'Ngoại ngữ',
    tags: ['ielts', 'writing'],
    upvotes: 33,
    upvoted: false,
    replies: 9,
    views: 612,
    ageMinutes: 240,
    solved: false,
    pinned: false
  },
  {
    id: 8,
    title: 'Sự khác nhau giữa axit và bazơ về bản chất?',
    excerpt: 'Theo thuyết Bronsted-Lowry thì axit và bazơ được định nghĩa thế nào?',
    author: 'Lan Phương',
    authorAccent: 'amber',
    category: 'Khoa học',
    tags: ['hoá-học', 'axit-bazơ'],
    upvotes: 22,
    upvoted: false,
    replies: 4,
    views: 188,
    ageMinutes: 480,
    solved: true,
    pinned: false
  }
]);

const activeCategory = ref<Category | 'all'>('all');
const activeSort = ref<SortKey>('newest');

const composerOpen = ref(false);
const newTitle = ref('');
const newBody = ref('');
const newCategory = ref<Category>('Toán');

let nextId = 9;

// ─── Derived ──────────────────────────────────────────────────────────────────

const filteredThreads = computed<Thread[]>(() => {
  let list = [...threads.value];
  if (activeCategory.value !== 'all') {
    list = list.filter((t) => t.category === activeCategory.value);
  }

  list.sort((a, b) => {
    switch (activeSort.value) {
      case 'hot':
        return b.upvotes + b.replies * 3 - (a.upvotes + a.replies * 3);
      case 'unanswered':
        return a.replies - b.replies || a.ageMinutes - b.ageMinutes;
      default:
        return a.ageMinutes - b.ageMinutes;
    }
  });

  // Pinned always float to top
  return [...list.filter((t) => t.pinned), ...list.filter((t) => !t.pinned)];
});

const topicCount = computed(() => threads.value.length);
const repliesToday = computed(() =>
  threads.value.filter((t) => t.ageMinutes < 1440).reduce((sum, t) => sum + t.replies, 0)
);
const solvedCount = computed(() => threads.value.filter((t) => t.solved).length);

const trendingTags = computed(() => {
  const counts = new Map<string, number>();
  for (const t of threads.value) {
    for (const tag of t.tags) {
      counts.set(tag, (counts.get(tag) ?? 0) + t.upvotes);
    }
  }
  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8)
    .map(([name, score]) => ({ name, score }));
});

const canPost = computed(() => newTitle.value.trim().length > 0 && newBody.value.trim().length > 0);

const categoryCount = computed(() => {
  const map: Record<string, number> = { all: threads.value.length };
  for (const c of composerCategories) {
    map[c] = threads.value.filter((t) => t.category === c).length;
  }
  return map;
});

// ─── Helpers ──────────────────────────────────────────────────────────────────

function timeAgo(minutes: number): string {
  if (minutes < 60) return `${minutes} phút trước`;
  if (minutes < 1440) return `${Math.floor(minutes / 60)} giờ trước`;
  return `${Math.floor(minutes / 1440)} ngày trước`;
}

function tagAccent(category: Category): Accent {
  return categoryAccent[category];
}

// ─── Handlers ─────────────────────────────────────────────────────────────────

function toggleUpvote(thread: Thread): void {
  thread.upvoted = !thread.upvoted;
  thread.upvotes += thread.upvoted ? 1 : -1;
}

function toggleComposer(): void {
  composerOpen.value = !composerOpen.value;
}

function publishThread(): void {
  if (!canPost.value) return;
  threads.value.unshift({
    id: nextId++,
    title: newTitle.value.trim(),
    excerpt: newBody.value.trim().slice(0, 160),
    author: 'Bạn',
    authorAccent: 'cyan',
    category: newCategory.value,
    tags: ['mới'],
    upvotes: 0,
    upvoted: false,
    replies: 0,
    views: 0,
    ageMinutes: 0,
    solved: false,
    pinned: false
  });
  newTitle.value = '';
  newBody.value = '';
  newCategory.value = 'Toán';
  composerOpen.value = false;
  activeSort.value = 'newest';
  activeCategory.value = 'all';
}

onUnmounted(() => {
  composerOpen.value = false;
});
</script>

<template>
  <div class="space-y-6">
    <!-- ── Hero ─────────────────────────────────────────────────────────────── -->
    <GlassPanel glow="violet" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Hỏi đáp cộng đồng</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Diễn đàn</span> học tập
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Đặt câu hỏi, chia sẻ kiến thức và cùng nhau giải đáp thắc mắc. Câu trả lời hay sẽ được cộng đồng bình chọn.
          </p>
        </div>
        <button
          type="button"
          class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-sm px-4 py-2 text-sm"
          @click="toggleComposer"
        >
          <AppIcon name="plus" class="h-4 w-4" />
          Đặt câu hỏi
        </button>
      </div>
    </GlassPanel>

    <!-- ── KPI row ───────────────────────────────────────────────────────────── -->
    <section class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <StatPanel label="Chủ đề" :value="String(topicCount)" icon="message" accent="violet" caption="Đang thảo luận" />
      <StatPanel label="Trả lời hôm nay" :value="String(repliesToday)" icon="check-circle" accent="green" :trend="14" caption="Cộng đồng năng động" />
      <StatPanel label="Đã giải đáp" :value="String(solvedCount)" icon="check" accent="cyan" caption="Câu hỏi có lời giải" />
      <StatPanel label="Thành viên" value="2.480" icon="users" accent="amber" :trend="5" caption="Đang tham gia diễn đàn" />
    </section>

    <!-- ── Composer ──────────────────────────────────────────────────────────── -->
    <Transition name="composer">
      <GlassPanel v-if="composerOpen" glow="cyan" class="overflow-hidden">
        <div class="space-y-4 p-5">
          <div class="flex items-center justify-between">
            <p class="flex items-center gap-2 text-sm font-semibold text-foreground">
              <span class="flex h-8 w-8 items-center justify-center rounded-sm bg-cyan-500/10 text-cyan-300">
                <AppIcon name="edit" class="h-4 w-4" />
              </span>
              Đặt câu hỏi mới
            </p>
            <button
              type="button"
              class="flex h-8 w-8 items-center justify-center rounded-sm text-muted-foreground hover:bg-white/[0.06] hover:text-foreground"
              aria-label="Đóng trình soạn thảo"
              @click="toggleComposer"
            >
              <AppIcon name="x" class="h-5 w-5" />
            </button>
          </div>

          <input
            v-model="newTitle"
            type="text"
            placeholder="Tiêu đề câu hỏi — hãy rõ ràng và ngắn gọn"
            class="cosmic-input"
            aria-label="Tiêu đề câu hỏi"
          />

          <textarea
            v-model="newBody"
            rows="4"
            placeholder="Mô tả chi tiết vấn đề của bạn, kèm những gì bạn đã thử…"
            class="cosmic-input resize-none"
            aria-label="Nội dung câu hỏi"
          />

          <div class="flex flex-wrap items-center justify-between gap-3">
            <div class="flex flex-wrap items-center gap-2">
              <span class="text-xs font-medium text-muted-foreground">Chủ đề:</span>
              <button
                v-for="c in composerCategories"
                :key="c"
                type="button"
                class="rounded-sm px-2.5 py-1 text-xs font-medium transition-colors"
                :class="
                  newCategory === c
                    ? 'bg-cyan-500/15 text-cyan-200'
                    : 'border border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:text-foreground'
                "
                @click="newCategory = c"
              >
                {{ c }}
              </button>
            </div>
            <button
              type="button"
              class="cosmic-btn inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm"
              :disabled="!canPost"
              @click="publishThread"
            >
              <AppIcon name="arrow-up" class="h-4 w-4" />
              Đăng
            </button>
          </div>
        </div>
      </GlassPanel>
    </Transition>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Left column -->
      <div class="space-y-5 lg:col-span-2">
        <!-- Category chips + sort -->
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div class="cosmic-scroll flex gap-2 overflow-x-auto pb-1">
            <button
              v-for="c in categories"
              :key="c.id"
              type="button"
              class="inline-flex shrink-0 items-center gap-1.5 rounded-sm px-3 py-1.5 text-xs font-medium transition-colors"
              :class="
                activeCategory === c.id
                  ? 'bg-cyan-500/15 text-cyan-200'
                  : 'border border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:text-foreground'
              "
              @click="activeCategory = c.id"
            >
              <AppIcon :name="c.icon" class="h-3.5 w-3.5" />
              {{ c.label }}
              <span class="rounded-sm bg-white/10 px-1.5 text-[0.6rem] font-semibold">{{ categoryCount[c.id] ?? 0 }}</span>
            </button>
          </div>

          <div class="inline-flex shrink-0 items-center gap-1 rounded-sm border border-white/[0.06] bg-white/[0.03] p-1">
            <button
              v-for="s in sortOptions"
              :key="s.id"
              type="button"
              class="inline-flex items-center gap-1.5 rounded-sm px-2.5 py-1.5 text-xs font-medium transition-colors"
              :class="
                activeSort === s.id
                  ? 'bg-violet-500/15 text-violet-200'
                  : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'
              "
              @click="activeSort = s.id"
            >
              <AppIcon :name="s.icon" class="h-3.5 w-3.5" />
              <span class="hidden sm:inline">{{ s.label }}</span>
            </button>
          </div>
        </div>

        <!-- Thread list -->
        <ul class="space-y-3">
          <li
            v-for="t in filteredThreads"
            :key="t.id"
            class="group flex gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:border-white/[0.14] hover:bg-white/[0.035]"
            :class="t.pinned ? 'ring-1 ring-amber-400/20' : ''"
          >
            <!-- Upvote -->
            <div class="flex shrink-0 flex-col items-center gap-1">
              <button
                type="button"
                class="flex h-9 w-9 items-center justify-center rounded-sm border transition-all"
                :class="
                  t.upvoted
                    ? 'upvote-pop border-cyan-400/50 bg-cyan-500/20 text-cyan-200'
                    : 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-300'
                "
                :aria-label="t.upvoted ? 'Bỏ bình chọn' : 'Bình chọn câu hỏi'"
                @click="toggleUpvote(t)"
              >
                <AppIcon name="arrow-up" class="h-4 w-4" />
              </button>
              <span class="text-sm font-bold tabular-nums" :class="t.upvoted ? 'text-cyan-200' : 'text-foreground'">{{ t.upvotes }}</span>
            </div>

            <!-- Body -->
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <span
                  v-if="t.pinned"
                  class="inline-flex items-center gap-1 rounded-sm bg-amber-500/15 px-1.5 py-0.5 text-[0.62rem] font-semibold text-amber-300"
                >
                  <AppIcon name="bookmark" class="h-3 w-3" /> Ghim
                </span>
                <span
                  v-if="t.solved"
                  class="solved-glow inline-flex items-center gap-1 rounded-sm bg-emerald-500/15 px-1.5 py-0.5 text-[0.62rem] font-semibold text-emerald-300"
                >
                  <AppIcon name="check-circle" class="h-3 w-3" /> Đã giải đáp
                </span>
                <span class="rounded-sm px-1.5 py-0.5 text-[0.62rem] font-semibold" :class="tagPill[tagAccent(t.category)]">
                  {{ t.category }}
                </span>
              </div>

              <h3 class="mt-1.5 text-[0.95rem] font-semibold leading-snug text-foreground transition-colors group-hover:text-cyan-200">
                {{ t.title }}
              </h3>
              <p class="mt-1 line-clamp-2 text-xs leading-relaxed text-muted-foreground">{{ t.excerpt }}</p>

              <!-- Tag pills -->
              <div class="mt-2 flex flex-wrap gap-1.5">
                <span
                  v-for="tag in t.tags"
                  :key="tag"
                  class="rounded-sm bg-white/[0.04] px-2 py-0.5 text-[0.65rem] text-muted-foreground"
                >
                  #{{ tag }}
                </span>
              </div>

              <!-- Meta -->
              <div class="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-[0.7rem] text-muted-foreground">
                <span class="flex items-center gap-1.5">
                  <Avatar :name="t.author" :accent="t.authorAccent" :size="20" />
                  <span class="font-medium text-foreground/80">{{ t.author }}</span>
                </span>
                <span class="flex items-center gap-1"><AppIcon name="message" class="h-3.5 w-3.5" /> {{ t.replies }} trả lời</span>
                <span class="flex items-center gap-1"><AppIcon name="eye" class="h-3.5 w-3.5" /> {{ t.views.toLocaleString('vi-VN') }}</span>
                <span class="flex items-center gap-1"><AppIcon name="clock" class="h-3.5 w-3.5" /> {{ timeAgo(t.ageMinutes) }}</span>
              </div>
            </div>
          </li>
        </ul>

        <div v-if="filteredThreads.length === 0" class="flex flex-col items-center justify-center rounded-sm border border-dashed border-white/[0.08] bg-white/[0.01] py-16 text-center">
          <AppIcon name="inbox" class="h-8 w-8 text-muted-foreground/50" />
          <p class="mt-3 text-sm font-medium text-foreground">Chưa có chủ đề nào</p>
          <p class="mt-1 max-w-xs text-xs text-muted-foreground">Hãy là người đầu tiên đặt câu hỏi trong chủ đề này.</p>
        </div>
      </div>

      <!-- Right column -->
      <div class="space-y-6">
        <SectionPanel title="Tag thịnh hành" subtitle="Theo lượt quan tâm" icon="flame" accent="amber">
          <div class="flex flex-wrap gap-2">
            <span
              v-for="tag in trendingTags"
              :key="tag.name"
              class="inline-flex items-center gap-1.5 rounded-sm border border-white/[0.08] bg-white/[0.02] px-2.5 py-1.5 text-xs text-muted-foreground transition-colors hover:border-amber-400/40 hover:text-amber-200"
            >
              <AppIcon name="trending-up" class="h-3.5 w-3.5 text-amber-300" />
              #{{ tag.name }}
              <span class="rounded-sm bg-white/10 px-1.5 text-[0.6rem] font-semibold">{{ tag.score }}</span>
            </span>
          </div>
        </SectionPanel>

        <SectionPanel title="Người đóng góp nổi bật" subtitle="Tuần này" icon="award" accent="violet" flush>
          <ul class="divide-y divide-white/[0.05]">
            <li class="flex items-center gap-3 px-5 py-2.5">
              <span class="flex h-7 w-7 items-center justify-center rounded-sm bg-amber-400/20 text-xs font-bold text-amber-300">1</span>
              <Avatar name="Minh Anh" accent="cyan" :size="32" />
              <span class="flex-1 truncate text-sm font-medium text-foreground">Minh Anh</span>
              <span class="text-xs font-semibold text-violet-300">18 trả lời</span>
            </li>
            <li class="flex items-center gap-3 px-5 py-2.5">
              <span class="flex h-7 w-7 items-center justify-center rounded-sm bg-slate-300/15 text-xs font-bold text-slate-200">2</span>
              <Avatar name="Bá Lộc" accent="amber" :size="32" />
              <span class="flex-1 truncate text-sm font-medium text-foreground">Bá Lộc</span>
              <span class="text-xs font-semibold text-violet-300">14 trả lời</span>
            </li>
            <li class="flex items-center gap-3 px-5 py-2.5">
              <span class="flex h-7 w-7 items-center justify-center rounded-sm bg-orange-400/20 text-xs font-bold text-orange-300">3</span>
              <Avatar name="Khánh Vy" accent="violet" :size="32" />
              <span class="flex-1 truncate text-sm font-medium text-foreground">Khánh Vy</span>
              <span class="text-xs font-semibold text-violet-300">11 trả lời</span>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Mẹo hỏi đáp hiệu quả" icon="lightbulb" accent="green">
          <ul class="space-y-3 text-sm leading-snug text-muted-foreground">
            <li class="flex gap-2.5">
              <AppIcon name="check" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-300" />
              Đặt tiêu đề cụ thể, đúng trọng tâm câu hỏi.
            </li>
            <li class="flex gap-2.5">
              <AppIcon name="check" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-300" />
              Mô tả những gì bạn đã thử để người khác dễ hỗ trợ.
            </li>
            <li class="flex gap-2.5">
              <AppIcon name="check" class="mt-0.5 h-4 w-4 shrink-0 text-emerald-300" />
              Chọn đúng chủ đề và thêm tag liên quan.
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <p class="pb-2 text-center text-xs text-muted-foreground/60">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
/* Composer expand */
.composer-enter-active,
.composer-leave-active {
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  overflow: hidden;
}
.composer-enter-from,
.composer-leave-to {
  opacity: 0;
  transform: translateY(-10px);
  max-height: 0;
}
.composer-enter-to,
.composer-leave-from {
  max-height: 520px;
}

/* Upvote pulse */
.upvote-pop {
  animation: upvote-pulse 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
@keyframes upvote-pulse {
  0% {
    transform: scale(1);
  }
  45% {
    transform: scale(1.22);
    box-shadow: 0 0 16px oklch(0.78 0.18 195 / 55%);
  }
  100% {
    transform: scale(1);
  }
}

/* Solved badge glow */
.solved-glow {
  animation: solved-breathe 2.6s ease-in-out infinite;
}
@keyframes solved-breathe {
  0%,
  100% {
    box-shadow: 0 0 0 oklch(0.75 0.18 145 / 0%);
  }
  50% {
    box-shadow: 0 0 12px oklch(0.75 0.18 145 / 45%);
  }
}
</style>
