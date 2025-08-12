<template>
  <AppHeader />
  <main class="container"><router-view /></main>
  <ToastHost />
</template>
<script setup>
import AppHeader from './components/AppHeader.vue'
import ToastHost from './components/ToastHost.vue'
</script>