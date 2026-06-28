<script setup lang="ts">
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
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
  <div class="error-page cosmic-page auth-cosmic">
    <StarfieldCanvas />

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
