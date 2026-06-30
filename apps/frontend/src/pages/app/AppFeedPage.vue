<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Avatar from '@/components/app/Avatar.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type FeedKind = 'status' | 'achievement' | 'document' | 'mission' | 'streak' | 'level';
type FilterId = 'all' | 'friends' | 'groups' | 'docs';

const { push } = useToast();

interface Embed {
  icon: string;
  title: string;
  subtitle: string;
  accent: Accent;
  meta?: string;
}

interface FeedItem {
  id: number;
  kind: FeedKind;
  author: string;
  authorAccent: Accent;
  handle: string;
  time: string;
  source: Exclude<FilterId, 'all'>;
  text: string;
  embed?: Embed;
  likes: number;
  comments: number;
  reposts: number;
  liked: boolean;
  reposted: boolean;
}

// ─── Filters ─────────────────────────────────────────────────────────────────
const filters: { id: FilterId; label: string; icon: string }[] = [
  { id: 'all', label: 'Tất cả', icon: 'list' },
  { id: 'friends', label: 'Bạn bè', icon: 'users' },
  { id: 'groups', label: 'Nhóm học', icon: 'message' },
  { id: 'docs', label: 'Tài liệu', icon: 'file-text' }
];
const activeFilter = ref<FilterId>('all');

// ─── Composer ────────────────────────────────────────────────────────────────
const draft = ref<string>('');
const composerMoods: { id: string; label: string; icon: string }[] = [
  { id: 'study', label: 'Đang học', icon: 'book-open' },
  { id: 'win', label: 'Khoe thành tích', icon: 'trophy' },
  { id: 'ask', label: 'Đặt câu hỏi', icon: 'help' }
];

let seq = 100;

function postDraft(): void {
  const text = draft.value.trim();
  if (!text) return;
  seq += 1;
  items.value.unshift({
    id: seq,
    kind: 'status',
    author: 'Bạn',
    authorAccent: 'cyan',
    handle: '@ban',
    time: 'Vừa xong',
    source: 'friends',
    text,
    likes: 0,
    comments: 0,
    reposts: 0,
    liked: false,
    reposted: false
  });
  draft.value = '';
  push('Đã đăng lên bảng tin của bạn', { icon: 'send', accent: 'cyan' });
}

function applyMood(label: string): void {
  draft.value = draft.value ? draft.value : `${label}: `;
}

// ─── Feed data ───────────────────────────────────────────────────────────────
const items = ref<FeedItem[]>([
  {
    id: 1,
    kind: 'achievement',
    author: 'Lê Thu Hà',
    authorAccent: 'magenta',
    handle: '@thuha',
    time: '8 phút trước',
    source: 'friends',
    text: 'Vừa mở khoá huy hiệu mới sau 30 ngày học liên tục! 🔥',
    embed: {
      icon: 'flame',
      title: 'Ngọn lửa bất diệt',
      subtitle: 'Huy hiệu Sử thi · Chuỗi 30 ngày',
      accent: 'amber'
    },
    likes: 48,
    comments: 12,
    reposts: 4,
    liked: false,
    reposted: false
  },
  {
    id: 2,
    kind: 'document',
    author: 'Trần Đức Anh',
    authorAccent: 'green',
    handle: '@ducanh',
    time: '24 phút trước',
    source: 'docs',
    text: 'Mình vừa chia sẻ bộ tài liệu thuật toán quy hoạch động, anh em vào lưu nhé!',
    embed: {
      icon: 'file-text',
      title: 'Quy hoạch động từ A-Z',
      subtitle: 'Lập trình · 32 phút đọc',
      accent: 'green',
      meta: '+55 coin'
    },
    likes: 126,
    comments: 31,
    reposts: 18,
    liked: true,
    reposted: false
  },
  {
    id: 3,
    kind: 'mission',
    author: 'Nhóm Toán cao cấp',
    authorAccent: 'cyan',
    handle: '@toancc',
    time: '1 giờ trước',
    source: 'groups',
    text: 'Thử thách tuần đã hoàn thành bởi 18 thành viên! Cùng nhau chinh phục mốc tiếp theo nào.',
    embed: {
      icon: 'target',
      title: 'Giải 50 bài tích phân',
      subtitle: 'Thử thách nhóm · 18/20 thành viên',
      accent: 'cyan'
    },
    likes: 73,
    comments: 9,
    reposts: 2,
    liked: false,
    reposted: false
  },
  {
    id: 4,
    kind: 'level',
    author: 'Phạm Hải Yến',
    authorAccent: 'violet',
    handle: '@haiyen',
    time: '2 giờ trước',
    source: 'friends',
    text: 'Lên cấp 24 rồi mọi người ơi! Cảm ơn nhóm Vật lý đã đồng hành 💜',
    embed: {
      icon: 'rocket',
      title: 'Cấp độ 24 — Nhà thám hiểm',
      subtitle: '12.480 / 13.000 XP',
      accent: 'violet'
    },
    likes: 95,
    comments: 22,
    reposts: 6,
    liked: false,
    reposted: false
  },
  {
    id: 5,
    kind: 'streak',
    author: 'Đỗ Khánh Linh',
    authorAccent: 'amber',
    handle: '@khanhlinh',
    time: '4 giờ trước',
    source: 'friends',
    text: 'Chuỗi học của mình đã chạm mốc 64 ngày. Ai muốn lập nhóm giữ streak cùng không? 😎',
    likes: 61,
    comments: 14,
    reposts: 1,
    liked: false,
    reposted: false
  }
]);

const filteredItems = computed<FeedItem[]>(() =>
  activeFilter.value === 'all'
    ? items.value
    : items.value.filter((i) => i.source === activeFilter.value)
);

const kindMeta: Record<FeedKind, { label: string; icon: string; accent: Accent }> = {
  status: { label: 'Trạng thái', icon: 'edit', accent: 'cyan' },
  achievement: { label: 'Thành tựu', icon: 'award', accent: 'amber' },
  document: { label: 'Tài liệu', icon: 'file-text', accent: 'green' },
  mission: { label: 'Nhiệm vụ', icon: 'target', accent: 'cyan' },
  streak: { label: 'Chuỗi học', icon: 'flame', accent: 'amber' },
  level: { label: 'Lên cấp', icon: 'rocket', accent: 'violet' }
};

function toggleLike(item: FeedItem): void {
  item.liked = !item.liked;
  item.likes += item.liked ? 1 : -1;
}

function toggleRepost(item: FeedItem): void {
  item.reposted = !item.reposted;
  item.reposts += item.reposted ? 1 : -1;
  if (item.reposted)
    push('Đã chia sẻ lại lên bảng tin của bạn', { icon: 'repeat', accent: 'green' });
}

function share(): void {
  push('Đã sao chép liên kết bài viết', { icon: 'link', accent: 'violet' });
}

// ─── Side rail ───────────────────────────────────────────────────────────────
interface OnlineFriend {
  id: number;
  name: string;
  status: string;
  accent: Accent;
}
const onlineFriends: OnlineFriend[] = [
  { id: 1, name: 'Nguyễn Minh Quân', status: 'Đang học Toán', accent: 'cyan' },
  { id: 2, name: 'Lê Thu Hà', status: 'Phòng tập trung', accent: 'magenta' },
  { id: 3, name: 'Vũ Quốc Bảo', status: 'Đang làm quiz', accent: 'green' },
  { id: 4, name: 'Phạm Hải Yến', status: 'Vừa online', accent: 'violet' }
];

interface Suggestion {
  id: number;
  name: string;
  field: string;
  accent: Accent;
  followed: boolean;
}
const suggestions = ref<Suggestion[]>([
  {
    id: 1,
    name: 'Hoàng Anh Tú',
    field: 'Hoá học · 12k theo dõi',
    accent: 'green',
    followed: false
  },
  {
    id: 2,
    name: 'Mai Phương Thảo',
    field: 'IELTS · 28k theo dõi',
    accent: 'magenta',
    followed: false
  },
  { id: 3, name: 'Lý Gia Hân', field: 'Sinh học · 9k theo dõi', accent: 'cyan', followed: false }
]);
function toggleFollow(s: Suggestion): void {
  s.followed = !s.followed;
  if (s.followed) push(`Đã theo dõi ${s.name}`, { icon: 'user', accent: s.accent });
}

const trendingTags: string[] = ['#KỳThiTHPT', '#TOEIC800', '#ThuậtToán', '#Pomodoro', '#GhiNhớSâu'];

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};

const stats = [
  { label: 'Bài viết hôm nay', value: 248, icon: 'edit', accent: 'cyan' as Accent },
  { label: 'Lượt tương tác', value: 5120, icon: 'heart', accent: 'magenta' as Accent },
  { label: 'Bạn đang online', value: 36, icon: 'users', accent: 'green' as Accent }
];
</script>

<template>
  <div class="space-y-6">
    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10">
        <p class="cosmic-badge">Bảng tin cộng đồng</p>
        <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
          Dòng chảy <span class="cosmic-gradient-text">học tập</span> của bạn
        </h1>
        <p class="mt-1 text-sm text-muted-foreground">
          Cập nhật hoạt động từ bạn bè, nhóm học và những tài liệu bạn theo dõi.
        </p>
      </div>
    </GlassPanel>

    <!-- STATS -->
    <section class="fx-stagger grid grid-cols-3 gap-3 sm:gap-4">
      <GlassPanel v-for="s in stats" :key="s.label" :glow="s.accent" hover padded>
        <div class="flex items-center gap-3">
          <span
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-sm"
            :class="accentChip[s.accent]"
          >
            <AppIcon :name="s.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0">
            <p class="text-lg font-bold text-foreground sm:text-xl">
              <CountUp :value="s.value" />
            </p>
            <p class="truncate text-[0.7rem] text-muted-foreground">{{ s.label }}</p>
          </div>
        </div>
      </GlassPanel>
    </section>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- MAIN COLUMN -->
      <div class="space-y-5 lg:col-span-2">
        <!-- Composer -->
        <GlassPanel glow="cyan" class="p-4 sm:p-5">
          <div class="flex gap-3">
            <Avatar name="Bạn" accent="cyan" :size="44" />
            <div class="min-w-0 flex-1">
              <textarea
                v-model="draft"
                rows="2"
                placeholder="Chia sẻ điều bạn vừa học được hôm nay..."
                class="cosmic-input w-full resize-none"
              />
              <div class="mt-3 flex flex-wrap items-center justify-between gap-2">
                <div class="flex flex-wrap gap-1.5">
                  <button
                    v-for="m in composerMoods"
                    :key="m.id"
                    type="button"
                    class="inline-flex items-center gap-1.5 rounded-sm border border-white/[0.08] bg-white/[0.03] px-2.5 py-1.5 text-xs text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
                    @click="applyMood(m.label)"
                  >
                    <AppIcon :name="m.icon" class="h-3.5 w-3.5" /> {{ m.label }}
                  </button>
                </div>
                <button
                  type="button"
                  class="cosmic-btn inline-flex items-center gap-1.5 rounded-sm px-4 py-2 text-sm disabled:opacity-50"
                  :disabled="!draft.trim()"
                  @click="postDraft"
                >
                  <AppIcon name="send" class="h-4 w-4" /> Đăng
                </button>
              </div>
            </div>
          </div>
        </GlassPanel>

        <!-- Filters -->
        <div class="cosmic-scroll -mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
          <button
            v-for="f in filters"
            :key="f.id"
            type="button"
            class="inline-flex shrink-0 items-center gap-1.5 rounded-sm border px-3.5 py-2 text-sm font-medium transition-all"
            :class="
              activeFilter === f.id
                ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
                : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
            "
            @click="activeFilter = f.id"
          >
            <AppIcon :name="f.icon" class="h-4 w-4" /> {{ f.label }}
          </button>
        </div>

        <!-- Feed -->
        <transition-group name="feed" tag="div" class="space-y-4">
          <GlassPanel
            v-for="item in filteredItems"
            :key="item.id"
            hover
            class="fx-sheen p-4 sm:p-5"
          >
            <div class="flex items-start gap-3">
              <Avatar :name="item.author" :accent="item.authorAccent" :size="44" />
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-x-2 gap-y-0.5">
                  <span class="text-sm font-semibold text-foreground">{{ item.author }}</span>
                  <span class="text-xs text-muted-foreground">{{ item.handle }}</span>
                  <span class="text-xs text-muted-foreground">· {{ item.time }}</span>
                </div>
                <span
                  class="mt-1 inline-flex items-center gap-1 rounded-sm px-2 py-0.5 text-[0.65rem] font-medium"
                  :class="accentChip[kindMeta[item.kind].accent]"
                >
                  <AppIcon :name="kindMeta[item.kind].icon" class="h-3 w-3" />
                  {{ kindMeta[item.kind].label }}
                </span>
              </div>
              <button
                type="button"
                class="shrink-0 rounded-sm p-1.5 text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground"
                aria-label="Tuỳ chọn"
              >
                <AppIcon name="more" class="h-4 w-4" />
              </button>
            </div>

            <p class="mt-3 text-sm leading-relaxed text-foreground/90">{{ item.text }}</p>

            <!-- Embed -->
            <div
              v-if="item.embed"
              class="mt-3 flex items-center gap-3 rounded-sm border border-white/[0.07] bg-white/[0.02] p-3"
            >
              <span
                class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm"
                :class="accentChip[item.embed.accent]"
              >
                <AppIcon :name="item.embed.icon" class="h-5 w-5" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-semibold text-foreground">{{ item.embed.title }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ item.embed.subtitle }}</p>
              </div>
              <span
                v-if="item.embed.meta"
                class="inline-flex shrink-0 items-center gap-1 rounded-sm bg-amber-500/10 px-2 py-0.5 text-xs font-semibold text-amber-300"
              >
                <AppIcon name="coin" class="h-3.5 w-3.5" /> {{ item.embed.meta }}
              </span>
            </div>

            <!-- Reactions -->
            <div class="mt-3 flex items-center gap-1 border-t border-white/[0.06] pt-3">
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-sm px-2.5 py-1.5 text-xs font-medium transition-colors"
                :class="item.liked ? 'text-rose-300' : 'text-muted-foreground hover:text-rose-300'"
                @click="toggleLike(item)"
              >
                <AppIcon name="heart" class="h-4 w-4" :class="item.liked ? 'fill-current' : ''" />
                {{ item.likes }}
              </button>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-sm px-2.5 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:text-cyan-300"
              >
                <AppIcon name="message" class="h-4 w-4" /> {{ item.comments }}
              </button>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-sm px-2.5 py-1.5 text-xs font-medium transition-colors"
                :class="
                  item.reposted
                    ? 'text-emerald-300'
                    : 'text-muted-foreground hover:text-emerald-300'
                "
                @click="toggleRepost(item)"
              >
                <AppIcon name="repeat" class="h-4 w-4" /> {{ item.reposts }}
              </button>
              <button
                type="button"
                class="ml-auto inline-flex items-center gap-1.5 rounded-sm px-2.5 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:text-violet-300"
                @click="share"
              >
                <AppIcon name="share" class="h-4 w-4" /> Chia sẻ
              </button>
            </div>
          </GlassPanel>
        </transition-group>

        <p
          v-if="filteredItems.length === 0"
          class="rounded-sm border border-dashed border-white/[0.1] py-10 text-center text-sm text-muted-foreground"
        >
          Chưa có hoạt động nào trong mục này.
        </p>
      </div>

      <!-- SIDE RAIL -->
      <div class="space-y-5">
        <SectionPanel
          title="Đang hoạt động"
          :subtitle="`${onlineFriends.length} bạn online`"
          icon="users"
          accent="green"
        >
          <ul class="space-y-2.5">
            <li v-for="f in onlineFriends" :key="f.id" class="flex items-center gap-3">
              <span class="relative">
                <Avatar :name="f.name" :accent="f.accent" :size="38" />
                <span
                  class="fx-glow-pulse absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-sm border-2 border-[oklch(0.13_0.025_280)] bg-emerald-400"
                />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ f.name }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ f.status }}</p>
              </div>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Gợi ý theo dõi" icon="sparkles" accent="magenta">
          <ul class="space-y-3">
            <li v-for="s in suggestions" :key="s.id" class="flex items-center gap-3">
              <Avatar :name="s.name" :accent="s.accent" :size="40" />
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ s.name }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ s.field }}</p>
              </div>
              <button
                type="button"
                class="shrink-0 rounded-sm border px-3 py-1.5 text-xs font-semibold transition-colors"
                :class="
                  s.followed
                    ? 'border-white/[0.12] bg-white/[0.04] text-muted-foreground'
                    : 'border-fuchsia-400/40 bg-fuchsia-500/15 text-fuchsia-200 hover:bg-fuchsia-500/25'
                "
                @click="toggleFollow(s)"
              >
                {{ s.followed ? 'Đang theo dõi' : 'Theo dõi' }}
              </button>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Chủ đề thịnh hành" icon="flame" accent="amber">
          <div class="flex flex-wrap gap-2">
            <RouterLink
              v-for="tag in trendingTags"
              :key="tag"
              to="/app/trending"
              class="rounded-sm border border-white/[0.07] bg-white/[0.02] px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-amber-400/40 hover:text-amber-200"
            >
              {{ tag }}
            </RouterLink>
          </div>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.feed-enter-active,
.feed-leave-active {
  transition:
    opacity 0.35s ease,
    transform 0.35s ease;
}
.feed-enter-from {
  opacity: 0;
  transform: translateY(-12px);
}
.feed-leave-to {
  opacity: 0;
  transform: scale(0.97);
}
.feed-leave-active {
  position: absolute;
}
@media (prefers-reduced-motion: reduce) {
  .feed-enter-active,
  .feed-leave-active {
    transition: none;
  }
}
</style>
