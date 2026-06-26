/**
 * Single source of truth for the user-facing app shell navigation.
 *
 * The sidebar, command palette (⌘K), the topbar app-launcher and the router all
 * read from these structures so a new "function" only has to be declared once.
 * Any item whose `to` is NOT in {@link BUILT_PATHS} is rendered by the shared
 * `AppComingSoonPage`, using its `title` / `icon` / `blurb` as route meta.
 */

export interface AppNavItem {
  /** Visible label (Vietnamese). */
  label: string;
  /** Icon name resolved by `AppIcon`. */
  icon: string;
  /** Router path. */
  to: string;
  /** Optional pill shown on the right of the sidebar row. */
  badge?: string;
  /** Description used by the coming-soon route + command palette subtitle. */
  blurb?: string;
  /** Short keyword string to widen command-palette fuzzy matching. */
  keywords?: string;
}

export interface AppNavGroup {
  /** Group heading. */
  label: string;
  /** Icon used when the group is shown as a launcher tile. */
  icon: string;
  items: AppNavItem[];
}

/** Routes that have a real, hand-built page component. Everything else → coming soon. */
export const BUILT_PATHS = new Set<string>([
  '/app',
  '/app/documents',
  '/app/missions',
  '/app/leaderboard',
  '/app/store',
  '/app/achievements',
  '/app/profile',
  '/app/settings',
  // Discovery
  '/app/explore',
  '/app/collections',
  // Learning
  '/app/paths',
  '/app/challenges',
  '/app/calendar',
  '/app/notes',
  '/app/flashcards',
  '/app/quizzes',
  '/app/assistant',
  '/app/focus',
  // Community
  '/app/friends',
  '/app/groups',
  '/app/forum',
  '/app/events',
  // Achievement
  '/app/stats',
  // Rewards
  '/app/daily-reward',
  '/app/offers',
  '/app/spin',
  // Personal
  '/app/wallet',
  '/app/downloads',
  '/app/history',
  // Discovery
  '/app/feed',
  '/app/trending',
  '/app/new',
  '/app/favorites',
  // Community
  '/app/mentors',
  // Achievement
  '/app/badges',
  '/app/certificates',
  // Support
  '/app/help',
  '/app/feedback'
]);

export const navGroups: AppNavGroup[] = [
  {
    label: 'Khám phá',
    icon: 'compass',
    items: [
      { label: 'Trang chủ', icon: 'home', to: '/app', keywords: 'dashboard bảng điều khiển trang chu' },
      { label: 'Bảng tin', icon: 'list', to: '/app/feed', blurb: 'Dòng hoạt động từ bạn học, nhóm và các tài liệu bạn theo dõi.', keywords: 'feed news hoat dong' },
      { label: 'Thư viện tài liệu', icon: 'file-text', to: '/app/documents', badge: '24', keywords: 'documents library tai lieu' },
      { label: 'Khám phá', icon: 'compass', to: '/app/explore', blurb: 'Khám phá tài liệu thịnh hành, chủ đề mới và nội dung được tuyển chọn cho bạn.' },
      { label: 'Thịnh hành', icon: 'trending-up', to: '/app/trending', blurb: 'Những tài liệu và chủ đề đang được cộng đồng quan tâm nhất tuần này.', keywords: 'trending hot pho bien' },
      { label: 'Mới cập nhật', icon: 'sparkles', to: '/app/new', blurb: 'Tài liệu vừa được thêm vào thư viện, cập nhật theo thời gian thực.', keywords: 'new latest moi' },
      { label: 'Bộ sưu tập', icon: 'folder', to: '/app/collections', blurb: 'Sắp xếp tài liệu thành các bộ sưu tập theo chủ đề và mục tiêu học tập.' },
      { label: 'Yêu thích', icon: 'heart', to: '/app/favorites', blurb: 'Tất cả tài liệu, nhiệm vụ và bộ sưu tập bạn đã đánh dấu yêu thích.', keywords: 'favorites yeu thich saved' }
    ]
  },
  {
    label: 'Học tập',
    icon: 'graduation',
    items: [
      { label: 'Nhiệm vụ', icon: 'target', to: '/app/missions', badge: '3', keywords: 'missions quests nhiem vu' },
      { label: 'Tập trung', icon: 'clock', to: '/app/focus', blurb: 'Hẹn giờ Pomodoro để học sâu, theo dõi phiên tập trung và nghỉ ngơi khoa học.', keywords: 'focus pomodoro tap trung timer hen gio dong ho' },
      { label: 'Lộ trình học', icon: 'route', to: '/app/paths', blurb: 'Theo dõi lộ trình học có cấu trúc với các cột mốc và phần thưởng.' },
      { label: 'Thử thách', icon: 'flag', to: '/app/challenges', blurb: 'Thử thách hằng ngày và theo mùa để nhận thưởng XP và coin lớn.', keywords: 'challenges thu thach' },
      { label: 'Lịch học', icon: 'calendar', to: '/app/calendar', blurb: 'Lên lịch các phiên học, nhắc nhở và xem chuỗi học của bạn theo tháng.', keywords: 'calendar schedule lich' },
      { label: 'Ghi chú', icon: 'edit', to: '/app/notes', blurb: 'Ghi chú nhanh, tổ chức theo tài liệu và đồng bộ trên mọi thiết bị.', keywords: 'notes ghi chu' },
      { label: 'Thẻ ghi nhớ', icon: 'layers', to: '/app/flashcards', blurb: 'Học với thẻ ghi nhớ thông minh theo phương pháp lặp lại ngắt quãng.', keywords: 'flashcards the ghi nho srs' },
      { label: 'Bài kiểm tra', icon: 'check-circle', to: '/app/quizzes', blurb: 'Luyện tập với các bài kiểm tra tương tác và theo dõi điểm số.', keywords: 'quiz bai kiem tra test' },
      { label: 'Trợ lý AI', icon: 'bot', to: '/app/assistant', blurb: 'Hỏi đáp, tóm tắt tài liệu và tạo dàn ý ôn tập với trợ lý AI.', keywords: 'ai assistant tro ly chatbot' }
    ]
  },
  {
    label: 'Cộng đồng',
    icon: 'users',
    items: [
      { label: 'Bảng xếp hạng', icon: 'trophy', to: '/app/leaderboard', keywords: 'leaderboard xep hang ranking' },
      { label: 'Bạn học', icon: 'users', to: '/app/friends', blurb: 'Kết nối với bạn học, theo dõi tiến độ và thi đua cùng nhau.', keywords: 'friends ban hoc' },
      { label: 'Nhóm học', icon: 'message', to: '/app/groups', blurb: 'Tham gia nhóm học để chia sẻ tài liệu, thảo luận và học cùng nhau.', keywords: 'groups nhom hoc study group' },
      { label: 'Diễn đàn', icon: 'globe', to: '/app/forum', blurb: 'Đặt câu hỏi, chia sẻ kiến thức và thảo luận với cả cộng đồng.', keywords: 'forum dien dan' },
      { label: 'Sự kiện', icon: 'calendar', to: '/app/events', blurb: 'Lịch các sự kiện học tập, cuộc thi và buổi học trực tuyến.', keywords: 'events su kien' },
      { label: 'Cố vấn', icon: 'graduation', to: '/app/mentors', blurb: 'Tìm cố vấn, đặt lịch tư vấn 1-1 và nhận định hướng học tập.', keywords: 'mentors co van tutor' }
    ]
  },
  {
    label: 'Thành tích',
    icon: 'award',
    items: [
      { label: 'Thành tựu', icon: 'award', to: '/app/achievements', keywords: 'achievements thanh tuu' },
      { label: 'Huy hiệu', icon: 'badge', to: '/app/badges', blurb: 'Bộ sưu tập huy hiệu bạn đã mở khoá qua các cột mốc học tập.', keywords: 'badges huy hieu' },
      { label: 'Chứng chỉ', icon: 'shield', to: '/app/certificates', blurb: 'Chứng chỉ hoàn thành khoá học và lộ trình, có thể chia sẻ.', keywords: 'certificates chung chi' },
      { label: 'Thống kê', icon: 'chart', to: '/app/stats', blurb: 'Phân tích chi tiết thời gian học, xu hướng và điểm mạnh của bạn.', keywords: 'stats analytics thong ke' }
    ]
  },
  {
    label: 'Phần thưởng',
    icon: 'gift',
    items: [
      { label: 'Cửa hàng Coin', icon: 'store', to: '/app/store', keywords: 'store shop cua hang' },
      { label: 'Quà hằng ngày', icon: 'gift', to: '/app/daily-reward', blurb: 'Điểm danh mỗi ngày để nhận coin, XP và vật phẩm bất ngờ.', keywords: 'daily reward qua hang ngay' },
      { label: 'Ưu đãi', icon: 'gem', to: '/app/offers', blurb: 'Các ưu đãi giới hạn thời gian và gói VIP dành riêng cho bạn.', keywords: 'offers uu dai deals' },
      { label: 'Vòng quay may mắn', icon: 'zap', to: '/app/spin', blurb: 'Quay số mỗi ngày để có cơ hội trúng coin và phần thưởng lớn.', keywords: 'spin lucky wheel vong quay' }
    ]
  },
  {
    label: 'Cá nhân',
    icon: 'user',
    items: [
      { label: 'Hồ sơ', icon: 'user', to: '/app/profile', keywords: 'profile ho so account' },
      { label: 'Ví của tôi', icon: 'coin', to: '/app/wallet', blurb: 'Quản lý số dư coin, gem và xem lịch sử giao dịch của bạn.', keywords: 'wallet vi coin balance' },
      { label: 'Tải xuống', icon: 'download', to: '/app/downloads', blurb: 'Tài liệu đã tải về để học ngoại tuyến, quản lý dung lượng.', keywords: 'downloads tai xuong offline' },
      { label: 'Lịch sử', icon: 'clock', to: '/app/history', blurb: 'Lịch sử đọc tài liệu và hoạt động gần đây của bạn.', keywords: 'history lich su recent' }
    ]
  }
];

export const bottomNavItems: AppNavItem[] = [
  { label: 'Cài đặt', icon: 'settings', to: '/app/settings', keywords: 'settings cai dat preferences' },
  { label: 'Trợ giúp', icon: 'help', to: '/app/help', blurb: 'Tìm câu trả lời, hướng dẫn sử dụng và liên hệ đội ngũ hỗ trợ.', keywords: 'help support tro giup' },
  { label: 'Phản hồi', icon: 'flag', to: '/app/feedback', blurb: 'Gửi góp ý, báo lỗi hoặc đề xuất tính năng mới cho Study Hub.', keywords: 'feedback phan hoi report' }
];

/** Flat list of every navigable item — used by the command palette. */
export const allNavItems: AppNavItem[] = [
  ...navGroups.flatMap((g) => g.items),
  ...bottomNavItems
];

/** De-duplicated coming-soon route descriptors derived from the nav config. */
export interface ComingSoonRoute {
  path: string;
  name: string;
  title: string;
  icon: string;
  blurb?: string;
}

export const comingSoonRoutes: ComingSoonRoute[] = (() => {
  const seen = new Set<string>();
  const out: ComingSoonRoute[] = [];
  for (const item of allNavItems) {
    if (BUILT_PATHS.has(item.to) || seen.has(item.to)) continue;
    seen.add(item.to);
    out.push({
      path: item.to.replace(/^\/app\//, ''),
      name: `app-${item.to.replace(/^\/app\//, '').replace(/\//g, '-')}`,
      title: item.label,
      icon: item.icon,
      blurb: item.blurb
    });
  }
  return out;
})();
