<script setup lang="ts">
import { provide } from 'vue';
import LandingNavigation from '@/components/landing/LandingNavigation.vue';
import LandingHero from '@/components/landing/LandingHero.vue';
import LandingSustainability from '@/components/landing/LandingSustainability.vue';
import LandingMission from '@/components/landing/LandingMission.vue';
import LandingGeographic from '@/components/landing/LandingGeographic.vue';
import LandingProduct from '@/components/landing/LandingProduct.vue';
import LandingContact from '@/components/landing/LandingContact.vue';

const scrollToSection = (href: string) => {
  const element = document.querySelector(href) as HTMLElement | null;
  if (!element) return;
  const navHeight = 80;
  const elementPosition = element.getBoundingClientRect().top + window.pageYOffset;
  const offsetPosition = elementPosition - navHeight;
  window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
};

provide('scrollToSection', scrollToSection);
</script>

<template>
  <div id="top" class="relative min-h-screen overflow-x-hidden">
    <LandingNavigation />
    <main id="main-content">
      <LandingHero />
      <LandingSustainability />
      <LandingMission />
      <LandingGeographic />
      <LandingProduct />
      <LandingContact />
    </main>
  </div>
</template>
