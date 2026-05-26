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
    :class="['npi-logo', `npi-logo--${variant}`, `npi-logo--${size}`]"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    :aria-label="`Gamified Study Hub`"
  >
    <path
      class="npi-logo__palm"
      d="M45 25C45 25 35 20 25 25C15 30 10 40 15 45L20 40C18 38 18 35 20 32C22 29 25 28 28 28C32 28 35 30 37 33C39 36 40 40 38 43L42 40C44 38 45 32 45 25Z"
      :fill="variant === 'light' ? '#c45b28' : '#c45b28'"
    />
    <path
      class="npi-logo__palm-mid"
      d="M40 20C40 20 32 15 25 20C18 25 15 32 18 38L22 34C20 32 20 30 22 28C24 26 26 25 28 25C31 25 33 27 35 29C37 31 38 34 36 38L39 36C41 35 42 30 42 25L40 20Z"
      :fill="variant === 'light' ? '#d4a24c' : '#d4a24c'"
    />

    <g class="npi-logo__text">
      <text
        x="54"
        y="34"
        :fill="variant === 'light' ? '#ffffff' : '#2c2416'"
        font-family="'Jakarta Sans', sans-serif"
        font-size="32"
        font-weight="800"
        letter-spacing="2"
      >
        GSH
      </text>
    </g>

    <g v-if="size === 'lg'" class="npi-logo__fullname">
      <text
        x="145"
        y="28"
        :fill="variant === 'light' ? '#ffffff' : '#2c2416'"
        font-family="'Jakarta Sans', sans-serif"
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
.npi-logo {
  display: inline-block;
  vertical-align: middle;
}

.npi-logo--light .npi-logo__text,
.npi-logo--light .npi-logo__palm,
.npi-logo--light .npi-logo__palm-mid {
  transition: opacity 0.3s ease;
}

.npi-logo--sm {
  width: 80px;
  height: 28px;
}

.npi-logo--md {
  width: 120px;
  height: 40px;
}

.npi-logo--lg {
  width: 300px;
  height: 50px;
}

.npi-logo:hover .npi-logo__palm {
  transform: scale(1.05);
  transform-origin: center;
  transition: transform 0.3s ease;
}

.npi-logo__palm,
.npi-logo__palm-mid {
  transition: transform 0.3s ease;
  transform-origin: center;
}
</style>
