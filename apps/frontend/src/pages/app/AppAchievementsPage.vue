<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import StatPanel from '@/components/app/StatPanel.vue';
import BadgeTile from '@/components/app/BadgeTile.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import Tabs from '@/components/app/Tabs.vue';

interface Achievement {
  name: string;
  icon: string;
  description: string;
  unlocked: boolean;
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
  progress?: number;
}

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface AchievementCategory {
  title: string;
  icon: string;
  accent: Accent;
  items: Achievement[];
}

const TOTAL_BADGES = 40;
const UNLOCKED_COUNT = 18;
const completionPct = Math.round((UNLOCKED_COUNT / TOTAL_BADGES) * 100);

const tabs = [
  { id: 'all', label: 'Tất cả' },
  { id: 'unlocked', label: 'Đã mở khoá', badge: UNLOCKED_COUNT },
  { id: 'locked', label: 'Chưa mở khoá', badge: TOTAL_BADGES - UNLOCKED_COUNT },
];
const filter = ref('all');

const nearlyDone: Achievement[] = [
  {
    name: 'Siêu ghi chú',
    icon: 'file-text',
    description: 'Tạo 50 ghi chú trong hệ thống',
    unlocked: false,
    rarity: 'rare',
    progress: 88,
  },
  {
    name: 'Học viên xuất sắc',
    icon: 'graduation',
    description: 'Hoàn thành 10 khoá học liên tiếp',
    unlocked: false,
    rarity: 'epic',
    progress: 80,
  },
  {
    name: 'Bạn bè thân thiết',
    icon: 'users',
    description: 'Kết bạn với 20 người',
    unlocked: false,
    rarity: 'rare',
    progress: 75,
  },
  {
    name: 'Nhà sưu tầm',
    icon: 'bookmark',
    description: 'Lưu 100 tài liệu vào bộ sưu tập',
    unlocked: false,
    rarity: 'epic',
    progress: 72,
  },
];

const categories: AchievementCategory[] = [
  {
    title: 'Học tập',
    icon: 'book-open',
    accent: 'cyan',
    items: [
      { name: 'Bước đầu', icon: 'book-open', description: 'Đọc tài liệu đầu tiên', unlocked: true, rarity: 'common' },
      { name: 'Chăm học', icon: 'graduation', description: 'Hoàn thành 5 tài liệu', unlocked: true, rarity: 'common' },
      { name: 'Ghi chú viên', icon: 'file-text', description: 'Tạo 10 ghi chú học tập', unlocked: true, rarity: 'rare' },
      { name: 'Tư duy sáng tạo', icon: 'lightbulb', description: 'Hoàn thành 3 bài tập tư duy', unlocked: true, rarity: 'rare' },
      { name: 'Bậc thầy tri thức', icon: 'graduation', description: 'Đọc 50 tài liệu các thể loại', unlocked: false, rarity: 'epic', progress: 60 },
    ],
  },
  {
    title: 'Chuỗi học',
    icon: 'flame',
    accent: 'amber',
    items: [
      { name: 'Ngày đầu tiên', icon: 'flame', description: 'Học 1 ngày liên tiếp', unlocked: true, rarity: 'common' },
      { name: 'Tuần lễ vàng', icon: 'calendar', description: 'Học 7 ngày liên tiếp', unlocked: true, rarity: 'rare' },
      { name: 'Kiên trì', icon: 'clock', description: 'Học 30 ngày liên tiếp', unlocked: true, rarity: 'epic' },
      { name: 'Bất khuất', icon: 'flame', description: 'Học 100 ngày liên tiếp', unlocked: false, rarity: 'legendary', progress: 45 },
    ],
  },
  {
    title: 'Xã hội',
    icon: 'users',
    accent: 'magenta',
    items: [
      { name: 'Người hòa đồng', icon: 'users', description: 'Kết bạn với 5 người', unlocked: true, rarity: 'common' },
      { name: 'Trái tim ấm áp', icon: 'heart', description: 'Thích 20 tài liệu của người khác', unlocked: true, rarity: 'rare' },
      { name: 'Chia sẻ vui', icon: 'share', description: 'Chia sẻ 10 tài liệu với bạn bè', unlocked: false, rarity: 'rare', progress: 50 },
      { name: 'Nhà trò chuyện', icon: 'message', description: 'Gửi 100 tin nhắn trong nhóm', unlocked: false, rarity: 'epic', progress: 38 },
    ],
  },
  {
    title: 'Sưu tầm',
    icon: 'folder',
    accent: 'green',
    items: [
      { name: 'Kho lưu trữ', icon: 'folder', description: 'Tạo bộ sưu tập đầu tiên', unlocked: true, rarity: 'common' },
      { name: 'Xếp lớp tài liệu', icon: 'layers', description: 'Tạo 5 bộ sưu tập khác nhau', unlocked: true, rarity: 'rare' },
      { name: 'Đánh dấu chuyên gia', icon: 'bookmark', description: 'Đánh dấu 50 tài liệu yêu thích', unlocked: false, rarity: 'rare', progress: 64 },
      { name: 'Viên ngọc quý', icon: 'gem', description: 'Sở hữu tài liệu hiếm đầu tiên', unlocked: false, rarity: 'legendary', progress: 20 },
    ],
  },
  {
    title: 'Đặc biệt',
    icon: 'crown',
    accent: 'violet',
    items: [
      { name: 'Khai phá', icon: 'rocket', description: 'Đăng ký và bắt đầu hành trình', unlocked: true, rarity: 'epic' },
      { name: 'Ngôi sao sáng', icon: 'star', description: 'Đạt điểm tuyệt đối trong một bài quiz', unlocked: true, rarity: 'epic' },
      { name: 'Nhà vô địch', icon: 'trophy', description: 'Xếp hạng #1 bảng xếp hạng tuần', unlocked: false, rarity: 'legendary', progress: 0 },
      { name: 'Huyền thoại', icon: 'crown', description: 'Mở khoá tất cả thành tựu trong một danh mục', unlocked: false, rarity: 'legendary', progress: 30 },
    ],
  },
];

const visibleCategories = computed<AchievementCategory[]>(() => {
  if (filter.value === 'all') return categories;
  return categories
    .map((cat) => ({
      ...cat,
      items: cat.items.filter((item) =>
        filter.value === 'unlocked' ? item.unlocked : !item.unlocked,
      ),
    }))
    .filter((cat) => cat.items.length > 0);
});

const totalCoins = 2340;
const rarestLabel = 'Huyền thoại';
const achievementPoints = 1570;
const rareBadgeCount = 4;
</script>

<template>
  <div class="space-y-6">
    <!-- Header panel -->
    <GlassPanel glow="violet" :padded="true">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-center lg:gap-10">
        <!-- Ring progress -->
        <div class="flex shrink-0 justify-center lg:justify-start">
          <RingProgress :value="completionPct" :size="120" accent="violet">
            <span class="text-xl font-bold text-foreground">{{ completionPct }}%</span>
            <span class="text-[0.6rem] text-muted-foreground">Đã mở khoá</span>
          </RingProgress>
        </div>

        <!-- Title + quick stats -->
        <div class="min-w-0 flex-1">
          <p class="cosmic-badge">Hành trình thành tựu</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Thành tựu</span>
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Theo dõi các cột mốc học tập của bạn và mở khoá huy hiệu đặc biệt.
          </p>

          <div class="mt-4 flex flex-wrap gap-3">
            <div class="inline-flex items-center gap-2 rounded-sm bg-white/[0.06] px-3 py-1.5 text-xs font-medium text-muted-foreground">
              <AppIcon name="award" class="h-3.5 w-3.5 text-violet-400" />
              Đã mở khoá
              <span class="font-bold text-foreground">{{ UNLOCKED_COUNT }}/{{ TOTAL_BADGES }}</span>
            </div>
            <div class="inline-flex items-center gap-2 rounded-sm bg-white/[0.06] px-3 py-1.5 text-xs font-medium text-muted-foreground">
              <AppIcon name="coin" class="h-3.5 w-3.5 text-amber-400" />
              Tổng coin nhận được
              <span class="font-bold text-amber-300">{{ totalCoins.toLocaleString('vi-VN') }}</span>
            </div>
            <div class="inline-flex items-center gap-2 rounded-sm bg-white/[0.06] px-3 py-1.5 text-xs font-medium text-muted-foreground">
              <AppIcon name="gem" class="h-3.5 w-3.5 text-fuchsia-400" />
              Hạng hiếm nhất
              <span class="font-bold text-fuchsia-300">{{ rarestLabel }}</span>
            </div>
          </div>
        </div>
      </div>
    </GlassPanel>

    <!-- Stat row -->
    <section class="grid grid-cols-2 gap-4 md:grid-cols-4">
      <StatPanel
        label="Đã mở khoá"
        :value="String(UNLOCKED_COUNT)"
        :unit="`/ ${TOTAL_BADGES}`"
        icon="award"
        accent="violet"
        :trend="12"
        caption="+2 tuần này"
      />
      <StatPanel
        label="Tỉ lệ hoàn thành"
        :value="`${completionPct}%`"
        icon="chart"
        accent="cyan"
        :trend="5"
        caption="Tốt hơn 80% người dùng"
      />
      <StatPanel
        label="Điểm thành tựu"
        :value="achievementPoints.toLocaleString('vi-VN')"
        icon="zap"
        accent="amber"
        :trend="8"
        caption="+120 tuần này"
        :sparkline="[900, 1020, 1100, 1200, 1310, 1420, 1510, 1570]"
      />
      <StatPanel
        label="Huy hiệu hiếm"
        :value="String(rareBadgeCount)"
        icon="gem"
        accent="magenta"
        caption="Legendary & Epic"
      />
    </section>

    <!-- Nearly done -->
    <SectionPanel
      title="Sắp đạt được"
      subtitle="Cố thêm chút nữa để mở khoá!"
      icon="target"
      accent="amber"
    >
      <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
        <BadgeTile
          v-for="badge in nearlyDone"
          :key="badge.name"
          :name="badge.name"
          :icon="badge.icon"
          :description="badge.description"
          :unlocked="false"
          :rarity="badge.rarity"
          :progress="badge.progress"
        />
      </div>
    </SectionPanel>

    <!-- Filter tabs -->
    <Tabs v-model="filter" :tabs="tabs" />

    <!-- Category sections -->
    <template v-for="cat in visibleCategories" :key="cat.title">
      <SectionPanel
        :title="cat.title"
        :icon="cat.icon"
        :accent="cat.accent"
        :subtitle="`${cat.items.filter((i) => i.unlocked).length}/${cat.items.length} đã mở khoá`"
      >
        <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          <BadgeTile
            v-for="badge in cat.items"
            :key="badge.name"
            :name="badge.name"
            :icon="badge.icon"
            :description="badge.description"
            :unlocked="badge.unlocked"
            :rarity="badge.rarity"
            :progress="badge.progress"
          />
        </div>
      </SectionPanel>
    </template>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
