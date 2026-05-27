<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { authApi } from '@/features/auth/authApi';
import { useAuthSession } from '@/features/auth/authSession';
import { ApiError } from '@/lib/api/apiError';

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
const { setSessionFromAuthResponse, logout } = useAuthSession();

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
  const redirectQuery = route.query.redirect;
  const redirectPath = Array.isArray(redirectQuery) ? redirectQuery[0] : redirectQuery;
  if (typeof redirectPath !== 'string') {
    return '/';
  }

  if (!redirectPath.startsWith('/') || redirectPath.startsWith('//')) {
    return '/';
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
    setSessionFromAuthResponse(response);
    formMessage.value = 'Đăng nhập thành công.';
    await router.push(getSafeRedirectPath());
  } catch (error) {
    logout();
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
  const colors = ['#fb923c', '#fbbf24', '#f472b6', '#a78bfa', '#38bdf8'];

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
  <div class="login-page">
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

      <div class="snow-layer">
        <span v-for="n in 18" :key="`snow-${n}`" class="snowflake" :style="{ '--i': n }"></span>
      </div>
    </div>

    <main class="auth-main">
      <div class="auth-shell">
        <div class="auth-brand animate-bounce-in">
          <div class="brand-icon-wrap" aria-hidden="true">
            <div class="pulse pulse-1"></div>
            <div class="pulse pulse-2"></div>
            <div class="brand-icon animate-gradient animate-wiggle">
              <svg class="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 11c0 3.517-1.009 6.799-2.753 9.571m-3.44-2.04l.054-.09A13.916 13.916 0 008 11a4 4 0 118 0c0 1.017-.07 2.019-.203 3m-2.118 6.844A21.88 21.88 0 0015.171 17m3.839 1.132c.645-2.266.99-4.659.99-7.132A8 8 0 008 4.07M3 15.364c.64-1.319 1-2.8 1-4.364 0-1.457.39-2.823 1.07-4"
                />
              </svg>
            </div>
          </div>

          <p class="brand-name">Gamified Study Hub</p>
          <h1 class="auth-title animate-gradient-text">Đăng nhập vào tài khoản</h1>
          <p class="auth-subtitle animate-fade-in delay-300">Tiếp tục quản lý tài liệu, nhiệm vụ và điểm thưởng cho cộng đồng học tập của bạn.</p>
        </div>

        <section class="auth-card card-hover animate-slide-up delay-200" aria-label="Form đăng nhập">
          <div class="social-grid animate-fade-in delay-400" aria-hidden="false">
            <button type="button" class="social-btn floating-icon" aria-label="Tiếp tục với Google">
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
                  :aria-invalid="Boolean(errors.password)"
                  :aria-describedby="errors.password ? 'password-error' : undefined"
                />
                <button
                  type="button"
                  class="password-toggle"
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
                <input v-model="form.remember" type="checkbox" class="remember-checkbox" />
                <span>Ghi nhớ đăng nhập</span>
              </label>
              <RouterLink to="/auth/forgot-password" class="auth-link">Quên mật khẩu?</RouterLink>
            </div>

            <button type="submit" class="submit-btn btn-shimmer animate-slide-up delay-500" :disabled="loading" :aria-busy="loading">
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
    </main>
    <canvas ref="trailCanvasRef" class="trail-canvas" aria-hidden="true"></canvas>
  </div>
</template>

<style scoped>
.login-page {
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

.blob-a {
  top: -5rem;
  left: -4rem;
  width: 18rem;
  height: 18rem;
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.5), rgba(252, 211, 77, 0.4));
}

.blob-b {
  top: 28%;
  right: -4rem;
  width: 16rem;
  height: 16rem;
  background: linear-gradient(135deg, rgba(244, 114, 182, 0.35), rgba(251, 113, 133, 0.3));
  animation-delay: 1.5s;
}

.blob-c {
  bottom: -6rem;
  left: 18%;
  width: 20rem;
  height: 20rem;
  background: linear-gradient(135deg, rgba(167, 139, 250, 0.35), rgba(56, 189, 248, 0.3));
  animation-delay: 2.5s;
}

.blob-d {
  top: 5rem;
  right: 30%;
  width: 12rem;
  height: 12rem;
  background: linear-gradient(135deg, rgba(125, 211, 252, 0.28), rgba(14, 165, 233, 0.2));
  animation-delay: 0.7s;
}

.blob-e {
  bottom: 26%;
  right: 8%;
  width: 14rem;
  height: 14rem;
  background: linear-gradient(135deg, rgba(134, 239, 172, 0.28), rgba(20, 184, 166, 0.22));
  animation-delay: 3s;
}

.shape {
  position: absolute;
  opacity: 0.35;
  animation: float 7s ease-in-out infinite;
}

.shape-circle {
  top: 15%;
  left: 10%;
  width: 2.2rem;
  height: 2.2rem;
  border-radius: 9999px;
  border: 2px solid #fb923c;
}

.shape-diamond {
  top: 26%;
  right: 15%;
  width: 1.8rem;
  height: 1.8rem;
  border: 2px solid #f472b6;
  transform: rotate(45deg);
  animation-delay: 1.2s;
}

.shape-triangle {
  bottom: 20%;
  left: 8%;
  width: 0;
  height: 0;
  border-left: 1rem solid transparent;
  border-right: 1rem solid transparent;
  border-bottom: 1.8rem solid rgba(167, 139, 250, 0.5);
  animation-delay: 2s;
}

.shape-star {
  top: 10%;
  left: 45%;
  width: 1.4rem;
  height: 1.4rem;
  background: #f9a8d4;
  clip-path: polygon(50% 0%, 61% 39%, 100% 50%, 61% 61%, 50% 100%, 39% 61%, 0% 50%, 39% 39%);
  animation-delay: 1.5s;
}

.sparkle {
  position: absolute;
  width: 0.4rem;
  height: 0.4rem;
  border-radius: 9999px;
  animation: sparkle 3s ease-in-out infinite;
}

.sparkle-a { top: 20%; left: 30%; background: #fdba74; }
.sparkle-b { top: 40%; right: 25%; background: #f9a8d4; animation-delay: 0.7s; }
.sparkle-c { bottom: 30%; left: 20%; background: #c4b5fd; animation-delay: 1.5s; }
.sparkle-d { top: 60%; right: 35%; background: #f59e0b; animation-delay: 2s; }

.snow-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.snowflake {
  --size: calc(4px + (var(--i) % 3) * 2px);
  position: absolute;
  top: -8%;
  left: calc((var(--i) * 5.4%) + ((var(--i) % 2) * 2%));
  width: var(--size);
  height: var(--size);
  border-radius: 9999px;
  background: radial-gradient(circle at 30% 30%, #ffffff 0%, #f0f9ff 60%, #dbeafe 100%);
  opacity: 0.7;
  filter: drop-shadow(0 0 4px rgba(255, 255, 255, 0.6));
  animation:
    snowfall calc(7s + (var(--i) % 5) * 1.2s) linear infinite,
    snowdrift calc(2.8s + (var(--i) % 4) * 0.6s) ease-in-out infinite;
  animation-delay: calc((var(--i) * -0.55s));
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

.auth-shell {
  width: 100%;
  max-width: 30rem;
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
  border: 2px solid rgba(251, 146, 60, 0.45);
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
  background: linear-gradient(135deg, #fb923c, #f59e0b, #facc15);
  box-shadow: 0 12px 30px rgba(251, 146, 60, 0.35);
}

.animate-gradient {
  background-size: 300% 300%;
  animation: gradient-shift 8s ease infinite, wiggle 3s ease-in-out infinite;
}

.animate-gradient-text {
  background: linear-gradient(90deg, #f97316, #ec4899, #8b5cf6);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: gradient-shift 8s ease infinite;
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
  margin: 0;
  color: rgba(146, 64, 14, 0.78);
  font-size: 0.92rem;
  line-height: 1.5;
}

.auth-card {
  border-radius: 1.6rem;
  border: 1px solid rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 22px 56px rgba(251, 146, 60, 0.15);
  padding: 1.5rem;
}

.card-hover {
  transition: all 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 25px 60px rgba(251, 146, 60, 0.14);
}

.social-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.75rem;
}

.social-btn {
  border: 1px solid #fed7aa;
  background: rgba(255, 255, 255, 0.82);
  color: #b45309;
  border-radius: 0.95rem;
  padding: 0.75rem 1rem;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.social-btn:hover {
  border-color: #fb923c;
  background: #fff7ed;
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
  background: linear-gradient(90deg, transparent, #fdba74, transparent);
}

.divider-text {
  font-size: 0.73rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #d97706;
  font-weight: 600;
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
  padding: 0.75rem 0.95rem;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.input-wrap {
  transition: transform 0.3s ease;
}

.input-wrap--focused {
  transform: scale(1.02);
}

.input-focus-effect:focus {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(251, 146, 60, 0.15);
}

.input-check {
  position: absolute;
  right: 0.95rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.95rem;
  color: #fb923c;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.input-check--show {
  opacity: 1;
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

.password-toggle {
  position: absolute;
  top: 50%;
  right: 0.75rem;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: #c2410c;
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
  color: #b45309;
  font-size: 0.87rem;
}

.remember-checkbox {
  width: 1rem;
  height: 1rem;
  accent-color: #f97316;
}

.auth-link {
  color: #ea580c;
  text-decoration: none;
  font-size: 0.87rem;
  font-weight: 600;
}

.auth-link:hover {
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  margin-top: 0.2rem;
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

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(251, 146, 60, 0.38);
}

.submit-btn:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.submit-message {
  margin: 0.95rem 0 0;
  border-radius: 0.75rem;
  padding: 0.65rem 0.8rem;
  background: #ffedd5;
  color: #9a3412;
  font-size: 0.84rem;
}

.register-row {
  margin: 1rem 0 0;
  text-align: center;
  color: #b45309;
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
  color: #a16207;
  text-decoration: none;
  font-size: 0.84rem;
}

.back-home-link:hover {
  text-decoration: underline;
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

.trail-canvas {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 50;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-16px);
  }
}

@keyframes gradient-shift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes wiggle {
  0%,
  100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(3deg);
  }
  75% {
    transform: rotate(-3deg);
  }
}

@keyframes morph {
  0%,
  100% {
    border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
  }
  25% {
    border-radius: 30% 60% 70% 40% / 50% 60% 30% 60%;
  }
  50% {
    border-radius: 50% 60% 30% 60% / 30% 60% 70% 40%;
  }
  75% {
    border-radius: 60% 40% 60% 30% / 60% 40% 30% 70%;
  }
}

@keyframes ring {
  from {
    transform: scale(0.8);
    opacity: 1;
  }
  to {
    transform: scale(1.7);
    opacity: 0;
  }
}

@keyframes sparkle {
  0%,
  100% {
    opacity: 0;
    transform: scale(0) rotate(0deg);
  }
  50% {
    opacity: 1;
    transform: scale(1) rotate(180deg);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%) rotate(30deg);
  }
  100% {
    transform: translateX(100%) rotate(30deg);
  }
}

@keyframes wave {
  0%,
  100% {
    transform: translateY(0) scaleY(1);
  }
  50% {
    transform: translateY(-6px) scaleY(1.08);
  }
}

@keyframes snowfall {
  0% {
    transform: translateY(-10vh);
    opacity: 0;
  }
  10% {
    opacity: 0.75;
  }
  100% {
    transform: translateY(115vh);
    opacity: 0.2;
  }
}

@keyframes snowdrift {
  0%,
  100% {
    margin-left: -6px;
  }
  50% {
    margin-left: 8px;
  }
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
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes bounce-in {
  0% {
    transform: scale(0.3);
    opacity: 0;
  }
  50% {
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
