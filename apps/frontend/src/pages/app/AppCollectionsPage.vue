<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import Avatar from '@/components/app/Avatar.vue';
import Toggle from '@/components/app/Toggle.vue';
import Tabs from '@/components/app/Tabs.vue';
import DocumentCard from '@/components/app/DocumentCard.vue';

// ─── Types ────────────────────────────────────────────────────────────────────

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Contributor {
  name: string;
  accent: Accent;
}

interface CollectionDoc {
  title: string;
  category: string;
  meta: string;
  readMinutes?: number;
  rating?: number;
  coins?: number;
  tone: Accent;
}

interface Collection {
  id: number;
  name: string;
  icon: string;
  accent: Accent;
  docCount: number;
  progress: number;
  owner: 'me' | 'other';
  shared: boolean;
  pinned: boolean;
  contributors: Contributor[];
  docs: CollectionDoc[];
}

// ─── Static config ────────────────────────────────────────────────────────────

const cover: Record<Accent, string> = {
  cyan: 'from-cyan-500/50 via-blue-600/30 to-transparent',
  magenta: 'from-fuchsia-500/50 via-purple-600/30 to-transparent',
  green: 'from-emerald-500/50 via-teal-600/30 to-transparent',
  violet: 'from-violet-500/50 via-indigo-600/30 to-transparent',
  amber: 'from-amber-400/50 via-orange-600/30 to-transparent',
};

const iconChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/20 text-cyan-100',
  magenta: 'bg-fuchsia-500/20 text-fuchsia-100',
  green: 'bg-emerald-500/20 text-emerald-100',
  violet: 'bg-violet-500/20 text-violet-100',
  amber: 'bg-amber-500/20 text-amber-100',
};

const iconChoices: string[] = ['folder', 'book-open', 'graduation', 'rocket', 'target', 'layers', 'star', 'flame'];
const accentChoices: Accent[] = ['cyan', 'violet', 'green', 'amber', 'magenta'];

const accentDot: Record<Accent, string> = {
  cyan: 'bg-cyan-400',
  magenta: 'bg-fuchsia-400',
  green: 'bg-emerald-400',
  violet: 'bg-violet-400',
  amber: 'bg-amber-300',
};

// ─── State ────────────────────────────────────────────────────────────────────

const collections = ref<Collection[]>([
  {
    id: 1,
    name: 'Ôn thi THPT Quốc gia',
    icon: 'graduation',
    accent: 'cyan',
    docCount: 24,
    progress: 68,
    owner: 'me',
    shared: true,
    pinned: true,
    contributors: [
      { name: 'Nguyễn An', accent: 'cyan' },
      { name: 'Trần Bình', accent: 'violet' },
      { name: 'Lê Cường', accent: 'green' },
      { name: 'Phạm Dung', accent: 'amber' },
    ],
    docs: [
      { title: 'Giải tích — Tổng ôn', category: 'Toán học', meta: 'PDF · 48 trang', readMinutes: 22, rating: 4.8, coins: 15, tone: 'cyan' },
      { title: 'Vật lý — Điện từ học', category: 'Vật lý', meta: 'Slide · 36 trang', readMinutes: 16, rating: 4.5, tone: 'violet' },
      { title: 'Hoá hữu cơ trọng tâm', category: 'Hoá học', meta: 'PDF · 40 trang', readMinutes: 18, rating: 4.7, coins: 20, tone: 'green' },
    ],
  },
  {
    id: 2,
    name: 'Lập trình Web Full-stack',
    icon: 'rocket',
    accent: 'violet',
    docCount: 18,
    progress: 42,
    owner: 'me',
    shared: false,
    pinned: false,
    contributors: [{ name: 'Nguyễn An', accent: 'cyan' }],
    docs: [
      { title: 'Vue 3 & TypeScript', category: 'Tin học', meta: 'PDF + Video · 72 trang', readMinutes: 35, rating: 5.0, coins: 40, tone: 'violet' },
      { title: 'Cấu trúc dữ liệu & Giải thuật', category: 'Tin học', meta: 'PDF · 56 trang', readMinutes: 30, rating: 4.9, coins: 25, tone: 'magenta' },
    ],
  },
  {
    id: 3,
    name: 'Tiếng Anh học thuật',
    icon: 'book-open',
    accent: 'green',
    docCount: 12,
    progress: 85,
    owner: 'other',
    shared: true,
    pinned: true,
    contributors: [
      { name: 'Mai Linh', accent: 'magenta' },
      { name: 'Hoàng Nam', accent: 'amber' },
      { name: 'Vũ Quân', accent: 'cyan' },
    ],
    docs: [
      { title: 'Phát âm nâng cao', category: 'Ngoại ngữ', meta: 'Audio + PDF · 28 trang', readMinutes: 14, rating: 4.6, coins: 12, tone: 'amber' },
      { title: 'Ngữ pháp B2', category: 'Ngoại ngữ', meta: 'PDF · 32 trang', readMinutes: 18, rating: 4.4, tone: 'green' },
    ],
  },
  {
    id: 4,
    name: 'Kỹ năng mềm cho sinh viên',
    icon: 'target',
    accent: 'amber',
    docCount: 9,
    progress: 30,
    owner: 'other',
    shared: true,
    pinned: false,
    contributors: [
      { name: 'Đỗ Hà', accent: 'violet' },
      { name: 'Bùi Khoa', accent: 'green' },
    ],
    docs: [
      { title: 'Thuyết trình chuyên nghiệp', category: 'Kỹ năng', meta: 'Slide · 20 trang', readMinutes: 10, rating: 4.8, coins: 8, tone: 'cyan' },
      { title: 'Tư duy phản biện', category: 'Kỹ năng', meta: 'PDF · 44 trang', readMinutes: 20, rating: 4.9, coins: 30, tone: 'magenta' },
    ],
  },
]);

const activeTab = ref('all');
const expandedId = ref<number | null>(null);
const showModal = ref(false);

// Modal form state
const formName = ref('');
const formIcon = ref<string>('folder');
const formAccent = ref<Accent>('cyan');
const formPrivate = ref(false);

let nextId = 5;

// ─── Derived ──────────────────────────────────────────────────────────────────

const tabs = computed(() => [
  { id: 'all', label: 'Tất cả', badge: collections.value.length },
  { id: 'mine', label: 'Của tôi', badge: collections.value.filter((c) => c.owner === 'me').length },
  { id: 'pinned', label: 'Đã ghim', badge: collections.value.filter((c) => c.pinned).length },
  { id: 'shared', label: 'Được chia sẻ', badge: collections.value.filter((c) => c.shared).length },
]);

const filteredCollections = computed<Collection[]>(() => {
  switch (activeTab.value) {
    case 'mine':
      return collections.value.filter((c) => c.owner === 'me');
    case 'pinned':
      return collections.value.filter((c) => c.pinned);
    case 'shared':
      return collections.value.filter((c) => c.shared);
    default:
      return collections.value;
  }
});

const totalDocs = computed(() => collections.value.reduce((sum, c) => sum + c.docCount, 0));
const avgProgress = computed(() => {
  if (collections.value.length === 0) return 0;
  return Math.round(
    collections.value.reduce((sum, c) => sum + c.progress, 0) / collections.value.length,
  );
});
const sharedCount = computed(() => collections.value.filter((c) => c.shared).length);
const canCreate = computed(() => formName.value.trim().length > 0);

// ─── Handlers ─────────────────────────────────────────────────────────────────

function toggleExpand(id: number): void {
  expandedId.value = expandedId.value === id ? null : id;
}

function togglePin(id: number): void {
  const col = collections.value.find((c) => c.id === id);
  if (col) col.pinned = !col.pinned;
}

function openModal(): void {
  formName.value = '';
  formIcon.value = 'folder';
  formAccent.value = 'cyan';
  formPrivate.value = false;
  showModal.value = true;
}

function closeModal(): void {
  showModal.value = false;
}

function createCollection(): void {
  if (!canCreate.value) return;
  collections.value.unshift({
    id: nextId++,
    name: formName.value.trim(),
    icon: formIcon.value,
    accent: formAccent.value,
    docCount: 0,
    progress: 0,
    owner: 'me',
    shared: !formPrivate.value,
    pinned: false,
    contributors: [{ name: 'Bạn', accent: formAccent.value }],
    docs: [],
  });
  showModal.value = false;
}

onUnmounted(() => {
  showModal.value = false;
  expandedId.value = null;
});
</script>

<template>
  <div class="space-y-6">
    <!-- ── Page header ─────────────────────────────────────────────────────── -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="cosmic-badge">Thư viện cá nhân</p>
        <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
          Bộ <span class="cosmic-gradient-text">sưu tập</span>
        </h1>
        <p class="mt-1 text-sm text-muted-foreground">
          Nhóm tài liệu theo chủ đề, theo dõi tiến độ và chia sẻ cùng bạn học.
        </p>
      </div>
      <button
        type="button"
        class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-xl px-4 py-2 text-sm"
        @click="openModal"
      >
        <AppIcon name="plus" class="h-4 w-4" />
        Tạo bộ sưu tập
      </button>
    </div>

    <!-- ── KPI row ─────────────────────────────────────────────────────────── -->
    <section class="grid grid-cols-2 gap-4 md:grid-cols-4">
      <StatPanel label="Bộ sưu tập" :value="String(collections.length)" icon="folder" accent="cyan" caption="Đang hoạt động" />
      <StatPanel label="Tổng tài liệu" :value="String(totalDocs)" unit="tài liệu" icon="file-text" accent="violet" caption="Trên mọi bộ sưu tập" />
      <StatPanel label="Tiến độ TB" :value="String(avgProgress)" unit="%" icon="chart" accent="green" :trend="6" caption="Tăng đều mỗi tuần" />
      <StatPanel label="Được chia sẻ" :value="String(sharedCount)" unit="công khai" icon="users" accent="amber" caption="Cộng tác cùng bạn bè" />
    </section>

    <!-- ── Filter tabs ─────────────────────────────────────────────────────── -->
    <Tabs :tabs="tabs" :model-value="activeTab" @update:model-value="activeTab = $event" />

    <!-- ── Collections grid ────────────────────────────────────────────────── -->
    <div v-if="filteredCollections.length > 0" class="grid grid-cols-1 gap-5 lg:grid-cols-2">
      <div v-for="col in filteredCollections" :key="col.id" class="space-y-0">
        <article
          class="group overflow-hidden rounded-2xl border border-white/[0.06] bg-white/[0.02] transition-all hover:-translate-y-1 hover:border-white/[0.14]"
          :class="col.pinned ? 'ring-1 ring-amber-400/30' : ''"
        >
          <!-- Gradient cover -->
          <div class="relative h-28 bg-gradient-to-br" :class="cover[col.accent]">
            <div
              class="absolute inset-0 opacity-50"
              style="background: radial-gradient(circle at 25% 20%, rgba(255,255,255,0.28), transparent 55%)"
            />
            <span class="absolute left-4 top-4 flex h-11 w-11 items-center justify-center rounded-xl backdrop-blur" :class="iconChip[col.accent]">
              <AppIcon :name="col.icon" class="h-6 w-6" />
            </span>
            <!-- Share / private indicator -->
            <span
              class="absolute right-14 top-4 inline-flex items-center gap-1 rounded-full bg-black/30 px-2 py-1 text-[0.68rem] font-medium text-white/90 backdrop-blur"
            >
              <AppIcon :name="col.shared ? 'globe' : 'lock'" class="h-3 w-3" />
              {{ col.shared ? 'Chia sẻ' : 'Riêng tư' }}
            </span>
            <!-- Pin toggle -->
            <button
              type="button"
              class="absolute right-3 top-3 flex h-8 w-8 items-center justify-center rounded-lg bg-black/30 text-white/80 backdrop-blur transition-colors hover:text-amber-300"
              :class="col.pinned ? 'text-amber-300' : ''"
              :aria-label="col.pinned ? 'Bỏ ghim bộ sưu tập' : 'Ghim bộ sưu tập'"
              @click="togglePin(col.id)"
            >
              <AppIcon name="bookmark" class="h-4 w-4" />
            </button>
          </div>

          <!-- Body -->
          <div class="p-4">
            <div class="flex items-start justify-between gap-2">
              <div class="min-w-0">
                <h3 class="truncate text-base font-semibold text-foreground">{{ col.name }}</h3>
                <p class="mt-0.5 flex items-center gap-1.5 text-xs text-muted-foreground">
                  <AppIcon name="file-text" class="h-3.5 w-3.5" /> {{ col.docCount }} tài liệu
                  <span class="text-muted-foreground/40">·</span>
                  {{ col.owner === 'me' ? 'Của tôi' : 'Được chia sẻ với bạn' }}
                </p>
              </div>
            </div>

            <!-- Progress -->
            <div class="mt-3">
              <div class="mb-1 flex items-center justify-between text-xs">
                <span class="text-muted-foreground">Tiến độ hoàn thành</span>
                <span class="tabular-nums font-medium text-foreground">{{ col.progress }}%</span>
              </div>
              <ProgressBar :value="col.progress" :accent="col.accent" :height="6" />
            </div>

            <!-- Footer: contributors + expand -->
            <div class="mt-4 flex items-center justify-between gap-3">
              <div class="flex items-center">
                <span
                  v-for="(person, idx) in col.contributors.slice(0, 4)"
                  :key="person.name"
                  class="-ml-2 rounded-full ring-2 ring-[#0c0c16] first:ml-0"
                  :style="{ zIndex: String(4 - idx) }"
                >
                  <Avatar :name="person.name" :accent="person.accent" :size="28" />
                </span>
                <span
                  v-if="col.contributors.length > 4"
                  class="-ml-2 flex h-7 w-7 items-center justify-center rounded-full bg-white/10 text-[0.65rem] font-semibold text-foreground ring-2 ring-[#0c0c16]"
                >
                  +{{ col.contributors.length - 4 }}
                </span>
              </div>

              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-lg border border-white/10 bg-white/[0.03] px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
                @click="toggleExpand(col.id)"
              >
                {{ expandedId === col.id ? 'Thu gọn' : 'Xem tài liệu' }}
                <AppIcon :name="expandedId === col.id ? 'chevron-down' : 'chevron-right'" class="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        </article>

        <!-- Expand panel -->
        <Transition name="expand">
          <div
            v-if="expandedId === col.id"
            class="mt-3 rounded-2xl border border-white/[0.06] bg-white/[0.01] p-4"
          >
            <p class="mb-3 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <AppIcon name="layers" class="h-4 w-4" /> Tài liệu trong bộ sưu tập
            </p>
            <div v-if="col.docs.length > 0" class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <DocumentCard
                v-for="doc in col.docs"
                :key="doc.title"
                :title="doc.title"
                :category="doc.category"
                :meta="doc.meta"
                :read-minutes="doc.readMinutes"
                :rating="doc.rating"
                :coins="doc.coins"
                :tone="doc.tone"
              />
            </div>
            <div v-else class="flex flex-col items-center justify-center py-8 text-center">
              <AppIcon name="inbox" class="h-7 w-7 text-muted-foreground/50" />
              <p class="mt-2 text-sm text-muted-foreground">Bộ sưu tập này chưa có tài liệu nào.</p>
            </div>
          </div>
        </Transition>
      </div>
    </div>

    <!-- ── Empty state ─────────────────────────────────────────────────────── -->
    <div
      v-else
      class="flex flex-col items-center justify-center rounded-2xl border border-dashed border-white/[0.08] bg-white/[0.01] py-16 text-center"
    >
      <span class="flex h-16 w-16 items-center justify-center rounded-2xl border border-white/[0.08] bg-white/[0.03] text-muted-foreground/60">
        <AppIcon name="folder" class="h-8 w-8" />
      </span>
      <p class="mt-4 text-base font-semibold text-foreground">Chưa có bộ sưu tập</p>
      <p class="mt-1.5 max-w-xs text-sm text-muted-foreground">
        Không có bộ sưu tập nào trong mục này. Tạo một bộ sưu tập mới để bắt đầu sắp xếp tài liệu.
      </p>
      <button type="button" class="cosmic-btn mt-5 inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm" @click="openModal">
        <AppIcon name="plus" class="h-4 w-4" />
        Tạo bộ sưu tập
      </button>
    </div>

    <!-- ── Create modal ────────────────────────────────────────────────────── -->
    <Transition name="modal">
      <div v-if="showModal" class="fixed inset-0 z-40 flex items-center justify-center p-4">
        <button
          type="button"
          class="absolute inset-0 bg-black/60 backdrop-blur-sm"
          aria-label="Đóng cửa sổ"
          @click="closeModal"
        />
        <div class="modal-panel glass-panel relative z-10 w-full max-w-md overflow-hidden rounded-2xl">
          <header class="flex items-center justify-between gap-3 border-b border-white/[0.06] px-5 py-4">
            <div class="flex items-center gap-2.5">
              <span class="flex h-9 w-9 items-center justify-center rounded-xl bg-cyan-500/10 text-cyan-300">
                <AppIcon name="plus" class="h-5 w-5" />
              </span>
              <p class="text-sm font-semibold text-foreground">Tạo bộ sưu tập mới</p>
            </div>
            <button
              type="button"
              class="flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-white/[0.06] hover:text-foreground"
              aria-label="Đóng"
              @click="closeModal"
            >
              <AppIcon name="x" class="h-5 w-5" />
            </button>
          </header>

          <div class="space-y-5 p-5">
            <!-- Name -->
            <div>
              <label class="mb-1.5 block text-xs font-medium text-muted-foreground">Tên bộ sưu tập</label>
              <input
                v-model="formName"
                type="text"
                placeholder="VD: Ôn thi giữa kỳ"
                class="cosmic-input"
                @keydown.enter="createCollection"
              />
            </div>

            <!-- Icon picker -->
            <div>
              <label class="mb-2 block text-xs font-medium text-muted-foreground">Biểu tượng</label>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="ic in iconChoices"
                  :key="ic"
                  type="button"
                  class="flex h-10 w-10 items-center justify-center rounded-xl border transition-all"
                  :class="
                    formIcon === ic
                      ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
                      : 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-white/20 hover:text-foreground'
                  "
                  :aria-label="`Chọn biểu tượng ${ic}`"
                  @click="formIcon = ic"
                >
                  <AppIcon :name="ic" class="h-5 w-5" />
                </button>
              </div>
            </div>

            <!-- Accent picker -->
            <div>
              <label class="mb-2 block text-xs font-medium text-muted-foreground">Màu nhấn</label>
              <div class="flex flex-wrap gap-2.5">
                <button
                  v-for="ac in accentChoices"
                  :key="ac"
                  type="button"
                  class="flex h-9 w-9 items-center justify-center rounded-xl border transition-all"
                  :class="
                    formAccent === ac ? 'scale-110 border-white/40' : 'border-white/[0.08] hover:border-white/20'
                  "
                  :aria-label="`Chọn màu ${ac}`"
                  @click="formAccent = ac"
                >
                  <span class="h-5 w-5 rounded-full" :class="accentDot[ac]" />
                </button>
              </div>
            </div>

            <!-- Privacy toggle -->
            <div class="flex items-center justify-between rounded-xl border border-white/[0.06] bg-white/[0.02] px-4 py-3">
              <div class="flex items-center gap-2.5">
                <AppIcon :name="formPrivate ? 'lock' : 'globe'" class="h-4 w-4 text-muted-foreground" />
                <div>
                  <p class="text-sm font-medium text-foreground">{{ formPrivate ? 'Riêng tư' : 'Chia sẻ công khai' }}</p>
                  <p class="text-xs text-muted-foreground">
                    {{ formPrivate ? 'Chỉ mình bạn xem được' : 'Bạn học có thể xem và cộng tác' }}
                  </p>
                </div>
              </div>
              <Toggle :model-value="formPrivate" @update:model-value="formPrivate = $event" />
            </div>
          </div>

          <footer class="flex items-center justify-end gap-2 border-t border-white/[0.06] px-5 py-4">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-2 text-sm text-muted-foreground hover:text-foreground"
              @click="closeModal"
            >
              Huỷ
            </button>
            <button
              type="button"
              class="cosmic-btn inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm"
              :disabled="!canCreate"
              @click="createCollection"
            >
              <AppIcon name="check" class="h-4 w-4" />
              Tạo bộ sưu tập
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

/* Expand transition */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  max-height: 0;
}
.expand-enter-to,
.expand-leave-from {
  max-height: 800px;
}
</style>
