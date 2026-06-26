<script setup lang="ts">
import { inject, nextTick, onMounted, onUnmounted } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import FlowParticles from '@/components/ui/FlowParticles.vue';

interface TierRow {
  label: string;
  value: string;
}

interface Tier {
  id: string;
  name: string;
  rows: TierRow[];
  cta: string;
  featured: boolean;
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

const tiers: Tier[] = [
  {
    id: 'starter',
    name: 'Khởi đầu',
    featured: false,
    cta: 'Bắt đầu miễn phí',
    rows: [
      { label: 'Đối tượng', value: 'Nhóm học nhỏ' },
      { label: 'Workspace', value: '1' },
      { label: 'Thành viên', value: '30' },
      { label: 'Coin thưởng/tháng', value: 'Cơ bản' },
      { label: 'Mở khóa VIP', value: '—' },
      { label: 'Bảng xếp hạng & thành tích', value: '✓' },
      { label: 'Hỗ trợ', value: 'Cộng đồng' }
    ]
  },
  {
    id: 'pro',
    name: 'Pro',
    featured: true,
    cta: 'Dùng thử Pro',
    rows: [
      { label: 'Đối tượng', value: 'CLB / gia sư' },
      { label: 'Workspace', value: '5' },
      { label: 'Thành viên', value: '200' },
      { label: 'Coin thưởng/tháng', value: 'Nâng cao' },
      { label: 'Mở khóa VIP', value: '✓' },
      { label: 'Bảng xếp hạng & thành tích', value: '✓' },
      { label: 'Hỗ trợ', value: 'Ưu tiên' }
    ]
  },
  {
    id: 'community',
    name: 'Cộng đồng',
    featured: false,
    cta: 'Liên hệ tư vấn',
    rows: [
      { label: 'Đối tượng', value: 'Trung tâm / trường' },
      { label: 'Workspace', value: 'Không giới hạn' },
      { label: 'Thành viên', value: 'Tùy chỉnh' },
      { label: 'Coin thưởng/tháng', value: 'Nâng cao + tùy chỉnh' },
      { label: 'Mở khóa VIP', value: '✓' },
      { label: 'Bảng xếp hạng & thành tích', value: '✓' },
      { label: 'Hỗ trợ', value: 'Chuyên biệt' }
    ]
  }
];

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.tier-card'], { opacity: 1, y: 0 });
    return;
  }

  nextTick(() => {
    registerCleanup(animation?.batchReveal?.('.tier-card', {
      trigger: '#vip-tiers',
      stagger: 0.1,
      y: 24
    }));

    registerCleanup(animation?.magneticButton?.('.tier-card--featured .cosmic-btn', {
      strength: 20
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
  <section id="vip-tiers" class="vip-section" aria-labelledby="vip-tiers-title">
    <div class="vip-effects" aria-hidden="true">
      <FlowParticles
        :count="6"
        color="#22e0d6"
        speed="slow"
        direction="right"
        size="small"
        :opacity="0.2"
        aria-hidden="true"
      />
      <span class="vip-glow vip-glow--cyan"></span>
      <span class="vip-glow vip-glow--violet"></span>
    </div>

    <div class="vip-shell">
      <header class="vip-intro">
        <span class="cosmic-badge">Bảng giá</span>
        <h2 id="vip-tiers-title" class="cosmic-gradient-text">
          Chọn gói phù hợp với cộng đồng của bạn
        </h2>
      </header>

      <div class="tier-grid" role="list">
        <article
          v-for="tier in tiers"
          :key="tier.id"
          role="listitem"
          class="tier-card cosmic-glass"
          :class="{ 'tier-card--featured': tier.featured, 'gradient-border': tier.featured }"
        >
          <span v-if="tier.featured" class="cosmic-badge tier-ribbon">Phổ biến</span>

          <h3 class="tier-name">{{ tier.name }}</h3>

          <dl class="tier-rows">
            <div v-for="row in tier.rows" :key="row.label" class="tier-row">
              <dt>{{ row.label }}</dt>
              <dd>{{ row.value }}</dd>
            </div>
          </dl>

          <button type="button" class="cosmic-btn tier-cta">{{ tier.cta }}</button>
        </article>
      </div>

      <p class="vip-footnote">
        Bảng so sánh minh hoạ — tính năng cụ thể có thể thay đổi.
      </p>
    </div>
  </section>
</template>

<style scoped>
.vip-section {
  position: relative;
  overflow: hidden;
  padding: clamp(3rem, 7vw, 6rem) 1.5rem;
  color: var(--workspace-text);
  background:
    linear-gradient(160deg, oklch(0.085 0.024 280 / 66%), oklch(0.11 0.03 270 / 62%)),
    transparent;
  isolation: isolate;
}

.vip-section::before {
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

.vip-effects {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.vip-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(120px);
  opacity: 0.45;
}

.vip-glow--cyan {
  top: -160px;
  left: -120px;
  width: 460px;
  height: 460px;
  background: radial-gradient(circle, oklch(0.82 0.16 195 / 40%), transparent 70%);
}

.vip-glow--violet {
  bottom: -200px;
  right: -140px;
  width: 520px;
  height: 520px;
  background: radial-gradient(circle, oklch(0.68 0.22 300 / 36%), transparent 70%);
}

.vip-shell {
  position: relative;
  z-index: 1;
  width: min(1200px, 100%);
  margin: 0 auto;
}

.vip-intro {
  text-align: center;
  max-width: 760px;
  margin: 0 auto clamp(2.5rem, 5vw, 3.5rem);
}

.cosmic-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  border: 1px solid oklch(0.78 0.18 195 / 35%);
  background: oklch(0.82 0.16 195 / 12%);
  color: var(--workspace-cyan);
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.vip-intro h2 {
  margin: 1.1rem 0 0;
  font-size: clamp(2rem, 4.5vw, 3.4rem);
  line-height: 1.08;
  letter-spacing: -0.01em;
}

.tier-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(1.25rem, 2.5vw, 1.75rem);
  align-items: stretch;
}

.tier-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: clamp(1.5rem, 2.5vw, 2.1rem);
  border-radius: 16px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s ease;
}

.tier-card:hover {
  transform: translateY(-6px);
}

.tier-card--featured {
  z-index: 1;
  box-shadow:
    0 0 0 1px oklch(0.68 0.22 300 / 22%) inset,
    0 28px 70px oklch(0.68 0.22 300 / 22%);
}

@media (min-width: 641px) {
  .tier-card--featured {
    transform: scale(1.035);
  }

  .tier-card--featured:hover {
    transform: scale(1.035) translateY(-6px);
  }
}

.tier-ribbon {
  position: absolute;
  top: -0.85rem;
  left: 50%;
  transform: translateX(-50%);
  border-color: oklch(0.68 0.22 300 / 50%);
  background: oklch(0.68 0.22 300 / 18%);
  color: oklch(0.84 0.12 305);
  box-shadow: 0 8px 24px oklch(0.68 0.22 300 / 30%);
  white-space: nowrap;
}

.tier-name {
  margin: 0 0 1.25rem;
  font-size: clamp(1.35rem, 2.4vw, 1.7rem);
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--workspace-text);
}

.tier-rows {
  display: flex;
  flex-direction: column;
  margin: 0 0 1.75rem;
  flex: 1;
}

.tier-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.7rem 0;
  border-bottom: 1px solid oklch(0.96 0.01 240 / 8%);
}

.tier-row:last-child {
  border-bottom: none;
}

.tier-row dt {
  margin: 0;
  font-size: 0.86rem;
  color: var(--workspace-muted);
  line-height: 1.4;
}

.tier-row dd {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 700;
  text-align: right;
  color: var(--workspace-text);
  line-height: 1.4;
}

.tier-cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-top: auto;
}

.vip-footnote {
  margin: clamp(2rem, 4vw, 2.75rem) 0 0;
  text-align: center;
  font-size: 0.82rem;
  color: var(--workspace-muted);
  line-height: 1.6;
}

@media (max-width: 960px) {
  .tier-grid {
    grid-template-columns: 1fr;
    max-width: 520px;
    margin: 0 auto;
  }
}

@media (max-width: 640px) {
  .vip-section {
    padding: 3.5rem 1rem;
  }

  .tier-card--featured {
    transform: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .tier-card,
  .tier-card:hover,
  .tier-card--featured:hover {
    transform: none;
    transition: none;
  }
}
</style>
