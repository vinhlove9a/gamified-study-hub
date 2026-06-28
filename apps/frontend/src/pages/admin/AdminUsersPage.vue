<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdminStatCard from '@/components/admin/AdminStatCard.vue';
import AdminPill from '@/components/admin/AdminPill.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Role = 'admin' | 'mod' | 'vip' | 'member';
type Status = 'active' | 'suspended' | 'pending';

interface UserRow {
  id: number;
  name: string;
  email: string;
  role: Role;
  status: Status;
  level: number;
  xp: number; // 0-100 trong cấp hiện tại
  coin: number;
  docs: number;
  streak: number;
  joined: string;
  lastActive: string;
}

interface ActivityItem {
  icon: string;
  tone: Accent;
  text: string;
  time: string;
}

// ── KPI ───────────────────────────────────────────────────────────────
const stats = [
  {
    label: 'Tổng người dùng',
    value: 12428,
    icon: 'users',
    accent: 'cyan' as const,
    trend: 12.4,
    trendLabel: '+248 trong 24h',
    spark: [10800, 11020, 11240, 11500, 11780, 12010, 12230, 12428]
  },
  {
    label: 'Đăng ký hôm nay',
    value: 248,
    prefix: '+',
    icon: 'user',
    accent: 'green' as const,
    trend: 9.6,
    trendLabel: 'so với hôm qua',
    spark: [180, 196, 172, 210, 228, 215, 240, 248]
  },
  {
    label: 'Người dùng VIP',
    value: 1840,
    icon: 'crown',
    accent: 'amber' as const,
    trend: 6.2,
    trendLabel: '14.8% tổng người dùng',
    spark: [1560, 1610, 1655, 1700, 1740, 1782, 1810, 1840]
  },
  {
    label: 'Tài khoản bị khoá',
    value: 37,
    icon: 'lock',
    accent: 'magenta' as const,
    trend: -4.1,
    trendLabel: 'giảm trong tuần',
    spark: [44, 43, 42, 41, 40, 39, 38, 37]
  }
];

// ── Bộ lọc segmented ──────────────────────────────────────────────────
const roleFilters: { value: Role | 'all'; label: string }[] = [
  { value: 'all', label: 'Tất cả' },
  { value: 'admin', label: 'Quản trị' },
  { value: 'mod', label: 'Kiểm duyệt' },
  { value: 'vip', label: 'VIP' },
  { value: 'member', label: 'Thành viên' }
];
const statusFilters: { value: Status | 'all'; label: string }[] = [
  { value: 'all', label: 'Tất cả' },
  { value: 'active', label: 'Hoạt động' },
  { value: 'suspended', label: 'Tạm khoá' },
  { value: 'pending', label: 'Chờ xác minh' }
];

const search = ref('');
const roleFilter = ref<Role | 'all'>('all');
const statusFilter = ref<Status | 'all'>('all');

// ── Nhãn hiển thị ───────────────────────────────────────────────────────
const roleLabel: Record<Role, string> = {
  admin: 'Quản trị',
  mod: 'Kiểm duyệt',
  vip: 'VIP',
  member: 'Thành viên'
};
const roleTone: Record<Role, 'magenta' | 'violet' | 'amber' | 'neutral'> = {
  admin: 'magenta',
  mod: 'violet',
  vip: 'amber',
  member: 'neutral'
};
const statusLabel: Record<Status, string> = {
  active: 'Hoạt động',
  suspended: 'Tạm khoá',
  pending: 'Chờ xác minh'
};
const statusTone: Record<Status, 'green' | 'rose' | 'amber'> = {
  active: 'green',
  suspended: 'rose',
  pending: 'amber'
};
// Màu avatar gradient luân phiên theo vai trò
const avatarGrad: Record<Role, string> = {
  admin: 'from-fuchsia-500/80 to-rose-500/80',
  mod: 'from-violet-500/80 to-indigo-500/80',
  vip: 'from-amber-400/80 to-orange-500/80',
  member: 'from-cyan-500/80 to-sky-500/80'
};
const xpAccent: Record<Role, Accent> = {
  admin: 'magenta',
  mod: 'violet',
  vip: 'amber',
  member: 'cyan'
};

function initials(name: string): string {
  const parts = name.trim().split(/\s+/);
  return (parts[0]?.[0] ?? '') + (parts[parts.length - 1]?.[0] ?? '');
}

// ── Dữ liệu mock ─────────────────────────────────────────────────────────
const users = ref<UserRow[]>([
  { id: 1, name: 'Nguyễn Minh Anh', email: 'minhanh.nguyen@fpt.edu.vn', role: 'admin', status: 'active', level: 42, xp: 68, coin: 18240, docs: 132, streak: 96, joined: '12/03/2023', lastActive: 'Vừa xong' },
  { id: 2, name: 'Trần Quốc Bảo', email: 'baotq@studyhub.vn', role: 'mod', status: 'active', level: 38, xp: 41, coin: 14120, docs: 88, streak: 54, joined: '04/06/2023', lastActive: '8 phút' },
  { id: 3, name: 'Lê Thu Hà', email: 'thuha.le@gmail.com', role: 'vip', status: 'active', level: 51, xp: 87, coin: 30410, docs: 64, streak: 121, joined: '28/01/2023', lastActive: '21 phút' },
  { id: 4, name: 'Phạm Đức Huy', email: 'duchuy.pham@gmail.com', role: 'member', status: 'active', level: 17, xp: 22, coin: 3120, docs: 9, streak: 12, joined: '15/09/2024', lastActive: '1 giờ' },
  { id: 5, name: 'Hoàng Thị Mai', email: 'maihoang@hotmail.com', role: 'vip', status: 'suspended', level: 29, xp: 55, coin: 9870, docs: 41, streak: 0, joined: '07/11/2023', lastActive: '3 ngày' },
  { id: 6, name: 'Vũ Tuấn Kiệt', email: 'kietvt@studyhub.vn', role: 'member', status: 'pending', level: 2, xp: 8, coin: 120, docs: 0, streak: 0, joined: '27/06/2026', lastActive: 'Chưa kích hoạt' },
  { id: 7, name: 'Đặng Khánh Linh', email: 'linh.dk@gmail.com', role: 'member', status: 'active', level: 23, xp: 60, coin: 5420, docs: 18, streak: 33, joined: '02/02/2024', lastActive: '12 phút' },
  { id: 8, name: 'Bùi Gia Bảo', email: 'giabao.bui@gmail.com', role: 'vip', status: 'active', level: 44, xp: 33, coin: 21800, docs: 57, streak: 78, joined: '19/04/2023', lastActive: '47 phút' },
  { id: 9, name: 'Ngô Phương Thảo', email: 'thaonp@fpt.edu.vn', role: 'mod', status: 'active', level: 35, xp: 72, coin: 11930, docs: 73, streak: 45, joined: '11/07/2023', lastActive: '2 giờ' },
  { id: 10, name: 'Đỗ Hải Nam', email: 'hainam.do@gmail.com', role: 'member', status: 'suspended', level: 14, xp: 19, coin: 980, docs: 4, streak: 0, joined: '23/05/2025', lastActive: '5 ngày' },
  { id: 11, name: 'Lý Tường Vy', email: 'tuongvy.ly@gmail.com', role: 'vip', status: 'active', level: 48, xp: 90, coin: 27650, docs: 49, streak: 110, joined: '30/12/2022', lastActive: '6 phút' },
  { id: 12, name: 'Cao Minh Quân', email: 'quancm@studyhub.vn', role: 'member', status: 'pending', level: 1, xp: 4, coin: 50, docs: 0, streak: 0, joined: '28/06/2026', lastActive: 'Chưa kích hoạt' },
  { id: 13, name: 'Trịnh Bảo Châu', email: 'baochau.trinh@gmail.com', role: 'member', status: 'active', level: 26, xp: 48, coin: 6210, docs: 22, streak: 27, joined: '14/03/2024', lastActive: '34 phút' },
  { id: 14, name: 'Phan Thanh Tùng', email: 'tungpt@gmail.com', role: 'member', status: 'active', level: 19, xp: 15, coin: 2740, docs: 11, streak: 8, joined: '09/08/2024', lastActive: '1 giờ' },
  { id: 15, name: 'Dương Khả Hân', email: 'khahan.duong@gmail.com', role: 'vip', status: 'active', level: 40, xp: 64, coin: 19320, docs: 38, streak: 89, joined: '21/05/2023', lastActive: '18 phút' },
  { id: 16, name: 'Mai Đức Thắng', email: 'thangmd@fpt.edu.vn', role: 'mod', status: 'suspended', level: 31, xp: 51, coin: 10240, docs: 60, streak: 0, joined: '03/10/2023', lastActive: '2 ngày' },
  { id: 17, name: 'Hồ Ngọc Diệp', email: 'ngocdiep.ho@gmail.com', role: 'member', status: 'active', level: 21, xp: 37, coin: 4480, docs: 15, streak: 19, joined: '17/01/2024', lastActive: '53 phút' },
  { id: 18, name: 'Tạ Minh Triết', email: 'minhtriet.ta@gmail.com', role: 'member', status: 'pending', level: 3, xp: 11, coin: 200, docs: 1, streak: 2, joined: '26/06/2026', lastActive: '4 giờ' },
  { id: 19, name: 'Lương Bích Ngọc', email: 'bichngoc.luong@gmail.com', role: 'vip', status: 'active', level: 46, xp: 79, coin: 24910, docs: 52, streak: 102, joined: '08/02/2023', lastActive: '9 phút' },
  { id: 20, name: 'Châu Gia Hưng', email: 'giahung.chau@gmail.com', role: 'member', status: 'active', level: 28, xp: 43, coin: 7050, docs: 26, streak: 41, joined: '12/04/2024', lastActive: '1 giờ' },
  { id: 21, name: 'Đinh Thuỳ Dương', email: 'thuyduong.dinh@gmail.com', role: 'member', status: 'suspended', level: 16, xp: 28, coin: 1620, docs: 6, streak: 0, joined: '30/07/2025', lastActive: '8 ngày' },
  { id: 22, name: 'Võ Anh Khoa', email: 'anhkhoa.vo@studyhub.vn', role: 'admin', status: 'active', level: 39, xp: 58, coin: 16730, docs: 95, streak: 67, joined: '25/02/2023', lastActive: '5 phút' },
  { id: 23, name: 'Phùng Diễm My', email: 'diemmy.phung@gmail.com', role: 'member', status: 'active', level: 24, xp: 70, coin: 5890, docs: 20, streak: 36, joined: '06/06/2024', lastActive: '27 phút' },
  { id: 24, name: 'Tô Hoàng Long', email: 'hoanglong.to@gmail.com', role: 'member', status: 'pending', level: 2, xp: 6, coin: 80, docs: 0, streak: 0, joined: '28/06/2026', lastActive: 'Chưa kích hoạt' }
]);

// ── Lọc + tìm kiếm ────────────────────────────────────────────────────
const filtered = computed<UserRow[]>(() => {
  const q = search.value.trim().toLowerCase();
  return users.value.filter((u) => {
    if (roleFilter.value !== 'all' && u.role !== roleFilter.value) return false;
    if (statusFilter.value !== 'all' && u.status !== statusFilter.value) return false;
    if (q && !u.name.toLowerCase().includes(q) && !u.email.toLowerCase().includes(q)) return false;
    return true;
  });
});

// ── Phân trang ────────────────────────────────────────────────────────
const pageSize = 8;
const page = ref(1);
const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)));
const paged = computed<UserRow[]>(() =>
  filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize)
);
const rangeFrom = computed(() => (filtered.value.length === 0 ? 0 : (page.value - 1) * pageSize + 1));
const rangeTo = computed(() => Math.min(page.value * pageSize, filtered.value.length));

// Đưa về trang 1 khi điều kiện lọc đổi
watch([search, roleFilter, statusFilter], () => {
  page.value = 1;
});
// Giữ trang hợp lệ nếu danh sách co lại
watch(totalPages, (tp) => {
  if (page.value > tp) page.value = tp;
});

// ── Chọn nhiều dòng ───────────────────────────────────────────────────
const selected = ref<Set<number>>(new Set());
const selectedCount = computed(() => selected.value.size);

function toggleRow(id: number): void {
  const next = new Set(selected.value);
  next.has(id) ? next.delete(id) : next.add(id);
  selected.value = next;
}
const pageIds = computed(() => paged.value.map((u) => u.id));
const allPageSelected = computed(
  () => pageIds.value.length > 0 && pageIds.value.every((id) => selected.value.has(id))
);
const somePageSelected = computed(
  () => pageIds.value.some((id) => selected.value.has(id)) && !allPageSelected.value
);
function toggleAllPage(): void {
  const next = new Set(selected.value);
  if (allPageSelected.value) {
    pageIds.value.forEach((id) => next.delete(id));
  } else {
    pageIds.value.forEach((id) => next.add(id));
  }
  selected.value = next;
}
function clearSelection(): void {
  selected.value = new Set();
}
function bulkSuspend(): void {
  users.value = users.value.map((u) =>
    selected.value.has(u.id) ? { ...u, status: 'suspended' } : u
  );
  clearSelection();
}
function bulkDelete(): void {
  users.value = users.value.filter((u) => !selected.value.has(u.id));
  clearSelection();
}

// ── Drawer chi tiết ───────────────────────────────────────────────────
const drawerUser = ref<UserRow | null>(null);
function openDrawer(u: UserRow): void {
  drawerUser.value = u;
}
function closeDrawer(): void {
  drawerUser.value = null;
}
function toggleSuspend(u: UserRow): void {
  const next: Status = u.status === 'suspended' ? 'active' : 'suspended';
  users.value = users.value.map((x) => (x.id === u.id ? { ...x, status: next } : x));
  if (drawerUser.value?.id === u.id) drawerUser.value = { ...drawerUser.value, status: next };
}
function setRole(role: Role): void {
  if (!drawerUser.value) return;
  const id = drawerUser.value.id;
  users.value = users.value.map((x) => (x.id === id ? { ...x, role } : x));
  drawerUser.value = { ...drawerUser.value, role };
}
function deleteUser(u: UserRow): void {
  users.value = users.value.filter((x) => x.id !== u.id);
  const next = new Set(selected.value);
  next.delete(u.id);
  selected.value = next;
  closeDrawer();
}

const drawerStats = computed(() => {
  const u = drawerUser.value;
  if (!u) return [];
  return [
    { label: 'Cấp độ', value: `Lv ${u.level}`, icon: 'award', accent: 'cyan' as const },
    { label: 'Coin', value: u.coin.toLocaleString('vi-VN'), icon: 'coin', accent: 'amber' as const },
    { label: 'Tài liệu', value: `${u.docs}`, icon: 'file-text', accent: 'green' as const },
    { label: 'Chuỗi ngày', value: `${u.streak} ngày`, icon: 'flame', accent: 'magenta' as const }
  ];
});

const drawerActivity: ActivityItem[] = [
  { icon: 'file-text', tone: 'green', text: 'Đăng tải "Đề cương Giải tích 2"', time: '2 giờ' },
  { icon: 'trophy', tone: 'violet', text: 'Hoàn thành nhiệm vụ "Học 7 ngày"', time: 'Hôm qua' },
  { icon: 'coin', tone: 'amber', text: 'Mua gói VIP Premium 12 tháng', time: '3 ngày' },
  { icon: 'message', tone: 'cyan', text: 'Bình luận trong diễn đàn Toán học', time: '4 ngày' },
  { icon: 'book-open', tone: 'cyan', text: 'Tạo bộ flashcard "3000 từ IELTS"', time: '1 tuần' }
];
const drawerActivityTone: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2">
          <span class="h-1.5 w-1.5 rounded-full bg-cyan-400 glow-cyan" />
          <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">
            Quản lý · Người dùng &amp; phân quyền
          </p>
        </div>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Quản lý người dùng</h2>
      </div>
      <div class="flex items-center gap-2">
        <button
          class="flex items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-white/[0.06]"
        >
          <AppIcon name="download" class="h-4 w-4" /> Xuất CSV
        </button>
        <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
          <AppIcon name="plus" class="h-4 w-4" /> Mời người dùng
        </button>
      </div>
    </div>

    <!-- KPI row -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <AdminStatCard
        v-for="s in stats"
        :key="s.label"
        :label="s.label"
        :value="s.value"
        :prefix="s.prefix"
        :icon="s.icon"
        :accent="s.accent"
        :trend="s.trend"
        :trend-label="s.trendLabel"
        :sparkline="s.spark"
      />
    </div>

    <!-- Users table -->
    <AdminPanel flush accent="cyan" glow="cyan">
      <template #header>
        <div class="flex min-w-0 flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div class="flex items-center gap-3">
            <span
              class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-cyan-500/20 bg-cyan-500/10 text-cyan-300"
            >
              <AppIcon name="users" class="h-5 w-5" />
            </span>
            <div class="min-w-0">
              <h3 class="truncate text-sm font-semibold tracking-wide text-foreground">Danh sách người dùng</h3>
              <p class="mt-0.5 truncate text-xs text-muted-foreground">
                {{ filtered.length.toLocaleString('vi-VN') }} kết quả phù hợp
              </p>
            </div>
          </div>

          <!-- Search -->
          <div class="relative w-full lg:w-72">
            <AppIcon
              name="search"
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
            />
            <input
              v-model="search"
              type="text"
              placeholder="Tìm theo tên hoặc email…"
              class="cosmic-input w-full rounded-xl py-2 pl-9 pr-3 text-sm"
            />
          </div>
        </div>
      </template>

      <!-- Toolbar: filters -->
      <div class="flex flex-col gap-3 px-5 pb-4 xl:flex-row xl:items-center xl:justify-between">
        <div class="flex items-center gap-2">
          <span class="hidden text-xs font-medium text-muted-foreground sm:inline">Vai trò:</span>
          <div class="flex flex-wrap rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
            <button
              v-for="f in roleFilters"
              :key="f.value"
              class="rounded-lg px-3 py-1.5 text-xs font-medium transition-colors"
              :class="roleFilter === f.value ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
              @click="roleFilter = f.value"
            >
              {{ f.label }}
            </button>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <span class="hidden text-xs font-medium text-muted-foreground sm:inline">Trạng thái:</span>
          <div class="flex flex-wrap rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
            <button
              v-for="f in statusFilters"
              :key="f.value"
              class="rounded-lg px-3 py-1.5 text-xs font-medium transition-colors"
              :class="statusFilter === f.value ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
              @click="statusFilter = f.value"
            >
              {{ f.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="cosmic-scroll overflow-x-auto border-t border-white/[0.06]">
        <table class="w-full min-w-[920px] border-collapse text-left">
          <thead>
            <tr class="border-b border-white/[0.06] text-[11px] uppercase tracking-wider text-muted-foreground">
              <th class="w-10 py-3 pl-5 pr-2">
                <input
                  type="checkbox"
                  class="h-4 w-4 cursor-pointer rounded border-white/20 bg-white/[0.04] accent-cyan-400"
                  :checked="allPageSelected"
                  :indeterminate.prop="somePageSelected"
                  @change="toggleAllPage"
                />
              </th>
              <th class="py-3 pr-3 font-medium">Người dùng</th>
              <th class="py-3 pr-3 font-medium">Vai trò</th>
              <th class="py-3 pr-3 font-medium">Trạng thái</th>
              <th class="py-3 pr-3 font-medium">Cấp độ</th>
              <th class="py-3 pr-3 font-medium">Coin</th>
              <th class="py-3 pr-3 font-medium">Tham gia</th>
              <th class="py-3 pr-3 font-medium">Hoạt động cuối</th>
              <th class="py-3 pr-5 text-right font-medium">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="u in paged"
              :key="u.id"
              class="group cursor-pointer border-b border-white/[0.04] transition-colors hover:bg-white/[0.03]"
              :class="selected.has(u.id) ? 'bg-cyan-500/[0.06]' : ''"
              @click="openDrawer(u)"
            >
              <!-- checkbox -->
              <td class="py-3 pl-5 pr-2" @click.stop>
                <input
                  type="checkbox"
                  class="h-4 w-4 cursor-pointer rounded border-white/20 bg-white/[0.04] accent-cyan-400"
                  :checked="selected.has(u.id)"
                  @change="toggleRow(u.id)"
                />
              </td>
              <!-- user -->
              <td class="py-3 pr-3">
                <div class="flex items-center gap-3">
                  <span
                    class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-br text-xs font-bold uppercase text-white"
                    :class="avatarGrad[u.role]"
                  >
                    {{ initials(u.name) }}
                  </span>
                  <div class="min-w-0">
                    <p class="truncate text-sm font-medium text-foreground">{{ u.name }}</p>
                    <p class="truncate text-xs text-muted-foreground">{{ u.email }}</p>
                  </div>
                </div>
              </td>
              <!-- role -->
              <td class="py-3 pr-3">
                <AdminPill :tone="roleTone[u.role]">{{ roleLabel[u.role] }}</AdminPill>
              </td>
              <!-- status -->
              <td class="py-3 pr-3">
                <AdminPill :tone="statusTone[u.status]" dot>{{ statusLabel[u.status] }}</AdminPill>
              </td>
              <!-- level -->
              <td class="py-3 pr-3">
                <div class="flex items-center gap-2">
                  <span class="font-mono text-xs font-semibold text-foreground">Lv {{ u.level }}</span>
                  <div class="w-16">
                    <ProgressBar :value="u.xp" :accent="xpAccent[u.role]" :height="4" />
                  </div>
                </div>
              </td>
              <!-- coin -->
              <td class="py-3 pr-3">
                <span class="inline-flex items-center gap-1.5 font-mono text-sm tabular-nums text-foreground">
                  <AppIcon name="coin" class="h-3.5 w-3.5 text-amber-300" />
                  {{ u.coin.toLocaleString('vi-VN') }}
                </span>
              </td>
              <!-- joined -->
              <td class="py-3 pr-3">
                <span class="font-mono text-xs text-muted-foreground">{{ u.joined }}</span>
              </td>
              <!-- last active -->
              <td class="py-3 pr-3">
                <span class="text-xs text-muted-foreground">{{ u.lastActive }}</span>
              </td>
              <!-- actions -->
              <td class="py-3 pr-5" @click.stop>
                <div class="flex items-center justify-end gap-1.5">
                  <button
                    class="flex h-8 w-8 items-center justify-center rounded-lg bg-white/[0.04] text-cyan-300 transition-colors hover:bg-cyan-500/15"
                    title="Xem chi tiết"
                    @click="openDrawer(u)"
                  >
                    <AppIcon name="eye" class="h-4 w-4" />
                  </button>
                  <button
                    class="flex h-8 w-8 items-center justify-center rounded-lg transition-colors"
                    :class="u.status === 'suspended'
                      ? 'bg-emerald-500/10 text-emerald-300 hover:bg-emerald-500/20'
                      : 'bg-rose-500/10 text-rose-300 hover:bg-rose-500/20'"
                    :title="u.status === 'suspended' ? 'Mở khoá' : 'Tạm khoá'"
                    @click="toggleSuspend(u)"
                  >
                    <AppIcon :name="u.status === 'suspended' ? 'unlock' : 'lock'" class="h-4 w-4" />
                  </button>
                  <button
                    class="flex h-8 w-8 items-center justify-center rounded-lg bg-white/[0.04] text-muted-foreground transition-colors hover:bg-white/[0.08] hover:text-foreground"
                    title="Thêm"
                  >
                    <AppIcon name="more" class="h-4 w-4" />
                  </button>
                </div>
              </td>
            </tr>

            <!-- empty -->
            <tr v-if="paged.length === 0">
              <td colspan="9" class="py-16 text-center">
                <div class="flex flex-col items-center gap-2 text-muted-foreground">
                  <AppIcon name="search" class="h-8 w-8 opacity-40" />
                  <p class="text-sm">Không tìm thấy người dùng phù hợp.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div
        class="flex flex-col gap-3 border-t border-white/[0.06] px-5 py-3.5 sm:flex-row sm:items-center sm:justify-between"
      >
        <p class="text-xs text-muted-foreground">
          Hiển thị <span class="font-mono text-foreground">{{ rangeFrom }}</span>–<span class="font-mono text-foreground">{{ rangeTo }}</span>
          trên <span class="font-mono text-foreground">{{ filtered.length }}</span> người dùng
        </p>
        <div class="flex items-center gap-1.5">
          <button
            class="flex h-8 w-8 items-center justify-center rounded-lg border border-white/[0.06] bg-white/[0.03] text-muted-foreground transition-colors enabled:hover:bg-white/[0.07] enabled:hover:text-foreground disabled:opacity-30"
            :disabled="page <= 1"
            @click="page--"
          >
            <AppIcon name="chevron-left" class="h-4 w-4" />
          </button>
          <button
            v-for="p in totalPages"
            :key="p"
            class="h-8 min-w-8 rounded-lg px-2.5 text-xs font-medium transition-colors"
            :class="p === page
              ? 'bg-cyan-500/15 text-cyan-300 border border-cyan-500/30'
              : 'border border-white/[0.06] bg-white/[0.03] text-muted-foreground hover:bg-white/[0.07] hover:text-foreground'"
            @click="page = p"
          >
            {{ p }}
          </button>
          <button
            class="flex h-8 w-8 items-center justify-center rounded-lg border border-white/[0.06] bg-white/[0.03] text-muted-foreground transition-colors enabled:hover:bg-white/[0.07] enabled:hover:text-foreground disabled:opacity-30"
            :disabled="page >= totalPages"
            @click="page++"
          >
            <AppIcon name="chevron-right" class="h-4 w-4" />
          </button>
        </div>
      </div>
    </AdminPanel>

    <!-- Bulk action bar -->
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="translate-y-4 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-4 opacity-0"
    >
      <div
        v-if="selectedCount > 0"
        class="fixed inset-x-0 bottom-6 z-40 flex justify-center px-4"
      >
        <div
          class="glass-panel gradient-border flex flex-wrap items-center gap-2 rounded-2xl px-4 py-3 shadow-2xl"
        >
          <span class="flex items-center gap-2 pr-2 text-sm font-medium text-foreground">
            <span class="flex h-7 min-w-7 items-center justify-center rounded-lg bg-cyan-500/15 px-2 font-mono text-xs text-cyan-300">
              {{ selectedCount }}
            </span>
            đã chọn
          </span>
          <span class="h-6 w-px bg-white/10" />
          <button
            class="flex items-center gap-1.5 rounded-lg bg-white/[0.05] px-3 py-1.5 text-xs font-medium text-foreground transition-colors hover:bg-white/[0.1]"
          >
            <AppIcon name="mail" class="h-4 w-4 text-cyan-300" /> Gửi email
          </button>
          <button
            class="flex items-center gap-1.5 rounded-lg bg-rose-500/10 px-3 py-1.5 text-xs font-medium text-rose-300 transition-colors hover:bg-rose-500/20"
            @click="bulkSuspend"
          >
            <AppIcon name="lock" class="h-4 w-4" /> Khoá
          </button>
          <button
            class="flex items-center gap-1.5 rounded-lg bg-rose-500/10 px-3 py-1.5 text-xs font-medium text-rose-300 transition-colors hover:bg-rose-500/20"
            @click="bulkDelete"
          >
            <AppIcon name="trash" class="h-4 w-4" /> Xoá
          </button>
          <button
            class="flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:text-foreground"
            @click="clearSelection"
          >
            <AppIcon name="x" class="h-4 w-4" /> Bỏ chọn
          </button>
        </div>
      </div>
    </transition>

    <!-- Detail drawer -->
    <transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="drawerUser"
        class="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm"
        @click="closeDrawer"
      />
    </transition>

    <transition
      enter-active-class="transition-transform duration-300 ease-out"
      enter-from-class="translate-x-full"
      enter-to-class="translate-x-0"
      leave-active-class="transition-transform duration-200 ease-in"
      leave-from-class="translate-x-0"
      leave-to-class="translate-x-full"
    >
      <aside
        v-if="drawerUser"
        class="glass-panel cosmic-scroll fixed inset-y-0 right-0 z-50 flex w-full max-w-md flex-col overflow-y-auto border-l border-white/[0.08]"
      >
        <!-- Drawer header -->
        <div class="flex items-start justify-between gap-3 border-b border-white/[0.06] p-5">
          <div class="flex min-w-0 items-center gap-3">
            <span
              class="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br text-lg font-bold uppercase text-white"
              :class="avatarGrad[drawerUser.role]"
            >
              {{ initials(drawerUser.name) }}
            </span>
            <div class="min-w-0">
              <h3 class="truncate text-base font-bold text-foreground">{{ drawerUser.name }}</h3>
              <p class="truncate text-xs text-muted-foreground">{{ drawerUser.email }}</p>
              <div class="mt-1.5 flex items-center gap-1.5">
                <AdminPill :tone="roleTone[drawerUser.role]">{{ roleLabel[drawerUser.role] }}</AdminPill>
                <AdminPill :tone="statusTone[drawerUser.status]" dot>{{ statusLabel[drawerUser.status] }}</AdminPill>
              </div>
            </div>
          </div>
          <button
            class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/[0.04] text-muted-foreground transition-colors hover:bg-white/[0.1] hover:text-foreground"
            @click="closeDrawer"
          >
            <AppIcon name="x" class="h-4 w-4" />
          </button>
        </div>

        <div class="space-y-5 p-5">
          <!-- Metrics -->
          <div class="grid grid-cols-2 gap-3">
            <div
              v-for="m in drawerStats"
              :key="m.label"
              class="rounded-xl border border-white/[0.05] bg-white/[0.02] p-3"
            >
              <div class="flex items-center gap-1.5 text-xs text-muted-foreground">
                <AppIcon :name="m.icon" class="h-3.5 w-3.5" :class="drawerActivityTone[m.accent].split(' ')[1]" />
                {{ m.label }}
              </div>
              <p class="mt-1 text-lg font-bold tabular-nums text-foreground">{{ m.value }}</p>
            </div>
          </div>

          <!-- XP progress -->
          <div class="rounded-xl border border-white/[0.05] bg-white/[0.02] p-4">
            <div class="mb-2 flex items-center justify-between text-xs">
              <span class="text-muted-foreground">Tiến độ cấp {{ drawerUser.level }} → {{ drawerUser.level + 1 }}</span>
              <span class="font-mono font-semibold text-foreground">{{ drawerUser.xp }}%</span>
            </div>
            <ProgressBar :value="drawerUser.xp" :accent="xpAccent[drawerUser.role]" :height="8" />
          </div>

          <!-- Recent activity -->
          <div>
            <h4 class="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <AppIcon name="clock" class="h-3.5 w-3.5" /> Hoạt động gần đây
            </h4>
            <ul class="space-y-2.5">
              <li v-for="(a, i) in drawerActivity" :key="i" class="flex gap-3">
                <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg" :class="drawerActivityTone[a.tone]">
                  <AppIcon :name="a.icon" class="h-4 w-4" />
                </span>
                <div class="min-w-0 flex-1">
                  <p class="text-sm text-foreground">{{ a.text }}</p>
                </div>
                <span class="shrink-0 font-mono text-[10px] text-muted-foreground">{{ a.time }}</span>
              </li>
            </ul>
          </div>

          <!-- Role control -->
          <div>
            <h4 class="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <AppIcon name="shield" class="h-3.5 w-3.5" /> Phân quyền
            </h4>
            <div class="grid grid-cols-2 gap-2">
              <button
                v-for="r in (['admin', 'mod', 'vip', 'member'] as Role[])"
                :key="r"
                class="rounded-xl border px-3 py-2 text-xs font-medium transition-colors"
                :class="drawerUser.role === r
                  ? 'border-cyan-500/40 bg-cyan-500/10 text-cyan-300'
                  : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:bg-white/[0.05] hover:text-foreground'"
                @click="setRole(r)"
              >
                {{ roleLabel[r] }}
              </button>
            </div>
          </div>

          <!-- Danger zone -->
          <div class="rounded-xl border border-rose-500/30 bg-rose-500/[0.04] p-4">
            <h4 class="mb-1 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-rose-300">
              <AppIcon name="alert" class="h-3.5 w-3.5" /> Vùng nguy hiểm
            </h4>
            <p class="mb-3 text-xs text-muted-foreground">
              Các thao tác này ảnh hưởng trực tiếp đến quyền truy cập của người dùng.
            </p>
            <div class="flex flex-col gap-2 sm:flex-row">
              <button
                class="flex flex-1 items-center justify-center gap-1.5 rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-xs font-medium text-rose-300 transition-colors hover:bg-rose-500/20"
                @click="toggleSuspend(drawerUser)"
              >
                <AppIcon :name="drawerUser.status === 'suspended' ? 'unlock' : 'lock'" class="h-4 w-4" />
                {{ drawerUser.status === 'suspended' ? 'Mở khoá tài khoản' : 'Tạm khoá tài khoản' }}
              </button>
              <button
                class="flex flex-1 items-center justify-center gap-1.5 rounded-lg border border-rose-500/40 bg-rose-500/15 px-3 py-2 text-xs font-medium text-rose-300 transition-colors hover:bg-rose-500/25"
                @click="deleteUser(drawerUser)"
              >
                <AppIcon name="trash" class="h-4 w-4" /> Xoá vĩnh viễn
              </button>
            </div>
          </div>
        </div>
      </aside>
    </transition>
  </div>
</template>
