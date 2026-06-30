<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import Avatar from '@/components/app/Avatar.vue';
import Tabs from '@/components/app/Tabs.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Status = 'online' | 'studying' | 'offline';

interface Friend {
  id: number;
  name: string;
  level: number;
  status: Status;
  streak: number;
  activity: string;
  accent: Accent;
  coins: number;
  xp: number;
  weeklyMinutes: number;
}

interface FriendRequest {
  id: number;
  name: string;
  level: number;
  mutual: number;
  note: string;
  accent: Accent;
}

interface Suggestion {
  id: number;
  name: string;
  level: number;
  reason: string;
  mutual: number;
  accent: Accent;
  sent: boolean;
}

// ─── Status config ────────────────────────────────────────────────────────────

const statusMeta: Record<Status, { label: string; dot: string; text: string }> = {
  online: { label: 'Trực tuyến', dot: 'bg-emerald-400 pulse-glow-green', text: 'text-emerald-300' },
  studying: { label: 'Đang học', dot: 'bg-cyan-400 pulse-glow-cyan', text: 'text-cyan-300' },
  offline: { label: 'Ngoại tuyến', dot: 'bg-white/30', text: 'text-muted-foreground' }
};

// ─── State ────────────────────────────────────────────────────────────────────

const search = ref('');
const activeTab = ref<'friends' | 'requests' | 'suggestions'>('friends');

const friends = ref<Friend[]>([
  { id: 1, name: 'Minh Anh', level: 14, status: 'studying', streak: 28, activity: 'Đang ôn "Giải tích 1"', accent: 'cyan', coins: 3120, xp: 8420, weeklyMinutes: 420 },
  { id: 2, name: 'Hoàng Long', level: 12, status: 'online', streak: 19, activity: 'Vừa hoàn thành quiz Hoá học', accent: 'violet', coins: 2870, xp: 7110, weeklyMinutes: 360 },
  { id: 3, name: 'Thu Hà', level: 10, status: 'online', streak: 11, activity: 'Đang ở trong nhóm "Tiếng Anh B2"', accent: 'green', coins: 1980, xp: 5240, weeklyMinutes: 300 },
  { id: 4, name: 'Quốc Bảo', level: 9, status: 'offline', streak: 7, activity: 'Hoạt động 2 giờ trước', accent: 'magenta', coins: 1640, xp: 4180, weeklyMinutes: 245 },
  { id: 5, name: 'Lan Phương', level: 13, status: 'studying', streak: 22, activity: 'Đang đọc "Lịch sử Việt Nam"', accent: 'amber', coins: 2510, xp: 6890, weeklyMinutes: 390 },
  { id: 6, name: 'Tuấn Kiệt', level: 8, status: 'offline', streak: 4, activity: 'Hoạt động hôm qua', accent: 'cyan', coins: 1220, xp: 3360, weeklyMinutes: 180 },
  { id: 7, name: 'Khánh Vy', level: 11, status: 'online', streak: 15, activity: 'Vừa mở khoá thành tựu "Chăm chỉ"', accent: 'violet', coins: 2090, xp: 5670, weeklyMinutes: 320 },
  { id: 8, name: 'Đức Huy', level: 7, status: 'offline', streak: 9, activity: 'Hoạt động 5 giờ trước', accent: 'green', coins: 980, xp: 2740, weeklyMinutes: 150 }
]);

const requests = ref<FriendRequest[]>([
  { id: 101, name: 'Ngọc Mai', level: 9, mutual: 4, note: 'Cùng nhóm "Ôn thi THPT"', accent: 'magenta' },
  { id: 102, name: 'Gia Hân', level: 6, mutual: 2, note: 'Học chung môn Ngoại ngữ', accent: 'cyan' },
  { id: 103, name: 'Bá Lộc', level: 12, mutual: 7, note: 'Top 10 bảng xếp hạng tuần', accent: 'amber' }
]);

const suggestions = ref<Suggestion[]>([
  { id: 201, name: 'Phương Linh', level: 10, reason: 'Cùng quan tâm Lập trình', mutual: 5, accent: 'violet', sent: false },
  { id: 202, name: 'Hữu Thắng', level: 8, reason: 'Hoạt động gần khu vực bạn', mutual: 3, accent: 'green', sent: false },
  { id: 203, name: 'Mỹ Duyên', level: 13, reason: 'Chuỗi học 30 ngày như bạn', mutual: 6, accent: 'amber', sent: false },
  { id: 204, name: 'Trọng Nghĩa', level: 9, reason: 'Học chung môn Toán', mutual: 2, accent: 'cyan', sent: false }
]);

const compareId = ref<number | null>(null);

// "Bạn" — the current user baseline for comparison
const me = {
  name: 'Bạn',
  level: 7,
  streak: 12,
  coins: 1250,
  xp: 1240,
  weeklyMinutes: 310
};

// ─── Derived ──────────────────────────────────────────────────────────────────

const onlineFriends = computed<Friend[]>(() =>
  friends.value.filter((f) => f.status !== 'offline')
);

const filteredFriends = computed<Friend[]>(() => {
  const q = search.value.trim().toLowerCase();
  if (!q) return friends.value;
  return friends.value.filter((f) => f.name.toLowerCase().includes(q));
});

const tabs = computed(() => [
  { id: 'friends', label: 'Bạn bè', badge: friends.value.length },
  { id: 'requests', label: 'Lời mời', badge: requests.value.length },
  { id: 'suggestions', label: 'Gợi ý', badge: suggestions.value.length }
]);

const leaderboard = computed(() =>
  [...friends.value]
    .sort((a, b) => b.coins - a.coins)
    .slice(0, 5)
    .map((f, idx) => ({ ...f, rank: idx + 1 }))
);

const compareFriend = computed<Friend | null>(() => {
  if (compareId.value === null) return null;
  return friends.value.find((f) => f.id === compareId.value) ?? null;
});

const rankColor: Record<number, string> = {
  1: 'bg-amber-400/20 text-amber-300',
  2: 'bg-slate-300/15 text-slate-200',
  3: 'bg-orange-400/20 text-orange-300'
};

// ─── Handlers ─────────────────────────────────────────────────────────────────

function acceptRequest(req: FriendRequest): void {
  requests.value = requests.value.filter((r) => r.id !== req.id);
  friends.value.unshift({
    id: req.id,
    name: req.name,
    level: req.level,
    status: 'online',
    streak: 1,
    activity: 'Vừa trở thành bạn học',
    accent: req.accent,
    coins: 0,
    xp: req.level * 200,
    weeklyMinutes: 0
  });
}

function declineRequest(id: number): void {
  requests.value = requests.value.filter((r) => r.id !== id);
}

function sendRequest(id: number): void {
  const s = suggestions.value.find((item) => item.id === id);
  if (s) s.sent = true;
}

function openCompare(id: number): void {
  compareId.value = id;
}

function closeCompare(): void {
  compareId.value = null;
}

function metric(mine: number, theirs: number): { pctMe: number; pctThem: number } {
  const max = Math.max(mine, theirs, 1);
  return { pctMe: Math.round((mine / max) * 100), pctThem: Math.round((theirs / max) * 100) };
}

onUnmounted(() => {
  compareId.value = null;
});
</script>

<template>
  <div class="space-y-6">
    <!-- ── Hero ─────────────────────────────────────────────────────────────── -->
    <GlassPanel glow="cyan" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Cộng đồng học tập</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Bạn học</span> của bạn
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Kết nối, thi đua và cùng nhau giữ chuỗi học. Theo dõi ai đang trực tuyến và so sánh tiến độ với bạn bè.
          </p>
        </div>

        <!-- Search -->
        <div class="relative w-full max-w-xs shrink-0">
          <AppIcon name="search" class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <input
            v-model="search"
            type="text"
            placeholder="Tìm bạn học theo tên…"
            class="cosmic-input pl-9"
            aria-label="Tìm bạn học"
          />
        </div>
      </div>

      <!-- Online-now strip -->
      <div class="mt-5 rounded-sm border border-white/[0.06] bg-white/[0.02] p-3">
        <div class="mb-2.5 flex items-center gap-2 px-1 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          <span class="h-2 w-2 rounded-sm bg-emerald-400 pulse-glow-green" />
          Đang hoạt động · {{ onlineFriends.length }}
        </div>
        <div class="cosmic-scroll flex gap-3 overflow-x-auto pb-1">
          <button
            v-for="f in onlineFriends"
            :key="f.id"
            type="button"
            class="group flex w-20 shrink-0 flex-col items-center gap-1.5 rounded-sm p-1.5 text-center transition-colors hover:bg-white/[0.04]"
            @click="openCompare(f.id)"
          >
            <span class="relative">
              <Avatar :name="f.name" :accent="f.accent" :size="48" />
              <span
                class="absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-sm ring-2 ring-[#0c0c16]"
                :class="statusMeta[f.status].dot"
              />
            </span>
            <span class="line-clamp-1 w-full text-[0.7rem] font-medium text-foreground group-hover:text-cyan-200">{{ f.name }}</span>
          </button>
        </div>
      </div>
    </GlassPanel>

    <!-- ── KPI row ───────────────────────────────────────────────────────────── -->
    <section class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <StatPanel label="Bạn bè" :value="String(friends.length)" icon="users" accent="cyan" caption="Trong danh sách của bạn" />
      <StatPanel label="Đang online" :value="String(onlineFriends.length)" icon="zap" accent="green" :trend="12" caption="Sẵn sàng học cùng" />
      <StatPanel label="Lời mời" :value="String(requests.length)" icon="mail" accent="magenta" caption="Đang chờ phản hồi" />
      <StatPanel label="Gợi ý" :value="String(suggestions.length)" icon="sparkles" accent="violet" caption="Có thể bạn quen" />
    </section>

    <!-- ── Tabs ──────────────────────────────────────────────────────────────── -->
    <Tabs :tabs="tabs" :model-value="activeTab" @update:model-value="activeTab = $event as typeof activeTab" />

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Left column -->
      <div class="space-y-6 lg:col-span-2">
        <!-- Friends tab -->
        <SectionPanel
          v-if="activeTab === 'friends'"
          title="Danh sách bạn bè"
          :subtitle="`${filteredFriends.length} người`"
          icon="users"
          accent="cyan"
          flush
        >
          <ul v-if="filteredFriends.length > 0" class="divide-y divide-white/[0.05]">
            <li
              v-for="f in filteredFriends"
              :key="f.id"
              class="flex items-center gap-3 px-5 py-3 transition-colors hover:bg-white/[0.03]"
            >
              <span class="relative">
                <Avatar :name="f.name" :accent="f.accent" :size="44" />
                <span
                  class="absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-sm ring-2 ring-[#0c0c16]"
                  :class="statusMeta[f.status].dot"
                />
              </span>
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <p class="truncate text-sm font-semibold text-foreground">{{ f.name }}</p>
                  <span class="inline-flex shrink-0 items-center gap-1 rounded-sm bg-white/[0.06] px-1.5 py-0.5 text-[0.65rem] font-semibold text-muted-foreground">
                    <AppIcon name="star" class="h-3 w-3 text-amber-300" /> Cấp {{ f.level }}
                  </span>
                </div>
                <p class="mt-0.5 flex items-center gap-1.5 truncate text-xs" :class="statusMeta[f.status].text">
                  {{ statusMeta[f.status].label }}
                  <span class="text-muted-foreground/40">·</span>
                  <span class="truncate text-muted-foreground">{{ f.activity }}</span>
                </p>
              </div>
              <div class="hidden shrink-0 items-center gap-1 rounded-sm border border-orange-400/20 bg-orange-500/10 px-2 py-1 text-xs font-semibold text-orange-300 sm:flex">
                <AppIcon name="flame" class="h-3.5 w-3.5" /> {{ f.streak }}
              </div>
              <button
                type="button"
                class="inline-flex shrink-0 items-center gap-1.5 rounded-sm border border-white/10 bg-white/[0.03] px-2.5 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
                @click="openCompare(f.id)"
              >
                <AppIcon name="chart" class="h-3.5 w-3.5" />
                <span class="hidden md:inline">So sánh tiến độ</span>
              </button>
            </li>
          </ul>
          <div v-else class="flex flex-col items-center justify-center py-12 text-center">
            <AppIcon name="search" class="h-7 w-7 text-muted-foreground/50" />
            <p class="mt-3 text-sm text-muted-foreground">Không tìm thấy bạn học nào khớp với "{{ search }}".</p>
          </div>
        </SectionPanel>

        <!-- Requests tab -->
        <SectionPanel
          v-else-if="activeTab === 'requests'"
          title="Lời mời kết bạn"
          :subtitle="`${requests.length} lời mời đang chờ`"
          icon="mail"
          accent="magenta"
          flush
        >
          <TransitionGroup
            v-if="requests.length > 0"
            tag="ul"
            name="row"
            class="divide-y divide-white/[0.05]"
          >
            <li
              v-for="req in requests"
              :key="req.id"
              class="flex items-center gap-3 px-5 py-3.5"
            >
              <Avatar :name="req.name" :accent="req.accent" :size="44" />
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <p class="truncate text-sm font-semibold text-foreground">{{ req.name }}</p>
                  <span class="inline-flex shrink-0 items-center gap-1 rounded-sm bg-white/[0.06] px-1.5 py-0.5 text-[0.65rem] font-semibold text-muted-foreground">
                    Cấp {{ req.level }}
                  </span>
                </div>
                <p class="mt-0.5 truncate text-xs text-muted-foreground">
                  {{ req.note }} · {{ req.mutual }} bạn chung
                </p>
              </div>
              <button
                type="button"
                class="inline-flex shrink-0 items-center gap-1.5 rounded-sm bg-emerald-500/15 px-3 py-1.5 text-xs font-semibold text-emerald-200 transition-colors hover:bg-emerald-500/25"
                @click="acceptRequest(req)"
              >
                <AppIcon name="check" class="h-3.5 w-3.5" /> Chấp nhận
              </button>
              <button
                type="button"
                class="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-sm border border-white/10 bg-white/[0.03] text-muted-foreground transition-colors hover:border-rose-400/40 hover:text-rose-300"
                aria-label="Từ chối lời mời"
                @click="declineRequest(req.id)"
              >
                <AppIcon name="x" class="h-4 w-4" />
              </button>
            </li>
          </TransitionGroup>
          <div v-else class="flex flex-col items-center justify-center py-12 text-center">
            <span class="flex h-14 w-14 items-center justify-center rounded-sm bg-white/[0.04] text-muted-foreground">
              <AppIcon name="inbox" class="h-7 w-7" />
            </span>
            <p class="mt-3 text-sm font-medium text-foreground">Không có lời mời nào</p>
            <p class="mt-1 max-w-xs text-xs text-muted-foreground">Bạn đã xử lý hết lời mời kết bạn. Hãy xem mục Gợi ý để tìm bạn mới.</p>
          </div>
        </SectionPanel>

        <!-- Suggestions tab -->
        <SectionPanel
          v-else
          title="Gợi ý kết bạn"
          subtitle="Dựa trên sở thích & hoạt động học tập"
          icon="sparkles"
          accent="violet"
        >
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <article
              v-for="s in suggestions"
              :key="s.id"
              class="rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:border-white/[0.12]"
            >
              <div class="flex items-center gap-3">
                <Avatar :name="s.name" :accent="s.accent" :size="48" />
                <div class="min-w-0">
                  <p class="truncate text-sm font-semibold text-foreground">{{ s.name }}</p>
                  <p class="text-xs text-muted-foreground">Cấp {{ s.level }} · {{ s.mutual }} bạn chung</p>
                </div>
              </div>
              <p class="mt-3 flex items-center gap-1.5 text-xs text-muted-foreground">
                <AppIcon name="sparkles" class="h-3.5 w-3.5 text-violet-300" /> {{ s.reason }}
              </p>
              <button
                type="button"
                class="mt-3 inline-flex w-full items-center justify-center gap-1.5 rounded-sm px-3 py-2 text-sm font-medium transition-colors"
                :class="
                  s.sent
                    ? 'cursor-default border border-emerald-400/30 bg-emerald-500/10 text-emerald-300'
                    : 'cosmic-btn'
                "
                :disabled="s.sent"
                @click="sendRequest(s.id)"
              >
                <AppIcon :name="s.sent ? 'check' : 'plus'" class="h-4 w-4" />
                {{ s.sent ? 'Đã gửi' : 'Kết bạn' }}
              </button>
            </article>
          </div>
        </SectionPanel>
      </div>

      <!-- Right column -->
      <div class="space-y-6">
        <SectionPanel title="BXH bạn bè" subtitle="Theo số coin" icon="trophy" accent="amber" flush>
          <ul class="divide-y divide-white/[0.05]">
            <li
              v-for="user in leaderboard"
              :key="user.id"
              class="flex items-center gap-3 px-5 py-2.5"
            >
              <span
                class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm text-xs font-bold"
                :class="rankColor[user.rank] ?? 'bg-white/[0.06] text-muted-foreground'"
              >
                {{ user.rank }}
              </span>
              <Avatar :name="user.name" :accent="user.accent" :size="32" />
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ user.name }}</p>
                <p class="text-[0.7rem] text-muted-foreground">{{ user.xp.toLocaleString('vi-VN') }} XP</p>
              </div>
              <span class="inline-flex shrink-0 items-center gap-1 text-sm font-semibold text-amber-300">
                <AppIcon name="coin" class="h-3.5 w-3.5" /> {{ user.coins.toLocaleString('vi-VN') }}
              </span>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Mẹo kết nối" icon="lightbulb" accent="green">
          <ul class="space-y-3">
            <li class="flex gap-3">
              <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm bg-cyan-500/15 text-cyan-300">
                <AppIcon name="users" class="h-4 w-4" />
              </span>
              <p class="text-sm leading-snug text-muted-foreground">Tham gia <span class="font-medium text-foreground">nhóm học</span> để gặp bạn mới cùng mục tiêu.</p>
            </li>
            <li class="flex gap-3">
              <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm bg-amber-500/15 text-amber-300">
                <AppIcon name="flame" class="h-4 w-4" />
              </span>
              <p class="text-sm leading-snug text-muted-foreground">Giữ <span class="font-medium text-foreground">chuỗi học</span> để leo lên BXH bạn bè.</p>
            </li>
            <li class="flex gap-3">
              <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm bg-violet-500/15 text-violet-300">
                <AppIcon name="chart" class="h-4 w-4" />
              </span>
              <p class="text-sm leading-snug text-muted-foreground"><span class="font-medium text-foreground">So sánh tiến độ</span> để cùng nhau tiến bộ mỗi tuần.</p>
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <!-- ── Compare modal ─────────────────────────────────────────────────────── -->
    <Transition name="modal">
      <div v-if="compareFriend" class="fixed inset-0 z-40 flex items-center justify-center p-4 !mt-0">
        <button
          type="button"
          class="absolute inset-0 bg-black/60 backdrop-blur-sm"
          aria-label="Đóng cửa sổ so sánh"
          @click="closeCompare"
        />
        <div class="modal-panel glass-panel relative z-10 w-full max-w-lg overflow-hidden rounded-sm">
          <header class="flex items-center justify-between gap-3 border-b border-white/[0.06] px-5 py-4">
            <div class="flex items-center gap-2.5">
              <span class="flex h-9 w-9 items-center justify-center rounded-sm bg-violet-500/10 text-violet-300">
                <AppIcon name="chart" class="h-5 w-5" />
              </span>
              <p class="text-sm font-semibold text-foreground">So sánh tiến độ</p>
            </div>
            <button
              type="button"
              class="flex h-8 w-8 items-center justify-center rounded-sm text-muted-foreground hover:bg-white/[0.06] hover:text-foreground"
              aria-label="Đóng"
              @click="closeCompare"
            >
              <AppIcon name="x" class="h-5 w-5" />
            </button>
          </header>

          <div class="p-5">
            <!-- Heads -->
            <div class="mb-5 flex items-center justify-between gap-4">
              <div class="flex flex-col items-center gap-2 text-center">
                <Avatar name="Bạn" accent="cyan" :size="52" />
                <p class="text-sm font-semibold text-foreground">{{ me.name }}</p>
                <span class="text-[0.7rem] text-muted-foreground">Cấp {{ me.level }}</span>
              </div>
              <span class="text-xs font-bold uppercase tracking-widest text-muted-foreground/60">VS</span>
              <div class="flex flex-col items-center gap-2 text-center">
                <Avatar :name="compareFriend.name" :accent="compareFriend.accent" :size="52" />
                <p class="text-sm font-semibold text-foreground">{{ compareFriend.name }}</p>
                <span class="text-[0.7rem] text-muted-foreground">Cấp {{ compareFriend.level }}</span>
              </div>
            </div>

            <!-- Metric bars -->
            <div class="space-y-4">
              <div>
                <div class="mb-1 flex items-center justify-between text-xs">
                  <span class="font-medium text-cyan-200">{{ me.streak }} ngày</span>
                  <span class="text-muted-foreground">Chuỗi học</span>
                  <span class="font-medium text-violet-200">{{ compareFriend.streak }} ngày</span>
                </div>
                <div class="flex items-center gap-2">
                  <div class="flex flex-1 justify-end">
                    <div class="w-full overflow-hidden rounded-sm bg-white/[0.06]">
                      <ProgressBar :value="metric(me.streak, compareFriend.streak).pctMe" accent="cyan" :height="8" />
                    </div>
                  </div>
                  <div class="flex-1">
                    <ProgressBar :value="metric(me.streak, compareFriend.streak).pctThem" accent="violet" :height="8" />
                  </div>
                </div>
              </div>

              <div>
                <div class="mb-1 flex items-center justify-between text-xs">
                  <span class="font-medium text-cyan-200">{{ me.coins.toLocaleString('vi-VN') }}</span>
                  <span class="text-muted-foreground">Coin</span>
                  <span class="font-medium text-violet-200">{{ compareFriend.coins.toLocaleString('vi-VN') }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <div class="flex-1"><ProgressBar :value="metric(me.coins, compareFriend.coins).pctMe" accent="cyan" :height="8" /></div>
                  <div class="flex-1"><ProgressBar :value="metric(me.coins, compareFriend.coins).pctThem" accent="violet" :height="8" /></div>
                </div>
              </div>

              <div>
                <div class="mb-1 flex items-center justify-between text-xs">
                  <span class="font-medium text-cyan-200">{{ me.weeklyMinutes }} phút</span>
                  <span class="text-muted-foreground">Học tuần này</span>
                  <span class="font-medium text-violet-200">{{ compareFriend.weeklyMinutes }} phút</span>
                </div>
                <div class="flex items-center gap-2">
                  <div class="flex-1"><ProgressBar :value="metric(me.weeklyMinutes, compareFriend.weeklyMinutes).pctMe" accent="cyan" :height="8" /></div>
                  <div class="flex-1"><ProgressBar :value="metric(me.weeklyMinutes, compareFriend.weeklyMinutes).pctThem" accent="violet" :height="8" /></div>
                </div>
              </div>
            </div>

            <p class="mt-5 rounded-sm border border-white/[0.06] bg-white/[0.02] px-4 py-3 text-center text-xs text-muted-foreground">
              <AppIcon name="sparkles" class="mr-1 inline h-3.5 w-3.5 text-amber-300" />
              {{ compareFriend.weeklyMinutes > me.weeklyMinutes
                ? `${compareFriend.name} đang học chăm hơn bạn ${compareFriend.weeklyMinutes - me.weeklyMinutes} phút tuần này — cố lên nhé!`
                : `Bạn đang dẫn trước ${compareFriend.name} ${me.weeklyMinutes - compareFriend.weeklyMinutes} phút tuần này. Giữ vững phong độ!` }}
            </p>
          </div>
        </div>
      </div>
    </Transition>

    <p class="pb-2 text-center text-xs text-muted-foreground/60">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
/* Accept / decline row transition */
.row-enter-active,
.row-leave-active {
  transition: all 0.32s cubic-bezier(0.22, 1, 0.36, 1);
}
.row-enter-from {
  opacity: 0;
  transform: translateX(-16px);
}
.row-leave-to {
  opacity: 0;
  transform: translateX(28px);
}
.row-leave-active {
  position: absolute;
  width: 100%;
}
.row-move {
  transition: transform 0.32s cubic-bezier(0.22, 1, 0.36, 1);
}

/* Modal scale-in */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.22s ease;
}
.modal-enter-active .modal-panel,
.modal-leave-active .modal-panel {
  transition: transform 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal-panel,
.modal-leave-to .modal-panel {
  transform: scale(0.92);
}
</style>
