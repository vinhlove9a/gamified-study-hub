<script setup lang="ts">
/**
 * Fullscreen confetti overlay. Call the exposed `burst()` from a parent ref to
 * celebrate a reward / completion. No-op under reduced motion.
 */
import { ref, onBeforeUnmount } from 'vue';

interface Confetto {
  id: number;
  left: number;
  delay: number;
  duration: number;
  color: string;
  rotate: number;
  size: number;
  variant: number;
}

const pieces = ref<Confetto[]>([]);
let seq = 0;
let timer = 0;

const palette: string[] = [
  'oklch(0.78 0.18 195)',
  'oklch(0.68 0.22 350)',
  'oklch(0.8 0.16 85)',
  'oklch(0.75 0.18 145)',
  'oklch(0.62 0.2 290)'
];

function burst(count = 52): void {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduce) return;
  seq += 1;
  const batch = seq * 1000;
  pieces.value = Array.from({ length: count }, (_, i) => ({
    id: batch + i,
    left: Math.random() * 100,
    delay: Math.random() * 0.3,
    duration: 1.5 + Math.random() * 1.2,
    color: palette[i % palette.length],
    rotate: Math.random() * 360,
    size: 6 + Math.random() * 9,
    variant: i % 3
  }));
  if (timer) window.clearTimeout(timer);
  timer = window.setTimeout(() => {
    pieces.value = [];
  }, 3000);
}

defineExpose({ burst });

onBeforeUnmount(() => {
  if (timer) window.clearTimeout(timer);
});
</script>

<template>
  <Teleport to="body">
    <div class="pointer-events-none fixed inset-0 z-[120] overflow-hidden" aria-hidden="true">
      <span
        v-for="p in pieces"
        :key="p.id"
        class="confetto-piece absolute top-[-24px] rounded-[2px]"
        :class="`confetto-v${p.variant}`"
        :style="{
          left: `${p.left}%`,
          width: `${p.size}px`,
          height: `${p.size * 1.5}px`,
          background: p.color,
          animationDelay: `${p.delay}s`,
          animationDuration: `${p.duration}s`,
          transform: `rotate(${p.rotate}deg)`
        }"
      />
    </div>
  </Teleport>
</template>

<style scoped>
.confetto-piece {
  animation-timing-function: cubic-bezier(0.3, 0.6, 0.5, 1);
  animation-fill-mode: forwards;
}
.confetto-v0 {
  animation-name: confetto-fall-l;
}
.confetto-v1 {
  animation-name: confetto-fall-c;
}
.confetto-v2 {
  animation-name: confetto-fall-r;
}
@keyframes confetto-fall-c {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(105vh) rotate(620deg);
    opacity: 0.15;
  }
}
@keyframes confetto-fall-l {
  0% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translate(-140px, 105vh) rotate(-540deg);
    opacity: 0.15;
  }
}
@keyframes confetto-fall-r {
  0% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translate(150px, 105vh) rotate(560deg);
    opacity: 0.15;
  }
}
@media (prefers-reduced-motion: reduce) {
  .confetto-piece {
    display: none;
  }
}
</style>
