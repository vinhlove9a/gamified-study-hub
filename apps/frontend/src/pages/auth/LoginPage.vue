<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { authApi, isMfaChallenge } from '@/features/auth/authApi';
import { useAuthSession } from '@/features/auth/authSession';
import { useMfaChallenge } from '@/features/auth/mfaChallenge';
import { startGoogleLogin } from '@/features/auth/oauth';
import { defaultRouteForRole } from '@/router/authGuard';
import { ApiError } from '@/lib/api/apiError';
import StarfieldCanvas from '@/components/dashboard/StarfieldCanvas.vue';
import AuthShowcasePanel from '@/components/auth/AuthShowcasePanel.vue';

interface FormState {
  email: string;
  password: string;
  remember: boolean;
}

const form = reactive<FormState>({
  email: '',
  password: '',
  remember: false
});
const router = useRouter();
const route = useRoute();
const { setSessionFromAuthResponse, clearSession, isAdmin } = useAuthSession();
const { setChallenge } = useMfaChallenge();

const showPassword = ref(false);
const loading = ref(false);
const formMessage = ref('');
const emailFocused = ref(false);
const errors = reactive<{ email?: string; password?: string }>({});
const trailCanvasRef = ref<HTMLCanvasElement | null>(null);

let rafId = 0;
let cleanupMouseMove: (() => void) | null = null;
let cleanupResize: (() => void) | null = null;

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const getSafeRedirectPath = (): string => {
  // An explicit, same-origin ?redirect wins; otherwise admins land on the
  // Dashboard (/admin) and everyone else on the app shell (/app).
  const fallback = defaultRouteForRole(isAdmin.value);
  const redirectQuery = route.query.redirect;
  const redirectPath = Array.isArray(redirectQuery) ? redirectQuery[0] : redirectQuery;
  if (typeof redirectPath !== 'string') {
    return fallback;
  }

  if (!redirectPath.startsWith('/') || redirectPath.startsWith('//')) {
    return fallback;
  }

  return redirectPath;
};

const applyFieldErrors = (fieldErrors?: Array<{ field: string; message: string }>) => {
  if (!fieldErrors) return;
  for (const fieldError of fieldErrors) {
    if (fieldError.field === 'email') {
      errors.email = fieldError.message;
    }
    if (fieldError.field === 'password') {
      errors.password = fieldError.message;
    }
  }
};

const validate = () => {
  errors.email = '';
  errors.password = '';

  if (!form.email.trim()) {
    errors.email = 'Vui lòng nhập email.';
  } else if (!emailRegex.test(form.email)) {
    errors.email = 'Email không đúng định dạng.';
  }

  if (!form.password.trim()) {
    errors.password = 'Vui lòng nhập mật khẩu.';
  }

  return !errors.email && !errors.password;
};

const handleSubmit = async () => {
  formMessage.value = '';
  if (!validate()) return;

  loading.value = true;
  try {
    const response = await authApi.login({
      email: form.email.trim(),
      password: form.password
    });
    if (isMfaChallenge(response)) {
      setChallenge(response.mfaToken, getSafeRedirectPath());
      await router.push('/auth/mfa');
      return;
    }
    setSessionFromAuthResponse(response);
    formMessage.value = 'Đăng nhập thành công.';
    await router.push(getSafeRedirectPath());
  } catch (error) {
    clearSession();
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

onMounted(() => {
  const canvas = trailCanvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const particles: Array<{
    x: number;
    y: number;
    size: number;
    speedX: number;
    speedY: number;
    life: number;
    decay: number;
    color: string;
  }> = [];
  // Cosmic cursor-trail palette (cyan / magenta / nebula) to match the deep-space theme.
  const colors = ['#22d3ee', '#38bdf8', '#e879f9', '#a78bfa', '#34d399'];

  const setCanvasSize = () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  };
  setCanvasSize();

  const onMove = (event: MouseEvent) => {
    for (let i = 0; i < 2; i += 1) {
      particles.push({
        x: event.clientX,
        y: event.clientY,
        size: Math.random() * 5 + 2,
        speedX: Math.random() * 2 - 1,
        speedY: Math.random() * 2 - 1,
        life: 1,
        decay: Math.random() * 0.02 + 0.02,
        color: colors[Math.floor(Math.random() * colors.length)]
      });
    }
  };

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    for (let i = particles.length - 1; i >= 0; i -= 1) {
      const p = particles[i];
      p.x += p.speedX;
      p.y += p.speedY;
      p.life -= p.decay;
      p.size *= 0.97;
      ctx.save();
      ctx.globalAlpha = p.life;
      ctx.fillStyle = p.color;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
      if (p.life <= 0 || p.size <= 0.5) particles.splice(i, 1);
    }
    rafId = requestAnimationFrame(animate);
  };

  window.addEventListener('mousemove', onMove);
  window.addEventListener('resize', setCanvasSize);
  cleanupMouseMove = () => window.removeEventListener('mousemove', onMove);
  cleanupResize = () => window.removeEventListener('resize', setCanvasSize);
  animate();
});

onUnmounted(() => {
  if (rafId) cancelAnimationFrame(rafId);
  cleanupMouseMove?.();
  cleanupResize?.();
});
</script>

<template>
  <div class="login-page cosmic-page">
    <StarfieldCanvas />

    <main class="auth-main">
      <div class="auth-split">
        <div class="auth-shell">
        <div class="auth-brand">
          <div class="brand-logo" aria-hidden="true">
            <img src="/images/gmf-logo.png" alt="Gamified Study Hub" class="brand-logo-img" />
          </div>

          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title">Đăng nhập vào tài khoản</h1>
          <p class="auth-subtitle">Tiếp tục quản lý tài liệu, nhiệm vụ và điểm thưởng cho cộng đồng học tập của bạn.</p>
        </div>

        <section class="auth-card" aria-label="Form đăng nhập">
          <div class="social-grid">
            <button type="button" class="social-btn" aria-label="Tiếp tục với Google" @click="startGoogleLogin">
              <span>Tiếp tục với Google</span>
            </button>
            <button type="button" class="social-btn" aria-label="Tiếp tục với GitHub">
              <span>Tiếp tục với GitHub</span>
            </button>
          </div>

          <div class="divider">
            <span class="divider-text">hoặc đăng nhập bằng email</span>
          </div>

          <form class="space-y-5" novalidate @submit.prevent="handleSubmit">
            <div>
              <label for="email" class="field-label">Email</label>
              <div class="input-wrap" :class="{ 'input-wrap--focused': emailFocused }">
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
                  @focus="emailFocused = true"
                  @blur="emailFocused = false"
                />
                <div class="input-check" :class="{ 'input-check--show': emailFocused && !errors.email && !!form.email }" aria-hidden="true">✓</div>
              </div>
              <p v-if="errors.email" id="email-error" class="field-error" role="alert">{{ errors.email }}</p>
            </div>

            <div>
              <label for="password" class="field-label">Mật khẩu</label>
              <div class="password-wrap">
                <input
                  id="password"
                  v-model="form.password"
                  class="field-input pr-12"
                  :class="{ 'field-input--error': errors.password }"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  autocomplete="current-password"
                  required
                  :disabled="loading"
                  :aria-invalid="Boolean(errors.password)"
                  :aria-describedby="errors.password ? 'password-error' : undefined"
                />
                <button
                  type="button"
                  class="password-toggle"
                  :disabled="loading"
                  :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                  @click="showPassword = !showPassword"
                >
                  {{ showPassword ? 'Ẩn' : 'Hiện' }}
                </button>
              </div>
              <p v-if="errors.password" id="password-error" class="field-error" role="alert">{{ errors.password }}</p>
            </div>

            <div class="option-row">
              <label class="remember-label">
                <input v-model="form.remember" type="checkbox" class="remember-checkbox" :disabled="loading" />
                <span>Ghi nhớ đăng nhập</span>
              </label>
              <RouterLink to="/auth/forgot-password" class="auth-link">Quên mật khẩu?</RouterLink>
            </div>

            <button type="submit" class="submit-btn" :disabled="loading" :aria-busy="loading">
              {{ loading ? 'Đang xử lý...' : 'Đăng nhập' }}
            </button>
          </form>

          <p v-if="formMessage" class="submit-message" role="status" aria-live="polite">{{ formMessage }}</p>

          <p class="register-row">
            Chưa có tài khoản?
            <RouterLink to="/auth/register" class="auth-link auth-link--strong">Tạo tài khoản</RouterLink>
          </p>

          <a href="/" class="back-home-link">Quay lại trang chủ</a>
        </section>
        </div>

        <AuthShowcasePanel variant="login" class="auth-aside" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.login-page {
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
  padding: 1.5rem;
}

.auth-split {
  width: 100%;
  display: grid;
  gap: 1.5rem;
  justify-items: center;
}

.auth-shell {
  width: 100%;
  max-width: 30rem;
}

.auth-aside {
  display: none;
}

@media (min-width: 960px) {
  .auth-main {
    padding: 2.5rem;
  }

  .auth-split {
    max-width: 70rem;
    grid-template-columns: minmax(26rem, 30rem) 1fr;
    align-items: start;
    gap: 2.5rem;
    justify-items: stretch;
  }

  .auth-shell {
    margin: 0;
    justify-self: start;
    position: sticky;
    top: 2.5rem;
  }

  .auth-aside {
    display: flex;
  }
}

/* ── Brand ─────────────────────────────────────────────────────────────── */
.auth-brand {
  text-align: center;
  margin-bottom: 1.5rem;
  animation: auth-fade-in 0.4s ease-out both;
}

.brand-logo {
  width: 3.5rem;
  height: 3.5rem;
  margin: 0 auto 0.875rem;
  border-radius: 0.75rem;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
}

.brand-logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  margin: 0;
  color: #8B8B9F;
  font-size: 0.9rem;
  line-height: 1.6;
}

/* ── Card ──────────────────────────────────────────────────────────────── */
.auth-card {
  border-radius: 1.25rem;
  padding: 1.5rem;
  background: rgba(14, 14, 22, 0.72);
  backdrop-filter: blur(24px) saturate(140%);
  -webkit-backdrop-filter: blur(24px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.02) inset,
    0 24px 48px rgba(0, 0, 0, 0.6);
  animation: auth-fade-up 0.5s ease-out both;
}

/* ── Social login ──────────────────────────────────────────────────────── */
.social-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.625rem;
}

.social-btn {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  color: #C0C0D4;
  border-radius: 0.625rem;
  padding: 0.75rem 1rem;
  font-weight: 500;
  font-size: 0.88rem;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.social-btn:hover {
  border-color: rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
  color: #E8E8F0;
}

/* ── Divider ───────────────────────────────────────────────────────────── */
.divider {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 1.25rem 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
}

.divider-text {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #5A5A6E;
  font-weight: 500;
}

/* ── Form fields ───────────────────────────────────────────────────────── */
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
  padding: 0.75rem 0.875rem;
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

.input-wrap {
  position: relative;
}

.input-check {
  position: absolute;
  right: 0.875rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.85rem;
  color: #10B981;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.input-check--show {
  opacity: 1;
}

.password-wrap {
  position: relative;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 0.75rem;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: #5A5A6E;
  font-size: 0.76rem;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #8B8B9F;
}

/* ── Option row ────────────────────────────────────────────────────────── */
.option-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.625rem;
}

.remember-label {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #8B8B9F;
  font-size: 0.85rem;
}

.remember-checkbox {
  width: 1rem;
  height: 1rem;
  accent-color: #22D3EE;
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
  font-weight: 600;
}

/* ── Submit button ─────────────────────────────────────────────────────── */
.submit-btn {
  width: 100%;
  margin-top: 0.25rem;
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

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── Messages & links ──────────────────────────────────────────────────── */
.submit-message {
  margin: 0.875rem 0 0;
  border-radius: 0.5rem;
  padding: 0.625rem 0.75rem;
  background: rgba(34, 211, 238, 0.05);
  border: 1px solid rgba(34, 211, 238, 0.12);
  color: #C0C0D4;
  font-size: 0.82rem;
}

.register-row {
  margin: 1rem 0 0;
  text-align: center;
  color: #8B8B9F;
  font-size: 0.88rem;
}

.back-home-link {
  margin-top: 0.875rem;
  display: block;
  text-align: center;
  color: #5A5A6E;
  text-decoration: none;
  font-size: 0.82rem;
  transition: color 0.2s ease;
}

.back-home-link:hover {
  color: #8B8B9F;
}

/* ── Animations ────────────────────────────────────────────────────────── */
@keyframes auth-fade-up {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes auth-fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ── Responsive ────────────────────────────────────────────────────────── */
@media (min-width: 640px) {
  .auth-main {
    padding: 2rem;
  }

  .auth-card {
    padding: 2rem;
  }

  .social-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-brand,
  .auth-card {
    animation: none;
  }
}
</style>
