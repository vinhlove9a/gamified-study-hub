<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import { adminNavGroups, type AdminNavGroup } from '@/features/admin/navigation';

interface Props {
  collapsed?: boolean;
}
const props = withDefaults(defineProps<Props>(), { collapsed: false });
defineEmits<{ (e: 'navigate'): void; (e: 'toggle-collapse'): void }>();

const route = useRoute();
const query = ref('');

const accentDot: Record<string, string> = {
  cyan: 'bg-cyan-400',
  magenta: 'bg-fuchsia-400',
  green: 'bg-emerald-400',
  violet: 'bg-violet-400',
  amber: 'bg-amber-400'
};

const visibleGroups = computed<AdminNavGroup[]>(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return adminNavGroups;
  return adminNavGroups
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

function isActive(to: string): boolean {
  if (to === '/admin') return route.path === '/admin';
  return route.path === to || route.path.startsWith(`${to}/`);
}
</script>

<template>
  <nav class="glass-panel flex h-full flex-col border-r border-white/[0.06] bg-background/40">
    <!-- Brand -->
    <div class="flex items-center gap-3 px-4 py-4" :class="props.collapsed ? 'justify-center' : ''">
      <span
        class="relative flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-400 to-fuchsia-500 text-white shadow-[0_0_18px_oklch(0.78_0.18_195/45%)]"
      >
        <AppIcon name="shield" class="h-5 w-5" />
      </span>
      <div v-if="!props.collapsed" class="min-w-0 leading-tight">
        <p class="truncate text-sm font-bold">
          <span class="glow-cyan text-cyan-100">DEEP</span><span class="text-fuchsia-200">WORKSPACE</span>
        </p>
        <p class="truncate text-[10px] font-mono uppercase tracking-widest text-muted-foreground">Mission Control</p>
      </div>
    </div>

    <!-- Quick filter -->
    <div v-if="!props.collapsed" class="px-3">
      <label class="relative flex items-center">
        <AppIcon name="search" class="pointer-events-none absolute left-2.5 h-4 w-4 text-muted-foreground" />
        <input
          v-model="query"
          type="search"
          placeholder="Tìm chức năng…"
          class="w-full rounded-xl border border-white/[0.08] bg-white/[0.03] py-2 pl-9 pr-3 text-sm text-foreground placeholder:text-muted-foreground/70 focus:border-cyan-500/40 focus:outline-none"
        />
      </label>
    </div>

    <!-- Nav groups -->
    <div class="cosmic-scroll mt-3 flex-1 overflow-y-auto px-3 pb-3">
      <div v-for="group in visibleGroups" :key="group.label" class="mb-4">
        <p
          v-if="!props.collapsed"
          class="px-2 pb-1.5 text-[10px] font-semibold uppercase tracking-widest text-muted-foreground/70"
        >
          {{ group.label }}
        </p>
        <ul class="space-y-0.5">
          <li v-for="item in group.items" :key="item.to">
            <RouterLink
              :to="item.to"
              :title="props.collapsed ? item.label : undefined"
              class="group relative flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition-colors"
              :class="[
                isActive(item.to)
                  ? 'bg-white/[0.06] text-foreground'
                  : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground',
                props.collapsed ? 'justify-center' : ''
              ]"
              @click="$emit('navigate')"
            >
              <span
                v-if="isActive(item.to)"
                class="absolute left-0 top-1/2 h-5 w-0.5 -translate-y-1/2 rounded-full bg-cyan-400 shadow-[0_0_8px_oklch(0.78_0.18_195/80%)]"
              />
              <AppIcon
                :name="item.icon"
                class="h-5 w-5 shrink-0 transition-transform group-hover:scale-110"
                :class="isActive(item.to) ? 'text-cyan-300 drop-shadow-[0_0_6px_oklch(0.78_0.18_195/70%)]' : ''"
              />
              <span v-if="!props.collapsed" class="min-w-0 flex-1 truncate">{{ item.label }}</span>
              <span
                v-if="!props.collapsed && item.badge"
                class="rounded-full px-1.5 py-0.5 text-[10px] font-semibold text-white/90"
                :class="[accentDot[item.accent ?? 'cyan'], 'bg-opacity-20']"
                :style="{ background: 'oklch(0.78 0.18 195 / 18%)' }"
              >
                {{ item.badge }}
              </span>
            </RouterLink>
          </li>
        </ul>
      </div>
    </div>

    <!-- Footer: system health + collapse -->
    <div class="border-t border-white/[0.06] px-3 py-3">
      <div v-if="!props.collapsed" class="gradient-border mb-2 rounded-xl p-3">
        <div class="flex items-center justify-between">
          <span class="flex items-center gap-2 text-xs font-medium text-foreground">
            <span class="h-2 w-2 rounded-full bg-emerald-400 pulse-glow-green" />
            Hệ thống ổn định
          </span>
          <span class="font-mono text-[10px] text-muted-foreground">99.98%</span>
        </div>
        <p class="mt-1 font-mono text-[10px] text-muted-foreground">API 42ms · DB 11ms · CDN OK</p>
      </div>
      <button
        type="button"
        class="flex w-full items-center justify-center gap-2 rounded-xl border border-white/[0.06] bg-white/[0.02] py-2 text-xs text-muted-foreground transition-colors hover:bg-white/[0.05] hover:text-foreground"
        @click="$emit('toggle-collapse')"
      >
        <AppIcon :name="props.collapsed ? 'chevron-right' : 'chevron-left'" class="h-4 w-4" />
        <span v-if="!props.collapsed">Thu gọn</span>
      </button>
    </div>
  </nav>
</template>
