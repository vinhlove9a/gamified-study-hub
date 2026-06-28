<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { useAuthSession } from '@/features/auth/authSession';
import { useMfaChallenge } from '@/features/auth/mfaChallenge';
import { ApiError } from '@/lib/api/apiError';

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
  <div class="mfa-page">
    <form class="mfa-card" @submit.prevent="handleSubmit">
      <p class="brand">Gamified Study Hub</p>
      <h1>Xác thực hai lớp</h1>
      <p class="hint">Nhập mã 6 chữ số trong ứng dụng xác thực (Google Authenticator…) hoặc một mã khôi phục.</p>
      <input
        v-model="code"
        inputmode="numeric"
        autocomplete="one-time-code"
        maxlength="14"
        placeholder="123456"
        class="code-input"
        :disabled="loading"
        aria-label="Mã xác thực"
      />
      <button type="submit" class="submit" :disabled="loading">
        {{ loading ? 'Đang xác thực…' : 'Xác nhận' }}
      </button>
      <p v-if="message" class="msg" role="alert">{{ message }}</p>
    </form>
  </div>
</template>

<style scoped>
.mfa-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 30% 20%, #1e1b4b 0%, #0b1020 55%, #050816 100%);
  color: #e6e9f5;
  padding: 1.25rem;
}
.mfa-card {
  width: 100%;
  max-width: 24rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 2rem;
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(139, 92, 246, 0.3);
}
.brand { margin: 0; color: #a78bfa; font-weight: 600; font-size: 0.85rem; }
h1 { margin: 0; font-size: 1.4rem; }
.hint { margin: 0; color: #9aa3c0; font-size: 0.85rem; line-height: 1.5; }
.code-input {
  margin-top: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 0.6rem;
  border: 1px solid rgba(139, 92, 246, 0.4);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
  font-size: 1.3rem;
  letter-spacing: 0.3rem;
  text-align: center;
}
.submit {
  padding: 0.8rem;
  border: none;
  border-radius: 0.6rem;
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}
.submit:disabled { opacity: 0.6; cursor: not-allowed; }
.msg { margin: 0; color: #fca5a5; font-size: 0.85rem; }
</style>
