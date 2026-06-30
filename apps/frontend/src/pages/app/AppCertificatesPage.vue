<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';
import CountUp from '@/components/app/CountUp.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

const { push } = useToast();

interface Certificate {
  id: number;
  title: string;
  issuer: string;
  date: string;
  code: string;
  hours: number;
  accent: Accent;
  icon: string;
  skills: string[];
}
interface InProgress {
  id: number;
  title: string;
  issuer: string;
  progress: number;
  remaining: string;
  accent: Accent;
  icon: string;
}

const certificates: Certificate[] = [
  {
    id: 1,
    title: 'Toán cao cấp: Giải tích & Đại số',
    issuer: 'Học viện Study Hub',
    date: '12/05/2026',
    code: 'SH-MATH-2026-0481',
    hours: 48,
    accent: 'cyan',
    icon: 'chart',
    skills: ['Đạo hàm', 'Tích phân', 'Ma trận']
  },
  {
    id: 2,
    title: 'Tiếng Anh học thuật IELTS',
    issuer: 'Study Hub Language',
    date: '28/03/2026',
    code: 'SH-LANG-2026-0193',
    hours: 36,
    accent: 'magenta',
    icon: 'globe',
    skills: ['Writing', 'Speaking', 'Reading']
  },
  {
    id: 3,
    title: 'Nền tảng Khoa học máy tính',
    issuer: 'Study Hub Tech',
    date: '02/02/2026',
    code: 'SH-CODE-2026-0072',
    hours: 60,
    accent: 'green',
    icon: 'command',
    skills: ['Thuật toán', 'CTDL', 'Python']
  }
];

const inProgress: InProgress[] = [
  {
    id: 1,
    title: 'Vật lý hiện đại',
    issuer: 'Study Hub Science',
    progress: 68,
    remaining: 'Còn 4 bài học',
    accent: 'violet',
    icon: 'rocket'
  },
  {
    id: 2,
    title: 'Kỹ năng học tập hiệu quả',
    issuer: 'Study Hub Skills',
    progress: 40,
    remaining: 'Còn 7 bài học',
    accent: 'amber',
    icon: 'lightbulb'
  }
];

const totalHours = computed<number>(() => certificates.reduce((s, c) => s + c.hours, 0));

const stats = [
  {
    label: 'Chứng chỉ đã đạt',
    value: certificates.length,
    icon: 'shield',
    accent: 'cyan' as Accent
  },
  {
    label: 'Giờ học tích luỹ',
    value: totalHours.value,
    icon: 'clock',
    accent: 'magenta' as Accent
  },
  { label: 'Đang theo học', value: inProgress.length, icon: 'book-open', accent: 'green' as Accent }
];

const selected = ref<Certificate | null>(null);
function openCert(c: Certificate): void {
  selected.value = c;
}
function download(): void {
  push('Đang chuẩn bị file PDF chứng chỉ...', { icon: 'download', accent: 'cyan' });
}
function share(): void {
  push('Đã sao chép liên kết xác thực chứng chỉ', { icon: 'link', accent: 'violet' });
}

const banner: Record<Accent, string> = {
  cyan: 'from-cyan-500/50 via-blue-600/30 to-transparent',
  magenta: 'from-fuchsia-500/50 via-purple-600/30 to-transparent',
  green: 'from-emerald-500/50 via-teal-600/30 to-transparent',
  violet: 'from-violet-500/50 via-indigo-600/30 to-transparent',
  amber: 'from-amber-400/50 via-orange-600/30 to-transparent'
};
const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
const accentBorder: Record<Accent, string> = {
  cyan: 'border-cyan-400/40',
  magenta: 'border-fuchsia-400/40',
  green: 'border-emerald-400/40',
  violet: 'border-violet-400/40',
  amber: 'border-amber-400/40'
};
</script>

<template>
  <div class="space-y-6">
    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10 flex items-center gap-4">
        <span
          class="fx-float flex h-14 w-14 items-center justify-center rounded-sm from-cyan-500/30 to-fuchsia-500/20 text-cyan-200"
        >
          <AppIcon name="shield" class="h-7 w-7" />
        </span>
        <div>
          <p class="cosmic-badge">Chứng chỉ của bạn</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            Thành quả được <span class="cosmic-gradient-text">công nhận</span>
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Chứng chỉ hoàn thành khoá học và lộ trình — có thể tải về và chia sẻ.
          </p>
        </div>
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
            <p class="text-lg font-bold text-foreground sm:text-xl"><CountUp :value="s.value" /></p>
            <p class="truncate text-[0.7rem] text-muted-foreground">{{ s.label }}</p>
          </div>
        </div>
      </GlassPanel>
    </section>

    <!-- EARNED -->
    <SectionPanel
      title="Chứng chỉ đã đạt"
      :subtitle="`${certificates.length} chứng chỉ`"
      icon="award"
      accent="cyan"
      flush
    >
      <div class="grid grid-cols-1 gap-4 px-5 pb-5 sm:grid-cols-2 lg:grid-cols-3">
        <button
          v-for="c in certificates"
          :key="c.id"
          type="button"
          class="fx-lift fx-sheen group flex flex-col overflow-hidden rounded-sm border border-white/[0.08] bg-white/[0.02] text-left"
          @click="openCert(c)"
        >
          <div class="relative h-24" :class="banner[c.accent]">
            <div
              class="absolute inset-0 opacity-40"
              style="
                background: radial-gradient(
                  circle at 25% 20%,
                  rgba(255, 255, 255, 0.3),
                  transparent 60%
                );
              "
            />
            <span
              class="absolute left-4 top-4 flex h-12 w-12 items-center justify-center rounded-sm bg-black/30 text-white backdrop-blur"
            >
              <AppIcon :name="c.icon" class="h-6 w-6" />
            </span>
            <span
              class="absolute right-4 top-4 flex h-10 w-10 items-center justify-center rounded-sm border-2 border-white/40 bg-white/15 text-white backdrop-blur"
            >
              <AppIcon name="shield" class="h-5 w-5" />
            </span>
          </div>
          <div class="flex flex-1 flex-col p-4">
            <h3 class="line-clamp-2 text-sm font-semibold leading-snug text-foreground">
              {{ c.title }}
            </h3>
            <p class="mt-1 text-xs text-muted-foreground">{{ c.issuer }}</p>
            <div
              class="mt-auto flex items-center justify-between pt-3 text-xs text-muted-foreground"
            >
              <span class="inline-flex items-center gap-1"
                ><AppIcon name="calendar" class="h-3.5 w-3.5" /> {{ c.date }}</span
              >
              <span class="inline-flex items-center gap-1 font-medium text-cyan-300"
                >Xem <AppIcon name="arrow-right" class="h-3.5 w-3.5"
              /></span>
            </div>
          </div>
        </button>
      </div>
    </SectionPanel>

    <!-- IN PROGRESS -->
    <SectionPanel
      title="Đang theo học"
      subtitle="Hoàn thành để nhận chứng chỉ"
      icon="route"
      accent="violet"
    >
      <ul class="space-y-3">
        <li
          v-for="p in inProgress"
          :key="p.id"
          class="flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3.5 py-3"
        >
          <span
            class="flex h-11 w-11 shrink-0 items-center justify-center rounded-sm"
            :class="accentChip[p.accent]"
          >
            <AppIcon :name="p.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0 flex-1">
            <div class="flex items-center justify-between gap-2">
              <p class="truncate text-sm font-semibold text-foreground">{{ p.title }}</p>
              <span class="shrink-0 text-xs font-semibold text-violet-300 tabular-nums"
                >{{ p.progress }}%</span
              >
            </div>
            <p class="text-xs text-muted-foreground">{{ p.issuer }} · {{ p.remaining }}</p>
            <ProgressBar :value="p.progress" :accent="p.accent" class="mt-2" />
          </div>
        </li>
      </ul>
    </SectionPanel>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>

    <!-- CERTIFICATE PREVIEW MODAL -->
    <transition name="modal">
      <div v-if="selected" class="fixed inset-0 z-[110] flex items-center justify-center p-4 !mt-0">
        <div class="absolute inset-0 bg-black/70 backdrop-blur-sm" @click="selected = null" />
        <div class="relative z-10 w-full max-w-lg">
          <button
            type="button"
            class="absolute -top-11 right-0 rounded-sm bg-white/10 p-2 text-foreground backdrop-blur transition-colors hover:bg-white/20"
            aria-label="Đóng"
            @click="selected = null"
          >
            <AppIcon name="x" class="h-5 w-5" />
          </button>
          <!-- Faux certificate -->
          <div
            class="overflow-hidden rounded-sm border-2 from-[oklch(0.16_0.03_280)] to-[oklch(0.1_0.02_280)] p-1"
            :class="accentBorder[selected.accent]"
          >
            <div
              class="relative rounded-sm border border-white/[0.08] px-6 py-8 text-center sm:px-10"
            >
              <div
                class="absolute inset-0 opacity-[0.04]"
                style="
                  background-image: radial-gradient(circle, currentColor 1px, transparent 1px);
                  background-size: 16px 16px;
                "
              />
              <div class="relative">
                <span
                  class="mx-auto flex h-16 w-16 items-center justify-center rounded-sm text-white"
                  :class="banner[selected.accent]"
                >
                  <AppIcon name="shield" class="h-8 w-8" />
                </span>
                <p
                  class="mt-4 text-xs font-semibold uppercase tracking-[0.3em] text-muted-foreground"
                >
                  Chứng nhận hoàn thành
                </p>
                <h3 class="mt-3 text-xl font-bold text-foreground sm:text-2xl">
                  {{ selected.title }}
                </h3>
                <p class="mt-2 text-sm text-muted-foreground">Chứng nhận rằng</p>
                <p class="mt-1 text-lg font-bold text-cyan-200">Học viên Study Hub</p>
                <p class="mx-auto mt-2 max-w-sm text-sm text-muted-foreground">
                  đã hoàn thành xuất sắc khoá học với {{ selected.hours }} giờ học tập và đáp ứng
                  mọi tiêu chí đánh giá.
                </p>
                <div class="mt-5 flex flex-wrap justify-center gap-2">
                  <span
                    v-for="sk in selected.skills"
                    :key="sk"
                    class="rounded-sm border border-white/[0.1] bg-white/[0.03] px-3 py-1 text-xs text-foreground"
                    >{{ sk }}</span
                  >
                </div>
                <div
                  class="mt-6 flex items-center justify-between border-t border-white/[0.08] pt-4 text-left"
                >
                  <div>
                    <p class="text-[0.7rem] uppercase tracking-wide text-muted-foreground">
                      Ngày cấp
                    </p>
                    <p class="text-sm font-medium text-foreground">{{ selected.date }}</p>
                  </div>
                  <div class="text-right">
                    <p class="text-[0.7rem] uppercase tracking-wide text-muted-foreground">
                      Mã chứng chỉ
                    </p>
                    <p class="font-mono text-sm font-medium text-foreground">{{ selected.code }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="mt-3 flex gap-2">
            <button
              type="button"
              class="cosmic-btn inline-flex flex-1 items-center justify-center gap-2 rounded-sm px-4 py-2.5 text-sm"
              @click="download"
            >
              <AppIcon name="download" class="h-4 w-4" /> Tải PDF
            </button>
            <button
              type="button"
              class="inline-flex flex-1 items-center justify-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
              @click="share"
            >
              <AppIcon name="share" class="h-4 w-4" /> Chia sẻ
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
@media (prefers-reduced-motion: reduce) {
  .modal-enter-active,
  .modal-leave-active {
    transition: none;
  }
}
</style>
