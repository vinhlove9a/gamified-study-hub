<script setup lang="ts">
import { inject, nextTick, onMounted, onUnmounted } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import FlowParticles from '@/components/ui/FlowParticles.vue';

type GsapType = typeof import('gsap').gsap;

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

interface Medal {
  icon: string;
  title: string;
  desc: string;
  glow: string;
}

const medals: Medal[] = [
  { icon: '🏅', title: 'Người mở đường', desc: 'Tải tài liệu đầu tiên', glow: 'pulse-glow-cyan' },
  { icon: '🔥', title: 'Streak 7 ngày', desc: 'Học đều một tuần liền', glow: 'pulse-glow-magenta' },
  { icon: '💎', title: 'Thợ săn tài liệu', desc: 'Sắp xếp 100 tài liệu', glow: 'pulse-glow-green' },
  { icon: '🎯', title: 'Bậc thầy nhiệm vụ', desc: 'Hoàn thành 50 nhiệm vụ', glow: 'pulse-glow-cyan' },
  { icon: '👑', title: 'Thành viên VIP', desc: 'Mở khóa toàn bộ tính năng', glow: 'pulse-glow-magenta' },
  { icon: '🏆', title: 'Quán quân tuần', desc: 'Đứng đầu leaderboard CLB', glow: 'pulse-glow-green' }
];

const onTilt = (e: PointerEvent) => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
  const el = e.currentTarget as HTMLElement;
  if (!el) return;
  const rect = el.getBoundingClientRect();
  const relX = (e.clientX - rect.left) / rect.width;
  const relY = (e.clientY - rect.top) / rect.height;
  const max = 10;
  const rotateY = (relX - 0.5) * 2 * max;
  const rotateX = -((relY - 0.5) * 2 * max);
  el.style.transform = `perspective(700px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg)`;
};

const onReset = (e: PointerEvent) => {
  const el = e.currentTarget as HTMLElement;
  if (!el) return;
  el.style.transform = '';
};

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.medal-card'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.countUp?.('.ach-doc', { end: 1000, duration: 2 }));
    registerCleanup(animation?.countUp?.('.ach-task', { end: 250, duration: 2 }));
    registerCleanup(animation?.countUp?.('.ach-streak', { end: 30, duration: 2 }));

    registerCleanup(animation?.batchReveal?.('.medal-card', {
      trigger: '#achievements',
      stagger: 0.08,
      y: 24
    }));
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
  <section id="achievements" class="ach-section" aria-labelledby="ach-title">
    <FlowParticles
      :count="6"
      color="#9b5cff"
      speed="slow"
      direction="right"
      size="small"
      :opacity="0.18"
      aria-hidden="true"
    />

    <div class="ach-glow ach-glow--one" aria-hidden="true"></div>
    <div class="ach-glow ach-glow--two" aria-hidden="true"></div>

    <div class="ach-shell">
      <header class="ach-intro">
        <span class="cosmic-badge">Achievements</span>
        <h2 id="ach-title" class="cosmic-gradient-text">Mỗi nỗ lực đều được ghi nhận</h2>
        <p class="ach-sub">
          Hệ thống huy hiệu, streak và VIP biến việc học thành hành trình đáng tự hào.
        </p>
      </header>

      <div class="ach-stats" role="group" aria-label="Chỉ số minh hoạ">
        <article class="cosmic-glass ach-stat">
          <strong class="ach-stat-value">
            <span class="ach-doc">1000</span>
          </strong>
          <span class="ach-stat-label">tài liệu được sắp xếp</span>
        </article>

        <article class="cosmic-glass ach-stat">
          <strong class="ach-stat-value">
            <span class="ach-task">250</span>
          </strong>
          <span class="ach-stat-label">nhiệm vụ mẫu hoàn thành</span>
        </article>

        <article class="cosmic-glass ach-stat">
          <strong class="ach-stat-value">
            <span class="ach-streak">30</span>
            <span class="stat-unit">ngày</span>
          </strong>
          <span class="ach-stat-label">streak dài nhất</span>
        </article>
      </div>

      <p class="ach-footnote">Số liệu minh hoạ cho mục tiêu sản phẩm.</p>

      <div class="medal-grid">
        <article
          v-for="medal in medals"
          :key="medal.title"
          class="cosmic-glass medal-card"
          @pointermove="onTilt"
          @pointerleave="onReset"
        >
          <span class="medal-icon" :class="medal.glow" aria-hidden="true">{{ medal.icon }}</span>
          <div class="medal-copy">
            <h3 class="medal-title">{{ medal.title }}</h3>
            <p class="medal-desc">{{ medal.desc }}</p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.ach-section {
  position: relative;
  overflow: hidden;
  padding: clamp(3rem, 7vw, 6rem) 1.5rem;
  color: oklch(0.96 0.01 240);
  background:
    radial-gradient(ellipse at 18% 12%, oklch(0.68 0.22 300 / 14%), transparent 52%),
    radial-gradient(ellipse at 82% 88%, oklch(0.82 0.16 195 / 12%), transparent 55%),
    oklch(0.07 0.02 280 / 64%);
  isolation: isolate;
}

.ach-glow {
  position: absolute;
  z-index: 0;
  border-radius: 999px;
  filter: blur(90px);
  pointer-events: none;
}

.ach-glow--one {
  top: -120px;
  left: -100px;
  width: 420px;
  height: 420px;
  background: oklch(0.68 0.22 300 / 18%);
}

.ach-glow--two {
  bottom: -160px;
  right: -120px;
  width: 480px;
  height: 480px;
  background: oklch(0.82 0.16 195 / 14%);
}

.ach-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  margin: 0 auto;
}

.ach-intro {
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
}

/* scoped pill: spec requires we define cosmic-badge ourselves */
.cosmic-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.7rem;
  font-size: 0.66rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: oklch(0.82 0.16 195);
  border: 1px solid oklch(0.78 0.18 195 / 35%);
  border-radius: 999px;
  background: oklch(0.82 0.16 195 / 10%);
}

.ach-intro h2 {
  margin: 1rem 0 0;
  font-size: clamp(2rem, 4.6vw, 3.4rem);
  line-height: 1.08;
  letter-spacing: -0.01em;
}

.ach-sub {
  margin: 1rem auto 0;
  max-width: 560px;
  color: oklch(0.7 0.03 240);
  font-size: clamp(1rem, 1.5vw, 1.15rem);
  line-height: 1.7;
}

.ach-stats {
  margin: clamp(2.5rem, 5vw, 3.5rem) auto 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(1rem, 2.5vw, 1.5rem);
}

.ach-stat {
  padding: clamp(1.5rem, 3vw, 2rem) 1.25rem;
  text-align: center;
  border-radius: 16px;
}

.ach-stat-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 0.4rem;
  font-size: clamp(2.4rem, 5vw, 3.6rem);
  line-height: 1;
  font-weight: 800;
  color: oklch(0.96 0.01 240);
}

.ach-stat-value .stat-unit {
  font-size: 0.32em;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: oklch(0.84 0.16 85);
}

.ach-stat-label {
  display: block;
  margin-top: 0.75rem;
  color: oklch(0.7 0.03 240);
  font-size: 0.92rem;
  line-height: 1.45;
}

.ach-footnote {
  margin: 1.25rem 0 0;
  text-align: center;
  color: oklch(0.7 0.03 240 / 70%);
  font-size: 0.8rem;
  font-style: italic;
}

.medal-grid {
  margin: clamp(2.5rem, 5vw, 3.5rem) auto 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(1rem, 2.5vw, 1.5rem);
}

.medal-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: clamp(1.25rem, 2.5vw, 1.6rem);
  border-radius: 16px;
  transform-style: preserve-3d;
  transition: transform 0.18s ease, box-shadow 0.25s ease, border-color 0.25s ease;
  will-change: transform;
}

.medal-card:hover {
  box-shadow: 0 24px 60px oklch(0 0 0 / 38%);
}

.medal-icon {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 60px;
  height: 60px;
  font-size: 1.8rem;
  border-radius: 999px;
  border: 1px solid oklch(0.96 0.01 240 / 12%);
  background: oklch(0.13 0.025 280);
}

/* ring tint pairs with the per-card pulse-glow utility bound in template */
.medal-card:nth-child(3n + 1) .medal-icon {
  border-color: oklch(0.82 0.16 195 / 40%);
}

.medal-card:nth-child(3n + 2) .medal-icon {
  border-color: oklch(0.68 0.22 330 / 40%);
}

.medal-card:nth-child(3n + 3) .medal-icon {
  border-color: oklch(0.78 0.16 150 / 40%);
}

.medal-copy {
  min-width: 0;
}

.medal-title {
  margin: 0;
  font-size: 1.08rem;
  font-weight: 700;
  color: oklch(0.96 0.01 240);
}

.medal-desc {
  margin: 0.35rem 0 0;
  color: oklch(0.7 0.03 240);
  font-size: 0.9rem;
  line-height: 1.5;
}

@media (max-width: 640px) {
  .ach-section {
    padding: clamp(3rem, 10vw, 4rem) 1rem;
  }

  .ach-stats,
  .medal-grid {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .medal-card {
    transition: none;
  }
}
</style>
