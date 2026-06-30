<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';

// ─── Types ────────────────────────────────────────────────────────────────────

type Role = 'user' | 'ai';

interface ChatMessage {
  id: number;
  role: Role;
  text: string;
  streaming?: boolean;
}

interface Conversation {
  id: string;
  title: string;
  preview: string;
  timestamp: string;
  unread?: boolean;
}

interface SuggestedPrompt {
  id: string;
  icon: string;
  label: string;
  prompt: string;
}

// ─── State ────────────────────────────────────────────────────────────────────

const conversations = ref<Conversation[]>([
  {
    id: 'c1',
    title: 'Ôn tập Giải tích chương 3',
    preview: 'Giúp mình tóm tắt phần giới hạn...',
    timestamp: 'Hôm nay · 09:24',
    unread: true,
  },
  {
    id: 'c2',
    title: 'Dàn ý tiểu luận Lịch sử',
    preview: 'Tạo dàn ý cho bài viết về...',
    timestamp: 'Hôm qua · 21:10',
  },
  {
    id: 'c3',
    title: 'Câu hỏi trắc nghiệm Hoá hữu cơ',
    preview: 'Tạo 20 câu hỏi ôn thi THPT',
    timestamp: '23/06 · 14:02',
  },
  {
    id: 'c4',
    title: 'Giải thích thuật toán sắp xếp',
    preview: 'So sánh QuickSort và MergeSort',
    timestamp: '21/06 · 08:45',
  },
  {
    id: 'c5',
    title: 'Lộ trình học tiếng Anh B2',
    preview: 'Gợi ý kế hoạch 8 tuần',
    timestamp: '18/06 · 19:30',
  },
]);

const activeConversationId = ref<string>('c1');

const suggestedPrompts: SuggestedPrompt[] = [
  {
    id: 's1',
    icon: 'file-text',
    label: 'Tóm tắt tài liệu này',
    prompt: 'Tóm tắt tài liệu mình đang đọc thành các ý chính ngắn gọn.',
  },
  {
    id: 's2',
    icon: 'list',
    label: 'Tạo dàn ý ôn tập',
    prompt: 'Tạo cho mình một dàn ý ôn tập có cấu trúc rõ ràng cho chủ đề này.',
  },
  {
    id: 's3',
    icon: 'lightbulb',
    label: 'Giải thích khái niệm…',
    prompt: 'Giải thích khái niệm này một cách dễ hiểu kèm ví dụ thực tế.',
  },
  {
    id: 's4',
    icon: 'help',
    label: 'Tạo bộ câu hỏi',
    prompt: 'Tạo cho mình một bộ câu hỏi trắc nghiệm để tự kiểm tra kiến thức.',
  },
];

const messages = ref<ChatMessage[]>([
  {
    id: 1,
    role: 'user',
    text: 'Chào bạn, giúp mình ôn lại khái niệm giới hạn của hàm số nhé.',
  },
  {
    id: 2,
    role: 'ai',
    text: 'Chắc chắn rồi! Giới hạn của hàm số mô tả giá trị mà hàm số tiến tới khi biến số tiến gần một điểm. Bạn muốn mình bắt đầu từ định nghĩa trực quan hay đi thẳng vào các quy tắc tính giới hạn?',
  },
  {
    id: 3,
    role: 'user',
    text: 'Cho mình định nghĩa trực quan trước đã.',
  },
  {
    id: 4,
    role: 'ai',
    text: 'Hiểu một cách trực quan: khi x càng tiến sát tới điểm a, nếu giá trị f(x) càng tiến sát tới một số L cố định, thì ta nói giới hạn của f(x) khi x tiến tới a bằng L. Hãy tưởng tượng bạn đi bộ tới gần một bức tường — vị trí của bạn là L, còn mỗi bước chân là một giá trị x gần dần.',
  },
]);

const inputText = ref('');
const isTyping = ref(false);
const threadRef = ref<HTMLElement | null>(null);

let messageId = 5;
let typingTimeout: ReturnType<typeof setTimeout> | null = null;
let streamInterval: ReturnType<typeof setInterval> | null = null;

// Canned replies cycled through to keep the demo lively.
const cannedReplies: string[] = [
  'Câu hỏi hay đấy! Để mình phân tích từng bước. Đầu tiên, ta cần xác định rõ dữ kiện đã cho, sau đó áp dụng công thức phù hợp và cuối cùng kiểm tra lại kết quả. Bạn muốn mình minh hoạ bằng một ví dụ cụ thể không?',
  'Mình hiểu rồi. Dưới đây là cách tiếp cận gợi ý: chia nhỏ vấn đề thành các phần dễ quản lý, ghi chú các điểm mấu chốt, rồi luyện tập với bài tập tương tự để củng cố. Hãy cho mình biết phần nào bạn thấy khó nhất nhé.',
  'Tuyệt vời! Đây là tóm tắt ngắn gọn cho bạn: ý chính được nhấn mạnh ở phần đầu, các ví dụ minh hoạ nằm ở giữa, và phần kết luận tổng hợp lại toàn bộ. Mình có thể tạo thêm thẻ ghi nhớ nếu bạn cần ôn nhanh.',
];
let replyIndex = 0;

// ─── Derived ──────────────────────────────────────────────────────────────────

const activeConversation = computed<Conversation | undefined>(() =>
  conversations.value.find((c) => c.id === activeConversationId.value),
);

const canSend = computed(() => inputText.value.trim().length > 0 && !isTyping.value);

// ─── Helpers ──────────────────────────────────────────────────────────────────

async function scrollToBottom(): Promise<void> {
  await nextTick();
  const el = threadRef.value;
  if (el) {
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
  }
}

function clearTimers(): void {
  if (typingTimeout) {
    clearTimeout(typingTimeout);
    typingTimeout = null;
  }
  if (streamInterval) {
    clearInterval(streamInterval);
    streamInterval = null;
  }
}

function streamReply(): void {
  const reply = cannedReplies[replyIndex % cannedReplies.length] ?? '';
  replyIndex += 1;
  const words = reply.split(' ');
  let wordPos = 0;

  const aiMessage: ChatMessage = { id: messageId++, role: 'ai', text: '', streaming: true };
  messages.value.push(aiMessage);

  streamInterval = setInterval(() => {
    if (wordPos >= words.length) {
      aiMessage.streaming = false;
      if (streamInterval) {
        clearInterval(streamInterval);
        streamInterval = null;
      }
      return;
    }
    aiMessage.text += (aiMessage.text ? ' ' : '') + words[wordPos];
    wordPos += 1;
    void scrollToBottom();
  }, 55);
}

function dispatchUserMessage(text: string): void {
  const trimmed = text.trim();
  if (!trimmed || isTyping.value) return;

  messages.value.push({ id: messageId++, role: 'user', text: trimmed });
  inputText.value = '';
  void scrollToBottom();

  isTyping.value = true;
  typingTimeout = setTimeout(() => {
    isTyping.value = false;
    streamReply();
  }, 1100);
}

// ─── Handlers ─────────────────────────────────────────────────────────────────

function sendMessage(): void {
  if (!canSend.value) return;
  dispatchUserMessage(inputText.value);
}

function handleEnter(event: KeyboardEvent): void {
  if (event.shiftKey) return;
  event.preventDefault();
  sendMessage();
}

function usePrompt(prompt: SuggestedPrompt): void {
  dispatchUserMessage(prompt.prompt);
}

function selectConversation(id: string): void {
  activeConversationId.value = id;
  const conv = conversations.value.find((c) => c.id === id);
  if (conv) conv.unread = false;
}

function startNewConversation(): void {
  clearTimers();
  isTyping.value = false;
  const id = `c${Date.now()}`;
  conversations.value.unshift({
    id,
    title: 'Cuộc trò chuyện mới',
    preview: 'Bắt đầu hỏi Study AI bất cứ điều gì…',
    timestamp: 'Vừa xong',
  });
  activeConversationId.value = id;
  messages.value = [
    {
      id: messageId++,
      role: 'ai',
      text: 'Xin chào! Mình là Study AI. Bạn muốn ôn tập, tóm tắt tài liệu hay luyện đề hôm nay? Hãy chọn một gợi ý hoặc nhập câu hỏi của bạn.',
    },
  ];
  void scrollToBottom();
}

function clearConversation(): void {
  clearTimers();
  isTyping.value = false;
  messages.value = [];
}

onUnmounted(() => {
  clearTimers();
});
</script>

<template>
  <div class="space-y-6">
    <!-- ── Page header ─────────────────────────────────────────────────────── -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="cosmic-badge">Trợ lý thông minh</p>
        <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">
          Trợ lý <span class="cosmic-gradient-text">AI</span>
        </h1>
        <p class="mt-1 text-sm text-muted-foreground">
          Hỏi đáp, tóm tắt và luyện đề cùng Study AI — người bạn học tập của bạn.
        </p>
      </div>
      <div class="flex shrink-0 items-center gap-2">
        <span
          class="inline-flex items-center gap-1.5 rounded-sm border border-violet-400/30 bg-violet-500/10 px-3 py-1.5 text-xs font-semibold text-violet-200"
        >
          <AppIcon name="sparkles" class="h-3.5 w-3.5" />
          Study AI · v2
        </span>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-3 py-2 text-sm text-muted-foreground hover:border-rose-400/40 hover:text-rose-200"
          @click="clearConversation"
        >
          <AppIcon name="trash" class="h-4 w-4" />
          <span class="hidden sm:inline">Xoá hội thoại</span>
        </button>
      </div>
    </div>

    <!-- ── Two-column layout ───────────────────────────────────────────────── -->
    <div class="grid grid-cols-1 gap-5 lg:grid-cols-[300px_minmax(0,1fr)]">
      <!-- ── Sidebar ──────────────────────────────────────────────────────── -->
      <aside class="space-y-4">
        <button
          type="button"
          class="cosmic-btn flex w-full items-center justify-center gap-2 rounded-sm px-4 py-2.5 text-sm"
          @click="startNewConversation"
        >
          <AppIcon name="plus" class="h-4 w-4" />
          Cuộc trò chuyện mới
        </button>

        <!-- Recent chats -->
        <div class="glass-panel overflow-hidden rounded-sm">
          <p class="px-4 pb-2 pt-3.5 text-[0.7rem] font-semibold uppercase tracking-wider text-muted-foreground">
            Gần đây
          </p>
          <div class="cosmic-scroll max-h-[260px] space-y-1 overflow-y-auto px-2 pb-2 lg:max-h-[300px]">
            <button
              v-for="conv in conversations"
              :key="conv.id"
              type="button"
              class="group flex w-full flex-col gap-0.5 rounded-sm border px-3 py-2.5 text-left transition-colors"
              :class="
                conv.id === activeConversationId
                  ? 'border-cyan-400/30 bg-cyan-500/10'
                  : 'border-transparent hover:border-white/[0.08] hover:bg-white/[0.03]'
              "
              @click="selectConversation(conv.id)"
            >
              <div class="flex items-center justify-between gap-2">
                <span
                  class="truncate text-sm font-medium"
                  :class="conv.id === activeConversationId ? 'text-cyan-100' : 'text-foreground'"
                >
                  {{ conv.title }}
                </span>
                <span
                  v-if="conv.unread"
                  class="h-2 w-2 shrink-0 rounded-sm bg-cyan-400"
                />
              </div>
              <span class="truncate text-xs text-muted-foreground">{{ conv.preview }}</span>
              <span class="text-[0.68rem] text-muted-foreground/60">{{ conv.timestamp }}</span>
            </button>
          </div>
        </div>

        <!-- Suggested prompts -->
        <div class="glass-panel overflow-hidden rounded-sm p-3">
          <p class="px-1 pb-2 text-[0.7rem] font-semibold uppercase tracking-wider text-muted-foreground">
            Gợi ý nhanh
          </p>
          <div class="flex flex-col gap-2">
            <button
              v-for="prompt in suggestedPrompts"
              :key="prompt.id"
              type="button"
              class="flex items-center gap-2.5 rounded-sm border border-white/[0.06] bg-white/[0.02] px-3 py-2 text-left text-sm text-muted-foreground transition-colors hover:border-violet-400/30 hover:bg-violet-500/10 hover:text-violet-100"
              @click="usePrompt(prompt)"
            >
              <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm bg-violet-500/10 text-violet-300">
                <AppIcon :name="prompt.icon" class="h-4 w-4" />
              </span>
              {{ prompt.label }}
            </button>
          </div>
        </div>
      </aside>

      <!-- ── Chat panel ───────────────────────────────────────────────────── -->
      <section class="glass-panel flex h-[600px] flex-col overflow-hidden rounded-sm">
        <!-- Chat header -->
        <header class="flex items-center justify-between gap-3 border-b border-white/[0.06] px-4 py-3">
          <div class="flex min-w-0 items-center gap-3">
            <span class="orb-avatar flex h-9 w-9 shrink-0 items-center justify-center rounded-sm text-white">
              <AppIcon name="bot" class="h-5 w-5" />
            </span>
            <div class="min-w-0">
              <p class="truncate text-sm font-semibold text-foreground">
                {{ activeConversation?.title ?? 'Study AI' }}
              </p>
              <p class="flex items-center gap-1.5 text-xs text-emerald-300">
                <span class="h-1.5 w-1.5 rounded-sm bg-emerald-400" /> Đang hoạt động
              </p>
            </div>
          </div>
          <span class="inline-flex items-center gap-1.5 rounded-sm bg-white/[0.04] px-2.5 py-1 text-xs text-muted-foreground">
            <AppIcon name="zap" class="h-3.5 w-3.5 text-amber-300" />
            Study AI · v2
          </span>
        </header>

        <!-- Thread -->
        <div ref="threadRef" class="cosmic-scroll flex-1 space-y-4 overflow-y-auto px-4 py-5">
          <!-- Empty state -->
          <div
            v-if="messages.length === 0"
            class="flex h-full flex-col items-center justify-center text-center"
          >
            <span class="orb-avatar flex h-16 w-16 items-center justify-center rounded-sm text-white">
              <AppIcon name="bot" class="h-8 w-8" />
            </span>
            <p class="mt-4 text-base font-semibold text-foreground">Bắt đầu cuộc trò chuyện</p>
            <p class="mt-1.5 max-w-sm text-sm text-muted-foreground">
              Chọn một gợi ý nhanh ở bên trái hoặc nhập câu hỏi của bạn vào ô bên dưới.
            </p>
          </div>

          <!-- Messages -->
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="flex items-end gap-2.5"
            :class="msg.role === 'user' ? 'flex-row-reverse' : ''"
          >
            <span
              v-if="msg.role === 'ai'"
              class="orb-avatar flex h-8 w-8 shrink-0 items-center justify-center rounded-sm text-white"
            >
              <AppIcon name="bot" class="h-4 w-4" />
            </span>
            <div
              class="max-w-[78%] rounded-sm px-4 py-2.5 text-sm leading-relaxed"
              :class="
                msg.role === 'user'
                  ? 'rounded-sm from-cyan-500/25 to-blue-600/20 text-cyan-50'
                  : 'rounded-sm border border-white/[0.06] bg-white/[0.03] text-foreground'
              "
            >
              {{ msg.text }}<span v-if="msg.streaming" class="ml-0.5 inline-block h-3.5 w-1.5 animate-pulse rounded-sm bg-cyan-300 align-middle" />
            </div>
          </div>

          <!-- Typing indicator -->
          <div v-if="isTyping" class="flex items-end gap-2.5">
            <span class="orb-avatar flex h-8 w-8 shrink-0 items-center justify-center rounded-sm text-white">
              <AppIcon name="bot" class="h-4 w-4" />
            </span>
            <div class="flex items-center gap-1.5 rounded-sm rounded-sm border border-white/[0.06] bg-white/[0.03] px-4 py-3">
              <span class="typing-dot h-2 w-2 rounded-sm bg-muted-foreground" style="animation-delay: 0ms" />
              <span class="typing-dot h-2 w-2 rounded-sm bg-muted-foreground" style="animation-delay: 160ms" />
              <span class="typing-dot h-2 w-2 rounded-sm bg-muted-foreground" style="animation-delay: 320ms" />
            </div>
          </div>
        </div>

        <!-- Composer -->
        <div class="border-t border-white/[0.06] p-3">
          <div class="flex items-end gap-2">
            <textarea
              v-model="inputText"
              rows="1"
              placeholder="Nhập câu hỏi của bạn… (Enter để gửi, Shift+Enter để xuống dòng)"
              class="cosmic-input max-h-32 min-h-[44px] flex-1 resize-none py-3"
              @keydown.enter="handleEnter"
            />
            <button
              type="button"
              class="cosmic-btn flex h-[44px] w-[44px] shrink-0 items-center justify-center rounded-sm"
              :disabled="!canSend"
              aria-label="Gửi tin nhắn"
              @click="sendMessage"
            >
              <AppIcon name="arrow-up" class="h-5 w-5" />
            </button>
          </div>
          <p class="mt-2 px-1 text-[0.7rem] text-muted-foreground/60">
            Study AI có thể mắc lỗi. Hãy kiểm tra lại các thông tin quan trọng.
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.orb-avatar {
  background: linear-gradient(135deg, oklch(0.78 0.18 195), oklch(0.6 0.2 290), oklch(0.68 0.22 350));
  background-size: 200% 200%;
  box-shadow: 0 0 18px oklch(0.6 0.2 290 / 45%);
  animation: orb-pulse 4s ease-in-out infinite;
}

@keyframes orb-pulse {
  0%,
  100% {
    background-position: 0% 50%;
    box-shadow: 0 0 16px oklch(0.6 0.2 290 / 35%);
  }
  50% {
    background-position: 100% 50%;
    box-shadow: 0 0 26px oklch(0.78 0.18 195 / 55%);
  }
}

.typing-dot {
  display: inline-block;
  animation: typing-bounce 1.2s ease-in-out infinite;
}

@keyframes typing-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}
</style>
