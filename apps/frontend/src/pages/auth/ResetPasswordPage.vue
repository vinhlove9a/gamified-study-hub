<script setup lang="ts">
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import { computed, reactive, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { ApiError } from '@/lib/api/apiError';

interface FormState {
  password: string;
  confirmPassword: string;
}

const form = reactive<FormState>({
  password: '',
  confirmPassword: ''
});
const route = useRoute();
const router = useRouter();

const loading = ref(false);
const formMessage = ref('');
const errors = reactive<{
  password?: string;
  confirmPassword?: string;
}>({});

const passwordStrength = computed(() => {
  const value = form.password.trim();
  if (value.length < 8) {
    return { label: 'Yếu', level: 'weak' };
  }

  const hasLetter = /[A-Za-z]/.test(value);
  const hasNumber = /\d/.test(value);
  const hasSpecial = /[^A-Za-z\d\s]/.test(value);

  if (hasLetter && hasNumber && hasSpecial) {
    return { label: 'Mạnh', level: 'strong' };
  }

  if (hasLetter && hasNumber) {
    return { label: 'Mạnh', level: 'strong' };
  }

  return { label: 'Trung bình', level: 'medium' };
});

const checklist = computed(() => {
  const password = form.password.trim();
  return {
    minLength: password.length >= 8,
    hasLetter: /[A-Za-z]/.test(password),
    hasNumber: /\d/.test(password),
    matched: !!form.confirmPassword && form.password === form.confirmPassword
  };
});

const validate = () => {
  errors.password = '';
  errors.confirmPassword = '';

  if (!form.password.trim()) {
    errors.password = 'Mật khẩu mới không được để trống';
  } else if (form.password.trim().length < 8) {
    errors.password = 'Mật khẩu nên có ít nhất 8 ký tự';
  }

  if (!form.confirmPassword.trim()) {
    errors.confirmPassword = 'Xác nhận mật khẩu không được để trống';
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = 'Xác nhận mật khẩu không khớp';
  }

  const token = typeof route.query.token === 'string' ? route.query.token.trim() : '';
  if (!token) {
    formMessage.value = 'Thiếu token đặt lại mật khẩu. Vui lòng mở lại liên kết từ email.';
  }

  return !errors.password && !errors.confirmPassword && !!token;
};

const handleSubmit = async () => {
  formMessage.value = '';
  if (!validate()) return;

  const token = typeof route.query.token === 'string' ? route.query.token.trim() : '';
  if (!token) {
    formMessage.value = 'Thiếu token đặt lại mật khẩu. Vui lòng mở lại liên kết từ email.';
    return;
  }

  loading.value = true;
  try {
    const response = await authApi.resetPassword({
      token,
      newPassword: form.password
    });
    formMessage.value = response.message;
    await router.push('/auth/notice?type=password-reset-success');
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
  <div class="reset-page cosmic-page auth-cosmic">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-shell">
        <div class="auth-brand animate-bounce-in">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title animate-gradient-text">Đặt lại mật khẩu</h1>
          <p class="auth-subtitle">Tạo mật khẩu mới để tiếp tục sử dụng Gamified Study Hub một cách an toàn.</p>
        </div>

        <section class="auth-card card-hover animate-slide-up delay-200" aria-label="Form đặt lại mật khẩu">
          <div class="auth-grid">
            <aside class="security-panel" aria-label="Quy trình đặt lại mật khẩu an toàn">
              <div class="lock-visual" aria-hidden="true">
                <span class="lock-pulse"></span>
                <span class="lock-body"></span>
                <span class="lock-shackle"></span>
              </div>

              <p class="panel-title">Đặt lại an toàn</p>

              <ol class="step-list" aria-label="Các bước bảo mật">
                <li class="step-item"><span class="step-index" aria-hidden="true">1</span><span>Kiểm tra liên kết</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">2</span><span>Tạo mật khẩu mới</span></li>
                <li class="step-item"><span class="step-index" aria-hidden="true">3</span><span>Đăng nhập an toàn</span></li>
              </ol>
            </aside>

            <form class="form-stack" novalidate @submit.prevent="handleSubmit">
              <div>
                <label for="password" class="field-label">Mật khẩu mới</label>
                <input
                  id="password"
                  v-model="form.password"
                  class="field-input"
                  :class="{ 'field-input--error': errors.password }"
                  type="password"
                  autocomplete="new-password"
                  required
                  :disabled="loading"
                  :aria-invalid="Boolean(errors.password)"
                  :aria-describedby="errors.password ? 'password-error password-strength' : 'password-strength'"
                />
                <p id="password-strength" class="strength-text" :class="`strength-text--${passwordStrength.level}`">
                  Độ mạnh mật khẩu: <strong>{{ passwordStrength.label }}</strong>
                </p>
                <p v-if="errors.password" id="password-error" class="field-error" role="alert">{{ errors.password }}</p>
              </div>

              <div>
                <label for="confirm-password" class="field-label">Xác nhận mật khẩu mới</label>
                <input
                  id="confirm-password"
                  v-model="form.confirmPassword"
                  class="field-input"
                  :class="{ 'field-input--error': errors.confirmPassword }"
                  type="password"
                  autocomplete="new-password"
                  required
                  :disabled="loading"
                  :aria-invalid="Boolean(errors.confirmPassword)"
                  :aria-describedby="errors.confirmPassword ? 'confirm-password-error' : undefined"
                />
                <p v-if="errors.confirmPassword" id="confirm-password-error" class="field-error" role="alert">{{ errors.confirmPassword }}</p>
              </div>

              <ul class="requirement-list" aria-label="Yêu cầu mật khẩu">
                <li class="requirement-item">
                  <span class="requirement-state" aria-hidden="true">{{ checklist.minLength ? '✓' : '○' }}</span>
                  <span>Tối thiểu 8 ký tự</span>
                </li>
                <li class="requirement-item">
                  <span class="requirement-state" aria-hidden="true">{{ checklist.hasLetter ? '✓' : '○' }}</span>
                  <span>Có chữ cái</span>
                </li>
                <li class="requirement-item">
                  <span class="requirement-state" aria-hidden="true">{{ checklist.hasNumber ? '✓' : '○' }}</span>
                  <span>Có số</span>
                </li>
                <li class="requirement-item">
                  <span class="requirement-state" aria-hidden="true">{{ checklist.matched ? '✓' : '○' }}</span>
                  <span>Hai mật khẩu khớp nhau</span>
                </li>
              </ul>

              <button type="submit" class="submit-btn btn-shimmer" :disabled="loading" :aria-busy="loading">
                {{ loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu' }}
              </button>

              <p class="helper-note">Hãy chọn mật khẩu đủ mạnh để bảo vệ tài khoản và dữ liệu học tập của cộng đồng bạn.</p>

              <p v-if="formMessage" class="submit-message" role="status" aria-live="polite">{{ formMessage }}</p>

              <div class="link-row" aria-label="Liên kết điều hướng">
                <RouterLink to="/auth/login" class="auth-link auth-link--strong">Quay lại đăng nhập</RouterLink>
                <span class="link-divider" aria-hidden="true">•</span>
                <RouterLink to="/auth/forgot-password" class="auth-link">Gửi lại liên kết đặt lại mật khẩu</RouterLink>
              </div>
            </form>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>
