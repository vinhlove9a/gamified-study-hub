<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import TiltSpotlightCard from '@/components/app/TiltSpotlightCard.vue';
import Avatar from '@/components/app/Avatar.vue';
import CountUp from '@/components/app/CountUp.vue';
import ConfettiBurst from '@/components/app/ConfettiBurst.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

const { push } = useToast();
const confetti = ref<InstanceType<typeof ConfettiBurst> | null>(null);

interface Mentor {
  id: number;
  name: string;
  field: string;
  fieldId: string;
  accent: Accent;
  rating: number;
  reviews: number;
  sessions: number;
  priceCoin: number;
  online: boolean;
  tags: string[];
  bio: string;
}

const fields: { id: string; label: string; icon: string }[] = [
  { id: 'all', label: 'Tất cả', icon: 'grid' },
  { id: 'math', label: 'Toán', icon: 'chart' },
  { id: 'lang', label: 'Ngoại ngữ', icon: 'globe' },
  { id: 'code', label: 'Lập trình', icon: 'command' },
  { id: 'science', label: 'Khoa học', icon: 'rocket' },
  { id: 'skill', label: 'Kỹ năng', icon: 'lightbulb' }
];
const activeField = ref<string>('all');
const query = ref<string>('');

const mentors: Mentor[] = [
  {
    id: 1,
    name: 'Nguyễn Minh Quân',
    field: 'Toán cao cấp',
    fieldId: 'math',
    accent: 'cyan',
    rating: 4.9,
    reviews: 312,
    sessions: 1240,
    priceCoin: 150,
    online: true,
    tags: ['Giải tích', 'Đại số', 'Luyện thi'],
    bio: 'Thạc sĩ Toán ứng dụng, 8 năm luyện thi đại học với hơn 1.000 học viên đạt điểm cao.'
  },
  {
    id: 2,
    name: 'Lê Thu Hà',
    field: 'Tiếng Anh học thuật',
    fieldId: 'lang',
    accent: 'magenta',
    rating: 4.8,
    reviews: 287,
    sessions: 980,
    priceCoin: 120,
    online: true,
    tags: ['IELTS', 'TOEIC', 'Writing'],
    bio: 'IELTS 8.5, chuyên gia luyện viết học thuật và phản biện cho kỳ thi quốc tế.'
  },
  {
    id: 3,
    name: 'Trần Đức Anh',
    field: 'Khoa học máy tính',
    fieldId: 'code',
    accent: 'green',
    rating: 4.9,
    reviews: 401,
    sessions: 1530,
    priceCoin: 180,
    online: false,
    tags: ['Thuật toán', 'Python', 'Phỏng vấn'],
    bio: 'Kỹ sư phần mềm cấp cao, đồng hành cùng bạn từ nền tảng đến phỏng vấn công ty lớn.'
  },
  {
    id: 4,
    name: 'Phạm Hải Yến',
    field: 'Vật lý hiện đại',
    fieldId: 'science',
    accent: 'violet',
    rating: 4.7,
    reviews: 156,
    sessions: 620,
    priceCoin: 140,
    online: true,
    tags: ['Cơ học', 'Lượng tử', 'Thí nghiệm'],
    bio: 'Nghiên cứu sinh Vật lý, biến những khái niệm trừu tượng thành ví dụ trực quan.'
  },
  {
    id: 5,
    name: 'Đỗ Khánh Linh',
    field: 'Phương pháp học tập',
    fieldId: 'skill',
    accent: 'amber',
    rating: 5.0,
    reviews: 220,
    sessions: 760,
    priceCoin: 100,
    online: false,
    tags: ['Ghi nhớ', 'Quản lý thời gian', 'Tư duy'],
    bio: 'Huấn luyện viên kỹ năng học, giúp bạn học ít hơn nhưng nhớ lâu và hiệu quả hơn.'
  },
  {
    id: 6,
    name: 'Hoàng Anh Tú',
    field: 'Hoá học hữu cơ',
    fieldId: 'science',
    accent: 'green',
    rating: 4.6,
    reviews: 98,
    sessions: 410,
    priceCoin: 130,
    online: true,
    tags: ['Hữu cơ', 'Phản ứng', 'Luyện thi'],
    bio: 'Giảng viên Hoá, nổi tiếng với sơ đồ chuỗi phản ứng dễ nhớ.'
  }
];

const featured = mentors[0];

const filtered = computed<Mentor[]>(() => {
  const q = query.value.trim().toLowerCase();
  return mentors
    .filter((m) => activeField.value === 'all' || m.fieldId === activeField.value)
    .filter(
      (m) =>
        q === '' ||
        m.name.toLowerCase().includes(q) ||
        m.field.toLowerCase().includes(q) ||
        m.tags.some((t) => t.toLowerCase().includes(q))
    );
});

const stats = [
  { label: 'Cố vấn', value: 240, icon: 'graduation', accent: 'cyan' as Accent },
  { label: 'Buổi đã diễn ra', value: 18600, icon: 'video', accent: 'magenta' as Accent },
  { label: 'Đánh giá trung bình', value: 4.8, decimals: 1, icon: 'star', accent: 'amber' as Accent }
];

// ─── Booking modal ───────────────────────────────────────────────────────────
const booking = ref<Mentor | null>(null);
const days: { id: string; label: string; sub: string }[] = [
  { id: 'd1', label: 'T2', sub: '24/06' },
  { id: 'd2', label: 'T3', sub: '25/06' },
  { id: 'd3', label: 'T4', sub: '26/06' },
  { id: 'd4', label: 'T5', sub: '27/06' },
  { id: 'd5', label: 'T6', sub: '28/06' }
];
const slots: string[] = ['08:00', '09:30', '14:00', '15:30', '19:00', '20:30'];
const selectedDay = ref<string>('d3');
const selectedSlot = ref<string>('');

function openBooking(m: Mentor): void {
  booking.value = m;
  selectedDay.value = 'd3';
  selectedSlot.value = '';
}
function closeBooking(): void {
  booking.value = null;
}
function confirmBooking(): void {
  if (!booking.value || !selectedSlot.value) return;
  const day = days.find((d) => d.id === selectedDay.value);
  const name = booking.value.name;
  closeBooking();
  push(`Đã đặt lịch với ${name} · ${day?.sub} ${selectedSlot.value}`, {
    icon: 'calendar',
    accent: 'green'
  });
  confetti.value?.burst();
}
function message(m: Mentor): void {
  push(`Đã mở cuộc trò chuyện với ${m.name}`, { icon: 'message', accent: 'cyan' });
}

const steps: { icon: string; title: string; text: string; accent: Accent }[] = [
  {
    icon: 'search',
    title: 'Tìm cố vấn',
    text: 'Lọc theo môn học, đánh giá và lĩnh vực chuyên môn.',
    accent: 'cyan'
  },
  {
    icon: 'calendar',
    title: 'Đặt lịch 1-1',
    text: 'Chọn khung giờ phù hợp và xác nhận buổi học.',
    accent: 'magenta'
  },
  {
    icon: 'video',
    title: 'Học trực tuyến',
    text: 'Tham gia buổi cố vấn qua video, ghi chú và đặt câu hỏi.',
    accent: 'green'
  },
  {
    icon: 'star',
    title: 'Đánh giá',
    text: 'Để lại nhận xét giúp cộng đồng tìm được cố vấn tốt.',
    accent: 'amber'
  }
];

const accentChip: Record<Accent, string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  magenta: 'bg-fuchsia-500/15 text-fuchsia-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  violet: 'bg-violet-500/15 text-violet-300',
  amber: 'bg-amber-500/15 text-amber-300'
};
const btnAccent: Record<Accent, string> = {
  cyan: 'border-cyan-400/40 bg-cyan-500/15 text-cyan-200 hover:bg-cyan-500/25',
  magenta: 'border-fuchsia-400/40 bg-fuchsia-500/15 text-fuchsia-200 hover:bg-fuchsia-500/25',
  green: 'border-emerald-400/40 bg-emerald-500/15 text-emerald-200 hover:bg-emerald-500/25',
  violet: 'border-violet-400/40 bg-violet-500/15 text-violet-200 hover:bg-violet-500/25',
  amber: 'border-amber-400/40 bg-amber-500/15 text-amber-200 hover:bg-amber-500/25'
};
</script>

<template>
  <div class="space-y-6">
    <ConfettiBurst ref="confetti" />

    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10">
        <p class="cosmic-badge"><AppIcon name="graduation" class="h-3.5 w-3.5" /> Cố vấn 1-1</p>
        <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
          Học cùng <span class="cosmic-gradient-text">chuyên gia</span> hàng đầu
        </h1>
        <p class="mt-1 max-w-xl text-sm text-muted-foreground">
          Kết nối với cố vấn theo từng môn học, đặt lịch buổi học riêng và bứt phá nhanh hơn.
        </p>
      </div>
    </GlassPanel>

    <!-- STATS -->
    <section class="fx-stagger grid grid-cols-3 gap-3 sm:gap-4">
      <GlassPanel v-for="s in stats" :key="s.label" :glow="s.accent" hover padded>
        <div class="flex items-center gap-3">
          <span
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
            :class="accentChip[s.accent]"
          >
            <AppIcon :name="s.icon" class="h-5 w-5" />
          </span>
          <div class="min-w-0">
            <p class="text-lg font-bold text-foreground sm:text-xl">
              <CountUp :value="s.value" :decimals="s.decimals ?? 0" />
            </p>
            <p class="truncate text-[0.7rem] text-muted-foreground">{{ s.label }}</p>
          </div>
        </div>
      </GlassPanel>
    </section>

    <!-- FEATURED -->
    <TiltSpotlightCard glow="cyan" class="p-5 sm:p-6">
      <div class="flex flex-col gap-5 sm:flex-row sm:items-center">
        <div class="relative shrink-0">
          <Avatar :name="featured.name" :accent="featured.accent" :size="96" />
          <span
            class="absolute -right-1 bottom-1 flex h-6 w-6 items-center justify-center rounded-full border-2 border-[oklch(0.13_0.025_280)] bg-amber-400 text-amber-950"
          >
            <AppIcon name="crown" class="h-3.5 w-3.5" />
          </span>
        </div>
        <div class="min-w-0 flex-1">
          <span class="cosmic-badge">Cố vấn nổi bật</span>
          <h2 class="mt-2 text-xl font-bold text-foreground">{{ featured.name }}</h2>
          <p class="text-sm text-cyan-300">{{ featured.field }}</p>
          <p class="mt-2 text-sm text-muted-foreground">{{ featured.bio }}</p>
          <div class="mt-3 flex flex-wrap items-center gap-4 text-sm">
            <span class="inline-flex items-center gap-1 text-amber-300"
              ><AppIcon name="star" class="h-4 w-4" /> {{ featured.rating }} ({{
                featured.reviews
              }})</span
            >
            <span class="inline-flex items-center gap-1 text-muted-foreground"
              ><AppIcon name="video" class="h-4 w-4" /> {{ featured.sessions }} buổi</span
            >
            <span class="inline-flex items-center gap-1 font-semibold text-amber-300"
              ><AppIcon name="coin" class="h-4 w-4" /> {{ featured.priceCoin }}/buổi</span
            >
          </div>
        </div>
        <div class="flex shrink-0 gap-2 sm:flex-col">
          <button
            type="button"
            class="cosmic-btn inline-flex flex-1 items-center justify-center gap-2 rounded-xl px-5 py-2.5 text-sm"
            @click="openBooking(featured)"
          >
            <AppIcon name="calendar" class="h-4 w-4" /> Đặt lịch
          </button>
          <button
            type="button"
            class="inline-flex flex-1 items-center justify-center gap-2 rounded-xl border border-white/10 px-5 py-2.5 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
            @click="message(featured)"
          >
            <AppIcon name="message" class="h-4 w-4" /> Nhắn tin
          </button>
        </div>
      </div>
    </TiltSpotlightCard>

    <!-- FILTERS -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div class="cosmic-scroll -mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
        <button
          v-for="f in fields"
          :key="f.id"
          type="button"
          class="inline-flex shrink-0 items-center gap-1.5 rounded-xl border px-3.5 py-2 text-sm font-medium transition-all"
          :class="
            activeField === f.id
              ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200 shadow-[inset_0_0_0_1px_oklch(0.78_0.18_195/30%)]'
              : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12] hover:text-foreground'
          "
          @click="activeField = f.id"
        >
          <AppIcon :name="f.icon" class="h-4 w-4" /> {{ f.label }}
        </button>
      </div>
      <div class="relative sm:w-64">
        <span
          class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
          ><AppIcon name="search" class="h-4 w-4"
        /></span>
        <input
          v-model="query"
          type="text"
          placeholder="Tìm cố vấn, môn học..."
          class="cosmic-input w-full !py-2 !pl-9 text-sm"
        />
      </div>
    </div>

    <!-- GRID -->
    <transition-group
      name="mentor"
      tag="div"
      class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3"
    >
      <GlassPanel v-for="m in filtered" :key="m.id" hover class="fx-sheen flex flex-col p-4">
        <div class="flex items-start gap-3">
          <div class="relative shrink-0">
            <Avatar :name="m.name" :accent="m.accent" :size="52" />
            <span
              class="absolute -bottom-0.5 -right-0.5 h-3.5 w-3.5 rounded-full border-2 border-[oklch(0.13_0.025_280)]"
              :class="m.online ? 'fx-glow-pulse bg-emerald-400' : 'bg-slate-500'"
            />
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-semibold text-foreground">{{ m.name }}</p>
            <p class="truncate text-xs text-cyan-300">{{ m.field }}</p>
            <div class="mt-1 flex items-center gap-2 text-xs">
              <span class="inline-flex items-center gap-0.5 text-amber-300"
                ><AppIcon name="star" class="h-3.5 w-3.5" /> {{ m.rating }}</span
              >
              <span class="text-muted-foreground">· {{ m.reviews }} đánh giá</span>
            </div>
          </div>
        </div>

        <div class="mt-3 flex flex-wrap gap-1.5">
          <span
            v-for="tag in m.tags"
            :key="tag"
            class="rounded-full border border-white/[0.07] bg-white/[0.02] px-2 py-0.5 text-[0.7rem] text-muted-foreground"
            >{{ tag }}</span
          >
        </div>

        <div
          class="mt-3 flex items-center justify-between border-t border-white/[0.06] pt-3 text-xs"
        >
          <span class="inline-flex items-center gap-1 text-muted-foreground"
            ><AppIcon name="video" class="h-3.5 w-3.5" /> {{ m.sessions }} buổi</span
          >
          <span class="inline-flex items-center gap-1 font-semibold text-amber-300"
            ><AppIcon name="coin" class="h-3.5 w-3.5" /> {{ m.priceCoin }}/buổi</span
          >
        </div>

        <div class="mt-3 flex gap-2">
          <button
            type="button"
            class="inline-flex flex-1 items-center justify-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-semibold transition-colors"
            :class="btnAccent[m.accent]"
            @click="openBooking(m)"
          >
            <AppIcon name="calendar" class="h-4 w-4" /> Đặt lịch
          </button>
          <button
            type="button"
            class="inline-flex shrink-0 items-center justify-center rounded-xl border border-white/10 px-3 py-2 text-muted-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
            aria-label="Nhắn tin"
            @click="message(m)"
          >
            <AppIcon name="message" class="h-4 w-4" />
          </button>
        </div>
      </GlassPanel>
    </transition-group>

    <p
      v-if="filtered.length === 0"
      class="rounded-2xl border border-dashed border-white/[0.1] py-10 text-center text-sm text-muted-foreground"
    >
      Không tìm thấy cố vấn phù hợp. Thử bỏ bớt bộ lọc nhé!
    </p>

    <!-- HOW IT WORKS -->
    <SectionPanel
      title="Cách hoạt động"
      subtitle="Chỉ 4 bước đơn giản"
      icon="route"
      accent="violet"
    >
      <div class="fx-stagger grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <div
          v-for="(s, i) in steps"
          :key="s.title"
          class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4"
        >
          <div class="flex items-center justify-between">
            <span
              class="flex h-10 w-10 items-center justify-center rounded-xl"
              :class="accentChip[s.accent]"
              ><AppIcon :name="s.icon" class="h-5 w-5"
            /></span>
            <span class="text-2xl font-bold text-white/10">{{ i + 1 }}</span>
          </div>
          <p class="mt-2 text-sm font-semibold text-foreground">{{ s.title }}</p>
          <p class="mt-0.5 text-xs text-muted-foreground">{{ s.text }}</p>
        </div>
      </div>
    </SectionPanel>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>

    <!-- BOOKING MODAL -->
    <transition name="modal">
      <div v-if="booking" class="fixed inset-0 z-[110] flex items-center justify-center p-4 !mt-0">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="closeBooking" />
        <GlassPanel glow="cyan" class="relative z-10 w-full max-w-md p-6">
          <button
            type="button"
            class="absolute right-4 top-4 rounded-lg p-1.5 text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground"
            aria-label="Đóng"
            @click="closeBooking"
          >
            <AppIcon name="x" class="h-5 w-5" />
          </button>
          <div class="flex items-center gap-3">
            <Avatar :name="booking.name" :accent="booking.accent" :size="48" />
            <div>
              <p class="text-sm font-semibold text-foreground">{{ booking.name }}</p>
              <p class="text-xs text-cyan-300">{{ booking.field }}</p>
            </div>
          </div>

          <p class="mt-5 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Chọn ngày
          </p>
          <div class="mt-2 grid grid-cols-5 gap-2">
            <button
              v-for="d in days"
              :key="d.id"
              type="button"
              class="flex flex-col items-center rounded-xl border py-2 text-center transition-all"
              :class="
                selectedDay === d.id
                  ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
                  : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12]'
              "
              @click="selectedDay = d.id"
            >
              <span class="text-sm font-bold">{{ d.label }}</span>
              <span class="text-[0.65rem]">{{ d.sub }}</span>
            </button>
          </div>

          <p class="mt-4 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Chọn giờ
          </p>
          <div class="mt-2 grid grid-cols-3 gap-2">
            <button
              v-for="slot in slots"
              :key="slot"
              type="button"
              class="rounded-xl border py-2 text-sm font-medium transition-all"
              :class="
                selectedSlot === slot
                  ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
                  : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12]'
              "
              @click="selectedSlot = slot"
            >
              {{ slot }}
            </button>
          </div>

          <div
            class="mt-5 flex items-center justify-between rounded-xl border border-white/[0.06] bg-white/[0.02] px-4 py-3"
          >
            <span class="text-sm text-muted-foreground">Chi phí buổi học</span>
            <span class="inline-flex items-center gap-1 text-base font-bold text-amber-300"
              ><AppIcon name="coin" class="h-4 w-4" /> {{ booking.priceCoin }}</span
            >
          </div>

          <button
            type="button"
            class="cosmic-btn mt-4 w-full rounded-xl px-4 py-2.5 text-sm disabled:opacity-50"
            :disabled="!selectedSlot"
            @click="confirmBooking"
          >
            Xác nhận đặt lịch
          </button>
        </GlassPanel>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.mentor-enter-active,
.mentor-leave-active {
  transition:
    opacity 0.35s ease,
    transform 0.35s ease;
}
.mentor-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.mentor-leave-to {
  opacity: 0;
  transform: scale(0.96);
}
.mentor-leave-active {
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
  .mentor-enter-active,
  .mentor-leave-active,
  .modal-enter-active,
  .modal-leave-active {
    transition: none;
  }
}
</style>
