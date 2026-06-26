import { createApp } from 'vue';
import App from './App.vue';
import gsapPlugin from '../plugins/gsapPlugin';
import router from '@/router';
import '../styles/main.css';
import '@/styles/dashboard.css';
import '@/styles/app-effects.css';

const app = createApp(App);
app.use(gsapPlugin);
app.use(router);
app.mount('#app');
