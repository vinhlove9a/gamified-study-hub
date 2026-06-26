<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

/**
 * Cinematic cross-fading "reel" backdrop for the landing page.
 *
 * Replicates the hero reel from the reference design (ex.html): a slow
 * cross-fade between full-bleed photographic frames, each drifting with a
 * Ken Burns zoom, layered under a dark cosmic gradient + faint scanline so
 * foreground content stays legible. Adapted to the Deep-Space / deep-work
 * theme — frames are bundled locally in /public/images/reel and tinted
 * cyan/violet rather than the reference's hard grayscale.
 *
 * Sits at z-index -2 (behind the twinkling StarfieldCanvas at -1 and all
 * content), so it serves as the single fixed background for the whole page.
 * Honours `prefers-reduced-motion`: shows a single static frame, no drift.
 */

// Ordered for the reel: open on a richly textured cosmic frame, alternate
// space ↔ grounded study scenes.
const frames = [
  '/images/reel/reel-2.jpg', // Milky Way over mountains
  '/images/reel/reel-4.jpg', // Milky Way galaxy texture
  '/images/reel/reel-1.jpg', // Earth at night from orbit
  '/images/reel/reel-5.jpg', // Stargazer silhouette
  '/images/reel/reel-3.jpg'  // Library reading hall
];

const FRAME_MS = 6500;

const rootRef = ref<HTMLElement | null>(null);
const active = ref(0);

let frameTimer = 0;
let scrollRaf = 0;

const advance = () => {
  active.value = (active.value + 1) % frames.length;
};

const startTimer = () => {
  if (frameTimer) return;
  frameTimer = window.setInterval(advance, FRAME_MS);
};

const stopTimer = () => {
  if (frameTimer) {
    window.clearInterval(frameTimer);
    frameTimer = 0;
  }
};

// Pause the reel while the tab is backgrounded (saves work + avoids racing
// through frames so the page opens on the first frame).
const onVisibilityChange = () => {
  if (document.hidden) {
    stopTimer();
  } else {
    startTimer();
  }
};

const onScroll = () => {
  if (scrollRaf) return;
  scrollRaf = window.requestAnimationFrame(() => {
    scrollRaf = 0;
    const root = rootRef.value;
    if (!root) return;
    // Gentle parallax: the oversized frames absorb the offset (no edge gap).
    const offset = Math.min(window.scrollY * 0.06, 64);
    root.style.setProperty('--reel-parallax', `${offset.toFixed(1)}px`);
  });
};

onMounted(() => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduceMotion) return; // single static frame, no auto-advance / drift

  if (!document.hidden) startTimer();
  document.addEventListener('visibilitychange', onVisibilityChange);
  window.addEventListener('scroll', onScroll, { passive: true });
});

onUnmounted(() => {
  stopTimer();
  if (scrollRaf) window.cancelAnimationFrame(scrollRaf);
  document.removeEventListener('visibilitychange', onVisibilityChange);
  window.removeEventListener('scroll', onScroll);
});
</script>

<template>
  <div ref="rootRef" class="reel-backdrop" aria-hidden="true">
    <div class="reel-frames">
      <div
        v-for="(src, index) in frames"
        :key="src"
        class="reel-frame"
        :class="{ 'is-active': index === active }"
      >
        <img :src="src" alt="" decoding="async" :loading="index === 0 ? 'eager' : 'lazy'" />
      </div>
    </div>

    <div class="reel-overlay"></div>
    <div class="reel-scan"></div>
    <div class="reel-vignette"></div>
  </div>
</template>

<style scoped>
.reel-backdrop {
  --reel-parallax: 0px;
  position: fixed;
  inset: 0;
  z-index: -2; /* behind StarfieldCanvas (-1) and all content */
  overflow: hidden;
  pointer-events: none;
  background: oklch(0.04 0.014 280); /* dark base while frames load */
}

.reel-frames {
  position: absolute;
  inset: -7% 0;
  transform: translateY(var(--reel-parallax));
  will-change: transform;
}

.reel-frame {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 2.1s ease-in-out;
}

.reel-frame.is-active {
  opacity: 1;
}

.reel-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  /* Cinematic, cosmic-tinted treatment (vs. the reference's hard grayscale) */
  filter: saturate(0.95) contrast(1.08) brightness(0.56);
  transform: scale(1.08);
}

.reel-frame.is-active img {
  animation: reelKenBurns 13s ease-out forwards;
}

@keyframes reelKenBurns {
  from {
    transform: scale(1.08) translate3d(0, 0, 0);
  }
  to {
    transform: scale(1.2) translate3d(-2.5%, -2.5%, 0);
  }
}

/* Dark cosmic gradient — darkens top (nav) & bottom for legibility, tints the theme. */
.reel-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(
      180deg,
      oklch(0.05 0.018 280 / 58%) 0%,
      oklch(0.06 0.02 280 / 38%) 30%,
      oklch(0.05 0.02 280 / 52%) 64%,
      oklch(0.04 0.015 280 / 90%) 100%
    ),
    radial-gradient(ellipse at 22% 28%, oklch(0.72 0.17 200 / 16%) 0%, transparent 55%),
    radial-gradient(ellipse at 82% 76%, oklch(0.6 0.22 320 / 14%) 0%, transparent 55%);
}

/* Faint horizontal scanline, echoing the reference's cinematic feel. */
.reel-scan {
  position: absolute;
  inset: 0;
  opacity: 0.5;
  background: repeating-linear-gradient(
    0deg,
    transparent 0 2px,
    oklch(0 0 0 / 22%) 3px,
    transparent 4px
  );
}

/* Edge vignette to focus the eye toward the centre. */
.reel-vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    ellipse at 50% 42%,
    transparent 42%,
    oklch(0.03 0.01 280 / 62%) 100%
  );
}

@media (max-width: 768px) {
  .reel-frames {
    inset: -4% 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .reel-frames {
    transform: none;
  }
  .reel-frame.is-active img {
    animation: none;
    transform: scale(1.04);
  }
}
</style>
