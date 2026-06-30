<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import AppIcon from '@/components/app/icons/AppIcon.vue';
import { allNavItems } from '@/features/app-shell/navigation';

/** A non-navigation command (toggle focus mode, fullscreen, logout, …). */
export interface PaletteAction {
  id: string;
  label: string;
  icon: string;
  hint?: string;
  run: () => void;
}

interface Props {
  open: boolean;
  actions?: PaletteAction[];
}

const props = withDefaults(defineProps<Props>(), { actions: () => [] });
const emit = defineEmits<{ (e: 'update:open', value: boolean): void }>();

const router = useRouter();
const query = ref('');
const activeIndex = ref(0);
const inputRef = ref<HTMLInputElement | null>(null);

/** Strip Vietnamese diacritics so "bang" matches "bảng". */
function normalize(s: string): string {
  return s
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/đ/g, 'd')
    .toLowerCase();
}

interface Row {
  kind: 'nav' | 'action';
  label: string;
  icon: string;
  hint?: string;
  to?: string;
  run?: () => void;
}

const allRows = computed<Row[]>(() => [
  ...allNavItems.map((i) => ({ kind: 'nav' as const, label: i.label, icon: i.icon, to: i.to, hint: i.blurb })),
  ...props.actions.map((a) => ({ kind: 'action' as const, label: a.label, icon: a.icon, hint: a.hint, run: a.run }))
]);

const filtered = computed<Row[]>(() => {
  const q = normalize(query.value.trim());
  if (!q) return allRows.value;
  return allRows.value.filter((r) => {
    const item = allNavItems.find((i) => i.to === r.to);
    const hay = normalize(`${r.label} ${item?.keywords ?? ''} ${r.hint ?? ''}`);
    return hay.includes(q);
  });
});

const navRows = computed(() => filtered.value.filter((r) => r.kind === 'nav'));
const actionRows = computed(() => filtered.value.filter((r) => r.kind === 'action'));

function close() {
  emit('update:open', false);
}

function select(row: Row) {
  if (row.kind === 'nav' && row.to) {
    void router.push(row.to);
  } else if (row.kind === 'action' && row.run) {
    row.run();
  }
  close();
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') {
    e.preventDefault();
    activeIndex.value = (activeIndex.value + 1) % Math.max(filtered.value.length, 1);
  } else if (e.key === 'ArrowUp') {
    e.preventDefault();
    activeIndex.value = (activeIndex.value - 1 + filtered.value.length) % Math.max(filtered.value.length, 1);
  } else if (e.key === 'Enter') {
    e.preventDefault();
    const row = filtered.value[activeIndex.value];
    if (row) select(row);
  } else if (e.key === 'Escape') {
    e.preventDefault();
    close();
  }
}

watch(query, () => {
  activeIndex.value = 0;
});

watch(
  () => props.open,
  (open) => {
    if (open) {
      query.value = '';
      activeIndex.value = 0;
      void nextTick(() => inputRef.value?.focus());
    }
  }
);

// Global ⌘K / Ctrl+K to open the palette from anywhere.
function onGlobalKey(e: KeyboardEvent) {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault();
    emit('update:open', !props.open);
  }
}
onMounted(() => window.addEventListener('keydown', onGlobalKey));
onBeforeUnmount(() => window.removeEventListener('keydown', onGlobalKey));

/** Flat index of a row within `filtered` (nav section precedes actions). */
function rowIndex(row: Row): number {
  return filtered.value.indexOf(row);
}
</script>

<template>
  <Teleport to="body">
      <div
        v-if="open"
        class="cmdk-overlay fixed inset-0 z-[100] flex items-start justify-center px-4 pt-[12vh]"
        @click.self="close"
      >
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="close" />
        <div
          class="glass-panel cmdk-panel relative z-10 w-full max-w-xl overflow-hidden rounded-sm"
          role="dialog"
          aria-modal="true"
          aria-label="Bảng lệnh"
        >
          <!-- Search row -->
          <div class="flex items-center gap-3 border-b border-white/[0.08] px-4 py-3">
            <AppIcon name="search" class="h-5 w-5 text-muted-foreground" />
            <input
              ref="inputRef"
              v-model="query"
              type="text"
              placeholder="Tìm chức năng, trang hoặc thao tác…"
              class="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground/70 focus:outline-none"
              @keydown="onKeydown"
            />
            <kbd class="rounded-sm border border-white/10 bg-white/[0.04] px-1.5 py-0.5 text-[0.65rem] font-medium text-muted-foreground">
              ESC
            </kbd>
          </div>

          <!-- Results -->
          <div class="cosmic-scroll max-h-[52vh] overflow-y-auto p-2">
            <p v-if="filtered.length === 0" class="px-3 py-8 text-center text-sm text-muted-foreground">
              Không có kết quả cho “{{ query }}”.
            </p>

            <template v-if="navRows.length">
              <p class="px-2 pb-1 pt-1.5 text-[0.65rem] font-semibold uppercase tracking-wider text-muted-foreground/70">
                Điều hướng
              </p>
              <button
                v-for="row in navRows"
                :key="`nav-${row.to}`"
                type="button"
                class="flex w-full items-center gap-3 rounded-sm px-3 py-2 text-left text-sm transition-colors"
                :class="rowIndex(row) === activeIndex ? 'bg-cyan-500/15 text-cyan-100' : 'text-muted-foreground hover:bg-white/[0.05] hover:text-foreground'"
                @click="select(row)"
                @mousemove="activeIndex = rowIndex(row)"
              >
                <span
                  class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm"
                  :class="rowIndex(row) === activeIndex ? 'bg-cyan-400/20 text-cyan-200' : 'bg-white/[0.05] text-muted-foreground'"
                >
                  <AppIcon :name="row.icon" class="h-4 w-4" />
                </span>
                <span class="min-w-0 flex-1">
                  <span class="block truncate font-medium text-foreground">{{ row.label }}</span>
                  <span v-if="row.hint" class="block truncate text-xs text-muted-foreground">{{ row.hint }}</span>
                </span>
                <AppIcon name="arrow-right" class="h-4 w-4 shrink-0 opacity-50" />
              </button>
            </template>

            <template v-if="actionRows.length">
              <p class="px-2 pb-1 pt-2.5 text-[0.65rem] font-semibold uppercase tracking-wider text-muted-foreground/70">
                Hành động
              </p>
              <button
                v-for="row in actionRows"
                :key="`act-${row.label}`"
                type="button"
                class="flex w-full items-center gap-3 rounded-sm px-3 py-2 text-left text-sm transition-colors"
                :class="rowIndex(row) === activeIndex ? 'bg-fuchsia-500/15 text-fuchsia-100' : 'text-muted-foreground hover:bg-white/[0.05] hover:text-foreground'"
                @click="select(row)"
                @mousemove="activeIndex = rowIndex(row)"
              >
                <span
                  class="flex h-8 w-8 shrink-0 items-center justify-center rounded-sm"
                  :class="rowIndex(row) === activeIndex ? 'bg-fuchsia-400/20 text-fuchsia-200' : 'bg-white/[0.05] text-muted-foreground'"
                >
                  <AppIcon :name="row.icon" class="h-4 w-4" />
                </span>
                <span class="min-w-0 flex-1 font-medium text-foreground">{{ row.label }}</span>
                <span v-if="row.hint" class="shrink-0 text-xs text-muted-foreground">{{ row.hint }}</span>
              </button>
            </template>
          </div>

          <!-- Footer hint -->
          <div class="flex items-center gap-4 border-t border-white/[0.08] px-4 py-2 text-[0.7rem] text-muted-foreground">
            <span class="flex items-center gap-1"><kbd class="rounded border border-white/10 bg-white/[0.04] px-1">↑</kbd><kbd class="rounded border border-white/10 bg-white/[0.04] px-1">↓</kbd> di chuyển</span>
            <span class="flex items-center gap-1"><kbd class="rounded border border-white/10 bg-white/[0.04] px-1">↵</kbd> chọn</span>
            <span class="ml-auto flex items-center gap-1">{{ filtered.length }} kết quả</span>
          </div>
        </div>
      </div>
  </Teleport>
</template>

<style scoped>
/* Entrance animation on insertion (v-if) rather than Vue's <transition>
 * enter/leave classes, which can stick when nested in <Teleport>.
 * Transform-only (no opacity) so the palette is fully visible even if the
 * animation timeline never advances — content visibility never depends on it. */
.cmdk-panel {
  animation: cmdk-pop 0.2s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes cmdk-pop {
  from {
    transform: translateY(-10px) scale(0.97);
  }
  to {
    transform: none;
  }
}
</style>
