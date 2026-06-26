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

interface Step {
  num: string;
  accent: string;
  title: string;
  detail: string;
}

const steps: Step[] = [
  {
    num: '1',
    accent: '#22e0d6',
    title: 'Tải tài liệu lên Vault',
    detail: 'Kéo thả PDF, slide, đề thi. Phân loại và gắn thẻ gọn gàng.'
  },
  {
    num: '2',
    accent: '#9b5cff',
    title: 'Giao thành nhiệm vụ',
    detail: 'Đặt hạn, độ khó và phần thưởng coin cho mỗi nhiệm vụ.'
  },
  {
    num: '3',
    accent: '#e879f9',
    title: 'Học và tích coin',
    detail: 'Hoàn thành nhiệm vụ, giữ streak mỗi ngày để nhận thưởng.'
  },
  {
    num: '4',
    accent: '#f8d66d',
    title: 'Mở khóa & leo hạng',
    detail: 'Đổi coin lấy VIP, huy hiệu và vị trí trên leaderboard.'
  }
];

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.step-card'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.batchReveal?.('.step-card', {
      trigger: '#how-it-works',
      duration: 0.6,
      stagger: 0.08,
      y: 24
    }));

    document.querySelectorAll('.step-card').forEach((card) => {
      registerCleanup(animation?.hoverScale?.(card as HTMLElement, { scale: 1.03, duration: 0.25 }));
    });
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
  <section id="how-it-works" class="hiw-section" aria-labelledby="how-it-works-title">
    <div class="hiw-effects" aria-hidden="true">
      <span class="hiw-glow hiw-glow--one"></span>
      <span class="hiw-glow hiw-glow--two"></span>
      <FlowParticles
        :count="6"
        color="#22e0d6"
        speed="slow"
        direction="right"
        size="small"
        :opacity="0.15"
        aria-hidden="true"
      />
    </div>

    <div class="hiw-shell">
      <header class="hiw-intro">
        <h2 id="how-it-works-title" class="cosmic-gradient-text">
          Từ tài liệu lộn xộn đến cộng đồng học tập có động lực
        </h2>
        <p>Bốn bước biến kho tài liệu của bạn thành một trò chơi học tập.</p>
      </header>

      <div class="hiw-core" aria-hidden="true">
        <div class="orbit-ring rotate-slow"></div>
        <div class="hiw-icon glow-cyan">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" aria-hidden="true">
            <path
              d="M3 7.5 12 3l9 4.5-9 4.5-9-4.5Z"
              stroke-linejoin="round"
            />
            <path
              d="M3 12l9 4.5 9-4.5M3 16.5 12 21l9-4.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>
      </div>

      <ol class="hiw-grid" aria-label="Bốn bước sử dụng nền tảng">
        <li
          v-for="step in steps"
          :key="step.num"
          class="step-card cosmic-glass"
          :style="{ '--accent': step.accent }"
        >
          <span class="step-connector" aria-hidden="true"></span>
          <span class="step-badge">
            <span class="step-badge-label">Bước</span>
            {{ step.num }}
          </span>
          <h3 class="step-title">{{ step.title }}</h3>
          <p class="step-detail">{{ step.detail }}</p>
        </li>
      </ol>
    </div>
  </section>
</template>

<style scoped>
.hiw-section {
  position: relative;
  overflow: hidden;
  padding: clamp(3rem, 7vw, 6rem) 1.5rem;
  color: oklch(0.96 0.01 240);
  background:
    radial-gradient(ellipse at 50% -10%, oklch(0.14 0.04 280 / 58%), transparent 60%),
    oklch(0.07 0.02 280 / 64%);
  isolation: isolate;
}

.hiw-section::before {
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

.hiw-effects {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.hiw-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(80px);
  opacity: 0.5;
}

.hiw-glow--one {
  top: -120px;
  left: -80px;
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, oklch(0.82 0.16 195 / 28%), transparent 70%);
}

.hiw-glow--two {
  bottom: -160px;
  right: -100px;
  width: 480px;
  height: 480px;
  background: radial-gradient(circle, oklch(0.68 0.22 300 / 24%), transparent 70%);
}

.hiw-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  margin: 0 auto;
}

.hiw-intro {
  text-align: center;
  max-width: 760px;
  margin: 0 auto;
}

.hiw-intro h2 {
  margin: 0;
  font-size: clamp(2rem, 4.4vw, 3.4rem);
  line-height: 1.08;
  letter-spacing: -0.01em;
}

.hiw-intro p {
  margin: 1rem auto 0;
  max-width: 560px;
  color: oklch(0.7 0.03 240);
  font-size: clamp(1rem, 1.5vw, 1.18rem);
  line-height: 1.7;
}

/* Central decorative core */
.hiw-core {
  position: relative;
  display: grid;
  place-items: center;
  width: clamp(120px, 18vw, 180px);
  height: clamp(120px, 18vw, 180px);
  margin: clamp(2.5rem, 5vw, 3.75rem) auto clamp(2rem, 4vw, 3rem);
}

.orbit-ring {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  border: 2px dashed oklch(0.82 0.16 195 / 45%);
  box-shadow:
    0 0 32px oklch(0.82 0.16 195 / 28%),
    inset 0 0 24px oklch(0.82 0.16 195 / 18%);
}

.orbit-ring::before {
  content: '';
  position: absolute;
  inset: 14%;
  border-radius: 999px;
  border: 1px solid oklch(0.68 0.22 300 / 35%);
}

.hiw-icon {
  position: relative;
  display: grid;
  place-items: center;
  width: 56%;
  height: 56%;
  border-radius: 999px;
  color: oklch(0.96 0.01 240);
  background:
    linear-gradient(145deg, oklch(0.82 0.16 195 / 90%), oklch(0.68 0.22 300 / 90%));
  box-shadow:
    0 18px 50px oklch(0 0 0 / 40%),
    inset 0 0 0 1px oklch(0.96 0.01 240 / 18%);
}

.hiw-icon svg {
  width: 52%;
  height: 52%;
}

/* Steps grid */
.hiw-grid {
  position: relative;
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: clamp(1rem, 2.2vw, 1.5rem);
}

.step-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.5rem 1.4rem;
  border-radius: 14px;
  border-top: 2px solid color-mix(in oklch, var(--accent), transparent 55%);
  overflow: hidden;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

.step-card::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(circle at 50% 0%, color-mix(in oklch, var(--accent), transparent 84%), transparent 60%);
}

.step-card:hover,
.step-card:focus-within {
  border-color: color-mix(in oklch, var(--accent), white 12%);
  box-shadow: 0 0 36px color-mix(in oklch, var(--accent), transparent 78%);
}

.step-badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: baseline;
  gap: 0.45rem;
  align-self: flex-start;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  font-size: 0.95rem;
  font-weight: 800;
  letter-spacing: 0.04em;
  color: color-mix(in oklch, var(--accent), white 16%);
  background: color-mix(in oklch, var(--accent), transparent 88%);
  border: 1px solid color-mix(in oklch, var(--accent), transparent 60%);
}

.step-badge-label {
  font-size: 0.6rem;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: oklch(0.7 0.03 240);
}

.step-title {
  position: relative;
  z-index: 1;
  margin: 0;
  font-size: clamp(1.05rem, 1.6vw, 1.25rem);
  line-height: 1.3;
  color: oklch(0.96 0.01 240);
}

.step-detail {
  position: relative;
  z-index: 1;
  margin: 0;
  color: oklch(0.7 0.03 240);
  font-size: 0.95rem;
  line-height: 1.6;
}

/* Connector line: hidden on desktop, becomes the timeline rail on mobile */
.step-connector {
  display: none;
}

@media (max-width: 1024px) {
  .hiw-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .hiw-section {
    padding: 3rem 1rem;
  }

  .hiw-core {
    width: 100px;
    height: 100px;
    margin: 2rem auto 1.75rem;
  }

  /* Vertical timeline with a static gradient connector rail */
  .hiw-grid {
    grid-template-columns: 1fr;
    gap: 0;
    padding-left: 2.25rem;
  }

  .hiw-grid::before {
    content: '';
    position: absolute;
    top: 0.5rem;
    bottom: 0.5rem;
    left: 0.75rem;
    width: 2px;
    border-radius: 999px;
    background: linear-gradient(
      to bottom,
      oklch(0.82 0.16 195 / 70%),
      oklch(0.68 0.22 300 / 70%),
      oklch(0.68 0.22 330 / 70%),
      oklch(0.84 0.16 85 / 70%)
    );
  }

  .step-card {
    margin-bottom: 1rem;
  }

  .step-card:last-child {
    margin-bottom: 0;
  }

  .step-connector {
    display: block;
    position: absolute;
    top: 1.85rem;
    left: -1.5rem;
    width: 0.75rem;
    height: 0.75rem;
    border-radius: 999px;
    background: var(--accent);
    box-shadow: 0 0 12px var(--accent);
  }
}

@media (prefers-reduced-motion: reduce) {
  .orbit-ring {
    animation: none;
  }
}
</style>
