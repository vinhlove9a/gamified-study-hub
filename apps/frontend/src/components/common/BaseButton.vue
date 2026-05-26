<script setup lang="ts">
import { computed } from 'vue';

type ButtonSize = 'md' | 'lg';
type ButtonVariant = 'primary' | 'outline';

interface Props {
  href?: string;
  size?: ButtonSize;
  variant?: ButtonVariant;
  ariaLabel?: string;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  href: '#',
  size: 'md',
  variant: 'primary',
  ariaLabel: undefined,
  disabled: false
});

const isLink = computed(() => Boolean(props.href) && !props.disabled);
const tagName = computed(() => (isLink.value ? 'a' : 'button'));

const sizeClasses = computed(() => (props.size === 'lg' ? 'min-h-12 px-7 py-3 text-body' : 'min-h-10 px-5 py-2 text-caption'));

const variantClasses = computed(() => {
  if (props.variant === 'outline') {
    return 'border border-gsh-border bg-white text-gsh-text hover:border-black/30';
  }

  return 'bg-gsh-accent text-white hover:bg-gsh-accent-hover';
});

const disabledClasses = computed(() => (props.disabled ? 'pointer-events-none cursor-not-allowed opacity-40' : ''));

const linkAttributes = computed(() => {
  if (isLink.value) {
    return {
      href: props.href,
      'aria-label': props.ariaLabel
    };
  }

  return {
    type: 'button',
    'aria-label': props.ariaLabel,
    'aria-disabled': 'true',
    disabled: true
  };
});
</script>

<template>
  <component
    :is="tagName"
    v-bind="linkAttributes"
    :class="[
      'inline-flex items-center justify-center whitespace-nowrap rounded-full border border-transparent font-display font-semibold tracking-tight transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gsh-accent/30 focus-visible:ring-offset-2',
      variantClasses,
      sizeClasses,
      disabledClasses
    ]"
  >
    <slot />
  </component>
</template>
