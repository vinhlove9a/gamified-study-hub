<script setup lang="ts">
import { ref } from 'vue';
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import AppSidebar from '@/components/app/AppSidebar.vue';
import AppTopbar from '@/components/app/AppTopbar.vue';
import ToastHost from '@/components/app/ToastHost.vue';

const mobileOpen = ref(false);
const collapsed = ref(false);
</script>

<template>
  <div class="cosmic-dashboard focused-learning-workspace min-h-screen">
    <StarfieldCanvas />
    <ToastHost />

    <aside
      class="fixed inset-y-0 left-0 z-20 hidden transition-[width] duration-200 lg:block"
      :class="collapsed ? 'w-[76px]' : 'w-64'"
    >
      <AppSidebar :collapsed="collapsed" @navigate="mobileOpen = false" @toggle-collapse="collapsed = !collapsed" />
    </aside>

    <transition name="fade">
      <div v-if="mobileOpen" class="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden" @click="mobileOpen = false" />
    </transition>
    <transition name="slide">
      <aside v-if="mobileOpen" class="fixed inset-y-0 left-0 z-50 w-64 lg:hidden">
        <button
          type="button"
          class="absolute -right-11 top-3 flex h-9 w-9 items-center justify-center rounded-sm border border-zinc-800 bg-zinc-950 text-foreground"
          aria-label="Đóng menu"
          @click="mobileOpen = false"
        >
          <span class="text-lg leading-none">✕</span>
        </button>
        <AppSidebar @navigate="mobileOpen = false" />
      </aside>
    </transition>

    <div class="flex min-h-screen flex-col transition-[padding] duration-200" :class="collapsed ? 'lg:pl-[76px]' : 'lg:pl-64'">
      <AppTopbar @open-sidebar="mobileOpen = true" />
      <main class="cosmic-scroll mx-auto w-full max-w-[1500px] flex-1 px-4 py-6 sm:px-6 lg:px-8">
        <RouterView v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.22s ease;
}
.slide-enter-from,
.slide-leave-to {
  transform: translateX(-100%);
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-leave-to {
  opacity: 0;
}

:global(.focused-learning-workspace.cosmic-dashboard) {
  background: oklch(0.08 0.01 260);
}

:global(.focused-learning-workspace .starfield-canvas) {
  display: none;
}

:global(.focused-learning-workspace .glass-panel) {
  border-color: oklch(0.28 0.02 260);
  background: oklch(0.12 0.01 260);
  box-shadow: none;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
}

:global(.focused-learning-workspace .glass-panel-hover:hover) {
  border-color: oklch(0.38 0.02 260);
  box-shadow: none;
  transform: none;
}

:global(.focused-learning-workspace .cosmic-gradient-text) {
  background: none;
  color: oklch(0.96 0.01 240);
  animation: none;
}

:global(.focused-learning-workspace .cosmic-badge) {
  border-radius: 0.125rem;
  border-color: oklch(0.28 0.02 260);
  background: oklch(0.09 0.01 260);
  color: oklch(0.68 0.02 250);
  letter-spacing: 0.08em;
}

:global(.focused-learning-workspace .cosmic-btn) {
  border-radius: 0.125rem;
  border-color: hsl(var(--primary));
  background: hsl(var(--primary));
  color: hsl(var(--primary-foreground));
  box-shadow: none;
  filter: none;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

:global(.focused-learning-workspace .cosmic-btn:hover:not(:disabled)) {
  transform: none;
  box-shadow: none;
  filter: none;
}

:global(.focused-learning-workspace .cosmic-btn::after),
:global(.focused-learning-workspace .fx-aurora::before),
:global(.focused-learning-workspace .fx-sheen::after) {
  display: none;
}

:global(.focused-learning-workspace .gradient-border) {
  background: oklch(0.12 0.01 260);
  border: 1px solid oklch(0.28 0.02 260);
  box-shadow: none;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
}

:global(.focused-learning-workspace .gradient-border::before) {
  display: none;
}

:global(.focused-learning-workspace .fx-lift:hover) {
  transform: none;
  box-shadow: none;
}
</style>
