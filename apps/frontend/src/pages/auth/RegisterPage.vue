<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { useAuthSession } from '@/features/auth/authSession';
import { ApiError } from '@/lib/api/apiError';
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import AuthShowcasePanel from '@/components/auth/AuthShowcasePanel.vue';

interface FormState {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  acceptedTerms: boolean;
}

const form = reactive<FormState>({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptedTerms: false
});
const router = useRouter();
const { setSessionFromAuthResponse } = useAuthSession();

const loading = ref(false);
const formMessage = ref('');
const errors = reactive<{
  fullName?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  acceptedTerms?: string;
}>({});

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const applyFieldErrors = (fieldErrors?: Array<{ field: string; message: string }>) => {
  if (!fieldErrors) return;
  for (const fieldError of fieldErrors) {
    if (fieldError.field === 'fullName') {
      errors.fullName = fieldError.message;
    }
    if (fieldError.field === 'email') {
      errors.email = fieldError.message;
    }
    if (fieldError.field === 'password') {
      errors.password = fieldError.message;
    }
  }
};

const passwordStrength = computed(() => {
  const value = form.password.trim();
  if (value.length < 8) {
    return { label: 'Yếu', level: 'weak' };
  }

  const hasLetter = /[A-Za-z]/.test(value);
  const hasNumber = /\d/.test(value);
  if (hasLetter && hasNumber) {
    return { label: 'Mạnh', level: 'strong' };
  }

  return { label: 'Trung bình', level: 'medium' };
});

const validate = () => {
  errors.fullName = '';
  errors.email = '';
  errors.password = '';
  errors.confirmPassword = '';
  errors.acceptedTerms = '';

  if (!form.fullName.trim()) {
    errors.fullName = 'Họ và tên không được để trống';
  }

  if (!form.email.trim()) {
    errors.email = 'Email không được để trống';
  } else if (!emailRegex.test(form.email)) {
    errors.email = 'Email không đúng định dạng';
  }

  if (!form.password.trim()) {
    errors.password = 'Mật khẩu không được để trống';
  } else if (form.password.trim().length < 8) {
    errors.password = 'Mật khẩu nên có ít nhất 8 ký tự';
  }

  if (form.confirmPassword !== form.password) {
    errors.confirmPassword = 'Xác nhận mật khẩu không khớp';
  }

  if (!form.acceptedTerms) {
    errors.acceptedTerms = 'Bạn cần đồng ý điều khoản trước khi tạo tài khoản';
  }

  return !errors.fullName && !errors.email && !errors.password && !errors.confirmPassword && !errors.acceptedTerms;
};

const handleSubmit = async () => {
  formMessage.value = '';
  if (!validate()) return;

  loading.value = true;
  try {
    const response = await authApi.register({
      fullName: form.fullName.trim(),
      email: form.email.trim(),
      password: form.password
    });
    if (response.accessToken) {
      setSessionFromAuthResponse(response);
    }
    formMessage.value = 'Tạo tài khoản thành công.';
    await router.push('/auth/notice?type=registration-success');
  } catch (error) {
    if (error instanceof ApiError) {
      applyFieldErrors(error.fieldErrors);
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
  <div class="register-page cosmic-page">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-split">
        <div class="auth-shell">
        <div class="auth-brand animate-bounce-in">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title cosmic-gradient-text">Tạo tài khoản mới</h1>
          <p class="auth-subtitle">Bắt đầu xây dựng cổng tài liệu học tập cho cộng đồng của bạn.</p>
        </div>

        <section class="auth-card cosmic-glass cosmic-glass-hover animate-slide-up delay-200" aria-label="Form đăng ký">
          <div class="auth-grid">
            <aside class="setup-panel" aria-label="Lộ trình khởi tạo tài khoản">
              <p class="setup-title">Lộ trình khởi tạo</p>
              <ul class="setup-list">
                <li class="setup-item is-current"><span class="dot" aria-hidden="true"></span>Tạo tài khoản</li>
                <li class="setup-item"><span class="dot" aria-hidden="true"></span>Thiết lập workspace</li>
                <li class="setup-item"><span class="dot" aria-hidden="true"></span>Tải tài liệu đầu tiên</li>
                <li class="setup-item"><span class="dot" aria-hidden="true"></span>Mời học viên tham gia</li>
              </ul>
            </aside>

            <form class="form-stack" novalidate @submit.prevent="handleSubmit">
              <div>
                <label for="full-name" class="field-label">Họ và tên</label>
                <input
                  id="full-name"
                  v-model="form.fullName"
                  class="field-input"
                  :class="{ 'field-input--error': errors.fullName }"
                  type="text"
                  autocomplete="name"
                  required
                  :disabled="loading"
                  :aria-invalid="Boolean(errors.fullName)"
                  :aria-describedby="errors.fullName ? 'full-name-error' : undefined"
                />
                <p v-if="errors.fullName" id="full-name-error" class="field-error" role="alert">{{ errors.fullName }}</p>
              </div>

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

              <div>
                <label for="password" class="field-label">Mật khẩu</label>
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
                <label for="confirm-password" class="field-label">Xác nhận mật khẩu</label>
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

              <div>
                <label class="terms-label" for="accepted-terms">
                  <input
                    id="accepted-terms"
                    v-model="form.acceptedTerms"
                    class="terms-checkbox"
                    type="checkbox"
                    :disabled="loading"
                    :aria-invalid="Boolean(errors.acceptedTerms)"
                    :aria-describedby="errors.acceptedTerms ? 'accepted-terms-error' : undefined"
                  />
                  <span>Tôi đồng ý với Điều khoản sử dụng và Chính sách quyền riêng tư.</span>
                </label>
                <p v-if="errors.acceptedTerms" id="accepted-terms-error" class="field-error" role="alert">{{ errors.acceptedTerms }}</p>
              </div>

              <button type="submit" class="submit-btn cosmic-btn" :disabled="loading" :aria-busy="loading">
                {{ loading ? 'Đang xử lý...' : 'Tạo tài khoản' }}
              </button>

              <p class="helper-note">Sau khi tạo tài khoản, bạn có thể tạo workspace đầu tiên cho CLB, nhóm học hoặc trung tâm của mình.</p>

              <p v-if="formMessage" class="submit-message" role="status" aria-live="polite">{{ formMessage }}</p>

              <p class="register-row">
                Đã có tài khoản?
                <RouterLink to="/auth/login" class="auth-link auth-link--strong">Đăng nhập</RouterLink>
              </p>
            </form>
          </div>
        </section>
        </div>

        <AuthShowcasePanel variant="register" class="auth-aside" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.register-page {
  overflow: hidden;
}

.auth-main {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.25rem;
}

.auth-split {
  width: 100%;
  display: grid;
  gap: 1.5rem;
  justify-items: center;
}

.auth-shell {
  width: 100%;
  max-width: 56rem;
}

.auth-aside {
  display: none;
}

@media (min-width: 1200px) {
  .auth-split {
    max-width: 84rem;
    grid-template-columns: minmax(40rem, 56rem) 1fr;
    align-items: start;
    gap: 2.5rem;
    justify-items: stretch;
  }

  .auth-shell {
    margin: 0;
  }

  .auth-aside {
    display: flex;
  }
}

.auth-brand {
  text-align: center;
  margin-bottom: 1rem;
}

.brand-name {
  margin: 0;
  color: oklch(0.82 0.16 195);
  font-weight: 600;
  font-size: 0.95rem;
}

.auth-title {
  margin: 0.35rem 0;
  font-size: 2rem;
  line-height: 1.2;
  font-weight: 700;
}

.auth-subtitle {
  margin: 0 auto;
  color: oklch(0.7 0.03 240 / 85%);
  font-size: 0.92rem;
  line-height: 1.5;
  max-width: 42rem;
}

.auth-card {
  border-radius: 1.6rem;
  padding: 1.1rem;
}

.auth-grid {
  display: grid;
  gap: 1rem;
}

.setup-panel {
  border-radius: 1rem;
  border: 1px solid oklch(0.68 0.22 350 / 25%);
  background: linear-gradient(155deg, oklch(0.18 0.04 280 / 50%), oklch(0.12 0.03 280 / 40%));
  padding: 0.9rem;
}

.setup-title {
  margin: 0 0 0.7rem;
  font-size: 0.84rem;
  font-weight: 700;
  color: oklch(0.72 0.22 330);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.setup-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.5rem;
}

.setup-item {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  font-size: 0.88rem;
  color: oklch(0.85 0.02 240);
  padding: 0.55rem 0.65rem;
  border: 1px solid oklch(0.7 0.1 280 / 14%);
  border-radius: 0.75rem;
  background: oklch(0.13 0.025 280 / 40%);
  animation: pulse-card 3s ease-in-out infinite;
}

.setup-item:nth-child(2) { animation-delay: 0.2s; }
.setup-item:nth-child(3) { animation-delay: 0.4s; }
.setup-item:nth-child(4) { animation-delay: 0.6s; }

.setup-item.is-current {
  border-color: oklch(0.78 0.18 195 / 45%);
  box-shadow: 0 0 18px oklch(0.78 0.18 195 / 12%);
}

.dot {
  width: 0.65rem;
  height: 0.65rem;
  border-radius: 9999px;
  background: linear-gradient(135deg, oklch(0.82 0.16 195), oklch(0.68 0.22 350));
  box-shadow: 0 0 0 3px oklch(0.78 0.18 195 / 18%);
}

.form-stack {
  display: grid;
  gap: 0.92rem;
}

.field-label {
  display: block;
  margin-bottom: 0.45rem;
  margin-left: 0.15rem;
  font-size: 0.87rem;
  color: oklch(0.85 0.03 220);
  font-weight: 600;
}

/* Cosmic glass field. */
.field-input {
  width: 100%;
  border: 1px solid oklch(0.7 0.1 280 / 25%);
  background: oklch(0.13 0.025 280 / 55%);
  color: oklch(0.96 0.01 240);
  border-radius: 0.95rem;
  padding: 0.72rem 0.95rem;
  font-size: 0.95rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.field-input::placeholder {
  color: oklch(0.7 0.03 240 / 55%);
}

.field-input:hover {
  border-color: oklch(0.78 0.18 195 / 35%);
}

.field-input:focus {
  outline: none;
  border-color: oklch(0.78 0.18 195 / 70%);
  background: oklch(0.13 0.025 280 / 75%);
  box-shadow: 0 0 0 3px oklch(0.78 0.18 195 / 22%), 0 0 18px oklch(0.78 0.18 195 / 18%);
}

.field-input--error {
  border-color: oklch(0.65 0.25 25 / 80%);
}

.field-error {
  margin: 0.35rem 0 0;
  color: oklch(0.72 0.2 20);
  font-size: 0.8rem;
}

.strength-text {
  margin: 0.45rem 0 0;
  font-size: 0.8rem;
  color: oklch(0.7 0.03 240 / 85%);
}

.strength-text--weak strong { color: oklch(0.72 0.2 20); }
.strength-text--medium strong { color: oklch(0.78 0.16 80); }
.strength-text--strong strong { color: oklch(0.78 0.16 150); }

.terms-label {
  display: flex;
  align-items: flex-start;
  gap: 0.55rem;
  color: oklch(0.8 0.03 230);
  font-size: 0.86rem;
  line-height: 1.4;
}

.terms-checkbox {
  width: 1rem;
  height: 1rem;
  margin-top: 0.16rem;
  accent-color: oklch(0.78 0.18 195);
  flex: 0 0 auto;
}

.submit-btn {
  width: 100%;
  border-radius: 0.95rem;
  font-size: 0.95rem;
  padding: 0.85rem 1rem;
  cursor: pointer;
}

.helper-note {
  margin: 0;
  font-size: 0.82rem;
  line-height: 1.45;
  color: oklch(0.7 0.03 240 / 75%);
}

.submit-message {
  margin: 0;
  border-radius: 0.75rem;
  padding: 0.65rem 0.8rem;
  background: oklch(0.78 0.18 195 / 10%);
  border: 1px solid oklch(0.78 0.18 195 / 25%);
  color: oklch(0.9 0.05 210);
  font-size: 0.84rem;
}

.register-row {
  margin: 0;
  text-align: center;
  color: oklch(0.7 0.03 240 / 85%);
  font-size: 0.9rem;
}

.auth-link {
  color: oklch(0.82 0.16 195);
  text-decoration: none;
  font-size: 0.87rem;
  font-weight: 600;
}

.auth-link:hover,
.auth-link:focus-visible {
  text-decoration: underline;
  color: oklch(0.88 0.14 200);
}

.auth-link--strong {
  margin-left: 0.25rem;
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
    grid-template-columns: minmax(210px, 240px) 1fr;
    gap: 1.2rem;
    align-items: start;
  }
}

@media (max-width: 420px) {
  .auth-card {
    padding: 0.9rem;
  }

  .auth-title {
    font-size: 1.65rem;
  }

  .terms-label {
    font-size: 0.82rem;
  }
}

.animate-slide-up { animation: slide-up 0.8s ease-out both; }
.animate-bounce-in { animation: bounce-in 0.8s ease-out both; }
.delay-200 { animation-delay: 0.2s; }

@keyframes pulse-card {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-2px); }
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
