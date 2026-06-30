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
  <aside class="showcase" aria-label="Giới thiệu Gamified Study Hub">
    <FlowParticles
      class="showcase-particles"
      :count="5"
      color="#22d3ee"
      speed="slow"
      :opacity="0.12"
      aria-hidden="true"
    />

    <header class="showcase-header">
      <span class="showcase-badge">Trung tâm điều phối</span>
      <h2 class="showcase-heading">{{ heading }}</h2>
      <p class="showcase-sub">{{ subtitle }}</p>
    </header>

    <div class="stat-block">
      <div class="stat-grid" role="group" aria-label="Chỉ số minh hoạ">
        <div class="stat-tile">
          <span class="stat-doc stat-value" aria-hidden="true">0</span>
          <span class="stat-tile-label">Tài liệu trong kho</span>
        </div>
        <div class="stat-tile">
          <span class="stat-mission stat-value" aria-hidden="true">0</span>
          <span class="stat-tile-label">Nhiệm vụ đã hoàn thành</span>
        </div>
        <div class="stat-tile">
          <span class="stat-value-row">
            <span class="stat-streak stat-value" aria-hidden="true">0</span>
            <span class="stat-unit">ngày</span>
          </span>
          <span class="stat-tile-label">Streak dài nhất</span>
        </div>
        <div class="stat-tile">
          <span class="stat-member stat-value" aria-hidden="true">0</span>
          <span class="stat-tile-label">Thành viên cộng đồng</span>
        </div>
      </div>
      <p class="figure-note">Số liệu minh hoạ cho mục tiêu sản phẩm.</p>
    </div>

    <ul class="feature-list">
      <li v-for="feature in features" :key="feature" class="feature-item">
        <span class="check" aria-hidden="true">✓</span>
        <span class="feature-text">{{ feature }}</span>
      </li>
    </ul>

    <div class="mini-preview" aria-hidden="true">
      <div class="mini-preview-head">
        <span class="mini-preview-title">Bảng xếp hạng tuần</span>
        <div class="streak-ring">
          <span class="streak-ring-text">36🔥</span>
        </div>
      </div>
      <ol class="lb-list">
        <li v-for="row in leaderboardRows" :key="row" class="lb-row">{{ row }}</li>
      </ol>
    </div>

    <div class="status-rail" role="list" aria-label="Trạng thái hệ thống">
      <span class="status-chip" role="listitem">
        <span class="status-dot" aria-hidden="true"></span>
        Hệ thống ổn định
      </span>
      <span class="status-chip" role="listitem">
        <span class="status-dot" aria-hidden="true"></span>
        Đồng bộ tức thì
      </span>
      <span class="status-chip" role="listitem">
        <span class="status-dot" aria-hidden="true"></span>
        Sẵn sàng cho CLB & gia sư
      </span>
    </div>
  </aside>
</template>

<style scoped>
.showcase {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: clamp(1.25rem, 2.5vw, 1.75rem);
  width: 100%;
  padding: clamp(1.5rem, 3vw, 2rem);
  border-radius: 1.25rem;
  overflow: hidden;
  color: #E8E8F0;
  isolation: isolate;
  background: rgba(14, 14, 22, 0.5);
  backdrop-filter: blur(20px) saturate(130%);
  -webkit-backdrop-filter: blur(20px) saturate(130%);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.showcase-particles {
  z-index: 0;
}

.showcase > *:not(.showcase-particles) {
  position: relative;
  z-index: 1;
}

/* ── Header ─────────────────────────────────────────────────────────────── */
.showcase-header {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.showcase-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.3rem 0.65rem;
  border-radius: 999px;
  font-size: 0.66rem;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #22D3EE;
  background: rgba(34, 211, 238, 0.06);
  border: 1px solid rgba(34, 211, 238, 0.12);
  align-self: flex-start;
}

.showcase-heading {
  margin: 0;
  font-size: clamp(1.35rem, 2.2vw, 1.75rem);
  line-height: 1.25;
  font-weight: 600;
  color: #E8E8F0;
  letter-spacing: -0.01em;
}

.showcase-sub {
  margin: 0;
  color: #8B8B9F;
  font-size: clamp(0.88rem, 1.2vw, 0.95rem);
  line-height: 1.65;
}

/* ── Stat grid ──────────────────────────────────────────────────────────── */
.stat-block {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.625rem;
}

.stat-tile {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.875rem 0.9rem;
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.04);
  background: rgba(255, 255, 255, 0.02);
}

.stat-value-row {
  display: inline-flex;
  align-items: baseline;
  gap: 0.3rem;
}

.stat-value {
  font-size: clamp(1.5rem, 2.5vw, 1.875rem);
  font-weight: 700;
  line-height: 1;
  color: #E8E8F0;
}

.stat-unit {
  font-size: 0.8rem;
  font-weight: 600;
  color: #5A5A6E;
}

.stat-tile-label {
  font-size: 0.76rem;
  line-height: 1.4;
  color: #5A5A6E;
}

.figure-note {
  margin: 0;
  font-size: 0.7rem;
  color: #3A3A4A;
}

/* ── Feature list ───────────────────────────────────────────────────────── */
.feature-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 0.625rem;
  font-size: 0.88rem;
  line-height: 1.5;
  color: #C0C0D4;
}

.check {
  flex: none;
  display: grid;
  place-items: center;
  width: 1.25rem;
  height: 1.25rem;
  margin-top: 0.1rem;
  border-radius: 0.375rem;
  font-size: 0.72rem;
  font-weight: 700;
  color: #22D3EE;
  background: rgba(34, 211, 238, 0.08);
  border: 1px solid rgba(34, 211, 238, 0.15);
}

/* ── Mini leaderboard ───────────────────────────────────────────────────── */
.mini-preview {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
  padding: 0.875rem 1rem;
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.04);
  background: rgba(255, 255, 255, 0.02);
}

.mini-preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.mini-preview-title {
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #5A5A6E;
}

.streak-ring {
  position: relative;
  flex: none;
  display: grid;
  place-items: center;
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 999px;
  background: conic-gradient(#22D3EE 0% 80%, rgba(255, 255, 255, 0.06) 80% 100%);
}

.streak-ring::before {
  content: '';
  position: absolute;
  width: 2.1rem;
  height: 2.1rem;
  border-radius: 999px;
  background: #0E0E16;
}

.streak-ring-text {
  position: relative;
  font-size: 0.76rem;
  font-weight: 700;
  color: #E8E8F0;
}

.lb-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.lb-row {
  padding: 0.5rem 0.625rem;
  border-radius: 0.5rem;
  font-size: 0.82rem;
  font-variant-numeric: tabular-nums;
  color: #8B8B9F;
  background: rgba(255, 255, 255, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.03);
}

.lb-row:first-child {
  border-color: rgba(34, 211, 238, 0.12);
  color: #C0C0D4;
}

/* ── Status rail ────────────────────────────────────────────────────────── */
.status-rail {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.35rem 0.65rem;
  border-radius: 999px;
  font-size: 0.76rem;
  color: #8B8B9F;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.status-dot {
  width: 0.45rem;
  height: 0.45rem;
  border-radius: 999px;
}

.status-chip:nth-child(1) .status-dot { background: #10B981; }
.status-chip:nth-child(2) .status-dot { background: #22D3EE; }
.status-chip:nth-child(3) .status-dot { background: #7C3AED; }

/* ── Responsive ─────────────────────────────────────────────────────────── */
@media (max-width: 640px) {
  .showcase {
    padding: 1.25rem 1rem;
    border-radius: 1rem;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }

  .mini-preview-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
