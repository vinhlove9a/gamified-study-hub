<script setup lang="ts">
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import { computed, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { ApiError } from '@/lib/api/apiError';

const loading = ref(false);
const resendCount = ref(0);
const formMessage = ref('');
const route = useRoute();
const router = useRouter();

const verifyToken = computed(() => (typeof route.query.token === 'string' ? route.query.token.trim() : ''));
const emailFromQuery = computed(() => (typeof route.query.email === 'string' ? route.query.email.trim() : ''));
const actionLabel = computed(() => (verifyToken.value ? 'Xác thực email' : 'Gửi lại email xác thực'));

const handleResend = async () => {
  formMessage.value = '';
  loading.value = true;
  try {
    if (verifyToken.value) {
      const response = await authApi.verifyEmail({ token: verifyToken.value });
      formMessage.value = response.message;
      await router.push('/auth/notice?type=email-verified');
      return;
    }

    if (!emailFromQuery.value) {
      formMessage.value = 'Thiếu email để gửi lại xác thực. Vui lòng đăng ký lại hoặc đăng nhập.';
      return;
    }

    const response = await authApi.resendVerification({ email: emailFromQuery.value });
    resendCount.value += 1;
    formMessage.value = response.message;
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
  <div class="verify-page cosmic-page auth-cosmic">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-shell">
        <div class="auth-brand">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title">Xác thực email của bạn</h1>
          <p class="auth-subtitle">Chúng tôi đã gửi một liên kết xác thực đến email đăng ký. Vui lòng kiểm tra hộp thư để hoàn tất quá trình tạo tài khoản.</p>
        </div>

        <section class="auth-card" aria-label="Xác thực email">
          <div class="auth-grid">
            <aside class="verify-panel" aria-label="Hướng dẫn xác thực email">
              <div class="panel-icon" aria-hidden="true">✉</div>

              <p class="panel-title">Xác thực tài khoản</p>

              <ol class="step-list" aria-label="Các bước xác thực">
                <li class="step-item"><span class="step-index" aria-hidden="true">1</span><span>Kiểm tra hộp thư</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">2</span><span>Mở liên kết xác thực</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">3</span><span>Quay lại đăng nhập</span></li>
              </ol>
            </aside>

            <div class="form-stack" role="group" aria-label="Hành động xác thực email">
              <button type="button" class="submit-btn" :disabled="loading" :aria-busy="loading" @click="handleResend">
                {{ loading ? 'Đang xử lý...' : actionLabel }}
              </button>

              <p class="helper-note">Không thấy email? Hãy kiểm tra thư mục spam hoặc gửi lại liên kết xác thực.</p>

              <p class="note-text">Liên kết xác thực giúp bảo vệ tài khoản và đảm bảo chỉ bạn có quyền truy cập vào workspace học tập của mình.</p>

              <p v-if="formMessage" class="submit-message" role="status" aria-live="polite">{{ formMessage }}</p>

              <p v-if="resendCount > 0" class="resend-note" role="status" aria-live="polite">
                Yêu cầu gửi lại đã được ghi nhận trong phiên demo này.
              </p>

              <div class="link-row" aria-label="Liên kết điều hướng">
                <RouterLink to="/auth/login" class="auth-link auth-link--strong">Quay lại đăng nhập</RouterLink>
                <span class="link-divider" aria-hidden="true">•</span>
                <RouterLink to="/auth/register" class="auth-link">Tạo tài khoản mới</RouterLink>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>
