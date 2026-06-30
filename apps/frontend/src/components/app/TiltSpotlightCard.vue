<script setup lang="ts">
/**
 * Glass card that tilts in 3D toward the pointer and renders a cursor-tracking
 * spotlight. All transforms are written imperatively to the element so there is
 * no reactive style churn, and everything is disabled under reduced motion.
 */
import { ref } from 'vue';

type Glow = 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';

interface Props {
  glow?: Glow;
  max?: number;
}

const props = withDefaults(defineProps<Props>(), {
  glow: 'cyan',
  max: 7
});

const el = ref<HTMLElement | null>(null);
const active = ref(false);
let reduce = false;

const glowColor: Record<Glow, string> = {
  cyan: 'oklch(0.78 0.18 195 / 22%)',
  magenta: 'oklch(0.68 0.22 350 / 22%)',
  green: 'oklch(0.75 0.18 145 / 22%)',
  violet: 'oklch(0.62 0.2 290 / 22%)',
  amber: 'oklch(0.8 0.16 85 / 22%)'
};

function onEnter(): void {
  reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduce || !el.value) return;
  el.value.style.setProperty('--spot-color', glowColor[props.glow]);
  active.value = true;
}

function onMove(e: PointerEvent): void {
  if (reduce || !el.value) return;
  const r = el.value.getBoundingClientRect();
  const px = (e.clientX - r.left) / r.width;
  const py = (e.clientY - r.top) / r.height;
  el.value.style.setProperty('--spot-x', `${(px * 100).toFixed(1)}%`);
  el.value.style.setProperty('--spot-y', `${(py * 100).toFixed(1)}%`);
  const ry = (px - 0.5) * props.max * 2;
  const rx = -(py - 0.5) * props.max * 2;
  el.value.style.transform = `perspective(900px) rotateX(${rx.toFixed(2)}deg) rotateY(${ry.toFixed(2)}deg)`;
}

function onLeave(): void {
  active.value = false;
  if (el.value) el.value.style.transform = 'perspective(900px)';
}
</script>

<template>
  <div
    ref="el"
    class="tilt-card glass-panel relative overflow-hidden rounded-sm"
    @pointerenter="onEnter"
    @pointermove="onMove"
    @pointerleave="onLeave"
  >
    <div class="tilt-spot" :class="active ? 'opacity-100' : 'opacity-0'" aria-hidden="true" />
    <div class="relative z-10">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.tilt-card {
  transition:
    transform 0.2s ease-out,
    box-shadow 0.3s ease;
  transform-style: preserve-3d;
  will-change: transform;
}
.tilt-card:hover {
  border-color: oklch(0.78 0.18 195 / 40%);
  box-shadow:
    0 18px 50px oklch(0 0 0 / 50%),
    0 0 34px oklch(0.78 0.18 195 / 16%);
}
.tilt-spot {
  position: absolute;
  inset: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  background: radial-gradient(
    circle at var(--spot-x, 50%) var(--spot-y, 50%),
    var(--spot-color, transparent),
    transparent 45%
  );
}
@media (prefers-reduced-motion: reduce) {
  .tilt-card {
    transition: none;
    transform: none !important;
  }
}
</style>
