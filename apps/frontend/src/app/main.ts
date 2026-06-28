import { createApp } from 'vue';
import App from './App.vue';
import gsapPlugin from '../plugins/gsapPlugin';
import router from '@/router';
import { setSessionLostHandler } from '@/lib/api/httpClient';
import { useAuthSession } from '@/features/auth/authSession';
import '../styles/main.css';
import '@/styles/dashboard.css';
import '@/styles/app-effects.css';
import '@/styles/auth.css';

// When a transparent refresh fails, drop the session and bounce to login
// (unless already on an auth page) — keeps the http layer framework-agnostic.
const { clearSession } = useAuthSession();
setSessionLostHandler(() => {
  clearSession();
  if (!window.location.pathname.startsWith('/auth/')) {
    window.location.assign('/auth/login');
  }
});

const app = createApp(App);
app.use(gsapPlugin);
app.use(router);
app.mount('#app');
