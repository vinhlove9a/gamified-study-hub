<script setup lang="ts">
import { onMounted, onUnmounted, nextTick, inject } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import { useAriaLive } from '@/composables/useAriaLive';
import IconRspoSmall from '@/components/ui/icons/IconRspoSmall.vue';
import IconIsccSmall from '@/components/ui/icons/IconIsccSmall.vue';
import IconHectaresPremium from '@/components/ui/icons/IconHectaresPremium.vue';
import IconExportsPremium from '@/components/ui/icons/IconExportsPremium.vue';
import IconEmployeesPremium from '@/components/ui/icons/IconEmployeesPremium.vue';

interface StatItem {
  id: string;
  value: number;
  displayValue: string;
  suffix: string;
  label: string;
  icon: object;
  accentColor: string;
}

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
const { announce } = useAriaLive();

const stats: StatItem[] = [
  { id: 'platform', value: 1, displayValue: '1', suffix: ' nền tảng', label: 'Quản lý tài liệu, nhiệm vụ, coin và mở khóa trong cùng một hệ thống.', icon: IconHectaresPremium, accentColor: '#d4a24c' },
  { id: 'users', value: 4, displayValue: '4', suffix: ' nhóm dùng', label: 'Phù hợp cho CLB đại học, gia sư, nhóm luyện thi và trung tâm nhỏ.', icon: IconExportsPremium, accentColor: '#c45b28' },
  { id: 'access', value: 24, displayValue: '24/7', suffix: ' truy cập', label: 'Học viên có thể tìm tài liệu và hoàn thành nhiệm vụ mọi lúc.', icon: IconEmployeesPremium, accentColor: '#2a5c55' }
];

let cleanupFns: Array<() => void> = [];
const announcedStats = new Set<string>();

onMounted(() => {
  if (!gsap) return;

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  if (shouldReduceMotion) {
    gsap.set(['.hero-content', '.stat-row', '.cert-row'], { opacity: 1 });
    nextTick(() => {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            if (entry.target.classList.contains('stat-row')) {
              const statIndex = Array.from(document.querySelectorAll('.stat-row')).indexOf(entry.target as HTMLElement);
              if (statIndex >= 0 && statIndex < stats.length) {
                const stat = stats[statIndex];
                if (!announcedStats.has(stat.id)) {
                  announcedStats.add(stat.id);
                  announce(`${stat.displayValue} ${stat.suffix}, ${stat.label}`);
                }
              }
            } else if (entry.target.classList.contains('cert-row')) {
              if (!announcedStats.has('certifications')) {
                announcedStats.add('certifications');
                announce('0 chi phí khởi đầu, có thể bắt đầu từ kho tài liệu hiện có.');
              }
            }
          }
        });
      }, { threshold: 0.5 });

      document.querySelectorAll('.stat-row, .cert-row').forEach((row) => {
        observer.observe(row);
      });

      cleanupFns.push(() => observer.disconnect());
    });
    return;
  }

  nextTick(() => {
    animation?.batchReveal?.('.hero-content', {
      trigger: '#stats',
      duration: 0.8,
      y: 30
    });

    animation?.batchReveal?.('.stat-row', {
      trigger: '#stats',
      stagger: 0.12,
      y: 40
    });

    animation?.batchReveal?.('.cert-row', {
      trigger: '#stats',
      duration: 0.5,
      y: 20
    });

    document.querySelectorAll('.stat-row').forEach((row) => {
      const cleanup = animation?.hoverScale?.(row as HTMLElement, { scale: 1.02, duration: 0.3 });
      if (cleanup) cleanupFns.push(cleanup);
    });

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          if (entry.target.classList.contains('stat-row')) {
            const statIndex = Array.from(document.querySelectorAll('.stat-row')).indexOf(entry.target as HTMLElement);
            if (statIndex >= 0 && statIndex < stats.length) {
              const stat = stats[statIndex];
              if (!announcedStats.has(stat.id)) {
                announcedStats.add(stat.id);
                announce(`${stat.displayValue} ${stat.suffix}, ${stat.label}`);
              }
            }
          } else if (entry.target.classList.contains('cert-row')) {
            if (!announcedStats.has('certifications')) {
              announcedStats.add('certifications');
              announce('0 chi phí khởi đầu, có thể bắt đầu từ kho tài liệu hiện có.');
            }
          }
        }
      });
    }, { threshold: 0.5 });

    document.querySelectorAll('.stat-row, .cert-row').forEach((row) => {
      observer.observe(row);
    });

    cleanupFns.push(() => observer.disconnect());
  });
});

onUnmounted(() => {
  cleanupFns.forEach((fn) => fn && typeof fn === 'function' && fn());
});
</script>

<template>
  <section id="stats" class="stats-section panel min-h-screen lg:h-screen flex flex-col lg:flex-row overflow-hidden">
    <div class="hero-statement w-full lg:w-1/2 bg-[#f5f0e8] flex flex-col justify-center px-8 lg:px-24 py-16 lg:py-0 relative">
      <div class="hero-content max-w-xl">
        <h2 class="section-label text-sm uppercase tracking-[0.3em] text-[#c45b28] mb-8 block">
          Tác động rõ ràng
        </h2>

        <div class="revenue-hero mb-10">
          <span class="revenue-currency text-4xl lg:text-6xl font-light text-[#2c2416]">1</span>
          <span class="revenue-value text-7xl lg:text-[10rem] leading-[0.9] font-bold text-[#2c2416] tabular-nums">NỀN TẢNG</span>
          <span class="revenue-suffix text-2xl lg:text-3xl text-[#d4a24c]">+</span>
          <div class="revenue-label mt-6 text-lg lg:text-xl text-[#2c2416]/60">
            Từ thư mục lưu trữ thành hệ thống học tập có tương tác.
          </div>
        </div>

        <p class="narrative-text text-xl lg:text-2xl text-[#2c2416]/80 leading-relaxed mb-12">
          Không chỉ là nơi để tải tài liệu, hệ thống giúp học viên có lý do để quay lại, hoàn thành nhiệm vụ và mở khóa nội dung giá trị hơn.
        </p>

        <div class="cert-row flex flex-wrap items-center gap-6 lg:gap-8" role="group" aria-label="Khởi đầu">
          <span class="text-xs uppercase tracking-widest text-[#2c2416]/40">Khởi đầu</span>
          <div class="w-px h-8 bg-[#2c2416]/10" aria-hidden="true"></div>
          <div class="flex items-center gap-4" role="group" aria-label="Certification badges">
            <IconRspoSmall class="w-10 h-10 lg:w-12 lg:h-12" role="img" aria-label="Kho tài liệu" />
            <IconIsccSmall class="w-10 h-10 lg:w-12 lg:h-12" role="img" aria-label="Nhiệm vụ và coin" />
          </div>
          <span class="text-sm lg:text-base text-[#2c2416]/60 hidden sm:block">0 chi phí khởi đầu: bắt đầu từ kho tài liệu sẵn có.</span>
        </div>
      </div>
    </div>

    <div class="stats-column w-full lg:w-1/2 bg-[#2c2416] flex flex-col justify-center px-8 lg:px-16 py-16 lg:py-0" role="group" aria-label="Statistics">
      <div
        v-for="(stat, index) in stats"
        :key="stat.id"
        class="stat-row flex items-center gap-6 lg:gap-10 mb-10 lg:mb-16 pb-10 lg:pb-16 border-b border-[#f5f0e8]/10"
        :class="{ 'border-b-0': index === stats.length - 1 }"
        role="group"
        :aria-label="`${stat.label}: ${stat.displayValue} ${stat.suffix}`"
        tabindex="0"
      >
        <div class="stat-icon w-16 h-16 lg:w-20 lg:h-20 flex-shrink-0" :style="{ color: stat.accentColor }" aria-hidden="true">
          <component :is="stat.icon" />
        </div>
        <div>
          <div class="stat-value flex items-baseline gap-2 lg:gap-3">
            <span class="text-5xl lg:text-7xl font-bold text-[#f5f0e8] tabular-nums tracking-tight">{{ stat.displayValue }}</span>
            <span class="text-xl lg:text-2xl" :style="{ color: stat.accentColor }">{{ stat.suffix }}</span>
          </div>
          <div class="stat-label text-xs lg:text-sm uppercase tracking-widest text-[#f5f0e8]/40 mt-3 lg:mt-4">
            {{ stat.label }}
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.stats-section {
  min-height: 100vh;
}

.section-label {
  font-size: 0.875rem;
  letter-spacing: 0.3em;
}

.revenue-currency {
  font-size: 1.5rem;
}

.revenue-value {
  font-size: 7rem;
  line-height: 0.9;
  letter-spacing: -0.02em;
  display: block;
}

.revenue-suffix {
  font-size: 1.25rem;
}

.revenue-label {
  font-size: 1.125rem;
  margin-top: 1.5rem;
}

.stat-value {
  line-height: 1;
}

.stat-icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.stat-row:focus-visible {
  outline: 3px solid var(--color-accent);
  outline-offset: 4px;
  border-radius: 4px;
}

@media (max-width: 1024px) {
  .revenue-value {
    font-size: 6rem;
  }

  .stat-icon {
    width: 4rem;
    height: 4rem;
  }

  .stat-value span:first-child {
    font-size: 3.5rem;
  }
}

@media (max-width: 768px) {
  .stats-section {
    height: auto;
    min-height: 100vh;
  }

  .hero-statement {
    padding: 4rem 2rem;
  }

  .stats-column {
    padding: 4rem 2rem;
  }

  .revenue-hero {
    margin-bottom: 3rem;
  }

  .revenue-currency {
    font-size: 1.25rem;
  }

  .revenue-value {
    font-size: 4.5rem;
  }

  .revenue-suffix {
    font-size: 1.125rem;
  }

  .revenue-label {
    font-size: 1rem;
    margin-top: 1rem;
  }

  .narrative-text {
    font-size: 1.125rem;
    margin-bottom: 3rem;
  }

  .stat-row {
    gap: 1.5rem;
    margin-bottom: 2.5rem;
    padding-bottom: 2.5rem;
  }

  .stat-icon {
    width: 3.5rem;
    height: 3.5rem;
  }

  .stat-value span:first-child {
    font-size: 2.5rem;
  }

  .stat-icon :deep(svg) {
    width: 100%;
    height: 100%;
  }
}

@media (max-width: 480px) {
  .hero-statement {
    padding: 3rem 1.5rem;
  }

  .stats-column {
    padding: 3rem 1.5rem;
  }

  .revenue-value {
    font-size: 3.5rem;
  }

  .cert-row {
    gap: 1rem;
  }

  .cert-row .w-px {
    display: none;
  }

  .stat-row {
    gap: 1rem;
    margin-bottom: 2rem;
    padding-bottom: 2rem;
  }

  .stat-icon {
    width: 3rem;
    height: 3rem;
  }

  .stat-value span:first-child {
    font-size: 2rem;
  }
}
</style>
