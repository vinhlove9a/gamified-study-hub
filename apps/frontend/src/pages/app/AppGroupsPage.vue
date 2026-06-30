<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import Avatar from '@/components/app/Avatar.vue';
import Tabs from '@/components/app/Tabs.vue';
import Toggle from '@/components/app/Toggle.vue';

// ─── Types ──────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Member {
  name: string;
  accent: Accent;
  online: boolean;
  xp: number;
}

interface ChatMessage {
  author: string;
  accent: Accent;
  text: string;
  time: string;
}

interface GroupDoc {
  title: string;
  meta: string;
}

interface Group {
  id: number;
  name: string;
  subject: string;
  description: string;
  accent: Accent;
  privacy: 'public' | 'private';
  activity: 'cao' | 'trung bình' | 'thấp';
  membership: 'mine' | 'discover' | 'invited';
  joined: boolean;
  members: Member[];
  chat: ChatMessage[];
  docs: GroupDoc[];
}

// ─── Static config ────────────────────────────────────────────────────────────

const cover: Record<Accent, string> = {
  cyan: 'from-cyan-500/50 via-blue-600/30 to-transparent',
  magenta: 'from-fuchsia-500/50 via-purple-600/30 to-transparent',
  green: 'from-emerald-500/50 via-teal-600/30 to-transparent',
  violet: 'from-violet-500/50 via-indigo-600/30 to-transparent',
  amber: 'from-amber-400/50 via-orange-600/30 to-transparent'
};

const iconChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/20 text-cyan-100',
  magenta: 'bg-fuchsia-500/20 text-fuchsia-100',
  green: 'bg-emerald-500/20 text-emerald-100',
  violet: 'bg-violet-500/20 text-violet-100',
  amber: 'bg-amber-500/20 text-amber-100'
};

const activityTone: Record<Group['activity'], string> = {
  cao: 'bg-emerald-500/15 text-emerald-300',
  'trung bình': 'bg-amber-500/15 text-amber-300',
  thấp: 'bg-white/[0.06] text-muted-foreground'
};

const accentChoices: Accent[] = ['cyan', 'violet', 'green', 'amber', 'magenta'];
const accentDot: Record<Accent, string> = {
  cyan: 'bg-cyan-400',
  magenta: 'bg-fuchsia-400',
  green: 'bg-emerald-400',
  violet: 'bg-violet-400',
  amber: 'bg-amber-300'
};

// ─── State ────────────────────────────────────────────────────────────────────

const groups = ref<Group[]>([
  {
    id: 1,
    name: 'CLB Toán học tinh hoa',
    subject: 'Toán học',
    description: 'Cùng nhau chinh phục các bài toán khó và ôn thi học sinh giỏi.',
    accent: 'cyan',
    privacy: 'public',
    activity: 'cao',
    membership: 'mine',
    joined: true,
    members: [
      { name: 'Minh Anh', accent: 'cyan', online: true, xp: 8420 },
      { name: 'Hoàng Long', accent: 'violet', online: true, xp: 7110 },
      { name: 'Thu Hà', accent: 'green', online: false, xp: 5240 },
      { name: 'Quốc Bảo', accent: 'magenta', online: true, xp: 4180 },
      { name: 'Lan Phương', accent: 'amber', online: false, xp: 6890 }
    ],
    chat: [
      { author: 'Minh Anh', accent: 'cyan', text: 'Mọi người làm bài 5 chương Tích phân chưa nhỉ?', time: '09:12' },
      { author: 'Hoàng Long', accent: 'violet', text: 'Mình xong rồi, để mình up lời giải nhé!', time: '09:15' },
      { author: 'Quốc Bảo', accent: 'magenta', text: 'Tối nay 8h họp nhóm online không mọi người?', time: '09:20' }
    ],
    docs: [
      { title: 'Tổng ôn Tích phân', meta: 'PDF · 32 trang' },
      { title: 'Bộ đề HSG 2026', meta: 'PDF · 18 trang' },
      { title: 'Công thức lượng giác', meta: 'Slide · 12 trang' }
    ]
  },
  {
    id: 2,
    name: 'English Speaking Club',
    subject: 'Ngoại ngữ',
    description: 'Luyện nói tiếng Anh mỗi ngày, chủ đề mới mỗi tuần.',
    accent: 'green',
    privacy: 'public',
    activity: 'cao',
    membership: 'mine',
    joined: true,
    members: [
      { name: 'Khánh Vy', accent: 'violet', online: true, xp: 5670 },
      { name: 'Thu Hà', accent: 'green', online: true, xp: 5240 },
      { name: 'Đức Huy', accent: 'cyan', online: false, xp: 2740 }
    ],
    chat: [
      { author: 'Khánh Vy', accent: 'violet', text: 'Topic tuần này: Travel & Culture nhé cả nhà!', time: '14:02' },
      { author: 'Đức Huy', accent: 'cyan', text: 'Great, I will prepare some vocabulary.', time: '14:08' }
    ],
    docs: [
      { title: 'IELTS Speaking Part 2', meta: 'PDF · 24 trang' },
      { title: 'Common Idioms', meta: 'Slide · 16 trang' }
    ]
  },
  {
    id: 3,
    name: 'Lập trình Web 2026',
    subject: 'Lập trình',
    description: 'Học Vue, TypeScript và xây dựng dự án thực tế cùng nhau.',
    accent: 'violet',
    privacy: 'private',
    activity: 'trung bình',
    membership: 'discover',
    joined: false,
    members: [
      { name: 'Bá Lộc', accent: 'amber', online: true, xp: 6300 },
      { name: 'Phương Linh', accent: 'violet', online: false, xp: 4900 },
      { name: 'Hữu Thắng', accent: 'green', online: true, xp: 3800 },
      { name: 'Trọng Nghĩa', accent: 'cyan', online: false, xp: 3100 }
    ],
    chat: [
      { author: 'Bá Lộc', accent: 'amber', text: 'Sprint này mình làm trang dashboard nha.', time: '20:30' },
      { author: 'Hữu Thắng', accent: 'green', text: 'Ok mình nhận phần biểu đồ.', time: '20:34' }
    ],
    docs: [
      { title: 'Vue 3 Composition API', meta: 'PDF · 56 trang' },
      { title: 'Clean Code TypeScript', meta: 'PDF · 40 trang' }
    ]
  },
  {
    id: 4,
    name: 'Hội mê Khoa học',
    subject: 'Khoa học',
    description: 'Khám phá Vật lý, Hoá học và các hiện tượng thú vị quanh ta.',
    accent: 'amber',
    privacy: 'public',
    activity: 'thấp',
    membership: 'discover',
    joined: false,
    members: [
      { name: 'Mỹ Duyên', accent: 'amber', online: false, xp: 5500 },
      { name: 'Tuấn Kiệt', accent: 'cyan', online: true, xp: 3360 }
    ],
    chat: [
      { author: 'Mỹ Duyên', accent: 'amber', text: 'Tuần sau mình làm thí nghiệm ảo về điện từ nhé.', time: '11:00' }
    ],
    docs: [{ title: 'Điện từ học cơ bản', meta: 'Slide · 28 trang' }]
  },
  {
    id: 5,
    name: 'Nhóm ôn thi THPT QG',
    subject: 'Tổng hợp',
    description: 'Cùng nhau bứt phá giai đoạn nước rút, chia sẻ tài liệu & đề thi.',
    accent: 'magenta',
    privacy: 'private',
    activity: 'cao',
    membership: 'invited',
    joined: false,
    members: [
      { name: 'Ngọc Mai', accent: 'magenta', online: true, xp: 4600 },
      { name: 'Gia Hân', accent: 'cyan', online: true, xp: 2900 },
      { name: 'Lan Phương', accent: 'amber', online: false, xp: 6890 }
    ],
    chat: [
      { author: 'Ngọc Mai', accent: 'magenta', text: 'Mời bạn vào nhóm cùng ôn thi nhé!', time: '08:45' }
    ],
    docs: [{ title: 'Bộ đề minh hoạ 2026', meta: 'PDF · 64 trang' }]
  }
]);

const activeTab = ref<'mine' | 'discover' | 'invited'>('mine');
const detailId = ref<number | null>(null);
const showModal = ref(false);

// Modal form state
const formName = ref('');
const formDesc = ref('');
const formSubject = ref('');
const formPrivate = ref(false);
const formAccent = ref<Accent>('cyan');

let nextId = 6;

// ─── Derived ──────────────────────────────────────────────────────────────────

const tabs = computed(() => [
  { id: 'mine', label: 'Của tôi', badge: groups.value.filter((g) => g.membership === 'mine').length },
  { id: 'discover', label: 'Khám phá', badge: groups.value.filter((g) => g.membership === 'discover').length },
  { id: 'invited', label: 'Lời mời', badge: groups.value.filter((g) => g.membership === 'invited').length }
]);

const filteredGroups = computed<Group[]>(() =>
  groups.value.filter((g) => g.membership === activeTab.value)
);

const myGroupsCount = computed(() => groups.value.filter((g) => g.joined).length);
const totalMembers = computed(() => groups.value.reduce((sum, g) => sum + g.members.length, 0));
const onlineNow = computed(() =>
  groups.value.reduce((sum, g) => sum + g.members.filter((m) => m.online).length, 0)
);
const invitedCount = computed(() => groups.value.filter((g) => g.membership === 'invited').length);

const detailGroup = computed<Group | null>(() => {
  if (detailId.value === null) return null;
  return groups.value.find((g) => g.id === detailId.value) ?? null;
});

const detailLeaderboard = computed(() => {
  const g = detailGroup.value;
  if (!g) return [];
  return [...g.members].sort((a, b) => b.xp - a.xp).slice(0, 5);
});

const canCreate = computed(() => formName.value.trim().length > 0);

const rankColor: Record<number, string> = {
  0: 'bg-amber-400/20 text-amber-300',
  1: 'bg-slate-300/15 text-slate-200',
  2: 'bg-orange-400/20 text-orange-300'
};

// ─── Handlers ─────────────────────────────────────────────────────────────────

function openDetail(id: number): void {
  detailId.value = id;
}

function closeDetail(): void {
  detailId.value = null;
}

function toggleMembership(group: Group): void {
  group.joined = !group.joined;
  if (group.joined) {
    group.membership = 'mine';
    if (!group.members.some((m) => m.name === 'Bạn')) {
      group.members.push({ name: 'Bạn', accent: formAccent.value, online: true, xp: 1240 });
    }
  } else {
    group.membership = 'discover';
    group.members = group.members.filter((m) => m.name !== 'Bạn');
  }
}

function openModal(): void {
  formName.value = '';
  formDesc.value = '';
  formSubject.value = '';
  formPrivate.value = false;
  formAccent.value = 'cyan';
  showModal.value = true;
}

function closeModal(): void {
  showModal.value = false;
}

function createGroup(): void {
  if (!canCreate.value) return;
  groups.value.unshift({
    id: nextId++,
    name: formName.value.trim(),
    subject: formSubject.value.trim() || 'Tổng hợp',
    description: formDesc.value.trim() || 'Nhóm học mới do bạn tạo.',
    accent: formAccent.value,
    privacy: formPrivate.value ? 'private' : 'public',
    activity: 'thấp',
    membership: 'mine',
    joined: true,
    members: [{ name: 'Bạn', accent: formAccent.value, online: true, xp: 1240 }],
    chat: [],
    docs: []
  });
  activeTab.value = 'mine';
  showModal.value = false;
}

onUnmounted(() => {
  showModal.value = false;
  detailId.value = null;
});
</script>

<template>
  <div class="space-y-6">
    <!-- ── Hero ─────────────────────────────────────────────────────────────── -->
    <GlassPanel glow="magenta" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Học cùng nhau</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Nhóm học</span> tập
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Tham gia hoặc tạo nhóm để chia sẻ tài liệu, trò chuyện và thi đua cùng bạn bè có chung mục tiêu.
          </p>
        </div>
        <button
          type="button"
          class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-sm px-4 py-2 text-sm"
          @click="openModal"
        >
          <AppIcon name="plus" class="h-4 w-4" />
          Tạo nhóm
        </button>
      </div>
    </GlassPanel>

    <!-- ── KPI row ───────────────────────────────────────────────────────────── -->
    <section class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <StatPanel label="Nhóm của tôi" :value="String(myGroupsCount)" icon="users" accent="cyan" caption="Đang tham gia" />
      <StatPanel label="Tổng thành viên" :value="String(totalMembers)" icon="user" accent="violet" caption="Trên mọi nhóm" />
      <StatPanel label="Đang online" :value="String(onlineNow)" icon="zap" accent="green" :trend="9" caption="Bạn học đang hoạt động" />
      <StatPanel label="Lời mời nhóm" :value="String(invitedCount)" icon="mail" accent="amber" caption="Đang chờ tham gia" />
    </section>

    <!-- ── Tabs ──────────────────────────────────────────────────────────────── -->
    <Tabs :tabs="tabs" :model-value="activeTab" @update:model-value="activeTab = $event as typeof activeTab" />

    <!-- ── Groups grid ───────────────────────────────────────────────────────── -->
    <div v-if="filteredGroups.length > 0" class="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3">
      <article
        v-for="g in filteredGroups"
        :key="g.id"
        class="group flex cursor-pointer flex-col overflow-hidden rounded-sm border border-white/[0.06] bg-white/[0.02] transition-all hover:-translate-y-1 hover:border-white/[0.14]"
        @click="openDetail(g.id)"
      >
        <!-- Gradient cover -->
        <div class="relative h-24" :class="cover[g.accent]">
          <div
            class="absolute inset-0 opacity-50"
            style="background: radial-gradient(circle at 25% 20%, rgba(255,255,255,0.28), transparent 55%)"
          />
          <span class="absolute left-4 top-4 flex h-11 w-11 items-center justify-center rounded-sm backdrop-blur" :class="iconChip[g.accent]">
            <AppIcon name="users" class="h-6 w-6" />
          </span>
          <span
            class="absolute right-4 top-4 inline-flex items-center gap-1 rounded-sm bg-black/30 px-2 py-1 text-[0.68rem] font-medium text-white/90 backdrop-blur"
          >
            <AppIcon :name="g.privacy === 'public' ? 'globe' : 'lock'" class="h-3 w-3" />
            {{ g.privacy === 'public' ? 'Công khai' : 'Riêng tư' }}
          </span>
        </div>

        <!-- Body -->
        <div class="flex flex-1 flex-col p-4">
          <div class="flex items-start justify-between gap-2">
            <h3 class="line-clamp-1 text-base font-semibold text-foreground">{{ g.name }}</h3>
            <span class="shrink-0 rounded-sm px-2 py-0.5 text-[0.65rem] font-semibold capitalize" :class="activityTone[g.activity]">
              {{ g.activity }}
            </span>
          </div>
          <p class="mt-0.5 flex items-center gap-1.5 text-xs text-cyan-300">
            <AppIcon name="book-open" class="h-3.5 w-3.5" /> {{ g.subject }}
          </p>
          <p class="mt-2 line-clamp-2 text-xs leading-relaxed text-muted-foreground">{{ g.description }}</p>

          <!-- Footer: member stack -->
          <div class="mt-auto flex items-center justify-between gap-3 pt-4">
            <div class="flex items-center">
              <span
                v-for="(m, idx) in g.members.slice(0, 4)"
                :key="m.name"
                class="-ml-2 rounded-sm ring-2 ring-[#0c0c16] first:ml-0"
                :style="{ zIndex: String(4 - idx) }"
              >
                <Avatar :name="m.name" :accent="m.accent" :size="28" />
              </span>
              <span
                v-if="g.members.length > 4"
                class="-ml-2 flex h-7 w-7 items-center justify-center rounded-sm bg-white/10 text-[0.65rem] font-semibold text-foreground ring-2 ring-[#0c0c16]"
              >
                +{{ g.members.length - 4 }}
              </span>
            </div>
            <span class="flex items-center gap-1 text-xs text-muted-foreground">
              <AppIcon name="user" class="h-3.5 w-3.5" /> {{ g.members.length }} thành viên
            </span>
          </div>
        </div>
      </article>
    </div>

    <!-- ── Empty state ───────────────────────────────────────────────────────── -->
    <div
      v-else
      class="flex flex-col items-center justify-center rounded-sm border border-dashed border-white/[0.08] bg-white/[0.01] py-16 text-center"
    >
      <span class="flex h-16 w-16 items-center justify-center rounded-sm border border-white/[0.08] bg-white/[0.03] text-muted-foreground/60">
        <AppIcon name="users" class="h-8 w-8" />
      </span>
      <p class="mt-4 text-base font-semibold text-foreground">Chưa có nhóm nào ở đây</p>
      <p class="mt-1.5 max-w-xs text-sm text-muted-foreground">
        {{ activeTab === 'invited' ? 'Bạn chưa nhận được lời mời nhóm nào.' : 'Hãy khám phá hoặc tạo một nhóm học mới để bắt đầu.' }}
      </p>
      <button type="button" class="cosmic-btn mt-5 inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm" @click="openModal">
        <AppIcon name="plus" class="h-4 w-4" />
        Tạo nhóm
      </button>
    </div>

    <!-- ── Detail drawer ─────────────────────────────────────────────────────── -->
    <Transition name="drawer">
      <div v-if="detailGroup" class="fixed inset-0 z-40 flex justify-end !mt-0">
        <button
          type="button"
          class="absolute inset-0 bg-black/60 backdrop-blur-sm"
          aria-label="Đóng bảng chi tiết"
          @click="closeDetail"
        />
        <aside class="drawer-panel cosmic-scroll relative z-10 flex h-full w-full max-w-md flex-col overflow-y-auto border-l border-white/[0.08] bg-[#0c0c16]">
          <!-- Cover header -->
          <div class="relative h-28 shrink-0" :class="cover[detailGroup.accent]">
            <div class="absolute inset-0 opacity-50" style="background: radial-gradient(circle at 30% 20%, rgba(255,255,255,0.3), transparent 55%)" />
            <button
              type="button"
              class="absolute right-3 top-3 flex h-8 w-8 items-center justify-center rounded-sm bg-black/30 text-white/80 backdrop-blur hover:text-white"
              aria-label="Đóng"
              @click="closeDetail"
            >
              <AppIcon name="x" class="h-5 w-5" />
            </button>
            <div class="absolute -bottom-6 left-5 flex h-14 w-14 items-center justify-center rounded-sm ring-4 ring-[#0c0c16]" :class="iconChip[detailGroup.accent]">
              <AppIcon name="users" class="h-7 w-7" />
            </div>
          </div>

          <div class="space-y-5 px-5 pb-6 pt-9">
            <!-- Title block -->
            <div>
              <div class="flex items-center gap-2">
                <h2 class="text-lg font-bold text-foreground">{{ detailGroup.name }}</h2>
                <span class="inline-flex items-center gap-1 rounded-sm bg-white/[0.06] px-2 py-0.5 text-[0.65rem] font-medium text-muted-foreground">
                  <AppIcon :name="detailGroup.privacy === 'public' ? 'globe' : 'lock'" class="h-3 w-3" />
                  {{ detailGroup.privacy === 'public' ? 'Công khai' : 'Riêng tư' }}
                </span>
              </div>
              <p class="mt-0.5 flex items-center gap-1.5 text-xs text-cyan-300">
                <AppIcon name="book-open" class="h-3.5 w-3.5" /> {{ detailGroup.subject }} · {{ detailGroup.members.length }} thành viên
              </p>
              <p class="mt-2 text-sm leading-relaxed text-muted-foreground">{{ detailGroup.description }}</p>

              <button
                type="button"
                class="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-sm px-4 py-2.5 text-sm font-semibold transition-colors"
                :class="
                  detailGroup.joined
                    ? 'border border-rose-400/30 bg-rose-500/10 text-rose-300 hover:bg-rose-500/20'
                    : 'cosmic-btn'
                "
                @click="toggleMembership(detailGroup)"
              >
                <AppIcon :name="detailGroup.joined ? 'logout' : 'plus'" class="h-4 w-4" />
                {{ detailGroup.joined ? 'Rời nhóm' : 'Tham gia' }}
              </button>
            </div>

            <!-- Members -->
            <div>
              <p class="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                <AppIcon name="users" class="h-4 w-4" /> Thành viên
              </p>
              <ul class="space-y-1.5">
                <li
                  v-for="m in detailGroup.members"
                  :key="m.name"
                  class="flex items-center gap-3 rounded-sm px-2 py-1.5 transition-colors hover:bg-white/[0.03]"
                >
                  <span class="relative">
                    <Avatar :name="m.name" :accent="m.accent" :size="32" />
                    <span
                      class="absolute -bottom-0.5 -right-0.5 h-2.5 w-2.5 rounded-sm ring-2 ring-[#0c0c16]"
                      :class="m.online ? 'bg-emerald-400 pulse-glow-green' : 'bg-white/30'"
                    />
                  </span>
                  <span class="flex-1 truncate text-sm text-foreground">{{ m.name }}</span>
                  <span class="text-[0.7rem]" :class="m.online ? 'text-emerald-300' : 'text-muted-foreground'">
                    {{ m.online ? 'online' : 'offline' }}
                  </span>
                </li>
              </ul>
            </div>

            <!-- Group chat preview -->
            <div>
              <p class="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                <AppIcon name="message" class="h-4 w-4" /> Trò chuyện gần đây
              </p>
              <div v-if="detailGroup.chat.length > 0" class="space-y-2.5 rounded-sm border border-white/[0.06] bg-white/[0.02] p-3">
                <div v-for="(msg, i) in detailGroup.chat" :key="i" class="flex gap-2.5">
                  <Avatar :name="msg.author" :accent="msg.accent" :size="28" />
                  <div class="min-w-0 flex-1">
                    <p class="flex items-baseline gap-2">
                      <span class="text-xs font-semibold text-foreground">{{ msg.author }}</span>
                      <span class="text-[0.65rem] text-muted-foreground/70">{{ msg.time }}</span>
                    </p>
                    <p class="text-sm leading-snug text-muted-foreground">{{ msg.text }}</p>
                  </div>
                </div>
              </div>
              <p v-else class="rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-4 text-center text-xs text-muted-foreground">
                Chưa có tin nhắn nào trong nhóm.
              </p>
            </div>

            <!-- Shared docs -->
            <div>
              <p class="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                <AppIcon name="file-text" class="h-4 w-4" /> Tài liệu chung
              </p>
              <ul v-if="detailGroup.docs.length > 0" class="space-y-1.5">
                <li
                  v-for="doc in detailGroup.docs"
                  :key="doc.title"
                  class="flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-2 transition-colors hover:border-white/[0.12]"
                >
                  <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm bg-cyan-500/15 text-cyan-300">
                    <AppIcon name="file-text" class="h-4 w-4" />
                  </span>
                  <div class="min-w-0 flex-1">
                    <p class="truncate text-sm font-medium text-foreground">{{ doc.title }}</p>
                    <p class="text-[0.7rem] text-muted-foreground">{{ doc.meta }}</p>
                  </div>
                  <AppIcon name="download" class="h-4 w-4 text-muted-foreground" />
                </li>
              </ul>
              <p v-else class="rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-4 text-center text-xs text-muted-foreground">
                Nhóm chưa có tài liệu chung.
              </p>
            </div>

            <!-- Mini leaderboard -->
            <div>
              <p class="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                <AppIcon name="trophy" class="h-4 w-4" /> BXH trong nhóm
              </p>
              <ul class="space-y-1.5">
                <li
                  v-for="(m, idx) in detailLeaderboard"
                  :key="m.name"
                  class="flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-2"
                >
                  <span class="flex h-6 w-6 shrink-0 items-center justify-center rounded-sm text-[0.7rem] font-bold" :class="rankColor[idx] ?? 'bg-white/[0.06] text-muted-foreground'">
                    {{ idx + 1 }}
                  </span>
                  <Avatar :name="m.name" :accent="m.accent" :size="28" />
                  <span class="flex-1 truncate text-sm text-foreground">{{ m.name }}</span>
                  <span class="inline-flex items-center gap-1 text-xs font-semibold text-violet-300">
                    <AppIcon name="zap" class="h-3.5 w-3.5" /> {{ m.xp.toLocaleString('vi-VN') }}
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </aside>
      </div>
    </Transition>

    <!-- ── Create modal ──────────────────────────────────────────────────────── -->
    <Transition name="modal">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 !mt-0">
        <button
          type="button"
          class="absolute inset-0 bg-black/60 backdrop-blur-sm"
          aria-label="Đóng cửa sổ"
          @click="closeModal"
        />
        <div class="modal-panel glass-panel relative z-10 w-full max-w-md overflow-hidden rounded-sm">
          <header class="flex items-center justify-between gap-3 border-b border-white/[0.06] px-5 py-4">
            <div class="flex items-center gap-2.5">
              <span class="flex h-9 w-9 items-center justify-center rounded-sm bg-fuchsia-500/10 text-fuchsia-300">
                <AppIcon name="plus" class="h-5 w-5" />
              </span>
              <p class="text-sm font-semibold text-foreground">Tạo nhóm học mới</p>
            </div>
            <button
              type="button"
              class="flex h-8 w-8 items-center justify-center rounded-sm text-muted-foreground hover:bg-white/[0.06] hover:text-foreground"
              aria-label="Đóng"
              @click="closeModal"
            >
              <AppIcon name="x" class="h-5 w-5" />
            </button>
          </header>

          <div class="space-y-5 p-5">
            <div>
              <label class="mb-1.5 block text-xs font-medium text-muted-foreground">Tên nhóm</label>
              <input
                v-model="formName"
                type="text"
                placeholder="VD: CLB Toán học tinh hoa"
                class="cosmic-input"
              />
            </div>

            <div>
              <label class="mb-1.5 block text-xs font-medium text-muted-foreground">Chủ đề / Môn học</label>
              <input
                v-model="formSubject"
                type="text"
                placeholder="VD: Toán học"
                class="cosmic-input"
              />
            </div>

            <div>
              <label class="mb-1.5 block text-xs font-medium text-muted-foreground">Mô tả</label>
              <textarea
                v-model="formDesc"
                rows="3"
                placeholder="Mục tiêu và hoạt động của nhóm…"
                class="cosmic-input resize-none"
              />
            </div>

            <!-- Accent picker -->
            <div>
              <label class="mb-2 block text-xs font-medium text-muted-foreground">Màu nhấn</label>
              <div class="flex flex-wrap gap-2.5">
                <button
                  v-for="ac in accentChoices"
                  :key="ac"
                  type="button"
                  class="flex h-9 w-9 items-center justify-center rounded-sm border transition-all"
                  :class="formAccent === ac ? 'scale-110 border-white/40' : 'border-white/[0.08] hover:border-white/20'"
                  :aria-label="`Chọn màu ${ac}`"
                  @click="formAccent = ac"
                >
                  <span class="h-5 w-5 rounded-sm" :class="accentDot[ac]" />
                </button>
              </div>
            </div>

            <!-- Privacy toggle -->
            <div class="flex items-center justify-between rounded-sm border border-white/[0.06] bg-white/[0.02] px-4 py-3">
              <div class="flex items-center gap-2.5">
                <AppIcon :name="formPrivate ? 'lock' : 'globe'" class="h-4 w-4 text-muted-foreground" />
                <div>
                  <p class="text-sm font-medium text-foreground">{{ formPrivate ? 'Riêng tư' : 'Công khai' }}</p>
                  <p class="text-xs text-muted-foreground">
                    {{ formPrivate ? 'Chỉ thành viên được mời mới tham gia' : 'Mọi người có thể tìm và tham gia' }}
                  </p>
                </div>
              </div>
              <Toggle :model-value="formPrivate" @update:model-value="formPrivate = $event" />
            </div>
          </div>

          <footer class="flex items-center justify-end gap-2 border-t border-white/[0.06] px-5 py-4">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2 text-sm text-muted-foreground hover:text-foreground"
              @click="closeModal"
            >
              Huỷ
            </button>
            <button
              type="button"
              class="cosmic-btn inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm"
              :disabled="!canCreate"
              @click="createGroup"
            >
              <AppIcon name="check" class="h-4 w-4" />
              Tạo nhóm
            </button>
          </footer>
        </div>
      </div>
    </Transition>

    <p class="pb-2 text-center text-xs text-muted-foreground/60">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
/* Drawer slide-in */
.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.25s ease;
}
.drawer-enter-active .drawer-panel,
.drawer-leave-active .drawer-panel {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}
.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}
.drawer-enter-from .drawer-panel,
.drawer-leave-to .drawer-panel {
  transform: translateX(100%);
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
