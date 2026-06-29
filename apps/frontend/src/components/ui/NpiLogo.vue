<script setup lang="ts">
interface Props {
  variant?: 'light' | 'dark';
  size?: 'sm' | 'md' | 'lg';
  /** Show the "Gamified Study Hub" wordmark next to the mark. */
  showWordmark?: boolean;
}

withDefaults(defineProps<Props>(), {
  variant: 'light',
  size: 'md',
  showWordmark: true
});

const markSize = {
  sm: 28,
  md: 36,
  lg: 48
};
</script>

<template>
  <span :class="['gsh-logo', `gsh-logo--${variant}`, `gsh-logo--${size}`]" aria-label="Gamified Study Hub">
    <img
      class="gsh-logo__mark"
      src="/images/gmf-logo.png"
      alt="Gamified Study Hub"
      :width="markSize[size]"
      :height="markSize[size]"
    />
    <span v-if="showWordmark" class="gsh-logo__text">Gamified Study Hub</span>
  </span>
</template>

<style scoped>
.gsh-logo {
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  vertical-align: middle;
}

.gsh-logo__mark {
  border-radius: 0.6rem;
  object-fit: cover;
  box-shadow: 0 0 18px oklch(0.78 0.18 195 / 35%);
  transition: transform 0.3s ease;
}

.gsh-logo:hover .gsh-logo__mark {
  transform: translateY(-1px) scale(1.03);
}

.gsh-logo__text {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.gsh-logo--light .gsh-logo__text {
  color: #ffffff;
}

.gsh-logo--dark .gsh-logo__text {
  color: #0f172a;
}

.gsh-logo--sm .gsh-logo__text {
  font-size: 0.9rem;
}

.gsh-logo--md .gsh-logo__text {
  font-size: 1.05rem;
}

.gsh-logo--lg .gsh-logo__text {
  font-size: 1.3rem;
}
</style>
