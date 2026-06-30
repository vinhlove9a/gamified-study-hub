<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthSession } from '@/features/auth/authSession';
import { defaultRouteForRole } from '@/router/authGuard';
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';

const route = useRoute();
const router = useRouter();
const { bootstrapSession, isAuthenticated, isAdmin } = useAuthSession();
const message = ref('Đang hoàn tất đăng nhập…');

onMounted(async () => {
  if (route.query.error) {
    await router.replace({ path: '/auth/error', query: { reason: String(route.query.error) } });
    return;
  }
  // Cookies were already set by the backend on redirect — hydrate the session.
  await bootstrapSession();
  if (isAuthenticated.value) {
    await router.replace(defaultRouteForRole(isAdmin.value));
  } else {
    message.value = 'Không thể xác thực phiên. Đang chuyển về đăng nhập…';
    await router.replace('/auth/login');
  }
});
</script>

<template>
  <div class="callback-page cosmic-page auth-cosmic">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-shell auth-shell--narrow">
        <section class="auth-card">
          <div class="callback-card">
            <span class="spinner" aria-hidden="true"></span>
            <p class="hint">{{ message }}</p>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.auth-shell--narrow {
  max-width: 26rem;
}
</style>
