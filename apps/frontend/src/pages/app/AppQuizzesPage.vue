<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import RingProgress from '@/components/app/RingProgress.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Difficulty = 'easy' | 'medium' | 'hard';

interface Question {
  id: number;
  text: string;
  options: string[];
  correct: number;
  explanation: string;
}

interface Quiz {
  id: string;
  title: string;
  subject: string;
  icon: string;
  accent: Accent;
  difficulty: Difficulty;
  estMinutes: number;
  coinReward: number;
  bestScore: number;
  perQuestionSeconds: number;
  questions: Question[];
}

interface Confetti {
  id: number;
  left: number;
  delay: number;
  duration: number;
  color: string;
  rotate: number;
}

interface ReviewRow {
  index: number;
  text: string;
  picked: number | null;
  correct: number;
  options: string[];
  isCorrect: boolean;
}

// ─── Quiz catalog ─────────────────────────────────────────────────────────────

const quizzes: Quiz[] = [
  {
    id: 'q-math',
    title: 'Đại số tuyến tính cơ bản',
    subject: 'Toán học',
    icon: 'chart',
    accent: 'violet',
    difficulty: 'medium',
    estMinutes: 6,
    coinReward: 80,
    bestScore: 75,
    perQuestionSeconds: 25,
    questions: [
      {
        id: 1,
        text: 'Định thức của ma trận đơn vị cấp n bằng bao nhiêu?',
        options: ['0', '1', 'n', 'n²'],
        correct: 1,
        explanation: 'Ma trận đơn vị có đường chéo chính toàn số 1, nên định thức luôn bằng 1.',
      },
      {
        id: 2,
        text: 'Hai vector vuông góc khi tích vô hướng của chúng bằng?',
        options: ['1', '-1', '0', 'Độ dài tích'],
        correct: 2,
        explanation: 'Tích vô hướng bằng 0 nghĩa là góc giữa hai vector là 90°.',
      },
      {
        id: 3,
        text: 'Hạng (rank) của ma trận là gì?',
        options: ['Số hàng', 'Số cột', 'Số dòng độc lập tuyến tính', 'Tổng các phần tử'],
        correct: 2,
        explanation: 'Hạng là số lượng hàng (hoặc cột) độc lập tuyến tính tối đa.',
      },
      {
        id: 4,
        text: 'Ma trận khả nghịch khi định thức của nó?',
        options: ['Bằng 0', 'Khác 0', 'Luôn dương', 'Là số nguyên'],
        correct: 1,
        explanation: 'Ma trận có nghịch đảo khi và chỉ khi định thức khác 0.',
      },
    ],
  },
  {
    id: 'q-eng',
    title: 'Ngữ pháp tiếng Anh trung cấp',
    subject: 'Tiếng Anh',
    icon: 'book-open',
    accent: 'cyan',
    difficulty: 'easy',
    estMinutes: 4,
    coinReward: 50,
    bestScore: 90,
    perQuestionSeconds: 20,
    questions: [
      {
        id: 1,
        text: 'Chọn câu đúng: "She ___ to school every day."',
        options: ['go', 'goes', 'going', 'gone'],
        correct: 1,
        explanation: 'Chủ ngữ ngôi thứ 3 số ít ở thì hiện tại đơn cần thêm "-es" → goes.',
      },
      {
        id: 2,
        text: 'Thì nào dùng cho hành động đang xảy ra ngay lúc nói?',
        options: ['Quá khứ đơn', 'Hiện tại tiếp diễn', 'Tương lai đơn', 'Hiện tại hoàn thành'],
        correct: 1,
        explanation: 'Hiện tại tiếp diễn (am/is/are + V-ing) diễn tả hành động đang diễn ra.',
      },
      {
        id: 3,
        text: 'Dạng so sánh hơn của "good" là?',
        options: ['gooder', 'more good', 'better', 'best'],
        correct: 2,
        explanation: '"Good" là tính từ bất quy tắc: good → better → best.',
      },
      {
        id: 4,
        text: 'Giới từ phù hợp: "I am interested ___ music."',
        options: ['on', 'at', 'in', 'for'],
        correct: 2,
        explanation: 'Cụm cố định "interested in" diễn tả sự quan tâm tới điều gì đó.',
      },
    ],
  },
  {
    id: 'q-cs',
    title: 'Thuật toán & độ phức tạp',
    subject: 'Tin học',
    icon: 'command',
    accent: 'green',
    difficulty: 'hard',
    estMinutes: 8,
    coinReward: 120,
    bestScore: 60,
    perQuestionSeconds: 30,
    questions: [
      {
        id: 1,
        text: 'Độ phức tạp thời gian xấu nhất của QuickSort là?',
        options: ['O(n)', 'O(n log n)', 'O(n²)', 'O(log n)'],
        correct: 2,
        explanation: 'Khi pivot luôn là phần tử nhỏ/lớn nhất, QuickSort suy biến thành O(n²).',
      },
      {
        id: 2,
        text: 'Cấu trúc dữ liệu nào phù hợp cho duyệt BFS?',
        options: ['Stack', 'Queue', 'Heap', 'Cây nhị phân'],
        correct: 1,
        explanation: 'BFS dùng hàng đợi (queue) để xử lý các đỉnh theo thứ tự FIFO.',
      },
      {
        id: 3,
        text: 'Bảng băm (hash table) có độ phức tạp tra cứu trung bình là?',
        options: ['O(1)', 'O(log n)', 'O(n)', 'O(n²)'],
        correct: 0,
        explanation: 'Với hàm băm tốt, tra cứu trung bình đạt O(1).',
      },
      {
        id: 4,
        text: 'Thuật toán Dijkstra giải bài toán nào?',
        options: ['Sắp xếp', 'Đường đi ngắn nhất', 'Tìm kiếm chuỗi', 'Nén dữ liệu'],
        correct: 1,
        explanation: 'Dijkstra tìm đường đi ngắn nhất từ một đỉnh tới các đỉnh khác (trọng số không âm).',
      },
      {
        id: 5,
        text: 'Quy hoạch động (DP) tối ưu nhờ?',
        options: ['Đệ quy thuần', 'Lưu kết quả con (memoization)', 'Vét cạn', 'Tham lam'],
        correct: 1,
        explanation: 'DP lưu lại kết quả bài toán con đã giải để tránh tính lặp.',
      },
    ],
  },
  {
    id: 'q-history',
    title: 'Lịch sử thế giới hiện đại',
    subject: 'Lịch sử',
    icon: 'flag',
    accent: 'amber',
    difficulty: 'medium',
    estMinutes: 5,
    coinReward: 70,
    bestScore: 0,
    perQuestionSeconds: 25,
    questions: [
      {
        id: 1,
        text: 'Chiến tranh thế giới thứ hai kết thúc vào năm nào?',
        options: ['1918', '1939', '1945', '1950'],
        correct: 2,
        explanation: 'WWII kết thúc năm 1945 với sự đầu hàng của phe Trục.',
      },
      {
        id: 2,
        text: 'Bức tường Berlin sụp đổ vào năm nào?',
        options: ['1985', '1989', '1991', '1995'],
        correct: 1,
        explanation: 'Bức tường Berlin sụp đổ ngày 9/11/1989, mở đầu thống nhất nước Đức.',
      },
      {
        id: 3,
        text: 'Tổ chức Liên Hợp Quốc được thành lập năm nào?',
        options: ['1945', '1946', '1950', '1955'],
        correct: 0,
        explanation: 'Liên Hợp Quốc chính thức thành lập ngày 24/10/1945.',
      },
      {
        id: 4,
        text: 'Cuộc Cách mạng Công nghiệp khởi đầu tại quốc gia nào?',
        options: ['Pháp', 'Đức', 'Anh', 'Mỹ'],
        correct: 2,
        explanation: 'Cách mạng Công nghiệp bắt đầu ở Anh vào nửa cuối thế kỷ 18.',
      },
    ],
  },
  {
    id: 'q-bio',
    title: 'Sinh học cơ thể người',
    subject: 'Sinh học',
    icon: 'heart',
    accent: 'magenta',
    difficulty: 'easy',
    estMinutes: 4,
    coinReward: 55,
    bestScore: 80,
    perQuestionSeconds: 20,
    questions: [
      {
        id: 1,
        text: 'Cơ quan nào bơm máu đi khắp cơ thể?',
        options: ['Phổi', 'Gan', 'Tim', 'Thận'],
        correct: 2,
        explanation: 'Tim là cơ quan bơm máu tuần hoàn nuôi toàn bộ cơ thể.',
      },
      {
        id: 2,
        text: 'Đơn vị chức năng của thận là?',
        options: ['Nơ-ron', 'Nephron', 'Phế nang', 'Tiểu cầu'],
        correct: 1,
        explanation: 'Nephron là đơn vị lọc máu cơ bản của thận.',
      },
      {
        id: 3,
        text: 'Hồng cầu mang chất gì để vận chuyển oxy?',
        options: ['Insulin', 'Hemoglobin', 'Collagen', 'Keratin'],
        correct: 1,
        explanation: 'Hemoglobin trong hồng cầu liên kết và vận chuyển oxy.',
      },
      {
        id: 4,
        text: 'Bộ phận nào điều khiển hoạt động có ý thức?',
        options: ['Tiểu não', 'Đại não', 'Tuỷ sống', 'Hành não'],
        correct: 1,
        explanation: 'Đại não điều khiển tư duy, vận động có ý thức và cảm giác.',
      },
    ],
  },
  {
    id: 'q-chem',
    title: 'Phản ứng hoá học cơ bản',
    subject: 'Hoá học',
    icon: 'gem',
    accent: 'cyan',
    difficulty: 'medium',
    estMinutes: 6,
    coinReward: 75,
    bestScore: 65,
    perQuestionSeconds: 25,
    questions: [
      {
        id: 1,
        text: 'Công thức hoá học của nước là?',
        options: ['CO₂', 'H₂O', 'O₂', 'NaCl'],
        correct: 1,
        explanation: 'Nước gồm 2 nguyên tử hydro và 1 nguyên tử oxy → H₂O.',
      },
      {
        id: 2,
        text: 'pH = 7 tương ứng với dung dịch?',
        options: ['Axit', 'Bazơ', 'Trung tính', 'Muối'],
        correct: 2,
        explanation: 'pH = 7 là trung tính, ví dụ nước tinh khiết.',
      },
      {
        id: 3,
        text: 'Phản ứng toả nhiệt là phản ứng?',
        options: ['Thu nhiệt', 'Giải phóng nhiệt', 'Không đổi nhiệt', 'Hấp thụ ánh sáng'],
        correct: 1,
        explanation: 'Phản ứng toả nhiệt giải phóng năng lượng ra môi trường.',
      },
      {
        id: 4,
        text: 'Khí nào chiếm tỉ lệ lớn nhất trong không khí?',
        options: ['Oxy', 'CO₂', 'Nitơ', 'Hydro'],
        correct: 2,
        explanation: 'Nitơ (N₂) chiếm khoảng 78% thể tích không khí.',
      },
    ],
  },
];

const difficultyMeta: Record<Difficulty, { label: string; classes: string }> = {
  easy: { label: 'Dễ', classes: 'border-emerald-400/30 bg-emerald-500/10 text-emerald-300' },
  medium: { label: 'Trung bình', classes: 'border-amber-400/30 bg-amber-500/10 text-amber-300' },
  hard: { label: 'Khó', classes: 'border-fuchsia-400/30 bg-fuchsia-500/10 text-fuchsia-300' },
};

// ─── Runner state ───────────────────────────────────────────────────────────

type View = 'catalog' | 'runner' | 'results';

const view = ref<View>('catalog');
const activeQuiz = ref<Quiz | null>(null);
const qIndex = ref<number>(0);
const picked = ref<number | null>(null);
const checked = ref<boolean>(false);
const streak = ref<number>(0);
const bestStreak = ref<number>(0);
const answers = ref<Array<number | null>>([]);
const timeLeft = ref<number>(0);
const displayScore = ref<number>(0);

let timerId: number | null = null;
let countUpId: number | null = null;

const currentQuestion = computed<Question | null>(() => {
  if (!activeQuiz.value) return null;
  return activeQuiz.value.questions[qIndex.value] ?? null;
});

const totalQuestions = computed<number>(() => activeQuiz.value?.questions.length ?? 0);

const runnerProgress = computed<number>(() => {
  if (totalQuestions.value === 0) return 0;
  return Math.round((qIndex.value / totalQuestions.value) * 100);
});

const timerPct = computed<number>(() => {
  if (!activeQuiz.value || activeQuiz.value.perQuestionSeconds === 0) return 0;
  return Math.round((timeLeft.value / activeQuiz.value.perQuestionSeconds) * 100);
});

const timerAccent = computed<Accent>(() => {
  if (timerPct.value > 50) return 'green';
  if (timerPct.value > 25) return 'amber';
  return 'magenta';
});

const correctCount = computed<number>(() => {
  if (!activeQuiz.value) return 0;
  return activeQuiz.value.questions.reduce(
    (acc, q, i) => acc + (answers.value[i] === q.correct ? 1 : 0),
    0,
  );
});

const wrongCount = computed<number>(() => totalQuestions.value - correctCount.value);
const scorePct = computed<number>(() =>
  totalQuestions.value === 0 ? 0 : Math.round((correctCount.value / totalQuestions.value) * 100),
);
const earnedCoin = computed<number>(() =>
  activeQuiz.value ? Math.round((scorePct.value / 100) * activeQuiz.value.coinReward) : 0,
);
const earnedXp = computed<number>(() => correctCount.value * 15 + bestStreak.value * 5);

const reviewRows = computed<ReviewRow[]>(() => {
  if (!activeQuiz.value) return [];
  return activeQuiz.value.questions.map((q, i) => ({
    index: i,
    text: q.text,
    picked: answers.value[i] ?? null,
    correct: q.correct,
    options: q.options,
    isCorrect: answers.value[i] === q.correct,
  }));
});

// ─── Timer ──────────────────────────────────────────────────────────────────

function clearTimer(): void {
  if (timerId !== null) {
    window.clearInterval(timerId);
    timerId = null;
  }
}

function startTimer(): void {
  clearTimer();
  if (!activeQuiz.value) return;
  timeLeft.value = activeQuiz.value.perQuestionSeconds;
  timerId = window.setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value -= 1;
    } else {
      // ran out of time → reveal then auto-advance
      if (!checked.value) checkAnswer();
      window.setTimeout(() => nextQuestion(), 700);
    }
  }, 1000);
}

// ─── Runner actions ───────────────────────────────────────────────────────────

function startQuiz(quiz: Quiz): void {
  activeQuiz.value = quiz;
  qIndex.value = 0;
  picked.value = null;
  checked.value = false;
  streak.value = 0;
  bestStreak.value = 0;
  answers.value = new Array(quiz.questions.length).fill(null);
  view.value = 'runner';
  startTimer();
}

function selectOption(i: number): void {
  if (checked.value) return;
  picked.value = i;
}

function checkAnswer(): void {
  if (checked.value || !currentQuestion.value) return;
  checked.value = true;
  clearTimer();
  answers.value[qIndex.value] = picked.value;
  if (picked.value === currentQuestion.value.correct) {
    streak.value += 1;
    if (streak.value > bestStreak.value) bestStreak.value = streak.value;
  } else {
    streak.value = 0;
  }
}

function nextQuestion(): void {
  if (!activeQuiz.value) return;
  if (qIndex.value < totalQuestions.value - 1) {
    qIndex.value += 1;
    picked.value = null;
    checked.value = false;
    startTimer();
  } else {
    finishQuiz();
  }
}

function finishQuiz(): void {
  clearTimer();
  view.value = 'results';
  launchConfetti();
  // count-up score animation
  displayScore.value = 0;
  if (countUpId !== null) window.clearInterval(countUpId);
  const target = scorePct.value;
  countUpId = window.setInterval(() => {
    if (displayScore.value < target) {
      displayScore.value = Math.min(target, displayScore.value + 2);
    } else if (countUpId !== null) {
      window.clearInterval(countUpId);
      countUpId = null;
    }
  }, 24);
}

function retryQuiz(): void {
  if (activeQuiz.value) startQuiz(activeQuiz.value);
}

function backToCatalog(): void {
  clearTimer();
  if (countUpId !== null) {
    window.clearInterval(countUpId);
    countUpId = null;
  }
  confetti.value = [];
  activeQuiz.value = null;
  view.value = 'catalog';
}

// ─── Confetti ─────────────────────────────────────────────────────────────────

const confetti = ref<Confetti[]>([]);
let confettiTimer: number | null = null;
const confettiColors = [
  'oklch(0.78 0.18 195)',
  'oklch(0.68 0.22 350)',
  'oklch(0.75 0.18 145)',
  'oklch(0.8 0.16 85)',
  'oklch(0.62 0.2 290)',
];

function launchConfetti(): void {
  const items: Confetti[] = [];
  for (let i = 0; i < 48; i += 1) {
    items.push({
      id: i,
      left: Math.random() * 100,
      delay: Math.random() * 0.5,
      duration: 1.6 + Math.random() * 1.4,
      color: confettiColors[i % confettiColors.length],
      rotate: Math.random() * 360,
    });
  }
  confetti.value = items;
  confettiTimer = window.setTimeout(() => {
    confetti.value = [];
  }, 3200);
}

onUnmounted(() => {
  clearTimer();
  if (countUpId !== null) window.clearInterval(countUpId);
  if (confettiTimer !== null) window.clearTimeout(confettiTimer);
});

// ─── Accent helpers ────────────────────────────────────────────────────────────

const quizIconClasses: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300',
};

function optionClasses(i: number): string {
  if (!checked.value) {
    return picked.value === i
      ? 'border-cyan-400/60 bg-cyan-500/15 text-cyan-100 option-pulse'
      : 'border-white/[0.08] bg-white/[0.02] text-foreground hover:border-cyan-400/30 hover:bg-white/[0.04]';
  }
  if (!currentQuestion.value) return 'border-white/[0.08] bg-white/[0.02]';
  if (i === currentQuestion.value.correct) {
    return 'border-emerald-400/60 bg-emerald-500/20 text-emerald-100';
  }
  if (i === picked.value) {
    return 'border-rose-400/60 bg-rose-500/20 text-rose-100';
  }
  return 'border-white/[0.06] bg-white/[0.01] text-muted-foreground opacity-60';
}
</script>

<template>
  <div class="space-y-6">
    <!-- Page header -->
    <GlassPanel glow="cyan" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Kiểm tra kiến thức</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Bài kiểm tra</span>
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Trả lời nhanh trước khi hết giờ, giữ chuỗi đúng để nhân điểm thưởng.
          </p>
        </div>
        <button
          v-if="view !== 'catalog'"
          type="button"
          class="inline-flex shrink-0 items-center gap-2 self-start rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40"
          @click="backToCatalog"
        >
          <AppIcon name="chevron-left" class="h-4 w-4" />
          Danh sách bài
        </button>
      </div>
    </GlassPanel>

    <!-- ───────────────── CATALOG ───────────────── -->
    <SectionPanel
      v-if="view === 'catalog'"
      title="Thư viện bài kiểm tra"
      subtitle="Chọn một bài để bắt đầu — hệ thống tính giờ từng câu"
      icon="target"
      accent="cyan"
    >
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="quiz in quizzes"
          :key="quiz.id"
          class="group flex flex-col gap-4 rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 transition-all hover:-translate-y-1 hover:border-white/[0.14]"
        >
          <div class="flex items-start justify-between gap-3">
            <span
              class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm transition-transform group-hover:scale-110"
              :class="quizIconClasses[quiz.accent]"
            >
              <AppIcon :name="quiz.icon" class="h-6 w-6" />
            </span>
            <span
              class="rounded-sm border px-2.5 py-1 text-[0.65rem] font-semibold"
              :class="difficultyMeta[quiz.difficulty].classes"
            >
              {{ difficultyMeta[quiz.difficulty].label }}
            </span>
          </div>

          <div class="min-w-0">
            <p class="font-semibold leading-snug text-foreground">{{ quiz.title }}</p>
            <p class="mt-0.5 text-xs text-muted-foreground">{{ quiz.subject }}</p>
          </div>

          <div class="flex flex-wrap gap-2 text-xs">
            <span class="inline-flex items-center gap-1 rounded-sm bg-white/[0.04] px-2 py-1 text-muted-foreground">
              <AppIcon name="list" class="h-3.5 w-3.5" />{{ quiz.questions.length }} câu
            </span>
            <span class="inline-flex items-center gap-1 rounded-sm bg-white/[0.04] px-2 py-1 text-muted-foreground">
              <AppIcon name="clock" class="h-3.5 w-3.5" />~{{ quiz.estMinutes }} phút
            </span>
            <span class="inline-flex items-center gap-1 rounded-sm bg-amber-500/10 px-2 py-1 text-amber-300">
              <AppIcon name="coin" class="h-3.5 w-3.5" />{{ quiz.coinReward }}
            </span>
          </div>

          <div class="flex items-center justify-between border-t border-white/[0.06] pt-3">
            <span class="text-xs text-muted-foreground">
              Điểm cao nhất:
              <span class="font-semibold" :class="quiz.bestScore > 0 ? 'text-cyan-300' : 'text-muted-foreground'">
                {{ quiz.bestScore > 0 ? quiz.bestScore + '%' : 'Chưa làm' }}
              </span>
            </span>
            <button
              type="button"
              class="cosmic-btn rounded-sm px-3.5 py-1.5 text-xs font-semibold"
              @click="startQuiz(quiz)"
            >
              <AppIcon name="play" class="mr-1 inline h-3.5 w-3.5" />Bắt đầu
            </button>
          </div>
        </div>
      </div>
    </SectionPanel>

    <!-- ───────────────── RUNNER ───────────────── -->
    <template v-if="view === 'runner' && activeQuiz && currentQuestion">
      <GlassPanel :glow="timerAccent" class="p-5 sm:p-6">
        <!-- timer + progress header -->
        <div class="flex items-center gap-4">
          <div class="relative">
            <RingProgress :value="timerPct" :size="76" :stroke="8" :accent="timerAccent">
              <span class="text-lg font-bold tabular-nums text-foreground">{{ timeLeft }}</span>
              <span class="text-[0.6rem] text-muted-foreground">giây</span>
            </RingProgress>
          </div>

          <div class="min-w-0 flex-1">
            <div class="mb-1.5 flex items-center justify-between text-sm">
              <span class="font-semibold text-foreground">Câu {{ qIndex + 1 }}/{{ totalQuestions }}</span>
              <span
                class="inline-flex items-center gap-1 rounded-sm px-2.5 py-1 text-xs font-semibold transition-colors"
                :class="streak >= 2 ? 'bg-fuchsia-500/15 text-fuchsia-300 combo-glow' : 'bg-white/[0.06] text-muted-foreground'"
              >
                <AppIcon name="flame" class="h-3.5 w-3.5" />
                Chuỗi {{ streak }}
              </span>
            </div>
            <ProgressBar :value="runnerProgress" :accent="activeQuiz.accent" :height="8" />
            <p class="mt-1.5 truncate text-xs text-muted-foreground">{{ activeQuiz.title }}</p>
          </div>
        </div>

        <!-- question -->
        <div class="mt-6 rounded-sm border border-white/[0.06] bg-white/[0.02] p-5">
          <p class="text-lg font-semibold leading-relaxed text-foreground">{{ currentQuestion.text }}</p>
        </div>

        <!-- options -->
        <div class="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
          <button
            v-for="(opt, i) in currentQuestion.options"
            :key="i"
            type="button"
            class="flex items-center gap-3 rounded-sm border px-4 py-3.5 text-left text-sm font-medium transition-all"
            :class="optionClasses(i)"
            :disabled="checked"
            @click="selectOption(i)"
          >
            <span
              class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm border border-white/10 bg-white/[0.04] text-xs font-bold"
            >
              {{ ['A', 'B', 'C', 'D'][i] }}
            </span>
            <span class="flex-1">{{ opt }}</span>
            <AppIcon
              v-if="checked && i === currentQuestion.correct"
              name="check-circle"
              class="h-5 w-5 text-emerald-300"
            />
            <AppIcon
              v-else-if="checked && i === picked"
              name="x"
              class="h-5 w-5 text-rose-300"
            />
          </button>
        </div>

        <!-- explanation -->
        <transition name="fade-up">
          <div
            v-if="checked"
            class="mt-4 flex items-start gap-2.5 rounded-sm border p-3.5 text-sm"
            :class="picked === currentQuestion.correct
              ? 'border-emerald-400/30 bg-emerald-500/[0.07] text-emerald-100'
              : 'border-amber-400/30 bg-amber-500/[0.07] text-amber-100'"
          >
            <AppIcon
              :name="picked === currentQuestion.correct ? 'check-circle' : 'lightbulb'"
              class="mt-0.5 h-4.5 w-4.5 shrink-0"
            />
            <div>
              <p class="font-semibold">
                {{ picked === currentQuestion.correct ? 'Chính xác!' : 'Chưa đúng' }}
              </p>
              <p class="mt-0.5 text-muted-foreground">{{ currentQuestion.explanation }}</p>
            </div>
          </div>
        </transition>

        <!-- action -->
        <div class="mt-5 flex justify-end">
          <button
            v-if="!checked"
            type="button"
            class="cosmic-btn rounded-sm px-5 py-2.5 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-40"
            :disabled="picked === null"
            @click="checkAnswer"
          >
            <AppIcon name="check" class="mr-1.5 inline h-4 w-4" />Kiểm tra
          </button>
          <button
            v-else
            type="button"
            class="cosmic-btn rounded-sm px-5 py-2.5 text-sm font-semibold"
            @click="nextQuestion"
          >
            {{ qIndex === totalQuestions - 1 ? 'Xem kết quả' : 'Câu tiếp' }}
            <AppIcon name="arrow-right" class="ml-1.5 inline h-4 w-4" />
          </button>
        </div>
      </GlassPanel>
    </template>

    <!-- ───────────────── RESULTS ───────────────── -->
    <template v-if="view === 'results' && activeQuiz">
      <GlassPanel glow="green" class="relative overflow-hidden p-6 sm:p-8">
        <!-- confetti -->
        <div class="pointer-events-none absolute inset-0 overflow-hidden">
          <span
            v-for="c in confetti"
            :key="c.id"
            class="confetti absolute"
            :style="{
              left: c.left + '%',
              backgroundColor: c.color,
              animationDelay: c.delay + 's',
              animationDuration: c.duration + 's',
              transform: 'rotate(' + c.rotate + 'deg)',
            }"
          />
        </div>

        <div class="relative flex flex-col items-center gap-5 text-center">
          <span class="cosmic-badge">Kết quả bài kiểm tra</span>
          <RingProgress :value="displayScore" :size="160" :stroke="13" accent="green">
            <span class="text-4xl font-bold tabular-nums text-emerald-50">{{ displayScore }}%</span>
            <span class="text-xs text-muted-foreground">đúng</span>
          </RingProgress>
          <h2 class="text-xl font-bold text-foreground">{{ activeQuiz.title }}</h2>

          <div class="grid w-full max-w-xl grid-cols-2 gap-3 sm:grid-cols-4">
            <div class="rounded-sm border border-emerald-400/20 bg-emerald-500/[0.07] p-3">
              <p class="text-lg font-bold tabular-nums text-emerald-200">{{ correctCount }}</p>
              <p class="text-[0.65rem] text-muted-foreground">Câu đúng</p>
            </div>
            <div class="rounded-sm border border-rose-400/20 bg-rose-500/[0.07] p-3">
              <p class="text-lg font-bold tabular-nums text-rose-200">{{ wrongCount }}</p>
              <p class="text-[0.65rem] text-muted-foreground">Câu sai</p>
            </div>
            <div class="rounded-sm border border-violet-400/20 bg-violet-500/[0.07] p-3">
              <p class="text-lg font-bold tabular-nums text-violet-200">+{{ earnedXp }}</p>
              <p class="text-[0.65rem] text-muted-foreground">XP nhận</p>
            </div>
            <div class="rounded-sm border border-amber-400/20 bg-amber-500/[0.07] p-3">
              <p class="text-lg font-bold tabular-nums text-amber-200">+{{ earnedCoin }}</p>
              <p class="text-[0.65rem] text-muted-foreground">Coin nhận</p>
            </div>
          </div>

          <span class="rounded-sm bg-fuchsia-500/15 px-3 py-1 text-xs font-semibold text-fuchsia-300">
            <AppIcon name="flame" class="mr-1 inline h-3.5 w-3.5" />Chuỗi đúng dài nhất: {{ bestStreak }}
          </span>

          <div class="flex flex-wrap items-center justify-center gap-3">
            <button type="button" class="cosmic-btn rounded-sm px-4 py-2 text-sm" @click="retryQuiz">
              <AppIcon name="refresh" class="mr-1.5 inline h-4 w-4" />Làm lại
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-1.5 rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40"
              @click="backToCatalog"
            >
              <AppIcon name="list" class="h-4 w-4" />Về danh sách
            </button>
          </div>
        </div>
      </GlassPanel>

      <!-- per-question review -->
      <SectionPanel
        title="Xem lại từng câu"
        subtitle="Đối chiếu đáp án bạn chọn với đáp án đúng"
        icon="list"
        accent="violet"
      >
        <div class="space-y-3">
          <div
            v-for="row in reviewRows"
            :key="row.index"
            class="rounded-sm border p-4"
            :class="row.isCorrect
              ? 'border-emerald-400/20 bg-emerald-500/[0.04]'
              : 'border-rose-400/20 bg-rose-500/[0.04]'"
          >
            <div class="flex items-start gap-3">
              <span
                class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm text-xs font-bold"
                :class="row.isCorrect ? 'bg-emerald-500/15 text-emerald-300' : 'bg-rose-500/15 text-rose-300'"
              >
                {{ row.index + 1 }}
              </span>
              <div class="min-w-0 flex-1">
                <p class="text-sm font-medium text-foreground">{{ row.text }}</p>
                <p class="mt-1.5 text-xs">
                  <span class="text-muted-foreground">Bạn chọn: </span>
                  <span :class="row.isCorrect ? 'text-emerald-300' : 'text-rose-300'">
                    {{ row.picked !== null ? row.options[row.picked] : 'Không trả lời' }}
                  </span>
                </p>
                <p v-if="!row.isCorrect" class="mt-0.5 text-xs">
                  <span class="text-muted-foreground">Đáp án đúng: </span>
                  <span class="text-emerald-300">{{ row.options[row.correct] }}</span>
                </p>
              </div>
              <AppIcon
                :name="row.isCorrect ? 'check-circle' : 'x'"
                class="h-5 w-5 shrink-0"
                :class="row.isCorrect ? 'text-emerald-300' : 'text-rose-300'"
              />
            </div>
          </div>
        </div>
      </SectionPanel>
    </template>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.option-pulse {
  animation: option-pulse 0.4s ease;
}

@keyframes option-pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.015);
  }
  100% {
    transform: scale(1);
  }
}

.combo-glow {
  animation: combo-glow 1.2s ease-in-out infinite;
}

@keyframes combo-glow {
  0%,
  100% {
    box-shadow: 0 0 0 0 oklch(0.68 0.22 350 / 30%);
  }
  50% {
    box-shadow: 0 0 18px 2px oklch(0.68 0.22 350 / 45%);
  }
}

.confetti {
  top: -12px;
  width: 9px;
  height: 14px;
  border-radius: 2px;
  animation-name: confetti-fall;
  animation-timing-function: linear;
  animation-fill-mode: forwards;
}

@keyframes confetti-fall {
  0% {
    transform: translateY(-12px) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(420px) rotate(540deg);
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
  .option-pulse,
  .combo-glow,
  .confetti,
  .fade-up-enter-active {
    animation: none;
    transition: none;
  }
}
</style>
