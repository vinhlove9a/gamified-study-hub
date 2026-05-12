import axios from 'axios';
import { appEnv } from '../config/env';

export const http = axios.create({
  baseURL: appEnv.apiBaseUrl,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});
