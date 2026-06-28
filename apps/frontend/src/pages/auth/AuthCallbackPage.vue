<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthSession } from '@/features/auth/authSession';
import { defaultRouteForRole } from '@/router/authGuard';

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
  <div class="callback-page">
    <div class="callback-card">
      <span class="spinner" aria-hidden="true"></span>
      <p>{{ message }}</p>
    </div>
  </div>
</template>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 30% 20%, #1e1b4b 0%, #0b1020 55%, #050816 100%);
  color: #e6e9f5;
}
.callback-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 2rem 2.5rem;
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(139, 92, 246, 0.3);
}
.spinner {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 9999px;
  border: 3px solid rgba(139, 92, 246, 0.25);
  border-top-color: #8b5cf6;
  animation: spin 0.9s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
