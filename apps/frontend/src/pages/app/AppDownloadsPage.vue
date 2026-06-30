<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import RingProgress from '@/components/app/RingProgress.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type DLStatus = 'done' | 'downloading' | 'available';
type DLType = 'doc' | 'audio' | 'video' | 'pack';

const { push } = useToast();

interface DownloadItem {
  id: number;
  title: string;
  type: DLType;
  sizeMb: number;
  date: string;
  status: DLStatus;
  progress: number;
}

const typeMeta: Record<DLType, { label: string; icon: string; accent: Accent }> = {
  doc: { label: 'Tài liệu', icon: 'file-text', accent: 'cyan' },
  audio: { label: 'Âm thanh', icon: 'headset', accent: 'magenta' },
  video: { label: 'Video', icon: 'video', accent: 'violet' },
  pack: { label: 'Gói học', icon: 'layers', accent: 'amber' }
};

const filters: { id: 'all' | DLType; label: string }[] = [
  { id: 'all', label: 'Tất cả' },
  { id: 'doc', label: 'Tài liệu' },
  { id: 'audio', label: 'Âm thanh' },
  { id: 'video', label: 'Video' },
  { id: 'pack', label: 'Gói học' }
];
const activeFilter = ref<'all' | DLType>('all');

const items = ref<DownloadItem[]>([
  {
    id: 1,
    title: 'Làm chủ đạo hàm (PDF)',
    type: 'doc',
    sizeMb: 12,
    date: 'Hôm nay',
    status: 'done',
    progress: 100
  },
  {
    id: 2,
    title: 'Podcast luyện nghe TOEIC #12',
    type: 'audio',
    sizeMb: 48,
    date: 'Hôm qua',
    status: 'done',
    progress: 100
  },
  {
    id: 3,
    title: 'Bài giảng video: Quy hoạch động',
    type: 'video',
    sizeMb: 220,
    date: '2 ngày trước',
    status: 'done',
    progress: 100
  },
  {
    id: 4,
    title: 'Gói ôn thi THPT môn Toán',
    type: 'pack',
    sizeMb: 156,
    date: '—',
    status: 'available',
    progress: 0
  },
  {
    id: 5,
    title: 'Tổng hợp công thức Vật lý (PDF)',
    type: 'doc',
    sizeMb: 8,
    date: '—',
    status: 'available',
    progress: 0
  },
  {
    id: 6,
    title: 'Video thí nghiệm Hoá học',
    type: 'video',
    sizeMb: 180,
    date: '—',
    status: 'available',
    progress: 0
  }
]);

const totalMb = 1024;
const usedMb = computed<number>(() =>
  items.value.filter((i) => i.status === 'done').reduce((s, i) => s + i.sizeMb, 0)
);
const usedPct = computed<number>(() => Math.min(100, Math.round((usedMb.value / totalMb) * 100)));
const doneCount = computed<number>(() => items.value.filter((i) => i.status === 'done').length);

const filtered = computed<DownloadItem[]>(() =>
  activeFilter.value === 'all'
    ? items.value
    : items.value.filter((i) => i.type === activeFilter.value)
);

function fmtSize(mb: number): string {
  return mb >= 1024 ? `${(mb / 1024).toFixed(1)} GB` : `${mb} MB`;
}

const timers = new Map<number, ReturnType<typeof setInterval>>();

function startDownload(item: DownloadItem): void {
  if (item.status !== 'available') return;
  item.status = 'downloading';
  item.progress = 0;
  const timer = setInterval(() => {
    item.progress = Math.min(100, item.progress + 4 + Math.random() * 9);
    if (item.progress >= 100) {
      item.progress = 100;
      item.status = 'done';
      item.date = 'Vừa xong';
      clearInterval(timer);
      timers.delete(item.id);
      push(`Đã tải xong: ${item.title}`, { icon: 'check-circle', accent: 'green' });
    }
  }, 220);
  timers.set(item.id, timer);
}

function cancelDownload(item: DownloadItem): void {
  const timer = timers.get(item.id);
  if (timer) {
    clearInterval(timer);
    timers.delete(item.id);
  }
  item.status = 'available';
  item.progress = 0;
}

function removeItem(item: DownloadItem): void {
  cancelDownload(item);
  item.status = 'available';
  item.date = '—';
  item.progress = 0;
  push(`Đã xoá khỏi máy: ${item.title}`, { icon: 'trash', accent: 'rose' });
}

function clearCache(): void {
  push('Đã dọn 64 MB bộ nhớ đệm', { icon: 'refresh', accent: 'cyan' });
}

onUnmounted(() => {
  timers.forEach((t) => clearInterval(t));
  timers.clear();
});

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
</script>

<template>
  <div class="space-y-6">
    <!-- HERO + STORAGE -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10 flex flex-col items-center gap-5 sm:flex-row sm:justify-between">
        <div>
          <p class="cosmic-badge"><AppIcon name="download" class="h-3.5 w-3.5" /> Tải xuống</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            Học <span class="cosmic-gradient-text">offline</span> mọi lúc
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            <span class="font-semibold text-cyan-300"><CountUp :value="doneCount" /></span> mục đã
            lưu · {{ fmtSize(usedMb) }} / {{ fmtSize(totalMb) }} dung lượng.
          </p>
          <button
            type="button"
            class="mt-4 inline-flex items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
            @click="clearCache"
          >
            <AppIcon name="refresh" class="h-4 w-4" /> Dọn bộ nhớ đệm
          </button>
        </div>
        <RingProgress
          :value="usedPct"
          :size="128"
          :stroke="12"
          :accent="usedPct > 85 ? 'amber' : 'cyan'"
        >
          <span class="text-2xl font-bold text-foreground"
            ><CountUp :value="usedPct" suffix="%"
          /></span>
          <span class="text-[0.7rem] text-muted-foreground">đã dùng</span>
        </RingProgress>
      </div>
    </GlassPanel>

    <!-- FILTERS -->
    <div class="cosmic-scroll -mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
      <button
        v-for="f in filters"
        :key="f.id"
        type="button"
        class="inline-flex shrink-0 items-center rounded-sm border px-3.5 py-2 text-sm font-medium transition-all"
        :class="
          activeFilter === f.id
            ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
            : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
        "
        @click="activeFilter = f.id"
      >
        {{ f.label }}
      </button>
    </div>

    <!-- LIST -->
    <SectionPanel
      title="Thư viện ngoại tuyến"
      :subtitle="`${filtered.length} mục`"
      icon="folder"
      accent="cyan"
      flush
    >
      <ul class="divide-y divide-white/[0.05]">
        <li v-for="item in filtered" :key="item.id" class="flex items-center gap-3 px-5 py-3.5">
          <span
            class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm"
            :class="accentChip[typeMeta[item.type].accent]"
          >
            <AppIcon :name="typeMeta[item.type].icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-semibold text-foreground">{{ item.title }}</p>
            <p class="truncate text-xs text-muted-foreground">
              {{ typeMeta[item.type].label }} · {{ fmtSize(item.sizeMb) }}
              <span v-if="item.status === 'done'"> · {{ item.date }}</span>
            </p>
            <div v-if="item.status === 'downloading'" class="mt-2 flex items-center gap-2">
              <ProgressBar :value="item.progress" accent="cyan" class="flex-1" />
              <span class="shrink-0 text-[0.7rem] tabular-nums text-cyan-300"
                >{{ Math.round(item.progress) }}%</span
              >
            </div>
          </div>

          <!-- Actions -->
          <div class="flex shrink-0 items-center gap-1.5">
            <template v-if="item.status === 'done'">
              <span
                class="hidden items-center gap-1 rounded-sm bg-emerald-500/10 px-2 py-1 text-[0.7rem] font-medium text-emerald-300 sm:inline-flex"
              >
                <AppIcon name="check-circle" class="h-3.5 w-3.5" /> Đã lưu
              </span>
              <button
                type="button"
                class="rounded-sm p-2 text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-cyan-300"
                aria-label="Mở"
              >
                <AppIcon
                  :name="item.type === 'video' || item.type === 'audio' ? 'play' : 'book-open'"
                  class="h-4 w-4"
                />
              </button>
              <button
                type="button"
                class="rounded-sm p-2 text-muted-foreground transition-colors hover:bg-rose-500/15 hover:text-rose-300"
                aria-label="Xoá"
                @click="removeItem(item)"
              >
                <AppIcon name="trash" class="h-4 w-4" />
              </button>
            </template>
            <template v-else-if="item.status === 'downloading'">
              <button
                type="button"
                class="rounded-sm border border-white/10 px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:border-rose-400/40 hover:text-rose-300"
                @click="cancelDownload(item)"
              >
                Huỷ
              </button>
            </template>
            <template v-else>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 rounded-sm border border-cyan-400/40 bg-cyan-500/15 px-3 py-1.5 text-xs font-semibold text-cyan-200 transition-colors hover:bg-cyan-500/25"
                @click="startDownload(item)"
              >
                <AppIcon name="download" class="h-4 w-4" /> Tải về
              </button>
            </template>
          </div>
        </li>
      </ul>
    </SectionPanel>

    <!-- TIPS -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
      <GlassPanel
        v-for="tip in [
          {
            icon: 'wifi-off',
            title: 'Học không cần mạng',
            text: 'Tài liệu đã tải vẫn mở được khi mất kết nối.',
            accent: 'cyan'
          },
          {
            icon: 'refresh',
            title: 'Tự đồng bộ',
            text: 'Tiến độ đọc offline sẽ đồng bộ khi có mạng lại.',
            accent: 'green'
          },
          {
            icon: 'shield',
            title: 'An toàn riêng tư',
            text: 'Nội dung tải về được mã hoá trên thiết bị của bạn.',
            accent: 'violet'
          }
        ]"
        :key="tip.title"
        hover
        padded
      >
        <span
          class="flex h-10 w-10 items-center justify-center rounded-sm"
          :class="accentChip[tip.accent as Accent]"
        >
          <AppIcon :name="tip.icon" class="h-5 w-5" />
        </span>
        <p class="mt-3 text-sm font-semibold text-foreground">{{ tip.title }}</p>
        <p class="mt-1 text-xs text-muted-foreground">{{ tip.text }}</p>
      </GlassPanel>
    </div>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
