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

/** Login returns either a full session (AuthResponse) or an MFA challenge. */
export interface MfaChallenge {
  mfaRequired: true;
  mfaToken: string;
}
export type LoginResult = AuthResponse | MfaChallenge;

export function isMfaChallenge(result: LoginResult): result is MfaChallenge {
  return (result as MfaChallenge).mfaRequired === true;
}

export interface MfaEnrollResponse {
  secret: string;
  otpauthUri: string;
  qrDataUri: string;
}

export const authApi = {
  login(payload: LoginRequest) {
    // skipRefresh: a 401 here means bad credentials, not an expired session.
    return request<LoginResult>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    }, { skipRefresh: true });
  },
  register(payload: RegisterRequest) {
    return request<AuthResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    }, { skipRefresh: true });
  },
  me() {
    return request<UserSummary>('/api/v1/auth/me');
  },
  logout() {
    return request<null>('/api/v1/auth/logout', { method: 'POST' }, { skipRefresh: true });
  },
  // --- MFA (TOTP) ---
  mfaVerify(payload: { mfaToken: string; code: string }) {
    return request<AuthResponse>('/api/v1/auth/mfa/verify', {
      method: 'POST',
      body: JSON.stringify(payload)
    }, { skipRefresh: true });
  },
  mfaStatus() {
    return request<{ enabled: boolean }>('/api/v1/auth/mfa/status');
  },
  mfaEnroll() {
    return request<MfaEnrollResponse>('/api/v1/auth/mfa/enroll', { method: 'POST' });
  },
  mfaVerifyEnroll(payload: { code: string }) {
    return request<{ recoveryCodes: string[] }>('/api/v1/auth/mfa/verify-enroll', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },
  mfaDisable(payload: { code: string }) {
    return request<null>('/api/v1/auth/mfa', {
      method: 'DELETE',
      body: JSON.stringify(payload)
    });
  },
  forgotPassword(payload: ForgotPasswordRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify(payload)
    }, { skipRefresh: true });
  },
  resetPassword(payload: ResetPasswordRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify(payload)
    }, { skipRefresh: true });
  },
  verifyEmail(payload: VerifyEmailRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/verify-email', {
      method: 'POST',
      body: JSON.stringify(payload)
    }, { skipRefresh: true });
  },
  resendVerification(payload: ResendVerificationRequest) {
    return request<AuthMessageResponse>('/api/v1/auth/resend-verification', {
      method: 'POST',
      body: JSON.stringify(payload)
    }, { skipRefresh: true });
  }
};
