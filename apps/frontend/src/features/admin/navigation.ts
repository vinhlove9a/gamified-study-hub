/**
 * Single source of truth for the admin console navigation.
 *
 * The admin sidebar, topbar quick-search and the router all read from these
 * structures so a new admin "section" only has to be declared once. Mirrors the
 * shape of the user-facing `app-shell/navigation.ts` but scoped to `/admin`.
 */

export interface AdminNavItem {
  /** Visible label (Vietnamese). */
  label: string;
  /** Icon name resolved by `AppIcon`. */
  icon: string;
  /** Router path. */
  to: string;
  /** Optional pill shown on the right of the sidebar row (e.g. a pending count). */
  badge?: string;
  /** Accent used for the badge / active glow. */
  accent?: 'cyan' | 'magenta' | 'green' | 'violet' | 'amber';
  /** Short description used as a subtitle / search hint. */
  blurb?: string;
  /** Keyword string to widen fuzzy matching. */
  keywords?: string;
}

export interface AdminNavGroup {
  label: string;
  items: AdminNavItem[];
}

export const adminNavGroups: AdminNavGroup[] = [
  {
    label: 'Điều hành',
    items: [
      {
        label: 'Tổng quan',
        icon: 'home',
        to: '/admin',
        accent: 'cyan',
        blurb: 'KPI toàn hệ thống, tình trạng máy chủ, hoạt động gần đây',
        keywords: 'dashboard overview tong quan trang chu'
      },
      {
        label: 'Phân tích',
        icon: 'chart',
        to: '/admin/analytics',
        accent: 'violet',
        blurb: 'Tương tác, giữ chân, phễu chuyển đổi, hiệu suất nội dung',
        keywords: 'analytics phan tich retention funnel cohort'
      },
      {
        label: 'Tài chính',
        icon: 'coin',
        to: '/admin/finance',
        accent: 'amber',
        blurb: 'Doanh thu VIP, giao dịch coin/gem, nền kinh tế ảo',
        keywords: 'finance revenue doanh thu vip coin gem giao dich'
      }
    ]
  },
  {
    label: 'Quản lý',
    items: [
      {
        label: 'Người dùng',
        icon: 'users',
        to: '/admin/users',
        accent: 'cyan',
        badge: '12.4K',
        blurb: 'Tài khoản, vai trò, trạng thái, khoá/mở, đặt lại mật khẩu',
        keywords: 'users nguoi dung tai khoan role ban khoa'
      },
      {
        label: 'Nội dung',
        icon: 'file-text',
        to: '/admin/content',
        accent: 'green',
        badge: '38',
        blurb: 'Duyệt tài liệu tải lên, danh mục, gắn cờ vi phạm',
        keywords: 'content noi dung tai lieu document duyet moderation'
      },
      {
        label: 'Gamification',
        icon: 'trophy',
        to: '/admin/gamification',
        accent: 'magenta',
        blurb: 'Nhiệm vụ, huy hiệu, phần thưởng hằng ngày, vòng quay',
        keywords: 'gamification mission badge reward nhiem vu huy hieu'
      },
      {
        label: 'Kiểm duyệt',
        icon: 'flag',
        to: '/admin/reports',
        accent: 'amber',
        badge: '7',
        blurb: 'Báo cáo vi phạm, hàng đợi xử lý người dùng & nội dung',
        keywords: 'reports kiem duyet bao cao vi pham abuse flag'
      }
    ]
  },
  {
    label: 'Vận hành',
    items: [
      {
        label: 'Hệ thống',
        icon: 'settings',
        to: '/admin/system',
        accent: 'cyan',
        blurb: 'Cờ tính năng, bảo trì, nhật ký kiểm toán, đội ngũ admin',
        keywords: 'system he thong feature flag maintenance audit log settings'
      }
    ]
  }
];

/** Flattened list — handy for the topbar search + breadcrumb title lookups. */
export const allAdminNavItems: AdminNavItem[] = adminNavGroups.flatMap((g) => g.items);
