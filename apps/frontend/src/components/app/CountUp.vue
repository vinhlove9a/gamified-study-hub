<script setup lang="ts">
/**
 * Animated number that counts from 0 → `value` when it scrolls into view.
 *
 * Robust for the preview/hidden-tab environment: rAF is paused while the
 * document is hidden, so when `document.hidden` is true (or the user prefers
 * reduced motion) we write the final value immediately instead of animating.
 */
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';

interface Props {
  value: number;
  duration?: number;
  decimals?: number;
  prefix?: string;
  suffix?: string;
  separator?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  duration: 1400,
  decimals: 0,
  prefix: '',
  suffix: '',
  separator: true
});

const display = ref<number>(0);
const root = ref<HTMLElement | null>(null);
let raf = 0;
let observer: IntersectionObserver | null = null;
let started = false;

function format(n: number): string {
  const fixed = n.toFixed(props.decimals);
  if (!props.separator) return `${props.prefix}${fixed}${props.suffix}`;
  const [intPart, frac] = fixed.split('.');
  const grouped = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${props.prefix}${frac ? `${grouped},${frac}` : grouped}${props.suffix}`;
}

function animate(): void {
  if (started) return;
  started = true;
  const start = performance.now();
  const from = 0;
  const to = props.value;
  const tick = (now: number): void => {
    const t = Math.min(1, (now - start) / props.duration);
    const eased = t === 1 ? 1 : 1 - 2 ** (-10 * t);
    display.value = from + (to - from) * eased;
    if (t < 1) raf = requestAnimationFrame(tick);
    else display.value = to;
  };
  raf = requestAnimationFrame(tick);
}

onMounted(() => {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduce || document.hidden || typeof IntersectionObserver === 'undefined' || !root.value) {
    display.value = props.value;
    started = true;
    return;
  }
  observer = new IntersectionObserver(
    (entries) => {
      if (entries.some((e) => e.isIntersecting)) {
        animate();
        observer?.disconnect();
      }
    },
    { threshold: 0.25 }
  );
  observer.observe(root.value);
});

watch(
  () => props.value,
  (v) => {
    if (started) display.value = v;
  }
);

onBeforeUnmount(() => {
  if (raf) cancelAnimationFrame(raf);
  observer?.disconnect();
});
</script>

<template>
  <span ref="root" class="fx-num">{{ format(display) }}</span>
</template>
