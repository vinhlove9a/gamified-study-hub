<script setup lang="ts">
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { ApiError } from '@/lib/api/apiError';

interface FormState {
  email: string;
}

const form = reactive<FormState>({
  email: ''
});
const router = useRouter();

const loading = ref(false);
const formMessage = ref('');
const errors = reactive<{ email?: string }>({});

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const validate = () => {
  errors.email = '';

  if (!form.email.trim()) {
    errors.email = 'Email không được để trống';
  } else if (!emailRegex.test(form.email)) {
    errors.email = 'Email không đúng định dạng';
  }

  return !errors.email;
};

const handleSubmit = async () => {
  formMessage.value = '';
  if (!validate()) return;

  loading.value = true;
  try {
    const response = await authApi.forgotPassword({
      email: form.email.trim()
    });
    formMessage.value = response.message;
    await router.push(`/auth/notice?type=reset-link-sent&email=${encodeURIComponent(form.email.trim())}`);
  } catch (error) {
    if (error instanceof ApiError) {
      formMessage.value = error.message;
    } else {
      formMessage.value = 'Không thể kết nối máy chủ. Vui lòng thử lại.';
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="forgot-page cosmic-page auth-cosmic">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-shell">
        <div class="auth-brand">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title">Quên mật khẩu?</h1>
          <p class="auth-subtitle">Nhập email tài khoản của bạn. Chúng tôi sẽ gửi hướng dẫn đặt lại mật khẩu để bạn tiếp tục sử dụng hệ thống.</p>
        </div>

        <section class="auth-card" aria-label="Form quên mật khẩu">
          <div class="auth-grid">
            <aside class="recovery-panel" aria-label="Hướng dẫn khôi phục mật khẩu">
              <div class="panel-icon" aria-hidden="true">✉</div>

              <p class="panel-title">Khôi phục an toàn</p>

              <ol class="step-list" aria-label="Các bước khôi phục">
                <li class="step-item">
                  <span class="step-index" aria-hidden="true">1</span>
                  <span>Xác nhận email</span>
                </li>
                <li class="step-item">
                  <span class="step-index" aria-hidden="true">2</span>
                  <span>Nhận liên kết bảo mật</span>
                </li>
                <li class="step-item">
                  <span class="step-index" aria-hidden="true">3</span>
                  <span>Đặt lại mật khẩu mới</span>
                </li>
              </ol>
            </aside>

            <form class="form-stack" novalidate @submit.prevent="handleSubmit">
              <div>
                <label for="email" class="field-label">Email</label>
                <input
                  id="email"
                  v-model="form.email"
                  class="field-input"
                  :class="{ 'field-input--error': errors.email }"
                  type="email"
                  placeholder="hello@example.com"
                  autocomplete="email"
                  required
                  :disabled="loading"
                  :aria-invalid="Boolean(errors.email)"
                  :aria-describedby="errors.email ? 'email-error' : undefined"
                />
                <p v-if="errors.email" id="email-error" class="field-error" role="alert">{{ errors.email }}</p>
              </div>

              <button type="submit" class="submit-btn" :disabled="loading" :aria-busy="loading">
                {{ loading ? 'Đang xử lý...' : 'Gửi liên kết đặt lại mật khẩu' }}
              </button>

              <p class="helper-note">Hãy sử dụng email bạn đã dùng khi tạo tài khoản Gamified Study Hub.</p>

              <p v-if="formMessage" class="submit-message" role="status" aria-live="polite">{{ formMessage }}</p>

              <div class="link-row" aria-label="Liên kết điều hướng">
                <RouterLink to="/auth/login" class="auth-link auth-link--strong">Quay lại đăng nhập</RouterLink>
                <span class="link-divider" aria-hidden="true">•</span>
                <RouterLink to="/auth/register" class="auth-link">Tạo tài khoản</RouterLink>
              </div>
            </form>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>
