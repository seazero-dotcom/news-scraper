<template>
  <div class="toast-host">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="t.type">
      {{ t.message }}
    </div>
  </div>
</template>

<script setup>
import { toasts } from '../lib/toast'
</script>

<style scoped>
.toast-host {
  position: fixed;
  right: 16px;
  bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 9999;
}
.toast {
  min-width: 220px;
  max-width: 360px;
  padding: 10px 14px;
  border-radius: 10px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.12);
  background: #2b2b2b;
  color: #fff;
  font-size: 14px;
}
.toast.success { background: #0ea75a; }
.toast.error   { background: #e05252; }
.toast.info    { background: #2b2b2b; }
</style>
