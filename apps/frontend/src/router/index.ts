import { createRouter, createWebHistory } from 'vue-router';
import LandingPage from '@/pages/LandingPage.vue';
import LoginPage from '@/pages/auth/LoginPage.vue';
import RegisterPage from '@/pages/auth/RegisterPage.vue';
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage.vue';
import ResetPasswordPage from '@/pages/auth/ResetPasswordPage.vue';
import VerifyEmailPage from '@/pages/auth/VerifyEmailPage.vue';
import AuthNoticePage from '@/pages/auth/AuthNoticePage.vue';
import AuthErrorPage from '@/pages/auth/AuthErrorPage.vue';
import AppShellLayout from '@/layouts/AppShellLayout.vue';
import AppHomePage from '@/pages/app/AppHomePage.vue';
import AppDocumentsPage from '@/pages/app/AppDocumentsPage.vue';
import AppMissionsPage from '@/pages/app/AppMissionsPage.vue';
import AppLeaderboardPage from '@/pages/app/AppLeaderboardPage.vue';
import AppStorePage from '@/pages/app/AppStorePage.vue';
import AppAchievementsPage from '@/pages/app/AppAchievementsPage.vue';
import AppProfilePage from '@/pages/app/AppProfilePage.vue';
import AppSettingsPage from '@/pages/app/AppSettingsPage.vue';
import AppComingSoonPage from '@/pages/app/AppComingSoonPage.vue';
import AppExplorePage from '@/pages/app/AppExplorePage.vue';
import AppCollectionsPage from '@/pages/app/AppCollectionsPage.vue';
import AppPathsPage from '@/pages/app/AppPathsPage.vue';
import AppChallengesPage from '@/pages/app/AppChallengesPage.vue';
import AppCalendarPage from '@/pages/app/AppCalendarPage.vue';
import AppNotesPage from '@/pages/app/AppNotesPage.vue';
import AppFlashcardsPage from '@/pages/app/AppFlashcardsPage.vue';
import AppQuizzesPage from '@/pages/app/AppQuizzesPage.vue';
import AppAssistantPage from '@/pages/app/AppAssistantPage.vue';
import AppFocusPage from '@/pages/app/AppFocusPage.vue';
import AppFriendsPage from '@/pages/app/AppFriendsPage.vue';
import AppGroupsPage from '@/pages/app/AppGroupsPage.vue';
import AppForumPage from '@/pages/app/AppForumPage.vue';
import AppEventsPage from '@/pages/app/AppEventsPage.vue';
import AppStatsPage from '@/pages/app/AppStatsPage.vue';
import AppDailyRewardPage from '@/pages/app/AppDailyRewardPage.vue';
import AppOffersPage from '@/pages/app/AppOffersPage.vue';
import AppSpinPage from '@/pages/app/AppSpinPage.vue';
import AppWalletPage from '@/pages/app/AppWalletPage.vue';
import AppFeedPage from '@/pages/app/AppFeedPage.vue';
import AppTrendingPage from '@/pages/app/AppTrendingPage.vue';
import AppNewPage from '@/pages/app/AppNewPage.vue';
import AppFavoritesPage from '@/pages/app/AppFavoritesPage.vue';
import AppMentorsPage from '@/pages/app/AppMentorsPage.vue';
import AppBadgesPage from '@/pages/app/AppBadgesPage.vue';
import AppCertificatesPage from '@/pages/app/AppCertificatesPage.vue';
import AppDownloadsPage from '@/pages/app/AppDownloadsPage.vue';
import AppHistoryPage from '@/pages/app/AppHistoryPage.vue';
import AppHelpPage from '@/pages/app/AppHelpPage.vue';
import AppFeedbackPage from '@/pages/app/AppFeedbackPage.vue';
import AdminDashboardPage from '@/pages/admin/AdminDashboardPage.vue';
import { comingSoonRoutes } from '@/features/app-shell/navigation';
import { authGuard } from './authGuard';

/** Routes for every sidebar/launcher function that doesn't yet have a real page. */
const generatedComingSoon = comingSoonRoutes.map((r) => ({
  path: r.path,
  name: r.name,
  component: AppComingSoonPage,
  meta: { title: r.title, icon: r.icon, blurb: r.blurb }
}));

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'landing',
      component: LandingPage
    },
    {
      path: '/app',
      component: AppShellLayout,
      meta: {
        requiresAuth: true
      },
      children: [
        { path: '', name: 'app-home', component: AppHomePage },
        { path: 'documents', name: 'app-documents', component: AppDocumentsPage },
        { path: 'missions', name: 'app-missions', component: AppMissionsPage },
        { path: 'leaderboard', name: 'app-leaderboard', component: AppLeaderboardPage },
        { path: 'store', name: 'app-store', component: AppStorePage },
        { path: 'achievements', name: 'app-achievements', component: AppAchievementsPage },
        { path: 'profile', name: 'app-profile', component: AppProfilePage },
        { path: 'settings', name: 'app-settings', component: AppSettingsPage },
        // Discovery
        { path: 'explore', name: 'app-explore', component: AppExplorePage },
        { path: 'collections', name: 'app-collections', component: AppCollectionsPage },
        // Learning
        { path: 'paths', name: 'app-paths', component: AppPathsPage },
        { path: 'challenges', name: 'app-challenges', component: AppChallengesPage },
        { path: 'calendar', name: 'app-calendar', component: AppCalendarPage },
        { path: 'notes', name: 'app-notes', component: AppNotesPage },
        { path: 'flashcards', name: 'app-flashcards', component: AppFlashcardsPage },
        { path: 'quizzes', name: 'app-quizzes', component: AppQuizzesPage },
        { path: 'assistant', name: 'app-assistant', component: AppAssistantPage },
        { path: 'focus', name: 'app-focus', component: AppFocusPage },
        // Community
        { path: 'friends', name: 'app-friends', component: AppFriendsPage },
        { path: 'groups', name: 'app-groups', component: AppGroupsPage },
        { path: 'forum', name: 'app-forum', component: AppForumPage },
        { path: 'events', name: 'app-events', component: AppEventsPage },
        // Achievement
        { path: 'stats', name: 'app-stats', component: AppStatsPage },
        // Rewards
        { path: 'daily-reward', name: 'app-daily-reward', component: AppDailyRewardPage },
        { path: 'offers', name: 'app-offers', component: AppOffersPage },
        { path: 'spin', name: 'app-spin', component: AppSpinPage },
        // Personal
        { path: 'wallet', name: 'app-wallet', component: AppWalletPage },
        // Discovery (newly built)
        { path: 'feed', name: 'app-feed', component: AppFeedPage },
        { path: 'trending', name: 'app-trending', component: AppTrendingPage },
        { path: 'new', name: 'app-new', component: AppNewPage },
        { path: 'favorites', name: 'app-favorites', component: AppFavoritesPage },
        // Community
        { path: 'mentors', name: 'app-mentors', component: AppMentorsPage },
        // Achievement
        { path: 'badges', name: 'app-badges', component: AppBadgesPage },
        { path: 'certificates', name: 'app-certificates', component: AppCertificatesPage },
        // Personal
        { path: 'downloads', name: 'app-downloads', component: AppDownloadsPage },
        { path: 'history', name: 'app-history', component: AppHistoryPage },
        // Support
        { path: 'help', name: 'app-help', component: AppHelpPage },
        { path: 'feedback', name: 'app-feedback', component: AppFeedbackPage },
        ...generatedComingSoon
      ]
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboardPage,
      meta: {
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    {
      path: '/auth/login',
      name: 'auth-login',
      component: LoginPage,
      meta: {
        guestOnly: true
      }
    },
    {
      path: '/auth/register',
      name: 'auth-register',
      component: RegisterPage,
      meta: {
        guestOnly: true
      }
    },
    {
      path: '/auth/forgot-password',
      name: 'auth-forgot-password',
      component: ForgotPasswordPage
    },
    {
      path: '/auth/reset-password',
      name: 'auth-reset-password',
      component: ResetPasswordPage
    },
    {
      path: '/auth/verify-email',
      name: 'auth-verify-email',
      component: VerifyEmailPage
    },
    {
      path: '/auth/notice',
      name: 'auth-notice',
      component: AuthNoticePage
    },
    {
      path: '/auth/error',
      name: 'auth-error',
      component: AuthErrorPage
    }
  ]
});

router.beforeEach(authGuard);

export default router;
