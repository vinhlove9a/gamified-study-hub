import type { NavigationGuardWithThis } from 'vue-router';
import { getAccessToken } from '@/features/auth/authTokenStorage';
import { useAuthSession } from '@/features/auth/authSession';

/** Where an authenticated user lands by default: admins get the Dashboard. */
export function defaultRouteForRole(isAdmin: boolean): string {
  return isAdmin ? '/admin' : '/app';
}

export const authGuard: NavigationGuardWithThis<undefined> = async (to) => {
  const { isAuthenticated, isAdmin, currentUser, bootstrapSession } = useAuthSession();

  // Resolve the session from a stored token before any auth/role decision.
  if ((to.meta.requiresAuth || to.meta.guestOnly) && !isAuthenticated.value && getAccessToken() && !currentUser.value) {
    await bootstrapSession();
  }

  if (to.meta.requiresAuth) {
    if (!isAuthenticated.value) {
      return { path: '/auth/login', query: { redirect: to.fullPath } };
    }

    // Admin-only routes fall back to the standard app shell for non-admins.
    if (to.meta.requiresAdmin && !isAdmin.value) {
      return { path: '/app' };
    }

    return true;
  }

  if (to.meta.guestOnly && isAuthenticated.value) {
    return { path: defaultRouteForRole(isAdmin.value) };
  }

  return true;
};
