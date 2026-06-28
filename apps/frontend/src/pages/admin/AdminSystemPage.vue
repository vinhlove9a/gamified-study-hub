<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdminPill from '@/components/admin/AdminPill.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type TabKey = 'general' | 'flags' | 'maintenance' | 'audit' | 'team' | 'integrations';
const tabs: { key: TabKey; label: string; icon: string }[] = [
  { key: 'general', label: 'Cài đặt chung', icon: 'sliders' },
  { key: 'flags', label: 'Cờ tính năng', icon: 'flag' },
  { key: 'maintenance', label: 'Bảo trì', icon: 'shield' },
  { key: 'audit', label: 'Nhật ký kiểm toán', icon: 'list' },
  { key: 'team', label: 'Đội ngũ admin', icon: 'users' },
  { key: 'integrations', label: 'Tích hợp', icon: 'command' }
];
const tab = ref<TabKey>('general');

// ── General settings ──────────────────────────────────────────────────────────
const general = reactive({
  siteName: 'Gamified Study Hub',
  tagline: 'Học tập game hoá — chinh phục tri thức',
  language: 'vi',
  timezone: 'Asia/Ho_Chi_Minh',
  allowRegistration: true,
  requireEmailVerify: true,
  allowGoogleLogin: true,
  darkByDefault: true
});

// ── Feature flags ───────────────────────────────────────────────────────────────
interface Flag {
  id: string;
  name: string;
  desc: string;
  env: 'Production' | 'Staging';
  rollout: number;
  enabled: boolean;
}
const flags = ref<Flag[]>([
  { id: 'ai-assistant', name: 'Trợ lý AI', desc: 'Gợi ý học tập & tóm tắt tài liệu bằng AI', env: 'Production', rollout: 100, enabled: true },
  { id: 'spin-wheel', name: 'Vòng quay may mắn', desc: 'Mini-game thưởng coin hằng ngày', env: 'Production', rollout: 80, enabled: true },
  { id: 'video-groups', name: 'Nhóm học video', desc: 'Phòng học nhóm có gọi video', env: 'Staging', rollout: 25, enabled: false },
  { id: 'vnpay', name: 'Thanh toán VNPay', desc: 'Cổng thanh toán VNPay cho gói VIP', env: 'Production', rollout: 100, enabled: true },
  { id: 'season-board', name: 'Bảng xếp hạng mùa', desc: 'Bảng xếp hạng theo mùa kèm phần thưởng', env: 'Production', rollout: 60, enabled: true },
  { id: 'ai-flashcard', name: 'Flashcard AI', desc: 'Tự sinh thẻ ghi nhớ từ tài liệu', env: 'Staging', rollout: 40, enabled: true },
  { id: 'focus-mode', name: 'Chế độ tập trung', desc: 'Ẩn nhiễu, hẹn giờ Pomodoro', env: 'Production', rollout: 100, enabled: true },
  { id: 'google-login', name: 'Đăng nhập Google', desc: 'OAuth 2.0 với tài khoản Google', env: 'Production', rollout: 100, enabled: true }
]);

// ── Maintenance ───────────────────────────────────────────────────────────────
const maintenance = reactive({
  enabled: false,
  window: '2026-07-02T02:00',
  message: 'Hệ thống đang được nâng cấp để phục vụ bạn tốt hơn. Vui lòng quay lại sau ít phút.'
});
const resources = [
  { label: 'CPU', value: 34, accent: 'cyan' as const, unit: '34%' },
  { label: 'Bộ nhớ (RAM)', value: 61, accent: 'violet' as const, unit: '4.8 / 8 GB' },
  { label: 'Đĩa', value: 73, accent: 'amber' as const, unit: '2.1 / 3 TB' },
  { label: 'Băng thông', value: 22, accent: 'green' as const, unit: '220 Mbps' }
];
const cacheOps = [
  { icon: 'refresh', label: 'Xoá bộ nhớ đệm', desc: 'Xoá toàn bộ cache Redis & CDN' },
  { icon: 'search', label: 'Xây lại chỉ mục tìm kiếm', desc: 'Reindex toàn bộ tài liệu' },
  { icon: 'repeat', label: 'Khởi động lại worker', desc: 'Khởi động lại hàng đợi xử lý nền' }
];

// ── Audit log ───────────────────────────────────────────────────────────────────
interface AuditRow {
  admin: string;
  initials: string;
  action: string;
  target: string;
  ip: string;
  time: string;
  tone: 'cyan' | 'green' | 'amber' | 'rose' | 'violet';
}
const auditLog: AuditRow[] = [
  { admin: 'Trần Vĩnh', initials: 'TV', action: 'Khoá tài khoản', target: '@spam_user_204', ip: '14.231.88.10', time: '2 phút trước', tone: 'rose' },
  { admin: 'Lê Hồng Anh', initials: 'LA', action: 'Duyệt tài liệu', target: 'Đề cương Giải tích 2', ip: '171.244.12.5', time: '15 phút trước', tone: 'green' },
  { admin: 'Trần Vĩnh', initials: 'TV', action: 'Sửa cờ tính năng', target: 'Flashcard AI → 40%', ip: '14.231.88.10', time: '38 phút trước', tone: 'amber' },
  { admin: 'Phạm Quốc Bảo', initials: 'QB', action: 'Gỡ nội dung', target: 'Bài viết forum #8821', ip: '113.161.40.2', time: '1 giờ trước', tone: 'violet' },
  { admin: 'Lê Hồng Anh', initials: 'LA', action: 'Cảnh cáo người dùng', target: '@hoangnam', ip: '171.244.12.5', time: '1 giờ trước', tone: 'amber' },
  { admin: 'Trần Vĩnh', initials: 'TV', action: 'Tạo nhiệm vụ', target: 'Học liền 7 ngày', ip: '14.231.88.10', time: '2 giờ trước', tone: 'cyan' },
  { admin: 'Nguyễn Thu Hà', initials: 'TH', action: 'Hoàn tiền giao dịch', target: '#TXN-90241 · ₫299K', ip: '42.118.9.77', time: '3 giờ trước', tone: 'violet' },
  { admin: 'Phạm Quốc Bảo', initials: 'QB', action: 'Duyệt tài liệu', target: '500 từ vựng TOEIC', ip: '113.161.40.2', time: '3 giờ trước', tone: 'green' },
  { admin: 'Trần Vĩnh', initials: 'TV', action: 'Bật chế độ bảo trì', target: 'Cửa sổ 02:00 02/07', ip: '14.231.88.10', time: '5 giờ trước', tone: 'rose' },
  { admin: 'Lê Hồng Anh', initials: 'LA', action: 'Mời quản trị viên', target: 'editor@studyhub.vn', ip: '171.244.12.5', time: 'Hôm qua', tone: 'cyan' },
  { admin: 'Nguyễn Thu Hà', initials: 'TH', action: 'Đổi vai trò', target: '@minhanh → VIP', ip: '42.118.9.77', time: 'Hôm qua', tone: 'amber' },
  { admin: 'Trần Vĩnh', initials: 'TV', action: 'Cập nhật tích hợp', target: 'SMTP Email', ip: '14.231.88.10', time: '2 ngày trước', tone: 'green' }
];
const auditQuery = ref('');
const filteredAudit = computed(() => {
  const q = auditQuery.value.trim().toLowerCase();
  if (!q) return auditLog;
  return auditLog.filter(
    (r) => r.action.toLowerCase().includes(q) || r.admin.toLowerCase().includes(q) || r.target.toLowerCase().includes(q)
  );
});
const auditTone: Record<AuditRow['tone'], string> = {
  cyan: 'bg-cyan-500/15 text-cyan-300',
  green: 'bg-emerald-500/15 text-emerald-300',
  amber: 'bg-amber-500/15 text-amber-300',
  rose: 'bg-rose-500/15 text-rose-300',
  violet: 'bg-violet-500/15 text-violet-300'
};

// ── Admin team ────────────────────────────────────────────────────────────────
interface Member {
  name: string;
  initials: string;
  email: string;
  role: 'Super Admin' | 'Quản trị' | 'Kiểm duyệt viên';
  online: boolean;
  last: string;
}
const team: Member[] = [
  { name: 'Trần Vĩnh', initials: 'TV', email: 'vinh@studyhub.vn', role: 'Super Admin', online: true, last: 'Đang hoạt động' },
  { name: 'Lê Hồng Anh', initials: 'LA', email: 'honganh@studyhub.vn', role: 'Quản trị', online: true, last: 'Đang hoạt động' },
  { name: 'Phạm Quốc Bảo', initials: 'QB', email: 'quocbao@studyhub.vn', role: 'Kiểm duyệt viên', online: false, last: '12 phút trước' },
  { name: 'Nguyễn Thu Hà', initials: 'TH', email: 'thuha@studyhub.vn', role: 'Quản trị', online: false, last: '1 giờ trước' },
  { name: 'Đỗ Minh Khôi', initials: 'MK', email: 'khoi@studyhub.vn', role: 'Kiểm duyệt viên', online: true, last: 'Đang hoạt động' },
  { name: 'Vũ Thanh Tùng', initials: 'TT', email: 'tung@studyhub.vn', role: 'Kiểm duyệt viên', online: false, last: 'Hôm qua' }
];
const roleTone: Record<Member['role'], 'magenta' | 'cyan' | 'violet'> = {
  'Super Admin': 'magenta',
  'Quản trị': 'cyan',
  'Kiểm duyệt viên': 'violet'
};

// ── Integrations ────────────────────────────────────────────────────────────────
interface Integration {
  icon: string;
  name: string;
  desc: string;
  status: 'connected' | 'unconfigured' | 'error';
}
const integrations: Integration[] = [
  { icon: 'mail', name: 'SMTP Email', desc: 'Gửi email giao dịch & thông báo', status: 'connected' },
  { icon: 'globe', name: 'Google OAuth', desc: 'Đăng nhập bằng tài khoản Google', status: 'connected' },
  { icon: 'zap', name: 'Redis Cache', desc: 'Bộ nhớ đệm & hàng đợi', status: 'connected' },
  { icon: 'folder', name: 'Lưu trữ S3', desc: 'Lưu tài liệu & ảnh người dùng', status: 'connected' },
  { icon: 'coin', name: 'Momo', desc: 'Cổng thanh toán ví Momo', status: 'unconfigured' },
  { icon: 'coin', name: 'VNPay', desc: 'Cổng thanh toán VNPay', status: 'connected' },
  { icon: 'send', name: 'Webhook', desc: 'Gửi sự kiện tới hệ thống ngoài', status: 'error' }
];
const intStatus: Record<Integration['status'], { tone: 'green' | 'neutral' | 'rose'; label: string }> = {
  connected: { tone: 'green', label: 'Đã kết nối' },
  unconfigured: { tone: 'neutral', label: 'Chưa cấu hình' },
  error: { tone: 'rose', label: 'Lỗi kết nối' }
};

const flagsOn = computed(() => flags.value.filter((f) => f.enabled).length);
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">Vận hành · Cấu hình hệ thống</p>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Hệ thống</h2>
      </div>
      <div class="flex items-center gap-2">
        <AdminPill tone="green" dot>{{ flagsOn }}/{{ flags.length }} tính năng bật</AdminPill>
        <AdminPill :tone="maintenance.enabled ? 'rose' : 'cyan'" dot>
          {{ maintenance.enabled ? 'Đang bảo trì' : 'Trực tuyến' }}
        </AdminPill>
      </div>
    </div>

    <!-- Maintenance banner -->
    <transition name="banner">
      <div
        v-if="maintenance.enabled"
        class="flex items-center gap-3 rounded-2xl border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-amber-200"
      >
        <AppIcon name="alert" class="h-5 w-5 shrink-0" />
        <p class="text-sm font-medium">Hệ thống đang ở <span class="font-bold">chế độ bảo trì</span> — người dùng thường không thể truy cập.</p>
      </div>
    </transition>

    <div class="grid grid-cols-1 gap-4 lg:grid-cols-[240px_1fr]">
      <!-- Tab rail -->
      <nav class="glass-panel h-fit rounded-2xl p-2">
        <button
          v-for="t in tabs"
          :key="t.key"
          class="group flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition-colors"
          :class="tab === t.key ? 'bg-white/[0.07] text-foreground' : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'"
          @click="tab = t.key"
        >
          <AppIcon :name="t.icon" class="h-5 w-5" :class="tab === t.key ? 'text-cyan-300' : ''" />
          <span class="flex-1 text-left">{{ t.label }}</span>
          <AppIcon v-if="tab === t.key" name="chevron-right" class="h-4 w-4 text-cyan-300" />
        </button>
      </nav>

      <!-- Tab content -->
      <div class="min-w-0 space-y-4">
        <!-- GENERAL -->
        <template v-if="tab === 'general'">
          <AdminPanel title="Thông tin nền tảng" subtitle="Hiển thị trên toàn hệ thống" icon="sliders" accent="cyan">
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <label class="block">
                <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Tên nền tảng</span>
                <input v-model="general.siteName" class="cosmic-input" />
              </label>
              <label class="block">
                <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Khẩu hiệu</span>
                <input v-model="general.tagline" class="cosmic-input" />
              </label>
              <label class="block">
                <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Ngôn ngữ mặc định</span>
                <select v-model="general.language" class="cosmic-input">
                  <option value="vi">Tiếng Việt</option>
                  <option value="en">English</option>
                </select>
              </label>
              <label class="block">
                <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Múi giờ</span>
                <select v-model="general.timezone" class="cosmic-input">
                  <option value="Asia/Ho_Chi_Minh">(GMT+7) Hồ Chí Minh</option>
                  <option value="Asia/Bangkok">(GMT+7) Bangkok</option>
                  <option value="UTC">UTC</option>
                </select>
              </label>
            </div>
          </AdminPanel>

          <AdminPanel title="Tuỳ chọn tài khoản" icon="user" accent="violet">
            <ul class="divide-y divide-white/[0.06]">
              <li class="flex items-center justify-between gap-4 py-3">
                <div>
                  <p class="text-sm font-medium text-foreground">Cho phép đăng ký</p>
                  <p class="text-xs text-muted-foreground">Người dùng mới có thể tự tạo tài khoản</p>
                </div>
                <button type="button" role="switch" :aria-checked="general.allowRegistration" class="admin-switch" :class="general.allowRegistration ? 'on' : ''" @click="general.allowRegistration = !general.allowRegistration"><span /></button>
              </li>
              <li class="flex items-center justify-between gap-4 py-3">
                <div>
                  <p class="text-sm font-medium text-foreground">Bắt buộc xác minh email</p>
                  <p class="text-xs text-muted-foreground">Yêu cầu xác nhận email trước khi dùng</p>
                </div>
                <button type="button" role="switch" :aria-checked="general.requireEmailVerify" class="admin-switch" :class="general.requireEmailVerify ? 'on' : ''" @click="general.requireEmailVerify = !general.requireEmailVerify"><span /></button>
              </li>
              <li class="flex items-center justify-between gap-4 py-3">
                <div>
                  <p class="text-sm font-medium text-foreground">Cho phép đăng nhập Google</p>
                  <p class="text-xs text-muted-foreground">Bật OAuth 2.0 với Google</p>
                </div>
                <button type="button" role="switch" :aria-checked="general.allowGoogleLogin" class="admin-switch" :class="general.allowGoogleLogin ? 'on' : ''" @click="general.allowGoogleLogin = !general.allowGoogleLogin"><span /></button>
              </li>
              <li class="flex items-center justify-between gap-4 py-3">
                <div>
                  <p class="text-sm font-medium text-foreground">Chế độ tối mặc định</p>
                  <p class="text-xs text-muted-foreground">Giao diện mặc định cho người dùng mới</p>
                </div>
                <button type="button" role="switch" :aria-checked="general.darkByDefault" class="admin-switch" :class="general.darkByDefault ? 'on' : ''" @click="general.darkByDefault = !general.darkByDefault"><span /></button>
              </li>
            </ul>
            <div class="mt-4 flex justify-end">
              <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
                <AppIcon name="check" class="h-4 w-4" /> Lưu thay đổi
              </button>
            </div>
          </AdminPanel>
        </template>

        <!-- FEATURE FLAGS -->
        <template v-else-if="tab === 'flags'">
          <AdminPanel title="Cờ tính năng" :subtitle="`${flagsOn} đang bật trên ${flags.length}`" icon="flag" accent="green" flush>
            <ul class="divide-y divide-white/[0.06]">
              <li v-for="f in flags" :key="f.id" class="flex items-center gap-4 px-5 py-4">
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-2">
                    <p class="truncate text-sm font-medium text-foreground">{{ f.name }}</p>
                    <AdminPill :tone="f.env === 'Production' ? 'cyan' : 'amber'">{{ f.env }}</AdminPill>
                  </div>
                  <p class="mt-0.5 truncate text-xs text-muted-foreground">{{ f.desc }}</p>
                </div>
                <div class="hidden w-40 shrink-0 sm:block">
                  <div class="mb-1 flex justify-between text-[11px]">
                    <span class="text-muted-foreground">Triển khai</span>
                    <span class="font-mono font-semibold text-foreground">{{ f.rollout }}%</span>
                  </div>
                  <ProgressBar :value="f.rollout" :accent="f.enabled ? 'green' : 'violet'" />
                </div>
                <button type="button" role="switch" :aria-checked="f.enabled" class="admin-switch shrink-0" :class="f.enabled ? 'on' : ''" @click="f.enabled = !f.enabled"><span /></button>
              </li>
            </ul>
          </AdminPanel>
        </template>

        <!-- MAINTENANCE -->
        <template v-else-if="tab === 'maintenance'">
          <AdminPanel title="Chế độ bảo trì" icon="shield" :accent="maintenance.enabled ? 'amber' : 'cyan'" :glow="maintenance.enabled ? 'magenta' : 'none'">
            <div class="flex items-center justify-between gap-4 rounded-xl border border-white/[0.06] bg-white/[0.02] p-4">
              <div>
                <p class="text-sm font-semibold text-foreground">Bật chế độ bảo trì</p>
                <p class="text-xs text-muted-foreground">Tạm khoá truy cập của người dùng thường để nâng cấp</p>
              </div>
              <button type="button" role="switch" :aria-checked="maintenance.enabled" class="admin-switch admin-switch--lg" :class="maintenance.enabled ? 'on on--warn' : ''" @click="maintenance.enabled = !maintenance.enabled"><span /></button>
            </div>
            <div class="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <label class="block">
                <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Lịch bảo trì</span>
                <input v-model="maintenance.window" type="datetime-local" class="cosmic-input" />
              </label>
              <label class="block sm:col-span-2">
                <span class="mb-1.5 block text-xs font-medium text-muted-foreground">Thông báo hiển thị cho người dùng</span>
                <textarea v-model="maintenance.message" rows="3" class="cosmic-input resize-none" />
              </label>
            </div>
          </AdminPanel>

          <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <AdminPanel title="Tài nguyên máy chủ" subtitle="Thời gian thực" icon="chart" accent="violet">
              <ul class="space-y-3">
                <li v-for="r in resources" :key="r.label">
                  <div class="mb-1 flex items-center justify-between text-xs">
                    <span class="text-muted-foreground">{{ r.label }}</span>
                    <span class="font-mono font-semibold text-foreground">{{ r.unit }}</span>
                  </div>
                  <ProgressBar :value="r.value" :accent="r.accent" />
                </li>
              </ul>
            </AdminPanel>

            <AdminPanel title="Bộ nhớ đệm & tác vụ nền" icon="refresh" accent="cyan">
              <ul class="space-y-2">
                <li
                  v-for="op in cacheOps"
                  :key="op.label"
                  class="flex items-center gap-3 rounded-xl border border-white/[0.06] bg-white/[0.02] px-3 py-2.5"
                >
                  <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-cyan-500/10 text-cyan-300">
                    <AppIcon :name="op.icon" class="h-4 w-4" />
                  </span>
                  <div class="min-w-0 flex-1">
                    <p class="text-sm font-medium text-foreground">{{ op.label }}</p>
                    <p class="truncate text-xs text-muted-foreground">{{ op.desc }}</p>
                  </div>
                  <button class="rounded-lg border border-white/[0.08] bg-white/[0.03] px-3 py-1.5 text-xs font-medium text-foreground transition-colors hover:bg-white/[0.07]">
                    Chạy
                  </button>
                </li>
              </ul>
            </AdminPanel>
          </div>
        </template>

        <!-- AUDIT LOG -->
        <template v-else-if="tab === 'audit'">
          <AdminPanel title="Nhật ký kiểm toán" subtitle="Mọi thao tác của quản trị viên" icon="list" accent="cyan" flush>
            <template #actions>
              <label class="relative flex items-center">
                <AppIcon name="search" class="pointer-events-none absolute left-2.5 h-4 w-4 text-muted-foreground" />
                <input v-model="auditQuery" type="search" placeholder="Tìm hành động…" class="cosmic-input !w-56 !py-2 !pl-9 text-sm" />
              </label>
            </template>
            <div class="cosmic-scroll overflow-x-auto">
              <table class="w-full min-w-[680px] text-left text-sm">
                <thead>
                  <tr class="border-b border-white/[0.06] text-[11px] uppercase tracking-wider text-muted-foreground">
                    <th class="px-5 py-3 font-medium">Quản trị viên</th>
                    <th class="px-5 py-3 font-medium">Hành động</th>
                    <th class="px-5 py-3 font-medium">Đối tượng</th>
                    <th class="px-5 py-3 font-medium">IP</th>
                    <th class="px-5 py-3 text-right font-medium">Thời gian</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-white/[0.04]">
                  <tr v-for="(r, i) in filteredAudit" :key="i" class="transition-colors hover:bg-white/[0.03]">
                    <td class="px-5 py-3">
                      <div class="flex items-center gap-2.5">
                        <span class="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-cyan-500 to-fuchsia-500 text-[10px] font-bold text-white">{{ r.initials }}</span>
                        <span class="font-medium text-foreground">{{ r.admin }}</span>
                      </div>
                    </td>
                    <td class="px-5 py-3">
                      <span class="inline-flex rounded-md px-2 py-0.5 text-xs font-medium" :class="auditTone[r.tone]">{{ r.action }}</span>
                    </td>
                    <td class="px-5 py-3 text-muted-foreground">{{ r.target }}</td>
                    <td class="px-5 py-3 font-mono text-xs text-muted-foreground">{{ r.ip }}</td>
                    <td class="px-5 py-3 text-right font-mono text-xs text-muted-foreground">{{ r.time }}</td>
                  </tr>
                  <tr v-if="!filteredAudit.length">
                    <td colspan="5" class="px-5 py-10 text-center text-sm text-muted-foreground">Không có bản ghi phù hợp.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </AdminPanel>
        </template>

        <!-- ADMIN TEAM -->
        <template v-else-if="tab === 'team'">
          <AdminPanel title="Đội ngũ quản trị" :subtitle="`${team.length} thành viên`" icon="users" accent="magenta">
            <template #actions>
              <button class="cosmic-btn flex items-center gap-2 rounded-xl px-3 py-1.5 text-xs">
                <AppIcon name="plus" class="h-4 w-4" /> Mời thành viên
              </button>
            </template>
            <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
              <div
                v-for="m in team"
                :key="m.email"
                class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:bg-white/[0.04]"
              >
                <div class="flex items-start gap-3">
                  <div class="relative shrink-0">
                    <span class="flex h-11 w-11 items-center justify-center rounded-full bg-gradient-to-br from-cyan-500 to-fuchsia-500 text-sm font-bold text-white">{{ m.initials }}</span>
                    <span class="absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-full border-2 border-[oklch(0.12_0.03_280)]" :class="m.online ? 'bg-emerald-400' : 'bg-white/30'" />
                  </div>
                  <div class="min-w-0 flex-1">
                    <p class="truncate text-sm font-semibold text-foreground">{{ m.name }}</p>
                    <p class="truncate text-xs text-muted-foreground">{{ m.email }}</p>
                  </div>
                </div>
                <div class="mt-3 flex items-center justify-between">
                  <AdminPill :tone="roleTone[m.role]">{{ m.role }}</AdminPill>
                  <span class="font-mono text-[10px] text-muted-foreground">{{ m.last }}</span>
                </div>
              </div>
            </div>
          </AdminPanel>
        </template>

        <!-- INTEGRATIONS -->
        <template v-else>
          <AdminPanel title="Tích hợp dịch vụ" subtitle="Kết nối hệ thống bên thứ ba" icon="command" accent="cyan">
            <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
              <div
                v-for="it in integrations"
                :key="it.name"
                class="flex flex-col rounded-xl border border-white/[0.06] bg-white/[0.02] p-4 transition-colors hover:bg-white/[0.04]"
              >
                <div class="flex items-center gap-3">
                  <span class="flex h-10 w-10 items-center justify-center rounded-xl border border-white/[0.08] bg-white/[0.04] text-cyan-300">
                    <AppIcon :name="it.icon" class="h-5 w-5" />
                  </span>
                  <div class="min-w-0 flex-1">
                    <p class="truncate text-sm font-semibold text-foreground">{{ it.name }}</p>
                    <AdminPill :tone="intStatus[it.status].tone" dot>{{ intStatus[it.status].label }}</AdminPill>
                  </div>
                </div>
                <p class="mt-3 text-xs text-muted-foreground">{{ it.desc }}</p>
                <button class="mt-3 w-full rounded-lg border border-white/[0.08] bg-white/[0.03] py-2 text-xs font-medium text-foreground transition-colors hover:bg-white/[0.07]">
                  Cấu hình
                </button>
              </div>
            </div>
          </AdminPanel>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-switch {
  position: relative;
  height: 1.5rem;
  width: 2.75rem;
  border-radius: 9999px;
  background: oklch(0.7 0.05 280 / 18%);
  border: 1px solid oklch(0.7 0.1 280 / 20%);
  transition: background 0.2s ease, border-color 0.2s ease;
}
.admin-switch > span {
  position: absolute;
  top: 50%;
  left: 3px;
  height: 1.05rem;
  width: 1.05rem;
  transform: translateY(-50%);
  border-radius: 9999px;
  background: oklch(0.9 0.02 240);
  transition: left 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.admin-switch.on {
  background: linear-gradient(135deg, oklch(0.78 0.18 195 / 90%), oklch(0.68 0.22 350 / 90%));
  border-color: oklch(0.78 0.18 195 / 50%);
  box-shadow: 0 0 14px oklch(0.78 0.18 195 / 35%);
}
.admin-switch.on > span {
  left: calc(100% - 1.05rem - 3px);
}
.admin-switch.on--warn {
  background: linear-gradient(135deg, oklch(0.8 0.16 85 / 95%), oklch(0.65 0.25 25 / 90%));
  border-color: oklch(0.8 0.16 85 / 50%);
  box-shadow: 0 0 14px oklch(0.8 0.16 85 / 40%);
}
.admin-switch--lg {
  height: 1.75rem;
  width: 3.25rem;
}
.admin-switch--lg > span {
  height: 1.25rem;
  width: 1.25rem;
}
.banner-enter-active,
.banner-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.banner-enter-from,
.banner-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
