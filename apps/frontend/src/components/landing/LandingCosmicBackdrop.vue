<script setup lang="ts">
/**
 * Immersive deep-space backdrop for the landing page.
 *
 * Sits *behind* the twinkling StarfieldCanvas (z-index lower than -10) so the
 * crisp stars layer on top of a slow, luminous nebula + aurora wash. Layers
 * (back → front): drifting nebula blooms → aurora curtains (additive `screen`
 * blend) → a giant orbited planet peeking from the bottom with an atmospheric
 * rim → a dawn "horizon" glow.
 *
 * Pure CSS transforms/opacity only — no JS loop, GPU-friendly, and it honours
 * `prefers-reduced-motion` (the global reduced-motion rule freezes the
 * animations to a single calm frame).
 */
</script>

<template>
  <div class="cosmic-backdrop" aria-hidden="true">
    <!-- Slow drifting nebula blooms -->
    <div class="neb neb--violet"></div>
    <div class="neb neb--cyan"></div>
    <div class="neb neb--magenta"></div>

    <!-- Aurora curtains (additive glow) -->
    <div class="aurora">
      <span class="aurora__band aurora__band--1"></span>
      <span class="aurora__band aurora__band--2"></span>
      <span class="aurora__band aurora__band--3"></span>
    </div>

    <!-- Orbited planet peeking from below the fold -->
    <div class="planet">
      <div class="planet__atmos"></div>
      <div class="planet__body"></div>
      <div class="planet__terminator"></div>
      <div class="planet__rim"></div>
    </div>

    <!-- Dawn horizon glow -->
    <div class="horizon"></div>
  </div>
</template>

<style scoped>
.cosmic-backdrop {
  position: fixed;
  inset: 0;
  z-index: -15; /* behind StarfieldCanvas (-10), above .cosmic-page gradient */
  overflow: hidden;
  pointer-events: none;
}

/* ── Nebula blooms ───────────────────────────────────────────────────────── */
.neb {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  mix-blend-mode: screen;
  will-change: transform, opacity;
}

.neb--violet {
  top: -16%;
  left: -10%;
  width: 60vw;
  height: 60vw;
  background: radial-gradient(circle at 50% 50%, oklch(0.58 0.26 295 / 68%), transparent 68%);
  animation: nebDrift 34s ease-in-out infinite;
}

.neb--cyan {
  bottom: -24%;
  right: -12%;
  width: 64vw;
  height: 64vw;
  background: radial-gradient(circle at 50% 50%, oklch(0.72 0.17 200 / 55%), transparent 68%);
  animation: nebDrift 44s ease-in-out infinite reverse;
}

.neb--magenta {
  top: 28%;
  left: 40%;
  width: 50vw;
  height: 50vw;
  background: radial-gradient(circle at 50% 50%, oklch(0.64 0.25 330 / 42%), transparent 72%);
  animation: nebDrift 52s ease-in-out infinite;
  animation-delay: -12s;
}

@keyframes nebDrift {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0.75;
  }
  50% {
    transform: translate3d(4vw, -3vh, 0) scale(1.12);
    opacity: 1;
  }
}

/* ── Aurora curtains ─────────────────────────────────────────────────────── */
.aurora {
  position: absolute;
  inset: -20% -10% auto -10%;
  height: 96vh;
  filter: blur(34px);
  mix-blend-mode: screen;
  opacity: 0.9;
}

.aurora__band {
  position: absolute;
  top: -30%;
  height: 150%;
  width: 36%;
  border-radius: 48% 52% 60% 40% / 60% 50% 50% 40%;
  transform-origin: top center;
  will-change: transform, opacity;
}

.aurora__band--1 {
  left: 4%;
  background: linear-gradient(
    180deg,
    transparent 0%,
    oklch(0.8 0.19 195 / 58%) 30%,
    oklch(0.72 0.2 170 / 42%) 60%,
    transparent 100%
  );
  animation: auroraSway 17s ease-in-out infinite;
}

.aurora__band--2 {
  left: 36%;
  width: 30%;
  background: linear-gradient(
    180deg,
    transparent 0%,
    oklch(0.64 0.26 300 / 54%) 34%,
    oklch(0.72 0.2 260 / 36%) 64%,
    transparent 100%
  );
  animation: auroraSway 23s ease-in-out infinite;
  animation-delay: -7s;
}

.aurora__band--3 {
  right: 4%;
  width: 34%;
  background: linear-gradient(
    180deg,
    transparent 0%,
    oklch(0.68 0.24 335 / 50%) 32%,
    oklch(0.8 0.18 200 / 34%) 62%,
    transparent 100%
  );
  animation: auroraSway 19s ease-in-out infinite;
  animation-delay: -3s;
}

@keyframes auroraSway {
  0%,
  100% {
    transform: skewX(-8deg) scaleY(1) translateX(0);
    opacity: 0.55;
  }
  50% {
    transform: skewX(10deg) scaleY(1.18) translateX(3vw);
    opacity: 0.95;
  }
}

/* ── Orbited planet (peeks from below the fold) ──────────────────────────── */
.planet {
  position: absolute;
  left: 50%;
  bottom: -88vh;
  width: 150vh;
  height: 150vh;
  transform: translateX(-50%);
}

.planet__atmos {
  position: absolute;
  inset: -7%;
  border-radius: 50%;
  background: radial-gradient(
    circle at 50% 22%,
    oklch(0.82 0.16 195 / 34%),
    oklch(0.6 0.22 300 / 16%) 42%,
    transparent 62%
  );
  filter: blur(26px);
  mix-blend-mode: screen;
  animation: planetBreathe 12s ease-in-out infinite;
}

.planet__body {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background:
    radial-gradient(circle at 42% 16%, oklch(0.26 0.07 280 / 95%), transparent 55%),
    radial-gradient(circle at 70% 80%, oklch(0.1 0.04 280), oklch(0.07 0.02 280));
  box-shadow:
    inset 0 0 120px oklch(0.04 0.02 280 / 90%),
    inset 22px -12px 90px oklch(0 0 0 / 70%);
}

.planet__terminator {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: linear-gradient(
    18deg,
    oklch(0.04 0.02 280 / 92%) 38%,
    transparent 64%
  );
}

.planet__rim {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: transparent;
  box-shadow:
    inset 0 6px 22px oklch(0.86 0.15 195 / 55%),
    inset 0 2px 60px oklch(0.78 0.18 200 / 30%);
}

@keyframes planetBreathe {
  0%,
  100% {
    opacity: 0.8;
  }
  50% {
    opacity: 1;
  }
}

/* ── Dawn horizon glow ───────────────────────────────────────────────────── */
.horizon {
  position: absolute;
  left: 50%;
  bottom: -4vh;
  width: 130vw;
  height: 30vh;
  transform: translateX(-50%);
  background: radial-gradient(
    ellipse at 50% 100%,
    oklch(0.82 0.16 195 / 26%) 0%,
    oklch(0.6 0.22 300 / 14%) 38%,
    transparent 70%
  );
  mix-blend-mode: screen;
  animation: horizonPulse 9s ease-in-out infinite;
}

@keyframes horizonPulse {
  0%,
  100% {
    opacity: 0.7;
  }
  50% {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .neb {
    filter: blur(48px);
  }
  .aurora {
    opacity: 0.7;
    filter: blur(28px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .neb,
  .aurora__band,
  .planet__atmos,
  .horizon {
    animation: none;
  }
}
</style>
