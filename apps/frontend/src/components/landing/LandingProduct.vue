<script setup lang="ts">
import { onMounted, onUnmounted, inject } from 'vue';
import type { AnimationAPI } from '@/types/gsap';

interface Specification {
  parameter: string
  value: string
  unit: string
  icon: string
}

interface ProductFeature {
  id: string
  title: string
  description: string
  icon: string
}

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
let cleanupFns: Array<() => void> = [];

const specifications: Specification[] = [
  { parameter: 'Kho tài liệu', value: '1', unit: 'hệ thống', icon: 'ffa-test' },
  { parameter: 'Tìm kiếm nhanh', value: '24/7', unit: 'truy cập', icon: 'moisture' },
  { parameter: 'Nhiệm vụ', value: 'N', unit: 'không giới hạn', icon: 'iodine' },
  { parameter: 'Điểm thưởng', value: 'Coin', unit: 'minh bạch', icon: 'melting-point' },
  { parameter: 'Mở khóa VIP', value: 'Coin', unit: 'thay tiền', icon: 'peroxide' },
  { parameter: 'Workspace', value: 'White-label', unit: 'riêng', icon: 'cloud-point' }
]

const features: ProductFeature[] = [
  {
    id: 'quality',
    title: 'Kho tài liệu thông minh',
    description: 'Sắp xếp tài liệu theo workspace, danh mục, thẻ và trạng thái hiển thị.',
    icon: 'quality-badge'
  },
  {
    id: 'traceability',
    title: 'Tìm kiếm nhanh',
    description: 'Giúp học viên tìm đúng tài liệu thay vì lạc trong hàng trăm file và folder.',
    icon: 'traceability'
  },
  {
    id: 'capacity',
    title: 'Nhiệm vụ học tập',
    description: 'Tạo các nhiệm vụ như điểm danh, hoàn thành bài tập, đóng góp tài liệu hoặc tham gia hoạt động.',
    icon: 'scale-capacity'
  },
  {
    id: 'certification',
    title: 'Ví coin học viên',
    description: 'Mỗi học viên có số dư coin riêng để ghi nhận nỗ lực và tiến độ.',
    icon: 'certification-badge'
  },
  {
    id: 'unlock',
    title: 'Mở khóa tài liệu VIP',
    description: 'Cho phép học viên dùng coin kiếm được để mở tài liệu nâng cao hoặc bộ đề giá trị.',
    icon: 'traceability'
  },
  {
    id: 'whitelabel',
    title: 'White-label workspace',
    description: 'Tùy biến logo, màu sắc và nhận diện cho từng CLB, gia sư hoặc trung tâm.',
    icon: 'quality-badge'
  }
]

const getIconPath = (iconName: string): string => {
  const icons: Record<string, string> = {
    'ffa-test': '/icons/product/ffa-test.svg',
    'moisture': '/icons/product/moisture.svg',
    'iodine': '/icons/product/iodine.svg',
    'melting-point': '/icons/product/melting-point.svg',
    'peroxide': '/icons/product/peroxide.svg',
    'cloud-point': '/icons/product/cloud-point.svg',
    'quality-badge': '/icons/product/quality-badge.svg',
    'traceability': '/icons/product/traceability.svg',
    'scale-capacity': '/icons/product/scale-capacity.svg',
    'certification-badge': '/icons/product/certification-badge.svg'
  }
  return icons[iconName] || ''
}

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.product-left', '.product-right', '.spec-card', '.feature-card'], { opacity: 1, y: 0 });
    return;
  }

  animation?.batchReveal?.('.product-left', {
    trigger: '#product',
    duration: 0.8,
    y: 30
  });

  animation?.batchReveal?.('.section-label', {
    trigger: '#product',
    duration: 0.5,
    y: 20
  });

  animation?.batchReveal?.('.product-title', {
    trigger: '#product',
    duration: 0.6,
    y: 24
  });

  animation?.batchReveal?.('.product-description', {
    trigger: '#product',
    duration: 0.5,
    y: 20
  });

  animation?.batchReveal?.('.capacity-badge', {
    trigger: '#product',
    duration: 0.7,
    y: 20
  });

  animation?.batchReveal?.('.cert-row', {
    trigger: '#product',
    duration: 0.5,
    stagger: 0.08,
    y: 20
  });

  animation?.batchReveal?.('.spec-card', {
    trigger: '#product',
    duration: 0.5,
    stagger: 0.08,
    y: 30
  });

  animation?.batchReveal?.('.feature-card', {
    trigger: '#product',
    duration: 0.5,
    stagger: 0.1,
    y: 30
  });

  document.querySelectorAll('.spec-card').forEach((card) => {
    const cleanup = animation?.hoverScale?.(card as HTMLElement, { scale: 1.02, duration: 0.3 });
    if (cleanup) cleanupFns.push(cleanup);
  });

  document.querySelectorAll('.feature-card').forEach((card) => {
    const cleanup = animation?.hoverScale?.(card as HTMLElement, { scale: 1.03, duration: 0.3 });
    if (cleanup) cleanupFns.push(cleanup);
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn && typeof fn === 'function' && fn());
});
</script>

<template>
  <section id="product" class="product-section panel">
    <div class="product-container">
      <div class="product-left">
        <div class="left-background">
          <div class="bg-noise"></div>
          <div class="bg-gradient-radial"></div>
          <div class="bg-accent-glow"></div>
          <div class="diagonal-overlay"></div>
        </div>

        <div class="left-content">
          <div class="product-visual">
            <svg viewBox="0 0 64 64" class="tank-icon" fill="none" aria-hidden="true">
              <rect x="12" y="20" width="40" height="38" rx="2" stroke="currentColor" stroke-width="2.5" />
              <ellipse cx="32" cy="20" rx="20" ry="6" stroke="currentColor" stroke-width="2.5" />
              <rect x="28" y="26" width="8" height="24" rx="1" stroke="currentColor" stroke-width="1.5" />
              <rect x="30" y="32" width="4" height="16" fill="currentColor" opacity="0.3" />
              <path d="M12 12H4V16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
              <path d="M52 52H60V48" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
              <circle cx="48" cy="14" r="5" stroke="currentColor" stroke-width="1.5" />
              <path d="M48 14L50 11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              <path d="M4 58H60" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
          </div>

          <div class="capacity-badge">
            <div class="capacity-icon">
              <svg viewBox="0 0 64 64" fill="none" aria-hidden="true">
                <rect x="8" y="36" width="10" height="20" rx="1" stroke="currentColor" stroke-width="2" />
                <rect x="22" y="28" width="10" height="28" rx="1" stroke="currentColor" stroke-width="2" />
                <rect x="36" y="16" width="10" height="40" rx="1" stroke="currentColor" stroke-width="2" />
                <rect x="50" y="8" width="10" height="48" rx="1" stroke="currentColor" stroke-width="2" />
                <path d="M4 56L20 44L36 32L52 16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
                <path d="M48 16L52 16L52 20" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
              </svg>
            </div>
            <div class="capacity-value">
              <span class="value-number">VIP</span>
              <span class="value-unit">UNLOCK</span>
            </div>
            <div class="capacity-label">Mở khóa tài liệu VIP</div>
          </div>

          <div class="cert-row">
            <a href="#" class="cert-badge" aria-label="Mở khóa tài liệu VIP">
              <svg viewBox="0 0 64 64" fill="none" aria-hidden="true">
                <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2.5" />
                <path d="M32 12C32 12 24 20 20 28C20 28 24 24 32 24C40 24 44 28 44 28C40 20 32 12 32 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                <path d="M32 24C32 24 26 30 26 36C26 36 32 36 32 36C32 36 38 36 38 36C38 30 32 24 32 24Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                <path d="M20 46H44" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                <path d="M22 50H42" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              </svg>
              <span class="cert-text">
                <span class="cert-name">VIP</span>
                <span class="cert-status">Unlock</span>
              </span>
            </a>
            <a href="#" class="cert-badge" aria-label="White-label workspace">
              <svg viewBox="0 0 64 64" fill="none" aria-hidden="true">
                <path d="M32 8L8 16V32C8 46 20 56 32 58C44 56 56 46 56 32V16L32 8Z" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
                <path d="M32 20C32 20 22 28 22 36C22 42 28 46 32 46C36 46 42 42 42 36C42 28 32 20 32 20Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                <ellipse cx="32" cy="36" rx="12" ry="8" stroke="currentColor" stroke-width="1.5" />
              </svg>
              <span class="cert-text">
                <span class="cert-name">White-label</span>
                <span class="cert-status">Workspace</span>
              </span>
            </a>
          </div>
        </div>
      </div>

      <div class="product-right">
        <div class="right-content">
          <span class="section-label">Tính năng cốt lõi</span>

          <h2 class="product-title">
            Tất cả những gì cần
            <span class="title-accent">để vận hành một cổng tài liệu học tập.</span>
          </h2>

          <p class="product-description">
            Từ lưu trữ tài liệu, tìm kiếm, nhiệm vụ, coin đến mở khóa tài liệu VIP - mọi thứ được thiết kế cho các cộng đồng học tập nhỏ muốn chuyên nghiệp hóa trải nghiệm học viên.
          </p>

          <div class="specs-section">
            <h3 class="specs-title">Nhóm chức năng</h3>
            <div class="specs-grid">
              <div
                v-for="spec in specifications"
                :key="spec.parameter"
                class="spec-card"
                tabindex="0"
                :aria-label="`${spec.parameter}: ${spec.value} ${spec.unit}`"
                role="article"
              >
                <div class="spec-icon">
                  <img :src="getIconPath(spec.icon)" :alt="spec.parameter" />
                </div>
                <div class="spec-content">
                  <span class="spec-parameter">{{ spec.parameter }}</span>
                  <div class="spec-value-row">
                    <span class="spec-value">{{ spec.value }}</span>
                    <span class="spec-unit">{{ spec.unit }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="features-section">
            <h3 class="features-title">Tính năng sản phẩm</h3>
            <div class="features-grid">
              <div
                v-for="feature in features"
                :key="feature.id"
                class="feature-card"
                tabindex="0"
                :aria-label="`${feature.title}: ${feature.description}`"
                role="article"
              >
                <div class="feature-icon">
                  <img :src="getIconPath(feature.icon)" :alt="feature.title" />
                </div>
                <h4 class="feature-title">{{ feature.title }}</h4>
                <p class="feature-description">{{ feature.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.product-section {
  width: 100%;
  min-height: 100vh;
  height: auto;
  overflow: hidden;
}

.product-container {
  display: flex;
  width: 100%;
  min-height: 100vh;
  height: auto;
  position: relative;
}

.product-left {
  width: 45%;
  min-height: 100vh;
  height: auto;
  position: relative;
  background: #245246;
  clip-path: polygon(0 0, calc(100% - 80px) 0, 100% 100%, 0 100%);
}

.left-background {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-noise {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  opacity: 0.025;
  pointer-events: none;
}

.bg-gradient-radial {
  position: absolute;
  top: -30%;
  right: -20%;
  width: 100%;
  height: 100%;
  background: radial-gradient(ellipse at 30% 50%, rgba(196, 91, 40, 0.12) 0%, transparent 50%);
}

.bg-accent-glow {
  position: absolute;
  bottom: -20%;
  left: -10%;
  width: 80%;
  height: 80%;
  background: radial-gradient(ellipse at 30% 50%, rgba(230, 184, 77, 0.1) 0%, transparent 50%);
}

.diagonal-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(196, 91, 40, 0.05) 0%,
    transparent 50%,
    rgba(36, 82, 70, 0.05) 100%
  );
}

.left-content {
  position: relative;
  z-index: 10;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 2rem 2rem 2rem 3rem;
}

.product-visual {
  margin-bottom: 1.5rem;
}

.tank-icon {
  width: 80px;
  height: 80px;
  color: #e6b84d;
  opacity: 0.6;
}

.capacity-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.375rem;
  margin-bottom: 1.5rem;
  padding: 1rem 1.5rem;
  background: rgba(245, 240, 232, 0.05);
  border: 1px solid rgba(230, 184, 77, 0.2);
  border-left: 3px solid #e6b84d;
}

.capacity-icon {
  width: 40px;
  height: 40px;
  margin-bottom: 0.375rem;
}

.capacity-icon svg {
  width: 100%;
  height: 100%;
  color: #e6b84d;
}

.capacity-value {
  display: flex;
  align-items: baseline;
  gap: 0.375rem;
}

.value-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: #f5f0e8;
  line-height: 1;
  font-family: 'Jakarta Sans', sans-serif;
}

.value-unit {
  font-size: 1rem;
  font-weight: 600;
  color: #e6b84d;
}

.capacity-label {
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: rgba(245, 240, 232, 0.6);
}

.cert-row {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.cert-badge {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(245, 240, 232, 0.03);
  border: 1px solid rgba(245, 240, 232, 0.1);
  border-radius: 6px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.cert-badge:hover {
  background: rgba(245, 240, 232, 0.08);
  border-color: rgba(230, 184, 77, 0.3);
  transform: translateX(4px);
}

.cert-badge:focus-visible {
  outline: 3px solid #d4a24c;
  outline-offset: 2px;
  background: rgba(245, 240, 232, 0.08);
  border-color: rgba(230, 184, 77, 0.4);
}

.cert-badge svg {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  color: #e6b84d;
}

.cert-text {
  display: flex;
  flex-direction: column;
}

.cert-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #f5f0e8;
}

.cert-status {
  font-size: 0.65rem;
  color: rgba(245, 240, 232, 0.5);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.product-right {
  width: 55%;
  min-height: 100vh;
  height: auto;
  background: #f5f0e8;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 2rem 2.5rem 2rem 2.5rem;
}

.right-content {
  max-width: 100%;
}

.section-label {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.25em;
  color: #c45b28;
  margin-bottom: 0.75rem;
}

.product-title {
  font-size: clamp(1.75rem, 3vw, 2.25rem);
  font-weight: 700;
  color: #2c2416;
  line-height: 1.15;
  margin-bottom: 1rem;
  font-family: 'Jakarta Sans', sans-serif;
}

.title-accent {
  color: #c45b28;
}

.product-description {
  font-size: 0.95rem;
  line-height: 1.6;
  color: rgba(44, 36, 22, 0.75);
  margin-bottom: 1.5rem;
  max-width: 90%;
}

.specs-section {
  margin-bottom: 1.25rem;
}

.specs-title {
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: #2c2416;
  margin-bottom: 0.75rem;
  opacity: 0.8;
}

.specs-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.spec-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: #faf7f2;
  border: 1px solid rgba(44, 36, 22, 0.08);
  border-radius: 6px;
  transition: all 0.3s ease;
}

.spec-card:hover {
  border-color: rgba(196, 91, 40, 0.25);
  box-shadow: 0 8px 24px rgba(44, 36, 22, 0.08);
}

.spec-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(196, 91, 40, 0.08);
  border-radius: 6px;
}

.spec-icon img {
  width: 20px;
  height: 20px;
}

.spec-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.spec-parameter {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(44, 36, 22, 0.5);
  font-weight: 500;
}

.spec-value-row {
  display: flex;
  align-items: baseline;
  gap: 0.375rem;
}

.spec-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c2416;
  font-family: 'Jakarta Sans', sans-serif;
}

.spec-unit {
  font-size: 0.75rem;
  font-weight: 600;
  color: #c45b28;
}

.features-section {
  margin-top: 1rem;
}

.features-title {
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: #2c2416;
  margin-bottom: 0.75rem;
  opacity: 0.8;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}

.feature-card {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  background: #2c2416;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: default;
}

.feature-card:hover {
  background: #3a3025;
}

.feature-card:focus-visible {
  outline: 3px solid #d4a24c;
  outline-offset: 2px;
  background: #3a3025;
}

.feature-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(196, 91, 40, 0.15);
  border-radius: 6px;
  margin-bottom: 0.75rem;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-icon {
  background: #c45b28;
}

.feature-icon img {
  width: 22px;
  height: 22px;
}

.feature-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #f5f0e8;
  margin-bottom: 0.375rem;
  font-family: 'Jakarta Sans', sans-serif;
}

.feature-description {
  font-size: 0.75rem;
  line-height: 1.5;
  color: rgba(245, 240, 232, 0.6);
}

@media (max-width: 1200px) {
  .product-left {
    clip-path: polygon(0 0, calc(100% - 60px) 0, 100% 100%, 0 100%);
  }

  .left-content {
    padding: 3rem 2rem 3rem 3rem;
  }

  .product-right {
    padding: 3rem 3rem 3rem 2rem;
  }

  .specs-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .product-section {
    min-height: 100vh;
  }

  .product-container {
    flex-direction: column;
  }

  .product-left {
    width: 100%;
    height: auto;
    min-height: 400px;
    clip-path: polygon(0 0, 100% 0, 100% calc(100% - 40px), 0 100%);
  }

  .product-right {
    width: 100%;
    height: auto;
    padding: 3rem 2rem;
  }

  .left-content {
    padding: 2rem;
    justify-content: center;
  }

  .product-visual {
    display: none;
  }

  .capacity-badge {
    flex-direction: row;
    align-items: center;
    gap: 1.5rem;
    padding: 1rem 1.5rem;
  }

  .cert-row {
    flex-direction: row;
  }

  .cert-badge {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .product-left {
    height: 45vh;
    min-height: 350px;
    clip-path: none;
  }

  .product-right {
    padding: 2rem 1.5rem;
  }

  .left-content {
    padding: 1.5rem;
  }

  .capacity-badge {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .value-number {
    font-size: 2.5rem;
  }

  .cert-row {
    flex-direction: column;
  }

  .cert-badge {
    width: 100%;
  }

  .specs-grid {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .product-description {
    font-size: 1rem;
  }

  .product-title {
    font-size: 2rem;
  }
}

@media (max-width: 480px) {
  .product-left {
    height: 40vh;
    min-height: 300px;
  }

  .capacity-badge {
    padding: 1rem;
  }

  .value-number {
    font-size: 2rem;
  }

  .capacity-label {
    font-size: 0.75rem;
  }

  .feature-card {
    padding: 1.25rem;
  }

  .spec-card {
    padding: 0.875rem 1rem;
  }

  .spec-value {
    font-size: 1.125rem;
  }
}
</style>
