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
        <div class="auth-brand">
          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title">Tạo tài khoản mới</h1>
          <p class="auth-subtitle">Bắt đầu xây dựng cổng tài liệu học tập cho cộng đồng của bạn.</p>
        </div>

        <section class="auth-card" aria-label="Form đăng ký">
          <div class="auth-grid">
            <aside class="setup-panel" aria-label="Lộ trình khởi tạo tài khoản">
              <p class="panel-title">Lộ trình khởi tạo</p>
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

              <button type="submit" class="submit-btn" :disabled="loading" :aria-busy="loading">
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
  background:
    radial-gradient(ellipse 80% 50% at 20% 0%, rgba(124, 58, 237, 0.05) 0%, transparent 60%),
    radial-gradient(ellipse 80% 50% at 80% 100%, rgba(34, 211, 238, 0.03) 0%, transparent 60%),
    #09090E;
  background-attachment: fixed;
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

/* ── Brand ─────────────────────────────────────────────────────────────── */
.auth-brand {
  text-align: center;
  margin-bottom: 1.25rem;
  animation: reg-fade-in 0.4s ease-out both;
}

.brand-name {
  margin: 0;
  color: #6B7280;
  font-weight: 500;
  font-size: 0.82rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.auth-title {
  margin: 0.5rem 0;
  font-size: 1.75rem;
  line-height: 1.25;
  font-weight: 600;
  color: #E8E8F0;
  letter-spacing: -0.01em;
}

.auth-subtitle {
  margin: 0 auto;
  color: #8B8B9F;
  font-size: 0.9rem;
  line-height: 1.6;
  max-width: 42rem;
}

/* ── Card ──────────────────────────────────────────────────────────────── */
.auth-card {
  border-radius: 1.25rem;
  padding: 1.25rem;
  background: rgba(14, 14, 22, 0.72);
  backdrop-filter: blur(24px) saturate(140%);
  -webkit-backdrop-filter: blur(24px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.02) inset,
    0 24px 48px rgba(0, 0, 0, 0.6);
  animation: reg-fade-up 0.5s ease-out both;
}

.auth-grid {
  display: grid;
  gap: 1.25rem;
}

/* ── Setup panel ───────────────────────────────────────────────────────── */
.setup-panel {
  border-radius: 0.875rem;
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.02);
  padding: 1.25rem;
}

.panel-title {
  margin: 0 0 0.75rem;
  font-size: 0.72rem;
  font-weight: 600;
  color: #6B7280;
  text-transform: uppercase;
  letter-spacing: 0.1em;
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
  gap: 0.625rem;
  font-size: 0.85rem;
  color: #8B8B9F;
  padding: 0.5rem 0.625rem;
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.01);
}

.setup-item.is-current {
  border-color: rgba(34, 211, 238, 0.2);
  background: rgba(34, 211, 238, 0.04);
  color: #C0C0D4;
}

.dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.15);
  flex: 0 0 auto;
}

.setup-item.is-current .dot {
  background: #22D3EE;
  box-shadow: 0 0 8px rgba(34, 211, 238, 0.4);
}

/* ── Form ──────────────────────────────────────────────────────────────── */
.form-stack {
  display: grid;
  gap: 0.875rem;
}

.field-label {
  display: block;
  margin-bottom: 0.375rem;
  font-size: 0.82rem;
  color: #8B8B9F;
  font-weight: 500;
}

.field-input {
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  color: #E8E8F0;
  border-radius: 0.625rem;
  padding: 0.7rem 0.875rem;
  font-size: 0.9rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.field-input::placeholder {
  color: #5A5A6E;
}

.field-input:hover {
  border-color: rgba(255, 255, 255, 0.12);
}

.field-input:focus {
  outline: none;
  border-color: rgba(34, 211, 238, 0.5);
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 0 3px rgba(34, 211, 238, 0.12), 0 0 20px rgba(34, 211, 238, 0.06);
}

.field-input--error {
  border-color: rgba(239, 68, 68, 0.5);
}

.field-error {
  margin: 0.375rem 0 0;
  color: #EF4444;
  font-size: 0.78rem;
}

.strength-text {
  margin: 0.375rem 0 0;
  font-size: 0.78rem;
  color: #5A5A6E;
}

.strength-text--weak strong { color: #EF4444; }
.strength-text--medium strong { color: #F59E0B; }
.strength-text--strong strong { color: #10B981; }

.terms-label {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  color: #8B8B9F;
  font-size: 0.84rem;
  line-height: 1.5;
}

.terms-checkbox {
  width: 1rem;
  height: 1rem;
  margin-top: 0.125rem;
  accent-color: #22D3EE;
  flex: 0 0 auto;
}

/* ── Submit button ─────────────────────────────────────────────────────── */
.submit-btn {
  width: 100%;
  border: 1px solid rgba(34, 211, 238, 0.3);
  background: linear-gradient(135deg, #22D3EE, #7C3AED);
  color: #09090E;
  border-radius: 0.625rem;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 0.75rem 1rem;
  cursor: pointer;
  box-shadow: 0 0 20px rgba(34, 211, 238, 0.12);
  transition: box-shadow 0.2s ease, filter 0.2s ease, transform 0.15s ease;
}

.submit-btn:hover:not(:disabled) {
  filter: brightness(1.1);
  box-shadow: 0 0 28px rgba(34, 211, 238, 0.2);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── Notes & links ─────────────────────────────────────────────────────── */
.helper-note {
  margin: 0;
  font-size: 0.8rem;
  line-height: 1.5;
  color: #5A5A6E;
}

.submit-message {
  margin: 0;
  border-radius: 0.5rem;
  padding: 0.625rem 0.75rem;
  background: rgba(34, 211, 238, 0.05);
  border: 1px solid rgba(34, 211, 238, 0.12);
  color: #C0C0D4;
  font-size: 0.82rem;
}

.register-row {
  margin: 0;
  text-align: center;
  color: #8B8B9F;
  font-size: 0.88rem;
}

.auth-link {
  color: #22D3EE;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: color 0.2s ease;
}

.auth-link:hover {
  color: #67E8F9;
}

.auth-link--strong {
  margin-left: 0.25rem;
  font-weight: 600;
}

/* ── Animations ────────────────────────────────────────────────────────── */
@keyframes reg-fade-up {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes reg-fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ── Responsive ────────────────────────────────────────────────────────── */
@media (min-width: 768px) {
  .auth-main {
    padding: 2rem;
  }

  .auth-card {
    padding: 1.75rem;
  }

  .auth-grid {
    grid-template-columns: minmax(210px, 240px) 1fr;
    gap: 1.5rem;
    align-items: start;
  }
}

@media (max-width: 420px) {
  .auth-card {
    padding: 1rem;
  }

  .auth-title {
    font-size: 1.5rem;
  }

  .terms-label {
    font-size: 0.8rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-brand,
  .auth-card {
    animation: none;
  }
}
</style>
