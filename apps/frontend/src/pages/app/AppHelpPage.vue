<script setup lang="ts">
import { ref, computed } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import { useToast } from '@/composables/useToast';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

const { push } = useToast();

const query = ref<string>('');

interface Category {
  id: string;
  label: string;
  icon: string;
  count: number;
  accent: Accent;
}
const categories: Category[] = [
  { id: 'start', label: 'Bắt đầu', icon: 'rocket', count: 12, accent: 'cyan' },
  { id: 'account', label: 'Tài khoản', icon: 'user', count: 9, accent: 'magenta' },
  { id: 'coin', label: 'Coin & Thưởng', icon: 'coin', count: 14, accent: 'amber' },
  { id: 'learn', label: 'Học tập', icon: 'book-open', count: 18, accent: 'green' },
  { id: 'tech', label: 'Kỹ thuật', icon: 'settings', count: 7, accent: 'violet' },
  { id: 'privacy', label: 'Bảo mật', icon: 'shield', count: 6, accent: 'cyan' }
];

interface Faq {
  id: number;
  q: string;
  a: string;
}
const faqs: Faq[] = [
  {
    id: 1,
    q: 'Làm thế nào để kiếm coin trên Study Hub?',
    a: 'Bạn nhận coin khi hoàn thành nhiệm vụ hằng ngày, đọc tài liệu, làm quiz, giữ chuỗi học và điểm danh. Coin dùng để mở khoá tài liệu VIP và vật phẩm trong cửa hàng.'
  },
  {
    id: 2,
    q: 'Chuỗi học (streak) hoạt động ra sao?',
    a: 'Mỗi ngày bạn học ít nhất 1 hoạt động, chuỗi sẽ tăng thêm 1. Bỏ lỡ một ngày sẽ làm chuỗi trở về 0 — trừ khi bạn dùng vật phẩm "băng đóng chuỗi".'
  },
  {
    id: 3,
    q: 'Tôi có thể học offline không?',
    a: 'Có. Vào mục Tải xuống để lưu tài liệu, podcast và video về máy. Tiến độ học offline sẽ tự đồng bộ khi có mạng trở lại.'
  },
  {
    id: 4,
    q: 'Làm sao để đổi mật khẩu?',
    a: 'Vào Cài đặt → Bảo mật → Đổi mật khẩu. Bạn cần nhập mật khẩu hiện tại và mật khẩu mới tối thiểu 8 ký tự.'
  },
  {
    id: 5,
    q: 'Huy hiệu và chứng chỉ khác nhau thế nào?',
    a: 'Huy hiệu ghi nhận các cột mốc và thành tích nhỏ. Chứng chỉ được cấp khi bạn hoàn thành trọn một khoá học hoặc lộ trình, có thể tải về và chia sẻ.'
  },
  {
    id: 6,
    q: 'Tôi gặp lỗi khi tải tài liệu, phải làm gì?',
    a: 'Hãy kiểm tra kết nối mạng, thử dọn bộ nhớ đệm trong mục Tải xuống, hoặc đăng xuất rồi đăng nhập lại. Nếu vẫn lỗi, liên hệ đội hỗ trợ.'
  },
  {
    id: 7,
    q: 'Cố vấn 1-1 tính phí như thế nào?',
    a: 'Mỗi buổi cố vấn có mức coin riêng do cố vấn đặt. Bạn thanh toán bằng coin khi đặt lịch và có thể đánh giá sau buổi học.'
  }
];

const filteredFaqs = computed<Faq[]>(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return faqs;
  return faqs.filter((f) => f.q.toLowerCase().includes(q) || f.a.toLowerCase().includes(q));
});

const openId = ref<number | null>(1);
function toggle(id: number): void {
  openId.value = openId.value === id ? null : id;
}

interface Contact {
  id: string;
  title: string;
  text: string;
  icon: string;
  accent: Accent;
  action: string;
  toast: string;
}
const contacts: Contact[] = [
  {
    id: 'chat',
    title: 'Trợ lý AI',
    text: 'Hỏi đáp tức thì, 24/7',
    icon: 'bot',
    accent: 'cyan',
    action: 'Trò chuyện',
    toast: 'Đang mở trợ lý AI...'
  },
  {
    id: 'mail',
    title: 'Email hỗ trợ',
    text: 'hotro@studyhub.vn',
    icon: 'mail',
    accent: 'magenta',
    action: 'Sao chép email',
    toast: 'Đã sao chép email hỗ trợ'
  },
  {
    id: 'phone',
    title: 'Tổng đài',
    text: '1900 6868 · 8h-22h',
    icon: 'headset',
    accent: 'green',
    action: 'Gọi ngay',
    toast: 'Đang kết nối tổng đài...'
  }
];
function contact(c: Contact): void {
  push(c.toast, { icon: c.icon, accent: c.accent });
}

const guides: { id: number; title: string; icon: string; accent: Accent }[] = [
  { id: 1, title: 'Hướng dẫn cho người mới bắt đầu', icon: 'rocket', accent: 'cyan' },
  { id: 2, title: 'Tối ưu chuỗi học và nhiệm vụ', icon: 'flame', accent: 'amber' },
  { id: 3, title: 'Cách dùng thẻ ghi nhớ hiệu quả', icon: 'layers', accent: 'magenta' },
  { id: 4, title: 'Mẹo tập trung với Pomodoro', icon: 'clock', accent: 'violet' }
];

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
    <!-- HERO + SEARCH -->
    <GlassPanel class="fx-aurora p-6 text-center sm:p-10">
      <div class="relative z-10">
        <p class="cosmic-badge mx-auto">
          <AppIcon name="help" class="h-3.5 w-3.5" /> Trung tâm trợ giúp
        </p>
        <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
          Chúng tôi có thể <span class="cosmic-gradient-text">giúp gì</span> cho bạn?
        </h1>
        <p class="mt-2 text-sm text-muted-foreground">
          Tìm câu trả lời nhanh hoặc liên hệ đội ngũ hỗ trợ.
        </p>
        <div class="relative mx-auto mt-6 max-w-xl">
          <span
            class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground"
            ><AppIcon name="search" class="h-5 w-5"
          /></span>
          <input
            v-model="query"
            type="text"
            placeholder="Tìm câu hỏi, ví dụ: làm sao kiếm coin..."
            class="cosmic-input w-full !py-3.5 !pl-12 text-sm"
          />
        </div>
      </div>
    </GlassPanel>

    <!-- STATUS -->
    <div
      class="flex items-center gap-3 rounded-2xl border border-emerald-400/20 bg-emerald-500/[0.07] px-4 py-3"
    >
      <span class="fx-glow-pulse flex h-2.5 w-2.5 shrink-0 rounded-full bg-emerald-400" />
      <p class="text-sm text-emerald-200">Tất cả hệ thống đang hoạt động bình thường.</p>
      <span class="ml-auto text-xs text-muted-foreground">Cập nhật 2 phút trước</span>
    </div>

    <!-- CATEGORIES -->
    <section class="fx-stagger grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
      <GlassPanel
        v-for="cat in categories"
        :key="cat.id"
        hover
        class="fx-lift cursor-pointer p-4 text-center"
      >
        <span
          class="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl"
          :class="accentChip[cat.accent]"
        >
          <AppIcon :name="cat.icon" class="h-6 w-6" />
        </span>
        <p class="mt-2.5 text-sm font-semibold text-foreground">{{ cat.label }}</p>
        <p class="text-[0.7rem] text-muted-foreground">{{ cat.count }} bài viết</p>
      </GlassPanel>
    </section>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- FAQ -->
      <div class="lg:col-span-2">
        <SectionPanel
          title="Câu hỏi thường gặp"
          :subtitle="`${filteredFaqs.length} câu hỏi`"
          icon="message"
          accent="cyan"
        >
          <ul class="space-y-2.5">
            <li
              v-for="f in filteredFaqs"
              :key="f.id"
              class="overflow-hidden rounded-xl border border-white/[0.06] bg-white/[0.02]"
            >
              <button
                type="button"
                class="flex w-full items-center gap-3 px-4 py-3.5 text-left"
                @click="toggle(f.id)"
              >
                <span class="text-sm font-semibold text-foreground">{{ f.q }}</span>
                <AppIcon
                  name="chevron-down"
                  class="ml-auto h-4 w-4 shrink-0 text-muted-foreground transition-transform duration-300"
                  :class="openId === f.id ? 'rotate-180 text-cyan-300' : ''"
                />
              </button>
              <transition name="faq">
                <div v-if="openId === f.id" class="px-4 pb-4">
                  <p class="text-sm leading-relaxed text-muted-foreground">{{ f.a }}</p>
                </div>
              </transition>
            </li>
          </ul>
          <p
            v-if="filteredFaqs.length === 0"
            class="py-6 text-center text-sm text-muted-foreground"
          >
            Không tìm thấy câu hỏi phù hợp. Thử từ khoá khác hoặc liên hệ hỗ trợ.
          </p>
        </SectionPanel>
      </div>

      <!-- CONTACT + GUIDES -->
      <div class="space-y-6">
        <SectionPanel title="Liên hệ hỗ trợ" icon="headset" accent="green">
          <ul class="space-y-2.5">
            <li
              v-for="c in contacts"
              :key="c.id"
              class="flex items-center gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] p-3"
            >
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
                :class="accentChip[c.accent]"
              >
                <AppIcon :name="c.icon" class="h-5 w-5" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-semibold text-foreground">{{ c.title }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ c.text }}</p>
              </div>
              <button
                type="button"
                class="shrink-0 rounded-lg border border-white/10 px-3 py-1.5 text-xs font-medium text-foreground transition-colors hover:border-cyan-400/40 hover:text-cyan-200"
                @click="contact(c)"
              >
                {{ c.action }}
              </button>
            </li>
          </ul>
        </SectionPanel>

        <SectionPanel title="Hướng dẫn phổ biến" icon="book-open" accent="violet">
          <ul class="space-y-1">
            <li v-for="g in guides" :key="g.id">
              <button
                type="button"
                class="flex w-full items-center gap-3 rounded-lg px-2 py-2.5 text-left transition-colors hover:bg-white/[0.04]"
              >
                <span
                  class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg"
                  :class="accentChip[g.accent]"
                >
                  <AppIcon :name="g.icon" class="h-4 w-4" />
                </span>
                <span class="flex-1 text-sm text-foreground">{{ g.title }}</span>
                <AppIcon name="chevron-right" class="h-4 w-4 text-muted-foreground" />
              </button>
            </li>
          </ul>
        </SectionPanel>
      </div>
    </div>

    <!-- CTA -->
    <GlassPanel
      glow="cyan"
      class="flex flex-col items-center gap-3 p-6 text-center sm:flex-row sm:justify-between sm:text-left"
    >
      <div>
        <p class="text-base font-semibold text-foreground">Vẫn chưa tìm được câu trả lời?</p>
        <p class="text-sm text-muted-foreground">
          Gửi phản hồi cho đội ngũ — chúng tôi luôn lắng nghe.
        </p>
      </div>
      <RouterLink
        to="/app/feedback"
        class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-xl px-5 py-2.5 text-sm"
      >
        <AppIcon name="send" class="h-4 w-4" /> Gửi phản hồi
      </RouterLink>
    </GlassPanel>

    <p class="pt-1 text-center text-xs text-muted-foreground/70">
      Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.
    </p>
  </div>
</template>

<style scoped>
.faq-enter-active,
.faq-leave-active {
  transition:
    opacity 0.25s ease,
    max-height 0.3s ease;
  overflow: hidden;
  max-height: 200px;
}
.faq-enter-from,
.faq-leave-to {
  opacity: 0;
  max-height: 0;
}
@media (prefers-reduced-motion: reduce) {
  .faq-enter-active,
  .faq-leave-active {
    transition: none;
  }
}
</style>
