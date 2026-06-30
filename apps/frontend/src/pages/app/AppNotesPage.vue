<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import StatPanel from '@/components/app/StatPanel.vue';

// ─── Types ────────────────────────────────────────────────────────────────────

type NoteColor = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type SortMode = 'recent' | 'pinned' | 'color';

interface Note {
  id: number;
  title: string;
  content: string;
  tag: string;
  color: NoteColor;
  updated: string;
  pinned: boolean;
}

interface ColorSwatch {
  id: NoteColor;
  label: string;
}

// ─── Static config ────────────────────────────────────────────────────────────

const swatches: ColorSwatch[] = [
  { id: 'cyan', label: 'Xanh ngọc' },
  { id: 'violet', label: 'Tím' },
  { id: 'green', label: 'Lục' },
  { id: 'amber', label: 'Hổ phách' },
  { id: 'magenta', label: 'Hồng' },
];

const swatchDot: Record<NoteColor, string> = {
  cyan: 'bg-cyan-400',
  magenta: 'bg-fuchsia-400',
  green: 'bg-emerald-400',
  violet: 'bg-violet-400',
  amber: 'bg-amber-300',
};

const accentStrip: Record<NoteColor, string> = {
  cyan: ' from-cyan-400 to-cyan-600',
  magenta: ' from-fuchsia-400 to-fuchsia-600',
  green: ' from-emerald-400 to-emerald-600',
  violet: ' from-violet-400 to-violet-600',
  amber: ' from-amber-300 to-amber-500',
};

const cardTint: Record<NoteColor, string> = {
  cyan: 'from-cyan-500/[0.07]',
  magenta: 'from-fuchsia-500/[0.07]',
  green: 'from-emerald-500/[0.07]',
  violet: 'from-violet-500/[0.08]',
  amber: 'from-amber-500/[0.07]',
};

const tagChipColor: Record<NoteColor, string> = {
  cyan: 'bg-cyan-500/10 text-cyan-200',
  magenta: 'bg-fuchsia-500/10 text-fuchsia-200',
  green: 'bg-emerald-500/10 text-emerald-200',
  violet: 'bg-violet-500/10 text-violet-200',
  amber: 'bg-amber-500/10 text-amber-200',
};

// ─── State ────────────────────────────────────────────────────────────────────

const notes = ref<Note[]>([
  {
    id: 1,
    title: 'Công thức đạo hàm cơ bản',
    content:
      'Đạo hàm của x^n là n·x^(n-1). Quy tắc tích, quy tắc thương và quy tắc chuỗi cần nhớ kỹ trước khi làm bài tập.',
    tag: 'Toán học',
    color: 'cyan',
    updated: 'Hôm nay',
    pinned: true,
  },
  {
    id: 2,
    title: 'Mốc thời gian Lịch sử VN',
    content:
      '1945: Cách mạng Tháng Tám. 1954: Chiến thắng Điện Biên Phủ. 1975: Thống nhất đất nước. Ôn theo trục thời gian.',
    tag: 'Lịch sử',
    color: 'amber',
    updated: 'Hôm qua',
    pinned: true,
  },
  {
    id: 3,
    title: 'Từ vựng IELTS chủ đề Môi trường',
    content:
      'sustainability, carbon footprint, renewable energy, deforestation. Học theo cụm và đặt câu ví dụ.',
    tag: 'Ngoại ngữ',
    color: 'green',
    updated: '2 ngày trước',
    pinned: false,
  },
  {
    id: 4,
    title: 'Độ phức tạp thuật toán',
    content:
      'O(1) hằng số, O(log n) nhị phân, O(n) tuyến tính, O(n log n) sắp xếp hiệu quả, O(n^2) lồng nhau.',
    tag: 'Tin học',
    color: 'violet',
    updated: '3 ngày trước',
    pinned: false,
  },
  {
    id: 5,
    title: 'Phản ứng oxi hoá khử',
    content:
      'Chất khử nhường electron (bị oxi hoá), chất oxi hoá nhận electron (bị khử). Cân bằng theo phương pháp thăng bằng electron.',
    tag: 'Hoá học',
    color: 'magenta',
    updated: '4 ngày trước',
    pinned: false,
  },
  {
    id: 6,
    title: 'Cấu trúc bài thuyết trình',
    content:
      'Mở bài thu hút, thân bài 3 luận điểm chính, kết bài kêu gọi hành động. Luyện trước gương 3 lần.',
    tag: 'Kỹ năng',
    color: 'cyan',
    updated: '5 ngày trước',
    pinned: false,
  },
]);

const searchQuery = ref('');
const activeTag = ref('all');
const sortMode = ref<SortMode>('recent');
const editingId = ref<number | null>(null);

let nextId = 7;

// ─── Derived ──────────────────────────────────────────────────────────────────

const availableTags = computed<string[]>(() => {
  const set = new Set<string>();
  notes.value.forEach((n) => set.add(n.tag));
  return Array.from(set);
});

const filteredNotes = computed<Note[]>(() => {
  const q = searchQuery.value.trim().toLowerCase();
  let list = notes.value.filter((n) => {
    const matchesQuery =
      q === '' || n.title.toLowerCase().includes(q) || n.content.toLowerCase().includes(q);
    const matchesTag = activeTag.value === 'all' || n.tag === activeTag.value;
    return matchesQuery && matchesTag;
  });

  list = [...list];
  if (sortMode.value === 'pinned') {
    list.sort((a, b) => Number(b.pinned) - Number(a.pinned));
  } else if (sortMode.value === 'color') {
    const order: NoteColor[] = ['cyan', 'violet', 'green', 'amber', 'magenta'];
    list.sort((a, b) => order.indexOf(a.color) - order.indexOf(b.color));
  } else {
    list.sort((a, b) => Number(b.pinned) - Number(a.pinned));
  }
  return list;
});

const pinnedCount = computed(() => notes.value.filter((n) => n.pinned).length);
const editingNote = computed<Note | undefined>(() =>
  notes.value.find((n) => n.id === editingId.value),
);

const sortOptions: { id: SortMode; label: string }[] = [
  { id: 'recent', label: 'Mới nhất' },
  { id: 'pinned', label: 'Đã ghim' },
  { id: 'color', label: 'Theo màu' },
];

// ─── Handlers ─────────────────────────────────────────────────────────────────

function createNote(): void {
  const note: Note = {
    id: nextId++,
    title: 'Ghi chú mới',
    content: '',
    tag: 'Chung',
    color: 'cyan',
    updated: 'Vừa xong',
    pinned: false,
  };
  notes.value.unshift(note);
  editingId.value = note.id;
}

function openEditor(id: number): void {
  editingId.value = id;
}

function closeEditor(): void {
  editingId.value = null;
}

function togglePin(id: number): void {
  const note = notes.value.find((n) => n.id === id);
  if (note) note.pinned = !note.pinned;
}

function deleteNote(id: number): void {
  notes.value = notes.value.filter((n) => n.id !== id);
  if (editingId.value === id) editingId.value = null;
}

function setColor(color: NoteColor): void {
  if (editingNote.value) editingNote.value.color = color;
}

function resetFilters(): void {
  searchQuery.value = '';
  activeTag.value = 'all';
}

// Close editor on unmount cleanup (no live timers, but keep contract consistent).
onUnmounted(() => {
  editingId.value = null;
});
</script>

<template>
  <div class="space-y-6">
    <!-- ── Page header ─────────────────────────────────────────────────────── -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="cosmic-badge">Sổ tay học tập</p>
        <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
          Ghi <span class="cosmic-gradient-text">chú</span>
        </h1>
        <p class="mt-1 text-sm text-muted-foreground">
          Lưu lại ý tưởng, công thức và mẹo ôn tập — tất cả ở một nơi.
        </p>
      </div>
      <button
        type="button"
        class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-sm px-4 py-2 text-sm"
        @click="createNote"
      >
        <AppIcon name="plus" class="h-4 w-4" />
        Ghi chú mới
      </button>
    </div>

    <!-- ── KPI row ─────────────────────────────────────────────────────────── -->
    <section class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <StatPanel
        label="Tổng ghi chú"
        :value="String(notes.length)"
        unit="ghi chú"
        icon="edit"
        accent="cyan"
        caption="Đồng bộ trên mọi thiết bị"
      />
      <StatPanel
        label="Đã ghim"
        :value="String(pinnedCount)"
        unit="quan trọng"
        icon="bookmark"
        accent="amber"
        caption="Luôn hiển thị trên cùng"
      />
      <StatPanel
        label="Thẻ"
        :value="String(availableTags.length)"
        unit="chủ đề"
        icon="badge"
        accent="violet"
        caption="Phân loại theo môn học"
      />
    </section>

    <!-- ── Search + sort ───────────────────────────────────────────────────── -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
      <div class="relative flex-1">
        <AppIcon
          name="search"
          class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground/60"
        />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Tìm theo tiêu đề hoặc nội dung..."
          class="h-11 w-full rounded-sm border border-white/[0.08] bg-white/[0.03] pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground/70 focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-400/20"
        />
      </div>

      <div class="flex shrink-0 gap-1 rounded-sm border border-white/[0.08] bg-white/[0.03] p-1">
        <button
          v-for="opt in sortOptions"
          :key="opt.id"
          type="button"
          class="rounded-sm px-3 py-1.5 text-sm font-medium transition-colors"
          :class="
            sortMode === opt.id
              ? 'bg-cyan-500/15 text-cyan-200'
              : 'text-muted-foreground hover:text-foreground'
          "
          @click="sortMode = opt.id"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <!-- ── Tag filter chips ────────────────────────────────────────────────── -->
    <div class="flex flex-wrap gap-2">
      <button
        type="button"
        class="rounded-sm border px-3 py-1 text-xs font-medium transition-colors"
        :class="
          activeTag === 'all'
            ? 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200'
            : 'border-white/[0.06] bg-white/[0.04] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
        "
        @click="activeTag = 'all'"
      >
        Tất cả
      </button>
      <button
        v-for="tag in availableTags"
        :key="tag"
        type="button"
        class="rounded-sm border px-3 py-1 text-xs font-medium transition-colors"
        :class="
          activeTag === tag
            ? 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200'
            : 'border-white/[0.06] bg-white/[0.04] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
        "
        @click="activeTag = tag"
      >
        {{ tag }}
      </button>
    </div>

    <!-- ── Notes grid ──────────────────────────────────────────────────────── -->
    <TransitionGroup
      v-if="filteredNotes.length > 0"
      tag="div"
      name="note"
      class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3"
    >
      <article
        v-for="note in filteredNotes"
        :key="note.id"
        class="note-card group relative flex cursor-pointer flex-col overflow-hidden rounded-sm border to-transparent p-4 transition-all hover:-translate-y-1"
        :class="[
          cardTint[note.color],
          note.pinned
            ? 'border-amber-400/30'
            : 'border-white/[0.06] hover:border-white/[0.14]',
        ]"
        @click="openEditor(note.id)"
      >
        <!-- Accent strip -->
        <span class="absolute inset-y-0 left-0 w-1" :class="accentStrip[note.color]" />

        <div class="flex items-start justify-between gap-2 pl-2">
          <h3 class="line-clamp-2 text-sm font-semibold leading-snug text-foreground">
            {{ note.title }}
          </h3>
          <button
            type="button"
            class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm transition-colors"
            :class="note.pinned ? 'text-amber-300' : 'text-muted-foreground/60 hover:text-amber-300'"
            :aria-label="note.pinned ? 'Bỏ ghim' : 'Ghim ghi chú'"
            @click.stop="togglePin(note.id)"
          >
            <AppIcon name="bookmark" class="h-4 w-4" />
          </button>
        </div>

        <p class="mt-2 line-clamp-4 pl-2 text-xs leading-relaxed text-muted-foreground">
          {{ note.content || 'Chưa có nội dung. Nhấn để chỉnh sửa.' }}
        </p>

        <div class="mt-auto flex items-center justify-between gap-2 pl-2 pt-3">
          <span
            class="inline-flex items-center gap-1 rounded-sm px-2 py-0.5 text-[0.68rem] font-medium"
            :class="tagChipColor[note.color]"
          >
            <AppIcon name="badge" class="h-3 w-3" /> {{ note.tag }}
          </span>
          <span class="flex items-center gap-1 text-[0.68rem] text-muted-foreground/60">
            <AppIcon name="clock" class="h-3 w-3" /> {{ note.updated }}
          </span>
        </div>
      </article>
    </TransitionGroup>

    <!-- ── Empty state ─────────────────────────────────────────────────────── -->
    <div
      v-else
      class="flex flex-col items-center justify-center rounded-sm border border-dashed border-white/[0.08] bg-white/[0.01] py-16 text-center"
    >
      <span class="flex h-16 w-16 items-center justify-center rounded-sm border border-white/[0.08] bg-white/[0.03] text-muted-foreground/60">
        <AppIcon name="inbox" class="h-8 w-8" />
      </span>
      <p class="mt-4 text-base font-semibold text-foreground">Không tìm thấy ghi chú</p>
      <p class="mt-1.5 max-w-xs text-sm text-muted-foreground">
        Không có ghi chú nào khớp với bộ lọc hiện tại. Thử từ khoá khác hoặc tạo ghi chú mới.
      </p>
      <div class="mt-5 flex flex-wrap justify-center gap-3">
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2 text-sm text-muted-foreground hover:border-cyan-400/40 hover:text-cyan-200"
          @click="resetFilters"
        >
          <AppIcon name="refresh" class="h-4 w-4" />
          Đặt lại bộ lọc
        </button>
        <button type="button" class="cosmic-btn inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm" @click="createNote">
          <AppIcon name="plus" class="h-4 w-4" />
          Ghi chú mới
        </button>
      </div>
    </div>

    <!-- ── Editor drawer ───────────────────────────────────────────────────── -->
    <Transition name="drawer">
      <div v-if="editingNote" class="fixed inset-0 z-40 flex justify-end">
        <button
          type="button"
          class="absolute inset-0 bg-black/60 backdrop-blur-sm"
          aria-label="Đóng trình chỉnh sửa"
          @click="closeEditor"
        />
        <div class="drawer-panel relative z-10 flex h-full w-full max-w-md flex-col overflow-hidden border-l border-white/[0.08] bg-[#0c0c16]">
          <header class="flex items-center justify-between gap-3 border-b border-white/[0.06] px-5 py-4">
            <div class="flex items-center gap-2.5">
              <span class="h-3 w-3 rounded-sm" :class="swatchDot[editingNote.color]" />
              <p class="text-sm font-semibold text-foreground">Chỉnh sửa ghi chú</p>
            </div>
            <button
              type="button"
              class="flex h-8 w-8 items-center justify-center rounded-sm text-muted-foreground hover:bg-white/[0.06] hover:text-foreground"
              aria-label="Đóng"
              @click="closeEditor"
            >
              <AppIcon name="x" class="h-5 w-5" />
            </button>
          </header>

          <div class="cosmic-scroll flex-1 space-y-5 overflow-y-auto p-5">
            <!-- Title -->
            <div>
              <label class="mb-1.5 block text-xs font-medium text-muted-foreground">Tiêu đề</label>
              <input
                v-model="editingNote.title"
                type="text"
                placeholder="Nhập tiêu đề..."
                class="cosmic-input"
              />
            </div>

            <!-- Content -->
            <div>
              <label class="mb-1.5 block text-xs font-medium text-muted-foreground">Nội dung</label>
              <textarea
                v-model="editingNote.content"
                rows="8"
                placeholder="Viết ghi chú của bạn..."
                class="cosmic-input resize-none"
              />
            </div>

            <!-- Tag -->
            <div>
              <label class="mb-1.5 block text-xs font-medium text-muted-foreground">Thẻ</label>
              <input
                v-model="editingNote.tag"
                type="text"
                placeholder="VD: Toán học"
                class="cosmic-input"
              />
            </div>

            <!-- Color picker -->
            <div>
              <label class="mb-2 block text-xs font-medium text-muted-foreground">Màu nhãn</label>
              <div class="flex flex-wrap gap-2.5">
                <button
                  v-for="sw in swatches"
                  :key="sw.id"
                  type="button"
                  class="flex h-9 w-9 items-center justify-center rounded-sm border transition-all"
                  :class="
                    editingNote.color === sw.id
                      ? 'scale-110 border-white/40'
                      : 'border-white/[0.08] hover:border-white/20'
                  "
                  :aria-label="`Chọn màu ${sw.label}`"
                  @click="setColor(sw.id)"
                >
                  <span class="h-5 w-5 rounded-sm" :class="swatchDot[sw.id]" />
                </button>
              </div>
            </div>
          </div>

          <!-- Footer actions -->
          <footer class="flex items-center justify-between gap-2 border-t border-white/[0.06] px-5 py-4">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-3.5 py-2 text-sm text-muted-foreground hover:border-rose-400/40 hover:text-rose-200"
              @click="deleteNote(editingNote.id)"
            >
              <AppIcon name="trash" class="h-4 w-4" />
              Xoá
            </button>
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-3.5 py-2 text-sm"
                :class="editingNote.pinned ? 'text-amber-300' : 'text-muted-foreground hover:text-amber-300'"
                @click="togglePin(editingNote.id)"
              >
                <AppIcon name="bookmark" class="h-4 w-4" />
                {{ editingNote.pinned ? 'Đã ghim' : 'Ghim' }}
              </button>
              <button type="button" class="cosmic-btn inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm" @click="closeEditor">
                <AppIcon name="check" class="h-4 w-4" />
                Xong
              </button>
            </div>
          </footer>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* TransitionGroup enter/leave + reorder */
.note-enter-active,
.note-leave-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}
.note-enter-from,
.note-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(8px);
}
.note-leave-active {
  position: absolute;
}
.note-move {
  transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

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
</style>
