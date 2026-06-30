<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import RingProgress from '@/components/app/RingProgress.vue';

// ─── SM-2 Spaced Repetition Algorithm ───────────────────────────────────────
// Implements a simplified SM-2: each card tracks ease factor (EF), interval (days),
// repetitions, lapses, and next review date. Rating 0-5 maps to hard/medium/easy.

interface SM2State {
  ease: number;      // ease factor, starts at 2.5, min 1.3
  interval: number;  // days until next review
  reps: number;      // consecutive correct reviews
  lapses: number;    // total failures
  due: number;       // timestamp (ms) of next review
  lastReview: number; // timestamp of last review (ms), 0 if never
}

const DAY_MS = 86_400_000;

function freshSM2(): SM2State {
  return { ease: 2.5, interval: 0, reps: 0, lapses: 0, due: Date.now(), lastReview: 0 };
}

function sm2Review(state: SM2State, quality: 0 | 3 | 5): SM2State {
  const now = Date.now();
  const s = { ...state, lastReview: now };

  if (quality < 3) {
    s.reps = 0;
    s.lapses += 1;
    s.interval = 1;
    s.ease = Math.max(1.3, s.ease - 0.2);
  } else {
    s.reps += 1;
    if (s.reps === 1) s.interval = 1;
    else if (s.reps === 2) s.interval = 3;
    else s.interval = Math.round(s.interval * s.ease);
    s.ease = Math.max(1.3, s.ease + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));
  }

  s.due = now + s.interval * DAY_MS;
  return s;
}

function isDue(state: SM2State): boolean {
  return Date.now() >= state.due;
}

function daysUntilDue(state: SM2State): number {
  return Math.ceil((state.due - Date.now()) / DAY_MS);
}

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Rating = 'hard' | 'medium' | 'easy';
type QualityMap = { hard: 0; medium: 3; easy: 5 };
const qualityMap: QualityMap = { hard: 0, medium: 3, easy: 5 };

interface Flashcard {
  id: number;
  front: string;
  back: string;
  sm2: SM2State;
}

interface Deck {
  id: string;
  name: string;
  subject: string;
  icon: string;
  accent: Accent;
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

// ─── Deck data with SM-2 state per card ─────────────────────────────────────

function makeCard(id: number, front: string, back: string, daysAgoReviewed?: number): Flashcard {
  const sm2 = freshSM2();
  if (daysAgoReviewed !== undefined) {
    sm2.lastReview = Date.now() - daysAgoReviewed * DAY_MS;
    sm2.reps = Math.floor(daysAgoReviewed / 3) + 1;
    sm2.interval = daysAgoReviewed > 6 ? 4 : 2;
    sm2.ease = 2.3 + Math.random() * 0.4;
    sm2.due = Date.now() + (Math.random() > 0.4 ? -DAY_MS : DAY_MS * 2);
  }
  return { id, front, back, sm2 };
}

const decks: Deck[] = [
  {
    id: 'eng-toeic', name: 'TOEIC 700+ Vocabulary', subject: 'Tiếng Anh', icon: 'book-open', accent: 'cyan',
    cards: [
      makeCard(1, '"Mitigate" nghĩa là gì?', 'Làm giảm bớt, xoa dịu mức độ nghiêm trọng của điều gì đó.', 2),
      makeCard(2, '"Procurement" nghĩa là gì?', 'Sự thu mua, quy trình mua sắm hàng hoá và dịch vụ cho doanh nghiệp.', 5),
      makeCard(3, '"Tentative" nghĩa là gì?', 'Tạm thời, chưa chắc chắn — ví dụ: a tentative schedule (lịch tạm).', 0),
      makeCard(4, '"Comprehensive" nghĩa là gì?', 'Toàn diện, bao quát đầy đủ mọi khía cạnh.', 1),
      makeCard(5, '"Reimburse" nghĩa là gì?', 'Hoàn trả, bồi hoàn lại tiền đã chi.', 3),
      makeCard(6, '"Endorse" nghĩa là gì?', 'Chứng nhận, tán thành, ký hậu.', 7),
      makeCard(7, '"Prevalent" nghĩa là gì?', 'Phổ biến, thịnh hành.', 0),
      makeCard(8, '"Scrutinize" nghĩa là gì?', 'Xem xét kỹ lưỡng, soi xét.', 4),
    ],
  },
  {
    id: 'math-derivative', name: 'Đạo hàm & Tích phân', subject: 'Toán học', icon: 'chart', accent: 'violet',
    cards: [
      makeCard(1, 'Đạo hàm của sin(x) là gì?', 'cos(x)', 1),
      makeCard(2, 'Đạo hàm của ln(x) là gì?', '1 / x (với x > 0)', 0),
      makeCard(3, 'Nguyên hàm của 1/x là gì?', 'ln|x| + C', 3),
      makeCard(4, 'Đạo hàm của e^x là gì?', 'e^x — hàm số duy nhất bằng chính đạo hàm của nó.', 5),
      makeCard(5, 'Quy tắc đạo hàm của tích u·v?', "(u·v)' = u'·v + u·v'", 2),
      makeCard(6, 'Đạo hàm của tan(x) là gì?', '1 + tan²(x) = 1/cos²(x)', 0),
    ],
  },
  {
    id: 'history-vn', name: 'Lịch sử Việt Nam 1945-1975', subject: 'Lịch sử', icon: 'flag', accent: 'amber',
    cards: [
      makeCard(1, 'Cách mạng Tháng Tám thành công năm nào?', 'Năm 1945, khai sinh nước Việt Nam Dân chủ Cộng hoà.', 0),
      makeCard(2, 'Chiến thắng Điện Biên Phủ diễn ra năm nào?', 'Năm 1954, kết thúc kháng chiến chống Pháp.', 2),
      makeCard(3, 'Hiệp định Genève ký kết năm nào?', 'Năm 1954, tạm chia đất nước tại vĩ tuyến 17.', 4),
      makeCard(4, 'Đại thắng mùa Xuân thống nhất đất nước năm nào?', 'Năm 1975, ngày 30 tháng 4.', 1),
    ],
  },
  {
    id: 'bio-cell', name: 'Sinh học tế bào', subject: 'Sinh học', icon: 'sparkles', accent: 'green',
    cards: [
      makeCard(1, 'Bào quan nào tạo năng lượng ATP?', 'Ti thể (mitochondria) — "nhà máy năng lượng" của tế bào.', 3),
      makeCard(2, 'Quá trình quang hợp diễn ra ở đâu?', 'Lục lạp (chloroplast) trong tế bào thực vật.', 0),
      makeCard(3, 'ADN được lưu trữ chủ yếu ở đâu?', 'Trong nhân tế bào (nucleus).', 1),
      makeCard(4, 'Ribosome có chức năng gì?', 'Tổng hợp protein từ chuỗi mARN.', 5),
    ],
  },
  {
    id: 'chem-table', name: 'Bảng tuần hoàn hoá học', subject: 'Hoá học', icon: 'gem', accent: 'magenta',
    cards: [
      makeCard(1, 'Ký hiệu hoá học của Natri?', 'Na (số hiệu nguyên tử 11).', 0),
      makeCard(2, 'Nguyên tố nhẹ nhất là gì?', 'Hydro (H) — số hiệu nguyên tử 1.', 2),
      makeCard(3, 'Khí trơ ở nhóm VIIIA gồm những nguyên tố nào?', 'He, Ne, Ar, Kr, Xe, Rn.', 0),
      makeCard(4, 'Ký hiệu hoá học của Vàng?', 'Au (từ tiếng Latin "aurum").', 3),
      makeCard(5, 'Số electron tối đa ở lớp ngoài cùng?', '8 electron (quy tắc bát tử).', 1),
    ],
  },
  {
    id: 'cs-algo', name: 'Cấu trúc dữ liệu & Giải thuật', subject: 'Tin học', icon: 'command', accent: 'cyan',
    cards: [
      makeCard(1, 'Độ phức tạp của tìm kiếm nhị phân?', 'O(log n) — chia đôi không gian tìm kiếm mỗi bước.', 8),
      makeCard(2, 'Stack hoạt động theo nguyên tắc nào?', 'LIFO — Last In, First Out.', 4),
      makeCard(3, 'Queue hoạt động theo nguyên tắc nào?', 'FIFO — First In, First Out.', 2),
      makeCard(4, 'Độ phức tạp trung bình của QuickSort?', 'O(n log n).', 0),
      makeCard(5, 'Cấu trúc dữ liệu nào dùng cho BFS?', 'Queue (hàng đợi).', 6),
    ],
  },
];

// ─── Computed deck stats (SM-2 based) ───────────────────────────────────────

function deckDueCount(deck: Deck): number {
  return deck.cards.filter(c => isDue(c.sm2)).length;
}

function deckMastery(deck: Deck): number {
  if (deck.cards.length === 0) return 0;
  const total = deck.cards.reduce((sum, c) => sum + Math.min(1, c.sm2.reps / 5), 0);
  return Math.round((total / deck.cards.length) * 100);
}

function deckTotalCards(deck: Deck): number {
  return deck.cards.length;
}

const allCards = computed(() => decks.flatMap(d => d.cards));
const totalDue = computed(() => allCards.value.filter(c => isDue(c.sm2)).length);
const totalMastered = computed(() => allCards.value.filter(c => c.sm2.reps >= 3).length);
const totalCards = computed(() => allCards.value.length);
const masteryPct = computed(() => Math.round((totalMastered.value / totalCards.value) * 100));

// ─── Review forecast (next 7 days) ──────────────────────────────────────────

const forecast = computed(() => {
  const days: { label: string; count: number; isToday: boolean }[] = [];
  const dayNames = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
  const today = new Date();
  for (let i = 0; i < 7; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    const startOfDay = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
    const endOfDay = startOfDay + DAY_MS;
    const count = allCards.value.filter(c => c.sm2.due >= startOfDay && c.sm2.due < endOfDay).length;
    days.push({ label: dayNames[date.getDay()], count, isToday: i === 0 });
  }
  return days;
});

const maxForecast = computed(() => Math.max(...forecast.value.map(d => d.count), 1));

// ─── Stats tiles ─────────────────────────────────────────────────────────────

const statTiles: StatTile[] = [
  { label: 'Tổng số thẻ', value: String(totalCards.value), icon: 'layers', accent: 'cyan', trend: 8, caption: `${decks.length} bộ thẻ`, sparkline: [320, 360, 380, 410, 440, 470, 486] },
  { label: 'Đến hạn hôm nay', value: String(totalDue.value), unit: 'thẻ', icon: 'clock', accent: 'amber', caption: 'Ưu tiên ôn trước 21:00', sparkline: [12, 18, 9, 24, 30, 22, 37] },
  { label: 'Đã thuộc', value: String(totalMastered.value), unit: 'thẻ', icon: 'check-circle', accent: 'green', trend: 5, caption: `${masteryPct.value}% tổng số`, sparkline: [180, 210, 240, 260, 280, 300, 312] },
  { label: 'Chuỗi ôn ngày', value: '12', unit: 'ngày', icon: 'flame', accent: 'magenta', caption: 'Kỷ lục: 28 ngày', sparkline: [6, 7, 8, 9, 10, 11, 12] },
];

// ─── Session state ──────────────────────────────────────────────────────────

const activeDeck = ref<Deck | null>(null);
const sessionCards = ref<Flashcard[]>([]);
const cardIndex = ref(0);
const flipped = ref(false);
const finished = ref(false);
const selectedRating = ref<Rating | null>(null);
const sessionStartTime = ref(0);
const sessionElapsed = ref(0);
let sessionTimer: number | null = null;

// Session settings
const cramMode = ref(false);
const reverseMode = ref(false);
const shuffleMode = ref(false);

const sessionStats = reactive({
  hard: 0,
  medium: 0,
  easy: 0,
  totalTime: 0,
});

const ratingOptions: RatingOption[] = [
  { id: 'hard', label: 'Chưa thuộc', accent: 'magenta', icon: 'flame', hint: 'phím 1' },
  { id: 'medium', label: 'Phân vân', accent: 'amber', icon: 'help', hint: 'phím 2' },
  { id: 'easy', label: 'Đã thuộc', accent: 'green', icon: 'check-circle', hint: 'phím 3' },
];

function shuffleArray<T>(arr: T[]): T[] {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

const currentCard = computed<Flashcard | null>(() => sessionCards.value[cardIndex.value] ?? null);
const sessionTotal = computed(() => sessionCards.value.length);
const reviewProgress = computed(() => {
  if (sessionTotal.value === 0) return 0;
  return Math.round(((cardIndex.value + (finished.value ? 1 : 0)) / sessionTotal.value) * 100);
});
const accuracyPct = computed(() => {
  const rated = sessionStats.easy + sessionStats.medium + sessionStats.hard;
  if (rated === 0) return 0;
  return Math.round(((sessionStats.easy + sessionStats.medium) / rated) * 100);
});
const earnedXp = computed(() => sessionStats.easy * 12 + sessionStats.medium * 6);
const formattedTime = computed(() => {
  const m = Math.floor(sessionElapsed.value / 60);
  const s = sessionElapsed.value % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
});

// ─── Session actions ────────────────────────────────────────────────────────

function startDeck(deck: Deck): void {
  let cards = deck.cards.filter(c => cramMode.value || isDue(c.sm2));
  if (cards.length === 0) cards = [...deck.cards];
  if (shuffleMode.value) cards = shuffleArray(cards);
  if (reverseMode.value) cards = cards.map(c => ({ ...c, front: c.back, back: c.front }));

  activeDeck.value = deck;
  sessionCards.value = cards;
  cardIndex.value = 0;
  flipped.value = false;
  finished.value = false;
  selectedRating.value = null;
  sessionStats.hard = 0;
  sessionStats.medium = 0;
  sessionStats.easy = 0;
  sessionStartTime.value = Date.now();
  sessionElapsed.value = 0;
  startSessionTimer();
}

function startSessionTimer(): void {
  if (sessionTimer) window.clearInterval(sessionTimer);
  sessionTimer = window.setInterval(() => {
    sessionElapsed.value = Math.floor((Date.now() - sessionStartTime.value) / 1000);
  }, 1000);
}

function stopSessionTimer(): void {
  if (sessionTimer) {
    window.clearInterval(sessionTimer);
    sessionTimer = null;
  }
}

function exitReview(): void {
  activeDeck.value = null;
  finished.value = false;
  stopSessionTimer();
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
  if (cardIndex.value < sessionTotal.value - 1) {
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
  sessionStats[rating] += 1;

  if (activeDeck.value && currentCard.value) {
    const originalCard = activeDeck.value.cards.find(c => c.id === currentCard.value!.id);
    if (originalCard) {
      originalCard.sm2 = sm2Review(originalCard.sm2, qualityMap[rating]);
    }
  }

  window.setTimeout(() => goNext(), 320);
}

function finishSession(): void {
  finished.value = true;
  stopSessionTimer();
  sessionStats.totalTime = sessionElapsed.value;
  launchSparkles();
}

function restartDeck(): void {
  if (activeDeck.value) startDeck(activeDeck.value);
}

// ─── Per-card info for review ───────────────────────────────────────────────

const currentCardInterval = computed(() => {
  if (!currentCard.value) return 0;
  return currentCard.value.sm2.interval;
});

const currentCardEase = computed(() => {
  if (!currentCard.value) return 2.5;
  return currentCard.value.sm2.ease;
});

const currentCardReps = computed(() => {
  if (!currentCard.value) return 0;
  return currentCard.value.sm2.reps;
});

// ─── Sparkle finish effect ──────────────────────────────────────────────────

const sparkles = ref<Sparkle[]>([]);
let sparkleTimer: number | null = null;

function launchSparkles(): void {
  const items: Sparkle[] = [];
  for (let i = 0; i < 28; i += 1) {
    items.push({ id: i, left: Math.random() * 100, top: Math.random() * 100, delay: Math.random() * 0.6, size: 4 + Math.random() * 8 });
  }
  sparkles.value = items;
  sparkleTimer = window.setTimeout(() => { sparkles.value = []; }, 2600);
}

function stopSparkles(): void {
  sparkles.value = [];
  if (sparkleTimer !== null) { window.clearTimeout(sparkleTimer); sparkleTimer = null; }
}

// ─── Keyboard controls ──────────────────────────────────────────────────────

function onKeydown(e: KeyboardEvent): void {
  if (!activeDeck.value || finished.value) return;
  if (e.code === 'Space') { e.preventDefault(); flipCard(); }
  else if (e.code === 'ArrowLeft') goPrev();
  else if (e.code === 'ArrowRight') goNext();
  else if (flipped.value) {
    if (e.key === '1') rateCard('hard');
    else if (e.key === '2') rateCard('medium');
    else if (e.key === '3') rateCard('easy');
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown));
onUnmounted(() => { window.removeEventListener('keydown', onKeydown); stopSparkles(); stopSessionTimer(); });

// ─── Accent helpers ─────────────────────────────────────────────────────────

const deckIconClasses: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300', magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300', violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

const ratingActiveClasses: Record<Accent, string> = {
  cyan: 'border-cyan-400/60 bg-cyan-500/20 text-cyan-200', magenta: 'border-fuchsia-400/60 bg-fuchsia-500/20 text-fuchsia-200',
  green: 'border-emerald-400/60 bg-emerald-500/20 text-emerald-200', violet: 'border-violet-400/60 bg-violet-500/20 text-violet-200',
  amber: 'border-amber-400/60 bg-amber-500/20 text-amber-200',
};

const ratingIdleClasses: Record<Accent, string> = {
  cyan: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-200',
  magenta: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-fuchsia-400/40 hover:text-fuchsia-200',
  green: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-emerald-400/40 hover:text-emerald-200',
  violet: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-violet-400/40 hover:text-violet-200',
  amber: 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-amber-400/40 hover:text-amber-200',
};

function formatDue(sm2: SM2State): string {
  const days = daysUntilDue(sm2);
  if (days < 0) return `${Math.abs(days)} ngày quá hạn`;
  if (days === 0) return 'Đến hạn hôm nay';
  if (days === 1) return 'Đến hạn mai';
  return `Đến hạn sau ${days} ngày`;
}
</script>

<template>
  <div class="space-y-6">
    <!-- Page header -->
    <GlassPanel glow="violet" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Ôn tập ngắt quãng (SM-2)</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">Thẻ ghi nhớ</h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Thuật toán SM-2 lên lịch ôn lại đúng lúc bạn sắp quên. Mỗi lần đánh giá thay đổi khoảng cách ôn tiếp theo.
          </p>
        </div>
        <div v-if="!activeDeck" class="flex shrink-0 flex-wrap items-center gap-3">
          <div class="flex items-center gap-2 rounded-xl border border-amber-400/20 bg-amber-500/10 px-4 py-2.5">
            <AppIcon name="clock" class="h-5 w-5 text-amber-300" />
            <div><p class="text-sm font-bold leading-none text-amber-200">{{ totalDue }} thẻ</p><p class="mt-0.5 text-[0.65rem] text-muted-foreground">Đến hạn hôm nay</p></div>
          </div>
          <div class="flex items-center gap-2 rounded-xl border border-emerald-400/20 bg-emerald-500/10 px-4 py-2.5">
            <AppIcon name="check-circle" class="h-5 w-5 text-emerald-300" />
            <div><p class="text-sm font-bold leading-none text-emerald-200">{{ masteryPct }}%</p><p class="mt-0.5 text-[0.65rem] text-muted-foreground">Đã thuộc</p></div>
          </div>
        </div>
        <button v-else type="button" class="inline-flex shrink-0 items-center gap-2 self-start rounded-xl border border-white/10 bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40" @click="exitReview">
          <AppIcon name="chevron-left" class="h-4 w-4" /> Thư viện bộ thẻ
        </button>
      </div>
    </GlassPanel>

    <!-- ───────────────── LIBRARY VIEW ───────────────── -->
    <template v-if="!activeDeck">
      <section class="grid grid-cols-2 gap-4 md:grid-cols-4">
        <StatPanel v-for="stat in statTiles" :key="stat.label" :label="stat.label" :value="stat.value" :unit="stat.unit" :icon="stat.icon" :accent="stat.accent" :trend="stat.trend" :caption="stat.caption" :sparkline="stat.sparkline" />
      </section>

      <!-- Review forecast -->
      <SectionPanel title="Dự báo ôn tập 7 ngày tới" subtitle="Số thẻ đến hạn mỗi ngày theo lịch SM-2" icon="calendar" accent="cyan">
        <div class="flex items-end justify-between gap-2 sm:gap-4" style="height: 120px;">
          <div v-for="(day, i) in forecast" :key="i" class="flex flex-1 flex-col items-center gap-2">
            <span class="text-xs font-semibold tabular-nums" :class="day.count > 0 ? 'text-foreground' : 'text-muted-foreground/50'">{{ day.count }}</span>
            <div class="w-full rounded-t-md transition-all" :style="{ height: `${(day.count / maxForecast) * 80 + 4}px`, background: day.isToday ? 'linear-gradient(180deg, #22D3EE, #7C3AED)' : 'rgba(255,255,255,0.08)' }" />
            <span class="text-[0.7rem]" :class="day.isToday ? 'font-bold text-cyan-300' : 'text-muted-foreground'">{{ day.label }}</span>
          </div>
        </div>
      </SectionPanel>

      <!-- Session settings -->
      <SectionPanel title="Tuỳ chọn phiên ôn" icon="settings" accent="amber">
        <div class="flex flex-wrap gap-3">
          <button type="button" class="setting-toggle" :class="{ active: cramMode }" @click="cramMode = !cramMode">
            <AppIcon name="zap" class="h-4 w-4" /> Cram mode (ôn tất cả, bỏ qua lịch)
          </button>
          <button type="button" class="setting-toggle" :class="{ active: shuffleMode }" @click="shuffleMode = !shuffleMode">
            <AppIcon name="refresh" class="h-4 w-4" /> Xáo trộn thẻ
          </button>
          <button type="button" class="setting-toggle" :class="{ active: reverseMode }" @click="reverseMode = !reverseMode">
            <AppIcon name="swap" class="h-4 w-4" /> Đảo ngược (đáp án → câu hỏi)
          </button>
        </div>
      </SectionPanel>

      <!-- Deck library -->
      <SectionPanel title="Thư viện bộ thẻ" subtitle="Chọn một bộ để bắt đầu phiên ôn" icon="layers" accent="violet">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <button v-for="deck in decks" :key="deck.id" type="button" class="deck-card group flex flex-col gap-4 rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4 text-left transition-all hover:-translate-y-1 hover:border-white/[0.14]" @click="startDeck(deck)">
            <div class="flex items-start justify-between gap-3">
              <span class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl transition-transform group-hover:scale-110" :class="deckIconClasses[deck.accent]">
                <AppIcon :name="deck.icon" class="h-6 w-6" />
              </span>
              <RingProgress :value="deckMastery(deck)" :size="52" :stroke="6" :accent="deck.accent">
                <span class="text-[0.7rem] font-bold tabular-nums text-foreground">{{ deckMastery(deck) }}%</span>
              </RingProgress>
            </div>

            <div class="min-w-0">
              <p class="truncate font-semibold text-foreground">{{ deck.name }}</p>
              <p class="mt-0.5 text-xs text-muted-foreground">{{ deck.subject }} · {{ deckTotalCards(deck) }} thẻ</p>
            </div>

            <div>
              <div class="mb-1.5 flex items-center justify-between text-xs">
                <span class="text-muted-foreground">Độ thành thạo</span>
                <span class="tabular-nums text-muted-foreground">{{ deckMastery(deck) }}%</span>
              </div>
              <ProgressBar :value="deckMastery(deck)" :accent="deck.accent" />
            </div>

            <div class="flex items-center justify-between">
              <span class="inline-flex items-center gap-1 rounded-full border border-amber-400/20 bg-amber-500/10 px-2.5 py-1 text-xs font-semibold text-amber-300">
                <AppIcon name="clock" class="h-3.5 w-3.5" /> {{ deckDueCount(deck) }} đến hạn
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
          <div class="pointer-events-none absolute inset-0">
            <span v-for="sp in sparkles" :key="sp.id" class="sparkle absolute rounded-full" :style="{ left: sp.left + '%', top: sp.top + '%', width: sp.size + 'px', height: sp.size + 'px', animationDelay: sp.delay + 's' }" />
          </div>

          <div class="relative flex flex-col items-center gap-5 text-center">
            <span class="cosmic-badge">Hoàn thành phiên ôn</span>
            <RingProgress :value="accuracyPct" :size="148" :stroke="12" accent="green">
              <span class="text-3xl font-bold tabular-nums text-emerald-50">{{ accuracyPct }}%</span>
              <span class="text-xs text-muted-foreground">chính xác</span>
            </RingProgress>
            <h2 class="text-xl font-bold text-foreground">Hoàn thành {{ activeDeck.name }}</h2>

            <div class="grid w-full max-w-lg grid-cols-2 gap-3 sm:grid-cols-4">
              <div class="summary-tile emerald">
                <p class="text-lg font-bold tabular-nums">{{ sessionStats.easy }}</p><p class="label">Đã thuộc</p>
              </div>
              <div class="summary-tile amber">
                <p class="text-lg font-bold tabular-nums">{{ sessionStats.medium }}</p><p class="label">Phân vân</p>
              </div>
              <div class="summary-tile magenta">
                <p class="text-lg font-bold tabular-nums">{{ sessionStats.hard }}</p><p class="label">Chưa thuộc</p>
              </div>
              <div class="summary-tile cyan">
                <p class="text-lg font-bold tabular-nums">{{ formattedTime }}</p><p class="label">Thời gian</p>
              </div>
            </div>

            <div class="flex items-center gap-4 rounded-xl border border-violet-400/20 bg-violet-500/[0.07] px-5 py-3">
              <AppIcon name="zap" class="h-5 w-5 text-violet-300" />
              <span class="text-sm text-muted-foreground">Nhận <span class="font-bold text-violet-200">+{{ earnedXp }} XP</span> — lịch ôn đã được cập nhật theo SM-2</span>
            </div>

            <div class="flex flex-wrap items-center justify-center gap-3">
              <button type="button" class="cosmic-btn rounded-xl px-4 py-2 text-sm" @click="restartDeck">
                <AppIcon name="refresh" class="mr-1.5 inline h-4 w-4" />Ôn lại
              </button>
              <button type="button" class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40" @click="exitReview">
                <AppIcon name="grid" class="h-4 w-4" />Chọn bộ khác
              </button>
            </div>
          </div>
        </GlassPanel>
      </template>

      <!-- Active card -->
      <template v-else>
        <GlassPanel :glow="activeDeck.accent" class="p-5 sm:p-6">
          <!-- Session header -->
          <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex items-center gap-3">
              <span class="flex h-10 w-10 items-center justify-center rounded-xl" :class="deckIconClasses[activeDeck.accent]">
                <AppIcon :name="activeDeck.icon" class="h-5 w-5" />
              </span>
              <div>
                <p class="font-semibold text-foreground">{{ activeDeck.name }}</p>
                <p class="text-xs text-muted-foreground">{{ activeDeck.subject }}</p>
              </div>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <span class="inline-flex items-center gap-1 rounded-full bg-white/[0.06] px-2.5 py-1 text-xs font-semibold text-muted-foreground">
                <AppIcon name="clock" class="h-3 w-3" /> {{ formattedTime }}
              </span>
              <span class="rounded-full bg-emerald-500/15 px-2.5 py-1 text-xs font-semibold text-emerald-300">
                ✓ {{ sessionStats.easy }}
              </span>
              <span class="rounded-full bg-amber-500/15 px-2.5 py-1 text-xs font-semibold text-amber-300">
                ? {{ sessionStats.medium }}
              </span>
              <span class="rounded-full bg-fuchsia-500/15 px-2.5 py-1 text-xs font-semibold text-fuchsia-300">
                ✕ {{ sessionStats.hard }}
              </span>
              <span class="rounded-full bg-white/[0.06] px-2.5 py-1 text-xs font-semibold text-muted-foreground">
                {{ cardIndex + 1 }}/{{ sessionTotal }}
              </span>
            </div>
          </div>

          <ProgressBar :value="reviewProgress" :accent="activeDeck.accent" :height="8" />

          <!-- 3D flip card -->
          <div class="mt-6 flex justify-center">
            <button type="button" class="flip-scene w-full max-w-2xl" :aria-label="flipped ? 'Lật về câu hỏi' : 'Lật xem đáp án'" @click="flipCard">
              <div class="flip-card" :class="{ 'is-flipped': flipped }">
                <div class="flip-face flip-front">
                  <span class="cosmic-badge">Câu hỏi</span>
                  <p class="mt-4 text-xl font-semibold leading-relaxed text-foreground sm:text-2xl">{{ currentCard?.front }}</p>
                  <p class="mt-6 inline-flex items-center gap-1.5 text-xs text-muted-foreground">
                    <AppIcon name="refresh" class="h-3.5 w-3.5" /> Nhấn vào thẻ hoặc Space để lật
                  </p>
                </div>
                <div class="flip-face flip-back">
                  <span class="cosmic-badge">Đáp án</span>
                  <p class="mt-4 text-lg leading-relaxed text-foreground sm:text-xl">{{ currentCard?.back }}</p>
                  <p class="mt-6 inline-flex items-center gap-1.5 text-xs text-emerald-300">
                    <AppIcon name="check-circle" class="h-3.5 w-3.5" /> Tự đánh giá mức độ ghi nhớ bên dưới
                  </p>
                </div>
              </div>
            </button>
          </div>

          <!-- Card metadata (SM-2 info) -->
          <div class="mt-4 flex flex-wrap items-center justify-center gap-3 text-[0.7rem] text-muted-foreground">
            <span class="inline-flex items-center gap-1"><AppIcon name="repeat" class="h-3 w-3" /> Lần ôn: {{ currentCardReps }}</span>
            <span class="inline-flex items-center gap-1"><AppIcon name="clock" class="h-3 w-3" /> Khoảng cách: {{ currentCardInterval }} ngày</span>
            <span class="inline-flex items-center gap-1"><AppIcon name="trending-up" class="h-3 w-3" /> Ease: {{ currentCardEase.toFixed(2) }}</span>
            <span v-if="currentCard" class="inline-flex items-center gap-1"><AppIcon name="calendar" class="h-3 w-3" /> {{ formatDue(currentCard.sm2) }}</span>
          </div>

          <!-- Rating buttons -->
          <transition name="fade-up">
            <div v-if="flipped" class="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
              <button v-for="opt in ratingOptions" :key="opt.id" type="button" class="flex items-center justify-center gap-2 rounded-xl border px-4 py-3 text-sm font-semibold transition-all" :class="selectedRating === opt.id ? ratingActiveClasses[opt.accent] : ratingIdleClasses[opt.accent]" @click="rateCard(opt.id)">
                <AppIcon :name="opt.icon" class="h-4 w-4" /> {{ opt.label }} <span class="text-[0.6rem] font-normal opacity-60">({{ opt.hint }})</span>
              </button>
            </div>
          </transition>

          <!-- Navigation -->
          <div class="mt-5 flex items-center justify-between">
            <button type="button" class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-3.5 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40 disabled:cursor-not-allowed disabled:opacity-40" :disabled="cardIndex === 0" @click="goPrev">
              <AppIcon name="chevron-left" class="h-4 w-4" />Trước
            </button>
            <span class="text-xs text-muted-foreground">
              <span class="font-mono text-foreground">Space</span> lật ·
              <span class="font-mono text-foreground">← →</span> điều hướng ·
              <span class="font-mono text-foreground">1 2 3</span> đánh giá
            </span>
            <button type="button" class="inline-flex items-center gap-1.5 rounded-xl border border-white/10 bg-white/[0.03] px-3.5 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40" @click="goNext">
              {{ cardIndex === sessionTotal - 1 ? 'Kết thúc' : 'Sau' }} <AppIcon name="chevron-right" class="h-4 w-4" />
            </button>
          </div>
        </GlassPanel>
      </template>
    </template>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.</p>
  </div>
</template>

<style scoped>
.flip-scene { perspective: 1400px; }
.flip-card { position: relative; height: 280px; width: 100%; transform-style: preserve-3d; transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1); }
.flip-card.is-flipped { transform: rotateY(180deg); }
.flip-face { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 1.75rem; text-align: center; border-radius: 1rem; backface-visibility: hidden; -webkit-backface-visibility: hidden; border: 1px solid rgb(255 255 255 / 0.1); background: linear-gradient(160deg, rgb(255 255 255 / 0.05), rgb(255 255 255 / 0.01)); }
.flip-front { box-shadow: inset 0 0 40px oklch(0.78 0.18 195 / 8%); }
.flip-back { transform: rotateY(180deg); box-shadow: inset 0 0 40px oklch(0.75 0.18 145 / 10%); }
.sparkle { background: radial-gradient(circle, oklch(0.85 0.18 145) 0%, transparent 70%); animation: sparkle-pop 1.6s ease-out forwards; }
@keyframes sparkle-pop { 0% { transform: scale(0) translateY(0); opacity: 0; } 30% { transform: scale(1.2) translateY(-6px); opacity: 1; } 100% { transform: scale(0.4) translateY(-40px); opacity: 0; } }
.fade-up-enter-active { transition: all 0.3s ease; }
.fade-up-enter-from { opacity: 0; transform: translateY(8px); }
.setting-toggle { display: inline-flex; align-items: center; gap: 0.5rem; border-radius: 0.625rem; border: 1px solid rgba(255,255,255,0.08); background: rgba(255,255,255,0.02); padding: 0.625rem 1rem; font-size: 0.85rem; font-weight: 500; color: #8B8B9F; cursor: pointer; transition: all 0.2s ease; }
.setting-toggle:hover { border-color: rgba(255,255,255,0.15); color: #C0C0D4; }
.setting-toggle.active { border-color: rgba(34,211,238,0.4); background: rgba(34,211,238,0.08); color: #22D3EE; }
.summary-tile { border-radius: 0.75rem; padding: 0.75rem; text-align: center; }
.summary-tile .label { font-size: 0.65rem; color: #5A5A6E; margin-top: 0.15rem; }
.summary-tile.emerald { border: 1px solid rgba(16,185,129,0.2); background: rgba(16,185,129,0.07); }
.summary-tile.emerald p { color: #6EE7B7; }
.summary-tile.amber { border: 1px solid rgba(245,158,11,0.2); background: rgba(245,158,11,0.07); }
.summary-tile.amber p { color: #FCD34D; }
.summary-tile.magenta { border: 1px solid rgba(236,72,153,0.2); background: rgba(236,72,153,0.07); }
.summary-tile.magenta p { color: #F9A8D4; }
.summary-tile.cyan { border: 1px solid rgba(34,211,238,0.2); background: rgba(34,211,238,0.07); }
.summary-tile.cyan p { color: #67E8F9; }
@media (prefers-reduced-motion: reduce) { .flip-card, .sparkle, .fade-up-enter-active { transition: none; animation: none; } }
</style>
