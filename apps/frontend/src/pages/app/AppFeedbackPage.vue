<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import ConfettiBurst from '@/components/app/ConfettiBurst.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type FbType = 'praise' | 'bug' | 'idea' | 'other';
type Status = 'reviewing' | 'planned' | 'building' | 'done';

const { push } = useToast();
const confetti = ref<InstanceType<typeof ConfettiBurst> | null>(null);

const types: { id: FbType; label: string; icon: string; accent: Accent }[] = [
  { id: 'praise', label: 'Khen ngợi', icon: 'heart', accent: 'magenta' },
  { id: 'bug', label: 'Báo lỗi', icon: 'bug', accent: 'amber' },
  { id: 'idea', label: 'Đề xuất', icon: 'lightbulb', accent: 'cyan' },
  { id: 'other', label: 'Khác', icon: 'message', accent: 'violet' }
];
const fbType = ref<FbType>('idea');

const rating = ref<number>(0);
const hoverRating = ref<number>(0);
const ratingLabels = ['', 'Tệ', 'Chưa tốt', 'Ổn', 'Tốt', 'Tuyệt vời!'];
const shownRating = computed<number>(() => hoverRating.value || rating.value);

const message = ref<string>('');
const email = ref<string>('');
const submitted = ref<boolean>(false);

const canSubmit = computed<boolean>(() => message.value.trim().length >= 10);

function submit(): void {
  if (!canSubmit.value) {
    push('Vui lòng nhập ít nhất 10 ký tự mô tả', { icon: 'alert', accent: 'rose' });
    return;
  }
  submitted.value = true;
  push('Cảm ơn bạn đã gửi phản hồi!', { icon: 'check-circle', accent: 'green' });
  confetti.value?.burst();
}

function reset(): void {
  submitted.value = false;
  rating.value = 0;
  hoverRating.value = 0;
  message.value = '';
  email.value = '';
  fbType.value = 'idea';
}

interface RoadmapItem {
  id: number;
  title: string;
  desc: string;
  status: Status;
  votes: number;
  voted: boolean;
}
const roadmap = ref<RoadmapItem[]>([
  {
    id: 1,
    title: 'Chế độ học nhóm thời gian thực',
    desc: 'Cùng học và thi đua trực tiếp với bạn bè trong một phòng.',
    status: 'building',
    votes: 342,
    voted: false
  },
  {
    id: 2,
    title: 'Trợ lý AI tạo flashcard tự động',
    desc: 'Sinh thẻ ghi nhớ từ bất kỳ tài liệu nào chỉ với một chạm.',
    status: 'planned',
    votes: 287,
    voted: true
  },
  {
    id: 3,
    title: 'Ứng dụng di động iOS & Android',
    desc: 'Mang Study Hub theo bạn mọi nơi với thông báo đẩy.',
    status: 'reviewing',
    votes: 521,
    voted: false
  },
  {
    id: 4,
    title: 'Chủ đề giao diện tuỳ chỉnh',
    desc: 'Cá nhân hoá màu sắc và hình nền không gian của bạn.',
    status: 'done',
    votes: 198,
    voted: false
  }
]);

function toggleVote(item: RoadmapItem): void {
  item.voted = !item.voted;
  item.votes += item.voted ? 1 : -1;
  if (item.voted) push('Đã bình chọn cho tính năng này', { icon: 'thumbs-up', accent: 'cyan' });
}

const statusMeta: Record<Status, { label: string; cls: string }> = {
  reviewing: { label: 'Đang xem xét', cls: 'bg-white/[0.06] text-muted-foreground' },
  planned: { label: 'Đã lên kế hoạch', cls: 'bg-violet-500/15 text-violet-300' },
  building: { label: 'Đang phát triển', cls: 'bg-cyan-500/15 text-cyan-300' },
  done: { label: 'Đã hoàn thành', cls: 'bg-emerald-500/15 text-emerald-300' }
};

const typeBtn: Record<Accent, string> = {
  cyan: 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200',
  magenta: 'border-fuchsia-400/50 bg-fuchsia-500/15 text-fuchsia-200',
  green: 'border-emerald-400/50 bg-emerald-500/15 text-emerald-200',
  violet: 'border-violet-400/50 bg-violet-500/15 text-violet-200',
  amber: 'border-amber-400/50 bg-amber-500/15 text-amber-200'
};
</script>

<template>
  <div class="space-y-6">
    <ConfettiBurst ref="confetti" />

    <!-- HERO -->
    <GlassPanel class="fx-aurora p-5 sm:p-6">
      <div class="relative z-10 flex items-center gap-4">
        <span
          class="fx-float flex h-14 w-14 items-center justify-center rounded-sm from-cyan-500/30 to-fuchsia-500/20 text-cyan-200"
        >
          <AppIcon name="megaphone" class="h-7 w-7" />
        </span>
        <div>
          <p class="cosmic-badge">Tiếng nói của bạn</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
            Cùng <span class="cosmic-gradient-text">kiến tạo</span> Study Hub
          </h1>
          <p class="mt-1 text-sm text-muted-foreground">
            Mọi góp ý đều được đội ngũ đọc và trân trọng.
          </p>
        </div>
      </div>
    </GlassPanel>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-5">
      <!-- FORM -->
      <div class="lg:col-span-3">
        <GlassPanel glow="cyan" class="p-5 sm:p-6">
          <!-- Success state -->
          <div v-if="submitted" class="py-8 text-center">
            <span
              class="float-slow mx-auto flex h-20 w-20 items-center justify-center rounded-sm from-emerald-500/30 to-cyan-500/20 text-emerald-300"
            >
              <AppIcon name="check-circle" class="h-10 w-10" />
            </span>
            <h2 class="mt-5 text-xl font-bold text-foreground">Đã gửi thành công! 🎉</h2>
            <p class="mx-auto mt-2 max-w-sm text-sm text-muted-foreground">
              Cảm ơn bạn đã dành thời gian chia sẻ. Phản hồi của bạn giúp Study Hub ngày càng tốt
              hơn.
            </p>
            <div class="mt-5 flex flex-wrap justify-center gap-2">
              <button
                type="button"
                class="cosmic-btn inline-flex items-center gap-2 rounded-sm px-4 py-2.5 text-sm"
                @click="reset"
              >
                <AppIcon name="edit" class="h-4 w-4" /> Gửi phản hồi khác
              </button>
              <RouterLink
                to="/app"
                class="inline-flex items-center gap-2 rounded-sm border border-white/10 px-4 py-2.5 text-sm font-medium text-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
              >
                <AppIcon name="home" class="h-4 w-4" /> Về trang chủ
              </RouterLink>
            </div>
          </div>

          <!-- Form -->
          <form v-else @submit.prevent="submit">
            <p class="text-sm font-semibold text-foreground">Loại phản hồi</p>
            <div class="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-4">
              <button
                v-for="t in types"
                :key="t.id"
                type="button"
                class="flex flex-col items-center gap-1.5 rounded-sm border py-3 text-xs font-medium transition-all"
                :class="
                  fbType === t.id
                    ? typeBtn[t.accent]
                    : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:border-white/[0.12]'
                "
                @click="fbType = t.id"
              >
                <AppIcon :name="t.icon" class="h-5 w-5" /> {{ t.label }}
              </button>
            </div>

            <p class="mt-5 text-sm font-semibold text-foreground">Bạn hài lòng đến mức nào?</p>
            <div class="mt-2 flex items-center gap-2">
              <div class="flex items-center gap-1" @mouseleave="hoverRating = 0">
                <button
                  v-for="n in 5"
                  :key="n"
                  type="button"
                  class="rounded-sm p-1 transition-transform hover:scale-110"
                  :aria-label="`${n} sao`"
                  @mouseenter="hoverRating = n"
                  @click="rating = n"
                >
                  <AppIcon
                    name="star"
                    class="h-7 w-7 transition-colors"
                    :class="
                      n <= shownRating ? 'text-amber-300 [&_path]:fill-current' : 'text-white/20'
                    "
                  />
                </button>
              </div>
              <span v-if="shownRating" class="text-sm font-medium text-amber-300">{{
                ratingLabels[shownRating]
              }}</span>
            </div>

            <label class="mt-5 block text-sm font-semibold text-foreground" for="fb-message"
              >Nội dung chi tiết</label
            >
            <textarea
              id="fb-message"
              v-model="message"
              rows="5"
              placeholder="Hãy mô tả cụ thể góp ý, lỗi gặp phải hoặc ý tưởng của bạn..."
              class="cosmic-input mt-2 w-full resize-none"
            />
            <p
              class="mt-1 text-xs"
              :class="canSubmit ? 'text-emerald-300' : 'text-muted-foreground'"
            >
              {{ message.trim().length }}/10 ký tự tối thiểu
            </p>

            <label class="mt-4 block text-sm font-semibold text-foreground" for="fb-email"
              >Email (tuỳ chọn)</label
            >
            <input
              id="fb-email"
              v-model="email"
              type="email"
              placeholder="email@cua-ban.com — để chúng tôi phản hồi"
              class="cosmic-input mt-2 w-full"
            />

            <div class="mt-5 flex items-center gap-3">
              <button
                type="submit"
                class="cosmic-btn inline-flex items-center gap-2 rounded-sm px-5 py-2.5 text-sm disabled:opacity-50"
                :disabled="!canSubmit"
              >
                <AppIcon name="send" class="h-4 w-4" /> Gửi phản hồi
              </button>
              <span class="inline-flex items-center gap-1.5 text-xs text-muted-foreground">
                <AppIcon name="shield" class="h-4 w-4" /> Phản hồi của bạn được bảo mật
              </span>
            </div>
          </form>
        </GlassPanel>
      </div>

      <!-- ROADMAP -->
      <div class="lg:col-span-2">
        <SectionPanel
          title="Lộ trình tính năng"
          subtitle="Bình chọn điều bạn mong muốn"
          icon="route"
          accent="violet"
        >
          <ul class="space-y-3">
            <li
              v-for="item in roadmap"
              :key="item.id"
              class="rounded-sm border border-white/[0.06] bg-white/[0.02] p-3.5"
            >
              <div class="flex items-start gap-3">
                <button
                  type="button"
                  class="flex w-12 shrink-0 flex-col items-center rounded-sm border py-1.5 transition-all"
                  :class="
                    item.voted
                      ? 'border-cyan-400/50 bg-cyan-500/15 text-cyan-200'
                      : 'border-white/[0.08] bg-white/[0.02] text-muted-foreground hover:border-cyan-400/30'
                  "
                  @click="toggleVote(item)"
                >
                  <AppIcon name="arrow-up" class="h-4 w-4" />
                  <span class="text-xs font-bold tabular-nums">{{ item.votes }}</span>
                </button>
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-semibold text-foreground">{{ item.title }}</p>
                  <p class="mt-0.5 text-xs text-muted-foreground">{{ item.desc }}</p>
                  <span
                    class="mt-2 inline-block rounded-sm px-2 py-0.5 text-[0.65rem] font-medium"
                    :class="statusMeta[item.status].cls"
                  >
                    {{ statusMeta[item.status].label }}
                  </span>
                </div>
              </div>
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>
