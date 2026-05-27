# Frontend Auth UI - Login + Register + Forgot Password + Reset Password + Verify Email + Auth Notice + Auth Error Pages

## Source template
- External template path: `/Users/vinhlove9a/Desktop/login-template`
- Files inspected: `html.html`, `css.css`, `config.config`
- Template stack: static HTML + inline CSS animations + Tailwind utility classes via CDN

## Implemented pages
- `apps/frontend/src/pages/auth/LoginPage.vue` (`/auth/login`)
- `apps/frontend/src/pages/auth/RegisterPage.vue` (`/auth/register`)
- `apps/frontend/src/pages/auth/ForgotPasswordPage.vue` (`/auth/forgot-password`)
- `apps/frontend/src/pages/auth/ResetPasswordPage.vue` (`/auth/reset-password`)
- `apps/frontend/src/pages/auth/VerifyEmailPage.vue` (`/auth/verify-email`)
- `apps/frontend/src/pages/auth/AuthNoticePage.vue` (`/auth/notice`)
- `apps/frontend/src/pages/auth/AuthErrorPage.vue` (`/auth/error`)
- Existing landing route remains: `/`

## Routing changes
- Added router file: `apps/frontend/src/router/index.ts`
- Updated app shell: `apps/frontend/src/app/App.vue` now renders `RouterView`
- Updated entrypoint: `apps/frontend/src/app/main.ts` to register router

## Routing updates
- Router file: `apps/frontend/src/router/index.ts`
- Current routes:
  - `/`
  - `/auth/login`
  - `/auth/register`
  - `/auth/forgot-password`
  - `/auth/reset-password`
  - `/auth/verify-email`
  - `/auth/notice`
  - `/auth/error`

## Auth error page route + query states
- Route: `/auth/error`
- Supported query types:
  - `/auth/error`
  - `/auth/error?type=expired-link`
  - `/auth/error?type=invalid-token`
  - `/auth/error?type=account-locked`
  - `/auth/error?type=verification-failed`
  - `/auth/error?type=reset-failed`

## Auth error UI-only behavior
- Uses frontend-only computed state from route query `type`.
- No backend API calls, no token storage, no real auth logic.
- Primary action points to login.
- Secondary action behavior:
  - Local demo action for generic/expired/invalid/account-locked states.
  - Link to `/auth/verify-email` for `verification-failed`.
  - Link to `/auth/forgot-password` for `reset-failed`.
- Demo messages:
  - `Yêu cầu khôi phục sẽ được kết nối API ở bước tiếp theo.`
  - `Thông tin hỗ trợ sẽ được cấu hình ở bước tiếp theo.`

## Auth error unique visual behavior
- Keeps the same auth background/card/button/link/focus style family as other auth pages.
- Adds a calmer recovery/error panel with CSS-only warning ring + state icon (not color-only):
  - `!` for warning states
  - `🔒` for locked-account state
  - `⛓` for invalid/expired style states
- Includes recovery steps:
  - `Kiểm tra lại liên kết`
  - `Yêu cầu liên kết mới`
  - `Quay lại đăng nhập`

## Auth notice page route + query states
- Route: `/auth/notice`
- Supported query types:
  - `/auth/notice`
  - `/auth/notice?type=registration-success`
  - `/auth/notice?type=reset-link-sent`
  - `/auth/notice?type=password-reset-success`
  - `/auth/notice?type=email-verified`

## Auth notice UI-only behavior
- Uses frontend-only computed state from route query `type`.
- No backend API calls, no token storage, no real auth logic.
- Primary action points to login.
- For variants with resend action (`registration-success`, `reset-link-sent`), a local demo resend button is shown.
- Demo resend message:
  - `Yêu cầu gửi lại sẽ được kết nối API ở bước tiếp theo.`

## Auth notice unique visual behavior
- Keeps the same auth background/card/button/link/focus style family as existing auth pages.
- Adds a dedicated success/info panel with CSS-only confirmation ring + state badge/icon:
  - info/email symbol for default + reset-link-sent
  - check/success symbol for registration-success, password-reset-success, email-verified
- Includes timeline-style next steps:
  - `Kiểm tra thông báo`
  - `Hoàn tất bước xác thực`
  - `Quay lại đăng nhập`

## Verify email page UI scope
- Title: `Xác thực email của bạn`
- Subtitle: `Chúng tôi đã gửi một liên kết xác thực đến email đăng ký. Vui lòng kiểm tra hộp thư để hoàn tất quá trình tạo tài khoản.`
- Primary action: `Gửi lại email xác thực`
- Secondary navigation: `Quay lại đăng nhập`
- Optional helper + note included below CTA.

## Verify email resend behavior (UI-only)
- Uses local reactive state only (`loading`, `formMessage`, `resendCount`).
- No email field required on this page.
- No API calls, no token storage, no auto navigation.
- Demo resend message:
  - `Email xác thực sẽ được gửi lại ở bước tích hợp API.`
- Local-only resend note after at least one resend:
  - `Yêu cầu gửi lại đã được ghi nhận trong phiên demo này.`

## Verify email unique visual behavior
- Keeps the same auth background/card/button/link/focus style family as other auth pages.
- Adds decorative inbox verification panel with:
  - CSS-only mailbox/envelope visual
  - verification check badge
  - soft delivery pulse
  - verification steps:
    - `Kiểm tra hộp thư`
    - `Mở liên kết xác thực`
    - `Quay lại đăng nhập`

## Reset password page UI scope
- Title: `Đặt lại mật khẩu`
- Subtitle: `Tạo mật khẩu mới để tiếp tục sử dụng Gamified Study Hub một cách an toàn.`
- Fields:
  - `Mật khẩu mới`
  - `Xác nhận mật khẩu mới`
- Primary action: `Đặt lại mật khẩu`
- Back navigation: `Quay lại đăng nhập`
- Optional helper note included below CTA.

## Reset password validation (UI-only)
- Uses local reactive state only (`password`, `confirmPassword`, `loading`, `formMessage`, `errors`).
- Validation messages:
  - `Mật khẩu mới không được để trống`
  - `Mật khẩu nên có ít nhất 8 ký tự`
  - `Xác nhận mật khẩu không được để trống`
  - `Xác nhận mật khẩu không khớp`
- Demo submit success message:
  - `Chức năng đặt lại mật khẩu sẽ được kết nối API ở bước tiếp theo.`

## Reset password strength/checklist UX
- UI-only password strength hint states:
  - `Yếu`
  - `Trung bình`
  - `Mạnh`
- UI-only requirement checklist:
  - `Tối thiểu 8 ký tự`
  - `Có chữ cái`
  - `Có số`
  - `Hai mật khẩu khớp nhau`

## Reset password unique visual behavior
- Keeps the same auth background/card/input/CTA/focus style family as other auth pages.
- Adds a decorative security panel with CSS-only lock + soft pulse/glow.
- Includes reset flow steps:
  - `Kiểm tra liên kết`
  - `Tạo mật khẩu mới`
  - `Đăng nhập an toàn`

## Forgot password page UI scope
- Title: `Quên mật khẩu?`
- Subtitle: `Nhập email tài khoản của bạn. Chúng tôi sẽ gửi hướng dẫn đặt lại mật khẩu để bạn tiếp tục sử dụng hệ thống.`
- Field:
  - `Email`
- Primary action: `Gửi liên kết đặt lại mật khẩu`
- Back navigation: `Quay lại đăng nhập`
- Optional helper note included below CTA.

## Forgot password validation (UI-only)
- Uses local reactive state only (`email`, `loading`, `formMessage`, `errors`).
- Validation messages:
  - `Email không được để trống`
  - `Email không đúng định dạng`
- Demo submit success message:
  - `Liên kết đặt lại mật khẩu sẽ được gửi qua email ở bước tích hợp API.`

## Forgot password unique visual behavior
- Keeps the same auth background/card/input/CTA/focus style family as login/register.
- Adds a decorative email recovery panel with CSS-only envelope + pulse indicator.
- Includes recovery steps:
  - `Xác nhận email`
  - `Nhận liên kết bảo mật`
  - `Đặt lại mật khẩu mới`

## Register page UI scope
- Title: `Tạo tài khoản mới`
- Subtitle: `Bắt đầu xây dựng cổng tài liệu học tập cho cộng đồng của bạn.`
- Fields:
  - `Họ và tên`
  - `Email`
  - `Mật khẩu`
  - `Xác nhận mật khẩu`
  - Terms checkbox: `Tôi đồng ý với Điều khoản sử dụng và Chính sách quyền riêng tư.`
- Primary action: `Tạo tài khoản`
- Secondary navigation: `Đã có tài khoản? Đăng nhập`
- Helper note included after submit button.

## Register validation (UI-only)
- Uses local reactive state only (no API calls).
- Validation messages:
  - `Họ và tên không được để trống`
  - `Email không được để trống`
  - `Email không đúng định dạng`
  - `Mật khẩu không được để trống`
  - `Mật khẩu nên có ít nhất 8 ký tự`
  - `Xác nhận mật khẩu không khớp`
  - `Bạn cần đồng ý điều khoản trước khi tạo tài khoản`
- Demo submit success message:
  - `Chức năng đăng ký sẽ được kết nối API ở bước tiếp theo.`

## Register unique visual behavior
- Keeps the same auth background/card/input/CTA quality as login.
- Adds a register-specific onboarding checklist panel (decorative, CSS-only):
  - `Tạo tài khoản`
  - `Thiết lập workspace`
  - `Tải tài liệu đầu tiên`
  - `Mời học viên tham gia`
- Adds UI-only password strength hint states:
  - `Yếu`
  - `Trung bình`
  - `Mạnh`

## Assets copied
- No external image/icon assets were copied from the template.
- Login UI uses CSS gradients and inline SVG only.

## Dependencies
- Added: `vue-router` (frontend routing only)
- No backend dependencies added.

## Current limitations
- Frontend UI only.
- No API integration.
- No token storage.
- No real authentication or JWT handling.
- Auth UI pages are complete; backend integration/auth business flow is pending.
- Social login buttons are presentation-only.

## Integration contract reference
- Backend API contract for upcoming auth integration:
  - `docs/architecture/auth-api-contract.md`

## Recommended next steps
1. Integrate authentication API and session strategy.
2. Wire real resend/verify/reset flows and backend error handling.
