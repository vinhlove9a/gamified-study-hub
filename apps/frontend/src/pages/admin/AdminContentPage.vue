<script setup lang="ts">
import { ref, computed } from 'vue';
import AdminPanel from '@/components/admin/AdminPanel.vue';
import AdminStatCard from '@/components/admin/AdminStatCard.vue';
import AdminPill from '@/components/admin/AdminPill.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import ProgressBar from '@/components/app/ProgressBar.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
type Tone = 'cyan' | 'green' | 'amber' | 'rose' | 'violet' | 'magenta' | 'neutral';
type DocStatus = 'pending' | 'approved' | 'rejected' | 'flagged';
type DocKind = 'doc' | 'image' | 'video' | 'book';
type CategoryKey = 'Toán' | 'CNTT' | 'Tiếng Anh' | 'Vật lý' | 'Hoá' | 'Kinh tế' | 'Khác';

interface ContentDoc {
  id: number;
  title: string;
  author: string;
  authorRole: string;
  category: CategoryKey;
  kind: DocKind;
  size: string;
  pages: number;
  uploaded: string;
  status: DocStatus;
  excerpt: string;
  flagReason?: string;
}

interface TabDef {
  key: DocStatus;
  label: string;
  icon: string;
  accent: Accent;
  tone: Tone;
}

interface RejectReason {
  key: string;
  label: string;
  icon: string;
}

// ── KPI ────────────────────────────────────────────────────────────────────
interface Kpi {
  label: string;
  value: number;
  icon: string;
  accent: Accent;
  decimals?: number;
  trend?: number;
  trendLabel?: string;
  spark?: number[];
}

// ── Mock data ────────────────────────────────────────────────────────────────
const docs = ref<ContentDoc[]>([
  { id: 1, title: 'Đề cương Giải tích 2 — FPTU kỳ Spring', author: 'Nguyễn Minh Anh', authorRole: 'Sinh viên · K18', category: 'Toán', kind: 'doc', size: '2.4 MB', pages: 42, uploaded: '5 phút', status: 'pending', excerpt: 'Tổng hợp lý thuyết tích phân bội, chuỗi số và phương trình vi phân kèm 60 bài tập có lời giải chi tiết.' },
  { id: 2, title: 'Slide Kiến trúc máy tính — Chương 3: Pipeline', author: 'Trần Quốc Bảo', authorRole: 'Sinh viên · K17', category: 'CNTT', kind: 'doc', size: '8.1 MB', pages: 58, uploaded: '18 phút', status: 'pending', excerpt: 'Bài giảng minh hoạ kỹ thuật đường ống lệnh, hazard và forwarding với sơ đồ trực quan.' },
  { id: 3, title: '500 từ vựng TOEIC theo chủ đề công sở', author: 'Lê Thu Hà', authorRole: 'Gia sư', category: 'Tiếng Anh', kind: 'doc', size: '1.2 MB', pages: 24, uploaded: '32 phút', status: 'pending', excerpt: 'Bộ từ vựng phân nhóm theo tình huống văn phòng, kèm phiên âm và ví dụ câu thực tế.' },
  { id: 4, title: 'Video bài giảng Vật lý 12 — Dao động điều hoà', author: 'Phạm Đức Huy', authorRole: 'Giảng viên', category: 'Vật lý', kind: 'video', size: '124 MB', pages: 0, uploaded: '1 giờ', status: 'pending', excerpt: 'Video 28 phút giảng kỹ con lắc lò xo, con lắc đơn và phương pháp giải nhanh trắc nghiệm.' },
  { id: 5, title: 'Sơ đồ tư duy Hoá hữu cơ 11', author: 'Vũ Khánh Linh', authorRole: 'Sinh viên · K19', category: 'Hoá', kind: 'image', size: '4.7 MB', pages: 0, uploaded: '2 giờ', status: 'pending', excerpt: 'Infographic tổng hợp các loại phản ứng của hidrocacbon và dẫn xuất, mã màu theo nhóm chức.' },
  { id: 6, title: 'Giáo trình Kinh tế vi mô (bản tóm tắt)', author: 'Đỗ Hoàng Nam', authorRole: 'Sinh viên · K18', category: 'Kinh tế', kind: 'book', size: '6.3 MB', pages: 96, uploaded: '3 giờ', status: 'pending', excerpt: 'Tóm lược cung cầu, độ co giãn, hành vi người tiêu dùng và cấu trúc thị trường cạnh tranh.' },
  { id: 7, title: 'Bộ đề thi thử THPT QG môn Toán 2026', author: 'Hoàng Thị Mai', authorRole: 'Giảng viên', category: 'Toán', kind: 'doc', size: '3.9 MB', pages: 64, uploaded: '5 giờ', status: 'pending', excerpt: '10 đề chuẩn cấu trúc Bộ GD, có ma trận đề và đáp án thang điểm chi tiết.' },
  { id: 8, title: 'Tài liệu ôn tập Lập trình Python cơ bản', author: 'Bùi Tiến Dũng', authorRole: 'Mentor CLB', category: 'CNTT', kind: 'doc', size: '2.1 MB', pages: 38, uploaded: 'Hôm qua', status: 'approved', excerpt: 'Hướng dẫn cú pháp, cấu trúc dữ liệu và 40 bài thực hành từ cơ bản tới nâng cao.' },
  { id: 9, title: 'Flashcard 3000 từ IELTS Academic', author: 'Lê Thu Hà', authorRole: 'Gia sư', category: 'Tiếng Anh', kind: 'doc', size: '5.8 MB', pages: 0, uploaded: 'Hôm qua', status: 'approved', excerpt: 'Bộ thẻ ghi nhớ phân theo band điểm, kèm collocation và đồng nghĩa thường gặp.' },
  { id: 10, title: 'Sổ tay công thức Vật lý đại cương', author: 'Phạm Đức Huy', authorRole: 'Giảng viên', category: 'Vật lý', kind: 'book', size: '1.9 MB', pages: 52, uploaded: '2 ngày', status: 'approved', excerpt: 'Tổng hợp công thức cơ – nhiệt – điện – quang theo từng chương, dạng tra cứu nhanh.' },
  { id: 11, title: 'Bài tập Hoá phân tích định lượng', author: 'Vũ Khánh Linh', authorRole: 'Sinh viên · K19', category: 'Hoá', kind: 'doc', size: '2.6 MB', pages: 30, uploaded: '2 ngày', status: 'approved', excerpt: 'Tuyển tập bài tập chuẩn độ axit – bazơ và tạo phức kèm hướng dẫn tính sai số.' },
  { id: 12, title: 'Tài liệu lậu — bản scan sách bản quyền', author: 'Ẩn danh', authorRole: 'Tài khoản mới', category: 'Khác', kind: 'image', size: '32 MB', pages: 0, uploaded: '3 ngày', status: 'rejected', excerpt: 'Bản chụp toàn bộ sách giáo khoa có dấu hiệu vi phạm bản quyền, không có nguồn hợp lệ.', flagReason: 'Vi phạm bản quyền' },
  { id: 13, title: 'Đề cương Kinh tế vĩ mô (file lỗi)', author: 'Đỗ Hoàng Nam', authorRole: 'Sinh viên · K18', category: 'Kinh tế', kind: 'doc', size: '0.4 MB', pages: 4, uploaded: '3 ngày', status: 'rejected', excerpt: 'Nội dung sơ sài, thiếu cấu trúc, nhiều trang trống — không đạt chuẩn xuất bản.', flagReason: 'Chất lượng kém' },
  { id: 14, title: 'Slide Mạng máy tính (trùng lặp)', author: 'Trần Quốc Bảo', authorRole: 'Sinh viên · K17', category: 'CNTT', kind: 'doc', size: '7.2 MB', pages: 44, uploaded: '4 ngày', status: 'rejected', excerpt: 'Tài liệu trùng với bản đã có trên hệ thống, không bổ sung nội dung mới.', flagReason: 'Trùng lặp' },
  { id: 15, title: 'Bài viết diễn đàn chứa quảng cáo', author: 'Ẩn danh', authorRole: 'Tài khoản mới', category: 'Khác', kind: 'doc', size: '0.2 MB', pages: 2, uploaded: '6 giờ', status: 'flagged', excerpt: 'Bị người dùng báo cáo: chèn liên kết quảng cáo và nội dung không liên quan học tập.', flagReason: 'Báo cáo từ cộng đồng' },
  { id: 16, title: 'Ảnh giải đề thi mờ, khó đọc', author: 'Nguyễn Văn Tú', authorRole: 'Sinh viên · K20', category: 'Toán', kind: 'image', size: '9.4 MB', pages: 0, uploaded: '1 ngày', status: 'flagged', excerpt: 'Bị gắn cờ do chất lượng ảnh thấp, nhiều người dùng phản hồi không xem được lời giải.', flagReason: 'Báo cáo từ cộng đồng' },
  { id: 17, title: 'Tổng hợp ngữ pháp Tiếng Anh THPT', author: 'Lê Thu Hà', authorRole: 'Gia sư', category: 'Tiếng Anh', kind: 'doc', size: '3.3 MB', pages: 48, uploaded: '40 phút', status: 'pending', excerpt: 'Hệ thống 12 thì, câu điều kiện, mệnh đề quan hệ kèm bảng tra và bài luyện tập.' }
]);

// ── Tabs ───────────────────────────────────────────────────────────────────
const tabs: TabDef[] = [
  { key: 'pending', label: 'Chờ duyệt', icon: 'clock', accent: 'amber', tone: 'amber' },
  { key: 'approved', label: 'Đã duyệt', icon: 'check-circle', accent: 'green', tone: 'green' },
  { key: 'rejected', label: 'Bị từ chối', icon: 'x', accent: 'magenta', tone: 'rose' },
  { key: 'flagged', label: 'Bị gắn cờ', icon: 'flag', accent: 'violet', tone: 'violet' }
];
const activeTab = ref<DocStatus>('pending');

const tabCount = (key: DocStatus) => docs.value.filter((d) => d.status === key).length;
const activeTabDef = computed(() => tabs.find((t) => t.key === activeTab.value)!);

// ── Filters ──────────────────────────────────────────────────────────────────
const categories: ('Tất cả' | CategoryKey)[] = ['Tất cả', 'Toán', 'CNTT', 'Tiếng Anh', 'Vật lý', 'Hoá', 'Kinh tế', 'Khác'];
const activeCategory = ref<'Tất cả' | CategoryKey>('Tất cả');
const search = ref('');
const view = ref<'list' | 'grid'>('list');

const filteredDocs = computed(() =>
  docs.value.filter((d) => {
    if (d.status !== activeTab.value) return false;
    if (activeCategory.value !== 'Tất cả' && d.category !== activeCategory.value) return false;
    if (search.value.trim() && !d.title.toLowerCase().includes(search.value.trim().toLowerCase())) return false;
    return true;
  })
);

// ── Category breakdown ────────────────────────────────────────────────────────
const catAccent: Record<CategoryKey, Accent> = {
  Toán: 'cyan',
  CNTT: 'violet',
  'Tiếng Anh': 'magenta',
  'Vật lý': 'amber',
  Hoá: 'green',
  'Kinh tế': 'cyan',
  Khác: 'violet'
};
const categoryBreakdown = computed(() => {
  const keys: CategoryKey[] = ['Toán', 'CNTT', 'Tiếng Anh', 'Vật lý', 'Hoá', 'Kinh tế', 'Khác'];
  const counts = keys.map((k) => ({ key: k, count: docs.value.filter((d) => d.category === k).length }));
  const max = Math.max(1, ...counts.map((c) => c.count));
  return counts.map((c) => ({ ...c, accent: catAccent[c.key], pct: Math.round((c.count / max) * 100) }));
});

// ── Selection / preview ──────────────────────────────────────────────────────
const selectedId = ref<number | null>(null);
const selectedDoc = computed(() => docs.value.find((d) => d.id === selectedId.value) ?? null);
function selectDoc(id: number) {
  selectedId.value = id;
}

// ── Kind metadata ────────────────────────────────────────────────────────────
const kindMeta: Record<DocKind, { icon: string; label: string; tone: Tone }> = {
  doc: { icon: 'file-text', label: 'Tài liệu', tone: 'cyan' },
  image: { icon: 'image', label: 'Hình ảnh', tone: 'violet' },
  video: { icon: 'video', label: 'Video', tone: 'magenta' },
  book: { icon: 'book-open', label: 'Giáo trình', tone: 'green' }
};
const statusMeta: Record<DocStatus, { label: string; tone: Tone }> = {
  pending: { label: 'Chờ duyệt', tone: 'amber' },
  approved: { label: 'Đã duyệt', tone: 'green' },
  rejected: { label: 'Bị từ chối', tone: 'rose' },
  flagged: { label: 'Bị gắn cờ', tone: 'violet' }
};

// ── Actions: approve / reject (real state transitions) ───────────────────────
function approve(id: number) {
  const d = docs.value.find((x) => x.id === id);
  if (!d) return;
  d.status = 'approved';
  d.flagReason = undefined;
}

// Reject modal
const rejectReasons: RejectReason[] = [
  { key: 'wrong', label: 'Sai nội dung', icon: 'alert' },
  { key: 'copyright', label: 'Vi phạm bản quyền', icon: 'shield' },
  { key: 'quality', label: 'Chất lượng kém', icon: 'thumbs-up' },
  { key: 'duplicate', label: 'Trùng lặp', icon: 'copy' }
];
const rejectTargetId = ref<number | null>(null);
const rejectReasonKey = ref<string>('quality');
const rejectModalOpen = computed(() => rejectTargetId.value !== null);

function openReject(id: number) {
  rejectTargetId.value = id;
  rejectReasonKey.value = 'quality';
}
function cancelReject() {
  rejectTargetId.value = null;
}
function confirmReject() {
  const d = docs.value.find((x) => x.id === rejectTargetId.value);
  if (d) {
    d.status = 'rejected';
    d.flagReason = rejectReasons.find((r) => r.key === rejectReasonKey.value)?.label ?? 'Khác';
  }
  rejectTargetId.value = null;
}

const kpis: Kpi[] = [
  { label: 'Chờ duyệt', value: 38, icon: 'clock', accent: 'amber', trendLabel: 'Hàng đợi kiểm duyệt' },
  { label: 'Đã duyệt hôm nay', value: 124, icon: 'check-circle', accent: 'green', trend: 9.4, trendLabel: '+18 so với hôm qua', spark: [88, 94, 101, 96, 108, 115, 119, 124] },
  { label: 'Bị từ chối', value: 9, icon: 'x', accent: 'magenta', trend: -3.1, trendLabel: 'Trong 24 giờ' },
  { label: 'Tổng tài liệu', value: 48211, icon: 'file-text', accent: 'cyan', trend: 4.7, trendLabel: '+612 tuần này', spark: [44200, 45100, 45800, 46400, 47000, 47500, 47900, 48211] }
];
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div class="flex items-center gap-2">
          <span class="h-1.5 w-1.5 rounded-full bg-amber-400 pulse-glow-green" />
          <p class="font-mono text-[11px] uppercase tracking-widest text-muted-foreground">Kiểm duyệt nội dung · Thư viện tài liệu</p>
        </div>
        <h2 class="mt-1 text-2xl font-bold tracking-tight text-foreground">Quản lý &amp; duyệt tài liệu</h2>
      </div>
      <div class="flex items-center gap-2">
        <button class="flex items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.03] px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground">
          <AppIcon name="filter" class="h-4 w-4" /> Bộ lọc nâng cao
        </button>
        <button class="flex items-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.03] px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground">
          <AppIcon name="settings" class="h-4 w-4" /> Cài đặt duyệt
        </button>
        <button class="cosmic-btn flex items-center gap-2 rounded-xl px-4 py-2 text-sm">
          <AppIcon name="upload" class="h-4 w-4" /> Tải lên thay người dùng
        </button>
      </div>
    </div>

    <!-- KPI row -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <AdminStatCard
        v-for="k in kpis"
        :key="k.label"
        :label="k.label"
        :value="k.value"
        :decimals="k.decimals"
        :icon="k.icon"
        :accent="k.accent"
        :trend="k.trend"
        :trend-label="k.trendLabel"
        :sparkline="k.spark"
      />
    </div>

    <!-- Status tabs + tools -->
    <AdminPanel flush accent="cyan">
      <template #header>
        <div class="flex flex-wrap gap-1.5">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="flex items-center gap-2 rounded-xl border px-3.5 py-2 text-sm font-medium transition-all"
            :class="
              activeTab === t.key
                ? 'border-white/[0.12] bg-white/[0.07] text-foreground'
                : 'border-transparent text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'
            "
            @click="activeTab = t.key"
          >
            <AppIcon :name="t.icon" class="h-4 w-4" :class="activeTab === t.key ? 'text-' + (t.accent === 'magenta' ? 'fuchsia' : t.accent === 'green' ? 'emerald' : t.accent) + '-300' : ''" />
            {{ t.label }}
            <span
              class="rounded-full px-1.5 py-0.5 font-mono text-[10px]"
              :class="activeTab === t.key ? 'bg-white/[0.1] text-foreground' : 'bg-white/[0.05] text-muted-foreground'"
            >{{ tabCount(t.key) }}</span>
          </button>
        </div>
      </template>

      <div class="border-t border-white/[0.06] px-5 py-4">
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <!-- Search -->
          <div class="relative w-full lg:max-w-xs">
            <AppIcon name="search" class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              v-model="search"
              type="text"
              placeholder="Tìm theo tiêu đề tài liệu…"
              class="cosmic-input w-full rounded-xl py-2 pl-9 pr-3 text-sm"
            />
          </div>

          <!-- Category chips + view toggle -->
          <div class="flex flex-wrap items-center gap-2">
            <div class="flex flex-wrap gap-1.5">
              <button
                v-for="c in categories"
                :key="c"
                class="rounded-lg border px-2.5 py-1 text-xs font-medium transition-colors"
                :class="
                  activeCategory === c
                    ? 'border-cyan-500/30 bg-cyan-500/10 text-cyan-300'
                    : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:text-foreground'
                "
                @click="activeCategory = c"
              >
                {{ c }}
              </button>
            </div>
            <div class="ml-1 flex rounded-xl border border-white/[0.06] bg-white/[0.03] p-0.5">
              <button
                class="flex h-7 w-7 items-center justify-center rounded-lg transition-colors"
                :class="view === 'list' ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
                title="Danh sách"
                @click="view = 'list'"
              >
                <AppIcon name="list" class="h-4 w-4" />
              </button>
              <button
                class="flex h-7 w-7 items-center justify-center rounded-lg transition-colors"
                :class="view === 'grid' ? 'bg-white/[0.08] text-foreground' : 'text-muted-foreground hover:text-foreground'"
                title="Lưới"
                @click="view = 'grid'"
              >
                <AppIcon name="grid" class="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </AdminPanel>

    <!-- Two-column layout -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <!-- Document list -->
      <AdminPanel
        class="lg:col-span-2"
        :title="activeTabDef.label"
        :subtitle="`${filteredDocs.length} tài liệu` + (activeCategory !== 'Tất cả' ? ` · ${activeCategory}` : '')"
        :icon="activeTabDef.icon"
        :accent="activeTabDef.accent"
        :glow="activeTab === 'pending' ? 'cyan' : 'none'"
      >
        <template #actions>
          <AdminPill :tone="activeTabDef.tone" dot>{{ filteredDocs.length }} mục</AdminPill>
        </template>

        <!-- Empty -->
        <div
          v-if="filteredDocs.length === 0"
          class="flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-white/[0.08] bg-white/[0.02] py-14 text-center"
        >
          <span class="flex h-12 w-12 items-center justify-center rounded-xl bg-white/[0.04] text-muted-foreground">
            <AppIcon name="inbox" class="h-6 w-6" />
          </span>
          <p class="text-sm font-medium text-foreground">Không có tài liệu phù hợp</p>
          <p class="text-xs text-muted-foreground">Thử đổi tab, danh mục hoặc xoá từ khoá tìm kiếm.</p>
        </div>

        <!-- List view -->
        <ul v-else-if="view === 'list'" class="space-y-2.5">
          <li
            v-for="d in filteredDocs"
            :key="d.id"
            class="group cursor-pointer rounded-xl border bg-white/[0.02] px-3.5 py-3 transition-colors hover:bg-white/[0.04]"
            :class="selectedId === d.id ? 'border-cyan-500/30 bg-cyan-500/[0.04]' : 'border-white/[0.05]'"
            @click="selectDoc(d.id)"
          >
            <div class="flex items-start gap-3">
              <span
                class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border"
                :class="kindMeta[d.kind].tone === 'cyan' ? 'border-cyan-500/20 bg-cyan-500/10 text-cyan-300'
                  : kindMeta[d.kind].tone === 'violet' ? 'border-violet-500/20 bg-violet-500/10 text-violet-300'
                  : kindMeta[d.kind].tone === 'magenta' ? 'border-fuchsia-500/20 bg-fuchsia-500/10 text-fuchsia-300'
                  : 'border-emerald-500/20 bg-emerald-500/10 text-emerald-300'"
              >
                <AppIcon :name="kindMeta[d.kind].icon" class="h-5 w-5" />
              </span>
              <div class="min-w-0 flex-1">
                <div class="flex items-start justify-between gap-2">
                  <p class="truncate text-sm font-semibold text-foreground">{{ d.title }}</p>
                  <AdminPill :tone="catAccent[d.category] === 'magenta' ? 'magenta' : catAccent[d.category] === 'green' ? 'green' : catAccent[d.category] === 'violet' ? 'violet' : catAccent[d.category] === 'amber' ? 'amber' : 'cyan'">{{ d.category }}</AdminPill>
                </div>
                <p class="mt-0.5 truncate text-xs text-muted-foreground">
                  {{ d.author }} · {{ d.size }}<span v-if="d.pages"> · {{ d.pages }} trang</span> · {{ d.uploaded }} trước
                </p>
                <p class="mt-1.5 line-clamp-2 text-xs leading-relaxed text-muted-foreground/80">{{ d.excerpt }}</p>

                <div class="mt-2.5 flex items-center justify-between gap-2">
                  <AdminPill v-if="activeTab !== 'pending'" :tone="statusMeta[d.status].tone" dot>
                    {{ statusMeta[d.status].label }}
                    <span v-if="d.flagReason"> · {{ d.flagReason }}</span>
                  </AdminPill>
                  <span v-else />

                  <div class="flex shrink-0 items-center gap-1.5" @click.stop>
                    <button
                      v-if="activeTab === 'pending' || activeTab === 'flagged'"
                      class="flex h-8 items-center gap-1 rounded-lg bg-emerald-500/10 px-2.5 text-xs font-medium text-emerald-300 transition-colors hover:bg-emerald-500/20"
                      title="Duyệt"
                      @click="approve(d.id)"
                    >
                      <AppIcon name="check" class="h-4 w-4" /> Duyệt
                    </button>
                    <button
                      v-if="activeTab === 'pending' || activeTab === 'flagged'"
                      class="flex h-8 items-center gap-1 rounded-lg bg-rose-500/10 px-2.5 text-xs font-medium text-rose-300 transition-colors hover:bg-rose-500/20"
                      title="Từ chối"
                      @click="openReject(d.id)"
                    >
                      <AppIcon name="x" class="h-4 w-4" /> Từ chối
                    </button>
                    <button
                      class="flex h-8 w-8 items-center justify-center rounded-lg bg-white/[0.04] text-muted-foreground transition-colors hover:bg-white/[0.08] hover:text-foreground"
                      title="Xem trước"
                      @click="selectDoc(d.id)"
                    >
                      <AppIcon name="eye" class="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </li>
        </ul>

        <!-- Grid view -->
        <div v-else class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <button
            v-for="d in filteredDocs"
            :key="d.id"
            class="flex flex-col gap-2 rounded-xl border bg-white/[0.02] p-3.5 text-left transition-colors hover:bg-white/[0.04]"
            :class="selectedId === d.id ? 'border-cyan-500/30 bg-cyan-500/[0.04]' : 'border-white/[0.05]'"
            @click="selectDoc(d.id)"
          >
            <div class="flex items-center gap-2.5">
              <span
                class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border"
                :class="kindMeta[d.kind].tone === 'cyan' ? 'border-cyan-500/20 bg-cyan-500/10 text-cyan-300'
                  : kindMeta[d.kind].tone === 'violet' ? 'border-violet-500/20 bg-violet-500/10 text-violet-300'
                  : kindMeta[d.kind].tone === 'magenta' ? 'border-fuchsia-500/20 bg-fuchsia-500/10 text-fuchsia-300'
                  : 'border-emerald-500/20 bg-emerald-500/10 text-emerald-300'"
              >
                <AppIcon :name="kindMeta[d.kind].icon" class="h-4 w-4" />
              </span>
              <AdminPill :tone="catAccent[d.category] === 'magenta' ? 'magenta' : catAccent[d.category] === 'green' ? 'green' : catAccent[d.category] === 'violet' ? 'violet' : catAccent[d.category] === 'amber' ? 'amber' : 'cyan'">{{ d.category }}</AdminPill>
            </div>
            <p class="line-clamp-2 text-sm font-semibold text-foreground">{{ d.title }}</p>
            <p class="truncate text-xs text-muted-foreground">{{ d.author }} · {{ d.size }} · {{ d.uploaded }} trước</p>
          </button>
        </div>
      </AdminPanel>

      <!-- Right column: preview + categories -->
      <div class="space-y-4">
        <!-- Preview -->
        <AdminPanel title="Xem trước tài liệu" subtitle="Chi tiết &amp; hành động" icon="eye" accent="magenta">
          <!-- Empty preview -->
          <div
            v-if="!selectedDoc"
            class="flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-white/[0.08] bg-white/[0.02] py-12 text-center"
          >
            <span class="flex h-12 w-12 items-center justify-center rounded-xl bg-white/[0.04] text-muted-foreground">
              <AppIcon name="file-text" class="h-6 w-6" />
            </span>
            <p class="text-sm font-medium text-foreground">Chưa chọn tài liệu</p>
            <p class="px-4 text-xs text-muted-foreground">Chọn một tài liệu trong danh sách để xem chi tiết và duyệt nhanh.</p>
          </div>

          <div v-else class="space-y-4">
            <!-- Visual placeholder -->
            <div class="flex aspect-[4/3] items-center justify-center rounded-xl border border-white/[0.06] bg-gradient-to-br from-white/[0.04] to-white/[0.01]">
              <AppIcon :name="kindMeta[selectedDoc.kind].icon" class="h-12 w-12 text-muted-foreground/60" />
            </div>

            <div>
              <div class="flex items-center gap-2">
                <AdminPill :tone="kindMeta[selectedDoc.kind].tone">{{ kindMeta[selectedDoc.kind].label }}</AdminPill>
                <AdminPill :tone="statusMeta[selectedDoc.status].tone" dot>{{ statusMeta[selectedDoc.status].label }}</AdminPill>
              </div>
              <h4 class="mt-2 text-base font-bold leading-snug text-foreground">{{ selectedDoc.title }}</h4>
              <p class="mt-1.5 text-xs leading-relaxed text-muted-foreground">{{ selectedDoc.excerpt }}</p>
            </div>

            <!-- Meta grid -->
            <div class="grid grid-cols-2 gap-2 text-xs">
              <div class="rounded-lg border border-white/[0.05] bg-white/[0.02] px-3 py-2">
                <p class="text-muted-foreground">Danh mục</p>
                <p class="mt-0.5 font-medium text-foreground">{{ selectedDoc.category }}</p>
              </div>
              <div class="rounded-lg border border-white/[0.05] bg-white/[0.02] px-3 py-2">
                <p class="text-muted-foreground">Kích thước</p>
                <p class="mt-0.5 font-mono font-medium text-foreground">{{ selectedDoc.size }}</p>
              </div>
              <div class="rounded-lg border border-white/[0.05] bg-white/[0.02] px-3 py-2">
                <p class="text-muted-foreground">Số trang</p>
                <p class="mt-0.5 font-mono font-medium text-foreground">{{ selectedDoc.pages || '—' }}</p>
              </div>
              <div class="rounded-lg border border-white/[0.05] bg-white/[0.02] px-3 py-2">
                <p class="text-muted-foreground">Tải lên</p>
                <p class="mt-0.5 font-medium text-foreground">{{ selectedDoc.uploaded }} trước</p>
              </div>
            </div>

            <!-- Author -->
            <div class="flex items-center gap-3 rounded-xl border border-white/[0.05] bg-white/[0.02] px-3 py-2.5">
              <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-cyan-500/30 to-fuchsia-500/30 text-foreground">
                <AppIcon name="user" class="h-4 w-4" />
              </span>
              <div class="min-w-0">
                <p class="truncate text-sm font-medium text-foreground">{{ selectedDoc.author }}</p>
                <p class="truncate text-xs text-muted-foreground">{{ selectedDoc.authorRole }}</p>
              </div>
            </div>

            <p v-if="selectedDoc.flagReason" class="flex items-center gap-2 rounded-lg border border-rose-500/20 bg-rose-500/[0.06] px-3 py-2 text-xs text-rose-300">
              <AppIcon name="alert" class="h-4 w-4 shrink-0" /> Lý do: {{ selectedDoc.flagReason }}
            </p>

            <!-- Actions -->
            <div class="space-y-2">
              <div v-if="selectedDoc.status === 'pending' || selectedDoc.status === 'flagged'" class="grid grid-cols-2 gap-2">
                <button
                  class="flex items-center justify-center gap-1.5 rounded-xl bg-emerald-500/15 py-2 text-sm font-semibold text-emerald-300 transition-colors hover:bg-emerald-500/25"
                  @click="approve(selectedDoc.id)"
                >
                  <AppIcon name="check" class="h-4 w-4" /> Duyệt
                </button>
                <button
                  class="flex items-center justify-center gap-1.5 rounded-xl bg-rose-500/15 py-2 text-sm font-semibold text-rose-300 transition-colors hover:bg-rose-500/25"
                  @click="openReject(selectedDoc.id)"
                >
                  <AppIcon name="x" class="h-4 w-4" /> Từ chối
                </button>
              </div>
              <div class="grid grid-cols-2 gap-2">
                <button class="flex items-center justify-center gap-1.5 rounded-xl border border-white/[0.06] bg-white/[0.02] py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground">
                  <AppIcon name="download" class="h-4 w-4" /> Tải xuống
                </button>
                <button class="flex items-center justify-center gap-1.5 rounded-xl border border-white/[0.06] bg-white/[0.02] py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground">
                  <AppIcon name="external-link" class="h-4 w-4" /> Xem trang
                </button>
              </div>
            </div>
          </div>
        </AdminPanel>

        <!-- Categories -->
        <AdminPanel title="Danh mục" subtitle="Phân bố tài liệu" icon="folder" accent="violet">
          <ul class="space-y-3">
            <li v-for="c in categoryBreakdown" :key="c.key">
              <div class="mb-1 flex items-center justify-between text-xs">
                <span class="text-muted-foreground">{{ c.key }}</span>
                <span class="font-mono font-semibold text-foreground">{{ c.count }}</span>
              </div>
              <ProgressBar :value="c.pct" :accent="c.accent" />
            </li>
          </ul>
        </AdminPanel>
      </div>
    </div>

    <!-- Reject reason modal -->
    <Teleport to="body">
      <div v-if="rejectModalOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="cancelReject" />
        <div class="glass-panel relative z-10 w-full max-w-md rounded-2xl border border-white/[0.08] p-6">
          <div class="flex items-start justify-between">
            <div class="flex items-center gap-3">
              <span class="flex h-9 w-9 items-center justify-center rounded-xl border border-rose-500/20 bg-rose-500/10 text-rose-300">
                <AppIcon name="x" class="h-5 w-5" />
              </span>
              <div>
                <h3 class="text-sm font-semibold text-foreground">Từ chối tài liệu</h3>
                <p class="text-xs text-muted-foreground">Chọn lý do để gửi cho người tải lên</p>
              </div>
            </div>
            <button class="flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground" @click="cancelReject">
              <AppIcon name="x" class="h-4 w-4" />
            </button>
          </div>

          <div class="mt-5 grid grid-cols-1 gap-2">
            <button
              v-for="r in rejectReasons"
              :key="r.key"
              class="flex items-center gap-3 rounded-xl border px-3.5 py-2.5 text-left text-sm transition-colors"
              :class="
                rejectReasonKey === r.key
                  ? 'border-rose-500/30 bg-rose-500/[0.08] text-foreground'
                  : 'border-white/[0.06] bg-white/[0.02] text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'
              "
              @click="rejectReasonKey = r.key"
            >
              <AppIcon :name="r.icon" class="h-4 w-4" :class="rejectReasonKey === r.key ? 'text-rose-300' : ''" />
              <span class="flex-1 font-medium">{{ r.label }}</span>
              <AppIcon v-if="rejectReasonKey === r.key" name="check" class="h-4 w-4 text-rose-300" />
            </button>
          </div>

          <div class="mt-6 flex justify-end gap-2">
            <button class="rounded-xl border border-white/[0.06] bg-white/[0.02] px-4 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-white/[0.06] hover:text-foreground" @click="cancelReject">
              Huỷ
            </button>
            <button class="flex items-center gap-2 rounded-xl bg-rose-500/20 px-4 py-2 text-sm font-semibold text-rose-200 transition-colors hover:bg-rose-500/30" @click="confirmReject">
              <AppIcon name="x" class="h-4 w-4" /> Xác nhận từ chối
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
