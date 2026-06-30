<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import { navGroups, bottomNavItems, type AppNavGroup } from '@/features/app-shell/navigation';

interface Props {
  collapsed?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false
});

defineEmits<{
  (e: 'navigate'): void;
  (e: 'toggle-collapse'): void;
}>();

const route = useRoute();

const query = ref('');

/** Filter groups + items by the live search box (label match, accent-insensitive-ish). */
const visibleGroups = computed<AppNavGroup[]>(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return navGroups;
  return navGroups
    .map((g) => ({
      ...g,
      items: g.items.filter(
        (it) =>
          it.label.toLowerCase().includes(q) ||
          (it.keywords ?? '').toLowerCase().includes(q) ||
          g.label.toLowerCase().includes(q)
      )
    }))
    .filter((g) => g.items.length > 0);
});

const totalFunctions = computed(
  () => navGroups.reduce((n, g) => n + g.items.length, 0) + bottomNavItems.length
);

function isActive(to: string): boolean {
  if (to === '/app') return route.path === '/app';
  return route.path === to || route.path.startsWith(`${to}/`);
}

const level = { current: 7, xp: 1240, next: 2000 };
const xpPct = computed(() => Math.round((level.xp / level.next) * 100));
</script>

<template>
  <div class="glass-panel flex h-full flex-col rounded-none border-y-0 border-l-0">
    <!-- Brand -->
    <div
      class="flex h-16 items-center gap-2.5 border-b border-white/[0.06] px-4"
      :class="props.collapsed ? 'justify-center px-0' : ''"
    >
      <img
        src="/images/gmf-logo.png"
        alt="Gamified Study Hub"
        class="h-9 w-9 shrink-0 rounded-sm object-cover"
      />
      <div v-if="!props.collapsed" class="min-w-0 leading-tight">
        <p class="cosmic-gradient-text truncate text-sm font-bold">Study Hub</p>
        <p class="truncate text-[0.7rem] text-muted-foreground">{{ totalFunctions }} tính năng · vũ trụ tri thức</p>
      </div>
    </div>

    <!-- Quick filter -->
    <div v-if="!props.collapsed" class="px-3 pt-3">
      <label class="relative flex items-center">
        <AppIcon name="search" class="pointer-events-none absolute left-2.5 h-4 w-4 text-muted-foreground" />
        <input
          v-model="query"
          type="search"
          placeholder="Lọc chức năng…"
          class="h-9 w-full rounded-sm border border-white/[0.08] bg-white/[0.03] pl-8 pr-3 text-sm text-foreground placeholder:text-muted-foreground/70 focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-400/20"
        />
      </label>
    </div>

    <!-- Nav -->
    <nav class="cosmic-scroll flex-1 overflow-y-auto px-3 py-3" aria-label="Điều hướng chính">
      <p
        v-if="!props.collapsed && visibleGroups.length === 0"
        class="px-2.5 py-6 text-center text-xs text-muted-foreground"
      >
        Không tìm thấy chức năng phù hợp.
      </p>

      <div v-for="group in visibleGroups" :key="group.label" class="mb-4 last:mb-0">
        <p
          v-if="!props.collapsed"
          class="px-2.5 pb-1.5 text-[0.66rem] font-semibold uppercase tracking-wider text-muted-foreground/70"
        >
          {{ group.label }}
        </p>
        <div v-else class="mx-auto mb-1.5 h-px w-7 bg-white/[0.07]" :title="group.label" />
        <ul class="space-y-0.5">
          <li v-for="item in group.items" :key="item.to">
            <RouterLink
              :to="item.to"
              :title="props.collapsed ? item.label : undefined"
              class="group relative flex items-center gap-3 rounded-sm px-2.5 py-2 text-sm font-medium transition-colors"
              :class="[
                isActive(item.to)
                  ? 'bg-cyan-500/10 text-cyan-200'
                  : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground',
                props.collapsed ? 'justify-center' : ''
              ]"
              @click="$emit('navigate')"
            >
              <span
                v-if="isActive(item.to)"
                class="absolute left-0 top-1/2 h-5 w-0.5 -translate-y-1/2 rounded-sm bg-cyan-400"
              />
              <AppIcon
                :name="item.icon"
                class="h-5 w-5 shrink-0 transition-transform group-hover:scale-110"
                :class="isActive(item.to) ? 'drop-' : ''"
              />
              <template v-if="!props.collapsed">
                <span class="flex-1 truncate text-left">{{ item.label }}</span>
                <span
                  v-if="item.badge"
                  class="shrink-0 rounded-sm px-1.5 py-0.5 text-[0.65rem] font-semibold"
                  :class="isActive(item.to) ? 'bg-cyan-400 text-slate-900' : 'bg-white/10 text-muted-foreground'"
                >
                  {{ item.badge }}
                </span>
              </template>
              <span
                v-else-if="item.badge"
                class="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-sm bg-cyan-400"
              />
            </RouterLink>
          </li>
        </ul>
      </div>
    </nav>

    <!-- XP mini -->
    <div v-if="!props.collapsed" class="px-4 pb-2">
      <div class="flex items-center justify-between text-[0.7rem] text-muted-foreground">
        <span>Cấp {{ level.current }}</span>
        <span class="tabular-nums">{{ level.xp }}/{{ level.next }} XP</span>
      </div>
      <div class="mt-1 h-1.5 w-full overflow-hidden rounded-sm bg-white/[0.06]">
        <div
          class="h-full rounded-sm from-cyan-400 to-fuchsia-500"
          :style="{ width: `${xpPct}%` }"
        />
      </div>
    </div>

    <!-- VIP promo -->
    <div v-if="!props.collapsed" class="px-3 pb-3 pt-2">
      <div class="gradient-border rounded-sm p-4">
        <span class="flex h-8 w-8 items-center justify-center rounded-sm bg-amber-400/15 text-amber-300">
          <AppIcon name="crown" class="h-5 w-5" />
        </span>
        <p class="mt-2.5 text-sm font-semibold text-foreground">Nâng cấp VIP</p>
        <p class="mt-0.5 text-xs text-muted-foreground">Mở khoá tài liệu cao cấp &amp; x2 coin.</p>
        <button type="button" class="cosmic-btn mt-3 w-full rounded-sm py-1.5 text-xs">Khám phá ngay</button>
      </div>
    </div>

    <!-- Bottom -->
    <div class="border-t border-white/[0.06] px-3 py-3">
      <ul class="space-y-0.5">
        <li v-for="item in bottomNavItems" :key="item.to">
          <RouterLink
            :to="item.to"
            :title="props.collapsed ? item.label : undefined"
            class="flex items-center gap-3 rounded-sm px-2.5 py-2 text-sm font-medium transition-colors"
            :class="[
              isActive(item.to) ? 'bg-cyan-500/10 text-cyan-200' : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground',
              props.collapsed ? 'justify-center' : ''
            ]"
            @click="$emit('navigate')"
          >
            <AppIcon :name="item.icon" class="h-5 w-5 shrink-0" />
            <span v-if="!props.collapsed" class="truncate">{{ item.label }}</span>
          </RouterLink>
        </li>
      </ul>

      <button
        type="button"
        class="mt-1 hidden w-full items-center gap-3 rounded-sm px-2.5 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-white/[0.04] hover:text-foreground lg:flex"
        :class="props.collapsed ? 'justify-center' : ''"
        :title="props.collapsed ? 'Mở rộng' : 'Thu gọn'"
        @click="$emit('toggle-collapse')"
      >
        <AppIcon :name="props.collapsed ? 'chevron-right' : 'chevron-left'" class="h-5 w-5 shrink-0" />
        <span v-if="!props.collapsed" class="truncate">Thu gọn</span>
      </button>
    </div>
  </div>
</template>
