<template>
  <div class="landing-marquee" aria-label="Chuỗi giá trị sản phẩm">
    <div class="landing-marquee__track">
      <span v-for="item in marqueeItems" :key="`a-${item}`">{{ item }}</span>
      <span v-for="item in marqueeItems" :key="`b-${item}`">{{ item }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const marqueeItems = [
  'Knowledge Vault',
  'Mission Loop',
  'Coin Engine',
  'Streak Orbit',
  'VIP Unlock',
  'Study Workspace',
  'Admin Cockpit',
  'Progress Radar'
];
</script>

<style scoped>
.landing-marquee {
  position: relative;
  overflow: hidden;
  border-top: 1px solid oklch(0.7 0.1 280 / 14%);
  border-bottom: 1px solid oklch(0.7 0.1 280 / 14%);
  background:
    linear-gradient(90deg, oklch(0.08 0.02 280 / 72%), oklch(0.12 0.035 286 / 72%)),
    transparent;
  padding: 1.25rem 0;
  isolation: isolate;
}

.landing-marquee::before,
.landing-marquee::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 2;
  width: min(18vw, 180px);
  pointer-events: none;
}

.landing-marquee::before {
  left: 0;
  background: linear-gradient(90deg, oklch(0.08 0.02 280), transparent);
}

.landing-marquee::after {
  right: 0;
  background: linear-gradient(270deg, oklch(0.12 0.035 286), transparent);
}

.landing-marquee__track {
  display: flex;
  width: max-content;
  gap: 3.75rem;
  white-space: nowrap;
  animation: landingMarqueeMove 42s linear infinite;
  font-family: var(--font-sans);
  font-size: clamp(1.1rem, 2vw, 1.75rem);
  font-weight: 700;
  color: oklch(0.88 0.04 240 / 72%);
}

.landing-marquee__track span {
  display: inline-flex;
  align-items: center;
  gap: 3.75rem;
}

.landing-marquee__track span::after {
  content: '';
  width: 0.45rem;
  height: 0.45rem;
  border-radius: 999px;
  background: linear-gradient(135deg, oklch(0.82 0.16 195), oklch(0.78 0.18 85));
  box-shadow: 0 0 16px oklch(0.82 0.16 195 / 45%);
}

@keyframes landingMarqueeMove {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(-50%);
  }
}

@media (max-width: 640px) {
  .landing-marquee {
    padding: 1rem 0;
  }

  .landing-marquee__track {
    gap: 2.25rem;
  }

  .landing-marquee__track span {
    gap: 2.25rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .landing-marquee__track {
    animation: none;
    transform: translateX(0);
  }
}
</style>
