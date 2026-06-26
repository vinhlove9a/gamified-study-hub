<script setup lang="ts">
interface Props {
  variant?: 'light' | 'dark';
  size?: 'sm' | 'md' | 'lg';
}

withDefaults(defineProps<Props>(), {
  variant: 'light',
  size: 'md'
});

const sizeMap = {
  sm: 80,
  md: 120,
  lg: 200
};
</script>

<template>
  <svg
    :viewBox="`0 0 ${sizeMap[size] * 3} ${sizeMap[size]}`"
    :class="['gsh-logo', `gsh-logo--${variant}`, `gsh-logo--${size}`]"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    :aria-label="`Gamified Study Hub`"
  >
    <g class="gsh-logo__mark">
      <circle
        cx="28"
        cy="28"
        r="7"
        :fill="variant === 'light' ? '#22e0d6' : '#0d9488'"
      />
      <circle
        cx="28"
        cy="28"
        r="16"
        :stroke="variant === 'light' ? '#22e0d6' : '#0d9488'"
        stroke-width="2"
        stroke-opacity="0.55"
      />
      <path
        d="M9 31C17 16 38 13 49 24"
        :stroke="variant === 'light' ? '#f8d66d' : '#b7791f'"
        stroke-width="3"
        stroke-linecap="round"
      />
      <path
        d="M8 35C21 46 41 42 50 29"
        :stroke="variant === 'light' ? '#9b5cff' : '#6d28d9'"
        stroke-width="3"
        stroke-linecap="round"
      />
      <circle cx="9" cy="31" r="3" :fill="variant === 'light' ? '#f8d66d' : '#b7791f'" />
      <circle cx="50" cy="29" r="3" :fill="variant === 'light' ? '#9b5cff' : '#6d28d9'" />
    </g>

    <g class="gsh-logo__text">
      <text
        x="54"
        y="34"
        :fill="variant === 'light' ? '#ffffff' : '#0f172a'"
        font-family="'Plus Jakarta Sans', sans-serif"
        font-size="32"
        font-weight="800"
        letter-spacing="2"
      >
        GSH
      </text>
    </g>

    <g v-if="size === 'lg'" class="gsh-logo__fullname">
      <text
        x="145"
        y="28"
        :fill="variant === 'light' ? '#ffffff' : '#0f172a'"
        font-family="'Plus Jakarta Sans', sans-serif"
        font-size="12"
        font-weight="500"
        letter-spacing="0.5"
      >
        GAMIFIED STUDY HUB
      </text>
    </g>
  </svg>
</template>

<style scoped>
.gsh-logo {
  display: inline-block;
  vertical-align: middle;
}

.gsh-logo--light .gsh-logo__text,
.gsh-logo--light .gsh-logo__mark {
  transition: opacity 0.3s ease;
}

.gsh-logo--sm {
  width: 80px;
  height: 28px;
}

.gsh-logo--md {
  width: 120px;
  height: 40px;
}

.gsh-logo--lg {
  width: 300px;
  height: 50px;
}

.gsh-logo:hover .gsh-logo__mark {
  transform: translateY(-1px) scale(1.03);
}

.gsh-logo__mark {
  transition: transform 0.3s ease;
  transform-origin: center;
}
</style>
