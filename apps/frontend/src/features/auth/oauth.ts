/**
 * Social login. A full-page navigation (NOT an XHR) to the backend's OAuth2
 * authorization endpoint — same origin, proxied by Caddy to the backend, which
 * redirects to Google and back to /auth/callback with cookies set.
 */
export function startGoogleLogin(): void {
  window.location.assign('/oauth2/authorization/google');
}
