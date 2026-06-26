<script setup lang="ts">
import { computed, ref } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import Tabs from '@/components/app/Tabs.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type NodeStatus = 'completed' | 'current' | 'locked';

interface LessonNode {
  id: string;
  title: string;
  desc: string;
  xp: number;
  lessons: number;
  reward: number;
  status: NodeStatus;
  icon: string;
}

interface LearningPath {
  id: string;
  name: string;
  tagline: string;
  icon: string;
  accent: Accent;
  nodes: LessonNode[];
}

const paths: LearningPath[] = [
  {
    id: 'math',
    name: 'Nền tảng Toán',
    tagline: 'Từ số học đến giải tích',
    icon: 'graduation',
    accent: 'cyan',
    nodes: [
      { id: 'm1', title: 'Số học cơ bản', desc: 'Phép tính, phân số và số thập phân — củng cố nền móng vững chắc.', xp: 120, lessons: 6, reward: 30, status: 'completed', icon: 'check' },
      { id: 'm2', title: 'Đại số nhập môn', desc: 'Biến số, biểu thức và phương trình bậc nhất.', xp: 180, lessons: 8, reward: 45, status: 'completed', icon: 'check' },
      { id: 'm3', title: 'Hàm số & Đồ thị', desc: 'Hàm bậc nhất, bậc hai và cách đọc đồ thị.', xp: 220, lessons: 10, reward: 60, status: 'current', icon: 'route' },
      { id: 'm4', title: 'Lượng giác', desc: 'Sin, cos, tan và các công thức biến đổi.', xp: 260, lessons: 9, reward: 70, status: 'locked', icon: 'lock' },
      { id: 'm5', title: 'Giới hạn & Đạo hàm', desc: 'Khái niệm giới hạn và đạo hàm của hàm số.', xp: 300, lessons: 11, reward: 90, status: 'locked', icon: 'lock' },
      { id: 'm6', title: 'Tích phân', desc: 'Nguyên hàm, tích phân xác định và ứng dụng.', xp: 360, lessons: 12, reward: 120, status: 'locked', icon: 'lock' }
    ]
  },
  {
    id: 'eng',
    name: 'Tiếng Anh giao tiếp',
    tagline: 'Tự tin nói chuyện mỗi ngày',
    icon: 'message',
    accent: 'green',
    nodes: [
      { id: 'e1', title: 'Phát âm nền tảng', desc: 'Bảng IPA, trọng âm và ngữ điệu cơ bản.', xp: 100, lessons: 5, reward: 25, status: 'completed', icon: 'check' },
      { id: 'e2', title: 'Chào hỏi & Giới thiệu', desc: 'Mẫu câu giao tiếp hằng ngày phổ biến nhất.', xp: 150, lessons: 7, reward: 40, status: 'completed', icon: 'check' },
      { id: 'e3', title: 'Hội thoại mua sắm', desc: 'Hỏi giá, mặc cả và thanh toán bằng tiếng Anh.', xp: 190, lessons: 8, reward: 50, status: 'current', icon: 'store' },
      { id: 'e4', title: 'Du lịch & Đặt phòng', desc: 'Giao tiếp tại sân bay, khách sạn và nhà hàng.', xp: 230, lessons: 9, reward: 60, status: 'locked', icon: 'lock' },
      { id: 'e5', title: 'Phỏng vấn xin việc', desc: 'Trả lời câu hỏi phỏng vấn một cách chuyên nghiệp.', xp: 280, lessons: 10, reward: 80, status: 'locked', icon: 'lock' }
    ]
  },
  {
    id: 'web',
    name: 'Lập trình Web',
    tagline: 'Xây dựng ứng dụng từ đầu',
    icon: 'zap',
    accent: 'violet',
    nodes: [
      { id: 'w1', title: 'HTML & CSS', desc: 'Cấu trúc trang và tạo kiểu giao diện cơ bản.', xp: 140, lessons: 8, reward: 35, status: 'completed', icon: 'check' },
      { id: 'w2', title: 'JavaScript nền tảng', desc: 'Biến, hàm, vòng lặp và thao tác DOM.', xp: 200, lessons: 12, reward: 55, status: 'current', icon: 'command' },
      { id: 'w3', title: 'Vue 3 cơ bản', desc: 'Component, reactivity và quản lý trạng thái.', xp: 260, lessons: 10, reward: 75, status: 'locked', icon: 'lock' },
      { id: 'w4', title: 'API & Fetch', desc: 'Gọi REST API, xử lý bất đồng bộ với async/await.', xp: 300, lessons: 9, reward: 90, status: 'locked', icon: 'lock' },
      { id: 'w5', title: 'Triển khai dự án', desc: 'Build, tối ưu và đưa ứng dụng lên môi trường thật.', xp: 360, lessons: 7, reward: 130, status: 'locked', icon: 'lock' }
    ]
  }
];

const activePathId = ref<string>(paths[0].id);
const activePath = computed<LearningPath>(() => paths.find((p) => p.id === activePathId.value) ?? paths[0]);

const pathTabs = computed(() =>
  paths.map((p) => ({
    id: p.id,
    label: p.name,
    badge: `${p.nodes.filter((n) => n.status === 'completed').length}/${p.nodes.length}`
  }))
);

// progress
const completedCount = computed<number>(() => activePath.value.nodes.filter((n) => n.status === 'completed').length);
const progressPct = computed<number>(() => Math.round((completedCount.value / activePath.value.nodes.length) * 100));
const earnedXp = computed<number>(() =>
  activePath.value.nodes.filter((n) => n.status === 'completed').reduce((sum, n) => sum + n.xp, 0)
);
const totalXp = computed<number>(() => activePath.value.nodes.reduce((sum, n) => sum + n.xp, 0));

// selected node detail (default = current node)
const selectedId = ref<string | null>(null);
const selectedNode = computed<LessonNode | null>(() => {
  const id = selectedId.value ?? activePath.value.nodes.find((n) => n.status === 'current')?.id ?? null;
  return activePath.value.nodes.find((n) => n.id === id) ?? null;
});

function selectNode(node: LessonNode): void {
  if (node.status === 'locked') return;
  selectedId.value = node.id;
}

function onTabChange(id: string): void {
  activePathId.value = id;
  selectedId.value = null;
}

const accentText: Record<Accent, string> = {
  cyan: 'text-cyan-300',
  magenta: 'text-fuchsia-300',
  green: 'text-emerald-300',
  violet: 'text-violet-300',
  amber: 'text-amber-300'
};
const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/10 text-cyan-300',
  magenta: 'bg-fuchsia-500/10 text-fuchsia-300',
  green: 'bg-emerald-500/10 text-emerald-300',
  violet: 'bg-violet-500/10 text-violet-300',
  amber: 'bg-amber-500/10 text-amber-300'
};
const pulseClass: Record<Accent, string> = {
  cyan: 'pulse-glow-cyan',
  magenta: 'pulse-glow-magenta',
  green: 'pulse-glow-green',
  violet: 'pulse-glow-cyan',
  amber: 'pulse-glow-magenta'
};

// node visual state classes
function nodeOuter(node: LessonNode): string {
  if (node.status === 'completed') {
    return 'border-emerald-400/50 bg-emerald-500/15 text-emerald-200 shadow-[0_0_22px_oklch(0.75_0.18_145/45%)]';
  }
  if (node.status === 'current') {
    return `border-cyan-400/70 bg-cyan-500/15 text-cyan-100 ${pulseClass[activePath.value.accent]}`;
  }
  return 'border-white/[0.08] bg-white/[0.02] text-muted-foreground';
}

// zigzag: even index left, odd index right
function isLeft(index: number): boolean {
  return index % 2 === 0;
}
</script>

<template>
  <div class="space-y-6">
    <!-- Hero -->
    <GlassPanel glow="violet" class="relative overflow-hidden p-5 sm:p-6">
      <div class="orbit-ring float-slow pointer-events-none absolute -right-16 -top-16 h-48 w-48 opacity-40" aria-hidden="true" />
      <div class="relative flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="cosmic-badge">Bản đồ kỹ năng</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            <span class="cosmic-gradient-text">Lộ trình học</span> 🚀
          </h1>
          <p class="mt-1.5 max-w-xl text-sm text-muted-foreground">
            Chinh phục từng cột mốc theo lộ trình được thiết kế sẵn. Mở khoá bài học mới và tích luỹ XP để thăng cấp.
          </p>
        </div>
        <div class="flex shrink-0 items-center gap-4">
          <RingProgress :value="progressPct" :size="108" :accent="activePath.accent">
            <span class="text-[0.6rem] uppercase tracking-wider text-muted-foreground">Tiến độ</span>
            <span class="text-2xl font-bold text-foreground">{{ progressPct }}%</span>
            <span class="text-[0.65rem] text-muted-foreground">{{ completedCount }}/{{ activePath.nodes.length }}</span>
          </RingProgress>
        </div>
      </div>
    </GlassPanel>

    <!-- Path selector -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <Tabs :tabs="pathTabs" :model-value="activePathId" @update:model-value="onTabChange" />
      <div class="flex items-center gap-2 text-sm text-muted-foreground">
        <AppIcon name="zap" class="h-4 w-4 text-amber-300" />
        <span class="font-semibold text-foreground">{{ earnedXp.toLocaleString('vi-VN') }}</span>
        / {{ totalXp.toLocaleString('vi-VN') }} XP
      </div>
    </div>

    <!-- Path cards -->
    <section class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <button
        v-for="p in paths"
        :key="p.id"
        type="button"
        class="rounded-2xl border p-4 text-left transition-all"
        :class="
          p.id === activePathId
            ? 'border-white/[0.18] bg-white/[0.05] shadow-[0_0_20px_oklch(0.6_0.2_290/25%)]'
            : 'border-white/[0.06] bg-white/[0.02] hover:border-white/[0.12]'
        "
        @click="onTabChange(p.id)"
      >
        <div class="flex items-center gap-3">
          <span class="flex h-11 w-11 items-center justify-center rounded-xl" :class="accentChip[p.accent]">
            <AppIcon :name="p.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0">
            <p class="truncate text-sm font-semibold text-foreground">{{ p.name }}</p>
            <p class="truncate text-xs text-muted-foreground">{{ p.tagline }}</p>
          </div>
        </div>
        <div class="mt-3 flex items-center justify-between text-xs">
          <span class="text-muted-foreground">
            {{ p.nodes.filter((n) => n.status === 'completed').length }}/{{ p.nodes.length }} cột mốc
          </span>
          <span class="inline-flex items-center gap-1 font-semibold" :class="accentText[p.accent]">
            <AppIcon name="trophy" class="h-3.5 w-3.5" />
            {{ Math.round((p.nodes.filter((n) => n.status === 'completed').length / p.nodes.length) * 100) }}%
          </span>
        </div>
      </button>
    </section>

    <!-- Roadmap + detail -->
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Roadmap -->
      <div class="lg:col-span-2">
        <SectionPanel :title="`Lộ trình · ${activePath.name}`" :subtitle="activePath.tagline" :icon="activePath.icon" :accent="activePath.accent">
          <div class="relative py-2">
            <!-- vertical guide line -->
            <span class="pointer-events-none absolute bottom-4 left-1/2 top-4 hidden w-px -translate-x-1/2 bg-gradient-to-b from-white/[0.04] via-white/[0.12] to-white/[0.04] sm:block" aria-hidden="true" />

            <ul class="space-y-3">
              <li
                v-for="(node, i) in activePath.nodes"
                :key="node.id"
                class="relative flex flex-col gap-3 sm:flex-row sm:items-center"
                :class="isLeft(i) ? 'sm:flex-row' : 'sm:flex-row-reverse'"
              >
                <!-- connector chevron on the spine -->
                <span
                  class="absolute left-1/2 top-1/2 hidden -translate-x-1/2 -translate-y-1/2 text-muted-foreground/40 sm:block"
                  aria-hidden="true"
                >
                  <AppIcon name="dot" class="h-3 w-3" />
                </span>

                <!-- node button (half row) -->
                <div class="sm:w-1/2" :class="isLeft(i) ? 'sm:pr-8' : 'sm:pl-8'">
                  <button
                    type="button"
                    :disabled="node.status === 'locked'"
                    :aria-label="node.title"
                    class="group flex w-full items-center gap-3 rounded-2xl border p-3.5 text-left transition-all"
                    :class="[
                      nodeOuter(node),
                      node.status === 'locked' ? 'cursor-not-allowed opacity-60' : 'hover:scale-[1.02]',
                      selectedNode && selectedNode.id === node.id ? 'ring-2 ring-white/30' : ''
                    ]"
                    @click="selectNode(node)"
                  >
                    <span
                      class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full border-2"
                      :class="
                        node.status === 'completed'
                          ? 'border-emerald-300/70 bg-emerald-500/25'
                          : node.status === 'current'
                            ? 'border-cyan-300/80 bg-cyan-500/25'
                            : 'border-white/10 bg-white/[0.04]'
                      "
                    >
                      <AppIcon :name="node.icon" class="h-5 w-5" />
                    </span>
                    <div class="min-w-0 flex-1">
                      <p class="truncate text-sm font-semibold text-foreground">{{ node.title }}</p>
                      <p class="flex items-center gap-2 text-xs">
                        <span class="inline-flex items-center gap-1 text-amber-300">
                          <AppIcon name="zap" class="h-3 w-3" /> {{ node.xp }} XP
                        </span>
                        <span class="text-muted-foreground">· {{ node.lessons }} bài</span>
                      </p>
                    </div>
                    <span
                      v-if="node.status === 'completed'"
                      class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-emerald-500/30 text-emerald-200"
                    >
                      <AppIcon name="check" class="h-3.5 w-3.5" />
                    </span>
                    <span
                      v-else-if="node.status === 'locked'"
                      class="shrink-0 text-muted-foreground"
                    >
                      <AppIcon name="lock" class="h-4 w-4" />
                    </span>
                    <span v-else class="shrink-0 text-cyan-300">
                      <AppIcon name="play" class="h-4 w-4" />
                    </span>
                  </button>
                </div>
              </li>
            </ul>
          </div>
        </SectionPanel>
      </div>

      <!-- Detail card -->
      <div>
        <SectionPanel title="Chi tiết cột mốc" subtitle="Nhấp vào một node để xem" icon="book-open" :accent="activePath.accent">
          <div v-if="selectedNode" class="space-y-4">
            <div class="flex items-center gap-3">
              <span
                class="flex h-12 w-12 items-center justify-center rounded-2xl"
                :class="accentChip[activePath.accent]"
              >
                <AppIcon :name="selectedNode.status === 'completed' ? 'check-circle' : selectedNode.icon" class="h-6 w-6" />
              </span>
              <div class="min-w-0">
                <p class="text-base font-semibold text-foreground">{{ selectedNode.title }}</p>
                <span
                  class="mt-0.5 inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[0.7rem] font-medium"
                  :class="
                    selectedNode.status === 'completed'
                      ? 'bg-emerald-500/15 text-emerald-300'
                      : selectedNode.status === 'current'
                        ? 'bg-cyan-500/15 text-cyan-300'
                        : 'bg-white/[0.06] text-muted-foreground'
                  "
                >
                  {{ selectedNode.status === 'completed' ? 'Đã hoàn thành' : selectedNode.status === 'current' ? 'Đang học' : 'Đã khoá' }}
                </span>
              </div>
            </div>

            <p class="text-sm leading-relaxed text-muted-foreground">{{ selectedNode.desc }}</p>

            <div class="grid grid-cols-3 gap-2.5">
              <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3 text-center">
                <p class="text-lg font-bold tabular-nums text-foreground">{{ selectedNode.lessons }}</p>
                <p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">bài học</p>
              </div>
              <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3 text-center">
                <p class="text-lg font-bold tabular-nums text-amber-300">{{ selectedNode.xp }}</p>
                <p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">XP</p>
              </div>
              <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3 text-center">
                <p class="inline-flex items-center justify-center gap-1 text-lg font-bold tabular-nums text-amber-300">
                  <AppIcon name="coin" class="h-4 w-4" />{{ selectedNode.reward }}
                </p>
                <p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">thưởng</p>
              </div>
            </div>

            <button
              type="button"
              :disabled="selectedNode.status === 'completed'"
              class="cosmic-btn flex w-full items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm"
              :class="selectedNode.status === 'completed' ? 'cursor-default opacity-60' : ''"
            >
              <AppIcon :name="selectedNode.status === 'completed' ? 'check' : 'play'" class="h-4 w-4" />
              {{ selectedNode.status === 'completed' ? 'Đã hoàn thành' : 'Tiếp tục học' }}
            </button>
          </div>

          <div v-else class="flex flex-col items-center justify-center py-10 text-center">
            <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/[0.04] text-muted-foreground">
              <AppIcon name="compass" class="h-6 w-6" />
            </span>
            <p class="mt-2 text-sm font-medium text-foreground">Chọn một cột mốc</p>
            <p class="mt-0.5 text-xs text-muted-foreground">Nhấp vào node đã mở khoá để xem chi tiết.</p>
          </div>
        </SectionPanel>

        <!-- Milestone markers -->
        <SectionPanel title="Cột mốc thưởng" subtitle="Phần thưởng theo tiến độ" icon="gift" accent="amber" class="mt-6">
          <ul class="space-y-3">
            <li
              v-for="(milestone, mi) in [
                { at: 25, label: 'Huy hiệu Đồng', reward: 100 },
                { at: 50, label: 'Huy hiệu Bạc', reward: 250 },
                { at: 75, label: 'Huy hiệu Vàng', reward: 500 },
                { at: 100, label: 'Hoàn thành lộ trình', reward: 1000 }
              ]"
              :key="mi"
              class="flex items-center gap-3"
            >
              <span
                class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border-2"
                :class="
                  progressPct >= milestone.at
                    ? 'border-amber-300/70 bg-amber-500/20 text-amber-200'
                    : 'border-white/10 bg-white/[0.03] text-muted-foreground'
                "
              >
                <AppIcon :name="progressPct >= milestone.at ? 'check' : 'lock'" class="h-4 w-4" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-foreground">{{ milestone.label }}</p>
                <p class="text-xs text-muted-foreground">Đạt {{ milestone.at }}% tiến độ</p>
              </div>
              <span class="inline-flex shrink-0 items-center gap-1 text-sm font-semibold text-amber-300">
                <AppIcon name="coin" class="h-3.5 w-3.5" /> {{ milestone.reward }}
              </span>
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
