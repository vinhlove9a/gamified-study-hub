<script setup lang="ts">
import { provide } from 'vue';
import LandingNavigation from '@/components/landing/LandingNavigation.vue';
import LandingHero from '@/components/landing/LandingHero.vue';
import LandingMarquee from '@/components/landing/LandingMarquee.vue';
import LandingInteractiveLab from '@/components/landing/LandingInteractiveLab.vue';
import LandingStatsBand from '@/components/landing/LandingStatsBand.vue';
import LandingHowItWorks from '@/components/landing/LandingHowItWorks.vue';
import LandingAchievements from '@/components/landing/LandingAchievements.vue';
import LandingTestimonials from '@/components/landing/LandingTestimonials.vue';
import LandingVipTiers from '@/components/landing/LandingVipTiers.vue';
import LandingFaq from '@/components/landing/LandingFaq.vue';
import LandingFinalCta from '@/components/landing/LandingFinalCta.vue';
import LandingReelBackdrop from '@/components/landing/LandingReelBackdrop.vue';
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';

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
  <div id="top" class="cosmic-page relative min-h-screen overflow-x-hidden">
    <LandingReelBackdrop />
    <StarfieldCanvas />
    <LandingNavigation />
    <main id="main-content" class="relative z-10">
      <LandingHero />
      <LandingMarquee />
      <LandingStatsBand />
      <LandingHowItWorks />
      <LandingInteractiveLab />
      <LandingAchievements />
      <LandingTestimonials />
      <LandingVipTiers />
      <LandingFaq />
      <LandingFinalCta />
    </main>
  </div>
</template>
