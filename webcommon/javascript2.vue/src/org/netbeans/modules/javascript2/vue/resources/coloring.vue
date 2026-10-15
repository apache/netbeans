<script setup>
import { ref } from 'vue'
const greeting = ref('Hello World!')
</script>

<template>
  <p class="greeting">{{ greeting }}</p>
  <a @[eventName]="doSomething"> ... </a>
  <form @submit.prevent="onSubmit">...</form>
  <time :title="toTitleDate(date)" :datetime="date">
    {{ formatDate(date) }}
  </time>
  <a :['foo' + bar]="value"> ... </a>
</template>

<style>
.greeting {
  color: red;
  font-weight: bold;
}
</style>
