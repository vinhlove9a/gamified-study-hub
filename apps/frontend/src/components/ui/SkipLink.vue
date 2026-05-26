<script setup lang="ts">
const handleSkip = (e: Event) => {
  e.preventDefault();
  const mainContent = document.querySelector('#main-content') || document.querySelector('main');
  if (mainContent) {
    mainContent.setAttribute('tabindex', '-1');
    (mainContent as HTMLElement).focus();
    (mainContent as HTMLElement).scrollIntoView({ behavior: 'smooth', block: 'start' });

    mainContent.addEventListener('blur', () => {
      mainContent.removeAttribute('tabindex');
    }, { once: true });
  }
};
</script>

<template>
  <a
    href="#main-content"
    class="skip-link"
    @click="handleSkip"
  >
    Skip to main content
  </a>
</template>

<style scoped>
.skip-link {
  position: absolute;
  top: -100px;
  left: 0;
  z-index: 10000;
  padding: 1rem 1.5rem;
  background: #2c2416;
  color: #ffffff;
  font-family: 'Jakarta Sans', sans-serif;
  font-size: 1rem;
  font-weight: 600;
  text-decoration: none;
  border-radius: 0 0 4px 0;
  transition: top 0.2s ease;
}

.skip-link:focus {
  top: 0;
  outline: 3px solid #d4a24c;
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .skip-link {
    transition: none;
  }
}
</style>
