import { createApp } from 'vue';
import App from './App.vue';
import gsapPlugin from '../plugins/gsapPlugin';
import router from '@/router';
import '../styles/main.css';

const app = createApp(App);
app.use(gsapPlugin);
app.use(router);
app.mount('#app');
