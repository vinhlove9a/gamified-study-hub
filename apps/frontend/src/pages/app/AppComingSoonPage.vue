<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import GlassPanel from '@/components/app/GlassPanel.vue';
import AppIcon from '@/components/app/icons/AppIcon.vue';

const route = useRoute();
const title = computed(() => (route.meta.title as string) ?? 'Sắp ra mắt');
const icon = computed(() => (route.meta.icon as string) ?? 'sparkles');
const blurb = computed(
  () => (route.meta.blurb as string) ?? 'Tính năng này đang được phát triển và sẽ sớm có mặt.'
);

const roadmap = ['Thiết kế giao diện', 'Kết nối dữ liệu', 'Thử nghiệm nội bộ', 'Ra mắt chính thức'];
</script>

<template>
  <div class="mx-auto max-w-2xl py-10">
    <GlassPanel glow="cyan" class="p-8 text-center sm:p-12">
      <span class="float-slow mx-auto flex h-20 w-20 items-center justify-center rounded-3xl bg-gradient-to-br from-cyan-500/20 to-fuchsia-500/20 text-cyan-300 shadow-[0_0_40px_oklch(0.78_0.18_195/30%)]">
        <AppIcon :name="icon" class="h-10 w-10" />
      </span>
      <p class="cosmic-badge mx-auto mt-6">Sắp ra mắt</p>
      <h1 class="mt-4 text-2xl font-bold text-foreground sm:text-3xl">{{ title }}</h1>
      <p class="mx-auto mt-3 max-w-md text-sm text-muted-foreground">{{ blurb }}</p>

      <div class="mt-8 flex flex-wrap items-center justify-center gap-2">
        <span
          v-for="(step, i) in roadmap"
          :key="step"
          class="inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-medium"
          :class="i === 0 ? 'border-emerald-400/30 bg-emerald-500/10 text-emerald-300' : 'border-white/[0.08] bg-white/[0.03] text-muted-foreground'"
        >
          <AppIcon :name="i === 0 ? 'check-circle' : 'dot'" class="h-3.5 w-3.5" />
          {{ step }}
        </span>
      </div>

      <RouterLink to="/app" class="cosmic-btn mt-8 inline-flex items-center gap-2 rounded-xl px-5 py-2.5 text-sm">
        <AppIcon name="home" class="h-4 w-4" /> Về trang chủ
      </RouterLink>
    </GlassPanel>
  </div>
</template>
