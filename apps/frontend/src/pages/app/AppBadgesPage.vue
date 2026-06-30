<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import BadgeTile from '@/components/app/BadgeTile.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Rarity = 'common' | 'rare' | 'epic' | 'legendary';

const { push } = useToast();

interface Badge {
  id: number;
  name: string;
  icon: string;
  description: string;
  unlocked: boolean;
  rarity: Rarity;
  category: string;
  requirement: string;
  holdersPct: number;
  progress?: number;
}

const categories: { id: string; label: string; icon: string }[] = [
  { id: 'all', label: 'Tất cả', icon: 'grid' },
  { id: 'learning', label: 'Học tập', icon: 'book-open' },
  { id: 'streak', label: 'Chuỗi học', icon: 'flame' },
  { id: 'social', label: 'Cộng đồng', icon: 'users' },
  { id: 'mastery', label: 'Tinh thông', icon: 'crown' },
  { id: 'special', label: 'Đặc biệt', icon: 'sparkles' }
];
const rarities: { id: string; label: string }[] = [
  { id: 'all', label: 'Mọi độ hiếm' },
  { id: 'common', label: 'Thường' },
  { id: 'rare', label: 'Hiếm' },
  { id: 'epic', label: 'Sử thi' },
  { id: 'legendary', label: 'Huyền thoại' }
];
const statusFilters: { id: string; label: string }[] = [
  { id: 'all', label: 'Tất cả' },
  { id: 'unlocked', label: 'Đã mở' },
  { id: 'locked', label: 'Chưa mở' }
];

const activeCategory = ref<string>('all');
const activeRarity = ref<string>('all');
const activeStatus = ref<string>('all');

const badges: Badge[] = [
  {
    id: 1,
    name: 'Bước đầu tiên',
    icon: 'rocket',
    description: 'Hoàn thành tài liệu đầu tiên của bạn.',
    unlocked: true,
    rarity: 'common',
    category: 'learning',
    requirement: 'Đọc xong 1 tài liệu',
    holdersPct: 94
  },
  {
    id: 2,
    name: 'Mọt sách',
    icon: 'book-open',
    description: 'Đọc 50 tài liệu trên Study Hub.',
    unlocked: true,
    rarity: 'rare',
    category: 'learning',
    requirement: 'Đọc 50 tài liệu',
    holdersPct: 38
  },
  {
    id: 3,
    name: 'Ngọn lửa bất diệt',
    icon: 'flame',
    description: 'Duy trì chuỗi học 30 ngày liên tục.',
    unlocked: true,
    rarity: 'epic',
    category: 'streak',
    requirement: 'Chuỗi 30 ngày',
    holdersPct: 12
  },
  {
    id: 4,
    name: 'Học giả huyền thoại',
    icon: 'crown',
    description: 'Đạt cấp độ 50 — đỉnh cao tri thức.',
    unlocked: false,
    rarity: 'legendary',
    category: 'mastery',
    requirement: 'Đạt cấp 50',
    holdersPct: 2,
    progress: 48
  },
  {
    id: 5,
    name: 'Người dẫn đường',
    icon: 'users',
    description: 'Mời 10 bạn cùng tham gia học tập.',
    unlocked: false,
    rarity: 'rare',
    category: 'social',
    requirement: 'Mời 10 bạn',
    holdersPct: 21,
    progress: 60
  },
  {
    id: 6,
    name: 'Bậc thầy Toán học',
    icon: 'chart',
    description: 'Hoàn thành toàn bộ lộ trình Toán cao cấp.',
    unlocked: false,
    rarity: 'epic',
    category: 'mastery',
    requirement: 'Hoàn thành lộ trình Toán',
    holdersPct: 7,
    progress: 35
  },
  {
    id: 7,
    name: 'Cú đêm',
    icon: 'moon',
    description: 'Học sau 0 giờ trong 10 ngày.',
    unlocked: true,
    rarity: 'rare',
    category: 'special',
    requirement: 'Học khuya 10 lần',
    holdersPct: 28
  },
  {
    id: 8,
    name: 'Tia chớp',
    icon: 'zap',
    description: 'Hoàn thành 1 bài quiz dưới 60 giây với điểm tuyệt đối.',
    unlocked: false,
    rarity: 'epic',
    category: 'special',
    requirement: 'Quiz hoàn hảo < 60s',
    holdersPct: 9,
    progress: 0
  },
  {
    id: 9,
    name: 'Nhà vô địch tuần',
    icon: 'trophy',
    description: 'Đứng đầu bảng xếp hạng tuần.',
    unlocked: false,
    rarity: 'legendary',
    category: 'social',
    requirement: 'Top 1 tuần',
    holdersPct: 1,
    progress: 20
  },
  {
    id: 10,
    name: 'Khởi động',
    icon: 'check-circle',
    description: 'Hoàn thành nhiệm vụ hằng ngày đầu tiên.',
    unlocked: true,
    rarity: 'common',
    category: 'learning',
    requirement: 'Hoàn thành 1 nhiệm vụ',
    holdersPct: 88
  },
  {
    id: 11,
    name: 'Thủ thư',
    icon: 'folder',
    description: 'Tạo 5 bộ sưu tập tài liệu.',
    unlocked: true,
    rarity: 'common',
    category: 'learning',
    requirement: 'Tạo 5 bộ sưu tập',
    holdersPct: 44
  },
  {
    id: 12,
    name: 'Người truyền cảm hứng',
    icon: 'heart',
    description: 'Nhận 100 lượt thích trên bảng tin.',
    unlocked: false,
    rarity: 'rare',
    category: 'social',
    requirement: 'Nhận 100 lượt thích',
    holdersPct: 16,
    progress: 72
  }
];

const unlockedCount = computed<number>(() => badges.filter((b) => b.unlocked).length);
const totalCount = badges.length;
const completion = computed<number>(() => Math.round((unlockedCount.value / totalCount) * 100));

const rarityCounts = computed<{ rarity: Rarity; label: string; count: number; total: number }[]>(
  () => {
    const defs: { rarity: Rarity; label: string }[] = [
      { rarity: 'common', label: 'Thường' },
      { rarity: 'rare', label: 'Hiếm' },
      { rarity: 'epic', label: 'Sử thi' },
      { rarity: 'legendary', label: 'Huyền thoại' }
    ];
    return defs.map((d) => ({
      ...d,
      count: badges.filter((b) => b.rarity === d.rarity && b.unlocked).length,
      total: badges.filter((b) => b.rarity === d.rarity).length
    }));
  }
);

const filtered = computed<Badge[]>(() =>
  badges
    .filter((b) => activeCategory.value === 'all' || b.category === activeCategory.value)
    .filter((b) => activeRarity.value === 'all' || b.rarity === activeRarity.value)
    .filter(
      (b) =>
        activeStatus.value === 'all' ||
        (activeStatus.value === 'unlocked' ? b.unlocked : !b.unlocked)
    )
);

const selected = ref<Badge | null>(null);
function openBadge(b: Badge): void {
  selected.value = b;
}
function shareBadge(): void {
  push('Đã sao chép liên kết khoe huy hiệu', { icon: 'share', accent: 'violet' });
}

const rarityLabel: Record<Rarity, string> = {
  common: 'Thường',
  rare: 'Hiếm',
  epic: 'Sử thi',
  legendary: 'Huyền thoại'
};
const rarityRing: Record<Rarity, string> = {
  common: 'from-slate-400/30 to-slate-500/20 text-slate-200',
  rare: 'from-cyan-400/40 to-blue-500/30 text-cyan-200',
  epic: 'from-fuchsia-400/40 to-purple-500/30 text-fuchsia-200',
  legendary: 'from-amber-300/50 to-orange-500/30 text-amber-200'
};
const rarityText: Record<Rarity, string> = {
  common: 'text-slate-300',
  rare: 'text-cyan-300',
  epic: 'text-fuchsia-300',
  legendary: 'text-amber-300'
};
</script>

<template>
  <div class="space-y-6">
    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div
        class="relative z-10 flex flex-col items-center gap-5 sm:flex-row sm:items-center sm:justify-between"
      >
        <div>
          <p class="cosmic-badge">
            <AppIcon name="award" class="h-3.5 w-3.5" /> Bộ sưu tập huy hiệu
          </p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            Vinh danh <span class="cosmic-gradient-text">hành trình</span> của bạn
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Bạn đã mở khoá
            <span class="font-semibold text-cyan-300"><CountUp :value="unlockedCount" /></span> /
            {{ totalCount }} huy hiệu.
          </p>
          <div class="mt-4 flex flex-wrap gap-2">
            <span
              v-for="r in rarityCounts"
              :key="r.rarity"
              class="inline-flex items-center gap-1.5 rounded-sm border border-white/[0.07] bg-white/[0.02] px-3 py-1 text-xs"
            >
              <span class="h-2 w-2 rounded-sm" :class="rarityRing[r.rarity]" />
              <span :class="rarityText[r.rarity]">{{ r.label }}</span>
              <span class="text-muted-foreground">{{ r.count }}/{{ r.total }}</span>
            </span>
          </div>
        </div>
        <RingProgress :value="completion" :size="120" :stroke="11" accent="amber">
          <span class="text-2xl font-bold text-foreground"
            ><CountUp :value="completion" suffix="%"
          /></span>
          <span class="text-[0.7rem] text-muted-foreground">hoàn thành</span>
        </RingProgress>
      </div>
    </GlassPanel>

    <!-- FILTERS -->
    <div class="space-y-3">
      <div class="cosmic-scroll -mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="inline-flex shrink-0 items-center gap-1.5 rounded-sm border px-3.5 py-2 text-sm font-medium transition-all"
          :class="
            activeCategory === cat.id
              ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
              : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
          "
          @click="activeCategory = cat.id"
        >
          <AppIcon :name="cat.icon" class="h-4 w-4" /> {{ cat.label }}
        </button>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <div
          class="inline-flex items-center gap-1 rounded-sm border border-white/[0.06] bg-white/[0.03] p-1"
        >
          <button
            v-for="r in rarities"
            :key="r.id"
            type="button"
            class="rounded-sm px-3 py-1.5 text-xs font-medium transition-colors"
            :class="
              activeRarity === r.id
                ? 'bg-fuchsia-500/15 text-fuchsia-200'
                : 'text-muted-foreground hover:text-foreground'
            "
            @click="activeRarity = r.id"
          >
            {{ r.label }}
          </button>
        </div>
        <div
          class="inline-flex items-center gap-1 rounded-sm border border-white/[0.06] bg-white/[0.03] p-1"
        >
          <button
            v-for="s in statusFilters"
            :key="s.id"
            type="button"
            class="rounded-sm px-3 py-1.5 text-xs font-medium transition-colors"
            :class="
              activeStatus === s.id
                ? 'bg-cyan-500/15 text-cyan-200'
                : 'text-muted-foreground hover:text-foreground'
            "
            @click="activeStatus = s.id"
          >
            {{ s.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- GRID -->
    <transition-group
      name="badge"
      tag="div"
      class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6"
    >
      <button
        v-for="b in filtered"
        :key="b.id"
        type="button"
        class="text-left"
        @click="openBadge(b)"
      >
        <BadgeTile
          :name="b.name"
          :icon="b.icon"
          :description="b.description"
          :unlocked="b.unlocked"
          :rarity="b.rarity"
          :progress="b.progress"
        />
      </button>
    </transition-group>

    <p
      v-if="filtered.length === 0"
      class="rounded-sm border border-dashed border-white/[0.1] py-10 text-center text-sm text-muted-foreground"
    >
      Không có huy hiệu nào khớp bộ lọc.
    </p>

    <!-- NEXT TO UNLOCK -->
    <SectionPanel
      title="Sắp mở khoá"
      subtitle="Những huy hiệu bạn đang tiến gần nhất"
      icon="target"
      accent="cyan"
    >
      <ul class="space-y-3">
        <li
          v-for="b in badges
            .filter((x) => !x.unlocked && (x.progress ?? 0) > 0)
            .sort((a, c) => (c.progress ?? 0) - (a.progress ?? 0))
            .slice(0, 3)"
          :key="b.id"
          class="flex items-center gap-3"
        >
          <span
            class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm
            :class="rarityRing[b.rarity]"
          >
            <AppIcon :name="b.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-semibold text-foreground">{{ b.name }}</p>
            <div class="mt-1.5 h-2 w-full overflow-hidden rounded-sm bg-white/[0.06]">
              <div
                class="h-full rounded-sm from-cyan-400 to-fuchsia-400"
                :style="{ width: `${b.progress ?? 0}%` }"
              />
            </div>
          </div>
          <span class="shrink-0 text-sm font-semibold text-cyan-300 tabular-nums"
            >{{ b.progress ?? 0 }}%</span
          >
        </li>
      </ul>
    </SectionPanel>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>

    <!-- DETAIL MODAL -->
    <transition name="modal">
      <div v-if="selected" class="fixed inset-0 z-[110] flex items-center justify-center p-4 !mt-0">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="selected = null" />
        <GlassPanel
          :glow="
            selected.rarity === 'legendary'
              ? 'amber'
              : selected.rarity === 'epic'
                ? 'magenta'
                : 'cyan'
          "
          class="relative z-10 w-full max-w-sm p-6 text-center"
        >
          <button
            type="button"
            class="absolute right-4 top-4 rounded-sm p-1.5 text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground"
            aria-label="Đóng"
            @click="selected = null"
          >
            <AppIcon name="x" class="h-5 w-5" />
          </button>
          <span
            class="float-slow mx-auto flex h-24 w-24 items-center justify-center rounded-sm border border-zinc-800 bg-zinc-950"
            :class="
              selected.unlocked
                ? rarityRing[selected.rarity]
                : 'text-muted-foreground'
            "
          >
            <AppIcon :name="selected.unlocked ? selected.icon : 'lock'" class="h-12 w-12" />
          </span>
          <p
            class="mt-4 inline-block rounded-sm px-3 py-1 text-xs font-semibold"
            :class="[rarityText[selected.rarity], 'bg-white/[0.06]']"
          >
            {{ rarityLabel[selected.rarity] }}
          </p>
          <h3 class="mt-2 text-xl font-bold text-foreground">{{ selected.name }}</h3>
          <p class="mt-1.5 text-sm text-muted-foreground">{{ selected.description }}</p>

          <div
            class="mt-4 space-y-2 rounded-sm border border-white/[0.06] bg-white/[0.02] p-3 text-left text-sm"
          >
            <div class="flex items-center justify-between">
              <span class="text-muted-foreground">Điều kiện</span>
              <span class="font-medium text-foreground">{{ selected.requirement }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-muted-foreground">Tỉ lệ sở hữu</span>
              <span class="font-medium text-foreground">{{ selected.holdersPct }}% người học</span>
            </div>
          </div>

          <div v-if="!selected.unlocked && selected.progress !== undefined" class="mt-3">
            <div class="h-2.5 w-full overflow-hidden rounded-sm bg-white/[0.06]">
              <div
                class="h-full rounded-sm from-cyan-400 to-fuchsia-400"
                :style="{ width: `${selected.progress}%` }"
              />
            </div>
            <p class="mt-1 text-xs text-muted-foreground">{{ selected.progress }}% tiến độ</p>
          </div>

          <button
            v-if="selected.unlocked"
            type="button"
            class="cosmic-btn mt-5 w-full rounded-sm px-4 py-2.5 text-sm"
            @click="shareBadge"
          >
            <AppIcon name="share" class="mr-1.5 inline h-4 w-4" /> Khoe huy hiệu
          </button>
          <p v-else class="mt-5 inline-flex items-center gap-1.5 text-xs text-muted-foreground">
            <AppIcon name="lock" class="h-4 w-4" /> Tiếp tục học để mở khoá
          </p>
        </GlassPanel>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.badge-enter-active,
.badge-leave-active {
  transition:
    opacity 0.35s ease,
    transform 0.35s ease;
}
.badge-enter-from {
  opacity: 0;
  transform: scale(0.9);
}
.badge-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
.badge-leave-active {
  position: absolute;
}
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
@media (prefers-reduced-motion: reduce) {
  .badge-enter-active,
  .badge-leave-active,
  .modal-enter-active,
  .modal-leave-active {
    transition: none;
  }
}
</style>
