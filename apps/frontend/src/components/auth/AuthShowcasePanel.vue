<script setup lang="ts">
import { computed, inject, nextTick, onMounted, onUnmounted } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import FlowParticles from '@/components/ui/FlowParticles.vue';

type GsapType = typeof import('gsap').gsap;

const props = withDefaults(defineProps<{ variant?: 'login' | 'register' }>(), {
  variant: 'login'
});

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);

let cleanupFns: Array<() => void> = [];

const registerCleanup = (item: unknown) => {
  if (!item) return;
  if (Array.isArray(item)) {
    item.forEach(registerCleanup);
    return;
  }
  if (typeof item === 'function') {
    cleanupFns.push(item as () => void);
    return;
  }
  if (typeof (item as { kill?: () => void }).kill === 'function') {
    cleanupFns.push(() => (item as { kill: () => void }).kill());
  }
};

const heading = computed(() =>
  props.variant === 'register'
    ? 'Khởi tạo workspace cho cộng đồng học tập của bạn.'
    : 'Mỗi lần đăng nhập là một bước tiến của cả cộng đồng.'
);

const subtitle = computed(() =>
  props.variant === 'register'
    ? 'Biến kho tài liệu thành nhiệm vụ, thưởng coin, mở khóa VIP và leo bảng xếp hạng — tất cả trong một workspace cho CLB, nhóm luyện thi hay lớp gia sư.'
    : 'Tài liệu của bạn vẫn ở đây, nhiệm vụ đang chờ, streak chưa hề nguội. Đăng nhập để tiếp tục tích coin và giữ vững phong độ deep work.'
);

const features = [
  'Kho tài liệu thông minh — phân loại, gắn thẻ, tìm kiếm tức thì',
  'Nhiệm vụ & coin — giao việc, theo dõi tiến độ, trao thưởng',
  'Streak & deep work — giữ nhịp học mỗi ngày, vào chế độ tập trung',
  'Bảng xếp hạng & thành tích — tạo động lực cho cả nhóm',
  'Mở khóa VIP — đổi coin lấy đặc quyền trong workspace'
];

const leaderboardRows = [
  '01 · Lớp 12A1 · 4.820 coin',
  '02 · Nhóm Toán nâng cao · 4.515 coin',
  '03 · CLB Tiếng Anh · 4.190 coin'
];

const communityBadges = ['CLB Đại học', 'Nhóm luyện thi', 'Lớp gia sư', 'Trung tâm', 'Tự học'];

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.feature-item', '.stat-tile'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(
      animation?.countUp?.('.stat-doc', { end: 1280, suffix: '+', duration: 1.6 })
    );
    registerCleanup(animation?.countUp?.('.stat-mission', { end: 9450, duration: 1.8 }));
    registerCleanup(animation?.countUp?.('.stat-streak', { end: 36, duration: 1.2 }));
    registerCleanup(animation?.countUp?.('.stat-member', { end: 312, duration: 1.4 }));

    registerCleanup(animation?.batchReveal?.('.feature-item', { stagger: 0.08, y: 16 }));
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => {
    if (fn && typeof fn === 'function') fn();
  });
  cleanupFns = [];
});
</script>

<template>
  <aside class="showcase cosmic-glass" aria-label="Giới thiệu Gamified Study Hub">
    <FlowParticles
      class="showcase-particles"
      :count="5"
      color="#22d3ee"
      speed="slow"
      :opacity="0.18"
      aria-hidden="true"
    />

    <!-- BLOCK 1 — header -->
    <header class="showcase-header animate-slide-up">
      <span class="cosmic-badge">TRUNG TÂM ĐIỀU PHỐI</span>
      <h2 class="cosmic-gradient-text">{{ heading }}</h2>
      <p class="showcase-sub">{{ subtitle }}</p>
    </header>

    <!-- BLOCK 2 — stat grid -->
    <div class="stat-block">
      <div class="stat-grid" role="group" aria-label="Chỉ số minh hoạ">
        <div class="stat-tile cosmic-glass animate-slide-up delay-200">
          <span class="stat-doc stat-value" aria-hidden="true">0</span>
          <span class="stat-tile-label">Tài liệu trong kho</span>
        </div>
        <div class="stat-tile cosmic-glass animate-slide-up delay-300">
          <span class="stat-mission stat-value" aria-hidden="true">0</span>
          <span class="stat-tile-label">Nhiệm vụ đã hoàn thành</span>
        </div>
        <div class="stat-tile cosmic-glass animate-slide-up delay-400">
          <span class="stat-value-row">
            <span class="stat-streak stat-value" aria-hidden="true">0</span>
            <span class="stat-unit">ngày</span>
          </span>
          <span class="stat-tile-label">Streak dài nhất</span>
        </div>
        <div class="stat-tile cosmic-glass animate-slide-up delay-500">
          <span class="stat-member stat-value" aria-hidden="true">0</span>
          <span class="stat-tile-label">Thành viên cộng đồng</span>
        </div>
      </div>
      <p class="figure-note">Số liệu minh hoạ cho mục tiêu sản phẩm.</p>
    </div>

    <!-- BLOCK 3 — feature checklist -->
    <ul class="feature-list">
      <li v-for="feature in features" :key="feature" class="feature-item">
        <span class="check glow-cyan" aria-hidden="true">✓</span>
        <span class="feature-text">{{ feature }}</span>
      </li>
    </ul>

    <!-- BLOCK 4 — mini leaderboard preview -->
    <div class="mini-preview cosmic-glass scan-line animate-fade-in delay-300" aria-hidden="true">
      <div class="mini-preview-head">
        <span class="mini-preview-title">Bảng xếp hạng tuần (xem trước)</span>
        <div class="streak-ring">
          <span class="streak-ring-text">36🔥</span>
        </div>
      </div>
      <ol class="lb-list">
        <li v-for="row in leaderboardRows" :key="row" class="lb-row">{{ row }}</li>
      </ol>
      <p class="mini-preview-note">Xem trước giao diện — dữ liệu minh hoạ.</p>
    </div>

    <!-- BLOCK 5 — status rail -->
    <div class="status-rail" role="list" aria-label="Trạng thái hệ thống">
      <span class="status-chip" role="listitem">
        <span class="status-dot pulse-glow-green" aria-hidden="true"></span>
        Hệ thống ổn định
      </span>
      <span class="status-chip" role="listitem">
        <span class="status-dot pulse-glow-cyan" aria-hidden="true"></span>
        Đồng bộ tức thì
      </span>
      <span class="status-chip" role="listitem">
        <span class="status-dot pulse-glow-magenta" aria-hidden="true"></span>
        Sẵn sàng cho CLB & gia sư
      </span>
    </div>

    <!-- BLOCK 6 — testimonial -->
    <blockquote class="testimonial-card cosmic-glass cosmic-glass-hover">
      <div class="testimonial-stars glow-cyan" aria-hidden="true">★★★★★</div>
      <p class="testimonial-quote">
        GSH giúp CLB mình biến kho đề thi lộn xộn thành lộ trình ôn rõ ràng. Tụi mình thi nhau giữ
        streak, lớp chưa bao giờ đông vui như vậy.
      </p>
      <footer class="testimonial-foot">
        <span class="testimonial-avatar" aria-hidden="true">MA</span>
        <cite class="testimonial-cite">— Minh Anh · Chủ nhiệm CLB Tiếng Anh</cite>
      </footer>
      <span class="cosmic-badge testimonial-honesty">Câu chuyện minh hoạ</span>
    </blockquote>

    <!-- BLOCK 7 — community badges -->
    <div class="community-badges">
      <div class="community-pills">
        <span v-for="badge in communityBadges" :key="badge" class="cosmic-badge">{{ badge }}</span>
      </div>
      <p class="community-caption">Được xây dựng cho mọi cộng đồng học tập.</p>
    </div>
  </aside>
</template>

<style scoped>
/* ── local (non-global) animation primitives ───────────────────────────── */
@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.animate-slide-up {
  animation: slide-up 0.8s ease-out both;
}

.animate-fade-in {
  animation: fade-in 1s ease-out both;
}

.delay-200 {
  animation-delay: 0.2s;
}
.delay-300 {
  animation-delay: 0.3s;
}
.delay-400 {
  animation-delay: 0.4s;
}
.delay-500 {
  animation-delay: 0.5s;
}

/* Local pill (matches the global cosmic-badge spec, scoped per instructions). */
.cosmic-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: oklch(0.82 0.16 195);
  background: oklch(0.78 0.18 195 / 10%);
  border: 1px solid oklch(0.78 0.18 195 / 35%);
}

/* ── root aside ─────────────────────────────────────────────────────────── */
.showcase {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: clamp(1.5rem, 3vw, 2.25rem);
  width: 100%;
  padding: clamp(1.75rem, 3.5vw, 2.75rem);
  border-radius: 1.5rem;
  overflow: hidden;
  color: oklch(0.96 0.01 240);
  isolation: isolate;
}

.showcase-particles {
  z-index: 0;
}

.showcase > *:not(.showcase-particles) {
  position: relative;
  z-index: 1;
}

/* ── BLOCK 1 header ─────────────────────────────────────────────────────── */
.showcase-header {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.showcase-header h2 {
  margin: 0;
  font-size: clamp(1.55rem, 2.6vw, 2.05rem);
  line-height: 1.18;
  letter-spacing: -0.01em;
}

.showcase-sub {
  margin: 0;
  color: oklch(0.7 0.03 240);
  font-size: clamp(0.95rem, 1.4vw, 1.05rem);
  line-height: 1.7;
}

/* ── BLOCK 2 stat grid ──────────────────────────────────────────────────── */
.stat-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.85rem;
}

.stat-tile {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  padding: 1.05rem 1.1rem;
  border-radius: 1rem;
}

.stat-value-row {
  display: inline-flex;
  align-items: baseline;
  gap: 0.4rem;
}

.stat-value {
  font-size: clamp(1.7rem, 3vw, 2.25rem);
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(120deg, oklch(0.82 0.16 195), oklch(0.68 0.22 300));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.stat-unit {
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: oklch(0.84 0.16 85);
}

.stat-tile-label {
  font-size: 0.78rem;
  line-height: 1.4;
  color: oklch(0.7 0.03 240);
}

.figure-note {
  margin: 0;
  font-size: 0.72rem;
  font-style: italic;
  color: oklch(0.7 0.03 240 / 75%);
}

/* ── BLOCK 3 feature checklist ──────────────────────────────────────────── */
.feature-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 0.7rem;
  font-size: 0.92rem;
  line-height: 1.5;
  color: oklch(0.96 0.01 240 / 92%);
}

.check {
  flex: none;
  display: grid;
  place-items: center;
  width: 1.4rem;
  height: 1.4rem;
  margin-top: 0.05rem;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 800;
  color: oklch(0.82 0.16 195);
  background: oklch(0.82 0.16 195 / 12%);
  border: 1px solid oklch(0.82 0.16 195 / 40%);
}

/* ── BLOCK 4 mini leaderboard preview ───────────────────────────────────── */
.mini-preview {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  padding: 1.15rem 1.25rem;
  border-radius: 1.1rem;
}

.mini-preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.mini-preview-title {
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: oklch(0.82 0.16 195);
}

.streak-ring {
  position: relative;
  flex: none;
  display: grid;
  place-items: center;
  width: 3.6rem;
  height: 3.6rem;
  border-radius: 999px;
  background: conic-gradient(
    oklch(0.82 0.16 195) 0% 80%,
    oklch(0.18 0.03 280) 80% 100%
  );
}

.streak-ring::before {
  content: '';
  position: absolute;
  width: 2.7rem;
  height: 2.7rem;
  border-radius: 999px;
  background: oklch(0.11 0.024 280);
}

.streak-ring-text {
  position: relative;
  font-size: 0.82rem;
  font-weight: 800;
  color: oklch(0.96 0.01 240);
}

.lb-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.lb-row {
  padding: 0.6rem 0.8rem;
  border-radius: 0.7rem;
  font-size: 0.85rem;
  font-variant-numeric: tabular-nums;
  color: oklch(0.96 0.01 240 / 90%);
  background: oklch(0.16 0.035 280 / 55%);
  border: 1px solid oklch(0.7 0.1 280 / 14%);
}

.lb-row:first-child {
  border-color: oklch(0.84 0.16 85 / 35%);
  box-shadow: inset 0 0 18px oklch(0.84 0.16 85 / 10%);
}

.mini-preview-note {
  margin: 0;
  font-size: 0.7rem;
  font-style: italic;
  color: oklch(0.7 0.03 240 / 75%);
}

/* ── BLOCK 5 status rail ────────────────────────────────────────────────── */
.status-rail {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.8rem;
  border-radius: 999px;
  font-size: 0.78rem;
  color: oklch(0.96 0.01 240 / 88%);
  background: oklch(0.16 0.035 280 / 55%);
  border: 1px solid oklch(0.7 0.1 280 / 16%);
}

.status-dot {
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 999px;
  background: oklch(0.78 0.16 150);
}

.status-chip:nth-child(2) .status-dot {
  background: oklch(0.82 0.16 195);
}

.status-chip:nth-child(3) .status-dot {
  background: oklch(0.68 0.22 330);
}

/* ── BLOCK 6 testimonial ────────────────────────────────────────────────── */
.testimonial-card {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  margin: 0;
  padding: 1.25rem 1.35rem;
  border-radius: 1.1rem;
}

.testimonial-stars {
  font-size: 0.95rem;
  letter-spacing: 0.18em;
  color: oklch(0.84 0.16 85);
}

.testimonial-quote {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.65;
  color: oklch(0.96 0.01 240 / 94%);
}

.testimonial-foot {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.testimonial-avatar {
  flex: none;
  display: grid;
  place-items: center;
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 999px;
  font-size: 0.85rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: oklch(0.1 0.02 280);
  background: linear-gradient(135deg, oklch(0.82 0.16 195), oklch(0.68 0.22 350));
  box-shadow: 0 0 18px oklch(0.78 0.18 195 / 35%);
}

.testimonial-cite {
  font-style: normal;
  font-size: 0.82rem;
  color: oklch(0.7 0.03 240);
}

.testimonial-honesty {
  align-self: flex-start;
}

/* ── BLOCK 7 community badges ───────────────────────────────────────────── */
.community-badges {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.community-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
}

.community-caption {
  margin: 0;
  font-size: 0.78rem;
  color: oklch(0.7 0.03 240);
}

/* ── responsive ─────────────────────────────────────────────────────────── */
@media (max-width: 640px) {
  .showcase {
    padding: 1.5rem 1.25rem;
    border-radius: 1.25rem;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }

  .mini-preview-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .animate-slide-up,
  .animate-fade-in {
    animation: none;
  }

  .scan-line::after {
    animation: none;
  }
}
</style>
