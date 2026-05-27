import { computed, ref } from 'vue';
import { authApi, type AuthResponse, type UserSummary } from './authApi';
import { clearAccessToken, getAccessToken, setAccessToken } from './authTokenStorage';
import { ApiError } from '@/lib/api/apiError';

const currentUser = ref<UserSummary | null>(null);
const isBootstrapping = ref(false);
const sessionError = ref<ApiError | null>(null);

const isAuthenticated = computed(() => Boolean(currentUser.value && getAccessToken()));

function clearSession(): void {
  currentUser.value = null;
  sessionError.value = null;
}

async function bootstrapSession(): Promise<void> {
  if (isBootstrapping.value) {
    return;
  }

  const token = getAccessToken();
  if (!token) {
    clearSession();
    return;
  }

  isBootstrapping.value = true;
  try {
    const user = await authApi.me();
    currentUser.value = user;
    sessionError.value = null;
  } catch (error) {
    clearAccessToken();
    currentUser.value = null;
    if (error instanceof ApiError) {
      sessionError.value = error;
    } else {
      sessionError.value = new ApiError({
        code: 'SESSION_BOOTSTRAP_FAILED',
        message: 'Failed to bootstrap session',
        status: 0
      });
    }
  } finally {
    isBootstrapping.value = false;
  }
}

function setSessionFromAuthResponse(authResponse: AuthResponse): void {
  setAccessToken(authResponse.accessToken);
  currentUser.value = authResponse.user;
  sessionError.value = null;
}

function logout(): void {
  clearAccessToken();
  clearSession();
}

export function useAuthSession() {
  return {
    currentUser,
    isAuthenticated,
    isBootstrapping,
    sessionError,
    bootstrapSession,
    setSessionFromAuthResponse,
    logout,
    clearSession
  };
}

