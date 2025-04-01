<!--
This example demonstrates handling user input with the v-on directive.
-->

<script setup>
import { ref } from 'vue'

const message = ref('Hello World!')

function reverseMessage() {
  // Access/mutate the value of a ref via
  // its .value property.
  message.value = message.value.split('').reverse().join('')
}

function notify() {
  alert('navigation was prevented.')
}
</script>

<template>
  <!--
    Note we don't need .value inside templates because
    refs are automatically "unwrapped" in templates.
  -->
  <h1>{{ message }}</h1>

  <!--
    Bind to a method/function.
    The @click syntax is short for v-on:click.
  -->
  <button @click="reverseMessage">Reverse Message</button>

  <!-- Can also be an inline expression statement -->
  <button @click="message += '!'">Append "!"</button>

  <!--
    Vue also provides modifiers for common tasks
    such as e.preventDefault() and e.stopPropagation()
  -->
  <a href="https://vuejs.org" @click.prevent="notify">
    A link with e.preventDefault()
  </a>
</template>

<style>
button, a {
  display: block;
  margin-bottom: 1em;
}
</style>