<script setup lang="ts">
import { inject, nextTick, onMounted, onUnmounted } from 'vue';
import { RouterLink } from 'vue-router';
import FlowParticles from '@/components/ui/FlowParticles.vue';
import type { AnimationAPI } from '@/types/gsap';

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
const scrollToSection = inject<(href: string) => void>('scrollToSection', () => {});

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
    gsap.set(['.final-headline', '.final-cta-primary'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.revealUp?.('.final-headline', { yStart: 30 }));
    registerCleanup(animation?.magneticButton?.('.final-cta-primary', { strength: 25 }));
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn && fn());
  cleanupFns = [];
});
</script>

<template>
  <section id="get-started" class="final-cta-section" aria-labelledby="final-cta-title">
    <div class="final-cta-decor" aria-hidden="true">
      <div class="final-aurora"></div>
      <FlowParticles
        :count="8"
        color="#22d3ee"
        speed="medium"
        :opacity="0.22"
      />
    </div>

    <div class="final-cta-shell">
      <span class="cosmic-badge">Sẵn sàng bắt đầu?</span>

      <h2 id="final-cta-title" class="final-headline cosmic-gradient-text glow-cyan">
        Biến kho tài liệu của bạn thành một trò chơi học tập.
      </h2>

      <p class="final-sub">
        Tạo workspace đầu tiên hôm nay. Giao nhiệm vụ, thưởng coin, giữ streak và xem cộng đồng
        của bạn cùng nhau tiến bộ.
      </p>

      <div class="final-cta-row">
        <RouterLink to="/auth/register" class="final-cta-primary cosmic-btn">
          Tạo tài khoản miễn phí
        </RouterLink>
        <button type="button" class="final-cta-ghost" @click="scrollToSection('#how-it-works')">
          Xem cách hoạt động
        </button>
      </div>

      <p class="cta-reassure">Không cần thẻ tín dụng · Bắt đầu trong vài phút</p>
    </div>
  </section>
</template>

<style scoped>
.final-cta-section {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  padding: clamp(3rem, 7vw, 6rem) 1.5rem;
  color: oklch(0.96 0.01 240);
}

.final-cta-decor {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.final-aurora {
  position: absolute;
  top: 50%;
  left: 50%;
  width: min(120vw, 900px);
  aspect-ratio: 1;
  transform: translate(-50%, -50%);
  border-radius: 999px;
  mix-blend-mode: screen;
  background: radial-gradient(
    circle at 50% 50%,
    oklch(0.82 0.16 195 / 24%) 0%,
    oklch(0.68 0.22 300 / 18%) 42%,
    transparent 70%
  );
  filter: blur(28px);
  animation: finalAuroraFloat 14s ease-in-out infinite;
}

.final-cta-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.cosmic-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.7rem;
  border: 1px solid oklch(0.78 0.18 195 / 35%);
  border-radius: 999px;
  background: oklch(0.82 0.16 195 / 10%);
  color: oklch(0.86 0.1 195);
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.final-headline {
  margin: 1.4rem 0 0;
  max-width: 18ch;
  font-size: clamp(2.2rem, 5.5vw, 4.5rem);
  font-weight: 700;
  line-height: 1.05;
  letter-spacing: -0.02em;
}

.final-sub {
  margin: 1.4rem 0 0;
  max-width: 60ch;
  color: oklch(0.7 0.03 240);
  font-size: clamp(1rem, 1.5vw, 1.18rem);
  line-height: 1.75;
}

.final-cta-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 2.25rem;
}

.final-cta-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}

.final-cta-ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.85rem 1.6rem;
  border: 1px solid oklch(0.78 0.18 195 / 30%);
  border-radius: 8px;
  background: oklch(0.13 0.025 280 / 60%);
  color: oklch(0.96 0.01 240);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: border-color 0.25s ease, background 0.25s ease, box-shadow 0.25s ease, transform 0.25s ease;
}

.final-cta-ghost:hover {
  border-color: oklch(0.82 0.16 195 / 60%);
  background: oklch(0.16 0.035 280 / 72%);
  box-shadow: 0 0 28px oklch(0.82 0.16 195 / 22%);
  transform: translateY(-2px);
}

.final-cta-ghost:focus-visible {
  outline: 3px solid oklch(0.82 0.16 195);
  outline-offset: 3px;
}

.cta-reassure {
  margin: 1.5rem 0 0;
  color: oklch(0.7 0.03 240 / 80%);
  font-size: 0.85rem;
  letter-spacing: 0.02em;
}

@keyframes finalAuroraFloat {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.85;
  }
  50% {
    transform: translate(-48%, -54%) scale(1.08);
    opacity: 1;
  }
}

@media (max-width: 640px) {
  .final-cta-row {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
    max-width: 360px;
  }

  .final-cta-primary,
  .final-cta-ghost {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .final-aurora {
    animation: none;
  }
}
</style>
