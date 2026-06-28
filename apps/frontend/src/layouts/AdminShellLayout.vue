<script setup lang="ts">
import { ref } from 'vue';
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import ToastHost from '@/components/app/ToastHost.vue';
import AdminSidebar from '@/components/admin/AdminSidebar.vue';
import AdminTopbar from '@/components/admin/AdminTopbar.vue';

const mobileOpen = ref(false);
const collapsed = ref(false);
</script>

<template>
  <div class="cosmic-dashboard min-h-screen">
    <StarfieldCanvas />
    <ToastHost />

    <!-- Desktop sidebar -->
    <aside
      class="fixed inset-y-0 left-0 z-20 hidden transition-[width] duration-200 lg:block"
      :class="collapsed ? 'w-[76px]' : 'w-64'"
    >
      <AdminSidebar :collapsed="collapsed" @navigate="mobileOpen = false" @toggle-collapse="collapsed = !collapsed" />
    </aside>

    <!-- Mobile drawer -->
    <transition name="fade">
      <div v-if="mobileOpen" class="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden" @click="mobileOpen = false" />
    </transition>
    <transition name="slide">
      <aside v-if="mobileOpen" class="fixed inset-y-0 left-0 z-50 w-64 shadow-2xl lg:hidden">
        <button
          type="button"
          class="absolute -right-11 top-3 flex h-9 w-9 items-center justify-center rounded-xl bg-white/10 text-foreground backdrop-blur"
          aria-label="Đóng menu"
          @click="mobileOpen = false"
        >
          <span class="text-lg leading-none">✕</span>
        </button>
        <AdminSidebar @navigate="mobileOpen = false" />
      </aside>
    </transition>

    <!-- Main column -->
    <div class="flex min-h-screen flex-col transition-[padding] duration-200" :class="collapsed ? 'lg:pl-[76px]' : 'lg:pl-64'">
      <AdminTopbar @open-sidebar="mobileOpen = true" />
      <main class="cosmic-scroll mx-auto w-full max-w-[1600px] flex-1 px-4 py-6 sm:px-6 lg:px-8">
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
</style>
