<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted } from 'vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import GlassPanel from '@/components/app/GlassPanel.vue';
import SectionPanel from '@/components/app/SectionPanel.vue';
import Tabs from '@/components/app/Tabs.vue';

type Accent = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface StoreItem { id: string; name: string; desc: string; price: number; currency: 'coin' | 'gem'; icon: string; accent: Accent; category: string; owned: boolean; inWishlist: boolean; stock?: number; originalPrice?: number; bundleTag?: string; }

const coinBalance = ref(1250);
const gemBalance = ref(48);
const activeCategory = ref('vatpham');
const tabs = [{ id: 'vatpham', label: 'Vật phẩm' }, { id: 'giaodien', label: 'Giao diện' }, { id: 'mokhoa', label: 'Mở khoá' }, { id: 'goivip', label: 'Gói VIP' }];

const allItems: StoreItem[] = [
  { id: 'xp2-1h', name: 'x2 XP (1 giờ)', desc: 'Nhân đôi điểm kinh nghiệm trong 60 phút', price: 80, currency: 'coin', icon: 'zap', accent: 'amber', category: 'vatpham', owned: false, inWishlist: false },
  { id: 'streak-freeze', name: 'Đóng băng chuỗi', desc: 'Bảo vệ chuỗi học trong 1 ngày', price: 120, currency: 'coin', icon: 'shield', accent: 'cyan', category: 'vatpham', owned: true, inWishlist: false },
  { id: 'quiz-hint', name: 'Gợi ý quiz', desc: 'Hiện gợi ý cho câu hỏi khó nhất', price: 50, currency: 'coin', icon: 'lightbulb', accent: 'violet', category: 'vatpham', owned: false, inWishlist: true },
  { id: 'retake', name: 'Mở lại bài', desc: 'Làm lại một bài kiểm tra đã hoàn thành', price: 200, currency: 'coin', icon: 'rocket', accent: 'magenta', category: 'vatpham', owned: false, inWishlist: false },
  { id: 'coin-boost', name: 'Tăng tốc nạp coin', desc: 'Tăng 50% coin nhận được trong 2 giờ', price: 150, currency: 'coin', icon: 'flame', accent: 'amber', category: 'vatpham', owned: true, inWishlist: false },
  { id: 'energy-regen', name: 'Học liên tục', desc: 'Giảm thời gian hồi phục năng lượng', price: 90, currency: 'coin', icon: 'clock', accent: 'green', category: 'vatpham', owned: false, inWishlist: false },
  { id: 'galaxy-theme', name: 'Giao diện Thiên Hà', desc: 'Bầu trời đêm đầy sao lung linh', price: 300, currency: 'coin', icon: 'star', accent: 'violet', category: 'giaodien', owned: false, inWishlist: false },
  { id: 'neon-theme', name: 'Giao diện Neon', desc: 'Màu sắc rực rỡ, năng động', price: 250, currency: 'coin', icon: 'sparkles', accent: 'magenta', category: 'giaodien', owned: false, inWishlist: true },
  { id: 'forest-theme', name: 'Giao diện Rừng Xanh', desc: 'Thư giãn với tông màu xanh lá', price: 200, currency: 'coin', icon: 'graduation', accent: 'green', category: 'giaodien', owned: true, inWishlist: false },
  { id: 'avatar-frame-gold', name: 'Khung Avatar Vàng', desc: 'Khung avatar sang trọng phiên bản giới hạn', price: 15, currency: 'gem', icon: 'crown', accent: 'amber', category: 'giaodien', owned: false, inWishlist: false },
  { id: 'premium-deck', name: 'Bộ thẻ Cao cấp', desc: 'Mở khoá 500 thẻ ghi nhớ cao cấp', price: 500, currency: 'coin', icon: 'layers', accent: 'cyan', category: 'mokhoa', owned: false, inWishlist: false, originalPrice: 700, bundleTag: 'Giảm 29%' },
  { id: 'vip-docs', name: 'Tài liệu VIP', desc: 'Truy cập tài liệu cao cấp không giới hạn', price: 30, currency: 'gem', icon: 'file-text', accent: 'violet', category: 'mokhoa', owned: false, inWishlist: false },
  { id: 'analytics-pro', name: 'Phân tích Nâng cao', desc: 'Báo cáo học tập chi tiết với AI', price: 400, currency: 'coin', icon: 'chart', accent: 'magenta', category: 'mokhoa', owned: false, inWishlist: false },
  { id: 'mentor-pass', name: 'Vé Cố vấn', desc: '3 buổi tư vấn 1-1 với cố vấn chuyên nghiệp', price: 25, currency: 'gem', icon: 'users', accent: 'green', category: 'mokhoa', owned: false, inWishlist: true },
  { id: 'flash-sale-xp', name: 'Flash Sale: x2 XP (3 giờ)', desc: 'Ưu đãi giới hạn — nhân đôi XP trong 3 giờ!', price: 150, currency: 'coin', icon: 'zap', accent: 'amber', category: 'vatpham', owned: false, inWishlist: false, stock: 7, originalPrice: 240, bundleTag: 'Flash Sale' },
  { id: 'bundle-starter', name: 'Gói Khởi đầu', desc: 'x2 XP + Đóng băng chuỗi + 100 coin', price: 180, currency: 'coin', icon: 'gift', accent: 'cyan', category: 'vatpham', owned: false, inWishlist: false, originalPrice: 300, bundleTag: 'Tiết kiệm 40%' },
];

const visibleItems = computed(() => allItems.filter(i => i.category === activeCategory.value));
const wishlistItems = computed(() => allItems.filter(i => i.inWishlist));
const ownedItems = computed(() => allItems.filter(i => i.owned));

const flashSaleSeconds = ref(3 * 3600 + 24 * 60 + 18);
let countdownTimer: number | null = null;
const flashCountdown = computed(() => {
  const h = Math.floor(flashSaleSeconds.value / 3600);
  const m = Math.floor((flashSaleSeconds.value % 3600) / 60);
  const s = flashSaleSeconds.value % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

interface VipTier { id: string; name: string; price: number; period: string; accent: Accent; popular?: boolean; benefits: string[]; }
const vipTiers: VipTier[] = [
  { id: 'bronze', name: 'Đồng', price: 0, period: 'miễn phí', accent: 'amber', benefits: ['5 tài liệu/tháng', 'Quiz cơ bản', 'Bảng xếp hạng nhóm bạn'] },
  { id: 'silver', name: 'Bạc', price: 200, period: 'tháng', accent: 'cyan', popular: true, benefits: ['Tài liệu không giới hạn', 'x2 coin mỗi ngày', 'Flashcard nâng cao', 'Phân tích chi tiết', 'Ưu đãi cửa hàng 10%'] },
  { id: 'gold', name: 'Vàng', price: 500, period: 'tháng', accent: 'violet', benefits: ['Tất cả quyền Bạc', 'x3 coin + 5 gem/tháng', 'Tài liệu VIP', 'Cố vấn 1 buổi/tháng', 'Ưu đãi cửa hàng 20%', 'Giao diện độc quyền'] },
];

const purchaseModal = reactive({ open: false, item: null as StoreItem | null, processing: false, success: false, error: '' });
const insufficientFunds = computed(() => {
  if (!purchaseModal.item) return false;
  if (purchaseModal.item.currency === 'coin') return coinBalance.value < purchaseModal.item.price;
  return gemBalance.value < purchaseModal.item.price;
});

function openPurchase(item: StoreItem) {
  if (item.owned) return;
  purchaseModal.open = true; purchaseModal.item = item; purchaseModal.processing = false; purchaseModal.success = false; purchaseModal.error = '';
}
function closePurchase() { purchaseModal.open = false; purchaseModal.item = null; }
function confirmPurchase() {
  if (!purchaseModal.item || purchaseModal.processing) return;
  purchaseModal.processing = true;
  setTimeout(() => {
    const item = purchaseModal.item!;
    if (item.currency === 'coin' && coinBalance.value < item.price) { purchaseModal.error = 'Không đủ coin.'; purchaseModal.processing = false; return; }
    if (item.currency === 'gem' && gemBalance.value < item.price) { purchaseModal.error = 'Không đủ gem.'; purchaseModal.processing = false; return; }
    if (item.currency === 'coin') coinBalance.value -= item.price; else gemBalance.value -= item.price;
    item.owned = true; item.inWishlist = false;
    purchaseModal.success = true; purchaseModal.processing = false;
    setTimeout(() => { if (purchaseModal.item === item) closePurchase(); }, 1500);
  }, 600);
}
function toggleWishlist(item: StoreItem) { item.inWishlist = !item.inWishlist; }

const accentBg: Record<Accent, string> = { cyan: 'bg-cyan-500/10 text-cyan-300', magenta: 'bg-fuchsia-500/10 text-fuchsia-300', green: 'bg-emerald-500/10 text-emerald-300', violet: 'bg-violet-500/10 text-violet-300', amber: 'bg-amber-500/10 text-amber-300' };

onMounted(() => { countdownTimer = window.setInterval(() => { if (flashSaleSeconds.value > 0) flashSaleSeconds.value--; }, 1000); });
onUnmounted(() => { if (countdownTimer) window.clearInterval(countdownTimer); });
</script>

<template>
  <div class="space-y-6">
    <GlassPanel glow="amber" class="p-5 sm:p-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="cosmic-badge">Cửa hàng vật phẩm</p>
          <h1 class="mt-2 text-2xl font-bold text-foreground sm:text-3xl">Cửa hàng Coin</h1>
          <p class="mt-1 text-sm text-muted-foreground">Dùng coin và gem để mở khoá vật phẩm, giao diện và đặc quyền.</p>
        </div>
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-2 rounded-sm border border-amber-400/20 bg-amber-500/10 px-4 py-2.5">
            <AppIcon name="coin" class="h-6 w-6 text-amber-300" />
            <div><p class="text-lg font-bold leading-none text-amber-200 tabular-nums">{{ coinBalance.toLocaleString('vi-VN') }}</p><p class="mt-0.5 text-[0.65rem] text-muted-foreground">Coin</p></div>
          </div>
          <div class="flex items-center gap-2 rounded-sm border border-violet-400/20 bg-violet-500/10 px-4 py-2.5">
            <AppIcon name="gem" class="h-6 w-6 text-violet-300" />
            <div><p class="text-lg font-bold leading-none text-violet-200 tabular-nums">{{ gemBalance }}</p><p class="mt-0.5 text-[0.65rem] text-muted-foreground">Gem</p></div>
          </div>
        </div>
      </div>
    </GlassPanel>

    <div v-if="activeCategory === 'vatpham'" class="relative overflow-hidden rounded-sm border border-amber-400/20 from-amber-500/10 via-orange-500/5 to-transparent p-4 sm:p-5">
      <div class="flex items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <span class="flex h-12 w-12 items-center justify-center rounded-sm bg-amber-500/20 text-amber-300"><AppIcon name="flame" class="h-6 w-6" /></span>
          <div><p class="font-bold text-amber-100">Flash Sale đang diễn ra</p><p class="text-xs text-muted-foreground">Giảm 37% — chỉ còn 7 suất</p></div>
        </div>
        <div class="text-right"><p class="text-[0.65rem] uppercase tracking-wider text-muted-foreground">Kết thúc sau</p><p class="font-mono text-xl font-bold text-amber-300 tabular-nums">{{ flashCountdown }}</p></div>
      </div>
    </div>

    <Tabs :tabs="[...tabs]" :model-value="activeCategory" @update:model-value="(v) => (activeCategory = v as string)" />

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      <div v-for="item in visibleItems" :key="item.id" class="group relative flex flex-col rounded-sm border border-white/[0.06] bg-white/[0.02] p-4 transition-all hover:border-white/[0.14]" :class="{ 'opacity-60': item.owned }">
        <span v-if="item.bundleTag" class="absolute -top-2 right-3 rounded-sm from-amber-500 to-orange-500 px-2.5 py-0.5 text-[0.62rem] font-bold text-slate-900">{{ item.bundleTag }}</span>
        <div class="flex items-start justify-between gap-3">
          <span class="flex h-12 w-12 items-center justify-center rounded-sm" :class="accentBg[item.accent]"><AppIcon :name="item.icon" class="h-6 w-6" /></span>
          <button type="button" class="rounded-sm p-1.5 text-muted-foreground transition-colors hover:text-fuchsia-300" :class="{ 'text-fuchsia-400': item.inWishlist }" :aria-label="item.inWishlist ? 'Bỏ khỏi wishlist' : 'Thêm vào wishlist'" @click="toggleWishlist(item)"><AppIcon name="heart" class="h-4 w-4" /></button>
        </div>
        <div class="mt-3 flex-1"><p class="font-semibold text-foreground">{{ item.name }}</p><p class="mt-0.5 text-xs leading-relaxed text-muted-foreground">{{ item.desc }}</p></div>
        <div v-if="item.stock !== undefined" class="mt-2"><div class="flex items-center justify-between text-[0.7rem] text-muted-foreground mb-1"><span>Còn lại</span><span class="font-semibold text-amber-300">{{ item.stock }}</span></div><div class="h-1 w-full overflow-hidden rounded-sm bg-white/[0.06]"><div class="h-full rounded-sm bg-amber-400" :style="{ width: `${(item.stock / 20) * 100}%` }" /></div></div>
        <div class="mt-3 flex items-center justify-between gap-2">
          <div class="flex items-center gap-1.5">
            <span v-if="item.originalPrice" class="text-xs text-muted-foreground line-through tabular-nums">{{ item.originalPrice }}</span>
            <span class="inline-flex items-center gap-1 text-sm font-bold tabular-nums" :class="item.currency === 'coin' ? 'text-amber-300' : 'text-violet-300'"><AppIcon :name="item.currency === 'coin' ? 'coin' : 'gem'" class="h-3.5 w-3.5" />{{ item.price }}</span>
          </div>
          <button v-if="item.owned" type="button" class="rounded-sm border border-emerald-400/20 bg-emerald-500/10 px-3 py-1.5 text-xs font-semibold text-emerald-300" disabled>Đã sở hữu</button>
          <button v-else type="button" class="cosmic-btn rounded-sm px-3 py-1.5 text-xs" @click="openPurchase(item)">Mua</button>
        </div>
      </div>
    </div>

    <SectionPanel v-if="activeCategory === 'goivip'" title="Gói VIP" subtitle="Nâng cấp để mở khoá toàn bộ tính năng" icon="crown" accent="amber">
      <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
        <div v-for="tier in vipTiers" :key="tier.id" class="relative rounded-sm border p-5" :class="tier.popular ? 'border-cyan-400/40 bg-cyan-500/[0.06]' : 'border-white/[0.06] bg-white/[0.02]'">
          <span v-if="tier.popular" class="absolute -top-2.5 left-1/2 -translate-x-1/2 rounded-sm from-cyan-400 to-violet-500 px-3 py-0.5 text-[0.62rem] font-bold text-slate-900">Phổ biến nhất</span>
          <p class="text-sm font-semibold uppercase tracking-wider" :class="tier.accent === 'amber' ? 'text-amber-300' : tier.accent === 'cyan' ? 'text-cyan-300' : 'text-violet-300'">{{ tier.name }}</p>
          <div class="mt-2 flex items-baseline gap-1"><span class="text-2xl font-bold text-foreground">{{ tier.price === 0 ? '0' : tier.price }}</span><span v-if="tier.price > 0" class="text-sm text-muted-foreground">coin / {{ tier.period }}</span><span v-else class="text-sm text-muted-foreground">{{ tier.period }}</span></div>
          <ul class="mt-4 space-y-2"><li v-for="b in tier.benefits" :key="b" class="flex items-start gap-2 text-xs text-muted-foreground"><AppIcon name="check-circle" class="mt-0.5 h-3.5 w-3.5 shrink-0 text-emerald-400" /><span>{{ b }}</span></li></ul>
          <button type="button" class="mt-5 w-full rounded-sm py-2 text-sm font-semibold transition-colors" :class="tier.popular ? 'cosmic-btn' : 'border border-white/10 bg-white/[0.03] text-foreground hover:border-white/20'" :disabled="tier.id === 'bronze'">{{ tier.id === 'bronze' ? 'Đang sử dụng' : 'Nâng cấp' }}</button>
        </div>
      </div>
    </SectionPanel>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <SectionPanel title="Wishlist" :subtitle="`${wishlistItems.length} mục`" icon="heart" accent="magenta">
        <div v-if="wishlistItems.length === 0" class="py-8 text-center text-sm text-muted-foreground">Chưa có vật phẩm nào trong wishlist.</div>
        <ul v-else class="space-y-2">
          <li v-for="item in wishlistItems" :key="item.id" class="flex items-center gap-3 rounded-sm border border-white/[0.04] bg-white/[0.01] px-3 py-2.5">
            <span class="flex h-8 w-8 items-center justify-center rounded-sm" :class="accentBg[item.accent]"><AppIcon :name="item.icon" class="h-4 w-4" /></span>
            <div class="min-w-0 flex-1"><p class="truncate text-sm font-medium text-foreground">{{ item.name }}</p><p class="inline-flex items-center gap-1 text-xs text-muted-foreground"><AppIcon :name="item.currency === 'coin' ? 'coin' : 'gem'" class="h-3 w-3" />{{ item.price }}</p></div>
            <button type="button" class="cosmic-btn rounded-sm px-2.5 py-1 text-xs" @click="openPurchase(item)">Mua</button>
            <button type="button" class="rounded-sm p-1.5 text-muted-foreground hover:text-rose-300" @click="toggleWishlist(item)"><AppIcon name="x" class="h-3.5 w-3.5" /></button>
          </li>
        </ul>
      </SectionPanel>
      <SectionPanel title="Kho đồ" :subtitle="`${ownedItems.length} mục đã sở hữu`" icon="grid" accent="cyan">
        <div v-if="ownedItems.length === 0" class="py-8 text-center text-sm text-muted-foreground">Chưa sở hữu vật phẩm nào.</div>
        <div v-else class="grid grid-cols-2 gap-2 sm:grid-cols-3">
          <div v-for="item in ownedItems" :key="item.id" class="flex items-center gap-2 rounded-sm border border-emerald-400/10 bg-emerald-500/[0.04] px-2.5 py-2">
            <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-sm" :class="accentBg[item.accent]"><AppIcon :name="item.icon" class="h-3.5 w-3.5" /></span>
            <span class="truncate text-xs font-medium text-foreground">{{ item.name }}</span>
          </div>
        </div>
      </SectionPanel>
    </div>

    <Teleport to="body">
      <div v-if="purchaseModal.open" class="fixed inset-0 z-[100] flex items-center justify-center px-4" @click.self="closePurchase">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" />
        <div class="glass-panel relative z-10 w-full max-w-sm rounded-sm p-5">
          <div v-if="!purchaseModal.success">
            <p class="text-sm font-semibold text-foreground">Xác nhận mua</p>
            <div v-if="purchaseModal.item" class="mt-4 flex items-center gap-3 rounded-sm border border-white/[0.06] bg-white/[0.02] p-3">
              <span class="flex h-10 w-10 items-center justify-center rounded-sm" :class="accentBg[purchaseModal.item.accent]"><AppIcon :name="purchaseModal.item.icon" class="h-5 w-5" /></span>
              <div class="min-w-0 flex-1"><p class="truncate text-sm font-medium text-foreground">{{ purchaseModal.item.name }}</p><p class="inline-flex items-center gap-1 text-xs text-muted-foreground"><AppIcon :name="purchaseModal.item.currency === 'coin' ? 'coin' : 'gem'" class="h-3 w-3" />{{ purchaseModal.item.price }}</p></div>
            </div>
            <div class="mt-3 flex items-center justify-between text-sm">
              <span class="text-muted-foreground">Số dư hiện tại</span>
              <span class="inline-flex items-center gap-1 font-semibold tabular-nums" :class="purchaseModal.item?.currency === 'coin' ? 'text-amber-300' : 'text-violet-300'"><AppIcon :name="purchaseModal.item?.currency === 'coin' ? 'coin' : 'gem'" class="h-3.5 w-3.5" />{{ (purchaseModal.item?.currency === 'coin' ? coinBalance : gemBalance).toLocaleString('vi-VN') }}</span>
            </div>
            <div class="mt-1 flex items-center justify-between text-sm">
              <span class="text-muted-foreground">Sau khi mua</span>
              <span class="inline-flex items-center gap-1 font-semibold tabular-nums text-muted-foreground"><AppIcon :name="purchaseModal.item?.currency === 'coin' ? 'coin' : 'gem'" class="h-3.5 w-3.5" />{{ ((purchaseModal.item?.currency === 'coin' ? coinBalance : gemBalance) - (purchaseModal.item?.price ?? 0)).toLocaleString('vi-VN') }}</span>
            </div>
            <p v-if="insufficientFunds" class="mt-3 rounded-sm border border-rose-400/20 bg-rose-500/10 px-3 py-2 text-xs text-rose-300">Không đủ {{ purchaseModal.item?.currency === 'coin' ? 'coin' : 'gem' }} để mua vật phẩm này.</p>
            <p v-if="purchaseModal.error" class="mt-3 rounded-sm border border-rose-400/20 bg-rose-500/10 px-3 py-2 text-xs text-rose-300">{{ purchaseModal.error }}</p>
            <div class="mt-5 flex gap-2">
              <button type="button" class="flex-1 rounded-sm border border-white/10 bg-white/[0.03] py-2.5 text-sm font-medium text-foreground transition-colors hover:border-white/20" @click="closePurchase">Huỷ</button>
              <button type="button" class="cosmic-btn flex-1 rounded-sm py-2.5 text-sm" :disabled="purchaseModal.processing || insufficientFunds" @click="confirmPurchase">{{ purchaseModal.processing ? 'Đang xử lý…' : 'Xác nhận' }}</button>
            </div>
          </div>
          <div v-else class="flex flex-col items-center gap-3 py-4 text-center">
            <span class="flex h-14 w-14 items-center justify-center rounded-sm bg-emerald-500/15 text-emerald-300"><AppIcon name="check-circle" class="h-7 w-7" /></span>
            <p class="font-semibold text-foreground">Mua thành công!</p><p class="text-xs text-muted-foreground">{{ purchaseModal.item?.name }} đã được thêm vào kho đồ.</p>
          </div>
        </div>
      </div>
    </Teleport>

    <p class="pt-2 text-center text-xs text-muted-foreground/70">Giao diện demo · dữ liệu hiển thị là mẫu, chưa kết nối hệ thống thực.</p>
  </div>
</template>
