<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdminStatCard from '@/components/admin/AdminStatCard.vue';
import AdminPill from '@/components/admin/AdminPill.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import RingProgress from '@/components/app/RingProgress.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type PillTone = 'cyan' | 'green' | 'amber' | 'rose' | 'violet' | 'magenta' | 'neutral';

/* ---------------- KPI ---------------- */
interface Kpi {
  label: string;
  value: number;
  prefix?: string;
  suffix?: string;
  decimals?: number;
  icon: string;
  accent: Accent;
  trend: number;
  trendLabel: string;
  spark: number[];
}
const kpis: Kpi[] = [
  { label: 'Nhiệm vụ đang chạy', value: 24, icon: 'target', accent: 'cyan', trend: 9.2, trendLabel: '+3 trong tuần', spark: [16, 17, 19, 18, 21, 22, 23, 24] },
  { label: 'Huy hiệu', value: 48, icon: 'award', accent: 'violet', trend: 4.3, trendLabel: '12 sử thi · 6 huyền thoại', spark: [40, 41, 43, 44, 45, 46, 47, 48] },
  { label: 'Coin phát hành hôm nay', value: 1.2, suffix: 'M', decimals: 1, icon: 'coin', accent: 'amber', trend: 14.7, trendLabel: 'qua nhiệm vụ & điểm danh', spark: [0.8, 0.9, 1.0, 0.95, 1.1, 1.05, 1.15, 1.2] },
  { label: 'Tỷ lệ hoàn thành nhiệm vụ', value: 68, suffix: '%', icon: 'check-circle', accent: 'green', trend: 6.1, trendLabel: 'trung bình 30 ngày', spark: [58, 60, 62, 61, 64, 66, 67, 68] }
];

/* ---------------- Tabs ---------------- */
interface TabDef { key: string; label: string; icon: string; accent: Accent }
const tabs: TabDef[] = [
  { key: 'quests', label: 'Nhiệm vụ', icon: 'target', accent: 'cyan' },
  { key: 'badges', label: 'Huy hiệu', icon: 'award', accent: 'violet' },
  { key: 'daily', label: 'Phần thưởng hằng ngày', icon: 'gift', accent: 'amber' },
  { key: 'wheel', label: 'Vòng quay', icon: 'refresh', accent: 'magenta' },
  { key: 'economy', label: 'Kinh tế', icon: 'coin', accent: 'green' }
];
const tab = ref('quests');

/* ---------------- Quests ---------------- */
type QuestType = 'Hằng ngày' | 'Hằng tuần' | 'Sự kiện';
interface Quest {
  id: number;
  name: string;
  desc: string;
  type: QuestType;
  coin: number;
  xp: number;
  completed: number;
  target: number;
  active: boolean;
}
const questTypeTone: Record<QuestType, PillTone> = {
  'Hằng ngày': 'cyan',
  'Hằng tuần': 'violet',
  'Sự kiện': 'magenta'
};
const quests = reactive<Quest[]>([
  { id: 1, name: 'Đăng nhập mỗi ngày', desc: 'Mở ứng dụng và điểm danh', type: 'Hằng ngày', coin: 50, xp: 20, completed: 8420, target: 12428, active: true },
  { id: 2, name: 'Học 30 phút liên tục', desc: 'Tập trung không gián đoạn', type: 'Hằng ngày', coin: 80, xp: 40, completed: 5210, target: 12428, active: true },
  { id: 3, name: 'Hoàn thành 1 bộ flashcard', desc: 'Ôn xong toàn bộ thẻ trong 1 bộ', type: 'Hằng ngày', coin: 60, xp: 30, completed: 4180, target: 12428, active: true },
  { id: 4, name: 'Tải lên 1 tài liệu', desc: 'Chia sẻ tài liệu cho cộng đồng', type: 'Hằng ngày', coin: 120, xp: 60, completed: 1320, target: 12428, active: true },
  { id: 5, name: 'Học đủ 7 ngày liên tiếp', desc: 'Duy trì streak một tuần', type: 'Hằng tuần', coin: 500, xp: 250, completed: 2840, target: 12428, active: true },
  { id: 6, name: 'Tạo 3 bộ flashcard', desc: 'Soạn thẻ ôn tập cho bản thân', type: 'Hằng tuần', coin: 300, xp: 150, completed: 1670, target: 12428, active: true },
  { id: 7, name: 'Đạt top 50 bảng xếp hạng', desc: 'Leo hạng trong tuần', type: 'Hằng tuần', coin: 400, xp: 200, completed: 980, target: 12428, active: false },
  { id: 8, name: 'Mời 1 bạn cùng học', desc: 'Giới thiệu bạn bè tham gia', type: 'Hằng tuần', coin: 350, xp: 180, completed: 720, target: 12428, active: true },
  { id: 9, name: 'Sự kiện Mùa thi 2026', desc: 'Học 100 thẻ trong tuần lễ ôn thi', type: 'Sự kiện', coin: 1500, xp: 800, completed: 3420, target: 12428, active: true },
  { id: 10, name: 'Thử thách 21 ngày tập trung', desc: 'Hoàn thành phiên học mỗi ngày', type: 'Sự kiện', coin: 2000, xp: 1200, completed: 1180, target: 12428, active: true },
  { id: 11, name: 'Săn huy hiệu Huyền thoại', desc: 'Mở khoá huy hiệu hiếm nhất', type: 'Sự kiện', coin: 3000, xp: 1500, completed: 210, target: 12428, active: false },
  { id: 12, name: 'Bình luận hữu ích trên forum', desc: 'Nhận 10 lượt thích bình luận', type: 'Hằng tuần', coin: 200, xp: 100, completed: 1540, target: 12428, active: true }
]);
function toggleQuest(q: Quest) {
  q.active = !q.active;
}

/* Quest create form */
const showQuestForm = ref(false);
interface QuestForm {
  name: string;
  type: QuestType;
  condition: string;
  coin: number;
  xp: number;
  active: boolean;
}
const newQuest = reactive<QuestForm>({
  name: '',
  type: 'Hằng ngày',
  condition: '',
  coin: 50,
  xp: 20,
  active: true
});
const questTypes: QuestType[] = ['Hằng ngày', 'Hằng tuần', 'Sự kiện'];
let questSeq = quests.length + 1;
function resetQuestForm() {
  newQuest.name = '';
  newQuest.type = 'Hằng ngày';
  newQuest.condition = '';
  newQuest.coin = 50;
  newQuest.xp = 20;
  newQuest.active = true;
}
function saveQuest() {
  if (!newQuest.name.trim()) return;
  quests.unshift({
    id: questSeq++,
    name: newQuest.name.trim(),
    desc: newQuest.condition.trim() || 'Nhiệm vụ tuỳ chỉnh',
    type: newQuest.type,
    coin: Number(newQuest.coin) || 0,
    xp: Number(newQuest.xp) || 0,
    completed: 0,
    target: 12428,
    active: newQuest.active
  });
  resetQuestForm();
  showQuestForm.value = false;
}

/* ---------------- Badges ---------------- */
type Rarity = 'Thường' | 'Hiếm' | 'Sử thi' | 'Huyền thoại';
interface Badge {
  id: number;
  name: string;
  icon: string;
  rarity: Rarity;
  unlocked: number;
  accent: Accent;
}
const rarityTone: Record<Rarity, PillTone> = {
  'Thường': 'neutral',
  'Hiếm': 'cyan',
  'Sử thi': 'violet',
  'Huyền thoại': 'amber'
};
const rarityAccent: Record<Rarity, Accent> = {
  'Thường': 'cyan',
  'Hiếm': 'cyan',
  'Sử thi': 'violet',
  'Huyền thoại': 'amber'
};
const badges: Badge[] = [
  { id: 1, name: 'Người mới khởi đầu', icon: 'star', rarity: 'Thường', unlocked: 92, accent: 'cyan' },
  { id: 2, name: 'Chăm chỉ 7 ngày', icon: 'flame', rarity: 'Thường', unlocked: 74, accent: 'cyan' },
  { id: 3, name: 'Thủ thư cộng đồng', icon: 'book-open', rarity: 'Hiếm', unlocked: 48, accent: 'cyan' },
  { id: 4, name: 'Bậc thầy Flashcard', icon: 'badge', rarity: 'Hiếm', unlocked: 36, accent: 'cyan' },
  { id: 5, name: 'Streak 30 ngày', icon: 'fire', rarity: 'Sử thi', unlocked: 21, accent: 'violet' },
  { id: 6, name: 'Quán quân tuần', icon: 'trophy', rarity: 'Sử thi', unlocked: 14, accent: 'violet' },
  { id: 7, name: 'Học giả tận tâm', icon: 'graduation', rarity: 'Sử thi', unlocked: 9, accent: 'violet' },
  { id: 8, name: 'Huyền thoại Mùa thi', icon: 'crown', rarity: 'Huyền thoại', unlocked: 3, accent: 'amber' },
  { id: 9, name: 'Bất bại 100 ngày', icon: 'award', rarity: 'Huyền thoại', unlocked: 1, accent: 'amber' }
];

/* ---------------- Daily rewards ---------------- */
interface DailyReward {
  day: number;
  type: 'coin' | 'gem' | 'x2';
  icon: string;
  accent: Accent;
  amount: number;
  special: boolean;
}
const dailyRewards = reactive<DailyReward[]>([
  { day: 1, type: 'coin', icon: 'coin', accent: 'amber', amount: 50, special: false },
  { day: 2, type: 'coin', icon: 'coin', accent: 'amber', amount: 80, special: false },
  { day: 3, type: 'gem', icon: 'gem', accent: 'cyan', amount: 5, special: false },
  { day: 4, type: 'coin', icon: 'coin', accent: 'amber', amount: 120, special: false },
  { day: 5, type: 'gem', icon: 'gem', accent: 'cyan', amount: 10, special: false },
  { day: 6, type: 'coin', icon: 'coin', accent: 'amber', amount: 200, special: false },
  { day: 7, type: 'x2', icon: 'sparkles', accent: 'magenta', amount: 25, special: true }
]);
const dailyTypeLabel: Record<DailyReward['type'], string> = {
  coin: 'Coin',
  gem: 'Gem',
  x2: 'Gem x2'
};
const checkinStats = [
  { label: 'Tỷ lệ điểm danh hôm nay', value: 71, accent: 'green' as Accent },
  { label: 'Hoàn thành đủ 7 ngày', value: 34, accent: 'cyan' as Accent },
  { label: 'Bỏ lỡ ≥ 1 ngày', value: 29, accent: 'amber' as Accent }
];

/* ---------------- Wheel ---------------- */
interface WheelSlot {
  id: number;
  label: string;
  type: 'coin' | 'gem' | 'x2' | 'trượt';
  accent: Accent;
  rate: number;
}
const wheelSlots = reactive<WheelSlot[]>([
  { id: 1, label: '50 Coin', type: 'coin', accent: 'amber', rate: 30 },
  { id: 2, label: '100 Coin', type: 'coin', accent: 'amber', rate: 22 },
  { id: 3, label: '5 Gem', type: 'gem', accent: 'cyan', rate: 15 },
  { id: 4, label: 'Chúc may mắn', type: 'trượt', accent: 'violet', rate: 12 },
  { id: 5, label: '300 Coin', type: 'coin', accent: 'amber', rate: 8 },
  { id: 6, label: '15 Gem', type: 'gem', accent: 'cyan', rate: 7 },
  { id: 7, label: 'Nhân x2 hôm nay', type: 'x2', accent: 'magenta', rate: 4 },
  { id: 8, label: '1000 Coin', type: 'coin', accent: 'amber', rate: 2 }
]);
const wheelTotal = computed(() => wheelSlots.reduce((sum, s) => sum + (Number(s.rate) || 0), 0));
const wheelColors = ['oklch(0.8 0.16 85)', 'oklch(0.78 0.18 195)', 'oklch(0.62 0.2 290)', 'oklch(0.68 0.22 350)'];
const wheelGradient = computed(() => {
  let acc = 0;
  const total = wheelTotal.value || 1;
  const stops = wheelSlots.map((s, i) => {
    const start = (acc / total) * 360;
    acc += Number(s.rate) || 0;
    const end = (acc / total) * 360;
    return `${wheelColors[i % wheelColors.length]} ${start}deg ${end}deg`;
  });
  return `conic-gradient(${stops.join(', ')})`;
});

/* ---------------- Economy ---------------- */
interface EconomyRow { label: string; icon: string; value: number; accent: Accent }
const coinSources: EconomyRow[] = [
  { label: 'Nhiệm vụ hằng ngày', icon: 'target', value: 540000, accent: 'cyan' },
  { label: 'Điểm danh', icon: 'calendar', value: 320000, accent: 'green' },
  { label: 'Sự kiện', icon: 'flag', value: 210000, accent: 'magenta' },
  { label: 'Vòng quay', icon: 'refresh', value: 130000, accent: 'amber' }
];
const coinSinks: EconomyRow[] = [
  { label: 'Cửa hàng vật phẩm', icon: 'store', value: 480000, accent: 'violet' },
  { label: 'Vòng quay may mắn', icon: 'refresh', value: 260000, accent: 'amber' },
  { label: 'Mở khoá tài liệu', icon: 'unlock', value: 190000, accent: 'cyan' },
  { label: 'Đổi quà VIP', icon: 'crown', value: 90000, accent: 'magenta' }
];
const totalSource = computed(() => coinSources.reduce((s, r) => s + r.value, 0));
const totalSink = computed(() => coinSinks.reduce((s, r) => s + r.value, 0));
const maxSource = Math.max(...coinSources.map((r) => r.value));
const maxSink = Math.max(...coinSinks.map((r) => r.value));
const inflation = computed(() => Math.round(((totalSource.value - totalSink.value) / totalSource.value) * 100));

interface Multiplier { id: number; label: string; desc: string; factor: string; enabled: boolean }
const multipliers = reactive<Multiplier[]>([
  { id: 1, label: 'Nhân đôi cuối tuần', desc: 'Coin x2 vào T7 & CN', factor: 'x2', enabled: true },
  { id: 2, label: 'Sự kiện Mùa thi', desc: 'XP x1.5 trong tuần lễ thi', factor: 'x1.5', enabled: true },
  { id: 3, label: 'Giờ vàng buổi tối', desc: 'Coin x1.5 từ 19h–22h', factor: 'x1.5', enabled: false }
]);
function toggleMultiplier(m: Multiplier) {
  m.enabled = !m.enabled;
}

function fmt(n: number) {
  return n.toLocaleString('vi-VN');
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2">
          <span class="h-1.5 w-1.5 rounded-full bg-fuchsia-400 pulse-glow-green" />
          <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">Hệ thống game hoá · Điều phối phần thưởng</p>
        </div>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Quản lý Gamification 🎮</h2>
      </div>
      <button
        class="cosmic-btn flex items-center gap-2 self-start rounded-xl px-4 py-2 text-sm"
        @click="tab = 'quests'; showQuestForm = true"
      >
        <AppIcon name="plus" class="h-4 w-4" /> Tạo nhiệm vụ
      </button>
    </div>

    <!-- KPI row -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <AdminStatCard
        v-for="k in kpis"
        :key="k.label"
        :label="k.label"
        :value="k.value"
        :prefix="k.prefix"
        :suffix="k.suffix"
        :decimals="k.decimals"
        :icon="k.icon"
        :accent="k.accent"
        :trend="k.trend"
        :trend-label="k.trendLabel"
        :sparkline="k.spark"
      />
    </div>

    <!-- Tab bar -->
    <div class="flex flex-wrap gap-1.5 rounded-2xl border border-white/[0.06] bg-white/[0.02] p-1.5">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="flex items-center gap-2 rounded-xl px-3.5 py-2 text-sm font-medium transition-all"
        :class="tab === t.key
          ? 'bg-white/[0.08] text-foreground shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08)]'
          : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'"
        @click="tab = t.key"
      >
        <AppIcon :name="t.icon" class="h-4 w-4" />
        {{ t.label }}
      </button>
    </div>

    <!-- ============ TAB: NHIỆM VỤ ============ -->
    <div v-if="tab === 'quests'" class="space-y-4">
      <!-- Create form -->
      <AdminPanel v-if="showQuestForm" title="Tạo nhiệm vụ mới" subtitle="Cấu hình điều kiện & phần thưởng" icon="plus" accent="cyan" glow="cyan">
        <template #actions>
          <button class="flex h-8 w-8 items-center justify-center rounded-lg bg-white/[0.05] text-muted-foreground transition-colors hover:bg-white/[0.1] hover:text-foreground" @click="showQuestForm = false">
            <AppIcon name="x" class="h-4 w-4" />
          </button>
        </template>
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <label class="block">
            <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Tên nhiệm vụ</span>
            <input v-model="newQuest.name" type="text" placeholder="VD: Học 45 phút mỗi ngày" class="cosmic-input w-full rounded-xl px-3 py-2 text-sm" />
          </label>
          <label class="block">
            <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Loại nhiệm vụ</span>
            <select v-model="newQuest.type" class="cosmic-input w-full rounded-xl px-3 py-2 text-sm">
              <option v-for="qt in questTypes" :key="qt" :value="qt">{{ qt }}</option>
            </select>
          </label>
          <label class="block md:col-span-2">
            <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Điều kiện hoàn thành</span>
            <input v-model="newQuest.condition" type="text" placeholder="VD: Hoàn thành 3 phiên học tập trung" class="cosmic-input w-full rounded-xl px-3 py-2 text-sm" />
          </label>
          <label class="block">
            <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Phần thưởng Coin</span>
            <div class="relative">
              <AppIcon name="coin" class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-amber-300" />
              <input v-model.number="newQuest.coin" type="number" min="0" class="cosmic-input w-full rounded-xl py-2 pl-9 pr-3 text-sm" />
            </div>
          </label>
          <label class="block">
            <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Phần thưởng XP</span>
            <div class="relative">
              <AppIcon name="zap" class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-cyan-300" />
              <input v-model.number="newQuest.xp" type="number" min="0" class="cosmic-input w-full rounded-xl py-2 pl-9 pr-3 text-sm" />
            </div>
          </label>
        </div>
        <div class="mt-4 flex items-center justify-between border-t border-white/[0.06] pt-4">
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors"
              :class="newQuest.active ? 'bg-cyan-500/70 shadow-[0_0_10px_oklch(0.78_0.18_195/45%)]' : 'bg-white/[0.12]'"
              @click="newQuest.active = !newQuest.active"
            >
              <span class="inline-block h-5 w-5 transform rounded-full bg-white transition-transform" :class="newQuest.active ? 'translate-x-[22px]' : 'translate-x-0.5'" />
            </button>
            <span class="text-sm text-foreground">Kích hoạt ngay khi lưu</span>
          </div>
          <div class="flex items-center gap-2">
            <button class="rounded-xl border border-white/[0.08] px-4 py-2 text-sm text-muted-foreground transition-colors hover:bg-white/[0.04] hover:text-foreground" @click="showQuestForm = false">Huỷ</button>
            <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm" @click="saveQuest">
              <AppIcon name="check" class="h-4 w-4" /> Lưu nhiệm vụ
            </button>
          </div>
        </div>
      </AdminPanel>

      <!-- Quest list -->
      <AdminPanel title="Danh sách nhiệm vụ" :subtitle="`${quests.length} nhiệm vụ · ${quests.filter((q) => q.active).length} đang bật`" icon="list" accent="cyan">
        <template #actions>
          <button v-if="!showQuestForm" class="flex items-center gap-1.5 rounded-lg bg-cyan-500/10 px-2.5 py-1 text-xs font-medium text-cyan-300 transition-colors hover:bg-cyan-500/20" @click="showQuestForm = true">
            <AppIcon name="plus" class="h-3.5 w-3.5" /> Tạo nhiệm vụ
          </button>
        </template>
        <ul class="space-y-2">
          <li
            v-for="q in quests"
            :key="q.id"
            class="flex flex-col gap-3 rounded-xl border border-white/[0.05] bg-white/[0.02] px-3.5 py-3 transition-colors hover:bg-white/[0.04] sm:flex-row sm:items-center"
          >
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <p class="truncate text-sm font-medium text-foreground">{{ q.name }}</p>
                <AdminPill :tone="questTypeTone[q.type]">{{ q.type }}</AdminPill>
              </div>
              <p class="truncate text-xs text-muted-foreground">{{ q.desc }}</p>
            </div>
            <div class="flex items-center gap-1.5 sm:w-28">
              <span class="inline-flex items-center gap-1 text-xs font-semibold text-amber-300"><AppIcon name="coin" class="h-3.5 w-3.5" />{{ fmt(q.coin) }}</span>
              <span class="inline-flex items-center gap-1 text-xs font-semibold text-cyan-300"><AppIcon name="zap" class="h-3.5 w-3.5" />{{ fmt(q.xp) }}</span>
            </div>
            <div class="sm:w-44">
              <div class="mb-1 flex items-center justify-between text-[11px] text-muted-foreground">
                <span>Hoàn thành</span>
                <span class="font-mono font-semibold text-foreground">{{ fmt(q.completed) }}</span>
              </div>
              <ProgressBar :value="Math.round((q.completed / q.target) * 100)" :accent="q.active ? 'cyan' : 'violet'" />
            </div>
            <div class="flex items-center gap-2 sm:w-24 sm:justify-end">
              <button
                type="button"
                class="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors"
                :class="q.active ? 'bg-emerald-500/70 shadow-[0_0_10px_oklch(0.75_0.18_145/45%)]' : 'bg-white/[0.12]'"
                :title="q.active ? 'Đang bật' : 'Đang tắt'"
                @click="toggleQuest(q)"
              >
                <span class="inline-block h-5 w-5 transform rounded-full bg-white transition-transform" :class="q.active ? 'translate-x-[22px]' : 'translate-x-0.5'" />
              </button>
            </div>
          </li>
        </ul>
      </AdminPanel>
    </div>

    <!-- ============ TAB: HUY HIỆU ============ -->
    <div v-else-if="tab === 'badges'" class="space-y-4">
      <AdminPanel title="Bộ sưu tập huy hiệu" subtitle="Theo độ hiếm & tỷ lệ mở khoá" icon="award" accent="violet" glow="magenta">
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <div
            v-for="b in badges"
            :key="b.id"
            class="group flex items-center gap-3 rounded-2xl border border-white/[0.06] bg-white/[0.02] p-3.5 transition-all hover:-translate-y-0.5 hover:bg-white/[0.04]"
          >
            <div class="relative">
              <RingProgress :value="b.unlocked" :size="64" :stroke="6" :accent="rarityAccent[b.rarity]" />
              <span class="absolute inset-0 flex items-center justify-center" :class="`text-${b.accent === 'amber' ? 'amber' : b.accent === 'violet' ? 'violet' : 'cyan'}-300`">
                <AppIcon :name="b.icon" class="h-5 w-5" />
              </span>
            </div>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-foreground">{{ b.name }}</p>
              <div class="mt-1 flex items-center gap-2">
                <AdminPill :tone="rarityTone[b.rarity]">{{ b.rarity }}</AdminPill>
                <span class="font-mono text-[11px] text-muted-foreground">{{ b.unlocked }}% mở khoá</span>
              </div>
            </div>
            <button class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/[0.05] text-muted-foreground opacity-0 transition-all hover:bg-white/[0.1] hover:text-foreground group-hover:opacity-100" title="Sửa huy hiệu">
              <AppIcon name="edit" class="h-4 w-4" />
            </button>
          </div>
        </div>
      </AdminPanel>
    </div>

    <!-- ============ TAB: PHẦN THƯỞNG HẰNG NGÀY ============ -->
    <div v-else-if="tab === 'daily'" class="space-y-4">
      <AdminPanel title="Chuỗi điểm danh 7 ngày" subtitle="Cấu hình phần thưởng từng ngày" icon="gift" accent="amber" glow="none">
        <div class="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-7">
          <div
            v-for="r in dailyRewards"
            :key="r.day"
            class="relative flex flex-col items-center gap-2 rounded-2xl border p-3 text-center transition-colors"
            :class="r.special ? 'border-fuchsia-500/30 bg-fuchsia-500/[0.06]' : 'border-white/[0.06] bg-white/[0.02]'"
          >
            <span class="font-mono text-[11px] uppercase tracking-wide text-muted-foreground">Ngày {{ r.day }}</span>
            <span
              class="flex h-11 w-11 items-center justify-center rounded-xl"
              :class="r.accent === 'amber' ? 'bg-amber-500/10 text-amber-300' : r.accent === 'cyan' ? 'bg-cyan-500/10 text-cyan-300' : 'bg-fuchsia-500/10 text-fuchsia-300'"
            >
              <AppIcon :name="r.icon" class="h-5 w-5" />
            </span>
            <span class="text-[11px] font-medium text-foreground">{{ dailyTypeLabel[r.type] }}</span>
            <input v-model.number="r.amount" type="number" min="0" class="cosmic-input w-full rounded-lg px-2 py-1 text-center text-sm font-semibold" />
            <span v-if="r.special" class="absolute -top-2 right-2 inline-flex items-center gap-1 rounded-full border border-fuchsia-500/30 bg-fuchsia-500/15 px-2 py-0.5 text-[10px] font-medium text-fuchsia-300">
              <AppIcon name="crown" class="h-3 w-3" /> Đặc biệt
            </span>
          </div>
        </div>
      </AdminPanel>

      <AdminPanel title="Thống kê điểm danh" subtitle="Tỷ lệ tham gia 30 ngày" icon="chart" accent="green">
        <ul class="space-y-3">
          <li v-for="s in checkinStats" :key="s.label">
            <div class="mb-1 flex items-center justify-between text-xs">
              <span class="text-muted-foreground">{{ s.label }}</span>
              <span class="font-mono font-semibold text-foreground">{{ s.value }}%</span>
            </div>
            <ProgressBar :value="s.value" :accent="s.accent" />
          </li>
        </ul>
      </AdminPanel>
    </div>

    <!-- ============ TAB: VÒNG QUAY ============ -->
    <div v-else-if="tab === 'wheel'" class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <AdminPanel class="lg:col-span-2" title="Cấu hình ô phần thưởng" subtitle="8 ô · điều chỉnh tỉ lệ trúng" icon="sliders" accent="magenta" glow="magenta">
        <template #actions>
          <AdminPill :tone="wheelTotal === 100 ? 'green' : 'amber'" dot>Tổng {{ wheelTotal }}%</AdminPill>
        </template>
        <ul class="space-y-2.5">
          <li
            v-for="s in wheelSlots"
            :key="s.id"
            class="flex items-center gap-3 rounded-xl border border-white/[0.05] bg-white/[0.02] px-3 py-2.5"
          >
            <span
              class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg"
              :class="s.accent === 'amber' ? 'bg-amber-500/10 text-amber-300' : s.accent === 'cyan' ? 'bg-cyan-500/10 text-cyan-300' : s.accent === 'magenta' ? 'bg-fuchsia-500/10 text-fuchsia-300' : 'bg-violet-500/10 text-violet-300'"
            >
              <AppIcon :name="s.type === 'coin' ? 'coin' : s.type === 'gem' ? 'gem' : s.type === 'x2' ? 'sparkles' : 'dot'" class="h-4 w-4" />
            </span>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-medium text-foreground">{{ s.label }}</p>
              <ProgressBar class="mt-1.5" :value="s.rate" :accent="s.accent" />
            </div>
            <div class="flex shrink-0 items-center gap-1">
              <input v-model.number="s.rate" type="number" min="0" max="100" class="cosmic-input w-16 rounded-lg px-2 py-1 text-center text-sm font-semibold" />
              <span class="text-xs text-muted-foreground">%</span>
            </div>
          </li>
        </ul>
        <p v-if="wheelTotal !== 100" class="mt-3 flex items-center gap-1.5 text-xs text-amber-300">
          <AppIcon name="alert" class="h-3.5 w-3.5" /> Tổng tỉ lệ nên bằng 100% (hiện tại {{ wheelTotal }}%).
        </p>
      </AdminPanel>

      <AdminPanel title="Minh hoạ vòng quay" subtitle="Phân bố theo tỉ lệ" icon="refresh" accent="amber">
        <div class="flex flex-col items-center gap-4">
          <div class="relative">
            <div
              class="h-44 w-44 rounded-full border-4 border-white/10 shadow-[0_0_30px_oklch(0.68_0.22_350/25%)]"
              :style="{ background: wheelGradient }"
            />
            <span class="absolute inset-0 m-auto flex h-16 w-16 flex-col items-center justify-center rounded-full border border-white/10 bg-[oklch(0.18_0.03_280)] text-center">
              <AppIcon name="gift" class="h-5 w-5 text-fuchsia-300" />
            </span>
            <span class="absolute -top-1 left-1/2 -translate-x-1/2 text-amber-300">
              <AppIcon name="chevron-down" class="h-5 w-5" />
            </span>
          </div>
          <ul class="w-full space-y-1.5">
            <li v-for="(s, i) in wheelSlots" :key="s.id" class="flex items-center gap-2 text-xs">
              <span class="h-2.5 w-2.5 shrink-0 rounded-sm" :style="{ background: wheelColors[i % wheelColors.length] }" />
              <span class="min-w-0 flex-1 truncate text-muted-foreground">{{ s.label }}</span>
              <span class="font-mono font-semibold text-foreground">{{ s.rate }}%</span>
            </li>
          </ul>
        </div>
      </AdminPanel>
    </div>

    <!-- ============ TAB: KINH TẾ ============ -->
    <div v-else class="space-y-4">
      <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <AdminPanel title="Nguồn phát coin" :subtitle="`${fmt(totalSource)} coin / ngày`" icon="trending-up" accent="green" glow="green">
          <ul class="space-y-3">
            <li v-for="r in coinSources" :key="r.label">
              <div class="mb-1 flex items-center gap-2 text-xs">
                <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-white/[0.05] text-foreground"><AppIcon :name="r.icon" class="h-3.5 w-3.5" /></span>
                <span class="min-w-0 flex-1 truncate text-muted-foreground">{{ r.label }}</span>
                <span class="font-mono font-semibold text-foreground">{{ fmt(r.value) }}</span>
              </div>
              <ProgressBar :value="Math.round((r.value / maxSource) * 100)" :accent="r.accent" />
            </li>
          </ul>
        </AdminPanel>

        <AdminPanel title="Nơi tiêu coin" :subtitle="`${fmt(totalSink)} coin / ngày`" icon="trending-down" accent="magenta" glow="magenta">
          <ul class="space-y-3">
            <li v-for="r in coinSinks" :key="r.label">
              <div class="mb-1 flex items-center gap-2 text-xs">
                <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-white/[0.05] text-foreground"><AppIcon :name="r.icon" class="h-3.5 w-3.5" /></span>
                <span class="min-w-0 flex-1 truncate text-muted-foreground">{{ r.label }}</span>
                <span class="font-mono font-semibold text-foreground">{{ fmt(r.value) }}</span>
              </div>
              <ProgressBar :value="Math.round((r.value / maxSink) * 100)" :accent="r.accent" />
            </li>
          </ul>
        </AdminPanel>

        <AdminPanel title="Cân bằng kinh tế" subtitle="Phát hành vs tiêu thụ" icon="chart" accent="cyan">
          <div class="flex flex-col items-center">
            <div class="relative">
              <RingProgress :value="Math.max(0, inflation)" :size="132" :stroke="12" accent="amber" />
              <span class="absolute inset-0 flex flex-col items-center justify-center">
                <span class="text-2xl font-bold text-foreground">{{ inflation }}%</span>
                <span class="text-[11px] text-muted-foreground">Lạm phát coin</span>
              </span>
            </div>
            <div class="mt-4 w-full space-y-2 text-xs">
              <div class="flex items-center justify-between rounded-lg border border-white/[0.05] bg-white/[0.02] px-3 py-2">
                <span class="text-muted-foreground">Phát hành / ngày</span>
                <span class="font-mono font-semibold text-emerald-300">+{{ fmt(totalSource) }}</span>
              </div>
              <div class="flex items-center justify-between rounded-lg border border-white/[0.05] bg-white/[0.02] px-3 py-2">
                <span class="text-muted-foreground">Tiêu thụ / ngày</span>
                <span class="font-mono font-semibold text-fuchsia-300">-{{ fmt(totalSink) }}</span>
              </div>
            </div>
          </div>
        </AdminPanel>
      </div>

      <AdminPanel title="Hệ số nhân thưởng" subtitle="Bật/tắt các đợt khuyến mãi" icon="zap" accent="violet">
        <ul class="space-y-2.5">
          <li
            v-for="m in multipliers"
            :key="m.id"
            class="flex items-center gap-3 rounded-xl border border-white/[0.05] bg-white/[0.02] px-3.5 py-3"
          >
            <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-violet-500/10 text-violet-300 font-mono text-xs font-bold">{{ m.factor }}</span>
            <div class="min-w-0 flex-1">
              <p class="text-sm font-medium text-foreground">{{ m.label }}</p>
              <p class="truncate text-xs text-muted-foreground">{{ m.desc }}</p>
            </div>
            <AdminPill :tone="m.enabled ? 'green' : 'neutral'" dot>{{ m.enabled ? 'Đang chạy' : 'Tạm tắt' }}</AdminPill>
            <button
              type="button"
              class="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors"
              :class="m.enabled ? 'bg-violet-500/70 shadow-[0_0_10px_oklch(0.62_0.2_290/45%)]' : 'bg-white/[0.12]'"
              @click="toggleMultiplier(m)"
            >
              <span class="inline-block h-5 w-5 transform rounded-full bg-white transition-transform" :class="m.enabled ? 'translate-x-[22px]' : 'translate-x-0.5'" />
            </button>
          </li>
        </ul>
      </AdminPanel>
    </div>
  </div>
</template>
