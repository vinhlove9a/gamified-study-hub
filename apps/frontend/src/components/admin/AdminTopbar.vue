<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import { allAdminNavItems } from '@/features/admin/navigation';
import { useAuthSession } from '@/features/auth/authSession';

defineEmits<{ (e: 'open-sidebar'): void }>();

const route = useRoute();
const router = useRouter();
const { currentUser, logout } = useAuthSession();

const displayName = computed(() => currentUser.value?.fullName?.trim() || currentUser.value?.email || 'Quản trị viên');
const email = computed(() => currentUser.value?.email ?? 'admin@studyhub.vn');
const initials = computed(() =>
  displayName.value
    .split(/\s+/)
    .slice(-2)
    .map((p) => p[0]?.toUpperCase() ?? '')
    .join('')
);

const current = computed(() => allAdminNavItems.find((i) => {
  if (i.to === '/admin') return route.path === '/admin';
  return route.path === i.to || route.path.startsWith(`${i.to}/`);
}));
const sectionTitle = computed(() => current.value?.label ?? 'Quản trị');
const sectionBlurb = computed(() => current.value?.blurb ?? '');

type Menu = 'none' | 'notifications' | 'user';
const openMenu = ref<Menu>('none');
function toggle(m: Exclude<Menu, 'none'>) {
  openMenu.value = openMenu.value === m ? 'none' : m;
}
function closeMenus() {
  openMenu.value = 'none';
}

const notifications = [
  { id: 1, icon: 'flag', tone: 'rose', title: '7 báo cáo vi phạm mới', desc: 'Hàng đợi kiểm duyệt đang chờ xử lý', time: '3 phút trước', unread: true },
  { id: 2, icon: 'file-text', tone: 'green', title: '38 tài liệu chờ duyệt', desc: 'Người dùng vừa tải lên hôm nay', time: '20 phút trước', unread: true },
  { id: 3, icon: 'users', tone: 'cyan', title: '+248 người dùng mới', desc: 'Tăng 12% so với hôm qua', time: '1 giờ trước', unread: false }
] as const;
const notifTone: Record<string, string> = {
  rose: 'bg-rose-500/15 text-rose-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  cyan: 'bg-cyan-500/15 text-cyan-300'
};
const unread = computed(() => notifications.filter((n) => n.unread).length);

// Live clock
const now = ref<Date | null>(null);
let timer: ReturnType<typeof setInterval> | undefined;
const time = computed(() =>
  now.value ? now.value.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : '--:--:--'
);

const loggingOut = ref(false);
async function handleLogout() {
  if (loggingOut.value) return;
  loggingOut.value = true;
  try {
    closeMenus();
    await logout();
    await router.push('/auth/login');
  } finally {
    loggingOut.value = false;
  }
}

onMounted(() => {
  now.value = new Date();
  timer = setInterval(() => (now.value = new Date()), 1000);
});
onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-white/[0.06] bg-background/60 backdrop-blur-xl">
    <div class="flex items-center justify-between gap-3 px-4 py-3 sm:px-6">
      <!-- Left: mobile menu + title -->
      <div class="flex min-w-0 items-center gap-3">
        <button
          type="button"
          class="flex h-9 w-9 items-center justify-center rounded-xl border border-white/[0.06] bg-white/[0.03] text-muted-foreground hover:text-foreground lg:hidden"
          aria-label="Mở menu"
          @click="$emit('open-sidebar')"
        >
          <AppIcon name="menu" class="h-5 w-5" />
        </button>
        <div class="min-w-0">
          <div class="flex items-center gap-2">
            <h1 class="truncate text-base font-bold tracking-tight text-foreground sm:text-lg">{{ sectionTitle }}</h1>
            <span class="hidden rounded-md border border-amber-500/25 bg-amber-500/10 px-1.5 py-0.5 font-mono text-[9px] uppercase tracking-wider text-amber-300 sm:inline">
              Admin
            </span>
          </div>
          <p v-if="sectionBlurb" class="hidden truncate text-xs text-muted-foreground sm:block">{{ sectionBlurb }}</p>
        </div>
      </div>

      <!-- Right cluster -->
      <div class="flex items-center gap-2">
        <!-- Live clock -->
        <div class="hidden flex-col items-end rounded-lg border border-white/[0.05] bg-white/[0.03] px-3 py-1.5 md:flex">
          <span class="font-mono text-[9px] uppercase tracking-widest text-muted-foreground">Giờ máy chủ</span>
          <span class="font-mono text-sm font-semibold tabular-nums leading-tight text-cyan-200">{{ time }}</span>
        </div>

        <!-- Back to user app -->
        <RouterLink
          to="/app"
          class="hidden items-center gap-1.5 rounded-lg border border-white/[0.05] bg-white/[0.03] px-3 py-2 text-xs font-medium text-muted-foreground transition-colors hover:text-foreground sm:flex"
        >
          <AppIcon name="external-link" class="h-4 w-4" />
          Xem như người dùng
        </RouterLink>

        <!-- Notifications -->
        <div class="relative">
          <button
            type="button"
            class="relative flex h-9 w-9 items-center justify-center rounded-lg border border-white/[0.05] bg-white/[0.03] text-muted-foreground transition-colors hover:text-foreground"
            aria-label="Thông báo"
            @click="toggle('notifications')"
          >
            <AppIcon name="bell" class="h-4 w-4" />
            <span v-if="unread" class="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-fuchsia-400 pulse-glow-magenta" />
          </button>
          <transition name="menu">
            <div
              v-if="openMenu === 'notifications'"
              class="glass-panel absolute right-0 top-12 z-50 w-80 rounded-2xl p-2"
            >
              <div class="flex items-center justify-between px-3 py-2">
                <span class="text-sm font-semibold text-foreground">Thông báo</span>
                <span class="rounded-full bg-fuchsia-500/15 px-2 py-0.5 text-[10px] font-semibold text-fuchsia-300">{{ unread }} mới</span>
              </div>
              <ul class="space-y-0.5">
                <li v-for="n in notifications" :key="n.id">
                  <button class="flex w-full items-start gap-3 rounded-xl px-3 py-2.5 text-left transition-colors hover:bg-white/[0.05]">
                    <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg" :class="notifTone[n.tone]">
                      <AppIcon :name="n.icon" class="h-4 w-4" />
                    </span>
                    <span class="min-w-0 flex-1">
                      <span class="block text-sm font-medium text-foreground">{{ n.title }}</span>
                      <span class="block truncate text-xs text-muted-foreground">{{ n.desc }}</span>
                      <span class="mt-0.5 block text-[10px] text-muted-foreground/70">{{ n.time }}</span>
                    </span>
                    <span v-if="n.unread" class="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-cyan-400" />
                  </button>
                </li>
              </ul>
            </div>
          </transition>
        </div>

        <!-- Admin profile -->
        <div class="relative">
          <button
            type="button"
            class="flex items-center gap-2 rounded-lg border border-white/[0.05] bg-white/[0.03] py-1 pl-2 pr-1 transition-colors hover:bg-white/[0.06]"
            @click="toggle('user')"
          >
            <div class="hidden text-left xl:block">
              <p class="text-xs font-medium leading-tight text-foreground">{{ displayName }}</p>
              <p class="font-mono text-[10px] text-muted-foreground">QUẢN TRỊ VIÊN</p>
            </div>
            <div class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-cyan-500 to-fuchsia-500 text-xs font-bold text-white">
              {{ initials }}
            </div>
            <AppIcon name="chevron-down" class="hidden h-3 w-3 text-muted-foreground xl:block" />
          </button>
          <transition name="menu">
            <div v-if="openMenu === 'user'" class="glass-panel absolute right-0 top-12 z-50 w-60 rounded-2xl p-2">
              <div class="px-3 py-2">
                <p class="truncate text-sm font-semibold text-foreground">{{ displayName }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ email }}</p>
              </div>
              <div class="my-1 h-px bg-white/[0.06]" />
              <RouterLink to="/admin/system" class="flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-white/[0.05] hover:text-foreground" @click="closeMenus">
                <AppIcon name="settings" class="h-4 w-4" /> Cấu hình hệ thống
              </RouterLink>
              <RouterLink to="/app" class="flex items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-white/[0.05] hover:text-foreground" @click="closeMenus">
                <AppIcon name="home" class="h-4 w-4" /> Về ứng dụng
              </RouterLink>
              <div class="my-1 h-px bg-white/[0.06]" />
              <button
                type="button"
                :disabled="loggingOut"
                class="flex w-full items-center gap-2.5 rounded-xl px-3 py-2 text-sm text-rose-300 transition-colors hover:bg-rose-500/10 disabled:opacity-60"
                @click="handleLogout"
              >
                <AppIcon name="logout" class="h-4 w-4" /> {{ loggingOut ? 'Đang đăng xuất…' : 'Đăng xuất' }}
              </button>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- click-away backdrop -->
    <div v-if="openMenu !== 'none'" class="fixed inset-0 z-40" @click="closeMenus" />
  </header>
</template>

<style scoped>
.menu-enter-active,
.menu-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.menu-enter-from,
.menu-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
