<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { useAuthSession } from '@/features/auth/authSession';
import { useMfaChallenge } from '@/features/auth/mfaChallenge';
import { ApiError } from '@/lib/api/apiError';
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';

const router = useRouter();
const { setSessionFromAuthResponse } = useAuthSession();
const { mfaToken, redirectPath, clear } = useMfaChallenge();

const code = ref('');
const loading = ref(false);
const message = ref('');

onMounted(() => {
  // No pending challenge (e.g. page refresh) → back to login.
  if (!mfaToken.value) {
    void router.replace('/auth/login');
  }
});

const handleSubmit = async () => {
  message.value = '';
  if (!mfaToken.value) {
    void router.replace('/auth/login');
    return;
  }
  if (!/^\d{6}$/.test(code.value.trim()) && code.value.trim().length < 6) {
    message.value = 'Nhập mã 6 chữ số từ ứng dụng xác thực, hoặc mã khôi phục.';
    return;
  }
  loading.value = true;
  try {
    const result = await authApi.mfaVerify({ mfaToken: mfaToken.value, code: code.value.trim() });
    setSessionFromAuthResponse(result);
    const target = redirectPath.value;
    clear();
    await router.push(target);
  } catch (error) {
    message.value =
      error instanceof ApiError ? error.message : 'Không thể kết nối máy chủ. Vui lòng thử lại.';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="mfa-page cosmic-page auth-cosmic">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-shell auth-shell--narrow">
        <div class="auth-brand animate-bounce-in">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title animate-gradient-text">Xác thực hai lớp</h1>
          <p class="auth-subtitle">Nhập mã 6 chữ số trong ứng dụng xác thực (Google Authenticator…) hoặc một mã khôi phục.</p>
        </div>

        <section class="auth-card card-hover animate-slide-up delay-200" aria-label="Xác thực hai lớp">
          <form class="form-stack" @submit.prevent="handleSubmit">
            <div>
              <label for="mfa-code" class="field-label">Mã xác thực</label>
              <input
                id="mfa-code"
                v-model="code"
                inputmode="numeric"
                autocomplete="one-time-code"
                maxlength="14"
                placeholder="123456"
                class="code-input"
                :disabled="loading"
                aria-label="Mã xác thực"
              />
            </div>

            <button type="submit" class="submit-btn btn-shimmer" :disabled="loading" :aria-busy="loading">
              {{ loading ? 'Đang xác thực…' : 'Xác nhận' }}
            </button>

            <p v-if="message" class="submit-message" role="alert">{{ message }}</p>

            <div class="link-row" aria-label="Liên kết điều hướng">
              <RouterLink to="/auth/login" class="auth-link auth-link--strong">Quay lại đăng nhập</RouterLink>
            </div>
          </form>
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
