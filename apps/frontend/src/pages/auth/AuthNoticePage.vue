<script setup lang="ts">
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import { computed, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { ApiError } from '@/lib/api/apiError';

const route = useRoute();
const loading = ref(false);
const formMessage = ref('');
const queryEmail = computed(() => (typeof route.query.email === 'string' ? route.query.email.trim() : ''));

const supportedTypes = ['registration-success', 'reset-link-sent', 'password-reset-success', 'email-verified'] as const;
type NoticeType = (typeof supportedTypes)[number] | 'default';

const noticeType = computed<NoticeType>(() => {
  const queryType = typeof route.query.type === 'string' ? route.query.type : '';
  return (supportedTypes as readonly string[]).includes(queryType) ? (queryType as NoticeType) : 'default';
});

const noticeConfig = computed(() => {
  if (noticeType.value === 'registration-success') {
    return {
      title: 'Tạo tài khoản thành công',
      message: 'Tài khoản của bạn đã được tạo. Vui lòng kiểm tra email để xác thực tài khoản trước khi đăng nhập.',
      primaryLabel: 'Đi tới đăng nhập',
      icon: 'success',
      secondaryLabel: 'Gửi lại email xác thực',
      secondaryMode: 'resend' as const
    };
  }

  if (noticeType.value === 'reset-link-sent') {
    return {
      title: 'Đã gửi liên kết đặt lại mật khẩu',
      message: 'Hãy kiểm tra email để tiếp tục đặt lại mật khẩu. Nếu không thấy email, hãy kiểm tra thư mục spam hoặc thử gửi lại.',
      primaryLabel: 'Quay lại đăng nhập',
      icon: 'info',
      secondaryLabel: 'Gửi lại liên kết',
      secondaryMode: 'resend' as const
    };
  }

  if (noticeType.value === 'password-reset-success') {
    return {
      title: 'Mật khẩu đã được cập nhật',
      message: 'Bạn có thể đăng nhập bằng mật khẩu mới để tiếp tục sử dụng Gamified Study Hub.',
      primaryLabel: 'Đăng nhập ngay',
      icon: 'success',
      secondaryLabel: '',
      secondaryMode: 'none' as const
    };
  }

  if (noticeType.value === 'email-verified') {
    return {
      title: 'Email đã được xác thực',
      message: 'Tài khoản của bạn đã sẵn sàng để sử dụng. Hãy đăng nhập để bắt đầu xây dựng cổng học tập cho cộng đồng của bạn.',
      primaryLabel: 'Đăng nhập ngay',
      icon: 'success',
      secondaryLabel: '',
      secondaryMode: 'none' as const
    };
  }

  return {
    title: 'Kiểm tra email của bạn',
    message: 'Chúng tôi đã gửi hướng dẫn tiếp theo đến email của bạn. Vui lòng kiểm tra hộp thư để tiếp tục.',
    primaryLabel: 'Quay lại đăng nhập',
    icon: 'info',
    secondaryLabel: '',
    secondaryMode: 'none' as const
  };
});

const handleResend = async () => {
  formMessage.value = '';
  if (!queryEmail.value) {
    formMessage.value = 'Thiếu email để gửi lại yêu cầu.';
    return;
  }

  loading.value = true;
  try {
    if (noticeType.value === 'registration-success') {
      const response = await authApi.resendVerification({ email: queryEmail.value });
      formMessage.value = response.message;
      return;
    }
    if (noticeType.value === 'reset-link-sent') {
      const response = await authApi.forgotPassword({ email: queryEmail.value });
      formMessage.value = response.message;
      return;
    }
    formMessage.value = 'Không có hành động gửi lại cho trạng thái hiện tại.';
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
  <div class="notice-page cosmic-page auth-cosmic">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-shell">
        <div class="auth-brand animate-bounce-in">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title animate-gradient-text">{{ noticeConfig.title }}</h1>
          <p class="auth-subtitle">{{ noticeConfig.message }}</p>
        </div>

        <section class="auth-card card-hover animate-slide-up delay-200" aria-label="Thông báo xác thực">
          <div class="auth-grid">
            <aside class="notice-panel" aria-label="Các bước tiếp theo">
              <div class="status-visual" aria-hidden="true">
                <span class="status-ring"></span>
                <span class="status-core" :class="{ 'status-core--success': noticeConfig.icon === 'success' }"></span>
                <span class="status-icon">{{ noticeConfig.icon === 'success' ? '✓' : '✉' }}</span>
              </div>

              <p class="panel-title">Bước tiếp theo</p>

              <ol class="step-list" aria-label="Lộ trình tiếp theo">
                <li class="step-item"><span class="step-index" aria-hidden="true">1</span><span>Kiểm tra thông báo</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">2</span><span>Hoàn tất bước xác thực</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">3</span><span>Quay lại đăng nhập</span></li>
              </ol>
            </aside>

            <div class="action-stack" role="group" aria-label="Hành động thông báo">
              <RouterLink to="/auth/login" class="submit-btn btn-shimmer">{{ noticeConfig.primaryLabel }}</RouterLink>

              <button
                v-if="noticeConfig.secondaryMode === 'resend'"
                type="button"
                class="secondary-btn"
                :disabled="loading"
                :aria-busy="loading"
                @click="handleResend"
              >
                {{ loading ? 'Đang xử lý...' : noticeConfig.secondaryLabel }}
              </button>

              <p v-if="formMessage" class="submit-message" role="status" aria-live="polite">{{ formMessage }}</p>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>
