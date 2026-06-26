<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import DocumentCard from '@/components/app/DocumentCard.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

// ─── Types ────────────────────────────────────────────────────────────────────

interface Category {
  id: string;
  label: string;
}

interface DocumentItem {
  title: string;
  category: string;
  meta: string;
  readMinutes?: number;
  progress?: number;
  coins?: number;
  rating?: number;
  locked?: boolean;
  bookmarked?: boolean;
  tone?: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
}

interface StatItem {
  label: string;
  value: string;
  unit?: string;
  icon: string;
  accent: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
  trend?: number;
  caption?: string;
  sparkline?: number[];
}

interface PageItem {
  label: string | number;
  value: number;
  active?: boolean;
}

// ─── State ────────────────────────────────────────────────────────────────────

const searchQuery = ref('');
const activeCategory = ref('all');
const viewMode = ref<'grid' | 'list'>('grid');
const activeSortLabel = ref('Mới nhất');
const showSortMenu = ref(false);

// ─── Static data ──────────────────────────────────────────────────────────────

const categories: Category[] = [
  { id: 'all', label: 'Tất cả' },
  { id: 'math', label: 'Toán học' },
  { id: 'physics', label: 'Vật lý' },
  { id: 'chemistry', label: 'Hoá học' },
  { id: 'language', label: 'Ngoại ngữ' },
  { id: 'cs', label: 'Tin học' },
  { id: 'history', label: 'Lịch sử' },
  { id: 'skill', label: 'Kỹ năng' },
];

const sortOptions: string[] = ['Mới nhất', 'Phổ biến nhất', 'Đánh giá cao', 'Ít xu nhất'];

const stats: StatItem[] = [
  {
    label: 'Tổng tài liệu',
    value: '248',
    icon: 'file-text',
    accent: 'cyan',
    trend: 5,
    caption: '+12 tuần này',
    sparkline: [180, 195, 205, 210, 220, 230, 240, 248],
  },
  {
    label: 'Đang đọc',
    value: '7',
    unit: 'tài liệu',
    icon: 'book-open',
    accent: 'violet',
    trend: 2,
    caption: '3 chưa mở',
    sparkline: [3, 4, 4, 5, 5, 6, 6, 7],
  },
  {
    label: 'Đã hoàn thành',
    value: '54',
    icon: 'check-circle',
    accent: 'green',
    trend: 8,
    caption: '+4 tuần này',
    sparkline: [30, 35, 38, 42, 45, 48, 51, 54],
  },
  {
    label: 'Đã lưu',
    value: '31',
    icon: 'bookmark',
    accent: 'amber',
    trend: 3,
    caption: 'Trong 5 bộ sưu tập',
    sparkline: [18, 20, 22, 24, 26, 28, 29, 31],
  },
];

const continueDocs: DocumentItem[] = [
  {
    title: 'Giải tích 1 — Giới hạn & Liên tục',
    category: 'Toán học',
    meta: 'Chương 3 · 24 trang',
    progress: 62,
    readMinutes: 12,
    tone: 'cyan',
    bookmarked: true,
  },
  {
    title: 'Lập trình hướng đối tượng với Java',
    category: 'Tin học',
    meta: 'Bài 8 · Lab thực hành',
    progress: 45,
    readMinutes: 20,
    tone: 'violet',
  },
  {
    title: 'Ngữ pháp tiếng Anh B2',
    category: 'Ngoại ngữ',
    meta: 'Unit 7 · Bài tập',
    progress: 38,
    readMinutes: 18,
    tone: 'green',
    bookmarked: true,
  },
];

const allDocuments: DocumentItem[] = [
  {
    title: 'Đại số tuyến tính — Ma trận & Định thức',
    category: 'Toán học',
    meta: 'PDF · 48 trang',
    readMinutes: 22,
    coins: 15,
    rating: 4.8,
    tone: 'cyan',
    bookmarked: true,
  },
  {
    title: 'Vật lý đại cương — Điện từ học',
    category: 'Vật lý',
    meta: 'Slide · 36 trang',
    readMinutes: 16,
    coins: 10,
    rating: 4.5,
    tone: 'violet',
  },
  {
    title: 'Hoá học hữu cơ — Tổng ôn THPT',
    category: 'Hoá học',
    meta: 'PDF · 40 trang',
    readMinutes: 18,
    coins: 20,
    rating: 4.7,
    tone: 'green',
    bookmarked: true,
  },
  {
    title: 'Cấu trúc dữ liệu & Giải thuật',
    category: 'Tin học',
    meta: 'PDF · 56 trang',
    readMinutes: 30,
    coins: 25,
    rating: 4.9,
    tone: 'magenta',
    locked: true,
  },
  {
    title: 'Tiếng Anh giao tiếp — Phát âm nâng cao',
    category: 'Ngoại ngữ',
    meta: 'Audio + PDF · 28 trang',
    readMinutes: 14,
    coins: 12,
    rating: 4.6,
    tone: 'amber',
  },
  {
    title: 'Lịch sử Việt Nam hiện đại 1945–1975',
    category: 'Lịch sử',
    meta: 'PDF · 62 trang',
    readMinutes: 28,
    coins: 18,
    rating: 4.4,
    tone: 'amber',
    bookmarked: true,
  },
  {
    title: 'Kỹ năng thuyết trình chuyên nghiệp',
    category: 'Kỹ năng',
    meta: 'Slide · 20 trang',
    readMinutes: 10,
    coins: 8,
    rating: 4.8,
    tone: 'cyan',
  },
  {
    title: 'Nhiệt động lực học — Chu trình Carnot',
    category: 'Vật lý',
    meta: 'PDF · 32 trang',
    readMinutes: 15,
    coins: 15,
    rating: 4.3,
    tone: 'violet',
    locked: true,
  },
  {
    title: 'Tư duy phản biện & Lập luận logic',
    category: 'Kỹ năng',
    meta: 'PDF · 44 trang',
    readMinutes: 20,
    coins: 30,
    rating: 4.9,
    tone: 'magenta',
    locked: true,
  },
  {
    title: 'Xác suất & Thống kê ứng dụng',
    category: 'Toán học',
    meta: 'PDF · 38 trang',
    readMinutes: 17,
    coins: 20,
    rating: 4.7,
    tone: 'cyan',
    bookmarked: true,
  },
  {
    title: 'Phản ứng hoá học — Tốc độ & Cân bằng',
    category: 'Hoá học',
    meta: 'Slide · 26 trang',
    readMinutes: 12,
    coins: 10,
    rating: 4.2,
    tone: 'green',
  },
  {
    title: 'Lập trình web với Vue 3 & TypeScript',
    category: 'Tin học',
    meta: 'PDF + Video · 72 trang',
    readMinutes: 35,
    coins: 40,
    rating: 5.0,
    tone: 'violet',
    locked: true,
    bookmarked: true,
  },
];

const pages: PageItem[] = [
  { label: 1, value: 1, active: true },
  { label: 2, value: 2 },
  { label: 3, value: 3 },
  { label: '...', value: -1 },
  { label: 8, value: 8 },
];

// ─── Derived ──────────────────────────────────────────────────────────────────

const filteredDocuments = computed(() => {
  if (activeCategory.value === 'all') return allDocuments;
  const map: Record<string, string> = {
    math: 'Toán học',
    physics: 'Vật lý',
    chemistry: 'Hoá học',
    language: 'Ngoại ngữ',
    cs: 'Tin học',
    history: 'Lịch sử',
    skill: 'Kỹ năng',
  };
  const label = map[activeCategory.value];
  return allDocuments.filter((d) => d.category === label);
});

// ─── Handlers ─────────────────────────────────────────────────────────────────

function selectSort(option: string): void {
  activeSortLabel.value = option;
  showSortMenu.value = false;
}
</script>

<template>
  <div class="space-y-6">
    <!-- ── Page header ─────────────────────────────────────────────────────── -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="cosmic-badge">Học liệu số</p>
        <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
          Thư viện <span class="cosmic-gradient-text">tài liệu</span>
        </h1>
        <p class="mt-1 text-sm text-muted-foreground">
          Khám phá, đọc và lưu tài liệu học tập từ cộng đồng.
        </p>
      </div>
      <button type="button" class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-xl px-4 py-2 text-sm">
        <AppIcon name="upload" class="h-4 w-4" />
        Tải lên tài liệu
      </button>
    </div>

    <!-- ── Stats row ───────────────────────────────────────────────────────── -->
    <section class="grid grid-cols-2 gap-4 md:grid-cols-4">
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

    <!-- ── Search + controls ───────────────────────────────────────────────── -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
      <!-- Search input -->
      <div class="relative flex-1">
        <AppIcon
          name="search"
          class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground/60"
        />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Tìm kiếm tài liệu, tác giả, chủ đề..."
          class="h-11 w-full rounded-xl border border-white/[0.08] bg-white/[0.03] pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground/70 focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-400/20"
        />
      </div>

      <!-- Sort button -->
      <div class="relative shrink-0">
        <button
          type="button"
          class="inline-flex h-11 items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-4 text-sm text-foreground hover:border-cyan-400/40 hover:text-cyan-200"
          @click="showSortMenu = !showSortMenu"
        >
          <AppIcon name="filter" class="h-4 w-4 text-muted-foreground" />
          {{ activeSortLabel }}
          <AppIcon name="chevron-down" class="h-3.5 w-3.5 text-muted-foreground" />
        </button>
        <!-- Dropdown -->
        <div
          v-if="showSortMenu"
          class="absolute right-0 top-[calc(100%+6px)] z-20 min-w-[160px] rounded-xl border border-white/[0.08] bg-[#0e0e1a] py-1.5 shadow-xl"
        >
          <button
            v-for="opt in sortOptions"
            :key="opt"
            type="button"
            class="flex w-full items-center gap-2 px-4 py-2 text-sm text-muted-foreground hover:bg-white/[0.04] hover:text-foreground"
            :class="opt === activeSortLabel ? 'text-cyan-300' : ''"
            @click="selectSort(opt)"
          >
            <AppIcon v-if="opt === activeSortLabel" name="check" class="h-3.5 w-3.5 text-cyan-400" />
            <span v-else class="h-3.5 w-3.5 shrink-0" />
            {{ opt }}
          </button>
        </div>
      </div>

      <!-- View mode toggle -->
      <div class="flex shrink-0 gap-1 rounded-xl border border-white/[0.08] bg-white/[0.03] p-1">
        <button
          type="button"
          class="flex h-[34px] w-[34px] items-center justify-center rounded-lg transition-colors"
          :class="
            viewMode === 'grid'
              ? 'bg-cyan-500/15 text-cyan-300'
              : 'text-muted-foreground hover:text-foreground'
          "
          @click="viewMode = 'grid'"
        >
          <AppIcon name="grid" class="h-4 w-4" />
        </button>
        <button
          type="button"
          class="flex h-[34px] w-[34px] items-center justify-center rounded-lg transition-colors"
          :class="
            viewMode === 'list'
              ? 'bg-cyan-500/15 text-cyan-300'
              : 'text-muted-foreground hover:text-foreground'
          "
          @click="viewMode = 'list'"
        >
          <AppIcon name="list" class="h-4 w-4" />
        </button>
      </div>
    </div>

    <!-- ── Category filter chips ───────────────────────────────────────────── -->
    <div class="flex flex-wrap gap-2">
      <button
        v-for="cat in categories"
        :key="cat.id"
        type="button"
        class="rounded-full border px-3 py-1 text-xs font-medium transition-colors"
        :class="
          activeCategory === cat.id
            ? 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200'
            : 'border-white/[0.06] bg-white/[0.04] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
        "
        @click="activeCategory = cat.id"
      >
        {{ cat.label }}
      </button>
    </div>

    <!-- ── Continue reading ────────────────────────────────────────────────── -->
    <SectionPanel
      title="Đang đọc tiếp"
      subtitle="Quay lại nơi bạn đã dừng"
      icon="play"
      accent="cyan"
      action-label="Xem tất cả"
    >
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <DocumentCard
          v-for="doc in continueDocs"
          :key="doc.title"
          :title="doc.title"
          :category="doc.category"
          :meta="doc.meta"
          :read-minutes="doc.readMinutes"
          :progress="doc.progress"
          :bookmarked="doc.bookmarked"
          :tone="doc.tone"
        />
      </div>
    </SectionPanel>

    <!-- ── All documents ───────────────────────────────────────────────────── -->
    <SectionPanel
      title="Tất cả tài liệu"
      :subtitle="`${filteredDocuments.length} tài liệu${activeCategory !== 'all' ? ' trong danh mục này' : ''}`"
      icon="layers"
      accent="violet"
    >
      <template #header-actions>
        <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs text-muted-foreground">
          Trang 1 / 8
        </span>
      </template>

      <!-- Grid view -->
      <div
        v-if="filteredDocuments.length > 0"
        class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
      >
        <DocumentCard
          v-for="doc in filteredDocuments"
          :key="doc.title"
          :title="doc.title"
          :category="doc.category"
          :meta="doc.meta"
          :read-minutes="doc.readMinutes"
          :coins="doc.coins"
          :rating="doc.rating"
          :locked="doc.locked"
          :bookmarked="doc.bookmarked"
          :tone="doc.tone"
        />
      </div>

      <!-- Empty state -->
      <div v-else class="flex flex-col items-center justify-center py-14 text-center">
        <span class="flex h-16 w-16 items-center justify-center rounded-2xl border border-white/[0.08] bg-white/[0.03] text-muted-foreground/60">
          <AppIcon name="inbox" class="h-8 w-8" />
        </span>
        <p class="mt-4 text-base font-semibold text-foreground">Không có tài liệu nào</p>
        <p class="mt-1.5 max-w-xs text-sm text-muted-foreground">
          Chưa có tài liệu nào trong danh mục này. Thử tìm ở danh mục khác hoặc tải lên tài liệu mới.
        </p>
        <div class="mt-5 flex flex-wrap justify-center gap-3">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-2 text-sm text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-200"
            @click="activeCategory = 'all'"
          >
            <AppIcon name="grid" class="h-4 w-4" />
            Xem tất cả danh mục
          </button>
          <button type="button" class="cosmic-btn inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
            <AppIcon name="upload" class="h-4 w-4" />
            Tải lên tài liệu
          </button>
        </div>
      </div>
    </SectionPanel>

    <!-- ── Advanced filter panel (decorative / tasteful empty state) ───────── -->
    <SectionPanel
      title="Bộ lọc nâng cao"
      subtitle="Thu hẹp kết quả theo tiêu chí cụ thể"
      icon="filter"
      accent="magenta"
    >
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <!-- Loại tài liệu -->
        <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
          <p class="mb-2.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">Loại tài liệu</p>
          <div class="flex flex-col gap-2">
            <label
              v-for="t in ['PDF', 'Slide', 'Video', 'Audio']"
              :key="t"
              class="flex cursor-pointer items-center gap-2.5 text-sm text-muted-foreground hover:text-foreground"
            >
              <span class="flex h-4 w-4 shrink-0 items-center justify-center rounded border border-white/[0.15] bg-white/[0.04]" />
              {{ t }}
            </label>
          </div>
        </div>

        <!-- Số trang -->
        <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
          <p class="mb-2.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">Độ dài</p>
          <div class="flex flex-col gap-2">
            <label
              v-for="r in ['Dưới 20 trang', '20–50 trang', '50–100 trang', 'Trên 100 trang']"
              :key="r"
              class="flex cursor-pointer items-center gap-2.5 text-sm text-muted-foreground hover:text-foreground"
            >
              <span class="flex h-4 w-4 shrink-0 items-center justify-center rounded border border-white/[0.15] bg-white/[0.04]" />
              {{ r }}
            </label>
          </div>
        </div>

        <!-- Xếp hạng -->
        <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
          <p class="mb-2.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">Đánh giá tối thiểu</p>
          <div class="flex flex-col gap-2">
            <label
              v-for="stars in [5, 4, 3, 2]"
              :key="stars"
              class="flex cursor-pointer items-center gap-2.5 text-sm text-muted-foreground hover:text-foreground"
            >
              <span class="flex h-4 w-4 shrink-0 items-center justify-center rounded border border-white/[0.15] bg-white/[0.04]" />
              <AppIcon name="star" class="h-3.5 w-3.5 text-amber-400" />
              {{ stars }} sao trở lên
            </label>
          </div>
        </div>

        <!-- Truy cập -->
        <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
          <p class="mb-2.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">Quyền truy cập</p>
          <div class="flex flex-col gap-2">
            <label
              v-for="a in ['Miễn phí', 'Trả xu', 'VIP', 'Đã tải lên bởi tôi']"
              :key="a"
              class="flex cursor-pointer items-center gap-2.5 text-sm text-muted-foreground hover:text-foreground"
            >
              <span class="flex h-4 w-4 shrink-0 items-center justify-center rounded border border-white/[0.15] bg-white/[0.04]" />
              {{ a }}
            </label>
          </div>
        </div>
      </div>

      <!-- Bộ lọc nâng cao — trạng thái trống -->
      <div class="mt-4 flex items-center justify-between rounded-xl border border-dashed border-white/[0.08] bg-white/[0.01] px-5 py-4">
        <div class="flex items-center gap-3">
          <AppIcon name="inbox" class="h-5 w-5 text-muted-foreground/50" />
          <p class="text-sm text-muted-foreground">Chưa có bộ lọc nào được áp dụng. Chọn tiêu chí ở trên để thu hẹp kết quả.</p>
        </div>
        <button
          type="button"
          class="shrink-0 text-xs text-muted-foreground hover:text-cyan-300"
        >
          Đặt lại
        </button>
      </div>

      <!-- Progress preview -->
      <div class="mt-4 space-y-2.5">
        <p class="text-xs font-medium text-muted-foreground">Tỷ lệ tài liệu theo danh mục</p>
        <div
          v-for="item in [
            { label: 'Toán học', pct: 28, accent: 'cyan' as const },
            { label: 'Tin học', pct: 22, accent: 'violet' as const },
            { label: 'Ngoại ngữ', pct: 18, accent: 'green' as const },
            { label: 'Vật lý', pct: 14, accent: 'amber' as const },
            { label: 'Khác', pct: 18, accent: 'magenta' as const },
          ]"
          :key="item.label"
          class="flex items-center gap-3"
        >
          <span class="w-20 shrink-0 text-right text-xs text-muted-foreground">{{ item.label }}</span>
          <div class="flex-1">
            <ProgressBar :value="item.pct" :accent="item.accent" :height="6" />
          </div>
          <span class="w-8 shrink-0 text-xs tabular-nums text-muted-foreground">{{ item.pct }}%</span>
        </div>
      </div>
    </SectionPanel>

    <!-- ── Pagination ──────────────────────────────────────────────────────── -->
    <div class="flex items-center justify-center gap-2 pt-2">
      <!-- Prev -->
      <button
        type="button"
        class="inline-flex h-9 w-9 items-center justify-center rounded-xl border border-white/[0.08] bg-white/[0.03] text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-200"
      >
        <AppIcon name="chevron-left" class="h-4 w-4" />
      </button>

      <!-- Pages -->
      <template v-for="page in pages" :key="page.value">
        <button
          type="button"
          class="inline-flex h-9 min-w-[36px] items-center justify-center rounded-xl border px-2.5 text-sm font-medium transition-colors"
          :class="
            page.active
              ? 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200'
              : page.value === -1
                ? 'cursor-default border-transparent bg-transparent text-muted-foreground/50 hover:border-transparent'
                : 'border-white/[0.08] bg-white/[0.03] text-muted-foreground hover:border-white/[0.15] hover:text-foreground'
          "
          :disabled="page.value === -1"
        >
          {{ page.label }}
        </button>
      </template>

      <!-- Next -->
      <button
        type="button"
        class="inline-flex h-9 w-9 items-center justify-center rounded-xl border border-white/[0.08] bg-white/[0.03] text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-200"
      >
        <AppIcon name="chevron-right" class="h-4 w-4" />
      </button>
    </div>

    <p class="pb-2 text-center text-xs text-muted-foreground/60">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
