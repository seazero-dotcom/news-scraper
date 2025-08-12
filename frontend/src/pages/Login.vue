<template>
  <div class="card" style="max-width:480px;margin:40px auto">
    <h2>로그인</h2>
    <p class="mt8">Google 계정으로 로그인해 주세요.</p>
    <a class="btn primary mt12" :href="backendAuthUrl">Google로 로그인</a>
  </div>
</template>

<script setup>
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const backendAuthUrl = `${API_BASE}/oauth2/authorization/google`
</script>
