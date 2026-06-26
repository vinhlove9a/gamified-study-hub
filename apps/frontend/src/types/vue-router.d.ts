import 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean;
    requiresAdmin?: boolean;
    guestOnly?: boolean;
    /** Coming-soon page heading. */
    title?: string;
    /** Coming-soon page icon (resolved by AppIcon). */
    icon?: string;
    /** Coming-soon page description. */
    blurb?: string;
  }
}
