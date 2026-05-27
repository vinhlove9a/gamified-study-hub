<script setup lang="ts">
import { computed, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';

const route = useRoute();
const loading = ref(false);
const formMessage = ref('');

const supportedTypes = ['expired-link', 'invalid-token', 'account-locked', 'verification-failed', 'reset-failed'] as const;
type ErrorType = (typeof supportedTypes)[number] | 'default';

const errorType = computed<ErrorType>(() => {
  const queryType = typeof route.query.type === 'string' ? route.query.type : '';
  return (supportedTypes as readonly string[]).includes(queryType) ? (queryType as ErrorType) : 'default';
});

const errorConfig = computed(() => {
  if (errorType.value === 'expired-link') {
    return {
      title: 'Liên kết đã hết hạn',
      message: 'Liên kết xác thực hoặc đặt lại mật khẩu đã quá thời gian sử dụng. Vui lòng yêu cầu một liên kết mới để tiếp tục.',
      primaryLabel: 'Quay lại đăng nhập',
      iconSymbol: '!',
      iconType: 'warning',
      secondary: { mode: 'local' as const, label: 'Gửi lại yêu cầu' }
    };
  }

  if (errorType.value === 'invalid-token') {
    return {
      title: 'Liên kết không hợp lệ',
      message: 'Mã xác thực không hợp lệ, đã được sử dụng hoặc không khớp với tài khoản của bạn.',
      primaryLabel: 'Quay lại đăng nhập',
      iconSymbol: '⛓',
      iconType: 'warning',
      secondary: { mode: 'local' as const, label: 'Yêu cầu liên kết mới' }
    };
  }

  if (errorType.value === 'account-locked') {
    return {
      title: 'Tài khoản tạm thời bị khóa',
      message: 'Tài khoản của bạn đang bị khóa tạm thời để bảo vệ dữ liệu học tập. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.',
      primaryLabel: 'Quay lại đăng nhập',
      iconSymbol: '🔒',
      iconType: 'locked',
      secondary: { mode: 'support' as const, label: 'Liên hệ hỗ trợ' }
    };
  }

  if (errorType.value === 'verification-failed') {
    return {
      title: 'Xác thực email thất bại',
      message: 'Chúng tôi không thể xác thực email của bạn bằng liên kết hiện tại. Vui lòng yêu cầu gửi lại email xác thực.',
      primaryLabel: 'Quay lại đăng nhập',
      iconSymbol: '!',
      iconType: 'warning',
      secondary: { mode: 'link' as const, label: 'Gửi lại email xác thực', to: '/auth/verify-email' }
    };
  }

  if (errorType.value === 'reset-failed') {
    return {
      title: 'Không thể đặt lại mật khẩu',
      message: 'Yêu cầu đặt lại mật khẩu không còn hợp lệ. Vui lòng gửi lại yêu cầu để nhận liên kết mới.',
      primaryLabel: 'Quay lại đăng nhập',
      iconSymbol: '⛓',
      iconType: 'warning',
      secondary: { mode: 'link' as const, label: 'Gửi lại liên kết đặt lại', to: '/auth/forgot-password' }
    };
  }

  return {
    title: 'Không thể hoàn tất yêu cầu',
    message: 'Liên kết không hợp lệ hoặc đã hết hạn. Vui lòng thử lại hoặc yêu cầu một liên kết mới.',
    primaryLabel: 'Quay lại đăng nhập',
    iconSymbol: '!',
    iconType: 'warning',
    secondary: { mode: 'local' as const, label: 'Yêu cầu liên kết mới' }
  };
});

const handleSecondaryAction = async () => {
  formMessage.value = '';
  loading.value = true;
  await new Promise((resolve) => setTimeout(resolve, 700));
  loading.value = false;

  if (errorConfig.value.secondary.mode === 'support') {
    formMessage.value = 'Thông tin hỗ trợ sẽ được cấu hình ở bước tiếp theo.';
    return;
  }

  formMessage.value = 'Yêu cầu khôi phục sẽ được kết nối API ở bước tiếp theo.';
};
</script>

<template>
  <div class="error-page">
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
          <h1 class="auth-title animate-gradient-text">{{ errorConfig.title }}</h1>
          <p class="auth-subtitle">{{ errorConfig.message }}</p>
        </div>

        <section class="auth-card card-hover animate-slide-up delay-200" aria-label="Thông báo lỗi xác thực">
          <div class="auth-grid">
            <aside class="error-panel" aria-label="Hướng dẫn khôi phục">
              <div class="warning-visual" aria-hidden="true">
                <span class="warning-ring"></span>
                <span class="warning-core" :class="{ 'warning-core--locked': errorConfig.iconType === 'locked' }"></span>
                <span class="warning-icon">{{ errorConfig.iconSymbol }}</span>
              </div>

              <p class="panel-title">Khôi phục truy cập</p>

              <ol class="step-list" aria-label="Các bước khôi phục">
                <li class="step-item"><span class="step-index" aria-hidden="true">1</span><span>Kiểm tra lại liên kết</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">2</span><span>Yêu cầu liên kết mới</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">3</span><span>Quay lại đăng nhập</span></li>
              </ol>
            </aside>

            <div class="action-stack" role="group" aria-label="Hành động khôi phục">
              <RouterLink to="/auth/login" class="submit-btn btn-shimmer">{{ errorConfig.primaryLabel }}</RouterLink>

              <RouterLink
                v-if="errorConfig.secondary.mode === 'link'"
                :to="errorConfig.secondary.to"
                class="secondary-link"
              >
                {{ errorConfig.secondary.label }}
              </RouterLink>

              <button
                v-else-if="errorConfig.secondary.mode === 'local' || errorConfig.secondary.mode === 'support'"
                type="button"
                class="secondary-btn"
                :disabled="loading"
                :aria-busy="loading"
                @click="handleSecondaryAction"
              >
                {{ loading ? 'Đang xử lý...' : errorConfig.secondary.label }}
              </button>

              <p v-if="formMessage" class="submit-message" role="status" aria-live="polite">{{ formMessage }}</p>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.error-page {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 25%, #fce7f3 50%, #ede9fe 75%, #e0f2fe 100%);
  overflow: hidden;
}
.auth-bg { position: fixed; inset: 0; pointer-events: none; }
.blob { position: absolute; border-radius: 9999px; filter: blur(2px); animation: float 8s ease-in-out infinite, morph 8s ease-in-out infinite; }
.blob-a { top: -5rem; left: -4rem; width: 18rem; height: 18rem; background: linear-gradient(135deg, rgba(251, 146, 60, 0.5), rgba(252, 211, 77, 0.4)); }
.blob-b { top: 28%; right: -4rem; width: 16rem; height: 16rem; background: linear-gradient(135deg, rgba(244, 114, 182, 0.35), rgba(251, 113, 133, 0.3)); animation-delay: 1.5s; }
.blob-c { bottom: -6rem; left: 18%; width: 20rem; height: 20rem; background: linear-gradient(135deg, rgba(167, 139, 250, 0.35), rgba(56, 189, 248, 0.3)); animation-delay: 2.5s; }
.blob-d { top: 5rem; right: 30%; width: 12rem; height: 12rem; background: linear-gradient(135deg, rgba(125, 211, 252, 0.28), rgba(14, 165, 233, 0.2)); animation-delay: 0.7s; }
.blob-e { bottom: 26%; right: 8%; width: 14rem; height: 14rem; background: linear-gradient(135deg, rgba(134, 239, 172, 0.28), rgba(20, 184, 166, 0.22)); animation-delay: 3s; }
.shape { position: absolute; opacity: 0.35; animation: float 7s ease-in-out infinite; }
.shape-circle { top: 15%; left: 10%; width: 2.2rem; height: 2.2rem; border-radius: 9999px; border: 2px solid #fb923c; }
.shape-diamond { top: 26%; right: 15%; width: 1.8rem; height: 1.8rem; border: 2px solid #f472b6; transform: rotate(45deg); animation-delay: 1.2s; }
.shape-triangle { bottom: 20%; left: 8%; width: 0; height: 0; border-left: 1rem solid transparent; border-right: 1rem solid transparent; border-bottom: 1.8rem solid rgba(167, 139, 250, 0.5); animation-delay: 2s; }
.shape-star { top: 10%; left: 45%; width: 1.4rem; height: 1.4rem; background: #f9a8d4; clip-path: polygon(50% 0%, 61% 39%, 100% 50%, 61% 61%, 50% 100%, 39% 61%, 0% 50%, 39% 39%); animation-delay: 1.5s; }
.sparkle { position: absolute; width: 0.4rem; height: 0.4rem; border-radius: 9999px; animation: sparkle 3s ease-in-out infinite; }
.sparkle-a { top: 20%; left: 30%; background: #fdba74; }
.sparkle-b { top: 40%; right: 25%; background: #f9a8d4; animation-delay: 0.7s; }
.sparkle-c { bottom: 30%; left: 20%; background: #c4b5fd; animation-delay: 1.5s; }
.sparkle-d { top: 60%; right: 35%; background: #f59e0b; animation-delay: 2s; }
.auth-main { position: relative; z-index: 1; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 1.25rem; }
.auth-shell { width: 100%; max-width: 56rem; }
.auth-brand { text-align: center; margin-bottom: 1rem; }
.brand-name { margin: 0; color: #b45309; font-weight: 600; font-size: 0.95rem; }
.auth-title { margin: 0.35rem 0; font-size: 2rem; line-height: 1.2; font-weight: 700; color: #9a3412; }
.auth-subtitle { margin: 0 auto; color: rgba(146, 64, 14, 0.78); font-size: 0.92rem; line-height: 1.5; max-width: 44rem; }
.animate-gradient-text { background: linear-gradient(90deg, #f97316, #ec4899, #8b5cf6); background-size: 200% auto; -webkit-background-clip: text; background-clip: text; color: transparent; animation: gradient-shift 8s ease infinite; }
.auth-card { border-radius: 1.6rem; border: 1px solid rgba(255, 255, 255, 0.75); background: rgba(255, 255, 255, 0.75); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); box-shadow: 0 22px 56px rgba(251, 146, 60, 0.15); padding: 1.1rem; }
.card-hover { transition: all 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.card-hover:hover { transform: translateY(-4px); box-shadow: 0 25px 60px rgba(251, 146, 60, 0.14); }
.auth-grid { display: grid; gap: 1rem; }
.error-panel { border-radius: 1rem; border: 1px solid #fed7aa; background: linear-gradient(155deg, rgba(255, 255, 255, 0.88), rgba(255, 247, 237, 0.9)); padding: 0.95rem; }
.warning-visual { position: relative; width: 3.6rem; height: 3.2rem; margin-bottom: 0.8rem; }
.warning-ring { position: absolute; inset: -0.4rem; border-radius: 1rem; border: 2px solid rgba(251, 146, 60, 0.36); animation: ring 2.2s ease-out infinite; }
.warning-core { position: absolute; inset: 0.4rem 0.4rem 0.2rem; border-radius: 0.7rem; background: linear-gradient(135deg, #fde68a, #fdba74); box-shadow: 0 8px 20px rgba(251, 146, 60, 0.22); }
.warning-core--locked { background: linear-gradient(135deg, #fde68a, #f59e0b); }
.warning-icon { position: absolute; right: -0.35rem; top: -0.25rem; width: 1.3rem; height: 1.3rem; border-radius: 9999px; background: #fff; color: #92400e; font-size: 0.8rem; font-weight: 700; display: inline-flex; align-items: center; justify-content: center; border: 2px solid #fdba74; }
.panel-title { margin: 0 0 0.7rem; font-size: 0.84rem; font-weight: 700; color: #b45309; text-transform: uppercase; letter-spacing: 0.06em; }
.step-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 0.55rem; }
.step-item { display: flex; align-items: center; gap: 0.55rem; font-size: 0.88rem; color: #9a3412; padding: 0.55rem 0.65rem; border: 1px solid #ffedd5; border-radius: 0.75rem; background: rgba(255, 255, 255, 0.8); }
.step-index { width: 1.2rem; height: 1.2rem; display: inline-flex; align-items: center; justify-content: center; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; color: #fff; background: linear-gradient(135deg, #fb923c, #f59e0b); flex: 0 0 auto; }
.action-stack { display: grid; gap: 0.9rem; }
.submit-btn { width: 100%; border: none; border-radius: 0.95rem; background: linear-gradient(135deg, #fb923c, #f59e0b, #facc15); color: #fff; font-size: 0.95rem; font-weight: 700; padding: 0.85rem 1rem; cursor: pointer; box-shadow: 0 14px 28px rgba(251, 146, 60, 0.3); transition: transform 0.2s ease, box-shadow 0.2s ease; text-decoration: none; text-align: center; }
.submit-btn:hover { transform: translateY(-1px); box-shadow: 0 16px 30px rgba(251, 146, 60, 0.38); }
.secondary-btn,
.secondary-link { width: 100%; border: 1px solid #fdba74; border-radius: 0.9rem; background: rgba(255, 255, 255, 0.8); color: #c2410c; font-size: 0.9rem; font-weight: 600; padding: 0.7rem 0.9rem; text-decoration: none; text-align: center; }
.secondary-btn { cursor: pointer; }
.secondary-btn:disabled { opacity: 0.7; cursor: not-allowed; }
.submit-btn:focus-visible,
.secondary-btn:focus-visible,
.secondary-link:focus-visible {
  outline: 3px solid rgba(249, 115, 22, 0.35);
  outline-offset: 2px;
}
.btn-shimmer { position: relative; overflow: hidden; }
.btn-shimmer::after { content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: linear-gradient(to right, transparent 0%, rgba(255, 255, 255, 0.35) 50%, transparent 100%); transform: rotate(30deg); animation: shimmer 3s infinite; }
.submit-message { margin: 0; border-radius: 0.75rem; padding: 0.65rem 0.8rem; background: #ffedd5; color: #9a3412; font-size: 0.84rem; }
@media (min-width: 768px) {
  .auth-main { padding: 2rem; }
  .auth-card { padding: 1.5rem; }
  .auth-grid { grid-template-columns: minmax(220px, 250px) 1fr; gap: 1.25rem; align-items: start; }
}
@media (max-width: 420px) {
  .auth-card { padding: 0.9rem; }
  .auth-title { font-size: 1.7rem; }
}
.animate-slide-up { animation: slide-up 0.8s ease-out both; }
.animate-bounce-in { animation: bounce-in 0.8s ease-out both; }
.delay-200 { animation-delay: 0.2s; }
@keyframes float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-16px)} }
@keyframes gradient-shift { 0%{background-position:0% 50%} 50%{background-position:100% 50%} 100%{background-position:0% 50%} }
@keyframes morph { 0%,100%{border-radius:60% 40% 30% 70% / 60% 30% 70% 40%} 25%{border-radius:30% 60% 70% 40% / 50% 60% 30% 60%} 50%{border-radius:50% 60% 30% 60% / 30% 60% 70% 40%} 75%{border-radius:60% 40% 60% 30% / 60% 40% 30% 70%} }
@keyframes sparkle { 0%,100%{opacity:0;transform:scale(0) rotate(0deg)} 50%{opacity:1;transform:scale(1) rotate(180deg)} }
@keyframes ring { from{transform:scale(0.88);opacity:1} to{transform:scale(1.5);opacity:0} }
@keyframes shimmer { 0%{transform:translateX(-100%) rotate(30deg)} 100%{transform:translateX(100%) rotate(30deg)} }
@keyframes slide-up { from{opacity:0;transform:translateY(40px)} to{opacity:1;transform:translateY(0)} }
@keyframes bounce-in { 0%{transform:scale(0.3);opacity:0} 50%{transform:scale(1.05)} 70%{transform:scale(0.9)} 100%{transform:scale(1);opacity:1} }
</style>
