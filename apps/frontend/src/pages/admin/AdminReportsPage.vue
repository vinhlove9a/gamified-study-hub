<script setup lang="ts">
import { ref, computed } from 'vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdminStatCard from '@/components/admin/AdminStatCard.vue';
import AdminPill from '@/components/admin/AdminPill.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type ReportStatus = 'new' | 'progress' | 'closed';
type ReportKind = 'spam' | 'harass' | 'misinfo' | 'copyright' | 'impersonate' | 'other';
type ReportLevel = 'low' | 'medium' | 'high' | 'critical';
type TargetKind = 'comment' | 'document' | 'profile' | 'forum';
type ResolveAction = 'ignore' | 'warn' | 'remove' | 'ban';

type PillTone = 'cyan' | 'green' | 'amber' | 'rose' | 'violet' | 'magenta' | 'neutral';
type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface KindMeta {
  label: string;
  icon: string;
  tone: PillTone;
}
interface LevelMeta {
  label: string;
  tone: PillTone;
}
interface ResolveMeta {
  action: ResolveAction;
  label: string;
  result: string;
  resultTone: PillTone;
  icon: string;
}
interface Report {
  id: number;
  status: ReportStatus;
  kind: ReportKind;
  level: ReportLevel;
  targetKind: TargetKind;
  targetName: string;
  context: string;
  excerpt: string;
  reporter: string;
  reportedUser: string;
  reportedHandle: string;
  priorReports: number;
  priorWarnings: number;
  joined: string;
  time: string;
  resolution?: ResolveAction;
}

const kindMeta: Record<ReportKind, KindMeta> = {
  spam: { label: 'Spam', icon: 'megaphone', tone: 'cyan' },
  harass: { label: 'Quấy rối', icon: 'alert', tone: 'rose' },
  misinfo: { label: 'Nội dung sai', icon: 'info', tone: 'amber' },
  copyright: { label: 'Bản quyền', icon: 'shield', tone: 'violet' },
  impersonate: { label: 'Mạo danh', icon: 'user', tone: 'magenta' },
  other: { label: 'Khác', icon: 'flag', tone: 'neutral' }
};

const levelMeta: Record<ReportLevel, LevelMeta> = {
  critical: { label: 'Nghiêm trọng', tone: 'rose' },
  high: { label: 'Cao', tone: 'amber' },
  medium: { label: 'Trung bình', tone: 'violet' },
  low: { label: 'Thấp', tone: 'neutral' }
};

const targetMeta: Record<TargetKind, { label: string; icon: string }> = {
  comment: { label: 'Bình luận', icon: 'message' },
  document: { label: 'Tài liệu', icon: 'file-text' },
  profile: { label: 'Hồ sơ', icon: 'user' },
  forum: { label: 'Bài viết forum', icon: 'globe' }
};

const resolveMeta: Record<ResolveAction, ResolveMeta> = {
  ignore: { action: 'ignore', label: 'Bỏ qua', result: 'Đã bỏ qua', resultTone: 'neutral', icon: 'check-circle' },
  warn: { action: 'warn', label: 'Cảnh cáo', result: 'Đã cảnh cáo', resultTone: 'amber', icon: 'alert' },
  remove: { action: 'remove', label: 'Gỡ nội dung', result: 'Đã gỡ nội dung', resultTone: 'violet', icon: 'trash' },
  ban: { action: 'ban', label: 'Cấm tài khoản', result: 'Đã cấm tài khoản', resultTone: 'rose', icon: 'lock' }
};

const stats = [
  { label: 'Chờ xử lý', value: 7, icon: 'inbox', accent: 'amber' as const, trend: -2, trendLabel: 'hàng đợi mới' },
  { label: 'Đã xử lý hôm nay', value: 23, icon: 'check-circle', accent: 'green' as const, trend: 14.3, trendLabel: '+5 so với hôm qua' },
  { label: 'Tài khoản bị cấm', value: 37, icon: 'lock', accent: 'magenta' as const, trend: 6.1, trendLabel: 'tổng đang cấm' },
  { label: 'Thời gian xử lý TB', value: 2.4, suffix: ' giờ', decimals: 1, icon: 'clock', accent: 'cyan' as const, trend: -11.2, trendLabel: 'nhanh hơn tuần trước' }
];

const reports = ref<Report[]>([
  {
    id: 1, status: 'new', kind: 'harass', level: 'critical', targetKind: 'comment',
    targetName: 'Bình luận trong nhóm "Ôn thi Giải tích 2"', context: 'Bình luận',
    excerpt: 'Học dốt thế này thì đừng có vào đây hỏi nữa, mất thời gian của mọi người…',
    reporter: 'Nguyễn Minh Anh', reportedUser: 'Trần Văn Cường', reportedHandle: '@cuongtv99',
    priorReports: 4, priorWarnings: 1, joined: '03/2024', time: '8 phút'
  },
  {
    id: 2, status: 'new', kind: 'spam', level: 'medium', targetKind: 'forum',
    targetName: 'Bài viết: "Bán tài liệu IELTS giá rẻ 50k"', context: 'Bài viết forum',
    excerpt: 'Liên hệ Zalo 09xx để nhận trọn bộ tài liệu IELTS 9.0, inbox ngay kẻo lỡ!!! Link: bit.ly/…',
    reporter: 'Lê Thu Hà', reportedUser: 'Phạm Quốc Đạt', reportedHandle: '@datpq_sale',
    priorReports: 9, priorWarnings: 2, joined: '11/2025', time: '21 phút'
  },
  {
    id: 3, status: 'new', kind: 'misinfo', level: 'high', targetKind: 'document',
    targetName: 'Tài liệu: "Tổng hợp công thức Vật lý 12 (bản chuẩn)"', context: 'Tài liệu',
    excerpt: 'Công thức tính công suất ghi sai đơn vị, nhiều bạn tải về dùng ôn thi sẽ bị nhầm…',
    reporter: 'Hoàng Đức Huy', reportedUser: 'Đỗ Thị Lan', reportedHandle: '@lando_phys',
    priorReports: 1, priorWarnings: 0, joined: '09/2023', time: '44 phút'
  },
  {
    id: 4, status: 'new', kind: 'impersonate', level: 'critical', targetKind: 'profile',
    targetName: 'Hồ sơ: "TS. Nguyễn Văn Khoa — Giảng viên FPTU"', context: 'Hồ sơ',
    excerpt: 'Tài khoản dùng ảnh và tên giảng viên thật để dụ sinh viên đóng phí "lớp ôn thi cấp tốc".',
    reporter: 'Vũ Hải Yến', reportedUser: 'Tài khoản giả mạo', reportedHandle: '@ts.khoa.fptu',
    priorReports: 6, priorWarnings: 0, joined: '06/2026', time: '1 giờ'
  },
  {
    id: 5, status: 'new', kind: 'copyright', level: 'high', targetKind: 'document',
    targetName: 'Tài liệu: "Giáo trình Kinh tế vĩ mô (PDF full)"', context: 'Tài liệu',
    excerpt: 'Đây là bản scan nguyên cuốn sách có bản quyền của NXB Giáo dục, đăng tải không xin phép.',
    reporter: 'Ngô Bảo Châu', reportedUser: 'Bùi Tiến Dũng', reportedHandle: '@dungbt_share',
    priorReports: 2, priorWarnings: 1, joined: '02/2025', time: '2 giờ'
  },
  {
    id: 6, status: 'new', kind: 'spam', level: 'low', targetKind: 'comment',
    targetName: 'Bình luận trong tài liệu "500 từ vựng TOEIC"', context: 'Bình luận',
    excerpt: 'up up up cho bạn nào cần nhé, tải về nhanh kẻo bay link 👍👍👍',
    reporter: 'Trịnh Thu Trang', reportedUser: 'Lý Gia Hân', reportedHandle: '@hanlg_toeic',
    priorReports: 0, priorWarnings: 0, joined: '04/2026', time: '3 giờ'
  },
  {
    id: 7, status: 'new', kind: 'other', level: 'medium', targetKind: 'forum',
    targetName: 'Bài viết: "Tranh luận về kỳ thi tốt nghiệp 2026"', context: 'Bài viết forum',
    excerpt: 'Nội dung lệch chủ đề học tập, chuyển sang công kích cá nhân giữa các thành viên trong nhóm.',
    reporter: 'Đặng Khánh Linh', reportedUser: 'Mai Tuấn Kiệt', reportedHandle: '@kietmt_2k6',
    priorReports: 3, priorWarnings: 0, joined: '12/2024', time: '5 giờ'
  },
  {
    id: 8, status: 'progress', kind: 'harass', level: 'high', targetKind: 'profile',
    targetName: 'Hồ sơ: "@toxic_player99"', context: 'Hồ sơ',
    excerpt: 'Liên tục nhắn tin làm phiền và đe doạ các thành viên nữ trong nhóm học nhóm.',
    reporter: 'Phan Mỹ Duyên', reportedUser: 'Hồ Anh Quân', reportedHandle: '@toxic_player99',
    priorReports: 12, priorWarnings: 3, joined: '07/2024', time: '6 giờ'
  },
  {
    id: 9, status: 'progress', kind: 'misinfo', level: 'medium', targetKind: 'forum',
    targetName: 'Bài viết: "Mẹo gian lận thi online không bị phát hiện"', context: 'Bài viết forum',
    excerpt: 'Hướng dẫn cách qua mặt camera giám sát khi thi trực tuyến, vi phạm quy chế học thuật.',
    reporter: 'Tô Hoài Nam', reportedUser: 'Châu Gia Bảo', reportedHandle: '@baocg_hack',
    priorReports: 5, priorWarnings: 1, joined: '01/2026', time: '9 giờ'
  },
  {
    id: 10, status: 'progress', kind: 'copyright', level: 'low', targetKind: 'document',
    targetName: 'Tài liệu: "Slide bài giảng Triết học Mác-Lênin"', context: 'Tài liệu',
    excerpt: 'Sử dụng lại slide của giảng viên khác mà không ghi nguồn, cần xác minh thêm.',
    reporter: 'Lương Thị Hồng', reportedUser: 'Nguyễn Thành Trung', reportedHandle: '@trungnt_phil',
    priorReports: 1, priorWarnings: 0, joined: '10/2023', time: '11 giờ'
  },
  {
    id: 11, status: 'closed', kind: 'spam', level: 'medium', targetKind: 'comment',
    targetName: 'Bình luận hàng loạt trong 14 nhóm học', context: 'Bình luận',
    excerpt: 'Đăng cùng một quảng cáo khoá học vào hàng chục nhóm chỉ trong vài phút.',
    reporter: 'Hệ thống tự động', reportedUser: 'Trương Văn Phúc', reportedHandle: '@phuctv_ads',
    priorReports: 18, priorWarnings: 2, joined: '03/2026', time: '1 ngày', resolution: 'ban'
  },
  {
    id: 12, status: 'closed', kind: 'harass', level: 'critical', targetKind: 'comment',
    targetName: 'Bình luận trong nhóm "Luyện đề Toán THPT"', context: 'Bình luận',
    excerpt: 'Dùng từ ngữ xúc phạm nặng nề nhắm vào một thành viên khác trong nhóm.',
    reporter: 'Kiều Anh Thư', reportedUser: 'Lê Hữu Phước', reportedHandle: '@phuoclh_x',
    priorReports: 7, priorWarnings: 2, joined: '05/2025', time: '1 ngày', resolution: 'ban'
  },
  {
    id: 13, status: 'closed', kind: 'misinfo', level: 'high', targetKind: 'document',
    targetName: 'Tài liệu: "Đáp án đề thi thử Hóa (sai 40%)"', context: 'Tài liệu',
    excerpt: 'Đáp án sai nhiều câu gây hiểu nhầm cho người ôn thi, đã được tác giả gỡ.',
    reporter: 'Đoàn Nhật Minh', reportedUser: 'Tạ Quang Huy', reportedHandle: '@huytq_chem',
    priorReports: 3, priorWarnings: 1, joined: '08/2024', time: '2 ngày', resolution: 'remove'
  },
  {
    id: 14, status: 'closed', kind: 'other', level: 'low', targetKind: 'forum',
    targetName: 'Bài viết: "Khảo sát chọn ngành đại học"', context: 'Bài viết forum',
    excerpt: 'Báo cáo nhầm, nội dung phù hợp quy định cộng đồng sau khi xem xét lại.',
    reporter: 'Cao Thuỳ Dương', reportedUser: 'Nguyễn Hải Đăng', reportedHandle: '@dangnh_uni',
    priorReports: 0, priorWarnings: 0, joined: '02/2024', time: '2 ngày', resolution: 'ignore'
  },
  {
    id: 15, status: 'closed', kind: 'impersonate', level: 'medium', targetKind: 'profile',
    targetName: 'Hồ sơ: "Admin StudyHub Official"', context: 'Hồ sơ',
    excerpt: 'Giả danh quản trị viên để yêu cầu thành viên cung cấp mật khẩu tài khoản.',
    reporter: 'Hà Phương Thảo', reportedUser: 'Tài khoản giả mạo', reportedHandle: '@studyhub.admin1',
    priorReports: 4, priorWarnings: 0, joined: '06/2026', time: '3 ngày', resolution: 'ban'
  }
]);

const tabs: { key: ReportStatus; label: string }[] = [
  { key: 'new', label: 'Mới' },
  { key: 'progress', label: 'Đang xử lý' },
  { key: 'closed', label: 'Đã đóng' }
];
const tab = ref<ReportStatus>('new');

const search = ref('');
const kindFilters: { key: ReportKind; label: string }[] = (Object.keys(kindMeta) as ReportKind[]).map((k) => ({ key: k, label: kindMeta[k].label }));
const activeKinds = ref<Set<ReportKind>>(new Set());
const levelFilters: { key: ReportLevel; label: string }[] = [
  { key: 'low', label: 'Thấp' },
  { key: 'medium', label: 'Trung bình' },
  { key: 'high', label: 'Cao' },
  { key: 'critical', label: 'Nghiêm trọng' }
];
const activeLevel = ref<ReportLevel | null>(null);

function toggleKind(k: ReportKind) {
  const next = new Set(activeKinds.value);
  if (next.has(k)) next.delete(k);
  else next.add(k);
  activeKinds.value = next;
}
function toggleLevel(l: ReportLevel) {
  activeLevel.value = activeLevel.value === l ? null : l;
}

const tabCounts = computed<Record<ReportStatus, number>>(() => {
  const acc: Record<ReportStatus, number> = { new: 0, progress: 0, closed: 0 };
  for (const r of reports.value) acc[r.status]++;
  return acc;
});

const filtered = computed<Report[]>(() => {
  const q = search.value.trim().toLowerCase();
  return reports.value.filter((r) => {
    if (r.status !== tab.value) return false;
    if (activeKinds.value.size && !activeKinds.value.has(r.kind)) return false;
    if (activeLevel.value && r.level !== activeLevel.value) return false;
    if (q) {
      const hay = `${r.targetName} ${r.reportedUser} ${r.reportedHandle} ${r.reporter} ${r.excerpt}`.toLowerCase();
      if (!hay.includes(q)) return false;
    }
    return true;
  });
});

const kindDistribution = computed(() => {
  const total = reports.value.length || 1;
  return (Object.keys(kindMeta) as ReportKind[])
    .map((k) => {
      const count = reports.value.filter((r) => r.kind === k).length;
      return { key: k, label: kindMeta[k].label, tone: kindMeta[k].tone, count, pct: Math.round((count / total) * 100) };
    })
    .sort((a, b) => b.count - a.count);
});

const accentByTone: Record<PillTone, Accent> = {
  cyan: 'cyan', green: 'green', amber: 'amber', rose: 'magenta', violet: 'violet', magenta: 'magenta', neutral: 'cyan'
};

const dailyHandled = [
  { label: 'T2', value: 14, pct: 64 },
  { label: 'T3', value: 19, pct: 86 },
  { label: 'T4', value: 11, pct: 50 },
  { label: 'T5', value: 22, pct: 100 },
  { label: 'T6', value: 23, pct: 100 },
  { label: 'T7', value: 9, pct: 41 },
  { label: 'CN', value: 6, pct: 27 }
];

const selectedId = ref<number | null>(null);
const selected = computed<Report | null>(() => reports.value.find((r) => r.id === selectedId.value) ?? null);

interface ToastState {
  text: string;
  tone: PillTone;
  icon: string;
}
const toast = ref<ToastState | null>(null);
let toastTimer: ReturnType<typeof setTimeout> | undefined;

function selectReport(r: Report) {
  selectedId.value = r.id;
}

function resolve(action: ResolveAction) {
  const r = selected.value;
  if (!r) return;
  const meta = resolveMeta[action];
  r.status = 'closed';
  r.resolution = action;
  toast.value = { text: `${meta.result} cho báo cáo "${r.targetName}".`, tone: meta.resultTone, icon: meta.icon };
  if (toastTimer) clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (toast.value = null), 4000);
  selectedId.value = null;
}

const accentTextClass: Record<PillTone, string> = {
  cyan: 'text-cyan-300',
  green: 'text-emerald-300',
  amber: 'text-amber-300',
  rose: 'text-rose-300',
  violet: 'text-violet-300',
  magenta: 'text-fuchsia-300',
  neutral: 'text-muted-foreground'
};
const iconBgClass: Record<PillTone, string> = {
  cyan: 'bg-cyan-500/10 text-cyan-300',
  green: 'bg-emerald-500/10 text-emerald-300',
  amber: 'bg-amber-500/10 text-amber-300',
  rose: 'bg-rose-500/10 text-rose-300',
  violet: 'bg-violet-500/10 text-violet-300',
  magenta: 'bg-fuchsia-500/10 text-fuchsia-300',
  neutral: 'bg-white/[0.06] text-muted-foreground'
};

const actionBtns: { action: ResolveAction; classes: string }[] = [
  { action: 'ignore', classes: 'border-white/[0.08] bg-white/[0.03] text-muted-foreground hover:bg-white/[0.06]' },
  { action: 'warn', classes: 'border-amber-500/30 bg-amber-500/10 text-amber-300 hover:bg-amber-500/20' },
  { action: 'remove', classes: 'border-violet-500/30 bg-violet-500/10 text-violet-300 hover:bg-violet-500/20' },
  { action: 'ban', classes: 'border-rose-500/30 bg-rose-500/10 text-rose-300 hover:bg-rose-500/20' }
];
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2">
          <span class="h-1.5 w-1.5 rounded-full bg-rose-400 pulse-glow-green" />
          <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">Kiểm duyệt · Hàng đợi vi phạm</p>
        </div>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Báo cáo vi phạm</h2>
      </div>
      <div class="flex items-center gap-2">
        <button class="flex items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.03] px-4 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground">
          <AppIcon name="refresh" class="h-4 w-4" /> Làm mới
        </button>
        <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
          <AppIcon name="download" class="h-4 w-4" /> Xuất nhật ký
        </button>
      </div>
    </div>

    <!-- Toast / inline notification -->
    <div
      v-if="toast"
      class="glass-panel flex items-center gap-3 rounded-2xl border px-4 py-3"
      :class="`border-${''}white/[0.08]`"
    >
      <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg" :class="iconBgClass[toast.tone]">
        <AppIcon :name="toast.icon" class="h-4 w-4" />
      </span>
      <p class="flex-1 text-sm text-foreground">{{ toast.text }}</p>
      <AdminPill :tone="toast.tone" dot>Đã chuyển sang "Đã đóng"</AdminPill>
      <button class="text-muted-foreground transition-colors hover:text-foreground" @click="toast = null">
        <AppIcon name="x" class="h-4 w-4" />
      </button>
    </div>

    <!-- KPI row -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <AdminStatCard
        v-for="s in stats"
        :key="s.label"
        :label="s.label"
        :value="s.value"
        :suffix="s.suffix"
        :decimals="s.decimals"
        :icon="s.icon"
        :accent="s.accent"
        :trend="s.trend"
        :trend-label="s.trendLabel"
      />
    </div>

    <!-- Status tabs -->
    <div class="flex flex-wrap items-center gap-2">
      <div class="flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
        <button
          v-for="t in tabs"
          :key="t.key"
          class="flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors"
          :class="tab === t.key ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
          @click="tab = t.key"
        >
          {{ t.label }}
          <span
            class="rounded-full px-1.5 py-0.5 font-mono text-[10px]"
            :class="tab === t.key ? 'bg-cyan-500/20 text-cyan-300' : 'bg-white/[0.06] text-muted-foreground'"
          >{{ tabCounts[t.key] }}</span>
        </button>
      </div>
    </div>

    <!-- Toolbar: search + filters -->
    <AdminPanel icon="filter" accent="violet" flush>
      <div class="flex flex-col gap-4 p-4">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
          <div class="relative lg:w-80">
            <AppIcon name="search" class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              v-model="search"
              type="text"
              placeholder="Tìm theo nội dung, người dùng, người báo cáo…"
              class="cosmic-input w-full rounded-xl py-2 pl-9 pr-3 text-sm"
            />
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <span class="font-mono text-[11px] uppercase tracking-wider text-muted-foreground">Mức độ</span>
            <button
              v-for="l in levelFilters"
              :key="l.key"
              class="rounded-lg border px-2.5 py-1 text-xs font-medium transition-colors"
              :class="activeLevel === l.key
                ? `border-transparent ${iconBgClass[levelMeta[l.key].tone]}`
                : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:text-foreground'"
              @click="toggleLevel(l.key)"
            >
              {{ l.label }}
            </button>
          </div>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <span class="font-mono text-[11px] uppercase tracking-wider text-muted-foreground">Loại</span>
          <button
            v-for="k in kindFilters"
            :key="k.key"
            class="flex items-center gap-1.5 rounded-lg border px-2.5 py-1 text-xs font-medium transition-colors"
            :class="activeKinds.has(k.key)
              ? `border-transparent ${iconBgClass[kindMeta[k.key].tone]}`
              : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:text-foreground'"
            @click="toggleKind(k.key)"
          >
            <AppIcon :name="kindMeta[k.key].icon" class="h-3.5 w-3.5" />
            {{ k.label }}
          </button>
        </div>
      </div>
    </AdminPanel>

    <!-- 2-column layout -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-5">
      <!-- Left: report list -->
      <div class="lg:col-span-3">
        <AdminPanel
          title="Danh sách báo cáo"
          :subtitle="`${filtered.length} mục · tab ${tabs.find((t) => t.key === tab)?.label}`"
          icon="flag"
          accent="amber"
        >
          <ul v-if="filtered.length" class="space-y-2.5">
            <li
              v-for="r in filtered"
              :key="r.id"
              class="cursor-pointer rounded-xl border px-3.5 py-3 transition-all hover:bg-white/[0.04]"
              :class="selectedId === r.id ? 'border-cyan-500/40 bg-cyan-500/[0.06]' : 'border-white/[0.05] bg-white/[0.02]'"
              @click="selectReport(r)"
            >
              <div class="flex items-start gap-3">
                <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg" :class="iconBgClass[kindMeta[r.kind].tone]">
                  <AppIcon :name="kindMeta[r.kind].icon" class="h-4 w-4" />
                </span>
                <div class="min-w-0 flex-1">
                  <div class="flex items-start justify-between gap-2">
                    <p class="min-w-0 flex-1 truncate text-sm font-medium text-foreground">{{ r.targetName }}</p>
                    <span class="shrink-0 font-mono text-[10px] text-muted-foreground">{{ r.time }} trước</span>
                  </div>
                  <div class="mt-1 flex flex-wrap items-center gap-1.5">
                    <AdminPill :tone="kindMeta[r.kind].tone">{{ kindMeta[r.kind].label }}</AdminPill>
                    <AdminPill :tone="levelMeta[r.level].tone" dot>{{ levelMeta[r.level].label }}</AdminPill>
                    <span class="flex items-center gap-1 rounded-md bg-white/[0.04] px-1.5 py-0.5 text-[10px] text-muted-foreground">
                      <AppIcon :name="targetMeta[r.targetKind].icon" class="h-3 w-3" /> {{ targetMeta[r.targetKind].label }}
                    </span>
                  </div>
                  <p class="mt-2 line-clamp-2 rounded-lg border-l-2 border-white/[0.08] bg-white/[0.02] px-2.5 py-1.5 text-xs italic text-muted-foreground">
                    "{{ r.excerpt }}"
                  </p>
                  <div class="mt-2 flex items-center justify-between text-[11px] text-muted-foreground">
                    <span class="flex items-center gap-1">
                      <AppIcon name="user" class="h-3 w-3" /> Bị báo cáo: <span class="text-foreground/80">{{ r.reportedUser }}</span>
                    </span>
                    <span class="flex items-center gap-1">
                      <AppIcon name="flag" class="h-3 w-3" /> {{ r.reporter }}
                    </span>
                  </div>
                  <div v-if="r.resolution" class="mt-2">
                    <AdminPill :tone="resolveMeta[r.resolution].resultTone" dot>{{ resolveMeta[r.resolution].result }}</AdminPill>
                  </div>
                </div>
              </div>
            </li>
          </ul>
          <div v-else class="flex flex-col items-center justify-center gap-2 py-12 text-center">
            <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/[0.04] text-muted-foreground">
              <AppIcon name="inbox" class="h-6 w-6" />
            </span>
            <p class="text-sm font-medium text-foreground">Không có báo cáo nào</p>
            <p class="text-xs text-muted-foreground">Thử đổi tab trạng thái hoặc xoá bộ lọc.</p>
          </div>
        </AdminPanel>
      </div>

      <!-- Right: detail + distribution -->
      <div class="space-y-4 lg:col-span-2">
        <!-- Detail panel -->
        <AdminPanel title="Chi tiết báo cáo" icon="eye" accent="cyan" glow="cyan">
          <div v-if="selected" class="space-y-4">
            <div class="flex items-start gap-3">
              <span class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl" :class="iconBgClass[kindMeta[selected.kind].tone]">
                <AppIcon :name="kindMeta[selected.kind].icon" class="h-5 w-5" />
              </span>
              <div class="min-w-0">
                <p class="text-sm font-semibold text-foreground">{{ selected.targetName }}</p>
                <div class="mt-1 flex flex-wrap items-center gap-1.5">
                  <AdminPill :tone="kindMeta[selected.kind].tone">{{ kindMeta[selected.kind].label }}</AdminPill>
                  <AdminPill :tone="levelMeta[selected.level].tone" dot>{{ levelMeta[selected.level].label }}</AdminPill>
                  <AdminPill tone="neutral">{{ targetMeta[selected.targetKind].label }}</AdminPill>
                </div>
              </div>
            </div>

            <!-- Reported content quote -->
            <div>
              <p class="mb-1.5 font-mono text-[11px] uppercase tracking-wider text-muted-foreground">Nội dung bị báo cáo</p>
              <blockquote class="rounded-xl border-l-2 border-rose-500/40 bg-rose-500/[0.05] px-3.5 py-3 text-sm italic text-foreground/90">
                "{{ selected.excerpt }}"
              </blockquote>
            </div>

            <!-- Reported user -->
            <div class="rounded-xl border border-white/[0.06] bg-white/[0.02] p-3.5">
              <div class="flex items-center justify-between">
                <p class="font-mono text-[11px] uppercase tracking-wider text-muted-foreground">Người bị báo cáo</p>
                <span class="font-mono text-[10px] text-muted-foreground">Tham gia {{ selected.joined }}</span>
              </div>
              <div class="mt-2 flex items-center gap-3">
                <span class="flex h-9 w-9 items-center justify-center rounded-full bg-fuchsia-500/15 text-fuchsia-300">
                  <AppIcon name="user" class="h-4 w-4" />
                </span>
                <div>
                  <p class="text-sm font-medium text-foreground">{{ selected.reportedUser }}</p>
                  <p class="font-mono text-xs text-muted-foreground">{{ selected.reportedHandle }}</p>
                </div>
              </div>
              <div class="mt-3 grid grid-cols-2 gap-2">
                <div class="rounded-lg bg-white/[0.03] px-3 py-2">
                  <p class="font-mono text-lg font-bold text-rose-300">{{ selected.priorReports }}</p>
                  <p class="text-[11px] text-muted-foreground">Lần bị báo cáo</p>
                </div>
                <div class="rounded-lg bg-white/[0.03] px-3 py-2">
                  <p class="font-mono text-lg font-bold text-amber-300">{{ selected.priorWarnings }}</p>
                  <p class="text-[11px] text-muted-foreground">Lần bị cảnh cáo</p>
                </div>
              </div>
            </div>

            <!-- Reporter -->
            <div class="flex items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.02] px-3.5 py-2.5 text-sm">
              <AppIcon name="flag" class="h-4 w-4 text-cyan-300" />
              <span class="text-muted-foreground">Người báo cáo:</span>
              <span class="font-medium text-foreground">{{ selected.reporter }}</span>
              <span class="ml-auto font-mono text-[10px] text-muted-foreground">{{ selected.time }} trước</span>
            </div>

            <!-- Already resolved -->
            <div v-if="selected.resolution" class="flex items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.02] px-3.5 py-2.5">
              <AppIcon :name="resolveMeta[selected.resolution].icon" class="h-4 w-4" :class="accentTextClass[resolveMeta[selected.resolution].resultTone]" />
              <span class="text-sm text-foreground">Kết quả xử lý:</span>
              <AdminPill :tone="resolveMeta[selected.resolution].resultTone" dot>{{ resolveMeta[selected.resolution].result }}</AdminPill>
            </div>

            <!-- Action row -->
            <div v-else>
              <p class="mb-2 font-mono text-[11px] uppercase tracking-wider text-muted-foreground">Hành động xử lý</p>
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="b in actionBtns"
                  :key="b.action"
                  class="flex items-center justify-center gap-1.5 rounded-xl border px-3 py-2.5 text-sm font-medium transition-colors"
                  :class="b.classes"
                  @click="resolve(b.action)"
                >
                  <AppIcon :name="resolveMeta[b.action].icon" class="h-4 w-4" />
                  {{ resolveMeta[b.action].label }}
                </button>
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div v-else class="flex flex-col items-center justify-center gap-2 py-12 text-center">
            <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/[0.04] text-cyan-300">
              <AppIcon name="eye" class="h-6 w-6" />
            </span>
            <p class="text-sm font-medium text-foreground">Chưa chọn báo cáo</p>
            <p class="text-xs text-muted-foreground">Chọn một báo cáo bên trái để xem chi tiết và xử lý.</p>
          </div>
        </AdminPanel>

        <!-- Distribution by kind -->
        <AdminPanel title="Phân loại báo cáo" subtitle="Theo loại vi phạm" icon="chart" accent="magenta">
          <ul class="space-y-3">
            <li v-for="d in kindDistribution" :key="d.key">
              <div class="mb-1 flex items-center gap-2 text-xs">
                <AppIcon :name="kindMeta[d.key].icon" class="h-3.5 w-3.5" :class="accentTextClass[d.tone]" />
                <span class="min-w-0 flex-1 truncate text-foreground">{{ d.label }}</span>
                <span class="font-mono font-semibold text-foreground">{{ d.count }}</span>
              </div>
              <ProgressBar :value="d.pct" :accent="accentByTone[d.tone]" />
            </li>
          </ul>
        </AdminPanel>

        <!-- Daily handled -->
        <AdminPanel title="Xử lý theo ngày" subtitle="Số báo cáo đóng / tuần này" icon="clock" accent="green">
          <div class="flex items-end justify-between gap-2" style="height: 120px">
            <div v-for="d in dailyHandled" :key="d.label" class="flex flex-1 flex-col items-center gap-1.5">
              <span class="font-mono text-[10px] text-foreground">{{ d.value }}</span>
              <div class="flex w-full flex-1 items-end">
                <div
                  class="w-full rounded-t-md bg-gradient-to-t from-emerald-500/40 to-emerald-400/80"
                  :style="{ height: `${d.pct}%` }"
                />
              </div>
              <span class="text-[10px] text-muted-foreground">{{ d.label }}</span>
            </div>
          </div>
        </AdminPanel>
      </div>
    </div>
  </div>
</template>
