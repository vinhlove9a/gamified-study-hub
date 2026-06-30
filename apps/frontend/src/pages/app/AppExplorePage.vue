<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import DocumentCard from '@/components/app/DocumentCard.vue';
import Avatar from '@/components/app/Avatar.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Tone = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type SortKey = 'trending' | 'newest' | 'rating';

interface Category {
  id: string;
  label: string;
  icon: string;
}

interface Doc {
  id: number;
  title: string;
  category: string;
  categoryId: string;
  meta: string;
  readMinutes: number;
  coins: number;
  rating: number;
  views: number;
  createdDaysAgo: number;
  trendScore: number;
  locked: boolean;
  bookmarked: boolean;
  tone: Tone;
  tags: string[];
}

interface Author {
  id: number;
  name: string;
  field: string;
  followers: number;
  accent: Accent;
  following: boolean;
}

interface TagCloudItem {
  id: string;
  label: string;
  weight: number;
}

interface PersonalPick {
  id: number;
  title: string;
  reason: string;
  icon: string;
  accent: Accent;
  readMinutes: number;
}

// ─── Categories ────────────────────────────────────────────────────────────

const categories: Category[] = [
  { id: 'all', label: 'Tất cả', icon: 'grid' },
  { id: 'math', label: 'Toán', icon: 'chart' },
  { id: 'lang', label: 'Ngoại ngữ', icon: 'globe' },
  { id: 'code', label: 'Lập trình', icon: 'command' },
  { id: 'science', label: 'Khoa học', icon: 'rocket' },
  { id: 'history', label: 'Lịch sử', icon: 'book-open' },
  { id: 'skill', label: 'Kỹ năng', icon: 'lightbulb' },
];

const activeCategory = ref<string>('all');
const sortKey = ref<SortKey>('trending');

const sortOptions: { id: SortKey; label: string }[] = [
  { id: 'trending', label: 'Thịnh hành' },
  { id: 'newest', label: 'Mới nhất' },
  { id: 'rating', label: 'Đánh giá cao' },
];

// ─── Search ───────────────────────────────────────────────────────────────

const searchQuery = ref<string>('');
const popularSearches: string[] = [
  'Đạo hàm tích phân',
  'TOEIC 800+',
  'Cấu trúc dữ liệu',
  'Vật lý lượng tử',
  'Tư duy phản biện',
  'Lịch sử Việt Nam',
];

function applyPopularSearch(term: string): void {
  searchQuery.value = term;
}

// ─── Tag cloud (also filters) ────────────────────────────────────────────────

const activeTag = ref<string>('');

const tagCloud: TagCloudItem[] = [
  { id: 'algebra', label: 'Đại số', weight: 5 },
  { id: 'toeic', label: 'TOEIC', weight: 4 },
  { id: 'python', label: 'Python', weight: 5 },
  { id: 'physics', label: 'Vật lý', weight: 3 },
  { id: 'essay', label: 'Viết luận', weight: 2 },
  { id: 'memory', label: 'Ghi nhớ', weight: 4 },
  { id: 'history-vn', label: 'Sử Việt', weight: 2 },
  { id: 'algorithm', label: 'Thuật toán', weight: 5 },
  { id: 'ielts', label: 'IELTS', weight: 3 },
  { id: 'calculus', label: 'Giải tích', weight: 4 },
  { id: 'softskill', label: 'Kỹ năng mềm', weight: 2 },
  { id: 'biology', label: 'Sinh học', weight: 3 },
];

function toggleTag(id: string): void {
  activeTag.value = activeTag.value === id ? '' : id;
}

const tagWeightClass: Record<number, string> = {
  1: 'text-xs',
  2: 'text-sm',
  3: 'text-base',
  4: 'text-lg',
  5: 'text-xl',
};

// ─── Documents ─────────────────────────────────────────────────────────────

const documents = ref<Doc[]>([
  {
    id: 1,
    title: 'Làm chủ đạo hàm: từ cơ bản đến nâng cao',
    category: 'Toán',
    categoryId: 'math',
    meta: 'Nguyễn Minh Quân · 12.4k lượt đọc',
    readMinutes: 22,
    coins: 40,
    rating: 4.8,
    views: 12400,
    createdDaysAgo: 3,
    trendScore: 98,
    locked: false,
    bookmarked: false,
    tone: 'cyan',
    tags: ['calculus', 'algebra'],
  },
  {
    id: 2,
    title: 'Chiến lược chinh phục TOEIC 800+ trong 60 ngày',
    category: 'Ngoại ngữ',
    categoryId: 'lang',
    meta: 'Lê Thu Hà · 9.1k lượt đọc',
    readMinutes: 18,
    coins: 35,
    rating: 4.9,
    views: 9100,
    createdDaysAgo: 1,
    trendScore: 95,
    locked: false,
    bookmarked: true,
    tone: 'magenta',
    tags: ['toeic', 'ielts'],
  },
  {
    id: 3,
    title: 'Thuật toán & cấu trúc dữ liệu với Python',
    category: 'Lập trình',
    categoryId: 'code',
    meta: 'Trần Đức Anh · 15.7k lượt đọc',
    readMinutes: 30,
    coins: 55,
    rating: 4.7,
    views: 15700,
    createdDaysAgo: 6,
    trendScore: 92,
    locked: true,
    bookmarked: false,
    tone: 'green',
    tags: ['python', 'algorithm'],
  },
  {
    id: 4,
    title: 'Nhập môn cơ học lượng tử cho người mới',
    category: 'Khoa học',
    categoryId: 'science',
    meta: 'Phạm Hải Yến · 6.3k lượt đọc',
    readMinutes: 26,
    coins: 45,
    rating: 4.6,
    views: 6300,
    createdDaysAgo: 2,
    trendScore: 88,
    locked: false,
    bookmarked: false,
    tone: 'violet',
    tags: ['physics', 'biology'],
  },
  {
    id: 5,
    title: 'Dòng chảy lịch sử Việt Nam thế kỷ 20',
    category: 'Lịch sử',
    categoryId: 'history',
    meta: 'Vũ Quốc Bảo · 4.8k lượt đọc',
    readMinutes: 24,
    coins: 30,
    rating: 4.5,
    views: 4800,
    createdDaysAgo: 9,
    trendScore: 74,
    locked: false,
    bookmarked: false,
    tone: 'amber',
    tags: ['history-vn'],
  },
  {
    id: 6,
    title: 'Kỹ thuật ghi nhớ Feynman & sơ đồ tư duy',
    category: 'Kỹ năng',
    categoryId: 'skill',
    meta: 'Đỗ Khánh Linh · 11.2k lượt đọc',
    readMinutes: 16,
    coins: 38,
    rating: 4.9,
    views: 11200,
    createdDaysAgo: 4,
    trendScore: 90,
    locked: false,
    bookmarked: true,
    tone: 'cyan',
    tags: ['memory', 'softskill'],
  },
  {
    id: 7,
    title: 'Tích phân ứng dụng trong vật lý',
    category: 'Toán',
    categoryId: 'math',
    meta: 'Nguyễn Minh Quân · 7.7k lượt đọc',
    readMinutes: 20,
    coins: 42,
    rating: 4.4,
    views: 7700,
    createdDaysAgo: 12,
    trendScore: 70,
    locked: false,
    bookmarked: false,
    tone: 'green',
    tags: ['calculus', 'physics'],
  },
  {
    id: 8,
    title: 'Viết luận tiếng Anh học thuật chuẩn IELTS',
    category: 'Ngoại ngữ',
    categoryId: 'lang',
    meta: 'Lê Thu Hà · 5.9k lượt đọc',
    readMinutes: 19,
    coins: 36,
    rating: 4.7,
    views: 5900,
    createdDaysAgo: 5,
    trendScore: 82,
    locked: true,
    bookmarked: false,
    tone: 'magenta',
    tags: ['ielts', 'essay'],
  },
]);

function toggleBookmark(id: number): void {
  const doc = documents.value.find((d) => d.id === id);
  if (doc) doc.bookmarked = !doc.bookmarked;
}

const filteredDocs = computed<Doc[]>(() => {
  const q = searchQuery.value.trim().toLowerCase();
  let list = documents.value.filter((doc) => {
    const matchCategory = activeCategory.value === 'all' || doc.categoryId === activeCategory.value;
    const matchTag = activeTag.value === '' || doc.tags.includes(activeTag.value);
    const matchQuery =
      q === '' || doc.title.toLowerCase().includes(q) || doc.category.toLowerCase().includes(q);
    return matchCategory && matchTag && matchQuery;
  });

  list = [...list].sort((a, b) => {
    if (sortKey.value === 'newest') return a.createdDaysAgo - b.createdDaysAgo;
    if (sortKey.value === 'rating') return b.rating - a.rating;
    return b.trendScore - a.trendScore;
  });

  return list;
});

const bookmarkedCount = computed<number>(() => documents.value.filter((d) => d.bookmarked).length);

// ─── Authors ───────────────────────────────────────────────────────────────

const authors = ref<Author[]>([
  { id: 1, name: 'Nguyễn Minh Quân', field: 'Toán cao cấp', followers: 18400, accent: 'cyan', following: false },
  { id: 2, name: 'Lê Thu Hà', field: 'Tiếng Anh học thuật', followers: 22100, accent: 'magenta', following: true },
  { id: 3, name: 'Trần Đức Anh', field: 'Khoa học máy tính', followers: 31500, accent: 'green', following: false },
  { id: 4, name: 'Phạm Hải Yến', field: 'Vật lý hiện đại', followers: 9700, accent: 'violet', following: false },
  { id: 5, name: 'Đỗ Khánh Linh', field: 'Phương pháp học tập', followers: 27800, accent: 'amber', following: false },
]);

function toggleFollow(id: number): void {
  const author = authors.value.find((a) => a.id === id);
  if (!author) return;
  author.following = !author.following;
  author.followers += author.following ? 1 : -1;
}

function formatFollowers(n: number): string {
  return n >= 1000 ? `${(n / 1000).toFixed(1)}k` : String(n);
}

const followingCount = computed<number>(() => authors.value.filter((a) => a.following).length);

// ─── Personalized picks ──────────────────────────────────────────────────────

const personalPicks: PersonalPick[] = [
  { id: 1, title: 'Ôn tập đạo hàm nhanh', reason: 'Vì bạn học Toán gần đây', icon: 'chart', accent: 'cyan', readMinutes: 12 },
  { id: 2, title: 'Từ vựng TOEIC chủ đề công sở', reason: 'Tiếp nối lộ trình của bạn', icon: 'globe', accent: 'magenta', readMinutes: 9 },
  { id: 3, title: 'Đệ quy & quay lui', reason: 'Phù hợp trình độ hiện tại', icon: 'command', accent: 'green', readMinutes: 17 },
  { id: 4, title: 'Sơ đồ tư duy hiệu quả', reason: 'Được học viên như bạn yêu thích', icon: 'lightbulb', accent: 'amber', readMinutes: 8 },
  { id: 5, title: 'Định luật Newton trực quan', reason: 'Mở rộng chủ đề Khoa học', icon: 'rocket', accent: 'violet', readMinutes: 14 },
];

// ─── KPI ──────────────────────────────────────────────────────────────────

const kpis = computed<{ label: string; value: string; icon: string; accent: Accent }[]>(() => [
  { label: 'Tài liệu mới tuần này', value: '128', icon: 'file-text', accent: 'cyan' },
  { label: 'Tác giả đang theo dõi', value: String(followingCount.value), icon: 'users', accent: 'magenta' },
  { label: 'Đã lưu của bạn', value: String(bookmarkedCount.value), icon: 'bookmark', accent: 'green' },
  { label: 'Chủ đề thịnh hành', value: String(tagCloud.length), icon: 'flame', accent: 'amber' },
]);

// ─── Accent helpers ──────────────────────────────────────────────────────────

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

const followBtnClass: Record<Accent, string> = {
  cyan: 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200 hover:bg-cyan-500/25',
  magenta: 'border-fuchsia-400/40 bg-fuchsia-500/15 text-fuchsia-200 hover:bg-fuchsia-500/25',
  green: 'border-emerald-400/40 bg-emerald-500/15 text-emerald-200 hover:bg-emerald-500/25',
  violet: 'border-violet-400/40 bg-violet-500/15 text-violet-200 hover:bg-violet-500/25',
  amber: 'border-amber-400/40 bg-amber-500/15 text-amber-200 hover:bg-amber-500/25',
};
</script>

<template>
  <div class="space-y-6">
    <!-- ───────────────── HERO ───────────────── -->
    <GlassPanel glow="cyan" class="p-5 sm:p-6">
      <p class="cosmic-badge">Khám phá kho tri thức</p>
      <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
        <span class="cosmic-gradient-text">Khám phá</span> điều bạn muốn học hôm nay
      </h1>
      <p class="mt-1 text-sm text-muted-foreground">
        Hàng nghìn tài liệu, lộ trình và tác giả truyền cảm hứng đang chờ bạn.
      </p>

      <!-- Search -->
      <div class="mt-5">
        <div class="relative">
          <span class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground">
            <AppIcon name="search" class="h-5 w-5" />
          </span>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm tài liệu, chủ đề, tác giả..."
            class="cosmic-input w-full !pl-12 !pr-12 py-3.5 text-sm"
          />
          <button
            v-if="searchQuery"
            type="button"
            aria-label="Xoá tìm kiếm"
            class="absolute right-3 top-1/2 -translate-y-1/2 flex h-7 w-7 items-center justify-center rounded-sm text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground"
            @click="searchQuery = ''"
          >
            <AppIcon name="x" class="h-4 w-4" />
          </button>
        </div>

        <div class="mt-3 flex flex-wrap items-center gap-2">
          <span class="text-xs text-muted-foreground">Tìm nhiều:</span>
          <button
            v-for="term in popularSearches"
            :key="term"
            type="button"
            class="rounded-sm border border-white/[0.08] bg-white/[0.03] px-3 py-1 text-xs text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
            @click="applyPopularSearch(term)"
          >
            {{ term }}
          </button>
        </div>
      </div>
    </GlassPanel>

    <!-- ───────────────── KPI TILES ───────────────── -->
    <section class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <GlassPanel v-for="kpi in kpis" :key="kpi.label" :glow="kpi.accent" hover padded>
        <div class="flex items-center gap-3">
          <span class="flex h-11 w-11 items-center justify-center rounded-sm" :class="accentChip[kpi.accent]">
            <AppIcon :name="kpi.icon" class="h-5 w-5" />
          </span>
          <div>
            <p class="text-xl font-bold tabular-nums text-foreground">{{ kpi.value }}</p>
            <p class="text-[0.7rem] text-muted-foreground">{{ kpi.label }}</p>
          </div>
        </div>
      </GlassPanel>
    </section>

    <!-- ───────────────── CATEGORY + SORT ───────────────── -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex flex-wrap gap-2">
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="inline-flex items-center gap-1.5 rounded-sm border px-3.5 py-2 text-sm font-medium transition-all"
          :class="activeCategory === cat.id
            ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
            : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'"
          @click="activeCategory = cat.id"
        >
          <AppIcon :name="cat.icon" class="h-4 w-4" />
          {{ cat.label }}
        </button>
      </div>

      <div class="inline-flex shrink-0 items-center gap-1 rounded-sm border border-white/[0.06] bg-white/[0.03] p-1">
        <AppIcon name="filter" class="ml-1.5 h-4 w-4 text-muted-foreground" />
        <button
          v-for="opt in sortOptions"
          :key="opt.id"
          type="button"
          class="rounded-sm px-3 py-1.5 text-xs font-medium transition-colors"
          :class="sortKey === opt.id
            ? 'bg-cyan-500/15 text-cyan-200'
            : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'"
          @click="sortKey = opt.id"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <!-- ───────────────── FEATURED COLLECTION BANNER ───────────────── -->
    <div class="featured-banner relative overflow-hidden rounded-sm border border-white/[0.08] p-6 sm:p-8">
      <div class="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div class="max-w-xl">
          <span class="inline-flex items-center gap-1.5 rounded-sm bg-white/15 px-3 py-1 text-xs font-semibold text-white backdrop-blur">
            <AppIcon name="sparkles" class="h-3.5 w-3.5" /> Bộ sưu tập nổi bật
          </span>
          <h2 class="mt-3 text-xl font-bold text-white sm:text-2xl">Hành trang mùa thi: 30 tài liệu chọn lọc</h2>
          <p class="mt-1.5 text-sm text-white/80">
            Tuyển tập do biên tập viên chọn lựa, đồng hành cùng bạn qua mùa ôn tập căng thẳng nhất năm.
          </p>
        </div>
        <button
          type="button"
          class="inline-flex shrink-0 items-center gap-2 rounded-sm bg-white px-5 py-3 text-sm font-semibold text-slate-900 transition-transform hover:scale-105"
        >
          Khám phá bộ sưu tập
          <AppIcon name="arrow-right" class="h-4 w-4" />
        </button>
      </div>
    </div>

    <!-- ───────────────── TRENDING GRID ───────────────── -->
    <SectionPanel
      title="Thịnh hành tuần này"
      :subtitle="`${filteredDocs.length} tài liệu phù hợp`"
      icon="flame"
      accent="magenta"
      flush
    >
      <transition-group
        name="card-fade"
        tag="div"
        class="grid grid-cols-1 gap-4 px-5 pb-5 sm:grid-cols-2 lg:grid-cols-3"
      >
        <DocumentCard
          v-for="doc in filteredDocs"
          :key="doc.id"
          class="doc-lift cursor-pointer"
          :title="doc.title"
          :category="doc.category"
          :meta="doc.meta"
          :read-minutes="doc.readMinutes"
          :coins="doc.coins"
          :rating="doc.rating"
          :locked="doc.locked"
          :bookmarked="doc.bookmarked"
          :tone="doc.tone"
          @click="toggleBookmark(doc.id)"
        />
      </transition-group>

      <p
        v-if="filteredDocs.length === 0"
        class="px-5 pb-6 pt-2 text-center text-sm text-muted-foreground"
      >
        Không tìm thấy tài liệu nào phù hợp. Thử bỏ bớt bộ lọc nhé!
      </p>
    </SectionPanel>

    <!-- ───────────────── PERSONALIZED ROW ───────────────── -->
    <SectionPanel title="Dành cho bạn" subtitle="Gợi ý theo lịch sử học tập" icon="sparkles" accent="cyan" flush>
      <div class="cosmic-scroll flex gap-4 overflow-x-auto px-5 pb-5">
        <article
          v-for="pick in personalPicks"
          :key="pick.id"
          class="doc-lift flex w-64 shrink-0 flex-col rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:border-white/[0.12]"
        >
          <span class="flex h-10 w-10 items-center justify-center rounded-sm" :class="accentChip[pick.accent]">
            <AppIcon :name="pick.icon" class="h-5 w-5" />
          </span>
          <h3 class="mt-3 text-sm font-semibold leading-snug text-foreground">{{ pick.title }}</h3>
          <p class="mt-1 text-xs text-muted-foreground">{{ pick.reason }}</p>
          <div class="mt-auto flex items-center justify-between pt-3">
            <span class="inline-flex items-center gap-1 text-xs text-muted-foreground">
              <AppIcon name="clock" class="h-3.5 w-3.5" /> {{ pick.readMinutes }}p
            </span>
            <span class="inline-flex items-center gap-1 text-xs font-medium text-cyan-300">
              Đọc ngay <AppIcon name="arrow-right" class="h-3.5 w-3.5" />
            </span>
          </div>
        </article>
      </div>
    </SectionPanel>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- ───────────────── FEATURED AUTHORS ───────────────── -->
      <div class="lg:col-span-2">
        <SectionPanel
          title="Tác giả nổi bật"
          :subtitle="`Bạn đang theo dõi ${followingCount} tác giả`"
          icon="users"
          accent="violet"
        >
          <ul class="space-y-3">
            <li
              v-for="author in authors"
              :key="author.id"
              class="flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-3 transition-colors hover:border-white/[0.12]"
            >
              <Avatar :name="author.name" :accent="author.accent" :size="44" />
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-semibold text-foreground">{{ author.name }}</p>
                <p class="truncate text-xs text-muted-foreground">
                  {{ author.field }} · {{ formatFollowers(author.followers) }} người theo dõi
                </p>
              </div>
              <button
                type="button"
                class="inline-flex shrink-0 items-center gap-1.5 rounded-sm border px-3.5 py-1.5 text-xs font-semibold transition-colors"
                :class="author.following
                  ? 'border-white/[0.12] bg-white/[0.04] text-muted-foreground hover:border-rose-400/40 hover:text-rose-300'
                  : followBtnClass[author.accent]"
                @click="toggleFollow(author.id)"
              >
                <AppIcon :name="author.following ? 'check' : 'plus'" class="h-3.5 w-3.5" />
                {{ author.following ? 'Đang theo dõi' : 'Theo dõi' }}
              </button>
            </li>
          </ul>
        </SectionPanel>
      </div>

      <!-- ───────────────── TAG CLOUD ───────────────── -->
      <SectionPanel title="Chủ đề thịnh hành" subtitle="Chạm để lọc nhanh" icon="layers" accent="amber">
        <div class="flex flex-wrap items-center gap-2.5">
          <button
            v-for="tag in tagCloud"
            :key="tag.id"
            type="button"
            class="rounded-sm border px-3 py-1 font-medium leading-none transition-all"
            :class="[
              tagWeightClass[tag.weight],
              activeTag === tag.id
                ? 'border-amber-400/50 bg-amber-500/15 text-amber-200'
                : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-amber-400/30 hover:text-amber-200',
            ]"
            @click="toggleTag(tag.id)"
          >
            #{{ tag.label }}
          </button>
        </div>
        <button
          v-if="activeTag"
          type="button"
          class="mt-4 inline-flex items-center gap-1.5 rounded-sm border border-white/[0.08] bg-white/[0.02] px-3 py-1.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
          @click="activeTag = ''"
        >
          <AppIcon name="x" class="h-3.5 w-3.5" /> Bỏ lọc chủ đề
        </button>
      </SectionPanel>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.featured-banner {
  background: linear-gradient(
    120deg,
    oklch(0.5 0.22 290) 0%,
    oklch(0.55 0.2 330) 45%,
    oklch(0.6 0.18 220) 100%
  );
  background-size: 180% 180%;
  animation: banner-shift 12s ease infinite;
}

.featured-banner::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 80% 20%, rgb(255 255 255 / 0.18), transparent 55%);
  pointer-events: none;
}

@keyframes banner-shift {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.doc-lift {
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.doc-lift:hover {
  transform: translateY(-4px);
}

.card-fade-enter-active,
.card-fade-leave-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}

.card-fade-enter-from,
.card-fade-leave-to {
  opacity: 0;
  transform: scale(0.96);
}

.card-fade-leave-active {
  position: absolute;
}

@media (prefers-reduced-motion: reduce) {
  .featured-banner,
  .doc-lift,
  .card-fade-enter-active,
  .card-fade-leave-active {
    animation: none;
    transition: none;
  }
}
</style>
