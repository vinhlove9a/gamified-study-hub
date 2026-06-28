import { ref } from 'vue';

/**
 * Holds the short-lived MFA challenge token in memory between the login step and
 * the MFA verification page (kept out of the URL — it's a single-use secret).
 */
const mfaToken = ref<string | null>(null);
const redirectPath = ref<string>('/app');

export function useMfaChallenge() {
  return {
    mfaToken,
    redirectPath,
    setChallenge(token: string, redirect: string) {
      mfaToken.value = token;
      redirectPath.value = redirect || '/app';
    },
    clear() {
      mfaToken.value = null;
      redirectPath.value = '/app';
    }
  };
}
