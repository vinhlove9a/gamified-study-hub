import { createApp } from 'vue';
import App from './App.vue';
import gsapPlugin from '../plugins/gsapPlugin';
import '../styles/main.css';

const app = createApp(App);
app.use(gsapPlugin);
app.mount('#app');
