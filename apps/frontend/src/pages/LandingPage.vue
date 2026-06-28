<script setup lang="ts">
// The marketing landing page is the design authored in ex.html, rebranded to
// the Gamified Study Hub. It is shipped verbatim as a standalone document at
// /landing.html (see apps/frontend/public/landing.html) so its Tailwind Play
// CDN, Google Fonts, Font Awesome, inline <style>, and vanilla <script> render
// exactly as designed — fully isolated from the SPA's own compiled Tailwind
// build and global styles. We embed it full-viewport here.
</script>

<template>
  <iframe
    class="landing-frame"
    src="/landing.html"
    title="Gamified Study Hub"
  ></iframe>
</template>

<style scoped>
.landing-frame {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  border: 0;
  display: block;
}
</style>
