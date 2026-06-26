<script setup lang="ts">
import { onMounted, onUnmounted, inject } from 'vue';
import type { AnimationAPI } from '@/types/gsap';

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
let cleanupFns: Array<() => void> = [];

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.left-content', '.right-inner', '.value-card'], { opacity: 1, y: 0 });
    return;
  }

  animation?.batchReveal?.('.left-content', {
    trigger: '#mission',
    duration: 0.8,
    y: 50
  });

  animation?.batchReveal?.('.right-inner', {
    trigger: '#mission',
    duration: 0.8,
    y: 50
  });

  animation?.batchReveal?.('.value-card', {
    trigger: '#mission',
    duration: 0.5,
    stagger: 0.1,
    y: 30
  });

  animation?.bgParallax?.('.bg-parallax', { speed: 0.15, scrub: true });

  document.querySelectorAll('.value-card').forEach((card) => {
    const cleanup = animation?.hoverScale?.(card as HTMLElement, { scale: 1.03, duration: 0.3 });
    if (cleanup) cleanupFns.push(cleanup);
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn && typeof fn === 'function' && fn());
});

const missionText = "Gamified Study Hub biến việc chia sẻ tài liệu thành một hành trình học tập có mục tiêu. Người quản lý có thể tổ chức nội dung, giao nhiệm vụ, trao coin và mở khóa tài liệu VIP dựa trên nỗ lực thật của học viên.";

const visionText = "Gamified Study Hub không chỉ lưu trữ file. Nền tảng giúp người quản lý thiết kế nhiệm vụ, trao coin, tạo streak và cho phép học viên mở khóa tài liệu VIP bằng chính nỗ lực học tập của mình.";

const values = [
  {
    id: 'sustainability',
    label: 'Tổ chức tài liệu rõ ràng',
    desc: 'Theo workspace, danh mục, thẻ và quyền truy cập'
  },
  {
    id: 'scale',
    label: 'Tạo nhiệm vụ học tập',
    desc: 'Điểm danh, bài tập, đóng góp tài liệu, hoạt động'
  },
  {
    id: 'innovation',
    label: 'Thưởng coin minh bạch',
    desc: 'Mỗi hành động học tập đều có thể được ghi nhận'
  },
  {
    id: 'pride',
    label: 'Mở khóa giá trị',
    desc: 'Dùng coin để mở bộ đề và tài liệu nâng cao'
  },
  {
    id: 'excellence',
    label: 'Duy trì streak',
    desc: 'Giữ động lực học tập dài hạn cho học viên'
  }
];
</script>

<template>
  <section id="mission" aria-label="Sứ mệnh" class="panel relative min-h-[700px] lg:h-screen overflow-hidden bg-[oklch(0.09_0.02_282_/_82%)]">
    <div class="absolute inset-0 z-0" aria-hidden="true">
      <div class="bg-parallax absolute inset-0 bg-cover bg-center bg-no-repeat"></div>
      <div class="absolute inset-0 bg-gradient-to-r from-[oklch(0.08_0.02_280)]/95 via-[oklch(0.08_0.02_280)]/88 to-[oklch(0.10_0.04_285)]/70"></div>
      <div class="absolute inset-0 opacity-[0.035] pointer-events-none" :style="{ backgroundImage: 'url(data:image/svg+xml,%3Csvg viewBox=%220 0 600 400%22 xmlns=%22http://www.w3.org/2000/svg%22%3E%3Cfilter id=%22noise%22%3E%3CfeTurbulence type=%22fractalNoise%22 baseFrequency=%220.8%22 numOctaves=%224%22 stitchTiles=%22stitch%22/%3E%3C/filter%3E%3Crect width=%22100%25%22 height=%22100%25%22 filter=%22url(%23noise)%22/%3E%3C/svg%3E)' }"></div>
      <div class="absolute top-0 right-0 w-1/2 h-1/2 bg-gradient-to-bl from-[oklch(0.78_0.18_195)]/12 via-transparent to-transparent"></div>
      <div class="absolute bottom-0 left-0 w-1/3 h-1/3 bg-gradient-to-tr from-[oklch(0.68_0.22_350)]/10 via-transparent to-transparent"></div>
    </div>

    <div class="mission-focus-web" aria-hidden="true">
      <span class="focus-thread focus-thread--one"></span>
      <span class="focus-thread focus-thread--two"></span>
      <span class="focus-thread focus-thread--three"></span>
      <span class="focus-node focus-node--one"></span>
      <span class="focus-node focus-node--two"></span>
      <span class="focus-node focus-node--three"></span>
    </div>

    <div class="relative z-10 h-full flex flex-col lg:flex-row">
      <div class="left-content flex-1 flex flex-col justify-center px-10 lg:px-16 xl:px-20 py-12 opacity-100 visible">
        <h2 class="section-label font-sans text-xs font-semibold tracking-[0.35em] uppercase text-[oklch(0.82_0.16_195)] mb-6">Mission Core</h2>
        <p class="font-sans font-bold text-[clamp(24px,4vw,42px)] leading-[1.35] tracking-[-0.01em] text-white [text-shadow:0_0_24px_oklch(0.78_0.18_195/0.35)] mb-6">
          {{ missionText }}
        </p>
      </div>

      <div class="right-content w-full lg:w-[45%] xl:w-[42%] bg-[oklch(0.12_0.025_280)]/55 backdrop-blur-md border-l border-[oklch(0.78_0.18_195)]/15 flex flex-col justify-center px-10 lg:px-14 xl:px-16 py-12">
        <div class="right-inner opacity-100 visible">
          <h2 class="section-label font-sans text-xs font-semibold tracking-[0.35em] uppercase text-[oklch(0.72_0.22_330)] mb-6 block">Workspace Orbit</h2>
          <p class="font-sans text-[clamp(16px,2.2vw,22px)] text-[oklch(0.96_0.01_240)]/85 leading-[1.7] mb-10">{{ visionText }}</p>

          <div class="space-y-5">
            <div class="flex items-center gap-4">
              <span class="w-12 h-px bg-gradient-to-r from-[oklch(0.78_0.18_195)] to-transparent" aria-hidden="true"></span>
              <h3 class="sr-only">Các trụ cột triển khai</h3>
              <span class="font-sans text-xs font-semibold tracking-[0.3em] uppercase text-[oklch(0.82_0.16_195)]" aria-hidden="true">Các module quỹ đạo</span>
            </div>
            <div role="region" aria-label="Các trụ cột triển khai" class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div v-for="value in values" :key="value.id" :aria-label="`${value.label}: ${value.desc}`" class="value-card group bg-[oklch(0.78_0.18_195)]/5 border border-[oklch(0.78_0.18_195)]/15 p-4 transition-all duration-400 hover:bg-[oklch(0.78_0.18_195)]/10 hover:border-[oklch(0.78_0.18_195)]/35 cursor-default rounded-sm opacity-100 visible">
                <span class="font-sans text-sm font-bold text-[oklch(0.96_0.01_240)] block mb-1.5 group-hover:text-[oklch(0.82_0.16_195)] transition-colors duration-300">{{ value.label }}</span>
                <span class="font-sans text-xs text-[oklch(0.7_0.03_240)]/80 leading-relaxed">{{ value.desc }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="absolute bottom-10 left-10 lg:left-16 z-20 flex items-center gap-4">
      <div class="w-12 h-12 rounded-full border border-[oklch(0.78_0.18_195)]/35 flex items-center justify-center bg-[oklch(0.09_0.02_282)]/50 backdrop-blur-md">
        <div class="w-3 h-3 rounded-full bg-[oklch(0.78_0.18_195)] shadow-[0_0_15px_oklch(0.78_0.18_195/0.6)]"></div>
      </div>
      <div>
        <span class="font-sans text-sm font-semibold tracking-widest text-[oklch(0.96_0.01_240)]/80 uppercase block leading-none mb-1">Gamified Study Hub</span>
        <span class="font-sans text-[10px] tracking-widest text-[oklch(0.7_0.03_240)]/60 uppercase">Vault - Mission - Coin - Unlock</span>
      </div>
    </div>

    <div class="absolute top-1/2 right-6 lg:right-10 z-20 hidden lg:flex flex-col items-center gap-3" aria-hidden="true">
      <span class="writing-vertical font-sans text-[10px] font-medium tracking-[0.25em] text-[oklch(0.7_0.03_240)]/20 uppercase rotate-180">Mission Core</span>
      <div class="w-px h-16 bg-gradient-to-b from-[oklch(0.78_0.18_195)]/20 to-transparent"></div>
    </div>
  </section>
</template>

<style scoped>
.bg-parallax {
  background-image: url('/images/study-hub-background.webp');
  filter: saturate(0.85) brightness(0.36) hue-rotate(175deg);
  opacity: 0.5;
}

.mission-focus-web {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}

.mission-focus-web::before {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.16;
  background-image:
    linear-gradient(oklch(0.82 0.16 195 / 10%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.84 0.16 85 / 7%) 1px, transparent 1px);
  background-size: 84px 84px;
  mask-image: linear-gradient(to right, transparent, black 18%, black 84%, transparent);
  animation: missionGridShift 18s linear infinite;
}

.focus-thread {
  position: absolute;
  width: min(52vw, 760px);
  height: 1px;
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 50%), oklch(0.84 0.16 85 / 32%), transparent);
  box-shadow: 0 0 20px oklch(0.82 0.16 195 / 20%);
  transform: rotate(-24deg);
  animation: missionThreadFlow 10s ease-in-out infinite;
}

.focus-thread--one {
  top: 20%;
  left: -15%;
}

.focus-thread--two {
  top: 46%;
  right: -24%;
  animation-delay: -3s;
  animation-duration: 14s;
}

.focus-thread--three {
  bottom: 18%;
  left: 18%;
  animation-delay: -6s;
  animation-duration: 16s;
  background: linear-gradient(90deg, transparent, oklch(0.68 0.22 330 / 34%), oklch(0.78 0.16 150 / 34%), transparent);
}

.focus-node {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 1px solid oklch(0.82 0.16 195 / 62%);
  border-radius: 999px;
  background: oklch(0.08 0.02 280 / 58%);
  box-shadow: 0 0 18px oklch(0.82 0.16 195 / 46%);
  animation: missionNodePulse 3.4s ease-in-out infinite;
}

.focus-node--one {
  top: 26%;
  left: 44%;
}

.focus-node--two {
  top: 58%;
  right: 18%;
  border-color: oklch(0.84 0.16 85 / 56%);
  box-shadow: 0 0 18px oklch(0.84 0.16 85 / 36%);
  animation-delay: -1.1s;
}

.focus-node--three {
  bottom: 24%;
  left: 18%;
  border-color: oklch(0.78 0.16 150 / 56%);
  box-shadow: 0 0 18px oklch(0.78 0.16 150 / 36%);
  animation-delay: -2.2s;
}

.writing-vertical {
  writing-mode: vertical-rl;
}

.value-card {
  position: relative;
  overflow: hidden;
  will-change: transform, opacity;
}

.value-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, oklch(0.82 0.16 195 / 16%), transparent);
  transform: translateX(-115%);
  transition: transform 0.6s ease;
}

.value-card:hover::after,
.value-card:focus-visible::after {
  transform: translateX(115%);
}

.value-card > * {
  position: relative;
  z-index: 1;
}

@keyframes missionGridShift {
  from {
    background-position: 0 0, 0 0;
  }

  to {
    background-position: 0 168px, 168px 0;
  }
}

@keyframes missionThreadFlow {
  0% {
    translate: -14vw 0;
    opacity: 0;
  }

  20%,
  78% {
    opacity: 1;
  }

  100% {
    translate: 88vw 0;
    opacity: 0;
  }
}

@keyframes missionNodePulse {
  0%,
  100% {
    opacity: 0.55;
    transform: scale(0.88);
  }

  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

@media (max-width: 1024px) {
  .section-label {
    font-size: 11px;
    letter-spacing: 0.3em !important;
  }
}

@media (max-width: 768px) {
  .absolute.bottom-10 {
    bottom: 1.5rem;
    left: 1.5rem;
  }

  .font-sans.text-sm {
    font-size: 0.875rem;
  }

  .font-sans.text-\[10px\] {
    font-size: 9px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .mission-focus-web::before,
  .focus-thread,
  .focus-node {
    animation: none;
  }
}
</style>
