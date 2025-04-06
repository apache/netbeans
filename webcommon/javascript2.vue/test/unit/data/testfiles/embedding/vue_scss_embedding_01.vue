<script setup>
import { ref } from 'vue'
const greeting = ref('Hello World!')
</script>

<template>
  <p class="greeting">{{ greeting }}</p>
  <p class="footer"></p>
</template>

<style lang="scss">
    $font-stack: Helvetica, sans-serif;
    $primary-color: #333;

    p {
      font: 100% $font-stack;
      color: $primary-color;
    }
    @mixin theme($theme: DarkGray) {
      background: $theme;
      box-shadow: 0 0 1px rgba($theme, .25);
      color: #fff;
    }

    .info {
      @include theme;
    }
</style>