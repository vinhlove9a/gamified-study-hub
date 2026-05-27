import { createRouter, createWebHistory } from 'vue-router';
import LandingPage from '@/pages/LandingPage.vue';
import LoginPage from '@/pages/auth/LoginPage.vue';
import RegisterPage from '@/pages/auth/RegisterPage.vue';
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage.vue';
import ResetPasswordPage from '@/pages/auth/ResetPasswordPage.vue';
import VerifyEmailPage from '@/pages/auth/VerifyEmailPage.vue';
import AuthNoticePage from '@/pages/auth/AuthNoticePage.vue';
import AuthErrorPage from '@/pages/auth/AuthErrorPage.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'landing',
      component: LandingPage
    },
    {
      path: '/auth/login',
      name: 'auth-login',
      component: LoginPage
    },
    {
      path: '/auth/register',
      name: 'auth-register',
      component: RegisterPage
    },
    {
      path: '/auth/forgot-password',
      name: 'auth-forgot-password',
      component: ForgotPasswordPage
    },
    {
      path: '/auth/reset-password',
      name: 'auth-reset-password',
      component: ResetPasswordPage
    },
    {
      path: '/auth/verify-email',
      name: 'auth-verify-email',
      component: VerifyEmailPage
    },
    {
      path: '/auth/notice',
      name: 'auth-notice',
      component: AuthNoticePage
    },
    {
      path: '/auth/error',
      name: 'auth-error',
      component: AuthErrorPage
    }
  ]
});

export default router;
