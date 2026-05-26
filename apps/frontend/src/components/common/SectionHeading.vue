<script setup lang="ts">
import { computed } from 'vue';
import SectionBadge from '@/components/common/SectionBadge.vue';

type HeadingAlign = 'left' | 'center';

interface Props {
  badge?: string;
  title: string;
  description?: string;
  align?: HeadingAlign;
}

const props = withDefaults(defineProps<Props>(), {
  badge: undefined,
  description: undefined,
  align: 'center'
});

const wrapperClass = computed(() => (props.align === 'center' ? 'mx-auto text-center' : 'text-left'));
const textWidthClass = computed(() => (props.align === 'center' ? 'mx-auto max-w-3xl' : 'max-w-3xl'));
</script>

<template>
  <div :class="wrapperClass">
    <SectionBadge v-if="badge" :label="badge" />
    <h2 class="mt-3 text-display-lg text-gsh-text">{{ title }}</h2>
    <p v-if="description" :class="['mt-5 text-body-lg text-gsh-secondary', textWidthClass]">
      {{ description }}
    </p>
  </div>
</template>
