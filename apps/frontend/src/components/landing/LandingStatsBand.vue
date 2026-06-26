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

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.band-metric'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.countUp?.('.band-clb', { end: 100, duration: 1.6, suffix: '+' }));
    registerCleanup(animation?.countUp?.('.band-doc', { end: 1280, duration: 1.9, suffix: '+' }));
    registerCleanup(animation?.countUp?.('.band-coin', { end: 50, duration: 1.5, suffix: 'K' }));
    registerCleanup(animation?.countUp?.('.band-task', { end: 9450, duration: 2.0 }));

    registerCleanup(animation?.batchReveal?.('.band-metric', {
      trigger: '#stats-band',
      duration: 0.6,
      stagger: 0.1,
      y: 20
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
  <section id="stats-band" class="stats-band" aria-labelledby="stats-band-title">
    <div class="band-effects" aria-hidden="true">
      <FlowParticles
        :count="8"
        color="#22e0d6"
        speed="slow"
        direction="right"
        size="small"
        :opacity="0.25"
        aria-hidden="true"
      />
      <span class="band-glow band-glow--cyan"></span>
      <span class="band-glow band-glow--violet"></span>
    </div>

    <div class="band-shell">
      <header class="band-header">
        <span class="cosmic-badge">Signal</span>
        <h2 id="stats-band-title" class="band-eyebrow">
          Được xây cho cộng đồng học tập
        </h2>
      </header>

      <div class="band-grid" role="group" aria-label="Chỉ số sản phẩm">
        <article class="band-metric">
          <strong class="band-value cosmic-gradient-text">
            <span class="band-clb">0</span>
          </strong>
          <span class="band-label">CLB &amp; nhóm học</span>
        </article>

        <article class="band-metric">
          <strong class="band-value cosmic-gradient-text">
            <span class="band-doc">0</span>
          </strong>
          <span class="band-label">tài liệu được tổ chức</span>
        </article>

        <article class="band-metric">
          <strong class="band-value cosmic-gradient-text">
            <span class="band-coin">0</span>
          </strong>
          <span class="band-label">coin trao thưởng</span>
        </article>

        <article class="band-metric">
          <strong class="band-value cosmic-gradient-text">
            <span class="band-task">0</span>
          </strong>
          <span class="band-label">nhiệm vụ hoàn thành</span>
        </article>
      </div>

      <p class="figure-note">Số liệu minh hoạ cho mục tiêu sản phẩm.</p>
    </div>
  </section>
</template>

<style scoped>
.stats-band {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  padding: clamp(3rem, 7vw, 6rem) 1.5rem;
  color: oklch(0.96 0.01 240);
  background:
    linear-gradient(135deg, oklch(0.09 0.025 280 / 64%), oklch(0.11 0.03 270 / 60%)),
    transparent;
}

.stats-band::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  background-image:
    linear-gradient(oklch(0.96 0.01 240 / 3%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.96 0.01 240 / 3%) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: linear-gradient(to bottom, transparent, black 16%, black 84%, transparent);
}

.band-effects {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.band-glow {
  position: absolute;
  width: clamp(280px, 36vw, 520px);
  height: clamp(280px, 36vw, 520px);
  border-radius: 999px;
  filter: blur(80px);
  opacity: 0.5;
}

.band-glow--cyan {
  top: -180px;
  left: -120px;
  background: radial-gradient(circle, oklch(0.82 0.16 195 / 32%), transparent 70%);
}

.band-glow--violet {
  bottom: -200px;
  right: -120px;
  background: radial-gradient(circle, oklch(0.68 0.22 300 / 28%), transparent 70%);
}

.band-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: clamp(1.75rem, 4vw, 2.75rem);
  text-align: center;
}

.band-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.85rem;
}

.cosmic-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.7rem;
  border: 1px solid oklch(0.78 0.18 195 / 35%);
  border-radius: 999px;
  background: oklch(0.82 0.16 195 / 10%);
  color: oklch(0.82 0.16 195);
  font-size: 0.66rem;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.band-eyebrow {
  margin: 0;
  font-size: clamp(0.95rem, 2.2vw, 1.25rem);
  font-weight: 700;
  letter-spacing: 0.02em;
  color: oklch(0.7 0.03 240);
}

.band-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: clamp(1rem, 3vw, 2.25rem);
  width: 100%;
}

.band-metric {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.6rem;
  padding: clamp(1.1rem, 2.2vw, 1.6rem) 0.75rem;
  border-radius: 14px;
  border: 1px solid oklch(0.96 0.01 240 / 7%);
  background: linear-gradient(160deg, oklch(0.15 0.03 280 / 55%), oklch(0.1 0.024 280 / 45%));
  box-shadow: 0 18px 50px oklch(0 0 0 / 28%);
}

.band-metric:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 22%;
  right: calc(clamp(1rem, 3vw, 2.25rem) / -2);
  bottom: 22%;
  width: 1px;
  background: linear-gradient(to bottom, transparent, oklch(0.82 0.16 195 / 26%), transparent);
}

.band-value {
  font-size: clamp(2.2rem, 6vw, 3.6rem);
  font-weight: 800;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.band-label {
  font-size: clamp(0.82rem, 1.4vw, 0.95rem);
  font-weight: 600;
  line-height: 1.4;
  color: oklch(0.7 0.03 240);
}

.figure-note {
  margin: 0;
  font-size: 0.78rem;
  color: oklch(0.62 0.025 240);
  letter-spacing: 0.01em;
}

@media (max-width: 640px) {
  .stats-band {
    padding: 3rem 1rem;
  }

  .band-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .band-metric::after {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .band-glow {
    animation: none;
  }
}
</style>
