<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import DocumentCard from '@/components/app/DocumentCard.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import Avatar from '@/components/app/Avatar.vue';
import Tabs from '@/components/app/Tabs.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Tone = Accent;
type TabId = 'docs' | 'collections' | 'missions' | 'authors';

const { push } = useToast();

interface FavDoc {
  id: number;
  title: string;
  category: string;
  meta: string;
  readMinutes: number;
  coins: number;
  rating: number;
  tone: Tone;
  locked: boolean;
}
interface FavCollection {
  id: number;
  title: string;
  count: number;
  progress: number;
  icon: string;
  accent: Accent;
}
interface FavMission {
  id: number;
  title: string;
  reward: number;
  current: number;
  total: number;
  accent: Accent;
}
interface FavAuthor {
  id: number;
  name: string;
  field: string;
  followers: string;
  accent: Accent;
}

const favDocs = ref<FavDoc[]>([
  {
    id: 1,
    title: 'Làm chủ đạo hàm từ cơ bản đến nâng cao',
    category: 'Toán',
    meta: 'Nguyễn Minh Quân · 12.4k đọc',
    readMinutes: 22,
    coins: 40,
    rating: 4.8,
    tone: 'cyan',
    locked: false
  },
  {
    id: 2,
    title: 'Chiến lược TOEIC 800+ trong 60 ngày',
    category: 'Ngoại ngữ',
    meta: 'Lê Thu Hà · 9.1k đọc',
    readMinutes: 18,
    coins: 35,
    rating: 4.9,
    tone: 'magenta',
    locked: false
  },
  {
    id: 3,
    title: 'Thuật toán & cấu trúc dữ liệu với Python',
    category: 'Lập trình',
    meta: 'Trần Đức Anh · 15.7k đọc',
    readMinutes: 30,
    coins: 55,
    rating: 4.7,
    tone: 'green',
    locked: true
  },
  {
    id: 4,
    title: 'Kỹ thuật ghi nhớ Feynman',
    category: 'Kỹ năng',
    meta: 'Đỗ Khánh Linh · 11.2k đọc',
    readMinutes: 16,
    coins: 38,
    rating: 4.9,
    tone: 'amber',
    locked: false
  },
  {
    id: 5,
    title: 'Nhập môn cơ học lượng tử',
    category: 'Khoa học',
    meta: 'Phạm Hải Yến · 6.3k đọc',
    readMinutes: 26,
    coins: 45,
    rating: 4.6,
    tone: 'violet',
    locked: false
  }
]);
const favCollections = ref<FavCollection[]>([
  {
    id: 1,
    title: 'Hành trang mùa thi 2026',
    count: 24,
    progress: 62,
    icon: 'rocket',
    accent: 'cyan'
  },
  {
    id: 2,
    title: 'Tiếng Anh giao tiếp',
    count: 18,
    progress: 40,
    icon: 'globe',
    accent: 'magenta'
  },
  { id: 3, title: 'Nền tảng lập trình', count: 32, progress: 75, icon: 'command', accent: 'green' }
]);
const favMissions = ref<FavMission[]>([
  {
    id: 1,
    title: 'Đọc 5 tài liệu Toán trong tuần',
    reward: 120,
    current: 3,
    total: 5,
    accent: 'cyan'
  },
  {
    id: 2,
    title: 'Hoàn thành 3 bài quiz Ngoại ngữ',
    reward: 90,
    current: 1,
    total: 3,
    accent: 'magenta'
  },
  { id: 3, title: 'Giữ chuỗi học 7 ngày', reward: 200, current: 6, total: 7, accent: 'amber' }
]);
const favAuthors = ref<FavAuthor[]>([
  { id: 1, name: 'Nguyễn Minh Quân', field: 'Toán cao cấp', followers: '18.4k', accent: 'cyan' },
  { id: 2, name: 'Lê Thu Hà', field: 'Tiếng Anh học thuật', followers: '22.1k', accent: 'magenta' },
  { id: 3, name: 'Trần Đức Anh', field: 'Khoa học máy tính', followers: '31.5k', accent: 'green' }
]);

const tabs = computed(() => [
  { id: 'docs', label: 'Tài liệu', badge: favDocs.value.length },
  { id: 'collections', label: 'Bộ sưu tập', badge: favCollections.value.length },
  { id: 'missions', label: 'Nhiệm vụ', badge: favMissions.value.length },
  { id: 'authors', label: 'Tác giả', badge: favAuthors.value.length }
]);
const activeTab = ref<TabId>('docs');
const query = ref<string>('');
const viewMode = ref<'grid' | 'list'>('grid');

const totalFavorites = computed<number>(
  () =>
    favDocs.value.length +
    favCollections.value.length +
    favMissions.value.length +
    favAuthors.value.length
);

function match(text: string): boolean {
  const q = query.value.trim().toLowerCase();
  return q === '' || text.toLowerCase().includes(q);
}
const shownDocs = computed<FavDoc[]>(() =>
  favDocs.value.filter((d) => match(d.title + d.category))
);
const shownCollections = computed<FavCollection[]>(() =>
  favCollections.value.filter((c) => match(c.title))
);
const shownMissions = computed<FavMission[]>(() => favMissions.value.filter((m) => match(m.title)));
const shownAuthors = computed<FavAuthor[]>(() =>
  favAuthors.value.filter((a) => match(a.name + a.field))
);

function removeDoc(id: number): void {
  favDocs.value = favDocs.value.filter((d) => d.id !== id);
  push('Đã bỏ tài liệu khỏi yêu thích', { icon: 'heart', accent: 'rose' });
}
function removeCollection(id: number): void {
  favCollections.value = favCollections.value.filter((c) => c.id !== id);
  push('Đã bỏ bộ sưu tập khỏi yêu thích', { icon: 'heart', accent: 'rose' });
}
function removeMission(id: number): void {
  favMissions.value = favMissions.value.filter((m) => m.id !== id);
  push('Đã bỏ nhiệm vụ khỏi yêu thích', { icon: 'heart', accent: 'rose' });
}
function removeAuthor(id: number): void {
  favAuthors.value = favAuthors.value.filter((a) => a.id !== id);
  push('Đã bỏ theo dõi tác giả', { icon: 'heart', accent: 'rose' });
}

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
const cover: Record<Accent, string> = {
  cyan: 'from-cyan-500/40 to-blue-700/30',
  magenta: 'from-fuchsia-500/40 to-purple-700/30',
  green: 'from-emerald-500/40 to-teal-700/30',
  violet: 'from-violet-500/40 to-indigo-700/30',
  amber: 'from-amber-400/40 to-orange-600/30'
};

const recommended: { id: number; title: string; reason: string; icon: string; accent: Accent }[] = [
  {
    id: 1,
    title: 'Lượng giác nâng cao',
    reason: 'Giống tài liệu Toán bạn đã lưu',
    icon: 'chart',
    accent: 'cyan'
  },
  {
    id: 2,
    title: 'Speaking IELTS 7.0+',
    reason: 'Tiếp nối bộ sưu tập Tiếng Anh',
    icon: 'globe',
    accent: 'magenta'
  },
  {
    id: 3,
    title: 'Đệ quy & quay lui',
    reason: 'Phù hợp lộ trình lập trình',
    icon: 'command',
    accent: 'green'
  }
];
</script>

<template>
  <div class="space-y-6">
    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10 flex items-center gap-4">
        <span
          class="fx-float flex h-14 w-14 items-center justify-center rounded-sm from-rose-500/30 to-fuchsia-500/20 text-rose-300"
        >
          <AppIcon name="heart" class="h-7 w-7 fill-current" />
        </span>
        <div>
          <p class="cosmic-badge">Bộ sưu tập của bạn</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            <CountUp :value="totalFavorites" /> mục
            <span class="cosmic-gradient-text">yêu thích</span>
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Tất cả tài liệu, bộ sưu tập, nhiệm vụ và tác giả bạn đã lưu.
          </p>
        </div>
      </div>
    </GlassPanel>

    <!-- TABS + TOOLBAR -->
    <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <Tabs
        :tabs="tabs"
        :model-value="activeTab"
        @update:model-value="(id) => (activeTab = id as TabId)"
      />
      <div class="flex items-center gap-2">
        <div class="relative flex-1 lg:w-64">
          <span
            class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
          >
            <AppIcon name="search" class="h-4 w-4" />
          </span>
          <input
            v-model="query"
            type="text"
            placeholder="Tìm trong yêu thích..."
            class="cosmic-input w-full !py-2 !pl-9 text-sm"
          />
        </div>
        <div
          v-if="activeTab === 'docs'"
          class="inline-flex shrink-0 items-center gap-1 rounded-sm border border-white/[0.06] bg-white/[0.03] p-1"
        >
          <button
            type="button"
            class="rounded-sm p-1.5 transition-colors"
            :class="
              viewMode === 'grid'
                ? 'bg-cyan-500/15 text-cyan-200'
                : 'text-muted-foreground hover:text-foreground'
            "
            aria-label="Dạng lưới"
            @click="viewMode = 'grid'"
          >
            <AppIcon name="grid" class="h-4 w-4" />
          </button>
          <button
            type="button"
            class="rounded-sm p-1.5 transition-colors"
            :class="
              viewMode === 'list'
                ? 'bg-cyan-500/15 text-cyan-200'
                : 'text-muted-foreground hover:text-foreground'
            "
            aria-label="Dạng danh sách"
            @click="viewMode = 'list'"
          >
            <AppIcon name="list" class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- DOCS -->
    <template v-if="activeTab === 'docs'">
      <transition-group
        v-if="shownDocs.length && viewMode === 'grid'"
        name="fav"
        tag="div"
        class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3"
      >
        <div v-for="doc in shownDocs" :key="doc.id" class="relative">
          <DocumentCard
            :title="doc.title"
            :category="doc.category"
            :meta="doc.meta"
            :read-minutes="doc.readMinutes"
            :coins="doc.coins"
            :rating="doc.rating"
            :locked="doc.locked"
            bookmarked
            :tone="doc.tone"
          />
          <button
            type="button"
            class="absolute right-2.5 top-2.5 z-10 flex h-7 w-7 items-center justify-center rounded-sm bg-black/40 text-rose-300 backdrop-blur transition-colors hover:bg-rose-500/30"
            aria-label="Bỏ yêu thích"
            @click="removeDoc(doc.id)"
          >
            <AppIcon name="heart" class="h-4 w-4 fill-current" />
          </button>
        </div>
      </transition-group>

      <transition-group v-else-if="shownDocs.length" name="fav" tag="ul" class="space-y-2.5">
        <li
          v-for="doc in shownDocs"
          :key="doc.id"
          class="fx-lift flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-3"
        >
          <span
            class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm
            :class="cover[doc.tone]"
          >
            <AppIcon name="file-text" class="h-5 w-5 text-white/90" />
          </span>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-semibold text-foreground">{{ doc.title }}</p>
            <p class="truncate text-xs text-muted-foreground">
              {{ doc.category }} · {{ doc.meta }}
            </p>
          </div>
          <span class="hidden items-center gap-1 text-xs text-amber-300 sm:inline-flex"
            ><AppIcon name="star" class="h-3.5 w-3.5" /> {{ doc.rating }}</span
          >
          <button
            type="button"
            class="shrink-0 rounded-sm p-2 text-rose-300 transition-colors hover:bg-rose-500/15"
            aria-label="Bỏ yêu thích"
            @click="removeDoc(doc.id)"
          >
            <AppIcon name="heart" class="h-4 w-4 fill-current" />
          </button>
        </li>
      </transition-group>
    </template>

    <!-- COLLECTIONS -->
    <transition-group
      v-else-if="activeTab === 'collections' && shownCollections.length"
      name="fav"
      tag="div"
      class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3"
    >
      <GlassPanel v-for="c in shownCollections" :key="c.id" hover class="fx-sheen overflow-hidden">
        <div class="relative h-20" :class="cover[c.accent]">
          <span
            class="absolute left-3 top-3 flex h-10 w-10 items-center justify-center rounded-sm bg-black/30 text-white backdrop-blur"
          >
            <AppIcon :name="c.icon" class="h-5 w-5" />
          </span>
          <button
            type="button"
            class="absolute right-2.5 top-2.5 flex h-7 w-7 items-center justify-center rounded-sm bg-black/40 text-rose-300 backdrop-blur transition-colors hover:bg-rose-500/30"
            aria-label="Bỏ yêu thích"
            @click="removeCollection(c.id)"
          >
            <AppIcon name="heart" class="h-4 w-4 fill-current" />
          </button>
        </div>
        <div class="p-4">
          <p class="text-sm font-semibold text-foreground">{{ c.title }}</p>
          <p class="mt-0.5 text-xs text-muted-foreground">{{ c.count }} tài liệu</p>
          <div class="mt-3">
            <ProgressBar :value="c.progress" :accent="c.accent" />
            <p class="mt-1 text-[0.7rem] text-muted-foreground">{{ c.progress }}% hoàn thành</p>
          </div>
        </div>
      </GlassPanel>
    </transition-group>

    <!-- MISSIONS -->
    <transition-group
      v-else-if="activeTab === 'missions' && shownMissions.length"
      name="fav"
      tag="ul"
      class="space-y-2.5"
    >
      <li
        v-for="m in shownMissions"
        :key="m.id"
        class="fx-lift flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3.5 py-3"
      >
        <span
          class="flex h-10 w-10 shrink-0 items-center justify-center rounded-sm"
          :class="accentChip[m.accent]"
        >
          <AppIcon name="target" class="h-5 w-5" />
        </span>
        <div class="min-w-0 flex-1">
          <div class="flex items-center justify-between gap-2">
            <p class="truncate text-sm font-semibold text-foreground">{{ m.title }}</p>
            <span
              class="inline-flex shrink-0 items-center gap-0.5 rounded-sm bg-amber-500/10 px-2 py-0.5 text-xs font-semibold text-amber-300"
              ><AppIcon name="coin" class="h-3 w-3" /> +{{ m.reward }}</span
            >
          </div>
          <div class="mt-2 flex items-center gap-2">
            <ProgressBar
              :value="Math.round((m.current / m.total) * 100)"
              :accent="m.accent"
              class="flex-1"
            />
            <span class="shrink-0 text-[0.7rem] tabular-nums text-muted-foreground"
              >{{ m.current }}/{{ m.total }}</span
            >
          </div>
        </div>
        <button
          type="button"
          class="shrink-0 rounded-sm p-2 text-rose-300 transition-colors hover:bg-rose-500/15"
          aria-label="Bỏ yêu thích"
          @click="removeMission(m.id)"
        >
          <AppIcon name="heart" class="h-4 w-4 fill-current" />
        </button>
      </li>
    </transition-group>

    <!-- AUTHORS -->
    <transition-group
      v-else-if="activeTab === 'authors' && shownAuthors.length"
      name="fav"
      tag="ul"
      class="grid grid-cols-1 gap-3 sm:grid-cols-2"
    >
      <li
        v-for="a in shownAuthors"
        :key="a.id"
        class="fx-lift flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3.5 py-3"
      >
        <Avatar :name="a.name" :accent="a.accent" :size="44" />
        <div class="min-w-0 flex-1">
          <p class="truncate text-sm font-semibold text-foreground">{{ a.name }}</p>
          <p class="truncate text-xs text-muted-foreground">
            {{ a.field }} · {{ a.followers }} theo dõi
          </p>
        </div>
        <button
          type="button"
          class="shrink-0 rounded-sm border border-white/10 px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-rose-400/40 hover:text-rose-300"
          @click="removeAuthor(a.id)"
        >
          Bỏ theo dõi
        </button>
      </li>
    </transition-group>

    <!-- EMPTY STATE -->
    <div
      v-if="
        (activeTab === 'docs' && !shownDocs.length) ||
        (activeTab === 'collections' && !shownCollections.length) ||
        (activeTab === 'missions' && !shownMissions.length) ||
        (activeTab === 'authors' && !shownAuthors.length)
      "
      class="rounded-sm border border-dashed border-white/[0.1] py-12 text-center"
    >
      <span
        class="mx-auto flex h-14 w-14 items-center justify-center rounded-sm bg-white/[0.04] text-muted-foreground"
      >
        <AppIcon name="heart" class="h-7 w-7" />
      </span>
      <p class="mt-3 text-sm font-medium text-foreground">Chưa có mục nào ở đây</p>
      <p class="mt-1 text-xs text-muted-foreground">
        Nhấn vào biểu tượng trái tim trên tài liệu để lưu vào yêu thích.
      </p>
      <RouterLink
        to="/app/explore"
        class="cosmic-btn mt-4 inline-flex items-center gap-2 rounded-sm px-4 py-2 text-sm"
      >
        <AppIcon name="compass" class="h-4 w-4" /> Khám phá tài liệu
      </RouterLink>
    </div>

    <!-- RECOMMENDED -->
    <section class="rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 sm:p-5">
      <h2 class="flex items-center gap-2 text-sm font-semibold text-foreground">
        <AppIcon name="sparkles" class="h-4 w-4 text-cyan-300" /> Có thể bạn cũng thích
      </h2>
      <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div
          v-for="r in recommended"
          :key="r.id"
          class="fx-lift flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] p-3"
        >
          <span
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-sm"
            :class="accentChip[r.accent]"
          >
            <AppIcon :name="r.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0">
            <p class="truncate text-sm font-semibold text-foreground">{{ r.title }}</p>
            <p class="truncate text-xs text-muted-foreground">{{ r.reason }}</p>
          </div>
        </div>
      </div>
    </section>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.fav-enter-active,
.fav-leave-active {
  transition:
    opacity 0.35s ease,
    transform 0.35s ease;
}
.fav-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fav-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
.fav-leave-active {
  position: absolute;
}
@media (prefers-reduced-motion: reduce) {
  .fav-enter-active,
  .fav-leave-active {
    transition: none;
  }
}
</style>
