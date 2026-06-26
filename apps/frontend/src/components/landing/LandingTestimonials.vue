<script setup lang="ts">
import { inject, nextTick, onMounted, onUnmounted } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import FlowParticles from '@/components/ui/FlowParticles.vue';

interface Testimonial {
  id: string;
  initials: string;
  quote: string;
  name: string;
  role: string;
  accent: string;
}

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);

let cleanupFns: Array<() => void> = [];

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

const testimonials: Testimonial[] = [
  {
    id: 'hn',
    initials: 'HN',
    quote:
      'Trước đây tụi mình gửi tài liệu qua chat rồi lạc mất. Giờ mọi thứ nằm gọn trong Vault, ai cũng biết phải ôn gì hôm nay.',
    name: 'Hoàng Nam',
    role: 'Trưởng nhóm luyện thi IELTS',
    accent: '#22e0d6'
  },
  {
    id: 'th',
    initials: 'TH',
    quote:
      'Cơ chế coin và streak khiến học viên tự giác hẳn. Bảng xếp hạng tạo không khí thi đua rất lành mạnh.',
    name: 'Thu Hà',
    role: 'Gia sư Toán cấp 3',
    accent: '#9b5cff'
  },
  {
    id: 'qb',
    initials: 'QB',
    quote:
      'Mình quản 4 lớp cùng lúc. Giao nhiệm vụ theo tài liệu giúp mình tiết kiệm hàng giờ mỗi tuần.',
    name: 'Quốc Bảo',
    role: 'Trung tâm tiếng Nhật Sakura',
    accent: '#e879f9'
  }
];

const communityNames = [
  'CLB Bách Khoa',
  'Nhóm IELTS 7.0+',
  'Lớp Toán Thầy Bảo',
  'Trung tâm Sakura',
  'Đội tuyển HSG Lý',
  'Nhóm tự học Y khoa'
];

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.testi-card'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.batchReveal?.('.testi-card', {
      trigger: '#testimonials',
      stagger: 0.12,
      y: 24
    }));

    document.querySelectorAll('.testi-card').forEach((card) => {
      registerCleanup(animation?.hoverScale?.(card as HTMLElement, { scale: 1.02, duration: 0.25 }));
    });
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => {
    if (fn && typeof fn === 'function') fn();
  });
  cleanupFns = [];
});
</script>

<template>
  <section id="testimonials" class="testi-section" aria-labelledby="testimonials-title">
    <div class="testi-effects" aria-hidden="true">
      <span class="testi-glow testi-glow--one"></span>
      <span class="testi-glow testi-glow--two"></span>
      <FlowParticles
        :count="6"
        color="#22e0d6"
        speed="slow"
        direction="right"
        size="small"
        :opacity="0.2"
        aria-hidden="true"
      />
    </div>

    <div class="testi-shell">
      <header class="testi-intro">
        <h2 id="testimonials-title" class="cosmic-gradient-text">Cộng đồng học tập nói gì</h2>
        <span class="cosmic-badge">Câu chuyện minh hoạ</span>
        <p class="testi-sub">
          Những câu chuyện minh hoạ về cách GSH thay đổi cách các nhóm học cùng nhau.
        </p>
      </header>

      <div class="testi-grid">
        <article
          v-for="item in testimonials"
          :key="item.id"
          class="testi-card cosmic-glass cosmic-glass-hover"
          :style="{ '--accent': item.accent }"
        >
          <header class="testi-card__head">
            <span class="testi-avatar" aria-hidden="true">{{ item.initials }}</span>
            <span class="testi-stars glow-cyan" aria-label="Đánh giá 5 trên 5 sao">
              <span aria-hidden="true">★★★★★</span>
            </span>
          </header>

          <blockquote class="testi-quote">{{ item.quote }}</blockquote>

          <cite class="testi-cite">{{ item.name }} · {{ item.role }}</cite>
        </article>
      </div>

      <div class="testi-marquee" aria-hidden="true">
        <div class="testi-marquee__track">
          <span v-for="name in communityNames" :key="`a-${name}`" class="testi-marquee__item">{{ name }}</span>
          <span v-for="name in communityNames" :key="`b-${name}`" class="testi-marquee__item">{{ name }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.testi-section {
  position: relative;
  overflow: hidden;
  padding: clamp(3rem, 7vw, 6rem) 1.5rem;
  color: oklch(0.96 0.01 240);
  background:
    linear-gradient(160deg, oklch(0.08 0.02 280 / 66%), oklch(0.11 0.03 270 / 62%)),
    transparent;
  isolation: isolate;
}

.testi-effects {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.testi-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(90px);
  opacity: 0.5;
}

.testi-glow--one {
  top: -160px;
  left: -120px;
  width: 480px;
  height: 480px;
  background: radial-gradient(circle, oklch(0.68 0.22 300 / 38%), transparent 70%);
}

.testi-glow--two {
  bottom: -200px;
  right: -140px;
  width: 560px;
  height: 560px;
  background: radial-gradient(circle, oklch(0.82 0.16 195 / 30%), transparent 70%);
}

.testi-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  margin: 0 auto;
}

.testi-intro {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
  margin-bottom: clamp(2.5rem, 5vw, 4rem);
}

.testi-intro h2 {
  margin: 0;
  font-size: clamp(2rem, 4.4vw, 3.4rem);
  line-height: 1.08;
}

.cosmic-badge {
  display: inline-flex;
  align-items: center;
  font-size: 0.66rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  padding: 0.35rem 0.7rem;
  border: 1px solid oklch(0.78 0.18 195 / 35%);
  border-radius: 999px;
  background: oklch(0.82 0.16 195 / 10%);
  color: oklch(0.82 0.16 195);
}

.testi-sub {
  margin: 0;
  max-width: 620px;
  color: oklch(0.7 0.03 240);
  font-size: clamp(1rem, 1.5vw, 1.15rem);
  line-height: 1.7;
}

.testi-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(1.25rem, 2.5vw, 2rem);
}

.testi-card {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: clamp(1.5rem, 2.5vw, 2rem);
  border-radius: 16px;
}

.testi-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.testi-avatar {
  display: grid;
  place-items: center;
  width: 3.25rem;
  height: 3.25rem;
  border-radius: 999px;
  font-size: 1.05rem;
  font-weight: 800;
  letter-spacing: 0.04em;
  color: oklch(0.08 0.02 280);
  background: linear-gradient(135deg, var(--accent), oklch(0.84 0.16 85));
  box-shadow: 0 0 24px color-mix(in oklch, var(--accent), transparent 60%);
}

.testi-stars {
  font-size: 1.05rem;
  letter-spacing: 0.12em;
  color: oklch(0.84 0.16 85);
}

.testi-quote {
  margin: 0;
  flex: 1;
  color: oklch(0.96 0.01 240);
  font-size: clamp(1rem, 1.4vw, 1.1rem);
  line-height: 1.7;
}

.testi-quote::before {
  content: '“';
  color: var(--accent);
  font-size: 1.4em;
  font-weight: 800;
  line-height: 0;
  margin-right: 0.1em;
}

.testi-cite {
  font-style: normal;
  font-size: 0.92rem;
  font-weight: 600;
  color: oklch(0.7 0.03 240);
  padding-top: 0.5rem;
  border-top: 1px solid oklch(0.96 0.01 240 / 8%);
}

.testi-marquee {
  position: relative;
  margin-top: clamp(2.5rem, 5vw, 4rem);
  overflow: hidden;
  border-top: 1px solid oklch(0.7 0.1 280 / 14%);
  border-bottom: 1px solid oklch(0.7 0.1 280 / 14%);
  padding: 1.1rem 0;
}

.testi-marquee::before,
.testi-marquee::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 2;
  width: min(16vw, 140px);
  pointer-events: none;
}

.testi-marquee::before {
  left: 0;
  background: linear-gradient(90deg, oklch(0.08 0.02 280), transparent);
}

.testi-marquee::after {
  right: 0;
  background: linear-gradient(270deg, oklch(0.11 0.03 270), transparent);
}

.testi-marquee__track {
  display: flex;
  width: max-content;
  gap: 3rem;
  white-space: nowrap;
  animation: testiMarqueeMove 32s linear infinite;
}

.testi-marquee:hover .testi-marquee__track {
  animation-play-state: paused;
}

.testi-marquee__item {
  display: inline-flex;
  align-items: center;
  gap: 3rem;
  font-size: clamp(1rem, 1.8vw, 1.4rem);
  font-weight: 700;
  color: oklch(0.88 0.04 240 / 70%);
}

.testi-marquee__item::after {
  content: '';
  width: 0.4rem;
  height: 0.4rem;
  border-radius: 999px;
  background: linear-gradient(135deg, oklch(0.82 0.16 195), oklch(0.68 0.22 300));
  box-shadow: 0 0 14px oklch(0.82 0.16 195 / 45%);
}

@keyframes testiMarqueeMove {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(-50%);
  }
}

@media (max-width: 640px) {
  .testi-section {
    padding: clamp(3rem, 7vw, 6rem) 1rem;
  }

  .testi-grid {
    grid-template-columns: 1fr;
  }

  .testi-marquee__track,
  .testi-marquee__item {
    gap: 2rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .testi-marquee__track {
    animation: none;
    transform: translateX(0);
  }
}
</style>
