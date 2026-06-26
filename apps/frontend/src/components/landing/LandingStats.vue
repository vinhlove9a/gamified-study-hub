<script setup lang="ts">
import { inject, nextTick, onMounted, onUnmounted } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import { useAriaLive } from '@/composables/useAriaLive';

interface StatItem {
  id: string;
  displayValue: string;
  suffix: string;
  label: string;
  signal: string;
  accentColor: string;
}

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
const { announce } = useAriaLive();

let cleanupFns: Array<() => void> = [];
const announcedStats = new Set<string>();

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

const stats: StatItem[] = [
  {
    id: 'vault',
    displayValue: '01',
    suffix: 'vault',
    label: 'Một kho tri thức thống nhất cho tài liệu, tag, danh mục và quyền truy cập.',
    signal: 'Knowledge Core',
    accentColor: '#22e0d6'
  },
  {
    id: 'loop',
    displayValue: '04',
    suffix: 'loops',
    label: 'Tài liệu, nhiệm vụ, coin và mở khóa VIP tạo thành chu trình giữ động lực.',
    signal: 'Mission Loop',
    accentColor: '#9b5cff'
  },
  {
    id: 'access',
    displayValue: '24/7',
    suffix: 'online',
    label: 'Học viên truy cập tài liệu, nhận nhiệm vụ và xem tiến độ ở mọi thời điểm.',
    signal: 'Always-on Orbit',
    accentColor: '#f8d66d'
  }
];

const telemetryRows = [
  ['Workspace', 'CLB, gia sư, nhóm luyện thi'],
  ['Data layer', 'Tài liệu, danh mục, tag, quyền'],
  ['Reward layer', 'Coin, streak, VIP unlock'],
  ['Admin layer', 'Theo dõi tiến độ và nhiệm vụ']
];

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.stats-intro > *', '.telemetry-row', '.stat-row', '.control-chip'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.batchReveal?.('.stats-intro > *', {
      trigger: '#stats',
      duration: 0.7,
      stagger: 0.08,
      y: 24
    }));

    registerCleanup(animation?.batchReveal?.('.control-chip', {
      trigger: '#stats',
      duration: 0.5,
      stagger: 0.08,
      y: 18
    }));

    registerCleanup(animation?.batchReveal?.('.stat-row', {
      trigger: '#stats',
      duration: 0.6,
      stagger: 0.12,
      y: 28
    }));

    document.querySelectorAll('.stat-row').forEach((row) => {
      registerCleanup(animation?.hoverScale?.(row as HTMLElement, { scale: 1.015, duration: 0.25 }));
    });

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;

        const statIndex = Array.from(document.querySelectorAll('.stat-row')).indexOf(entry.target as HTMLElement);
        const stat = stats[statIndex];

        if (stat && !announcedStats.has(stat.id)) {
          announcedStats.add(stat.id);
          announce(`${stat.displayValue} ${stat.suffix}, ${stat.label}`);
        }
      });
    }, { threshold: 0.5 });

    document.querySelectorAll('.stat-row').forEach((row) => observer.observe(row));
    cleanupFns.push(() => observer.disconnect());
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => {
    if (fn && typeof fn === 'function') fn();
  });
});
</script>

<template>
  <section id="stats" class="stats-section panel" aria-labelledby="stats-title">
    <div class="stats-effects" aria-hidden="true">
      <span class="stats-sweep stats-sweep--one"></span>
      <span class="stats-sweep stats-sweep--two"></span>
      <span class="stats-ring stats-ring--one"></span>
      <span class="stats-ring stats-ring--two"></span>
    </div>

    <div class="stats-shell">
      <div class="stats-intro">
        <span class="stats-kicker">Workspace telemetry</span>
        <h2 id="stats-title">
          Một buồng điều khiển cho toàn bộ hành trình học.
        </h2>
        <p>
          Gamified Study Hub không chỉ lưu file. Nền tảng biến kho tài liệu thành hệ thống có nhiệm vụ,
          phần thưởng, tiến độ và quyền truy cập rõ ràng cho từng cộng đồng.
        </p>

        <div class="control-grid" aria-label="Các lớp vận hành">
          <div v-for="[label, value] in telemetryRows" :key="label" class="control-chip">
            <span>{{ label }}</span>
            <strong>{{ value }}</strong>
          </div>
        </div>
      </div>

      <div class="stats-console" role="group" aria-label="Chỉ số sản phẩm">
        <div class="console-header">
          <span class="console-dot"></span>
          <span>Live system signals</span>
        </div>

        <div class="stat-list">
          <article
            v-for="stat in stats"
            :key="stat.id"
            class="stat-row"
            :style="{ '--accent': stat.accentColor }"
            tabindex="0"
            :aria-label="`${stat.displayValue} ${stat.suffix}: ${stat.label}`"
          >
            <div class="stat-node" aria-hidden="true">
              <span></span>
            </div>
            <div class="stat-copy">
              <span class="stat-signal">{{ stat.signal }}</span>
              <strong>
                {{ stat.displayValue }}
                <small>{{ stat.suffix }}</small>
              </strong>
              <p>{{ stat.label }}</p>
            </div>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.stats-section {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  padding: clamp(4rem, 8vw, 7rem) 1.5rem;
  color: var(--workspace-text);
  background:
    linear-gradient(135deg, oklch(0.08 0.025 280 / 94%), oklch(0.105 0.032 255 / 92%)),
    var(--workspace-bg);
  isolation: isolate;
}

.stats-section::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  background-image:
    linear-gradient(oklch(0.96 0.01 240 / 3%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.96 0.01 240 / 3%) 1px, transparent 1px);
  background-size: 72px 72px;
  mask-image: linear-gradient(to bottom, transparent, black 14%, black 86%, transparent);
}

.stats-effects {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.stats-sweep {
  position: absolute;
  left: -24vw;
  width: min(64vw, 860px);
  height: 2px;
  transform: rotate(-16deg);
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 58%), oklch(0.84 0.16 85 / 44%), transparent);
  box-shadow: 0 0 24px oklch(0.82 0.16 195 / 26%);
  animation: statsSweep 12s linear infinite;
}

.stats-sweep--one {
  top: 20%;
}

.stats-sweep--two {
  top: 68%;
  animation-delay: -5s;
  animation-duration: 17s;
  background: linear-gradient(90deg, transparent, oklch(0.78 0.16 150 / 46%), oklch(0.68 0.22 330 / 34%), transparent);
}

.stats-ring {
  position: absolute;
  border: 1px dashed oklch(0.82 0.16 195 / 20%);
  border-radius: 999px;
  animation: statsRing 44s linear infinite;
}

.stats-ring--one {
  top: 12%;
  right: -170px;
  width: 520px;
  height: 520px;
}

.stats-ring--two {
  bottom: -220px;
  left: -260px;
  width: 720px;
  height: 720px;
  border-color: oklch(0.84 0.16 85 / 16%);
  animation-direction: reverse;
  animation-duration: 64s;
}

.stats-shell {
  position: relative;
  z-index: 1;
  width: min(1400px, 100%);
  min-height: calc(100vh - 10rem);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(360px, 0.75fr);
  gap: clamp(2rem, 5vw, 5rem);
  align-items: center;
}

.stats-intro {
  max-width: 760px;
}

.stats-kicker,
.console-header,
.control-chip span,
.stat-signal {
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.stats-kicker {
  display: inline-flex;
  color: var(--workspace-cyan);
  margin-bottom: 1rem;
}

.stats-intro h2 {
  margin: 0;
  font-size: clamp(2.2rem, 5vw, 5rem);
  line-height: 1.02;
  letter-spacing: 0;
}

.stats-intro p {
  max-width: 680px;
  margin: 1.4rem 0 2rem;
  color: var(--workspace-muted);
  font-size: clamp(1rem, 1.5vw, 1.18rem);
  line-height: 1.75;
}

.control-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.85rem;
}

.control-chip {
  position: relative;
  overflow: hidden;
  min-height: 96px;
  padding: 1rem;
  border: 1px solid var(--workspace-border);
  border-radius: 8px;
  background: linear-gradient(145deg, oklch(0.16 0.035 280 / 68%), oklch(0.11 0.024 280 / 72%));
  box-shadow: 0 18px 50px oklch(0 0 0 / 30%);
}

.control-chip::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 14%), transparent);
  transform: translateX(-120%);
  animation: chipDataFlow 5.5s ease-in-out infinite;
}

.control-chip:nth-child(2)::before {
  animation-delay: -1.2s;
}

.control-chip:nth-child(3)::before {
  animation-delay: -2.4s;
}

.control-chip:nth-child(4)::before {
  animation-delay: -3.6s;
}

.control-chip > * {
  position: relative;
  z-index: 1;
}

.control-chip span {
  display: block;
  color: var(--workspace-cyan);
  margin-bottom: 0.55rem;
}

.control-chip strong {
  display: block;
  color: var(--workspace-text);
  line-height: 1.45;
}

.stats-console {
  position: relative;
  padding: 1.25rem;
  border: 1px solid oklch(0.78 0.18 195 / 22%);
  border-radius: 10px;
  background:
    linear-gradient(180deg, oklch(0.16 0.035 280 / 78%), oklch(0.1 0.026 280 / 86%));
  box-shadow:
    0 28px 80px oklch(0 0 0 / 42%),
    inset 0 0 0 1px oklch(0.96 0.01 240 / 4%);
  overflow: hidden;
}

.stats-console::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(115deg, transparent 0 38%, oklch(0.82 0.16 195 / 11%) 50%, transparent 62% 100%),
    radial-gradient(ellipse at 20% 15%, oklch(0.84 0.16 85 / 10%), transparent 45%);
  background-size: 240% 100%, 100% 100%;
  animation: consoleSweep 7s linear infinite;
}

.stats-console::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(to bottom, transparent 0 48%, oklch(0.82 0.16 195 / 7%) 50%, transparent 52% 100%);
  background-size: 100% 12px;
  opacity: 0.5;
}

.console-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding-bottom: 1rem;
  color: oklch(0.82 0.04 240 / 76%);
  border-bottom: 1px solid oklch(0.96 0.01 240 / 9%);
}

.console-dot {
  width: 0.58rem;
  height: 0.58rem;
  border-radius: 999px;
  background: var(--workspace-cyan);
  box-shadow: 0 0 16px oklch(0.82 0.16 195 / 70%);
}

.stat-list {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 0.9rem;
  padding-top: 1rem;
}

.stat-row {
  position: relative;
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr);
  gap: 1rem;
  align-items: center;
  padding: 1rem;
  border: 1px solid color-mix(in oklch, var(--accent), transparent 68%);
  border-radius: 8px;
  background: color-mix(in oklch, var(--accent), transparent 92%);
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
  overflow: hidden;
}

.stat-row::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, color-mix(in oklch, var(--accent), transparent 72%), transparent);
  opacity: 0;
  transform: translateX(-110%);
  transition: opacity 0.25s ease;
}

.stat-row:hover::after,
.stat-row:focus-visible::after {
  opacity: 1;
  animation: statRowScan 0.95s ease-out;
}

.stat-row:hover,
.stat-row:focus-visible {
  border-color: color-mix(in oklch, var(--accent), white 14%);
  box-shadow: 0 0 34px color-mix(in oklch, var(--accent), transparent 76%);
}

.stat-node {
  position: relative;
  z-index: 1;
  width: 54px;
  height: 54px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  border: 1px solid color-mix(in oklch, var(--accent), transparent 35%);
  box-shadow: inset 0 0 18px color-mix(in oklch, var(--accent), transparent 82%);
}

.stat-node::before {
  content: '';
  position: absolute;
  inset: -8px;
  border: 1px solid color-mix(in oklch, var(--accent), transparent 62%);
  border-radius: inherit;
  animation: statNodePulse 2.8s ease-in-out infinite;
}

.stat-node span {
  position: relative;
  z-index: 1;
  width: 16px;
  height: 16px;
  border-radius: 999px;
  background: var(--accent);
  box-shadow: 0 0 18px var(--accent);
}

.stat-copy {
  position: relative;
  z-index: 1;
}

.stat-copy strong {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  margin: 0.25rem 0 0.35rem;
  color: var(--workspace-text);
  font-size: clamp(2rem, 4vw, 3.4rem);
  line-height: 1;
}

.stat-copy small {
  font-size: 0.82rem;
  color: color-mix(in oklch, var(--accent), white 18%);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.stat-signal {
  color: var(--accent);
}

.stat-copy p {
  margin: 0;
  color: var(--workspace-muted);
  line-height: 1.55;
}

@keyframes statsSweep {
  from {
    translate: -20vw 0;
    opacity: 0;
  }

  16%,
  74% {
    opacity: 1;
  }

  to {
    translate: 130vw 0;
    opacity: 0;
  }
}

@keyframes statsRing {
  from {
    transform: rotate(0deg) scale(1);
  }

  50% {
    transform: rotate(180deg) scale(1.03);
  }

  to {
    transform: rotate(360deg) scale(1);
  }
}

@keyframes chipDataFlow {
  0%,
  42% {
    transform: translateX(-120%);
  }

  72%,
  100% {
    transform: translateX(120%);
  }
}

@keyframes consoleSweep {
  from {
    background-position: 170% 0, 0 0;
  }

  to {
    background-position: -70% 0, 0 0;
  }
}

@keyframes statRowScan {
  from {
    transform: translateX(-110%);
  }

  to {
    transform: translateX(110%);
  }
}

@keyframes statNodePulse {
  0%,
  100% {
    opacity: 0.35;
    transform: scale(0.92);
  }

  50% {
    opacity: 0.9;
    transform: scale(1.05);
  }
}

@media (max-width: 1024px) {
  .stats-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-section {
    padding: 4rem 1rem;
  }

  .control-grid {
    grid-template-columns: 1fr;
  }

  .stats-console {
    padding: 1rem;
  }

  .stat-row {
    grid-template-columns: 42px minmax(0, 1fr);
    padding: 0.9rem;
  }

  .stat-node {
    width: 42px;
    height: 42px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .stats-sweep,
  .stats-ring,
  .control-chip::before,
  .stats-console::before,
  .stat-row:hover::after,
  .stat-row:focus-visible::after,
  .stat-node::before {
    animation: none;
  }
}
</style>
