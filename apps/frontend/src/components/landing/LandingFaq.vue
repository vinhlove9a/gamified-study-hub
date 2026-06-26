<script setup lang="ts">
import { inject, nextTick, onMounted, onUnmounted, ref } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import FlowParticles from '@/components/ui/FlowParticles.vue';

interface FaqItem {
  q: string;
  a: string;
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

const openIndex = ref<number>(-1);

const toggle = (i: number) => {
  openIndex.value = openIndex.value === i ? -1 : i;
};

const faqs: FaqItem[] = [
  {
    q: 'GSH dành cho ai?',
    a: 'CLB sinh viên, nhóm luyện thi, lớp gia sư, trung tâm và cả người tự học — bất kỳ ai muốn biến tài liệu thành lộ trình học có động lực.'
  },
  {
    q: 'Tôi tạo workspace như thế nào?',
    a: 'Sau khi đăng ký, bạn đặt tên cho workspace của CLB/nhóm, tải tài liệu lên Vault rồi giao thành nhiệm vụ chỉ trong vài phút.'
  },
  {
    q: 'Coin và streak hoạt động ra sao?',
    a: 'Hoàn thành nhiệm vụ để nhận coin, học đều mỗi ngày để giữ streak. Coin dùng để mở khóa VIP, huy hiệu và phần thưởng trong workspace.'
  },
  {
    q: 'Dữ liệu tài liệu của tôi có an toàn không?',
    a: 'Tài liệu được lưu trong workspace riêng của bạn; bạn kiểm soát ai được xem và tham gia. (Chi tiết chính sách sẽ công bố khi ra mắt.)'
  },
  {
    q: 'GSH có miễn phí không?',
    a: 'Sẽ có gói miễn phí để bắt đầu, cùng các gói nâng cao cho cộng đồng lớn hơn. Thông tin chi tiết sẽ được cập nhật.'
  }
];

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.faq-item'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.batchReveal?.('.faq-item', {
      trigger: '#faq',
      duration: 0.6,
      stagger: 0.08,
      y: 20
    }));
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
  <section id="faq" class="faq-section" aria-labelledby="faq-title">
    <div class="faq-glow" aria-hidden="true">
      <span class="faq-orb faq-orb--cyan"></span>
      <span class="faq-orb faq-orb--violet"></span>
    </div>

    <FlowParticles
      :count="6"
      color="#22e0d6"
      speed="slow"
      direction="right"
      size="small"
      :opacity="0.18"
      aria-hidden="true"
    />

    <div class="faq-shell">
      <header class="faq-head">
        <span class="cosmic-badge">FAQ</span>
        <h2 id="faq-title" class="cosmic-gradient-text">Câu hỏi thường gặp</h2>
        <p>Những điều cần biết trước khi đưa cộng đồng học tập của bạn lên Gamified Study Hub.</p>
      </header>

      <div class="faq-list">
        <article
          v-for="(item, i) in faqs"
          :key="i"
          class="faq-item cosmic-glass"
          :class="{ 'is-open': openIndex === i }"
        >
          <h3 class="faq-q-wrap">
            <button
              :id="`faq-q-${i}`"
              type="button"
              class="faq-q"
              :aria-expanded="openIndex === i"
              :aria-controls="`faq-panel-${i}`"
              @click="toggle(i)"
            >
              <span class="faq-q-text">{{ item.q }}</span>
              <span class="faq-chevron" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
                  <path
                    d="M6 9l6 6 6-6"
                    stroke="currentColor"
                    stroke-width="2.2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </span>
            </button>
          </h3>

          <div
            :id="`faq-panel-${i}`"
            role="region"
            :aria-labelledby="`faq-q-${i}`"
            class="faq-panel"
            :class="{ 'is-open': openIndex === i }"
          >
            <div class="faq-panel-inner">
              <p>{{ item.a }}</p>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.faq-section {
  position: relative;
  overflow: hidden;
  padding: clamp(3rem, 7vw, 6rem) 1.5rem;
  color: var(--workspace-text, oklch(0.96 0.01 240));
  background:
    linear-gradient(160deg, oklch(0.085 0.024 280 / 66%), oklch(0.105 0.03 270 / 62%)),
    transparent;
  isolation: isolate;
}

.faq-section::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  background-image:
    linear-gradient(oklch(0.96 0.01 240 / 3%) 1px, transparent 1px),
    linear-gradient(90deg, oklch(0.96 0.01 240 / 3%) 1px, transparent 1px);
  background-size: 72px 72px;
  mask-image: linear-gradient(to bottom, transparent, black 16%, black 84%, transparent);
}

.faq-glow {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.faq-orb {
  position: absolute;
  width: 460px;
  height: 460px;
  border-radius: 999px;
  filter: blur(120px);
  opacity: 0.4;
}

.faq-orb--cyan {
  top: -140px;
  right: -120px;
  background: radial-gradient(circle, oklch(0.82 0.16 195 / 40%), transparent 70%);
  animation: faqOrbDrift 18s ease-in-out infinite;
}

.faq-orb--violet {
  bottom: -180px;
  left: -140px;
  background: radial-gradient(circle, oklch(0.68 0.22 300 / 36%), transparent 70%);
  animation: faqOrbDrift 22s ease-in-out infinite reverse;
}

.faq-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  margin: 0 auto;
  display: grid;
  gap: clamp(2rem, 4vw, 3rem);
}

.faq-head {
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
}

.cosmic-badge {
  display: inline-flex;
  align-items: center;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: 0.68rem;
  font-weight: 800;
  padding: 0.35rem 0.7rem;
  border: 1px solid oklch(0.78 0.18 195 / 35%);
  border-radius: 999px;
  background: oklch(0.82 0.16 195 / 10%);
  color: var(--workspace-cyan, oklch(0.82 0.16 195));
}

.faq-head h2 {
  margin: 1rem 0 0;
  font-size: clamp(2rem, 5vw, 3.6rem);
  line-height: 1.05;
  letter-spacing: -0.01em;
}

.faq-head p {
  margin: 1rem auto 0;
  max-width: 560px;
  color: var(--workspace-muted, oklch(0.7 0.03 240));
  font-size: clamp(1rem, 1.6vw, 1.15rem);
  line-height: 1.7;
}

.faq-list {
  display: grid;
  gap: 0.85rem;
}

.faq-item {
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.faq-item.is-open {
  border-color: oklch(0.82 0.16 195 / 38%);
  box-shadow: 0 0 38px oklch(0.82 0.16 195 / 16%);
}

.faq-q-wrap {
  margin: 0;
  font-size: inherit;
  font-weight: inherit;
}

.faq-q {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: clamp(1.1rem, 2.2vw, 1.5rem) clamp(1.2rem, 2.6vw, 1.75rem);
  background: transparent;
  border: 0;
  text-align: left;
  cursor: pointer;
  color: var(--workspace-text, oklch(0.96 0.01 240));
  font-family: inherit;
  transition: color 0.25s ease;
}

.faq-q:hover {
  color: var(--workspace-cyan, oklch(0.82 0.16 195));
}

.faq-q:focus-visible {
  outline: 2px solid oklch(0.82 0.16 195 / 70%);
  outline-offset: -3px;
  border-radius: 12px;
}

.faq-q-text {
  font-size: clamp(1.02rem, 1.9vw, 1.22rem);
  font-weight: 700;
  line-height: 1.4;
}

.faq-chevron {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  border: 1px solid oklch(0.78 0.18 195 / 28%);
  color: var(--workspace-cyan, oklch(0.82 0.16 195));
  background: oklch(0.82 0.16 195 / 8%);
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1), background 0.25s ease, border-color 0.25s ease;
}

.faq-item.is-open .faq-chevron {
  transform: rotate(180deg);
  background: oklch(0.82 0.16 195 / 16%);
  border-color: oklch(0.78 0.18 195 / 55%);
}

.faq-panel {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.faq-panel.is-open {
  grid-template-rows: 1fr;
}

.faq-panel-inner {
  overflow: hidden;
  min-height: 0;
}

.faq-panel-inner p {
  margin: 0;
  padding: 0 clamp(1.2rem, 2.6vw, 1.75rem) clamp(1.2rem, 2.4vw, 1.6rem);
  color: var(--workspace-muted, oklch(0.7 0.03 240));
  font-size: clamp(0.95rem, 1.5vw, 1.05rem);
  line-height: 1.75;
}

@keyframes faqOrbDrift {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(28px, -22px, 0) scale(1.08);
  }
}

@media (max-width: 640px) {
  .faq-section {
    padding: 3rem 1rem;
  }

  .faq-orb {
    width: 320px;
    height: 320px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .faq-orb,
  .faq-chevron,
  .faq-panel {
    animation: none;
    transition: none;
  }
}
</style>
