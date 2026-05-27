<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { useAuthSession } from '@/features/auth/authSession';
import { ApiError } from '@/lib/api/apiError';

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
  <div class="register-page">
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
          <h1 class="auth-title animate-gradient-text">Tạo tài khoản mới</h1>
          <p class="auth-subtitle">Bắt đầu xây dựng cổng tài liệu học tập cho cộng đồng của bạn.</p>
        </div>

        <section class="auth-card card-hover animate-slide-up delay-200" aria-label="Form đăng ký">
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
                    :aria-invalid="Boolean(errors.acceptedTerms)"
                    :aria-describedby="errors.acceptedTerms ? 'accepted-terms-error' : undefined"
                  />
                  <span>Tôi đồng ý với Điều khoản sử dụng và Chính sách quyền riêng tư.</span>
                </label>
                <p v-if="errors.acceptedTerms" id="accepted-terms-error" class="field-error" role="alert">{{ errors.acceptedTerms }}</p>
              </div>

              <button type="submit" class="submit-btn btn-shimmer" :disabled="loading" :aria-busy="loading">
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
    </main>
  </div>
</template>

<style scoped>
.register-page {
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
  max-width: 42rem;
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

.setup-panel {
  border-radius: 1rem;
  border: 1px solid #fde68a;
  background: linear-gradient(155deg, rgba(255, 255, 255, 0.88), rgba(255, 247, 237, 0.9));
  padding: 0.9rem;
}

.setup-title {
  margin: 0 0 0.7rem;
  font-size: 0.84rem;
  font-weight: 700;
  color: #b45309;
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
  color: #9a3412;
  padding: 0.55rem 0.65rem;
  border: 1px solid #ffedd5;
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.8);
  animation: pulse-card 3s ease-in-out infinite;
}

.setup-item:nth-child(2) { animation-delay: 0.2s; }
.setup-item:nth-child(3) { animation-delay: 0.4s; }
.setup-item:nth-child(4) { animation-delay: 0.6s; }

.setup-item.is-current {
  border-color: #fdba74;
}

.dot {
  width: 0.65rem;
  height: 0.65rem;
  border-radius: 9999px;
  background: linear-gradient(135deg, #fb923c, #f59e0b);
  box-shadow: 0 0 0 3px rgba(251, 146, 60, 0.18);
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
  color: #b45309;
  font-weight: 600;
}

.field-input {
  width: 100%;
  border: 2px solid #fed7aa;
  background: rgba(255, 255, 255, 0.9);
  color: #9a3412;
  border-radius: 0.95rem;
  padding: 0.72rem 0.95rem;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.field-input::placeholder {
  color: #fdba74;
}

.field-input:hover {
  border-color: #fdba74;
}

.field-input:focus {
  outline: none;
  border-color: #fb923c;
  box-shadow: 0 0 0 3px rgba(251, 146, 60, 0.25);
}

.field-input--error {
  border-color: #dc2626;
}

.field-error {
  margin: 0.35rem 0 0;
  color: #b91c1c;
  font-size: 0.8rem;
}

.strength-text {
  margin: 0.45rem 0 0;
  font-size: 0.8rem;
  color: #9a3412;
}

.strength-text--weak strong { color: #b91c1c; }
.strength-text--medium strong { color: #b45309; }
.strength-text--strong strong { color: #15803d; }

.terms-label {
  display: flex;
  align-items: flex-start;
  gap: 0.55rem;
  color: #b45309;
  font-size: 0.86rem;
  line-height: 1.4;
}

.terms-checkbox {
  width: 1rem;
  height: 1rem;
  margin-top: 0.16rem;
  accent-color: #f97316;
  flex: 0 0 auto;
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

.submit-message {
  margin: 0;
  border-radius: 0.75rem;
  padding: 0.65rem 0.8rem;
  background: #ffedd5;
  color: #9a3412;
  font-size: 0.84rem;
}

.register-row {
  margin: 0;
  text-align: center;
  color: #b45309;
  font-size: 0.9rem;
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

@keyframes shimmer {
  0% { transform: translateX(-100%) rotate(30deg); }
  100% { transform: translateX(100%) rotate(30deg); }
}

@keyframes pulse-card {
  0%,
  100% { transform: translateY(0); }
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
