<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, inject } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import { useKeyboardNavigation } from '@/composables/useKeyboardNavigation';
import { useFocusManagement } from '@/composables/useFocusManagement';
import { announceToScreenReader } from '@/utils/accessibility';

interface Milestone {
  year: string;
  title: string;
  description: string;
  details: string[];
}

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
const { handleArrowKeys, handleEnterOrSpace } = useKeyboardNavigation();
const { saveFocus, returnFocus } = useFocusManagement();

const activeNode = ref<number | null>(null);
const expandedNodes = ref<number[]>([]);
const visibleNodes = ref<number[]>([]);
const milestoneRefs = ref<(HTMLElement | null)[]>([]);
const focusedMilestoneIndex = ref<number | null>(null);

const cleanupFns: Array<() => void> = [];

const registerCleanup = (item: unknown) => {
  if (!item) return;
  if (Array.isArray(item)) {
    item.forEach(registerCleanup);
    return;
  }
  if (typeof (item as { kill?: () => void }).kill === 'function') {
    cleanupFns.push(() => (item as { kill: () => void }).kill());
  }
};

const milestones: Milestone[] = [
  {
    year: '01',
    title: 'Thư mục ngày càng lộn xộn',
    description: 'Tài liệu nằm rải rác trong nhiều folder, tên file không thống nhất và rất khó tìm lại.',
    details: [
      'Nhiều folder trùng nội dung',
      'Khó theo dõi phiên bản tài liệu',
      'Mất thời gian tìm lại tài liệu đúng'
    ]
  },
  {
    year: '02',
    title: 'Học viên thiếu động lực quay lại',
    description: 'Việc tải tài liệu thường chỉ diễn ra một lần. Không có nhiệm vụ, streak hay điểm thưởng để tạo thói quen học tập thường xuyên.',
    details: [
      'Không có vòng lặp học tập',
      'Không có nhiệm vụ định kỳ',
      'Tương tác giảm dần theo thời gian'
    ]
  },
  {
    year: '03',
    title: 'Không có cơ chế ghi nhận nỗ lực',
    description: 'Điểm danh, làm bài, đóng góp tài liệu hoặc hoàn thành mục tiêu học tập chưa được ghi nhận thành phần thưởng rõ ràng.',
    details: [
      'Không có điểm thưởng minh bạch',
      'Học viên không thấy tiến bộ',
      'Khó duy trì streak học tập'
    ]
  },
  {
    year: '04',
    title: 'Khó tạo khác biệt cho cộng đồng',
    description: 'CLB, gia sư hoặc trung tâm nhỏ khó biến kho tài liệu của mình thành một trải nghiệm học tập chuyên nghiệp và có tính gắn kết.',
    details: [
      'Khó xây dựng thương hiệu riêng',
      'Trải nghiệm học rời rạc',
      'Thiếu hệ thống khuyến khích học tập'
    ]
  }
];

const getNodeStyle = (index: number): Record<string, string> => {
  const styles: Record<number, Record<string, string>> = {
    0: { top: '5%', right: '10%' },
    1: { top: '22%', right: '25%' },
    2: { top: '39%', right: '15%' },
    3: { top: '56%', right: '28%' },
    4: { top: '73%', right: '12%' },
    5: { top: '90%', right: '24%' }
  };
  return styles[index] || {};
};

const toggleMilestone = (index: number) => {
  const wasExpanded = expandedNodes.value.includes(index);
  const milestoneNode = milestoneRefs.value[index];

  if (wasExpanded) {
    expandedNodes.value = expandedNodes.value.filter(i => i !== index);
    activeNode.value = activeNode.value === index ? null : index;
    if (milestoneNode) {
      saveFocus();
      nextTick(() => {
        returnFocus(milestoneNode);
        announceToScreenReader(`${milestones[index].year} ${milestones[index].title} collapsed`);
      });
    }
  } else {
    expandedNodes.value.push(index);
    activeNode.value = index;
    if (milestoneNode) {
      saveFocus();
      nextTick(() => {
        const detailsElement = milestoneNode.querySelector(`#milestone-details-${index}`) as HTMLElement;
        if (detailsElement) {
          detailsElement.focus();
          detailsElement.setAttribute('tabindex', '-1');
        }
        announceToScreenReader(`${milestones[index].year} ${milestones[index].title} expanded. ${milestones[index].details.length} details available`);
      });
    }
  }
};

const focusMilestone = (index: number) => {
  const node = milestoneRefs.value[index];
  if (node) {
    node.focus();
    focusedMilestoneIndex.value = index;
  }
};

const navigateToPreviousMilestone = () => {
  const currentIndex = focusedMilestoneIndex.value ?? 0;
  const previousIndex = currentIndex === 0 ? milestones.length - 1 : currentIndex - 1;
  focusMilestone(previousIndex);
};

const navigateToNextMilestone = () => {
  const currentIndex = focusedMilestoneIndex.value ?? 0;
  const nextIndex = (currentIndex + 1) % milestones.length;
  focusMilestone(nextIndex);
};

const scrollToMission = () => {
  const missionSection = document.querySelector('#mission');
  if (missionSection) {
    missionSection.scrollIntoView({ behavior: 'smooth' });
  }
};

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.sustainability-background', '.bg-image', '.journey-intro', '.journey-timeline', '.milestone-node'], { opacity: 1 });
    return;
  }

  nextTick(() => {
    const triggerEl = document.querySelector('#sustainability') as HTMLElement;
    const isAlreadyInView = triggerEl && triggerEl.getBoundingClientRect().top <= window.innerHeight * 0.6;

    const tl = gsap.timeline({
      defaults: { ease: 'power3.out' },
      scrollTrigger: {
        trigger: '#sustainability',
        start: 'top 60%',
        once: true
      }
    });
    registerCleanup(tl);

    if (isAlreadyInView) {
      tl.progress(1);
    }

    tl.from('.sustainability-background', {
      opacity: 0,
      duration: 0.8
    })
      .from('.bg-image', {
        scale: 1.2,
        duration: 1.5,
        ease: 'power2.out'
      }, '-=0.4');

    registerCleanup(animation?.batchReveal?.('.journey-intro > *', {
      trigger: '#sustainability',
      stagger: 0.1,
      y: 24
    }));

    registerCleanup(gsap.from('.timeline-track', {
      scaleX: 0,
      duration: 0.8,
      ease: 'power2.inOut',
      scrollTrigger: {
        trigger: '#sustainability',
        start: 'top 60%',
        once: true
      }
    }));

    registerCleanup(animation?.batchReveal?.('.milestone-node', {
      trigger: '#sustainability',
      stagger: 0.15,
      y: 24
    }));
  });

  registerCleanup(gsap.from('.floating-particles', {
    opacity: 0,
    duration: 0.5
  }));

  registerCleanup(gsap.from('.corner-accent', {
    scale: 0,
    opacity: 0,
    duration: 0.4,
    stagger: 0.1,
    ease: 'back.out(2)'
  }));

  registerCleanup(animation?.bgParallax?.('.bg-image', { speed: 0.2, scrub: true }));

  registerCleanup(gsap.to('.bg-gradient-mesh', {
    scrollTrigger: {
      trigger: '.sustainability-section',
      start: 'top bottom',
      end: 'bottom top',
      scrub: 2
    },
    xPercent: -10,
    ease: 'none'
  }));

  const timeline = document.querySelector('.journey-timeline') as HTMLElement | null;
  if (timeline) {
    const cleanupArrowKeys = handleArrowKeys(timeline, {
      onArrowUp: navigateToPreviousMilestone,
      onArrowDown: navigateToNextMilestone,
      onArrowLeft: navigateToPreviousMilestone,
      onArrowRight: navigateToNextMilestone
    });
    if (cleanupArrowKeys) cleanupFns.push(cleanupArrowKeys);
  }

  setTimeout(() => {
    document.querySelectorAll('.milestone-node').forEach((node, index) => {
      const cleanup = handleEnterOrSpace(node as HTMLElement, () => toggleMilestone(index));
      if (cleanup) cleanupFns.push(cleanup);
    });
  }, 100);

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const index = milestoneRefs.value.findIndex((el) => el === entry.target);
        if (index >= 0 && !visibleNodes.value.includes(index)) {
          visibleNodes.value.push(index);
        }
      }
    });
  }, { threshold: 0.5 });

  milestoneRefs.value.forEach((node) => {
    if (node) observer.observe(node);
  });

  cleanupFns.push(() => observer.disconnect());
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn && typeof fn === 'function' && fn());
});
</script>

<template>
  <section id="sustainability" class="sustainability-section panel">
    <div class="sustainability-background">
      <div class="bg-image-wrapper">
        <img
          src="/images/oil-palm.jpg"
          alt="Hình nền trang trí cho phần giới thiệu"
          class="bg-image"
        />
      </div>
      <div class="bg-overlay"></div>
      <div class="bg-texture"></div>
      <div class="bg-gradient-mesh"></div>
    </div>

    <div class="sustainability-content">
      <div class="journey-intro">
        <div class="intro-badge">Vấn đề hiện tại</div>
        <h2 class="journey-headline">
          Tài liệu học tập không thiếu. Vấn đề là học viên không tương tác.
        </h2>
        <p class="journey-description">
          Nhiều CLB, gia sư và nhóm luyện thi đang dùng thư mục Drive để chia sẻ tài liệu. Nhưng sau một thời gian, thư mục trở nên rối, tài liệu khó tìm, học viên tải về rồi bỏ đó và người quản lý không biết ai đang thực sự học.
        </p>

        <div class="journey-stats">
          <div class="stat-item">
            <span class="stat-number">Drive</span>
            <span class="stat-label">dễ thành kho lộn xộn</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">1 lần</span>
            <span class="stat-label">tải rồi rời đi</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">0</span>
            <span class="stat-label">cơ chế thưởng rõ ràng</span>
          </div>
        </div>

        <div class="journey-ctas">
          <button 
            class="cta-primary" 
            aria-label="Đi đến phần sứ mệnh"
            @click="scrollToMission"
          >
            Xem cách Gamified Study Hub giải quyết
            <svg class="cta-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </button>
        </div>

        <div class="scroll-indicator">
          <span class="scroll-text">Kéo xuống để xem</span>
          <div class="scroll-line"></div>
        </div>
      </div>

      <div 
        class="journey-timeline"
        role="region"
        aria-label="Các vấn đề hiện tại"
      >
        <div class="timeline-track" aria-hidden="true">
          <div class="timeline-progress"></div>
        </div>

        <div
          v-for="(milestone, index) in milestones"
          :id="`milestone-${index}`"
          :key="milestone.year"
          :ref="el => milestoneRefs[index] = el as HTMLElement"
          class="milestone-node"
          :class="[
            `node-${index}`,
            { 'is-visible': visibleNodes.includes(index) },
            { 'is-active': activeNode === index }
          ]"
          :style="getNodeStyle(index)"
          role="button"
          :tabindex="0"
          :aria-expanded="expandedNodes.includes(index)"
          :aria-controls="`milestone-details-${index}`"
          :aria-label="`${milestone.year}: ${milestone.title}. ${milestone.description}`"
          @click="toggleMilestone(index)"
          @focus="focusedMilestoneIndex = index"
        >
          <div class="node-marker">
            <div class="marker-ring"></div>
            <div class="marker-core"></div>
          </div>

          <div class="node-content">
            <div class="node-year">{{ milestone.year }}</div>
            <h3 class="node-title">{{ milestone.title }}</h3>
            <p class="node-description">{{ milestone.description }}</p>

            <transition name="expand">
              <div 
                v-if="expandedNodes.includes(index)" 
                :id="`milestone-details-${index}`"
                class="node-details"
                tabindex="-1"
              >
                <ul class="details-list" role="list">
                  <li v-for="detail in milestone.details" :key="detail" role="listitem">
                    <svg class="check-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                      <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                    </svg>
                    {{ detail }}
                  </li>
                </ul>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>

    <div class="floating-particles">
      <div class="particle" style="top: 15%; left: 20%; animation-delay: 0s;"></div>
      <div class="particle" style="top: 35%; left: 75%; animation-delay: 1s;"></div>
      <div class="particle" style="top: 55%; left: 30%; animation-delay: 2s;"></div>
      <div class="particle" style="top: 75%; left: 80%; animation-delay: 3s;"></div>
      <div class="particle" style="top: 25%; left: 55%; animation-delay: 4s;"></div>
      <div class="particle" style="top: 65%; left: 15%; animation-delay: 5s;"></div>
      <div class="particle" style="top: 85%; left: 45%; animation-delay: 6s;"></div>
      <div class="particle" style="top: 45%; left: 90%; animation-delay: 7s;"></div>
    </div>
    <div class="corner-accent top-left"></div>
    <div class="corner-accent bottom-right"></div>
  </section>
</template>

<style scoped>
.sustainability-section {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  background: #2a5c55;
}

.sustainability-background {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.bg-image-wrapper {
  position: absolute;
  inset: 0;
}

.bg-image {
  width: 100%;
  height: 120%;
  object-fit: cover;
  position: absolute;
  top: -10%;
  opacity: 0.52;
  filter: saturate(0.55) brightness(0.75);
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(22, 36, 34, 0.9) 0%,
    rgba(26, 47, 43, 0.84) 50%,
    rgba(23, 41, 38, 0.82) 100%
  );
}

.bg-texture {
  position: absolute;
  inset: 0;
  opacity: 0.04;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  pointer-events: none;
}

.bg-gradient-mesh {
  position: absolute;
  width: 150%;
  height: 150%;
  top: -25%;
  left: -25%;
  background: radial-gradient(
    circle at 30% 20%,
    rgba(196, 91, 40, 0.15) 0%,
    transparent 40%
  ),
  radial-gradient(
    circle at 70% 80%,
    rgba(212, 162, 76, 0.1) 0%,
    transparent 40%
  );
  pointer-events: none;
}

.sustainability-content {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 35% 65%;
  min-height: 100vh;
  padding: 4rem 6rem;
}

.journey-intro {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-right: 4rem;
}

.intro-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 0.7rem;
  font-weight: 600;
  color: #d4a24c;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  background: rgba(212, 162, 76, 0.15);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  width: fit-content;
  margin-bottom: 1.5rem;
}

.journey-headline {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: clamp(2.5rem, 5vw, 4rem);
  font-weight: 700;
  line-height: 1.1;
  color: #ffffff;
  margin: 0 0 1.5rem 0;
  letter-spacing: -0.02em;
}

.journey-description {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 1.1rem;
  font-weight: 400;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 2.5rem 0;
}

.journey-stats {
  display: flex;
  gap: 2rem;
  margin-bottom: 2.5rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 2rem;
  font-weight: 800;
  color: #d4a24c;
  line-height: 1;
}

.stat-label {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 0.8rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 0.25rem;
}

.journey-ctas {
  display: flex;
  gap: 1rem;
  margin-bottom: 3rem;
}

.cta-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: #c45b28;
  color: #ffffff;
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cta-primary:hover {
  background: #d4a24c;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(196, 91, 40, 0.35);
}

.cta-arrow {
  width: 18px;
  height: 18px;
  transition: transform 0.3s ease;
}

.cta-primary:hover .cta-arrow {
  transform: translateX(4px);
}

.scroll-indicator {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.scroll-text {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 0.75rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.scroll-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.3), transparent);
  position: relative;
  overflow: hidden;
}

.scroll-line::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 50%;
  height: 100%;
  background: #d4a24c;
  animation: scrollFlow 2s ease-in-out infinite;
}

@keyframes scrollFlow {
  0%, 100% { transform: translateX(-100%); opacity: 0; }
  50% { transform: translateX(100%); opacity: 1; }
}

.journey-timeline {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timeline-track {
  position: absolute;
  width: 4px;
  height: 80%;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  transform: rotate(-25deg);
}

.timeline-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 0%;
  background: linear-gradient(180deg, #c45b28, #d4a24c);
  border-radius: 2px;
  transition: height 0.1s linear;
}

.milestone-node {
  position: absolute;
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.milestone-node:hover {
  transform: scale(1.05);
}

.milestone-node:focus-visible {
  outline: 3px solid var(--color-accent);
  outline-offset: 4px;
  border-radius: 4px;
  transform: scale(1.05);
}

.node-marker {
  position: relative;
  width: 48px;
  height: 48px;
}

.marker-core {
  width: 16px;
  height: 16px;
  background: #ffffff;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.3s ease;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
}

.node-marker:hover .marker-core,
.milestone-node.is-active .marker-core {
  width: 20px;
  height: 20px;
  background: #d4a24c;
  box-shadow: 0 0 30px rgba(212, 162, 76, 0.6);
}

.marker-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transition: all 0.3s ease;
}

.node-marker:hover .marker-ring,
.milestone-node.is-active .marker-ring {
  width: 56px;
  height: 56px;
  border-color: #d4a24c;
  opacity: 0.5;
}

.node-content {
  position: absolute;
  left: 70px;
  top: 50%;
  transform: translateY(-50%);
  width: clamp(250px, 23vw, 360px);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 1.25rem;
  opacity: 0;
  transition: all 0.4s ease;
  pointer-events: none;
}

.milestone-node.is-visible .node-content {
  opacity: 1;
  pointer-events: auto;
}

.milestone-node:hover .node-content,
.milestone-node.is-active .node-content {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(212, 162, 76, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.node-1 .node-content,
.node-3 .node-content {
  left: auto;
  right: 70px;
}

.node-year {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #ffffff, #d4a24c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.5rem;
}

.node-title {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
}

.node-description {
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.5;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 200px;
}

.node-details {
  outline: none;
}

.node-details:focus-visible {
  outline: 3px solid var(--color-accent);
  outline-offset: 4px;
  border-radius: 4px;
}

.details-list {
  list-style: none;
  padding: 0.75rem 0 0 0;
  margin: 0.75rem 0 0 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.details-list li {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.9);
  padding: 0.35rem 0;
}

.check-icon {
  width: 16px;
  height: 16px;
  color: #d4a24c;
  flex-shrink: 0;
  margin-top: 2px;
}

.floating-particles {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: rgba(212, 162, 76, 0.3);
  border-radius: 50%;
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); opacity: 0.3; }
  50% { transform: translateY(-30px) rotate(180deg); opacity: 0.6; }
}

.corner-accent {
  position: absolute;
  width: 200px;
  height: 200px;
  z-index: 1;
  pointer-events: none;
}

.corner-accent.top-left {
  top: -50px;
  left: -50px;
  background: radial-gradient(circle, rgba(196, 91, 40, 0.1) 0%, transparent 70%);
}

.corner-accent.bottom-right {
  bottom: -80px;
  right: -80px;
  background: radial-gradient(circle, rgba(212, 162, 76, 0.08) 0%, transparent 70%);
}

@media (max-width: 1024px) {
  .sustainability-content {
    grid-template-columns: 1fr;
    padding: 3rem 2rem;
  }

  .journey-intro {
    padding-right: 0;
    padding-bottom: 3rem;
    text-align: center;
    align-items: center;
  }

  .journey-stats {
    justify-content: center;
  }

  .journey-ctas {
    justify-content: center;
  }

  .journey-timeline {
    min-height: auto;
    width: 100%;
    align-items: stretch;
    justify-content: flex-start;
    display: flex;
    flex-direction: column;
  }

  .timeline-track {
    display: none;
  }

  .milestone-node {
    position: relative !important;
    top: auto !important;
    right: auto !important;
    left: auto !important;
    display: flex;
    align-items: center;
    gap: 1.5rem;
    margin-bottom: 2rem;
    width: 100%;
    max-width: 100%;
  }

  .node-content {
    position: static;
    transform: none;
    width: 100%;
    max-width: 100%;
    min-width: 0;
    overflow-wrap: anywhere;
  }
}

@media (max-width: 767px) {
  .sustainability-content {
    padding: 2rem 1.5rem;
  }

  .journey-stats {
    flex-direction: column;
    gap: 1.5rem;
    align-items: center;
  }

  .journey-ctas {
    width: 100%;
  }

  .cta-primary {
    width: 100%;
    justify-content: center;
  }

  .journey-timeline {
    min-height: auto;
  }

  .node-content {
    padding: 1rem;
    max-width: calc(100vw - 6rem);
  }

  .node-title,
  .node-description,
  .details-list li {
    overflow-wrap: anywhere;
    word-break: normal;
  }
}
</style>
