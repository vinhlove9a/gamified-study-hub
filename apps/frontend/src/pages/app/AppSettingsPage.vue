<script setup lang="ts">
import { ref, reactive } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Toggle from '@/components/app/Toggle.vue';
import Avatar from '@/components/app/Avatar.vue';
import MfaEnrollPanel from '@/components/app/MfaEnrollPanel.vue';

// ─── Types ────────────────────────────────────────────────────────────────────

interface NavItem {
  id: string;
  label: string;
  icon: string;
  accent: string;
}

interface NotificationSettings {
  emailNotif: boolean;
  pushNotif: boolean;
  taskReminder: boolean;
  leaderboardUpdate: boolean;
  rewardCoin: boolean;
  weeklyNewsletter: boolean;
}

interface AppearanceSettings {
  reduceMotion: boolean;
}

interface StudySettings {
  studyReminder: boolean;
  autoSound: boolean;
}

interface PrivacySettings {
  publicProfile: boolean;
  showOnLeaderboard: boolean;
  allowFriendInvites: boolean;
}

type ThemeOption = 'dark' | 'light' | 'system';
type AccentColor = 'cyan' | 'fuchsia' | 'emerald' | 'violet' | 'amber';
type DailyGoalOption = 15 | 30 | 45 | 60 | 90 | 120;
type LanguageOption = 'vi' | 'en';

// ─── Nav ──────────────────────────────────────────────────────────────────────

const navItems: NavItem[] = [
  { id: 'account', label: 'Tài khoản', icon: 'user', accent: 'text-cyan-400' },
  { id: 'notifications', label: 'Thông báo', icon: 'bell', accent: 'text-violet-400' },
  { id: 'appearance', label: 'Giao diện', icon: 'palette', accent: 'text-fuchsia-400' },
  { id: 'study', label: 'Học tập', icon: 'graduation', accent: 'text-emerald-400' },
  { id: 'privacy', label: 'Quyền riêng tư', icon: 'shield', accent: 'text-amber-400' },
  { id: 'danger', label: 'Vùng nguy hiểm', icon: 'trash', accent: 'text-rose-400' },
];

const activeSection = ref<string>('account');

function scrollTo(id: string): void {
  activeSection.value = id;
  const el = document.getElementById(`section-${id}`);
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ─── Account ──────────────────────────────────────────────────────────────────

const accountForm = reactive({
  fullName: '',
  email: '',
  displayName: '',
});

// ─── Notifications ────────────────────────────────────────────────────────────

const notifications = reactive<NotificationSettings>({
  emailNotif: true,
  pushNotif: true,
  taskReminder: true,
  leaderboardUpdate: false,
  rewardCoin: true,
  weeklyNewsletter: false,
});

interface NotifRow {
  key: keyof NotificationSettings;
  label: string;
  description: string;
}

const notifRows: NotifRow[] = [
  { key: 'emailNotif', label: 'Email thông báo', description: 'Nhận thông báo qua email về hoạt động tài khoản' },
  { key: 'pushNotif', label: 'Thông báo đẩy', description: 'Nhận thông báo đẩy trực tiếp trên trình duyệt' },
  { key: 'taskReminder', label: 'Nhắc nhở nhiệm vụ', description: 'Nhắc bạn hoàn thành nhiệm vụ hằng ngày' },
  { key: 'leaderboardUpdate', label: 'Cập nhật bảng xếp hạng', description: 'Thông báo khi thứ hạng của bạn thay đổi' },
  { key: 'rewardCoin', label: 'Phần thưởng & coin', description: 'Thông báo khi nhận được coin hoặc phần thưởng mới' },
  { key: 'weeklyNewsletter', label: 'Bản tin hằng tuần', description: 'Tổng hợp hoạt động và gợi ý học tập mỗi tuần' },
];

// ─── Appearance ───────────────────────────────────────────────────────────────

const selectedTheme = ref<ThemeOption>('dark');
const selectedAccent = ref<AccentColor>('cyan');
const appearance = reactive<AppearanceSettings>({ reduceMotion: false });

interface ThemeChoice {
  value: ThemeOption;
  label: string;
  icon: string;
}

const themeChoices: ThemeChoice[] = [
  { value: 'dark', label: 'Tối', icon: 'moon' },
  { value: 'light', label: 'Sáng', icon: 'sun' },
  { value: 'system', label: 'Hệ thống', icon: 'globe' },
];

interface AccentDot {
  value: AccentColor;
  bg: string;
  ring: string;
}

const accentDots: AccentDot[] = [
  { value: 'cyan', bg: 'bg-cyan-400', ring: 'ring-cyan-400' },
  { value: 'fuchsia', bg: 'bg-fuchsia-400', ring: 'ring-fuchsia-400' },
  { value: 'emerald', bg: 'bg-emerald-400', ring: 'ring-emerald-400' },
  { value: 'violet', bg: 'bg-violet-400', ring: 'ring-violet-400' },
  { value: 'amber', bg: 'bg-amber-400', ring: 'ring-amber-400' },
];

// ─── Study ────────────────────────────────────────────────────────────────────

const dailyGoal = ref<DailyGoalOption>(45);
const selectedLanguage = ref<LanguageOption>('vi');
const study = reactive<StudySettings>({ studyReminder: true, autoSound: false });

const goalOptions: DailyGoalOption[] = [15, 30, 45, 60, 90, 120];

// ─── Privacy ─────────────────────────────────────────────────────────────────

const privacy = reactive<PrivacySettings>({
  publicProfile: true,
  showOnLeaderboard: true,
  allowFriendInvites: true,
});

interface PrivacyRow {
  key: keyof PrivacySettings;
  label: string;
  description: string;
}

const privacyRows: PrivacyRow[] = [
  { key: 'publicProfile', label: 'Hồ sơ công khai', description: 'Cho phép người khác xem trang hồ sơ của bạn' },
  { key: 'showOnLeaderboard', label: 'Hiển thị trên bảng xếp hạng', description: 'Tên bạn sẽ xuất hiện trong bảng xếp hạng toàn cộng đồng' },
  { key: 'allowFriendInvites', label: 'Cho phép lời mời kết bạn', description: 'Nhận lời mời kết bạn từ người dùng khác' },
];

// ─── Save ─────────────────────────────────────────────────────────────────────

const saveState = ref<'idle' | 'saving' | 'saved'>('idle');

function handleSave(): void {
  saveState.value = 'saving';
  setTimeout(() => {
    saveState.value = 'saved';
    setTimeout(() => {
      saveState.value = 'idle';
    }, 2000);
  }, 800);
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <GlassPanel glow="violet" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Cài đặt cá nhân</p>
          <h1 class="mt-3 text-2xl font-bold text-foreground sm:text-3xl">
            Cài <span class="cosmic-gradient-text">đặt</span>
          </h1>
          <p class="mt-1.5 text-sm text-muted-foreground">
            Quản lý tài khoản, tuỳ chỉnh giao diện và kiểm soát quyền riêng tư của bạn.
          </p>
        </div>
        <button
          type="button"
          class="cosmic-btn inline-flex shrink-0 items-center gap-2 rounded-sm px-5 py-2.5 text-sm font-semibold"
          :disabled="saveState === 'saving'"
          @click="handleSave"
        >
          <AppIcon
            :name="saveState === 'saved' ? 'check' : 'zap'"
            class="h-4 w-4"
          />
          <span v-if="saveState === 'idle'">Lưu thay đổi</span>
          <span v-else-if="saveState === 'saving'">Đang lưu…</span>
          <span v-else>Đã lưu!</span>
        </button>
      </div>
    </GlassPanel>

    <!-- Body: sidebar nav + sections -->
    <div class="grid gap-6 lg:grid-cols-[220px_1fr]">
      <!-- ── Sticky side nav ── -->
      <aside class="lg:sticky lg:top-6 lg:self-start">
        <div class="rounded-sm border border-white/[0.06] bg-white/[0.02] p-2">
          <nav class="space-y-0.5">
            <button
              v-for="item in navItems"
              :key="item.id"
              type="button"
              class="group flex w-full items-center gap-3 rounded-sm px-3 py-2.5 text-left text-sm font-medium transition-colors"
              :class="
                activeSection === item.id
                  ? 'bg-white/[0.07] text-foreground'
                  : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'
              "
              @click="scrollTo(item.id)"
            >
              <AppIcon
                :name="item.icon"
                class="h-4 w-4 shrink-0 transition-colors"
                :class="activeSection === item.id ? item.accent : 'text-muted-foreground group-hover:' + item.accent"
              />
              {{ item.label }}
              <AppIcon
                v-if="activeSection === item.id"
                name="chevron-right"
                class="ml-auto h-3.5 w-3.5 text-muted-foreground/60"
              />
            </button>
          </nav>
        </div>
      </aside>

      <!-- ── Main settings content ── -->
      <div class="min-w-0 space-y-8">

        <!-- ──────────────── 1. TÀI KHOẢN ──────────────── -->
        <div id="section-account">
          <SectionPanel
            title="Tài khoản"
            subtitle="Thông tin hồ sơ cá nhân của bạn"
            icon="user"
            accent="cyan"
          >
            <!-- Avatar row -->
            <div class="flex flex-col gap-6 sm:flex-row sm:items-start">
              <div class="flex flex-col items-center gap-3 sm:items-start">
                <Avatar name="Người dùng" :size="80" accent="cyan" />
                <button
                  type="button"
                  class="inline-flex items-center gap-1.5 rounded-sm border border-white/10 bg-white/[0.03] px-3.5 py-2 text-xs font-medium text-foreground hover:border-cyan-400/40"
                >
                  <AppIcon name="upload" class="h-3.5 w-3.5" />
                  Đổi ảnh
                </button>
              </div>

              <!-- Fields -->
              <div class="flex-1 space-y-4">
                <div class="grid gap-4 sm:grid-cols-2">
                  <div class="space-y-1.5">
                    <label class="block text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                      Họ và tên
                    </label>
                    <input
                      v-model="accountForm.fullName"
                      type="text"
                      placeholder="Nguyễn Văn A"
                      class="h-11 w-full rounded-sm border border-white/[0.08] bg-white/[0.03] px-3.5 text-sm text-foreground placeholder:text-muted-foreground/70 focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-400/20"
                    />
                  </div>
                  <div class="space-y-1.5">
                    <label class="block text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                      Tên hiển thị
                    </label>
                    <input
                      v-model="accountForm.displayName"
                      type="text"
                      placeholder="nguyen_van_a"
                      class="h-11 w-full rounded-sm border border-white/[0.08] bg-white/[0.03] px-3.5 text-sm text-foreground placeholder:text-muted-foreground/70 focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-400/20"
                    />
                  </div>
                </div>
                <div class="space-y-1.5">
                  <label class="block text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                    Email
                  </label>
                  <input
                    v-model="accountForm.email"
                    type="email"
                    placeholder="nguyen.van.a@example.com"
                    class="h-11 w-full rounded-sm border border-white/[0.08] bg-white/[0.03] px-3.5 text-sm text-foreground placeholder:text-muted-foreground/70 focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-400/20"
                  />
                </div>
                <p class="text-[0.7rem] text-muted-foreground/70">
                  Tên hiển thị sẽ xuất hiện trong bảng xếp hạng và hồ sơ công khai.
                </p>
              </div>
            </div>
          </SectionPanel>
        </div>

        <!-- ──────────────── 2. THÔNG BÁO ──────────────── -->
        <div id="section-notifications">
          <SectionPanel
            title="Thông báo"
            subtitle="Kiểm soát kênh và loại thông báo bạn nhận"
            icon="bell"
            accent="violet"
          >
            <div class="divide-y divide-white/[0.05]">
              <div
                v-for="row in notifRows"
                :key="row.key"
                class="flex items-center justify-between gap-4 py-4 first:pt-0 last:pb-0"
              >
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-semibold text-foreground">{{ row.label }}</p>
                  <p class="mt-0.5 text-xs text-muted-foreground">{{ row.description }}</p>
                </div>
                <Toggle v-model="notifications[row.key]" />
              </div>
            </div>
          </SectionPanel>
        </div>

        <!-- ──────────────── 3. GIAO DIỆN ──────────────── -->
        <div id="section-appearance">
          <SectionPanel
            title="Giao diện"
            subtitle="Chế độ hiển thị và màu sắc tuỳ chỉnh"
            icon="palette"
            accent="magenta"
          >
            <div class="space-y-6">
              <!-- Theme picker -->
              <div>
                <p class="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Chủ đề
                </p>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="choice in themeChoices"
                    :key="choice.value"
                    type="button"
                    class="inline-flex items-center gap-2 rounded-sm border px-4 py-2 text-sm font-medium transition-colors"
                    :class="
                      selectedTheme === choice.value
                        ? 'border-fuchsia-400/60 bg-fuchsia-500/10 text-fuchsia-200'
                        : 'border-white/10 bg-white/[0.03] text-muted-foreground hover:border-white/[0.18] hover:text-foreground'
                    "
                    @click="selectedTheme = choice.value"
                  >
                    <AppIcon :name="choice.icon" class="h-4 w-4" />
                    {{ choice.label }}
                  </button>
                </div>
              </div>

              <!-- Accent picker -->
              <div>
                <p class="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Màu nhấn
                </p>
                <div class="flex items-center gap-3">
                  <button
                    v-for="dot in accentDots"
                    :key="dot.value"
                    type="button"
                    class="h-7 w-7 rounded-sm transition-transform hover:scale-110"
                    :class="[
                      dot.bg,
                      selectedAccent === dot.value
                        ? `ring-2 ring-offset-2 ring-offset-black/80 ${dot.ring} scale-110`
                        : '',
                    ]"
                    :aria-label="dot.value"
                    @click="selectedAccent = dot.value"
                  />
                </div>
              </div>

              <!-- Reduce motion toggle -->
              <div class="flex items-center justify-between gap-4 rounded-sm border border-white/[0.06] bg-white/[0.02] p-4">
                <div>
                  <p class="text-sm font-semibold text-foreground">Giảm chuyển động</p>
                  <p class="mt-0.5 text-xs text-muted-foreground">Tắt hoạt ảnh và hiệu ứng chuyển cảnh trong giao diện</p>
                </div>
                <Toggle v-model="appearance.reduceMotion" />
              </div>
            </div>
          </SectionPanel>
        </div>

        <!-- ──────────────── 4. HỌC TẬP ──────────────── -->
        <div id="section-study">
          <SectionPanel
            title="Học tập"
            subtitle="Mục tiêu, nhắc nhở và tùy chọn học tập"
            icon="graduation"
            accent="green"
          >
            <div class="space-y-6">
              <!-- Daily goal -->
              <div>
                <p class="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Mục tiêu hằng ngày (phút)
                </p>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="opt in goalOptions"
                    :key="opt"
                    type="button"
                    class="rounded-sm border px-4 py-2 text-sm font-semibold tabular-nums transition-colors"
                    :class="
                      dailyGoal === opt
                        ? 'border-emerald-400/60 bg-emerald-500/10 text-emerald-200'
                        : 'border-white/10 bg-white/[0.03] text-muted-foreground hover:border-white/[0.18] hover:text-foreground'
                    "
                    @click="dailyGoal = opt"
                  >
                    {{ opt }}
                  </button>
                </div>
              </div>

              <!-- Language -->
              <div class="space-y-1.5">
                <label class="block text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Ngôn ngữ giao diện
                </label>
                <select
                  v-model="selectedLanguage"
                  class="h-11 w-full rounded-sm border border-white/[0.08] bg-white/[0.03] px-3.5 text-sm text-foreground focus:border-emerald-400/60 focus:outline-none focus:ring-2 focus:ring-emerald-400/20"
                >
                  <option value="vi">Tiếng Việt</option>
                  <option value="en">English</option>
                </select>
              </div>

              <!-- Study toggles -->
              <div class="divide-y divide-white/[0.05] rounded-sm border border-white/[0.06] bg-white/[0.02]">
                <div class="flex items-center justify-between gap-4 p-4">
                  <div>
                    <p class="text-sm font-semibold text-foreground">Nhắc nhở học tập</p>
                    <p class="mt-0.5 text-xs text-muted-foreground">Nhắc bạn bắt đầu phiên học theo thời gian đã đặt</p>
                  </div>
                  <Toggle v-model="study.studyReminder" />
                </div>
                <div class="flex items-center justify-between gap-4 p-4">
                  <div>
                    <p class="text-sm font-semibold text-foreground">Tự động phát âm thanh</p>
                    <p class="mt-0.5 text-xs text-muted-foreground">Phát nhạc nền khi bắt đầu phiên học tập trung</p>
                  </div>
                  <Toggle v-model="study.autoSound" />
                </div>
              </div>
            </div>
          </SectionPanel>
        </div>

        <!-- ──────────────── 5. QUYỀN RIÊNG TƯ ──────────────── -->
        <div id="section-privacy">
          <SectionPanel
            title="Quyền riêng tư"
            subtitle="Kiểm soát thông tin được chia sẻ công khai"
            icon="shield"
            accent="amber"
          >
            <div class="divide-y divide-white/[0.05]">
              <div
                v-for="row in privacyRows"
                :key="row.key"
                class="flex items-center justify-between gap-4 py-4 first:pt-0 last:pb-0"
              >
                <div class="min-w-0 flex-1">
                  <p class="text-sm font-semibold text-foreground">{{ row.label }}</p>
                  <p class="mt-0.5 text-xs text-muted-foreground">{{ row.description }}</p>
                </div>
                <Toggle v-model="privacy[row.key]" />
              </div>
            </div>
          </SectionPanel>
        </div>

        <!-- ──────────────── 5b. BẢO MẬT (2FA) ──────────────── -->
        <div id="section-mfa">
          <SectionPanel
            title="Xác thực hai lớp (2FA)"
            subtitle="Thêm một lớp bảo vệ bằng mã một lần từ ứng dụng xác thực"
            icon="shield"
            accent="violet"
          >
            <MfaEnrollPanel />
          </SectionPanel>
        </div>

        <!-- ──────────────── 6. VÙNG NGUY HIỂM ──────────────── -->
        <div id="section-danger">
          <SectionPanel
            title="Vùng nguy hiểm"
            subtitle="Những thao tác không thể hoàn tác — hãy cân nhắc kỹ"
            icon="trash"
            accent="magenta"
          >
            <div class="rounded-sm border border-rose-500/20 bg-rose-500/[0.04] p-4">
              <div class="mb-4 flex items-start gap-3">
                <span class="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-sm bg-rose-500/15 text-rose-300">
                  <AppIcon name="flag" class="h-4 w-4" />
                </span>
                <div>
                  <p class="text-sm font-semibold text-rose-200">Cảnh báo</p>
                  <p class="mt-0.5 text-xs text-muted-foreground">
                    Các thao tác trong khu vực này sẽ ảnh hưởng nghiêm trọng đến tài khoản của bạn. Vui lòng đọc kỹ trước khi thực hiện.
                    Dữ liệu đã xoá không thể khôi phục.
                  </p>
                </div>
              </div>

              <div class="space-y-3">
                <!-- Sign out all devices -->
                <div class="flex flex-col gap-3 rounded-sm border border-white/[0.06] bg-black/20 p-4 sm:flex-row sm:items-center sm:justify-between">
                  <div class="min-w-0 flex-1">
                    <p class="text-sm font-semibold text-foreground">Đăng xuất tất cả thiết bị</p>
                    <p class="mt-0.5 text-xs text-muted-foreground">
                      Kết thúc phiên đăng nhập trên toàn bộ thiết bị đang đăng nhập. Bạn sẽ bị đăng xuất ngay trên thiết bị hiện tại.
                    </p>
                  </div>
                  <button
                    type="button"
                    class="inline-flex shrink-0 items-center gap-2 rounded-sm border border-white/10 bg-white/[0.03] px-4 py-2 text-sm font-medium text-foreground hover:border-cyan-400/40"
                  >
                    <AppIcon name="logout" class="h-4 w-4" />
                    Đăng xuất tất cả
                  </button>
                </div>

                <!-- Delete account -->
                <div class="flex flex-col gap-3 rounded-sm border border-rose-500/20 bg-black/20 p-4 sm:flex-row sm:items-center sm:justify-between">
                  <div class="min-w-0 flex-1">
                    <p class="text-sm font-semibold text-rose-200">Xoá tài khoản</p>
                    <p class="mt-0.5 text-xs text-muted-foreground">
                      Xoá vĩnh viễn tài khoản và toàn bộ dữ liệu của bạn. Hành động này không thể hoàn tác.
                    </p>
                  </div>
                  <button
                    type="button"
                    class="inline-flex shrink-0 items-center gap-2 rounded-sm border border-rose-500/40 bg-rose-500/10 px-4 py-2 text-sm font-medium text-rose-300 hover:bg-rose-500/20"
                  >
                    <AppIcon name="trash" class="h-4 w-4" />
                    Xoá tài khoản
                  </button>
                </div>
              </div>
            </div>
          </SectionPanel>
        </div>

        <!-- Floating save footer (mobile-friendly) -->
        <div class="sticky bottom-6 flex justify-end pt-2">
          <button
            type="button"
            class="cosmic-btn inline-flex items-center gap-2 rounded-sm px-6 py-3 text-sm font-semibold"
            :disabled="saveState === 'saving'"
            @click="handleSave"
          >
            <AppIcon
              :name="saveState === 'saved' ? 'check-circle' : 'zap'"
              class="h-4 w-4"
            />
            <span v-if="saveState === 'idle'">Lưu thay đổi</span>
            <span v-else-if="saveState === 'saving'">Đang lưu…</span>
            <span v-else>Đã lưu thành công!</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
