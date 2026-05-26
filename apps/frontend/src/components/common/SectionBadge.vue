<script setup lang="ts">
interface Props {
  label: string;
}

defineProps<Props>();
</script>

<template>
  <span class="section-label">
    {{ label }}
  </span>
</template>
