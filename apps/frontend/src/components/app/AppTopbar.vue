<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import Avatar from '@/components/app/Avatar.vue';
import AppCommandPalette, { type PaletteAction } from '@/components/app/AppCommandPalette.vue';
import { navGroups } from '@/features/app-shell/navigation';
import { useAuthSession } from '@/features/auth/authSession';

interface Props {
  coins?: number;
  gems?: number;
  streak?: number;
  level?: number;
}

const props = withDefaults(defineProps<Props>(), {
  coins: 1250,
  gems: 48,
  streak: 12,
  level: 7
});

defineEmits<{ (e: 'open-sidebar'): void }>();

const router = useRouter();
const { currentUser, logout } = useAuthSession();

const displayName = computed(() => currentUser.value?.fullName?.trim() || currentUser.value?.email || 'Người học');
const email = computed(() => currentUser.value?.email ?? 'user@studyhub.vn');

type Menu = 'none' | 'notifications' | 'user' | 'create' | 'apps' | 'messages';
const openMenu = ref<Menu>('none');
const paletteOpen = ref(false);

function toggle(menu: Exclude<Menu, 'none'>) {
  openMenu.value = openMenu.value === menu ? 'none' : menu;
}
function closeMenus() {
  openMenu.value = 'none';
}
function goTo(path: string) {
  closeMenus();
  void router.push(path);
}

// ── Quick-create actions ────────────────────────────────────────────────────
const createActions = [
  { icon: 'upload', label: 'Tải tài liệu lên', desc: 'PDF, slide, ảnh…', to: '/app/documents' },
  { icon: 'folder', label: 'Bộ sưu tập mới', desc: 'Nhóm tài liệu theo chủ đề', to: '/app/collections' },
  { icon: 'edit', label: 'Ghi chú mới', desc: 'Ghi lại ý tưởng nhanh', to: '/app/notes' },
  { icon: 'layers', label: 'Bộ thẻ ghi nhớ', desc: 'Học theo lặp lại ngắt quãng', to: '/app/flashcards' },
  { icon: 'target', label: 'Đặt mục tiêu học', desc: 'Tạo nhiệm vụ cho riêng bạn', to: '/app/missions' },
  { icon: 'users', label: 'Tạo nhóm học', desc: 'Mời bạn bè cùng học', to: '/app/groups' }
] as const;

// ── Messages ─────────────────────────────────────────────────────────────────
const messages = [
  { id: 1, name: 'Minh Anh', text: 'Tài liệu Giải tích hay quá, cảm ơn cậu!', time: '2 phút', accent: 'cyan' as const, unread: true },
  { id: 2, name: 'Nhóm Ôn thi cuối kỳ', text: 'Quốc Bảo: 8h tối nay học chung nhé mọi người', time: '25 phút', accent: 'violet' as const, unread: true },
  { id: 3, name: 'Thu Hà', text: 'Mình gửi cậu bộ flashcard tiếng Anh nè', time: '1 giờ', accent: 'green' as const, unread: false }
];
const unreadMessages = computed(() => messages.filter((m) => m.unread).length);

// ── Notifications ─────────────────────────────────────────────────────────────
const notifications = [
  { id: 1, icon: 'coin', tone: 'amber', title: 'Bạn nhận được 50 coin', desc: 'Hoàn thành nhiệm vụ "Đọc 3 tài liệu"', time: '5 phút trước', unread: true },
  { id: 2, icon: 'flame', tone: 'orange', title: 'Chuỗi học 12 ngày!', desc: 'Tiếp tục để giữ chuỗi của bạn', time: '2 giờ trước', unread: true },
  { id: 3, icon: 'trophy', tone: 'violet', title: 'Lên hạng Bạc', desc: 'Bạn vừa thăng hạng trên bảng xếp hạng', time: 'Hôm qua', unread: false }
] as const;
const notifTone: Record<string, string> = {
  amber: 'bg-amber-500/15 text-amber-300',
  orange: 'bg-orange-500/15 text-orange-300',
  violet: 'bg-violet-500/15 text-violet-300'
};

const menuItems = [
  { icon: 'user', label: 'Hồ sơ của tôi', to: '/app/profile' },
  { icon: 'chart', label: 'Tiến độ học tập', to: '/app/stats' },
  { icon: 'coin', label: 'Ví của tôi', to: '/app/wallet' },
  { icon: 'award', label: 'Thành tựu', to: '/app/achievements' },
  { icon: 'settings', label: 'Cài đặt tài khoản', to: '/app/settings' }
];

// ── Focus mode ────────────────────────────────────────────────────────────────
const focusMode = ref(false);
function toggleFocus() {
  focusMode.value = !focusMode.value;
  document.body.classList.toggle('app-focus', focusMode.value);
}

// ── Fullscreen ────────────────────────────────────────────────────────────────
const isFullscreen = ref(false);
function syncFullscreen() {
  isFullscreen.value = Boolean(document.fullscreenElement);
}
function toggleFullscreen() {
  if (document.fullscreenElement) {
    void document.exitFullscreen?.();
  } else {
    void document.documentElement.requestFullscreen?.();
  }
}

// ── Logout ────────────────────────────────────────────────────────────────────
const loggingOut = ref(false);
async function handleLogout() {
  if (loggingOut.value) return;
  loggingOut.value = true;
  try {
    closeMenus();
    logout();
    await router.push('/auth/login');
  } finally {
    loggingOut.value = false;
  }
}

// ── Command-palette actions (non-navigation) ───────────────────────────────────
const paletteActions = computed<PaletteAction[]>(() => [
  { id: 'upload', label: 'Tải tài liệu lên', icon: 'upload', run: () => router.push('/app/documents') },
  { id: 'new-collection', label: 'Tạo bộ sưu tập mới', icon: 'folder', run: () => router.push('/app/collections') },
  { id: 'new-note', label: 'Tạo ghi chú mới', icon: 'edit', run: () => router.push('/app/notes') },
  { id: 'focus', label: focusMode.value ? 'Tắt chế độ tập trung' : 'Bật chế độ tập trung', icon: 'eye', run: toggleFocus },
  { id: 'fullscreen', label: isFullscreen.value ? 'Thoát toàn màn hình' : 'Toàn màn hình', icon: isFullscreen.value ? 'minimize' : 'maximize', run: toggleFullscreen },
  { id: 'logout', label: 'Đăng xuất', icon: 'logout', hint: '', run: handleLogout }
]);

onMounted(() => document.addEventListener('fullscreenchange', syncFullscreen));
onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', syncFullscreen);
  document.body.classList.remove('app-focus');
});
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-white/[0.06] bg-background/60 backdrop-blur-xl">
    <div class="flex h-16 items-center gap-2 px-3 sm:gap-3 sm:px-5">
      <!-- Mobile menu -->
      <button
        type="button"
        class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground lg:hidden"
        aria-label="Mở menu"
        @click="$emit('open-sidebar')"
      >
        <AppIcon name="menu" class="h-5 w-5" />
      </button>

      <!-- Command-palette launcher (replaces the old dead search box) -->
      <button
        type="button"
        class="relative hidden h-10 flex-1 items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] pl-3 pr-2 text-sm text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-foreground sm:flex md:max-w-md"
        @click="paletteOpen = true"
      >
        <AppIcon name="search" class="h-4 w-4" />
        <span class="flex-1 text-left">Tìm tài liệu, chức năng, thao tác…</span>
        <kbd class="hidden rounded-md border border-white/10 bg-white/[0.04] px-1.5 py-0.5 text-[0.65rem] font-medium md:block">⌘K</kbd>
      </button>

      <div class="flex-1 sm:hidden" />

      <!-- Right cluster -->
      <div class="flex shrink-0 items-center gap-1.5 sm:gap-2">
        <!-- Mobile search → palette -->
        <button
          type="button"
          class="flex h-10 w-10 items-center justify-center rounded-xl text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground sm:hidden"
          aria-label="Tìm kiếm"
          @click="paletteOpen = true"
        >
          <AppIcon name="search" class="h-5 w-5" />
        </button>

        <!-- Quick create -->
        <div class="relative">
          <button
            type="button"
            class="cosmic-btn hidden h-10 items-center gap-1.5 rounded-xl px-3.5 text-sm md:inline-flex"
            :class="openMenu === 'create' ? 'brightness-110' : ''"
            @click="toggle('create')"
          >
            <AppIcon name="plus" class="h-4 w-4" /> Tạo mới
            <AppIcon name="chevron-down" class="h-3.5 w-3.5 opacity-80" />
          </button>
          <button
            type="button"
            class="flex h-10 w-10 items-center justify-center rounded-xl text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground md:hidden"
            aria-label="Tạo mới"
            @click="toggle('create')"
          >
            <AppIcon name="plus" class="h-5 w-5" />
          </button>

          <div v-if="openMenu === 'create'" class="glass-panel absolute right-0 top-12 z-50 w-64 overflow-hidden rounded-2xl">
            <p class="border-b border-white/[0.06] px-4 py-2.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground/70">Tạo mới</p>
            <div class="py-1.5">
              <button
                v-for="a in createActions"
                :key="a.label"
                type="button"
                class="flex w-full items-center gap-3 px-3 py-2 text-left transition-colors hover:bg-white/[0.05]"
                @click="goTo(a.to)"
              >
                <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-cyan-500/10 text-cyan-300">
                  <AppIcon :name="a.icon" class="h-4 w-4" />
                </span>
                <span class="min-w-0">
                  <span class="block truncate text-sm font-medium text-foreground">{{ a.label }}</span>
                  <span class="block truncate text-xs text-muted-foreground">{{ a.desc }}</span>
                </span>
              </button>
            </div>
          </div>
        </div>

        <!-- App launcher grid -->
        <div class="relative hidden sm:block">
          <button
            type="button"
            class="flex h-10 w-10 items-center justify-center rounded-xl text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground"
            :class="openMenu === 'apps' ? 'bg-white/[0.06] text-foreground' : ''"
            aria-label="Tất cả chức năng"
            @click="toggle('apps')"
          >
            <AppIcon name="grid" class="h-5 w-5" />
          </button>

          <div v-if="openMenu === 'apps'" class="glass-panel absolute right-0 top-12 z-50 w-72 overflow-hidden rounded-2xl">
            <p class="border-b border-white/[0.06] px-4 py-2.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground/70">Khu vực</p>
            <div class="grid grid-cols-3 gap-1.5 p-3">
              <button
                v-for="g in navGroups"
                :key="g.label"
                type="button"
                class="flex flex-col items-center gap-1.5 rounded-xl border border-white/[0.05] bg-white/[0.02] px-2 py-3 text-center transition-colors hover:border-cyan-400/40 hover:bg-cyan-500/[0.06]"
                @click="goTo(g.items[0].to)"
              >
                <span class="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-500/20 to-fuchsia-500/20 text-cyan-200">
                  <AppIcon :name="g.icon" class="h-5 w-5" />
                </span>
                <span class="text-[0.7rem] font-medium leading-tight text-foreground">{{ g.label }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Focus mode -->
        <button
          type="button"
          class="hidden h-10 w-10 items-center justify-center rounded-xl transition-colors md:flex"
          :class="focusMode ? 'bg-cyan-500/15 text-cyan-200' : 'text-muted-foreground hover:bg-white/[0.06] hover:text-foreground'"
          :title="focusMode ? 'Tắt chế độ tập trung' : 'Bật chế độ tập trung'"
          @click="toggleFocus"
        >
          <AppIcon name="eye" class="h-5 w-5" />
        </button>

        <!-- Fullscreen -->
        <button
          type="button"
          class="hidden h-10 w-10 items-center justify-center rounded-xl text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground lg:flex"
          :title="isFullscreen ? 'Thoát toàn màn hình' : 'Toàn màn hình'"
          @click="toggleFullscreen"
        >
          <AppIcon :name="isFullscreen ? 'minimize' : 'maximize'" class="h-5 w-5" />
        </button>

        <!-- Streak -->
        <span class="hidden items-center gap-1.5 rounded-xl border border-orange-400/25 bg-orange-500/10 px-2.5 py-1.5 text-sm font-semibold text-orange-300 sm:inline-flex" title="Chuỗi học liên tục">
          <AppIcon name="flame" class="h-4 w-4" /> {{ props.streak }}
        </span>

        <!-- Gems -->
        <span class="hidden items-center gap-1.5 rounded-xl border border-violet-400/25 bg-violet-500/10 px-2.5 py-1.5 text-sm font-semibold text-violet-300 lg:inline-flex" title="Đá quý">
          <AppIcon name="gem" class="h-4 w-4" /> {{ props.gems }}
        </span>

        <!-- Coins -->
        <button
          type="button"
          class="inline-flex items-center gap-1.5 rounded-xl border border-amber-400/25 bg-amber-500/10 px-2.5 py-1.5 text-sm font-semibold text-amber-300 transition-colors hover:border-amber-400/50"
          title="Số dư coin — mở ví"
          @click="goTo('/app/wallet')"
        >
          <AppIcon name="coin" class="h-4 w-4" /> {{ props.coins.toLocaleString('vi-VN') }}
        </button>

        <!-- Messages -->
        <div class="relative">
          <button
            type="button"
            class="relative flex h-10 w-10 items-center justify-center rounded-xl text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground"
            :class="openMenu === 'messages' ? 'bg-white/[0.06] text-foreground' : ''"
            aria-label="Tin nhắn"
            @click="toggle('messages')"
          >
            <AppIcon name="message" class="h-5 w-5" />
            <span v-if="unreadMessages" class="absolute right-1.5 top-1.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-cyan-400 px-1 text-[0.6rem] font-bold text-slate-900">
              {{ unreadMessages }}
            </span>
          </button>

          <div v-if="openMenu === 'messages'" class="glass-panel absolute right-0 top-12 z-50 w-80 overflow-hidden rounded-2xl">
            <div class="flex items-center justify-between border-b border-white/[0.06] px-4 py-3">
              <p class="text-sm font-semibold text-foreground">Tin nhắn</p>
              <button type="button" class="text-xs font-medium text-cyan-300 hover:underline" @click="goTo('/app/groups')">Mở hộp thư</button>
            </div>
            <ul class="cosmic-scroll max-h-80 overflow-y-auto">
              <li
                v-for="m in messages"
                :key="m.id"
                class="flex cursor-pointer gap-3 px-4 py-3 transition-colors hover:bg-white/[0.04]"
                :class="m.unread ? 'bg-cyan-500/[0.05]' : ''"
                @click="goTo('/app/groups')"
              >
                <Avatar :name="m.name" :size="36" :accent="m.accent" />
                <div class="min-w-0 flex-1">
                  <p class="truncate text-sm font-medium text-foreground">{{ m.name }}</p>
                  <p class="truncate text-xs text-muted-foreground">{{ m.text }}</p>
                </div>
                <span class="shrink-0 text-[0.7rem] text-muted-foreground/70">{{ m.time }}</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Notifications -->
        <div class="relative">
          <button
            type="button"
            class="relative flex h-10 w-10 items-center justify-center rounded-xl text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground"
            :class="openMenu === 'notifications' ? 'bg-white/[0.06] text-foreground' : ''"
            aria-label="Thông báo"
            @click="toggle('notifications')"
          >
            <AppIcon name="bell" class="h-5 w-5" />
            <span class="absolute right-2.5 top-2.5 h-2 w-2 rounded-full bg-fuchsia-400 pulse-glow-magenta" />
          </button>

          <div v-if="openMenu === 'notifications'" class="glass-panel absolute right-0 top-12 z-50 w-80 overflow-hidden rounded-2xl">
            <div class="flex items-center justify-between border-b border-white/[0.06] px-4 py-3">
              <p class="text-sm font-semibold text-foreground">Thông báo</p>
              <button type="button" class="text-xs font-medium text-cyan-300 hover:underline">Đánh dấu đã đọc</button>
            </div>
            <ul class="cosmic-scroll max-h-80 overflow-y-auto">
              <li
                v-for="n in notifications"
                :key="n.id"
                class="flex gap-3 px-4 py-3 transition-colors hover:bg-white/[0.04]"
                :class="n.unread ? 'bg-cyan-500/[0.05]' : ''"
              >
                <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl" :class="notifTone[n.tone]">
                  <AppIcon :name="n.icon" class="h-4 w-4" />
                </span>
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-medium text-foreground">{{ n.title }}</p>
                  <p class="truncate text-xs text-muted-foreground">{{ n.desc }}</p>
                  <p class="mt-0.5 text-[0.7rem] text-muted-foreground/70">{{ n.time }}</p>
                </div>
                <span v-if="n.unread" class="mt-1 h-2 w-2 shrink-0 rounded-full bg-cyan-400" />
              </li>
            </ul>
            <button type="button" class="block w-full border-t border-white/[0.06] py-2.5 text-center text-xs font-semibold text-cyan-300 hover:bg-white/[0.04]">
              Xem tất cả thông báo
            </button>
          </div>
        </div>

        <!-- User menu -->
        <div class="relative">
          <button
            type="button"
            class="flex items-center gap-2 rounded-xl py-1 pl-1 pr-1.5 transition-colors hover:bg-white/[0.06] sm:pr-2"
            :class="openMenu === 'user' ? 'bg-white/[0.06]' : ''"
            @click="toggle('user')"
          >
            <Avatar :name="displayName" :size="32" accent="cyan" />
            <span class="hidden text-left leading-tight lg:block">
              <span class="block max-w-[10rem] truncate text-sm font-semibold text-foreground">{{ displayName }}</span>
              <span class="block text-[0.7rem] text-muted-foreground">Học viên · Cấp {{ props.level }}</span>
            </span>
            <AppIcon name="chevron-down" class="hidden h-4 w-4 text-muted-foreground lg:block" />
          </button>

          <div v-if="openMenu === 'user'" class="glass-panel absolute right-0 top-12 z-50 w-64 overflow-hidden rounded-2xl">
            <div class="flex items-center gap-3 border-b border-white/[0.06] px-4 py-3">
              <Avatar :name="displayName" :size="40" accent="cyan" />
              <div class="min-w-0">
                <p class="truncate text-sm font-semibold text-foreground">{{ displayName }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ email }}</p>
              </div>
            </div>
            <div class="py-1.5">
              <button
                v-for="m in menuItems"
                :key="m.label"
                type="button"
                class="flex w-full items-center gap-3 px-4 py-2 text-sm text-muted-foreground transition-colors hover:bg-white/[0.04] hover:text-foreground"
                @click="goTo(m.to)"
              >
                <AppIcon :name="m.icon" class="h-4 w-4 text-muted-foreground" />
                {{ m.label }}
              </button>
            </div>
            <div class="border-t border-white/[0.06] py-1.5">
              <button
                type="button"
                class="flex w-full items-center gap-3 px-4 py-2 text-sm font-medium text-rose-300 transition-colors hover:bg-rose-500/10 disabled:opacity-50"
                :disabled="loggingOut"
                @click="handleLogout"
              >
                <AppIcon name="logout" class="h-4 w-4" />
                {{ loggingOut ? 'Đang đăng xuất…' : 'Đăng xuất' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <button
      v-if="openMenu !== 'none'"
      type="button"
      class="fixed inset-0 z-40 cursor-default"
      aria-label="Đóng menu"
      tabindex="-1"
      @click="closeMenus"
    />

    <AppCommandPalette v-model:open="paletteOpen" :actions="paletteActions" />
  </header>
</template>
