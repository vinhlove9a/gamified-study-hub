<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

type Point = [number, number];

interface MorphState {
  path: string;
  ghosts: [string, string, string];
  vertices: Array<{ x: number; y: number; r: number }>;
}

const vertexCount = 12;
const svgRef = ref<SVGSVGElement | null>(null);
const blobPath = ref('');
const ghostPaths = ref<[string, string, string]>(['', '', '']);
const vertices = ref<MorphState['vertices']>([]);

let rafId = 0;
let startTime = 0;
let mouseFx = 0;
let mouseFy = 0;
let reduceMotionQuery: MediaQueryList | null = null;

const catmullRomClosed = (points: Point[]): string => {
  const n = points.length;
  let d = '';

  for (let i = 0; i < n; i += 1) {
    const p0 = points[(i - 1 + n) % n];
    const p1 = points[i];
    const p2 = points[(i + 1) % n];
    const p3 = points[(i + 2) % n];

    if (i === 0) {
      d += `M ${p1[0].toFixed(2)},${p1[1].toFixed(2)} `;
    }

    const cp1x = p1[0] + (p2[0] - p0[0]) / 6;
    const cp1y = p1[1] + (p2[1] - p0[1]) / 6;
    const cp2x = p2[0] - (p3[0] - p1[0]) / 6;
    const cp2y = p2[1] - (p3[1] - p1[1]) / 6;

    d += `C ${cp1x.toFixed(2)},${cp1y.toFixed(2)} ${cp2x.toFixed(2)},${cp2y.toFixed(2)} ${p2[0].toFixed(2)},${p2[1].toFixed(2)} `;
  }

  return `${d}Z`;
};

const buildPoints = (t: number, cx: number, cy: number, offset = 0): Point[] => {
  const baseRadius = 150;
  const points: Point[] = [];
  const shiftedTime = t - offset;

  for (let i = 0; i < vertexCount; i += 1) {
    const angle = (i / vertexCount) * Math.PI * 2;
    const radius =
      baseRadius +
      Math.sin(shiftedTime * 0.7 + i * 0.9) * 32 +
      Math.cos(shiftedTime * 0.5 + i * 1.7) * 22 +
      Math.sin(shiftedTime * 1.1 + i * 0.3) * 12;

    points.push([
      cx + Math.cos(angle) * radius,
      cy + Math.sin(angle) * radius
    ]);
  }

  return points;
};

const draw = (t: number) => {
  const cx = 250 + mouseFx;
  const cy = 250 + mouseFy;
  const points = buildPoints(t, cx, cy);

  blobPath.value = catmullRomClosed(points);
  ghostPaths.value = [
    catmullRomClosed(buildPoints(t, cx, cy, 0.3)),
    catmullRomClosed(buildPoints(t, cx, cy, 0.6)),
    catmullRomClosed(buildPoints(t, cx, cy, 0.9))
  ];

  vertices.value = points.map(([x, y], i) => ({
    x,
    y,
    r: Math.max(1.5, 2 + Math.sin(t * 2 + i) * 1.5)
  }));
};

const tick = () => {
  const t = (performance.now() - startTime) / 1000;
  draw(t);
  rafId = window.requestAnimationFrame(tick);
};

const handlePointerMove = (event: PointerEvent) => {
  if (!svgRef.value || reduceMotionQuery?.matches) return;

  const rect = svgRef.value.getBoundingClientRect();
  mouseFx = ((event.clientX - rect.left) / rect.width - 0.5) * 30;
  mouseFy = ((event.clientY - rect.top) / rect.height - 0.5) * 30;
};

const handlePointerLeave = () => {
  mouseFx = 0;
  mouseFy = 0;
};

onMounted(() => {
  reduceMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
  draw(0);

  if (reduceMotionQuery.matches) return;

  startTime = performance.now();
  rafId = window.requestAnimationFrame(tick);
});

onUnmounted(() => {
  if (rafId) {
    window.cancelAnimationFrame(rafId);
  }
});
</script>

<template>
  <div class="hero-morph" aria-hidden="true">
    <div class="shape-meta shape-meta--tl">12 learning signals<br />orbit sync</div>
    <div class="shape-meta shape-meta--br">vault + mission + reward<br />adaptive loop</div>

    <svg
      ref="svgRef"
      class="hero-morph__svg"
      viewBox="0 0 500 500"
      @pointermove="handlePointerMove"
      @pointerleave="handlePointerLeave"
    >
      <defs>
        <radialGradient id="studyBlobGrad" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stop-color="#22e0d6" stop-opacity="0.36" />
          <stop offset="58%" stop-color="#22e0d6" stop-opacity="0.09" />
          <stop offset="100%" stop-color="#22e0d6" stop-opacity="0" />
        </radialGradient>
        <linearGradient id="studyStrokeGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#22e0d6" />
          <stop offset="52%" stop-color="#c084fc" />
          <stop offset="100%" stop-color="#f8d66d" />
        </linearGradient>
        <filter id="studyBlobGlow" x="-30%" y="-30%" width="160%" height="160%">
          <feGaussianBlur stdDeviation="3" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      <line x1="250" y1="20" x2="250" y2="480" class="hero-morph__axis" />
      <line x1="20" y1="250" x2="480" y2="250" class="hero-morph__axis" />

      <path :d="ghostPaths[2]" class="hero-morph__ghost hero-morph__ghost--3" />
      <path :d="ghostPaths[1]" class="hero-morph__ghost hero-morph__ghost--2" />
      <path :d="ghostPaths[0]" class="hero-morph__ghost hero-morph__ghost--1" />
      <path :d="blobPath" fill="url(#studyBlobGrad)" />
      <path :d="blobPath" class="hero-morph__stroke" />

      <g>
        <circle
          v-for="(dot, index) in vertices"
          :key="index"
          :cx="dot.x"
          :cy="dot.y"
          :r="dot.r"
          class="hero-morph__vertex"
        />
      </g>

      <circle cx="250" cy="250" r="3" class="hero-morph__center" />
      <circle cx="250" cy="250" r="8" class="hero-morph__center-ring" />
    </svg>

    <span class="float-symbol float-symbol--one">XP</span>
    <span class="float-symbol float-symbol--two">VIP</span>
    <span class="float-symbol float-symbol--three">COIN</span>
  </div>
</template>

<style scoped>
.hero-morph {
  position: relative;
  aspect-ratio: 1;
  width: min(42vw, 560px);
  min-width: 320px;
  pointer-events: none;
}

.hero-morph__svg {
  display: block;
  width: 100%;
  height: 100%;
  pointer-events: auto;
  touch-action: none;
  filter: drop-shadow(0 28px 70px oklch(0.78 0.18 195 / 18%));
}

.hero-morph__axis {
  stroke: oklch(0.96 0.01 240 / 8%);
  stroke-dasharray: 2 7;
}

.hero-morph__ghost {
  fill: none;
  stroke-width: 1;
}

.hero-morph__ghost--1 {
  stroke: oklch(0.82 0.16 195 / 24%);
}

.hero-morph__ghost--2 {
  stroke: oklch(0.82 0.16 195 / 15%);
}

.hero-morph__ghost--3 {
  stroke: oklch(0.82 0.16 195 / 9%);
  stroke-dasharray: 1 5;
}

.hero-morph__stroke {
  fill: none;
  stroke: url(#studyStrokeGrad);
  stroke-width: 2;
  filter: url(#studyBlobGlow);
}

.hero-morph__vertex {
  fill: oklch(0.82 0.16 195);
  opacity: 0.95;
}

.hero-morph__center {
  fill: oklch(0.82 0.16 195);
}

.hero-morph__center-ring {
  fill: none;
  stroke: oklch(0.82 0.16 195 / 42%);
}

.shape-meta {
  position: absolute;
  z-index: 2;
  font-family: var(--font-sans);
  font-size: 0.66rem;
  font-weight: 700;
  line-height: 1.5;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: oklch(0.83 0.04 240 / 62%);
}

.shape-meta--tl {
  top: 8%;
  left: 4%;
}

.shape-meta--br {
  right: 4%;
  bottom: 8%;
  text-align: right;
}

.float-symbol {
  position: absolute;
  z-index: 1;
  font-family: var(--font-sans);
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  user-select: none;
  opacity: 0.28;
}

.float-symbol--one {
  top: 10%;
  right: -5%;
  color: oklch(0.82 0.16 195);
  font-size: 2rem;
  animation: morphFloatY 9s ease-in-out infinite;
}

.float-symbol--two {
  bottom: 21%;
  left: -8%;
  color: oklch(0.72 0.22 330);
  font-size: 1.8rem;
  animation: morphFloatY 7s ease-in-out infinite reverse;
}

.float-symbol--three {
  top: 51%;
  right: -10%;
  color: oklch(0.84 0.16 85);
  font-size: 1.35rem;
  animation: morphFloatY 11s ease-in-out infinite;
}

@keyframes morphFloatY {
  0%,
  100% {
    transform: translateY(0) rotate(-3deg);
  }

  50% {
    transform: translateY(-18px) rotate(3deg);
  }
}

@media (max-width: 1024px) {
  .hero-morph {
    width: min(58vw, 440px);
    min-width: 260px;
    opacity: 0.58;
  }

  .shape-meta,
  .float-symbol {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .float-symbol {
    animation: none;
  }
}
</style>
