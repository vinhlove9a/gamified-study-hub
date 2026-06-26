<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, inject } from 'vue';
import type { AnimationAPI } from '@/types/gsap';
import { useAriaLive } from '@/composables/useAriaLive';
import { useFocusManagement } from '@/composables/useFocusManagement';
import { focusElement } from '@/utils/accessibility';
import asiaMapUrl from '@/assets/images/asiaLow.svg?url';

interface ContactInfo {
  id: string
  type: string
  label: string
  value: string
  icon: string
  link?: string
}

interface Office {
  id: string
  name: string
  address: string
  role: string
  isHq: boolean
}

interface FooterLink {
  label: string
  href: string
  external?: boolean
}

type GsapType = typeof import('gsap').gsap;

const gsap = inject<GsapType | null>('gsap', null);
const animation = inject<AnimationAPI | null>('animation', null);
let cleanupFns: Array<() => void> = [];

const { announce, announceAssertive } = useAriaLive();
const { returnFocus } = useFocusManagement();

const formErrors = ref<Record<string, string>>({})
const successMessageRef = ref<HTMLElement | null>(null)

const contactInfo: ContactInfo[] = [
  {
    id: 'email',
    type: 'Email',
    label: 'Email',
    value: 'xuanvinhchan@gmail.com',
    icon: 'email',
    link: 'mailto:xuanvinhchan@gmail.com'
  },
  {
    id: 'phone',
    type: 'Liên hệ',
    label: 'Điện thoại',
    value: '099999999',
    icon: 'phone',
    link: 'tel:099999999'
  },
  {
    id: 'trade',
    type: 'Demo',
    label: 'Mission Control',
    value: 'Đặt lịch xem workspace',
    icon: 'trade',
    link: '#'
  }
]

const offices: Office[] = [
  {
    id: 'hq',
    name: 'Workspace cộng đồng',
    address: 'Dành cho CLB đại học và cộng đồng học tập nội bộ',
    role: 'CLB & cộng đồng',
    isHq: true
  },
  {
    id: 'singapore',
    name: 'Workspace gia sư / trung tâm',
    address: 'Phù hợp cho gia sư, mentor, nhóm luyện thi và trung tâm nhỏ',
    role: 'Gia sư & trung tâm',
    isHq: false
  }
]

const footerProductLinks: FooterLink[] = [
  { label: 'Tính năng', href: '#product' },
  { label: 'Cách hoạt động', href: '#mission' },
  { label: 'Kho tài liệu', href: '#product' },
  { label: 'Nhiệm vụ học tập', href: '#sustainability' },
  { label: 'Điểm thưởng & Coin', href: '#stats' },
  { label: 'Mở khóa VIP', href: '#contact' }
]

const footerAudienceLinks: FooterLink[] = [
  { label: 'CLB Đại học', href: '#geographic' },
  { label: 'Gia sư & Mentor', href: '#geographic' },
  { label: 'Nhóm luyện thi TOEIC/IELTS', href: '#geographic' },
  { label: 'Trung tâm nhỏ', href: '#geographic' },
  { label: 'Cộng đồng học tập nội bộ', href: '#geographic' }
]

const footerResourceLinks: FooterLink[] = [
  { label: 'Tài liệu', href: '#product' },
  { label: 'Lộ trình sản phẩm', href: '#mission' },
  { label: 'Demo', href: '#contact' },
  { label: 'GitHub', href: 'https://github.com/vinhlove9a', external: true },
  { label: 'Hỗ trợ', href: '#contact' }
]

const footerBottomLinks: FooterLink[] = [
  { label: 'Quyền riêng tư', href: '#contact' },
  { label: 'Điều khoản', href: '#contact' },
  { label: 'Liên hệ', href: '#contact' }
]

const formData = ref({
  name: '',
  email: '',
  company: '',
  message: ''
})

const submitted = ref(false)
const submitting = ref(false)

const validateForm = (): boolean => {
  formErrors.value = {}
  let isValid = true

  if (!formData.value.name.trim()) {
    formErrors.value.name = 'Họ và tên là bắt buộc'
    isValid = false
  }

  if (!formData.value.email.trim()) {
    formErrors.value.email = 'Email là bắt buộc'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    formErrors.value.email = 'Vui lòng nhập email hợp lệ'
    isValid = false
  }

  if (!formData.value.message.trim()) {
    formErrors.value.message = 'Nội dung yêu cầu là bắt buộc'
    isValid = false
  }

  return isValid
}

const clearFieldError = (field: string) => {
  if (formErrors.value[field]) {
    delete formErrors.value[field]
  }
}

const submitForm = async () => {
  if (!validateForm()) {
    announceAssertive('Vui lòng kiểm tra lại các trường trong biểu mẫu')
    const firstErrorField = document.querySelector<HTMLElement>('[aria-invalid="true"]')
    if (firstErrorField) {
      focusElement(firstErrorField)
    }
    return
  }

  submitting.value = true
  announce('Đang gửi yêu cầu...', 'polite')

  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    submitted.value = true
    submitting.value = false
    formErrors.value = {}

    announce('Yêu cầu đã được ghi nhận. Chúng tôi sẽ liên hệ lại trong thời gian sớm nhất.', 'polite')

    nextTick(() => {
      if (successMessageRef.value) {
        focusElement(successMessageRef.value)
      }
    })
  } catch {
    submitting.value = false
    announceAssertive('Có lỗi khi gửi yêu cầu. Vui lòng thử lại.')
  }
}

const resetForm = () => {
  submitted.value = false
  formData.value = {
    name: '',
    email: '',
    company: '',
    message: ''
  }
  formErrors.value = {}
  
  nextTick(() => {
    const firstInput = document.querySelector<HTMLElement>('#name')
    if (firstInput) {
      focusElement(firstInput)
    }
  })
}

onMounted(() => {
  if (!gsap) return

  const shouldReduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

  if (shouldReduceMotion) {
    gsap.set(['.contact-hero', '.hero-stat', '.contact-content', '.info-card', '.office-card', '.form-group', '.section-title', '.offices-section'], { opacity: 1, y: 0 })
    return
  }

  nextTick(() => {
    animation?.batchReveal?.('.contact-hero', {
      trigger: '#contact',
      duration: 1,
      y: 40
    })

    animation?.batchReveal?.('.hero-stat', {
      trigger: '#contact',
      duration: 0.5,
      stagger: 0.12,
      y: 20
    })

    animation?.batchReveal?.('.contact-content', {
      trigger: '#contact',
      duration: 0.8,
      y: 60
    })

    animation?.batchReveal?.('.section-title', {
      trigger: '#contact',
      duration: 0.6,
      stagger: 0.1,
      y: 20
    })

    animation?.batchReveal?.('.info-card', {
      trigger: '#contact',
      duration: 0.5,
      stagger: 0.1,
      y: 30
    })

    animation?.batchReveal?.('.offices-section', {
      trigger: '#contact',
      duration: 0.6,
      y: 30
    })

    animation?.batchReveal?.('.office-card', {
      trigger: '#contact',
      duration: 0.5,
      stagger: 0.15,
      y: 20
    })

    animation?.batchReveal?.('.form-group', {
      trigger: '#contact',
      duration: 0.4,
      stagger: 0.08,
      y: 20
    })

    document.querySelectorAll('.info-card').forEach((card) => {
      const cleanup = animation?.hoverScale?.(card as HTMLElement, { scale: 1.02, duration: 0.3 })
      if (cleanup) cleanupFns.push(cleanup)
    })

    document.querySelectorAll('.office-card').forEach((card) => {
      const cleanup = animation?.hoverScale?.(card as HTMLElement, { scale: 1.02, duration: 0.3 })
      if (cleanup) cleanupFns.push(cleanup)
    })

    document.querySelectorAll('.form-input, .form-textarea').forEach((input) => {
      const cleanup = animation?.focusRing?.(input as HTMLElement, { scale: 1.01, duration: 0.2 })
      if (cleanup) cleanupFns.push(cleanup)
    })
  })
})

onUnmounted(() => {
  cleanupFns.forEach(fn => fn && typeof fn === 'function' && fn())
  returnFocus(null)
})
</script>

<template>
  <section id="contact" class="contact-section">
    <div class="contact-hero">
      <div class="hero-background">
        <div class="bg-noise"></div>
        <div class="bg-gradient-sweep"></div>
        <div class="bg-pattern-overlay"></div>
        <div class="hero-accent-bar"></div>
      </div>

      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" />
            </svg>
          </span>
          <span class="badge-text">Kết nối kho tri thức vào quỹ đạo học tập</span>
        </div>

        <h2 class="hero-title">
          Sẵn sàng mở Deep Workspace
          <span class="title-accent cosmic-gradient-text">cho cộng đồng của bạn?</span>
        </h2>

        <p class="hero-description">
          Bắt đầu từ tài liệu đang có, rồi đưa chúng vào một hệ thống có mission, coin, streak và cổng mở khóa VIP để học viên có lý do quay lại mỗi ngày.
        </p>

        <div class="hero-stats">
          <div class="hero-stat">
            <span class="stat-value">1</span>
            <span class="stat-label">Workspace</span>
          </div>
          <div class="hero-stat">
            <span class="stat-value">24/7</span>
            <span class="stat-label">Orbit online</span>
          </div>
          <div class="hero-stat">
            <span class="stat-value">0</span>
            <span class="stat-label">Rào cản khởi đầu</span>
          </div>
        </div>
      </div>
    </div>

    <div class="contact-content">
      <div class="content-grid">
        <div class="info-column">
          <div class="info-section">
            <h3 class="section-title">Liên hệ</h3>
            <p class="section-description">
              Dành cho cộng đồng học tập, CLB đại học, gia sư và trung tâm nhỏ cần một cổng tài liệu có nhiệm vụ và điểm thưởng.
            </p>

            <div class="info-grid">
              <a
                v-for="info in contactInfo"
                :key="info.id"
                :href="info.link"
                :aria-label="`${info.label}: ${info.value}`"
                class="info-card"
              >
                <div class="info-icon">
                  <svg v-if="info.icon === 'email'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="2" y="4" width="20" height="16" rx="2" />
                    <path d="M22 6L12 13 2 6" />
                  </svg>
                  <svg v-else-if="info.icon === 'phone'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" />
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10" />
                    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
                    <path d="M2 12h20" />
                  </svg>
                </div>
                <div class="info-details">
                  <span class="info-label">{{ info.label }}</span>
                  <span class="info-value">{{ info.value }}</span>
                </div>
                <div class="info-arrow">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M5 12h14M12 5l7 7-7 7" />
                  </svg>
                </div>
              </a>
            </div>
          </div>

          <div class="offices-section">
            <h3 class="section-title">Mô hình phù hợp</h3>
            <div class="offices-grid">
              <div
                v-for="office in offices"
                :key="office.id"
                :aria-label="`${office.name}, ${office.role}. ${office.address}`"
                class="office-card"
              >
                <div class="office-header">
                  <span class="office-badge" :class="office.isHq ? 'badge-hq' : 'badge-regional'">
                    {{ office.isHq ? 'Nhóm 1' : 'Nhóm 2' }}
                  </span>
                  <span class="office-role">{{ office.role }}</span>
                </div>
                <span class="office-name">{{ office.name }}</span>
                <span class="office-address">{{ office.address }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="form-column">
          <div class="contact-form">
            <div class="form-header">
              <span class="form-label">Bắt đầu miễn phí</span>
              <h3 class="form-title">Gửi yêu cầu <span class="title-highlight">triển khai</span></h3>
            </div>

            <div v-if="!submitted" class="form-fields">
              <div class="form-row">
                <div class="form-group">
                  <label for="name" class="form-label-input">Họ và tên *</label>
                  <input
                    id="name"
                    v-model="formData.name"
                    type="text"
                    placeholder="Nguyễn Văn A"
                    class="form-input"
                    :class="{ 'form-input-error': formErrors.name }"
                    :aria-required="true"
                    :aria-invalid="!!formErrors.name"
                    :aria-describedby="formErrors.name ? 'name-error' : undefined"
                    required
                    @blur="clearFieldError('name')"
                  />
                  <span
                    v-if="formErrors.name"
                    id="name-error"
                    class="form-error"
                    role="alert"
                    aria-live="polite"
                  >
                    {{ formErrors.name }}
                  </span>
                </div>
                <div class="form-group">
                  <label for="email" class="form-label-input">Email *</label>
                  <input
                    id="email"
                    v-model="formData.email"
                    type="email"
                    placeholder="xuanvinhchan@gmail.com"
                    class="form-input"
                    :class="{ 'form-input-error': formErrors.email }"
                    :aria-required="true"
                    :aria-invalid="!!formErrors.email"
                    :aria-describedby="formErrors.email ? 'email-error' : undefined"
                    required
                    @blur="clearFieldError('email')"
                  />
                  <span
                    v-if="formErrors.email"
                    id="email-error"
                    class="form-error"
                    role="alert"
                    aria-live="polite"
                  >
                    {{ formErrors.email }}
                  </span>
                </div>
              </div>

              <div class="form-group">
                <label for="company" class="form-label-input">Tên cộng đồng / trung tâm</label>
                <input
                  id="company"
                  v-model="formData.company"
                  type="text"
                  placeholder="CLB tiếng Anh / Nhóm TOEIC / Trung tâm nhỏ"
                  class="form-input"
                  aria-required="false"
                />
              </div>

              <div class="form-group">
                <label for="message" class="form-label-input">Bạn muốn xây dựng cổng học tập cho ai? *</label>
                <textarea
                  id="message"
                  v-model="formData.message"
                  rows="4"
                  placeholder="Tôi muốn tạo kho tài liệu có nhiệm vụ và điểm thưởng cho học viên."
                  class="form-textarea"
                  :class="{ 'form-input-error': formErrors.message }"
                  :aria-required="true"
                  :aria-invalid="!!formErrors.message"
                  :aria-describedby="formErrors.message ? 'message-error' : undefined"
                  required
                  @blur="clearFieldError('message')"
                ></textarea>
                <span
                  v-if="formErrors.message"
                  id="message-error"
                  class="form-error"
                  role="alert"
                  aria-live="polite"
                >
                  {{ formErrors.message }}
                </span>
              </div>

              <button
                class="submit-btn cosmic-btn"
                :disabled="submitting"
                :aria-label="submitting ? 'Đang gửi yêu cầu' : 'Gửi yêu cầu'"
                @click="submitForm"
              >
                <span v-if="!submitting">Gửi yêu cầu</span>
                <span v-else class="submitting">
                  <svg class="spinner" viewBox="0 0 24 24" aria-hidden="true">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="30 70" />
                  </svg>
                  Đang gửi...
                </span>
              </button>
            </div>

            <div
              v-else
              ref="successMessageRef"
              class="success-message"
              tabindex="-1"
              role="status"
              aria-live="polite"
            >
              <div class="success-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                  <polyline points="22 4 12 14.01 9 11.01" />
                </svg>
              </div>
              <h4 class="success-title">Yêu cầu đã được ghi nhận</h4>
              <p class="success-desc">
                Yêu cầu đã được ghi nhận. Chúng tôi sẽ liên hệ lại trong thời gian sớm nhất.
              </p>
              <button
                class="reset-btn"
                aria-label="Gửi thêm yêu cầu"
                @click="resetForm"
              >
                Gửi thêm
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <footer class="landing-footer" aria-label="Thông tin cuối trang">
      <div class="footer-background" aria-hidden="true">
        <div class="footer-map-layer">
          <div class="footer-map-bg" :style="{ backgroundImage: `url(${asiaMapUrl})` }"></div>
        </div>
        <div class="footer-map-overlay"></div>
        <div class="footer-noise-overlay"></div>
      </div>
      <div class="footer-inner">
        <div class="footer-grid">
          <div class="footer-brand">
            <h3 class="footer-heading footer-brand-title">Gamified Study Hub</h3>
            <p class="footer-description">
              Cổng tài liệu học tập có điểm thưởng, giúp CLB, gia sư và cộng đồng học tập tổ chức tài liệu, giao nhiệm vụ, thưởng coin và mở khóa nội dung giá trị cho học viên.
            </p>
          </div>

          <nav class="footer-column" aria-label="Liên kết sản phẩm">
            <h3 class="footer-heading">Sản phẩm</h3>
            <ul class="footer-links">
              <li v-for="link in footerProductLinks" :key="`product-${link.label}`">
                <a class="footer-link" :href="link.href">{{ link.label }}</a>
              </li>
            </ul>
          </nav>

          <nav class="footer-column" aria-label="Liên kết đối tượng sử dụng">
            <h3 class="footer-heading">Đối tượng sử dụng</h3>
            <ul class="footer-links">
              <li v-for="link in footerAudienceLinks" :key="`audience-${link.label}`">
                <a class="footer-link" :href="link.href">{{ link.label }}</a>
              </li>
            </ul>
          </nav>

          <nav class="footer-column" aria-label="Liên kết tài nguyên">
            <h3 class="footer-heading">Tài nguyên</h3>
            <ul class="footer-links">
              <li v-for="link in footerResourceLinks" :key="`resource-${link.label}`">
                <a
                  class="footer-link"
                  :href="link.href"
                  :target="link.external ? '_blank' : undefined"
                  :rel="link.external ? 'noreferrer' : undefined"
                >
                  {{ link.label }}
                </a>
              </li>
            </ul>
          </nav>

          <div class="footer-column footer-contact">
            <h3 class="footer-heading">Người phát triển</h3>
            <ul class="footer-contact-list">
              <li><span class="contact-name">Trần Xuân Vinh</span></li>
              <li>Fullstack Developer</li>
              <li>Hà Nội, Việt Nam</li>
              <li><a class="footer-link" href="mailto:xuanvinhchan@gmail.com">Email: xuanvinhchan@gmail.com</a></li>
              <li><a class="footer-link" href="tel:099999999">Điện thoại: 099999999</a></li>
              <li>
                <a class="footer-link" href="https://www.linkedin.com/in/vinhlove9a" target="_blank" rel="noreferrer">
                  LinkedIn: vinhlove9a
                </a>
              </li>
              <li>
                <a class="footer-link" href="https://github.com/vinhlove9a" target="_blank" rel="noreferrer">
                  GitHub: vinhlove9a
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div class="footer-bottom">
          <p class="footer-copyright">Gamified Study Hub. Built by Vinhlove9a - Trần Xuân Vinh.</p>
          <nav aria-label="Liên kết pháp lý và liên hệ">
            <ul class="footer-bottom-links">
              <li v-for="link in footerBottomLinks" :key="`bottom-${link.label}`">
                <a class="footer-link footer-link-small" :href="link.href">{{ link.label }}</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </footer>
  </section>
</template>

<style scoped>
.contact-section {
  width: 100%;
  min-height: 100vh;
  background: oklch(0.09 0.02 282 / 80%);
}

.contact-hero {
  position: relative;
  width: 100%;
  padding: 5rem 5rem 4rem;
  background: linear-gradient(135deg, oklch(0.1 0.03 285 / 85%), oklch(0.08 0.02 280 / 80%));
  overflow: hidden;
}

.hero-background {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-noise {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  opacity: 0.02;
}

.bg-gradient-sweep {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 80%;
  height: 150%;
  background: conic-gradient(
    from 180deg at 50% 50%,
    oklch(0.78 0.18 195 / 14%) 0deg,
    oklch(0.68 0.22 350 / 12%) 120deg,
    oklch(0.78 0.18 195 / 6%) 240deg,
    transparent 360deg
  );
}

.bg-pattern-overlay {
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23f5f0e8' fill-opacity='1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.hero-accent-bar {
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, oklch(0.78 0.18 195) 0%, oklch(0.68 0.22 350) 50%, oklch(0.78 0.18 195) 100%);
}

.hero-content {
  position: relative;
  z-index: 10;
  max-width: 800px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  background: oklch(0.78 0.18 195 / 12%);
  border: 1px solid oklch(0.78 0.18 195 / 30%);
  border-radius: 100px;
  margin-bottom: 1.5rem;
}

.badge-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: oklch(0.82 0.16 195);
}

.badge-icon svg {
  width: 100%;
  height: 100%;
}

.badge-text {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: oklch(0.82 0.16 195);
}

.hero-title {
  font-size: clamp(2.25rem, 4.5vw, 3.5rem);
  font-weight: 700;
  color: oklch(0.96 0.01 240);
  line-height: 1.15;
  margin-bottom: 1.25rem;
  font-family: 'Jakarta Sans', sans-serif;
}

.title-accent {
  display: block;
  color: oklch(0.78 0.18 195);
}

.hero-description {
  font-size: 1.125rem;
  line-height: 1.7;
  color: oklch(0.96 0.01 240 / 70%);
  margin-bottom: 2.5rem;
  max-width: 600px;
}

.hero-stats {
  display: flex;
  gap: 3rem;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: oklch(0.82 0.16 195);
  line-height: 1;
  font-family: 'Jakarta Sans', sans-serif;
}

.stat-label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: oklch(0.7 0.03 240);
}

.contact-content {
  padding: 4rem 5rem;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 4rem;
  max-width: 1400px;
  margin: 0 auto;
}

.info-column {
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: oklch(0.96 0.01 240);
  margin-bottom: 0.75rem;
  font-family: 'Jakarta Sans', sans-serif;
}

.section-description {
  font-size: 1rem;
  line-height: 1.6;
  color: oklch(0.96 0.01 240 / 70%);
  margin-bottom: 1.5rem;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  background: oklch(0.13 0.025 280 / 55%);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid oklch(0.7 0.1 280 / 15%);
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.info-card:hover {
  border-color: oklch(0.78 0.18 195 / 45%);
  box-shadow: 0 8px 24px oklch(0 0 0 / 40%), 0 0 28px oklch(0.78 0.18 195 / 18%);
}

.info-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: oklch(0.78 0.18 195 / 12%);
  border-radius: 10px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.info-card:hover .info-icon {
  background: oklch(0.78 0.18 195);
}

.info-icon svg {
  width: 22px;
  height: 22px;
  color: oklch(0.82 0.16 195);
  transition: all 0.3s ease;
}

.info-card:hover .info-icon svg {
  color: oklch(0.08 0.02 280);
}

.info-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: oklch(0.7 0.03 240);
}

.info-value {
  font-size: 0.95rem;
  font-weight: 500;
  color: oklch(0.96 0.01 240);
}

.info-arrow {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: oklch(0.7 0.03 240);
  transition: all 0.3s ease;
}

.info-card:hover .info-arrow {
  color: oklch(0.82 0.16 195);
  transform: translateX(4px);
}

.info-arrow svg {
  width: 100%;
  height: 100%;
}

.offices-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.office-card {
  padding: 1.25rem;
  background: oklch(0.15 0.03 280 / 65%);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid oklch(0.7 0.1 280 / 15%);
  border-radius: 12px;
  transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
  will-change: transform, opacity;
}

.office-card:hover {
  border-color: oklch(0.78 0.18 195 / 40%);
  box-shadow: 0 12px 32px oklch(0 0 0 / 45%), 0 0 30px oklch(0.78 0.18 195 / 16%);
}

.office-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.office-badge {
  font-size: 0.65rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.badge-hq {
  background: oklch(0.78 0.18 195 / 20%);
  color: oklch(0.82 0.16 195);
}

.badge-regional {
  background: oklch(0.68 0.22 350 / 20%);
  color: oklch(0.74 0.2 350);
}

.office-role {
  font-size: 0.7rem;
  color: oklch(0.7 0.03 240);
}

.office-name {
  display: block;
  font-size: 1rem;
  font-weight: 600;
  color: oklch(0.96 0.01 240);
  margin-bottom: 0.375rem;
}

.office-address {
  font-size: 0.8rem;
  color: oklch(0.7 0.03 240);
  line-height: 1.4;
}

.form-column {
  display: flex;
  justify-content: end;
}

.contact-form {
  width: 100%;
  max-width: 480px;
  padding: 2.5rem;
  background: oklch(0.12 0.025 280 / 70%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid oklch(0.7 0.1 280 / 15%);
  border-radius: 16px;
  box-shadow: 0 8px 40px oklch(0 0 0 / 45%);
}

.form-header {
  margin-bottom: 2rem;
}

.form-label {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: oklch(0.82 0.16 195);
  margin-bottom: 0.5rem;
}

.form-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: oklch(0.96 0.01 240);
  font-family: 'Jakarta Sans', sans-serif;
}

.title-highlight {
  color: oklch(0.78 0.18 195);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-label-input {
  display: block;
  font-size: 0.8rem;
  font-weight: 500;
  color: oklch(0.7 0.03 240);
  margin-bottom: 0.5rem;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.875rem 1rem;
  background: oklch(0.13 0.025 280 / 60%);
  border: 1px solid oklch(0.7 0.1 280 / 25%);
  border-radius: 8px;
  font-size: 0.95rem;
  color: oklch(0.96 0.01 240);
  transition: all 0.3s ease;
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: oklch(0.7 0.03 240);
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: oklch(0.78 0.18 195);
  background: oklch(0.15 0.03 280 / 70%);
  box-shadow: 0 0 0 3px oklch(0.78 0.18 195 / 25%), 0 0 18px oklch(0.78 0.18 195 / 20%);
}

.form-input-error {
  border-color: oklch(0.65 0.25 25 / 80%);
  background: oklch(0.16 0.04 25 / 50%);
}

.form-input-error:focus {
  border-color: oklch(0.65 0.25 25 / 80%);
  box-shadow: 0 0 0 3px oklch(0.65 0.25 25 / 25%);
}

.form-error {
  display: block;
  font-size: 0.8rem;
  color: oklch(0.72 0.2 20);
  margin-top: 0.375rem;
  font-weight: 500;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.submit-btn {
  width: 100%;
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, oklch(0.78 0.18 195), oklch(0.68 0.22 350));
  color: oklch(0.08 0.02 280);
  font-size: 1rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px oklch(0.78 0.18 195 / 45%);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submitting {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.spinner {
  width: 18px;
  height: 18px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.success-message {
  text-align: center;
  padding: 1.5rem 0;
}

.success-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: oklch(0.75 0.18 145 / 12%);
  border-radius: 50%;
}

.success-icon svg {
  width: 32px;
  height: 32px;
  color: oklch(0.75 0.18 145);
}

.success-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: oklch(0.96 0.01 240);
  margin-bottom: 0.5rem;
  font-family: 'Jakarta Sans', sans-serif;
}

.success-desc {
  font-size: 0.9rem;
  line-height: 1.6;
  color: oklch(0.7 0.03 240);
  margin-bottom: 1.5rem;
}

.reset-btn {
  padding: 0.75rem 1.5rem;
  background: transparent;
  color: oklch(0.82 0.16 195);
  font-size: 0.9rem;
  font-weight: 600;
  border: 2px solid oklch(0.78 0.18 195);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: oklch(0.78 0.18 195);
  color: oklch(0.08 0.02 280);
}

.landing-footer {
  position: relative;
  overflow: hidden;
  background: oklch(0.07 0.02 280 / 92%);
  color: oklch(0.96 0.01 240 / 78%);
  border-top: 1px solid oklch(0.7 0.1 280 / 12%);
}

.footer-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.footer-map-layer {
  position: absolute;
  inset: 0;
}

.footer-map-bg {
  position: absolute;
  inset: 0;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center center;
  filter: hue-rotate(150deg) brightness(0.6) contrast(1.12);
  opacity: 0.14;
  transform: none;
}

.footer-map-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(100deg, oklch(0.08 0.02 280 / 88%) 18%, oklch(0.1 0.03 285 / 72%) 56%, oklch(0.07 0.02 280 / 85%) 100%);
}

.footer-noise-overlay {
  position: absolute;
  inset: 0;
  opacity: 0.02;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

.footer-inner {
  position: relative;
  z-index: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 3rem 5rem 2rem;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr 1fr 1.2fr;
  gap: 1.75rem;
}

.footer-brand-title {
  color: oklch(0.96 0.01 240);
}

.footer-heading {
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: oklch(0.82 0.16 195);
  margin-bottom: 0.8rem;
}

.footer-description {
  font-size: 0.95rem;
  line-height: 1.6;
  max-width: 34ch;
}

.footer-links,
.footer-contact-list,
.footer-bottom-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links,
.footer-contact-list {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.footer-contact-list {
  color: oklch(0.7 0.03 240);
  font-size: 0.92rem;
  line-height: 1.5;
}

.contact-name {
  color: oklch(0.96 0.01 240);
  font-weight: 600;
}

.footer-link {
  color: oklch(0.7 0.03 240);
  text-decoration: none;
  transition: color 0.2s ease;
  text-underline-offset: 0.18em;
}

.footer-link:hover {
  color: oklch(0.82 0.16 195);
  text-decoration: underline;
}

.footer-link:focus-visible {
  outline: 2px solid oklch(0.78 0.18 195);
  outline-offset: 2px;
  border-radius: 3px;
  color: oklch(0.96 0.01 240);
}

.footer-bottom {
  margin-top: 2.25rem;
  padding-top: 1.1rem;
  border-top: 1px solid oklch(0.96 0.01 240 / 10%);
  display: flex;
  gap: 1rem;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
}

.footer-copyright {
  margin: 0;
  font-size: 0.9rem;
  color: oklch(0.7 0.03 240);
}

.footer-bottom-links {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.footer-link-small {
  font-size: 0.88rem;
}

@media (max-width: 1200px) {
  .contact-hero {
    padding: 4rem 3rem 3rem;
  }

  .contact-content {
    padding: 3rem 3rem;
  }

  .content-grid {
    gap: 3rem;
  }

  .footer-inner {
    padding: 2.5rem 3rem 1.75rem;
  }

  .footer-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 1.5rem 2rem;
  }

  .footer-map-bg {
    transform: none;
    opacity: 0.2;
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
    gap: 2.5rem;
  }

  .form-column {
    justify-content: start;
  }

  .contact-form {
    max-width: 100%;
  }

  .offices-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .contact-hero {
    padding: 3rem 2rem 2.5rem;
  }

  .contact-content {
    padding: 2.5rem 1.5rem;
  }

  .hero-stats {
    flex-wrap: wrap;
    gap: 1.5rem;
  }

  .stat-value {
    font-size: 2rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .info-card {
    padding: 1rem;
  }

  .office-card {
    padding: 1rem;
  }

  .footer-inner {
    padding: 2.25rem 1.5rem 1.5rem;
  }

  .footer-grid {
    grid-template-columns: 1fr;
    gap: 1.4rem;
  }

  .footer-description {
    max-width: 100%;
  }

  .footer-map-bg {
    transform: none;
    opacity: 0.16;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 1.75rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .contact-form {
    padding: 1.5rem;
  }

  .form-title {
    font-size: 1.25rem;
  }
}
</style>
