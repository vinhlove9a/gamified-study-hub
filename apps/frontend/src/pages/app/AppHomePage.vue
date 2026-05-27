<script setup lang="ts">
import { useAuthSession } from '@/features/auth/authSession';
import AppShellLayout from '@/layouts/AppShellLayout.vue';

const { currentUser } = useAuthSession();
</script>

<template>
  <AppShellLayout>
    <section class="app-home">
      <header class="home-header">
        <p class="eyebrow">Protected /app Route</p>
        <h1 class="title">Không gian ứng dụng đã sẵn sàng</h1>
        <p class="subtitle">
          Đây là nền tảng app shell đầu tiên để kiểm thử route guard và luồng phiên đăng nhập.
        </p>
      </header>

      <article class="user-card">
        <h2>Thông tin phiên hiện tại</h2>
        <p><strong>Họ tên:</strong> {{ currentUser?.fullName || 'Chưa có' }}</p>
        <p><strong>Email:</strong> {{ currentUser?.email || 'Chưa có' }}</p>
      </article>

      <section class="module-grid" aria-label="Mô-đun dự kiến">
        <article class="module-card">
          <h3>Smart Search tài liệu</h3>
          <p>Tìm kiếm học liệu theo ngữ cảnh và cấp độ học tập.</p>
        </article>
        <article class="module-card">
          <h3>Nhiệm vụ học tập</h3>
          <p>Theo dõi nhiệm vụ theo tuần và trạng thái hoàn thành.</p>
        </article>
        <article class="module-card">
          <h3>Coin/VIP unlock</h3>
          <p>Hiển thị cơ chế điểm thưởng và mở khóa quyền truy cập.</p>
        </article>
        <article class="module-card">
          <h3>Workspace trung tâm/CLB</h3>
          <p>Tổ chức nhóm học tập theo trung tâm, CLB hoặc cộng đồng.</p>
        </article>
      </section>
    </section>
  </AppShellLayout>
</template>

<style scoped>
.app-home {
  display: grid;
  gap: 1rem;
}

.home-header {
  background: #fff;
  border: 1px solid #f0e2cf;
  border-radius: 0.8rem;
  padding: 1rem;
}

.eyebrow {
  margin: 0;
  color: #7b341e;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.title {
  margin: 0.3rem 0 0;
  font-size: 1.4rem;
  color: #2c2416;
}

.subtitle {
  margin: 0.4rem 0 0;
  color: #6d5c49;
}

.user-card {
  background: #fffefb;
  border: 1px solid #f0e2cf;
  border-radius: 0.8rem;
  padding: 1rem;
}

.user-card h2 {
  margin: 0 0 0.5rem;
  font-size: 1rem;
}

.user-card p {
  margin: 0.2rem 0;
}

.module-grid {
  display: grid;
  gap: 0.85rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.module-card {
  background: #fff;
  border: 1px solid #f0e2cf;
  border-radius: 0.8rem;
  padding: 0.9rem;
}

.module-card h3 {
  margin: 0 0 0.4rem;
  font-size: 1rem;
}

.module-card p {
  margin: 0;
  color: #6d5c49;
}

@media (max-width: 900px) {
  .module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
