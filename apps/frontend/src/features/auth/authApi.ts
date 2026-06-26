import { request } from '@/lib/api/httpClient';

export type UserRole = 'admin' | 'user' | (string & {});

export interface UserSummary {
  id: string;
  fullName: string;
  email: string;
  status: string;
  emailVerified: boolean;
  // Optional so the type stays compatible with backends that don't yet emit it;
  // the admin dashboard gate checks `role === 'admin'`.
  role?: UserRole;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserSummary;
}

export interface AuthMessageResponse {
  message: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface VerifyEmailRequest {
  token: string;
}

export interface ResendVerificationRequest {
  email: string;
}

export const authApi = {
  login(payload: LoginRequest) {
    return request<AuthResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  register(payload: RegisterRequest) {
    return request<AuthResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  me() {
    return request<UserSummary>('/api/v1/auth/me');
  },
  forgotPassword(payload: ForgotPasswordRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  resetPassword(payload: ResetPasswordRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  verifyEmail(payload: VerifyEmailRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  resendVerification(payload: ResendVerificationRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/resend-verification', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  }
};
