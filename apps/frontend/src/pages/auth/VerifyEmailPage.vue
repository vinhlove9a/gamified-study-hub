<script setup lang="ts">
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
  <div class="verify-page">
    <div class="auth-bg" aria-hidden="true">
      <div class="blob blob-a"></div>
      <div class="blob blob-b"></div>
      <div class="blob blob-c"></div>
      <div class="blob blob-d"></div>
      <div class="blob blob-e"></div>

      <div class="shape shape-circle"></div>
      <div class="shape shape-diamond"></div>
      <div class="shape shape-triangle"></div>
      <div class="shape shape-star"></div>

      <div class="sparkle sparkle-a"></div>
      <div class="sparkle sparkle-b"></div>
      <div class="sparkle sparkle-c"></div>
      <div class="sparkle sparkle-d"></div>
    </div>

    <main class="auth-main">
      <div class="auth-shell">
        <div class="auth-brand animate-bounce-in">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title animate-gradient-text">Xác thực email của bạn</h1>
          <p class="auth-subtitle">Chúng tôi đã gửi một liên kết xác thực đến email đăng ký. Vui lòng kiểm tra hộp thư để hoàn tất quá trình tạo tài khoản.</p>
        </div>

        <section class="auth-card card-hover animate-slide-up delay-200" aria-label="Xác thực email">
          <div class="auth-grid">
            <aside class="verify-panel" aria-label="Hướng dẫn xác thực email">
              <div class="mailbox-visual" aria-hidden="true">
                <span class="delivery-pulse"></span>
                <span class="mailbox-body"></span>
                <span class="mailbox-top"></span>
                <span class="verify-badge">✓</span>
              </div>

              <p class="panel-title">Xác thực tài khoản</p>

              <ol class="step-list" aria-label="Các bước xác thực">
                <li class="step-item"><span class="step-index" aria-hidden="true">1</span><span>Kiểm tra hộp thư</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">2</span><span>Mở liên kết xác thực</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">3</span><span>Quay lại đăng nhập</span></li>
              </ol>
            </aside>

            <div class="form-stack" role="group" aria-label="Hành động xác thực email">
              <button type="button" class="submit-btn btn-shimmer" :disabled="loading" :aria-busy="loading" @click="handleResend">
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

<style scoped>
.verify-page {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 25%, #fce7f3 50%, #ede9fe 75%, #e0f2fe 100%);
  overflow: hidden;
}

.auth-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 9999px;
  filter: blur(2px);
  animation: float 8s ease-in-out infinite, morph 8s ease-in-out infinite;
}

.blob-a { top: -5rem; left: -4rem; width: 18rem; height: 18rem; background: linear-gradient(135deg, rgba(251, 146, 60, 0.5), rgba(252, 211, 77, 0.4)); }
.blob-b { top: 28%; right: -4rem; width: 16rem; height: 16rem; background: linear-gradient(135deg, rgba(244, 114, 182, 0.35), rgba(251, 113, 133, 0.3)); animation-delay: 1.5s; }
.blob-c { bottom: -6rem; left: 18%; width: 20rem; height: 20rem; background: linear-gradient(135deg, rgba(167, 139, 250, 0.35), rgba(56, 189, 248, 0.3)); animation-delay: 2.5s; }
.blob-d { top: 5rem; right: 30%; width: 12rem; height: 12rem; background: linear-gradient(135deg, rgba(125, 211, 252, 0.28), rgba(14, 165, 233, 0.2)); animation-delay: 0.7s; }
.blob-e { bottom: 26%; right: 8%; width: 14rem; height: 14rem; background: linear-gradient(135deg, rgba(134, 239, 172, 0.28), rgba(20, 184, 166, 0.22)); animation-delay: 3s; }

.shape {
  position: absolute;
  opacity: 0.35;
  animation: float 7s ease-in-out infinite;
}

.shape-circle { top: 15%; left: 10%; width: 2.2rem; height: 2.2rem; border-radius: 9999px; border: 2px solid #fb923c; }
.shape-diamond { top: 26%; right: 15%; width: 1.8rem; height: 1.8rem; border: 2px solid #f472b6; transform: rotate(45deg); animation-delay: 1.2s; }
.shape-triangle { bottom: 20%; left: 8%; width: 0; height: 0; border-left: 1rem solid transparent; border-right: 1rem solid transparent; border-bottom: 1.8rem solid rgba(167, 139, 250, 0.5); animation-delay: 2s; }
.shape-star { top: 10%; left: 45%; width: 1.4rem; height: 1.4rem; background: #f9a8d4; clip-path: polygon(50% 0%, 61% 39%, 100% 50%, 61% 61%, 50% 100%, 39% 61%, 0% 50%, 39% 39%); animation-delay: 1.5s; }

.sparkle { position: absolute; width: 0.4rem; height: 0.4rem; border-radius: 9999px; animation: sparkle 3s ease-in-out infinite; }
.sparkle-a { top: 20%; left: 30%; background: #fdba74; }
.sparkle-b { top: 40%; right: 25%; background: #f9a8d4; animation-delay: 0.7s; }
.sparkle-c { bottom: 30%; left: 20%; background: #c4b5fd; animation-delay: 1.5s; }
.sparkle-d { top: 60%; right: 35%; background: #f59e0b; animation-delay: 2s; }

.auth-main {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.25rem;
}

.auth-shell {
  width: 100%;
  max-width: 56rem;
}

.auth-brand {
  text-align: center;
  margin-bottom: 1rem;
}

.brand-name {
  margin: 0;
  color: #b45309;
  font-weight: 600;
  font-size: 0.95rem;
}

.auth-title {
  margin: 0.35rem 0;
  font-size: 2rem;
  line-height: 1.2;
  font-weight: 700;
  color: #9a3412;
}

.auth-subtitle {
  margin: 0 auto;
  color: rgba(146, 64, 14, 0.78);
  font-size: 0.92rem;
  line-height: 1.5;
  max-width: 44rem;
}

.animate-gradient-text {
  background: linear-gradient(90deg, #f97316, #ec4899, #8b5cf6);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: gradient-shift 8s ease infinite;
}

.auth-card {
  border-radius: 1.6rem;
  border: 1px solid rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 22px 56px rgba(251, 146, 60, 0.15);
  padding: 1.1rem;
}

.card-hover {
  transition: all 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 25px 60px rgba(251, 146, 60, 0.14);
}

.auth-grid {
  display: grid;
  gap: 1rem;
}

.verify-panel {
  border-radius: 1rem;
  border: 1px solid #fde68a;
  background: linear-gradient(155deg, rgba(255, 255, 255, 0.88), rgba(255, 247, 237, 0.9));
  padding: 0.95rem;
}

.mailbox-visual {
  position: relative;
  width: 3.6rem;
  height: 3rem;
  margin-bottom: 0.8rem;
}

.delivery-pulse {
  position: absolute;
  inset: -0.45rem;
  border-radius: 1rem;
  border: 2px solid rgba(251, 146, 60, 0.42);
  animation: ring 2s ease-out infinite;
}

.mailbox-body {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2rem;
  border-radius: 0.5rem;
  background: linear-gradient(135deg, #fdba74, #fb923c);
  box-shadow: 0 8px 20px rgba(251, 146, 60, 0.3);
}

.mailbox-top {
  position: absolute;
  top: 0.25rem;
  left: 0.35rem;
  right: 0.35rem;
  height: 1rem;
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.85);
  clip-path: polygon(0 100%, 50% 32%, 100% 100%, 100% 50%, 50% 0, 0 50%);
}

.verify-badge {
  position: absolute;
  top: -0.2rem;
  right: -0.35rem;
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 9999px;
  background: #15803d;
  color: #fff;
  font-size: 0.75rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}

.panel-title {
  margin: 0 0 0.7rem;
  font-size: 0.84rem;
  font-weight: 700;
  color: #b45309;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.step-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.55rem;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  font-size: 0.88rem;
  color: #9a3412;
  padding: 0.55rem 0.65rem;
  border: 1px solid #ffedd5;
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.8);
}

.step-index {
  width: 1.2rem;
  height: 1.2rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #fb923c, #f59e0b);
  flex: 0 0 auto;
}

.form-stack {
  display: grid;
  gap: 0.95rem;
}

.submit-btn {
  width: 100%;
  border: none;
  border-radius: 0.95rem;
  background: linear-gradient(135deg, #fb923c, #f59e0b, #facc15);
  color: #fff;
  font-size: 0.95rem;
  font-weight: 700;
  padding: 0.85rem 1rem;
  cursor: pointer;
  box-shadow: 0 14px 28px rgba(251, 146, 60, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(251, 146, 60, 0.38);
}

.submit-btn:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.btn-shimmer {
  position: relative;
  overflow: hidden;
}

.btn-shimmer::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(to right, transparent 0%, rgba(255, 255, 255, 0.35) 50%, transparent 100%);
  transform: rotate(30deg);
  animation: shimmer 3s infinite;
}

.helper-note {
  margin: 0;
  font-size: 0.82rem;
  line-height: 1.45;
  color: #9a3412;
}

.note-text {
  margin: 0;
  font-size: 0.8rem;
  line-height: 1.45;
  color: #9a3412;
}

.submit-message,
.resend-note {
  margin: 0;
  border-radius: 0.75rem;
  padding: 0.65rem 0.8rem;
  background: #ffedd5;
  color: #9a3412;
  font-size: 0.84rem;
}

.link-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.link-divider {
  color: #f59e0b;
}

.auth-link {
  color: #ea580c;
  text-decoration: none;
  font-size: 0.87rem;
  font-weight: 600;
}

.auth-link:hover,
.auth-link:focus-visible {
  text-decoration: underline;
}

.auth-link--strong {
  font-weight: 700;
}

@media (min-width: 768px) {
  .auth-main {
    padding: 2rem;
  }

  .auth-card {
    padding: 1.5rem;
  }

  .auth-grid {
    grid-template-columns: minmax(220px, 250px) 1fr;
    gap: 1.25rem;
    align-items: start;
  }
}

@media (max-width: 420px) {
  .auth-card {
    padding: 0.9rem;
  }

  .auth-title {
    font-size: 1.7rem;
  }

  .helper-note,
  .note-text {
    font-size: 0.78rem;
  }
}

.animate-slide-up { animation: slide-up 0.8s ease-out both; }
.animate-bounce-in { animation: bounce-in 0.8s ease-out both; }
.delay-200 { animation-delay: 0.2s; }

@keyframes float {
  0%,
  100% { transform: translateY(0); }
  50% { transform: translateY(-16px); }
}

@keyframes gradient-shift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes morph {
  0%,
  100% { border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%; }
  25% { border-radius: 30% 60% 70% 40% / 50% 60% 30% 60%; }
  50% { border-radius: 50% 60% 30% 60% / 30% 60% 70% 40%; }
  75% { border-radius: 60% 40% 60% 30% / 60% 40% 30% 70%; }
}

@keyframes sparkle {
  0%,
  100% { opacity: 0; transform: scale(0) rotate(0deg); }
  50% { opacity: 1; transform: scale(1) rotate(180deg); }
}

@keyframes ring {
  from {
    transform: scale(0.85);
    opacity: 1;
  }
  to {
    transform: scale(1.5);
    opacity: 0;
  }
}

@keyframes shimmer {
  0% { transform: translateX(-100%) rotate(30deg); }
  100% { transform: translateX(100%) rotate(30deg); }
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounce-in {
  0% { transform: scale(0.3); opacity: 0; }
  50% { transform: scale(1.05); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); opacity: 1; }
}
</style>
