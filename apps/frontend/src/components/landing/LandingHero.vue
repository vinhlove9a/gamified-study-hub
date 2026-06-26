<script setup lang="ts">
import { onMounted, onUnmounted, ref, inject } from 'vue';
import { useRouter } from 'vue-router';
import type { AnimationAPI } from '@/types/gsap';
import FlowParticles from '@/components/ui/FlowParticles.vue';
import LandingHeroMorph from '@/components/landing/LandingHeroMorph.vue';
import { useKeyboardNavigation } from '@/composables/useKeyboardNavigation';

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
const router = useRouter();
const { handleEnterOrSpace } = useKeyboardNavigation();

const ctaButtonRef = ref<HTMLButtonElement | null>(null);
let bgParallaxTween: ReturnType<GsapType['to']> | null = null;
let cleanupFns: Array<() => void> = [];

const focusMetrics = [
  { label: 'Sprint', value: '50:10', detail: 'deep focus' },
  { label: 'Noise', value: '-42db', detail: 'calm layer' },
  { label: 'Flow', value: '87%', detail: 'steady loop' }
];

const sessionBars = [68, 42, 88, 56, 74, 36, 92, 62, 80, 48, 70, 54];

const goToLoginPage = () => {
  router.push('/auth/login');
};

const onImageLoad = () => {
  if (!gsap) return;
  bgParallaxTween?.scrollTrigger?.kill();
  bgParallaxTween?.kill();

  bgParallaxTween = gsap.to('.hero-bg__image', {
    scrollTrigger: {
      trigger: '.hero-section',
      start: 'top top',
      end: 'bottom top',
      scrub: true
    },
    yPercent: 8,
    ease: 'none'
  });
};

const onImageError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  img.style.display = 'none';
};

onMounted(() => {
  if (!gsap) return;

  if (ctaButtonRef.value) {
    const cleanupKeyboard = handleEnterOrSpace(ctaButtonRef.value, goToLoginPage);
    if (cleanupKeyboard) cleanupFns.push(cleanupKeyboard);
  }

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.hero-bg', '.hero-overlay', '.hero-content', '.hero-cta-group', '.scroll-indicator'], { opacity: 1 });
    return;
  }

  const isCompactViewport = window.matchMedia('(max-width: 768px)').matches;

  if (isCompactViewport) {
    gsap.set(['.hero-bg', '.hero-overlay', '.hero-content', '.hero-cta-group'], { opacity: 1, y: 0 });
    gsap.set('.scroll-indicator', { opacity: 0 });
    gsap.from('.hero-line', {
      scaleX: 0,
      duration: 0.45,
      ease: 'power3.out'
    });
  } else {
  const tl = gsap.timeline();

  tl.set('.hero-bg', { opacity: 0 });
  tl.set('.hero-overlay', { opacity: 0 });
  tl.set('.hero-content', { opacity: 0, y: 40 });
  tl.set('.hero-cta-group', { opacity: 0, y: 20 });
  tl.set('.scroll-indicator', { opacity: 0 });

  tl.to('.hero-bg', {
    opacity: 1,
    duration: 1.2,
    ease: 'power2.out'
  })
    .to('.hero-overlay', {
      opacity: 1,
      duration: 0.8,
      ease: 'power2.out'
    }, '-=0.6');

  tl.from('.hero-line', {
    scaleX: 0,
    duration: 0.8,
    ease: 'power3.out'
  }, '-=0.4');

  tl.to('.hero-content', {
    opacity: 1,
    y: 0,
    duration: 1,
    ease: 'power3.out'
  }, '-=0.3');

  tl.to('.hero-cta-group', {
    opacity: 1,
    y: 0,
    duration: 0.6,
    ease: 'power2.out'
  }, '-=0.5');

  tl.to('.scroll-indicator', {
    opacity: 1,
    duration: 0.5,
    ease: 'power2.out'
  }, '-=0.2');
  }

  const cleanupHover = animation?.hoverScale?.('.hero-cta', { scale: 1.03, duration: 0.3 });
  const cleanupMagnetic = animation?.magneticButton?.('.hero-cta', { strength: 20, duration: 0.25 });

  if (cleanupHover) cleanupFns.push(cleanupHover);
  if (cleanupMagnetic) cleanupFns.push(cleanupMagnetic);

  gsap.to('.floating-particle', {
    y: -40,
    rotation: 360,
    duration: 12,
    repeat: -1,
    yoyo: true,
    ease: 'sine.inOut',
    stagger: {
      each: 1.5,
      from: 'random'
    }
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn && typeof fn === 'function' && fn());
  bgParallaxTween?.scrollTrigger?.kill();
  bgParallaxTween?.kill();
  bgParallaxTween = null;
});
</script>

<template>
  <section id="hero" class="hero-section panel">
    <div class="hero-bg">
      <img
        src="https://cdn.ilearn.vn/assets/landing/hero-bg.webp"
        alt="Không gian học tập số với nhiệm vụ, điểm thưởng và tài liệu VIP"
        class="hero-bg__image"
        @load="onImageLoad"
        @error="onImageError"
      />
    </div>

    <div class="hero-overlay">
      <div class="overlay-noise"></div>
      <div class="overlay-gradient"></div>
      <div class="overlay-vignette"></div>
      <div class="hero-deepwork-grid" aria-hidden="true"></div>
      <div class="hero-beam hero-beam--one" aria-hidden="true"></div>
      <div class="hero-beam hero-beam--two" aria-hidden="true"></div>
      <div class="hero-orbit-field" aria-hidden="true">
        <span></span>
        <span></span>
        <span></span>
      </div>
      <div aria-hidden="true">
        <FlowParticles
          :count="6"
          color="#22e0d6"
          speed="medium"
          direction="right"
          size="small"
          :opacity="0.3"
        />
      </div>
      <div class="floating-particles" aria-hidden="true">
        <div class="particle particle-1 floating-particle"></div>
        <div class="particle particle-2 floating-particle"></div>
        <div class="particle particle-3 floating-particle"></div>
        <div class="particle particle-4 floating-particle"></div>
      </div>
    </div>

    <LandingHeroMorph class="hero-morph-layer" />

    <aside class="hero-cockpit" aria-label="Trạng thái deep work">
      <div class="cockpit-header">
        <span class="cockpit-live-dot"></span>
        <span>Focus session</span>
      </div>

      <div class="cockpit-clock">
        <span>50</span>
        <small>min</small>
      </div>

      <div class="cockpit-bars" aria-hidden="true">
        <span
          v-for="(level, index) in sessionBars"
          :key="index"
          :style="{ '--bar-level': `${level}%`, '--bar-delay': `${index * -0.16}s` }"
        ></span>
      </div>

      <div class="cockpit-metrics">
        <div v-for="metric in focusMetrics" :key="metric.label" class="cockpit-metric">
          <span>{{ metric.label }}</span>
          <strong>{{ metric.value }}</strong>
          <small>{{ metric.detail }}</small>
        </div>
      </div>
    </aside>

    <div class="hero-content">
      <div class="content-wrapper">
        <div class="hero-line"></div>

        <span class="hero-badge">
          <span class="badge-dot"></span>
          Deep Workspace cho cộng đồng học tập
        </span>

        <h1 class="hero-headline">
          Điều phối kho tài liệu
          <span class="headline-accent cosmic-gradient-text">như một trạm vũ trụ học tập.</span>
        </h1>

        <p class="hero-tagline">
          Gamified Study Hub gom tài liệu, nhiệm vụ, coin, streak và mở khóa VIP vào một cockpit duy nhất,
          giúp CLB, gia sư và nhóm luyện thi vận hành cộng đồng học tập có nhịp quay lại rõ ràng.
        </p>

        <div class="hero-cta-group">
          <button
            ref="ctaButtonRef"
            class="hero-cta" 
            aria-label="Vào Workspace"
            @click="goToLoginPage"
          >
            <span>Vào Workspace</span>
            <svg class="cta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </button>

          <div class="hero-stats-mini">
            <div class="mini-stat">
              <span class="mini-value">Vault</span>
              <span class="mini-label">tài liệu có cấu trúc</span>
            </div>
            <div class="mini-divider"></div>
            <div class="mini-stat">
              <span class="mini-value">Orbit</span>
              <span class="mini-label">nhiệm vụ & streak</span>
            </div>
            <div class="mini-divider"></div>
            <div class="mini-stat">
              <span class="mini-value">Coin</span>
              <span class="mini-label">mở khóa VIP</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="scroll-indicator" aria-hidden="true">
      <div class="scroll-line">
        <div class="scroll-progress"></div>
      </div>
      <span class="scroll-text">Khám phá</span>
    </div>

    <div class="hero-accent-corner"></div>
  </section>
</template>

<style scoped>
.hero-section {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 700px;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-bg__image {
  display: block;
  width: 100%;
  height: 120%;
  object-fit: cover;
  object-position: center 60%;
  transform: translateY(-10%);
  opacity: 0.12;
  filter: saturate(0.85) brightness(0.38) hue-rotate(175deg);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
}

.overlay-noise {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  opacity: 0.035;
}

.overlay-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    oklch(0.07 0.02 280 / 72%) 0%,
    oklch(0.1 0.03 285 / 52%) 40%,
    oklch(0.12 0.06 240 / 30%) 100%
  );
}

.overlay-vignette {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(
      ellipse at 12% 12%,
      oklch(0.78 0.18 195 / 12%) 0%,
      transparent 45%
    ),
    radial-gradient(
      ellipse at 88% 88%,
      oklch(0.68 0.22 350 / 10%) 0%,
      transparent 45%
    ),
    radial-gradient(
      ellipse at 30% 50%,
      transparent 0%,
      transparent 40%,
      oklch(0.06 0.02 280 / 35%) 100%
    );
}

.hero-deepwork-grid {
  position: absolute;
  inset: -12%;
  opacity: 0.26;
  background-image:
    linear-gradient(oklch(0.82 0.16 195 / 8%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.82 0.16 195 / 8%) 1px, transparent 1px);
  background-size: 68px 68px;
  transform: perspective(700px) rotateX(58deg) translateY(15%);
  transform-origin: center bottom;
  mask-image: linear-gradient(to bottom, transparent 0%, black 24%, black 68%, transparent 100%);
  animation: heroGridDrift 16s linear infinite;
}

.hero-beam {
  position: absolute;
  width: min(56vw, 780px);
  height: 2px;
  left: -18vw;
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 70%), oklch(0.84 0.16 85 / 52%), transparent);
  box-shadow: 0 0 28px oklch(0.82 0.16 195 / 36%);
  transform: rotate(-18deg);
  animation: heroBeamSweep 8s ease-in-out infinite;
}

.hero-beam--one {
  top: 28%;
}

.hero-beam--two {
  top: 64%;
  animation-delay: -3s;
  animation-duration: 11s;
  background: linear-gradient(90deg, transparent, oklch(0.68 0.22 330 / 48%), oklch(0.82 0.16 195 / 56%), transparent);
}

.hero-orbit-field {
  position: absolute;
  top: 50%;
  right: clamp(0rem, 6vw, 5rem);
  width: min(48vw, 620px);
  aspect-ratio: 1;
  transform: translateY(-50%);
  opacity: 0.5;
}

.hero-orbit-field span {
  position: absolute;
  inset: 0;
  border: 1px dashed oklch(0.82 0.16 195 / 28%);
  border-radius: 999px;
  animation: heroOrbit 34s linear infinite;
}

.hero-orbit-field span:nth-child(1) {
  inset: 0;
}

.hero-orbit-field span:nth-child(2) {
  inset: 7%;
  border-color: oklch(0.84 0.16 85 / 22%);
  animation-duration: 44s;
  animation-direction: reverse;
}

.hero-orbit-field span:nth-child(3) {
  inset: 14%;
  border-color: oklch(0.68 0.22 330 / 20%);
  animation-duration: 58s;
}

.floating-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: oklch(0.78 0.18 195 / 45%);
  border-radius: 50%;
  box-shadow: 0 0 8px oklch(0.78 0.18 195 / 50%);
}

.particle-1 { top: 20%; left: 15%; }
.particle-2 {
  top: 35%;
  left: 8%;
  width: 6px;
  height: 6px;
  background: oklch(0.68 0.22 350 / 45%);
  box-shadow: 0 0 8px oklch(0.68 0.22 350 / 50%);
}
.particle-3 { top: 55%; left: 20%; width: 3px; height: 3px; }
.particle-4 {
  top: 70%;
  left: 12%;
  background: oklch(0.68 0.22 350 / 45%);
  box-shadow: 0 0 8px oklch(0.68 0.22 350 / 50%);
}

.hero-content {
  position: relative;
  z-index: 3;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem 5rem;
}

.hero-morph-layer {
  position: absolute;
  top: 50%;
  right: clamp(1.25rem, 5vw, 5rem);
  z-index: 2;
  transform: translateY(-50%);
}

.hero-cockpit {
  position: absolute;
  right: clamp(1.25rem, 4vw, 5rem);
  bottom: clamp(4.5rem, 8vh, 7rem);
  z-index: 4;
  width: min(340px, 28vw);
  padding: 1rem;
  border: 1px solid oklch(0.82 0.16 195 / 20%);
  border-radius: 10px;
  color: oklch(0.96 0.01 240);
  background:
    linear-gradient(145deg, oklch(0.18 0.04 280 / 42%), oklch(0.1 0.03 260 / 58%)),
    oklch(0.08 0.02 280 / 40%);
  backdrop-filter: blur(18px) saturate(140%);
  -webkit-backdrop-filter: blur(18px) saturate(140%);
  box-shadow:
    0 20px 70px oklch(0 0 0 / 42%),
    inset 0 0 0 1px oklch(0.96 0.01 240 / 5%),
    0 0 36px oklch(0.82 0.16 195 / 12%);
  overflow: hidden;
}

.hero-cockpit::before,
.hero-cockpit::after {
  content: '';
  position: absolute;
  pointer-events: none;
}

.hero-cockpit::before {
  inset: 0;
  background:
    linear-gradient(90deg, transparent 0 45%, oklch(0.82 0.16 195 / 10%) 50%, transparent 55% 100%),
    repeating-linear-gradient(180deg, transparent 0 8px, oklch(0.96 0.01 240 / 4%) 9px, transparent 10px 18px);
  background-size: 220% 100%, 100% 18px;
  animation: cockpitScan 4.6s linear infinite;
}

.hero-cockpit::after {
  top: 0;
  left: 12%;
  right: 12%;
  height: 1px;
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 72%), transparent);
  box-shadow: 0 0 18px oklch(0.82 0.16 195 / 42%);
}

.cockpit-header,
.cockpit-clock,
.cockpit-bars,
.cockpit-metrics {
  position: relative;
  z-index: 1;
}

.cockpit-header {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  color: oklch(0.84 0.04 240 / 76%);
  font-size: 0.66rem;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.cockpit-live-dot {
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 999px;
  background: oklch(0.78 0.16 150);
  box-shadow: 0 0 0 0 oklch(0.78 0.16 150 / 70%);
  animation: cockpitPulse 2s ease-in-out infinite;
}

.cockpit-clock {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  margin-top: 0.85rem;
}

.cockpit-clock span {
  font-size: clamp(3rem, 5vw, 4.5rem);
  font-weight: 800;
  line-height: 0.9;
  color: oklch(0.96 0.01 240);
  text-shadow: 0 0 26px oklch(0.82 0.16 195 / 30%);
}

.cockpit-clock small {
  color: oklch(0.84 0.16 85);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.cockpit-bars {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  align-items: end;
  gap: 0.32rem;
  height: 68px;
  margin: 1rem 0;
  padding: 0.65rem;
  border: 1px solid oklch(0.96 0.01 240 / 8%);
  border-radius: 8px;
  background: oklch(0.05 0.02 270 / 30%);
}

.cockpit-bars span {
  display: block;
  height: var(--bar-level);
  min-height: 8px;
  border-radius: 999px 999px 2px 2px;
  background: linear-gradient(180deg, oklch(0.84 0.16 85), oklch(0.82 0.16 195));
  box-shadow: 0 0 16px oklch(0.82 0.16 195 / 24%);
  transform-origin: bottom;
  animation: barBreathe 2.8s ease-in-out infinite;
  animation-delay: var(--bar-delay);
}

.cockpit-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.65rem;
}

.cockpit-metric {
  min-width: 0;
  padding: 0.65rem;
  border: 1px solid oklch(0.82 0.16 195 / 14%);
  border-radius: 8px;
  background: oklch(0.16 0.03 280 / 36%);
}

.cockpit-metric span,
.cockpit-metric small {
  display: block;
  overflow-wrap: anywhere;
  color: oklch(0.74 0.04 240 / 70%);
  font-size: 0.58rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  line-height: 1.4;
  text-transform: uppercase;
}

.cockpit-metric strong {
  display: block;
  margin: 0.2rem 0;
  color: oklch(0.82 0.16 195);
  font-size: 1rem;
  line-height: 1;
}

.content-wrapper {
  position: relative;
  max-width: 720px;
}

.content-wrapper::before {
  content: '';
  position: absolute;
  top: -12%;
  left: -16%;
  width: 70%;
  height: 130%;
  background: radial-gradient(
    ellipse at 30% 40%,
    oklch(0.62 0.22 300 / 22%) 0%,
    oklch(0.78 0.18 195 / 12%) 38%,
    transparent 68%
  );
  filter: blur(20px);
  pointer-events: none;
  z-index: -1;
  animation: heroContentGlow 11s ease-in-out infinite;
}

@keyframes heroContentGlow {
  0%,
  100% {
    opacity: 0.7;
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate3d(2%, -2%, 0) scale(1.06);
  }
}

.hero-line {
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, oklch(0.78 0.18 195), oklch(0.68 0.22 350));
  margin-bottom: 2rem;
  border-radius: 2px;
  box-shadow: 0 0 12px oklch(0.78 0.18 195 / 50%);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem 0.5rem 0.5rem;
  background: oklch(0.78 0.18 195 / 10%);
  border: 1px solid oklch(0.78 0.18 195 / 25%);
  border-radius: 100px;
  margin-bottom: 1.5rem;
}

.badge-dot {
  width: 8px;
  height: 8px;
  background: oklch(0.78 0.18 195);
  border-radius: 50%;
  box-shadow: 0 0 8px oklch(0.78 0.18 195 / 70%);
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.2); }
}

.hero-badge {
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: oklch(0.82 0.16 195);
}

.hero-headline {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: clamp(2.5rem, 5.5vw, 4.5rem);
  font-weight: 700;
  line-height: 1.05;
  color: oklch(0.96 0.01 240);
  margin: 0 0 1.5rem 0;
  letter-spacing: -0.02em;
}

.headline-accent {
  display: block;
  color: oklch(0.82 0.16 195);
  filter: drop-shadow(0 0 28px oklch(0.78 0.18 195 / 45%)) drop-shadow(0 0 48px oklch(0.68 0.22 330 / 25%));
}

.hero-tagline {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: clamp(1rem, 1.1vw, 1.2rem);
  font-weight: 400;
  line-height: 1.75;
  color: oklch(0.96 0.01 240 / 80%);
  margin: 0 0 2.5rem 0;
  max-width: 580px;
}

.hero-cta-group {
  display: flex;
  align-items: center;
  gap: 3rem;
}

.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: 1rem;
  padding: 1.125rem 2rem;
  background: linear-gradient(135deg, oklch(0.78 0.18 195) 0%, oklch(0.68 0.22 350) 100%);
  color: oklch(0.08 0.02 280);
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 0 24px oklch(0.78 0.18 195 / 40%);
}

.hero-cta:hover {
  box-shadow: 0 0 36px oklch(0.78 0.18 195 / 60%), 0 12px 32px oklch(0.68 0.22 350 / 40%);
}

.hero-cta:focus-visible {
  outline: 3px solid oklch(0.96 0.01 240);
  outline-offset: 3px;
  box-shadow: 0 0 0 1px oklch(0.78 0.18 195 / 50%), 0 0 36px oklch(0.78 0.18 195 / 60%);
}

.cta-icon {
  width: 20px;
  height: 20px;
  transition: transform 0.3s ease;
}

.hero-cta:hover .cta-icon {
  transform: translateX(4px);
}

.hero-stats-mini {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.mini-stat {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.mini-value {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: oklch(0.82 0.16 195);
  line-height: 1;
}

.mini-label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: oklch(0.7 0.03 240);
}

.mini-divider {
  width: 1px;
  height: 36px;
  background: oklch(0.96 0.01 240 / 15%);
}

.scroll-indicator {
  position: absolute;
  bottom: 2.5rem;
  left: 5rem;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.scroll-line {
  width: 1px;
  height: 60px;
  background: oklch(0.96 0.01 240 / 10%);
  overflow: hidden;
  border-radius: 1px;
}

.scroll-progress {
  width: 100%;
  height: 40%;
  background: linear-gradient(to bottom, oklch(0.78 0.18 195), transparent);
  animation: scrollFlow 2s ease-in-out infinite;
}

@keyframes scrollFlow {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(150%); }
}

@keyframes heroGridDrift {
  from {
    background-position: 0 0, 0 0;
  }

  to {
    background-position: 0 136px, 136px 0;
  }
}

@keyframes heroBeamSweep {
  0% {
    translate: -24vw 0;
    opacity: 0;
  }

  18%,
  72% {
    opacity: 1;
  }

  100% {
    translate: 128vw 0;
    opacity: 0;
  }
}

@keyframes heroOrbit {
  from {
    transform: rotate(0deg) scale(1);
  }

  50% {
    transform: rotate(180deg) scale(1.035);
  }

  to {
    transform: rotate(360deg) scale(1);
  }
}

@keyframes cockpitScan {
  from {
    background-position: 160% 0, 0 0;
  }

  to {
    background-position: -60% 0, 0 18px;
  }
}

@keyframes cockpitPulse {
  0% {
    box-shadow: 0 0 0 0 oklch(0.78 0.16 150 / 70%);
  }

  70% {
    box-shadow: 0 0 0 10px oklch(0.78 0.16 150 / 0%);
  }

  100% {
    box-shadow: 0 0 0 0 oklch(0.78 0.16 150 / 0%);
  }
}

@keyframes barBreathe {
  0%,
  100% {
    transform: scaleY(0.72);
    opacity: 0.72;
  }

  50% {
    transform: scaleY(1);
    opacity: 1;
  }
}

.scroll-text {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 0.7rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: oklch(0.7 0.03 240);
  writing-mode: vertical-rl;
  text-orientation: mixed;
  transform: rotate(180deg);
}

.hero-accent-corner {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 300px;
  height: 300px;
  background:
    radial-gradient(
      ellipse at bottom right,
      oklch(0.68 0.22 350 / 10%) 0%,
      transparent 60%
    ),
    linear-gradient(
      135deg,
      transparent 50%,
      oklch(0.78 0.18 195 / 8%) 100%
    );
  pointer-events: none;
  z-index: 1;
}

@media (max-width: 1200px) {
  .hero-content {
    padding: 2rem 3rem;
  }

  .scroll-indicator {
    left: 3rem;
  }
}

@media (max-width: 1024px) {
  .hero-cta-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 1.5rem;
  }

  .hero-stats-mini {
    padding-left: 0.25rem;
  }

  .hero-morph-layer {
    right: -5rem;
  }

  .hero-cockpit {
    display: none;
  }
}

@media (max-width: 768px) {
  .hero-section {
    min-height: 600px;
  }

  .hero-content {
    padding: 2rem 1.5rem;
  }

  .hero-line {
    width: 40px;
    margin-bottom: 1.5rem;
  }

  .hero-headline {
    font-size: 2.25rem;
  }

  .hero-tagline {
    font-size: 1rem;
    line-height: 1.6;
  }

  .hero-cta-group {
    gap: 1.25rem;
  }

  .hero-cta {
    width: 100%;
    justify-content: center;
    padding: 1rem 1.5rem;
  }

  .hero-stats-mini {
    width: 100%;
    justify-content: center;
    padding-left: 0.5rem;
  }

  .scroll-indicator {
    display: none;
  }

  .hero-accent-corner {
    width: 150px;
    height: 150px;
  }

  .hero-morph-layer {
    display: none;
  }

  .hero-deepwork-grid,
  .hero-beam,
  .hero-orbit-field {
    opacity: 0.25;
  }
}

@media (max-width: 480px) {
  .hero-badge {
    padding: 0.4rem 0.75rem 0.4rem 0.4rem;
    font-size: 0.65rem;
  }

  .hero-headline {
    font-size: 1.875rem;
  }

  .hero-stats-mini {
    gap: 1rem;
  }

  .mini-value {
    font-size: 1.25rem;
  }

  .mini-divider {
    height: 28px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-deepwork-grid,
  .hero-beam,
  .hero-orbit-field span,
  .hero-cockpit::before,
  .cockpit-live-dot,
  .cockpit-bars span {
    animation: none;
  }
}
</style>
