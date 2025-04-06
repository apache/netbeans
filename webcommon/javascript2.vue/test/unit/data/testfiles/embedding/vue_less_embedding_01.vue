<script setup>
import { ref } from 'vue'
const greeting = ref('Hello World!')
</script>

<template>
  <p class="greeting">{{ greeting }}</p>
</template>

<style lang="less">
    //the less file that produced the above//variables
    @size: 1em;
    p{
        background-color: red;
        &.greeting {
            padding: @size;
            background-color: blue;
            a {
              padding: @size * 5
            }
        }
        &.footer {
            padding: @size * 4;
            background-color: yellow;
            a {
              text-decoration: none;
              padding: @size / 2;
            }
        }
    }
</style>