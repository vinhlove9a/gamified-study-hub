import { ApiError, type ApiFieldError } from './apiError';
import { csrfHeader } from '@/features/auth/csrf';

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

interface RequestOptions {
  /** Skip the transparent 401 -> refresh -> retry (used by the auth endpoints themselves). */
  skipRefresh?: boolean;
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
const REFRESH_PATH = '/api/v1/auth/refresh';

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

// --- Session-lost callback (set by the app so this layer stays framework-free) ---
let sessionLostHandler: (() => void) | null = null;

export function setSessionLostHandler(handler: () => void): void {
  sessionLostHandler = handler;
}

// --- Single-flight refresh: concurrent 401s share one /refresh call ---
let refreshInFlight: Promise<void> | null = null;

async function refreshOnce(): Promise<void> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      const response = await fetch(toAbsoluteUrl(REFRESH_PATH), {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', ...csrfHeader('POST') }
      });
      if (!response.ok) {
        throw new ApiError({ code: 'SESSION_EXPIRED', message: 'Session expired', status: response.status });
      }
    })().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

async function doFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers || {});
  headers.set('Content-Type', 'application/json');
  for (const [key, value] of Object.entries(csrfHeader(init?.method))) {
    headers.set(key, value);
  }

  const response = await fetch(toAbsoluteUrl(path), {
    ...init,
    credentials: 'include',
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

export async function request<T>(path: string, init?: RequestInit, options?: RequestOptions): Promise<T> {
  try {
    return await doFetch<T>(path, init);
  } catch (error) {
    const isUnauthorized = error instanceof ApiError && error.status === 401;
    if (!isUnauthorized || options?.skipRefresh) {
      throw error;
    }

    // Access token likely expired: refresh once (single-flight), then retry once.
    try {
      await refreshOnce();
    } catch {
      sessionLostHandler?.();
      throw error;
    }
    return doFetch<T>(path, init);
  }
}
