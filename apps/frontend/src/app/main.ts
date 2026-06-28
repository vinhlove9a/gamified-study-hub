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

// When a transparent refresh fails, drop the session and bounce to login —
// but ONLY if a session was actually active. A failed refresh during the
// initial /me probe just means "anonymous visitor", and guests must be able to
// browse public pages (landing, auth screens) without being kicked to login.
const { clearSession, isAuthenticated } = useAuthSession();
setSessionLostHandler(() => {
  const lostActiveSession = isAuthenticated.value;
  clearSession();
  if (lostActiveSession && !window.location.pathname.startsWith('/auth/')) {
    window.location.assign('/auth/login');
  }
});

const app = createApp(App);
app.use(gsapPlugin);
app.use(router);
app.mount('#app');
