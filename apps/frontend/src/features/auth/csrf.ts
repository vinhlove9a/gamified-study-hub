/**
 * CSRF double-submit helper. Spring Security (CookieCsrfTokenRepository) sets a
 * JS-readable `XSRF-TOKEN` cookie; we echo it back in the `X-XSRF-TOKEN` header
 * on state-changing requests.
 */

export function getCsrfToken(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

export function isMutating(method?: string): boolean {
  return !!method && /^(POST|PUT|PATCH|DELETE)$/i.test(method);
}

export function csrfHeader(method?: string): Record<string, string> {
  if (!isMutating(method)) {
    return {};
  }
  const token = getCsrfToken();
  return token ? { 'X-XSRF-TOKEN': token } : {};
}
