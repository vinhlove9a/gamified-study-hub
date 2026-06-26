<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

const rootRef = ref<HTMLElement | null>(null);
const microLines = Array.from({ length: 18 }, (_, index) => index);

let rafId = 0;
let targetX = 0.5;
let targetY = 0.45;
let currentX = 0.5;
let currentY = 0.45;
let reduceMotion = false;

const writeVars = () => {
  const root = rootRef.value;
  if (!root) return;

  const maxScroll = document.documentElement.scrollHeight - window.innerHeight;
  const scrollProgress = maxScroll > 0 ? Math.min(1, Math.max(0, window.scrollY / maxScroll)) : 0;

  root.style.setProperty('--focus-x', `${(currentX * 100).toFixed(2)}%`);
  root.style.setProperty('--focus-y', `${(currentY * 100).toFixed(2)}%`);
  root.style.setProperty('--scroll-progress', scrollProgress.toFixed(4));
};

const handlePointerMove = (event: PointerEvent) => {
  targetX = event.clientX / Math.max(1, window.innerWidth);
  targetY = event.clientY / Math.max(1, window.innerHeight);
};

const tick = () => {
  currentX += (targetX - currentX) * 0.075;
  currentY += (targetY - currentY) * 0.075;
  writeVars();
  rafId = window.requestAnimationFrame(tick);
};

onMounted(() => {
  reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  writeVars();

  window.addEventListener('scroll', writeVars, { passive: true });
  window.addEventListener('resize', writeVars);

  if (!reduceMotion) {
    window.addEventListener('pointermove', handlePointerMove, { passive: true });
    rafId = window.requestAnimationFrame(tick);
  }
});

onUnmounted(() => {
  window.removeEventListener('scroll', writeVars);
  window.removeEventListener('resize', writeVars);
  window.removeEventListener('pointermove', handlePointerMove);

  if (rafId) {
    window.cancelAnimationFrame(rafId);
  }
});
</script>

<template>
  <div ref="rootRef" class="deepwork-atmosphere" aria-hidden="true">
    <div class="deepwork-atmosphere__grid"></div>
    <div class="deepwork-atmosphere__pointer-lines"></div>
    <div class="deepwork-atmosphere__scan"></div>

    <div class="deepwork-atmosphere__ribbons">
      <span class="ribbon ribbon--one"></span>
      <span class="ribbon ribbon--two"></span>
      <span class="ribbon ribbon--three"></span>
      <span class="ribbon ribbon--four"></span>
    </div>

    <div class="deepwork-atmosphere__frame">
      <span class="frame-corner frame-corner--tl"></span>
      <span class="frame-corner frame-corner--tr"></span>
      <span class="frame-corner frame-corner--bl"></span>
      <span class="frame-corner frame-corner--br"></span>
    </div>

    <div class="deepwork-atmosphere__rail">
      <span></span>
    </div>

    <div class="deepwork-atmosphere__micro-lines">
      <span
        v-for="line in microLines"
        :key="line"
        :style="{
          '--line-offset': `${8 + (line % 4) * 5}vw`,
          '--line-duration': `${9 + line * 0.55}s`,
          '--line-delay': `${line * -0.9}s`
        }"
      ></span>
    </div>
  </div>
</template>

<style scoped>
.deepwork-atmosphere {
  --focus-x: 50%;
  --focus-y: 45%;
  --scroll-progress: 0;
  position: fixed;
  inset: 0;
  z-index: 6;
  overflow: hidden;
  pointer-events: none;
  mix-blend-mode: screen;
}

.deepwork-atmosphere__grid,
.deepwork-atmosphere__scan,
.deepwork-atmosphere__pointer-lines,
.deepwork-atmosphere__ribbons,
.deepwork-atmosphere__frame,
.deepwork-atmosphere__micro-lines {
  position: absolute;
  inset: 0;
}

.deepwork-atmosphere__grid {
  opacity: 0.34;
  background-image:
    linear-gradient(oklch(0.82 0.16 195 / 4%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.82 0.16 195 / 4%) 1px, transparent 1px),
    linear-gradient(oklch(0.84 0.16 85 / 3%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.84 0.16 85 / 3%) 1px, transparent 1px);
  background-position:
    0 calc(var(--scroll-progress) * -240px),
    calc(var(--scroll-progress) * 160px) 0,
    0 calc(var(--scroll-progress) * -90px),
    calc(var(--scroll-progress) * 90px) 0;
  background-size: 96px 96px, 96px 96px, 24px 24px, 24px 24px;
  mask-image: linear-gradient(to bottom, transparent, black 14%, black 78%, transparent);
}

.deepwork-atmosphere__pointer-lines::before,
.deepwork-atmosphere__pointer-lines::after {
  content: '';
  position: absolute;
  opacity: 0.3;
  filter: blur(0.2px);
}

.deepwork-atmosphere__pointer-lines::before {
  top: var(--focus-y);
  left: 50%;
  width: 100vw;
  height: 1px;
  transform: translateX(-50%);
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 32%), transparent);
}

.deepwork-atmosphere__pointer-lines::after {
  top: 50%;
  left: var(--focus-x);
  width: 1px;
  height: 100vh;
  transform: translateY(-50%);
  background: linear-gradient(180deg, transparent, oklch(0.84 0.16 85 / 20%), transparent);
}

.deepwork-atmosphere__scan {
  opacity: 0.16;
  background:
    repeating-linear-gradient(
      180deg,
      transparent 0 8px,
      oklch(0.96 0.01 240 / 8%) 9px,
      transparent 10px 18px
    );
}

.ribbon {
  position: absolute;
  width: min(56vw, 720px);
  height: 2px;
  transform: rotate(-18deg);
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 62%), oklch(0.84 0.16 85 / 42%), transparent);
  box-shadow: 0 0 22px oklch(0.82 0.16 195 / 26%);
  animation: ribbonDrift 14s linear infinite;
}

.ribbon--one {
  top: 14%;
  left: -22%;
}

.ribbon--two {
  top: 42%;
  right: -25%;
  animation-duration: 18s;
  animation-delay: -5s;
  background: linear-gradient(90deg, transparent, oklch(0.68 0.22 330 / 42%), oklch(0.82 0.16 195 / 42%), transparent);
}

.ribbon--three {
  bottom: 26%;
  left: -28%;
  animation-duration: 20s;
  animation-delay: -9s;
}

.ribbon--four {
  bottom: 8%;
  right: -18%;
  animation-duration: 16s;
  animation-delay: -2s;
  background: linear-gradient(90deg, transparent, oklch(0.78 0.16 150 / 38%), oklch(0.84 0.16 85 / 36%), transparent);
}

.frame-corner {
  position: absolute;
  width: 58px;
  height: 58px;
  opacity: 0.38;
  border-color: oklch(0.82 0.16 195 / 56%);
}

.frame-corner--tl {
  top: 1rem;
  left: 1rem;
  border-top: 1px solid;
  border-left: 1px solid;
}

.frame-corner--tr {
  top: 1rem;
  right: 1rem;
  border-top: 1px solid;
  border-right: 1px solid;
}

.frame-corner--bl {
  bottom: 1rem;
  left: 1rem;
  border-bottom: 1px solid;
  border-left: 1px solid;
}

.frame-corner--br {
  right: 1rem;
  bottom: 1rem;
  border-right: 1px solid;
  border-bottom: 1px solid;
}

.deepwork-atmosphere__rail {
  position: absolute;
  top: 8.5rem;
  right: 1.2rem;
  width: 2px;
  height: calc(100vh - 12rem);
  overflow: hidden;
  border-radius: 999px;
  background: oklch(0.96 0.01 240 / 8%);
  box-shadow: 0 0 18px oklch(0.82 0.16 195 / 16%);
}

.deepwork-atmosphere__rail span {
  display: block;
  width: 100%;
  height: 100%;
  transform: scaleY(var(--scroll-progress));
  transform-origin: top;
  border-radius: inherit;
  background: linear-gradient(180deg, oklch(0.82 0.16 195), oklch(0.84 0.16 85), oklch(0.68 0.22 330));
}

.deepwork-atmosphere__micro-lines span {
  position: absolute;
  right: var(--line-offset);
  bottom: -12%;
  width: 1px;
  height: clamp(70px, 10vh, 160px);
  opacity: 0.16;
  background: linear-gradient(180deg, transparent, oklch(0.82 0.16 195 / 72%), transparent);
  animation: microLift var(--line-duration) linear infinite;
  animation-delay: var(--line-delay);
}

@keyframes ribbonDrift {
  from {
    translate: -18vw 0;
    opacity: 0;
  }

  12%,
  78% {
    opacity: 1;
  }

  to {
    translate: 110vw 0;
    opacity: 0;
  }
}

@keyframes microLift {
  from {
    transform: translateY(0);
  }

  to {
    transform: translateY(-128vh);
  }
}

@media (max-width: 768px) {
  .deepwork-atmosphere {
    opacity: 0.62;
  }

  .deepwork-atmosphere__pointer-lines,
  .deepwork-atmosphere__micro-lines,
  .deepwork-atmosphere__rail {
    display: none;
  }

  .frame-corner {
    width: 34px;
    height: 34px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .ribbon,
  .deepwork-atmosphere__micro-lines span {
    animation: none;
  }
}
</style>
