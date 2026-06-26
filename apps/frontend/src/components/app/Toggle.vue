<script setup lang="ts">
interface Props {
  modelValue: boolean;
}

const props = defineProps<Props>();
defineEmits<{ (e: 'update:modelValue', value: boolean): void }>();
</script>

<template>
  <button
    type="button"
    role="switch"
    :aria-checked="props.modelValue"
    class="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors"
    :class="
      props.modelValue
        ? 'bg-cyan-500/80 shadow-[0_0_14px_oklch(0.78_0.18_195/45%)]'
        : 'bg-white/10'
    "
    @click="$emit('update:modelValue', !props.modelValue)"
  >
    <span
      class="inline-block h-4.5 w-4.5 transform rounded-full bg-white transition-transform"
      :class="props.modelValue ? 'translate-x-[22px]' : 'translate-x-[3px]'"
      style="height: 18px; width: 18px"
    />
  </button>
</template>
