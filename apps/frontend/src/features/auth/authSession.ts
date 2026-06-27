import { computed, ref } from 'vue';
import { authApi, type AuthResponse, type UserSummary } from './authApi';
import { ApiError } from '@/lib/api/apiError';

const currentUser = ref<UserSummary | null>(null);
const isBootstrapping = ref(false);
const sessionError = ref<ApiError | null>(null);
let bootstrapPromise: Promise<void> | null = null;

// Auth lives in httpOnly cookies (not readable by JS), so the only signal is
// whether /me succeeds.
const isAuthenticated = computed(() => Boolean(currentUser.value));
const isAdmin = computed(() => currentUser.value?.role === 'admin');

function clearSession(): void {
  currentUser.value = null;
  sessionError.value = null;
}

async function bootstrapSession(): Promise<void> {
  if (isBootstrapping.value) {
    if (bootstrapPromise) {
      await bootstrapPromise;
    }
    return;
  }

  isBootstrapping.value = true;
  bootstrapPromise = (async () => {
    try {
      // httpClient transparently refreshes on 401 if a valid refresh cookie
      // exists; only a genuine "not logged in" lands in the catch.
      currentUser.value = await authApi.me();
      sessionError.value = null;
    } catch (error) {
      currentUser.value = null;
      // A plain 401 is the normal anonymous state — don't treat it as an error.
      sessionError.value = error instanceof ApiError && error.status !== 401 ? error : null;
    } finally {
      isBootstrapping.value = false;
      bootstrapPromise = null;
    }
  })();

  await bootstrapPromise;
}

function setSessionFromAuthResponse(authResponse: AuthResponse): void {
  // Cookies are set by the server (Set-Cookie); we only hold the user in memory.
  currentUser.value = authResponse.user;
  sessionError.value = null;
}

async function logout(): Promise<void> {
  try {
    await authApi.logout();
  } catch {
    // Drop local state even if the server call fails.
  } finally {
    clearSession();
  }
}

export function useAuthSession() {
  return {
    currentUser,
    isAuthenticated,
    isAdmin,
    isBootstrapping,
    sessionError,
    bootstrapSession,
    setSessionFromAuthResponse,
    logout,
    clearSession
  };
}
