<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuthSession } from '@/features/auth/authSession';

const { bootstrapSession } = useAuthSession();

onMounted(() => {
  void bootstrapSession();
});
</script>

<template>
  <RouterView />
</template>
