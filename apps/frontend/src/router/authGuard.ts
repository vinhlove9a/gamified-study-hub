import type { NavigationGuardWithThis } from 'vue-router';
import { getAccessToken } from '@/features/auth/authTokenStorage';
import { useAuthSession } from '@/features/auth/authSession';

export const authGuard: NavigationGuardWithThis<undefined> = async (to) => {
  const { isAuthenticated, currentUser, bootstrapSession } = useAuthSession();

  if (to.meta.requiresAuth) {
    if (!isAuthenticated.value && getAccessToken() && !currentUser.value) {
      await bootstrapSession();
    }

    if (isAuthenticated.value) {
      return true;
    }

    return {
      path: '/auth/login',
      query: {
        redirect: to.fullPath
      }
    };
  }

  if (to.meta.guestOnly && isAuthenticated.value) {
    return { path: '/app' };
  }

  return true;
};
