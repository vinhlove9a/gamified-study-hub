import { getAccessToken } from '@/features/auth/authTokenStorage';
import { ApiError, type ApiFieldError } from './apiError';

interface ApiResponseEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

interface ApiErrorEnvelope {
  success?: boolean;
  code?: string;
  message?: string;
  fieldErrors?: ApiFieldError[];
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');

function toAbsoluteUrl(path: string): string {
  if (!API_BASE_URL) {
    return path;
  }
  if (/^https?:\/\//.test(path)) {
    return path;
  }
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
}

function toApiError(status: number, payload: unknown): ApiError {
  const body = (payload || {}) as ApiErrorEnvelope;
  return new ApiError({
    code: body.code || 'UNKNOWN_ERROR',
    message: body.message || 'Request failed',
    status,
    fieldErrors: body.fieldErrors
  });
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getAccessToken();
  const headers = new Headers(init?.headers || {});
  headers.set('Content-Type', 'application/json');
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(toAbsoluteUrl(path), {
    ...init,
    headers
  });

  let payload: unknown = null;
  try {
    payload = await response.json();
  } catch {
    payload = null;
  }

  if (!response.ok) {
    throw toApiError(response.status, payload);
  }

  const envelope = payload as ApiResponseEnvelope<T>;
  if (!envelope?.success) {
    throw new ApiError({
      code: 'API_RESPONSE_INVALID',
      message: envelope?.message || 'Request failed',
      status: response.status
    });
  }

  return envelope.data;
}
