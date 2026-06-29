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
        <div class="auth-brand animate-bounce-in">
          <div class="brand-icon-wrap" aria-hidden="true">
            <div class="pulse pulse-1"></div>
            <div class="pulse pulse-2"></div>
            <div class="brand-icon animate-wiggle">
              <img src="/images/gmf-logo.png" alt="Gamified Study Hub" class="h-full w-full rounded-2xl object-cover" />
            </div>
          </div>

          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title cosmic-gradient-text">Đăng nhập vào tài khoản</h1>
          <p class="auth-subtitle animate-fade-in delay-300">Tiếp tục quản lý tài liệu, nhiệm vụ và điểm thưởng cho cộng đồng học tập của bạn.</p>
        </div>

        <section class="auth-card cosmic-glass cosmic-glass-hover animate-slide-up delay-200" aria-label="Form đăng nhập">
          <div class="social-grid animate-fade-in delay-400" aria-hidden="false">
            <button type="button" class="social-btn floating-icon" aria-label="Tiếp tục với Google" @click="startGoogleLogin">
              <span>Tiếp tục với Google</span>
            </button>
            <button type="button" class="social-btn floating-icon" aria-label="Tiếp tục với GitHub">
              <span>Tiếp tục với GitHub</span>
            </button>
          </div>

          <div class="divider">
            <span class="divider-text">hoặc đăng nhập bằng email</span>
          </div>

          <form class="space-y-5" novalidate @submit.prevent="handleSubmit">
            <div class="animate-slide-up delay-300">
              <label for="email" class="field-label">Email</label>
              <div class="relative input-wrap" :class="{ 'input-wrap--focused': emailFocused }">
                <input
                  id="email"
                  v-model="form.email"
                  class="field-input input-focus-effect"
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

            <div class="animate-slide-up delay-400">
              <label for="password" class="field-label">Mật khẩu</label>
              <div class="relative">
                <input
                  id="password"
                  v-model="form.password"
                  class="field-input pr-12 input-focus-effect"
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

            <div class="option-row animate-fade-in delay-500">
              <label class="remember-label">
                <input v-model="form.remember" type="checkbox" class="remember-checkbox" :disabled="loading" />
                <span>Ghi nhớ đăng nhập</span>
              </label>
              <RouterLink to="/auth/forgot-password" class="auth-link">Quên mật khẩu?</RouterLink>
            </div>

            <button type="submit" class="submit-btn cosmic-btn animate-slide-up delay-500" :disabled="loading" :aria-busy="loading">
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

        <div class="wave-row" aria-hidden="true">
          <span>🌸</span><span>🌼</span><span>🦋</span><span>✨</span><span>🌈</span>
        </div>
        </div>

        <AuthShowcasePanel variant="login" class="auth-aside" />
      </div>
    </main>
    <canvas ref="trailCanvasRef" class="trail-canvas" aria-hidden="true"></canvas>
  </div>
</template>

<style scoped>
.login-page {
  overflow: hidden;
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

.auth-brand {
  text-align: center;
  margin-bottom: 1.5rem;
}

.brand-icon-wrap {
  position: relative;
  width: 5rem;
  height: 5rem;
  margin: 0 auto 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pulse {
  position: absolute;
  inset: 0;
  border: 2px solid oklch(0.78 0.18 195 / 45%);
  border-radius: 9999px;
  animation: ring 2s ease-out infinite;
}

.pulse-2 {
  animation-delay: 0.8s;
}

.brand-icon {
  width: 4rem;
  height: 4rem;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: oklch(0.08 0.02 280);
  background: linear-gradient(135deg, oklch(0.82 0.16 195), oklch(0.68 0.22 350));
  box-shadow: 0 12px 30px oklch(0.78 0.18 195 / 40%), 0 0 28px oklch(0.68 0.22 350 / 30%);
}

.animate-wiggle {
  animation: wiggle 3s ease-in-out infinite;
}

.brand-name {
  margin: 0;
  color: oklch(0.82 0.16 195);
  font-weight: 600;
  font-size: 0.95rem;
  letter-spacing: 0.02em;
}

.auth-title {
  margin: 0.35rem 0;
  font-size: 2rem;
  line-height: 1.2;
  font-weight: 700;
}

.auth-subtitle {
  margin: 0;
  color: oklch(0.7 0.03 240 / 85%);
  font-size: 0.92rem;
  line-height: 1.5;
}

.auth-card {
  border-radius: 1.6rem;
  padding: 1.5rem;
}

.social-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.75rem;
}

.social-btn {
  border: 1px solid oklch(0.7 0.1 280 / 22%);
  background: oklch(0.13 0.025 280 / 45%);
  color: oklch(0.92 0.02 240);
  border-radius: 0.95rem;
  padding: 0.75rem 1rem;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.social-btn:hover {
  border-color: oklch(0.78 0.18 195 / 45%);
  background: oklch(0.78 0.18 195 / 10%);
  color: oklch(0.9 0.12 200);
}

.floating-icon {
  transition: all 0.3s ease;
}

.floating-icon:hover {
  transform: translateY(-5px) scale(1.03);
}

.divider {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 1.2rem 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, oklch(0.78 0.18 195 / 35%), transparent);
}

.divider-text {
  font-size: 0.73rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: oklch(0.7 0.03 240 / 80%);
  font-weight: 600;
}

.field-label {
  display: block;
  margin-bottom: 0.45rem;
  margin-left: 0.15rem;
  font-size: 0.87rem;
  color: oklch(0.85 0.03 220);
  font-weight: 600;
}

/* Cosmic glass field (see .cosmic-input recipe in dashboard.css). */
.field-input {
  width: 100%;
  border: 1px solid oklch(0.7 0.1 280 / 25%);
  background: oklch(0.13 0.025 280 / 55%);
  color: oklch(0.96 0.01 240);
  border-radius: 0.95rem;
  padding: 0.75rem 0.95rem;
  font-size: 0.95rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, transform 0.3s ease;
}

.input-wrap {
  transition: transform 0.3s ease;
}

.input-wrap--focused {
  transform: scale(1.02);
}

.input-focus-effect:focus {
  transform: translateY(-2px);
}

.input-check {
  position: absolute;
  right: 0.95rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.95rem;
  color: oklch(0.78 0.18 195);
  text-shadow: 0 0 8px oklch(0.78 0.18 195 / 70%);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.input-check--show {
  opacity: 1;
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

.password-toggle {
  position: absolute;
  top: 50%;
  right: 0.75rem;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: oklch(0.82 0.16 195);
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
}

.option-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.6rem;
}

.remember-label {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: oklch(0.8 0.03 230);
  font-size: 0.87rem;
}

.remember-checkbox {
  width: 1rem;
  height: 1rem;
  accent-color: oklch(0.78 0.18 195);
}

.auth-link {
  color: oklch(0.82 0.16 195);
  text-decoration: none;
  font-size: 0.87rem;
  font-weight: 600;
}

.auth-link:hover {
  text-decoration: underline;
  color: oklch(0.88 0.14 200);
}

.submit-btn {
  width: 100%;
  margin-top: 0.2rem;
  border-radius: 0.95rem;
  font-size: 0.95rem;
  padding: 0.85rem 1rem;
  cursor: pointer;
}

.submit-message {
  margin: 0.95rem 0 0;
  border-radius: 0.75rem;
  padding: 0.65rem 0.8rem;
  background: oklch(0.78 0.18 195 / 10%);
  border: 1px solid oklch(0.78 0.18 195 / 25%);
  color: oklch(0.9 0.05 210);
  font-size: 0.84rem;
}

.register-row {
  margin: 1rem 0 0;
  text-align: center;
  color: oklch(0.7 0.03 240 / 85%);
  font-size: 0.9rem;
}

.auth-link--strong {
  margin-left: 0.25rem;
  font-weight: 700;
}

.back-home-link {
  margin-top: 0.9rem;
  display: block;
  text-align: center;
  color: oklch(0.7 0.03 240 / 70%);
  text-decoration: none;
  font-size: 0.84rem;
}

.back-home-link:hover {
  text-decoration: underline;
  color: oklch(0.82 0.16 195);
}

.wave-row {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
  gap: 0.25rem;
}

.wave-row span {
  display: inline-block;
  animation: wave 3s ease-in-out infinite;
}

.wave-row span:nth-child(2) { animation-delay: 0.1s; }
.wave-row span:nth-child(3) { animation-delay: 0.2s; }
.wave-row span:nth-child(4) { animation-delay: 0.3s; }
.wave-row span:nth-child(5) { animation-delay: 0.4s; }

/* Cursor-trail canvas sits above content but ignores pointer events. */
.trail-canvas {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 50;
}

@keyframes wiggle {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(3deg); }
  75% { transform: rotate(-3deg); }
}

@keyframes ring {
  from { transform: scale(0.8); opacity: 1; }
  to { transform: scale(1.7); opacity: 0; }
}

@keyframes wave {
  0%, 100% { transform: translateY(0) scaleY(1); }
  50% { transform: translateY(-6px) scaleY(1.08); }
}

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

.animate-slide-up {
  animation: slide-up 0.8s ease-out both;
}

.animate-fade-in {
  animation: fade-in 1s ease-out both;
}

.animate-bounce-in {
  animation: bounce-in 0.8s ease-out both;
}

.delay-200 { animation-delay: 0.2s; }
.delay-300 { animation-delay: 0.3s; }
.delay-400 { animation-delay: 0.4s; }
.delay-500 { animation-delay: 0.5s; }

@keyframes slide-up {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes bounce-in {
  0% { transform: scale(0.3); opacity: 0; }
  50% { transform: scale(1.05); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); opacity: 1; }
}
</style>
