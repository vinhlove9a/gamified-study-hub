<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import RingProgress from '@/components/app/RingProgress.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Rating = 'hard' | 'medium' | 'easy';

interface Flashcard {
  id: number;
  front: string;
  back: string;
}

interface Deck {
  id: string;
  name: string;
  subject: string;
  icon: string;
  accent: Accent;
  mastery: number;
  due: number;
  cards: Flashcard[];
}

interface StatTile {
  label: string;
  value: string;
  unit?: string;
  icon: string;
  accent: Accent;
  trend?: number;
  caption?: string;
  sparkline?: number[];
}

interface RatingOption {
  id: Rating;
  label: string;
  accent: Accent;
  icon: string;
  hint: string;
}

interface Sparkle {
  id: number;
  left: number;
  top: number;
  delay: number;
  size: number;
}

// ─── Stats ────────────────────────────────────────────────────────────────────

const statTiles: StatTile[] = [
  {
    label: 'Tổng số thẻ',
    value: '486',
    icon: 'layers',
    accent: 'cyan',
    trend: 8,
    caption: 'Trên 6 bộ thẻ',
    sparkline: [320, 360, 380, 410, 440, 470, 486],
  },
  {
    label: 'Đến hạn ôn hôm nay',
    value: '37',
    unit: 'thẻ',
    icon: 'clock',
    accent: 'amber',
    caption: 'Ưu tiên ôn trước 21:00',
    sparkline: [12, 18, 9, 24, 30, 22, 37],
  },
  {
    label: 'Đã thuộc',
    value: '312',
    unit: 'thẻ',
    icon: 'check-circle',
    accent: 'green',
    trend: 5,
    caption: '64% bộ sưu tập',
    sparkline: [180, 210, 240, 260, 280, 300, 312],
  },
  {
    label: 'Chuỗi ôn ngày',
    value: '12',
    unit: 'ngày',
    icon: 'flame',
    accent: 'magenta',
    trend: 0,
    caption: 'Kỷ lục: 28 ngày',
    sparkline: [6, 7, 8, 9, 10, 11, 12],
  },
];

// ─── Decks ─────────────────────────────────────────────────────────────────────

const decks: Deck[] = [
  {
    id: 'eng-toeic',
    name: 'TOEIC 700+ Vocabulary',
    subject: 'Tiếng Anh',
    icon: 'book-open',
    accent: 'cyan',
    mastery: 72,
    due: 12,
    cards: [
      { id: 1, front: '"Mitigate" nghĩa là gì?', back: 'Làm giảm bớt, xoa dịu mức độ nghiêm trọng của điều gì đó.' },
      { id: 2, front: '"Procurement" nghĩa là gì?', back: 'Sự thu mua, quy trình mua sắm hàng hoá và dịch vụ cho doanh nghiệp.' },
      { id: 3, front: '"Tentative" nghĩa là gì?', back: 'Tạm thời, chưa chắc chắn — ví dụ: a tentative schedule (lịch tạm).' },
      { id: 4, front: '"Comprehensive" nghĩa là gì?', back: 'Toàn diện, bao quát đầy đủ mọi khía cạnh.' },
      { id: 5, front: '"Reimburse" nghĩa là gì?', back: 'Hoàn trả, bồi hoàn lại tiền đã chi.' },
    ],
  },
  {
    id: 'math-derivative',
    name: 'Đạo hàm & Tích phân',
    subject: 'Toán học',
    icon: 'chart',
    accent: 'violet',
    mastery: 58,
    due: 9,
    cards: [
      { id: 1, front: 'Đạo hàm của sin(x) là gì?', back: 'cos(x)' },
      { id: 2, front: 'Đạo hàm của ln(x) là gì?', back: '1 / x (với x > 0)' },
      { id: 3, front: 'Nguyên hàm của 1/x là gì?', back: 'ln|x| + C' },
      { id: 4, front: 'Đạo hàm của e^x là gì?', back: 'e^x — hàm số duy nhất bằng chính đạo hàm của nó.' },
      { id: 5, front: 'Quy tắc đạo hàm của tích u·v?', back: "(u·v)' = u'·v + u·v'" },
    ],
  },
  {
    id: 'history-vn',
    name: 'Lịch sử Việt Nam 1945-1975',
    subject: 'Lịch sử',
    icon: 'flag',
    accent: 'amber',
    mastery: 41,
    due: 6,
    cards: [
      { id: 1, front: 'Cách mạng Tháng Tám thành công năm nào?', back: 'Năm 1945, khai sinh nước Việt Nam Dân chủ Cộng hoà.' },
      { id: 2, front: 'Chiến thắng Điện Biên Phủ diễn ra năm nào?', back: 'Năm 1954, kết thúc kháng chiến chống Pháp.' },
      { id: 3, front: 'Hiệp định Genève ký kết năm nào?', back: 'Năm 1954, tạm chia đất nước tại vĩ tuyến 17.' },
      { id: 4, front: 'Đại thắng mùa Xuân thống nhất đất nước năm nào?', back: 'Năm 1975, ngày 30 tháng 4.' },
    ],
  },
  {
    id: 'bio-cell',
    name: 'Sinh học tế bào',
    subject: 'Sinh học',
    icon: 'sparkles',
    accent: 'green',
    mastery: 66,
    due: 4,
    cards: [
      { id: 1, front: 'Bào quan nào tạo năng lượng ATP?', back: 'Ti thể (mitochondria) — "nhà máy năng lượng" của tế bào.' },
      { id: 2, front: 'Quá trình quang hợp diễn ra ở đâu?', back: 'Lục lạp (chloroplast) trong tế bào thực vật.' },
      { id: 3, front: 'ADN được lưu trữ chủ yếu ở đâu?', back: 'Trong nhân tế bào (nucleus).' },
      { id: 4, front: 'Ribosome có chức năng gì?', back: 'Tổng hợp protein từ chuỗi mARN.' },
    ],
  },
  {
    id: 'chem-table',
    name: 'Bảng tuần hoàn hoá học',
    subject: 'Hoá học',
    icon: 'gem',
    accent: 'magenta',
    mastery: 35,
    due: 14,
    cards: [
      { id: 1, front: 'Ký hiệu hoá học của Natri?', back: 'Na (số hiệu nguyên tử 11).' },
      { id: 2, front: 'Nguyên tố nhẹ nhất là gì?', back: 'Hydro (H) — số hiệu nguyên tử 1.' },
      { id: 3, front: 'Khí trơ ở nhóm VIIIA gồm những nguyên tố nào?', back: 'He, Ne, Ar, Kr, Xe, Rn.' },
      { id: 4, front: 'Ký hiệu hoá học của Vàng?', back: 'Au (từ tiếng Latin "aurum").' },
      { id: 5, front: 'Số electron tối đa ở lớp ngoài cùng?', back: '8 electron (quy tắc bát tử).' },
    ],
  },
  {
    id: 'cs-algo',
    name: 'Cấu trúc dữ liệu & Giải thuật',
    subject: 'Tin học',
    icon: 'command',
    accent: 'cyan',
    mastery: 80,
    due: 2,
    cards: [
      { id: 1, front: 'Độ phức tạp của tìm kiếm nhị phân?', back: 'O(log n) — chia đôi không gian tìm kiếm mỗi bước.' },
      { id: 2, front: 'Stack hoạt động theo nguyên tắc nào?', back: 'LIFO — Last In, First Out.' },
      { id: 3, front: 'Queue hoạt động theo nguyên tắc nào?', back: 'FIFO — First In, First Out.' },
      { id: 4, front: 'Độ phức tạp trung bình của QuickSort?', back: 'O(n log n).' },
    ],
  },
];

// ─── Review session state ────────────────────────────────────────────────────

const activeDeck = ref<Deck | null>(null);
const cardIndex = ref<number>(0);
const flipped = ref<boolean>(false);
const finished = ref<boolean>(false);
const selectedRating = ref<Rating | null>(null);
const knownCount = ref<number>(0);
const accurateCount = ref<number>(0);

const ratingOptions: RatingOption[] = [
  { id: 'hard', label: 'Chưa thuộc', accent: 'magenta', icon: 'flame', hint: 'phím 1' },
  { id: 'medium', label: 'Phân vân', accent: 'amber', icon: 'help', hint: 'phím 2' },
  { id: 'easy', label: 'Đã thuộc', accent: 'green', icon: 'check-circle', hint: 'phím 3' },
];

const currentCard = computed<Flashcard | null>(() => {
  if (!activeDeck.value) return null;
  return activeDeck.value.cards[cardIndex.value] ?? null;
});

const totalCards = computed<number>(() => activeDeck.value?.cards.length ?? 0);

const reviewProgress = computed<number>(() => {
  if (totalCards.value === 0) return 0;
  return Math.round(((cardIndex.value + (finished.value ? 1 : 0)) / totalCards.value) * 100);
});

const accuracyPct = computed<number>(() => {
  if (totalCards.value === 0) return 0;
  return Math.round((accurateCount.value / totalCards.value) * 100);
});

const earnedXp = computed<number>(() => knownCount.value * 12 + accurateCount.value * 4);
const masteryGained = computed<number>(() => Math.round((accurateCount.value / Math.max(1, totalCards.value)) * 8));

// ─── Session actions ────────────────────────────────────────────────────────

function startDeck(deck: Deck): void {
  activeDeck.value = deck;
  cardIndex.value = 0;
  flipped.value = false;
  finished.value = false;
  selectedRating.value = null;
  knownCount.value = 0;
  accurateCount.value = 0;
}

function exitReview(): void {
  activeDeck.value = null;
  finished.value = false;
  stopSparkles();
}

function flipCard(): void {
  if (finished.value) return;
  flipped.value = !flipped.value;
}

function goPrev(): void {
  if (cardIndex.value > 0) {
    cardIndex.value -= 1;
    flipped.value = false;
    selectedRating.value = null;
  }
}

function goNext(): void {
  if (!activeDeck.value) return;
  if (cardIndex.value < totalCards.value - 1) {
    cardIndex.value += 1;
    flipped.value = false;
    selectedRating.value = null;
  } else {
    finishSession();
  }
}

function rateCard(rating: Rating): void {
  if (!flipped.value || finished.value) return;
  selectedRating.value = rating;
  if (rating === 'easy') {
    knownCount.value += 1;
    accurateCount.value += 1;
  } else if (rating === 'medium') {
    accurateCount.value += 1;
  }
  window.setTimeout(() => goNext(), 320);
}

function finishSession(): void {
  finished.value = true;
  launchSparkles();
}

function restartDeck(): void {
  if (activeDeck.value) startDeck(activeDeck.value);
}

// ─── Sparkle finish effect ────────────────────────────────────────────────────

const sparkles = ref<Sparkle[]>([]);
let sparkleTimer: number | null = null;

function launchSparkles(): void {
  const items: Sparkle[] = [];
  for (let i = 0; i < 28; i += 1) {
    items.push({
      id: i,
      left: Math.random() * 100,
      top: Math.random() * 100,
      delay: Math.random() * 0.6,
      size: 4 + Math.random() * 8,
    });
  }
  sparkles.value = items;
  sparkleTimer = window.setTimeout(() => {
    sparkles.value = [];
  }, 2600);
}

function stopSparkles(): void {
  sparkles.value = [];
  if (sparkleTimer !== null) {
    window.clearTimeout(sparkleTimer);
    sparkleTimer = null;
  }
}

// ─── Keyboard controls ────────────────────────────────────────────────────────

function onKeydown(e: KeyboardEvent): void {
  if (!activeDeck.value || finished.value) return;
  if (e.code === 'Space') {
    e.preventDefault();
    flipCard();
  } else if (e.code === 'ArrowLeft') {
    goPrev();
  } else if (e.code === 'ArrowRight') {
    goNext();
  } else if (flipped.value) {
    if (e.key === '1') rateCard('hard');
    else if (e.key === '2') rateCard('medium');
    else if (e.key === '3') rateCard('easy');
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown));
onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown);
  stopSparkles();
});

// ─── Accent helpers ────────────────────────────────────────────────────────────

const deckIconClasses: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

const ratingActiveClasses: Record<Accent, string> = {
  cyan: 'border-cyan-400/60 bg-cyan-500/20 text-cyan-200 shadow-[0_0_24px_oklch(0.78_0.18_195/45%)]',
  magenta: 'border-fuchsia-400/60 bg-fuchsia-500/20 text-fuchsia-200 shadow-[0_0_24px_oklch(0.68_0.22_350/45%)]',
  green: 'border-emerald-400/60 bg-emerald-500/20 text-emerald-200 shadow-[0_0_24px_oklch(0.75_0.18_145/45%)]',
  violet: 'border-violet-400/60 bg-violet-500/20 text-violet-200 shadow-[0_0_24px_oklch(0.62_0.2_290/45%)]',
  amber: 'border-amber-400/60 bg-amber-500/20 text-amber-200 shadow-[0_0_24px_oklch(0.8_0.16_85/45%)]',
};

const ratingIdleClasses: Record<Accent, string> = {
  cyan: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-200',
  magenta: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-fuchsia-400/40 hover:text-fuchsia-200',
  green: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-emerald-400/40 hover:text-emerald-200',
  violet: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-violet-400/40 hover:text-violet-200',
  amber: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-amber-400/40 hover:text-amber-200',
};
</script>

<template>
  <div class="space-y-6">
    <!-- Page header -->
    <GlassPanel glow="violet" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Ôn tập ngắt quãng (SRS)</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Thẻ ghi nhớ</span>
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Lật thẻ, tự đánh giá và để hệ thống lên lịch ôn lại đúng lúc bạn sắp quên.
          </p>
        </div>
        <div v-if="!activeDeck" class="flex shrink-0 items-center gap-3">
          <div class="flex items-center gap-2 rounded-xl border border-amber-400/20 bg-amber-500/10 px-4 py-2.5">
            <AppIcon name="clock" class="h-5 w-5 text-amber-300" />
            <div>
              <p class="text-sm font-bold leading-none text-amber-200">37 thẻ</p>
              <p class="mt-0.5 text-[0.65rem] text-muted-foreground">Đến hạn hôm nay</p>
            </div>
          </div>
          <div class="flex items-center gap-2 rounded-xl border border-fuchsia-400/20 bg-fuchsia-500/10 px-4 py-2.5">
            <AppIcon name="flame" class="h-5 w-5 text-fuchsia-300" />
            <div>
              <p class="text-sm font-bold leading-none text-fuchsia-200">12 ngày</p>
              <p class="mt-0.5 text-[0.65rem] text-muted-foreground">Chuỗi ôn</p>
            </div>
          </div>
        </div>
        <button
          v-else
          type="button"
          class="inline-flex shrink-0 items-center gap-2 self-start rounded-xl border border-white/10 bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40"
          @click="exitReview"
        >
          <AppIcon name="chevron-left" class="h-4 w-4" />
          Thư viện bộ thẻ
        </button>
      </div>
    </GlassPanel>

    <!-- ───────────────── LIBRARY VIEW ───────────────── -->
    <template v-if="!activeDeck">
      <section class="grid grid-cols-2 gap-4 md:grid-cols-4">
        <StatPanel
          v-for="stat in statTiles"
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

      <SectionPanel
        title="Thư viện bộ thẻ"
        subtitle="Chọn một bộ để bắt đầu phiên ôn tập"
        icon="layers"
        accent="violet"
      >
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <button
            v-for="deck in decks"
            :key="deck.id"
            type="button"
            class="deck-card group flex flex-col gap-4 rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4 text-left transition-all hover:-translate-y-1 hover:border-white/[0.14]"
            @click="startDeck(deck)"
          >
            <div class="flex items-start justify-between gap-3">
              <span
                class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl transition-transform group-hover:scale-110"
                :class="deckIconClasses[deck.accent]"
              >
                <AppIcon :name="deck.icon" class="h-6 w-6" />
              </span>
              <RingProgress :value="deck.mastery" :size="52" :stroke="6" :accent="deck.accent">
                <span class="text-[0.7rem] font-bold tabular-nums text-foreground">{{ deck.mastery }}%</span>
              </RingProgress>
            </div>

            <div class="min-w-0">
              <p class="truncate font-semibold text-foreground">{{ deck.name }}</p>
              <p class="mt-0.5 text-xs text-muted-foreground">{{ deck.subject }} · {{ deck.cards.length }} thẻ</p>
            </div>

            <div>
              <div class="mb-1.5 flex items-center justify-between text-xs">
                <span class="text-muted-foreground">Độ thành thạo</span>
                <span class="tabular-nums text-muted-foreground">{{ deck.mastery }}%</span>
              </div>
              <ProgressBar :value="deck.mastery" :accent="deck.accent" />
            </div>

            <div class="flex items-center justify-between">
              <span
                class="inline-flex items-center gap-1 rounded-full border border-amber-400/20 bg-amber-500/10 px-2.5 py-1 text-xs font-semibold text-amber-300"
              >
                <AppIcon name="clock" class="h-3.5 w-3.5" />
                {{ deck.due }} đến hạn
              </span>
              <span class="inline-flex items-center gap-1 text-xs font-medium text-cyan-300">
                Ôn ngay <AppIcon name="arrow-right" class="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5" />
              </span>
            </div>
          </button>
        </div>
      </SectionPanel>
    </template>

    <!-- ───────────────── REVIEW MODE ───────────────── -->
    <template v-else>
      <!-- Finished summary -->
      <template v-if="finished">
        <GlassPanel glow="green" class="relative overflow-hidden p-6 sm:p-8">
          <!-- sparkles -->
          <div class="pointer-events-none absolute inset-0">
            <span
              v-for="sp in sparkles"
              :key="sp.id"
              class="sparkle absolute rounded-full"
              :style="{
                left: sp.left + '%',
                top: sp.top + '%',
                width: sp.size + 'px',
                height: sp.size + 'px',
                animationDelay: sp.delay + 's',
              }"
            />
          </div>

          <div class="relative flex flex-col items-center gap-5 text-center">
            <span class="cosmic-badge">Hoàn thành phiên ôn</span>
            <RingProgress :value="accuracyPct" :size="148" :stroke="12" accent="green">
              <span class="text-3xl font-bold tabular-nums text-emerald-50">{{ accuracyPct }}%</span>
              <span class="text-xs text-muted-foreground">chính xác</span>
            </RingProgress>
            <h2 class="text-xl font-bold text-foreground">
              Tuyệt vời! Bạn đã ôn xong <span class="cosmic-gradient-text">{{ activeDeck.name }}</span>
            </h2>

            <div class="grid w-full max-w-lg grid-cols-2 gap-3 sm:grid-cols-4">
              <div class="rounded-xl border border-emerald-400/20 bg-emerald-500/[0.07] p-3">
                <p class="text-lg font-bold tabular-nums text-emerald-200">{{ knownCount }}</p>
                <p class="text-[0.65rem] text-muted-foreground">Đã thuộc</p>
              </div>
              <div class="rounded-xl border border-cyan-400/20 bg-cyan-500/[0.07] p-3">
                <p class="text-lg font-bold tabular-nums text-cyan-200">{{ totalCards }}</p>
                <p class="text-[0.65rem] text-muted-foreground">Tổng thẻ</p>
              </div>
              <div class="rounded-xl border border-violet-400/20 bg-violet-500/[0.07] p-3">
                <p class="text-lg font-bold tabular-nums text-violet-200">+{{ earnedXp }}</p>
                <p class="text-[0.65rem] text-muted-foreground">XP nhận</p>
              </div>
              <div class="rounded-xl border border-amber-400/20 bg-amber-500/[0.07] p-3">
                <p class="text-lg font-bold tabular-nums text-amber-200">+{{ masteryGained }}%</p>
                <p class="text-[0.65rem] text-muted-foreground">Thành thạo</p>
              </div>
            </div>

            <div class="flex flex-wrap items-center justify-center gap-3">
              <button type="button" class="cosmic-btn rounded-xl px-4 py-2 text-sm" @click="restartDeck">
                <AppIcon name="refresh" class="mr-1.5 inline h-4 w-4" />Ôn lại
              </button>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40"
                @click="exitReview"
              >
                <AppIcon name="grid" class="h-4 w-4" />Chọn bộ khác
              </button>
            </div>
          </div>
        </GlassPanel>
      </template>

      <!-- Active card -->
      <template v-else>
        <GlassPanel :glow="activeDeck.accent" class="p-5 sm:p-6">
          <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex items-center gap-3">
              <span
                class="flex h-10 w-10 items-center justify-center rounded-xl"
                :class="deckIconClasses[activeDeck.accent]"
              >
                <AppIcon :name="activeDeck.icon" class="h-5 w-5" />
              </span>
              <div>
                <p class="font-semibold text-foreground">{{ activeDeck.name }}</p>
                <p class="text-xs text-muted-foreground">{{ activeDeck.subject }}</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="rounded-full bg-emerald-500/15 px-2.5 py-1 text-xs font-semibold text-emerald-300">
                Đã thuộc {{ knownCount }}
              </span>
              <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs font-semibold text-muted-foreground">
                Thẻ {{ cardIndex + 1 }}/{{ totalCards }}
              </span>
            </div>
          </div>

          <ProgressBar :value="reviewProgress" :accent="activeDeck.accent" :height="8" />

          <!-- 3D flip card -->
          <div class="mt-6 flex justify-center">
            <button
              type="button"
              class="flip-scene w-full max-w-2xl"
              :aria-label="flipped ? 'Lật về câu hỏi' : 'Lật xem đáp án'"
              @click="flipCard"
            >
              <div class="flip-card" :class="{ 'is-flipped': flipped }">
                <!-- front -->
                <div class="flip-face flip-front">
                  <span class="cosmic-badge">Câu hỏi</span>
                  <p class="mt-4 text-xl font-semibold leading-relaxed text-foreground sm:text-2xl">
                    {{ currentCard?.front }}
                  </p>
                  <p class="mt-6 inline-flex items-center gap-1.5 text-xs text-muted-foreground">
                    <AppIcon name="refresh" class="h-3.5 w-3.5" />
                    Nhấn vào thẻ hoặc Space để lật
                  </p>
                </div>
                <!-- back -->
                <div class="flip-face flip-back">
                  <span class="cosmic-badge">Đáp án</span>
                  <p class="mt-4 text-lg leading-relaxed text-foreground sm:text-xl">
                    {{ currentCard?.back }}
                  </p>
                  <p class="mt-6 inline-flex items-center gap-1.5 text-xs text-emerald-300">
                    <AppIcon name="check-circle" class="h-3.5 w-3.5" />
                    Tự đánh giá mức độ ghi nhớ bên dưới
                  </p>
                </div>
              </div>
            </button>
          </div>

          <!-- rating buttons -->
          <transition name="fade-up">
            <div v-if="flipped" class="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
              <button
                v-for="opt in ratingOptions"
                :key="opt.id"
                type="button"
                class="flex items-center justify-center gap-2 rounded-xl border px-4 py-3 text-sm font-semibold transition-all"
                :class="selectedRating === opt.id ? ratingActiveClasses[opt.accent] : ratingIdleClasses[opt.accent]"
                @click="rateCard(opt.id)"
              >
                <AppIcon :name="opt.icon" class="h-4 w-4" />
                {{ opt.label }}
                <span class="text-[0.6rem] font-normal opacity-60">({{ opt.hint }})</span>
              </button>
            </div>
          </transition>

          <!-- navigation -->
          <div class="mt-5 flex items-center justify-between">
            <button
              type="button"
              class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-3.5 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40 disabled:cursor-not-allowed disabled:opacity-40"
              :disabled="cardIndex === 0"
              @click="goPrev"
            >
              <AppIcon name="chevron-left" class="h-4 w-4" />Trước
            </button>

            <span class="text-xs text-muted-foreground">
              Phím tắt: <span class="font-mono text-foreground">Space</span> lật ·
              <span class="font-mono text-foreground">← →</span> điều hướng ·
              <span class="font-mono text-foreground">1 2 3</span> đánh giá
            </span>

            <button
              type="button"
              class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-3.5 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40"
              @click="goNext"
            >
              {{ cardIndex === totalCards - 1 ? 'Kết thúc' : 'Sau' }}
              <AppIcon name="chevron-right" class="h-4 w-4" />
            </button>
          </div>
        </GlassPanel>
      </template>
    </template>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.flip-scene {
  perspective: 1400px;
}

.flip-card {
  position: relative;
  height: 280px;
  width: 100%;
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.flip-card.is-flipped {
  transform: rotateY(180deg);
}

.flip-face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.75rem;
  text-align: center;
  border-radius: 1rem;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  border: 1px solid rgb(255 255 255 / 0.1);
  background: linear-gradient(160deg, rgb(255 255 255 / 0.05), rgb(255 255 255 / 0.01));
}

.flip-front {
  box-shadow: inset 0 0 40px oklch(0.78 0.18 195 / 8%);
}

.flip-back {
  transform: rotateY(180deg);
  box-shadow: inset 0 0 40px oklch(0.75 0.18 145 / 10%);
}

.sparkle {
  background: radial-gradient(circle, oklch(0.85 0.18 145) 0%, transparent 70%);
  animation: sparkle-pop 1.6s ease-out forwards;
}

@keyframes sparkle-pop {
  0% {
    transform: scale(0) translateY(0);
    opacity: 0;
  }
  30% {
    transform: scale(1.2) translateY(-6px);
    opacity: 1;
  }
  100% {
    transform: scale(0.4) translateY(-40px);
    opacity: 0;
  }
}

.fade-up-enter-active {
  transition: all 0.3s ease;
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

@media (prefers-reduced-motion: reduce) {
  .flip-card,
  .sparkle,
  .fade-up-enter-active {
    transition: none;
    animation: none;
  }
}
</style>
