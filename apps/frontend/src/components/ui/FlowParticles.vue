<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

interface Props {
  count?: number;
  color?: string;
  speed?: 'slow' | 'medium' | 'fast';
  direction?: 'left' | 'right';
  size?: 'small' | 'medium' | 'large';
  opacity?: number;
}

const props = withDefaults(defineProps<Props>(), {
  count: 6,
  color: '#e6b84d',
  speed: 'medium',
  direction: 'right',
  size: 'small',
  opacity: 0.4
});

const containerRef = ref<HTMLElement | null>(null);
const particles = ref<Array<{ id: number; x: number; y: number; size: number; delay: number }>>([]);
let animationFrame = 0;
let mouseX = 0;
let mouseY = 0;

const speedMultiplier = {
  slow: 1.5,
  medium: 1,
  fast: 0.6
};

const sizeMultiplier = {
  small: 0.5,
  medium: 1,
  large: 1.5
};

const initParticles = () => {
  particles.value = Array.from({ length: props.count }, (_, i) => ({
    id: i,
    x: Math.random() * 100,
    y: Math.random() * 100,
    size: (Math.random() * 3 + 2) * sizeMultiplier[props.size],
    delay: Math.random() * 5
  }));
};

const animate = () => {
  if (!containerRef.value) {
    animationFrame = requestAnimationFrame(animate);
    return;
  }

  const rect = containerRef.value.getBoundingClientRect();
  const width = rect.width;
  const height = rect.height;

  particles.value.forEach((particle, i) => {
    const el = containerRef.value?.querySelector(`[data-particle="${i}"]`) as HTMLElement | null;
    if (!el) return;

    let xPercent = particle.x;
    let yPercent = particle.y;

    if (props.direction === 'right') {
      xPercent += 0.015 * speedMultiplier[props.speed];
      if (xPercent > 110) {
        xPercent = -10;
        yPercent = Math.random() * 100;
      }
    } else {
      xPercent -= 0.015 * speedMultiplier[props.speed];
      if (xPercent < -10) {
        xPercent = 110;
        yPercent = Math.random() * 100;
      }
    }

    const yOffset = Math.sin(Date.now() * 0.001 + particle.delay) * 3;
    const mouseInfluenceX = ((mouseX - width / 2) / width) * 5;
    const mouseInfluenceY = ((mouseY - height / 2) / height) * 5;

    el.style.transform = `translate(${xPercent + mouseInfluenceX}%, ${yPercent + yOffset + mouseInfluenceY}%)`;

    particle.x = xPercent;
    particle.y = yPercent;
  });

  animationFrame = requestAnimationFrame(animate);
};

const handleMouseMove = (e: MouseEvent) => {
  mouseX = e.clientX;
  mouseY = e.clientY;
};

onMounted(() => {
  initParticles();
  animate();
  window.addEventListener('mousemove', handleMouseMove);
});

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove);
  cancelAnimationFrame(animationFrame);
});

const particleColor = props.color;
</script>

<template>
  <div ref="containerRef" class="flow-particles" :style="{ '--particle-color': particleColor, '--particle-opacity': opacity }">
    <div
      v-for="particle in particles"
      :key="particle.id"
      :data-particle="particle.id"
      class="particle"
      :style="{
        width: `${particle.size}px`,
        height: `${particle.size}px`
      }"
    >
      <svg viewBox="0 0 24 24" fill="currentColor" class="particle-icon">
        <circle cx="12" cy="12" r="8" />
      </svg>
    </div>
  </div>
</template>

<style scoped>
.flow-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.particle {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--particle-color);
  opacity: var(--particle-opacity);
  will-change: transform, opacity;
}

.particle-icon {
  width: 100%;
  height: 100%;
  filter: blur(0.5px);
}
</style>
