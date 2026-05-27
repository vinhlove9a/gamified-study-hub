<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthSession } from '@/features/auth/authSession';

const router = useRouter();
const { currentUser, logout } = useAuthSession();

const displayName = computed(() => {
  if (currentUser.value?.fullName?.trim()) {
    return currentUser.value.fullName;
  }
  return currentUser.value?.email ?? 'Người dùng';
});

const handleLogout = async () => {
  logout();
  await router.push('/auth/login');
};
</script>

<template>
  <div class="app-shell">
    <aside class="app-sidebar" aria-label="Điều hướng ứng dụng">
      <div class="brand-block">
        <p class="brand-title">Gamified Study Hub</p>
        <p class="brand-subtitle">App Shell Foundation</p>
      </div>

      <nav>
        <ul class="nav-list">
          <li class="nav-item nav-item--active">Tổng quan</li>
          <li class="nav-item">Tài liệu</li>
          <li class="nav-item">Nhiệm vụ</li>
          <li class="nav-item">Điểm thưởng</li>
          <li class="nav-item">Cài đặt</li>
        </ul>
      </nav>
    </aside>

    <div class="app-main">
      <header class="app-header">
        <div>
          <p class="header-title">Khu vực đăng nhập</p>
          <p class="header-subtitle">Xin chào, {{ displayName }}</p>
        </div>
        <button type="button" class="logout-btn" @click="handleLogout">
          Đăng xuất
        </button>
      </header>

      <main class="app-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  background: linear-gradient(160deg, #fff6e8 0%, #fffaf2 55%, #f7f4ef 100%);
}

.app-sidebar {
  border-right: 1px solid #f0e2cf;
  padding: 1.25rem 1rem;
  background: #fff9f0;
}

.brand-block {
  margin-bottom: 1.5rem;
}

.brand-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #7b341e;
}

.brand-subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.82rem;
  color: #7c6f60;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.4rem;
}

.nav-item {
  padding: 0.65rem 0.75rem;
  border-radius: 0.55rem;
  color: #4b3d2e;
  font-weight: 500;
  background: transparent;
}

.nav-item--active {
  background: #ffe7c2;
  color: #7b341e;
}

.app-main {
  min-width: 0;
}

.app-header {
  display: flex;
  gap: 1rem;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #f0e2cf;
  background: rgba(255, 255, 255, 0.85);
}

.header-title {
  margin: 0;
  font-size: 0.85rem;
  color: #7c6f60;
}

.header-subtitle {
  margin: 0.2rem 0 0;
  font-size: 1rem;
  font-weight: 600;
  color: #2c2416;
}

.logout-btn {
  border: 1px solid #f0d4ab;
  background: #fff;
  color: #7b341e;
  font-weight: 600;
  border-radius: 0.55rem;
  padding: 0.5rem 0.85rem;
  cursor: pointer;
}

.app-content {
  padding: 1.25rem;
}

@media (max-width: 900px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .app-sidebar {
    border-right: none;
    border-bottom: 1px solid #f0e2cf;
    padding: 1rem;
  }

  .nav-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
