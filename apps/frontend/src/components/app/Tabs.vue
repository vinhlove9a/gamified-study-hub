<script setup lang="ts">
interface Tab {
  id: string;
  label: string;
  badge?: string | number;
}

interface Props {
  tabs: Tab[];
  modelValue: string;
}

const props = defineProps<Props>();
defineEmits<{ (e: 'update:modelValue', id: string): void }>();
</script>

<template>
  <div class="inline-flex flex-wrap gap-1 rounded-sm border border-white/[0.06] bg-white/[0.03] p-1">
    <button
      v-for="tab in props.tabs"
      :key="tab.id"
      type="button"
      class="inline-flex items-center gap-1.5 rounded-sm px-3.5 py-1.5 text-sm font-medium transition-colors"
      :class="
        props.modelValue === tab.id
          ? 'bg-cyan-500/15 text-cyan-200'
          : 'text-muted-foreground hover:bg-white/[0.04] hover:text-foreground'
      "
      @click="$emit('update:modelValue', tab.id)"
    >
      {{ tab.label }}
      <span
        v-if="tab.badge !== undefined"
        class="rounded-sm bg-white/10 px-1.5 py-0.5 text-[0.65rem] font-semibold"
      >
        {{ tab.badge }}
      </span>
    </button>
  </div>
</template>
